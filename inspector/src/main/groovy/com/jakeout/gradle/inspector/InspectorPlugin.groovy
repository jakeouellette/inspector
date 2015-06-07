package com.jakeout.gradle.inspector

import org.gradle.api.Plugin
import org.gradle.api.Project

class InspectorPlugin implements Plugin<Project> {

    InspectorGradleListener listener

    public void apply(Project project) {
        listener = InspectorGradleListener.setupFolderDiff(project)
    }
}