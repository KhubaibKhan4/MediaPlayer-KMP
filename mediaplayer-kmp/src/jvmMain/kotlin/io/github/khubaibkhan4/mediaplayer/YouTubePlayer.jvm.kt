package io.github.khubaibkhan4.mediaplayer

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
actual fun VideoPlayer(
    modifier: Modifier,
    url: String,
    autoPlay: Boolean,
    showControls: Boolean,
    headers: Map<String, String>,
    onPlayerEvent: (PlayerEvent) -> Unit,
) {
    if (isYouTubeUrl(url)) {
        val videoId = extractYouTubeVideoId(url)
        if (videoId == null) {
            LaunchedEffect(url) { onPlayerEvent(PlayerEvent.Error("Invalid YouTube URL: $url")) }
            return
        }
        val embedUrl = "https://www.youtube.com/embed/$videoId" +
            "?autoplay=${if (autoPlay) 1 else 0}&controls=${if (showControls) 1 else 0}" +
            "&start=${extractYouTubeStartSeconds(url)}&rel=0&modestbranding=1&playsinline=1"
        DesktopWebView(
            modifier = modifier,
            url = embedUrl,
            autoPlay = autoPlay,
            showControls = showControls,
            isYouTube = true,
            onPlayerEvent = onPlayerEvent
        )
    } else {
        DesktopWebView(
            modifier = modifier,
            url = url,
            autoPlay = autoPlay,
            showControls = showControls,
            headers = headers,
            onPlayerEvent = onPlayerEvent
        )
    }
}

@Composable
actual fun MediaPlayer(
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
    showControls: Boolean,
    onPlayerEvent: (PlayerEvent) -> Unit,
) {
    DesktopAudioPlayer(
        modifier = modifier,
        audioURL = url,
        headers = headers,
        startTime = startTime,
        endTime = endTime,
        autoPlay = autoPlay,
        volumeIconColor = volumeIconColor,
        playIconColor = playIconColor,
        sliderTrackColor = sliderTrackColor,
        sliderIndicatorColor = sliderIndicatorColor,
        showControls = showControls,
        onPlayerEvent = onPlayerEvent
    )
}
