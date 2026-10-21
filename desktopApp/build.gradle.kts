import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization)
}

dependencies {
    implementation(projects.shared)

    implementation(compose.desktop.currentOs)
    implementation(libs.kotlinx.coroutinesSwing)

    implementation(libs.compose.uiToolingPreview)
    implementation(libs.compose.material3)
    implementation(compose.materialIconsExtended)

    // Networking & Serialization
    implementation(libs.ktor.network)
    implementation(libs.kotlinx.serialization.json)
    implementation("com.sanket.tools.nexpad:protocol:1.0.0")

    // Driver Integration
    implementation(libs.jna)
    implementation(libs.jna.platform)

    // AOA Proof-of-Concept Test — libusb JVM bindings
    implementation("org.usb4java:usb4java:1.3.0")

    // Navigation 3 (Compose Multiplatform)
    implementation("androidx.navigation3:navigation3-runtime:1.1.0")
    implementation("org.jetbrains.androidx.navigation3:navigation3-ui:1.1.1")

    // Reorderable Drag and Drop
    implementation(libs.reorderable)

    testImplementation(libs.junit)
    testImplementation(libs.kotlin.test)
}

compose.desktop {
    application {
        mainClass = "com.sanket.tools.nexpaddesktop.MainKt"

        jvmArgs += listOf(
            "-Xms32m",
            "-Xmx256m",
            "-XX:+UseG1GC",
            "-XX:ParallelGCThreads=2",
            "-XX:ConcGCThreads=1",
            "-XX:MaxGCPauseMillis=16",
            "-XX:G1PeriodicGCInterval=3000",
            "-XX:+ShrinkHeapInSteps"
        )

        nativeDistributions {

            targetFormats(TargetFormat.Msi, TargetFormat.Exe, TargetFormat.AppImage)
            packageName = "NEXPAD"
            packageVersion = "1.0.0"
            description = "Next-Generation Low-Latency Mobile-to-PC Gamepad & Motion Aiming System"
            copyright = "© 2026 NEXPAD. All rights reserved."
            vendor = "NEXPAD"
            licenseFile.set(rootProject.file("LICENSE"))

            windows {
                iconFile.set(project.file("src/main/resources/images/nexpad_icon.ico"))
                menu = true
                shortcut = true
                dirChooser = true
                upgradeUuid = "68c5b058-ec37-4d92-9112-9c3db6f80e01"
            }

            buildTypes.release.proguard {
                isEnabled.set(false)
            }
        }
    }
}