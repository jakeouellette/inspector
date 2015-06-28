package kotlinx.html

val <T> empty_contents: T.() -> Unit = { }

public abstract class HtmlBodyTag(containingTag: HtmlTag?, name: String, renderStyle: RenderStyle = RenderStyle.expanded, contentStyle: ContentStyle = ContentStyle.block) : HtmlTag(containingTag, name, renderStyle, contentStyle) {
    public var id: String by Attributes.id
    public var style: String by Attributes.style

    public fun addClass(c: String) {
        val old = tryGet("class")
        setClass(if (old != null && old.isNotEmpty()) "$old $c" else c)
    }

    public fun setClass(c: String) {
        attribute("class", c)
    }
}

public fun <T : HtmlBodyTag> HtmlBodyTag.contentTag(tag: T, styleClass: String? = null, contents: T.() -> Unit = empty_contents) {
    if (styleClass != null) tag.addClass(styleClass)
    build(tag, contents)
}


public fun HtmlBodyTag.a(c: String? = null, contents: A.() -> Unit = empty_contents): Unit = contentTag(A(this), c, contents)
public open class A(containingTag: HtmlBodyTag) : HtmlBodyTag(containingTag, "a", contentStyle = ContentStyle.propagate) {
    public var href: Link by Attributes.href
    public var rel: String by Attributes.rel
    public var target: String by Attributes.target
}

public fun HtmlBodyTag.button(c: String? = null, contents: BUTTON.() -> Unit = empty_contents): Unit = contentTag(BUTTON(this), c, contents)
public open class BUTTON(containingTag: HtmlBodyTag) : HtmlBodyTag(containingTag, "button", RenderStyle.expanded, ContentStyle.propagate) {
    public var name: String by Attributes.name
    public var value: String by Attributes.value
    public var buttonType: String by Attributes.buttonType
    public var href: Link by Attributes.href
}

public fun HTML.body(c: String? = null, contents: BODY.() -> Unit) {
    val tag = BODY(this)
    if (c != null) tag.addClass(c)
    build(tag, contents)
}

public class BODY(containingTag: HTML) : HtmlBodyTag(containingTag, "body")

