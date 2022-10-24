package com.example.polyglotast;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import com.example.polyglotast.utils.FileNotFoundInfo;
import com.example.polyglotast.utils.NodePosition;
import jsitter.api.*;
import kotlin.Pair;

import javax.swing.text.Position;

public class PolyglotTreeHandler {
    protected NodeType nodetype = new NodeType("source_file");
    protected Path pathPrefix = null;
    protected String code;
    protected Parser<NodeType> parser;
    protected Tree<NodeType> tree;

    protected Zipper<?> cursor; // internal cursor for build and method calls; should not be used by an external
                                // class, only for methods within the tree handler
    protected HashMap<Node<?>, PolyglotTreeHandler> evalNodesToSubtreesMap;
    protected HashSet<PolyglotTreeHandler> directParents;

    protected static HashMap<PolyglotTreeHandler, Path> filePathOfTreeHandler = new HashMap<>();
    protected static HashMap<Path, PolyglotTreeHandler> filePathToTreeHandler = new HashMap<>();

    private HashSet<FileNotFoundInfo> filesNotFound = new HashSet<>();

    protected boolean insideSubtree; // indicates whether this.cursor is inside a sub tree, or still in the original
                                     // tree

    public void clearTreeHandlerPathAndInstance(){
        filePathOfTreeHandler.clear();
        filePathToTreeHandler.clear();
    }

    public HashSet<FileNotFoundInfo> getFilesNotFound() {
        return filesNotFound;
    }

    public static HashMap<PolyglotTreeHandler, Path> getfilePathOfTreeHandler(){
        return filePathOfTreeHandler;
    }

    public static HashMap<Path, PolyglotTreeHandler> getfilePathToTreeHandler(){
        return filePathToTreeHandler;
    }

    /**
     * Return a set of all PolyglotTreeHandler that host this tree (is a root of this tree)
     * @return set of all PolyglotTreeHandler that host this tree
     */
    public HashSet<PolyglotTreeHandler> getHostTrees(){
        HashSet<PolyglotTreeHandler> results = new HashSet<>();
        HashSet<PolyglotTreeHandler> visited = new HashSet<>();
        visited.add(this);
        for (PolyglotTreeHandler parentTree : this.directParents) {
            results.addAll(parentTree.getHostTrees(visited));
        }
        if(results.size() == 0) results.add(this);
        return results;
    }

    private HashSet<PolyglotTreeHandler> getHostTrees(HashSet<PolyglotTreeHandler> visited){
        HashSet<PolyglotTreeHandler> results = new HashSet<>();
        visited.add(this);
        if(this.directParents.size() == 0){
            results.add(this);
        } else {
            for (PolyglotTreeHandler parentTree : this.directParents) {
                if(visited.contains(parentTree)){
                    results.add(parentTree);
                } else {
                    results.addAll(parentTree.getHostTrees(visited));
                }
            }
        }
        return results;
    }

    /**
     * Return a set of all PolyglotTreeHandler that are directly or not directly subtrees to this tree
     * @returnset of all PolyglotTreeHandler that are directly or not directly subtrees to this tree
     */
    public HashSet<PolyglotTreeHandler> getSubTrees(){
        HashSet<PolyglotTreeHandler> results = new HashSet<>();
        HashSet<PolyglotTreeHandler> visited = new HashSet<>();
        visited.add(this);
        for (PolyglotTreeHandler subTree : this.evalNodesToSubtreesMap.values()) {
            if(subTree != this) results.addAll(subTree.getSubTrees(visited));
        }
        return results;
    }

    private HashSet<PolyglotTreeHandler> getSubTrees(HashSet<PolyglotTreeHandler> visited){
        HashSet<PolyglotTreeHandler> results = new HashSet<>();
        visited.add(this);
        results.add(this);
        for (PolyglotTreeHandler subTree : this.evalNodesToSubtreesMap.values()) {
            if(!visited.contains(subTree)) results.addAll(subTree.getSubTrees(visited));
        }
        return results;
    }

    /**
     * Returns a new polyglot tree from the given code in the specified language.
     * 
     * @param code     The polyglot code to be parsed
     * @param language The language the code is written in
     */
    public PolyglotTreeHandler(String code, String language) {
        this(code, language, null);
    }

