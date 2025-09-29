package html_embeded_content.domain.factory

import html_embeded_content.data.Callback
import html_embeded_content.data.EmbedOptions
import html_embeded_content.domain.repository.HtmlContentViewer.HtmlContentViewer
import kotlinx.browser.document
import org.w3c.dom.HTMLIFrameElement

actual class HtmlContentViewerFactory actual constructor() {
    actual fun createHtmlContentViewer(): HtmlContentViewer = JsHtmlContentViewer()
}

class JsHtmlContentViewer : HtmlContentViewer {
    val iframe: HTMLIFrameElement = (document.createElement("iframe") as HTMLIFrameElement).apply {
        style.border = "none"
        style.width = "100%"
        style.height = "100%"
        document.body?.appendChild(this)
    }
    private var pageLoadCallback: Callback = {}
    private var errorCallback: (Throwable) -> Unit = {}

    override fun loadUrl(url: String, options: EmbedOptions) {
        iframe.src = url
    }

    override fun refresh() {
        iframe.contentWindow?.location?.reload()
    }

    override fun injectCustomStyle(css: String) {
        iframe.onload = {
            val style = document.createElement("style")
            style.innerHTML = css
            iframe.contentDocument?.head?.appendChild(style)
        }
    }

    override fun setPageLoadListener(callback: Callback) {
        pageLoadCallback = callback
        iframe.onload = {
            pageLoadCallback()
            null
        }
    }

    override fun setErrorListener(callback: (Throwable) -> Unit) {
        errorCallback = callback
        iframe.onerror = { jsAny: JsAny?, s: String, i: Int, i1: Int, jsAny1: JsAny? ->
            errorCallback(Exception("Failed to load content"))
            null
        }
    }
    override fun evaluateJavaScript(script: String, callback: ((String?) -> Unit)?) {
        try {
            val result = jsEval(script)
            callback?.invoke(result?.toString())
        } catch (e: Throwable) {
            callback?.invoke(null)
        }
    }
}
@JsFun("function(script) { return eval(script); }")
external fun jsEval(script: String): Any?