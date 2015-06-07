package com.jakeout.gradle.inspector.tasks.model

import com.zutubi.diff.PatchFile
import groovy.transform.Canonical

@Canonical
class TaskDiffResults {
    public int filesTouched
    public int hunksAdded
    public int hunksRemoved
    public boolean anyUndeclaredChanges
    public Map<String, Integer> changesByType
    public Optional<PatchFile> patchFile
}
