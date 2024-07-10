import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import utils.Constant.NoThumbnail

@Composable
expect fun VideoPlayer(modifier: Modifier, url: String)
@Composable
expect fun MediaPlayer(modifier: Modifier, url: String)