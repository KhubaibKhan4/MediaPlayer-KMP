package io.github.khubaibkhan4.mediaplayer

import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.viewinterop.UIKitInteropProperties
import androidx.compose.ui.viewinterop.UIKitView
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.readValue
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import platform.AVFoundation.AVPlayer
import platform.AVFoundation.AVPlayerItem
import platform.AVFoundation.AVPlayerItemDidPlayToEndTimeNotification
import platform.AVFoundation.AVPlayerItemFailedToPlayToEndTimeNotification
import platform.AVFoundation.AVPlayerItemStatusFailed
import platform.AVFoundation.AVPlayerItemStatusReadyToPlay
import platform.AVFoundation.AVPlayerTimeControlStatusPaused
import platform.AVFoundation.AVPlayerTimeControlStatusPlaying
import platform.AVFoundation.AVPlayerTimeControlStatusWaitingToPlayAtSpecifiedRate
import platform.AVFoundation.AVURLAsset
import platform.AVFoundation.currentItem
import platform.AVFoundation.pause
import platform.AVFoundation.play
import platform.AVFoundation.timeControlStatus
import platform.AVKit.AVPlayerViewController
import platform.CoreGraphics.CGRectZero
import platform.Foundation.NSBundle
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSOperationQueue
import platform.Foundation.NSURL
import platform.UIKit.NSLayoutConstraint
import platform.UIKit.UIView
import platform.WebKit.WKAudiovisualMediaTypeNone
import platform.WebKit.WKScriptMessage
import platform.WebKit.WKScriptMessageHandlerProtocol
import platform.WebKit.WKUserContentController
import platform.WebKit.WKWebView
import platform.WebKit.WKWebViewConfiguration
import platform.darwin.NSObject

@Composable
actual fun VideoPlayer(
    modifier: Modifier,
    url: String,
    autoPlay: Boolean,
    showControls: Boolean,
    headers: Map<String, String>,
    onPlayerEvent: (PlayerEvent) -> Unit,
) {
    if (isYouTubeUrl(url)) {
        YouTubeIFramePlayer(
            url = url,
            modifier = modifier,
            autoPlay = autoPlay,
            showControls = showControls,
            onPlayerEvent = onPlayerEvent
        )
    } else {
        // AVPlayer detects the format itself, so URLs without a file extension work too.
        AvPlayerView(
            modifier = modifier,
            url = url,
            autoPlay = autoPlay,
            showControls = showControls,
            headers = headers,
            onPlayerEvent = onPlayerEvent
        )
    }
}

@Composable
actual fun MediaPlayer(
    modifier: Modifier,
    url: String,
    headers: Map<String, String>,
    startTime: Color,
    endTime: Color,
    autoPlay: Boolean,
    volumeIconColor: Color,
    playIconColor: Color,
    sliderTrackColor: Color,
    sliderIndicatorColor: Color,
    showControls: Boolean,
    onPlayerEvent: (PlayerEvent) -> Unit,
) {
    AvPlayerView(
        modifier = modifier,
        url = url,
        autoPlay = autoPlay,
        showControls = showControls,
        headers = headers,
        onPlayerEvent = onPlayerEvent
    )
}

@Composable
internal fun AvPlayerView(
    modifier: Modifier = Modifier,
    url: String,
    autoPlay: Boolean,
    showControls: Boolean,
    headers: Map<String, String> = emptyMap(),
    onPlayerEvent: (PlayerEvent) -> Unit = {},
) {
    val currentOnPlayerEvent by rememberUpdatedState(onPlayerEvent)
    val player = remember(url, headers) { createAVPlayer(url, headers) }
    val avPlayerViewController = remember { AVPlayerViewController() }

    if (player == null) {
        LaunchedEffect(url) { currentOnPlayerEvent(PlayerEvent.Error("Invalid media URL: $url")) }
        return
    }

    // Start once per source; doing this in `update` would resume playback on every recomposition.
    LaunchedEffect(player) { if (autoPlay) player.play() }
    AVPlayerEventsEffect(player) { currentOnPlayerEvent(it) }
    DisposableEffect(player) { onDispose { player.pause() } }

    UIKitView(
        factory = {
            val playerContainer = UIView()
            val playerView = avPlayerViewController.view
            playerView.translatesAutoresizingMaskIntoConstraints = false
            playerContainer.addSubview(playerView)
            NSLayoutConstraint.activateConstraints(
                listOf(
                    playerView.leadingAnchor.constraintEqualToAnchor(playerContainer.leadingAnchor),
                    playerView.trailingAnchor.constraintEqualToAnchor(playerContainer.trailingAnchor),
                    playerView.topAnchor.constraintEqualToAnchor(playerContainer.topAnchor),
                    playerView.bottomAnchor.constraintEqualToAnchor(playerContainer.bottomAnchor)
                )
            )
            playerContainer
        },
        modifier = modifier,
        update = {
            avPlayerViewController.player = player
            avPlayerViewController.showsPlaybackControls = showControls
            avPlayerViewController.allowsPictureInPicturePlayback = showControls
        },
        onRelease = {
            avPlayerViewController.player?.pause()
            avPlayerViewController.player = null
        },
        properties = UIKitInteropProperties(
            isInteractive = true,
            isNativeAccessibilityEnabled = true
        )
    )
}

