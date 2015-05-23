package com.jakeout.gradle.inspector

import groovy.transform.Canonical
import org.gradle.api.Task

@Canonical
class TaskExecutionStats {
    public long startTime
    public long endTime
    public String path
    public String name
    public List<Task> dependsOnTasks
    public Task task
}
