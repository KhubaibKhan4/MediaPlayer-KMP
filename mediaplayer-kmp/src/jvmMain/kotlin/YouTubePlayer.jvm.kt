import androidx.compose.foundation.layout.Box
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
actual fun VideoPlayer(
    modifier: Modifier,
    url: String,
    autoPlay: Boolean
) {

    when {
        url.contains("youtube.com") || url.contains("youtu.be") -> {
            val videoId = splitLinkForVideoId(url)
            DesktopWebView(modifier, "https://www.youtube.com/embed/$videoId", autoPlay)
        }

        isVideoFile(url) -> {
            url?.let {
                DesktopVideoPlayer(modifier, videoURL = it,autoPlay = autoPlay)
            }
        }
    }
}


@Composable
fun DesktopVideoPlayer(
    modifier: Modifier,
    videoURL: String,
    autoPlay: Boolean
) {
    Box(modifier = modifier) {
        var isLoading by remember { mutableStateOf(true) }
        DesktopWebView(modifier, videoURL,autoPlay)

        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }
    }
}

fun isVideoFile(url: String?): Boolean {
    return url?.matches(
        Regex(
            ".*\\.(mp4|mkv|webm|avi|mov|wmv|flv|m4v|3gp|mpeg)$",
            RegexOption.IGNORE_CASE
        )
    ) == true
}

fun splitLinkForVideoId(
    url: String?,
): String {
    return url?.substringAfter("v=").toString()
}

@Composable
actual fun MediaPlayer(
    modifier: Modifier,
    url: String,
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
            DesktopAudioPlayer(
                modifier,
                audioURL = url,
                startTime,
                endTime,
                volumeIconColor,
                playIconColor,
                sliderTrackColor,
                sliderIndicatorColor
            )
        }
        else ->{
            DesktopAudioPlayer(
                modifier,
                audioURL = url,
                startTime,
                endTime,
                volumeIconColor,
                playIconColor,
                sliderTrackColor,
                sliderIndicatorColor
            )
        }
    }
}
fun isAudioFile(url: String?): Boolean {
    return url?.matches(
        Regex(".*\\.(mp3|wav|aac|ogg|m4a|m3u|pls|m3u8)\$", RegexOption.IGNORE_CASE)
    ) == true || url?.matches(
        Regex(".*(radio|stream|icecast|shoutcast|audio|listen).*", RegexOption.IGNORE_CASE)
    ) == true
}