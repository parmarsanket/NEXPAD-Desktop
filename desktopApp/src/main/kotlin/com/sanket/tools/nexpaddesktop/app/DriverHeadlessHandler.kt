package com.sanket.tools.nexpaddesktop.app

import com.sanket.tools.nexpaddesktop.connection.usb.winusb.WinUsbDriverManager
import kotlin.system.exitProcess

/**
 * Handles headless driver installation/reversal executed via elevated helper processes.
 */
object DriverHeadlessHandler {

    /**
     * Inspects CLI arguments. If headless driver commands are present, executes them
     * and terminates the process.
     *
     * @return true if a headless command was handled (process exited), false to proceed.
     */
    fun handleCommandLine(args: Array<String>): Boolean {
        // 1. Check for headless driver installation mode
        val installDriverIndex = args.indexOf("--install-driver")
        if (installDriverIndex != -1) {
            LogRedirector.attachHeadlessDriverLog()
            val vidHex = if (installDriverIndex + 1 < args.size) args[installDriverIndex + 1].trim() else ""
            val pidHex = if (installDriverIndex + 2 < args.size) args[installDriverIndex + 2].trim() else ""
            val rawMi = if (installDriverIndex + 3 < args.size) args[installDriverIndex + 3].trim() else "none"
            val miParam = if (rawMi.all { it.isDigit() }) rawMi else "none"

            if (vidHex.isBlank() || pidHex.isBlank()) {
                System.err.println("Main: Invalid arguments for --install-driver. VID or PID is missing.")
                exitProcess(1)
            }

            println("Main: Running in headless driver installation mode for VID: $vidHex PID: $pidHex MI: $miParam")
            val exitCode = WinUsbDriverManager.installWinUsb(vidHex, pidHex, miParam)
            exitProcess(exitCode)
        }

        // 2. Check for headless driver rollback / uninstall mode
        val revertDriverIndex = args.indexOfFirst { it == "--revert-driver" || it == "--uninstall-driver" }
        if (revertDriverIndex != -1) {
            LogRedirector.attachHeadlessDriverLog()
            val vidHex = if (revertDriverIndex + 1 < args.size) args[revertDriverIndex + 1].trim() else ""
            val pidHex = if (revertDriverIndex + 2 < args.size) args[revertDriverIndex + 2].trim() else ""
            if (vidHex.isBlank() || pidHex.isBlank()) {
                System.err.println("Main: Invalid arguments for --revert-driver. VID or PID is missing.")
                exitProcess(1)
            }
            println("Main: Running in headless driver rollback mode for VID: $vidHex PID: $pidHex")
            val exitCode = WinUsbDriverManager.revertWinUsb(vidHex, pidHex)
            exitProcess(exitCode)
        }

        return false
    }
}
