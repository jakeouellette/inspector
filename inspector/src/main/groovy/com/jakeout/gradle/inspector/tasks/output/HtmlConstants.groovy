package com.jakeout.gradle.inspector.tasks.output

public class HtmlConstants {

    private static def UNKNOWN_FILE = """<span class="fa-stack">
    <i class="font-warn fa fa-circle fa-stack-2x"></i>
    <i class="fa-inverse fa fa-question fa-stack-1x fa-stacked-symbol"></i>
    <i class="fa-inverse fa fa-file-o fa-stack-1x"></i>
</span>"""

    private static def KNOWN_FILE = """<span class="fa-stack">
    <i class="fa fa-file-o fa-stack-1x"></i>
</span>"""
}
