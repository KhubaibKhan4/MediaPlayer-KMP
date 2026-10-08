package io.github.khubaibkhan4.mediaplayer

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import coil3.compose.rememberAsyncImagePainter
import kotlinx.browser.window
import kotlinx.coroutines.delay
import org.w3c.dom.HTMLIFrameElement
import org.w3c.dom.HTMLMediaElement
import org.w3c.dom.MessageEvent
import org.w3c.dom.events.Event
import org.w3c.dom.url.URL
import org.w3c.files.Blob
import org.w3c.xhr.BLOB
import org.w3c.xhr.XMLHttpRequest
import org.w3c.xhr.XMLHttpRequestResponseType

@Composable
actual fun VideoPlayer(
    modifier: Modifier,
    url: String,
    autoPlay: Boolean,
    showControls: Boolean,
    headers: Map<String, String>,
    onPlayerEvent: (PlayerEvent) -> Unit,
) {
    if (isYouTubeUrl(url)) {
        val videoId = extractYouTubeVideoId(url)
        if (videoId == null) {
            LaunchedEffect(url) { onPlayerEvent(PlayerEvent.Error("Invalid YouTube URL: $url")) }
            return
        }
        HTMLVideoPlayer(videoId, modifier, autoPlay, showControls, extractYouTubeStartSeconds(url), onPlayerEvent)
    } else {
        HTMLMediaPlayer(
            tag = "video",
            modifier = modifier.defaultMinSize(minHeight = 200.dp),
            url = url,
            headers = headers,
            autoPlay = autoPlay,
            showControls = showControls,
            onPlayerEvent = onPlayerEvent
        )
    }
}

@Composable
actual fun MediaPlayer(
    modifier: Modifier,
    url: String,
    headers: Map<String, String>,
    startTime: Color,
    endTime: Color,
    autoPlay: Boolean,
    volumeIconColor: Color,
    playIconColor: Color,
    sliderTrackColor: Color,
    sliderIndicatorColor: Color,
    showControls: Boolean,
    onPlayerEvent: (PlayerEvent) -> Unit,
) {
    val isAudio = isAudioFile(url)
    HTMLMediaPlayer(
        tag = if (isAudio) "audio" else "video",
        modifier = if (isAudio) {
            modifier.fillMaxWidth().defaultMinSize(minHeight = 54.dp)
        } else {
            modifier.defaultMinSize(minHeight = 200.dp)
        },
        url = url,
        headers = headers,
        autoPlay = autoPlay,
        showControls = showControls,
        onPlayerEvent = onPlayerEvent
    )
}

/** A native `<video>` or `<audio>` element. With [headers] the file is fetched first and played from a blob URL. */
@Composable
private fun HTMLMediaPlayer(
    tag: String,
    modifier: Modifier,
    url: String,
    headers: Map<String, String>,
    autoPlay: Boolean,
    showControls: Boolean,
    onPlayerEvent: (PlayerEvent) -> Unit,
) {
    val currentOnPlayerEvent by rememberUpdatedState(onPlayerEvent)
    var source by remember(url, headers) { mutableStateOf(if (headers.isEmpty()) url else null) }

    if (headers.isNotEmpty()) {
        DisposableEffect(url, headers) {
            var blobUrl: String? = null
            var cancelled = false
            fetchBlobUrl(url, headers,
                onSuccess = { if (cancelled) URL.revokeObjectURL(it) else { blobUrl = it; source = it } },
                onError = { if (!cancelled) currentOnPlayerEvent(PlayerEvent.Error(it)) }
            )
            onDispose {
                cancelled = true
                blobUrl?.let { URL.revokeObjectURL(it) }
            }
        }
    }

    HtmlView(
        modifier = modifier,
        factory = {
            val media = createElement(tag) as HTMLMediaElement
            media.setAttribute("style", "background:black;object-fit:contain")
            media.setAttribute("playsinline", "true")
            var readySent = false
            val events = mapOf(
                "playing" to PlayerEvent.Playing,
                "pause" to PlayerEvent.Paused,
                "waiting" to PlayerEvent.Buffering,
                "ended" to PlayerEvent.Ended,
            )
            events.forEach { (name, event) -> media.addEventListener(name, { currentOnPlayerEvent(event) }) }
            media.addEventListener("loadstart", { readySent = false })
            media.addEventListener("canplay", {
                if (!readySent) {
                    readySent = true
                    currentOnPlayerEvent(PlayerEvent.Ready)
                }
            })
            media.addEventListener("error", {
                currentOnPlayerEvent(PlayerEvent.Error(media.error?.let { "Media error ${it.code}" } ?: "Media error"))
            })
            media
        },
        update = { media ->
            media.controls = showControls
            media.autoplay = autoPlay
            val src = source
            if (src != null && media.getAttribute("src") != src) media.setAttribute("src", src)
        }
    )
}

