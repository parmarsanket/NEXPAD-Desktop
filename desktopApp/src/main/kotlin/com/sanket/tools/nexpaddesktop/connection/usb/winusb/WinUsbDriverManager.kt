package com.sanket.tools.nexpaddesktop.connection.usb.winusb

import java.io.File
import kotlin.system.exitProcess

object WinUsbDriverManager {
    
    private fun installForDevice(vidHex: String, pidHex: String, miStr: String): Int {
        try {
            val isComposite = miStr != "none"
            val deviceId = if (isComposite) {
                "USB\\VID_$vidHex&PID_$pidHex&MI_$miStr"
            } else {
                "USB\\VID_$vidHex&PID_$pidHex"
            }
            
            val safeId = deviceId.replace("\\", "_").replace("&", "_")
            val infPath = System.getProperty("java.io.tmpdir") + "nexpad_usb_$safeId" + "_" + System.nanoTime()
            val infFile = File(infPath, "nexpad.inf")
            infFile.parentFile.mkdirs()
            
            val targetDevice = LibWdiBinding.wdi_device_info().apply {
                this.vid = vidHex.toShort(16)
                this.pid = pidHex.toShort(16)
                this.device_id = deviceId
                this.hardware_id = deviceId
                this.desc = "NEXPAD USB Device"
                this.is_composite = if (isComposite) 1 else 0
                this.mi = if (isComposite) miStr.toByte() else 0
            }
            
            println("[WinUSB/Driver] Forged Target Hardware ID: ${targetDevice.hardware_id}")
            
            try {
                val optionsPrepare = LibWdiBinding.wdi_options_prepare().apply {
                    this.driver_type = LibWdiBinding.WDI_WINUSB
                    this.vendor_name = "NEXPAD"
                }
                
                println("[WinUSB/Driver] Preparing driver at $infPath...")
                val prepRes = LibWdiBinding.INSTANCE.wdi_prepare_driver(
                    targetDevice, infPath, "nexpad.inf", optionsPrepare
                )
                
                if (prepRes != 0) {
                    val errDesc = try { LibWdiBinding.INSTANCE.wdi_strerror(prepRes) } catch (_: Throwable) { "$prepRes" }
                    println("[WinUSB/Driver] ERROR: wdi_prepare_driver failed: $prepRes ($errDesc)")
                    return 1
                }
                
                val optionsInstall = LibWdiBinding.wdi_options_install()
                println("[WinUSB/Driver] Installing WinUSB driver...")
                val installRes = LibWdiBinding.INSTANCE.wdi_install_driver(
                    targetDevice, infPath, "nexpad.inf", optionsInstall
                )
                
                if (installRes != 0) {
                    val errDesc = try { LibWdiBinding.INSTANCE.wdi_strerror(installRes) } catch (_: Throwable) { "$installRes" }
                    println("[WinUSB/Driver] ERROR: wdi_install_driver failed: $installRes ($errDesc)")
                    return 1
                }
                
                println("[WinUSB/Driver] SUCCESS: WinUSB driver installed for $deviceId.")
                return 0
            } catch (e: Exception) {
                println("[WinUSB/Driver] ERROR during driver operations: ${e.message}")
                return 1
            }
        } catch (e: Exception) {
            println("[WinUSB/Driver] ERROR: ${e.message}")
            return 1
        }
    }

    fun installWinUsb(vidHex: String, pidHex: String, miStr: String): Int {
        println("[WinUSB/Driver] Elevating to install WinUSB for VID:$vidHex PID:$pidHex MI:$miStr...")
        val res = installForDevice(vidHex, pidHex, miStr)
        if (res != 0) return res

        // Pre-install WinUSB for Google AOA Accessory (18D1:2D00 and 18D1:2D01)
        // so Phase 2 AOA accessory re-enumeration succeeds without requiring a second elevation prompt!
        if (!vidHex.equals("18D1", ignoreCase = true)) {
            println("[WinUSB/Driver] Pre-installing WinUSB for AOA Accessory (VID:18D1 PID:2D00)...")
            try { installForDevice("18D1", "2D00", "none") } catch (_: Exception) {}
            try { installForDevice("18D1", "2D01", "none") } catch (_: Exception) {}
        }
        return 0
    }
}
