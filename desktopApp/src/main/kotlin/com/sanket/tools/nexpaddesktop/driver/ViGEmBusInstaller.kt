package com.sanket.tools.nexpaddesktop.driver

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.net.HttpURLConnection
import java.net.URI

/**
 * Utility responsible for discovering, resolving, and downloading the official
 * signed ViGEmBus kernel driver installer package across MSIX sandbox containers,
 * NSIS installations, and local development environments.
 */
object ViGEmBusInstaller {

    private const val OFFICIAL_DOWNLOAD_URL =
        "https://github.com/nefarius/ViGEmBus/releases/download/v1.22.0/ViGEmBus_Setup_1.22.0.exe"

    /**
     * Resolves the [ViGEmBusSetup.exe] binary from local application bundles.
     * Evaluates running process location (MSIX VFS/NSIS), working directory,
     * project redist folders, and Program Files installations.
     */
    fun resolveInstallerFile(): File? {
        // 1. Current Running Process Directory (MSIX VFS / Packaged AppImage)
        try {
            val command = ProcessHandle.current().info().command().orElse(null)
            if (command != null) {
                val exeFile = File(command)
                val sibling = File(exeFile.parentFile, "ViGEmBusSetup.exe")
                if (sibling.exists() && sibling.isFile && sibling.length() > 100_000) {
                    println("[ViGEmInstaller] Resolved via process sibling: ${sibling.absolutePath}")
                    return sibling
                }
            }
        } catch (e: Throwable) {
            println("[ViGEmInstaller] Could not check process handle for installer: ${e.message}")
        }

        // 2. JPackage App Path Property
        val jpackagePath = System.getProperty("jpackage.app-path")
        if (jpackagePath != null) {
            val jpFile = File(jpackagePath)
            val sibling = File(jpFile.parentFile, "ViGEmBusSetup.exe")
            if (sibling.exists() && sibling.isFile && sibling.length() > 100_000) {
                println("[ViGEmInstaller] Resolved via jpackage.app-path: ${sibling.absolutePath}")
                return sibling
            }
        }

        // 3. User Directory & Subfolder Candidates
        val userDir = File(System.getProperty("user.dir"))
        val candidatePaths = listOf(
            File(userDir, "ViGEmBusSetup.exe"),
            File(userDir, "redist/ViGEmBusSetup.exe"),
            File(userDir, "installer_build/ViGEmBusSetup.exe"),
            File(userDir.parentFile, "NEXPADDesktop/redist/ViGEmBusSetup.exe"),
            File(userDir.parentFile, "redist/ViGEmBusSetup.exe")
        )
        for (candidate in candidatePaths) {
            if (candidate.exists() && candidate.isFile && candidate.length() > 100_000) {
                println("[ViGEmInstaller] Resolved via local candidate: ${candidate.absolutePath}")
                return candidate
            }
        }

        // 4. Standard Program Files Installation Directory
        val programFiles = System.getenv("ProgramFiles") ?: "C:\\Program Files"
        val pfCandidate = File(programFiles, "NEXPAD\\ViGEmBusSetup.exe")
        if (pfCandidate.exists() && pfCandidate.isFile && pfCandidate.length() > 100_000) {
            println("[ViGEmInstaller] Resolved via Program Files: ${pfCandidate.absolutePath}")
            return pfCandidate
        }

        // 5. Existing downloaded file in Temp Directory
        val tempCandidate = File(System.getProperty("java.io.tmpdir"), "ViGEmBusSetup.exe")
        if (tempCandidate.exists() && tempCandidate.isFile && tempCandidate.length() > 5_000_000) {
            println("[ViGEmInstaller] Resolved via temp cache: ${tempCandidate.absolutePath}")
            return tempCandidate
        }

        println("[ViGEmInstaller] ViGEmBusSetup.exe not found in local directories.")
        return null
    }

    /**
     * Downloads the official signed ViGEmBus installer from GitHub Releases
     * directly into the system temporary directory with real-time progress callbacks.
     */
    suspend fun downloadInstaller(
        onProgress: (progress: Float, status: String) -> Unit
    ): File? = withContext(Dispatchers.IO) {
        val destination = File(System.getProperty("java.io.tmpdir"), "ViGEmBusSetup.exe")
        if (destination.exists() && destination.length() > 5_000_000) {
            onProgress(0.15f, "Found cached installer package...")
            return@withContext destination
        }

        try {
            onProgress(0.08f, "Connecting to official ViGEmBus repository...")
            val url = URI(OFFICIAL_DOWNLOAD_URL).toURL()
            val connection = url.openConnection() as HttpURLConnection
            connection.instanceFollowRedirects = true
            connection.connectTimeout = 15000
            connection.readTimeout = 30000
            connection.connect()

            val responseCode = connection.responseCode
            if (responseCode !in 200..299) {
                println("[ViGEmInstaller] Failed to download installer, HTTP status: $responseCode")
                return@withContext null
            }

            val totalBytes = connection.contentLengthLong
            var downloadedBytes = 0L

            connection.inputStream.use { input ->
                destination.outputStream().use { output ->
                    val buffer = ByteArray(32 * 1024)
                    var bytesRead: Int
                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                        downloadedBytes += bytesRead
                        if (totalBytes > 0) {
                            val downloadFraction = downloadedBytes.toFloat() / totalBytes
                            val scaledProgress = 0.08f + (downloadFraction * 0.40f)
                            val mbDownloaded = downloadedBytes / (1024 * 1024)
                            val mbTotal = totalBytes / (1024 * 1024)
                            onProgress(scaledProgress, "Downloading driver installer ($mbDownloaded MB / $mbTotal MB)...")
                        }
                    }
                }
            }

            if (destination.exists() && destination.length() > 1_000_000) {
                println("[ViGEmInstaller] Downloaded successfully: ${destination.absolutePath}")
                onProgress(0.48f, "Download complete. Verifying installer...")
                return@withContext destination
            }
        } catch (e: Exception) {
            println("[ViGEmInstaller] Exception while downloading ViGEmBus: ${e.message}")
        }
        return@withContext null
    }
}