public fun HtmlBodyTag.hr(c: String? = null): Unit = contentTag(HR(this), c)
public fun HtmlBodyTag.br(c: String? = null): Unit = contentTag(BR(this), c)
public fun HtmlBodyTag.wbr(c: String? = null): Unit = contentTag(WBR(this), c)
public fun HtmlBodyTag.div(c: String? = null, contents: DIV.() -> Unit = empty_contents): Unit = contentTag(DIV(this), c, contents)
public fun HtmlBodyTag.b(c: String? = null, contents: B.() -> Unit = empty_contents): Unit = contentTag(B(this), c, contents)
public fun HtmlBodyTag.i(c: String? = null, contents: I.() -> Unit = empty_contents): Unit = contentTag(I(this), c, contents)
public fun HtmlBodyTag.p(c: String? = null, contents: P.() -> Unit = empty_contents): Unit = contentTag(P(this), c, contents)
public fun HtmlBodyTag.pre(c: String? = null, contents: PRE.() -> Unit = empty_contents): Unit = contentTag(PRE(this), c, contents)
public fun HtmlBodyTag.span(c: String? = null, contents: SPAN.() -> Unit = empty_contents): Unit = contentTag(SPAN(this), c, contents)
public fun HtmlBodyTag.sub(c: String? = null, contents: SUB.() -> Unit = empty_contents): Unit = contentTag(SUB(this), c, contents)
public fun HtmlBodyTag.sup(c: String? = null, contents: SUP.() -> Unit = empty_contents): Unit = contentTag(SUP(this), c, contents)
public fun HtmlBodyTag.ins(c: String? = null, contents: INS.() -> Unit = empty_contents): Unit = contentTag(INS(this), c, contents)
public fun HtmlBodyTag.del(c: String? = null, contents: DEL.() -> Unit = empty_contents): Unit = contentTag(DEL(this), c, contents)
public fun HtmlBodyTag.s(c: String? = null, contents: S.() -> Unit = empty_contents): Unit = contentTag(S(this), c, contents)
public fun HtmlBodyTag.u(c: String? = null, contents: U.() -> Unit = empty_contents): Unit = contentTag(U(this), c, contents)
public fun HtmlBodyTag.abbr(c: String? = null, contents: ABBR.() -> Unit = empty_contents): Unit = contentTag(ABBR(this), c, contents)
public fun HtmlBodyTag.small(c: String? = null, contents: SMALL.() -> Unit = empty_contents): Unit = contentTag(SMALL(this), c, contents)
public fun HtmlBodyTag.mark(c: String? = null, contents: MARK.() -> Unit = empty_contents): Unit = contentTag(MARK(this), c, contents)
public fun HtmlBodyTag.address(c: String? = null, contents: ADDRESS.() -> Unit = empty_contents): Unit = contentTag(ADDRESS(this), c, contents)
public fun HtmlBodyTag.time(c: String? = null, contents: TIME.() -> Unit = empty_contents): Unit = contentTag(TIME(this), c, contents)
public fun HtmlBodyTag.cite(c: String? = null, contents: CITE.() -> Unit = empty_contents): Unit = contentTag(CITE(this), c, contents)
public fun HtmlBodyTag.q(c: String? = null, contents: Q.() -> Unit = empty_contents): Unit = contentTag(Q(this), c, contents)
public fun HtmlBodyTag.blockquote(c: String? = null, contents: BLOCKQUOTE.() -> Unit = empty_contents): Unit = contentTag(BLOCKQUOTE(this), c, contents)

public open class HR(containingTag: HtmlBodyTag) : HtmlBodyTag(containingTag, "hr", RenderStyle.empty)
public open class BR(containingTag: HtmlBodyTag) : HtmlBodyTag(containingTag, "br", RenderStyle.empty)
public open class WBR(containingTag: HtmlBodyTag) : HtmlBodyTag(containingTag, "wbr", RenderStyle.empty)
public open class DIV(containingTag: HtmlBodyTag) : HtmlBodyTag(containingTag, "div")
public open class B(containingTag: HtmlBodyTag) : HtmlBodyTag(containingTag, "b", contentStyle = ContentStyle.propagate)
public open class I(containingTag: HtmlBodyTag) : HtmlBodyTag(containingTag, "i", contentStyle = ContentStyle.propagate)
public open class P(containingTag: HtmlBodyTag) : HtmlBodyTag(containingTag, "p")
public open class PRE(containingTag: HtmlBodyTag) : HtmlBodyTag(containingTag, "pre")
public open class SPAN(containingTag: HtmlBodyTag) : HtmlBodyTag(containingTag, "span", contentStyle = ContentStyle.propagate)
public open class SUB(containingTag: HtmlBodyTag) : HtmlBodyTag(containingTag, "sub", contentStyle = ContentStyle.propagate)
public open class SUP(containingTag: HtmlBodyTag) : HtmlBodyTag(containingTag, "sup", contentStyle = ContentStyle.propagate)
public open class INS(containingTag: HtmlBodyTag) : HtmlBodyTag(containingTag, "ins", contentStyle = ContentStyle.propagate)
public open class DEL(containingTag: HtmlBodyTag) : HtmlBodyTag(containingTag, "del", contentStyle = ContentStyle.propagate)
public open class S(containingTag: HtmlBodyTag) : HtmlBodyTag(containingTag, "s", contentStyle = ContentStyle.propagate)
public open class U(containingTag: HtmlBodyTag) : HtmlBodyTag(containingTag, "u", contentStyle = ContentStyle.propagate)
public open class ABBR(containingTag: HtmlBodyTag) : HtmlBodyTag(containingTag, "abbr", contentStyle = ContentStyle.propagate)
public open class SMALL(containingTag: HtmlBodyTag) : HtmlBodyTag(containingTag, "small", contentStyle = ContentStyle.propagate)
public open class MARK(containingTag: HtmlBodyTag) : HtmlBodyTag(containingTag, "mark", contentStyle = ContentStyle.propagate)
public open class TIME(containingTag: HtmlBodyTag) : HtmlBodyTag(containingTag, "time")
public open class ADDRESS(containingTag: HtmlBodyTag) : HtmlBodyTag(containingTag, "address")
public open class CITE(containingTag: HtmlBodyTag) : HtmlBodyTag(containingTag, "cite", contentStyle = ContentStyle.propagate)
public open class Q(containingTag: HtmlBodyTag) : HtmlBodyTag(containingTag, "q", contentStyle = ContentStyle.propagate)
public open class BLOCKQUOTE(containingTag: HtmlBodyTag) : HtmlBodyTag(containingTag, "blockquote") {
    public var cite: Link by Attributes.cite
}

