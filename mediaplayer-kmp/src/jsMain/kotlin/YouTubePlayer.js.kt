import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import io.kamel.core.Resource
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource
import kotlinx.browser.document

@Composable
actual fun VideoPlayer(
    modifier: Modifier,
    url: String?,
    thumbnail: String?,
    onPlayClick: () -> Unit,
) {
    var isPlaying by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }

    if (isPlaying) {
        when {
            url?.contains("youtube.com") == true || url?.contains("youtu.be") == true -> {
                val videoId = extractVideoId(url.toString())
                HTMLVideoPlayer(modifier, videoId) {
                    isLoading = it
                }
            }

            isVideoFile(url) -> {
                url?.let {
                    HTMLMP4Player(modifier, videoURL = it) {
                        isLoading = it
                    }
                }
            }
        }
    } else {
        Box(modifier = modifier.fillMaxWidth()) {
            val image: Resource<Painter> = asyncPainterResource(thumbnail.toString())
            KamelImage(
                resource = image,
                contentDescription = "Thumbnail Image",
                contentScale = ContentScale.Crop,
                onFailure = {
                    isLoading = false
                },
                onLoading = {
                    isLoading = true
                },
                modifier =modifier,
            )
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(45.dp)
                        .align(Alignment.Center)
                        .clickable {
                            onPlayClick()
                            isPlaying = !isPlaying
                        }
                )
            }
        }
    }
}

@Composable
fun HTMLMP4Player(
    modifier: Modifier,
    videoURL: String,
    onLoadingChange: (Boolean) -> Unit,
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
                    onLoadingChange(false)
                })
                video.addEventListener("loadstart", {
                    onLoadingChange(true)
                })
                video
            }
        )
    }
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
            modifier = Modifier.fillMaxWidth().height(300.dp),
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
                    "accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture"
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
