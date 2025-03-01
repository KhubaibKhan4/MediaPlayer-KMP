import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.viewinterop.UIKitInteropProperties
import androidx.compose.ui.viewinterop.UIKitView
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import platform.AVFoundation.AVPlayer
import platform.AVFoundation.AVPlayerItem
import platform.AVFoundation.AVURLAsset
import platform.AVFoundation.pause
import platform.AVFoundation.play
import platform.AVKit.AVPlayerViewController
import platform.Foundation.NSURL
import platform.UIKit.NSLayoutConstraint
import platform.UIKit.UIView
import platform.WebKit.WKAudiovisualMediaTypes
import platform.WebKit.WKWebView
import kotlin.time.Duration.Companion.seconds

@Composable
actual fun VideoPlayer(
    modifier: Modifier,
    url: String,
    autoPlay: Boolean,
    showControls: Boolean
) {
    if (url.contains("youtube.com") || url.contains("youtu.be")) {
        YouTubeIFramePlayer(
            url = url,
            modifier = modifier,
            autoPlay = autoPlay,
            showControls = showControls
        )
    } else if (isVideoFile(url) || isAudioFile(url)) {
        AvPlayerView(
            modifier = modifier,
            url = url,
            autoPlay = autoPlay,
            showControls = showControls)
    } else {
        Text("Unsupported media format", modifier = modifier)
    }
}

@Composable
fun AvPlayerView(
    modifier: Modifier = Modifier,
    url: String,
    autoPlay: Boolean,
    showControls: Boolean
) {
    val validUrl = remember(url) { NSURL.URLWithString(url) }

    val player = remember {
        validUrl?.let { AVPlayer(uRL = it) }
    }

    val avPlayerViewController = remember { AVPlayerViewController() }

    avPlayerViewController.player = player
    avPlayerViewController.showsPlaybackControls = showControls
    avPlayerViewController.allowsPictureInPicturePlayback = showControls

    UIKitView(
        factory = {
            val playerContainer = UIView()

            avPlayerViewController.view.translatesAutoresizingMaskIntoConstraints = false
            playerContainer.addSubview(avPlayerViewController.view)

            NSLayoutConstraint.activateConstraints(
                listOf(
                    avPlayerViewController.view.leadingAnchor.constraintEqualToAnchor(playerContainer.leadingAnchor),
                    avPlayerViewController.view.trailingAnchor.constraintEqualToAnchor(playerContainer.trailingAnchor),
                    avPlayerViewController.view.topAnchor.constraintEqualToAnchor(playerContainer.topAnchor),
                    avPlayerViewController.view.bottomAnchor.constraintEqualToAnchor(playerContainer.bottomAnchor)
                )
            )

            playerContainer
        },
        modifier = modifier,
        update = { _ ->
            if (autoPlay) {
                player?.play()
            } else {
                player?.pause()
            }
        },
        onRelease = {
            player?.pause()
        },
        properties = UIKitInteropProperties(
            isInteractive = true,
            isNativeAccessibilityEnabled = true
        )
    )
}

fun isAudioFile(url: String?): Boolean {
    return url?.matches(
        Regex(".*\\.(mp3|wav|aac|ogg|m4a|m3u|pls|m3u8)\$", RegexOption.IGNORE_CASE)
    ) == true || url?.matches(
        Regex(".*(radio|stream|icecast|shoutcast|audio|listen).*", RegexOption.IGNORE_CASE)
    ) == true
}

