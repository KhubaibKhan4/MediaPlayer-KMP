import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.browser.document
import org.w3c.dom.HTMLElement
import org.w3c.dom.url.URL
import org.w3c.files.Blob
import org.w3c.xhr.BLOB
import org.w3c.xhr.XMLHttpRequest
import org.w3c.xhr.XMLHttpRequestResponseType

@Composable
actual fun VideoPlayer(
    modifier: Modifier,
    url: String,
    autoPlay: Boolean
) {
    when {
        url.contains("youtube.com") || url.contains("youtu.be") -> {
            val videoId = extractVideoId(url)
            HTMLVideoPlayer(videoId,modifier,autoPlay)
        }

        isVideoFile(url) -> {
            HTMLMP4Player(modifier, videoURL = url,autoPlay)
        }
    }
}

@Composable
fun HTMLMP4Player(
    modifier: Modifier,
    videoURL: String,
    autoPlay: Boolean
) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        HtmlView(
            modifier = Modifier.fillMaxWidth().height(300.dp),
            factory = {
                val video = createElement("video")
                video.setAttribute("width", "100%")
                video.setAttribute("height", "100%")
                video.setAttribute("src", videoURL)
                video.setAttribute("controls", "true")
                video.setAttribute("autoplay",autoPlay.toString())
                video.addEventListener("loadeddata", {
                    println("Loading Video: false")
                })
                video.addEventListener("loadstart", {
                    println("Loading Video: true")
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
    autoPlay: Boolean
) {
    val audioBlobUrl = remember(audioURL, headers) {
        fetchAudioBlobUrl(audioURL, headers)
    }

    HtmlView(
        modifier = modifier.fillMaxSize(),
        factory = {
            val audio = createElement("audio")
            audio.setAttribute("controls", "true")
            if (audioBlobUrl != null) {
                audio.setAttribute("src", audioBlobUrl)
            }
            audio.setAttribute("autoplay", autoPlay.toString())
            audio
        }
    )
}

/**
 * Fetches the audio file as a Blob URL with custom headers
 */
private fun fetchAudioBlobUrl(audioURL: String, headers: Map<String, String>): String? {
    var blobUrl: String? = null
    val xhr = XMLHttpRequest()

    xhr.open("GET", audioURL, false)
    headers.forEach { (key, value) ->
        xhr.setRequestHeader(key, value)
    }
    xhr.responseType = XMLHttpRequestResponseType.BLOB

    xhr.onload = {
        if (xhr.status in 200..299) {
            val blob = xhr.response as Blob
            blobUrl = URL.createObjectURL(blob)
        }
    }

    xhr.onerror = {
        console.error("Failed to load audio from $audioURL with headers: $headers")
    }

    xhr.send()

    return blobUrl
}


fun isAudioFile(url: String?): Boolean {
    return url?.matches(
        Regex(".*\\.(mp3|wav|aac|ogg|m4a|m3u|pls|m3u8)\$", RegexOption.IGNORE_CASE)
    ) == true || url?.matches(
        Regex(".*(radio|stream|icecast|shoutcast|audio|listen).*", RegexOption.IGNORE_CASE)
    ) == true
}

private fun extractVideoId(url: String): String {
    val videoIdRegex =
        Regex("""(?:youtube\.com\/(?:[^\/]+\/.+\/|(?:v|e(?:mbed)?)\/|.*[?&]v=)|youtu\.be\/)([^"&?\/\s]{11})""")
    val matchResult = videoIdRegex.find(url)
    return matchResult?.groupValues?.get(1) ?: "default_video_id"
}

fun isVideoFile(url: String?): Boolean {
    return url?.matches(
        Regex(".*\\.(mp4|mkv|webm|avi|mov|wmv|flv|m4v|3gp|mpeg|m3u8|ts|dash)\$", RegexOption.IGNORE_CASE)
    ) == true || url?.matches(
        Regex(".*(stream|video|live|media).*", RegexOption.IGNORE_CASE)
    ) == true
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
    sliderIndicatorColor: Color
) {
    when {
        isAudioFile(url) -> {
            HTMLAudioPlayer(modifier, audioURL = url, autoPlay = autoPlay, headers = headers)
        }
    }
}

@Composable
fun HTMLVideoPlayer(
    videoId: String,
    modifier: Modifier,
    autoPlay: Boolean
) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        HtmlView(
            modifier = modifier.fillMaxWidth(),
            factory = {
                val iframe = createElement("iframe") as HTMLElement
                iframe.setAttribute("width", "100%")
                iframe.setAttribute("height", "100%")
                iframe.setAttribute(
                    "src",
                    "https://www.youtube.com/embed/$videoId?autoplay=1&mute=1&showinfo=0"
                )
                iframe.setAttribute("frameborder", "0")
                iframe.setAttribute(
                    "allow",
                    "accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture"
                )
                iframe.setAttribute("autoplay", autoPlay.toString())
                iframe.setAttribute("allowfullscreen", "true")
                iframe.setAttribute("referrerpolicy", "no-referrer-when-downgrade")

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
                    }, 1000);
                """.trimIndent()
                injectJavaScript(iframe, script)
                iframe
            },
            update = {
                it.setAttribute("width", "100%")
                it.setAttribute("height", "100%")
            }
        )
    }
}
private fun injectJavaScript(iframe: HTMLElement, script: String) {
    val scriptElement = document.createElement("script") as HTMLElement
    scriptElement.textContent = script
    iframe.appendChild(scriptElement)
}