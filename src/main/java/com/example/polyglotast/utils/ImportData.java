package com.example.polyglotast.utils;

import com.example.polyglotast.PolyglotTreeHandler;
import com.example.polyglotast.PolyglotZipper;

import java.nio.file.Path;

public class ImportData {

    private int char_pos;
    private int line_pos;
    private int char_pos_end;
    private int line_pos_end;
    private String var_name;
    private Path filePath;
    private String id;

    public Path getFilePath() {
        return filePath;
    }

    public int getChar_pos() {
        return char_pos;
    }

    public int getLine_pos() {
        return line_pos;
    }

    public int getChar_pos_end() {
        return char_pos_end;
    }

    public int getLine_pos_end() {
        return line_pos_end;
    }

    public String getVar_name() {
        return var_name;
    }

    public String getId() {
        return id;
    }

    public ImportData(PolyglotZipper zipper) {
        this.var_name = "";
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
        this.char_pos = zipper.getPosition().component2();
        this.line_pos = zipper.getPosition().component1();
        this.char_pos_end = zipper.down().right().down().right().right().getPosition().component2() + 1;
        this.line_pos_end = zipper.down().right().down().right().right().getPosition().component1();
        this.filePath = PolyglotTreeHandler.getfilePathOfTreeHandler().get(zipper.getCurrentTree());
        this.id = filePath.toString() + this.char_pos + this.line_pos + this.var_name;
    }

}
