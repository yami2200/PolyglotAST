package com.example.polyglotast;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
//import jdk.jfr.Name;
import kotlin.Pair;

public class PolyglotDUBuilder implements PolyglotTreeProcessor {

    //A value can be exported only once, but imported several times.
    private HashMap<String, LinkedList<Pair<Integer, Integer>>> imports;
    private HashMap<String, Pair<Integer, Integer>> exports;
    //Keeping track of nodes so we can identify cycles
    private List<PolyglotZipper> visited;

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
        //TODO cycle detection
        if (visited.contains(zipper)) {
            System.err.println("cycle! " + zipper.toString());
        }
        this.visited.add(zipper);

        if (zipper.isImport()) {
            String name = zipper.getBindingName();
            if (!this.imports.containsKey(name)) {
                this.imports.put(name, new LinkedList<>());
            }
            this.imports.get(name).add(zipper.getPosition());
        }
        if (zipper.isExport()) {
            this.exports.put(zipper.getBindingName(), zipper.getPosition());
        }

        PolyglotZipper next = zipper.down();
        while (!next.isNull()) {
            this.process(next);

            next = next.right();
        }
    }

    public void printInconsistencies() {
        Set<String> exports = this.exports.keySet();
        Set<String> imports = this.imports.keySet();

        for(String imp : imports) {
            if(!exports.contains(imp)) {
                System.out.println("imported but not exported: " + imp);
            }
        }
        
        for(String exp : exports) {
            if(!imports.contains(exp)) {
                System.out.println("exported but not imported: " + exp);
            }
        }
    }

    public void printCycles() {
        System.err.println("Not Yet Implemented: print cycles in PolyglotAST");
    }

}
