package com.example.polyglotast;

import java.nio.file.Path;
import java.util.HashSet;

public abstract class PolyglotDeepTreeProcessor implements PolyglotTreeProcessor {

    private static HashSet<String> listRootNodeType;

    protected boolean loopInSingleFile;

    static{
        listRootNodeType = new HashSet<>();
        listRootNodeType.add("program");
        listRootNodeType.add("module");
    }
    protected HashSet<Path> listPathsVisited;

    public HashSet<Path> getPathsCovered(){
        return this.listPathsVisited;
    }

    public PolyglotDeepTreeProcessor(){
        this.listPathsVisited = new HashSet<>();
    }

    @Override
    public void process(PolyglotZipper zipper) {
        this.loopInSingleFile = false;
        if(PolyglotTreeHandler.getfilePathOfTreeHandler().containsKey(zipper.getCurrentTree())) listPathsVisited.add(PolyglotTreeHandler.getfilePathOfTreeHandler().get(zipper.getCurrentTree()));

        this.processZipperNode(zipper);

        if(zipper.isEval() && PolyglotTreeHandler.getfilePathOfTreeHandler().containsKey(zipper.getCurrentTree())){
            if(zipper.down().isNull() || (listRootNodeType.contains(zipper.down().getType()) && zipper.getCurrentTree().equals(zipper.down().getCurrentTree()))){
                this.loopInSingleFile = true;
            }
        }

        PolyglotZipper next = zipper.down();
        Path nextPath = null;
        if(PolyglotTreeHandler.getfilePathOfTreeHandler().containsKey(next.getCurrentTree())) nextPath = PolyglotTreeHandler.getfilePathOfTreeHandler().get(next.getCurrentTree());
        while (!this.loopInSingleFile && !next.isNull() && !(zipper.getCurrentTree() != next.getCurrentTree() && nextPath != null && listPathsVisited.contains(nextPath))) {
            this.process(next);
            next = next.right();
        }
    }

    abstract public void processZipperNode(PolyglotZipper zipper);
}
