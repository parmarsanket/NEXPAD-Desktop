package com.sanket.tools.nexpaddesktop.connection.usb

import org.usb4java.Context
import org.usb4java.DeviceDescriptor
import org.usb4java.DeviceHandle
import org.usb4java.DeviceList
import org.usb4java.LibUsb

class ElevationRequiredException(val vid: Short, val pid: Short) : Exception("WinUSB Driver elevation required for VID:$vid PID:$pid")

object UsbScanner {

    /**
     * Checks if a device is likely an Android phone by inspecting its USB interface classes.
     * Android phones typically expose MTP (Class 6, Subclass 1, Protocol 1) 
     * or ADB (Class 255, Subclass 66, Protocol 1).
     */
    private fun isAndroidDevice(device: org.usb4java.Device): Boolean {
        val desc = DeviceDescriptor()
        LibUsb.getDeviceDescriptor(device, desc)
        val vidStr = String.format("%04X", desc.idVendor())
        val pidStr = String.format("%04X", desc.idProduct())

        val config = org.usb4java.ConfigDescriptor()
        val configResult = LibUsb.getConfigDescriptor(device, 0.toByte(), config)
        
        if (configResult >= 0) {
            val ifaces = config.iface()
            var isAndroid = false
            if (ifaces != null) {
                for (iface in ifaces) {
                    val alts = iface.altsetting()
                    if (alts != null && alts.size > 0) {
                        for (alt in alts) {
                            val cls = alt.bInterfaceClass().toInt() and 0xFF
                            val subCls = alt.bInterfaceSubClass().toInt() and 0xFF
                            val proto = alt.bInterfaceProtocol().toInt() and 0xFF

                            // MTP / PTP (Media Transfer Protocol)
                            val isMtp = (cls == 0x06 && subCls == 0x01 && proto == 0x01)
                            // ADB (Android Debug Bridge)
                            val isAdb = (cls == 0xFF && subCls == 0x42 && proto == 0x01)

                            if (isMtp || isAdb) {
                                println("UsbScanner: Found Android Device Match [VID:$vidStr PID:$pidStr] - Class:$cls SubClass:$subCls Proto:$proto")
                                isAndroid = true
                                break
                            } else {
                                if (vidStr != "1022" && vidStr != "8086") { // Filter out AMD/Intel hubs
                                    println("UsbScanner: Found Interface on [VID:$vidStr PID:$pidStr] - Class:$cls SubClass:$subCls Proto:$proto")
                                }
                            }
                        }
                    }
                    if (isAndroid) break
                }
            } else {
                if (vidStr != "1022" && vidStr != "8086") {
                    println("UsbScanner: Device [VID:$vidStr PID:$pidStr] has no interfaces.")
                }
            }
            LibUsb.freeConfigDescriptor(config)
            return isAndroid
        } else {
            if (vidStr != "1022" && vidStr != "8086") {
                println("UsbScanner: Warning - Could not get config descriptor for [VID:$vidStr PID:$pidStr]. Error: $configResult")
            }
        }
        return false
    }

    /**
     * Scans for a specific VID/PID.
     * Throws ElevationRequiredException if the device is found but access is blocked by Windows (MTP mode).
     */
    fun findDevice(context: Context, vid: Short, pid: Short): DeviceHandle? {
        val deviceList = DeviceList()
        val count = LibUsb.getDeviceList(context, deviceList)
        if (count < 0) return null

        try {
            for (device in deviceList) {
                val descriptor = DeviceDescriptor()
                if (LibUsb.getDeviceDescriptor(device, descriptor) == LibUsb.SUCCESS) {
                    if (descriptor.idVendor() == vid && descriptor.idProduct() == pid) {
                        val handle = DeviceHandle()
                        val openResult = LibUsb.open(device, handle)
                        
                        if (openResult == LibUsb.SUCCESS) {
                            println("UsbScanner: Opened device VID:${String.format("%04X", vid)} PID:${String.format("%04X", pid)}")
                            LibUsb.setAutoDetachKernelDriver(handle, true)
                            return handle
                        } else if (openResult == LibUsb.ERROR_ACCESS || openResult == LibUsb.ERROR_NOT_SUPPORTED) {
                            println("UsbScanner: ACCESS DENIED (error $openResult). Phone is likely in MTP mode.")
                            throw ElevationRequiredException(vid, pid)
                        } else {
                            println("UsbScanner: Found device but can't open it (error $openResult: ${LibUsb.errorName(openResult)})")
                        }
                    }
                }
            }
        } finally {
            LibUsb.freeDeviceList(deviceList, true)
        }
        return null
    }

