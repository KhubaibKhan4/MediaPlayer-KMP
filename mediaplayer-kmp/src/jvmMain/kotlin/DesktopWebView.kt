import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.SwingPanel
import androidx.compose.ui.graphics.Color
import javafx.application.Platform
import javafx.concurrent.Worker
import javafx.embed.swing.JFXPanel
import javafx.scene.Scene
import javafx.scene.web.WebView
import javax.swing.JPanel

fun initJavaFX() {
    System.setProperty("prism.order", "sw")
    System.setProperty("prism.verbose", "true")
}
@Composable
fun DesktopWebView(
    modifier: Modifier,
    url: String,
    autoPlay: Boolean,
    showControls: Boolean
) {
    val jPanel: JPanel = remember { JPanel() }
    val jfxPanel = JFXPanel()
    val isLoading = remember { mutableStateOf(true) }

    Box(modifier = modifier) {
        SwingPanel(
            factory = {
                jfxPanel.apply { buildWebView(url, autoPlay, showControls, isLoading) }
                jPanel.add(jfxPanel)
            },
            modifier = modifier.fillMaxSize()
        )

        if (isLoading.value) {
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.7f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color.White)
            }
        }
    }

    DisposableEffect(url) { onDispose { jPanel.remove(jfxPanel) } }
}

private fun JFXPanel.buildWebView(
    url: String,
    autoPlay: Boolean,
    showControls: Boolean,
    isLoading: MutableState<Boolean>
) {
    initJavaFX()
    Platform.runLater {
        val webView = WebView()
        val webEngine = webView.engine

        webEngine.userAgent =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.3"

        webEngine.isJavaScriptEnabled = true
        webEngine.load(url)

        val scene = Scene(webView)
        setScene(scene)

        webEngine.loadWorker.stateProperty().addListener { _, _, newState ->
            when (newState) {
                Worker.State.SUCCEEDED -> {
                    isLoading.value = false


                    val hideYouTubeUI = """
                        setTimeout(function() {
                            // Hide YouTube overlays
                            var overlaySelectors = [
                                '.ytp-gradient-top', 
                                '.ytp-gradient-bottom'
                            ];
                            overlaySelectors.forEach(function(selector) {
                                var element = document.querySelector(selector);
                                if (element !== null) {
                                    element.style.display = 'none';
                                }
                            });

                            // Hide YouTube logo, title, and eye icon
                            var brandingSelectors = [
                                '.ytp-chrome-top', // Top bar (title, logo)
                                '.ytp-cards-button', // "i" button (eye icon)
                                '.ytp-title-text', // Video title
                                '.ytp-watch-later-button', // "Watch Later" button
                                '.ytp-share-button', // "Share" button
                                '.ytp-credits-roll', // Rolling credits
                                '.ytp-paid-content-overlay', // Sponsored content
                                '.ytp-show-cards-title', // Suggested videos title
                                '#owner', // Channel name section
                                '#info', // Views and upload date
                                '.ytp-ce-element', // End screen recommendations
                                '.ytp-next-button' // Next video button
                            ];
                            brandingSelectors.forEach(function(selector) {
                                var element = document.querySelector(selector);
                                if (element !== null) {
                                    element.style.display = 'none';
                                }
                            });

                            // Hide video info panel on pause
                            var hideVideoInfo = function() {
                                var videoInfoPanel = document.querySelector('.ytp-title');
                                if (videoInfoPanel !== null) {
                                    videoInfoPanel.style.display = 'none';
                                }
                            };
                            hideVideoInfo();
                            document.addEventListener("pause", hideVideoInfo, true);
                        }, 1000);
                    """.trimIndent()

                    webEngine.executeScript(hideYouTubeUI)

                    if (autoPlay) {
                        val autoPlayScript = """
                            setTimeout(function() {
                                var video = document.querySelector('video');
                                if (video) {
                                    video.play();
                                }
                            }, 1000);
                        """.trimIndent()
                        webEngine.executeScript(autoPlayScript)
                    }

                    val toggleControlsScript = """
                        setTimeout(function() {
                            var video = document.querySelector('video');
                            if (video) {
                                video.controls = $showControls;
                            }
                        }, 1000);
                    """.trimIndent()
                    webEngine.executeScript(toggleControlsScript)
                }
                Worker.State.RUNNING -> {
                    isLoading.value = true
                }
                Worker.State.FAILED -> {
                    isLoading.value = false
                }
                else -> {}
            }
        }
    }
}
