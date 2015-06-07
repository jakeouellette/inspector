package com.jakeout.gradle.inspector.tasks.output

import com.jakeout.gradle.inspector.tasks.TaskAnalyzer
import com.jakeout.gradle.inspector.tasks.model.TaskDiffResults
import com.jakeout.gradle.inspector.tasks.model.TaskExecutionResults
import com.zutubi.diff.PatchFile
import com.zutubi.diff.PatchType
import com.zutubi.diff.unified.UnifiedHunk
import com.zutubi.diff.unified.UnifiedPatch
import groovy.xml.MarkupBuilder
import org.apache.commons.io.FilenameUtils

class DiffWriter {
    private static def SUPPORTED_IMAGE_FORMATS = ['png', 'jpg', 'jpeg', 'svg', 'gif'] as Set

    public static void write(File out,
                             TaskExecutionResults executionResults,
                             TaskDiffResults diffResults,
                             Optional<TaskDiffResults> comparisonResults,
                             List<String> overlappingTasks) {
        def bw = new BufferedWriter(new FileWriter(out))
        def markup = new MarkupBuilder(bw)

        markup.html {
            head {
                title:
                "Changes from ${executionResults.name}"
                link(rel: "stylesheet", href: "font/css/font-awesome.min.css", type: "text/css")
                link(rel: "stylesheet", href: "diff.css", type: "text/css")
            }

            body {
                if (!overlappingTasks.isEmpty()) {
                    overlappingTaskHtml(owner.delegate, diffResults, overlappingTasks)
                }

                if (comparisonResults.isPresent() && comparisonResults.get().patchFile.isPresent()) {
                    h1: "Comparison to last build"
                    writePatch(owner.delegate, comparisonResults.get().patchFile.get(), executionResults)
                    // Attached to next output
                }

                if (diffResults.patchFile.isPresent()) {
                    h1: "Changes from start of task to task completion"
                    writePatch(owner.delegate, diffResults.patchFile.get(), executionResults)
                }
            }

        }
        bw.close()
    }

    public static void overlappingTaskHtml(body, result, overlappingTasks) {
        body.p(result.anyUndeclaredChanges ?
                "Note that this task was run in parallel (potentially explaining undeclared changes) with:" :
                "Note that this task was run in parallel with:")

        body.ul {
            overlappingTasks.each {
                li:
                it.taskName
            }
        }
    }

    public static void writePatch(body, PatchFile patchFile, TaskExecutionResults executionResults) {
        patchFile.getPatches().each { patch ->
            body.div(class: "patchFile") {
                div(class: "fileHeader") {
                    Optional<String> rootDirOfDeclaredOutput = TaskAnalyzer.findOutput(patch.newFile, executionResults.task.getOutputs().files)

                    if (rootDirOfDeclaredOutput.isPresent()) {
                        mkp.yieldUnescaped(HtmlConstants.KNOWN_FILE)
                        span(class: "filename", rootDirOfDeclaredOutput.get())
                        String suffix = patch.newFile.replaceFirst(rootDirOfDeclaredOutput.get(), "")
                        if (!suffix.isEmpty()) {
                            span(class: "filenameSuffix", suffix)
                        }
                    } else {
                        mkp.yieldUnescaped(HtmlConstants.UNKNOWN_FILE)
                        span(class: "filenameSuffix", patch.newFile)
                    }
                }

                def extension = FilenameUtils.getExtension(patch.newFile)
                if (PatchType.ADD.equals(patch.type) && SUPPORTED_IMAGE_FORMATS.contains(extension)) {
                    img(src: patch.newFile)
                }

                if (patch instanceof UnifiedPatch) {
                    ((UnifiedPatch) patch).getHunks().each { hunk ->
                        div(class: "hunk") {
                            details {

                                mkp.yieldUnescaped('<summary>')

                                int line = 0;
                                boolean summaryEnded = false;
                                hunk.getLines().each { l->
                                    if (line > 4 && !summaryEnded) {
                                        summaryEnded = true;
                                        mkp.yieldUnescaped('</summary>')
                                    }

                                    line++
                                    switch (l.type) {
                                        case UnifiedHunk.LineType.ADDED:
                                            div(class: "added") { code { mkp.yield("+ $l.content") } }
                                            break;
                                        case UnifiedHunk.LineType.DELETED:
                                            div(class: "deleted") { code { mkp.yield("- $l.content") } }
                                            break;
                                        case UnifiedHunk.LineType.COMMON:
                                            div(class: "common") { code { mkp.yield("&nbsp $l.content") } }
                                            break;
                                    }
                                }

                                if (!summaryEnded) {
                                    mkp.yieldUnescaped('</summary>')
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