private fun fetchBlobUrl(url: String, headers: Map<String, String>, onSuccess: (String) -> Unit, onError: (String) -> Unit) {
    val xhr = XMLHttpRequest()
    xhr.open("GET", url, true)
    headers.forEach { (key, value) -> xhr.setRequestHeader(key, value) }
    xhr.responseType = XMLHttpRequestResponseType.BLOB
    xhr.onload = {
        if (xhr.status.toInt() in 200..299) {
            onSuccess(URL.createObjectURL(xhr.response as Blob))
        } else {
            onError("HTTP ${xhr.status} while loading $url")
        }
    }
    xhr.onerror = { onError("Network error while loading $url") }
    xhr.send()
}

private var nextYouTubePlayerId = 1

@Composable
internal fun HTMLVideoPlayer(
    videoId: String,
    modifier: Modifier = Modifier,
    autoPlay: Boolean = false,
    showControls: Boolean = true,
    startSeconds: Int = 0,
    onPlayerEvent: (PlayerEvent) -> Unit = {},
) {
    val currentOnPlayerEvent by rememberUpdatedState(onPlayerEvent)
    var isLoading by remember { mutableStateOf(true) }
    val playerId = remember { nextYouTubePlayerId++ }
    val iframeRef = remember { arrayOfNulls<HTMLIFrameElement>(1) }
    var playerReady by remember { mutableStateOf(false) }

    Box(
        modifier = modifier.defaultMinSize(minHeight = 200.dp),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            Image(
                painter = rememberAsyncImagePainter("https://img.youtube.com/vi/$videoId/hqdefault.jpg"),
                contentDescription = "Video Thumbnail",
                modifier = Modifier.matchParentSize()
            )
            CircularProgressIndicator()
        }

        HtmlView(
            modifier = Modifier.matchParentSize(),
            factory = {
                val iframe = createElement("iframe") as HTMLIFrameElement
                val params = listOf(
                    "autoplay=${if (autoPlay) 1 else 0}",
                    // Browsers only allow autoplay without a user gesture when muted.
                    "mute=${if (autoPlay) 1 else 0}",
                    "controls=${if (showControls) 1 else 0}",
                    "start=$startSeconds",
                    "playsinline=1",
                    "modestbranding=1",
                    "rel=0",
                    "enablejsapi=1",
                    "origin=${window.location.origin}",
                ).joinToString("&")
                iframe.src = "https://www.youtube.com/embed/$videoId?$params"
                iframe.width = "100%"
                iframe.height = "100%"
                iframe.setAttribute("frameborder", "0")
                iframe.setAttribute(
                    "allow",
                    "accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture"
                )
                iframe.setAttribute("allowfullscreen", "true")
                iframe.setAttribute("referrerpolicy", "strict-origin-when-cross-origin")
                iframe.addEventListener("load", { isLoading = false })
                iframeRef[0] = iframe
                iframe
            }
        )
    }

    DisposableEffect(playerId) {
        var lastState: PlayerEvent? = null
        val listener: (Event) -> Unit = listener@{ event ->
            val message = event as MessageEvent
            if (!message.origin.contains("youtube.com") && !message.origin.contains("youtube-nocookie.com")) return@listener
            val parsed = youTubeEventFromMessage(message.data, playerId) ?: return@listener
            if (parsed == "ready" && !playerReady) {
                playerReady = true
                sendYouTubeCommand(iframeRef[0], playerId, "addEventListener", "onStateChange")
                sendYouTubeCommand(iframeRef[0], playerId, "addEventListener", "onError")
            }
            // State updates arrive both as onStateChange and infoDelivery; drop repeats.
            val playerEvent = youTubeBridgeEvent(parsed) ?: return@listener
            if (playerEvent != lastState) {
                lastState = playerEvent
                currentOnPlayerEvent(playerEvent)
            }
        }
        window.addEventListener("message", listener)
        onDispose { window.removeEventListener("message", listener) }
    }

    // Ask the embedded player to start posting events to this window (the protocol the IFrame API uses).
    // Repeat until it answers, since the player inside the iframe initialises asynchronously.
    LaunchedEffect(playerId) {
        repeat(40) {
            if (playerReady) return@LaunchedEffect
            iframeRef[0]?.contentWindow?.postMessage(
                """{"event":"listening","id":$playerId,"channel":"widget"}""".toJsString(),
                "*"
            )
            delay(250)
        }
    }
}

private fun sendYouTubeCommand(iframe: HTMLIFrameElement?, playerId: Int, func: String, arg: String) {
    iframe?.contentWindow?.postMessage(
        """{"event":"command","func":"$func","args":["$arg"],"id":$playerId,"channel":"widget"}""".toJsString(),
        "*"
    )
}

/** Normalises a YouTube widget message for [playerId] to `ready`, `state:<n>` or `error:<n>`. */
private fun youTubeEventFromMessage(data: JsAny?, playerId: Int): String? = js(
    """
    (function (raw, id) {
        try {
            var d = typeof raw === 'string' ? JSON.parse(raw) : raw;
            if (!d || typeof d !== 'object' || d.id !== id) return null;
            if (d.event === 'onReady' || d.event === 'initialDelivery') return 'ready';
            if (d.event === 'onStateChange') return 'state:' + d.info;
            if (d.event === 'onError') return 'error:' + d.info;
            if (d.event === 'infoDelivery' && d.info && typeof d.info.playerState === 'number') return 'state:' + d.info.playerState;
        } catch (e) {}
        return null;
    })(data, playerId)
    """
)
