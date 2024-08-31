import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.bundling.Jar
import org.gradle.kotlin.dsl.`maven-publish`

plugins {
    `maven-publish`
    signing
}

publishing {
    // Configure all publications
    publications.withType<MavenPublication> {
        // Stub javadoc.jar artifact
        artifact(tasks.register("${name}JavadocJar", Jar::class) {
            archiveClassifier.set("javadoc")
            archiveAppendix.set(this@withType.name)
        })

        // Provide artifacts information required by Maven Central
        pom {
            name.set("MediaPlayer-KMP")
            description.set("Compose & Kotlin Multiplatform Library that Help You to Play Videos/ YouTube Videos Native Notifications on Android, iOS, Web & Desktop.")
            inceptionYear.set("2024")
            url.set("https://github.com/KhubaibKhan4/MediaPlayer-KMP")

            licenses {
                license {
                    name.set("MIT")
                    url.set("https://opensource.org/licenses/MIT")
                }
            }

            // Specify developers information
            developers {
                developer {
                    id.set("khubaibkhan4")
                    name.set("Muhammad Khubaib Imtiaz")
                    email.set("18.bscs.803@gmail.com")
                }
            }

            // Specify SCM information
            scm {
                url.set("https://github.com/KhubaibKhan4/MediaPlayer-KMP")
            }
        }
    }
}

signing {
    if (project.hasProperty("signing.gnupg.keyName")) {
        useGpgCmd()
        sign(publishing.publications)
    }
}
