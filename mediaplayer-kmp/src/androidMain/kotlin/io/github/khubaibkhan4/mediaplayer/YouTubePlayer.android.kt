package io.github.khubaibkhan4.mediaplayer

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.annotation.OptIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.ui.PlayerView
import coil3.compose.rememberAsyncImagePainter
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.FullscreenListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.options.IFramePlayerOptions
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import kotlinx.coroutines.delay
import utils.findComponentActivity

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
        if (extractYouTubeVideoId(url) != null) {
            YoutubeVideoPlayer(
                modifier = modifier,
                youtubeURL = url,
                autoPlay = autoPlay,
                showControls = showControls,
                onPlayerEvent = onPlayerEvent
            )
        } else {
            LaunchedEffect(url) { onPlayerEvent(PlayerEvent.Error("Invalid YouTube URL: $url")) }
        }
    } else {
        // ExoPlayer sniffs the container itself, so URLs without a file extension work too.
        ExoPlayerVideoPlayer(
            modifier = modifier,
            videoURL = url,
            autoPlay = autoPlay,
            showControls = showControls,
            headers = headers,
            onPlayerEvent = onPlayerEvent
        )
    }
}

/** Builds a media source that sends [headers] with every HTTP request and handles HLS, DASH and progressive files. */
@OptIn(UnstableApi::class)
internal fun buildMediaSource(context: Context, url: String, headers: Map<String, String>): MediaSource {
    val httpFactory = DefaultHttpDataSource.Factory()
        .setAllowCrossProtocolRedirects(true)
        .setDefaultRequestProperties(headers)
    val dataSourceFactory = DefaultDataSource.Factory(context, httpFactory)
    return DefaultMediaSourceFactory(dataSourceFactory).createMediaSource(MediaItem.fromUri(url))
}

/** Forwards ExoPlayer state changes to [onPlayerEvent]. */
private class ExoPlayerEventListener(
    private val player: Player,
    private val setLoading: (Boolean) -> Unit,
    private val onPlayerEvent: () -> (PlayerEvent) -> Unit,
) : Player.Listener {
    private var readySent = false

    override fun onPlaybackStateChanged(playbackState: Int) {
        setLoading(playbackState == Player.STATE_BUFFERING)
        when (playbackState) {
            Player.STATE_BUFFERING -> onPlayerEvent()(PlayerEvent.Buffering)
            Player.STATE_READY -> if (!readySent) {
                readySent = true
                onPlayerEvent()(PlayerEvent.Ready)
            }
            Player.STATE_ENDED -> onPlayerEvent()(PlayerEvent.Ended)
            else -> Unit
        }
    }

    override fun onIsPlayingChanged(isPlaying: Boolean) {
        when {
            isPlaying -> onPlayerEvent()(PlayerEvent.Playing)
            // isPlaying also turns false while buffering or at the end; only report a real pause.
            !player.playWhenReady -> onPlayerEvent()(PlayerEvent.Paused)
        }
    }

    override fun onPlayerError(error: PlaybackException) {
        setLoading(false)
        onPlayerEvent()(PlayerEvent.Error(error.message ?: error.errorCodeName))
    }
}

