package com.example.polyglotast.utils;

import com.example.polyglotast.PolyglotTreeHandler;
import com.example.polyglotast.PolyglotZipper;
import kotlin.Pair;

import java.nio.file.Path;

public class ImportData extends PolyglotExpImpData{

    public String storageVariable;

    public Pair<Integer, Integer> storageVarPosition;

    public String getStorageVariable() {
        return storageVariable;
    }

    public ImportData(PolyglotZipper zipper) {
        this.var_name = "";
        this.storageVariable = "";
        switch (zipper.getLang()) {
            case "python":
                this.var_name_position = zipper.down().right().down().right().getPosition();
                /*if (zipper.getBindingName().length() > 5 && zipper.getBindingName().substring(0, 5).equals("name=")) {
                    this.var_name = zipper.getBindingName().substring(6).substring(0, zipper.getBindingName().length() - 7);
                    this.var_name_position = new Pair<>(this.var_name_position.component1(), this.var_name_position.component2() + 5);
                } else if(zipper.getBindingName().length() > 2) {
                    this.var_name = zipper.getBindingName().substring(1, zipper.getBindingName().length() - 1);
                }*/
                if(zipper.down().right().down().right().getType().equals("string")){
                    String name = zipper.getCurrentTree().nodeToCode(zipper.down().right().down().right().node);
                    this.var_name = name.substring(1, name.length() - 1);
                } else if(zipper.down().right().down().right().down().getCode().equals("name")){
                    String name = zipper.getCurrentTree().nodeToCode(zipper.down().right().down().right().down().right().right().node);
                    this.var_name = name.substring(1, name.length() - 1);
                    this.var_name_position = zipper.down().right().down().right().down().right().right().getPosition();
                }
                break;
            case "javascript":
                this.var_name = zipper.getBindingName().substring(1).substring(0, zipper.getBindingName().length() - 2);
                this.var_name_position = zipper.down().right().down().right().getPosition();
                break;
        }
        if(zipper.left() != null && zipper.left().node != null && zipper.left().getType().equals("=") && zipper.left().left() != null && zipper.left().left().node != null){
            this.storageVariable = zipper.left().left().getCode();
            this.storageVarPosition = zipper.left().left().getPosition();
        }
        this.var_name_position = new Pair<>(this.var_name_position.component1(), this.var_name_position.component2() + 1); // remove (") or (') character position
        System.out.println(var_name_position);
        this.char_pos = zipper.getPosition().component2();
        this.line_pos = zipper.getPosition().component1();
        this.char_pos_end = zipper.down().right().down().right().right().getPosition().component2() + 1;
        this.line_pos_end = zipper.down().right().down().right().right().getPosition().component1();
        this.filePath = PolyglotTreeHandler.getfilePathOfTreeHandler().get(zipper.getCurrentTree());
        this.id = filePath.toString() + this.char_pos + this.line_pos + this.var_name;
    }

}
