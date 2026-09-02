package com.sanket.tools.nexpaddesktop.service

import com.sun.jna.Library
import com.sun.jna.Native
import com.sun.jna.Pointer
import com.sun.jna.Structure

/**
 * JNA Bindings for libwdi (Windows Driver Installer)
 * Allows us to programmatically install WinUSB for the AOA handshake without UAC prompts
 * (assuming the background service is running as LocalSystem/Admin).
 */
interface LibWdiBinding : Library {
    companion object {
        val INSTANCE: LibWdiBinding by lazy {
            Native.load("libwdi", LibWdiBinding::class.java)
        }
        
        // WDI Constants
        const val WDI_WINUSB = 0
    }

    @Structure.FieldOrder("driver_type", "vendor_name", "device_name", "desc", "extra_lines", "buses")
    open class wdi_options_prepare : Structure() {
        @JvmField var driver_type: Int = 0
        @JvmField var vendor_name: String? = null
        @JvmField var device_name: String? = null
        @JvmField var desc: String? = null
        @JvmField var extra_lines: String? = null
        @JvmField var buses: Pointer? = null // PWDI_BUS_PREPARE array
    }

    @Structure.FieldOrder("hwid", "is_extension", "driver_type", "inf_name", "buses", "pending_install", "cancel_install")
    open class wdi_options_install : Structure() {
        @JvmField var hwid: String? = null
        @JvmField var is_extension: Byte = 0
        @JvmField var driver_type: Int = 0
        @JvmField var inf_name: String? = null
        @JvmField var buses: Pointer? = null // PWDI_BUS_INSTALL array
        @JvmField var pending_install: Pointer? = null // BOOL*
        @JvmField var cancel_install: Pointer? = null // BOOL*
    }

    // Creates a list of all USB devices
    fun wdi_create_list(list: Pointer?, options: Pointer?): Int
    
    // Destroys the list
    fun wdi_destroy_list(list: Pointer?): Int
    
    // Prepares a WinUSB driver INF file in the specified directory
    fun wdi_prepare_driver(device_info: Pointer?, path: String?, inf_name: String?, options: wdi_options_prepare?): Int
    
    // Installs the driver (requires Admin)
    fun wdi_install_driver(device_info: Pointer?, path: String?, inf_name: String?, options: wdi_options_install?): Int
}
