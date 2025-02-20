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
    private Path path;


    public PolyglotTypeVisitor(PolyglotZipper zipper){
        this.varZipper = zipper;
        this.varName = zipper.getCode();
        this.listExpImp = new ArrayList<>();
        this.path = PolyglotTreeHandler.getfilePathOfTreeHandler().get(zipper.getCurrentTree());
    }


    @Override
    public boolean processZipperNode(PolyglotZipper zipper) {
        if(zipper.node == this.varZipper.node) return false;
        if(zipper.isImport()){
            ImportData data = new ImportData(zipper);
            if(!data.getStorageVariable().equals("") && data.storageVarPosition != null){
                this.listExpImp.add(new Pair<>(data.getStorageVariable(), data));
            }
        } else if(zipper.isExport()){
            ExportData exp = new ExportData(zipper);
            if(!exp.getVar_name().equals("")){
                this.listExpImp.add(new Pair<>(exp.getVar_name(), exp));
            }
        }
        return true;
    }

    public TypingResult getTypeResult(){
        return getRecursiveTypeResult(this.varName, this.listExpImp.size(), null);
    }

    private TypingResult getRecursiveTypeResult(String variableName, int maxIndex, ExportData exp){
        String importedName = "";
        for (int i = maxIndex-1; i>=0 ; i--){
            if(this.listExpImp.get(i).component1().equals(variableName)){
                // The variable is imported
                if(this.listExpImp.get(i).component2() instanceof ImportData && importedName.equals("")){
                    if((exp == null && this.listExpImp.get(i).component2().getFilePath().equals(path)) || (exp != null && exp.getFilePath().equals(this.listExpImp.get(i).component2().getFilePath()))){
                        importedName = this.listExpImp.get(i).component2().getVar_name();
                    }
                }
            }
            // The variable is exported and is later imported
            if(this.listExpImp.get(i).component2() instanceof ExportData && this.listExpImp.get(i).component1().equals(importedName)){
                ExportData newExp = (ExportData) this.listExpImp.get(i).component2();
                // variable exported with another variable value
                if(newExp.getType().equals("identifier")){
                    // recursive call to check the type of the variable exported
                    return this.getRecursiveTypeResult(newExp.getExpVar(), i, newExp);
                }
                // return the type of the value that was exported
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
        UNKNOWN, // unknown type
        VALUETYPE, // The type of a value
        EXPORTTYPE; // Hover location and file to hover to get the type of a variable
    }

}