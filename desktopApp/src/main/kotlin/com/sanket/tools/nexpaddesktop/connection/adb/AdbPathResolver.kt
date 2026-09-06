package com.sanket.tools.nexpaddesktop.connection.adb

import java.io.File

/**
 * Resolves the path to the ADB (Android Debug Bridge) executable.
 *
 * Design Philosophy:
 * NEXPAD is built for gamers and non-developers who will NEVER install Android Studio.
 * We bundle a standalone, battle-tested ADB distribution (`tools/adb/adb.exe`) directly with the app.
 *
 * Priority:
 * 1. Bundled Standalone ADB in `tools/adb/` (Primary & default for all production and dev environments)
 * 2. System PATH (Fallback if bundled is absent or on custom platforms)
 * 3. Standard Environment Variables (%ANDROID_HOME%, %ANDROID_SDK_ROOT%) (Developer fallback only)
 */
object AdbPathResolver {

    private var cachedAdbPath: String? = null

    fun resolveAdbPath(): String? {
        cachedAdbPath?.let { return it }

        // 1. PRIORITY #1: Standalone bundled ADB in tools/adb/
        val bundledAdb = findBundledAdb()
        if (bundledAdb != null) {
            println("[ADB/Resolver] Using bundled standalone ADB: ${bundledAdb.absolutePath}")
            cachedAdbPath = bundledAdb.absolutePath
            return bundledAdb.absolutePath
        }

        // 2. PRIORITY #2: System PATH (fallback)
        if (canExecute("adb")) {
            println("[ADB/Resolver] Fallback: Found ADB in system PATH.")
            cachedAdbPath = "adb"
            return "adb"
        }

        // 3. PRIORITY #3: Standard Environment Variables (developer fallback)
        val envPaths = listOfNotNull(
            System.getenv("ANDROID_HOME")?.let { File(it, "platform-tools/adb.exe") },
            System.getenv("ANDROID_SDK_ROOT")?.let { File(it, "platform-tools/adb.exe") }
        )
        for (file in envPaths) {
            if (file.exists() && file.canExecute() && canExecute(file.absolutePath)) {
                println("[ADB/Resolver] Fallback: Found ADB via environment: ${file.absolutePath}")
                cachedAdbPath = file.absolutePath
                return file.absolutePath
            }
        }

        println("[ADB/Resolver] WARNING: ADB executable not found.")
        return null
    }

    /**
     * Locates the bundled ADB executable across all runtime environments:
     * - Packaged Installed Application (relative to the running .exe)
     * - Classpath/JAR code source directory
     * - Development / Gradle working directory (project root or desktopApp subproject)
     */
    private fun findBundledAdb(): File? {
        val candidateRoots = mutableListOf<File>()

        // A. Directory of the currently running process/executable (e.g., C:\Program Files\NEXPAD)
        try {
            System.getProperty("jpackage.app-path")?.let { appPath ->
                val exeFile = File(appPath)
                val dir = if (exeFile.isFile) exeFile.parentFile else exeFile
                if (dir != null && dir.exists()) {
                    candidateRoots.add(dir)
                }
            }
            ProcessHandle.current().info().command().ifPresent { cmd ->
                val exeFile = File(cmd)
                val dir = if (exeFile.isFile) exeFile.parentFile else exeFile
                if (dir != null && dir.exists()) {
                    candidateRoots.add(dir)
                }
            }
        } catch (_: Throwable) {}

        // B. Application resources or code source directory (JAR / ClassLoader location)
        try {
            System.getProperty("compose.application.resources.dir")?.let { resDir ->
                val f = File(resDir)
                if (f.exists()) candidateRoots.add(f)
            }
            AdbPathResolver::class.java.protectionDomain?.codeSource?.location?.toURI()?.let { uri ->
                val codeLocation = File(uri)
                val dir = if (codeLocation.isFile) codeLocation.parentFile else codeLocation
                if (dir != null && dir.exists()) {
                    candidateRoots.add(dir)
                    dir.parentFile?.let { candidateRoots.add(it) } // e.g., parent of /app
                }
            }
        } catch (_: Throwable) {}

        // C. Current working directory (user.dir) and parent
        try {
            val userDir = File(System.getProperty("user.dir") ?: ".")
            candidateRoots.add(userDir)
            userDir.parentFile?.let { candidateRoots.add(it) }
        } catch (_: Throwable) {}

        // Relative paths within candidate directories
        val relativeSubPaths = listOf(
            "tools/adb/adb.exe",
            "desktopApp/tools/adb/adb.exe",
            "tools/adb.exe",
            "adb.exe"
        )

        for (root in candidateRoots) {
            for (subPath in relativeSubPaths) {
                val candidate = File(root, subPath)
                if (candidate.exists() && candidate.isFile && candidate.canExecute()) {
                    if (canExecute(candidate.absolutePath)) {
                        return candidate.canonicalFile
                    }
                }
            }
        }

        return null
    }

    private fun canExecute(command: String): Boolean {
        return try {
            val process = ProcessBuilder(command, "version")
                .redirectErrorStream(true)
                .start()
            val exitCode = process.waitFor()
            exitCode == 0
        } catch (_: Exception) {
            false
        }
    }
}
