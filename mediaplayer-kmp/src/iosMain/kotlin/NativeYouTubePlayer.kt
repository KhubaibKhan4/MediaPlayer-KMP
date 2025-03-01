import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitView
import cocoapods.YouTubePlayer.YouTubePlayerView
import kotlinx.cinterop.ExperimentalForeignApi

@OptIn(ExperimentalForeignApi::class)
@Composable
fun NativeYouTubePlayer(
    url: String,
    modifier: Modifier = Modifier
) {
    val videoId = remember(url) {
        url.substringAfter("v=").substringBefore("&").ifEmpty { url.substringAfterLast("/") }
    }

    UIKitView(
        factory = {
            val newPlayerView = YouTubePlayerView()

            newPlayerView
        },
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f),
        update = { view ->

        }
    )
}
