package html_embeded_content.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import html_embeded_content.domain.repository.HtmlContentViewer.HtmlContentViewer

@Composable
expect fun HtmlContentViewerView(
    viewer: HtmlContentViewer,
    modifier: Modifier = Modifier
)