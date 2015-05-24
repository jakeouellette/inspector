package com.jakeout.gradle.inspector

import com.jakeout.gradle.inspector.tasks.output.DiffWriter
import com.jakeout.gradle.inspector.tasks.output.IndexWriter
import com.jakeout.gradle.inspector.tasks.TaskAnalyzer
import com.jakeout.gradle.inspector.tasks.TaskExecutionStats
import com.jakeout.gradle.inspector.tasks.TaskStats
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

import java.awt.Desktop
import java.nio.file.Files
import java.nio.file.Path

class InspectorGradleListener implements TaskExecutionListener, BuildListener {

    private final static String PROFILE_PATH = "buildProfile"
    private final static String DIFF_INCREMENTAL = "diffIncremental"
    private final static String DIFF_REPORT = "report"

    private final List<TaskExecutionStats> changes = new LinkedList<TaskExecutionStats>()

    private final InspectorConfig config

    private final Map<String, TaskAnalyzer> taskAnalyzers = new LinkedHashMap<String, TaskAnalyzer>()
    private final Project project

    public static InspectorGradleListener setupFolderDiff(Project project) {
        def taskRoot = new File(project.projectDir, PROFILE_PATH)
        def config = new InspectorConfig(new File(taskRoot, DIFF_INCREMENTAL), new File(taskRoot, DIFF_REPORT), taskRoot, project.buildDir)

        FileUtils.deleteDirectory(config.reportDir)
        FileUtils.deleteDirectory(config.incrementalDir)
        FileUtils.deleteDirectory(config.taskRoot)
        FileUtils.forceMkdir(config.reportDir)
        FileUtils.forceMkdir(config.incrementalDir)

        def hook = new InspectorGradleListener(config, project)
        project.gradle.addListener(hook)
        hook
    }

    private InspectorGradleListener(InspectorConfig config, Project project) {
        this.config = config
        this.project = project
    }

    @Override
    void beforeExecute(Task task) {
        if(task.project.equals(project)) {
            // In case a clean task removed the build folder.
            FileUtils.forceMkdir(config.projectBuildDir)

            taskAnalyzers.put(task.name, new TaskAnalyzer(config, task, System.currentTimeMillis()))
            DiffUtil.backup(config.projectBuildDir, config.taskDir(task))
        }
    }

    @Override
    void afterExecute(Task task, TaskState taskState) {
        if(task.project.equals(project)) {
            def listener = taskAnalyzers.get(task.name)
            if (listener) {
                listener.onAfterExecute(taskState)
            } else {
                println("No task listener for :" + task.name)
            }
        }
    }

    public File getIndex() {
        return new File(config.reportDir, "index.html")
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

            Map<String, File> subprojectsByFile = new HashMap<String, File>()
            for (Project subProj : project.getSubprojects()) {
                def plugin = subProj.getPlugins().findPlugin(InspectorPlugin.class)
                if(plugin != null) {
                    def childListener = plugin.listener
                    subprojectsByFile.put(subProj.name, childListener.getIndex())
                }
            }

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
                    getIndex(),
                    subprojectsByFile,
                    sortedTasks,
                    new File(new File(config.reportDir, "vis"), "dag.js"))
            println "Build inspection written to file://${getIndex()}"
            if (project.hasProperty('showInspection')) {
                Desktop.getDesktop().browse(new URI("file://${getIndex()}"))
            }
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
