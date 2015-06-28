package com.jakeout.gradle.inspector.tasks.model

import org.gradle.api.Task

data class TaskExecutionResults(
        val startTime: Long,
        val endTime: Long,
        val path: String,
        val name: String,
        val dependsOnTasks: List<Task>,
        val task: Task)
