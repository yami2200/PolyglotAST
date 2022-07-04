package com.example.polyglotast;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;


public class App {

    public static void main(String[] args) throws IOException, URISyntaxException {

        String code = "import polyglot\n" +
                "polyglot.export_value(name=\"test\", value=3)\n" +
                "polyglot.export_value(name=\"ex_not_im\", value=100)\n" +
                "x = polyglot.import_value(name=\"test\")\n" +
                "print(x)\n" +
                // "polyglot.eval(language=\"js\", path=\"GraalSamples/arraysort.js\")";
                "y = polyglot.import_value(name=\"im_not_ex\")\n" +
                "print(y)\n" +
                "polyglot.eval(language=\"js\", string=\"var x = 42;\"\n\"console.log(x);\")";

        // cycle example
        String simplecycle = "import polyglot\n" +
                "def my_python_function():" +
                "\tprint(\"hello from python\")" +
                "\tpolyglot.eval(language=\"js\", string=\"console.log(Polyglot.eval(\"python\", \"my_python_function\"))\")";

        String nocycle = "import polyglot;\n" +
                "def my_python_function():" +
                "\tprint(\"hello from python\")" +
                "\tpolyglot.eval(language=\"js\", string=\"console.log(Polyglot.eval(\"python\", \"print()\"))\")" +
                "def second_function():" +
                "\tmy_python_function()";

        String import_export_inconsistency = "import polyglot\n" +
                "polyglot.export_value(name=\"test\", value=3);\n" +
                "polyglot.export_value(name=\"ex_not_im\", value=100);\n" +
                "x = polyglot.import_value(name=\"test\");\n" +
                "print(x);\n" +
                "y = polyglot.import_value(name=\"im_not_ex\");\n" +
                "print(y);\n" +
                "polyglot.eval(language=\"js\", string=\"var x = 42;\"\n\"console.log(x);\")";


        String code2 = "import polyglot\n" +
                "polyglot.export_value(name='name', value='test')\n" +
                "print(\"Hello from python\")\n" +
                "test = polyglot.import_value('name')\n" +
                "print(\"test\")\n" +
                "test = polyglot.import_value('test')\n" +
                "test = polyglot.import_value('aze')\n" +
                "test = polyglot.import_value('xwc')";

                System.out.println("evaluating the following code \n----------------------------------------------------\n"
                + code + "\n----------------------------------------------------");

        //PolyglotTreeHandler tree = new PolyglotTreeHandler(code2, "python");
        //PolyglotTreeHandler tree = new PolyglotTreeHandler(Paths.get("GraalSamples/arraysort.js"), "javascript");
        PolyglotTreeHandler tree = new PolyglotTreeHandler(Paths.get(new URI("file:///home/romain/Desktop/VScode%20test%20extension/test1_host.py")), "python");
        PolyglotTreePrinter p = new PolyglotTreePrinter();
        tree.apply(p);
        HashMap<Path, HashSet<FileNotFoundInfo>> filesNotFound = PolyglotTreeHandler.getGlobalFilesNotFound();
        for(Path path : filesNotFound.keySet()){
            for(FileNotFoundInfo notF : filesNotFound.get(path)){
                System.out.println(path.toString() + " : "+ notF.getFileName()+ " at line "+notF.line_position+", at char "+notF.char_position);
            }
        }
        //tree.printFilesNotFound();
        //System.out.println("AST : \n" + p.getRes());
        p.printUMLInFile("code_example.puml");
        PolyglotDUBuilder du = new PolyglotDUBuilder();
        tree.apply(du);
        //PolyglotVariableSpotter variableSpotter = new PolyglotVariableSpotter();
        //tree.apply(variableSpotter);

        System.out.println("exports:" + du.getExports());
        System.out.println("imports:" + du.getImports());
        System.out.println("imports false: "+du.getImportInconsistencies());
        System.out.println("exports false: "+du.getExportInconsistencies());
        //du.printInconsistencies();

    }

}