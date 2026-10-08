package io.github.khubaibkhan4.mediaplayer

import javafx.application.Platform
import java.io.File
import java.io.IOException
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.util.concurrent.atomic.AtomicBoolean

private val fxStartRequested = AtomicBoolean(false)

/** Runs [block] on the JavaFX application thread, starting the toolkit on first use. */
internal fun runOnFxThread(block: () -> Unit) {
    if (fxStartRequested.compareAndSet(false, true)) {
        // Without this JavaFX shuts down once the last JFXPanel is removed, and every later player fails.
        Platform.setImplicitExit(false)
        try {
            Platform.startup(block)
            return
        } catch (_: IllegalStateException) {
            // Toolkit was already started (e.g. by a JFXPanel); fall through to runLater.
        }
    }
    Platform.runLater(block)
}

/**
 * Turns [url] into a URI JavaFX can open. Blocking, call it off the UI thread.
 *
 * Local paths (including Windows paths such as `D:\music\a.mp3`) become `file:` URIs.
 * Remote URLs are used directly unless [headers] are given, in which case the media is downloaded
 * to a temporary file because JavaFX cannot attach headers to its own requests.
 */
internal fun resolveMediaSource(url: String, headers: Map<String, String>): String {
    val scheme = url.substringBefore(':', missingDelimiterValue = "").lowercase()
    if (scheme != "http" && scheme != "https") {
        // A one-letter "scheme" is a Windows drive letter, not a URI.
        return if (scheme.length > 1) url else File(url).absoluteFile.toURI().toString()
    }
    if (headers.isEmpty()) return url

    val client = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL).build()
    val request = HttpRequest.newBuilder(URI.create(url)).apply {
        headers.forEach { (key, value) -> header(key, value) }
    }.build()
    val response = client.send(request, HttpResponse.BodyHandlers.ofInputStream())
    if (response.statusCode() !in 200..299) {
        response.body().close()
        throw IOException("HTTP ${response.statusCode()} while loading $url")
    }
    // JavaFX picks the decoder from the file extension, so keep the original one.
    val extension = url.substringBefore('?').substringBefore('#').substringAfterLast('/')
        .substringAfterLast('.', missingDelimiterValue = "tmp")
    val tempFile = File.createTempFile("mediaplayer-kmp", ".$extension").apply { deleteOnExit() }
    response.body().use { input -> tempFile.outputStream().use { input.copyTo(it) } }
    return tempFile.toURI().toString()
}
