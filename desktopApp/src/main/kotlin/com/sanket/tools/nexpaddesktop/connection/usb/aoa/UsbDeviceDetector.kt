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

    /**
     * Enumerates USB devices and identifies Android phones and AOA devices based on classes/PIDs.
     * Does NOT use pnputil. Returns a list of candidates.
     */
    fun findCandidates(context: Context): List<CandidateUsbDevice> {
        val candidates = mutableListOf<CandidateUsbDevice>()
        val deviceList = DeviceList()
        
        if (LibUsb.getDeviceList(context, deviceList) < 0) return emptyList()

        try {
            for (device in deviceList) {
                val desc = DeviceDescriptor()
                if (LibUsb.getDeviceDescriptor(device, desc) != LibUsb.SUCCESS) continue

                val vid = desc.idVendor()
                val pid = desc.idProduct()

                // Skip common PC hubs/controllers
                if (vid == 0x1022.toShort() || vid == 0x8086.toShort() || vid == 0x1B21.toShort()) continue

                if (vid == 0x18D1.toShort() && ACCESSORY_PIDS.contains(pid)) {
                    // Ref the device before returning it because freeDeviceList will unref it
                    LibUsb.refDevice(device)
                    candidates.add(CandidateUsbDevice.AoaDevice(device, vid, pid))
                    continue
                }

                val mtpInterface = findMtpInterface(device)
                val adbInterface = findAdbInterface(device)
                
                if (mtpInterface != null || adbInterface != null) {
                    LibUsb.refDevice(device)
                    candidates.add(CandidateUsbDevice.AndroidDevice(device, vid, pid, mtpInterface))
                }
            }
        } finally {
            LibUsb.freeDeviceList(deviceList, true)
        }
        return candidates
    }

    private fun findMtpInterface(device: Device): Int? {
        val config = org.usb4java.ConfigDescriptor()
        if (LibUsb.getConfigDescriptor(device, 0.toByte(), config) >= 0) {
            try {
                val ifaces = config.iface() ?: return null
                for (iface in ifaces) {
                    val alts = iface.altsetting() ?: continue
                    for (alt in alts) {
                        val cls = alt.bInterfaceClass().toInt() and 0xFF
                        val subCls = alt.bInterfaceSubClass().toInt() and 0xFF
                        val proto = alt.bInterfaceProtocol().toInt() and 0xFF
                        
                        // MTP / PTP
                        if (cls == 0x06 && subCls == 0x01 && proto == 0x01) {
                            return alt.bInterfaceNumber().toInt() and 0xFF
                        }
                    }
                }
            } finally {
                LibUsb.freeConfigDescriptor(config)
            }
        }
        return null
    }

    private fun findAdbInterface(device: Device): Int? {
        val config = org.usb4java.ConfigDescriptor()
        if (LibUsb.getConfigDescriptor(device, 0.toByte(), config) >= 0) {
            try {
                val ifaces = config.iface() ?: return null
                for (iface in ifaces) {
                    val alts = iface.altsetting() ?: continue
                    for (alt in alts) {
                        val cls = alt.bInterfaceClass().toInt() and 0xFF
                        val subCls = alt.bInterfaceSubClass().toInt() and 0xFF
                        val proto = alt.bInterfaceProtocol().toInt() and 0xFF
                        
                        // ADB
                        if (cls == 0xFF && subCls == 0x42 && proto == 0x01) {
                            return alt.bInterfaceNumber().toInt() and 0xFF
                        }
                    }
                }
            } finally {
                LibUsb.freeConfigDescriptor(config)
            }
        }
        return null
    }
}
