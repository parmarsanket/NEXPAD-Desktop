import java.security.MessageDigest
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

            targetFormats(TargetFormat.AppImage)
            packageName = "NEXPAD Desktop"
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

val nsisPath = "C:\\Program Files (x86)\\NSIS\\makensis.exe"
val setupExeFile = rootProject.file("installer_build/NEXPAD_Desktop_Setup.exe")
val playStoreSetupFile = rootProject.file("../NEXPAD_PlayStore_Assets/NEXPAD_Desktop_Setup.exe")
val desktopReleaseDir = rootProject.file("../Desktop_Release_v1.0.0")
val desktopReleaseSetupFile = rootProject.file("../Desktop_Release_v1.0.0/NEXPAD_Desktop_Setup.exe")
val installerDir = rootProject.projectDir

tasks.register<Exec>("packageReleaseExe") {
    group = "distribution"
    description = "Packages release distribution, builds the official NSIS setup installer, and copies it to Desktop_Release_v1.0.0 and NEXPAD_PlayStore_Assets."
    dependsOn("createReleaseDistributable")
    notCompatibleWithConfigurationCache("Executes external NSIS compiler and file synchronization")
    workingDir = installerDir
    commandLine(nsisPath, "installer.nsi")

    doLast {
        if (setupExeFile.exists()) {
            val md = MessageDigest.getInstance("SHA-256")
            setupExeFile.inputStream().use { fis ->
                val buffer = ByteArray(65536)
                var read: Int
                while (fis.read(buffer).also { read = it } != -1) {
                    md.update(buffer, 0, read)
                }
            }
            val sha256 = md.digest().joinToString("") { b -> "%02X".format(b) }
            println("🔑 [SHA-256 CHECKSUM]: $sha256")
            if (desktopReleaseDir.exists()) {
                setupExeFile.copyTo(desktopReleaseSetupFile, overwrite = true)
                File(desktopReleaseDir, "NEXPAD_Desktop_Setup.exe.sha256").writeText("$sha256 *NEXPAD_Desktop_Setup.exe\n")
                println("📦 [RELEASE SETUP] Successfully copied official installer to: ${desktopReleaseSetupFile.absolutePath}")
            }
            if (playStoreSetupFile.parentFile.exists()) {
                setupExeFile.copyTo(playStoreSetupFile, overwrite = true)
                File(playStoreSetupFile.parentFile, "NEXPAD_Desktop_Setup.exe.sha256").writeText("$sha256 *NEXPAD_Desktop_Setup.exe\n")
                println("📦 [RELEASE SETUP] Successfully copied official installer to: ${playStoreSetupFile.absolutePath}")
            }
        }
    }
}

tasks.register("packageExe") {
    group = "distribution"
    description = "Alias for packageReleaseExe."
    dependsOn("packageReleaseExe")
}

tasks.register("buildInstaller") {
    group = "distribution"
    description = "Alias for packageReleaseExe."
    dependsOn("packageReleaseExe")
}
