package com.jakeout.gradle.inspector

import groovy.transform.Canonical
import org.gradle.api.Task

@Canonical
class InspectorConfig {

    public boolean cleanUpEachTask

    public boolean compareBuild

    public boolean showInspection

    public File incrementalDir

    public File inspectionRoot

    public File projectBuildDir

    public File reportDir

    public File compareIncrementalDir

    public File compareInspectionRoot

    public File compareReportDir

    public File taskOut(Task task) {
        new File(incrementalDir, task.name.replace(':', '.') + '.diff')
    }

    /**
     *  New incremental dir to compare against the old.
     */
    public File compareTaskOut(Task task) {
        new File(incrementalDir, task.name.replace(':', '.') + '.compare.diff')
    }

    public String taskReport(Task task) {
        task.name.replace(':', '.') + '-report.html'
    }

    public File taskDir(Task task) {
        new File(incrementalDir, task.name.replace(':', '.'))
    }

    public File compareTaskDir(Task task) {
        new File(compareIncrementalDir, task.name.replace(':', '.'))
    }
}
