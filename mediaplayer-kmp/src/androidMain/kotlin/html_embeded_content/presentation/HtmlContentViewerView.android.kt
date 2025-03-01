package html_embeded_content.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import html_embeded_content.domain.factory.AndroidHtmlContentViewer
import html_embeded_content.domain.repository.HtmlContentViewer.HtmlContentViewer

@Composable
actual fun HtmlContentViewerView(
    viewer: HtmlContentViewer,
    modifier: Modifier
) {
    val androidViewer = viewer as? AndroidHtmlContentViewer
        ?: error("Expected AndroidHtmlContentViewer")

    AndroidView(
        factory = { androidViewer.webView },
        modifier = modifier
    )
}