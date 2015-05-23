package com.jakeout.gradle.inspector

import org.gradle.api.Task

class InspectorConfig {

    public final File incrementalDir

    public final File taskRoot

    public final File projectBuildDir

    public final File reportDir

    public InspectorConfig(File incrementalDir, File reportDir, File taskRoot, File projectBuildDir) {
        this.incrementalDir = incrementalDir
        this.taskRoot = taskRoot
        this.reportDir = reportDir
        this.projectBuildDir = projectBuildDir
    }
    public File taskOut(Task task) {
        new File(incrementalDir, task.name.replace(":", ".") + ".diff")
    }

    public String taskReport(Task task) {
        task.name.replace(":", ".") + "-report.html"
    }

    public File taskDir(Task task) {
        new File(incrementalDir, task.name.replace(":", "."))
    }
}
