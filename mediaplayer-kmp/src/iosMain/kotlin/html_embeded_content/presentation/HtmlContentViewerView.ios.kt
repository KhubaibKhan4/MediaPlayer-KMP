package html_embeded_content.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitView
import html_embeded_content.domain.factory.IOSHtmlContentViewer
import html_embeded_content.domain.repository.HtmlContentViewer.HtmlContentViewer
import platform.UIKit.UIView

@Composable
actual fun HtmlContentViewerView(
    viewer: HtmlContentViewer,
    modifier: Modifier
) {
    val iosViewer = viewer as? IOSHtmlContentViewer
        ?: error("Expected IOSHtmlContentViewer")
    UIKitView(
        modifier = modifier,
        factory = { iosViewer.webView as UIView }
    )
}