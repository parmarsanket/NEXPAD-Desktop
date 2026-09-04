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
    
    // Known Android Manufacturer Vendor IDs (covers >99% of global Android devices)
    private val KNOWN_ANDROID_VIDS = setOf<Short>(
        0x18D1.toShort(), // Google
        0x22B8.toShort(), // Motorola
        0x04E8.toShort(), // Samsung
        0x2717.toShort(), // Xiaomi
        0x22D9.toShort(), // OPPO / OnePlus
        0x2A70.toShort(), // Realme
        0x2B7E.toShort(), // Vivo
        0x12D1.toShort(), // Huawei / Honor
        0x0FCE.toShort(), // Sony
        0x1004.toShort(), // LG
        0x0BB4.toShort(), // HTC
        0x0B05.toShort(), // ASUS
        0x2E04.toShort(), // Nothing Phone
        0x0E8D.toShort(), // MediaTek Reference
        0x05C6.toShort()  // Qualcomm Reference
    )

    // Cache for dynamically verified Android/WPD/WinUSB devices
    private val verifiedAndroidDevices = mutableSetOf<String>()
    
    private var lastPnpUtilCheckTime = 0L
    private var cachedPnpUtilOutput = ""

    /**
     * Dynamically asks Windows if a specific VID/PID is currently registered as a WPD (MTP) device
     * OR as a WinUSB device (e.g. previously installed by NEXPAD).
     */
    private fun isWindowsAndroidPnpDevice(vidHex: String, pidHex: String): Boolean {
        val deviceId = "$vidHex&PID_$pidHex".lowercase()
        if (verifiedAndroidDevices.contains(deviceId)) return true

        val now = System.currentTimeMillis()
        // Query pnputil at most once every 5 seconds to avoid CPU spikes
        if (now - lastPnpUtilCheckTime > 5000) {
            try {
                // Query both WPD (fresh MTP devices) and USBDevice (devices with WinUSB already bound)
                val procWpd = ProcessBuilder("pnputil.exe", "/enum-devices", "/connected", "/class", "WPD")
                    .redirectErrorStream(true)
                    .start()
                val outWpd = procWpd.inputStream.bufferedReader().readText().lowercase()
                procWpd.waitFor()

                val procUsb = ProcessBuilder("pnputil.exe", "/enum-devices", "/connected", "/class", "USBDevice")
                    .redirectErrorStream(true)
                    .start()
                val outUsb = procUsb.inputStream.bufferedReader().readText().lowercase()
                procUsb.waitFor()

                cachedPnpUtilOutput = "$outWpd\n$outUsb"
                lastPnpUtilCheckTime = now
            } catch (e: Exception) {
                println("[USB/Detector] Failed to query pnputil - ${e.message}")
            }
        }
        
        if (cachedPnpUtilOutput.contains(deviceId)) {
            verifiedAndroidDevices.add(deviceId)
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
                    continue
                }

                val vid = desc.idVendor()
                val pid = desc.idProduct()
                val vidHex = String.format("%04X", vid)
                val pidHex = String.format("%04X", pid)

                // Skip common PC hubs/controllers (AMD, Intel, ASMedia) to reduce log spam
                if (vid == 0x1022.toShort() || vid == 0x8086.toShort() || vid == 0x1B21.toShort()) continue

                // 1. Check if device is already in AOA Accessory Mode
                if (vid == 0x18D1.toShort() && ACCESSORY_PIDS.contains(pid)) {
                    LibUsb.refDevice(device)
                    candidates.add(CandidateUsbDevice.AoaDevice(device, vid, pid))
                    continue
                }

                // 2. Check if device is an Android phone (known vendor, MTP descriptor, ADB descriptor, or PnP WPD/USBDevice)
                val isKnownAndroidVendor = KNOWN_ANDROID_VIDS.contains(vid)
                val mtpInterface = findMtpInterface(device, vid, pid)
                val adbInterface = if (mtpInterface == null && !isKnownAndroidVendor) findAdbInterface(device, vid, pid) else null
                val isPnpAndroid = if (mtpInterface == null && adbInterface == null && !isKnownAndroidVendor) {
                    isWindowsAndroidPnpDevice(vidHex, pidHex)
                } else false
                
                if (isKnownAndroidVendor || mtpInterface != null || adbInterface != null || isPnpAndroid) {
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
