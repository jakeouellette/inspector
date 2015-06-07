package com.jakeout.gradle.utils.gtree

public class GTreeNode extends GTree {

    public List<GTree> children

    public GTreeNode(String name, List<GTree> children) {
        super(name)
        this.children = children
    }

    // TODO: this is a bit inefficient a lookup
    def propertyMissing(String name) {
        for (GTree child : children) {
            if (child.name.equals(name)) {
                return child
            }
        }
        throw new IllegalArgumentException(name)
    }

    @Override
    public String toString() {
        return name + "{\n" + children.inject("", { i, c -> i + " " + c + "\n" }) + "}"
    }
}