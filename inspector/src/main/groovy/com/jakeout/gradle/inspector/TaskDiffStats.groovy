package com.jakeout.gradle.inspector

import groovy.transform.Canonical

@Canonical
class TaskDiffStats {
    public int filesTouched
    public int hunksAdded
    public int hunksRemoved
    public boolean anyUndeclaredChanges
    public Map<String, Integer> changesByType

    public TaskDiffStats() {
        filesTouched = 0
        hunksAdded = 0
        hunksRemoved = 0
        anyUndeclaredChanges = false
        changesByType = new HashMap<String, Integer>()
    }
}
