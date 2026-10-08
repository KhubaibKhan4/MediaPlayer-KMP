package io.github.khubaibkhan4.mediaplayer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.SwingPanel
import androidx.compose.ui.graphics.Color
import javafx.concurrent.Worker
import javafx.embed.swing.JFXPanel
import javafx.scene.Scene
import javafx.scene.web.WebEngine
import javafx.scene.web.WebView
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import netscape.javascript.JSObject
import javax.swing.SwingUtilities

@Composable
internal fun DesktopWebView(
    modifier: Modifier,
    url: String,
    autoPlay: Boolean,
    showControls: Boolean,
    isYouTube: Boolean = false,
    headers: Map<String, String> = emptyMap(),
    onPlayerEvent: (PlayerEvent) -> Unit = {},
) {
    val currentOnPlayerEvent by rememberUpdatedState(onPlayerEvent)
    var isLoading by remember { mutableStateOf(true) }
    val jfxPanel = remember { JFXPanel() }
    val webView = remember {
        FxMediaWebView(
            panel = jfxPanel,
            onEvent = { event -> SwingUtilities.invokeLater { currentOnPlayerEvent(event) } },
            onLoading = { loading -> SwingUtilities.invokeLater { isLoading = loading } }
        )
    }

    Box(modifier = modifier) {
        SwingPanel(
            factory = { jfxPanel },
            modifier = Modifier.fillMaxSize()
        )

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.7f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color.White)
            }
        }
    }

    LaunchedEffect(webView, url, headers, autoPlay, showControls) {
        isLoading = true
        val source = try {
            withContext(Dispatchers.IO) { resolveMediaSource(url, headers) }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            isLoading = false
            currentOnPlayerEvent(PlayerEvent.Error(e.message ?: "Failed to load $url"))
            return@LaunchedEffect
        }
        val setupScript = mediaSetupScript(autoPlay, showControls, isYouTube)
        runOnFxThread {
            // WebView doesn't build a media document for a bare file URL, so wrap plain files in a <video> page.
            if (isYouTube) webView.load(source, setupScript) else webView.loadHtml(videoPageHtml(source), setupScript)
        }
    }

    // Unload the page so audio stops when the player leaves the composition.
    DisposableEffect(webView) { onDispose { runOnFxThread { webView.dispose() } } }
}

/**
 * Called from page JavaScript through `window.kmpBridge`. WebView invokes it reflectively,
 * so it must stay reachable from Kotlin (WebView only keeps a weak reference).
 */
internal class MediaEventBridge(private val onEvent: (String, String?) -> Unit) {
    fun event(name: String, detail: String?) = onEvent(name, detail)
}

/** Owns one JavaFX [WebView]. Every method must be called on the JavaFX thread. */
private class FxMediaWebView(
    private val panel: JFXPanel,
    private val onEvent: (PlayerEvent) -> Unit,
    private val onLoading: (Boolean) -> Unit,
) {
    private var engine: WebEngine? = null
    private var setupScript = ""
    private var readySent = false
    private var active = true
    private val bridge = MediaEventBridge(::handleEvent)

    fun load(url: String, setupScript: String) {
        if (!active) return
        prepare(setupScript).load(url)
    }

    fun loadHtml(html: String, setupScript: String) {
        if (!active) return
        prepare(setupScript).loadContent(html)
    }

    private fun prepare(setupScript: String): WebEngine {
        this.setupScript = setupScript
        readySent = false
        return engine ?: createEngine().also { engine = it }
    }

    fun dispose() {
        active = false
        engine?.loadContent("")
    }

    private fun createEngine(): WebEngine {
        val webView = WebView()
        panel.scene = Scene(webView)
        return webView.engine.apply {
            userAgent =
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.3"
            isJavaScriptEnabled = true
            loadWorker.stateProperty().addListener { _, _, state ->
                if (!active) return@addListener
                when (state) {
                    Worker.State.RUNNING -> onLoading(true)
                    Worker.State.SUCCEEDED -> {
                        onLoading(false)
                        (executeScript("window") as JSObject).setMember("kmpBridge", bridge)
                        executeScript(setupScript)
                    }
                    Worker.State.FAILED -> {
                        onLoading(false)
                        onEvent(PlayerEvent.Error(loadWorker.exception?.message ?: "Failed to load $location"))
                    }
                    else -> Unit
                }
            }
        }
    }

    private fun handleEvent(name: String, detail: String?) {
        if (!active) return
        val event = when (name) {
            "ready" -> if (readySent) null else PlayerEvent.Ready.also { readySent = true }
            "playing" -> PlayerEvent.Playing
            "paused" -> PlayerEvent.Paused
            "buffering" -> PlayerEvent.Buffering
            "ended" -> PlayerEvent.Ended
            "error" -> PlayerEvent.Error(detail ?: "Media error")
            else -> null
        }
        event?.let(onEvent)
    }
}

private fun videoPageHtml(source: String): String {
    val src = source.replace("&", "&amp;").replace("\"", "&quot;")
    return """
        <html><head><style>
            html, body { margin: 0; width: 100%; height: 100%; overflow: hidden; background: black; }
            video { position: absolute; top: 0; left: 0; width: 100%; height: 100%; object-fit: contain; }
        </style></head>
        <body><video src="$src" playsinline></video></body></html>
    """.trimIndent()
}

private const val HIDE_YOUTUBE_UI_CSS = """
.ytp-gradient-top, .ytp-gradient-bottom, .ytp-chrome-top, .ytp-cards-button, .ytp-title-text,
.ytp-watch-later-button, .ytp-share-button, .ytp-credits-roll, .ytp-paid-content-overlay,
.ytp-show-cards-title, #owner, #info, .ytp-ce-element, .ytp-next-button { display: none !important; }
video { position: absolute !important; top: 0 !important; left: 0 !important;
        width: 100% !important; height: 100% !important; object-fit: contain !important; }
body, html, #player { margin: 0 !important; padding: 0 !important; width: 100% !important;
        height: 100% !important; overflow: hidden !important; background-color: black !important; }
"""

/** Waits for the page's `<video>` element, applies the player options and forwards its events to [MediaEventBridge]. */
private fun mediaSetupScript(autoPlay: Boolean, showControls: Boolean, isYouTube: Boolean): String {
    val style = if (isYouTube) {
        val css = HIDE_YOUTUBE_UI_CSS.replace("\n", " ")
        "var style = document.createElement('style'); style.innerHTML = '$css'; document.head.appendChild(style);"
    } else {
        ""
    }
    // YouTube draws its own controls; for plain files toggle the native ones.
    val controls = if (isYouTube) "" else "v.controls = $showControls;"
    return """
        (function () {
            $style
            function attach(tries) {
                var v = document.querySelector('video');
                if (!v) { if (tries < 60) setTimeout(function () { attach(tries + 1); }, 500); return; }
                if (v.__kmpAttached) return;
                v.__kmpAttached = true;
                var events = { canplay: 'ready', playing: 'playing', pause: 'paused', waiting: 'buffering', ended: 'ended' };
                Object.keys(events).forEach(function (name) {
                    v.addEventListener(name, function () { window.kmpBridge.event(events[name], null); });
                });
                v.addEventListener('error', function () {
                    window.kmpBridge.event('error', v.error ? 'Media error ' + v.error.code : null);
                });
                $controls
                if (v.readyState >= 3) window.kmpBridge.event('ready', null);
                if ($autoPlay) v.play();
            }
            attach(0);
        })();
    """.trimIndent()
}
