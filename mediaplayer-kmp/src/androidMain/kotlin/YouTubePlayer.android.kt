import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.annotation.OptIn
import androidx.annotation.RequiresApi
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.ui.PlayerView
import coil3.compose.rememberAsyncImagePainter
import com.mediaplayer.kmp.AppContext
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.FullscreenListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.options.IFramePlayerOptions
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.kotlinx.multiplatform.library.template.R
import utils.findComponentActivity

@Composable
actual fun VideoPlayer(
    modifier: Modifier,
    url: String,
    autoPlay: Boolean,
    showControls: Boolean,
) {
    when {
        url?.contains("youtube.com") == true || url?.contains("youtu.be") == true -> {
            val videoId = extractVideoId(url)
            if (videoId != null) {
                YoutubeVideoPlayer(youtubeURL = url, autoPlay = autoPlay, showControls = showControls)
            } else {
                println("Video Id is Null or Invalid")
            }
        }

        isVideoFile(url) -> {
            ExoPlayerVideoPlayer(modifier = modifier, videoURL = url!!, autoPlay = autoPlay, showControls = showControls)
        }
    }
}

@OptIn(UnstableApi::class)
@Composable
fun ExoPlayerVideoPlayer(
    modifier: Modifier,
    videoURL: String,
    autoPlay: Boolean,
    showControls: Boolean
) {
    val context = AppContext.get()
    val exoPlayer = remember { ExoPlayer.Builder(context).build() }
    var isLoading by remember { mutableStateOf(true) }
    var isFullScreen by remember { mutableStateOf(false) }

    DisposableEffect(exoPlayer) {
        val mediaItem = MediaItem.fromUri(videoURL)
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
        exoPlayer.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                super.onPlaybackStateChanged(playbackState)
                isLoading = playbackState == Player.STATE_BUFFERING
            }
        })
        when(autoPlay){
            true -> exoPlayer.playWhenReady = true
            false -> exoPlayer.playWhenReady = false
        }

        onDispose {
            exoPlayer.release()
        }
    }

    Box(modifier = modifier) {
        AndroidView(
            factory = {
                PlayerView(context).apply {
                    player = exoPlayer
                    useController = true
                    if (showControls) {
                        showController()
                    }else{
                        hideController()
                    }
                    setControllerVisibilityListener(object : PlayerView.ControllerVisibilityListener {
                        @SuppressLint("ResourceType")
                        override fun onVisibilityChanged(visibility: Int) {
                            if (visibility == View.VISIBLE) {
                                findViewById<View>(R.drawable.baseline_fullscreen_24)?.setOnClickListener {
                                    isFullScreen = !isFullScreen
                                    handleFullScreen(context, isFullScreen)
                                }
                            }
                        }
                    })
                }
            },
            modifier = modifier,
            update = { view ->
                view.player = exoPlayer
            },
            onReset = { view ->
                view.clearFocus()
            }
        )

        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}

fun handleFullScreen(activity: Context, isFullScreen: Boolean) {
    if (isFullScreen) {
        (activity as Activity).requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val windowInsetsController = activity.window.insetsController
            windowInsetsController?.apply {
                hide(WindowInsets.Type.systemBars())
                systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            @Suppress("DEPRECATION")
            activity.window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                        View.SYSTEM_UI_FLAG_FULLSCREEN
        }
    } else {
        (activity as Activity).requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val windowInsetsController = activity.window.insetsController
            windowInsetsController?.apply {
                show(WindowInsets.Type.systemBars())
            }
        } else {
            @Suppress("DEPRECATION")
            activity.window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
        }
    }
}

