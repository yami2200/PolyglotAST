package com.example.polyglotast;

import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

import com.example.polyglotast.utils.CodeArea;
import com.example.polyglotast.utils.ExportData;
import com.example.polyglotast.utils.ExportImportStep;
import com.example.polyglotast.utils.ImportData;

public class PolyglotDUBuilder implements PolyglotTreeProcessor {

    private HashMap<String, ArrayList<ImportData>> imports;
    private HashMap<String, ArrayList<ExportData>> exports;
    private HashSet<Path> listPathsVisited;


    private HashMap<String, HashSet<ImportData>> importWithoutExport;
    private HashMap<String, HashSet<ImportData>> importBeforeExport;
    private HashMap<String, HashSet<ImportData>> importFromSameFile;
    private HashMap<String, HashSet<ExportData>> exportWithoutImport;
    private HashSet<CodeArea> evalSameFile;

    public PolyglotDUBuilder() {
        this.imports = new HashMap<>();
        this.exports = new HashMap<>();
        this.listPathsVisited = new HashSet<>();

        this.importBeforeExport = new HashMap<>();
        this.importWithoutExport = new HashMap<>();
        this.importFromSameFile = new HashMap<>();
        this.exportWithoutImport = new HashMap<>();
        this.evalSameFile = new HashSet<>();
    }

    protected PolyglotDUBuilder(PolyglotDUBuilder parent) {
        this.imports = parent.imports;
        this.exports = parent.exports;
        this.listPathsVisited = parent.listPathsVisited;

        this.importBeforeExport = parent.importBeforeExport;
        this.importWithoutExport = parent.importWithoutExport;
        this.importFromSameFile = parent.importFromSameFile;
        this.exportWithoutImport = parent.exportWithoutImport;
        this.evalSameFile = parent.evalSameFile;
    }

    public void updateMaps(PolyglotDUBuilder son) {
        this.imports.putAll(son.imports);
        this.exports.putAll(son.exports);
        this.listPathsVisited.addAll(son.listPathsVisited);

        this.importBeforeExport.putAll(son.importBeforeExport);
        this.importWithoutExport.putAll(son.importWithoutExport);
        this.importFromSameFile.putAll(son.importFromSameFile);
        this.exportWithoutImport.putAll(son.exportWithoutImport);
        this.evalSameFile.addAll(son.evalSameFile);
    }

    public HashSet<CodeArea> getEvalSameFile() {
        return evalSameFile;
    }

    public HashMap<String, HashSet<ImportData>> getImportWithoutExport() {
        return importWithoutExport;
    }

    public HashMap<String, HashSet<ImportData>> getImportBeforeExport() {
        return importBeforeExport;
    }

    public HashMap<String, HashSet<ImportData>> getImportFromSameFile() {
        return importFromSameFile;
    }

    public HashMap<String, HashSet<ExportData>> getExportWithoutImport() {
        return exportWithoutImport;
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
        boolean loopInSingleFile = false;
        if(PolyglotTreeHandler.getfilePathOfTreeHandler().containsKey(zipper.getCurrentTree())) listPathsVisited.add(PolyglotTreeHandler.getfilePathOfTreeHandler().get(zipper.getCurrentTree()));
        if(zipper.isImport()){
            ImportData imp = new ImportData(zipper);
            if(!imp.getVar_name().equals("")){
                // Check Probable Error/Warning
                this.computePotentialImportErrors(imp);
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
                // Check Probable Error/Warning
                this.computePotentialExportErrors(exp);
                // Add to the map of exports
                if(exports.containsKey(exp.getVar_name())){
                    exports.get(exp.getVar_name()).add(exp);
                } else {
                    ArrayList<ExportData> list = new ArrayList<>();
                    list.add(exp);
                    exports.put(exp.getVar_name(), list);
                }
            }
        } else if(zipper.isEval() && PolyglotTreeHandler.getfilePathOfTreeHandler().containsKey(zipper.getCurrentTree())){
            if(zipper.down().isNull() || ((zipper.down().getType().equals("program") || zipper.down().getType().equals("module")) && zipper.getCurrentTree().equals(zipper.down().getCurrentTree()))){
                loopInSingleFile = true;
                String[] lines = zipper.getCurrentTree().nodeToCode(zipper.node).split("\r\n|\r|\n");
                int nbLines = lines.length;
                CodeArea line = new CodeArea(zipper.getPosition().component2(),
                        zipper.getPosition().component1(),
                        zipper.getPosition().component2()+(lines[nbLines-1]).length(),
                        zipper.getPosition().component1()+nbLines-1,
                        PolyglotTreeHandler.getfilePathOfTreeHandler().get(zipper.getCurrentTree()));
                this.evalSameFile.add(line);
            }
        }

        PolyglotZipper next = zipper.down();
        Path nextPath = null;
        if(PolyglotTreeHandler.getfilePathOfTreeHandler().containsKey(next.getCurrentTree())) nextPath = PolyglotTreeHandler.getfilePathOfTreeHandler().get(next.getCurrentTree());
        while (!loopInSingleFile && !next.isNull() && !(zipper.getCurrentTree() != next.getCurrentTree() && nextPath != null && listPathsVisited.contains(nextPath))) {
            this.process(next);
            next = next.right();
        }
    }

    private void computePotentialExportErrors(ExportData exp){
        if(this.importWithoutExport.containsKey(exp.getVar_name())){
            if(this.importBeforeExport.containsKey(exp.getVar_name())){
                this.importBeforeExport.get(exp.getVar_name()).addAll(this.importWithoutExport.get(exp.getVar_name()));
            } else {
                HashSet<ImportData> list = new HashSet<>();
                list.addAll(this.importWithoutExport.get(exp.getVar_name()));
                this.importBeforeExport.put(exp.getVar_name(), list);
            }
            this.importWithoutExport.remove(exp.getVar_name());
        } else if(!this.imports.containsKey(exp.getVar_name())) {
            if(this.exportWithoutImport.containsKey(exp.getVar_name())){
                this.exportWithoutImport.get(exp.getVar_name()).add(exp);
            } else {
                HashSet<ExportData> list = new HashSet<>();
                list.add(exp);
                this.exportWithoutImport.put(exp.getVar_name(), list);
            }
        }
    }

    private void computePotentialImportErrors(ImportData imp){
        // Check if the variable was exported
        if(this.exports.containsKey(imp.getVar_name()) && this.exports.get(imp.getVar_name()).size()>0) {
            // Remove variable from the "export without import" list
            if(this.exportWithoutImport.containsKey(imp.getVar_name())) this.exportWithoutImport.remove(imp.getVar_name());
            // Check if last export of variable was in the same file
            if(this.exports.get(imp.getVar_name()).get(this.exports.get(imp.getVar_name()).size()-1).getFilePath().equals(imp.getFilePath())){
                if(this.importFromSameFile.containsKey(imp.getVar_name())){
                    this.importFromSameFile.get(imp.getVar_name()).add(imp);
                } else {
                    HashSet<ImportData> list = new HashSet();
                    list.add(imp);
                    this.importFromSameFile.put(imp.getVar_name(), list);
                }
            }
        } else {
            // Add to list of import without export
            if(this.importWithoutExport.containsKey(imp.getVar_name())){
                this.importWithoutExport.get(imp.getVar_name()).add(imp);
            } else {
                HashSet<ImportData> list = new HashSet();
                list.add(imp);
                this.importWithoutExport.put(imp.getVar_name(), list);
            }
        }
    }
}

