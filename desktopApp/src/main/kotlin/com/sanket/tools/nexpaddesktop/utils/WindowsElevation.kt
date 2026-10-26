package com.sanket.tools.nexpaddesktop.utils

import com.sun.jna.platform.win32.Kernel32
import com.sun.jna.platform.win32.Shell32
import com.sun.jna.platform.win32.ShellAPI
import com.sun.jna.platform.win32.WinBase
import com.sun.jna.platform.win32.WinError
import com.sun.jna.platform.win32.WinUser
import com.sun.jna.ptr.IntByReference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

object WindowsElevation {

    private const val SHELLEXECUTE_MASK_NOCLOSE_PROCESS = 0x00000040
    private const val SHELLEXECUTE_MASK_NOASYNC = 0x00000100
    private const val SHELL_VERB_RUN_AS = "runas"
    private const val STILL_ACTIVE = 259

    sealed interface Result {
        data class Success(val exitCode: Int) : Result
        data object UserCancelled : Result
        data class Error(val errorCode: Int, val message: String) : Result
    }

    data class ProcessTarget(
        val executable: File,
        val arguments: String = "",
        val workingDirectory: File? = null
    )

    /**
     * Executes an elevated process without any flashing CMD/PowerShell windows.
     * Suspends asynchronously on Dispatchers.IO until the elevated process exits or times out.
     * Optionally reports progress percentage (0.0 to 1.0) and status descriptions in real time.
     */
    suspend fun runElevated(
        target: ProcessTarget,
        onProgress: ((progress: Float, status: String) -> Unit)? = null
    ): Result = withContext(Dispatchers.IO) {
        validateTarget(target)?.let { return@withContext it }

        onProgress?.invoke(0.05f, "Requesting Windows Administrator approval...")

        val sei = ShellAPI.SHELLEXECUTEINFO().apply {
            cbSize = size()
            fMask = SHELLEXECUTE_MASK_NOCLOSE_PROCESS or SHELLEXECUTE_MASK_NOASYNC
            lpVerb = SHELL_VERB_RUN_AS
            lpFile = target.executable.absolutePath
            lpParameters = target.arguments.ifBlank { null }
            lpDirectory = target.workingDirectory?.absolutePath
            nShow = WinUser.SW_HIDE // completely hide window
        }

        if (!Shell32.INSTANCE.ShellExecuteEx(sei)) {
            val lastError = Kernel32.INSTANCE.GetLastError()
            return@withContext if (lastError == WinError.ERROR_CANCELLED) {
                Result.UserCancelled
            } else {
                Result.Error(lastError, "ShellExecuteEx failed with error code: $lastError")
            }
        }

        val hProcess = sei.hProcess ?: return@withContext Result.Error(-1, "Process handle was null")

        try {
            val exitCodeRef = IntByReference()
            val maxWaitMs = 90_000L // 90 seconds resilient timeout
            val pollIntervalMs = 250
            val startTime = System.currentTimeMillis()
            var exitFound = false

            val logFile = File("C:\\Users\\Public\\nexpad_driver_install.log")
            var lastLogReadPos = 0L
            val isViGEm = target.executable.name.contains("ViGEm", ignoreCase = true)

            if (isViGEm) {
                onProgress?.invoke(0.50f, "Installing ViGEmBus kernel driver into Windows...")
            } else {
                onProgress?.invoke(0.12f, "Initializing driver installer engine...")
            }

            while (System.currentTimeMillis() - startTime < maxWaitMs) {
                if (isViGEm) {
                    val elapsed = System.currentTimeMillis() - startTime
                    val progress = (0.50f + (elapsed.toFloat() / 12000f) * 0.40f).coerceAtMost(0.92f)
                    onProgress?.invoke(progress, "Configuring virtual gamepad devices...")
                }
                // Live stream driver installation output from the background process
                if (logFile.exists() && logFile.length() > lastLogReadPos) {
                    try {
                        java.io.RandomAccessFile(logFile, "r").use { raf ->
                            raf.seek(lastLogReadPos)
                            var line = raf.readLine()
                            while (line != null) {
                                println("Main [DriverLog]: $line")
                                when {
                                    line.contains("Running in headless driver installation", ignoreCase = true) ->
                                        onProgress?.invoke(0.20f, "Preparing driver package...")
                                    line.contains("extract_binaries", ignoreCase = true) || line.contains("Preparing driver", ignoreCase = true) ->
                                        onProgress?.invoke(0.38f, "Extracting WinUSB driver files...")
                                    line.contains("creating and self-signing", ignoreCase = true) || line.contains("CreateCat", ignoreCase = true) ->
                                        onProgress?.invoke(0.58f, "Signing security certificates...")
                                    line.contains("Installing WinUSB driver", ignoreCase = true) ->
                                        onProgress?.invoke(0.78f, "Installing driver to Windows PnP...")
                                    line.contains("SUCCESS: WinUSB driver installed", ignoreCase = true) ->
                                        onProgress?.invoke(0.90f, "Configuring AOA mode...")
                                    line.contains("Pre-installing WinUSB for Google AOA Accessory", ignoreCase = true) || line.contains("18D1", ignoreCase = true) ->
                                        onProgress?.invoke(0.96f, "Verifying AOA driver store...")
                                }
                                line = raf.readLine()
                            }
                            lastLogReadPos = raf.filePointer
                        }
                    } catch (_: Exception) {}
                }

                val waitRes = Kernel32.INSTANCE.WaitForSingleObject(hProcess, pollIntervalMs)
                if (waitRes == WinBase.WAIT_OBJECT_0) {
                    exitFound = true
                    break
                }
                if (Kernel32.INSTANCE.GetExitCodeProcess(hProcess, exitCodeRef)) {
                    if (exitCodeRef.value != STILL_ACTIVE) {
                        exitFound = true
                        break
                    }
                }
            }

            // Flush any remaining log output
            if (logFile.exists() && logFile.length() > lastLogReadPos) {
                try {
                    java.io.RandomAccessFile(logFile, "r").use { raf ->
                        raf.seek(lastLogReadPos)
                        var line = raf.readLine()
                        while (line != null) {
                            println("Main [DriverLog]: $line")
                            line = raf.readLine()
                        }
                    }
                } catch (_: Exception) {}
            }

            if (exitFound && Kernel32.INSTANCE.GetExitCodeProcess(hProcess, exitCodeRef)) {
                if (exitCodeRef.value == 0 || exitCodeRef.value == 3010) {
                    onProgress?.invoke(1.0f, "Driver Installed Successfully!")
                }
                Result.Success(exitCodeRef.value)
            } else if (exitFound) {
                onProgress?.invoke(1.0f, "Driver Installed Successfully!")
                Result.Success(0)
            } else {
                val lastErr = Kernel32.INSTANCE.GetLastError()
                Result.Error(lastErr, "Elevated driver installation timed out after 90 seconds.")
            }
        } finally {
            Kernel32.INSTANCE.CloseHandle(hProcess)
        }
    }

