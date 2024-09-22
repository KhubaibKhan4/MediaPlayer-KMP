import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateObserver
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.round
import kotlinx.browser.document
import org.w3c.dom.Document
import org.w3c.dom.Element


val LocalLayerContainer = staticCompositionLocalOf<Element> {
    error("CompositionLocal LayerContainer not provided")
}

@Composable
fun ProvideLayerContainer(content: @Composable () -> Unit) {
    val rootElement = remember { document.createElement("div") }

    DisposableEffect(Unit) {
        document.body?.appendChild(rootElement)
        onDispose {
            document.body?.removeChild(rootElement)
        }
    }

    CompositionLocalProvider(LocalLayerContainer provides rootElement) {
        content()
    }
}

@Composable
fun <T : Element> HtmlView(
    factory: Document.() -> T,
    modifier: Modifier = Modifier,
    update: (T) -> Unit = NoOpUpdate
) {
    val componentInfo = remember { ComponentInfo<T>() }
    val root = LocalLayerContainer.current
    val density = LocalDensity.current.density

    Box(
        modifier = modifier.onGloballyPositioned { coordinates ->
            val location = coordinates.positionInWindow().round()
            val size = coordinates.size
            changeCoordinates(
                componentInfo.component,
                size.width / density,
                size.height / density,
                location.x / density,
                location.y / density
            )
        }
    )

    DisposableEffect(factory) {
        componentInfo.container = document.createElement("div")
        componentInfo.component = document.factory()
        root.appendChild(componentInfo.container)
        componentInfo.container.appendChild(componentInfo.component)
        componentInfo.updater = Updater(componentInfo.component, update)
        initializingElement(componentInfo.component)
        onDispose {
            root.removeChild(componentInfo.container)
            componentInfo.updater.dispose()
        }
    }

    SideEffect {
        componentInfo.updater.update = update
    }
}

private fun initializingElement(element: Element): Unit = js(
    """
    {
        element.style.position = 'absolute';
        element.style.margin = '0px';
    }
"""
)

private fun changeCoordinates(
    element: Element,
    width: Float,
    height: Float,
    x: Float,
    y: Float
): Unit = js(
    """
    {
        element.style.width = width + 'px';
        element.style.height = height + 'px';
        element.style.left = x + 'px';
        element.style.top = y + 'px';
    }
"""
)

val NoOpUpdate: Element.() -> Unit = {}

private class ComponentInfo<T : Element> {
    lateinit var container: Element
    lateinit var component: T
    lateinit var updater: Updater<T>
}

private class Updater<T : Element>(
    private val component: T,
    update: (T) -> Unit
) {
    private var isDisposed = false
    private val snapshotObserver = SnapshotStateObserver { command ->
        command()
    }
    private val scheduleUpdate = { _: T ->
        if (isDisposed.not()) {
            performUpdate()
        }
    }

    var update: (T) -> Unit = update
        set(value) {
            if (field != value) {
                field = value
                performUpdate()
            }
        }

    private fun performUpdate() {
        snapshotObserver.observeReads(component, scheduleUpdate) {
            update(component)
        }
    }

    init {
        snapshotObserver.start()
        performUpdate()
    }

    fun dispose() {
        snapshotObserver.stop()
        snapshotObserver.clear()
        isDisposed = true
    }
}