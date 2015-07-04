package com.jakeout.gradle.inspector.tasks.output

import com.jakeout.gradle.inspector.tasks.TaskAnalyzer
import com.jakeout.gradle.inspector.tasks.model.FileState
import com.jakeout.gradle.inspector.tasks.model.TaskDiffResults
import com.jakeout.gradle.inspector.tasks.model.TaskExecutionResults
import com.zutubi.diff.PatchFile
import com.zutubi.diff.unified.UnifiedHunk
import com.zutubi.diff.unified.UnifiedPatch
import kotlinx.html.*
import org.apache.commons.io.FilenameUtils
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter

class DiffWriter {
    companion object {
        val SUPPORTED_IMAGE_FORMATS = setOf("png", "jpg", "jpeg", "svg", "gif")

        fun write(out: File,
                  executionResults: TaskExecutionResults,
                  diffResults: TaskDiffResults,
                  comparisonResults: TaskDiffResults?,
                  overlappingTasks: List<String>) {
            val bw = BufferedWriter(FileWriter(out))

            bw.write(
                    html {
                        head {
                            title { +"Changes from ${executionResults.name}" }
                            link(rel = "stylesheet", href = "font/css/font-awesome.min.css")
                            link(rel = "stylesheet", href = "diff.css")
                        }

                        body {
                            if (!overlappingTasks.isEmpty()) {
                                overlappingTaskHtml(diffResults, overlappingTasks)
                            }

                            if (comparisonResults != null && comparisonResults.patchFile != null) {
                                h1 { +"Comparison to last build" }
                                if (comparisonResults.binaries.size() > 0) {
                                    h2 { +"Binary Comparisons" }
                                    for (binary in comparisonResults.binaries.entrySet()) {
                                        writeBinary(binary.getKey(), binary.getValue(), executionResults)
                                    }
                                }
                                writePatch(comparisonResults.patchFile, executionResults)
                                // Attached to next output
                            }

                            if (diffResults.binaries.size() > 0) {
                                h2 { +"Binary inputs" }
                                for (binary in diffResults.binaries.entrySet()) {
                                    writeBinary(binary.getKey(), binary.getValue(), executionResults)
                                }
                                // Attached to next output
                            }

                            if (diffResults.patchFile != null && diffResults.patchFile.getPatches().size() != 0) {
                                h1 { +"Changes from start of task to task completion" }
                                writePatch(diffResults.patchFile, executionResults)
                            } else {
                                h1 { +"No changes from start of task to task completion" }
                            }
                        }

                    }.toString()
            )
            bw.close()
        }

        fun BODY.overlappingTaskHtml(taskDiffResults: TaskDiffResults, overlappingTasks: List<String>) {
            p {
                +(if (taskDiffResults.anyUndeclaredChanges)
                    "Note that this task was run in parallel (potentially explaining undeclared changes) with:" else
                    "Note that this task was run in parallel with:")

                ul {
                    for (it in overlappingTasks) li { +it }
                }
            }
        }

        fun BODY.writeBinary(binaryName: String, state: FileState, taskExecutionResults: TaskExecutionResults) {
            div(c = "fileHeader") {
                span(c = "filechange") { +state.toString().toLowerCase().capitalize() }
                writeName(binaryName, taskExecutionResults)
                visualize(binaryName)
            }
        }

        fun BODY.writePatch(patchFile: PatchFile, taskExecutionResults: TaskExecutionResults) {

            for (patch in patchFile.getPatches()) {
                div(c = "patchFile") {
                    div(c = "fileHeader") {
                        writeName(patch.getNewFile(), taskExecutionResults)

                        visualize(patch.getNewFile())

                        if (patch is UnifiedPatch) {
                            for (hunk in  patch.getHunks()) {
                                writePatch(hunk)
                            }
                        }
                    }
                }
            }
        }

        fun DIV.visualize(newFile: String) {
            val extension = FilenameUtils.getExtension(newFile)
            if (SUPPORTED_IMAGE_FORMATS.contains(extension)) {
                img { src = DirectLink(newFile) }
            }
        }

        fun DIV.writePatch(hunk: UnifiedHunk) {
            div(c = "hunk") {
                details {
                    !"<summary>"

                    var line = 0;
                    var summaryEnded = false;
                    for (l in hunk.getLines()) {
                        if (line > 4 && !summaryEnded) {
                            summaryEnded = true;
                            !"</summary>"
                        }
                        line++

                        writeLine(l)
                    }

                    if (!summaryEnded) {
                        !"</summary>"
                    }
                }
            }
        }

        fun DIV.writeName(name: String, taskExecutionResults: TaskExecutionResults) {
            val rootDirOfDeclaredOutput: String? = TaskAnalyzer.findOutput(name, taskExecutionResults.task.getOutputs().getFiles())

            if (rootDirOfDeclaredOutput != null) {
                !HtmlConstants.KNOWN_FILE
                span(c = "filename") { +rootDirOfDeclaredOutput }
                val suffix = name.replaceFirstLiteral(rootDirOfDeclaredOutput, "")

                if (!suffix.isEmpty()) {
                    span(c = "filenameSuffix") { +suffix }
                }
            } else {
                !HtmlConstants.UNKNOWN_FILE
                span(c = "filenameSuffix") { +name }
            }
        }

        fun DIV.writeLine(line: UnifiedHunk.Line) {
            when (line.getType()) {
                UnifiedHunk.LineType.ADDED -> div(c = "added") { code { +"+ ${line.getContent()}" } }
                UnifiedHunk.LineType.DELETED -> div(c = "deleted") { code { +"- ${line.getContent()}" } }
                UnifiedHunk.LineType.COMMON -> div(c = "common") { code { +"&nbsp ${line.getContent()}" } }
            }
        }
    }

}
