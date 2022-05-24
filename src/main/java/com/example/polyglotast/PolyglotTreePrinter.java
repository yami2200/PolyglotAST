package com.example.polyglotast;

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
        if (zipper.down().isNull()) {
            this.result += this.indent + zipper.getType() + " : " + zipper.getCode() + "(" + zipper.getPosition() + ")\n";
            return;
        } else {
            this.result += this.indent + zipper.getType() + "\n";
        }
        PolyglotZipper next = zipper.down();
        while (!next.isNull()) {
            PolyglotTreePrinter nextp = new PolyglotTreePrinter("|  " + this.indent);
            nextp.process(next);
            this.result += nextp.getRes();
            next = next.right();
        }
    }

}
