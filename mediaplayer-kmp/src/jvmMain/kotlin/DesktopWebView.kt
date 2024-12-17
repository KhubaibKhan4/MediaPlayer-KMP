import javafx.application.Platform
import javafx.concurrent.Worker
import javafx.embed.swing.JFXPanel
import javafx.scene.Scene
import javafx.scene.web.WebView

fun initJavaFX() {
    System.setProperty("prism.order", "sw")
    System.setProperty("prism.verbose", "true")
}

private fun JFXPanel.buildWebView(url: String, autoPlay: Boolean) {
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
            if (newState == Worker.State.SUCCEEDED) {
                val script = """
                    setTimeout(function() {
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
                    }, 1000); // Adjust the timeout value as needed
                """.trimIndent()
                webEngine.executeScript(script)

                if (autoPlay) {
                    val autoPlayScript = """
                        setTimeout(function() {
                            var video = document.querySelector('video');
                            if (video) {
                                video.play();
                            }
                        }, 1000); // Adjust the timeout value if necessary
                    """.trimIndent()

                    webEngine.executeScript(autoPlayScript)
                }
            }
        }
    }
}