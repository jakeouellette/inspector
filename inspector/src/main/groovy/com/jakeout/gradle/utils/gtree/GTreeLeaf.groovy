package com.jakeout.gradle.utils.gtree

public class GTreeLeaf extends GTree {
    public GTreeLeaf(String name) {
        super(name)
    }

    @Override
    public String toString() {
        return name
    }
}
