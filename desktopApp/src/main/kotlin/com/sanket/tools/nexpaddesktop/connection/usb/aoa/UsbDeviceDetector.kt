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
    val friendlyName: String?
    
    fun unref() {
        LibUsb.unrefDevice(device)
    }
    
    data class AoaDevice(
        override val device: Device, 
        override val vid: Short, 
        override val pid: Short,
        override val friendlyName: String? = null
    ) : CandidateUsbDevice

    data class AndroidDevice(
        override val device: Device, 
        override val vid: Short, 
        override val pid: Short,
        val mtpInterfaceNumber: Int?,
        override val friendlyName: String? = null
    ) : CandidateUsbDevice
}

object UsbDeviceDetector {

    // Official Google Android Open Accessory standard PIDs (from Google AOA 2.0 Spec)
    private val ACCESSORY_PIDS = setOf<Short>(0x2D00, 0x2D01, 0x2D04, 0x2D05)
    
    // Dynamic cache of connected Android devices discovered via Windows PnP
    private val dynamicPnpAndroidDevices = mutableSetOf<String>()
    private val dynamicPnpAdbDevices = mutableSetOf<String>()
    private val dynamicPnpDeviceNames = mutableMapOf<String, String>()
    @Volatile var cachedLastPhoneName: String? = null
    
    private var lastPnpScanTime = 0L

    /**
     * Queries Windows PnP dynamically for connected Android devices.
     *
     * Detection is **fully protocol/class-based** — no vendor ID (VID) list is used.
     * Any phone from any brand is discovered through standard Windows device class identifiers:
     *
     *   1. Class WPD  ("Windows Portable Device") — the class Windows assigns to every MTP device.
     *      Covers stock Android in file-transfer / MTP mode.
     *
     *   2. Compatible ID "MS_COMP_MTP" present in the Instance ID — Microsoft's own MTP marker that
     *      appears on composite devices whose MTP interface is registered with the WPD stack.
     *      Example Instance ID: USB\VID_04E8&PID_6860&MS_COMP_MTP&SAMSUNG_ANDROID (Samsung Note 20).
     *      This is how Samsung (and other OEMs with proprietary composite drivers) show up even when
     *      they do NOT use the standard WPD class name.
     *
     *   3. Class AndroidUsbDeviceClass — ADB-enabled devices (USB Debugging ON). Tracked separately
     *      in [dynamicPnpAdbDevices] to exclude them from AOA processing.
     *
     *   4. NEXPAD WinUSB devices — phone already has the NEXPAD WinUSB driver bound.
     */
    private fun refreshPnpAndroidDevices() {
        val now = System.currentTimeMillis()
        if (now - lastPnpScanTime < 5000) return
        lastPnpScanTime = now

        try {
            val proc = ProcessBuilder("pnputil.exe", "/enum-devices", "/connected")
                .redirectErrorStream(true)
                .start()
            try { proc.outputStream.close() } catch (_: Throwable) {}
            val text = try {
                proc.inputStream.bufferedReader().use { it.readText() }
            } finally {
                if (!proc.waitFor(3, java.util.concurrent.TimeUnit.SECONDS)) {
                    proc.destroyForcibly()
                } else {
                    proc.destroy()
                }
            }

            dynamicPnpAndroidDevices.clear()
            dynamicPnpAdbDevices.clear()
            val blocks = text.split("Instance ID:")
            for (block in blocks) {
                val lower = block.lowercase()
                val isWpd = lower.contains("class name:                 wpd")
                val isAndroidClass = lower.contains("class name:                 androidusbdeviceclass")
                val isNexpadWinUsb = lower.contains("class name:                 usbdevice") &&
                                     (lower.contains("nexpad") || lower.contains("libusb.info") || lower.contains("winusb"))
                // MS_COMP_MTP appears in the Instance ID of every composite MTP device registered with
                // Windows, regardless of OEM.  This is the universal signal for phones using proprietary
                // composite drivers (Samsung, Vivo, OPPO, Xiaomi, etc.) that do NOT expose class WPD.
                val hasMtpCompatId = lower.contains("ms_comp_mtp")

                val vidMatch = Regex("vid_([0-9a-fA-F]{4})", RegexOption.IGNORE_CASE).find(block)
                val pidMatch = Regex("pid_([0-9a-fA-F]{4})", RegexOption.IGNORE_CASE).find(block)
                val descMatch = Regex("Device Description:\\s*([^\\r\\n]+)", RegexOption.IGNORE_CASE).find(block)
                val rawDesc = descMatch?.groupValues?.get(1)?.trim()

                if (vidMatch != null && pidMatch != null) {
                    val key = "${vidMatch.groupValues[1]}:${pidMatch.groupValues[1]}".uppercase()
                    if (isAndroidClass) {
                        dynamicPnpAdbDevices.add(key)
                    }
                    if (isWpd || isNexpadWinUsb || hasMtpCompatId) {
                        dynamicPnpAndroidDevices.add(key)
                    }
                    if (rawDesc != null && !rawDesc.contains("Composite", ignoreCase = true) && !rawDesc.equals("MTP USB Device", ignoreCase = true)) {
                        dynamicPnpDeviceNames[key] = rawDesc
                        cachedLastPhoneName = rawDesc
                    }
                }
            }
        } catch (e: Exception) {
            println("[USB/Detector] Failed to query pnputil - ${e.message}")
        }
    }

