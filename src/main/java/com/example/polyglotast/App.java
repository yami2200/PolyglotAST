package com.example.polyglotast;

import java.io.IOException;


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

        String code = "import polyglot\n" +
        "polyglot.export_value(name=\"test\", value=3)\n" +
        "x = polyglot.import_value(name=\"test\")\n" +
        "print(x)\n" + 
        "polyglot.eval(language=\"js\", string=\"var x = 42;\"\n\"console.log(x);\")";
        // "polyglot.eval(string=\"var x = 42;\\n console.log(x); \", language=\"js\")";

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