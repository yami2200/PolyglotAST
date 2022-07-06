package com.example.polyglotast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PolyglotTreePrinter implements PolyglotTreeProcessor {

    private final String indent;
    private String result;

    public PolyglotTreePrinter() {
        this.indent = "| ";
        this.result = "";
    }

    public PolyglotTreePrinter(String indent) {
        this.indent = indent;
        this.result = "";
    }

    public String getRes() {
        return this.result;
    }

    private String getResAsPUML(){
        String puml = "@startuml\n";
        int id = 0;
        ArrayList<String> list_parents = new ArrayList<String>();
        list_parents.add("AST");
        puml += "object \"AST\" as AST\n";
        Scanner scanner = new Scanner(this.result);
        Pattern pattern_regex = Pattern.compile("(.*) : (.*)$");
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            int line_indent = getLineIndentCount(line);
            String obj = line.substring(line_indent*3+2, line.length());
            Matcher matcher = pattern_regex.matcher(obj);
            if(matcher.find()){
                if(!matcher.group(1).contains("\"")){
                    puml += "object \""+ matcher.group(1)+"\" as "+id+"\n";
                } else {
                    puml += "object \""+ matcher.group(1).replace('"', '\'')+"\" as "+id+"\n";
                }
                if(matcher.group(2).equals("") && scanner.hasNextLine()){
                    puml += id+" : "+scanner.nextLine()+"\n";
                } else {
                    puml += id+" : "+matcher.group(2)+"\n";
                }
                puml += list_parents.get(line_indent) + " -down-> "+id+"\n";
                id++;
            } else {
                puml += "object \""+obj+"\" as "+obj+id+"\n";
                puml += list_parents.get(line_indent) + " -down-> "+obj+id+"\n";
                while(line_indent+1 >= list_parents.size()) {
                    list_parents.add(obj+id);
                }
                list_parents.set(line_indent+1, obj+id);
                id++;
            }
        }
        return puml+"@enduml";
    }

    /**
     * [Experimental] : Write PUML representation of the Tree processed by the Tree Printer into .puml file
     * @param filename name of the file to write (must have .puml extension)
     * @throws IOException
     */
    public void printUMLInFile(String filename) throws IOException {
        File file = new File("UMLrepresentations/"+filename);
        file.createNewFile(); // if file already exists will do nothing
        FileOutputStream out = new FileOutputStream(file, false);
        out.write(this.getResAsPUML().getBytes());
        out.close();
    }

    private int getLineIndentCount(String line){
        int pos = 0;
        int indent = 0;
        while(line.substring(pos, pos+3).equals("|  ")){
            indent++;
            pos+=3;
        }
        return indent;
    }

    @Override
    public void process(PolyglotZipper zipper) {
        this.result = "";
        if (zipper.down().isNull()) {
            this.result += this.indent + zipper.getType() + " : " + zipper.getCode() + "(" + zipper.getPosition() + ")\n";
            return;
        } else {
            this.result += this.indent + zipper.getType() + "\n";
        }
        PolyglotZipper next = zipper.down();
        while (!next.isNull()) {
            PolyglotTreePrinter nextp = new PolyglotTreePrinter("|  " + this.indent);
            nextp.process(next);
            this.result += nextp.getRes();
            next = next.right();
        }
    }

}
