package com.example.polyglotast;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import com.example.polyglotast.utils.CodeArea;
import com.example.polyglotast.utils.ExportData;
import com.example.polyglotast.utils.ImportData;
import kotlin.Pair;



public class PolyglotVariableSpotter extends PolyglotDeepTreeProcessor {

    private HashMap<String, HashMap<PolyglotTreeHandler, ArrayList<ImportData>>> imports;
    private HashMap<String, HashMap<PolyglotTreeHandler, ArrayList<ExportData>>> exports;
    private HashSet<Path> listPathsVisited;

    public PolyglotVariableSpotter() {
        super();
        this.imports = new HashMap<>();
        this.exports = new HashMap<>();
    }

    public HashMap<String, HashMap<PolyglotTreeHandler, ArrayList<ImportData>>> getImports() {
        return imports;
    }

    public HashMap<String, HashMap<PolyglotTreeHandler, ArrayList<ExportData>>> getExports() {
        return exports;
    }

    @Override
    public void processZipperNode(PolyglotZipper zipper) {
        if(zipper.isImport()) {
            ImportData imp = new ImportData(zipper);
            String name = imp.getVar_name();
            if(!name.equals("")){
                if(!this.imports.containsKey(name)) this.imports.put(name, new HashMap<>());
                if(!this.imports.get(name).containsKey(zipper.getCurrentTree())) this.imports.get(name).put(zipper.getCurrentTree(), new ArrayList<>());
                this.imports.get(name).get(zipper.getCurrentTree()).add(imp);
            }
        }
        else if(zipper.isExport()) {
            ExportData exp = new ExportData(zipper);
            String name = exp.getVar_name();
            if(!name.equals("")){
                if(!this.exports.containsKey(name)) this.exports.put(name, new HashMap<>());
                if(!this.exports.get(name).containsKey(zipper.getCurrentTree())) this.exports.get(name).put(zipper.getCurrentTree(), new ArrayList<>());
                this.exports.get(name).get(zipper.getCurrentTree()).add(exp);
            }
        }
    }

}
