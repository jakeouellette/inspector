# inspector

Gradle build inspector, clarifies what's going on inside your Gradle build. Features:

 - Makes it easy to understand your task dependencies by generating a task dependency graph.

 - Makes it easy to know what files have changed on disk by running diff (windows not supported)

 - *incubating* Compare the differences in file changes on disk between two builds by using -PcompareLastBuild

## To use:

Checkout and install locally:

    >/gradlew install

Then, add the plugin and mavenLocal() to your build:

    buildscript {
        repositories {
            mavenLocal()
        }
        dependencies {
            classpath 'com.jakeout:gradle-inspector:+'
        }
    }


    allprojects {
        apply plugin: 'com.jakeout.gradle-inspector'
    }

## Build properties

*Run with -PpropertyName to enable*

 - __showInspection__: to auto-open the web URL.

- *incubating* __compareLastBuild__: Generates a report comparing this build's inputs and outputs against the last
build you made. These changes are written to the report page for each task (above the normal report.)

This can be handy (e.g., if you're trying to figure out what is different about a new version of a Gradle plugin.)

Note: Each build throws away intermediate results unless this option is turned on, so you must enable this for at least
one build for it to work.