package com.jakeout.gradle.inspector.tasks

import com.jakeout.gradle.inspector.InspectorConfig
import com.jakeout.gradle.inspector.tasks.model.AnalysisResult
import com.jakeout.gradle.inspector.tasks.model.TaskDiffResults
import com.jakeout.gradle.inspector.tasks.model.TaskExecutionResults
import com.jakeout.gradle.utils.DiffUtil
import com.zutubi.diff.PatchFile
import com.zutubi.diff.unified.UnifiedHunk
import com.zutubi.diff.unified.UnifiedPatch
import org.apache.commons.io.FileUtils
import org.apache.commons.io.FilenameUtils
import org.gradle.api.Task
import org.gradle.api.file.FileCollection
import org.gradle.api.logging.Logging
import org.gradle.api.tasks.TaskState
import java.util.HashMap
import java.util.LinkedList

class TaskAnalyzer(val config: InspectorConfig, val task: Task, val buildStarted: Long) {

    companion object {
        fun evaluateDiff(executionResults: TaskExecutionResults, patch: PatchFile?): TaskDiffResults {
            var filesTouched = 0
            var added = 0
            var removed = 0
            val changesByType = HashMap<String, Int>()

            var anyUndeclaredChanges = false
            if (patch != null) {
                val patches = patch.getPatches()
                for (p in patches) {
                    filesTouched++

                    val rootDirOfDeclaredOutput: String? = findOutput(p.getNewFile(), executionResults.task.getOutputs().getFiles())

                    if (rootDirOfDeclaredOutput == null) {
                        anyUndeclaredChanges = true
                    }

                    val extension = FilenameUtils.getExtension(p.getNewFile())

                    val count = changesByType.get(extension)
                    if (count == null) {
                        changesByType.put(extension, 1)
                    } else {
                        changesByType.put(extension, count + 1)
                    }

                    if (p is UnifiedPatch) {
                        for (h in p.getHunks()) {
                            for (l in h.getLines()) {
                                when (l.getType()) {
                                    UnifiedHunk.LineType.ADDED -> added++
                                    UnifiedHunk.LineType.DELETED -> removed++
                                    UnifiedHunk.LineType.COMMON -> {
                                    }
                                }
                            }
                        }
                    }
                }
            }

            return TaskDiffResults(
                    filesTouched = filesTouched,
                    hunksAdded = added,
                    hunksRemoved = removed,
                    anyUndeclaredChanges = anyUndeclaredChanges,
                    changesByType = changesByType,
                    patchFile = patch)
        }

        fun findOutput(file: String, files: FileCollection): String? {
            for (f in files) {
                if (file.equals(f.getAbsolutePath()) || file.startsWith(f.getAbsolutePath() + '/')) {
                    return f.getAbsolutePath();
                }
            }
            return null
        }
    }

    var results: AnalysisResult? = null

    fun onAfterExecute(state: TaskState): AnalysisResult? {
        try {
            val execution = getExecutionResults()

            val patchFile: PatchFile? = DiffUtil.diff(
                    config.projectBuildDir,
                    config.taskDir(task),
                    config.taskOut(task))

            val results = AnalysisResult(
                    diffResults = evaluateDiff(execution, patchFile),
                    executionResults = execution,
                    comparisonResults = getComparisonResults(execution))
            this.results = results
        } catch (e: Exception) {
            Logging.getLogger(javaClass<TaskAnalyzer>()).error("Analyzer failed to diff task: " + task.getName(), e)
        } finally {
            // Don't clean up, e.g., if the user wants to preserve this data to compare against the next build.
            if (config.cleanUpEachTask) {
                FileUtils.deleteDirectory(config.taskDir(task))
            }
        }
        return this.results
    }

    fun getComparisonResults(execution: TaskExecutionResults): TaskDiffResults? {
        if (!config.compareBuild) {
            return null
        } else {
            val compareTaskOut = config.compareTaskOut(task)
            val comparePatchFile = DiffUtil.diff(config.compareTaskDir(task), config.taskDir(task), compareTaskOut)
            return evaluateDiff(execution, comparePatchFile)
        }
    }

    fun getExecutionResults(): TaskExecutionResults {
        val endTime = System.currentTimeMillis()
        val dependsOnTasks = LinkedList<Task>()
        for (t in task.getDependsOn()) {
            if (t is Task) {
                dependsOnTasks.add(t)
            }
        }

        val tReportRel = config.taskReport(task)

        return TaskExecutionResults(
                name = task.getName(),
                path = tReportRel,
                dependsOnTasks = dependsOnTasks,
                task = task,
                startTime = buildStarted,
                endTime = endTime)
    }
}