    public PolyglotTreeHandler(String code) {
        this(code, "python"); // default to python parser without a specified language

    }
    public PolyglotTreeHandler(Path fileName) throws IOException {
        this(fileName, "python");
    }

    public PolyglotTreeHandler(Path fileName, String language) throws IOException {
        PolyglotTreeHandler.filePathOfTreeHandler.put(this, fileName);
        PolyglotTreeHandler.filePathToTreeHandler.put(fileName, this);

        this.initPolyglotTree(Files.readString(fileName), language, fileName.getParent());
    }

    public PolyglotTreeHandler(String code, String language, Path prefix) {
        this.initPolyglotTree(code, language, prefix);
    }

    private void initPolyglotTree(String code, String language, Path prefix){
        this.pathPrefix = prefix;
        this.evalNodesToSubtreesMap = new HashMap<>();
        this.directParents = new HashSet<>();

        Language<NodeType> lang = null;
        try {
            try {
                lang = Language.load(nodetype, language, "tree_sitter_" + language, "ts" + language,
                        Language.class.getClassLoader()); // throws UnsatisfiedLinkException if the language is not
                // installed
            } catch (RuntimeException ule) {
                lang = Language.load(nodetype, language, "tree_sitter_" + language, "libts" + language,
                        Language.class.getClassLoader()); // throws UnsatisfiedLinkException if the language is not
                // installed
            }
        } catch (UnsatisfiedLinkError ule) {
            System.err.println("Language " + language + " is not installed.");
        } finally {
            if (lang != null) {
                lang.register(nodetype);
                this.parser = lang.parser();
                this.code = code;
                this.tree = parser.parse(new StringText(this.code), null);
                this.cursor = this.tree.getRoot().zipper();
                this.insideSubtree = false;
                buildPolyglotTree(this.cursor);
            }
        }
    }

    /**
     * Recursively builds a polyglot tree by traversing the given subtree
     * 
     * @param zipper The root of the subtree to traverse
     */
    private void buildPolyglotTree(Zipper<?> zipper) {
        if (isPolyglotEvalCall(zipper)) {
            makePolyglotSubtree(zipper);
        } else {
            zipper = zipper.down();
            while (zipper != null) {
                this.buildPolyglotTree(zipper);
                zipper = zipper.right();
            }
        }

    }

    /**
     * Reparse the tree with a new code (used when a changed has been made to the code of this tree)
     * @param newcode the code to parse for that tree
     */
    public void reparsePolyglotTree(String newcode){
        this.code = newcode;
        this.tree = parser.parse(new StringText(newcode), null);
        this.cursor = this.tree.getRoot().zipper();

        this.insideSubtree = false;
        this.filesNotFound.clear();
        this.clearLinkSubTrees();
        this.buildPolyglotTree(this.cursor);
    }

    /*
        INCREMENTAL PARSING OF TREE
        //TO-FIX : The parsing is still very slow on big files
     */
    public void reparsePolyglotTree(Pair<Integer, Integer> start, Pair<Integer, Integer> end, String text){
        ArrayList<Edit> edits = new ArrayList<>();
        Pair<Integer, Integer> startBytePos = getBytesAndCharacterLengthAtPosition(start.component1(), start.component2());
        Pair<Integer, Integer> endBytePos = getBytesAndCharacterLengthAtPosition(end.component1(), end.component2());
        String newCode = this.code.substring(0, startBytePos.component2()) + text + this.code.substring(endBytePos.component2());
        Edit edit = new Edit(startBytePos.component1(), endBytePos.component1(), startBytePos.component1()+text.getBytes(StandardCharsets.UTF_8).length-1);
        edits.add(edit);

        this.tree = this.tree.adjust(edits);
        this.code = newCode;
        this.tree = parser.parse(new StringText(newCode), this.tree);

        this.cursor = this.tree.getRoot().zipper();
        this.insideSubtree = false;
        this.filesNotFound.clear();
        this.clearLinkSubTrees();
        this.buildPolyglotTree(this.cursor);
    }

