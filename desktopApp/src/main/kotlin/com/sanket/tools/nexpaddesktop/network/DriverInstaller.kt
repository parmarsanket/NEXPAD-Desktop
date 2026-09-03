package com.sanket.tools.nexpaddesktop.network

import com.sun.jna.platform.win32.Kernel32
import java.io.File
import kotlin.system.exitProcess

/**
 * DriverInstaller contains the logic to install the WinUSB driver for AOA mode,
 * and restore the original MTP driver using pnputil.
 * 
 * This class is intended to be run in a brief, elevated context (e.g. via UAC prompt)
 * when launched with --install-driver or --restore-driver.
 */
object DriverInstaller {
    
    // Moto G85 MTP mode
    private const val VID: Short = 0x22B8.toShort()
    private const val PID: Short = 0x2E82.toShort()

    fun installWinUsb(vidHex: String, pidHex: String) {
        println("DriverInstaller: Elevating to install WinUSB for VID:$vidHex PID:$pidHex...")
        try {
            val deviceId = "USB\\VID_$vidHex&PID_$pidHex"
            
            // Generate driver INF
            val safeId = deviceId.replace("\\", "_").replace("&", "_")
            val infPath = System.getProperty("java.io.tmpdir") + "nexpad_usb_$safeId" + "_" + System.nanoTime()
            val infFile = File(infPath, "nexpad.inf")
            infFile.parentFile.mkdirs()
            
            val deviceInfo = LibWdiBinding.wdi_device_info().apply {
                this.vid = vidHex.toShort(16)
                this.pid = pidHex.toShort(16)
                this.device_id = deviceId
                this.hardware_id = deviceId
                this.desc = "NEXPAD USB Device"
            }
            
            val optionsPrepare = LibWdiBinding.wdi_options_prepare().apply {
                this.driver_type = LibWdiBinding.WDI_WINUSB
                this.vendor_name = "NEXPAD"
            }
            
            println("DriverInstaller: Preparing driver at $infPath...")
            var res = LibWdiBinding.INSTANCE.wdi_prepare_driver(
                deviceInfo,
                infPath,
                "nexpad.inf",
                optionsPrepare
            )
            if (res != 0) {
                println("DriverInstaller ERROR: wdi_prepare_driver failed: $res")
                Thread.sleep(5000)
                exitProcess(1)
            }
            
            val optionsInstall = LibWdiBinding.wdi_options_install()
            
            println("DriverInstaller: Installing driver for hardware ID: $deviceId...")
            res = LibWdiBinding.INSTANCE.wdi_install_driver(
                deviceInfo,
                infPath,
                "nexpad.inf",
                optionsInstall
            )
            if (res != 0) {
                println("DriverInstaller ERROR: wdi_install_driver failed: $res")
                Thread.sleep(5000)
                exitProcess(1)
            }
            
            println("DriverInstaller SUCCESS: WinUSB driver installed.")
            Thread.sleep(5000)
            exitProcess(0)
        } catch (e: Exception) {
            println("DriverInstaller ERROR: ${e.message}")
            e.printStackTrace()
            Thread.sleep(5000)
            exitProcess(1)
        }
    }

    fun restoreMtp() {
        println("DriverInstaller: Elevating to restore MTP driver...")
        try {
            // Uninstalls our custom WinUSB driver using pnputil
            // Windows will automatically fall back to the default MTP driver
            println("DriverInstaller: Running pnputil to delete nexpad.inf...")
            val proc = ProcessBuilder(
                "cmd.exe", "/c",
                "pnputil.exe /delete-driver nexpad.inf /uninstall /force"
            ).redirectErrorStream(true).start()
            
            val output = proc.inputStream.bufferedReader().readText()
            proc.waitFor()
            
            println("pnputil output:\n$output")
            println("DriverInstaller SUCCESS: MTP driver restored.")
            exitProcess(0)
        } catch (e: Exception) {
            println("DriverInstaller ERROR: ${e.message}")
            e.printStackTrace()
            exitProcess(1)
        }
    }
}
