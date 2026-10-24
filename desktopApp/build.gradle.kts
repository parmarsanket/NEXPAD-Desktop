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
    description =
        "Builds the release distributable, creates the NSIS installer, " +
        "and copies it with its SHA-256 checksum."

    dependsOn("createReleaseDistributable")

    notCompatibleWithConfigurationCache(
        "Executes external NSIS compiler and file synchronization"
    )

    workingDir = installerDir
    commandLine(nsisPath, "installer.nsi")

    doFirst {
        check(File(nsisPath).isFile) {
            "NSIS compiler not found: $nsisPath"
        }

        check(File(installerDir, "installer.nsi").isFile) {
            "installer.nsi not found in: ${installerDir.absolutePath}"
        }

        // Ensure NSIS can write its output.
        setupExeFile.parentFile?.mkdirs()
    }

    doLast {
        check(setupExeFile.isFile) {
            "Installer was not generated: ${setupExeFile.absolutePath}. " +
                "Check the OutFile path in installer.nsi."
        }

        // Calculate SHA-256 without loading the whole EXE into memory.
        val md = MessageDigest.getInstance("SHA-256")

        setupExeFile.inputStream().use { input ->
            val buffer = ByteArray(64 * 1024)

            while (true) {
                val count = input.read(buffer)
                if (count < 0) break

                md.update(buffer, 0, count)
            }
        }

        val sha256 = md.digest().joinToString("") { byte ->
            "%02X".format(byte)
        }

        val destinations = listOf(
            desktopReleaseSetupFile,
            playStoreSetupFile
        )

        destinations.forEach { destination ->
            destination.parentFile?.mkdirs()

            setupExeFile.copyTo(
                target = destination,
                overwrite = true
            )

            destination
                .resolveSibling("${destination.name}.sha256")
                .writeText("$sha256 *${destination.name}\n")

            println(
                "📦 [RELEASE SETUP] Successfully copied official installer to: ${destination.absolutePath}"
            )
        }

        println("🔑 [SHA-256 CHECKSUM]: $sha256")
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
