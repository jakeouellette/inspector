package com.jakeout.gradle.inspector.tasks.model

import groovy.transform.Canonical

@Canonical
class AnalysisResult {
    public TaskDiffResults diffResults
    public TaskExecutionResults executionResults
    public Optional<TaskDiffResults> comparisonResults
}
