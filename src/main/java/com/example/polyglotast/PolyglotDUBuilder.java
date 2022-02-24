package com.example.polyglotast;

import java.util.HashMap;
import java.util.LinkedList;
//import jdk.jfr.Name;
import kotlin.Pair;

public class PolyglotDUBuilder implements PolyglotTreeProcessor {

    private HashMap<String, LinkedList<Pair<Integer, Integer>>> imports;
    private HashMap<String, Pair<Integer, Integer>> exports;

    public PolyglotDUBuilder() {
        this.imports = new HashMap<>();
        this.exports = new HashMap<>();
    }

    protected PolyglotDUBuilder(PolyglotDUBuilder parent) {
        this.imports = parent.imports;
        this.exports = parent.exports;
    }

    protected void updateMaps(PolyglotDUBuilder son) {
        this.imports.putAll(son.imports);
        this.exports.putAll(son.exports);
    }

    public HashMap<String, Pair<Integer, Integer>> getExports() {
        return this.exports;
    }

    public HashMap<String, LinkedList<Pair<Integer, Integer>>> getImports() {
        return this.imports;
    }

    @Override
    public void process(PolyglotZipper zipper) {
        if (zipper.isImport()) {
            String name = zipper.getBindingName();
            if (this.exports.containsKey(name)) {
                if (!this.imports.containsKey(name)) {
                    this.imports.put(name, new LinkedList<>());
                }
                this.imports.get(name).add(zipper.getPosition());
            }
        }
        if (zipper.isExport()) {
            String name = zipper.getBindingName();
            this.exports.put(name,
                    zipper.getPosition());
        }

        PolyglotZipper next = zipper.down();
        while (!next.isNull()) {
            PolyglotDUBuilder nextp = new PolyglotDUBuilder(this);
            nextp.process(next);
            this.updateMaps(nextp);
            next = next.right();
        }
    }

}
