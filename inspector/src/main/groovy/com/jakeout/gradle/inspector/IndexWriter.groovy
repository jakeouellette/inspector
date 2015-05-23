package com.jakeout.gradle.inspector

import com.jakeout.gradle.tasks.TasksUtil

import java.nio.file.Files

class IndexWriter {

    private static String REPLACED_NODES = "// !-- REPLACE WITH NODES --!"
    private static String REPLACED_EDGES = "// !-- REPLACE WITH EDGES --!"

    public static final write(File index, List<TaskStats> children, File visFile) {
        BufferedWriter bw = new BufferedWriter(new FileWriter(index, true));
        bw.write('<body>')
        writeIndex(children, bw);
        updateDag(visFile, children)
        bw.write('</body>')
        bw.close()
    }

    public static void writeIndex(List<TaskStats> children, BufferedWriter bw) {

        bw.write('<ul>')
        for (TaskStats taskStats : children) {
            def executionStats = taskStats.executionStats
            def diffStats = taskStats.diffStats
            String dependsOn = (executionStats.dependsOnTasks.isEmpty() ? "" : "$executionStats.dependsOnTasks<br/>")
            if (diffStats.filesTouched > 0) {
                bw.write("<li><a href=\"$executionStats.path\">$executionStats.name</a>"
                        + "[changed:$diffStats.filesTouched]"
                        + "[+$diffStats.hunksAdded]"
                        + "[-$diffStats.hunksRemoved]<br/>"
                        + (diffStats.changesByType.isEmpty() ? "" : "$diffStats.changesByType<br/>")
                        + dependsOn
                        + "</li>")
            } else {
                bw.write("<li>$executionStats.name<br/>$dependsOn</li>")
            }
        }
        bw.write('</ul>')
    }

    public static void updateDag(File visFile, List<TaskStats> children) {
        File tmpFile = new File(visFile.getAbsolutePath() + ".tmp")
        BufferedWriter bw2 = new BufferedWriter(new FileWriter(tmpFile));
        FileReader fr = new FileReader(visFile);
        String s;
        BufferedReader br = new BufferedReader(fr)

        while ((s = br.readLine()) != null) {

            if (s.equals(REPLACED_NODES)) {
                writeNodes(bw2, children)
            } else if (s.equals(REPLACED_EDGES)) {
                writeEdges(bw2, children)
            } else {
                bw2.writeLine(s)
            }
        }
        bw2.close()
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
