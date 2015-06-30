package com.jakeout.gradle.inspector

import com.jakeout.gradle.inspector.tasks.TaskAnalyzer
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
import java.awt.Desktop
import java.io.File
import java.net.URI
import java.nio.file.Files
import java.util.HashMap
import java.util.LinkedHashMap
import java.util.LinkedList


public class InspectorGradleListener(val config: InspectorConfig, val project: Project) : TaskExecutionListener, BuildListener {
    companion object {
        val SHOW_INSPECTION_PROPERTY = "showInspection"
        val COMPARE_LAST_BUILD_PROPERTY = "compareLastBuild"
        val PROFILE_PATH = "buildProfile"
        val DIFF_INCREMENTAL = "diffIncremental"
        val DIFF_REPORT = "report"
        val PROFILE_PATH_COMPARE = "buildProfileCompare"

        fun setupFolderDiff(project: Project): InspectorGradleListener {

            val inspectionRoot = File(project.getProjectDir(), PROFILE_PATH)
            val compareInspectionRoot = File(project.getProjectDir(), PROFILE_PATH_COMPARE)

            val incrementalDir = File(inspectionRoot, DIFF_INCREMENTAL)
            val compareIncrementalDir = File(compareInspectionRoot, DIFF_INCREMENTAL)

            val reportDir = File(inspectionRoot, DIFF_REPORT)
            val compareReportDir = File(compareInspectionRoot, DIFF_REPORT)


            val showInspection = project.hasProperty(SHOW_INSPECTION_PROPERTY)

            val compareBuild = project.hasProperty(COMPARE_LAST_BUILD_PROPERTY)
            val cleanUpEachTask = !compareBuild

            val config = InspectorConfig(
                    incrementalDir = incrementalDir,
                    reportDir = reportDir,
                    inspectionRoot = inspectionRoot,
                    projectBuildDir = project.getBuildDir(),
                    cleanUpEachTask = cleanUpEachTask,
                    compareBuild = compareBuild,
                    compareInspectionRoot = compareInspectionRoot,
                    compareIncrementalDir = compareIncrementalDir,
                    compareReportDir = compareReportDir,
                    showInspection = showInspection)

            if (config.compareBuild) {
                FileUtils.deleteDirectory(config.compareInspectionRoot)
                FileUtils.moveDirectory(config.inspectionRoot, config.compareInspectionRoot)
            }

            FileUtils.deleteDirectory(config.reportDir)
            FileUtils.deleteDirectory(config.incrementalDir)
            FileUtils.deleteDirectory(config.inspectionRoot)
            FileUtils.forceMkdir(config.reportDir)
            FileUtils.forceMkdir(config.incrementalDir)

            val hook = InspectorGradleListener(config, project)
            project.getGradle().addListener(hook)
            return hook
        }

        fun getOverlappingTasks(analysisResults: List<AnalysisResult>): Map<String, List<String>> {
            val overlappingTasks = HashMap<String, LinkedList<String>>()

            for (result in analysisResults) {
                val task1 = result.executionResults.task.getName()
                overlappingTasks.put(task1, LinkedList<String>())
                val task1Start = result.executionResults.startTime
                val task1End = result.executionResults.endTime
                for (result2 in analysisResults) {
                    val task2 = result2.executionResults.task.getName()
                    if (!task2.equals(task1)) {
                        val task2Start = result2.executionResults.startTime
                        val task2End = result2.executionResults.endTime

                        if ((task1Start > task2Start && task1Start < task2End) ||
                                (task1End > task2Start && task1End < task2End)) {

                            val overlapping: LinkedList<String> = overlappingTasks.get(task1)
                            overlapping.add(task2)
                        }
                    }

                }
            }

            return overlappingTasks
        }
    }

    val taskAnalyzers = LinkedHashMap<String, TaskAnalyzer>()

    override fun beforeExecute(task: Task) {
        if (task.getProject().equals(project)) {
            // In case a clean task removed the build folder.
            FileUtils.forceMkdir(config.projectBuildDir)

            taskAnalyzers.put(task.getName(), TaskAnalyzer(config, task, System.currentTimeMillis()))
            DiffUtil.backup(config.projectBuildDir, config.taskDir(task))
        }
    }

    override fun afterExecute(task: Task, taskState: TaskState) {
        if (task.getProject().equals(project)) {
            val listener = taskAnalyzers.get(task.getName())
            if (listener != null) {
                listener.onAfterExecute(taskState)
            } else {
                println("No task listener for : ${task.getName()}")
            }
        }
    }

    fun getIndex(): File = File(config.reportDir, "index.html")

    override fun buildFinished(buildResult: BuildResult) {
        try {
            FileUtils.forceMkdir(File(config.reportDir, "vis"))
            // TODO: infer these dynamically
            makeFile("diff.css")
            makeFile("vis/d3.v3.min.js")
            makeFile("vis/dag.js")
            makeFile("vis/dagre-d3.js")
            makeFile("vis/jquery-1.9.1.min.js")
            makeFile("vis/tipsy.css")
            makeFile("vis/tipsy.js")

            makeFile("font/css/font-awesome.css")
            makeFile("font/css/font-awesome.min.css")
            makeFile("font/fonts/FontAwesome.otf")
            makeFile("font/fonts/fontawesome-webfont.eot")
            makeFile("font/fonts/fontawesome-webfont.svg")
            makeFile("font/fonts/fontawesome-webfont.ttf")
            makeFile("font/fonts/fontawesome-webfont.woff")
            makeFile("font/fonts/fontawesome-webfont.woff2")

            val subprojectsByFile = HashMap<String, File>()
            for (subProj in project.getSubprojects()) {
                val plugin = subProj.getPlugins().findPlugin(javaClass<InspectorPlugin>()) as InspectorPlugin
                val listener = plugin.listener
                if (listener != null) {
                    subprojectsByFile.put(subProj.getName(), listener.getIndex())
                }
            }

            Files.copy(
                    this.javaClass.getClassLoader().getResourceAsStream("vis-report.html"),
                    File(config.reportDir, "index.html").toPath())


            val sortedResults = taskAnalyzers.values()
                    .map { t: TaskAnalyzer -> t.results }
                    .filterNotNull()
                    .toSortedListBy { a: AnalysisResult -> a.executionResults.startTime }

            val overlappingTasks = getOverlappingTasks(sortedResults)

            for (analysisResult in sortedResults) {
                val overlappingName = overlappingTasks.get(analysisResult.executionResults.task.getName()) ?: LinkedList<String>()
                DiffWriter.write(
                        File(config.reportDir, analysisResult.executionResults.path),
                        analysisResult.executionResults,
                        analysisResult.diffResults,
                        analysisResult.comparisonResults,
                        overlappingName)
            }

            IndexWriter.write(
                    getIndex(),
                    subprojectsByFile,
                    sortedResults,
                    File(File(config.reportDir, "vis"), "dag.js"))
            println("Build inspection written to file://${getIndex()}")
            if (config.showInspection) {
                Desktop.getDesktop().browse(URI("file://${getIndex()}"))
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }

    }

    fun makeFile(path: String) {
        val file = File(config.reportDir, path).toPath()
        if (Files.exists(file)) {
            Files.delete(file)
        }

        file.getParent().toFile().mkdirs()

        Files.copy(
                this.javaClass.getClassLoader().getResourceAsStream(path),
                file)

    }

    override fun buildStarted(gradle: Gradle) {
    }

    override fun projectsEvaluated(gradle: Gradle) {
    }

    override fun projectsLoaded(gradle: Gradle) {
    }

    override fun settingsEvaluated(settings: Settings) {
    }
}
