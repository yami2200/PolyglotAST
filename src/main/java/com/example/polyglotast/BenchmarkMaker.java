package com.example.polyglotast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class BenchmarkMaker {

    public BenchmarkMaker(){}

    /**
     * Run a benchmark if the arguments are proper
     * @param args arguments of the benchmark
     * @return a benchmark has been made
     */
    public boolean runBenchmark(String[] args) throws IOException {

        if(args.length == 4 && args[0].equals("bench_coldstart")){
            benchmark_cold_start(args);
            return true;
        }

        if(args.length == 5 && args[0].equals("bench_create")){
            benchmark_create(args);
            return true;
        }

        if(args.length == 5 && args[0].equals("bench_create_afterchanges")){
            benchmark_create_afterchanges(args);
            return true;
        }

        if(args.length == 5 && args[0].equals("bench_change")){
            benchmark_change(args);
            return true;
        }

        return false;
    }


    /**
     Measure the time to create the first polyglot tree of the process
     ARGS[0] == "bench_coldstart"
     ARGS[1] : filename
     ARGS[2] : path to store benchmark record data
     ARGS[3] : file programming language
     */
    public void benchmark_cold_start(String[] args) throws IOException {
        if(!args[0].equals("bench_coldstart") || args.length != 4) return;
        String filename = args[1];
        long timeElapsed;
        try{
            long start = System.currentTimeMillis();
            PolyglotTreeHandler tree = new PolyglotTreeHandler(Paths.get(filename), args[3]);
            long finish = System.currentTimeMillis();
            timeElapsed = finish - start;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        String csvPath = args[2]+"bench_coldstart_"+ Paths.get(filename).getFileName().toString().split("[.]")[0]+".csv";
        this.writeOrAppendInFile(csvPath, timeElapsed+"\n", "TIME\n"+timeElapsed+"\n");
    }

    /**
     Measure the time to create a same polyglot tree from scratch
     ARGS[0] == "bench_create"
     ARGS[1] : fileName
     ARGS[2] : path to store benchmark record data
     ARGS[3] : file programming language
     ARGS[4] : nb of iterations
     */
    public void benchmark_create(String[] args) throws IOException {
        if(!args[0].equals("bench_create") || args.length != 5) return;
        String filename = args[1];
        String csvPath = args[2]+"bench_create_"+ Paths.get(filename).getFileName().toString().split("[.]")[0]+".csv";
        long timeElapsed;

        try{
            PolyglotTreeHandler tree = new PolyglotTreeHandler(Paths.get(filename), args[3]);
            int iterations = Integer.parseInt(args[4]);
            for(int i = 0; i < iterations; i++){
                PolyglotTreeHandler.filePathOfTreeHandler.clear();
                PolyglotTreeHandler.filePathToTreeHandler.clear();
                System.out.println(i+1+"/"+iterations);
                long start = System.currentTimeMillis();
                PolyglotTreeHandler tree1 = new PolyglotTreeHandler(Paths.get(filename), args[3]);
                long finish = System.currentTimeMillis();
                timeElapsed = finish - start;
                this.writeOrAppendInFile(csvPath, timeElapsed+"\n", "TIME\n"+timeElapsed+"\n");
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     Measure the time to recreate a tree from scratch after a change made in a file
     ARGS[0] == "bench_create_afterchanges"
     ARGS[1] : fileName host
     ARGS[2] : path to store benchmark record data
     ARGS[3] : file host programming language
     ARGS[4] : fileName ChangeFile
     */
    public void benchmark_create_afterchanges(String[] args) throws IOException {
        if(!args[0].equals("bench_create_afterchanges") || args.length != 5) return;
        String filenameHost = args[1];
        String fileNameGuest = args[4];
        Path pathGuest = Paths.get(fileNameGuest);
        String csvPath = args[2]+"bench_create_afterchanges_"+ pathGuest.getFileName().toString().split("[.]")[0]+".csv";
        long timeElapsed = 0;

        try{
            PolyglotTreeHandler tree = new PolyglotTreeHandler(Paths.get(filenameHost), args[3]);

            Files.move(pathGuest, pathGuest.resolveSibling("originalguest.temp"));

            int iteration = 0;
            String[] fileNameSplit = pathGuest.getFileName().toString().split("[.]");
            File file = new File(pathGuest.getParent()+"/Changes/"+fileNameSplit[0]+"/"+fileNameSplit[0]+"_change"+iteration+"."+fileNameSplit[1]);
            int sumTime = 0;
            while(file.exists()){
                System.out.println("Changes:"+iteration);
                Files.copy(file.toPath(), pathGuest, StandardCopyOption.REPLACE_EXISTING);

                PolyglotTreeHandler.filePathOfTreeHandler.clear();
                PolyglotTreeHandler.filePathToTreeHandler.clear();

                long start = System.currentTimeMillis();
                PolyglotTreeHandler tree1 = new PolyglotTreeHandler(Paths.get(filenameHost), args[3]);
                long finish = System.currentTimeMillis();
                timeElapsed = finish - start;
                sumTime+=timeElapsed;

                iteration++;
                file = new File(pathGuest.getParent()+"/Changes/"+fileNameSplit[0]+"/"+fileNameSplit[0]+"_change"+iteration+"."+fileNameSplit[1]);
            }
            File guest = new File(pathGuest.toString());
            if(guest.delete()){
                File oldGuest = new File(pathGuest.getParent().resolve("originalguest.temp").toString());
                oldGuest.renameTo(guest);
            } else {
                System.out.println("ERROR DELETE : TEMP FILE STORE THE GUEST");
            }

            this.writeOrAppendInFile(csvPath, sumTime/iteration+"\n", "TIME\n"+sumTime/iteration+"\n");

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     Measure the time after a change in a file
     ARGS[0] == "bench_change"
     ARGS[1] : fileName host
     ARGS[2] : path to store benchmark record data
     ARGS[3] : file host programming language
     ARGS[4] : fileName ChangeFile
     */
    public void benchmark_change(String[] args){
        if(!args[0].equals("bench_change") || args.length != 5) return;
        String filenameHost = args[1];
        String fileNameGuest = args[4];
        Path pathGuest = Paths.get(fileNameGuest);
        String csvPath = args[2]+"bench_changes_"+ pathGuest.getFileName().toString().split("[.]")[0]+".csv";
        long timeElapsed = 0;

        try{
            PolyglotTreeHandler tree = new PolyglotTreeHandler(Paths.get(filenameHost), args[3]);

            int iteration = 0;
            String[] fileNameSplit = pathGuest.getFileName().toString().split("[.]");
            File file = new File(pathGuest.getParent()+"/Changes/"+fileNameSplit[0]+"/"+fileNameSplit[0]+"_change"+iteration+"."+fileNameSplit[1]);
            int sumTime = 0;
            while(file.exists()){
                System.out.println("Changes:"+iteration);
                String newCode = Files.readString(file.toPath());
                long start = System.currentTimeMillis();
                PolyglotTreeHandler.filePathToTreeHandler.get(pathGuest).reparsePolyglotTree(newCode);
                long finish = System.currentTimeMillis();
                timeElapsed = finish - start;
                sumTime+=timeElapsed;

                iteration++;
                file = new File(pathGuest.getParent()+"/Changes/"+fileNameSplit[0]+"/"+fileNameSplit[0]+"_change"+iteration+"."+fileNameSplit[1]);
            }

            this.writeOrAppendInFile(csvPath, sumTime/iteration+"\n", "TIME\n"+sumTime/iteration+"\n");

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }


    private void writeOrAppendInFile(String path, String appendText, String writeText) throws IOException {
        File f = new File(path);
        if(f.exists() && !f.isDirectory()) {
            BufferedWriter writer = new BufferedWriter(new FileWriter(path, true));
            writer.append(appendText);
            writer.close();
        } else {
            Files.createFile(Path.of(path));
            BufferedWriter writer = new BufferedWriter(new FileWriter(path));
            writer.write(writeText);
            writer.close();
        }
    }

}


