package com.example.polyglotast;

import com.example.polyglotast.utils.ExportData;
import com.example.polyglotast.utils.ImportData;
import com.example.polyglotast.utils.PolyglotExpImpData;
import jsitter.api.Zipper;
import kotlin.Pair;

import java.nio.file.Path;
import java.util.ArrayList;

public class PolyglotTypeVisitor extends PolyglotDeepTreeProcessor{

    private PolyglotZipper varZipper;
    private String varName;
    private ArrayList<Pair<String, PolyglotExpImpData>> listExpImp;


    public PolyglotTypeVisitor(PolyglotZipper zipper){
        this.varZipper = zipper;
        this.varName = zipper.getCode();
        this.listExpImp = new ArrayList<>();
    }

    @Override
    public boolean processZipperNode(PolyglotZipper zipper) {
        if(zipper.node == this.varZipper.node) return false;
        if(zipper.isImport()){
            ImportData data = new ImportData(zipper);
            this.listExpImp.add(new Pair<>(data.getStorageVariable(), data));
        } else if(zipper.isExport()){
            ExportData exp = new ExportData(zipper);
            this.listExpImp.add(new Pair<>(exp.getVar_name(), exp));
        }
        return true;
    }

    public TypingResult getTypeResult(){
        return getRecursiveTypeResult(this.varName, this.listExpImp.size(), null);
        /*String importedName = "";
        for (int i = this.listExpImp.size()-1; i>=0 ; i--){
            if(this.listExpImp.get(i).component1().equals(this.varName)){
                if(this.listExpImp.get(i).component2() instanceof ImportData && importedName.equals("")){
                    importedName = this.listExpImp.get(i).component2().getVar_name();
                }
            }
            if(this.listExpImp.get(i).component2() instanceof ExportData && this.listExpImp.get(i).component1().equals(importedName)){
                ExportData exp = (ExportData) this.listExpImp.get(i).component2();
                if(exp.getType().equals("identifier")){
                    return this.getRecursiveTypeResult(exp.getExpVar(), i, exp);
                }
                return new TypingResult(exp.getType());
            }
        }
        return new TypingResult();*/
    }

    private TypingResult getRecursiveTypeResult(String variableName, int maxIndex, ExportData exp){
        String importedName = "";
        for (int i = maxIndex-1; i>=0 ; i--){
            if(this.listExpImp.get(i).component1().equals(variableName)){
                if(this.listExpImp.get(i).component2() instanceof ImportData && importedName.equals("")){
                    importedName = this.listExpImp.get(i).component2().getVar_name();
                }
            }
            if(this.listExpImp.get(i).component2() instanceof ExportData && this.listExpImp.get(i).component1().equals(importedName)){
                ExportData newExp = (ExportData) this.listExpImp.get(i).component2();
                if(newExp.getType().equals("identifier")){
                    return this.getRecursiveTypeResult(newExp.getExpVar(), i, newExp);
                }
                return new TypingResult(newExp.getType());
            }
        }
        if(exp == null) return new TypingResult();
        return new TypingResult(exp.getExpVarPosition(), exp.getFilePath());
    }

    public class TypingResult {
        public TypeResultEnum typeResult;
        public Pair<Integer, Integer> hoverLocation;
        public Path fileExportPath;
        public String type;

        public TypingResult(){
            this.typeResult = TypeResultEnum.UNKNOWN;
            this.hoverLocation = null;
            this.fileExportPath = null;
            this.type = "";
        }

        public TypingResult(Pair<Integer, Integer> hoverLocation, Path fileExportPath){
            this.typeResult = TypeResultEnum.EXPORTTYPE;
            this.hoverLocation = hoverLocation;
            this.fileExportPath = fileExportPath;
            this.type = "";
        }

        public TypingResult(String type){
            this.typeResult = TypeResultEnum.VALUETYPE;
            this.hoverLocation = null;
            this.fileExportPath = null;
            this.type = type;
        }
    }

    public enum TypeResultEnum {
        UNKNOWN, VALUETYPE, EXPORTTYPE;
    }

}