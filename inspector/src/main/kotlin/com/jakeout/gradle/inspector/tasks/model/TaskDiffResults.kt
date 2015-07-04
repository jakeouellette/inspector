package com.jakeout.gradle.inspector.tasks.model

import com.zutubi.diff.PatchType
import com.zutubi.diff.unified.UnifiedHunk

public enum class FileState { ADDED, DELETED, CHANGED, UNKNOWN }

data class FileDifference(
        val file: String,
        val comparedFile: String,
        val state: FileState)

data class FileContentsDifference(
        val file: String,
        val comparedFile: String,
        val type: PatchType,
        val changes: List<UnifiedHunk>)

data class TaskDiffResults(
        val filesTouched: Int,
        val hunksAdded: Int,
        val hunksRemoved: Int,
        val anyUndeclaredChanges: Boolean,
        val changesByType: Map<String, Int>,
        val changedContents: List<FileContentsDifference>,
        val changedFiles: List<FileDifference>)
