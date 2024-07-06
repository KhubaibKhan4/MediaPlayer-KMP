import android.os.Build
import android.widget.MediaController
import android.widget.VideoView
import androidx.activity.ComponentActivity
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.pierfrancescosoffritti.androidyoutubeplayer.core.customui.DefaultPlayerUiController
import com.pierfrancescosoffritti.androidyoutubeplayer.core.customui.PlayerUiController
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.options.IFramePlayerOptions
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView

@RequiresApi(Build.VERSION_CODES.S)
@Composable
actual fun VideoPlayer(
    modifier: Modifier,
    url: String?,
    thumbnail: String?,
    onPlayClick: () -> Unit
) {
    var isPlaying by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }

    if (isPlaying) {
        when {
            url?.contains("youtube.com") == true || url?.contains("youtu.be") == true -> {
                YoutubeVideoPlayer(youtubeURL = url)
            }
            isVideoFile(url) -> {
                url?.let { AndroidVideoPlayer(videoURL = it) }
            }
        }
    } else {
        Box(modifier = modifier.fillMaxWidth()) {
            coil3.compose.AsyncImage(
                modifier = modifier,
                model = thumbnail,
                contentDescription = "Thumbnail Image",
                contentScale = ContentScale.Crop,

                onError = {
                    isLoading = false
                },
                onLoading = {
                    isLoading = true
                },
                onSuccess = {
                    isLoading = false
                },

                )
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(45.dp)
                        .align(Alignment.Center)
                        .clickable {
                            onPlayClick()
                            isPlaying = !isPlaying
                        }
                )
            }
        }
    }
}

@Composable
fun AndroidVideoPlayer(videoURL: String) {
    val context = LocalContext.current
    val videoView = remember { VideoView(context) }
    val mediaController = remember { MediaController(context) }

    AndroidView(
        factory = {
            videoView.apply {
                setVideoPath(videoURL)
                setMediaController(mediaController)
                setOnPreparedListener {
                    start()
                }
            }
        },
        update = {
            it.setVideoPath(videoURL)
        },
        modifier = Modifier.fillMaxSize()
    )
}

fun isVideoFile(url: String?): Boolean {
    return url?.matches(Regex(".*\\.(mp4|mkv|webm|avi|mov|wmv|flv|m4v|3gp|mpeg)\$", RegexOption.IGNORE_CASE)) == true
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
        val videoId = splitLinkForVideoId(youtubeURL)
        var player: com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer? =
            null
        val playerFragment = YouTubePlayerView(mContext)
        val playerStateListener = object : AbstractYouTubePlayerListener() {
            override fun onReady(youTubePlayer: com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer) {
                super.onReady(youTubePlayer)
                player = youTubePlayer
                youTubePlayer.loadVideo(videoId, 0f)
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

    fun splitLinkForVideoId(
        url: String?,
    ): String {
        return (url!!.split("="))[1]
    }