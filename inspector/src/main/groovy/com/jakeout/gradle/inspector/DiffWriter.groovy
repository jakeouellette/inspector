package com.jakeout.gradle.inspector

import com.zutubi.diff.Patch
import com.zutubi.diff.PatchFile
import com.zutubi.diff.PatchType
import com.zutubi.diff.unified.UnifiedHunk
import com.zutubi.diff.unified.UnifiedPatch
import groovy.transform.Canonical
import org.apache.commons.io.FilenameUtils
import org.gradle.api.Task
import org.springframework.web.util.HtmlUtils

class DiffWriter {
    private static def SUPPORTED_IMAGE_FORMATS = ["png", "jpg", "jpeg", "svg", "gif"] as Set

    private final TaskExecutionStats executionStats
    private final BufferedWriter bw

    public DiffWriter(File out, TaskExecutionStats executionStats) {
        this.executionStats = executionStats
        this.bw = new BufferedWriter(new FileWriter(out));
    }

    public TaskDiffStats write(Optional<PatchFile> patch) {
        bw.write("<html><head><title>Changes from ${executionStats.name}</title><link rel=\"stylesheet\" type=\"text/css\" href=\"diff.css\"></head><body>")

        int filesTouched = 0
        int added = 0
        int removed = 0
        def changesByType = new HashMap<String, Integer>()

        boolean anyUndeclaredChanges = false
        if(patch.isPresent()) {
            List<Patch> patches = patch.get().getPatches()
            for (Patch p : patches) {
                filesTouched++
                bw.write("<h1>$p.newFile</h1>")

                boolean foundOutput
                for (File f : executionStats.task.getOutputs().files) {
                    if (p.newFile.startsWith(f.absolutePath + "/")) {
                        foundOutput = true
                    }
                }
                if (foundOutput) {
                    bw.write("<p><em>Declared output</em></p>")
                } else {
                    anyUndeclaredChanges = true
                    bw.write("<p><em>Undeclared output</em></p>")
                }

                def extension = FilenameUtils.getExtension(p.newFile)
                if (PatchType.ADD.equals(p.type) && SUPPORTED_IMAGE_FORMATS.contains(extension)) {
                    bw.write("<img src=\"$p.newFile\"/")
                }
                Integer count = changesByType.get(extension)
                if (count == null) {
                    changesByType.put(extension, 1)
                } else {
                    changesByType.put(extension, count + 1)
                }

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
                                    added++
                                    bw.write("<div class=\"added\"><code>+ ${HtmlUtils.htmlEscape(l.content)}</code></div>")
                                    break;
                                case UnifiedHunk.LineType.DELETED:
                                    removed++
                                    bw.write("<div class=\"deleted\"><code>- ${HtmlUtils.htmlEscape(l.content)}</code></div>")
                                    break;
                                case UnifiedHunk.LineType.COMMON:
                                    bw.write("<div class=\"common\"><code>= ${HtmlUtils.htmlEscape(l.content)}</code></div>")
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

        new TaskDiffStats(
                filesTouched: filesTouched,
                hunksAdded: added,
                hunksRemoved: removed,
                anyUndeclaredChanges: anyUndeclaredChanges,
                changesByType: changesByType)
    }

    public void close() {
        bw.write("</body></html>")
        bw.close()
    }
}
