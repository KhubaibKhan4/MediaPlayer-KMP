package html_embeded_content.domain.factory

import html_embeded_content.data.Callback
import html_embeded_content.data.EmbedOptions
import html_embeded_content.domain.repository.HtmlContentViewer.HtmlContentViewer
import kotlinx.cinterop.ExperimentalForeignApi
import platform.CoreGraphics.CGRectMake
import platform.Foundation.NSError
import platform.Foundation.NSURL
import platform.Foundation.NSURLRequest
import platform.WebKit.WKNavigation
import platform.WebKit.WKNavigationDelegateProtocol
import platform.WebKit.WKWebView
import platform.WebKit.WKWebViewConfiguration
import platform.darwin.NSObject
import platform.Foundation.*
import platform.WebKit.*
import platform.CoreGraphics.CGRectZero

actual class HtmlContentViewerFactory actual constructor() {
    actual fun createHtmlContentViewer(): HtmlContentViewer = IOSHtmlContentViewer()
}

class IOSHtmlContentViewer : HtmlContentViewer {
    @OptIn(ExperimentalForeignApi::class)
    val webView: WKWebView = WKWebView(
        frame = CGRectMake(0.0, 0.0, 0.0, 0.0),
        configuration = WKWebViewConfiguration()
    )
    private var pageLoadCallback: Callback = {}
    private var errorCallback: (Throwable) -> Unit = {}

    init {
        webView.navigationDelegate = object : NSObject(), WKNavigationDelegateProtocol {
            override fun webView(
                webView: WKWebView,
                didFailProvisionalNavigation: WKNavigation?,
                withError: NSError
            ) {
                errorCallback(Exception(withError.localizedDescription))
            }

            override fun webView(
                webView: WKWebView,
                decidePolicyForNavigationAction: WKNavigationAction,
                preferences: WKWebpagePreferences,
                decisionHandler: (WKNavigationActionPolicy, WKWebpagePreferences?) -> Unit
            ) {
                decisionHandler(WKNavigationActionPolicy.WKNavigationActionPolicyAllow, preferences)
            }

            override fun webView(
                webView: WKWebView,
                didReceiveAuthenticationChallenge: NSURLAuthenticationChallenge,
                completionHandler: (NSURLSessionAuthChallengeDisposition, NSURLCredential?) -> Unit
            ) {
                completionHandler(NSURLSessionAuthChallengePerformDefaultHandling, null)
            }

            override fun webView(
                webView: WKWebView,
                decidePolicyForNavigationResponse: WKNavigationResponse,
                decisionHandler: (WKNavigationResponsePolicy) -> Unit
            ) {
                decisionHandler(WKNavigationResponsePolicy.WKNavigationResponsePolicyAllow)
            }

            override fun webView(
                webView: WKWebView,
                decidePolicyForNavigationAction: WKNavigationAction,
                decisionHandler: (WKNavigationActionPolicy) -> Unit
            ) {
                decisionHandler(WKNavigationActionPolicy.WKNavigationActionPolicyAllow)
            }

            override fun webView(
                webView: WKWebView,
                navigationAction: WKNavigationAction,
                didBecomeDownload: WKDownload
            ) {
                // Optionally implement download handling.
            }

            override fun webView(
                webView: WKWebView,
                navigationResponse: WKNavigationResponse,
                didBecomeDownload: WKDownload
            ) {
                // Optionally implement download handling.
            }

            override fun webView(
                webView: WKWebView,
                authenticationChallenge: NSURLAuthenticationChallenge,
                shouldAllowDeprecatedTLS: (Boolean) -> Unit
            ) {
                shouldAllowDeprecatedTLS(false)
            }

            override fun webViewWebContentProcessDidTerminate(webView: WKWebView) {
                errorCallback(Exception("Web content process terminated"))
            }
        }
    }

    override fun loadUrl(url: String, options: EmbedOptions) {
        val nsUrl = NSURL.URLWithString(url) ?: return
        val request = NSURLRequest(nsUrl)
        webView.loadRequest(request)
    }

    override fun refresh() {
        webView.reload()
    }

    override fun injectCustomStyle(css: String) {
        val js = "var style = document.createElement('style'); style.innerHTML = '$css'; document.head.appendChild(style);"
        webView.evaluateJavaScript(js, null)
    }

    override fun setPageLoadListener(callback: Callback) {
        pageLoadCallback = callback
    }

    override fun setErrorListener(callback: (Throwable) -> Unit) {
        errorCallback = callback
    }

    override fun evaluateJavaScript(script: String, callback: ((String?) -> Unit)?) {
        webView.evaluateJavaScript(script) { result, error ->
            if (error != null) {
                callback?.invoke(null)
            } else {
                callback?.invoke(result?.toString())
            }
        }
    }
}