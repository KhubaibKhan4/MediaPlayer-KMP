import com.vanniktech.maven.publish.SonatypeHost
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.NativeBuildType
import org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalWasmDsl


plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.compose)
    alias(libs.plugins.compose.compiler)
    id("com.vanniktech.maven.publish") version "0.29.0"
    id("com.google.osdetector") version "1.7.3"
    alias(libs.plugins.cocoapods)
}

kotlin {
    jvm()
    androidTarget {
        publishLibraryVariants("release")
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_1_8)
        }
    }
    js(IR)  {
        moduleName = "composeApp"
        browser {
            commonWebpackConfig {
                outputFileName = "sampleApp.js"
            }
        }
        binaries.executable()
    }
    wasmJs {
        moduleName = "composeApp"
        browser {
            commonWebpackConfig {
                outputFileName = "sampleApp.js"
            }
        }
        binaries.executable()
    }
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    cocoapods {
        summary = "Some description for the Shared Module"
        homepage = "Link to the Shared Module homepage"
        version = "1.0"
        ios.deploymentTarget = "13.0"
       // podfile = project.file("../iosApp/Podfile")
        framework {
            isStatic = false
        }

        pod("YouTubePlayer") {
            version = "0.7"
            extraOpts += listOf("-compiler-option", "-fmodules")
            // extraOpts += listOf("-compiler-option", "-DFB_SONARKIT_ENABLED=1")
        }

        xcodeConfigurationToNativeBuildType["CUSTOM_DEBUG"] = NativeBuildType.DEBUG
        xcodeConfigurationToNativeBuildType["CUSTOM_RELEASE"] = NativeBuildType.RELEASE
    }


    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(compose.runtime)
                implementation(compose.material3)
                implementation(compose.foundation)
                implementation(compose.ui)
                implementation(compose.materialIconsExtended)
                implementation(libs.kotlinx.coroutines.core)
                implementation("io.coil-kt.coil3:coil-compose:3.1.0")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }
        val androidMain by getting {
            dependencies {
                implementation(libs.startup.runtime)
                implementation(libs.core)
                implementation(libs.custom.ui)
                implementation(libs.androidx.appcompat)
                implementation(libs.androidx.activityCompose)
                implementation(libs.compose.uitooling)
                implementation(compose.ui)
                implementation(libs.androidx.media3.exoplayer)
                implementation(libs.androidx.media3.exoplayer.dash)
                implementation(libs.androidx.media3.ui)
                implementation(libs.androidx.media3.session)
                implementation(libs.jetbrains.kotlinx.serialization.json)
                implementation(libs.kotlinx.serialization.core)
                implementation(libs.kotlinx.coroutines.android)
            }
        }
        val jvmMain by getting {
            dependencies {
                implementation(compose.desktop.common)
                implementation(compose.desktop.currentOs)
                val fxSuffix = when (osdetector.classifier) {
                    "linux-x86_64" -> "linux"
                    "linux-aarch_64" -> "linux-aarch64"
                    "windows-x86_64" -> "win"
                    "osx-x86_64" -> "mac"
                    "osx-aarch_64" -> "mac-aarch64"
                    else -> throw IllegalStateException("Unknown OS: ${osdetector.classifier}")
                }
                implementation("org.openjfx:javafx-base:19:${fxSuffix}")
                implementation("org.openjfx:javafx-graphics:19:${fxSuffix}")
                implementation("org.openjfx:javafx-controls:19:${fxSuffix}")
                implementation("org.openjfx:javafx-swing:19:${fxSuffix}")
                implementation("org.openjfx:javafx-web:19:${fxSuffix}")
                implementation("org.openjfx:javafx-media:19:${fxSuffix}")
                implementation(libs.kotlinx.coroutines.swing)
            }
        }
        val jsMain by getting {
            dependencies {
                implementation(compose.html.core)
            }
        }
    }
}

android {
    namespace = "org.jetbrains.kotlinx.multiplatform.library.template"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    defaultConfig {
        minSdk = 21
    }
}
mavenPublishing {
    coordinates(
        groupId = "io.github.khubaibkhan4",
        artifactId = "mediaplayer-kmp",
        version = "2.0.8"
    )

    pom {
        name.set("MediaPlayer-KMP")
        description.set("Compose & Kotlin Multiplatform Library that Help You to Play Videos/ YouTube Videos Native Notifications on Android, iOS, Web & Desktop.")
        inceptionYear.set("2024")
        url.set("https://github.com/KhubaibKhan4/MediaPlayer-KMP")

        licenses {
            license {
                name.set("GPL-2.0")
                url.set("https://opensource.org/license/gpl-2-0")
            }
        }

        developers {
            developer {
                id.set("khubaibkhan4")
                name.set("Muhammad Khubaib Imtiaz")
                email.set("18.bscs.803@gmail.com")
            }
        }

        scm {
            url.set("https://github.com/KhubaibKhan4/MediaPlayer-KMP")
        }
    }

    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)

    signAllPublications()
}

task("testClasses") {}
tasks.withType<JavaExec> {
    jvmArgs = listOf("--add-modules", "javafx.controls,javafx.fxml", "--add-opens", "javafx.graphics/javafx.scene=ALL-UNNAMED", "--add-opens", "javafx.graphics/com.sun.javafx.scene=ALL-UNNAMED", "--add-opens", "javafx.graphics/com.sun.javafx.stage=ALL-UNNAMED")
}
