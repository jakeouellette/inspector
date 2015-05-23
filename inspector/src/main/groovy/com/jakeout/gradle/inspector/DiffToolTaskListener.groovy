package com.jakeout.gradle.inspector

import com.zutubi.diff.PatchFile
import com.zutubi.diff.PatchFileParser
import com.zutubi.diff.unified.UnifiedPatchParser
import org.apache.commons.io.FileUtils
import org.gradle.api.Task
import org.gradle.api.tasks.TaskState

class DiffToolTaskListener {

    private final InspectorConfig config
    private final Task task
    private final long buildStarted

    public TaskStats taskStats = null

    public DiffToolTaskListener(InspectorConfig config, Task t, long buildStarted) {
        this.config = config
        this.task = t
        this.buildStarted = buildStarted
    }

    TaskStats afterExecute(Task task, TaskState state) {
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
            File tReport = new File(config.reportDir, tReportRel)
            TaskDiffStats diffStats
            TaskExecutionStats execution = new TaskExecutionStats(
                    name: task.name,
                    path: tReportRel,
                    dependsOnTasks: dependsOnTasks,
                    task: task,
                    startTime: buildStarted,
                    endTime: endTime)


            DiffTool.diff(config.projectBuildDir, tDir, tOut)
            Optional<PatchFile> patchFile = (tOut.exists() && tOut.size() > 0) ?
                    Optional.of(new PatchFileParser(new UnifiedPatchParser()).parse(new FileReader(tOut))) :
                    Optional.empty()

            DiffWriter writer = new DiffWriter(tReport, execution);
            diffStats = writer.write(patchFile)
            writer.close()

            FileUtils.deleteDirectory(config.taskDir(task))
            this.taskStats = new TaskStats(executionStats: execution, diffStats: diffStats)
        } catch (Throwable e) {
            e.printStackTrace()
        }
    }
}