    private Pair<Integer, Integer> getBytesAndCharacterLengthAtPosition(int line, int chara){
        int pos = 0;
        int charp = 0;
        String[] lines = this.code.split("(?<=\r\n|\r|\n)");
        if(line>lines.length) return new Pair(-1, -1);
        for(int i = 0; i< line; i++){
            pos += (lines[i]).getBytes(StandardCharsets.UTF_8).length;
            charp += (lines[i]).length();
        }
        if(line==lines.length) return new Pair(pos, charp);
        if(chara > lines[line].length()) return new Pair(-1, -1);
        pos += lines[line].substring(0, chara).getBytes(StandardCharsets.UTF_8).length;
        charp += chara;
        return new Pair(pos, charp);
    }

    public void clearLinkSubTrees(){
        for (PolyglotTreeHandler subTree : this.evalNodesToSubtreesMap.values()) {
            subTree.directParents.remove(this);
        }
        this.evalNodesToSubtreesMap.clear();
    }

    /****************
     * The following four functions are the ones required to extend to add language
     * support
     ************************/
    /****************
     * To add another language, add the relevant tree-sitter library in the
     * resources folder
     ***********************/
    /****************
     * and implement new cases for the language to spot polyglot functions and build
     * subtrees
     **********************/

    /**
     * From a polyglot eval function node, builds the corresponding secondary AST
     * and adds it to the subtrees map.
     * 
     * @param zipper The eval function node
     */
    private void makePolyglotSubtree(Zipper<?> zipper) { // TW : nested switches ahead
        String newLang = "";
        String subProgram = "";
        Path newPathPrefix = null;
        Path fileName = null;
        Zipper<?> node = null;
        switch (this.parser.getLanguage().getName()) {
            // first level of cases identify which language we're working with; this is a
            // property of the AST itself
            case "python": // TODO : handle identifier-passed strings
                ArrayList<Zipper<?>> args = new ArrayList<>();
                args.add(zipper.down().right().down().right().down());  // this selects the keyword identifier
                                                                        // (which is required) of the first
                                                                        // argument
                args.add(zipper.down().right().down().right().right().right().down());  // same thing, but for the
                                                                                        // second
                // Python specifies arguments with their names, so we need to check for each one
                // with all possibilities in mind

                for (Zipper<?> arg : args) {
                    switch (this.nodeToCode(arg)) {
                        case "language":
                            newLang = this.nodeToCode(arg.right().right());
                            newLang = newLang.substring(1, newLang.length() - 1);
                            break;
                        case "path":
                            node = arg.right().right();
                            String path = this.nodeToCode(node);
                            if (this.pathPrefix != null) { // check if we have a relative directory to work from
                                fileName = this.pathPrefix.resolve(path.substring(1, path.length() - 1));
                            } else {
                                fileName = Path.of(path.substring(1, path.length() - 1));
                            }
                            newPathPrefix = fileName.getParent(); // retrieve next relative directory
                            try {
                                subProgram = Files.readString(fileName);
                            } catch (IOException e) {
                                PolyglotZipper zip = new PolyglotZipper(this, arg);
                                Integer end_char = zip.getPosition().component2()+1;
                                Integer end_line = zip.getPosition().component1()+1;
                                try{
                                    end_char = zip.right().right().down().right().getPosition().component2()+1;
                                    end_line = zip.right().right().down().right().getPosition().component1();
                                } catch (Exception ex) {
                                    System.err.println("Error : Attempting to parse charcter position of evalFile file name");
                                }
                                filesNotFound.add(new FileNotFoundInfo(fileName.getFileName().toString(), zip.getPosition().component2(), zip.getPosition().component1(), end_char, end_line,"python"));
                                System.err.println("Attempting to read a polyglot subfile " + fileName.toString()
                                        + " that does not exist; corresponding subtree will be empty");
                                return;
                            }
                            break;
                        case "string":
                            node = arg.right().right();
                            subProgram = this.nodeToCode(node);
                            subProgram = subProgram.substring(1, subProgram.length() - 1);
                            break;
                        default: // we dont throw an exception to let the framework attempt to build the rest of
                            // the tree
                            System.err.println("Something went wrong : " + zipper.getType().getName()
                                    + " node was recognized as an eval node, but the subsequent nodes dont match known eval arguments");
                    }
                }

                break;

            case "javascript": // TODO : handle identifier-passed strings
                // JS uses different subfunctions for code execution, so
                // we can check whether this is a file or string based eval call
                switch (this.nodeToCode(zipper.down().down().right().right())) {
                    case "eval":
                        newLang = this.nodeToCode(zipper.down().right().down().right());
                        newLang = newLang.substring(1, newLang.length() - 1);

                        node = zipper.down().right().down().right().right().right();
                        switch (node.getType().getName()) {
                            case "string":
                                subProgram = this.nodeToCode(node);
                                subProgram = subProgram.substring(1, subProgram.length() - 1);
                                break;
                            case "identifier": // TODO : handle identifier-passed strings
                                break;
                            default:
                                break;
                        }

                        break;
                    case "evalFile":
                        newLang = this.nodeToCode(zipper.down().right().down().right());
                        newLang = newLang.substring(1, newLang.length() - 1);

                        node = zipper.down().right().down().right().right().right();
                        switch (node.getType().getName()) {
                            case "string":
                                String path = this.nodeToCode(node);
                                if (this.pathPrefix != null) { // check if we have a relative directory to work from
                                    fileName = this.pathPrefix.resolve(path.substring(1, path.length() - 1));
                                } else {
                                    fileName = Path.of(path.substring(1, path.length() - 1));
                                }
                                newPathPrefix = fileName.getParent(); // retrieve next relative directory
                                try {
                                    subProgram = Files.readString(fileName);
                                } catch (IOException e) {
                                    PolyglotZipper zip = new PolyglotZipper(this, node);
                                    Integer end_char = zip.getPosition().component2()+1;
                                    Integer end_line = zip.getPosition().component1()+1;
                                    try{
                                        end_char = zip.down().right().right().getPosition().component2()+1;
                                        end_line = zip.down().right().right().getPosition().component1();
                                    } catch (Exception ex) {
                                        System.err.println("Error : Attempting to parse charcter position of evalFile file name");
                                    }
                                    filesNotFound.add(new FileNotFoundInfo(fileName.getFileName().toString(), zip.getPosition().component2(), zip.getPosition().component1(), end_char, end_line, "javascript"));
                                    System.err.println("Attempting to read a polyglot subfile " + fileName.toString()
                                                    + " that does not exist; corresponding subtree will be empty");
                                    return;
                                }
                                break;
                            case "identifier": // TODO : handle identifier-passed strings
                                break;
                            default:
                                break;
                        }
                        break;
                    default:
                        break;
                }
                break;
            default:
                throw new AssertionError();
        }

        assert !newLang.equals("") && node != null; // check everything went right
        newLang = mapTSLangToGraalLang(newLang);
        PolyglotTreeHandler newSubTree;
        if(fileName == null || !filePathToTreeHandler.containsKey(fileName)){
            if (newPathPrefix != null) {
                newSubTree = new PolyglotTreeHandler("", newLang, newPathPrefix);
            } else {
                newSubTree = new PolyglotTreeHandler("", newLang, this.pathPrefix);
            }
            if(fileName != null){
                PolyglotTreeHandler.filePathOfTreeHandler.put(newSubTree, fileName);
                PolyglotTreeHandler.filePathToTreeHandler.put(fileName, newSubTree);
            }
            this.addLinkToSubtree(zipper, newSubTree);
            newSubTree.reparsePolyglotTree(subProgram);
        } else {
            newSubTree = filePathToTreeHandler.get(fileName);
            this.addLinkToSubtree(zipper, newSubTree);
        }
        //assert this.evalNodesToSubtreesMap.containsKey(node.getNode());
    }