@OptIn(UnstableApi::class)
@Composable
internal fun ExoPlayerVideoPlayer(
    modifier: Modifier,
    videoURL: String,
    autoPlay: Boolean,
    showControls: Boolean,
    headers: Map<String, String> = emptyMap(),
    onPlayerEvent: (PlayerEvent) -> Unit = {},
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val currentOnPlayerEvent by rememberUpdatedState(onPlayerEvent)
    val exoPlayer = remember(context) { ExoPlayer.Builder(context).build() }
    var isLoading by remember { mutableStateOf(true) }
    var isFullScreen by rememberSaveable { mutableStateOf(false) }

    DisposableEffect(exoPlayer) {
        val listener = ExoPlayerEventListener(exoPlayer, { isLoading = it }, { currentOnPlayerEvent })
        exoPlayer.addListener(listener)
        onDispose {
            exoPlayer.removeListener(listener)
            exoPlayer.release()
        }
    }

    DisposableEffect(exoPlayer, videoURL, headers) {
        exoPlayer.setMediaSource(buildMediaSource(context, videoURL, headers))
        exoPlayer.prepare()
        exoPlayer.playWhenReady = autoPlay
        onDispose { }
    }

    DisposableEffect(lifecycleOwner, exoPlayer) {
        var resumeOnStart = false
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> {
                    resumeOnStart = exoPlayer.playWhenReady
                    exoPlayer.playWhenReady = false
                }
                Lifecycle.Event.ON_RESUME -> if (resumeOnStart) exoPlayer.playWhenReady = true
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Box(modifier = modifier) {
        ExoPlayerSurface(
            exoPlayer = exoPlayer,
            // Only one view can own the video surface; hand it to the fullscreen dialog while it is open.
            attached = !isFullScreen,
            showControls = showControls,
            isFullScreen = false,
            onFullScreenChange = { isFullScreen = it },
            modifier = Modifier.fillMaxSize()
        )

        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }
    }

    if (isFullScreen) {
        Dialog(
            onDismissRequest = { isFullScreen = false },
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                decorFitsSystemWindows = false
            )
        ) {
            val dialogWindow = (LocalView.current.parent as? DialogWindowProvider)?.window
            LaunchedEffect(dialogWindow) {
                dialogWindow ?: return@LaunchedEffect
                WindowCompat.getInsetsController(dialogWindow, dialogWindow.decorView).apply {
                    hide(WindowInsetsCompat.Type.systemBars())
                    systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                }
            }
            ExoPlayerSurface(
                exoPlayer = exoPlayer,
                attached = true,
                showControls = true,
                isFullScreen = true,
                onFullScreenChange = { isFullScreen = it },
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            )
        }
    }
}

@OptIn(UnstableApi::class)
@Composable
private fun ExoPlayerSurface(
    exoPlayer: ExoPlayer,
    attached: Boolean,
    showControls: Boolean,
    isFullScreen: Boolean,
    onFullScreenChange: (Boolean) -> Unit,
    modifier: Modifier,
) {
    val currentOnFullScreenChange by rememberUpdatedState(onFullScreenChange)
    AndroidView(
        factory = { context ->
            PlayerView(context).apply {
                // Registering a listener is what makes PlayerView show its fullscreen button.
                setFullscreenButtonClickListener { currentOnFullScreenChange(it) }
                setFullscreenButtonState(isFullScreen)
            }
        },
        update = { view ->
            view.player = if (attached) exoPlayer else null
            view.useController = showControls
        },
        onRelease = { view -> view.player = null },
        modifier = modifier
    )
}

