package com.jakeout.gradle.inspector.tasks.output

import com.jakeout.gradle.inspector.tasks.TaskAnalyzer
import com.jakeout.gradle.inspector.tasks.model.*
import com.zutubi.diff.unified.UnifiedHunk
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

                            if (comparisonResults != null) {
                                h1 { +"Comparison to last build" }
                                writeDiff(comparisonResults, executionResults)
                                // Attached to next output
                            }

                            h1 { +"Changes from start of task to task completion" }
                            writeDiff(diffResults, executionResults)
                        }

                    }.toString()
            )
            bw.close()
        }

        private fun HtmlBodyTag.writeDiff(diff: TaskDiffResults, executionResults: TaskExecutionResults) {
            if (diff.changesByType.size() == 0) {
                h2 { +"No changes from start of task to task completion" }
                return
            }

            if (diff.changedFiles.size() > 0) {
                h2 { +"Non-plaintext Files" }
                for (file in diff.changedFiles) {
                    writeBinary(file, executionResults)
                }
            }

            if (diff.changedContents.size() > 0) {
                h2 { +"Plaintext Files" }
                for (patch in diff.changedContents) {
                    writePatch(patch, executionResults)
                }
            }
        }

        fun HtmlBodyTag.overlappingTaskHtml(taskDiffResults: TaskDiffResults, overlappingTasks: List<String>) {
            p {
                +(if (taskDiffResults.anyUndeclaredChanges)
                    "Note that this task was run in parallel (potentially explaining undeclared changes) with:" else
                    "Note that this task was run in parallel with:")

                ul {
                    for (it in overlappingTasks) li { +it }
                }
            }
        }

        fun HtmlBodyTag.writeBinary(difference: FileDifference, taskExecutionResults: TaskExecutionResults) {
            div(c = "binaryFile changeBlock") {
                div(c = "changeHeader") {
                    span(c = difference.state.toString().toLowerCase()) { }
                    writeName(difference.file, difference.state, taskExecutionResults)
                }
                visualize(difference.file)
            }
        }

        fun HtmlBodyTag.writePatch(difference: FileContentsDifference, taskExecutionResults: TaskExecutionResults) {
            div(c = "patchFile changeBlock") {
                div(c = "changeHeader") {
                    writeName(difference.file, null, taskExecutionResults)
                }
                visualize(difference.file)
                div(c = "patches") {
                    for (hunk in  difference.changes) {
                        writePatch(hunk)
                    }
                }
            }
        }

        fun HtmlBodyTag.visualize(newFile: String) {
            val extension = FilenameUtils.getExtension(newFile)
            if (SUPPORTED_IMAGE_FORMATS.contains(extension)) {
                div(c = "fileView") { img { src = DirectLink(newFile) } }
            }
        }

        fun HtmlBodyTag.writePatch(hunk: UnifiedHunk) {
            div(c = "hunk") {
                val hunkItr = hunk.getLines().iterator()

                for (line in 1..4) {
                    if (!hunkItr.hasNext()) {
                        break
                    }
                    writeLine(hunkItr.next())
                }

                details {
                    summary { +"..." }
                    while (hunkItr.hasNext()) {
                        writeLine(hunkItr.next())
                    }

                }
            }
        }

        fun HtmlBodyTag.writeName(name: String, state: FileState? = null, taskExecutionResults: TaskExecutionResults) {
            val rootDirOfDeclaredOutput: String? = TaskAnalyzer.findOutput(name, taskExecutionResults.task.getOutputs().getFiles())

            if (rootDirOfDeclaredOutput != null) {
                knownFile()
                noteFileState(state)
                span(c = "filename") { +rootDirOfDeclaredOutput }
                val suffix = name.replaceFirstLiteral(rootDirOfDeclaredOutput, "")

                if (!suffix.isEmpty()) {
                    span(c = "filenameSuffix") { +suffix }
                }
            } else {
                unknownFile()
                noteFileState(state)
                span(c = "filenameSuffix") { +name }
            }
        }

        fun HtmlBodyTag.writeLine(line: UnifiedHunk.Line) {
            when (line.getType()) {
                UnifiedHunk.LineType.ADDED -> div(c = "added") { code { +"+ ${line.getContent()}" } }
                UnifiedHunk.LineType.DELETED -> div(c = "deleted") { code { +"- ${line.getContent()}" } }
                UnifiedHunk.LineType.COMMON -> div(c = "common") { code { +"&nbsp ${line.getContent()}" } }
            }
        }

        fun HtmlBodyTag.noteFileState(fileState: FileState?) {
            if (fileState != null) {
                span(c = "fa-stack file-type-icon") {
                    if (fileState.equals(FileState.ADDED)) {
                        i(c = "font-shadow-added fa fa-circle fa-stack-2x")
                        i(c = "fa fa-plus fa-stack-1x fa-stacked-symbol")
                        i(c = "fa fa-file-o fa-stack-1x")
                    } else if (fileState.equals(FileState.DELETED)) {
                        i(c = "font-shadow-deleted fa fa-circle fa-stack-2x")
                        i(c = "fa fa-minus fa-stack-1x fa-stacked-symbol")
                        i(c = "fa fa-file-o fa-stack-1x")
                    } else {
                        i(c = "fa fa-file-o fa-stack-1x")
                    }
                }
            }
        }

        fun HtmlBodyTag.unknownFile() {
            span(c = "fa-stack file-type-icon") {
                i(c = "fa fa-question fa-stack-1x")
            }
            span(c = "warning") { em { +"undeclared output" } }
        }


        fun HtmlBodyTag.knownFile() {
            // empty -- display nothing for known files
        }
    }
}
