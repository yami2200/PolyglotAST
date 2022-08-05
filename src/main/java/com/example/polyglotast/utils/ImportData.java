package com.example.polyglotast.utils;

import com.example.polyglotast.PolyglotTreeHandler;
import com.example.polyglotast.PolyglotZipper;

import java.nio.file.Path;

public class ImportData extends PolyglotExpImpData{

    public String storageVariable;

    public String getStorageVariable() {
        return storageVariable;
    }

    public ImportData(PolyglotZipper zipper) {
        this.var_name = "";
        this.storageVariable = "";
        switch (zipper.getLang()) {
            case "python":
                if (zipper.getBindingName().substring(0, 5).equals("name=")) {
                    this.var_name = zipper.getBindingName().substring(6).substring(0, zipper.getBindingName().length() - 7);
                } else {
                    this.var_name = zipper.getBindingName().substring(1, zipper.getBindingName().length() - 1);
                }
                break;
            case "javascript":
                this.var_name = zipper.getBindingName().substring(1).substring(0, zipper.getBindingName().length() - 2);
                break;
        }
        if(zipper.left() != null && zipper.left().node != null && zipper.left().getType().equals("=") && zipper.left().left() != null && zipper.left().left().node != null){
            this.storageVariable = zipper.left().left().getCode();
        }
        this.char_pos = zipper.getPosition().component2();
        this.line_pos = zipper.getPosition().component1();
        this.char_pos_end = zipper.down().right().down().right().right().getPosition().component2() + 1;
        this.line_pos_end = zipper.down().right().down().right().right().getPosition().component1();
        this.filePath = PolyglotTreeHandler.getfilePathOfTreeHandler().get(zipper.getCurrentTree());
        this.id = filePath.toString() + this.char_pos + this.line_pos + this.var_name;
    }

}
