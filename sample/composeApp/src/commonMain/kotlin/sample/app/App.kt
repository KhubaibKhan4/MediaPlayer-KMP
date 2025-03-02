package sample.app

import MediaPlayer
import VideoPlayer
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import html_embeded_content.data.EmbedOptions
import html_embeded_content.domain.factory.HtmlContentViewerFactory
import html_embeded_content.domain.factory.HtmlEmbedFeature
import html_embeded_content.presentation.HtmlContentViewerView
import kotlinx.coroutines.launch

@Composable
fun App() {
    MainScree(modifier = Modifier.fillMaxSize())
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScree(modifier: Modifier = Modifier) {
    var isMedia by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    val coroutineScope = rememberCoroutineScope()

    if(isMedia){
        ModalBottomSheet(
            onDismissRequest = {
                isMedia = !isMedia
                coroutineScope.launch {
                    sheetState.hide()
                }
            },
            sheetState = sheetState,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(360.dp)
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                VideoPlayer(
                    modifier = Modifier.fillMaxWidth()
                        .height(340.dp)
                        .aspectRatio(16/9f)
                    ,
                    url = "https://freetestdata.com/wp-content/uploads/2022/02/Free_Test_Data_1MB_MP4.mp4",
                    showControls = true,
                    autoPlay = false
                )
            }

        }
    }

    Box(
        modifier = Modifier.fillMaxSize().background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(onClick = {
                isMedia = !isMedia
                coroutineScope.launch { sheetState.show() }
            }) {
                Text("Open Video Player")
            }

            val viewer = HtmlContentViewerFactory().createHtmlContentViewer()
            val htmlEmbedFeature = HtmlEmbedFeature(viewer)
            htmlEmbedFeature.embedHtml(
                url = "https://github.com/KhubaibKhan4/MediaPlayer-KMP/",
                options = EmbedOptions(
                    customCss = "body { background-color: #f0f0f0; }",
                    onPageLoaded = { println("Page loaded successfully!") },
                    onError = { error -> println("Error loading page: $error") }
                )
            )

            HtmlContentViewerView(
                viewer = viewer,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}