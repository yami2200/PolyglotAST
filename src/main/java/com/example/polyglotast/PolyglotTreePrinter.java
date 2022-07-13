package com.example.polyglotast;

import java.util.HashSet;

public class PolyglotTreePrinter implements PolyglotTreeProcessor {

    private final String indent;
    private String result;

    public PolyglotTreePrinter() {
        this.indent = "| ";
        this.result = "";
    }

    public PolyglotTreePrinter(String indent) {
        this.indent = indent;
        this.result = "";
    }

    public String getRes() {
        return this.result;
    }

    @Override
    public void process(PolyglotZipper zipper) {
        this.process(zipper, new HashSet<PolyglotTreeHandler>());
    }

    public void process(PolyglotZipper zipper, HashSet<PolyglotTreeHandler> alreadyVisited){
        if(!alreadyVisited.contains(zipper.getCurrentTree())){
            alreadyVisited.add(zipper.getCurrentTree());
        }
        this.result = "";
        if (zipper.down().isNull()) {
            this.result += this.indent + zipper.getType() + " : " + zipper.getCode() + "(" + zipper.getPosition() + ")\n";
            return;
        } else {
            this.result += this.indent + zipper.getType() + "\n";
        }
        PolyglotZipper next = zipper.down();
        while (!next.isNull() && !(next.getCurrentTree() != zipper.getCurrentTree() && alreadyVisited.contains(next.getCurrentTree()))) {
            PolyglotTreePrinter nextp = new PolyglotTreePrinter("|  " + this.indent);
            nextp.process(next, alreadyVisited);
            this.result += nextp.getRes();
            next = next.right();
        }
    }

}
