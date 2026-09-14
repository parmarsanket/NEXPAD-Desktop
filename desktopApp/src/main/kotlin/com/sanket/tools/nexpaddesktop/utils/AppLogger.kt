package com.sanket.tools.nexpaddesktop.utils

import androidx.compose.runtime.mutableStateOf
import java.io.PrintStream
import java.util.Collections

data class LogEntry(val message: String, var count: Int = 1) {
    fun displayString(): String = if (count > 1) "$message (x$count)" else message
}

object AppLogger {
    private val rawLogs = Collections.synchronizedList(ArrayList<LogEntry>())
    val updateTrigger = mutableStateOf(0)
    
    fun getLogsCopy(): List<String> {
        synchronized(rawLogs) {
            return rawLogs.map { it.displayString() }
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

    private fun addLog(message: String) {
        synchronized(rawLogs) {
            val lastLog = rawLogs.lastOrNull()
            if (lastLog != null && lastLog.message == message) {
                lastLog.count++
            } else {
                if (rawLogs.size > 2000) {
                    rawLogs.subList(0, 500).clear()
                }
                rawLogs.add(LogEntry(message))
            }
        }
        // Increment trigger to notify UI to re-read getLogsCopy()
        updateTrigger.value++
    }
}
