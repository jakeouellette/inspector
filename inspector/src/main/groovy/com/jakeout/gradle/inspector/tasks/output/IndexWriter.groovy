package com.jakeout.gradle.inspector.tasks.output

import com.jakeout.gradle.inspector.tasks.model.TaskDiffResults
import com.jakeout.gradle.inspector.tasks.model.TaskExecutionResults
import com.jakeout.gradle.inspector.tasks.model.AnalysisResult
import com.jakeout.gradle.utils.TaskUtil

import java.nio.file.Files

class IndexWriter {

    private static String REPLACED_NODES = "// !-- REPLACE WITH NODES --!"
    private static String REPLACED_EDGES = "// !-- REPLACE WITH EDGES --!"
    private static String REPLACED_SIDEBAR = "<!-- SIDEBAR CONTENT -->"

    public static
    final write(File index, Map<String, File> subProjectIndicies, List<AnalysisResult> children, File visFile) {
        updateFile(index, getSidebarReplacer(children, subProjectIndicies))
        updateFile(visFile, getGraphReplacer(children))
    }

    public static Closure getSidebarReplacer(List<AnalysisResult> children, Map<String, File> subProjectIndicies) {
        return { br, bw ->
            String s
            while ((s = br.readLine()) != null) {
                if (s.contains(REPLACED_SIDEBAR)) {
                    bw.write('<ul>')
                    subProjectIndicies.each { name, file ->
                        bw.write("<li><a href=\"$file\">$name</a></li>")
                    }
                    bw.write('</ul>')
                    bw.write('<ul>')
                    for (AnalysisResult analysisResult : children) {
                        def executionResults = analysisResult.executionResults
                        def diffResults = analysisResult.diffResults
                        if (diffResults.filesTouched > 0) {
                            bw.write("<li><a href=\"$executionResults.path\">$executionResults.name</a>"
                                    + "[changed:$diffResults.filesTouched]"
                                    + "[+$diffResults.hunksAdded]"
                                    + "[-$diffResults.hunksRemoved]"
                                    + "</li>")
                        } else {
                            bw.write("<li>$executionResults.name</li>")
                        }
                    }
                    bw.write('</ul>')
                } else {
                    bw.writeLine(s)
                }
            }
        }
    }

    public static Closure getGraphReplacer(List<AnalysisResult> children) {
        return { br, bw ->
            String s
            while ((s = br.readLine()) != null) {
                if (s.equals(REPLACED_NODES)) {
                    writeNodes(bw, children)
                } else if (s.equals(REPLACED_EDGES)) {
                    writeEdges(bw, children)
                } else {
                    bw.writeLine(s)
                }
            }
        }
    }

    public static void updateFile(File visFile, Closure replacement) {
        File tmpFile = new File(visFile.getAbsolutePath() + ".tmp")
        BufferedWriter bw = new BufferedWriter(new FileWriter(tmpFile))
        FileReader fr = new FileReader(visFile)
        BufferedReader br = new BufferedReader(fr)

        replacement(br, bw)

        bw.close()
        br.close()
        Files.delete(visFile.toPath())
        Files.move(tmpFile.toPath(), visFile.toPath())
    }

    public static void writeNodes(BufferedWriter bw, List<AnalysisResult> results) {
        boolean first = true
        results.each { AnalysisResult analysisResult ->
            TaskExecutionResults tes = analysisResult.executionResults
            TaskDiffResults d = analysisResult.diffResults
            if (!first) {
                bw.write(', \n')
            }

            first = false
            String color = (d.changesByType == null || d.changesByType.isEmpty()) ?
                    '#fff' :
                    (d.anyUndeclaredChanges ? '#88f' : '#f88')

            String description = d.changesByType.isEmpty() ? '' : d.changesByType.toString()
            def style =
                    "basefill: \"$color\", \n" +
                            "style: \"fill:$color\", \n"
            bw.write("\"$tes.name\": { \n $style" +
                    "description: \"$description\" }")
        }

    }

    public static void writeEdges(BufferedWriter bw, List<AnalysisResult> tasks) {
        Set<String> names = tasks.collect { d -> d.executionResults.name }.toSet()

        tasks.each { AnalysisResult analysisResult ->
            TaskExecutionResults tes = analysisResult.executionResults
            tes.dependsOnTasks.each { dependsOn ->
                List<String> inputs = TaskUtil.dependentFiles(tes.task, dependsOn)
                // TODO: add an on hover effect
                if (names.contains(tes.name) && names.contains(dependsOn.name)) {
                    bw.writeLine("g.setEdge(\"$dependsOn.name\", \"$tes.name\", { label: \"\" });")
                }
            }
        }
    }
}
