import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.interop.UIKitView
import kotlinx.cinterop.CValue
import kotlinx.cinterop.ExperimentalForeignApi
import platform.AVFoundation.AVPlayer
import platform.AVFoundation.AVPlayerLayer
import platform.AVFoundation.play
import platform.AVKit.AVPlayerViewController
import platform.CoreGraphics.CGRect
import platform.Foundation.NSURL
import platform.QuartzCore.CATransaction
import platform.QuartzCore.kCATransactionDisableActions
import platform.UIKit.UIView
import platform.WebKit.WKWebView

@Composable
actual fun VideoPlayer(
    modifier: Modifier,
    url: String,
) {
    if (url.contains("youtube.com") || url.contains("youtu.be")) {
        YouTubeIFramePlayer(url = url, modifier = modifier)
    } else if (isVideoFile(url) || isAudioFile(url)) {
        AvPlayerView(modifier,url)
    } else {
        Text("Unsupported media format", modifier = modifier)
    }
}

@OptIn(ExperimentalForeignApi::class)
@Composable
fun AvPlayerView(modifier: Modifier = Modifier, url: String) {
    val player = remember {
        NSURL.URLWithString(url.toString())?.let { AVPlayer(uRL = it) }
    }
    val playerLayer = remember { AVPlayerLayer() }
    val avPlayerViewController = remember { AVPlayerViewController() }
    avPlayerViewController.player = player
    avPlayerViewController.showsPlaybackControls = true

    playerLayer.player = player
    UIKitView(
        factory = {
            val playerContainer = UIView()
            playerContainer.addSubview(avPlayerViewController.view)
            playerContainer
        },
        onResize = { view: UIView, rect: CValue<CGRect> ->
            CATransaction.begin()
            CATransaction.setValue(true, kCATransactionDisableActions)
            view.layer.setFrame(rect)
            playerLayer.setFrame(rect)
            avPlayerViewController.view.layer.frame = rect
            CATransaction.commit()
        },
        update = { view ->
            player?.play()
            avPlayerViewController.player?.play()
        },
        modifier = modifier
    )
}

fun isVideoFile(url: String?): Boolean {
    return url?.matches(Regex(".*\\.(mp4|mkv|webm|avi|mov|wmv|flv|m4v|3gp|mpeg)\$", RegexOption.IGNORE_CASE)) == true
}
@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun MediaPlayer(
    modifier: Modifier,
    url: String,
    startTime: Color,
    endTime: Color,
    volumeIconColor: Color,
    playIconColor: Color,
    sliderTrackColor: Color,
    sliderIndicatorColor: Color
) {
    val player = remember {
        when {
            url.contains("youtube.com") || url.contains("youtu.be") -> {
                NSURL.URLWithString(url)?.let { AVPlayer(uRL = it) }
            }
            isVideoFile(url) || isAudioFile(url) -> {
                NSURL.URLWithString(url)?.let { AVPlayer(uRL = it) }
            }
            else -> null
        }
    }
    val playerLayer = remember { AVPlayerLayer() }
    val avPlayerViewController = remember { AVPlayerViewController() }
    avPlayerViewController.player = player
    avPlayerViewController.showsPlaybackControls = true

    playerLayer.player = player
    UIKitView(
        factory = {
            val playerContainer = UIView()
            playerContainer.addSubview(avPlayerViewController.view)
            playerContainer
        },
        onResize = { view: UIView, rect: CValue<CGRect> ->
            CATransaction.begin()
            CATransaction.setValue(true, kCATransactionDisableActions)
            view.layer.setFrame(rect)
            playerLayer.setFrame(rect)
            avPlayerViewController.view.layer.frame = rect
            CATransaction.commit()
        },
        update = { view ->
            player?.play()
            avPlayerViewController.player?.play()
        },
        modifier = modifier
    )
}

fun isAudioFile(url: String?): Boolean {
    return url?.matches(Regex(".*\\.(mp3|wav|aac|ogg|m4a)\$", RegexOption.IGNORE_CASE)) == true
}

@OptIn(ExperimentalForeignApi::class)
@Composable
fun YouTubeIFramePlayer(url: String, modifier: Modifier) {
    val videoId = remember(url) {
        url?.substringAfter("v=")?.substringBefore("&") ?: url?.substringAfterLast("/")
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
                    src="https://www.youtube.com/embed/$videoId?autoplay=1" 
                    allow="autoplay;">
                </iframe>
            </div>
        </body>
        </html>
    """.trimIndent()

    UIKitView(
        factory = {
            val webView = WKWebView()
            webView.scrollView.scrollEnabled = false
            webView.loadHTMLString(htmlContent, baseURL = null)
            webView
        },
        onResize = { view: UIView, rect: CValue<CGRect> ->
            view.setFrame(rect)
        },
        update = { view ->
            // Update logic if needed
        },
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(16f / 13f)
    )
}
