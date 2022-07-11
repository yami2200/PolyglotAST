package com.example.polyglotast.utils;

import com.example.polyglotast.PolyglotTreeHandler;
import com.example.polyglotast.PolyglotZipper;

import java.nio.file.Path;

public class ExportData {

    private int char_pos;
    private int line_pos;
    private int char_pos_end;
    private int line_pos_end;
    private String var_name;
    private Path filePath;

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

    public ExportData(PolyglotZipper zipper) {
        this.var_name = "";
        switch (zipper.getLang()) {
            case "python":
                if (zipper.getCurrentTree().nodeToCode(zipper.down().right().down().right().down().node).equals("name")) { // export(name="..", value="..")
                    String name = zipper.getCurrentTree().nodeToCode(zipper.down().right().down().right().down().right().right().node);
                    this.var_name = name.substring(1, name.length() - 1);
                } else if (zipper.getCurrentTree().nodeToCode(zipper.down().right().down().right().right().right().down().node).equals("name")) { // export(value="..", name="..")
                    String name = zipper.getCurrentTree().nodeToCode(zipper.down().right().down().right().right().right().down().right().right().node);
                    this.var_name = name.substring(1, name.length() - 1);
                }
                break;
            case "javascript":
                this.var_name = zipper.getBindingName().substring(1).substring(0, zipper.getBindingName().length() - 2);
                break;
        }
        this.char_pos = zipper.getPosition().component2();
        this.line_pos = zipper.getPosition().component1();
        this.char_pos_end = zipper.down().right().down().right().right().right().right().getPosition().component2() + 1;
        this.line_pos_end = zipper.down().right().down().right().right().right().right().getPosition().component1();
        this.filePath = PolyglotTreeHandler.getfilePathOfTreeHandler().get(zipper.getCurrentTree());
    }

}
