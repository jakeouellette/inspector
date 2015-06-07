package com.jakeout.gradle.utils.gtree

class GTreeLeafBuilder implements GTreeBuilder {

    String name

    public GTreeLeafBuilder(String name) {
        this.name = name
    }

    @Override
    public GTree build() {
        return new GTreeLeaf(name)
    }
}
