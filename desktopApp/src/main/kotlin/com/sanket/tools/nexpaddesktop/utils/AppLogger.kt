package com.sanket.tools.nexpaddesktop.utils

import androidx.compose.runtime.mutableStateOf
import java.io.PrintStream
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Collections

enum class LogCategory(val displayName: String, val badge: String) {
    ALL("All", "ALL"),
    NETWORK("Network", "NET"),
    HANDSHAKE("Handshake", "INIT"),
    INPUT("Input", "INPUT"),
    MOTION("Motion", "IMU"),
    DRIVER("Driver", "VIGEM"),
    HAPTICS("Haptics", "HAPTIC"),
    WARNING("Warnings", "WARN"),
    ERROR("Errors", "ERR"),
    SYSTEM("System", "SYS")
}

data class LogEntry(
    val id: Long = 0L,
    val timestamp: String = "",
    val message: String,
    val category: LogCategory = LogCategory.SYSTEM,
    var count: Int = 1
) {
    fun displayString(): String {
        val timePrefix = if (timestamp.isNotBlank()) "[$timestamp] " else ""
        return if (count > 1) "$timePrefix$message (x$count)" else "$timePrefix$message"
    }
}

object AppLogger {
    private val rawLogs = Collections.synchronizedList(ArrayList<LogEntry>())
    val updateTrigger = mutableStateOf(0)
    private var nextId = 1L
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")
    
    fun getLogsCopy(): List<String> {
        synchronized(rawLogs) {
            return rawLogs.map { it.displayString() }
        }
    }

    fun getLogEntriesCopy(): List<LogEntry> {
        synchronized(rawLogs) {
            return ArrayList(rawLogs)
        }
    }
    
    fun initGlobalRedirect() {
        val originalOut = System.out
        System.setOut(object : PrintStream(originalOut) {
            override fun println(x: String?) {
                super.println(x)
                if (x != null) addLog(x)
            }
            override fun print(x: String?) {
                super.print(x)
                if (x != null) addLog(x)
            }
        })
    }

    private fun classify(message: String): LogCategory {
        val upper = message.uppercase()
        return when {
            upper.contains("ERROR") || upper.contains("❌") || upper.contains("EXCEPTION") || upper.contains("FAILED") -> LogCategory.ERROR
            upper.contains("WARN") || upper.contains("⚠️") || upper.contains("DROPPED") || upper.contains("TIMEOUT") -> LogCategory.WARNING
            upper.contains("HANDSHAKE") || upper.contains("🤝") || upper.contains("CONNECTED TO") -> LogCategory.HANDSHAKE
            upper.contains("HAPTIC") || upper.contains("RUMBLE") || upper.contains("FEEDBACK") || upper.contains("💥") -> LogCategory.HAPTICS
            upper.contains("DSU") || upper.contains("GYRO") || upper.contains("ACCEL") || upper.contains("IMU") || upper.contains("MOTION") || upper.contains("STEERING") -> LogCategory.MOTION
            upper.contains("VIGEM") || upper.contains("DRIVER") || upper.contains("XBOX") || upper.contains("DS4") || upper.contains("DUALSHOCK") -> LogCategory.DRIVER
            upper.contains("UDP") || upper.contains("NETWORK") || upper.contains("PACKET") || upper.contains("DISCONNECT") || upper.contains("PORT") || upper.contains("SOCKET") -> LogCategory.NETWORK
            upper.contains("INPUT") || upper.contains("BUTTON") || upper.contains("STICK") || upper.contains("TRIGGER") -> LogCategory.INPUT
            else -> LogCategory.SYSTEM
        }
    }

    private fun addLog(message: String) {
        val trimmed = message.trimEnd()
        if (trimmed.isEmpty()) return

        val now = LocalTime.now().format(timeFormatter)
        val category = classify(trimmed)

        synchronized(rawLogs) {
            val lastLog = rawLogs.lastOrNull()
            if (lastLog != null && lastLog.message == trimmed) {
                lastLog.count++
            } else {
                if (rawLogs.size > 2500) {
                    rawLogs.subList(0, 600).clear()
                }
                rawLogs.add(
                    LogEntry(
                        id = nextId++,
                        timestamp = now,
                        message = trimmed,
                        category = category
                    )
                )
            }
        }
        // Increment trigger to notify UI to re-read getLogsCopy() / getLogEntriesCopy()
        updateTrigger.value++
    }

    fun clearLogs() {
        synchronized(rawLogs) {
            rawLogs.clear()
        }
        updateTrigger.value++
    }
}
