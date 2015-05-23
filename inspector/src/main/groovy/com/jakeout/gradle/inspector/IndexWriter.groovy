package com.jakeout.gradle.inspector

import java.nio.file.Files

class IndexWriter {

    private static String REPLACED_NODES = "// !-- REPLACE WITH NODES --!"
    private static String REPLACED_EDGES = "// !-- REPLACE WITH EDGES --!"

    public static final write(File index, List<TaskExecution> children, Set<String> tasksExecuted, File visFile) {
        // Disable writing to the index for the moment
        // TODO: generate more value with this, then re-add it
        //writeIndex(index, children);

        updateDag(visFile, children, tasksExecuted)
    }

    public static void writeIndex(File index, List<TaskExecution> children) {
        BufferedWriter bw = new BufferedWriter(new FileWriter(index));
        bw.write('<ul>')
        for (TaskExecution child : children) {
            if (child.path && child.filesTouched > 0) {
                bw.write("<li><a href=\"$child.path\">$child.name</a>"
                        + "[changed:$child.filesTouched]"
                        + "[+$child.hunksAdded]"
                        + "[-$child.hunksRemoved]<br/>"
                        + "$child.changesByType<br/>"
                        + "$child.dependsOnTasks<br/>"
                        + "</li>")
            } else {
                bw.write("<li>$child.name<br/>"
                        + "$child.dependsOnTasks<br/>"
                        + "</li>")
            }
        }

        bw.write('</ul>')
        bw.write('</body>')
        bw.close()
    }

    public static void updateDag(File visFile, List<TaskExecution> children, Set<String> tasksExecuted) {
        File tmpFile = new File(visFile.getAbsolutePath() + ".tmp")
        BufferedWriter bw2 = new BufferedWriter(new FileWriter(tmpFile));
        FileReader fr = new FileReader(visFile);
        String s;
        BufferedReader br = new BufferedReader(fr)

        while ((s = br.readLine()) != null) {

            if (s.equals(REPLACED_NODES)) {
                writeNodes(bw2, children)
            } else if (s.equals(REPLACED_EDGES)) {
                writeEdges(bw2, children, tasksExecuted)
            } else {
                bw2.writeLine(s)
            }
        }
        bw2.close()
        br.close()
        Files.delete(visFile.toPath())
        Files.move(tmpFile.toPath(), visFile.toPath())
    }

    public static void writeNodes(BufferedWriter bw, List<TaskExecution> tasks) {
        boolean first = true
        tasks.each { TaskExecution t ->
            if (!first) {
                bw.write(', \n')
            }
            first = false
            String color = (t.changesByType == null || t.changesByType.size == 0) ?
                    "#fff" :
                    (t.anyUndeclaredChanges ? "#77f" : "#7f7")
            def style =
                    "basefill: \"" + color + "\", \n" +
                            "style: \"fill:" + color + "\", \n"
            bw.write("\"$t.name\": { \n $style" +
                    "description: \"${t.changesByType.toString()}\" }")
        }

    }

    public static void writeEdges(BufferedWriter bw, List<TaskExecution> tasks, Set<String> tasksExecuted) {


        tasks.each { TaskExecution t ->

            t.dependsOnTasks.each { d ->
                if (tasksExecuted.contains(t.name) && tasksExecuted.contains(d.name)) {
                    bw.writeLine("g.setEdge(\"$d.name\", \"$t.name\", { label: \"\" });")
                }
            }
        }
    }
}
