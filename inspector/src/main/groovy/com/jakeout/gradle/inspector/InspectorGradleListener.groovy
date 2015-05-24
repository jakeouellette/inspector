package com.jakeout.gradle.inspector

import com.jakeout.gradle.inspector.output.DiffWriter
import com.jakeout.gradle.inspector.output.IndexWriter
import com.jakeout.gradle.utils.DiffUtil
import org.apache.commons.io.FileUtils
import org.gradle.BuildListener
import org.gradle.BuildResult
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.execution.TaskExecutionListener
import org.gradle.api.initialization.Settings
import org.gradle.api.invocation.Gradle
import org.gradle.api.tasks.TaskState

import java.nio.file.Files
import java.nio.file.Path

class InspectorGradleListener implements TaskExecutionListener, BuildListener {

    private final static String PROFILE_PATH = "buildProfile"
    private final static String DIFF_INCREMENTAL = "diffIncremental"
    private final static String DIFF_REPORT = "report"

    private final List<TaskExecutionStats> changes = new LinkedList<TaskExecutionStats>()

    private final InspectorConfig config

    private final Map<String, TaskAnalyzer> taskAnalyzers = new LinkedHashMap<String, TaskAnalyzer>()

    public static void setupFolderDiff(Project project) {
        def taskRoot = new File(project.projectDir, PROFILE_PATH)
        def config = new InspectorConfig(new File(taskRoot, DIFF_INCREMENTAL), new File(taskRoot, DIFF_REPORT), taskRoot, project.buildDir)

        FileUtils.deleteDirectory(config.reportDir)
        FileUtils.deleteDirectory(config.incrementalDir)
        FileUtils.deleteDirectory(config.taskRoot)
        FileUtils.forceMkdir(config.reportDir)
        FileUtils.forceMkdir(config.incrementalDir)

        def hook = new InspectorGradleListener(config)
        project.gradle.addListener(hook)
        hook
    }

    private InspectorGradleListener(InspectorConfig config) {
        this.config = config
    }

    @Override
    void beforeExecute(Task task) {
        // In case a clean task removed the build folder.
        FileUtils.forceMkdir(config.projectBuildDir)

        taskAnalyzers.put(task.name, new TaskAnalyzer(config, task, System.currentTimeMillis()))
        DiffUtil.backup(config.projectBuildDir, config.taskDir(task))
    }

    @Override
    void afterExecute(Task task, TaskState taskState) {
        def listener = taskAnalyzers.get(task.name)
        if (listener) {
            listener.onAfterExecute(taskState)
        } else {
            println("No task listener for :" + task.name)
        }
    }

    @Override
    void buildFinished(BuildResult result) {
        try {
            FileUtils.forceMkdir(new File(config.reportDir, "vis"))
            // TODO: infer these dynamically
            makeFile("diff.css")
            makeFile("vis/d3.v3.min.js")
            makeFile("vis/dag.js")
            makeFile("vis/dagre-d3.js")
            makeFile("vis/jquery-1.9.1.min.js")
            makeFile("vis/tipsy.css")
            makeFile("vis/tipsy.js")

            Files.copy(this.getClass().getClassLoader().getResourceAsStream("vis/vis-report.html"),
                    new File(config.reportDir, "index.html").toPath())


            List<TaskStats> sortedTasks = new ArrayList<TaskStats>(
                    taskAnalyzers.values().collect { t -> t.taskStats })
                    .findResults { t -> t }
                    .sort { a, b -> Long.compare(a.executionStats.startTime, b.executionStats.startTime) }

            Map<String, List<String>> overlappingTasks = getOverlappingTasks(sortedTasks)

            for (TaskStats stats : sortedTasks) {
                DiffWriter.write(
                        new File(config.reportDir, stats.executionStats.path),
                        stats.executionStats,
                        stats.diffStats,
                        overlappingTasks.get(stats.executionStats.task.name))
            }

            IndexWriter.write(
                    new File(config.reportDir, "index.html"),
                    sortedTasks,
                    new File(new File(config.reportDir, "vis"), "dag.js"))
            println "Diff Report Written."
        } catch (Throwable e) {
            e.printStackTrace()
        }

    }

    public static Map<String, List<String>> getOverlappingTasks(List<TaskStats> taskStats) {
        Map<String, List<String>> overlappingTasks = new HashMap<String, List<String>>()

        for (TaskStats ts : taskStats) {
            def task1 = ts.executionStats.task.name
            overlappingTasks.put(task1, new LinkedList<String>())
            def task1Start = ts.executionStats.startTime
            def task1End = ts.executionStats.endTime
            for (TaskStats ts2 : taskStats) {
                def task2 = ts2.executionStats.task.name
                if (!task2.equals(task1)) {
                    def task2Start = ts2.executionStats.startTime
                    def task2End = ts2.executionStats.endTime

                    if ((task1Start > task2Start && task1Start < task2End) ||
                            (task1End > task2Start && task1End < task2End)) {

                        List<String> overlapping = overlappingTasks.get(task1)
                        overlapping.add(task2)
                    }
                }

            }
        }
        overlappingTasks
    }

    void makeFile(String path) {
        Path file = new File(config.reportDir, path).toPath()
        if (Files.exists(file)) {
            Files.delete(file)
        }
        Files.copy(
                this.getClass().getClassLoader().getResourceAsStream(path),
                file)

    }

    @Override
    void buildStarted(Gradle gradle) {

    }

    @Override
    void projectsEvaluated(Gradle gradle) {}

    @Override
    void projectsLoaded(Gradle gradle) {}

    @Override
    void settingsEvaluated(Settings settings) {}

}
