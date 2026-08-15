import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
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
}

compose.desktop {
    application {
        mainClass = "com.sanket.tools.nexpaddesktop.MainKt"

        nativeDistributions {

            targetFormats(TargetFormat.Msi, TargetFormat.Exe, TargetFormat.AppImage)
            packageName = "com.sanket.tools.nexpaddesktop"
            packageVersion = "1.0.0"

            buildTypes.release.proguard {
                isEnabled.set(false)
            }
        }
    }
}