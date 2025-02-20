package com.example.polyglotast.utils;

import kotlin.Pair;

import java.nio.file.Path;

public abstract class PolyglotExpImpData {
    protected int char_pos;
    protected int line_pos;
    protected int char_pos_end;
    protected int line_pos_end;
    protected String var_name;
    protected Path filePath;

    protected String id;

    protected Pair<Integer, Integer> var_name_position;

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
    public Pair<Integer, Integer> getVar_name_position() {
        return var_name_position;
    }
}
