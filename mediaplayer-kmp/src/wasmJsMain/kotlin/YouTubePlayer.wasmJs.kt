import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import coil3.compose.rememberAsyncImagePainter
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.suspendCancellableCoroutine
import org.w3c.dom.HTMLAudioElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLIFrameElement
import org.w3c.dom.HTMLScriptElement
import org.w3c.dom.HTMLVideoElement
import org.w3c.dom.events.Event
import org.w3c.dom.url.URL
import org.w3c.files.Blob
import org.w3c.xhr.BLOB
import org.w3c.xhr.XMLHttpRequest
import org.w3c.xhr.XMLHttpRequestResponseType
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@Composable
actual fun VideoPlayer(
    modifier: Modifier,
    url: String,
    autoPlay: Boolean,
    showControls: Boolean
) {
    when {
        url.contains("youtube.com") || url.contains("youtu.be") -> {
            val videoId = extractVideoId(url)
            HTMLVideoPlayer(videoId, modifier, autoPlay)
        }
        isVideoFile(url) -> {
            HTMLMP4Player(modifier, videoURL = url, autoPlay, showControls)
        }
    }
}

@Composable
fun HTMLMP4Player(
    modifier: Modifier,
    videoURL: String,
    autoPlay: Boolean,
    showControls: Boolean
) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        HtmlView(
            modifier = Modifier.fillMaxWidth().height(300.dp),
            factory = {
                val video = document.createElement("video") as HTMLVideoElement
                video.apply {
                    width = 100
                    height = 100
                    src = videoURL
                    controls = showControls
                    autoplay = autoPlay
                }
                video.addEventListener("loadeddata", {
                    println("Video Loaded: $videoURL")
                })
                video.addEventListener("loadstart", {
                    println("Loading Video: $videoURL")
                })
                video
            }
        )
    }
}

@Composable
fun HTMLAudioPlayer(
    modifier: Modifier,
    audioURL: String,
    headers: Map<String, String>,
    autoPlay: Boolean,
    showControls: Boolean
) {
    val audioBlobUrl by remember(audioURL, headers) {
        mutableStateOf("")
    }
    LaunchedEffect(Unit){
        fetchAudioBlobUrl(audioURL, headers)
    }

    HtmlView(
        modifier = modifier.fillMaxSize(),
        factory = {
            val audio = document.createElement("audio") as HTMLAudioElement
            audio.apply {
                controls = showControls
                autoplay = autoPlay
                src = audioBlobUrl ?: audioURL
            }
            audio
        }
    )
}

/**
 * Fetches the audio file as a Blob URL with custom headers asynchronously.
 */
private suspend fun fetchAudioBlobUrl(audioURL: String, headers: Map<String, String>): String? {
    return suspendCancellableCoroutine { continuation ->
        val xhr = XMLHttpRequest()
        xhr.open("GET", audioURL, true)
        headers.forEach { (key, value) -> xhr.setRequestHeader(key, value) }
        xhr.responseType = XMLHttpRequestResponseType.BLOB

        xhr.onload = {
            if (xhr.status in 200..299) {
                val blob = xhr.response as Blob
                continuation.resume(URL.createObjectURL(blob))
            } else {
                continuation.resumeWithException(Exception("Failed to load audio: $audioURL"))
            }
        }

        xhr.onerror = {
            continuation.resumeWithException(Exception("Network error fetching audio: $audioURL"))
        }

        xhr.send()
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
    showControls: Boolean
) {
    when {
        isAudioFile(url) -> {
            HTMLAudioPlayer(modifier, audioURL = url, autoPlay = autoPlay, headers = headers, showControls = showControls)
        }
    }
}

@Composable
fun HTMLVideoPlayer(
    videoId: String,
    modifier: Modifier = Modifier,
    autoPlay: Boolean = false
) {
    var isLoading by remember { mutableStateOf(true) }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
                Image(
                    painter = rememberAsyncImagePainter("https://img.youtube.com/vi/$videoId/hqdefault.jpg"),
                    contentDescription = "Video Thumbnail",
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        HtmlView(
            modifier = Modifier.fillMaxWidth().height(200.dp),
            factory = {
                val iframe = document.createElement("iframe") as HTMLIFrameElement
                iframe.apply {
                    width = "100%"
                    height = "100%"
                    src = "https://www.youtube.com/embed/$videoId?autoplay=${if (autoPlay) 1 else 0}&mute=1&modestbranding=1&rel=0&showinfo=0"
                    setAttribute("frameborder", "0")
                    setAttribute(
                        "allow",
                        "accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture"
                    )
                    setAttribute("allowfullscreen", "true")
                    setAttribute("referrerpolicy", "no-referrer-when-downgrade")
                }

                injectJavaScript(iframe, """
                    let checkIframeLoaded = setInterval(function() {
                        let player = document.querySelector("iframe");
                        if (player && player.contentWindow && player.contentWindow.document.readyState === "complete") {
                            clearInterval(checkIframeLoaded);
                            
                            // Hide loading indicator (Communicate with Kotlin via JS bridge)
                            window.dispatchEvent(new Event("iframeReady"));

                            // ✅ Hide YouTube UI elements
                            setTimeout(function() {
                                document.querySelector('.ytp-gradient-top')?.style.display = 'none';
                                document.querySelector('.ytp-gradient-bottom')?.style.display = 'none';
                                document.querySelector('.ytp-chrome-top')?.style.display = 'none';
                                document.querySelector('.ytp-watch-later-button')?.style.display = 'none';
                                document.querySelector('.ytp-share-button')?.style.display = 'none';
                            }, 1000);
                        }
                    }, 500);
                """.trimIndent())

                iframe
            },
            update = {
                it.setAttribute("width", "100%")
                it.setAttribute("height", "100%")
            }
        )
    }
    var listener : (Event) -> Unit = {}

    LaunchedEffect(Unit) {
         listener = {
            isLoading = false
        }
        window.addEventListener("iframeReady", listener)

    }
    DisposableEffect(Unit){
        onDispose {
            window.removeEventListener("iframeReady", listener)
        }
    }
}

/**
 * Inject JavaScript into an iframe to customize YouTube UI.
 */
private fun injectJavaScript(iframe: HTMLIFrameElement, script: String) {
    val scriptElement = document.createElement("script") as HTMLScriptElement
    scriptElement.textContent = script
    iframe.appendChild(scriptElement)
}
fun isVideoFile(url: String?): Boolean {
    return url?.matches(
        Regex(".*\\.(mp4|mkv|webm|avi|mov|wmv|flv|m4v|3gp|mpeg|m3u8|ts|dash)\$", RegexOption.IGNORE_CASE)
    ) == true || url?.contains("video", ignoreCase = true) == true
}

fun isAudioFile(url: String?): Boolean {
    return url?.matches(
        Regex(".*\\.(mp3|wav|aac|ogg|m4a|m3u8)\$", RegexOption.IGNORE_CASE)
    ) == true || url?.contains("audio", ignoreCase = true) == true
}

private fun extractVideoId(url: String): String {
    val videoIdRegex =
        Regex("""(?:youtube\.com\/(?:[^\/]+\/.+\/|(?:v|e(?:mbed)?)\/|.*[?&]v=)|youtu\.be\/)([^"&?\/\s]{11})""")
    return videoIdRegex.find(url)?.groupValues?.get(1) ?: "default_video_id"
}