package com.jakeout.gradle.inspector

import com.zutubi.diff.Patch
import com.zutubi.diff.PatchFile
import com.zutubi.diff.PatchType
import com.zutubi.diff.unified.UnifiedHunk
import com.zutubi.diff.unified.UnifiedPatch
import org.apache.commons.io.FilenameUtils
import org.gradle.api.Task
import org.springframework.web.util.HtmlUtils

class DiffWriter {

    private static def SUPPORTED_IMAGE_FORMATS = ["png", "jpg", "jpeg", "svg", "gif"] as Set

    private final File out
    private final String name
    private final String relPath
    private final Map<String, Integer> changesByType = new HashMap<String, Integer>()
    private final BufferedWriter bw
    private final List<Task> dependsOnTask
    private final List<Task> taskGraphDependencies

    public DiffWriter(File out, String name, String relPath, List<Task> dependsOnTask) {
        this.name = name
        this.relPath = relPath
        this.out = out
        this.dependsOnTask = dependsOnTask
        this.taskGraphDependencies = taskGraphDependencies
        this.bw = new BufferedWriter(new FileWriter(out));
        bw.write("<html><head><title>Changes from ${name}</title><link rel=\"stylesheet\" type=\"text/css\" href=\"diff.css\"></head><body>")
    }

    public TaskExecution write(PatchFile patch) {
        int filesTouched = 0
        int added = 0
        int removed = 0

        List<Patch> patches = patch.getPatches()
        for (Patch p : patches) {
            filesTouched++
            bw.write("<h1>$p.newFile</h1>")
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


        new TaskExecution(
                path: relPath,
                name: name,
                filesTouched: filesTouched,
                hunksAdded: added,
                hunksRemoved: removed,
                changesByType: changesByType,
                dependsOnTasks: dependsOnTask)
    }

    public void close() {
        bw.write("</body></html>")
        bw.close()
    }
}
