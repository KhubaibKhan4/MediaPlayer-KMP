package html_embeded_content.domain.factory

import html_embeded_content.domain.repository.HtmlContentViewer.HtmlContentViewer

import android.content.Context
import android.webkit.WebView
import android.webkit.WebViewClient
import com.mediaplayer.kmp.AppContext
import com.mediaplayer.kmp.AppContextInitializer
import html_embeded_content.data.Callback
import html_embeded_content.data.EmbedOptions

actual class HtmlContentViewerFactory actual constructor() {
    private val context = AppContext.get()
    actual fun createHtmlContentViewer(): HtmlContentViewer = AndroidHtmlContentViewer(context)
}

class AndroidHtmlContentViewer(private val context: Context) : HtmlContentViewer {

    val webView: WebView = WebView(context).apply {
        settings.javaScriptEnabled = true
    }
    private var pageLoadCallback: Callback = {}
    private var errorCallback: (Throwable) -> Unit = {}

    init {
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                pageLoadCallback()
            }
            override fun onReceivedError(
                view: WebView?, errorCode: Int, description: String?, failingUrl: String?
            ) {
                errorCallback(Exception("Error: $description"))
            }
        }
    }

    override fun loadUrl(url: String, options: EmbedOptions) {
        webView.loadUrl(url)
    }

    override fun refresh() {
        webView.reload()
    }

    override fun injectCustomStyle(css: String) {
        // Inject CSS via javascript after the page has loaded.
        val js = """
            javascript:(function() {
                var style = document.createElement('style');
                style.innerHTML = '$css';
                document.head.appendChild(style);
            })()
        """.trimIndent()
        webView.loadUrl(js)
    }

    override fun setPageLoadListener(callback: Callback) {
        pageLoadCallback = callback
    }

    override fun setErrorListener(callback: (Throwable) -> Unit) {
        errorCallback = callback
    }
}