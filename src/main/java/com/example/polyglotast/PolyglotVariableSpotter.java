package com.example.polyglotast;

import java.util.HashMap;

import kotlin.Pair;



public class PolyglotVariableSpotter implements PolyglotTreeProcessor {


    private HashMap<Pair<String, Boolean>, Pair<Integer, Integer>> result; // boolean encodes defined/used
    private HashMap<Pair<String, Boolean>, Pair<Integer, Integer>> finalResult;

    public PolyglotVariableSpotter() {
        this.result = new HashMap<>();
    }

    public PolyglotVariableSpotter(HashMap<Pair<String, Boolean>, Pair<Integer, Integer>> previous) {
        this.result = previous;
    }

    public HashMap<Pair<String, Boolean>, Pair<Integer, Integer>> getLocalState() {
        return this.result;
    }

    public HashMap<Pair<String, Boolean>, Pair<Integer, Integer>> getFinalState() {
        return this.finalResult;
    }

    @Override
    public void process(PolyglotZipper zipper) {
        if(zipper.isImport()) {
            Integer pos = zipper.getPos();
            Integer end = pos + zipper.getLength();
            String name = zipper.getBindingName();
            this.result.put(new Pair<>(name, false), 
            new Pair<>(pos, end));
        }
        if(zipper.isExport()) {
            Integer pos = zipper.getPos();
            Integer end = pos + zipper.getLength();
            String name = zipper.getBindingName();
            this.result.put(new Pair<>(name, true), 
            new Pair<>(pos, end));
        }

        PolyglotZipper next = zipper.down();
        if(next.isNull()) {
            this.finalResult = this.result;
        } else {
            this.finalResult = new HashMap<>();
        }
        while (!next.isNull()) {
            PolyglotVariableSpotter nextp = new PolyglotVariableSpotter(this.result);
            nextp.process(next);
            this.finalResult.putAll(nextp.getFinalState());
            next = next.right();
        }
    }
    
}
