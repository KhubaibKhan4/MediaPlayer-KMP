plugins {
    alias(libs.plugins.kotlinMultiplatform)
}

kotlin {
    listOf(
        macosX64(),
        macosArm64(),
        linuxX64(),
        mingwX64(),
    ).forEach {
        it.binaries.executable {
            entryPoint = "main"
        }
    }

    sourceSets {
        commonMain.dependencies {
//            implementation(project(":shared"))
        }
    }
}
