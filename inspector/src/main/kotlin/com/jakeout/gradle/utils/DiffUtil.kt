package com.jakeout.gradle.utils

import com.zutubi.diff.PatchFile
import com.zutubi.diff.PatchFileParser
import com.zutubi.diff.unified.UnifiedPatchParser
import org.apache.commons.io.FileUtils
import org.codehaus.groovy.runtime.DefaultGroovyMethods
import org.codehaus.groovy.runtime.ProcessGroovyMethods
import java.io.BufferedWriter
import java.io.File
import java.io.FileReader
import java.io.FileWriter

public object DiffUtil {

    fun backup(f: File, target: File) {
        FileUtils.copyDirectory(f, target)
    }

    fun diff(source: File, target: File, out: File): PatchFile? {
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
            return PatchFileParser(UnifiedPatchParser()).parse(FileReader(out))
        } else {
            return null
        }
    }
}
