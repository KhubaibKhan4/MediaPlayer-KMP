import android.os.Build
import androidx.activity.ComponentActivity
import androidx.annotation.OptIn
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.options.IFramePlayerOptions
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView

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
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build()
    }
    var isLoading by remember { mutableStateOf(true) }

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
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center)
            )
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

@RequiresApi(Build.VERSION_CODES.S)
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
    val videoId = extractVideoId(youtubeURL)
    var player: com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer? = null
    val playerFragment = YouTubePlayerView(mContext)
    val playerStateListener = object : AbstractYouTubePlayerListener() {
        override fun onReady(youTubePlayer: com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer) {
            super.onReady(youTubePlayer)
            player = youTubePlayer
            youTubePlayer.loadVideo(videoId!!, 0f)
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
    val playerBuilder = IFramePlayerOptions.Builder().apply {
        controls(1)
        fullscreen(0)
        autoplay(1)
        modestBranding(1)
        ccLoadPolicy(1)
        rel(0)
    }
    AndroidView(
        modifier = modifier.background(Color.DarkGray),
        factory = {
            playerFragment.apply {
                enableAutomaticInitialization = false
                initialize(playerStateListener, playerBuilder.build())
            }
        }
    )
    DisposableEffect(key1 = Unit, effect = {
        val activity = (mContext as? ComponentActivity)
        activity ?: return@DisposableEffect onDispose {}
        onDispose {
            playerFragment.removeYouTubePlayerListener(playerStateListener)
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

fun extractVideoId(url: String?): String? {
    return url?.let {
        val regex =
            Regex("""(?:youtube\.com\/(?:[^\/]+\/.+\/|(?:v|e(?:mbed)?)\/|.*[?&]v=)|youtu\.be\/)([^"&?\/\s]{11})""")
        regex.find(it)?.groupValues?.get(1)
    }
}

@OptIn(UnstableApi::class)
@Composable
fun ExoPlayerAudioPlayer(audioURL: String) {
    val context = LocalContext.current
    val exoPlayer = remember { ExoPlayer.Builder(context).build() }
    var isPlayingAudio by remember { mutableStateOf(true) }
    var isLoading by remember { mutableStateOf(true) }
    var currentTime by remember { mutableStateOf(0L) }
    var duration by remember { mutableStateOf(0L) }

    DisposableEffect(exoPlayer) {
        val mediaItem = MediaItem.fromUri(audioURL)
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
        exoPlayer.playWhenReady = true

        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                isLoading = state == Player.STATE_BUFFERING
                if (state == Player.STATE_READY) {
                    duration = exoPlayer.duration
                }
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                isPlayingAudio = exoPlayer.isPlaying
            }

            @Deprecated("Deprecated in Java")
            override fun onPositionDiscontinuity(reason: Int) {
                currentTime = exoPlayer.currentPosition
            }
        }
        exoPlayer.addListener(listener)

        onDispose {
            exoPlayer.removeListener(listener)
            exoPlayer.release()
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
                value = currentTime.toFloat(),
                onValueChange = { exoPlayer.seekTo(it.toLong()) },
                valueRange = 0f..duration.toFloat(),
                modifier = Modifier.fillMaxWidth()
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(formatTime(currentTime))
                Text(formatTime(duration))
            }

            IconButton(onClick = {
                if (isPlayingAudio) exoPlayer.pause() else exoPlayer.play()
            }) {
                Icon(
                    imageVector = if (isPlayingAudio) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = null
                )
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

fun isAudioFile(url: String?): Boolean {
    return url?.matches(
        Regex(".*\\.(mp3|wav|aac|ogg|m4a)\$", RegexOption.IGNORE_CASE)
    ) == true
}

@Composable
actual fun MediaPlayer(modifier: Modifier, url: String) {
    when {
        isAudioFile(url) -> {
            ExoPlayerAudioPlayer(audioURL = url)
        }
    }
}