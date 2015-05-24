package com.jakeout.gradle.inspector.output

import com.jakeout.gradle.inspector.TaskDiffStats
import com.jakeout.gradle.inspector.TaskExecutionStats
import com.jakeout.gradle.inspector.TaskStats
import com.jakeout.gradle.tasks.TasksUtil

import java.nio.file.Files

class IndexWriter {

    private static String REPLACED_NODES = "// !-- REPLACE WITH NODES --!"
    private static String REPLACED_EDGES = "// !-- REPLACE WITH EDGES --!"
    private static String REPLACED_SIDEBAR = "<!-- SIDEBAR CONTENT -->"

    public static final write(File index, Map<String, File> subProjectIndicies, List<TaskStats> children, File visFile) {
        updateFile(index, getSidebarReplacer(children, subProjectIndicies))
        updateFile(visFile, getGraphReplacer(children))
    }

    public static Closure getSidebarReplacer(List<TaskStats> children, Map<String, File> subProjectIndicies) {
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
                    for (TaskStats taskStats : children) {
                        def executionStats = taskStats.executionStats
                        def diffStats = taskStats.diffStats
                        String dependsOn = (executionStats.dependsOnTasks.isEmpty() ? "" : "$executionStats.dependsOnTasks<br/>")
                        if (diffStats.filesTouched > 0) {
                            bw.write("<li><a href=\"$executionStats.path\">$executionStats.name</a>"
                                    + "[changed:$diffStats.filesTouched]"
                                    + "[+$diffStats.hunksAdded]"
                                    + "[-$diffStats.hunksRemoved]"
                                    + "</li>")
                        } else {
                            bw.write("<li>$executionStats.name</li>")
                        }
                    }
                    bw.write('</ul>')
                } else {
                    bw.writeLine(s)
                }
            }
        }
    }

    public static Closure getGraphReplacer(List<TaskStats> children) {
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

    public static void writeNodes(BufferedWriter bw, List<TaskStats> tasks) {
        boolean first = true
        tasks.each { TaskStats taskStats ->
            TaskExecutionStats tes = taskStats.executionStats
            TaskDiffStats d = taskStats.diffStats
            if (!first) {
                bw.write(', \n')
            }

            first = false
            String color = (d.changesByType == null || d.changesByType.isEmpty()) ?
                    "#fff" :
                    (d.anyUndeclaredChanges ? "#77f" : "#7f7")

            String description = d.changesByType.isEmpty() ? "" : d.changesByType.toString()
            def style =
                    "basefill: \"$color\", \n" +
                            "style: \"fill:$color\", \n"
            bw.write("\"$tes.name\": { \n $style" +
                    "description: \"$description\" }")
        }

    }

    public static void writeEdges(BufferedWriter bw, List<TaskStats> tasks) {
        Set<String> names = tasks.collect { d -> d.executionStats.name }.toSet()

        tasks.each { TaskStats taskStats ->
            TaskExecutionStats tes = taskStats.executionStats
            tes.dependsOnTasks.each { dependsOn ->
                List<String> inputs = TasksUtil.dependentFiles(tes.task, dependsOn)
                // TODO: add an on hover effect
                if (names.contains(tes.name) && names.contains(dependsOn.name)) {
                    bw.writeLine("g.setEdge(\"$dependsOn.name\", \"$tes.name\", { label: \"\" });")
                }
            }
        }
    }
}
