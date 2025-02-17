# MediaPlayer-KMP

[![Maven Central](https://img.shields.io/maven-central/v/io.github.khubaibkhan4/mediaplayer-kmp.svg?label=Maven%20Central)](https://search.maven.org/artifact/io.github.khubaibkhan4/mediaplayer-kmp)
![GitHub License](https://img.shields.io/github/license/KhubaibKhan4/MediaPlayer-KMP)
![GitHub Issues](https://img.shields.io/github/issues/KhubaibKhan4/MediaPlayer-KMP)
![GitHub Pull Requests](https://img.shields.io/github/issues-pr/KhubaibKhan4/MediaPlayer-KMP)
![GitHub Last Commit](https://img.shields.io/github/last-commit/KhubaibKhan4/MediaPlayer-KMP)
![GitHub Stars](https://img.shields.io/github/stars/KhubaibKhan4/MediaPlayer-KMP?style=social)

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

- **Platform Agnostic:** Supports Android, iOS, Web, and Desktop platforms through Kotlin
  Multiplatform.
- **Compose Multiplatform Integration:** Seamlessly integrates with JetBrains Compose Multiplatform
  UI framework.
- **YouTube Video Playback:** Easily load and play YouTube videos with minimal setup.
- **Audio Player Playback:** Easily load and play Audios with minimal setup.
- **Event Handling:** Provides callbacks and event listeners for video playback actions and events.
- **Streaming Support:** Provides Streaming Support For the Video and Audio Links.
- **AutoPlay:** AutoPlayer Support Implementation.
- **Auth Token:** Authentication Token Implementation.
- **ReelsView Support:** Reels View Support Implementation.

## Future Plans

- **Desktop Playback Support**.
- **Audio player Customisation Support**.
- **Video and Audio picker for Android, iOS, Web and Desktop.**
- **Local Content Support.**
- **Playback Support.**

## Installation

You can include MediaPlayer-KMP in your project by adding the following dependency:

**Version Catelog**

```
[versions]
mediaPlayerKMP = "2.0.6"

[libraries]
alert-kmp = { module = "io.github.khubaibkhan4:mediaplayer-kmp", version.ref = "mediaPlayerKMP" }

```

```groovy
implementation("io.github.khubaibkhan4:mediaplayer-kmp:2.0.6")
```

## Usage

### YouTube Video Player

For the YouTube Player, you just need to provide the youtube video link. It will automatically
detect it & will launch the YouTube Player.

```groovy
import io.github.khubaibkhan4.mediaplayer.VideoPlayer

fun main() {
    VideoPlayer(modifier = Modifier.fillMaxWidth().height(340.dp),
            url = "https://www.youtube.com/watch?v=AD2nEllUMJw", // Automatically Detect the URL, Wether to Play YouTube Video or .mp4 e.g
            showControls: Boolean = true,
    )
}
```

### Video Player

For the YouTube Player, you just need to provide the youtube video link. It will automatically
detect it & will launch the YouTube Player. It almost supports all the video extensions.

```groovy
import io.github.khubaibkhan4.mediaplayer.VideoPlayer

fun main() {
    VideoPlayer(modifier = Modifier.fillMaxWidth().height(340.dp),
            url = "https://freetestdata.com/wp-content/uploads/2022/02/Free_Test_Data_1MB_MP4.mp4", // Automatically Detect the URL, Wether to Play YouTube Video or .mp4 e.g
            showControls: Boolean = true,
    )
}
```
### Play Local File

To Play Local files, you just need to provide the file path.

```groovy
import io.github.khubaibkhan4.mediaplayer.VideoPlayer

fun main() {
   VideoPlayerScreen(
                url = filePath,
                autoPlay = false,
                onInteraction = {},
                showControls = true
   )
}
```

## Audio Player Support

Audio Player Support is Implemented. It supports `mp3` `wav` `aac` `ogg` `m4a`. It Supports Play
Back, Volume Up, Down and Stability as well.

```groovy
import io.github.khubaibkhan4.mediaplayer.VideoPlayer

fun main() {
    MediaPlayer(
            modifier = Modifier.fillMaxWidth(),
            url = "https://commondatastorage.googleapis.com/codeskulptor-demos/DDR_assets/Kangaroo_MusiQue_-_The_Neverwritten_Role_Playing_Game.mp3",
            startTime = Color.Black,
            endTime = Color.Black,
            volumeIconColor = Color.Black,
            playIconColor = Color.Blue,
            sliderTrackColor = Color.LightGray,
            sliderIndicatorColor = Color.Blue,
            showControls: Boolean = true,
    )
}
```

### Authentication Token Support

Providing the Auth Tokens are pretty simple. You just need to add the headers. You can add the
headers with key and value pairs.

```groovy
MediaPlayer(
        modifier = Modifier.fillMaxSize(),
        url = "https://example.com/protected-video.mp4",
        headers = mapOf(
                "Authorization" to "Bearer your_token",
                "Custom-Header" to "YourValue"
        ),
        startTime = Color.Red,
        endTime = Color.Blue,
        autoPlay = true,
        volumeIconColor = Color.Green,
        playIconColor = Color.Yellow,
        sliderTrackColor = Color.Gray,
        sliderIndicatorColor = Color.Magenta,
        showControls: Boolean = true,
)


```

## Reels View Support

```groovy
@Composable
fun MainScreen() {
    val videoUrls = listOf(
            "https://www.example.com/video1.mp4",
            "https://www.example.com/video2.mp4",
            "https://www.example.com/video3.mp4"
    )
    ReelsView(
            videoUrls = videoUrls,
            pagerType = PagerType.Vertical, // Choose PagerType.Vertical or PagerType.Horizontal
            modifier = Modifier.fillMaxSize(),
            autoPlay = true, // Enable auto-play for videos
            showControls: Boolean = true,
            onInteraction = { page, url ->
                println("User interacted with page $page, video URL: $url")
            }
    )
}


```

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

We expect all contributors and users of the Alert-KMP Library Project to adhere to our code of
conduct. Please review the [Code of Conduct](CODE_OF_CONDUCT.md) for details on expected behavior
and reporting procedures.
