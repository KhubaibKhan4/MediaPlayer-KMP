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
<img src="https://img.shields.io/liberapay/patrons/KhubaibKhanDev.svg?logo=liberapay">


## Overview

MediaPlayer-KMP is a Kotlin Multiplatform (KMP) library that allows you to display and play YouTube videos across Android, iOS, Web, and Desktop platforms using JetBrains Compose Multiplatform. It provides a unified API for video playback that seamlessly integrates into Kotlin's multiplatform ecosystem.

## Features

- **Platform Agnostic:** Supports Android, iOS, Web, and Desktop platforms through Kotlin Multiplatform.
- **Compose Multiplatform Integration:** Seamlessly integrates with JetBrains Compose Multiplatform UI framework.
- **YouTube Video Playback:** Easily load and play YouTube videos with minimal setup.
- **Event Handling:** Provides callbacks and event listeners for video playback actions and events.

## Installation

You can include Alert-KMP in your project by adding the following dependency:

**Version Catelog**
```
[versions]
mediaPlayerKMP = "1.0.3"

[libraries]
alert-kmp = { module = "io.github.khubaibkhan4:mediaplayer-kmp", version.ref = "mediaPlayerKMP" }

```


```groovy
implementation("io.github.khubaibkhan4:mediaplayer-kmp:1.0.3")
```


## Usage

### YouTube Video Player
For the YouTube Player, you just need to provide the youtube video link. It will automatically detect it & will launch the YouTube Player. 
```groovy
import io.github.khubaibkhan4.mediaplayer.VideoPlayer

fun main() {
   VideoPlayer(modifier = Modifier.fillMaxWidth().height(340.dp),
               url ="https://www.youtube.com/watch?v=AD2nEllUMJw", // Automatically Detect the URL, Wether to Play YouTube Video or .mp4 e.g
     )
}
```

###  Video Player
For the YouTube Player, you just need to provide the youtube video link. It will automatically detect it & will launch the YouTube Player. It almost supports all the video extensions.

```groovy
import io.github.khubaibkhan4.mediaplayer.VideoPlayer

fun main() {
   VideoPlayer(modifier = Modifier.fillMaxWidth().height(340.dp),
               url ="https://freetestdata.com/wp-content/uploads/2022/02/Free_Test_Data_1MB_MP4.mp4", // Automatically Detect the URL, Wether to Play YouTube Video or .mp4 e.g
     )
}
```

## Audio Player Support
Audio Player Support is Implemented. It supports `mp3` `wav` `aac` `ogg` `m4a`. It Supports Play Back, Volume Up, Down and Stability as well. 
```groovy
import io.github.khubaibkhan4.mediaplayer.VideoPlayer

fun main() {
    MediaPlayer(
        modifier = Modifier.fillMaxWidth(),
        url = "https://commondatastorage.googleapis.com/codeskulptor-demos/DDR_assets/Sevish_-__nbsp_.mp3"
    )
}
```

## 🤝 Connect with Me

Let's chat about potential projects, job opportunities, or any other collaboration! Feel free to connect with me through the following channels:

[![LinkedIn](https://img.shields.io/badge/LinkedIn-Connect-blue?style=for-the-badge&logo=linkedin)](https://www.linkedin.com/in/khubaibkhandev)
[![Twitter](https://img.shields.io/badge/Twitter-Follow-blue?style=for-the-badge&logo=twitter)](https://twitter.com/codespacepro)
[![Email](https://img.shields.io/badge/Email-Drop%20a%20Message-red?style=for-the-badge&logo=gmail)](mailto:18.bscs.803@gmail.com)

  ## 💰 You can help me by Donating
  [![BuyMeACoffee](https://img.shields.io/badge/Buy%20Me%20a%20Coffee-ffdd00?style=for-the-badge&logo=buy-me-a-coffee&logoColor=black)](https://buymeacoffee.com/khubaibkhan) [![PayPal](https://img.shields.io/badge/PayPal-00457C?style=for-the-badge&logo=paypal&logoColor=white)](https://paypal.me/18.bscs) [![Patreon](https://img.shields.io/badge/Patreon-F96854?style=for-the-badge&logo=patreon&logoColor=white)](https://patreon.com/MuhammadKhubaibImtiaz) [![Ko-Fi](https://img.shields.io/badge/Ko--fi-F16061?style=for-the-badge&logo=ko-fi&logoColor=white)](https://ko-fi.com/muhammadkhubaibimtiaz) 

## Screenshots
 | ![Screenshot 1](https://github.com/KhubaibKhan4/MediaPlayer-KMP/blob/master/assests/screenshots/1.png) | ![Screenshot 2](https://github.com/KhubaibKhan4/MediaPlayer-KMP/blob/master/assests/screenshots/2.png) | ![Screenshot 3](https://github.com/KhubaibKhan4/MediaPlayer-KMP/blob/master/assests/screenshots/3.png) |
| --- | --- | --- |
 ![Screenshot 2](https://github.com/KhubaibKhan4/MediaPlayer-KMP/blob/master/assests/screenshots/6.png) 

| ![Screenshot 1](https://github.com/KhubaibKhan4/MediaPlayer-KMP/blob/master/assests/screenshots/4.png) | 
| --- |
| ![Screenshot 2](https://github.com/KhubaibKhan4/MediaPlayer-KMP/blob/master/assests/screenshots/5.png) | 


## Demo

https://github.com/KhubaibKhan4/MediaPlayer-KMP/assets/98816544/657ad29d-5129-4f78-af56-ad354ba0935d


## Desktop Demo

https://github.com/KhubaibKhan4/MediaPlayer-KMP/assets/98816544/efd68685-2f41-4445-ad76-c183869ab93a


  ## Contribution Guidelines
We welcome contributions to the MediaPlayer-KMP Library Project! To contribute, please follow these guidelines:

- **Reporting Bugs**: If you encounter a bug, please open an issue and provide detailed information about the bug, including steps to reproduce it.
- **Suggesting Features**: We encourage you to suggest new features or improvements by opening an issue and describing your idea.
- **Submitting Pull Requests**: If you'd like to contribute code, please fork the repository, create a new branch for your changes, and submit a pull request with a clear description of the changes.

## Code of Conduct
We expect all contributors and users of the Alert-KMP Library Project to adhere to our code of conduct. Please review the [Code of Conduct](CODE_OF_CONDUCT.md) for details on expected behavior and reporting procedures.