    public void addLinkToSubtree(Zipper<?> zipper, PolyglotTreeHandler tree){
        this.evalNodesToSubtreesMap.put(zipper.getNode(), tree);
        tree.directParents.add(this);
    }

    /**
     * Determines whether or not a given node is a polyglot eval function call
     * 
     * @param node The node to be checked
     * @return True if the node is an eval function call in the language of this
     *         program
     */
    protected boolean isPolyglotEvalCall(Zipper<?> node) {
        if(node == null || node.down() == null) return false;
        switch (this.parser.getLanguage().getName()) {
            case "python":
                return node.getType().getName().equals("call") && node.down().getType().getName().equals("attribute")
                        && nodeToCode(node.down()).equals("polyglot.eval");
            case "javascript":
                return node.getType().getName().equals("call_expression")
                        && node.down().getType().getName().equals("member_expression")
                        && (nodeToCode(node.down()).equals("Polyglot.eval")
                                || nodeToCode(node.down()).equals("Polyglot.evalFile"));
            default:
                return false;
        }
    }

    /**
     * Determines whether or not a given node is a polyglot eval function call
     * 
     * @param zip The polyglot zipper of the node to be checked
     * @return True if the node is an eval function call in the language of this
     *         program
     */
    protected boolean isPolyglotEvalCall(PolyglotZipper zip) {
        return this.isPolyglotEvalCall(zip.node);
    }

