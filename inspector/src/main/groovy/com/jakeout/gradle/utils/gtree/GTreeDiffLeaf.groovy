package com.jakeout.gradle.utils.gtree


public class GTreeDiffLeaf extends GTreeLeaf implements HasDiff {

    public HasDiff.Diff changeType

    public String originalValueIfExisted

    public String newValueIfAny

    public GTreeDiffLeaf(String name, String originalValueIfExisted, String newValueIfAny, HasDiff.Diff changeType) {
        super(name)
        this.changeType = changeType
        this.originalValueIfExisted = originalValueIfExisted
        this.newValueIfAny = newValueIfAny

    }

    @Override
    HasDiff.Diff getType() {
        return changeType
    }


    @Override
    public String toString() {
        if (HasDiff.Diff.ADDED.equals(changeType)) {
            return "+" + name + " " + newValueIfAny
        } else if (HasDiff.Diff.REMOVED.equals(changeType)) {
            return "-" + name + " " + originalValueIfExisted
        } else if (HasDiff.Diff.CONTENTS_CHANGED.equals(changeType)) {
            return "~" + name + " " + originalValueIfExisted + " -> " + newValueIfAny
        } else {
            return name + " " + originalValueIfExisted
        }
    }
}
