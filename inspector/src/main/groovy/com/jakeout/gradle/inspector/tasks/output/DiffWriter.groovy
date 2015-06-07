package com.jakeout.gradle.inspector.tasks.output

import com.jakeout.gradle.inspector.tasks.TaskAnalyzer
import com.jakeout.gradle.inspector.tasks.TaskDiffStats
import com.jakeout.gradle.inspector.tasks.TaskExecutionStats
import com.zutubi.diff.Patch
import com.zutubi.diff.PatchFile
import com.zutubi.diff.PatchType
import com.zutubi.diff.unified.UnifiedHunk
import com.zutubi.diff.unified.UnifiedPatch
import org.apache.commons.io.FilenameUtils
import org.springframework.web.util.HtmlUtils

class DiffWriter {
    private static def SUPPORTED_IMAGE_FORMATS = ['png', 'jpg', 'jpeg', 'svg', 'gif'] as Set

    public static void write(File out,
                             TaskExecutionStats executionStats,
                             TaskDiffStats stats,
                             Optional<TaskDiffStats> compareStats,
                             List<String> overlappingTasks) {
        def bw = new BufferedWriter(new FileWriter(out));
        bw.write("<html><head><title>Changes from ${executionStats.name}</title>" +
                "<link rel=\"stylesheet\" href=\"font/css/font-awesome.min.css\">" +
                "<link rel=\"stylesheet\" type=\"text/css\" href=\"diff.css\"></head><body>")

        if (!overlappingTasks.isEmpty()) {
            if (!stats.anyUndeclaredChanges) {
                bw.write('<p> Note that this task was run in parallel with:</p>')
            } else {
                bw.write('<p> Note that this task was run in parallel (potentially explaining undeclared changes) with:</p>')
            }

            bw.write('<ul>')
            for (String taskName : overlappingTasks) {
                bw.write("<li>$taskName</li")
            }
            bw.write('</ul>')
        }

        if (compareStats.isPresent() && compareStats.get().patchFile.isPresent()) {
            bw.write('<h1>Comparison to last build</h1>')
            writePatch(bw, compareStats.get().patchFile.get(), executionStats)
            // Attached to next output
        }

        if (stats.patchFile.isPresent()) {
            bw.write('<h1>Changes from start of task to task completion</h1>')
            writePatch(bw, stats.patchFile.get(), executionStats)
        }

        bw.write('</body></html>')
        bw.close()
    }

    public static void writePatch(BufferedWriter bw, PatchFile patch, TaskExecutionStats executionStats) {
        List<Patch> patches = patch.getPatches()
        for (Patch p : patches) {
            bw.write('<div class="patchFile">')

            bw.write('<div class="fileHeader">')
            Optional<String> rootDirOfDeclaredOutput = TaskAnalyzer.findOutput(p.newFile, executionStats.task.getOutputs().files)

            if (rootDirOfDeclaredOutput.isPresent()) {
                String remainder = p.newFile.replaceFirst(rootDirOfDeclaredOutput.get(), "")
                if (!remainder.isEmpty()) {
                    remainder = '<span class="filenameSuffix">' + remainder + '</span>'
                }
                bw.write(HtmlConstants.KNOWN_FILE + '<span class="filename">' + rootDirOfDeclaredOutput.get() + '</span>' + remainder)
            } else {
                bw.write(HtmlConstants.UNKNOWN_FILE + '<span class="warning"><em>undeclared output</em></span><span class="filename">' + p.newFile + '</span>')
            }
            bw.write('</div>')

            def extension = FilenameUtils.getExtension(p.newFile)
            if (PatchType.ADD.equals(p.type) && SUPPORTED_IMAGE_FORMATS.contains(extension)) {
                bw.write("<img src=\"$p.newFile\"/")
            }

            if (p instanceof UnifiedPatch) {
                UnifiedPatch up = (UnifiedPatch) p
                for (UnifiedHunk h : up.getHunks()) {
                    int line = 0;
                    boolean summaryEnded = false;
                    bw.write('<div class=\"hunk\">')
                    bw.write('<details><summary>')

                    for (UnifiedHunk.Line l : h.getLines()) {
                        if (line > 4 && !summaryEnded) {
                            summaryEnded = true;
                            bw.write('</summary>')
                        }
                        line++
                        switch (l.type) {
                            case UnifiedHunk.LineType.ADDED:
                                String divClass = 'added'
                                bw.write("<div class=\"$divClass\"><code>+ ${HtmlUtils.htmlEscape(l.content)}</code></div>")
                                break;
                            case UnifiedHunk.LineType.DELETED:
                                String divClass = 'deleted'
                                bw.write("<div class=\"$divClass\"><code>- ${HtmlUtils.htmlEscape(l.content)}</code></div>")
                                break;
                            case UnifiedHunk.LineType.COMMON:
                                String divClass = 'common'
                                bw.write("<div class=\"$divClass\"><code>= ${HtmlUtils.htmlEscape(l.content)}</code></div>")
                                break;
                        }
                    }
                    if (!summaryEnded) {
                        bw.write('</summary>')
                    }
                }
                bw.write('</details>')
                bw.write('</div>')
            }
            bw.write('</div>')
        }
    }
}