/** Reports [AVPlayer] state through [onPlayerEvent]. AVPlayer exposes most state via KVO, so it is polled here. */
@Composable
private fun AVPlayerEventsEffect(player: AVPlayer, onPlayerEvent: (PlayerEvent) -> Unit) {
    val ended = remember(player) { BooleanArray(1) }

    DisposableEffect(player) {
        val center = NSNotificationCenter.defaultCenter
        val item = player.currentItem
        val endObserver = center.addObserverForName(
            AVPlayerItemDidPlayToEndTimeNotification, item, NSOperationQueue.mainQueue
        ) { _ ->
            ended[0] = true
            onPlayerEvent(PlayerEvent.Ended)
        }
        val failObserver = center.addObserverForName(
            AVPlayerItemFailedToPlayToEndTimeNotification, item, NSOperationQueue.mainQueue
        ) { _ -> onPlayerEvent(PlayerEvent.Error("Playback failed before reaching the end")) }
        onDispose {
            center.removeObserver(endObserver)
            center.removeObserver(failObserver)
        }
    }

    LaunchedEffect(player) {
        var readySent = false
        var failureSent = false
        var lastState: PlayerEvent? = null
        while (isActive) {
            val item = player.currentItem
            when (item?.status) {
                AVPlayerItemStatusReadyToPlay -> if (!readySent) {
                    readySent = true
                    onPlayerEvent(PlayerEvent.Ready)
                }
                AVPlayerItemStatusFailed -> if (!failureSent) {
                    failureSent = true
                    onPlayerEvent(PlayerEvent.Error(item?.error?.localizedDescription ?: "Failed to load media"))
                }
                else -> Unit
            }
            val state = when (player.timeControlStatus) {
                AVPlayerTimeControlStatusPlaying -> PlayerEvent.Playing
                AVPlayerTimeControlStatusWaitingToPlayAtSpecifiedRate -> PlayerEvent.Buffering
                AVPlayerTimeControlStatusPaused -> if (readySent) PlayerEvent.Paused else null
                else -> null
            }
            if (state != null && state != lastState) {
                lastState = state
                if (state == PlayerEvent.Playing) ended[0] = false
                // Reaching the end also pauses the player; Ended was already reported.
                if (!(state == PlayerEvent.Paused && ended[0])) onPlayerEvent(state)
            }
            delay(250)
        }
    }
}

private fun createAVPlayer(url: String, headers: Map<String, String>): AVPlayer? {
    val nsUrl = when {
        url.startsWith("/") -> NSURL.fileURLWithPath(url)
        else -> NSURL.URLWithString(url)
    } ?: return null
    val options: Map<Any?, *>? = if (headers.isEmpty()) null else mapOf("AVURLAssetHTTPHeaderFieldsKey" to headers)
    val asset = AVURLAsset(uRL = nsUrl, options = options)
    return AVPlayer(playerItem = AVPlayerItem(asset = asset))
}

/** Receives `window.webkit.messageHandlers.kmpPlayer.postMessage(...)` calls from the YouTube page. */
internal class YouTubeMessageHandler : NSObject(), WKScriptMessageHandlerProtocol {
    var onMessage: (String) -> Unit = {}

