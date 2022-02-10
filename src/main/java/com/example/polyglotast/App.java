package com.example.polyglotast;

import com.sun.jna.Native;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Stack;

import jsitter.api.Language;
import jsitter.api.NodeType;
import jsitter.api.Parser;
import jsitter.api.Tree;
import jsitter.api.Zipper;
import jsitter.api.Node;
import jsitter.api.StringText;
import jsitter.interop.JSitter;


public class App {


    public static void main(String[] args) throws IOException {

        // String code = "import polyglot\n" 
        // + "x = 42\n" 
        // + "polyglot.export_value('test', x)\n"
        // + "polyglot.eval(language='js', string='var x = 42; Polyglot.eval(\"python\", \"print(x)\")')";
       Path srcPath = Path.of("/home/philemon/PROJET M1/Code/GraalSamples/polyglot_example.py");
       String code = Files.readString(srcPath);
       
        PolyglotTreeHandler tree = new PolyglotTreeHandler(code);
        PolyglotTreePrinter p = new PolyglotTreePrinter();
        tree.apply(p);
        System.out.println("AST : \n" + p.getRes());
        PolyglotDUBuilder du = new PolyglotDUBuilder();
        tree.apply(du);
        System.out.println(du.getExports());
        System.out.println(du.getImports());
        
        
    }

}