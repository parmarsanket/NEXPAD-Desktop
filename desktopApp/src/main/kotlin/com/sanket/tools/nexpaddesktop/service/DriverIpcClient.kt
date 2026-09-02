package com.sanket.tools.nexpaddesktop.service

import java.io.RandomAccessFile

class DriverIpcClient {
    fun sendCommand(command: String): Boolean {
        return try {
            val pipeName = """\\.\pipe\nexpad-driver-ipc"""
            val pipe = RandomAccessFile(pipeName, "rw")
            pipe.writeBytes(command + "\n")
            
            // Wait for OK
            val response = pipe.readLine()
            pipe.close()
            response == "OK"
        } catch (e: Exception) {
            println("IPC Client Error: ${e.message}")
            false
        }
    }
}