    /**
     * Determines whether or not a given node is a polyglot import function call
     * 
     * @param node The node to be checked
     * @return True if the node is an import function call in the language of this
     *         program
     */
    public boolean isPolyglotImportCall(Zipper<?> node) {
        if(node == null || node.down() == null) return false;
        switch (this.parser.getLanguage().getName()) {
            case "python":
                return node.getType().getName().equals("call") && node.down().getType().getName().equals("attribute")
                        && nodeToCode(node.down()).equals("polyglot.import_value");
            case "javascript":
                return node.getType().getName().equals("call_expression")
                        && node.down().getType().getName().equals("member_expression")
                        && (nodeToCode(node.down()).equals("Polyglot.import"));
            default:
                return false;
        }
    }

    /**
     * Determines whether or not a given node is a polyglot import function call
     * 
     * @param zip The polyglot zipper of the node to be checked
     * @return True if the node is an import function call in the language of this
     *         program
     */
    public boolean isPolyglotImportCall(PolyglotZipper zip) {
        return this.isPolyglotImportCall(zip.node);
    }

    /**
     * Determines whether or not a given node is a polyglot export function call
     * 
     * @param node The node to be checked
     * @return True if the node is an export function call in the language of this
     *         program
     */
    public boolean isPolyglotExportCall(Zipper<?> node) {
        if(node == null || node.down() == null) return false;
        switch (this.parser.getLanguage().getName()) {
            case "python":
                return node.getType().getName().equals("call") && node.down().getType().getName().equals("attribute")
                        && nodeToCode(node.down()).equals("polyglot.export_value");
            case "javascript":
                return node.getType().getName().equals("call_expression")
                        && node.down().getType().getName().equals("member_expression")
                        && (nodeToCode(node.down()).equals("Polyglot.export"));
            default:
                return false;
        }
    }

    /**
     * Determines whether or not a given node is a polyglot export function call
     * 
     * @param zip The polyglot zipper of the node to be checked
     * @return True if the node is an export function call in the language of this
     *         program
     */
    public boolean isPolyglotExportCall(PolyglotZipper zip) {
        return this.isPolyglotExportCall(zip.node);
    }

    /**
     * Simple function for the mapping of tree-sitter language identifiers to
     * GraalVM language identifiers.
     * 
     * @param tslang the tree-sitter language library identifier
     * @return The GraalVM eval function identifier for the same language
     */
    protected static String mapTSLangToGraalLang(String tslang) {
        String result = tslang;
        switch (tslang) {
            case "js":
                result = "javascript";
            default:
                break;
        }
        return result;
    }

    // TODO : hide internal tree cursor methods and replace with the PolyglotZipper
    // interface

    public Zipper<?> getRoot() {
        return this.tree.getRoot().zipper();
    }

    public Zipper<?> goToRoot() {
        this.evalNodesToSubtreesMap.values().forEach(t -> { // reset cursor for all subtrees as well
            t.goToRoot();
        });
        this.cursor = this.tree.getRoot().zipper();
        return this.cursor;
    }

    // TODO : return a special "polyglot node" if the cursor is an eval, import or
    public Zipper<?> getCurrentCursor() {
        // export function call (mark it or new inherited class ?)
        if (this.insideSubtree) {
            return this.getCurrentSubtree().getCurrentCursor();
        }
        return this.cursor;
    }

