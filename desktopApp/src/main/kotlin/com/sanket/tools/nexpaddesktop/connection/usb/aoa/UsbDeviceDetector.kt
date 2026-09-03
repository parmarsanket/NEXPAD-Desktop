package com.sanket.tools.nexpaddesktop.connection.usb.aoa

import org.usb4java.Context
import org.usb4java.Device
import org.usb4java.DeviceDescriptor
import org.usb4java.DeviceList
import org.usb4java.LibUsb

sealed interface CandidateUsbDevice {
    val device: Device
    val vid: Short
    val pid: Short
    
    fun unref() {
        LibUsb.unrefDevice(device)
    }
    
    data class AoaDevice(
        override val device: Device, 
        override val vid: Short, 
        override val pid: Short
    ) : CandidateUsbDevice

    data class AndroidDevice(
        override val device: Device, 
        override val vid: Short, 
        override val pid: Short,
        val mtpInterfaceNumber: Int?
    ) : CandidateUsbDevice
}

object UsbDeviceDetector {

    private val ACCESSORY_PIDS = setOf<Short>(0x2D00, 0x2D01, 0x2D04, 0x2D05)
    
    // Cache for dynamically verified WPD (Windows Portable Devices)
    private val verifiedWpdDevices = mutableSetOf<String>()
    
    private var lastPnpUtilCheckTime = 0L
    private var cachedPnpUtilOutput = ""

    /**
     * Dynamically asks Windows if a specific VID/PID is currently registered as a WPD (MTP) device.
     * This perfectly bypasses the Windows Descriptor block without hardcoding ANY brands!
     */
    private fun isWindowsPortableDevice(vidHex: String, pidHex: String): Boolean {
        val deviceId = "$vidHex&PID_$pidHex".lowercase()
        if (verifiedWpdDevices.contains(deviceId)) return true

        val now = System.currentTimeMillis()
        // Run pnputil at most once every 5 seconds to avoid CPU spikes, but allow dynamic detection
        // when a user changes their phone from 'Charging' to 'File Transfer' (MTP)
        if (now - lastPnpUtilCheckTime > 5000) {
            try {
                val proc = ProcessBuilder("pnputil.exe", "/enum-devices", "/connected", "/class", "WPD")
                    .redirectErrorStream(true)
                    .start()
                cachedPnpUtilOutput = proc.inputStream.bufferedReader().readText().lowercase()
                proc.waitFor()
                lastPnpUtilCheckTime = now
            } catch (e: Exception) {
                println("[USB/Detector] Failed to query pnputil - ${e.message}")
            }
        }
        
        if (cachedPnpUtilOutput.contains(deviceId)) {
            verifiedWpdDevices.add(deviceId)
            return true
        }
        
        return false
    }

    /**
     * Enumerates USB devices and identifies Android phones and AOA devices based on classes/PIDs.
     * CALLER IS RESPONSIBLE for calling .unref() on every candidate returned by this method!
     */
    fun findCandidates(context: Context): List<CandidateUsbDevice> {
        val candidates = mutableListOf<CandidateUsbDevice>()
        val deviceList = DeviceList()
        
        if (LibUsb.getDeviceList(context, deviceList) < 0) return emptyList()

        try {
            for (device in deviceList) {
                val desc = DeviceDescriptor()
                if (LibUsb.getDeviceDescriptor(device, desc) != LibUsb.SUCCESS) {
                    println("[USB/Detector] Failed to get device descriptor for a device.")
                    continue
                }

                val vid = desc.idVendor()
                val pid = desc.idProduct()
                val vidHex = String.format("%04X", vid)
                val pidHex = String.format("%04X", pid)

                // Skip common PC hubs/controllers (AMD, Intel, ASMedia) to reduce log spam
                if (vid == 0x1022.toShort() || vid == 0x8086.toShort() || vid == 0x1B21.toShort()) continue

                if (vid == 0x18D1.toShort() && ACCESSORY_PIDS.contains(pid)) {
                    // We DO NOT log here anymore to prevent 1-second log spam!
                    // AoaManager will handle the state transition logging.
                    LibUsb.refDevice(device)
                    candidates.add(CandidateUsbDevice.AoaDevice(device, vid, pid))
                    continue
                }

                val mtpInterface = findMtpInterface(device, vid, pid)
                val adbInterface = findAdbInterface(device, vid, pid)
                
                // Fallback: If Windows hides the interfaces, ask Windows natively if it's a Portable Device (MTP)
                val isWpdAndroid = if (mtpInterface == null && adbInterface == null) isWindowsPortableDevice(vidHex, pidHex) else false
                
                if (mtpInterface != null || adbInterface != null || isWpdAndroid) {
                    LibUsb.refDevice(device)
                    candidates.add(CandidateUsbDevice.AndroidDevice(device, vid, pid, mtpInterface))
                }
            }
        } finally {
            LibUsb.freeDeviceList(deviceList, true)
        }
        return candidates
    }

    private fun findMtpInterface(device: Device, vid: Short, pid: Short): Int? {
        val config = org.usb4java.ConfigDescriptor()
        if (LibUsb.getActiveConfigDescriptor(device, config) < 0) return null
        try {
            val ifaces = config.iface() ?: return null
            for (iface in ifaces) {
                val alts = iface.altsetting() ?: continue
                for (alt in alts) {
                    val intClass = alt.bInterfaceClass().toInt() and 0xFF
                    val intSubClass = alt.bInterfaceSubClass().toInt() and 0xFF
                    val intProtocol = alt.bInterfaceProtocol().toInt() and 0xFF
                    
                    if (intClass == LibUsb.CLASS_IMAGE.toInt() && intSubClass == 0x01 && intProtocol == 0x01) {
                        return (alt.bInterfaceNumber().toInt() and 0xFF)
                    }
                    if (intClass == LibUsb.CLASS_VENDOR_SPEC.toInt() && intSubClass == 0xFF && intProtocol == 0x00) {
                        return (alt.bInterfaceNumber().toInt() and 0xFF)
                    }
                }
            }
            return null
        } finally {
            LibUsb.freeConfigDescriptor(config)
        }
    }

    private fun findAdbInterface(device: Device, vid: Short, pid: Short): Int? {
        val config = org.usb4java.ConfigDescriptor()
        if (LibUsb.getActiveConfigDescriptor(device, config) < 0) return null
        try {
            val ifaces = config.iface() ?: return null
            for (iface in ifaces) {
                val alts = iface.altsetting() ?: continue
                for (alt in alts) {
                    val intClass = alt.bInterfaceClass().toInt() and 0xFF
                    val intSubClass = alt.bInterfaceSubClass().toInt() and 0xFF
                    val intProtocol = alt.bInterfaceProtocol().toInt() and 0xFF
                    
                    if (intClass == LibUsb.CLASS_VENDOR_SPEC.toInt() && intSubClass == 0x42 && intProtocol == 0x01) {
                        return (alt.bInterfaceNumber().toInt() and 0xFF)
                    }
                }
            }
            return null
        } finally {
            LibUsb.freeConfigDescriptor(config)
        }
    }
}
