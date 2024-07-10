import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.SwingPanel
import javafx.application.Platform
import javafx.embed.swing.JFXPanel
import javafx.scene.Scene
import javafx.scene.layout.StackPane
import javafx.scene.media.Media
import javafx.scene.media.MediaPlayer
import java.net.URI
import javax.swing.JPanel

@Composable
fun DesktopAudioPlayer(
    modifier: Modifier,
    audioURL: String,
) {
    val jPanel: JPanel = remember { JPanel() }
    val jfxPanel = remember { JFXPanel() }

    DisposableEffect(audioURL) {
        onDispose {
            jPanel.remove(jfxPanel)
        }
    }

    SwingPanel(
        factory = {
            Platform.runLater {
                val media = Media(URI.create(audioURL).toString())
                val mediaPlayer = MediaPlayer(media)
                mediaPlayer.isAutoPlay = true

                val root = StackPane()

                val scene = Scene(root, 800.0, 600.0)
                jfxPanel.scene = scene

                // Setup audio-specific settings
                mediaPlayer.setOnReady {
                    println("Audio is ready to play")
                }
                mediaPlayer.setOnEndOfMedia {
                    println("Audio has finished playing")
                }
                mediaPlayer.setOnError {
                    println("Error occurred: ${mediaPlayer.error}")
                }

                // Play the audio
                mediaPlayer.play()
            }
            jPanel.add(jfxPanel)
            jPanel
        },
        modifier = modifier,
    )
}
