package com.jakeout.gradle.utils

import com.zutubi.diff.PatchFile
import com.zutubi.diff.PatchFileParser
import com.zutubi.diff.unified.UnifiedPatchParser
import org.apache.commons.io.FileUtils
import org.codehaus.groovy.runtime.DefaultGroovyMethods
import org.codehaus.groovy.runtime.ProcessGroovyMethods
import java.io.*
import java.nio.file.Files
import java.util.regex.Pattern

public object DiffUtil {

    fun backup(f: File, target: File) {
        FileUtils.copyDirectory(f, target)
    }

    fun diff(source: File, target: File, out: File, includeBinary: Boolean): PatchFile? {

        val cmd = "diff -rNu ${target.getAbsolutePath()} ${source.getAbsolutePath()}"
        val sout = StringBuffer()
        val serr = StringBuffer()
        val proc = DefaultGroovyMethods.execute(cmd)
        ProcessGroovyMethods.consumeProcessOutput(proc, sout, serr)
        proc.waitFor()
        val soutStr: String = sout.toString()
        if (soutStr.length() != 0) {
            val bw = BufferedWriter(FileWriter(out));
            bw.write(soutStr);
            bw.close();
        }

        if (out.exists() && out.length() > 0) {
            if (includeBinary) {
                updateFile(out, binaryWrap())
            }
            return PatchFileParser(UnifiedPatchParser()).parse(FileReader(out))
        } else {
            return null
        }
    }

    fun updateFile(diff: File, replacement: (BufferedReader, BufferedWriter) -> Unit) {
        val tmpFile = File(diff.getAbsolutePath() + ".tmp")
        val bw = BufferedWriter(FileWriter(tmpFile))
        val fr = FileReader(diff)
        val br = BufferedReader(fr)

        replacement(br, bw)

        bw.close()
        br.close()
        Files.delete(diff.toPath())
        Files.move(tmpFile.toPath(), diff.toPath())
    }

    fun binaryWrap(): (BufferedReader, BufferedWriter) -> Unit {
        return { br, bw ->
            var s = br.readLine()
            while (s != null) {
                bw.write(binaryWrap(s))
                s = br.readLine()
            }
        }
    }

    fun binaryWrap(diff: String): String {
        val pattern = Pattern.compile(("Binary files (.*) and (.*) differ"))
        val matcher = pattern.matcher(diff.toString())
        if (matcher.matches() && matcher.groupCount() == 2) {
            val from = matcher.group(1)
            val to = matcher.group(2)
            val fromFile = File(from)
            val toFile = File(to)
            var retString =
                    "diff -rNuaq " + from + " " + to + "\n" +
                            "--- " + from + "\t1969-12-31 19:00:00.000000000 -0500 \n" +
                            "+++ " + to + "\t1969-12-31 19:00:00.000000000 -0500 \n"
            if (fromFile.exists() && toFile.exists()) {
                // should never happen
                retString += "@@ -0,0 +0,0 @@\n"
            } else if (fromFile.exists() && !toFile.exists()) {
                retString += "@@ -1,1 +0,0 @@\n-"
            } else if (!fromFile.exists() && toFile.exists()) {
                retString += "@@ +1,1 +0,0 @@\n+"
            } else {
                // should never happen
                retString += "@@ -0,0 +0,0 @@\n"
            }
            return retString
        }
        return diff + "\n"
    }
}
