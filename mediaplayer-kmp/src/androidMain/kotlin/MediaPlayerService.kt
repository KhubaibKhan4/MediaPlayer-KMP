import android.content.Intent
import android.os.Bundle
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.CommandButton
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.session.SessionCommand
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jetbrains.kotlinx.multiplatform.library.template.R

@androidx.media3.common.util.UnstableApi
class PlaybackService : MediaSessionService() {
    private var mediaSession: MediaSession? = null
    private lateinit var player: ExoPlayer

    private val customCommandSeekBackward = SessionCommand("seekBackward", Bundle())
    private val customCommandSeekForward = SessionCommand("seekForward", Bundle())

    override fun onCreate() {
        super.onCreate()
        player = ExoPlayer.Builder(this).build()
        mediaSession = MediaSession.Builder(this, player).build()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? =
        mediaSession

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        if (player.playWhenReady) {
            player.pause()
        }
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
    }

    fun onConnect(
        session: MediaSession,
        controller: MediaSession.ControllerInfo
    ): MediaSession.ConnectionResult {
        val sessionCommands =
            MediaSession.ConnectionResult.DEFAULT_SESSION_AND_LIBRARY_COMMANDS.buildUpon()
                .add(customCommandSeekBackward)
                .add(customCommandSeekForward)
                .build()

        return if (session.isMediaNotificationController(controller)) {
            val playerCommands = MediaSession.ConnectionResult.DEFAULT_PLAYER_COMMANDS.buildUpon()
                .remove(Player.COMMAND_SEEK_TO_PREVIOUS)
                .remove(Player.COMMAND_SEEK_TO_PREVIOUS_MEDIA_ITEM)
                .remove(Player.COMMAND_SEEK_TO_NEXT)
                .remove(Player.COMMAND_SEEK_TO_NEXT_MEDIA_ITEM)
                .build()
            MediaSession.ConnectionResult.AcceptedResultBuilder(session)
                .setCustomLayout(
                    listOf(
                        createSeekBackwardButton(customCommandSeekBackward),
                        createSeekForwardButton(customCommandSeekForward)
                    )
                )
                .setAvailablePlayerCommands(playerCommands)
                .setAvailableSessionCommands(sessionCommands)
                .build()
        } else if (session.isAutoCompanionController(controller)) {
            MediaSession.ConnectionResult.AcceptedResultBuilder(session)
                .setAvailableSessionCommands(sessionCommands)
                .build()
        } else {
            MediaSession.ConnectionResult.AcceptedResultBuilder(session).build()
        }
    }

    private fun createSeekBackwardButton(command: SessionCommand): CommandButton {
        return CommandButton.Builder()
            .setSessionCommand(command)
            .setDisplayName("Seek Backward")
            .setIconResId(R.drawable.baseline_arrow_back_24)
            .build()
    }

    private fun createSeekForwardButton(command: SessionCommand): CommandButton {
        return CommandButton.Builder()
            .setSessionCommand(command)
            .setDisplayName("Seek Forward")
            .setIconResId(R.drawable.baseline_fast_forward_24)
            .build()
    }

    private fun serializeMediaItems(mediaItems: List<MediaItem>): String {
        val serializableItems = mediaItems.map { SerializableMediaItem.fromMediaItem(it) }
        return Json.encodeToString(serializableItems)
    }

    private fun deserializeMediaItems(json: String): List<MediaItem> {
        val serializableItems = Json.decodeFromString<List<SerializableMediaItem>>(json)
        return serializableItems.map { SerializableMediaItem.toMediaItem(it) }
    }

    private fun restorePlaylist(): MediaSession.MediaItemsWithStartPosition {
        val sharedPreferences = getSharedPreferences("PlaybackPreferences", MODE_PRIVATE)
        val mediaItemsJson = sharedPreferences.getString("savedPlaylist", null)
        val startPosition = sharedPreferences.getLong("startPosition", 0L)
        val startIndex = sharedPreferences.getInt("startIndex", 0)

        val mediaItems = if (mediaItemsJson != null) {
            deserializeMediaItems(mediaItemsJson)
        } else {
            emptyList()
        }

        return MediaSession.MediaItemsWithStartPosition(mediaItems, startIndex, startPosition)
    }

}

@Serializable
data class SerializableMediaItem(
    val uri: String,
    val title: String,
    val artist: String
) {
    companion object {
        fun fromMediaItem(mediaItem: MediaItem): SerializableMediaItem {
            return SerializableMediaItem(
                uri = mediaItem.localConfiguration?.uri.toString(),
                title = mediaItem.mediaMetadata.title.toString(),
                artist = mediaItem.mediaMetadata.artist.toString()
            )
        }

        fun toMediaItem(serializableMediaItem: SerializableMediaItem): MediaItem {
            return MediaItem.Builder()
                .setUri(serializableMediaItem.uri)
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle(serializableMediaItem.title)
                        .setArtist(serializableMediaItem.artist)
                        .build()
                )
                .build()
        }
    }
}