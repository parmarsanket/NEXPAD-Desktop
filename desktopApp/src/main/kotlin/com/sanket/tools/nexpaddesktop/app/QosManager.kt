package com.sanket.tools.nexpaddesktop.app

import com.sun.jna.Native
import com.sun.jna.platform.win32.Kernel32
import com.sun.jna.platform.win32.WinDef
import com.sun.jna.win32.StdCallLibrary

interface WinMMQos : StdCallLibrary {
    fun timeBeginPeriod(uPeriod: Int): Int
    fun timeEndPeriod(uPeriod: Int): Int

    companion object {
        val INSTANCE: WinMMQos? by lazy {
            try { Native.load("winmm", WinMMQos::class.java) } catch (_: Exception) { null }
        }
    }
}

/**
 * Manages Windows real-time QoS optimizations:
 * 1. Process Priority -> HIGH_PRIORITY_CLASS (minimal jitter from background tasks)
 * 2. Multimedia Timer Resolution -> 1ms (smooth 120Hz/200Hz polling)
 */
object QosManager {

    fun applyWindowsQos() {
        if (!System.getProperty("os.name").lowercase().contains("win")) return

        // 1. Set Windows Process Priority to HIGH_PRIORITY_CLASS (0x00000080 = 128)
        try {
            val currentProcess = Kernel32.INSTANCE.GetCurrentProcess()
            Kernel32.INSTANCE.SetPriorityClass(currentProcess, WinDef.DWORD(128))
            println("⚡ [QoS] Windows Process Priority set to HIGH_PRIORITY_CLASS.")
        } catch (e: Exception) {
            println("⚠️ [QoS] Failed to set process priority: ${e.message}")
        }

        // 2. Set timer resolution to 1ms
        try {
            WinMMQos.INSTANCE?.timeBeginPeriod(1)
            println("⚡ [QoS] Windows Multimedia Timer resolution set to 1ms.")
        } catch (e: Exception) {
            println("⚠️ [QoS] Failed to set timer resolution: ${e.message}")
        }
    }

    fun restoreWindowsQos() {
        try {
            WinMMQos.INSTANCE?.timeEndPeriod(1)
        } catch (_: Exception) {}
    }
}
