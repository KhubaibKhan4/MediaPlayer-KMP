import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
actual fun VideoPlayer(
    modifier: Modifier,
    url: String,
) {
    when {
        url.contains("youtube.com") || url.contains("youtu.be") -> {
            val videoId = extractVideoId(url)
            HTMLVideoPlayer(modifier, videoId) {

            }
        }

        isVideoFile(url) -> {
            HTMLMP4Player(modifier, videoURL = url)
        }
    }
}

@Composable
fun HTMLMP4Player(
    modifier: Modifier,
    videoURL: String,
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
) {
    HtmlView(
        modifier = modifier.fillMaxSize(),
        factory = {
            val audio = createElement("audio")
            audio.setAttribute("controls", "true")
            audio.setAttribute("src", audioURL)
            audio
        }
    )
}

fun isAudioFile(url: String?): Boolean {
    return url?.matches(Regex(".*\\.(mp3|wav|aac|ogg|m4a)\$", RegexOption.IGNORE_CASE)) == true
}

@Composable
fun HTMLVideoPlayer(
    modifier: Modifier,
    videoId: String,
    onLoadingChange: (Boolean) -> Unit,
) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        HtmlView(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp),
            factory = {
                val iframe = createElement("iframe")
                iframe.setAttribute("width", "100%")
                iframe.setAttribute("height", "100%")
                iframe.setAttribute(
                    "src",
                    "https://www.youtube.com/embed/$videoId?autoplay=1&mute=1&showinfo=0"
                )
                iframe.setAttribute("frameborder", "0")
                iframe.setAttribute(
                    "allow",
                    "accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; fullscreen" // Allow fullscreen
                )
                iframe.setAttribute("allowfullscreen", "true")
                iframe.setAttribute("referrerpolicy", "no-referrer-when-downgrade")
                iframe.addEventListener("load", {
                    onLoadingChange(false)
                })
                iframe
            }
        )
    }
}

private fun extractVideoId(url: String): String {
    val videoIdRegex =
        Regex("""(?:youtube\.com\/(?:[^\/]+\/.+\/|(?:v|e(?:mbed)?)\/|.*[?&]v=)|youtu\.be\/)([^"&?\/\s]{11})""")
    val matchResult = videoIdRegex.find(url)
    return matchResult?.groupValues?.get(1) ?: "default_video_id"
}

fun isVideoFile(url: String?): Boolean {
    return url?.matches(
        Regex(
            ".*\\.(mp4|mkv|webm|avi|mov|wmv|flv|m4v|3gp|mpeg)\$",
            RegexOption.IGNORE_CASE
        )
    ) == true
}

@Composable
actual fun MediaPlayer(
    modifier: Modifier, url: String,
    startTime: Color,
    endTime: Color,
    volumeIconColor: Color,
    playIconColor: Color,
    sliderTrackColor: Color,
    sliderIndicatorColor: Color
) {
    when {
        isAudioFile(url) -> {
            HTMLAudioPlayer(modifier, audioURL = url)
        }
    }
}