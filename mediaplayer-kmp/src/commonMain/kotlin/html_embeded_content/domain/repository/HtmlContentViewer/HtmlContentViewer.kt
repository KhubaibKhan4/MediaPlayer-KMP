package html_embeded_content.domain.repository.HtmlContentViewer

import html_embeded_content.data.Callback
import html_embeded_content.data.EmbedOptions

interface HtmlContentViewer {
    fun loadUrl(url: String, options: EmbedOptions = EmbedOptions())
    fun refresh()
    fun injectCustomStyle(css: String)
    fun setPageLoadListener(callback: Callback)
    fun setErrorListener(callback: (Throwable) -> Unit)
}