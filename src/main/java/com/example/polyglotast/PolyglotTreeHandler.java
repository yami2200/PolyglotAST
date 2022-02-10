package com.example.polyglotast;

import java.util.HashMap;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

import jsitter.api.Language;
import jsitter.api.NodeType;
import jsitter.api.Parser;
import jsitter.api.Tree;
import jsitter.api.Zipper;
import jsitter.api.Node;
import jsitter.api.StringText;
import jsitter.interop.JSitter;
import kotlin.Pair;

public class PolyglotTreeHandler {
    protected NodeType nodetype = new NodeType("source_file");
    protected String code;
    protected Parser<?> parser;
    protected Tree<?> tree;
    protected Zipper<?> cursor; // internal cursor for build and method calls; should not be used by an external
                                // class, only for methods within the tree handler
    protected HashMap<Node<?>, PolyglotTreeHandler> evalNodesToSubtreesMap;
    protected boolean insideSubtree; // indicates whether this.cursor is inside a sub tree, or still in the original
                                     // tree

    /**
     * Returns a new polyglot tree from the given code in the specified language.
     * 
     * @param code     The polyglot code to be parsed
     * @param language The language the code is written in
     */
    public PolyglotTreeHandler(String code, String language) {
        this.evalNodesToSubtreesMap = new HashMap<>();
        Language<NodeType> lang = Language.load(nodetype, language, "tree_sitter_" + language, "ts" + language,
                Language.class.getClassLoader()); // throws UnsatisfiedLinkException if the language is not installed
        lang.register(nodetype);
        this.parser = lang.parser();
        this.code = code;
        this.tree = parser.parse(new StringText(this.code), null);
        this.cursor = this.tree.getRoot().zipper();
        this.insideSubtree = false;
        buildPolyglotTree(this.cursor);
    }

    public PolyglotTreeHandler(String code) {
        this(code, "python"); // default to python parser without a specified language

    }

    /**
     * Recursively builds a polyglot tree by traversing the given subtree
     * 
     * @param zipper The root of the subtree to traverse
     */
    private void buildPolyglotTree(Zipper<?> zipper) {
        if (isPolyglotEvalCall(zipper)) {
            // handle polyglot evaluation
            makePolyglotSubtree(zipper);
        } else {
            zipper = zipper.down();
            while (zipper != null) {
                this.buildPolyglotTree(zipper);
                zipper = zipper.right();
            }
        }

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
        Zipper<?> node = null;
        switch (this.parser.getLanguage().getName()) {
            case "python":
                Zipper<?> arg1 = zipper.down().right().down().right().down(); // this selects the keyword identifier
                                                                              // (which is required) of the first
                                                                              // argument
                Zipper<?> arg2 = zipper.down().right().down().right().right().right().down(); // same thing, but for the
                                                                                              // second
                // argument
                switch (this.nodeToCode(arg1)) {
                    case "language":
                        newLang = this.nodeToCode(arg1.right().right());
                        newLang = newLang.substring(1, newLang.length() - 1);
                        System.out.println(newLang);
                        break;
                    case "path": // TODO : load source code from a given file
                        break;
                    case "string": // TODO : handle identifier-passed strings
                        node = arg1.right().right();
                        subProgram = this.nodeToCode(node);
                        subProgram = subProgram.substring(1, subProgram.length() - 1);
                        System.out.println(subProgram);
                        break;
                    default:
                        System.out.println("Something went wrong : " + zipper.getType().getName()
                                + " node was recognized as an eval node, but the subsequent nodes dont match known eval arguments");
                        break;
                }

                switch (this.nodeToCode(arg2)) {
                    case "language":
                        newLang = this.nodeToCode(arg2.right().right());
                        newLang = newLang.substring(1, newLang.length() - 1);
                        System.out.println(newLang);
                        break;
                    case "path": // TODO : load source code from a given file
                        break;
                    case "string": // TODO : handle identifier-passed strings
                        node = arg2.right().right();

                        subProgram = this.nodeToCode(node);

                        subProgram = subProgram.substring(1, subProgram.length() - 1);
                        System.out.println(subProgram);
                        break;
                    default:
                        System.out.println("Something went wrong : " + zipper.getType().getName()
                                + " node was recognized as an eval node, but the subsequent nodes dont match known eval arguments");

                        break;
                }

                break;
            case "javascript":
                switch (this.nodeToCode(zipper.down().down().right().right())) { // file or string-based eval call
                    case "eval":
                        newLang = this.nodeToCode(zipper.down().right().down().right());
                        newLang = newLang.substring(1, newLang.length() - 1);

                        node = zipper.down().right().down().right().right().right();
                        switch (node.getType().getName()) {
                            case "string":
                                subProgram = this.nodeToCode(node);
                                subProgram = subProgram.substring(1, subProgram.length() - 1);
                                break;
                            case "identifier": // TODO : handle variable-passed argument
                                break;
                            default:
                                break;
                        }

                        break;
                    case "evalFile": // TODO : support reading code from files
                        break;
                    default:
                        break;
                }
                break;
            default:
                throw new AssertionError();
        }
        assert !newLang.equals("") && node != null;
        newLang = mapTSLangToGraalLang(newLang);
        PolyglotTreeHandler newSubTree = new PolyglotTreeHandler(subProgram, newLang);
        this.evalNodesToSubtreesMap.put(zipper.getNode(), newSubTree);
        assert this.evalNodesToSubtreesMap.containsKey(node.getNode());
        System.out.println("Added new polyglot transition on node " + node.getNode().hashCode());
    }

    /**
     * Determines whether or not a given node is a polyglot eval function call
     * 
     * @param node The node to be checked
     * @return True if the node is an eval function call in the language of this
     *         program
     */
    protected boolean isPolyglotEvalCall(Zipper<?> node) {
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
     * @param node The polyglot zipper of the node to be checked
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
     * @param node The polyglot zipper of the node to be checked
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

    public Zipper<?> getCurrentCursor() { // TODO : return a special "polyglot node" if the cursor is an eval, import or
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
    protected String nodeToCode(Zipper<?> node) {
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
        for(int i = 0; i < pos; i++) {
            if(this.code.charAt(i) == '\n') {
                lineCount++;
                offset = 0;
            } else {
                offset++;
            }
        }
        return new Pair<>(lineCount, offset);
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
