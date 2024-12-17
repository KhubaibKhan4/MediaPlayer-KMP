import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

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