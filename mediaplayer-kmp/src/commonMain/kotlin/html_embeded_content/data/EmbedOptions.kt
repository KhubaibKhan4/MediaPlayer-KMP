package html_embeded_content.data

typealias Callback = () -> Unit


data class EmbedOptions(
    val customCss: String? = null,
    val onPageLoaded: Callback? = null,
    val onError: ((Throwable) -> Unit)? = null
)