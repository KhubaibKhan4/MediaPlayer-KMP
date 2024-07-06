import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp

@Composable
actual fun VideoPlayer(
    modifier: Modifier,
    url: String?,
    thumbnail: String?,
    onPlayClick: () -> Unit
) {
    var isPlaying by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }

    if (isPlaying) {
        when {
            url?.contains("youtube.com") == true || url?.contains("youtu.be") == true -> {
                val videoId = splitLinkForVideoId(url.toString())
                DesktopWebView(modifier, "https://www.youtube.com/embed/$videoId") {
                    isLoading = it
                }
            }
            isVideoFile(url) -> {
                url?.let {
                    DesktopVideoPlayer(modifier, videoURL = it) {
                        isLoading = it
                    }
                }
            }
        }
    } else {
        Box(modifier = modifier.fillMaxWidth()) {
            coil3.compose.AsyncImage(
                modifier = modifier.fillMaxWidth(),
                model = thumbnail,
                contentDescription = "Thumbnail Image",
                contentScale = ContentScale.Crop,

                onError = {
                    isLoading = false
                },
                onLoading = {
                    isLoading = true
                },
                onSuccess = {
                    isLoading = false
                },

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
fun DesktopVideoPlayer(
    modifier: Modifier,
    videoURL: String,
    onLoadingChange: (Boolean) -> Unit
) {
    Box(modifier = modifier) {
        var isLoading by remember { mutableStateOf(true) }

        DesktopWebView(modifier, videoURL) {
            isLoading = it
            onLoadingChange(it)
        }

        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }
    }
}

fun isVideoFile(url: String?): Boolean {
    return url?.matches(Regex(".*\\.(mp4|mkv|webm|avi|mov|wmv|flv|m4v|3gp|mpeg)$", RegexOption.IGNORE_CASE)) == true
}

fun splitLinkForVideoId(
    url: String?,
): String {
    return url?.substringAfter("v=").toString()
}