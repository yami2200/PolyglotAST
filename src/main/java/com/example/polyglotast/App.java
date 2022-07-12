package com.example.polyglotast;

import jsitter.api.Encoding;
import jsitter.api.Text;
import jsitter.api.Zipper;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
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

        System.out.println("evaluating the following code \n----------------------------------------------------\n" + code + "\n----------------------------------------------------");


        //PolyglotTreeHandler tree = new PolyglotTreeHandler(code2, "python");
        //PolyglotTreeHandler tree = new PolyglotTreeHandler(Paths.get("GraalSamples/arraysort.js"), "javascript");
        //PolyglotTreeHandler tree = new PolyglotTreeHandler(Paths.get(new URI("file:///home/romain/Desktop/VScode%20test%20extension/test1_host.py")), "python");
        /*PolyglotTreePrinter p = new PolyglotTreePrinter();
        tree.apply(p);*/
        //tree.printFilesNotFound();
        //System.out.println("AST : \n" + p.getRes());
        //p.printUMLInFile("code_example.puml");
        /*PolyglotDUBuilder du = new PolyglotDUBuilder();
        tree.apply(du);
        //PolyglotVariableSpotter variableSpotter = new PolyglotVariableSpotter();
        //tree.apply(variableSpotter);

        System.out.println("exports:" + du.getExports());
        System.out.println("imports:" + du.getImports());
        System.out.println("imports false: "+du.getImportInconsistencies());
        System.out.println("exports false: "+du.getExportInconsistencies());
        */
        /*String newCode ="import polyglot\n" +
                "#polyglot.eval(language='python', path='kfjiqsdfjsdiffazui.py')\n" +
                "#polyglot.eval(language='javascript', path='test1_1.js')\n" +
                "# test ajout milieu\n" +
                "test = polyglot.import_value(\"test\")\n" +
                "polyglot.export_value(name='video', value=3)\n" +
                "vie = polyglot.import_value('vie')";*/
        //String newCode = "# test ajout milieu\n";
        /*byte start = 0;
        byte end = 2;
        byte newend = (byte)(newCode.length()+2);
        tree.editPolyglotTree(newCode, start, end, newend);*/
        //String newCode = Files.readString(Paths.get(new URI("file:///home/romain/Desktop/VScode%20test%20extension/juel%20copy.js")));
        //tree.editPolyglotTree(newCode);
        //tree.apply(p);
        //System.out.println(p.getRes());
        //p.printUMLInFile("code_example_edited.puml");
        //System.out.println(tree.tree.getRoot().zipper().down() == zip);
        //du.printInconsistencies();
        /*PolyglotTreeHandler tree_cycle = new PolyglotTreeHandler(Paths.get(new URI("file:///home/romain/Desktop/VScode%20test%20extension/test_cycle.js")), "javascript");
        PolyglotTreePrinter p = new PolyglotTreePrinter();
        tree_cycle.apply(p);
        p.printUMLInFile("cyclic.puml");
        System.out.println(tree_cycle.getSubTrees());

        System.out.println(" TREES WITH SAME GUEST");

        PolyglotTreeHandler tree1 = new PolyglotTreeHandler(Paths.get(new URI("file:///home/romain/Desktop/VScode%20test%20extension/host.js")), "javascript");
        PolyglotTreeHandler tree2 = new PolyglotTreeHandler(Paths.get(new URI("file:///home/romain/Desktop/VScode%20test%20extension/host.py")), "python");
        tree1.apply(p);
        p.printUMLInFile("guestx2_host1.puml");
        tree2.apply(p);
        p.printUMLInFile("guestx2_host2.puml");
        System.out.println(tree1.getSubTrees());
        System.out.println(tree2.getSubTrees());
        PolyglotTreeHandler tree_guest = PolyglotTreeHandler.filePathToTreeHandler.get(Paths.get(new URI("file:///home/romain/Desktop/VScode%20test%20extension/guestx2.py")));
        tree_guest.reparsePolyglotTree("import polyglot\n" +
                "# File called by differents Host :)\n" +
                "print(\"c'est print !\")\n" +
                "polyglot.eval(language='python', path='guestx2_guest.py')");
        tree1.apply(p);
        p.printUMLInFile("guestx2_host1_after_edit.puml");
        tree2.apply(p);
        p.printUMLInFile("guestx2_host2_after_edit.puml");

        System.out.println(" TREE BASIC WITH 3 FILES");*/

        PolyglotTreePrinter p = new PolyglotTreePrinter();
        PolyglotTreeHandler tree = new PolyglotTreeHandler(Paths.get(new URI("file:///home/romain/Desktop/VScode%20test%20extension/example4_Cyclic_ImportExportTest/Part2.js")), "javascript");
        tree.apply(p);
        p.printUMLInFile("cycle_importexport.puml");
        PolyglotDUBuilder du = new PolyglotDUBuilder();
        tree.apply(du);
    }



}