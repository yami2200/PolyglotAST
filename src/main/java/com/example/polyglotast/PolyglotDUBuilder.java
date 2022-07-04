package com.example.polyglotast;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
//import jdk.jfr.Name;
import kotlin.Pair;

public class PolyglotDUBuilder implements PolyglotTreeProcessor {

    //A value can be exported only once, but imported several times.
    private HashMap<String, LinkedList<Pair<Integer, Integer>>> imports;
    private HashMap<String, Pair<Integer, Integer>> exports;
    //Keeping track of nodes so we can identify cycles
    private List<String> visited;

    public PolyglotDUBuilder() {
        this.imports = new HashMap<>();
        this.exports = new HashMap<>();
        this.visited = new LinkedList<String>();
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
            String name = getVariableNameFromZipper(zipper);
            if (!this.imports.containsKey(name)) {
                this.imports.put(name, new LinkedList<>());
            }
            this.imports.get(name).add(zipper.getPosition());
        }
        if (zipper.isExport()) {
            this.exports.put(getVariableNameFromZipper(zipper), zipper.getPosition());
        }

        PolyglotZipper next = zipper.down();
        while (!next.isNull()) {
            this.process(next);
            next = next.right();
        }
    }

    public String getVariableNameFromZipper(PolyglotZipper zipper){
        String regex = "name=('|\")(.*)('|\")";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(zipper.getBindingName());
        if (m.find()) {
            return m.group(2);
        }
        return zipper.getBindingName().replaceAll("\'", "").replaceAll("\"", "");
    }

    public HashMap<String, Pair<Integer, Integer>> getExportInconsistencies(){
        Set<String> exports = this.exports.keySet();
        Set<String> imports = this.imports.keySet();
        HashMap<String, Pair<Integer, Integer>> result = new HashMap<String, Pair<Integer, Integer>>();

        for(String exp : exports) {
            if(!imports.contains(exp)) {
                result.put(exp, this.exports.get(exp));
            }
        }
        return result;
    }

    public HashMap<String, LinkedList<Pair<Integer, Integer>>> getImportInconsistencies(){
        Set<String> exports = this.exports.keySet();
        Set<String> imports = this.imports.keySet();
        HashMap<String, LinkedList<Pair<Integer, Integer>>> map = new HashMap<String, LinkedList<Pair<Integer, Integer>>>();

        for(String imp : imports) {
            if(!exports.contains(imp)) {
                if(map.containsKey(imp)){
                    map.get(imp).addAll(this.imports.get(imp));
                } else {
                    map.put(imp, this.imports.get(imp));
                }
            }
        }
        return map;
    }

    public void printInconsistencies() {
        HashMap<String, LinkedList<Pair<Integer, Integer>>> importInconsistencies = getImportInconsistencies();
        for(String imp : importInconsistencies.keySet()){
            System.out.println("imported but not exported: " + imp);
        }


    }
}
