package com.jakeout.gradle.inspector

import org.gradle.api.Task
import java.io.File

data class InspectorConfig(
        val cleanUpEachTask: Boolean,
        val compareBuild: Boolean,
        val showInspection: Boolean,
        val incrementalDir: File,
        val inspectionRoot: File,
        val projectBuildDir: File,
        val reportDir: File,
        val compareIncrementalDir: File,
        val compareInspectionRoot: File,
        val compareReportDir: File) {

    fun taskOut(task: Task) = File(incrementalDir, task.nameString() + ".diff")
    fun compareTaskOut(task: Task) = File(incrementalDir, task.nameString() + ".compare.diff")
    fun taskReport(task: Task) = task.nameString() + "-report.html"
    fun taskDir(task: Task) = File(incrementalDir, task.nameString())
    fun compareTaskDir(task: Task) = File(compareIncrementalDir, task.nameString())
    fun Task.nameString() = getName().replace(":", ".")
}
