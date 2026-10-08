package io.github.khubaibkhan4.mediaplayer

/**
 * Playback lifecycle events reported through the `onPlayerEvent` callback of
 * [VideoPlayer], [MediaPlayer] and [ReelsView].
 */
sealed interface PlayerEvent {
    /** The media is loaded and can start playing. */
    data object Ready : PlayerEvent

    /** Playback started or resumed. */
    data object Playing : PlayerEvent

    /** Playback was paused. */
    data object Paused : PlayerEvent

    /** Playback is stalled waiting for data. */
    data object Buffering : PlayerEvent

    /** Playback reached the end of the media. */
    data object Ended : PlayerEvent

    /** The media could not be loaded or played. */
    data class Error(val message: String) : PlayerEvent
}