    protected void setCursor(Zipper<?> nextCursor) {
        if (this.insideSubtree) {
            this.getCurrentSubtree().setCursor(nextCursor);
        } else {
            this.cursor = nextCursor;
        }
    }

    // TODO : handle going back to parent polyglot
    public Zipper<?> up() {
        return this.cursor.up();
    }

    // TODO : handle going back to parent polyglot
    public Zipper<?> goUp() {
        this.cursor = this.cursor.up();
        return this.cursor;
    }

    public Zipper<?> down() {
        if (this.evalNodesToSubtreesMap.containsKey(this.cursor.getNode())) {
            PolyglotTreeHandler subtree = this.getCurrentSubtree();
            if (this.insideSubtree) {
                return subtree.down();
            } else {
                return subtree.goToRoot();
            }
        }
        return this.cursor.down();
    }

    public Zipper<?> goDown() {
        if (this.evalNodesToSubtreesMap.containsKey(this.cursor.getNode())) {
            PolyglotTreeHandler subtree = this.getCurrentSubtree();
            if (this.insideSubtree) {
                return subtree.goDown();
            } else {
                this.insideSubtree = true;
                return subtree.goToRoot();
            }
        } else {
            this.cursor = this.cursor.down();
        }
        return this.cursor;
    }

    public Zipper<?> right() {
        if (insideSubtree) {
            return this.getCurrentSubtree().right();
        }
        return this.cursor.right();
    }

    public Zipper<?> goRight() {
        if (insideSubtree) {
            return this.getCurrentSubtree().goRight();
        }
        this.cursor = this.cursor.right();
        return this.cursor;
    }

    public Zipper<?> left() {
        if (insideSubtree) {
            return this.getCurrentSubtree().left();
        }
        return this.cursor.left();
    }

    public Zipper<?> goLeft() {
        if (insideSubtree) {
            return this.getCurrentSubtree().goLeft();
        }
        this.cursor = this.cursor.left();
        return this.cursor;
    }

    public Zipper<?> downRight() {
        if (insideSubtree) {
            return this.getCurrentSubtree().downRight();
        }
        return this.cursor.downRight();
    }

    public Zipper<?> goDownRight() {
        if (insideSubtree) {
            return this.getCurrentSubtree().goDownRight();
        }
        this.cursor = this.cursor.downRight();
        return this.cursor;
    }

    public PolyglotZipper getRootZipper() {
        return new PolyglotZipper(this);
    }

    /**
     * Returns the code string for a given node if it is in the tree. If given a
     * node not present in the tree, it will return an incoherent string.
     * 
     * @param node The node to return corresponding code of
     * @return The code that gave this node when it was parsed
     */
    public String nodeToCode(Zipper<?> node) {
        int pos = node.getByteOffset() / 2;
        int length = node.getByteSize() / 2;
        String label = code.substring(pos, pos + length);
        return label;
    }

    protected Pair<Integer, Integer> getNodePosition(Zipper<?> node) {
        int pos = node.getByteOffset() / 2;
        int length = node.getByteSize() / 2;
        int lineCount = 0;
        int offset = 0;
        for (int i = 0; i < pos; i++) {
            if (this.code.charAt(i) == '\n') {
                lineCount++;
                offset = 0;
            } else {
                offset++;
            }
        }
        return new Pair<>(lineCount, offset);
    }

    public NodePosition getNodePositionForMultipleRequest(Zipper<?> node, int previous_line, int previous_line_char) {
        int pos = node.getByteOffset() / 2;
        int length = node.getByteSize() / 2;
        NodePosition result = new NodePosition();
        int lineCount = previous_line;
        int offset = 0;
        int i = 0;
        for (i = previous_line_char; i < pos; i++) {
            if (this.code.charAt(i) == '\n') {
                lineCount++;
                offset = 0;
            } else {
                offset++;
            }
        }
        result.previous_line = lineCount;
        result.previous_line_char = i - offset;
        result.position = new Pair<>(lineCount, offset);
        return result;
    }

