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

        String code = "import polyglot\n" +
        "polyglot.export_value(name=\"test\", value=3)\n" +
        "polyglot.export_value(name=\"ex_not_im\", value=100)\n" +
        "x = polyglot.import_value(name=\"test\")\n" +
        "print(x)\n" +
        "y = polyglot.import_value(name=\"im_not_ex\")\n" +
        "print(y)\n" +
        "polyglot.eval(language=\"js\", string=\"var x = 42;\"\n\"console.log(x);\")";

        System.out.println(code + "\n\n");

        PolyglotTreeHandler tree = new PolyglotTreeHandler(code);
        PolyglotTreePrinter p = new PolyglotTreePrinter();
        tree.apply(p);
        System.out.println("AST : \n" + p.getRes());
        PolyglotDUBuilder du = new PolyglotDUBuilder();
        tree.apply(du);

        System.out.println("exports:" + du.getExports());
        System.out.println("imports:" + du.getImports());
        du.printInconsistencies();
        du.printCycles();
    }

}