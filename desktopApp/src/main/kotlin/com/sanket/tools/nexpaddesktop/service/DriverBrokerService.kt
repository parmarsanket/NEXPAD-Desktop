package com.sanket.tools.nexpaddesktop.service

import java.io.RandomAccessFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext

/**
 * The driver broker service runs as a background process (LocalSystem) to swap USB drivers
 * using libwdi without requiring UAC prompts every time.
 */
object DriverBrokerService {
    
    suspend fun start() = withContext(Dispatchers.IO) {
        println("DriverBrokerService: Starting named pipe server...")
        val pipeName = """\\.\pipe\nexpad-driver-ipc"""
        
        while (isActive) {
            try {
                // This blocks until a client connects
                val pipe = RandomAccessFile(pipeName, "rw")
                val command = pipe.readLine()
                
                if (command != null) {
                    println("DriverBrokerService: Received command: $command")
                    when {
                        command.startsWith("SWAP_TO_WINUSB") -> swapToWinUsb()
                        command.startsWith("RESTORE_MTP") -> restoreMtp()
                        else -> println("DriverBrokerService: Unknown command")
                    }
                    // Send ACK back
                    pipe.writeBytes("OK\n")
                }
                pipe.close()
            } catch (e: Exception) {
                delay(100) // Wait before retrying pipe creation
            }
        }
    }

    private fun swapToWinUsb() {
        println("DriverBrokerService: Executing swapToWinUsb via libwdi...")
        try {
            val libwdi = LibWdiBinding.INSTANCE
            
            // 1. Prepare WinUSB driver (generates INF and self-signs CAT)
            val prepOptions = LibWdiBinding.wdi_options_prepare()
            prepOptions.driver_type = LibWdiBinding.WDI_WINUSB
            prepOptions.vendor_name = "NEXPAD"
            prepOptions.device_name = "NEXPAD AOA Target"
            prepOptions.write() // Sync to native memory

            val prepResult = libwdi.wdi_prepare_driver(
                null, 
                "C:\\Windows\\Temp\\nexpad_usb", 
                "nexpad.inf", 
                prepOptions
            )
            println("DriverBrokerService: wdi_prepare_driver returned $prepResult")

            if (prepResult == 0) {
                // 2. Install the driver for our phone's specific VID:PID
                // Moto G85 uses VID 22B8 and PID 2E82 in MTP mode
                val installOptions = LibWdiBinding.wdi_options_install()
                installOptions.driver_type = LibWdiBinding.WDI_WINUSB
                installOptions.hwid = "USB\\VID_22B8&PID_2E82"
                installOptions.write()

                val installResult = libwdi.wdi_install_driver(
                    null,
                    "C:\\Windows\\Temp\\nexpad_usb",
                    "nexpad.inf",
                    installOptions
                )
                println("DriverBrokerService: wdi_install_driver returned $installResult")
            }
        } catch (e: Exception) {
            println("DriverBrokerService ERROR: Failed to call libwdi. Is libwdi.dll in the redist folder? Error: ${e.message}")
        }
    }

    private fun restoreMtp() {
        println("DriverBrokerService: Executing restoreMtp...")
        try {
            // Using pnputil to remove the injected WinUSB driver, Windows automatically falls back to MTP
            val process = Runtime.getRuntime().exec("pnputil.exe /delete-driver nexpad.inf /uninstall /force")
            process.waitFor()
            println("DriverBrokerService: restoreMtp pnputil returned ${process.exitValue()}")
        } catch (e: Exception) {
            println("DriverBrokerService ERROR: restoreMtp failed - ${e.message}")
        }
    }
}

fun main() = kotlinx.coroutines.runBlocking {
    println("NEXPAD Driver Service starting...")
    DriverBrokerService.start()
}
