package com.jakeout.gradle.inspector.tasks.output

import com.jakeout.gradle.inspector.tasks.model.AnalysisResult
import kotlinx.html.*
import java.io.*
import java.nio.file.Files

object IndexWriter {
    val REPLACED_NODES = "// !-- REPLACE WITH NODES --!"
    val REPLACED_EDGES = "// !-- REPLACE WITH EDGES --!"
    val REPLACED_SIDEBAR = "<!-- SIDEBAR CONTENT -->"

    fun write(index: File,
              subProjectIndicies: Map<String, File>,
              children: List<AnalysisResult>,
              visFile: File) {
        updateFile(index, getSidebarReplacer(children, subProjectIndicies))
        updateFile(visFile, getGraphReplacer(children))
    }

    fun getSidebarReplacer(children: List<AnalysisResult>, subProjectIndicies: Map<String, File>)
            : (BufferedReader, BufferedWriter) -> Unit {
        return { br, bw ->
            var s = br.readLine()
            val partial = partial {
                while (s != null) {
                    if (s.contains(REPLACED_SIDEBAR)) {
                        if (subProjectIndicies.size() > 0) {
                            ul {
                                for (entry in subProjectIndicies.entrySet()) {
                                    li {
                                        a {
                                            attribute("href", entry.getValue().toString())
                                            +entry.getKey()
                                        }
                                    }
                                }
                            }
                        }
                        if (children.size() > 0) {
                            ul {
                                for (analysisResult in children) {
                                    val executionResults = analysisResult.executionResults
                                    val diffResults = analysisResult.diffResults
                                    if (diffResults.filesTouched > 0) {
                                        li {
                                            a {
                                                attribute("href", executionResults.path)
                                                +executionResults.name
                                            }
                                            +"[changed:${diffResults.filesTouched}]"
                                            +"[+${diffResults.hunksAdded}]"
                                            +"[-${diffResults.hunksRemoved}]"
                                        }
                                    } else {
                                        li {
                                            +executionResults.name
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        !(s + "\n")
                    }
                    s = br.readLine()
                }
            }
            bw.write(partial.toString())
        }
    }

    fun getGraphReplacer(children: List<AnalysisResult>)
            : (BufferedReader, BufferedWriter) -> Unit {
        return { br, bw ->
            var s = br.readLine()
            while (s != null) {
                if (s.equals(REPLACED_NODES)) {
                    writeNodes(bw, children)
                } else if (s.equals(REPLACED_EDGES)) {
                    writeEdges(bw, children)
                } else {
                    bw.write(s)
                    bw.newLine()
                }
                s = br.readLine()
            }
        }
    }

    fun updateFile(visFile: File, replacement: (BufferedReader, BufferedWriter) -> Unit) {
        val tmpFile = File(visFile.getAbsolutePath() + ".tmp")
        val bw = BufferedWriter(FileWriter(tmpFile))
        val fr = FileReader(visFile)
        val br = BufferedReader(fr)

        replacement(br, bw)

        bw.close()
        br.close()
        Files.delete(visFile.toPath())
        Files.move(tmpFile.toPath(), visFile.toPath())
    }

    fun writeNodes(bw: BufferedWriter, results: List<AnalysisResult>) {
        var first = true
        for (analysisResult in results) {
            val tes = analysisResult.executionResults
            val d = analysisResult.diffResults
            if (!first) {
                bw.write(", \n")
            }

            first = false
            val color =
                    if (d.changesByType.isEmpty())
                        "#fff"
                    else
                        if (d.anyUndeclaredChanges) "#88f" else "#8f8"

            val description = if (d.changesByType.isEmpty()) "" else d.changesByType.toString()
            val style =
                    "basefill: \"$color\", \n" +
                            "style: \"fill:$color\", \n"
            bw.write("\"${tes.name}\": { \n $style" +
                    "description: \"$description\" }")
        }
    }

    fun writeEdges(bw: BufferedWriter, tasks: List<AnalysisResult>) {
        val names = tasks.map { d: AnalysisResult -> d.executionResults.name }.toSet()

        for (analysisResult in tasks) {
            val tes = analysisResult.executionResults
            for (dependsOn in tes.dependsOnTasks) {
                // val inputs = TaskUtil.dependentFiles(tes.task, dependsOn)
                // TODO: add an on hover effect
                if (names.contains(tes.name) && names.contains(dependsOn.getName())) {
                    bw.write("g.setEdge(\"${dependsOn.getName()}\", \"${tes.name}\", { label: \"\" });")
                    bw.newLine()
                }
            }
        }
    }
}