    /**
     * Enumerates USB devices and identifies Android phones and AOA devices based on classes/PIDs.
     * EXCLUDES any device that has USB Debugging (ADB) enabled, reserving it strictly for ADB Bridge!
     * CALLER IS RESPONSIBLE for calling .unref() on every candidate returned by this method!
     */
    fun findCandidates(context: Context): List<CandidateUsbDevice> {
        val candidates = mutableListOf<CandidateUsbDevice>()
        val deviceList = DeviceList()
        
        if (LibUsb.getDeviceList(context, deviceList) < 0) return emptyList()

        refreshPnpAndroidDevices()

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
                val deviceKey = "$vidHex:$pidHex".uppercase()

                // Skip common PC internal root hubs/controllers (AMD, Intel, ASMedia)
                if (vid == 0x1022.toShort() || vid == 0x8086.toShort() || vid == 0x1B21.toShort()) continue

                // 1. Google AOA Accessory Mode (standard 0x18D1:0x2D00..0x2D05)
                if (vid == 0x18D1.toShort() && ACCESSORY_PIDS.contains(pid)) {
                    LibUsb.refDevice(device)
                    val phoneName = dynamicPnpDeviceNames[deviceKey] ?: cachedLastPhoneName
                    candidates.add(CandidateUsbDevice.AoaDevice(device, vid, pid, phoneName))
                    continue
                }

                // Mutual exclusion: If USB Debugging is ON (device has ADB interface or AndroidUsbDeviceClass in PnP),
                // NEVER treat it as an AOA candidate! It belongs exclusively to ADB Bridge.
                val isAdb = dynamicPnpAdbDevices.contains(deviceKey) || findAdbInterface(device, vid, pid) != null
                if (isAdb) {
                    // USB Debugging is ON on this device! Skip AOA processing completely.
                    continue
                }

                // 2. Dynamic Discovery for devices WITHOUT USB Debugging:
                // - Descriptors expose standard MTP interface
                // - OR Windows PnP registers it under WPD or NEXPAD USBDevice
                val mtpInterface = findMtpInterface(device, vid, pid)
                val isPnpAndroid = dynamicPnpAndroidDevices.contains(deviceKey)

                if (mtpInterface != null || isPnpAndroid) {
                    LibUsb.refDevice(device)
                    val phoneName = dynamicPnpDeviceNames[deviceKey] ?: cachedLastPhoneName
                    candidates.add(CandidateUsbDevice.AndroidDevice(device, vid, pid, mtpInterface, phoneName))
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
                    
                    // Standard MTP (Class 6 / Sub 1 / Prot 1)
                    if (intClass == LibUsb.CLASS_IMAGE.toInt() && intSubClass == 0x01 && intProtocol == 0x01) {
                        return (alt.bInterfaceNumber().toInt() and 0xFF)
                    }
                    // Vendor-specific MTP (Class FF / Sub FF / Prot 0)
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
                    
                    // Standard Android ADB (Class FF / Sub 42 / Prot 1)
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