fun isVideoFile(url: String?): Boolean {
    return url?.matches(
        Regex(".*\\.(mp4|mkv|webm|avi|mov|wmv|flv|m4v|3gp|mpeg|m3u8|ts|dash)\$", RegexOption.IGNORE_CASE)
    ) == true || url?.matches(
        Regex(".*(stream|video|live|media).*", RegexOption.IGNORE_CASE)
    ) == true
}
@Composable
fun YoutubeVideoPlayer(
    modifier: Modifier = Modifier,
    youtubeURL: String?,
    isPlaying: (Boolean) -> Unit = {},
    isLoading: (Boolean) -> Unit = {},
    onVideoEnded: () -> Unit = {},
    autoPlay: Boolean,
    showControls: Boolean
) {
    val context = LocalContext.current
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    val activity = context as Activity
    val videoId = extractVideoId(youtubeURL)
    val startTimeInSeconds = extractStartTime(youtubeURL)

    var player: com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer? = null
    val playerView = YouTubePlayerView(context)
    var isFullScreen by remember { mutableStateOf(false) }
    var isLoadingState by remember { mutableStateOf(true) }
    var thumbnailLoaded by remember { mutableStateOf(false) }

    val thumbnailUrl = "https://img.youtube.com/vi/$videoId/hqdefault.jpg"

    var fullscreenView: View? by remember { mutableStateOf(null) }

    val fullScreenListener = object : FullscreenListener {
        override fun onEnterFullscreen(view: View, exitFullscreen: () -> Unit) {
            isFullScreen = true
            fullscreenView = view
            playerView.visibility = View.GONE

            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

            Handler(Looper.getMainLooper()).post {
                (activity.window.decorView as ViewGroup).addView(view)
                configureFullScreen(activity, true)
            }
            player?.play()
        }

        override fun onExitFullscreen() {
            isFullScreen = false
            playerView.visibility = View.VISIBLE
            fullscreenView?.let { view ->
                (activity.window.decorView as ViewGroup).removeView(view)
                fullscreenView = null
            }

            Handler(Looper.getMainLooper()).post {
                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                configureFullScreen(activity, false)
            }
            player?.play()
        }
    }

    val playerStateListener = object : AbstractYouTubePlayerListener() {
        override fun onReady(youTubePlayer: com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer) {
            player = youTubePlayer
            youTubePlayer.loadVideo(videoId!!, startTimeInSeconds)
            isLoadingState = false
            isLoading(false)
        }

        override fun onStateChange(
            youTubePlayer: com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer,
            state: PlayerConstants.PlayerState,
        ) {
            when (state) {
                PlayerConstants.PlayerState.BUFFERING -> {
                    isLoadingState = true
                    isLoading(true)
                    isPlaying(false)
                }
                PlayerConstants.PlayerState.PLAYING -> {
                    isLoadingState = false
                    isLoading(false)
                    isPlaying(true)
                    thumbnailLoaded = true
                }
                PlayerConstants.PlayerState.ENDED -> {
                    isPlaying(false)
                    isLoading(false)
                    onVideoEnded()
                }
                else -> {}
            }
        }
    }

    val playerBuilder = IFramePlayerOptions.Builder().apply {
        controls(if (showControls) 1 else 0)
        fullscreen(1)
        autoplay(if (autoPlay) 1 else 0)
        modestBranding(1)
        rel(0)
        ivLoadPolicy(3)
        ccLoadPolicy(1)
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
                    initialize(playerStateListener, playerBuilder.build())
                    addFullscreenListener(fullScreenListener)
                }
            }
        )
    }

    DisposableEffect(Unit) {
        onDispose {
            playerView.removeYouTubePlayerListener(playerStateListener)
            playerView.removeFullscreenListener(fullScreenListener)
            playerView.release()
            player = null
        }
    }

    DisposableEffect(lifecycleOwner) {
        val lifecycle = lifecycleOwner.lifecycle
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> player?.play()
                Lifecycle.Event.ON_PAUSE -> player?.pause()
                else -> {}
            }
        }
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer) }
    }
}

fun configureFullScreen(activity: Activity, enable: Boolean) {
    if (enable) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            activity.window.setDecorFitsSystemWindows(false)
            activity.window.insetsController?.apply {
                hide(WindowInsets.Type.systemBars())
                systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            @Suppress("DEPRECATION")
            activity.window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
            @Suppress("DEPRECATION")
            activity.window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        }
    } else {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            activity.window.setDecorFitsSystemWindows(true)
            activity.window.insetsController?.show(WindowInsets.Type.systemBars())
        } else {
            @Suppress("DEPRECATION")
            activity.window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
            @Suppress("DEPRECATION")
            activity.window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
        }
    }
}
fun extractStartTime(url: String?): Float {
    url?.let {
        val regex = Regex(".*[?&]t=(\\d+)")
        val matchResult = regex.find(it)
        if (matchResult != null) {
            val timeInSeconds = matchResult.groupValues[1].toFloat()
            return timeInSeconds
        }
    }
    return 0f
}


