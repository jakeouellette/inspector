package com.jakeout.gradle.utils.gtree

import org.junit.Assert
import org.junit.Test

class GTreeTest {

    // TODO: The current way I'm testing diffs is a bit heuristic.
    // Avoid searching into strings.

    @Test
    public void testTreesAreSame() {
        def node1 = GTrees.create("a") {
            b "2"
        }

        def node2 = GTrees.create("a") {
            b "2"
        }

        println("testTreesAreSame:")
        println(node1)
        println(node2)
        println(GTrees.diff(node1, node2))
        def diff = GTrees.diff(node1, node2)
        println(diff)
        Assert.assertEquals(0, diff.toString().count("-b"))
        Assert.assertEquals(0, diff.toString().count("+b"))
        Assert.assertEquals(0, diff.toString().count("~b"))
    }

    @Test
    public void testTreesAreDifferent() {
        def a = GTrees.create("a") {
            b "1"
        }

        def b = GTrees.create("a") {
            b "2"
        }
        println("testTreesAreDifferent:")
        println(a)
        println(b)
        def diff = GTrees.diff(a, b)
        println(diff)
        Assert.assertEquals(0, diff.toString().count("-b"))
        Assert.assertEquals(0, diff.toString().count("+b"))
        Assert.assertEquals(1, diff.toString().count("~b"))
    }

    @Test
    public void testTreesSupportDeleteAndAdd() {
        def node1 = GTrees.create("a") {
            b {
                c {
                    d {
                        e 1

                        i {

                        }
                    }

                    f 3
                }
            }
        }

        def node2 = GTrees.create("a") {
            b {
                c {
                    d {
                        e 2
                    }

                }

                g {
                    h 3
                }
            }
        }

        def diff = GTrees.diff(node1, node2)
        println(diff)
        Assert.assertEquals(1, diff.toString().count("-f"))
        Assert.assertEquals(1, diff.toString().count("-i"))
        Assert.assertEquals(1, diff.toString().count("+g"))
        Assert.assertEquals(1, diff.toString().count("+h"))
        Assert.assertEquals(1, diff.toString().count("~e"))
    }

    @Test
    public void treeIsExtractable() {
        def exampleProjectMap = [android: [defaultConfig:
                                                   [minSdkVersion   : 1,
                                                    targetSdkVersion: 2]]]

        def exampleProjectTree = GTrees.create("project") {
            android {
                defaultConfig {
                    minSdkVersion 1
                    targetSdkVersion 2
                }
            }
        }

        def template = GTrees.createTemplate {
            android {
                defaultConfig {
                    minSdkVersion()
                    targetSdkVersion()
                }
            }
        }

        def result = GTrees.extract(template, exampleProjectMap)

        def diff = GTrees.diff(exampleProjectTree, result)

        println(diff)
        Assert.assertEquals("1", result.android.defaultConfig.minSdkVersion.value)
        Assert.assertEquals("2", result.android.defaultConfig.targetSdkVersion.value)
    }

    @Test
    public void extractedTreeHasOneDifference() {

        def exampleProjectMap = [android: [defaultConfig:
                                                   [minSdkVersion   : 2,
                                                    targetSdkVersion: 2]]]

        def exampleProjectMap2 = [android: [defaultConfig:
                                                    [minSdkVersion   : 1,
                                                     targetSdkVersion: 2]]]

        def template = GTrees.createTemplate {
            android {
                defaultConfig {
                    minSdkVersion()
                    targetSdkVersion()
                }
            }
        }

        def extraction1 = GTrees.extract(template, exampleProjectMap)
        def extraction2 = GTrees.extract(template, exampleProjectMap2)

        def diff = GTrees.diff(extraction1, extraction2)

        println(diff)
        Assert.assertEquals("1", diff.android.defaultConfig.minSdkVersion.newValueIfAny)
        Assert.assertEquals(HasDiff.Diff.CONTENTS_CHANGED, diff.android.defaultConfig.minSdkVersion.changeType)
        Assert.assertEquals("2", diff.android.defaultConfig.targetSdkVersion.originalValueIfExisted)
        Assert.assertEquals(HasDiff.Diff.UNCHANGED, diff.android.defaultConfig.targetSdkVersion.changeType)
    }
}
