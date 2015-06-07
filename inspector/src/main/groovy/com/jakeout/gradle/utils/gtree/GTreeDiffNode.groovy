package com.jakeout.gradle.utils.gtree

import com.jakeout.gradle.utils.StringUtil

public class GTreeDiffNode extends GTreeNode implements HasDiff {

    HasDiff.Diff changeType

    public GTreeDiffNode(String name, List<GTree> children, HasDiff.Diff type) {
        super(name, children)
        this.changeType = type
    }

    @Override
    HasDiff.Diff getType() {
        return changeType
    }

    @Override
    public String toString() {
        return changeType.toString() + name + "{\n" +
                StringUtil.tabAppender(1).apply(" " + children.inject("", { i, c -> i + " " + c + "\n" })) + "}"
    }
}
