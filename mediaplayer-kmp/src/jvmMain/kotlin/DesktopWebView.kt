import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.SwingPanel
import javafx.application.Platform
import javafx.concurrent.Worker
import javafx.embed.swing.JFXPanel
import javafx.scene.Scene
import javafx.scene.web.WebView
import javax.swing.JPanel

@Composable
fun DesktopWebView(
    modifier: Modifier,
    url: String,
    onLoadingChange: (Boolean) -> Unit
) {
    val jPanel: JPanel = remember { JPanel() }
    val jfxPanel = JFXPanel()

    SwingPanel(
        factory = {
            jfxPanel.apply { buildWebView(url, onLoadingChange) }
            jPanel.add(jfxPanel)
        },
        modifier = modifier,
    )

    DisposableEffect(url) {
        onDispose { jPanel.remove(jfxPanel) }
    }
}

private fun JFXPanel.buildWebView(url: String, onLoadingChange: (Boolean) -> Unit) {
    Platform.runLater {
        val webView = WebView()
        val webEngine = webView.engine

        onLoadingChange(true)

        webEngine.loadWorker.stateProperty().addListener { _, _, newState ->
            if (newState == Worker.State.SUCCEEDED) {
                onLoadingChange(false)
            }
        }

        webEngine.isJavaScriptEnabled = true
        webEngine.load(url)
        webEngine.executeScript("""
            document.addEventListener('fullscreenchange', function() {
                if (!document.fullscreenElement) {
                    // Handle exit full screen
                    console.log('Exited full screen');
                }
            });

            document.addEventListener('webkitfullscreenchange', function() {
                if (!document.webkitFullscreenElement) {
                    // Handle exit full screen
                    console.log('Exited full screen');
                }
            });
        """.trimIndent())

        val scene = Scene(webView)
        setScene(scene)
    }
}