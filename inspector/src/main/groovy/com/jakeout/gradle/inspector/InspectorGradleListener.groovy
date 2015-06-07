package com.jakeout.gradle.inspector

import com.jakeout.gradle.inspector.tasks.TaskAnalyzer
import com.jakeout.gradle.inspector.tasks.model.TaskExecutionResults
import com.jakeout.gradle.inspector.tasks.model.AnalysisResult
import com.jakeout.gradle.inspector.tasks.output.DiffWriter
import com.jakeout.gradle.inspector.tasks.output.IndexWriter
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

import java.awt.*
import java.nio.file.Files
import java.nio.file.Path
import java.util.List

class InspectorGradleListener implements TaskExecutionListener, BuildListener {

    private final static String SHOW_INSPECTION_PROPERTY = 'showInspection'
    private final static String COMPARE_LAST_BUILD_PROPERTY = 'compareLastBuild'
    private final static String PROFILE_PATH = 'buildProfile'
    private final static String DIFF_INCREMENTAL = 'diffIncremental'
    private final static String DIFF_REPORT = 'report'
    private final static String PROFILE_PATH_COMPARE = 'buildProfileCompare'

    private final List<TaskExecutionResults> changes = new LinkedList<TaskExecutionResults>()

    private final InspectorConfig config

    private final Map<String, TaskAnalyzer> taskAnalyzers = new LinkedHashMap<String, TaskAnalyzer>()
    private final Project project

    public static InspectorGradleListener setupFolderDiff(Project project) {

        def inspectionRoot = new File(project.projectDir, PROFILE_PATH)
        def compareInspectionRoot = new File(project.projectDir, PROFILE_PATH_COMPARE)

        def incrementalDir = new File(inspectionRoot, DIFF_INCREMENTAL)
        def compareIncrementalDir = new File(compareInspectionRoot, DIFF_INCREMENTAL)

        def reportDir = new File(inspectionRoot, DIFF_REPORT)
        def compareReportDir = new File(compareInspectionRoot, DIFF_REPORT)

        boolean cleanUpEachTask = true
        boolean compareBuild = false
        boolean showInspection = project.hasProperty(SHOW_INSPECTION_PROPERTY)

        if (project.hasProperty(COMPARE_LAST_BUILD_PROPERTY)) {
            compareBuild = true
            // needed to save the diff-comparison for the next run.
            cleanUpEachTask = false

        }

        def config = new InspectorConfig(
                incrementalDir: incrementalDir,
                reportDir: reportDir,
                inspectionRoot: inspectionRoot,
                projectBuildDir: project.buildDir,
                cleanUpEachTask: cleanUpEachTask,
                compareBuild: compareBuild,
                compareInspectionRoot: compareInspectionRoot,
                compareIncrementalDir: compareIncrementalDir,
                compareReportDir: compareReportDir,
                showInspection: showInspection)

        if (config.compareBuild) {
            FileUtils.deleteDirectory(config.compareInspectionRoot)
            FileUtils.moveDirectory(config.inspectionRoot, config.compareInspectionRoot)
        }

        FileUtils.deleteDirectory(config.reportDir)
        FileUtils.deleteDirectory(config.incrementalDir)
        FileUtils.deleteDirectory(config.inspectionRoot)
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
        if (task.project.equals(project)) {
            // In case a clean task removed the build folder.
            FileUtils.forceMkdir(config.projectBuildDir)

            taskAnalyzers.put(task.name, new TaskAnalyzer(config, task, System.currentTimeMillis()))
            DiffUtil.backup(config.projectBuildDir, config.taskDir(task))
        }
    }

    @Override
    void afterExecute(Task task, TaskState taskState) {
        if (task.project.equals(project)) {
            def listener = taskAnalyzers.get(task.name)
            if (listener) {
                listener.onAfterExecute(taskState)
            } else {
                println("No task listener for : $task.name")
            }
        }
    }

    public File getIndex() {
        return new File(config.reportDir, "index.html")
    }

    @Override
    void buildFinished(BuildResult buildResult) {
        try {
            FileUtils.forceMkdir(new File(config.reportDir, 'vis'))
            // TODO: infer these dynamically
            makeFile('diff.css')
            makeFile('vis/d3.v3.min.js')
            makeFile('vis/dag.js')
            makeFile('vis/dagre-d3.js')
            makeFile('vis/jquery-1.9.1.min.js')
            makeFile('vis/tipsy.css')
            makeFile('vis/tipsy.js')

            makeFile('font/css/font-awesome.css')
            makeFile('font/css/font-awesome.min.css')
            makeFile('font/fonts/FontAwesome.otf')
            makeFile('font/fonts/fontawesome-webfont.eot')
            makeFile('font/fonts/fontawesome-webfont.svg')
            makeFile('font/fonts/fontawesome-webfont.ttf')
            makeFile('font/fonts/fontawesome-webfont.woff')
            makeFile('font/fonts/fontawesome-webfont.woff2')

            Map<String, File> subprojectsByFile = new HashMap<String, File>()
            for (Project subProj : project.getSubprojects()) {
                def plugin = subProj.getPlugins().findPlugin(InspectorPlugin.class)
                if (plugin != null) {
                    def childListener = plugin.listener
                    subprojectsByFile.put(subProj.name, childListener.getIndex())
                }
            }

            Files.copy(this.getClass().getClassLoader().getResourceAsStream('vis-report.html'),
                    new File(config.reportDir, 'index.html').toPath())


            List<AnalysisResult> sortedResults = new ArrayList<AnalysisResult>(
                    taskAnalyzers.values().collect { t -> t.results })
                    .findResults { t -> t }
                    .sort { a, b -> Long.compare(a.executionResults.startTime, b.executionResults.startTime) }

            Map<String, List<String>> overlappingTasks = getOverlappingTasks(sortedResults)

            for (AnalysisResult analysisResult : sortedResults) {
                DiffWriter.write(
                        new File(config.reportDir, analysisResult.executionResults.path),
                        analysisResult.executionResults,
                        analysisResult.diffResults,
                        analysisResult.comparisonResults,
                        overlappingTasks.get(analysisResult.executionResults.task.name))
            }

            IndexWriter.write(
                    getIndex(),
                    subprojectsByFile,
                    sortedResults,
                    new File(new File(config.reportDir, 'vis'), 'dag.js'))
            println "Build inspection written to file://${getIndex()}"
            if (config.showInspection) {
                Desktop.getDesktop().browse(new URI("file://${getIndex()}"))
            }
        } catch (Throwable e) {
            e.printStackTrace()
        }

    }

    public static Map<String, List<String>> getOverlappingTasks(List<AnalysisResult> analysisResults) {
        Map<String, List<String>> overlappingTasks = new HashMap<String, List<String>>()

        for (AnalysisResult result : analysisResults) {
            def task1 = result.executionResults.task.name
            overlappingTasks.put(task1, new LinkedList<String>())
            def task1Start = result.executionResults.startTime
            def task1End = result.executionResults.endTime
            for (AnalysisResult result2 : analysisResults) {
                def task2 = result2.executionResults.task.name
                if (!task2.equals(task1)) {
                    def task2Start = result2.executionResults.startTime
                    def task2End = result2.executionResults.endTime

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

        file.getParent().toFile().mkdirs()

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
