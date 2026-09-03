package com.sanket.tools.nexpaddesktop.connection.usb.winusb

import com.sun.jna.ptr.PointerByReference
import java.io.File
import java.nio.file.Files
import kotlin.system.exitProcess

object WinUsbDriverManager {
    
    fun installWinUsb(vidHex: String, pidHex: String, miStr: String): Int {
        println("WinUsbDriverManager: Elevating to install WinUSB for VID:$vidHex PID:$pidHex MI:$miStr...")
        
        val vid = vidHex.toShort(16)
        val pid = pidHex.toShort(16)
        val mi: Byte? = if (miStr != "none") miStr.toByte() else null

        val listPtr = PointerByReference()
        val createOpts = LibWdiBinding.wdi_options_create_list().apply {
            list_all = 1
        }
        
        val createRes = LibWdiBinding.INSTANCE.wdi_create_list(listPtr, createOpts)
        if (createRes != 0 || listPtr.value == null) {
            println("WinUsbDriverManager ERROR: wdi_create_list failed: $createRes")
            return 1
        }
        
        var targetDevice: LibWdiBinding.wdi_device_info? = null
        try {
            var currPtr = listPtr.value
            while (currPtr != null) {
                val info = LibWdiBinding.wdi_device_info(currPtr)
                if (info.vid == vid && info.pid == pid) {
                    if (mi == null || info.mi == mi) {
                        targetDevice = info
                        break
                    }
                }
                currPtr = info.next
            }
            
            if (targetDevice == null) {
                println("WinUsbDriverManager ERROR: Could not find target device in Windows PnP tree.")
                return 1
            }
            
            println("WinUsbDriverManager: Found Target Hardware ID: ${targetDevice.hardware_id}")
            
            val secureTempDir = Files.createTempDirectory("nexpad_wdi_").toFile()
            secureTempDir.deleteOnExit()
            
            try {
                val infPath = secureTempDir.absolutePath
                val optionsPrepare = LibWdiBinding.wdi_options_prepare().apply {
                    this.driver_type = LibWdiBinding.WDI_WINUSB
                    this.vendor_name = "NEXPAD"
                }
                
                println("WinUsbDriverManager: Preparing driver at $infPath...")
                val prepRes = LibWdiBinding.INSTANCE.wdi_prepare_driver(
                    targetDevice, infPath, "nexpad.inf", optionsPrepare
                )
                
                if (prepRes != 0) {
                    println("WinUsbDriverManager ERROR: wdi_prepare_driver failed: $prepRes")
                    return 1
                }
                
                val optionsInstall = LibWdiBinding.wdi_options_install()
                println("WinUsbDriverManager: Installing WinUSB driver...")
                val installRes = LibWdiBinding.INSTANCE.wdi_install_driver(
                    targetDevice, infPath, "nexpad.inf", optionsInstall
                )
                
                if (installRes != 0) {
                    println("WinUsbDriverManager ERROR: wdi_install_driver failed: $installRes")
                    return 1
                }
                
                println("WinUsbDriverManager SUCCESS: WinUSB driver installed.")
                return 0
            } finally {
                secureTempDir.deleteRecursively()
            }
        } catch (e: Exception) {
            println("WinUsbDriverManager ERROR: ${e.message}")
            return 1
        } finally {
            LibWdiBinding.INSTANCE.wdi_destroy_list(listPtr.value)
        }
    }
}
