package com.jakeout.gradle.inspector.tasks.output

public class HtmlConstants {

    private static def UNKNOWN_FILE = """<span class="fa-stack file-type-icon">
    <i class="font-warn fa fa-circle fa-stack-2x"></i>
    <i class="fa-inverse fa fa-question fa-stack-1x fa-stacked-symbol"></i>
    <i class="fa-inverse fa fa-file-o fa-stack-1x"></i>
</span>
<span class="warning"><em>undeclared output</em></span>"""

    private static def KNOWN_FILE = """<span class="fa-stack file-type-icon">
    <i class="fa fa-file-o fa-stack-1x"></i>
</span>"""
}
