package com.jakeout.gradle.inspector

import com.zutubi.diff.PatchFile
import com.zutubi.diff.PatchFileParser
import com.zutubi.diff.unified.UnifiedPatchParser
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

class TaskDiffHook implements TaskExecutionListener, BuildListener {

    private final static String PROFILE_PATH = "buildProfile"
    private final static String DIFF_INCREMENTAL = "diffIncremental"
    private final static String DIFF_REPORT = "report"

    private final File taskRoot
    private final File buildDir
    private final File incrementalDir
    private final File reportDir

    private final List<TaskExecution> changes = new LinkedList<TaskExecution>()
    private final Set<String> executedNames = new HashSet<String>()

    public static void setupFolderDiff(Project project) {
        def hook = new TaskDiffHook(new File(project.projectDir, PROFILE_PATH), project.buildDir)
        project.gradle.addListener(hook)
        hook
    }

    private TaskDiffHook(File taskRoot, File buildDir) {
        this.taskRoot = taskRoot
        this.buildDir = buildDir
        this.incrementalDir = new File(taskRoot, DIFF_INCREMENTAL)
        this.reportDir = new File(taskRoot, DIFF_REPORT)

        FileUtils.deleteDirectory(reportDir)
        FileUtils.deleteDirectory(incrementalDir)
        FileUtils.deleteDirectory(taskRoot)
        FileUtils.forceMkdir(reportDir)
        FileUtils.forceMkdir(incrementalDir)
    }

    @Override
    void beforeExecute(Task task) {
        if (!buildDir.exists()) {
            return
        }
        DiffTool.backup(buildDir, taskDir(task))
    }

    @Override
    void afterExecute(Task task, TaskState taskState) {
        try {
            if (!buildDir.exists()) {
                return
            }

            List<Task> dependsOnTasks = new LinkedList<Task>()
            executedNames.add(task.name)
            task.getDependsOn().each { t ->
                if (t instanceof Task) {
                    dependsOnTasks.add((Task) t)
                }
            }

            def tDir = taskDir(task)
            def tOut = taskOut(task)
            String tReportRel = taskReport(task)
            File tReport = new File(reportDir, tReportRel)

            DiffTool.diff(buildDir, tDir, tOut)
            if (tOut.exists() && tOut.size() > 0) {
                PatchFileParser parser = new PatchFileParser(new UnifiedPatchParser())
                PatchFile patchFile = parser.parse(new FileReader(tOut))
                DiffWriter writer = new DiffWriter(tReport, task.name, tReportRel, dependsOnTasks);
                def patchChangeInfo = writer.write(patchFile)
                writer.close()
                changes.add(patchChangeInfo)
            } else {
                changes.add(new TaskExecution(
                        name: task.name,
                        dependsOnTasks: dependsOnTasks))
            }

            FileUtils.deleteDirectory(taskDir(task))
        } catch (Throwable e) {
            e.printStackTrace()
        }
    }

    @Override
    void buildFinished(BuildResult result) {
        try {
            FileUtils.forceMkdir(new File(reportDir, "vis"))
            // TODO: infer these dynamically
            makeFile("diff.css")
            makeFile("vis/d3.v3.min.js")
            makeFile("vis/dag.js")
            makeFile("vis/dagre-d3.js")
            makeFile("vis/demo.js")
            makeFile("vis/hover.html")
            makeFile("vis/jquery-1.9.1.min.js")
            makeFile("vis/tipsy.css")
            makeFile("vis/tipsy.js")

            IndexWriter.write(new File(reportDir, "index.html"), changes, executedNames, new File(new File(reportDir, "vis"), "hover.html"))
            println "Diff Report Written."
        } catch (Throwable e) {
            e.printStackTrace()
        }

    }

    void makeFile(String path) {
        Path file = new File(reportDir, path).toPath()
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

    public File taskOut(Task task) {
        new File(incrementalDir, task.name.replace(":", ".") + ".diff")
    }

    public String taskReport(Task task) {
        task.name.replace(":", ".") + "-report.html"
    }

    public File taskDir(Task task) {
        new File(incrementalDir, task.name.replace(":", "."))
    }
}
