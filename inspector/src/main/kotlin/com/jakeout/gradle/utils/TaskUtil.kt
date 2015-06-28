package com.jakeout.gradle.utils

import org.gradle.api.Task
import java.util.LinkedList

public object TaskUtil {

    fun dependentFiles(parent: Task, child: Task): List<String> {
        val dependentFiles = LinkedList<String>()

        for (intoParent in parent.getInputs().getFiles()) {
            for (outFromChild in child.getOutputs().getFiles()) {
                if (intoParent.getAbsolutePath().startsWith(outFromChild.getAbsolutePath())) {
                    if (intoParent.getAbsolutePath().equals(outFromChild.getAbsolutePath())) {
                        dependentFiles.add(intoParent.getAbsolutePath())
                    } else {
                        dependentFiles.add(intoParent.getAbsolutePath() + " (" + outFromChild.getAbsolutePath() + ")")
                    }
                }
            }
        }
        return dependentFiles
    }
}
