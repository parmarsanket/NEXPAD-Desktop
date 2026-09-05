package com.sanket.tools.nexpaddesktop.connection.adb

import java.io.File

/**
 * Resolves the path to the ADB (Android Debug Bridge) executable.
 * Priority:
 * 1. System PATH (executes `adb version`)
 * 2. Android SDK standard locations (%LOCALAPPDATA%\Android\Sdk\platform-tools\adb.exe, %ANDROID_HOME%, %ANDROID_SDK_ROOT%)
 * 3. Bundled fallback (`tools/adb/adb.exe` in application directory)
 */
object AdbPathResolver {

    private var cachedAdbPath: String? = null

    fun resolveAdbPath(): String? {
        cachedAdbPath?.let { return it }

        // 1. Check system PATH
        if (canExecute("adb")) {
            println("[ADB/Resolver] Found ADB in system PATH.")
            cachedAdbPath = "adb"
            return "adb"
        }

        // 2. Check standard Android SDK locations on Windows
        val localAppData = System.getenv("LOCALAPPDATA")
        val candidatePaths = mutableListOf<String>()

        if (!localAppData.isNullOrBlank()) {
            candidatePaths.add("$localAppData\\Android\\Sdk\\platform-tools\\adb.exe")
        }

        val androidHome = System.getenv("ANDROID_HOME")
        if (!androidHome.isNullOrBlank()) {
            candidatePaths.add("$androidHome\\platform-tools\\adb.exe")
        }

        val androidSdkRoot = System.getenv("ANDROID_SDK_ROOT")
        if (!androidSdkRoot.isNullOrBlank()) {
            candidatePaths.add("$androidSdkRoot\\platform-tools\\adb.exe")
        }

        // User profile fallback
        val userHome = System.getProperty("user.home")
        if (!userHome.isNullOrBlank()) {
            candidatePaths.add("$userHome\\AppData\\Local\\Android\\Sdk\\platform-tools\\adb.exe")
        }

        for (path in candidatePaths) {
            val file = File(path)
            if (file.exists() && file.canExecute()) {
                if (canExecute(file.absolutePath)) {
                    println("[ADB/Resolver] Found ADB in Android SDK: ${file.absolutePath}")
                    cachedAdbPath = file.absolutePath
                    return file.absolutePath
                }
            }
        }

        // 3. Check bundled tools fallback
        val bundledPaths = listOf(
            File("desktopApp/tools/adb/adb.exe"),
            File("tools/adb/adb.exe"),
            File("tools/adb.exe"),
            File("adb.exe")
        )

        for (file in bundledPaths) {
            if (file.exists() && file.canExecute()) {
                if (canExecute(file.absolutePath)) {
                    println("[ADB/Resolver] Found bundled ADB: ${file.absolutePath}")
                    cachedAdbPath = file.absolutePath
                    return file.absolutePath
                }
            }
        }

        println("[ADB/Resolver] WARNING: ADB executable not found.")
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
