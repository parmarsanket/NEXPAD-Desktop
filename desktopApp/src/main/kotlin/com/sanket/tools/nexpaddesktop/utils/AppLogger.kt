package com.sanket.tools.nexpaddesktop.utils

import androidx.compose.runtime.mutableStateOf
import java.io.PrintStream
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Collections
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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

enum class LogLevel(val displayName: String, val badge: String) {
    ALL("All", "ALL"),
    VERBOSE("Verbose", "VRB"),
    INFO("Info", "INF"),
    WARN("Warning", "WRN"),
    ERROR("Error", "ERR")
}

data class LogEntry(
    val id: Long = 0L,
    val timestamp: String = "",
    val message: String,
    val category: LogCategory = LogCategory.SYSTEM,
    val level: LogLevel = LogLevel.INFO,
    var count: Int = 1
) {
    fun displayString(): String {
        val timePrefix = if (timestamp.isNotBlank()) "[$timestamp] " else ""
        val levelPrefix = "[${level.badge}] "
        val catPrefix = "[${category.badge}] "
        return if (count > 1) "$timePrefix$levelPrefix$catPrefix$message (x$count)" else "$timePrefix$levelPrefix$catPrefix$message"
    }
}

object AppLogger {
    private const val MAX_LOGS = 2000
    private val rawLogs = ArrayDeque<LogEntry>(MAX_LOGS)
    val updateTrigger = mutableStateOf(0)
    private var nextId = 1L
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")

    private val loggerScope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Default + kotlinx.coroutines.SupervisorJob())
    private val pendingUiNotification = java.util.concurrent.atomic.AtomicBoolean(false)

    private fun requestUiNotification() {
        if (pendingUiNotification.compareAndSet(false, true)) {
            loggerScope.launch {
                kotlinx.coroutines.delay(100L) // 100ms batch coalesce window (max 10 UI ticks/sec)
                pendingUiNotification.set(false)
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    updateTrigger.value++
                }
            }
        }
    }
    
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
                if (x != null) addLog(x, isErrStream = false)
            }
            override fun print(x: String?) {
                super.print(x)
                if (x != null) addLog(x, isErrStream = false)
            }
        })

        val originalErr = System.err
        System.setErr(object : PrintStream(originalErr) {
            override fun println(x: String?) {
                super.println(x)
                if (x != null) addLog(x, isErrStream = true)
            }
            override fun print(x: String?) {
                super.print(x)
                if (x != null) addLog(x, isErrStream = true)
            }
        })
    }

    private fun classifyCategory(message: String): LogCategory {
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

    private fun classifyLevel(message: String, isErrStream: Boolean): LogLevel {
        if (isErrStream) return LogLevel.ERROR
        val upper = message.uppercase()
        return when {
            upper.contains("ERROR") || upper.contains("❌") || upper.contains("EXCEPTION") || upper.contains("FAILED") -> LogLevel.ERROR
            upper.contains("WARN") || upper.contains("⚠️") || upper.contains("DROPPED") || upper.contains("TIMEOUT") -> LogLevel.WARN
            upper.contains("DEBUG") || upper.contains("VRB") || upper.contains("VERBOSE") -> LogLevel.VERBOSE
            else -> LogLevel.INFO
        }
    }

    private fun addLog(message: String, isErrStream: Boolean = false) {
        val trimmed = message.trimEnd()
        if (trimmed.isEmpty()) return

        val now = LocalTime.now().format(timeFormatter)
        val category = if (isErrStream) LogCategory.ERROR else classifyCategory(trimmed)
        val level = classifyLevel(trimmed, isErrStream)

        synchronized(rawLogs) {
            val lastLog = rawLogs.lastOrNull()
            if (lastLog != null && lastLog.message == trimmed) {
                lastLog.count++
            } else {
                while (rawLogs.size >= MAX_LOGS) {
                    rawLogs.removeFirst()
                }
                rawLogs.addLast(
                    LogEntry(
                        id = nextId++,
                        timestamp = now,
                        message = trimmed,
                        category = category,
                        level = level
                    )
                )
            }
        }
        // Throttled notification: batches high-throughput logs to max 10Hz UI updates on Main dispatcher
        requestUiNotification()
    }

    fun clearLogs() {
        synchronized(rawLogs) {
            rawLogs.clear()
        }
        loggerScope.launch(kotlinx.coroutines.Dispatchers.Main) {
            updateTrigger.value++
        }
    }

    fun exportLogsToFile(): java.io.File {
        val timestamp = java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
        val homeDir = java.io.File(System.getProperty("user.home"))
        val downloadsDir = java.io.File(homeDir, "Downloads")
        val targetDir = if (downloadsDir.exists() && downloadsDir.isDirectory) downloadsDir else java.io.File(".")
        val file = java.io.File(targetDir, "nexpad_debug_session_$timestamp.log")
        
        val sb = StringBuilder()
        sb.appendLine("================================================================================")
        sb.appendLine("           NEXPAD DESKTOP ENGINE — DIAGNOSTIC TELEMETRY LOG                     ")
        sb.appendLine("================================================================================")
        sb.appendLine("Session Generated: ${java.time.LocalDateTime.now()}")
        sb.appendLine("Host Platform:     ${System.getProperty("os.name")} (${System.getProperty("os.arch")})")
        sb.appendLine("Java Runtime:      ${System.getProperty("java.version")} by ${System.getProperty("java.vendor")}")
        sb.appendLine("Total Entries:     ${rawLogs.size}")
        sb.appendLine("================================================================================")
        sb.appendLine("")
        
        synchronized(rawLogs) {
            rawLogs.forEach { entry ->
                val countStr = if (entry.count > 1) " [x${entry.count}]" else ""
                sb.appendLine(String.format("%04d [%s] [%-5s] [%-7s] %s%s", 
                    entry.id % 10000, 
                    entry.timestamp, 
                    entry.level.badge, 
                    entry.category.badge, 
                    entry.message, 
                    countStr
                ))
            }
        }
        
        file.writeText(sb.toString(), Charsets.UTF_8)
        return file
    }
}
