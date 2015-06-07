package com.jakeout.gradle.inspector.tasks

import com.jakeout.gradle.inspector.InspectorConfig
import com.jakeout.gradle.utils.DiffUtil
import com.zutubi.diff.Patch
import com.zutubi.diff.PatchFile
import com.zutubi.diff.unified.UnifiedHunk
import com.zutubi.diff.unified.UnifiedPatch
import org.apache.commons.io.FileUtils
import org.apache.commons.io.FilenameUtils
import org.gradle.api.Task
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.TaskState

class TaskAnalyzer {

    private final InspectorConfig config
    private final Task task
    private final long buildStarted

    public TaskStats taskStats = null

    public TaskAnalyzer(InspectorConfig config, Task t, long buildStarted) {
        this.config = config
        this.task = t
        this.buildStarted = buildStarted
    }

    public TaskStats onAfterExecute(TaskState state) {
        try {
            long endTime = System.currentTimeMillis()
            List<Task> dependsOnTasks = new LinkedList<Task>()
            task.getDependsOn().each { t ->
                if (t instanceof Task) {
                    dependsOnTasks.add((Task) t)
                }
            }

            def tDir = config.taskDir(task)
            def tOut = config.taskOut(task)
            String tReportRel = config.taskReport(task)
            TaskDiffStats diffStats
            TaskExecutionStats execution = new TaskExecutionStats(
                    name: task.name,
                    path: tReportRel,
                    dependsOnTasks: dependsOnTasks,
                    task: task,
                    startTime: buildStarted,
                    endTime: endTime)


            Optional<PatchFile> patchFile = DiffUtil.diff(config.projectBuildDir, tDir, tOut)

            Optional<TaskDiffStats> compareStats = Optional.empty()
            if (config.compareBuild) {
                def compareTaskOut = config.compareTaskOut(task)
                Optional<PatchFile> comparePatchFile = DiffUtil.diff(config.compareTaskDir(task), tDir, compareTaskOut)
                compareStats = Optional.of(evaluateDiff(execution, comparePatchFile))
            }


            diffStats = evaluateDiff(execution, patchFile)
            this.taskStats = new TaskStats(executionStats: execution, diffStats: diffStats, compareStats: compareStats)
        } catch (Throwable e) {
            e.printStackTrace()
        } finally {
            if (config.cleanUpEachTask) {
                FileUtils.deleteDirectory(config.taskDir(task))
            }
        }
    }

    public static TaskDiffStats evaluateDiff(TaskExecutionStats executionStats, Optional<PatchFile> patch) {
        int filesTouched = 0
        int added = 0
        int removed = 0
        def changesByType = new HashMap<String, Integer>()

        boolean anyUndeclaredChanges = false
        if (patch.isPresent()) {
            List<Patch> patches = patch.get().getPatches()
            for (Patch p : patches) {
                filesTouched++

                Optional<String> rootDirOfDeclaredOutput = findOutput(p.newFile, executionStats.task.getOutputs().files)

                if (!rootDirOfDeclaredOutput.isPresent()) {
                    anyUndeclaredChanges = true
                }

                def extension = FilenameUtils.getExtension(p.newFile)

                Integer count = changesByType.get(extension)
                if (count == null) {
                    changesByType.put(extension, 1)
                } else {
                    changesByType.put(extension, count + 1)
                }

                if (p instanceof UnifiedPatch) {
                    UnifiedPatch up = (UnifiedPatch) p
                    for (UnifiedHunk h : up.getHunks()) {
                        for (UnifiedHunk.Line l : h.getLines()) {
                            switch (l.type) {
                                case UnifiedHunk.LineType.ADDED:
                                    added++
                                    break;
                                case UnifiedHunk.LineType.DELETED:
                                    removed++
                                    break;
                                case UnifiedHunk.LineType.COMMON:
                                    break;
                            }
                        }
                    }
                }
            }
        }

        new TaskDiffStats(
                filesTouched: filesTouched,
                hunksAdded: added,
                hunksRemoved: removed,
                anyUndeclaredChanges: anyUndeclaredChanges,
                changesByType: changesByType,
                patchFile: patch)
    }

    public static Optional<String> findOutput(String file, FileCollection files) {
        for (File f : files) {
            if (file.equals(f.absolutePath) || file.startsWith(f.absolutePath + '/')) {
                return Optional.of(f);
            }
        }
        Optional.empty()
    }
}
