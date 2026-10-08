package io.github.khubaibkhan4.mediaplayer

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.VolumeDown
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
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import javafx.scene.media.Media
import javafx.scene.media.MediaPlayer
import javafx.util.Duration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean
import javax.swing.SwingUtilities

@Composable
internal fun DesktopAudioPlayer(
    modifier: Modifier = Modifier,
    audioURL: String,
    headers: Map<String, String>,
    startTime: Color,
    endTime: Color,
    autoPlay: Boolean,
    volumeIconColor: Color,
    playIconColor: Color,
    sliderTrackColor: Color,
    sliderIndicatorColor: Color,
    showControls: Boolean,
    onPlayerEvent: (PlayerEvent) -> Unit = {},
) {
    val currentOnPlayerEvent by rememberUpdatedState(onPlayerEvent)
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    var isPlaying by remember { mutableStateOf(false) }
    var currentTime by remember { mutableStateOf(0.0) }
    var duration by remember { mutableStateOf(0.0) }
    var isLoaded by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var volume by remember { mutableStateOf(0.5) }

    DisposableEffect(audioURL, headers) {
        isLoaded = false
        isPlaying = false
        errorMessage = null
        currentTime = 0.0
        duration = 0.0

        // JavaFX invokes these callbacks on its own thread; Compose state belongs to the Swing thread.
        fun onUi(block: () -> Unit) = SwingUtilities.invokeLater(block)
        fun fail(message: String) = onUi {
            errorMessage = message
            isLoaded = false
            currentOnPlayerEvent(PlayerEvent.Error(message))
        }

        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        val disposed = AtomicBoolean(false)
        var created: MediaPlayer? = null

        scope.launch {
            val source = try {
                resolveMediaSource(audioURL, headers)
            } catch (e: Exception) {
                fail(e.message ?: "Failed to load $audioURL")
                return@launch
            }
            runOnFxThread {
                if (disposed.get()) return@runOnFxThread
                val player = try {
                    MediaPlayer(Media(source))
                } catch (e: Exception) {
                    fail(e.message ?: "Unsupported media: $audioURL")
                    return@runOnFxThread
                }
                created = player
                player.apply {
                    volumeProperty().value = volume
                    setOnReady {
                        val total = media.duration.toSeconds()
                        onUi {
                            duration = if (total.isFinite()) total else 0.0
                            isLoaded = true
                            currentOnPlayerEvent(PlayerEvent.Ready)
                        }
                        if (autoPlay) play()
                    }
                    setOnPlaying { onUi { isPlaying = true; currentOnPlayerEvent(PlayerEvent.Playing) } }
                    setOnPaused { onUi { isPlaying = false; currentOnPlayerEvent(PlayerEvent.Paused) } }
                    setOnStalled { onUi { currentOnPlayerEvent(PlayerEvent.Buffering) } }
                    setOnEndOfMedia {
                        stop()
                        onUi { isPlaying = false; currentOnPlayerEvent(PlayerEvent.Ended) }
                    }
                    setOnError { fail(error?.message ?: "Playback error") }
                    currentTimeProperty().addListener { _, _, newValue ->
                        val seconds = newValue.toSeconds()
                        onUi { currentTime = seconds }
                    }
                }
                onUi { if (!disposed.get()) mediaPlayer = player }
            }
        }

        onDispose {
            disposed.set(true)
            scope.cancel()
            mediaPlayer = null
            runOnFxThread { created?.dispose() }
        }
    }

    Column(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val error = errorMessage
        when {
            error != null -> Text(error, color = endTime)
            !isLoaded -> CircularProgressIndicator()
            showControls -> Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(onClick = {
                    mediaPlayer?.let { if (isPlaying) it.pause() else it.play() }
                }) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        tint = playIconColor
                    )
                }

                Text(
                    formatTime(currentTime),
                    modifier = Modifier.padding(start = 8.dp),
                    color = startTime
                )
                Slider(
                    value = currentTime.toFloat().coerceIn(0f, duration.toFloat()),
                    onValueChange = {
                        currentTime = it.toDouble()
                        mediaPlayer?.seek(Duration.seconds(it.toDouble()))
                    },
                    valueRange = 0f..duration.toFloat(),
                    modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
                    colors = SliderDefaults.colors(
                        thumbColor = sliderIndicatorColor,
                        activeTrackColor = sliderTrackColor
                    )
                )
                Text(
                    formatTime(duration),
                    modifier = Modifier.padding(end = 8.dp),
                    color = endTime
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    IconButton(onClick = {
                        volume = (volume + 0.1).coerceIn(0.0, 1.0)
                        mediaPlayer?.volume = volume
                    }) {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = "Increase Volume",
                            tint = volumeIconColor
                        )
                    }
                    IconButton(onClick = {
                        volume = (volume - 0.1).coerceIn(0.0, 1.0)
                        mediaPlayer?.volume = volume
                    }) {
                        Icon(
                            imageVector = Icons.Default.VolumeDown,
                            contentDescription = "Decrease Volume",
                            tint = volumeIconColor
                        )
                    }
                    Text(
                        "${(volume * 100).toInt()}%",
                        modifier = Modifier.padding(4.dp),
                        color = endTime
                    )
                }
            }
            else -> Text(
                formatTime(currentTime) + " / " + formatTime(duration),
                color = endTime
            )
        }
    }
}

internal fun formatTime(seconds: Double): String {
    val totalSeconds = seconds.toLong()
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val secs = totalSeconds % 60

    return if (hours > 0) {
        String.format("%02d:%02d:%02d", hours, minutes, secs)
    } else {
        String.format("%02d:%02d", minutes, secs)
    }
}
