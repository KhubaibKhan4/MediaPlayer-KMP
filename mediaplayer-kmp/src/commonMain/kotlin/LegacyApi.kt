// Root-package aliases kept so code written against 2.1.x (`import VideoPlayer`) still compiles.
// The real API lives in `io.github.khubaibkhan4.mediaplayer`.

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

private const val MOVED = "Moved to the io.github.khubaibkhan4.mediaplayer package. The root-package alias will be removed in a future release."

@Deprecated(MOVED, ReplaceWith("VideoPlayer(modifier, url, autoPlay, showControls)", "io.github.khubaibkhan4.mediaplayer.VideoPlayer"))
@Composable
fun VideoPlayer(modifier: Modifier, url: String, autoPlay: Boolean, showControls: Boolean) =
    io.github.khubaibkhan4.mediaplayer.VideoPlayer(modifier, url, autoPlay, showControls)

@Deprecated(
    MOVED,
    ReplaceWith(
        "MediaPlayer(modifier, url, headers, startTime, endTime, autoPlay, volumeIconColor, playIconColor, sliderTrackColor, sliderIndicatorColor, showControls)",
        "io.github.khubaibkhan4.mediaplayer.MediaPlayer"
    )
)
@Composable
fun MediaPlayer(
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
) = io.github.khubaibkhan4.mediaplayer.MediaPlayer(
    modifier, url, headers, startTime, endTime, autoPlay,
    volumeIconColor, playIconColor, sliderTrackColor, sliderIndicatorColor, showControls
)

@Deprecated(MOVED, ReplaceWith("PagerType", "io.github.khubaibkhan4.mediaplayer.PagerType"))
typealias PagerType = io.github.khubaibkhan4.mediaplayer.PagerType

@Deprecated(
    MOVED,
    ReplaceWith("ReelsView(videoUrls, pagerType, modifier, autoPlay, showControls, onInteraction)", "io.github.khubaibkhan4.mediaplayer.ReelsView")
)
@Composable
fun ReelsView(
    videoUrls: List<String>,
    pagerType: io.github.khubaibkhan4.mediaplayer.PagerType,
    modifier: Modifier = Modifier,
    autoPlay: Boolean = true,
    showControls: Boolean = true,
    onInteraction: (Int, String) -> Unit = { _, _ -> },
) = io.github.khubaibkhan4.mediaplayer.ReelsView(videoUrls, pagerType, modifier, autoPlay, showControls, onInteraction)

@Deprecated(
    MOVED,
    ReplaceWith("VideoPlayerScreen(url, autoPlay, onInteraction, showControls)", "io.github.khubaibkhan4.mediaplayer.VideoPlayerScreen")
)
@Composable
fun VideoPlayerScreen(url: String, autoPlay: Boolean, onInteraction: () -> Unit, showControls: Boolean) =
    io.github.khubaibkhan4.mediaplayer.VideoPlayerScreen(url, autoPlay, onInteraction, showControls)
