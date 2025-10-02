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

    DisposableEffect(url) {
        onDispose {
            Platform.runLater {
                (jfxPanel.scene?.root as? WebView)?.engine?.load("")
            }
            jPanel.remove(jfxPanel)
        }
    }
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
                            var style = document.createElement('style');
                            style.innerHTML = `
                                /* Hide overlays, gradients, and unnecessary UI */
                                .ytp-gradient-top, 
                                .ytp-gradient-bottom, 
                                .ytp-chrome-top, 
                                .ytp-cards-button, 
                                .ytp-title-text, 
                                .ytp-watch-later-button, 
                                .ytp-share-button, 
                                .ytp-credits-roll, 
                                .ytp-paid-content-overlay, 
                                .ytp-show-cards-title, 
                                #owner, 
                                #info, 
                                .ytp-ce-element, 
                                .ytp-next-button {
                                    display: none !important;
                                }

                                /* Make video fill the entire WebView */
                                video {
                                    position: absolute !important;
                                    top: 0 !important;
                                    left: 0 !important;
                                    width: 100% !important;
                                    height: 100% !important;
                                    object-fit: cover !important;
                                }
                                
                                /* Prevent UI shifts */
                                body, html, #player {
                                    margin: 0 !important;
                                    padding: 0 !important;
                                    width: 100% !important;
                                    height: 100% !important;
                                    overflow: hidden !important;
                                    background-color: black !important;
                                }
                            `;
                            document.head.appendChild(style);
                        }, 500);
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