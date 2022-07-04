package com.example.polyglotast;

public class FileNotFoundInfo {
    String fileName;
    Integer char_position;
    Integer line_position;
    String originLanguage;


    public FileNotFoundInfo(String fileName, Integer char_position, Integer line_position, String originLanguage) {
        this.fileName = fileName;
        this.char_position = char_position;
        this.line_position = line_position;
        this.originLanguage = originLanguage;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Integer getChar_position() {
        return char_position;
    }

    public void setChar_position(Integer char_position) {
        this.char_position = char_position;
    }

    public Integer getLine_position() {
        return line_position;
    }

    public void setLine_position(Integer line_position) {
        this.line_position = line_position;
    }

    public String getOriginLanguage() {
        return originLanguage;
    }

    public void setOriginLanguage(String originLanguage) {
        this.originLanguage = originLanguage;
    }
}