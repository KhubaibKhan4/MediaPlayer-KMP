package html_embeded_content.domain.factory

import html_embeded_content.domain.repository.HtmlContentViewer.HtmlContentViewer

expect class HtmlContentViewerFactory() {
    fun createHtmlContentViewer(): HtmlContentViewer
}