package com.jakeout.gradle.inspector.tasks.model

import com.zutubi.diff.PatchFile
import java.util.*

public enum class FileState { ADDED, DELETED, CHANGED, UNKNOWN }

data class TaskDiffResults(
        val filesTouched: Int,
        val hunksAdded: Int,
        val hunksRemoved: Int,
        val anyUndeclaredChanges: Boolean,
        val changesByType: Map<String, Int>,
        val patchFile: PatchFile?,
        val binaries: Map<String, FileState>)
