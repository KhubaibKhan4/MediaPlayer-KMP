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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.SwingPanel
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import javafx.application.Platform
import javafx.embed.swing.JFXPanel
import javafx.scene.Scene
import javafx.scene.layout.StackPane
import javafx.scene.media.Media
import javafx.scene.media.MediaPlayer
import javafx.util.Duration
import java.net.URI
import javax.swing.JPanel

@Composable
fun DesktopAudioPlayer(
    modifier: Modifier = Modifier,
    audioURL: String,
    startTime: Color,
    endTime: Color,
    volumeIconColor: Color,
    playIconColor: Color,
    sliderTrackColor: Color,
    sliderIndicatorColor: Color
) {
    val mediaPlayerState = remember { mutableStateOf<MediaPlayer?>(null) }
    val isPlaying = remember { mutableStateOf(false) }
    val currentTime = remember { mutableStateOf(0.0) }
    val duration = remember { mutableStateOf(0.0) }
    val isLoaded = remember { mutableStateOf(false) }
    val volume = remember { mutableStateOf(0.5) }

    DisposableEffect(audioURL) {
        Platform.startup {
            val media = Media(audioURL)
            val mediaPlayer = MediaPlayer(media).apply {
                setOnReady {
                    duration.value = media.duration.toSeconds()
                    isLoaded.value = true
                }
                currentTimeProperty().addListener { _, _, newValue ->
                    currentTime.value = newValue.toSeconds()
                }
                setOnEndOfMedia {
                    isPlaying.value = false
                }
                volumeProperty().value = volume.value
            }
            mediaPlayerState.value = mediaPlayer
        }

        onDispose {
            mediaPlayerState.value?.dispose()
            mediaPlayerState.value = null
        }
    }

    Column(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (!isLoaded.value) {
            CircularProgressIndicator()
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(onClick = {
                    mediaPlayerState.value?.let {
                        if (isPlaying.value) {
                            it.pause()
                        } else {
                            it.play()
                        }
                        isPlaying.value = !isPlaying.value
                    }
                }) {
                    Icon(
                        imageVector = if (isPlaying.value) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = playIconColor
                    )
                }

                Text(
                    formatTime(currentTime.value),
                    modifier = Modifier.padding(start = 8.dp),
                    color = startTime
                )
                Slider(
                    value = currentTime.value.toFloat(),
                    onValueChange = {
                        mediaPlayerState.value?.seek(Duration.seconds(it.toDouble()))
                    },
                    valueRange = 0f..duration.value.toFloat(),
                    modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
                    colors = SliderDefaults.colors(
                        thumbColor = sliderIndicatorColor,
                        activeTrackColor = sliderTrackColor
                    )
                )
                Text(
                    formatTime(duration.value),
                    modifier = Modifier.padding(end = 8.dp),
                    color = endTime
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    IconButton(onClick = {
                        volume.value = (volume.value + 0.1).coerceIn(0.0, 1.0)
                        mediaPlayerState.value?.volume = volume.value
                    }) {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = "Increase Volume",
                            tint = volumeIconColor
                        )
                    }
                    IconButton(onClick = {
                        volume.value = (volume.value - 0.1).coerceIn(0.0, 1.0)
                        mediaPlayerState.value?.volume = volume.value
                    }) {
                        Icon(
                            imageVector = Icons.Default.VolumeDown,
                            contentDescription = "Decrease Volume",
                            tint = volumeIconColor
                        )
                    }
                    Text(
                        "${(volume.value * 100).toInt()}%",
                        modifier = Modifier.padding(4.dp),
                        color = endTime
                    )
                }
            }
        }
    }
}

fun formatTime(seconds: Double): String {
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
