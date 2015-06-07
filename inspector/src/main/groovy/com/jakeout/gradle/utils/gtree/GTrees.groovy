package com.jakeout.gradle.utils.gtree

public class GTrees {

    public static GTree create(String s, Closure c) {
        GTreeNodeBuilder.create(s, c).build()
    }

    public static GTree createTemplate(Closure c) {
        GTreeNodeBuilder.create("template", c).build()
    }

    public static GTree extract(GTree template, Object o) {
        extractNode((GTreeNode)template, o)
    }

    public static GTree extractNode(GTreeNode parameterTree, Object value) {
        List<GTree> children = new LinkedList<>()
        String name = parameterTree.name
        for (GTree tree : parameterTree.children) {
            children.add(extractRecursive(tree, value))
        }
        return new GTreeNode(name, children)
    }

    private static GTree extractRecursive(GTree parameterTree, Object o) {
        Object value = o.("${parameterTree.name}")
        if (parameterTree instanceof GTreeLeaf) {
            new GTreeValuedLeaf(parameterTree.name, String.valueOf(value))
        } else if (parameterTree instanceof GTreeNode) {
            extractNode(parameterTree, value)
        } else {
            throw new IllegalArgumentException("Tree type was invalid: " + parameterTree.class)
        }
    }

    public static GTree diff(GTree valuedTree, GTree comparedTree) {
        if (valuedTree == null) {
            if (comparedTree instanceof GTreeValuedLeaf) {
                GTreeValuedLeaf second = (GTreeValuedLeaf) comparedTree
                return new GTreeDiffLeaf(second.name, null, second.value, HasDiff.Diff.ADDED)
            } else if (comparedTree instanceof GTreeNode) {
                GTreeNode second = (GTreeNode) comparedTree
                return new GTreeDiffNode(second.name, second.children.collect { c -> diff(null, c) }, HasDiff.Diff.ADDED)
            } else {
                throw new IllegalArgumentException("cannot compare two nulls.")
            }
        }

        if (valuedTree instanceof GTreeValuedLeaf) {
            if (comparedTree == null) {
                GTreeValuedLeaf first = (GTreeValuedLeaf) valuedTree
                return new GTreeDiffLeaf(first.name, first.value, null, HasDiff.Diff.REMOVED)
            }

            if (comparedTree instanceof GTreeValuedLeaf) {
                GTreeValuedLeaf first = (GTreeValuedLeaf) valuedTree
                GTreeValuedLeaf second = (GTreeValuedLeaf) comparedTree
                if (first.value.equals(second.value)) {
                    return new GTreeDiffLeaf(first.name, first.value, null, HasDiff.Diff.UNCHANGED)
                } else {
                    return new GTreeDiffLeaf(first.name, first.value, second.value, HasDiff.Diff.CONTENTS_CHANGED)
                }
            } else if (comparedTree instanceof GTreeNode) {
                throw new IllegalArgumentException("Leaves cannot change into nodes")
            }
        } else if (valuedTree instanceof GTreeNode) {
            if (comparedTree == null) {
                GTreeNode first = (GTreeNode) valuedTree
                return new GTreeDiffNode(first.name, first.children.collect { c -> diff(c, null) }, HasDiff.Diff.REMOVED)
            }

            if (comparedTree instanceof GTreeNode) {

                GTreeNode first = (GTreeNode) valuedTree
                GTreeNode second = (GTreeNode) comparedTree
                List<GTree> childrenAccountedFor = new LinkedList<>(first.children)
                List<GTree> childrenAccountedFor2 = new LinkedList<>(second.children)

                List<GTree> childComparisons = new LinkedList<>()
                base:
                Iterator<GTree> children1 = childrenAccountedFor.iterator()
                while (children1.hasNext()) {
                    GTree child = children1.next()
                    Iterator<GTree> children2 = childrenAccountedFor2.iterator()
                    while (children2.hasNext()) {
                        GTree child2 = children2.next()
                        if (child.name.equals(child2.name)) {
                            children1.remove()
                            children2.remove()
                            childComparisons.add(diff(child, child2))
                            continue base;
                        }

                    }
                }

                for (GTree child : childrenAccountedFor) {
                    childComparisons.add(diff(child, null))
                }

                for (GTree child : childrenAccountedFor2) {
                    childComparisons.add(diff(null, child))
                }

                return new GTreeDiffNode(first.name, childComparisons, HasDiff.Diff.UNCHANGED)
            } else if (comparedTree instanceof GTreeValuedLeaf) {
                throw new IllegalArgumentException("Nodes cannot change into leaves")
            } else {
                throw new IllegalArgumentException("Trees must be nodes or kv pairs")
            }
        } else {
            throw new IllegalArgumentException("Tree already has value for key.")
        }
    }
}
