package com.jakeout.gradle.inspector.tasks

import org.gradle.api.Task

class TasksUtil {
    public static List<String> dependentFiles(Task parent, Task child) {
        List<String> dependentFiles = new LinkedList<String>()

        for (File intoParent : parent.getInputs().getFiles()) {
            for (File outFromChild : child.getOutputs().getFiles()) {
                if (intoParent.absolutePath.startsWith(outFromChild.absolutePath)) {
                    if (intoParent.absolutePath.equals(outFromChild.absolutePath)) {
                        dependentFiles.add(intoParent.absolutePath)
                    } else {
                        dependentFiles.add(intoParent.absolutePath + ' (' + outFromChild.absolutePath + ')')
                    }
                }
            }
        }
        return dependentFiles
    }
}
