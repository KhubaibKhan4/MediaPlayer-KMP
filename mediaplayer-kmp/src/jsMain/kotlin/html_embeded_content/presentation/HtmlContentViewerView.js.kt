package html_embeded_content.presentation

import HtmlView
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import html_embeded_content.domain.factory.JsHtmlContentViewer
import html_embeded_content.domain.repository.HtmlContentViewer.HtmlContentViewer
import kotlinx.browser.document
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Iframe
import org.w3c.dom.HTMLElement

@Composable
actual fun HtmlContentViewerView(
    viewer: HtmlContentViewer,
    modifier: Modifier
) {
    val jsViewer = viewer as? JsHtmlContentViewer
        ?: error("Expected JsHtmlContentViewer")
    HtmlView(
        modifier = modifier,
        factory = {
            val iframe = document.createElement("iframe") as HTMLElement
            iframe.setAttribute("width", "100%")
            iframe.setAttribute("height", "100%")
            iframe.setAttribute("src", jsViewer.iframe.src)
            iframe.setAttribute("frameborder", "0")
            iframe.setAttribute(
                "allow",
                "accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture"
            )
            iframe.setAttribute("allowfullscreen", "true")
            iframe.setAttribute("referrerpolicy", "no-referrer-when-downgrade")
            iframe.asDynamic().onload = {

            }
            iframe
        },
        update = { element ->
            element.setAttribute("width", "100%")
            element.setAttribute("height", "100%")
        }
    )
}