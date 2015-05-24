package com.jakeout.gradle.inspector.tasks

import groovy.transform.Canonical

@Canonical
class TaskStats {
    public TaskDiffStats diffStats
    public TaskExecutionStats executionStats
}