public fun HtmlBodyTag.em(c: String? = null, contents: EM.() -> Unit = empty_contents): Unit = contentTag(EM(this), c, contents)
public fun HtmlBodyTag.strong(c: String? = null, contents: STRONG.() -> Unit = empty_contents): Unit = contentTag(STRONG(this), c, contents)
public fun HtmlBodyTag.code(c: String? = null, contents: CODE.() -> Unit = empty_contents): Unit = contentTag(CODE(this), c, contents)
public fun HtmlBodyTag.kbd(c: String? = null, contents: KBD.() -> Unit = empty_contents): Unit = contentTag(KBD(this), c, contents)
public fun HtmlBodyTag.dfn(c: String? = null, contents: DFN.() -> Unit = empty_contents): Unit = contentTag(DFN(this), c, contents)
public fun HtmlBodyTag.samp(c: String? = null, contents: SAMP.() -> Unit = empty_contents): Unit = contentTag(SAMP(this), c, contents)
public fun HtmlBodyTag.variable(c: String? = null, contents: VARIABLE.() -> Unit = empty_contents): Unit = contentTag(VARIABLE(this), c, contents)
public open class EM(containingTag: HtmlBodyTag) : HtmlBodyTag(containingTag, "em", contentStyle = ContentStyle.propagate)
public open class STRONG(containingTag: HtmlBodyTag) : HtmlBodyTag(containingTag, "strong", contentStyle = ContentStyle.propagate)
public open class CODE(containingTag: HtmlBodyTag) : HtmlBodyTag(containingTag, "code")
public open class KBD(containingTag: HtmlBodyTag) : HtmlBodyTag(containingTag, "kbd", contentStyle = ContentStyle.propagate)
public open class DFN(containingTag: HtmlBodyTag) : HtmlBodyTag(containingTag, "dfn", contentStyle = ContentStyle.propagate)
public open class SAMP(containingTag: HtmlBodyTag) : HtmlBodyTag(containingTag, "samp", contentStyle = ContentStyle.propagate)
public open class VARIABLE(containingTag: HtmlBodyTag) : HtmlBodyTag(containingTag, "var", contentStyle = ContentStyle.propagate)

public fun HtmlBodyTag.progress(c: String? = null, contents: PROGRESS.() -> Unit = empty_contents): Unit = contentTag(PROGRESS(this), c, contents)
public fun HtmlBodyTag.meter(c: String? = null, contents: METER.() -> Unit = empty_contents): Unit = contentTag(METER(this), c, contents)
public open class PROGRESS(containingTag: HtmlBodyTag) : HtmlBodyTag(containingTag, "progress", contentStyle = ContentStyle.propagate)
public open class METER(containingTag: HtmlBodyTag) : HtmlBodyTag(containingTag, "meter", contentStyle = ContentStyle.propagate)

