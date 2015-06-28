package com.jakeout.gradle.inspector.tasks.model

data class AnalysisResult(
        val diffResults: TaskDiffResults,
        val executionResults: TaskExecutionResults,
        val comparisonResults: TaskDiffResults?)