    public Zipper<?> getNodeAtPosition(Pair<Integer, Integer> position){
        return getNodeAtPosition(position, this.getRoot());
    }

    protected Zipper<?> getNodeAtPosition(Pair<Integer, Integer> position, Zipper<?> root){
        Zipper<?> current = root;
        Pair<Integer, Integer> currentPos = getNodePosition(root);
        if(isPositionBeforeOrEqual(currentPos, position)){
            if(current.right() != null){
                Pair<Integer, Integer> newPos = getNodePosition(current.right());
                while(isPositionBeforeOrEqual(newPos, position)){
                    current = current.right();
                    if(current.right() == null){
                        if(current.down() == null){
                            if(newPos.component2() + this.nodeToCode(current).length() > position.component2()) {return current;}
                            return null;
                        }
                        return getNodeAtPosition(position, current.down());
                    }
                    newPos = getNodePosition(current.right());
                }
                if(current.down() == null){
                    if(getNodePosition(current).component2() + this.nodeToCode(current).length() > position.component2()){ return current; }
                    if(current.up() != null && getNodePosition(current.up()).component2() + this.nodeToCode(current.up()).length() > position.component2()) { return current.up();};
                    return null;
                }
                return getNodeAtPosition(position, current.down());
            } else if(current.down() == null) {

                if(currentPos.component2() + this.nodeToCode(current).length() > position.component2()) {
                    return current;
                }
                return null;
            }
            else return getNodeAtPosition(position, root.down());
        }
        return null;
    }

    private boolean isPositionBeforeOrEqual(Pair<Integer, Integer> pos1, Pair<Integer, Integer> pos2){
        return pos1.component1() < pos2.component1() || (pos1.component1().equals(pos2.component1()) && pos1.component2() <= pos2.component2());
    }

    // TODO : option for AST instead of CST ? (hide tokens)
    public String treeToString() {
        Zipper<?> previousLocation = this.cursor; // We save these so that we can
        boolean previousLocBool = this.insideSubtree; // use the cursor freely and restore it to the user
        this.goToRoot();
        String result = treeToStringImplem("");
        this.cursor = previousLocation;
        this.insideSubtree = previousLocBool;
        return result;
    }

    public String treeToString(String indent) {
        Zipper<?> previousLocation = this.cursor; // We save these so that we can
        boolean previousLocBool = this.insideSubtree; // use the cursor freely and restore it to the user
        this.goToRoot();
        String result = treeToStringImplem(indent);
        this.cursor = previousLocation;
        this.insideSubtree = previousLocBool;
        return result;
    }

    protected String treeToStringImplem(String indent) {
        String result = indent;
        if (this.isPolyglotEvalCall(this.cursor)) {
            result += "polyglot_eval_call";
        } else {
            result += this.getCurrentCursor().getType().getName();
        }
        if (this.down() == null) { // if this is a leaf, display the code of it and dont go down any more
            if (this.insideSubtree) {
                result += " : " + this.getCurrentSubtree().nodeToCode(this.getCurrentCursor()) + "\n";
            } else {
                result += " : " + this.nodeToCode(this.getCurrentCursor()) + "\n";
            }
        } else {
            result += "\n";
            this.goDown();
            Zipper<?> nextNode = this.getCurrentCursor();
            boolean nextLocation = this.insideSubtree;
            while (nextNode != null) {
                this.setCursor(nextNode);
                nextNode = this.right();
                if (insideSubtree) {
                    result += this.getCurrentSubtree().treeToStringImplem(indent + "|   ");
                } else {
                    result += this.treeToStringImplem(indent + "|   ");
                }
                this.insideSubtree = nextLocation;
            }

        }
        return result;

    }

    public String cursorToString() {
        if (this.insideSubtree) {
            return this.getCurrentSubtree().cursorToString();
        }
        return this.nodeToCode(this.cursor);
    }

    protected PolyglotTreeHandler getCurrentSubtree() {
        return this.evalNodesToSubtreesMap.get(this.cursor.getNode());
    }

    public void apply(PolyglotTreeProcessor processor) {
        processor.process(this.getRootZipper());
    }

    public String getLang() {
        return this.parser.getLanguage().getName();
    }

}
