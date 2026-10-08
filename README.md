# MediaPlayer-KMP

[![Maven Central](https://img.shields.io/maven-central/v/io.github.khubaibkhan4/mediaplayer-kmp.svg?label=Maven%20Central)](https://search.maven.org/artifact/io.github.khubaibkhan4/mediaplayer-kmp)
![GitHub License](https://img.shields.io/github/license/KhubaibKhan4/MediaPlayer-KMP)
![GitHub Issues](https://img.shields.io/github/issues/KhubaibKhan4/MediaPlayer-KMP)
![GitHub Pull Requests](https://img.shields.io/github/issues-pr/KhubaibKhan4/MediaPlayer-KMP)
![GitHub Last Commit](https://img.shields.io/github/last-commit/KhubaibKhan4/MediaPlayer-KMP)
![GitHub Stars](https://img.shields.io/github/stars/KhubaibKhan4/MediaPlayer-KMP?style=social)
![SavePalestine](https://raw.githubusercontent.com/OneDroid/.github/refs/heads/main/images/badge/save-palestine.svg)

![Supported Platforms](https://img.shields.io/badge/platform-Android-green.svg)
![Supported Platforms](https://img.shields.io/badge/platform-iOS-blue.svg)
![Supported Platforms](https://img.shields.io/badge/platform-JS-yellow.svg)
![Supported Platforms](https://img.shields.io/badge/platform-JVM-red.svg)
![Supported Platforms](https://img.shields.io/badge/platform-WASMJS-blue.svg)

<img src="https://img.shields.io/liberapay/patrons/KhubaibKhanDev.svg?logo=liberapay">
</br>
<a href='https://ko-fi.com/Y8Y2YABZ7' target='_blank'><img height='36' style='border:0px;height:36px;' src='https://storage.ko-fi.com/cdn/kofi6.png?v=6' border='0' alt='Buy Me a Coffee at ko-fi.com' /></a>

![Untitledvideo-MadewithClipchamp57-ezgif com-video-to-gif-converter](https://github.com/user-attachments/assets/37a34a60-e5ad-48c5-9e4e-7a974cd40c62)

## Overview

MediaPlayer-KMP is a Kotlin Multiplatform (KMP) library that allows you to display and play YouTube
videos across Android, iOS, Web, and Desktop platforms using JetBrains Compose Multiplatform. It
provides a unified API for video playback that seamlessly integrates into Kotlin's multiplatform
ecosystem.

## Features

- **One API, every platform:** `VideoPlayer` and `MediaPlayer` work on Android, iOS, Desktop (JVM), JS and Wasm.
- **YouTube playback:** pass any YouTube link (`watch?v=`, `youtu.be`, `shorts`, `embed`) and the YouTube player is used automatically.
- **Video & audio files:** MP4, WebM, MKV, MOV, HLS (`.m3u8`), DASH, MP3, AAC, WAV, OGG, FLAC and more (format support depends on the platform's native player).
- **Local files:** play files from device storage or disk by path.
- **Playback events:** `Ready`, `Playing`, `Paused`, `Buffering`, `Ended` and `Error` callbacks.
- **Auth headers:** send `Authorization` or any custom header (e.g. a JWT) with media requests.
- **Fullscreen on Android**, **AutoPlay**, **show/hide controls**.
- **ReelsView:** vertical or horizontal pager of videos.
- **Embedded web content:** load any web page and run JavaScript in it.

## Platform Support

| Feature | Android | iOS | Desktop | Web (JS / Wasm) |
|---|---|---|---|---|
| Video files & streams | ExoPlayer (HLS, DASH, MP4…) | AVPlayer | JavaFX WebView | `<video>` |
| YouTube | ✅ | ✅ | ⚠️ limited (see Troubleshooting) | ✅ |
| Audio player | ✅ | ✅ | ✅ | ✅ |
| Local files | ✅ | ✅ | ✅ | — |
| Playback events | ✅ | ✅ | ✅ | ✅ |
| Request headers | ✅ streamed | ✅ streamed | ⚠️ file downloaded first | ⚠️ file downloaded first |
| Fullscreen button | ✅ | native controls | — | native controls |

## Installation

**Version Catalog**

```toml
[versions]
mediaPlayerKMP = "2.2.0"

[libraries]
mediaplayer-kmp = { module = "io.github.khubaibkhan4:mediaplayer-kmp", version.ref = "mediaPlayerKMP" }
```

```kotlin
// composeApp/build.gradle.kts
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.mediaplayer.kmp)
            // or: implementation("io.github.khubaibkhan4:mediaplayer-kmp:2.2.0")
        }
    }
}
```

All public APIs live in the `io.github.khubaibkhan4.mediaplayer` package:

```kotlin
import io.github.khubaibkhan4.mediaplayer.VideoPlayer
import io.github.khubaibkhan4.mediaplayer.MediaPlayer
import io.github.khubaibkhan4.mediaplayer.PlayerEvent
import io.github.khubaibkhan4.mediaplayer.ReelsView
import io.github.khubaibkhan4.mediaplayer.PagerType
```

### Platform setup

**Android** — nothing extra. The library only adds the `INTERNET` permission; it does not declare any
foreground service, so the Play Console will not ask you about media-playback services.

**iOS** — nothing extra.

**Web (JS / Wasm)** — nothing extra. Players are positioned over the Compose canvas inside
`document.body` by default.

**Desktop (JVM)** — the desktop player is built on JavaFX, which ships separate native jars per OS.
The library only compiles against JavaFX, so **your desktop app must add the JavaFX jars for the OS it
runs on**:

```kotlin
// composeApp/build.gradle.kts
kotlin {
    sourceSets {
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            listOf("base", "graphics", "controls", "swing", "web", "media").forEach {
                implementation("org.openjfx:javafx-$it:19:${javaFxClassifier()}")
            }
        }
    }
}

fun javaFxClassifier(): String {
    val os = System.getProperty("os.name").lowercase()
    val arm = System.getProperty("os.arch").let { it == "aarch64" || it == "arm64" }
    return when {
        os.contains("win") -> "win"
        os.contains("mac") -> if (arm) "mac-aarch64" else "mac"
        else -> if (arm) "linux-aarch64" else "linux"
    }
}
```

The library targets Java 17, so desktop apps need JDK 17 or newer.

## Usage

### Video Player (YouTube, files and streams)

`VideoPlayer` detects the URL type automatically: YouTube links open the YouTube player, everything
else (MP4, HLS, DASH, URLs without an extension, local paths) uses the platform's native player.

```kotlin
VideoPlayer(
    modifier = Modifier.fillMaxWidth().height(340.dp),
    url = "https://www.youtube.com/watch?v=AD2nEllUMJw",
    autoPlay = true,
    showControls = true,
)

VideoPlayer(
    modifier = Modifier.fillMaxWidth().height(340.dp),
    url = "https://freetestdata.com/wp-content/uploads/2022/02/Free_Test_Data_1MB_MP4.mp4",
    autoPlay = false,
    showControls = true,
)
```

| Parameter | Description |
|---|---|
| `modifier` | Size and layout of the player. Give it a height (or aspect ratio). |
| `url` | YouTube link, media URL or local file path. A YouTube `t=` parameter sets the start time. |
| `autoPlay` | Start playing as soon as the media is ready. |
| `showControls` | Show the native playback controls. |
| `headers` | Optional HTTP headers sent with media requests (ignored for YouTube). |
| `onPlayerEvent` | Optional callback receiving `PlayerEvent`s. |

### Play Local Files

Pass the file path, for example `/storage/emulated/0/Movies/a.mp4` on Android,
`/Users/me/Movies/a.mp4` on macOS or `D:\Videos\a.mp4` on Windows. `file://` URIs work too.

```kotlin
VideoPlayer(
    modifier = Modifier.fillMaxWidth().height(340.dp),
    url = filePath,
    autoPlay = false,
    showControls = true,
)
```

### Audio Player

`MediaPlayer` shows an audio player with play/pause, a seek bar and volume control for audio URLs
(`mp3`, `wav`, `aac`, `ogg`, `m4a`, `flac`, radio streams…). On Android, iOS and Web, video URLs passed
to it are shown with the video player; on Desktop `MediaPlayer` always plays audio only, so use
`VideoPlayer` for video there.

```kotlin
MediaPlayer(
    modifier = Modifier.fillMaxWidth(),
    url = "https://commondatastorage.googleapis.com/codeskulptor-demos/DDR_assets/Kangaroo_MusiQue_-_The_Neverwritten_Role_Playing_Game.mp3",
    headers = emptyMap(),
    startTime = Color.Black,
    endTime = Color.Black,
    autoPlay = true,
    volumeIconColor = Color.Black,
    playIconColor = Color.Blue,
    sliderTrackColor = Color.LightGray,
    sliderIndicatorColor = Color.Blue,
    showControls = true,
)
```

### Authentication Headers

Both `VideoPlayer` and `MediaPlayer` accept `headers`, e.g. for private videos protected by a JWT:

```kotlin
VideoPlayer(
    modifier = Modifier.fillMaxWidth().height(340.dp),
    url = "https://example.com/protected-video.m3u8",
    autoPlay = true,
    showControls = true,
    headers = mapOf(
        "Authorization" to "Bearer $jwt",
        "Custom-Header" to "YourValue",
    ),
)
```

On Android and iOS the headers are sent with every streaming request (including HLS segments).
On Web and Desktop the file is downloaded first and then played, so headers are not suitable for
live streams or very large files there.

### Playback Events

`VideoPlayer`, `MediaPlayer` and `ReelsView` report playback state through `onPlayerEvent`:

```kotlin
VideoPlayer(
    modifier = Modifier.fillMaxWidth().height(340.dp),
    url = "https://example.com/video.mp4",
    autoPlay = true,
    showControls = true,
    onPlayerEvent = { event ->
        when (event) {
            PlayerEvent.Ready -> println("Loaded")
            PlayerEvent.Playing -> println("Started")
            PlayerEvent.Paused -> println("Paused")
            PlayerEvent.Buffering -> println("Buffering")
            PlayerEvent.Ended -> println("Finished")
            is PlayerEvent.Error -> println("Failed: ${event.message}")
        }
    }
)
```

Events are delivered on the UI thread, so you can update Compose state directly.

### Fullscreen (Android)

The Android video player shows a fullscreen button in its controls. Tapping it opens the video in an
immersive fullscreen dialog; tapping it again or pressing Back returns to the inline player. The
library does not force an orientation; lock or rotate the activity yourself if your app needs
landscape fullscreen. YouTube videos use the YouTube player's own fullscreen button.

### Reels View

```kotlin
@Composable
fun MainScreen() {
    val videoUrls = listOf(
        "https://www.example.com/video1.mp4",
        "https://www.example.com/video2.mp4",
        "https://www.example.com/video3.mp4"
    )
    ReelsView(
        videoUrls = videoUrls,
        pagerType = PagerType.Vertical, // or PagerType.Horizontal
        modifier = Modifier.fillMaxSize(),
        autoPlay = true,
        showControls = true,
        onInteraction = { page, url -> println("Page $page: $url") },
        onPlayerEvent = { page, event -> println("Page $page: $event") }
    )
}
```

### Embed Content from Url

```kotlin
val viewer = HtmlContentViewerFactory().createHtmlContentViewer()
val htmlEmbedFeature = HtmlEmbedFeature(viewer)

htmlEmbedFeature.embedHtml(
    url = "https://github.com/KhubaibKhan4/MediaPlayer-KMP/",
    options = EmbedOptions(
        customCss = "body { background-color: #f0f0f0; }",
        onPageLoaded = { println("Page loaded successfully!") },
        onError = { error -> println("Error loading page: $error") }
    )
)

HtmlContentViewerView(
    viewer = viewer,
    modifier = Modifier.fillMaxSize()
)
```

### JavaScript execution + element querying

```kotlin
val viewer = HtmlContentViewerFactory().createHtmlContentViewer()

viewer.loadUrl("https://example.com")
viewer.setPageLoadListener {
    viewer.evaluateJavaScript("document.title") { title ->
        println("Page title: $title")
    }
}
```

## Migrating from 2.1.x

1. **Imports:** 2.1.x declared everything in the root package (`import VideoPlayer`). Switch to
   `import io.github.khubaibkhan4.mediaplayer.VideoPlayer` (same for `MediaPlayer`, `ReelsView`,
   `PagerType`, `VideoPlayerScreen`). The old imports still compile but are deprecated; the IDE
   quick-fix rewrites them for you. They will be removed in a future release.
2. **Desktop:** add the JavaFX dependencies shown in [Platform setup](#platform-setup). The library no
   longer bundles them, because bundling pinned the publisher's OS (macOS) for every user.
3. **Android:** the library no longer declares a `PlaybackService`, a media-button receiver or the
   `FOREGROUND_SERVICE*` permissions. If your app relied on them, declare them in your own manifest.
4. **New optional parameters:** `headers` and `onPlayerEvent` on `VideoPlayer`; `onPlayerEvent` on
   `MediaPlayer`, `ReelsView` and `VideoPlayerScreen`. Existing calls keep working.
5. Platform-specific helpers that were accidentally public (`ExoPlayerVideoPlayer`,
   `YoutubeVideoPlayer`, `DesktopWebView`, `HtmlView`, `isVideoFile`, `formatTime`, …) are now internal.
   Use `VideoPlayer` / `MediaPlayer` instead.

## Troubleshooting

**Desktop: `Error initializing QuantumRenderer: no suitable pipeline found` / `No toolkit found`**
Make sure the JavaFX jars match your OS ([Platform setup](#platform-setup)). In VMs or on Linux without
GPU drivers, force software rendering with the JVM argument `-Dprism.order=sw`:

```kotlin
compose.desktop {
    application {
        jvmArgs += listOf("-Dprism.order=sw")
    }
}
```

**Desktop: `has been compiled by a more recent version of the Java Runtime`**
Use JDK 17 or newer (2.2.0+ is compiled for Java 17).

**Desktop: a format does not play (e.g. FLV, MKV, live FLV streams)**
The desktop player relies on JavaFX's WebKit/GStreamer, which supports MP4 (H.264/AAC), HLS, MP3,
AAC, WAV and AIFF. Other formats are not supported on desktop yet.

**Desktop: YouTube does not start**
YouTube's player does not fully support JavaFX's embedded browser. Playback of plain video files is
unaffected.

**Web: the player draws above dialogs / dropdown menus**
Web players are real HTML elements placed above the Compose canvas, so Compose popups cannot cover
them. This is a Compose for Web limitation
([CMP-6858](https://youtrack.jetbrains.com/issue/CMP-6858)); hide the player while a dialog is open.

**Web: autoplay starts muted**
Browsers block autoplay with sound, so YouTube videos with `autoPlay = true` start muted.

## Running the sample

```bash
./gradlew :sample:composeApp:run
```

```bash
./gradlew :sample:composeApp:installDebug
```

```bash
./gradlew :sample:composeApp:wasmJsBrowserDevelopmentRun
```

For iOS, open the sample in Xcode (or use the Kotlin Multiplatform plugin in Android Studio) and run
it on a simulator.

## Future Plans

- Player controller API (play, pause, seek and position from code).
- Video quality / track selection.
- A more capable desktop backend (more formats, live streams, YouTube).
- Background playback with media notifications (opt-in).
- Subtitles, Picture-in-Picture and playlists.

## 🤝 Connect with Me

Let's chat about potential projects, job opportunities, or any other collaboration! Feel free to
connect with me through the following channels:

[![LinkedIn](https://img.shields.io/badge/LinkedIn-Connect-blue?style=for-the-badge&logo=linkedin)](https://www.linkedin.com/in/khubaibkhandev)
[![Twitter](https://img.shields.io/badge/Twitter-Follow-blue?style=for-the-badge&logo=twitter)](https://twitter.com/codespacepro)
[![Email](https://img.shields.io/badge/Email-Drop%20a%20Message-red?style=for-the-badge&logo=gmail)](mailto:18.bscs.803@gmail.com)

## 💰 You can help me by Donating

[![BuyMeACoffee](https://img.shields.io/badge/Buy%20Me%20a%20Coffee-ffdd00?style=for-the-badge&logo=buy-me-a-coffee&logoColor=black)](https://buymeacoffee.com/khubaibkhan) [![PayPal](https://img.shields.io/badge/PayPal-00457C?style=for-the-badge&logo=paypal&logoColor=white)](https://paypal.me/18.bscs) [![Patreon](https://img.shields.io/badge/Patreon-F96854?style=for-the-badge&logo=patreon&logoColor=white)](https://patreon.com/MuhammadKhubaibImtiaz) [![Ko-Fi](https://img.shields.io/badge/Ko--fi-F16061?style=for-the-badge&logo=ko-fi&logoColor=white)](https://ko-fi.com/muhammadkhubaibimtiaz)

## Screenshots

| ![Screenshot 1](https://github.com/KhubaibKhan4/MediaPlayer-KMP/blob/master/assests/screenshots/1.png) | ![Screenshot 2](https://github.com/KhubaibKhan4/MediaPlayer-KMP/blob/master/assests/screenshots/2.png)                              | ![Screenshot 3](https://github.com/KhubaibKhan4/MediaPlayer-KMP/blob/master/assests/screenshots/3.png) |
|--------------------------------------------------------------------------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------|--------------------------------------------------------------------------------------------------------|
 ![Screenshot 2](https://github.com/KhubaibKhan4/MediaPlayer-KMP/blob/master/assests/screenshots/6.png) | ![Screenshot_20240710_153958](https://github.com/KhubaibKhan4/MediaPlayer-KMP/assets/98816544/bbda1012-f4a9-46ad-824a-66a710c67c0b) 

| ![Screenshot 1](https://github.com/KhubaibKhan4/MediaPlayer-KMP/blob/master/assests/screenshots/4.png)                                | 
|---------------------------------------------------------------------------------------------------------------------------------------|
| ![Screenshot 2](https://github.com/KhubaibKhan4/MediaPlayer-KMP/blob/master/assests/screenshots/5.png)                                | 
| ![Screenshot 2024-07-10 153852](https://github.com/KhubaibKhan4/MediaPlayer-KMP/assets/98816544/1238c26b-8553-459d-b606-7da89459eb04) |
| ![Screenshot 2024-07-10 153944](https://github.com/KhubaibKhan4/MediaPlayer-KMP/assets/98816544/2bd8bd9e-298c-4488-8348-8f94b6705a66) |

## Embedded Content Support

| Mobile  | Desktop  |
|---------|---------|
| ![Screenshot 1](https://github.com/KhubaibKhan4/MediaPlayer-KMP/blob/master/assests/screenshots/android.png) | ![Screenshot 2](https://github.com/KhubaibKhan4/MediaPlayer-KMP/blob/master/assests/screenshots/desktop.png) |
                           

## Demo

https://github.com/KhubaibKhan4/MediaPlayer-KMP/assets/98816544/657ad29d-5129-4f78-af56-ad354ba0935d

## Desktop Demo

https://github.com/KhubaibKhan4/MediaPlayer-KMP/assets/98816544/efd68685-2f41-4445-ad76-c183869ab93a

## Star History

<a href="https://star-history.com/#KhubaibKhan4/MediaPlayer-KMP&Timeline">
 <picture>
   <source media="(prefers-color-scheme: dark)" srcset="https://api.star-history.com/svg?repos=KhubaibKhan4/MediaPlayer-KMP&type=Timeline&theme=dark" />
   <source media="(prefers-color-scheme: light)" srcset="https://api.star-history.com/svg?repos=KhubaibKhan4/MediaPlayer-KMP&type=Timeline" />
   <img alt="Star History Chart" src="https://api.star-history.com/svg?repos=KhubaibKhan4/MediaPlayer-KMP&type=Timeline" />
 </picture>
</a>

**Stargazers**

[![Stargazers repo roster for @KhubaibKhan4/MediaPlayer-KMP](http://reporoster.com/stars/dark/KhubaibKhan4/MediaPlayer-KMP)](https://github.com/KhubaibKhan4/YMediaPlayer-KMP/stargazers)

**Forkers**

[![Forkers repo roster for @KhubaibKhan4/MediaPlayer-KMP](http://reporoster.com/forks/dark/KhubaibKhan4/MediaPlayer-KMP)](https://github.com/KhubaibKhan4/MediaPlayer-KMP/network/members)

## Contribution Guidelines

We welcome contributions to the MediaPlayer-KMP Library Project! To contribute, please follow these
guidelines:

- **Reporting Bugs**: If you encounter a bug, please open an issue and provide detailed information
  about the bug, including steps to reproduce it.
- **Suggesting Features**: We encourage you to suggest new features or improvements by opening an
  issue and describing your idea.
- **Submitting Pull Requests**: If you'd like to contribute code, please fork the repository, create
  a new branch for your changes, and submit a pull request with a clear description of the changes.

## Support Us:

- We need your support for doing more open source contributions.

## Code of Conduct

We expect all contributors and users of the MediaPlayer-KMP Library Project to adhere to our code of
conduct. Please review the [Code of Conduct](CODE_OF_CONDUCT.md) for details on expected behavior
and reporting procedures.
