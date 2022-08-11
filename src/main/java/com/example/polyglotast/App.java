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

        PolyglotTreeHandler tree = new PolyglotTreeHandler(Paths.get("GraalSamples/functionusage.js"), "javascript");

        // USED TO GET INCONSISTENCIES
        PolyglotDUBuilder builder = new PolyglotDUBuilder();
        tree.apply(builder);

        // USED TO VISUALIZE THE POLYGLOT PROGRAM IN PLANT UML
        PolyglotTreeVisualizer visualizer = new PolyglotTreeVisualizer();
        tree.apply(visualizer);
        visualizer.save("fonctionusage.puml");

        // SPOT VARIABLES EXPORT & IMPORT
        PolyglotVariableSpotter variableSpotter = new PolyglotVariableSpotter();
        tree.apply(variableSpotter);

        System.out.println(variableSpotter.getImports());
        System.out.println(variableSpotter.getExports());

        // HOVER TEST
        Zipper<?> zip = tree.getNodeAtPosition(new Pair<>(4,4)); // Hover line 4 (counting from line 0) and character 4
        if(zip != null) System.out.println(tree.getNodePosition(zip).component1() + " : "+tree.getNodePosition(zip).component2() +" to "+tree.getNodePosition(zip).component1() + " : "+(tree.getNodePosition(zip).component2()+tree.nodeToCode(zip).length()));

        PolyglotTypeVisitor type = new PolyglotTypeVisitor(new PolyglotZipper(tree,zip));
        tree.apply(type);
        PolyglotTypeVisitor.TypingResult result = type.getTypeResult();
        System.out.println(result.typeResult);

    }
}