public fun HtmlBodyTag.dl(c: String? = null, contents: DL.() -> Unit = empty_contents): Unit = contentTag(DL(this), c, contents)
public fun DL.dt(c: String? = null, contents: DT.() -> Unit = empty_contents): Unit = contentTag(DT(this), c, contents)
public fun DL.dd(c: String? = null, contents: DD.() -> Unit = empty_contents): Unit = contentTag(DD(this), c, contents)

public open class DL(containingTag: HtmlBodyTag) : HtmlBodyTag(containingTag, "dl")
public open class DD(containingTag: DL) : HtmlBodyTag(containingTag, "dd", contentStyle = ContentStyle.propagate)
public open class DT(containingTag: DL) : HtmlBodyTag(containingTag, "dt", contentStyle = ContentStyle.propagate)

public fun HtmlBodyTag.ul(c: String? = null, contents: UL.() -> Unit = empty_contents): Unit = contentTag(UL(this), c, contents)
public fun HtmlBodyTag.ol(c: String? = null, contents: OL.() -> Unit = empty_contents): Unit = contentTag(OL(this), c, contents)
public fun ListTag.li(c: String? = null, contents: LI.() -> Unit = empty_contents): Unit = contentTag(LI(this), c, contents)

public abstract class ListTag(containingTag: HtmlBodyTag, name: String) : HtmlBodyTag(containingTag, name)
public open class OL(containingTag: HtmlBodyTag) : ListTag(containingTag, "ol")
public open class UL(containingTag: HtmlBodyTag) : ListTag(containingTag, "ul")
public open class LI(containingTag: ListTag) : HtmlBodyTag(containingTag, "li")

public fun HtmlBodyTag.h1(c: String? = null, contents: H1.() -> Unit = empty_contents): Unit = contentTag(H1(this), c, contents)
public fun HtmlBodyTag.h2(c: String? = null, contents: H2.() -> Unit = empty_contents): Unit = contentTag(H2(this), c, contents)
public fun HtmlBodyTag.h3(c: String? = null, contents: H3.() -> Unit = empty_contents): Unit = contentTag(H3(this), c, contents)
public fun HtmlBodyTag.h4(c: String? = null, contents: H4.() -> Unit = empty_contents): Unit = contentTag(H4(this), c, contents)
public fun HtmlBodyTag.h5(c: String? = null, contents: H5.() -> Unit = empty_contents): Unit = contentTag(H5(this), c, contents)
public fun HtmlBodyTag.h6(c: String? = null, contents: H6.() -> Unit = empty_contents): Unit = contentTag(H6(this), c, contents)

public open class H1(containingTag: HtmlBodyTag) : HtmlBodyTag(containingTag, "h1")
public open class H2(containingTag: HtmlBodyTag) : HtmlBodyTag(containingTag, "h2")
public open class H3(containingTag: HtmlBodyTag) : HtmlBodyTag(containingTag, "h3")
public open class H4(containingTag: HtmlBodyTag) : HtmlBodyTag(containingTag, "h4")
public open class H5(containingTag: HtmlBodyTag) : HtmlBodyTag(containingTag, "h5")
public open class H6(containingTag: HtmlBodyTag) : HtmlBodyTag(containingTag, "h6")

public fun HtmlBodyTag.img(c: String? = null, contents: IMG.() -> Unit = empty_contents): Unit = contentTag(IMG(this), c, contents)
public open class IMG(containingTag: HtmlBodyTag) : HtmlBodyTag(containingTag, "img", RenderStyle.empty, ContentStyle.text) {
    public var width: Int by Attributes.width
    public var height: Int by Attributes.height
    public var src: Link by Attributes.src
    public var alt: String by Attributes.alt
}


