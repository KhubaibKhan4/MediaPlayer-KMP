package io.github.khubaibkhan4.mediaplayer

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Plays a YouTube link or any other video URL / local file path.
 *
 * @param headers HTTP headers sent with media requests (e.g. `Authorization: Bearer <jwt>`).
 * Supported on Android and iOS; on Web and Desktop the whole file is downloaded first when headers are set.
 * Ignored for YouTube links.
 * @param onPlayerEvent Receives playback lifecycle events such as [PlayerEvent.Playing],
 * [PlayerEvent.Ended] and [PlayerEvent.Error].
 */
@Composable
expect fun VideoPlayer(
    modifier: Modifier,
    url: String,
    autoPlay: Boolean,
    showControls: Boolean,
    headers: Map<String, String> = emptyMap(),
    onPlayerEvent: (PlayerEvent) -> Unit = {},
)

@Composable
expect fun MediaPlayer(
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
    showControls: Boolean = true,
    onPlayerEvent: (PlayerEvent) -> Unit = {},
)

enum class PagerType {
    Horizontal,
    Vertical
}

@Composable
fun ReelsView(
    videoUrls: List<String>,
    pagerType: PagerType,
    modifier: Modifier = Modifier,
    autoPlay: Boolean = true,
    showControls: Boolean = true,
    onInteraction: (Int, String) -> Unit = { _, _ -> },
    onPlayerEvent: (page: Int, event: PlayerEvent) -> Unit = { _, _ -> },
) {
    val pagerState = rememberPagerState(pageCount = { videoUrls.size })

    when (pagerType) {
        PagerType.Horizontal -> {
            HorizontalPager(
                state = pagerState,
                modifier = modifier,
            ) { page ->
                VideoPlayerScreen(
                    url = videoUrls[page],
                    autoPlay = autoPlay,
                    onInteraction = { onInteraction(page, videoUrls[page]) },
                    showControls = showControls,
                    onPlayerEvent = { onPlayerEvent(page, it) }
                )
            }
        }
        PagerType.Vertical -> {
            VerticalPager(
                state = pagerState,
                modifier = modifier,
            ) { page ->
                VideoPlayerScreen(
                    url = videoUrls[page],
                    autoPlay = autoPlay,
                    onInteraction = { onInteraction(page, videoUrls[page]) },
                    showControls = showControls,
                    onPlayerEvent = { onPlayerEvent(page, it) }
                )
            }
        }
    }
}

@Composable
fun VideoPlayerScreen(
    url: String,
    autoPlay: Boolean,
    onInteraction: () -> Unit,
    showControls: Boolean,
    onPlayerEvent: (PlayerEvent) -> Unit = {},
) {
    VideoPlayer(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
        url = url,
        autoPlay = autoPlay,
        showControls = showControls,
        onPlayerEvent = onPlayerEvent
    )
}
