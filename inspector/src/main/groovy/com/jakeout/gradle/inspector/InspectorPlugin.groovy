package com.jakeout.gradle.inspector

import org.gradle.api.Plugin
import org.gradle.api.Project

class InspectorPlugin implements Plugin<Project> {

    public void apply(Project project) {
        InspectorGradleListener.setupFolderDiff(project)
    }
}