public fun HtmlBodyTag.table(c: String? = null, contents: TABLE.() -> Unit = empty_contents): Unit = contentTag(TABLE(this), c, contents)
public fun TABLE.caption(c: String? = null, contents: CAPTION.() -> Unit = empty_contents): Unit = contentTag(CAPTION(this), c, contents)
public fun TABLE.tbody(c: String? = null, contents: TBODY.() -> Unit = empty_contents): Unit = contentTag(TBODY(this), c, contents)
public fun TABLE.thead(c: String? = null, contents: THEAD.() -> Unit = empty_contents): Unit = contentTag(THEAD(this), c, contents)
public fun TABLE.colgroup(c: String? = null, contents: COLGROUP.() -> Unit = empty_contents): Unit = contentTag(COLGROUP(this), c, contents)
public fun COLGROUP.col(c: String? = null, contents: COL.() -> Unit = empty_contents): Unit = contentTag(COL(this), c, contents)
public fun TABLE.tfoot(c: String? = null, contents: TFOOT.() -> Unit = empty_contents): Unit = contentTag(TFOOT(this), c, contents)
public fun TableTag.tr(c: String? = null, contents: TR.() -> Unit = empty_contents): Unit = contentTag(TR(this), c, contents)
public fun TR.th(c: String? = null, contents: TH.() -> Unit = empty_contents): Unit = contentTag(TH(this), c, contents)
public fun TR.td(c: String? = null, contents: TD.() -> Unit = empty_contents): Unit = contentTag(TD(this), c, contents)

public abstract class TableTag(containingTag: HtmlBodyTag, name: String) : HtmlBodyTag(containingTag, name)
public open class TABLE(containingTag: HtmlBodyTag) : TableTag(containingTag, "table")
public open class CAPTION(containingTag: TABLE) : HtmlBodyTag(containingTag, "caption")
public open class COLGROUP(containingTag: TABLE) : HtmlBodyTag(containingTag, "colgroup")
public open class COL(containingTag: COLGROUP) : HtmlBodyTag(containingTag, "col")
public open class THEAD(containingTag: TABLE) : TableTag(containingTag, "thead")
public open class TFOOT(containingTag: TABLE) : TableTag(containingTag, "tfoot")
public open class TBODY(containingTag: TABLE) : TableTag(containingTag, "tbody")
public open class TR(containingTag: TableTag) : HtmlBodyTag(containingTag, "tr")
public open class TH(containingTag: TR) : HtmlBodyTag(containingTag, "th")
public open class TD(containingTag: TR) : HtmlBodyTag(containingTag, "td")

public fun HtmlBodyTag.form(c: String? = null, contents: FORM.() -> Unit = empty_contents): Unit = contentTag(FORM(this), c, contents)

public open class FORM(containingTag: HtmlBodyTag) : HtmlBodyTag(containingTag, "form") {
    public var action: Link by Attributes.action
    public var enctype: String by Attributes.enctype
    public var method: String by Attributes.method
}

public fun HtmlBodyTag.select(c: String? = null, contents: SELECT.() -> Unit = empty_contents): Unit = contentTag(SELECT(this), c, contents)
public fun SelectTag.option(c: String? = null, contents: OPTION.() -> Unit = empty_contents): Unit = contentTag(OPTION(this), c, contents)
public fun SELECT.optgroup(c: String? = null, contents: OPTGROUP.() -> Unit = empty_contents): Unit = contentTag(OPTGROUP(this), c, contents)
public abstract class SelectTag(containingTag: HtmlBodyTag, name: String) : HtmlBodyTag(containingTag, name)
public open class SELECT(containingTag: HtmlBodyTag) : SelectTag(containingTag, "select") {
    public var name: String by Attributes.name
    public var size: Int by Attributes.size
    public var multiple: Boolean by Attributes.multiple
    public var disabled: Boolean by Attributes.disabled
    public var required: Boolean by Attributes.required
}

public open class OPTION(containingTag: HtmlBodyTag) : HtmlBodyTag(containingTag, "option") {
    public var value: String by Attributes.value
    public var label: String by Attributes.label
    public var disabled: Boolean by Attributes.disabled
    public var selected: Boolean by Attributes.selected
}

