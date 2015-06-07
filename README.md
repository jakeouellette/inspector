# inspector
Gradle build inspector, clarifies what's going on inside your Gradle build. Shows change in your file system during a build.

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

 - __showInspection__: to auto-open the web URL

- __compareLastBuild__: Compare against the last build (Each build throws away intermediate results unless this option
is turned on, so you must enable this for at least one build for it to work.)