package com.jakeout.gradle.inspector.tasks.output

import com.jakeout.gradle.inspector.tasks.TaskAnalyzer
import com.jakeout.gradle.inspector.tasks.model.TaskDiffResults
import com.jakeout.gradle.inspector.tasks.model.TaskExecutionResults
import com.zutubi.diff.PatchFile
import com.zutubi.diff.PatchType
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
                                writePatch(comparisonResults.patchFile, executionResults)
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

        fun BODY.writePatch(patchFile: PatchFile, taskExecutionResults: TaskExecutionResults) {

            for (patch in patchFile.getPatches()) {
                div(c = "patchFile") {
                    div(c = "fileHeader") {
                        val rootDirOfDeclaredOutput: String? = TaskAnalyzer.findOutput(patch.getNewFile(), taskExecutionResults.task.getOutputs().getFiles())

                        if (rootDirOfDeclaredOutput != null) {
                            !HtmlConstants.KNOWN_FILE
                            span(c = "filename") { +rootDirOfDeclaredOutput }
                            val suffix = patch.getNewFile().replaceFirstLiteral(rootDirOfDeclaredOutput, "")

                            if (!suffix.isEmpty()) {
                                span(c = "filenameSuffix") { +suffix }
                            }
                        } else {
                            !HtmlConstants.UNKNOWN_FILE
                            span(c = "filenameSuffix") { +patch.getNewFile() }
                        }
                    }

                    val extension = FilenameUtils.getExtension(patch.getNewFile())
                    if (PatchType.ADD.equals(patch.getType()) && SUPPORTED_IMAGE_FORMATS.contains(extension)) {
                        img { src = DirectLink(patch.getNewFile()) }
                    }

                    if (patch is UnifiedPatch) {
                        for (hunk in  patch.getHunks()) {
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
                                        when (l.getType()) {
                                            UnifiedHunk.LineType.ADDED -> div(c = "added") { code { +"+ ${l.getContent()}" } }
                                            UnifiedHunk.LineType.DELETED -> div(c = "deleted") { code { +"- ${l.getContent()}" } }
                                            UnifiedHunk.LineType.COMMON -> div(c = "common") { code { +"&nbsp ${l.getContent()}" } }
                                        }
                                    }

                                    if (!summaryEnded) {
                                        !"</summary>"
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
