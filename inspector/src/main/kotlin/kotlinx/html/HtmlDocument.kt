package kotlinx.html

public open class HTML() : HtmlTag(null, "html", RenderStyle.adaptive) {

    public var doctype: String = "<!DOCTYPE html>"

    override fun renderElement(builder: StringBuilder, indent: String) {
        builder.append("$doctype\n")
        super<HtmlTag>.renderElement(builder, indent)
    }
}

public fun html(init: HTML.() -> Unit): HTML = build(HTML(), init)
