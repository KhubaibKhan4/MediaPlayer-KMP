import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
expect fun VideoPlayer(modifier: Modifier, url: String, autoPlay: Boolean)

@Composable
expect fun MediaPlayer(
    modifier: Modifier,
    url: String,
    headers: Map<String, String> = emptyMap(),
    startTime: Color,
    endTime: Color,
    autoPlay: Boolean,
    volumeIconColor: Color,
    playIconColor: Color,
    sliderTrackColor: Color,
    sliderIndicatorColor: Color
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
    onInteraction: (Int, String) -> Unit = { _, _ -> }
) {
    val pagerState = rememberPagerState(pageCount = {videoUrls.size})

    when(pagerType){
        PagerType.Horizontal -> {
            HorizontalPager(
                state = pagerState,
                modifier = modifier,
            ) { page ->
                VideoPlayerScreen(
                    url = videoUrls[page],
                    autoPlay = autoPlay,
                    onInteraction = { onInteraction(page, videoUrls[page]) }
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
                    onInteraction = { onInteraction(page, videoUrls[page]) }
                )
            }
        }
    }
}

@Composable
fun VideoPlayerScreen(
    url: String,
    autoPlay: Boolean,
    onInteraction: () -> Unit
) {
    VideoPlayer(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
        url = url,
        autoPlay = autoPlay
    )
}