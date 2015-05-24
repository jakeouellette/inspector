package com.jakeout.gradle.inspector.tasks.output

import com.jakeout.gradle.inspector.tasks.TaskAnalyzer
import com.jakeout.gradle.inspector.tasks.TaskDiffStats
import com.jakeout.gradle.inspector.tasks.TaskExecutionStats
import com.zutubi.diff.Patch
import com.zutubi.diff.PatchType
import com.zutubi.diff.unified.UnifiedHunk
import com.zutubi.diff.unified.UnifiedPatch
import org.apache.commons.io.FilenameUtils
import org.springframework.web.util.HtmlUtils

class DiffWriter {
    private static def SUPPORTED_IMAGE_FORMATS = ["png", "jpg", "jpeg", "svg", "gif"] as Set

    public static void write(File out,
                             TaskExecutionStats executionStats,
                             TaskDiffStats stats,
                             List<String> overlappingTasks) {
        def bw = new BufferedWriter(new FileWriter(out));
        bw.write("<html><head><title>Changes from ${executionStats.name}</title><link rel=\"stylesheet\" type=\"text/css\" href=\"diff.css\"></head><body>")

        if(!overlappingTasks.isEmpty()) {
            if(!stats.anyUndeclaredChanges) {
                bw.write("<p> Note that this task was run in parallel with:</p>")
            } else {
                bw.write("<p> Note that this task was run in parallel (potentially explaining undeclared changes) with:</p>")
            }

            bw.write("<ul>")
            for(String taskName : overlappingTasks) {
                bw.write("<li>$taskName</li")
            }
            bw.write("</ul>")
        }
        if (stats.patchFile.isPresent()) {
            List<Patch> patches = stats.patchFile.get().getPatches()
            for (Patch p : patches) {
                bw.write("<h1>$p.newFile</h1>")

                boolean foundOutput = TaskAnalyzer.findOutput(p.newFile, executionStats.task.getOutputs().files)

                if (!foundOutput) {
                    bw.write("<p><em>Undeclared output</em></p>")
                }

                def extension = FilenameUtils.getExtension(p.newFile)
                if (PatchType.ADD.equals(p.type) && SUPPORTED_IMAGE_FORMATS.contains(extension)) {
                    bw.write("<img src=\"$p.newFile\"/")
                }

                String divClassPrefix = !foundOutput ? "undeclared" : ""
                if (p instanceof UnifiedPatch) {
                    UnifiedPatch up = (UnifiedPatch) p
                    for (UnifiedHunk h : up.getHunks()) {
                        int line = 0;
                        boolean summaryEnded = false;
                        bw.write("<div class=\"hunk\">")
                        bw.write("<details><summary>")

                        for (UnifiedHunk.Line l : h.getLines()) {
                            if (line > 4 && !summaryEnded) {
                                summaryEnded = true;
                                bw.write("</summary>")
                            }
                            line++
                            switch (l.type) {
                                case UnifiedHunk.LineType.ADDED:
                                    String divClass = divClassPrefix + "added"
                                    bw.write("<div class=\"$divClass\"><code>+ ${HtmlUtils.htmlEscape(l.content)}</code></div>")
                                    break;
                                case UnifiedHunk.LineType.DELETED:
                                    String divClass = divClassPrefix + "deleted"
                                    bw.write("<div class=\"$divClass\"><code>- ${HtmlUtils.htmlEscape(l.content)}</code></div>")
                                    break;
                                case UnifiedHunk.LineType.COMMON:
                                    String divClass = divClassPrefix + "common"
                                    bw.write("<div class=\"$divClass\"><code>= ${HtmlUtils.htmlEscape(l.content)}</code></div>")
                                    break;
                            }
                        }
                        if (!summaryEnded) {
                            bw.write("</summary>")
                        }
                    }
                    bw.write("</details>")
                    bw.write("</div>")
                }
            }
        }
        bw.write("</body></html>")
        bw.close()
    }
}
