package com.example.polyglotast;

import com.sun.jna.Native;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Stack;
import java.util.stream.Stream;

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

        /*String code = "import polyglot\n" +
        "polyglot.export_value(name=\"test\", value=3)\n" +
        "polyglot.export_value(name=\"ex_not_im\", value=100)\n" +
        "x = polyglot.import_value(name=\"test\")\n" +
        "print(x)\n" +
        "y = polyglot.import_value(name=\"im_not_ex\")\n" +
        "print(y)\n" +
        "polyglot.eval(language=\"js\", string=\"var x = 42;\"\n\"console.log(x);\")";*/

        //cycle example
        String simplecycle = "import polyglot\n" + 
        "def my_python_function():" +
        "\tprint(\"hello from python\")" +
        "\tpolyglot.eval(language=\"js\", string=\"console.log(Polyglot.eval(\"python\", \"my_python_function\"))\")"
        ;

        String nocycle = "import polyglot;\n" + 
        "def my_python_function():" +
        "\tprint(\"hello from python\")" +
        "\tpolyglot.eval(language=\"js\", string=\"console.log(Polyglot.eval(\"python\", \"print()\"))\")" +
        "def second_function():" +
        "\tmy_python_function()"
        ;

        String import_export_inconsistency = "import polyglot\n" +
        "polyglot.export_value(name=\"test\", value=3);\n" +
        "polyglot.export_value(name=\"ex_not_im\", value=100);\n" +
        "x = polyglot.import_value(name=\"test\");\n" +
        "print(x);\n" +
        "y = polyglot.import_value(name=\"im_not_ex\");\n" +
        "print(y);\n" +
        "polyglot.eval(language=\"js\", string=\"var x = 42;\"\n\"console.log(x);\")"
        ;

        //String code = import_export_inconsistency;

        Stream<String> codestream = Files.lines(Paths.get("/home/robbert/PolyglotAST/src/main/pythonexample.txt"), StandardCharsets.UTF_8);
        StringBuilder sb = new StringBuilder();
        codestream.forEach(s -> sb.append(s).append("\n"));
        String code = sb.toString();
        codestream.close();

        System.out.println("evaluating code \n" + code);

        PolyglotTreeHandler tree = new PolyglotTreeHandler(code);
        PolyglotTreePrinter p = new PolyglotTreePrinter();
        tree.apply(p);
        System.out.println("AST : \n" + p.getRes());
        PolyglotDUBuilder du = new PolyglotDUBuilder();
        tree.apply(du);

        System.out.println("exports:" + du.getExports());
        System.out.println("imports:" + du.getImports());
        du.printInconsistencies();

        //PolyglotRenamer pr = new PolyglotRenamer("polyglot_call", "test", "test_renamed");
        //tree.apply(pr);
    }

}