public open class OPTGROUP(containingTag: HtmlBodyTag) : SelectTag(containingTag, "optgroup")

public fun HtmlBodyTag.input(c: String? = null, contents: INPUT.() -> Unit = empty_contents): Unit = contentTag(INPUT(this), c, contents)
public open class INPUT(containingTag: HtmlBodyTag) : HtmlBodyTag(containingTag, "input", RenderStyle.expanded, ContentStyle.propagate) {
    public var alt: String by Attributes.alt
    public var autocomplete: Boolean by Attributes.autocomplete
    public var autofocus: Boolean by Attributes.autofocus
    public var checked: Boolean by Attributes.checked
    public var disabled: Boolean by Attributes.disabled
    public var height: Int by Attributes.height
    public var maxlength: Int by Attributes.maxlength
    public var multiple: Boolean by Attributes.multiple
    public var inputType: String by Attributes.inputType
    public var name: String by Attributes.name
    public var pattern: String by Attributes.pattern
    public var placeholder: String by Attributes.placeholder
    public var readonly: Boolean by Attributes.readonly
    public var required: Boolean by Attributes.required
    public var size: Int by Attributes.size
    public var src: Link by Attributes.src
    public var step: Int by Attributes.step
    public var value: String by Attributes.value
    public var width: Int by Attributes.width
}

public fun HtmlBodyTag.label(c: String? = null, contents: LABEL.() -> Unit = empty_contents): Unit = contentTag(LABEL(this), c, contents)
public open class LABEL(containingTag: HtmlBodyTag) : HtmlBodyTag(containingTag, "label") {
    public var forId: String by Attributes.forId
}

public fun HtmlBodyTag.textarea(c: String? = null, contents: TEXTAREA.() -> Unit = empty_contents): Unit = contentTag(TEXTAREA(this), c, contents)
public open class TEXTAREA(containingTag: HtmlBodyTag) : HtmlBodyTag(containingTag, "textarea") {
    public var autofocus: Boolean by Attributes.autofocus
    public var cols: Int by Attributes.cols
    public var disabled: Boolean by Attributes.disabled
    public var maxlength: Int by Attributes.maxlength
    public var name: String by Attributes.name
    public var placeholder: String by Attributes.placeholder
    public var readonly: Boolean by Attributes.readonly
    public var required: Boolean by Attributes.required
    public var rows: Int by Attributes.rows
    public var wrap: String by Attributes.wrap
}

public fun HtmlBodyTag.fieldset(c: String? = null, contents: FIELDSET.() -> Unit = empty_contents): Unit = contentTag(FIELDSET(this), c, contents)
public fun FIELDSET.legend(c: String? = null, contents: LEGEND.() -> Unit = empty_contents): Unit = contentTag(LEGEND(this), c, contents)
public open class FIELDSET(containingTag: HtmlBodyTag) : HtmlBodyTag(containingTag, "fieldset")
public open class LEGEND(containingTag: FIELDSET) : HtmlBodyTag(containingTag, "legend")

public fun HtmlBodyTag.figure(c: String? = null, contents: FIGURE.() -> Unit = empty_contents): Unit = contentTag(FIGURE(this), c, contents)
public fun FIGURE.figcaption(c: String? = null, contents: FIGCAPTION.() -> Unit = empty_contents): Unit = contentTag(FIGCAPTION(this), c, contents)
public open class FIGURE(containingTag: HtmlBodyTag) : HtmlBodyTag(containingTag, "figure")
public open class FIGCAPTION(containingTag: FIGURE) : HtmlBodyTag(containingTag, "figcaption")

