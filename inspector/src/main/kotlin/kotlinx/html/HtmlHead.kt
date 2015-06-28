package kotlinx.html

public fun HTML.head(init: HEAD.() -> Unit): Unit {
    build(HEAD(this), init)
}

public fun HEAD.title(init: TITLE.() -> Unit = { }): Unit {
    build(TITLE(this), init)
}

public fun HEAD.title(text: String) {
    build(TITLE(this), { +text })
}

public fun HEAD.link(href: String, rel: String = "stylesheet", mimeType: String = "text/css", content: _LINK.() -> Unit = { }) {
    return link(DirectLink(href), rel, mimeType, content)
}

public fun HEAD.link(href: Link, rel: String = "stylesheet", mimeType: String = "text/css", content: _LINK.() -> Unit = { }) {
    val tag = build(_LINK(this), content)
    tag.href = href
    tag.rel = rel
    tag.mimeType = mimeType
}

public fun HEAD.meta(name: String, content: String, body: META.() -> Unit = empty_contents) {
    val tag = build(META(this), body)
    tag.name = name
    tag.content = content
}

public fun HEAD.base(href: String, target: String, content: BASE.() -> Unit = empty_contents) {
    val tag = build(BASE(this), content)
    tag.href = href
    tag.target = target
}

public fun HtmlTag.script(src: Link, mimeType: String = "text/javascript") {
    val tag = build(SCRIPTSRC(this), { })
    tag.src = src
    tag.mimeType = mimeType
}

public fun HtmlTag.script(mimeType: String = "text/javascript", content: SCRIPTBLOCK.() -> Unit) {
    val tag = build(SCRIPTBLOCK(this), content)
    tag.mimeType = mimeType
}

public class HEAD(containingTag: HTML) : HtmlTag(containingTag, "head") {
}

public class META(containingTag: HEAD) : HtmlTag(containingTag, "meta") {
    public var name: String by Attributes.name
    public var content: String by StringAttribute("content")
}

public class BASE(containingTag: HEAD) : HtmlTag(containingTag, "base") {
    public var href: String by StringAttribute("href")
    public var target: String by StringAttribute("target")
}

public class _LINK(containingTag: HEAD) : HtmlTag(containingTag, "link", RenderStyle.empty) {
    public var href: Link by Attributes.href
    public var rel: String by Attributes.rel
    public var mimeType: String by Attributes.mimeType

    init {
        rel = "stylesheet"
        mimeType = "text/css"
    }
}

public class SCRIPTSRC(containingTag: HtmlTag) : HtmlTag(containingTag, "script") {
    public var src: Link by Attributes.src
    public var mimeType: String by Attributes.mimeType

    init {
        mimeType = "text/javascript"
    }
}

public class SCRIPTBLOCK(containingTag: HtmlTag) : HtmlTag(containingTag, "script") {
    public var mimeType: String by Attributes.mimeType

    init {
        mimeType = "text/javascript"
    }
}

public class TITLE(containingTag: HEAD) : HtmlTag(containingTag, "title")

public fun HtmlBodyTag.noscript(c: String? = null, contents: NOSCRIPT.() -> Unit = empty_contents): Unit = contentTag(NOSCRIPT(this), c, contents)
public class NOSCRIPT(containingTag: HtmlBodyTag) : HtmlBodyTag(containingTag, "noscript")
