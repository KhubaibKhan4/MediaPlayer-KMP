import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import utils.Constant.NoThumbnail

@Composable
expect fun VideoPlayer(modifier: Modifier, url: String)

@Composable
expect fun MediaPlayer(
    modifier: Modifier,
    url: String,
    startTime: Color,
    endTime: Color,
    volumeIconColor: Color,
    playIconColor: Color,
    sliderTrackColor: Color,
    sliderIndicatorColor: Color
)