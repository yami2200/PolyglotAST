package com.example.polyglotast.utils;

import com.example.polyglotast.PolyglotTreeHandler;
import com.example.polyglotast.PolyglotZipper;
import jsitter.api.Zipper;
import kotlin.Pair;

import java.nio.file.Path;

public class ExportData extends PolyglotExpImpData {

    protected String type;
    protected String expVar;
    protected Pair<Integer, Integer> expVarPosition;

    public Pair<Integer, Integer> getExpVarPosition() {
        return expVarPosition;
    }
    public String getType() {
        return type;
    }
    public String getExpVar() {
        return expVar;
    }


    public ExportData(PolyglotZipper zipper) {
        this.var_name = "";
        this.expVar = "";
        switch (zipper.getLang()) {
            case "python":
                if (zipper.getCurrentTree().nodeToCode(zipper.down().right().down().right().down().node).equals("name")) { // export(name="..", value="..")
                    String name = zipper.getCurrentTree().nodeToCode(zipper.down().right().down().right().down().right().right().node);
                    this.var_name_position = zipper.down().right().down().right().down().right().right().getPosition();
                    this.var_name = name.substring(1, name.length() - 1);
                    if (zipper.getCurrentTree().nodeToCode(zipper.down().right().down().right().right().right().down().node).equals("value")){
                        this.type = zipper.down().right().down().right().right().right().down().right().right().node.getType().toString();
                        if(this.type.equals("identifier")) {
                            this.expVar = zipper.down().right().down().right().right().right().down().right().right().getCode();
                            this.expVarPosition = zipper.down().right().down().right().right().right().down().right().right().getPosition();
                        }
                    }
                } else if (zipper.getCurrentTree().nodeToCode(zipper.down().right().down().right().right().right().down().node).equals("name")) { // export(value="..", name="..")
                    String name = zipper.getCurrentTree().nodeToCode(zipper.down().right().down().right().right().right().down().right().right().node);
                    this.var_name_position = zipper.down().right().down().right().right().right().down().right().right().getPosition();
                    this.var_name = name.substring(1, name.length() - 1);
                    if (zipper.getCurrentTree().nodeToCode(zipper.down().right().down().right().down().node).equals("value")){
                        this.type = zipper.down().right().down().right().down().right().right().node.getType().toString();
                        if(this.type.equals("identifier")) {
                            this.expVar = zipper.down().right().down().right().down().right().right().getCode();
                            this.expVarPosition = zipper.down().right().down().right().down().right().right().getPosition();
                        }
                    }
                }
                break;
            case "javascript":
                this.var_name = zipper.getBindingName().substring(1).substring(0, zipper.getBindingName().length() - 2);
                this.var_name_position = zipper.down().right().down().right().getPosition();
                this.type = zipper.down().right().down().right().right().right().getType();
                if(this.type.equals("identifier")) {
                    this.expVar = zipper.down().right().down().right().right().right().getCode();
                    this.expVarPosition = zipper.down().right().down().right().right().right().getPosition();
                }
                break;
        }
        this.var_name_position = new Pair<>(this.var_name_position.component1(), this.var_name_position.component2() + 1); // remove (") or (') character position
        this.char_pos = zipper.getPosition().component2();
        this.line_pos = zipper.getPosition().component1();
        this.char_pos_end = zipper.down().right().down().right().right().right().right().getPosition().component2() + 1;
        this.line_pos_end = zipper.down().right().down().right().right().right().right().getPosition().component1();
        this.filePath = PolyglotTreeHandler.getfilePathOfTreeHandler().get(zipper.getCurrentTree());
        this.id = filePath.toString() + this.char_pos + this.line_pos + this.var_name;
        this.convertType(zipper);
    }

    //TODO : handle binary_operator (2+2), function call, javascript object
    public void convertType(PolyglotZipper zipper){
        switch (zipper.getLang()) {
            case "python":
            case "javascript":
                if(this.type.equals("true") || this.type.equals("false")) this.type = "boolean";
                break;
        }
    }


}
