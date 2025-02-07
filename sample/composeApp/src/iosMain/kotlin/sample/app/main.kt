import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIViewController
import sample.app.App

fun MainViewController(): UIViewController = ComposeUIViewController { App() }