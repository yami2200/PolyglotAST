package com.example.polyglotast.utils;

public class ExportImportStep {
    boolean isImport;
    String variable;
    int index;

    public ExportImportStep(boolean isImport, String variable, int index) {
        this.isImport = isImport;
        this.variable = variable;
        this.index = index;
    }

    public boolean isImport() {
        return isImport;
    }

    public String getVariable() {
        return variable;
    }

    public int getIndex() {
        return index;
    }
}
