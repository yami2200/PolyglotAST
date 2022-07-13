package com.example.polyglotast.utils;

import java.nio.file.Path;

public class CodeArea {
    public int char_pos;
    public int line_pos;
    public int char_pos_end;
    public int line_pos_end;
    public String id;
    public Path filePath;

    public CodeArea(int char_pos, int line_pos, int char_pos_end, int line_pos_end, Path filePath) {
        this.char_pos = char_pos;
        this.line_pos = line_pos;
        this.char_pos_end = char_pos_end;
        this.line_pos_end = line_pos_end;
        this.filePath = filePath;
        this.id = this.char_pos + this.line_pos + this.char_pos_end + this.line_pos_end + "id";
    }

    @Override
    public String toString() {
        return "CodeArea{" +
                "char_pos=" + char_pos +
                ", line_pos=" + line_pos +
                ", char_pos_end=" + char_pos_end +
                ", line_pos_end=" + line_pos_end +
                '}';
    }


}
