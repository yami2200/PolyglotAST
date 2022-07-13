package com.example.polyglotast;

import com.example.polyglotast.utils.ExportData;
import com.example.polyglotast.utils.ImportData;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

public class PolyglotTreeVisualizer implements PolyglotTreeProcessor {

    private String UMLResult;
    private int currentID;

    private static final HashMap<String, HashMap<String, String>> colorMap;
    private static final HashSet<String> colorSet;

    static {
        colorSet = new HashSet<>();
        colorSet.add("red");
        colorSet.add("blue");
        colorSet.add("yellow");
        colorSet.add("green");
        colorSet.add("pink");
        colorSet.add("purple");

        colorMap = new HashMap<>();
        colorMap.put("red", new HashMap<>());
        colorMap.get("red").put("polyglot_import_call", "#db1d1d");
        colorMap.get("red").put("polyglot_export_call", "#db1d1d");
        colorMap.get("red").put("all", "#eb7878");
        colorMap.put("blue", new HashMap<>());
        colorMap.get("blue").put("polyglot_import_call", "#1d63db");
        colorMap.get("blue").put("polyglot_export_call", "#1d63db");
        colorMap.get("blue").put("all", "#6f9eed");
        colorMap.put("yellow", new HashMap<>());
        colorMap.get("yellow").put("polyglot_import_call", "#ffff1c");
        colorMap.get("yellow").put("polyglot_export_call", "#ffff1c");
        colorMap.get("yellow").put("all", "#f1f299");
        colorMap.put("green", new HashMap<>());
        colorMap.get("green").put("polyglot_import_call", "#219114");
        colorMap.get("green").put("polyglot_export_call", "#219114");
        colorMap.get("green").put("all", "#7ce37b");
        colorMap.put("pink", new HashMap<>());
        colorMap.get("pink").put("polyglot_import_call", "#e329b8");
        colorMap.get("pink").put("polyglot_export_call", "#e329b8");
        colorMap.get("pink").put("all", "#f584db");
        colorMap.put("purple", new HashMap<>());
        colorMap.get("purple").put("polyglot_import_call", "#6c26de");
        colorMap.get("purple").put("polyglot_export_call", "#6c26de");
        colorMap.get("purple").put("all", "#a77df0");
    }

    public PolyglotTreeVisualizer(){
        this.UMLResult = "";
        this.currentID = 0;
    }

    public void save(String fileName) throws IOException {
        if(this.UMLResult.equals("")) return;
        File file = new File("UMLrepresentations/"+fileName);
        file.createNewFile();
        FileOutputStream out = new FileOutputStream(file, false);
        out.write(this.UMLResult.getBytes());
        out.close();
    }

    @Override
    public void process(PolyglotZipper zipper) {
        this.currentID = 0;
        this.UMLResult = "@startuml\n"+"object \"Polyglot AST\" as "+this.currentID+" #666\n";
        if(PolyglotTreeHandler.getfilePathOfTreeHandler().containsKey(zipper.getCurrentTree())){
            this.UMLResult += this.currentID + " : Host : "+ PolyglotTreeHandler.getfilePathOfTreeHandler().get(zipper.getCurrentTree()).getFileName().toString()+"\n";
        }
        this.process(zipper, new HashSet<PolyglotTreeHandler>(), this.currentID, this.currentID, (String) colorSet.toArray()[new Random().nextInt(colorSet.size())]);
        this.UMLResult += "@enduml";
    }

    public void process(PolyglotZipper zipper, HashSet<PolyglotTreeHandler> alreadyVisited, int parentID, int startingID, String color){
        if(!alreadyVisited.contains(zipper.getCurrentTree())) alreadyVisited.add(zipper.getCurrentTree());
        this.currentID = startingID+1;
        int currentParent = this.currentID;
        this.UMLResult += "object \""+zipper.getType().replace('"', '\'')+"\" as "+this.currentID+" "+this.getColor(zipper, color)+"\n"+this.getZipperExtraAttributes(zipper, currentParent);
        if (zipper.down().isNull()){
            if(!zipper.getCode().equals(zipper.getType()) && zipper.getCode().length()>1) this.UMLResult += this.currentID +" : "+zipper.getCode()+"\n";
            this.UMLResult += this.currentID +" : (" + zipper.getPosition() + ")\n";
        }
        this.UMLResult += parentID + " -down-> "+this.currentID+"\n";

        if(zipper.isEval() && ((zipper.down().getType().equals("program") || zipper.down().getType().equals("module")) && zipper.getCurrentTree().equals(zipper.down().getCurrentTree()))) return;
        PolyglotZipper next = zipper.down();
        while (!next.isNull() && !(next.getCurrentTree() != zipper.getCurrentTree() && alreadyVisited.contains(next.getCurrentTree()))) {
            PolyglotTreeVisualizer visualizer = new PolyglotTreeVisualizer();
            visualizer.process(next, alreadyVisited, currentParent, this.currentID, this.nextColor(zipper, color));

            this.UMLResult += visualizer.UMLResult;
            this.currentID = visualizer.currentID+1;

            next = next.right();
        }
    }

    public String getColor(PolyglotZipper zipper, String currentColor){
        if(zipper.isEval()) return "#777";
        if(zipper.isImport()) return colorMap.get(currentColor).get("polyglot_import_call");
        if(zipper.isExport()) return colorMap.get(currentColor).get("polyglot_export_call");
        return colorMap.get(currentColor).get("all");
    }

    public String nextColor(PolyglotZipper zipper, String currentColor){
        if(zipper.isEval() && !zipper.down().isNull() && zipper.down().right().isNull()){
            HashSet<String> colors = new HashSet<>(colorSet);
            colors.remove(currentColor);
            int index = new Random().nextInt(colors.size());
            return (String) colors.toArray()[index];
        }
        return currentColor;
    }

    public String getZipperExtraAttributes(PolyglotZipper zipper, int zipperID){
        if(zipper.isEval()){
            if(!PolyglotTreeHandler.getfilePathOfTreeHandler().containsKey(zipper.down().currentTree)) return "";
            String filename = PolyglotTreeHandler.getfilePathOfTreeHandler().get(zipper.down().currentTree).getFileName().toString();
            if(zipper.down().isNull() || !((zipper.down().getType().equals("program") || zipper.down().getType().equals("module")))) {
                return zipperID+ " : EvalFile Error : file not found\n";
            }
            return zipperID+ " : EvalFile : "+filename+"\n";
        }
        if(zipper.isImport()){
            ImportData imp = new ImportData(zipper);
            if(!imp.getVar_name().equals("")){
                return zipperID+ " : Import : "+imp.getVar_name()+"\n";
            }
            return "";
        }
        if(zipper.isExport()){
            ExportData exp = new ExportData(zipper);
            if(!exp.getVar_name().equals("")){
                return zipperID+ " : Export : "+exp.getVar_name()+"\n";
            }
            return "";
        }
        if(zipper.getType().equals("string")){
            return zipperID+ " : "+zipper.getCurrentTree().nodeToCode(zipper.node)+"\n";
        }
        return "";
    }
}