    private fun validateTarget(target: ProcessTarget): Result.Error? {
        if (!target.executable.exists()) {
            return Result.Error(WinError.ERROR_FILE_NOT_FOUND, "Executable does not exist: ${target.executable.absolutePath}")
        }
        if (!target.executable.isFile) {
            return Result.Error(WinError.ERROR_FILE_NOT_FOUND, "Executable is not a file: ${target.executable.absolutePath}")
        }
        target.workingDirectory?.let {
            if (!it.isDirectory) {
                return Result.Error(WinError.ERROR_PATH_NOT_FOUND, "Working directory does not exist: ${it.absolutePath}")
            }
        }
        return null
    }
}

object ElevationTargetResolver {
    
    private const val MAIN_CLASS = "com.sanket.tools.nexpaddesktop.MainKt"

    fun resolveHelper(subArgs: String): WindowsElevation.ProcessTarget {
        val debugLog = StringBuilder()
        debugLog.appendLine("=== ElevationTargetResolver Debug ===")
        
        // 1. Try Compose Multiplatform / JPackage current executable directly (Java 9+)
        val currentCommand = ProcessHandle.current().info().command().orElse(null)
        debugLog.appendLine("currentCommand: $currentCommand")
        
        if (currentCommand != null && currentCommand.endsWith(".exe", ignoreCase = true)) {
            val exeFile = File(currentCommand)
            debugLog.appendLine("exeFile exists: ${exeFile.exists()}, name: ${exeFile.name}")
            if (exeFile.exists() && !exeFile.name.equals("java.exe", ignoreCase = true) && !exeFile.name.equals("javaw.exe", ignoreCase = true)) {
                // We are running inside a packaged wrapper (e.g. NEXPAD.exe)
                val target = WindowsElevation.ProcessTarget(
                    executable = exeFile,
                    arguments = subArgs
                )
                debugLog.appendLine("Selected Target 1 (Packaged EXE): ${target.executable.absolutePath} ${target.arguments}")
                File("C:\\Users\\Public\\nexpad_elevation_debug.txt").writeText(debugLog.toString())
                return target
            }
        }

        // 2. Legacy jpackage property fallback
        val jpackageAppPath = System.getProperty("jpackage.app-path")
        debugLog.appendLine("jpackage.app-path: $jpackageAppPath")
        if (jpackageAppPath != null && File(jpackageAppPath).exists()) {
            val target = WindowsElevation.ProcessTarget(
                executable = File(jpackageAppPath),
                arguments = subArgs
            )
            debugLog.appendLine("Selected Target 2 (jpackage.app-path): ${target.executable.absolutePath} ${target.arguments}")
            File("C:\\Users\\Public\\nexpad_elevation_debug.txt").writeText(debugLog.toString())
            return target
        }

        // 3. IDE / Gradle Fallback (Running via java.exe directly)
        // ShellExecuteEx has a ~2048-character limit on lpParameters.
        // In dev/Gradle mode, the classpath alone is 7000+ characters, causing silent truncation and failure.
        // We write a small helper batch file that executes java with the full classpath and captures all logs.
        val javaHome = File(System.getProperty("java.home"))
        var javaExe = File(javaHome, "bin/java.exe")
        if (!javaExe.exists()) {
            javaExe = File(javaHome, "bin/javaw.exe")
        }
        val classpath = System.getProperty("java.class.path")
        
        debugLog.appendLine("javaExe: ${javaExe.absolutePath}, exists: ${javaExe.exists()}")
        debugLog.appendLine("classpath: $classpath")

        val logFile = File("C:\\Users\\Public\\nexpad_driver_install.log")
        val batFile = File(System.getProperty("java.io.tmpdir"), "nexpad_driver_elevate.bat")

        val batContent = buildString {
            appendLine("@echo off")
            appendLine("chcp 65001 >nul")
            append("> \"${logFile.absolutePath}\" 2>&1 ")
            append("\"${javaExe.absolutePath}\" -cp ")
            append(quoteWindowsArgument(classpath))
            append(' ')
            append(MAIN_CLASS)
            if (subArgs.isNotBlank()) {
                append(' ')
                append(subArgs)
            }
            appendLine()
            appendLine("exit %ERRORLEVEL%")
        }
        batFile.writeText(batContent)

        val comSpec = System.getenv("COMSPEC") ?: "cmd.exe"
        val target = WindowsElevation.ProcessTarget(
            executable = File(comSpec),
            arguments = "/c \"${batFile.absolutePath}\""
        )
        debugLog.appendLine("Selected Target 3 (Batch Wrapper): ${target.executable.absolutePath} ${target.arguments}")
        File("C:\\Users\\Public\\nexpad_elevation_debug.txt").writeText(debugLog.toString())
        return target
    }

    private fun quoteWindowsArgument(value: String): String {
        if (value.isEmpty()) return "\"\""
        if (!value.any { it == ' ' || it == '\t' || it == '"' }) return value
        return "\"${value.replace("\"", "\\\"")}\""
    }
}
