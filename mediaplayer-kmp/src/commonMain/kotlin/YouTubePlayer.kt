import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import utils.Constant.NoThumbnail

@Composable
expect fun VideoPlayer(modifier: Modifier, url: String?, thumbnail: String?= NoThumbnail, onPlayClick: () -> Unit)