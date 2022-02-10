package com.example.polyglotast;

import java.net.IDN;
import java.util.ArrayList;

import jsitter.api.Language;
import jsitter.api.NodeType;
import jsitter.api.Parser;
import jsitter.api.Tree;
import jsitter.api.Zipper;
import jsitter.api.Node;
import jsitter.api.StringText;
import jsitter.interop.JSitter;

public class MultiPolyglotZipper {
    
    protected ArrayList<PolyglotZipper> threads;

    public MultiPolyglotZipper(PolyglotZipper source) {
        this.threads = new ArrayList<>();
        this.threads.add(source);
    }

    public void goUp(int i) {
        this.threads.get(i).goUp();
    }

    public void goDown(int i) {
        this.threads.get(i).goDown();
    }

    public void goRight(int i) {
        this.threads.get(i).goRight();
    }

    public void goLeft(int i) {
        this.threads.get(i).goLeft();
    }
}
