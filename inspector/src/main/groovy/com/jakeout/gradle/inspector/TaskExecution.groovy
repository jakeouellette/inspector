package com.jakeout.gradle.inspector

import groovy.transform.Canonical
import org.gradle.api.Task

@Canonical
class TaskExecution {
    public String path
    public String name
    public int filesTouched
    public int hunksAdded
    public int hunksRemoved
    public Map<String, Integer> changesByType

    public List<Task> dependsOnTasks
}
