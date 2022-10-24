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
        NodePosition firstPos = zipper.getCurrentTree().getNodePositionForMultipleRequest(zipper.down().right().down().right().node, 0, 0);
        this.var_name_position = firstPos.position;
        switch (zipper.getLang()) {
            case "python":
                if(zipper.down().right().down().right().getType().equals("string")){
                    String name = zipper.getCurrentTree().nodeToCode(zipper.down().right().down().right().node);
                    this.var_name = name.substring(1, name.length() - 1);
                } else if(zipper.down().right().down().right().down().getCode().equals("name")){
                    String name = zipper.getCurrentTree().nodeToCode(zipper.down().right().down().right().down().right().right().node);
                    this.var_name = name.substring(1, name.length() - 1);
                    this.var_name_position = zipper.getCurrentTree().getNodePositionForMultipleRequest(zipper.down().right().down().right().down().right().right().node, firstPos.previous_line, firstPos.previous_line_char).position;
                }
                break;
            case "javascript":
                this.var_name = zipper.getBindingName().substring(1).substring(0, zipper.getBindingName().length() - 2);
                break;
        }
        if(zipper.left() != null && zipper.left().node != null && zipper.left().getType().equals("=") && zipper.left().left() != null && zipper.left().left().node != null){
            this.storageVariable = zipper.left().left().getCode();
            this.storageVarPosition = zipper.left().left().getPosition();
            if(zipper.left().left().getType().equals("ERROR")){
                try{
                    this.storageVariable = zipper.left().left().down().right().getCode();
                    this.storageVarPosition = zipper.left().left().down().right().getPosition();
                } catch (Exception e){
                    this.storageVariable = "";
                    this.storageVarPosition = null;
                }
            }
        }
        this.var_name_position = new Pair<>(this.var_name_position.component1(), this.var_name_position.component2() + 1); // remove (") or (') character position
        NodePosition initPos = zipper.getCurrentTree().getNodePositionForMultipleRequest(zipper.node, firstPos.previous_line, firstPos.previous_line_char);
        this.char_pos = initPos.position.component2();
        this.line_pos = initPos.position.component1();
        NodePosition endPos = zipper.getCurrentTree().getNodePositionForMultipleRequest(zipper.down().right().down().right().right().node, firstPos.previous_line, firstPos.previous_line_char);
        this.char_pos_end = endPos.position.component2() + 1;
        this.line_pos_end = endPos.position.component1();
        this.filePath = PolyglotTreeHandler.getfilePathOfTreeHandler().get(zipper.getCurrentTree());
        this.id = filePath.toString() + this.char_pos + this.line_pos + this.var_name;
    }

}
