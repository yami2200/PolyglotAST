package com.example.polyglotast;

import jsitter.api.Zipper;
import kotlin.Pair;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.sql.SQLOutput;


public class App {

    public static void main(String[] args) throws IOException, URISyntaxException {

        BenchmarkMaker benchmarkMaker = new BenchmarkMaker();
        if(benchmarkMaker.runBenchmark(args)) return;

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
        //PolyglotTreeHandler tree = new PolyglotTreeHandler(Paths.get("GraalSamples/functionusage.js"), "javascript");
        /*PolyglotTreeHandler tree = new PolyglotTreeHandler(Paths.get(new URI("file:///home/romain/Desktop/VScode%20test%20extension/test_singlefile.js")), "javascript");
        PolyglotDUBuilder builder = new PolyglotDUBuilder();
        tree.apply(builder);
        PolyglotTreeVisualizer visualizer = new PolyglotTreeVisualizer();
        tree.apply(visualizer);
        visualizer.save("singleFileLoop.puml");*/
        /*PolyglotTreeHandler tree = new PolyglotTreeHandler(Paths.get(new URI("file:///home/romain/Desktop/VScode%20test%20extension/test1_host.py")), "python");
        PolyglotVariableSpotter variableSpotter = new PolyglotVariableSpotter();
        tree.apply(variableSpotter);
        System.out.println(variableSpotter.getImports());
        System.out.println(variableSpotter.getExports());*/
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

        /*PolyglotTreeHandler tree = new PolyglotTreeHandler(Paths.get(new URI("file:///home/romain/Desktop/VScode%20test%20extension/example3_multihost_diag/host1.py")), "python");
        PolyglotTreeVisualizer visualizer = new PolyglotTreeVisualizer();
        tree.apply(visualizer);
        visualizer.save("multihost_v2.puml");*/

        /*PolyglotTreeHandler tree;
        long start = System.currentTimeMillis();
        //tree = new PolyglotTreeHandler(Paths.get(new URI("file:///home/romain/Desktop/VScode%20test%20extension/test1_host.py")), "python");
        long finish = System.currentTimeMillis();
        long timeElapsed = finish - start;
        System.out.println(timeElapsed);*/

        /*start = System.currentTimeMillis();
        PolyglotTreeHandler.filePathToTreeHandler = new HashMap<>();
        PolyglotTreeHandler.filePathOfTreeHandler = new HashMap<>();
        tree = new PolyglotTreeHandler(Paths.get(new URI("file:///home/romain/Desktop/VScode%20test%20extension/test1_host.py")), "python");
        finish = System.currentTimeMillis();
        timeElapsed = finish - start;
        System.out.println(timeElapsed);

        start = System.currentTimeMillis();
        PolyglotTreeHandler.filePathToTreeHandler = new HashMap<>();
        PolyglotTreeHandler.filePathOfTreeHandler = new HashMap<>();
        tree = new PolyglotTreeHandler(Paths.get(new URI("file:///home/romain/Desktop/VScode%20test%20extension/test1_host.py")), "python");
        finish = System.currentTimeMillis();
        timeElapsed = finish - start;
        System.out.println(timeElapsed);*/

        /*System.out.println("JAVSCRIPT");

        start = System.currentTimeMillis();
        PolyglotTreeHandler.filePathToTreeHandler = new HashMap<>();
        PolyglotTreeHandler.filePathOfTreeHandler = new HashMap<>();
        tree = new PolyglotTreeHandler(Paths.get("/home/romain/Desktop/Benchmarks Polyglot AST/Files/polyglot_4files/4files_host.py"), "python");
        System.out.println(tree.getSubTrees());
        //tree = new PolyglotTreeHandler(Paths.get(new URI("file:///home/romain/Desktop/VScode%20test%20extension/example3_multihost_diag/host3.js")), "javascript");
        finish = System.currentTimeMillis();
        timeElapsed = finish - start;
        System.out.println(timeElapsed);*/

        /*for(int i = 0; i<100; i++){
            start = System.currentTimeMillis();
            PolyglotTreeHandler.filePathToTreeHandler = new HashMap<>();
            PolyglotTreeHandler.filePathOfTreeHandler = new HashMap<>();
            tree = new PolyglotTreeHandler(Paths.get("/home/romain/Desktop/Benchmarks Polyglot AST/Files/polyglot_2files/2files_host_1,6k.py"), "python");
            //tree = new PolyglotTreeHandler(Paths.get("eval/client.py"), "python");
            //tree = new PolyglotTreeHandler(Paths.get(new URI("file:///home/romain/Desktop/VScode%20test%20extension/example3_multihost_diag/host3.js")), "javascript");
            finish = System.currentTimeMillis();
            timeElapsed = finish - start;
            System.out.println(timeElapsed);
        }*/

        /*PolyglotVariableSpotter varSpotter = new PolyglotVariableSpotter();
        tree.apply(varSpotter);*/

        // Incremental Test
        PolyglotTreeHandler tree = new PolyglotTreeHandler(Paths.get(new URI("file:///home/romain/Desktop/VScode%20test%20extension/example0_vis/host.py")), "python");
        Zipper<?> zip = tree.getNodeAtPosition(new Pair<>(3,0));
        System.out.println((zip == null ? "null" : zip.getType() + " - " + new PolyglotZipper(tree, zip).getCode()));
        if(zip != null) System.out.println(tree.getNodePosition(zip).component1() + " : "+tree.getNodePosition(zip).component2() +" to "+tree.getNodePosition(zip).component1() + " : "+(tree.getNodePosition(zip).component2()+tree.nodeToCode(zip).length()));

        PolyglotTypeVisitor type = new PolyglotTypeVisitor(new PolyglotZipper(tree,zip));
        tree.apply(type);
        PolyglotTypeVisitor.TypingResult result = type.getTypeResult();
        System.out.println(result.type);
        System.out.println(result.typeResult);

        PolyglotTreeVisualizer vis = new PolyglotTreeVisualizer();
        tree.apply(vis);
        vis.save("test.puml");


        /*tree.reparsePolyglotTree(new Pair<>(1,0), new Pair<>(1,0), "//");
        tree.apply(vis);
        vis.save("vis2.puml");*/

        /*
        PolyglotTreeHandler tree = new PolyglotTreeHandler(Paths.get(new URI("file:///home/romain/Desktop/VScode%20test%20extension/solo_80k.js")), "javascript");
        for(int i = 0; i<10; i++){

            tree.reparsePolyglotTree(new Pair<>(21,0), new Pair<>(21,0), "//");

            tree = new PolyglotTreeHandler(Paths.get(new URI("file:///home/romain/Desktop/VScode%20test%20extension/solo_80k.js")), "javascript");
        }

        for(int i = 0; i<10; i++){
            String newCode = Files.readString(Paths.get(new URI("file:///home/romain/Desktop/VScode%20test%20extension/solo_80k.js")));
            long start = System.currentTimeMillis();
            tree.reparsePolyglotTree(newCode);
            long finish = System.currentTimeMillis();
            long timeElapsed = finish - start;
            System.out.println(timeElapsed);
            tree = new PolyglotTreeHandler(Paths.get(new URI("file:///home/romain/Desktop/VScode%20test%20extension/solo_80k.js")), "javascript");
        }
        */
    }
}