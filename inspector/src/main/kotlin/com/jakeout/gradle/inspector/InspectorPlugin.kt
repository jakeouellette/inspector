package com.jakeout.gradle.inspector

import org.gradle.api.Plugin
import org.gradle.api.Project

class InspectorPlugin : Plugin<Project> {
    override fun apply(project: Project?) {
        if (project != null) {
            listener = InspectorGradleListener.setupFolderDiff(project)
        }
    }

    var listener: InspectorGradleListener? = null
}
