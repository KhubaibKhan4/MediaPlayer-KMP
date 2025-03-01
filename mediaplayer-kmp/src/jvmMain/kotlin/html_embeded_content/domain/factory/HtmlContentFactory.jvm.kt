package html_embeded_content.domain.factory

import html_embeded_content.data.Callback
import html_embeded_content.data.EmbedOptions
import html_embeded_content.domain.repository.HtmlContentViewer.HtmlContentViewer
import html_embeded_content.presentation.JvmHtmlContentViewerDemo
import javafx.application.Platform
import javafx.embed.swing.JFXPanel
import javafx.scene.Scene
import javafx.scene.web.WebEngine
import javafx.scene.web.WebView

actual class HtmlContentViewerFactory actual constructor() {
    actual fun createHtmlContentViewer(): HtmlContentViewer = JvmHtmlContentViewerDemo()
}
class JvmHtmlContentViewer : HtmlContentViewer {
    private val jfxPanel = JFXPanel()
    private lateinit var webView: WebView
    private lateinit var engine: WebEngine
    private var pageLoadCallback: Callback = {}
    private var errorCallback: ((Throwable) -> Unit)? = null
    private var errorListenerAdded = false

    init {
        // Initialize JavaFX components on the JavaFX Application Thread.
        Platform.runLater {
            webView = WebView()
            engine = webView.engine

            // Setup a status listener to trigger page load callback.
            engine.loadWorker.stateProperty().addListener { _, _, newState ->
                if (newState.toString() == "SUCCEEDED") {
                    pageLoadCallback()
                }
            }

            // If an error callback was already set, add the listener now.
            if (!errorListenerAdded && errorCallback != null) {
                engine.loadWorker.exceptionProperty().addListener { _, _, exception ->
                    exception?.let { errorCallback?.invoke(it) }
                }
                errorListenerAdded = true
            }

            jfxPanel.scene = Scene(webView)
        }
    }

    override fun loadUrl(url: String, options: EmbedOptions) {
        Platform.runLater {
            // If engine is not yet initialized, this block will run once it is.
            engine.load(url)
        }
    }

    override fun refresh() {
        Platform.runLater {
            engine.reload()
        }
    }

    override fun injectCustomStyle(css: String) {
        val js = "var style = document.createElement('style'); style.innerHTML = '$css'; document.head.appendChild(style);"
        Platform.runLater {
            engine.executeScript(js)
        }
    }

    override fun setPageLoadListener(callback: Callback) {
        pageLoadCallback = callback
    }

    override fun setErrorListener(callback: (Throwable) -> Unit) {
        errorCallback = callback
        Platform.runLater {
            if (::engine.isInitialized && !errorListenerAdded) {
                engine.loadWorker.exceptionProperty().addListener { _, _, exception ->
                    exception?.let { errorCallback?.invoke(it) }
                }
                errorListenerAdded = true
            }
        }
    }
}