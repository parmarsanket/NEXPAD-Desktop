package com.sanket.tools.nexpaddesktop.network

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
    
    @Structure.FieldOrder("next", "vid", "pid", "is_composite", "mi", "desc", "driver", "device_id", "hardware_id", "compatible_id", "upper_filter", "driver_version")
    open class wdi_device_info : Structure() {
        @JvmField var next: Pointer? = null
        @JvmField var vid: Short = 0
        @JvmField var pid: Short = 0
        @JvmField var is_composite: Int = 0
        @JvmField var mi: Byte = 0
        @JvmField var desc: String? = null
        @JvmField var driver: String? = null
        @JvmField var device_id: String? = null
        @JvmField var hardware_id: String? = null
        @JvmField var compatible_id: String? = null
        @JvmField var upper_filter: String? = null
        @JvmField var driver_version: Long = 0
    }

    @Structure.FieldOrder("driver_type", "vendor_name", "device_guid", "disable_cat", "disable_signing", "cert_subject", "use_wcid_driver", "external_inf")
    open class wdi_options_prepare : Structure() {
        @JvmField var driver_type: Int = 0
        @JvmField var vendor_name: String? = null
        @JvmField var device_guid: String? = null
        @JvmField var disable_cat: Int = 0
        @JvmField var disable_signing: Int = 0
        @JvmField var cert_subject: String? = null
        @JvmField var use_wcid_driver: Int = 0
        @JvmField var external_inf: Int = 0
    }

    @Structure.FieldOrder("hWnd", "install_filter_driver", "pending_install_timeout")
    open class wdi_options_install : Structure() {
        @JvmField var hWnd: Pointer? = null
        @JvmField var install_filter_driver: Int = 0
        @JvmField var pending_install_timeout: Int = 0
    }

    // Creates a list of all USB devices
    fun wdi_create_list(list: Pointer?, options: Pointer?): Int
    
    // Destroys the list
    fun wdi_destroy_list(list: Pointer?): Int
    
    // Prepares a WinUSB driver INF file in the specified directory
    fun wdi_prepare_driver(device_info: wdi_device_info?, path: String?, inf_name: String?, options: wdi_options_prepare?): Int
    
    // Installs the driver (requires Admin)
    fun wdi_install_driver(device_info: wdi_device_info?, path: String?, inf_name: String?, options: wdi_options_install?): Int
}