    /**
     * Finds the first available Android phone dynamically by checking USB Classes.
     * No hardcoded VIDs required!
     */
    // Cache for dynamically verified WPD (Windows Portable Devices)
    private val verifiedWpdDevices = mutableSetOf<String>()
    private val rejectedDevices = mutableSetOf<String>()

    /**
     * Dynamically asks Windows if a specific VID/PID is currently registered as a WPD (MTP) device.
     * This perfectly bypasses the Windows Descriptor block without hardcoding ANY brands!
     */
    private fun isWindowsPortableDevice(vidHex: String, pidHex: String): Boolean {
        val deviceId = "$vidHex&PID_$pidHex".lowercase()
        if (verifiedWpdDevices.contains(deviceId)) return true
        if (rejectedDevices.contains(deviceId)) return false

        try {
            val proc = ProcessBuilder("pnputil.exe", "/enum-devices", "/connected", "/class", "WPD")
                .redirectErrorStream(true)
                .start()
            val output = proc.inputStream.bufferedReader().readText().lowercase()
            proc.waitFor()
            
            if (output.contains(deviceId)) {
                verifiedWpdDevices.add(deviceId)
                return true
            }
        } catch (e: Exception) {
            println("UsbScanner: Failed to query pnputil - ${e.message}")
        }
        
        rejectedDevices.add(deviceId)
        return false
    }

    fun findAnyAndroidPhone(context: Context): Pair<DeviceHandle, org.usb4java.Device>? {
        val deviceList = DeviceList()
        if (LibUsb.getDeviceList(context, deviceList) < 0) return null

        try {
            for (device in deviceList) {
                val desc = DeviceDescriptor()
                LibUsb.getDeviceDescriptor(device, desc)
                val vid = desc.idVendor()
                val vidStr = String.format("%04X", vid)
                val pidStr = String.format("%04X", desc.idProduct())

                // Skip common hubs/host controllers to save time
                if (vidStr == "1022" || vidStr == "8086" || vidStr == "1B21") continue

                // 1. Dynamic Descriptor Check (Works if ADB is on, or Linux/macOS)
                val isDynamicAndroid = isAndroidDevice(device)
                
                // 2. Dynamic Windows WPD Check (For Windows MTP where descriptors are locked)
                val isWpdAndroid = if (!isDynamicAndroid) isWindowsPortableDevice(vidStr, pidStr) else false

                if (isDynamicAndroid || isWpdAndroid) {
                    if (isWpdAndroid) {
                        println("UsbScanner: Dynamically verified Windows MTP Device via pnputil [VID:$vidStr PID:$pidStr]")
                    }
                    
                    val handle = DeviceHandle()
                    val openResult = LibUsb.open(device, handle)
                    if (openResult == LibUsb.SUCCESS) {
                        println("UsbScanner: Successfully opened [VID:$vidStr PID:$pidStr]")
                        LibUsb.setAutoDetachKernelDriver(handle, true)
                        LibUsb.refDevice(device) // CRITICAL: Increment ref count because freeDeviceList unrefs it!
                        return Pair(handle, device)
                    } else if (openResult == LibUsb.ERROR_ACCESS || openResult == LibUsb.ERROR_NOT_SUPPORTED) {
                        println("UsbScanner: Access Denied for [VID:$vidStr PID:$pidStr]. Throwing ElevationRequiredException...")
                        throw ElevationRequiredException(desc.idVendor(), desc.idProduct())
                    } else {
                        println("UsbScanner: Failed to open [VID:$vidStr PID:$pidStr]. Error: $openResult")
                    }
                }
            }
        } finally {
            LibUsb.freeDeviceList(deviceList, true) // This unrefs ALL devices in the list
        }
        return null
    }
}
