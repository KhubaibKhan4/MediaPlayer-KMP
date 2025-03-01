package html_embeded_content.domain.factory

import html_embeded_content.data.EmbedOptions
import html_embeded_content.domain.repository.HtmlContentViewer.HtmlContentViewer

class HtmlEmbedFeature(viewer: HtmlContentViewer) {
    private val htmlViewer = viewer

    fun embedHtml(url: String, options: EmbedOptions = EmbedOptions()) {
        options.customCss?.let { htmlViewer.injectCustomStyle(it) }
        options.onPageLoaded?.let { htmlViewer.setPageLoadListener(it) }
        options.onError?.let { htmlViewer.setErrorListener(it) }
        htmlViewer.loadUrl(url, options)
    }

    fun refreshContent() = htmlViewer.refresh()
}