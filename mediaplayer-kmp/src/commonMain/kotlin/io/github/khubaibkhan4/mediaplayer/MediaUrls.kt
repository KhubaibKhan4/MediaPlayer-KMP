package io.github.khubaibkhan4.mediaplayer

private val videoExtension = Regex(
    """.*\.(mp4|mkv|webm|avi|mov|wmv|flv|m4v|3gp|mpeg|mpg|ogv|ogg|mts|m2ts|vob|f4v|mxf|rm|rmvb|asf|divx|mpe|mpv|ts|m3u8|mpd|dash)$""",
    RegexOption.IGNORE_CASE
)
private val audioExtension = Regex(""".*\.(mp3|wav|aac|ogg|oga|opus|flac|m4a|m3u|pls|m3u8)$""", RegexOption.IGNORE_CASE)
private val audioKeyword = Regex("radio|stream|icecast|shoutcast|audio|listen", RegexOption.IGNORE_CASE)
private val youTubeId = Regex("""(?:youtube\.com/(?:[^/]+/.+/|(?:v|e(?:mbed)?|shorts|live)/|.*[?&]v=)|youtu\.be/)([^"&?/\s]{11})""")
private val youTubeStart = Regex("""[?&#](?:t|start)=(\d+)""")

/** Strips the query string and fragment so `video.mp4?token=…` is still detected as a file. */
private fun String.withoutQuery(): String = substringBefore('?').substringBefore('#')

internal fun isYouTubeUrl(url: String): Boolean =
    url.contains("youtube.com", ignoreCase = true) || url.contains("youtu.be", ignoreCase = true)

internal fun extractYouTubeVideoId(url: String?): String? =
    url?.let { youTubeId.find(it)?.groupValues?.get(1) }

/** Start offset in seconds from a `t=` / `start=` parameter, or 0. */
internal fun extractYouTubeStartSeconds(url: String?): Int =
    url?.let { youTubeStart.find(it)?.groupValues?.get(1)?.toIntOrNull() } ?: 0

internal fun isVideoFile(url: String?): Boolean =
    !url.isNullOrBlank() && url.withoutQuery().matches(videoExtension)

internal fun isAudioFile(url: String?): Boolean {
    if (url.isNullOrBlank()) return false
    val path = url.withoutQuery()
    if (path.matches(audioExtension)) return true
    if (path.matches(videoExtension)) return false
    return audioKeyword.containsMatchIn(url)
}