@Composable
internal fun YoutubeVideoPlayer(
    modifier: Modifier = Modifier,
    youtubeURL: String?,
    autoPlay: Boolean,
    showControls: Boolean,
    onPlayerEvent: (PlayerEvent) -> Unit = {},
) {
    val context = LocalContext.current
    val activity = remember(context) { context.findComponentActivity() }
    val lifecycleOwner = LocalLifecycleOwner.current
    val currentOnPlayerEvent by rememberUpdatedState(onPlayerEvent)

    val videoId = extractYouTubeVideoId(youtubeURL) ?: return
    val startTimeInSeconds = extractYouTubeStartSeconds(youtubeURL).toFloat()
    val thumbnailUrl = "https://img.youtube.com/vi/$videoId/hqdefault.jpg"

    var isLoadingState by remember { mutableStateOf(true) }
    var thumbnailLoaded by remember { mutableStateOf(false) }
    val playerRef = remember { arrayOfNulls<YouTubePlayer>(1) }
    val playerView = remember(context) { YouTubePlayerView(context) }

    val fullScreenListener = remember(playerView, activity) {
        object : FullscreenListener {
            private var fullscreenView: View? = null

            override fun onEnterFullscreen(fullscreenView: View, exitFullscreen: () -> Unit) {
                val window = activity?.window ?: return
                this.fullscreenView = fullscreenView
                playerView.visibility = View.GONE
                (window.decorView as ViewGroup).addView(fullscreenView)
                WindowCompat.getInsetsController(window, window.decorView).apply {
                    hide(WindowInsetsCompat.Type.systemBars())
                    systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                }
            }

            override fun onExitFullscreen() {
                val window = activity?.window ?: return
                playerView.visibility = View.VISIBLE
                fullscreenView?.let { (window.decorView as ViewGroup).removeView(it) }
                fullscreenView = null
                WindowCompat.getInsetsController(window, window.decorView)
                    .show(WindowInsetsCompat.Type.systemBars())
            }
        }
    }

    val playerStateListener = remember(videoId) {
        object : AbstractYouTubePlayerListener() {
            override fun onReady(youTubePlayer: YouTubePlayer) {
                playerRef[0] = youTubePlayer
                // loadVideo always starts playback, cueVideo only shows the poster frame.
                if (autoPlay) {
                    youTubePlayer.loadVideo(videoId, startTimeInSeconds)
                } else {
                    youTubePlayer.cueVideo(videoId, startTimeInSeconds)
                }
                isLoadingState = false
                currentOnPlayerEvent(PlayerEvent.Ready)
            }

            override fun onStateChange(youTubePlayer: YouTubePlayer, state: PlayerConstants.PlayerState) {
                when (state) {
                    PlayerConstants.PlayerState.BUFFERING -> currentOnPlayerEvent(PlayerEvent.Buffering)
                    PlayerConstants.PlayerState.PLAYING -> {
                        isLoadingState = false
                        thumbnailLoaded = true
                        currentOnPlayerEvent(PlayerEvent.Playing)
                    }
                    PlayerConstants.PlayerState.PAUSED -> currentOnPlayerEvent(PlayerEvent.Paused)
                    PlayerConstants.PlayerState.ENDED -> currentOnPlayerEvent(PlayerEvent.Ended)
                    else -> Unit
                }
            }

            override fun onError(youTubePlayer: YouTubePlayer, error: PlayerConstants.PlayerError) {
                isLoadingState = false
                currentOnPlayerEvent(PlayerEvent.Error("YouTube error: $error"))
            }
        }
    }

    val playerOptions = remember(showControls, autoPlay) {
        IFramePlayerOptions.Builder()
            .controls(if (showControls) 1 else 0)
            .fullscreen(1)
            .autoplay(if (autoPlay) 1 else 0)
            .modestBranding(1)
            .rel(0)
            .ivLoadPolicy(3)
            .ccLoadPolicy(1)
            .build()
    }

    Box(modifier = modifier.background(Color.Black)) {
        if (!thumbnailLoaded) {
            Image(
                painter = rememberAsyncImagePainter(thumbnailUrl),
                contentDescription = "Video Thumbnail",
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            )
        }

        if (isLoadingState) {
            CircularProgressIndicator(
                modifier = Modifier
                    .size(50.dp)
                    .align(Alignment.Center),
                color = Color.White
            )
        }

        AndroidView(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .alpha(if (isLoadingState) 0f else 1f),
            factory = {
                playerView.apply {
                    enableAutomaticInitialization = false
                    initialize(playerStateListener, playerOptions)
                    addFullscreenListener(fullScreenListener)
                }
            }
        )
    }

    DisposableEffect(playerView) {
        onDispose {
            playerView.removeYouTubePlayerListener(playerStateListener)
            playerView.removeFullscreenListener(fullScreenListener)
            playerView.release()
            playerRef[0] = null
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> if (autoPlay) playerRef[0]?.play()
                Lifecycle.Event.ON_PAUSE -> playerRef[0]?.pause()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
}

@OptIn(UnstableApi::class)
@Composable
internal fun ExoPlayerAudioPlayer(
    audioURL: String,
    headers: Map<String, String>,
    startTime: Color,
    endTime: Color,
    autoPlay: Boolean,
    volumeIconColor: Color,
    playIconColor: Color,
    sliderTrackColor: Color,
    sliderIndicatorColor: Color,
    onPlayerEvent: (PlayerEvent) -> Unit = {},
) {
    val context = LocalContext.current
    val currentOnPlayerEvent by rememberUpdatedState(onPlayerEvent)
    val exoPlayer = remember(context) { ExoPlayer.Builder(context).build() }
    var isPlayingAudio by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    var currentTime by remember { mutableFloatStateOf(0f) }
    var duration by remember { mutableFloatStateOf(0f) }
    var volume by remember { mutableFloatStateOf(1f) }

    DisposableEffect(exoPlayer) {
        val eventListener = ExoPlayerEventListener(exoPlayer, { isLoading = it }, { currentOnPlayerEvent })
        val uiListener = object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                if (state == Player.STATE_READY) duration = exoPlayer.duration.coerceAtLeast(0L).toFloat()
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                isPlayingAudio = isPlaying
            }
        }
        exoPlayer.addListener(eventListener)
        exoPlayer.addListener(uiListener)
        onDispose {
            exoPlayer.removeListener(eventListener)
            exoPlayer.removeListener(uiListener)
            exoPlayer.release()
        }
    }

    DisposableEffect(exoPlayer, audioURL, headers) {
        exoPlayer.setMediaSource(buildMediaSource(context, audioURL, headers))
        exoPlayer.prepare()
        exoPlayer.playWhenReady = autoPlay
        onDispose { }
    }

    LaunchedEffect(exoPlayer) {
        while (true) {
            if (exoPlayer.isPlaying) currentTime = exoPlayer.currentPosition.toFloat()
            delay(500L)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (isLoading) {
            CircularProgressIndicator()
        } else {
            Slider(
                value = currentTime.coerceIn(0f, duration.coerceAtLeast(0f)),
                onValueChange = {
                    currentTime = it
                    exoPlayer.seekTo(it.toLong())
                },
                valueRange = 0f..duration.coerceAtLeast(0f),
                modifier = Modifier.fillMaxWidth(),
                colors = SliderDefaults.colors(
                    thumbColor = sliderIndicatorColor,
                    activeTrackColor = sliderTrackColor
                )
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(formatTime(currentTime.toLong()), color = startTime)
                Text(formatTime(duration.toLong()), color = endTime)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                IconButton(onClick = {
                    if (isPlayingAudio) exoPlayer.pause() else exoPlayer.play()
                }) {
                    Icon(
                        imageVector = if (isPlayingAudio) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlayingAudio) "Pause" else "Play",
                        tint = playIconColor
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                IconButton(onClick = {
                    volume = if (volume == 1f) 0f else 1f
                    exoPlayer.volume = volume
                }) {
                    Icon(
                        imageVector = if (volume == 1f) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                        contentDescription = if (volume == 1f) "Mute" else "Unmute",
                        tint = volumeIconColor
                    )
                }
            }
        }
    }
}

internal fun formatTime(ms: Long): String {
    val seconds = (ms / 1000) % 60
    val minutes = (ms / (1000 * 60)) % 60
    val hours = ms / (1000 * 60 * 60)

    return if (hours > 0) {
        String.format("%02d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%02d:%02d", minutes, seconds)
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
    if (isAudioFile(url)) {
        ExoPlayerAudioPlayer(
            audioURL = url,
            headers = headers,
            startTime = startTime,
            endTime = endTime,
            autoPlay = autoPlay,
            volumeIconColor = volumeIconColor,
            playIconColor = playIconColor,
            sliderTrackColor = sliderTrackColor,
            sliderIndicatorColor = sliderIndicatorColor,
            onPlayerEvent = onPlayerEvent
        )
    } else {
        ExoPlayerVideoPlayer(
            modifier = modifier,
            videoURL = url,
            autoPlay = autoPlay,
            showControls = showControls,
            headers = headers,
            onPlayerEvent = onPlayerEvent
        )
    }
}
