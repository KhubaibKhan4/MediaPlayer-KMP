package io.github.khubaibkhan4.mediaplayer

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class MediaUrlsTest {
    @Test
    fun extractsYouTubeIdsFromCommonUrlShapes() {
        val id = "AD2nEllUMJw"
        listOf(
            "https://www.youtube.com/watch?v=$id",
            "https://www.youtube.com/watch?feature=share&v=$id&t=42",
            "https://youtu.be/$id",
            "https://youtu.be/$id?si=abc",
            "https://www.youtube.com/embed/$id",
            "https://www.youtube.com/shorts/$id",
            "https://m.youtube.com/watch?v=$id",
        ).forEach { assertEquals(id, extractYouTubeVideoId(it), it) }
        assertNull(extractYouTubeVideoId("https://www.youtube.com/"))
    }

    @Test
    fun readsYouTubeStartTime() {
        assertEquals(42, extractYouTubeStartSeconds("https://youtu.be/AD2nEllUMJw?t=42"))
        assertEquals(0, extractYouTubeStartSeconds("https://youtu.be/AD2nEllUMJw"))
    }

    @Test
    fun detectsVideoFilesIgnoringQueryStrings() {
        assertTrue(isVideoFile("https://cdn.example.com/clip.mp4"))
        assertTrue(isVideoFile("https://cdn.example.com/clip.MP4?token=abc.def"))
        assertTrue(isVideoFile("https://cdn.example.com/live/index.m3u8#t=10"))
        assertTrue(isVideoFile("""D:\videos\clip.mkv"""))
        assertFalse(isVideoFile("https://cdn.example.com/watch"))
        assertFalse(isVideoFile(""))
    }

    @Test
    fun detectsAudio() {
        assertTrue(isAudioFile("https://cdn.example.com/song.mp3?sig=1"))
        assertTrue(isAudioFile("""D:\2025031215.mp3"""))
        assertTrue(isAudioFile("https://radio.example.com/live"))
        // A video file must not be treated as audio just because the host name says "stream".
        assertFalse(isAudioFile("https://stream.example.com/movie.mp4"))
        assertFalse(isAudioFile(null))
    }

    @Test
    fun mapsYouTubeBridgeMessages() {
        assertEquals(PlayerEvent.Ready, youTubeBridgeEvent("ready"))
        assertEquals(PlayerEvent.Playing, youTubeBridgeEvent("state:1"))
        assertEquals(PlayerEvent.Ended, youTubeBridgeEvent("state:0"))
        assertNull(youTubeBridgeEvent("state:-1"))
        assertIs<PlayerEvent.Error>(youTubeBridgeEvent("error:150"))
    }
}
