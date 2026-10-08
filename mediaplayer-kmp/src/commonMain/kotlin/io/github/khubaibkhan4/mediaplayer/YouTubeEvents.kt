package io.github.khubaibkhan4.mediaplayer

/**
 * Maps the compact messages produced by the web/iOS YouTube bridges
 * (`ready`, `state:<YT.PlayerState>`, `error:<code>`) to a [PlayerEvent].
 */
internal fun youTubeBridgeEvent(message: String?): PlayerEvent? = when {
    message == null -> null
    message == "ready" -> PlayerEvent.Ready
    message.startsWith("state:") -> when (message.removePrefix("state:")) {
        "0" -> PlayerEvent.Ended
        "1" -> PlayerEvent.Playing
        "2" -> PlayerEvent.Paused
        "3" -> PlayerEvent.Buffering
        else -> null
    }
    message.startsWith("error:") -> PlayerEvent.Error(youTubeErrorMessage(message.removePrefix("error:")))
    else -> null
}

private fun youTubeErrorMessage(code: String): String = when (code) {
    "2" -> "YouTube error 2: invalid video id"
    "5" -> "YouTube error 5: the video cannot be played in an HTML5 player"
    "100" -> "YouTube error 100: video not found or private"
    "101", "150" -> "YouTube error $code: the owner does not allow embedding"
    "152", "153" -> "YouTube error $code: embedder identity missing (no HTTP referrer)"
    else -> "YouTube error $code"
}
