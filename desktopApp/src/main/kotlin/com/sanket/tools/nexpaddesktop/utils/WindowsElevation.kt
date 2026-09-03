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

    sealed class Result {
        data class Success(val exitCode: Int) : Result()
        object UserCancelled : Result() // User clicked 'No' on the UAC prompt
        data class Error(val errorCode: Int, val message: String) : Result()
    }

    /**
     * Executes an elevated process without any flashing CMD/PowerShell windows.
     * Suspends asynchronously on Dispatchers.IO until the elevated process exits.
     */
    suspend fun runElevated(
        executablePath: String,
        arguments: String = "",
        workingDir: String? = null
    ): Result = withContext(Dispatchers.IO) {
        val sei = ShellAPI.SHELLEXECUTEINFO().apply {
            cbSize = size()
            fMask = 0x00000040 // SEE_MASK_NOCLOSEPROCESS
            lpVerb = "runas"
            lpFile = executablePath
            lpParameters = arguments
            lpDirectory = workingDir
            nShow = WinUser.SW_HIDE // completely hide window
        }

        val success = Shell32.INSTANCE.ShellExecuteEx(sei)
        if (!success) {
            val lastError = Kernel32.INSTANCE.GetLastError()
            return@withContext if (lastError == WinError.ERROR_CANCELLED) { // 1223
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

    fun getElevationTarget(subArgs: String): Pair<String, String> {
        val jpackageAppPath = System.getProperty("jpackage.app-path")
        if (jpackageAppPath != null && File(jpackageAppPath).exists()) {
            return Pair(jpackageAppPath, subArgs)
        }

        val javaHome = System.getProperty("java.home")
        val javaExe = "$javaHome\\bin\\java.exe"
        val classpath = System.getProperty("java.class.path")
        val mainClass = "com.sanket.tools.nexpaddesktop.MainKt"
        val arguments = "-cp \"$classpath\" $mainClass $subArgs"
        return Pair(javaExe, arguments)
    }
}
