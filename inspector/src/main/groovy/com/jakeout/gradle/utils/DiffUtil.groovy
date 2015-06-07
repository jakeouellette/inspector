package com.jakeout.gradle.utils

import com.zutubi.diff.PatchFile
import com.zutubi.diff.PatchFileParser
import com.zutubi.diff.unified.UnifiedPatchParser
import org.apache.commons.io.FileUtils

class DiffUtil {

    public static void backup(File f, File target) {
        FileUtils.copyDirectory(f, target)
    }

    public static Optional<PatchFile> diff(File source, File target, File out) {
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
        (out.exists() && out.size() > 0) ?
                Optional.of(new PatchFileParser(new UnifiedPatchParser()).parse(new FileReader(out))) :
                Optional.empty()
    }
}
