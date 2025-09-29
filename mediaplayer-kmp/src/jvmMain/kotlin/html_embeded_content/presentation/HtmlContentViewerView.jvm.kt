package html_embeded_content.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.SwingPanel
import html_embeded_content.data.Callback
import html_embeded_content.data.EmbedOptions
import html_embeded_content.domain.factory.JvmHtmlContentViewer
import html_embeded_content.domain.repository.HtmlContentViewer.HtmlContentViewer
import javafx.application.Platform
import javafx.embed.swing.JFXPanel
import javafx.scene.Scene
import javafx.scene.web.WebEngine
import javafx.scene.web.WebView
import java.awt.BorderLayout
import javax.swing.JPanel

@Composable
actual fun HtmlContentViewerView(
    viewer: HtmlContentViewer,
    modifier: Modifier
) {
    val jvmViewer = viewer as? JvmHtmlContentViewerDemo
        ?: error("Expected JvmHtmlContentViewer")
    SwingPanel(
        modifier = modifier,
        factory = { jvmViewer.swingPanel }
    )
}

class JvmHtmlContentViewerDemo : HtmlContentViewer {
    val swingPanel: JPanel = JPanel(BorderLayout())
    private val jfxPanel: JFXPanel = JFXPanel()

    // These properties will be initialized on the JavaFX Application Thread.
    private lateinit var webView: WebView
    private lateinit var engine: WebEngine

    private var pageLoadCallback: Callback = {}
    private var errorCallback: (Throwable) -> Unit = {}

    init {
        swingPanel.add(jfxPanel, BorderLayout.CENTER)

        // Initialize the JavaFX components.
        Platform.runLater {
            webView = WebView()
            engine = webView.engine

            engine.loadWorker.stateProperty().addListener { _, _, newState ->
                if (newState.toString() == "SUCCEEDED") {
                    pageLoadCallback()
                }
            }


            engine.loadWorker.exceptionProperty().addListener { _, _, exception ->
                exception?.let { errorCallback(it) }
            }

            jfxPanel.scene = Scene(webView)
        }
    }

    override fun loadUrl(url: String, options: EmbedOptions) {
        Platform.runLater {
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
    }

    override fun evaluateJavaScript(script: String, callback: ((String?) -> Unit)?) {
        Platform.runLater {
            try {
                val result = engine.executeScript(script)
                callback?.invoke(result?.toString())
            } catch (e: Exception) {
                callback?.invoke(null)
            }
        }
    }
}