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
    private const val SHELL_VERB_RUN_AS = "runas"

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
     * Suspends asynchronously on Dispatchers.IO until the elevated process exits.
     */
    suspend fun runElevated(target: ProcessTarget): Result = withContext(Dispatchers.IO) {
        validateTarget(target)?.let { return@withContext it }

        val sei = ShellAPI.SHELLEXECUTEINFO().apply {
            cbSize = size()
            fMask = SHELLEXECUTE_MASK_NOCLOSE_PROCESS
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
            Kernel32.INSTANCE.WaitForSingleObject(hProcess, WinBase.INFINITE)
            val exitCodeRef = IntByReference()
            if (Kernel32.INSTANCE.GetExitCodeProcess(hProcess, exitCodeRef)) {
                Result.Success(exitCodeRef.value)
            } else {
                val err = Kernel32.INSTANCE.GetLastError()
                Result.Error(err, "Failed to get process exit code (Win32: $err)")
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
        val javaHome = File(System.getProperty("java.home"))
        var javaExe = File(javaHome, "bin/java.exe")
        if (!javaExe.exists()) {
            javaExe = File(javaHome, "bin/javaw.exe")
        }
        val classpath = System.getProperty("java.class.path")
        
        debugLog.appendLine("javaExe: ${javaExe.absolutePath}, exists: ${javaExe.exists()}")
        debugLog.appendLine("classpath: $classpath")

        val commandLine = buildString {
            append("-cp ")
            append(quoteWindowsArgument(classpath))
            append(' ')
            append(MAIN_CLASS)
            if (subArgs.isNotBlank()) {
                append(' ')
                append(subArgs)
            }
        }

        val target = WindowsElevation.ProcessTarget(
            executable = javaExe,
            arguments = commandLine
        )
        debugLog.appendLine("Selected Target 3 (Java CLI): ${target.executable.absolutePath} ${target.arguments}")
        File("C:\\Users\\Public\\nexpad_elevation_debug.txt").writeText(debugLog.toString())
        return target
    }

    private fun quoteWindowsArgument(value: String): String {
        if (value.isEmpty()) return "\"\""
        if (!value.any { it == ' ' || it == '\t' || it == '"' }) return value
        return "\"${value.replace("\"", "\\\"")}\""
    }
}
