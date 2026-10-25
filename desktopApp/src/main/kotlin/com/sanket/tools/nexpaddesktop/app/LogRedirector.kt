package com.sanket.tools.nexpaddesktop.app

import com.sanket.tools.nexpaddesktop.utils.AppLogger
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.io.PrintStream

/**
 * Handles global console redirection to in-app memory logs and headless file logging.
 */
object LogRedirector {

    fun init() {
        AppLogger.initGlobalRedirect()
    }

    /**
     * Guarantees file logging to C:\Users\Public\nexpad_driver_install.log even in packaged GUI EXE mode
     * where no console is attached.
     */
    fun attachHeadlessDriverLog() {
        try {
            val logFile = File("C:\\Users\\Public\\nexpad_driver_install.log")
            val fileOut = PrintStream(FileOutputStream(logFile, false), true, "UTF-8")
            val origOut = System.out
            val origErr = System.err
            System.setOut(PrintStream(object : OutputStream() {
                override fun write(b: Int) { origOut.write(b); fileOut.write(b) }
                override fun write(b: ByteArray, off: Int, len: Int) { origOut.write(b, off, len); fileOut.write(b, off, len) }
                override fun flush() { origOut.flush(); fileOut.flush() }
            }, true, "UTF-8"))
            System.setErr(PrintStream(object : OutputStream() {
                override fun write(b: Int) { origErr.write(b); fileOut.write(b) }
                override fun write(b: ByteArray, off: Int, len: Int) { origErr.write(b, off, len); fileOut.write(b, off, len) }
                override fun flush() { origErr.flush(); fileOut.flush() }
            }, true, "UTF-8"))
        } catch (_: Exception) {}
    }
}
