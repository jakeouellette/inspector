package com.jakeout.gradle.utils.gtree

public class GTreeValuedLeaf extends GTree {
    public final String value

    public GTreeValuedLeaf(String name, String value) {
        super(name)
        this.value = value;
    }


    @Override
    public String toString() {
        return name + " " + value
    }
}