import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun VideoPlayer(modifier: Modifier, url: String?, thumbnail: String?)