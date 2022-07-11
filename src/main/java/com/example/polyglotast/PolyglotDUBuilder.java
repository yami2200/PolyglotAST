package com.example.polyglotast;

import java.nio.file.Path;
import java.util.*;

import com.example.polyglotast.utils.ExportData;
import com.example.polyglotast.utils.ExportImportStep;
import com.example.polyglotast.utils.ImportData;

public class PolyglotDUBuilder implements PolyglotTreeProcessor {

    private HashMap<String, ArrayList<ImportData>> imports;
    private HashMap<String, ArrayList<ExportData>> exports;
    private HashSet<Path> listPathsVisited;
    private ArrayList<ExportImportStep> listOperation;


    private HashMap<String, HashSet<ImportData>> importWithoutExport;
    private HashMap<String, HashSet<ImportData>> importBeforeExport;
    private HashMap<String, HashSet<ImportData>> importFromSameFile;
    private HashMap<String, HashSet<ExportData>> exportWithoutImport;

    public PolyglotDUBuilder() {
        this.imports = new HashMap<>();
        this.exports = new HashMap<>();
        this.listPathsVisited = new HashSet<>();
        this.listOperation = new ArrayList<>();
    }

    protected PolyglotDUBuilder(PolyglotDUBuilder parent) {
        this.imports = parent.imports;
        this.exports = parent.exports;
        this.listPathsVisited = parent.listPathsVisited;
        this.listOperation = parent.listOperation;
    }

    protected void updateMaps(PolyglotDUBuilder son) {
        this.imports.putAll(son.imports);
        this.exports.putAll(son.exports);
        this.listPathsVisited.addAll(son.listPathsVisited);
        this.listOperation.addAll(son.listOperation);
    }

    public ArrayList<ExportImportStep> getListOperation(){
        return this.listOperation;
    }

    public HashSet<Path> getPathsCovered(){
        return this.listPathsVisited;
    }

    public HashMap<String, ArrayList<ImportData>> getImports(){
        return this.imports;
    }

    public HashMap<String, ArrayList<ExportData>> getExports(){
        return this.exports;
    }

    @Override
    public void process(PolyglotZipper zipper) {
        if(zipper.isImport()){
            ImportData imp = new ImportData(zipper);
            if(!imp.getVar_name().equals("")){
                // Add path to a list, usefull to refresh diagnostics of the specific file
                listPathsVisited.add(imp.getFilePath());
                // Check Probable Error/Warning

                // Add to the map of imports
                if(imports.containsKey(imp.getVar_name())){
                    imports.get(imp.getVar_name()).add(imp);
                } else {
                    ArrayList<ImportData> list = new ArrayList<>();
                    list.add(imp);
                    imports.put(imp.getVar_name(), list);
                }
            }
        } else if(zipper.isExport()){
            ExportData exp = new ExportData(zipper);
            if(!exp.getVar_name().equals("")){
                // Add path to a list, usefull to refresh diagnostics of the specific file
                listPathsVisited.add(exp.getFilePath());
                // Check Probable Error/Warning

                // Add to the map of exports
                if(exports.containsKey(exp.getVar_name())){
                    exports.get(exp.getVar_name()).add(exp);
                } else {
                    ArrayList<ExportData> list = new ArrayList<>();
                    list.add(exp);
                    exports.put(exp.getVar_name(), list);
                }
            }
        }

        PolyglotZipper next = zipper.down();
        while (!next.isNull()) {
            this.process(next);
            next = next.right();
        }
    }
}

