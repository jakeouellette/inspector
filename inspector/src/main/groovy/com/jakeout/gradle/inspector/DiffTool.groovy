package com.jakeout.gradle.inspector

import org.apache.commons.io.FileUtils

class DiffTool {

    public static void backup(File f, File target) {
        FileUtils.copyDirectory(f, target)
    }

    public static String diff(File source, File target, File out) {
        def cmd = "diff -rNu $target.absolutePath $source.absolutePath"
        def sout = new StringBuffer()
        def serr = new StringBuffer()
        def proc = cmd.execute()
        proc.consumeProcessOutput(sout, serr)
        proc.waitFor()
        String soutStr = sout.toString()
        if (!soutStr.isEmpty()) {
            FileUtils.write(out, sout);
        }
        sout
    }
}
