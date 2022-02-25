package com.example.polyglotast;

import jsitter.api.Language;
import jsitter.api.NodeType;
import jsitter.api.Parser;
import jsitter.api.Tree;
import jsitter.api.Zipper;
import jsitter.api.Node;
import jsitter.api.StringText;
import jsitter.interop.JSitter;
import kotlin.Pair;


public class PolyglotZipper {

    protected PolyglotTreeHandler currentTree;
    protected Zipper<?> node;

    public PolyglotZipper(PolyglotTreeHandler tree, Zipper<?> node) {
        this.currentTree = tree;
        this.node = node;
    }

    public PolyglotZipper(PolyglotTreeHandler tree) {
        this(tree, tree.getRoot());
    }

    public PolyglotZipper(PolyglotTreeHandler tree, Node<?> node) {
        this.currentTree = tree;
        this.node = node.zipper();
    }

    // TODO : handle going back to parent polyglot
    public PolyglotZipper up() {
        return new PolyglotZipper(this.currentTree, this.node.up());
    }

    // TODO : handle going back to parent polyglot
    public void goUp() {
        this.node = node.up();
    }

    public PolyglotZipper left() {
        return new PolyglotZipper(this.currentTree, this.node.left());
    }

    public void goLeft() {
        this.node = node.left();
    }

    public PolyglotZipper right() {
        return new PolyglotZipper(this.currentTree, this.node.right());
    }

    public void goRight() {
        this.node = node.right();
    }

    public PolyglotZipper down() {
        if (this.currentTree.evalNodesToSubtreesMap.containsKey(this.node.getNode())) {
            return new PolyglotZipper(this.currentTree.evalNodesToSubtreesMap.get(this.node.getNode()));
        } else {
            return new PolyglotZipper(this.currentTree, this.node.down());
        }
    }

    public void goDown() {
        if (this.currentTree.evalNodesToSubtreesMap.containsKey(this.node.getNode())) {
            this.currentTree = this.currentTree.evalNodesToSubtreesMap.get(this.node.getNode());
            this.node = this.currentTree.getRoot();
        } else {
            this.node = this.node.down();
        }
    }

    public Node<?> getNode() {
        return this.node.getNode();
    }
    public Pair<Integer, Integer> getPosition() {
        return this.currentTree.getNodePosition(this.node);
    }

    public String getType() {
        if (this.currentTree.isPolyglotEvalCall(this)) {
            return "polyglot_eval_call";
        }
        if (this.currentTree.isPolyglotImportCall(this)) {
            return "polyglot_import_call";
        }
        if (this.currentTree.isPolyglotExportCall(this)) {
            return "polyglot_export_call";
        }
        return this.node.getType().getName();
    }

    public boolean isEval() {
        return this.currentTree.isPolyglotEvalCall(this);
    }

    public boolean isImport() {
        return this.currentTree.isPolyglotImportCall(this);
    }

    public boolean isExport() {
        return this.currentTree.isPolyglotExportCall(this);
    }

    public String getLang() {
        return this.currentTree.getLang();
    }

    public String getBindingName() {
        if(this.isImport() || this.isExport()) {
            switch (this.getLang()) {
                case "python":
                    return this.currentTree.nodeToCode(this.node.down().right().down().right());
                case "javascript":

                    break;
                default:
                    throw new AssertionError();
            }
            return "";
        } else {
            return null;
        }
    }

    public String getCode() {
        return this.currentTree.nodeToCode(this.node);
    }

    public int getPos() {
        return this.node.getByteOffset() / 2;
    }

    public int getLength() {
        return this.node.getByteSize() / 2;
    }

    public boolean isNull() {
        return this.node == null;
    }

    @Override
    public String toString() {
        return this.node.toSexp();
    }

}