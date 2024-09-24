import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.os.Build
import android.view.ActionProvider
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.annotation.OptIn
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerControlView
import androidx.media3.ui.PlayerView
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

@RequiresApi(Build.VERSION_CODES.S)
@Composable
actual fun VideoPlayer(
    modifier: Modifier,
    url: String,
) {
    when {
        url?.contains("youtube.com") == true || url?.contains("youtu.be") == true -> {
            val videoId = extractVideoId(url)
            if (videoId != null) {
                YoutubeVideoPlayer(youtubeURL = url)
            } else {
                println("Video Id is Null or Invalid")
            }
        }

        isVideoFile(url) -> {
            ExoPlayerVideoPlayer(videoURL = url!!)
        }
    }
}

@Composable
fun ExoPlayerVideoPlayer(videoURL: String) {
    val context = LocalContext.current
    val activity = context as ComponentActivity
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
        exoPlayer.playWhenReady = true

        onDispose {
            exoPlayer.release()
        }
    }

    Box(modifier = Modifier.fillMaxWidth()) {
        AndroidView(
            factory = {
                PlayerView(context).apply {
                    player = exoPlayer
                    useController = true
                    setControllerVisibilityListener(object : PlayerView.ControllerVisibilityListener {
                        @SuppressLint("ResourceType")
                        override fun onVisibilityChanged(visibility: Int) {
                            if (visibility == View.VISIBLE) {
                                findViewById<View>(R.drawable.baseline_fullscreen_24)?.setOnClickListener {
                                    isFullScreen = !isFullScreen
                                    handleFullScreen(activity, isFullScreen)
                                }
                            }
                        }
                    })
                }
            },
            modifier = Modifier.fillMaxWidth(),
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

fun handleFullScreen(activity: ComponentActivity, isFullScreen: Boolean) {
    if (isFullScreen) {
        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

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
        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED

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
        Regex(
            ".*\\.(mp4|mkv|webm|avi|mov|wmv|flv|m4v|3gp|mpeg)\$",
            RegexOption.IGNORE_CASE
        )
    ) == true
}

@Composable
fun YoutubeVideoPlayer(
    modifier: Modifier = Modifier,
    youtubeURL: String?,
    isPlaying: (Boolean) -> Unit = {},
    isLoading: (Boolean) -> Unit = {},
    onVideoEnded: () -> Unit = {},
) {
    val mContext = LocalContext.current
    val mLifeCycleOwner = LocalLifecycleOwner.current
    val activity = mContext as ComponentActivity
    val videoId = extractVideoId(youtubeURL)
    val startTimeInSeconds = extractStartTime(youtubeURL)
    var player: com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer? = null
    val playerFragment = YouTubePlayerView(mContext)
    var isFullScreen by remember { mutableStateOf(false) }

    val playerStateListener = object : AbstractYouTubePlayerListener() {
        override fun onReady(youTubePlayer: com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer) {
            super.onReady(youTubePlayer)
            player = youTubePlayer
            youTubePlayer.loadVideo(videoId!!, startTimeInSeconds)
        }

        override fun onStateChange(
            youTubePlayer: com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer,
            state: PlayerConstants.PlayerState,
        ) {
            super.onStateChange(youTubePlayer, state)
            when (state) {
                PlayerConstants.PlayerState.BUFFERING -> {
                    isLoading.invoke(true)
                    isPlaying.invoke(false)
                }

                PlayerConstants.PlayerState.PLAYING -> {
                    isLoading.invoke(false)
                    isPlaying.invoke(true)
                }

                PlayerConstants.PlayerState.ENDED -> {
                    isPlaying.invoke(false)
                    isLoading.invoke(false)
                    onVideoEnded.invoke()
                }

                else -> {}
            }
        }

        override fun onError(
            youTubePlayer: com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer,
            error: PlayerConstants.PlayerError,
        ) {
            super.onError(youTubePlayer, error)
            println("iFramePlayer Error Reason = $error")
        }
    }

    val fullScreenListener = object : FullscreenListener {

        override fun onEnterFullscreen(fullscreenView: View, exitFullscreen: () -> Unit) {
            isFullScreen = true
            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                activity.window.insetsController?.apply {
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
        }

        override fun onExitFullscreen() {
            isFullScreen = false
            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                activity.window.insetsController?.apply {
                    show(WindowInsets.Type.systemBars())
                }
            } else {
                @Suppress("DEPRECATION")
                activity.window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
            }
        }
    }

    val playerBuilder = IFramePlayerOptions.Builder().apply {
        controls(1)
        fullscreen(1)
        autoplay(1)
        modestBranding(1)
        ccLoadPolicy(1)
        rel(0)
    }

    AndroidView(
        modifier = modifier
            .background(Color.DarkGray)
            .fillMaxSize(),
        factory = {
            playerFragment.apply {
                enableAutomaticInitialization = false
                initialize(playerStateListener, playerBuilder.build())
                addFullscreenListener(fullScreenListener)
            }
        }
    )

    DisposableEffect(key1 = Unit, effect = {
        onDispose {
            playerFragment.removeYouTubePlayerListener(playerStateListener)
            playerFragment.removeFullscreenListener(fullScreenListener)
            playerFragment.release()
            player = null
        }
    })

    DisposableEffect(mLifeCycleOwner) {
        val lifecycle = mLifeCycleOwner.lifecycle
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    player?.play()
                }

                Lifecycle.Event.ON_PAUSE -> {
                    player?.pause()
                }

                else -> {
                    //
                }
            }
        }
        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
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
    startTime: Color,
    endTime: Color,
    volumeIconColor: Color,
    playIconColor: Color,
    sliderTrackColor: Color,
    sliderIndicatorColor: Color
) {
    val context = LocalContext.current
    val exoPlayer = remember { ExoPlayer.Builder(context).build() }
    var isPlayingAudio by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    var currentTime by remember { mutableStateOf(0f) }
    var duration by remember { mutableStateOf(0f) }
    var volume by remember { mutableStateOf(1f) }

    DisposableEffect(audioURL) {
        val mediaItem = MediaItem.fromUri(audioURL)
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
        exoPlayer.playWhenReady = true

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
    startTime: Color,
    endTime: Color,
    volumeIconColor: Color,
    playIconColor: Color,
    sliderTrackColor: Color,
    sliderIndicatorColor: Color
) {
    if (isAudioFile(url)) {
        ExoPlayerAudioPlayer(
            audioURL = url,
            startTime = startTime,
            endTime = endTime,
            volumeIconColor = volumeIconColor,
            playIconColor = playIconColor,
            sliderTrackColor = sliderTrackColor,
            sliderIndicatorColor = sliderIndicatorColor
        )
    } else {
        ExoPlayerVideoPlayer(videoURL = url)
    }
}

fun isAudioFile(url: String?): Boolean {
    return url?.matches(
        Regex(".*\\.(mp3|wav|aac|ogg|m4a)\$", RegexOption.IGNORE_CASE)
    ) == true
}