    override fun userContentController(
        userContentController: WKUserContentController,
        didReceiveScriptMessage: WKScriptMessage
    ) {
        (didReceiveScriptMessage.body as? String)?.let(onMessage)
    }
}

private const val YOUTUBE_MESSAGE_HANDLER = "kmpPlayer"

@OptIn(ExperimentalForeignApi::class)
@Composable
internal fun YouTubeIFramePlayer(
    url: String,
    modifier: Modifier,
    autoPlay: Boolean,
    showControls: Boolean,
    onPlayerEvent: (PlayerEvent) -> Unit = {},
) {
    val currentOnPlayerEvent by rememberUpdatedState(onPlayerEvent)
    val videoId = remember(url) { extractYouTubeVideoId(url) }
    if (videoId == null) {
        LaunchedEffect(url) { currentOnPlayerEvent(PlayerEvent.Error("Invalid YouTube URL: $url")) }
        return
    }
    val html = remember(videoId, autoPlay, showControls) {
        youTubeHtml(videoId, autoPlay, showControls, extractYouTubeStartSeconds(url))
    }
    val messageHandler = remember { YouTubeMessageHandler() }
    messageHandler.onMessage = { message -> youTubeBridgeEvent(message)?.let(currentOnPlayerEvent) }

    key(html) {
        UIKitView(
            factory = {
                // WKWebView copies its configuration, so it must be fully set up before the view is created.
                val configuration = WKWebViewConfiguration().apply {
                    allowsInlineMediaPlayback = true
                    mediaTypesRequiringUserActionForPlayback = WKAudiovisualMediaTypeNone
                    userContentController.addScriptMessageHandler(messageHandler, YOUTUBE_MESSAGE_HANDLER)
                }
                WKWebView(frame = CGRectZero.readValue(), configuration = configuration).apply {
                    scrollView.scrollEnabled = false
                    loadHTMLString(html, baseURL = youTubeBaseUrl())
                }
            },
            modifier = modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f),
            update = { },
            onRelease = { webView ->
                webView.configuration.userContentController.removeScriptMessageHandlerForName(YOUTUBE_MESSAGE_HANDLER)
                webView.stopLoading()
                webView.loadHTMLString("", baseURL = null)
            },
            properties = UIKitInteropProperties(
                isInteractive = true,
                isNativeAccessibilityEnabled = true
            )
        )
    }
}

/**
 * YouTube rejects embeds without an HTTP referrer (errors 152/153). For apps it asks for
 * `https://<bundle id>`, which also gives the page a real origin for the IFrame API.
 */
private fun youTubeBaseUrl(): NSURL? {
    val bundleId = NSBundle.mainBundle.bundleIdentifier?.lowercase() ?: "localhost"
    return NSURL.URLWithString("https://$bundleId")
}

private fun youTubeHtml(videoId: String, autoPlay: Boolean, showControls: Boolean, startSeconds: Int): String {
    val autoplay = if (autoPlay) 1 else 0
    val controls = if (showControls) 1 else 0
    return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta name="viewport" content="width=device-width, initial-scale=1, user-scalable=no">
            <style>
                html, body { margin: 0; padding: 0; width: 100%; height: 100%; overflow: hidden; background-color: black; }
                #player { position: absolute; top: 0; left: 0; width: 100%; height: 100%; }
            </style>
        </head>
        <body>
            <div id="player"></div>
            <script>
                function post(message) {
                    try { window.webkit.messageHandlers.$YOUTUBE_MESSAGE_HANDLER.postMessage(message); } catch (e) {}
                }
                function onYouTubeIframeAPIReady() {
                    new YT.Player('player', {
                        width: '100%',
                        height: '100%',
                        videoId: '$videoId',
                        playerVars: {
                            autoplay: $autoplay,
                            controls: $controls,
                            start: $startSeconds,
                            playsinline: 1,
                            rel: 0,
                            modestbranding: 1,
                            iv_load_policy: 3,
                            origin: window.location.origin
                        },
                        events: {
                            onReady: function (event) {
                                post('ready');
                                if ($autoplay) event.target.playVideo();
                            },
                            onStateChange: function (event) { post('state:' + event.data); },
                            onError: function (event) { post('error:' + event.data); }
                        }
                    });
                }
            </script>
            <script src="https://www.youtube.com/iframe_api"></script>
        </body>
        </html>
    """.trimIndent()
}
