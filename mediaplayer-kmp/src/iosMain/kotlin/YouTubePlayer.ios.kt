import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.interop.UIKitView
import androidx.compose.ui.viewinterop.UIKitInteropProperties
import androidx.compose.ui.viewinterop.UIKitView
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.CValue
import kotlinx.cinterop.CValues
import kotlinx.cinterop.DoubleVar
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.cValuesOf
import kotlinx.cinterop.convert
import kotlinx.cinterop.memScoped
import platform.AVFoundation.AVPlayer
import platform.AVFoundation.AVPlayerItem
import platform.AVFoundation.AVPlayerLayer
import platform.AVFoundation.AVURLAsset
import platform.AVFoundation.pause
import platform.AVFoundation.play
import platform.AVKit.AVPlayerViewController
import platform.CoreGraphics.CGRect
import platform.CoreGraphics.CGSize
import platform.Foundation.NSIndexPath
import platform.Foundation.NSURL
import platform.QuartzCore.CATransaction
import platform.QuartzCore.kCATransactionDisableActions
import platform.UIKit.UICollectionView
import platform.UIKit.UICollectionViewCell
import platform.UIKit.UICollectionViewDataSourceProtocol
import platform.UIKit.UICollectionViewFlowLayout
import platform.UIKit.UICollectionViewScrollDirection
import platform.*
import platform.CoreGraphics.CGPoint
import platform.CoreGraphics.CGRectGetMidX
import platform.CoreGraphics.CGRectGetMidY
import platform.Foundation.NSCoder
import platform.UIKit.UICollectionViewDelegateProtocol
import platform.UIKit.UIColor.Companion.blackColor
import platform.UIKit.UINib
import platform.UIKit.UIScrollView
import platform.UIKit.UIView
import platform.UIKit.UIViewController
import platform.UIKit.row
import platform.WebKit.WKWebView
import platform.darwin.NSInteger
import platform.darwin.NSObject

@Composable
actual fun VideoPlayer(
    modifier: Modifier,
    url: String,
    autoPlay: Boolean
) {
    if (url.contains("youtube.com") || url.contains("youtu.be")) {
        YouTubeIFramePlayer(url = url, modifier = modifier,autoPlay = autoPlay)
    } else if (isVideoFile(url) || isAudioFile(url)) {
        AvPlayerView(modifier,url,autoPlay)
    } else {
        Text("Unsupported media format", modifier = modifier)
    }
}

@Composable
fun AvPlayerView(modifier: Modifier = Modifier, url: String, autoPlay: Boolean) {
    val player = remember {
        NSURL.URLWithString(url.toString())?.let { AVPlayer(uRL = it) }
    }
    val playerLayer = remember { AVPlayerLayer() }
    val avPlayerViewController = remember { AVPlayerViewController() }
    avPlayerViewController.player = player
    avPlayerViewController.showsPlaybackControls = true
    avPlayerViewController.allowsPictureInPicturePlayback = true

    playerLayer.player = player
    UIKitView(
        factory = {
            val playerContainer = UIView()
            playerContainer.addSubview(avPlayerViewController.view)
            playerContainer
        },
        modifier = modifier,
        update = { view ->
            when(autoPlay){
                true -> player?.play()
                false -> {

                }
            }
            avPlayerViewController.player?.play()
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
    sliderIndicatorColor: Color
) {
    val player = remember {
        createAVPlayerWithHeaders(url, headers)
    }
    val playerLayer = remember { AVPlayerLayer() }
    val avPlayerViewController = remember { AVPlayerViewController() }

    avPlayerViewController.player = player
    avPlayerViewController.showsPlaybackControls = true
    avPlayerViewController.allowsPictureInPicturePlayback = true

    playerLayer.player = player

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
fun YouTubeIFramePlayer(url: String, modifier: Modifier,autoPlay: Boolean) {
    val videoId = remember(url) {
        url?.substringAfter("v=")?.substringBefore("&") ?: url?.substringAfterLast("/")
    }
    val isAutoPlay = when(autoPlay){
        true -> 1
        false -> 0
    }
    val htmlContent = """
        <html>
        <head>
            <style>
                body, html {
                    margin: 0;
                    padding: 0;
                    height: 100%;
                    overflow: hidden;
                }
                .video-container {
                    position: relative;
                    width: 100%;
                    height: 100%;
                }
                .video-container iframe {
                    position: absolute;
                    top: 0;
                    left: 0;
                    width: 100%;
                    height: 100%;
                    border: none;
                }
            </style>
        </head>
        <body>
            <div class="video-container">
                <iframe 
                    src="https://www.youtube.com/embed/$videoId?autoplay=$isAutoPlay" 
                    allow="autoplay;">
                </iframe>
            </div>
        </body>
        </html>
    """.trimIndent()

    UIKitView<WKWebView>(
        factory = {
            val webView = WKWebView()
            webView.scrollView.scrollEnabled = false
            webView.loadHTMLString(htmlContent, baseURL = null)
            webView
        },
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(16f / 13f),
        update = { view ->
            // Update logic if needed
        },
        properties = UIKitInteropProperties(
            isInteractive = true,
            isNativeAccessibilityEnabled = true
        )
    )
}
