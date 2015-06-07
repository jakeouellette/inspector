package com.jakeout.gradle.utils.gtree

public class GTreeNodeBuilder implements GTreeBuilder {

    public final List<GTreeBuilder> subtrees

    public String name

    public static GTreeNodeBuilder create(String s, Closure c) {
        def builder = new GTreeNodeBuilder(s)
        c.delegate = builder
        c.resolveStrategy = Closure.DELEGATE_ONLY
        c()
        return builder
    }

    public GTreeNodeBuilder(String s) {
        this.name = s
        this.subtrees = new LinkedList<>()
    }

    // TODO: Value'd leaf builder via methodMissing(String, String)

    def methodMissing(String name, args) {
        if (args.length == 0) {
            subtrees.add(new GTreeLeafBuilder(name))
        } else {
            if (args[0] instanceof Closure) {
                subtrees.add(create(name, (Closure) args[0]))
            } else {
                // TODO: make into valued builder
                subtrees.add(new GTreeBuilder() {
                    @Override
                    GTree build() {
                        return new GTreeValuedLeaf(name, args[0].toString())
                    }
                })
            }
        }
    }

    @Override
    public GTree build() {
        return new GTreeNode(name, subtrees.collect { gtb -> gtb.build() })
    }
}