public fun HtmlBodyTag.canvas(c: String? = null, contents: CANVAS.() -> Unit = empty_contents): Unit = contentTag(CANVAS(this), c, contents)
public open class CANVAS(containingTag: HtmlBodyTag) : HtmlBodyTag(containingTag, "canvas") {
    public var width: Int by Attributes.width
    public var height: Int by Attributes.height
}

public fun HtmlBodyTag.nav(c: String? = null, contents: NAV.() -> Unit = empty_contents): Unit = contentTag(NAV(this), c, contents)
public fun HtmlBodyTag.article(c: String? = null, contents: ARTICLE.() -> Unit = empty_contents): Unit = contentTag(ARTICLE(this), c, contents)
public fun HtmlBodyTag.aside(c: String? = null, contents: ASIDE.() -> Unit = empty_contents): Unit = contentTag(ASIDE(this), c, contents)
public fun HtmlBodyTag.section(c: String? = null, contents: SECTION.() -> Unit = empty_contents): Unit = contentTag(SECTION(this), c, contents)
public fun HtmlBodyTag.header(c: String? = null, contents: HEADER.() -> Unit = empty_contents): Unit = contentTag(HEADER(this), c, contents)
public fun HtmlBodyTag.footer(c: String? = null, contents: FOOTER.() -> Unit = empty_contents): Unit = contentTag(FOOTER(this), c, contents)
public fun HtmlBodyTag.details(c: String? = null, contents: DETAILS.() -> Unit = empty_contents): Unit = contentTag(DETAILS(this), c, contents)
public fun DETAILS.summary(c: String? = null, contents: SUMMARY.() -> Unit = empty_contents): Unit = contentTag(SUMMARY(this), c, contents)

public open class NAV(containingTag: HtmlBodyTag) : HtmlBodyTag(containingTag, "nav")
public open class ARTICLE(containingTag: HtmlBodyTag) : HtmlBodyTag(containingTag, "article")
public open class ASIDE(containingTag: HtmlBodyTag) : HtmlBodyTag(containingTag, "aside")
public open class SECTION(containingTag: HtmlBodyTag) : HtmlBodyTag(containingTag, "section")
public open class HEADER(containingTag: HtmlBodyTag) : HtmlBodyTag(containingTag, "header")
public open class FOOTER(containingTag: HtmlBodyTag) : HtmlBodyTag(containingTag, "footer")
public open class DETAILS(containingTag: HtmlBodyTag) : HtmlBodyTag(containingTag, "details")
public open class SUMMARY(containingTag: DETAILS) : HtmlBodyTag(containingTag, "summary")

public fun HtmlBodyTag.audio(c: String? = null, contents: AUDIO.() -> Unit = empty_contents): Unit = contentTag(AUDIO(this), c, contents)
public fun HtmlBodyTag.video(c: String? = null, contents: VIDEO.() -> Unit = empty_contents): Unit = contentTag(VIDEO(this), c, contents)
public fun MediaTag.track(c: String? = null, contents: TRACK.() -> Unit = empty_contents): Unit = contentTag(TRACK(this), c, contents)
public fun MediaTag.source(c: String? = null, contents: SOURCE.() -> Unit = empty_contents): Unit = contentTag(SOURCE(this), c, contents)
public abstract class MediaTag(containingTag: HtmlBodyTag, name: String) : HtmlBodyTag(containingTag, name)
public open class AUDIO(containingTag: HtmlBodyTag) : MediaTag(containingTag, "audio")
public open class VIDEO(containingTag: HtmlBodyTag) : MediaTag(containingTag, "video")
public open class TRACK(containingTag: MediaTag) : HtmlBodyTag(containingTag, "track")
public open class SOURCE(containingTag: MediaTag) : HtmlBodyTag(containingTag, "source")

public fun HtmlBodyTag.embed(c: String? = null, contents: EMBED.() -> Unit = empty_contents): Unit = contentTag(EMBED(this), c, contents)
public open class EMBED(containingTag: HtmlBodyTag) : HtmlBodyTag(containingTag, "embed")