fun isVideoFile(url: String?): Boolean {
    return url?.matches(
        Regex(".*\\.(mp4|mkv|webm|avi|mov|wmv|flv|m4v|3gp|mpeg|m3u8|ts|dash)\$", RegexOption.IGNORE_CASE)
    ) == true || url?.matches(
        Regex(".*(stream|video|live).*", RegexOption.IGNORE_CASE)
    ) == true
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
) {
    val player = remember {
        createAVPlayerWithHeaders(url, headers)
    }
    val avPlayerViewController = remember { AVPlayerViewController() }

    // Configure the player and controls based on `showControls`
    avPlayerViewController.player = player
    avPlayerViewController.showsPlaybackControls = showControls
    avPlayerViewController.allowsPictureInPicturePlayback = showControls

    UIKitView(
        factory = {
            val playerContainer = UIView()
            playerContainer.addSubview(avPlayerViewController.view)
            playerContainer
        },
        modifier = modifier,
        update = { _ ->
            if (autoPlay) {
                player?.play()
            } else {
                player?.pause()
            }
        },
        onRelease = {
            player?.pause()
        },
        properties = UIKitInteropProperties(
            isInteractive = true,
            isNativeAccessibilityEnabled = true
        )
    )
}

/**
 * Helper function to create AVPlayer with headers
 */
private fun createAVPlayerWithHeaders(url: String, headers: Map<String, String>): AVPlayer? {
    val url = NSURL.URLWithString(url) ?: return null

    val asset = AVURLAsset(uRL = url, options = mapOf("AVURLAssetHTTPHeaderFieldsKey" to headers))
    val item = AVPlayerItem(asset = asset)
    return AVPlayer(playerItem = item)
}

@Composable
fun YouTubeIFramePlayer(
    url: String,
    modifier: Modifier,
    autoPlay: Boolean,
    showControls: Boolean
) {
    val scope = rememberCoroutineScope()
    val videoId = remember(url) {
        url.substringAfter("v=").substringBefore("&").ifEmpty { url.substringAfterLast("/") }
    }
    val isAutoPlay = if (autoPlay) 1 else 0
    val controls = if (showControls) 1 else 0

    val thumbnailUrl = "https://img.youtube.com/vi/$videoId/hqdefault.jpg"

    val htmlContent = """
        <html>
        <head>
            <meta name="viewport" content="width=device-width, initial-scale=1, user-scalable=no">
            <style>
                body, html {
                    margin: 0;
                    padding: 0;
                    height: 100%;
                    overflow: hidden;
                    background-color: black;
                }
                .video-container {
                    position: relative;
                    width: 100%;
                    height: 100%;
                }
                .video-container iframe, .video-container img {
                    position: absolute;
                    top: 0;
                    left: 0;
                    width: 100%;
                    height: 100%;
                    border: none;
                }
                #thumbnail {
                    display: block;
                    background-color: black;
                }
                #youtubePlayer {
                    display: none;
                }
            </style>
            <script>
                function onYouTubeIframeAPIReady() {
                    var player = new YT.Player('youtubePlayer', {
                        events: {
                            'onStateChange': function(event) {
                                if (event.data == YT.PlayerState.PLAYING) {
                                    document.getElementById("thumbnail").style.display = "none";
                                    document.getElementById("youtubePlayer").style.display = "block";
                                }
                            }
                        }
                    });
                }
            </script>
            <script src="https://www.youtube.com/iframe_api"></script>
        </head>
        <body>
            <div class="video-container">
                <img id="thumbnail" src="$thumbnailUrl" alt="Video thumbnail" />
                <iframe 
                    id="youtubePlayer"
                    src="https://www.youtube.com/embed/$videoId?autoplay=$isAutoPlay&controls=$controls&playsinline=1&rel=0&modestbranding=1&fs=0&iv_load_policy=3&enablejsapi=1" 
                    allow="autoplay; encrypted-media;"
                    frameborder="0">
                </iframe>
            </div>
        </body>
        </html>
    """.trimIndent()

    UIKitView<WKWebView>(
        factory = {
            val webView = WKWebView().apply {
                scrollView.scrollEnabled = false
                configuration.allowsInlineMediaPlayback = true
                configuration.mediaTypesRequiringUserActionForPlayback = WKAudiovisualMediaTypes.MAX_VALUE
                loadHTMLString(htmlContent, baseURL = null)
            }
            webView
        },
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f),
        update = { view ->
            view.configuration.allowsInlineMediaPlayback = true
           scope.launch {
                view.reload()
            }
        }
    )
}