fun extractVideoId(url: String?): String? {
    return url?.let {
        val regex =
            Regex("""(?:youtube\.com\/(?:[^\/]+\/.+\/|(?:v|e(?:mbed)?)\/|.*[?&]v=)|youtu\.be\/)([^"&?\/\s]{11})""")
        regex.find(it)?.groupValues?.get(1)
    }
}

@OptIn(UnstableApi::class)
@Composable
fun ExoPlayerAudioPlayer(
    audioURL: String,
    headers: Map<String, String>,
    startTime: Color,
    endTime: Color,
    autoPlay: Boolean,
    volumeIconColor: Color,
    playIconColor: Color,
    sliderTrackColor: Color,
    sliderIndicatorColor: Color
) {
    val context = LocalContext.current
    val exoPlayer = remember { ExoPlayer.Builder(context).build() }
    var isPlayingAudio by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    var currentTime by remember { mutableFloatStateOf(0f) }
    var duration by remember { androidx.compose.runtime.mutableFloatStateOf(0f) }
    var volume by remember { mutableFloatStateOf(1f) }

    DisposableEffect(audioURL) {
        val dataSourceFactory = DefaultDataSource.Factory(context) {
            DefaultHttpDataSource.Factory()
                .setDefaultRequestProperties(headers)
                .createDataSource()
        }

        val mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
            .createMediaSource(MediaItem.fromUri(audioURL))

        exoPlayer.setMediaSource(mediaSource)
        exoPlayer.prepare()
        exoPlayer.playWhenReady = autoPlay

        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                isLoading = state == Player.STATE_BUFFERING
                if (state == Player.STATE_READY) {
                    duration = exoPlayer.duration.toFloat()
                    isPlayingAudio = exoPlayer.isPlaying
                }
                if (state == Player.STATE_ENDED) {
                    isPlayingAudio = false
                }
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                isPlayingAudio = isPlaying
            }
        }
        exoPlayer.addListener(listener)

        val updateTimeJob = CoroutineScope(Dispatchers.Main).launch {
            while (true) {
                if (exoPlayer.isPlaying) {
                    currentTime = exoPlayer.currentPosition.toFloat()
                }
                delay(1000L)
            }
        }

        onDispose {
            exoPlayer.removeListener(listener)
            exoPlayer.release()
            updateTimeJob.cancel()
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
                value = currentTime,
                onValueChange = { exoPlayer.seekTo(it.toLong()) },
                valueRange = 0f..duration,
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
                Text(formatTime(currentTime.toLong()))
                Text(formatTime(duration.toLong()))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                IconButton(onClick = {
                    if (isPlayingAudio) {
                        exoPlayer.pause()
                    } else {
                        exoPlayer.play()
                    }
                }) {
                    Icon(
                        imageVector = if (isPlayingAudio) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = null,
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
                        contentDescription = null,
                        tint = volumeIconColor
                    )
                }
            }
        }
    }
}

fun formatTime(ms: Long): String {
    val seconds = (ms / 1000) % 60
    val minutes = (ms / (1000 * 60)) % 60
    val hours = (ms / (1000 * 60 * 60)) % 24

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
            sliderIndicatorColor = sliderIndicatorColor
        )
    } else {
        ExoPlayerVideoPlayer(modifier = modifier,videoURL = url, autoPlay = autoPlay, showControls = showControls)
    }
}

fun isAudioFile(url: String?): Boolean {
    return url?.matches(
        Regex(".*\\.(mp3|wav|aac|ogg|m4a|m3u|pls|m3u8)\$", RegexOption.IGNORE_CASE)
    ) == true || url?.matches(
        Regex(".*(radio|stream|icecast|shoutcast|audio|listen).*", RegexOption.IGNORE_CASE)
    ) == true
}
