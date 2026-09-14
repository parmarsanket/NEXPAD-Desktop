package com.sanket.tools.nexpaddesktop.connection.usb.aoa

import org.usb4java.DeviceHandle
import org.usb4java.LibUsb

data class AoaEndpoints(
    val interfaceNumber: Int,
    val bulkIn: Byte,
    val bulkOut: Byte
)

object AoaInterfaceDiscovery {

    /**
     * Finds the AOA accessory interface and its endpoints.
     * Also guarantees that the device is set to Configuration 1 as required by AOA spec.
     */
    fun discover(handle: DeviceHandle): AoaEndpoints? {
        val device = LibUsb.getDevice(handle)
        
        // AOA Specification: Set Configuration to 1
        val currentConfig = java.nio.IntBuffer.allocate(1)
        if (LibUsb.getConfiguration(handle, currentConfig) == LibUsb.SUCCESS) {
            if (currentConfig.get(0) != 1) {
                println("AoaInterfaceDiscovery: Setting configuration to 1...")
                val setConfigResult = LibUsb.setConfiguration(handle, 1)
                if (setConfigResult != LibUsb.SUCCESS) {
                    println("AoaInterfaceDiscovery ERROR: Failed to set configuration to 1: ${LibUsb.errorName(setConfigResult)}")
                    // Some Windows WinUSB backends block setConfiguration if it's already implicitly set. We will try to proceed.
                }
            }
        }

        val config = org.usb4java.ConfigDescriptor()
        val getConfigResult = LibUsb.getActiveConfigDescriptor(device, config)
        if (getConfigResult < 0) {
            println("AoaInterfaceDiscovery ERROR: Failed to get active configuration descriptor: ${LibUsb.errorName(getConfigResult)}")
            return null
        }

        try {
            val ifaces = config.iface() ?: return null
            for (iface in ifaces) {
                val alts = iface.altsetting() ?: continue
                for (alt in alts) {
                    val endpoints = alt.endpoint() ?: continue
                    
                    var bulkIn: Byte = -1
                    var bulkOut: Byte = -1

                    for (ep in endpoints) {
                        val epAddr = ep.bEndpointAddress()
                        val attr = ep.bmAttributes()
                        val isBulk = (attr.toInt() and LibUsb.TRANSFER_TYPE_MASK.toInt()) == LibUsb.TRANSFER_TYPE_BULK.toInt()
                        
                        if (isBulk) {
                            val isIn = (epAddr.toInt() and LibUsb.ENDPOINT_DIR_MASK.toInt()) == LibUsb.ENDPOINT_IN.toInt()
                            if (isIn) {
                                bulkIn = epAddr
                            } else {
                                bulkOut = epAddr
                            }
                        }
                    }

                    // The Accessory interface must have at least one Bulk IN and one Bulk OUT
                    if (bulkIn.toInt() != -1 && bulkOut.toInt() != -1) {
                        return AoaEndpoints(
                            interfaceNumber = alt.bInterfaceNumber().toInt() and 0xFF,
                            bulkIn = bulkIn,
                            bulkOut = bulkOut
                        )
                    }
                }
            }
            return null
        } finally {
            LibUsb.freeConfigDescriptor(config)
        }
    }
}
