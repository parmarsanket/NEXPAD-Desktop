package com.sanket.tools.nexpaddesktop.connection.usb.aoa

import org.usb4java.DeviceHandle
import org.usb4java.LibUsb
import java.nio.ByteBuffer
import java.nio.IntBuffer
import kotlinx.coroutines.isActive
import kotlinx.coroutines.coroutineScope

object AoaStreamer {

    suspend fun startBulkStreaming(
        handle: DeviceHandle,
        onInputReceived: (com.sanket.tools.nexpad.model.GamepadInput) -> Unit
    ) = coroutineScope {
        val claimResult = LibUsb.claimInterface(handle, 0)
        if (claimResult != LibUsb.SUCCESS) {
            println("AoaStreamer ERROR: Failed to claim interface: ${LibUsb.errorName(claimResult)}")
            return@coroutineScope
        }
        println("AoaStreamer: Claimed USB interface 0. Starting bulk read loop...")

        // Dynamically find the Bulk IN endpoint
        var endpointIn: Byte = -1
        val device = LibUsb.getDevice(handle)
        val config = org.usb4java.ConfigDescriptor()
        if (LibUsb.getActiveConfigDescriptor(device, config) >= 0) {
            val ifaces = config.iface()
            if (ifaces != null && ifaces.size > 0) {
                val alts = ifaces[0].altsetting()
                if (alts != null && alts.size > 0) {
                    val endpoints = alts[0].endpoint()
                    if (endpoints != null) {
                        for (ep in endpoints) {
                            val epAddr = ep.bEndpointAddress()
                            val attr = ep.bmAttributes()
                            val isBulk = (attr.toInt() and LibUsb.TRANSFER_TYPE_MASK.toInt()) == LibUsb.TRANSFER_TYPE_BULK.toInt()
                            val isIn = (epAddr.toInt() and LibUsb.ENDPOINT_DIR_MASK.toInt()) == LibUsb.ENDPOINT_IN.toInt()
                            if (isBulk && isIn) {
                                endpointIn = epAddr
                                break
                            }
                        }
                    }
                }
            }
            LibUsb.freeConfigDescriptor(config)
        }

        if (endpointIn.toInt() == -1) {
            println("AoaStreamer ERROR: Could not find Bulk IN endpoint!")
            LibUsb.releaseInterface(handle, 0)
            return@coroutineScope
        }
        println("AoaStreamer: Found dynamic Bulk IN endpoint: ${String.format("0x%02X", endpointIn)}")
        val buffer = ByteBuffer.allocateDirect(64)
        val transferred = IntBuffer.allocate(1)

        try {
            while (isActive) {
                buffer.clear()
                transferred.clear()
                val result = LibUsb.bulkTransfer(handle, endpointIn, buffer, transferred, 1000)

                if (result == LibUsb.SUCCESS && transferred.get(0) > 0) {
                    val bytesRead = transferred.get(0)
                    val data = ByteArray(bytesRead)
                    buffer.get(data)
                    
                    if (bytesRead == com.sanket.tools.nexpad.protocol.NexpadProtocol.INPUT_PACKET_SIZE) {
                        val input = com.sanket.tools.nexpad.protocol.NexpadProtocol.decodeInput(data)
                        if (input != null) {
                            onInputReceived(input)
                        }
                    }
                } else if (result == LibUsb.ERROR_TIMEOUT) {
                    // Normal timeout, keep polling
                } else if (result == LibUsb.ERROR_PIPE || result == LibUsb.ERROR_NO_DEVICE) {
                    println("AoaStreamer: Device disconnected (${LibUsb.errorName(result)}).")
                    break
                } else if (result < 0) {
                    println("AoaStreamer: Bulk read error: ${LibUsb.errorName(result)}")
                    break
                }
            }
        } finally {
            LibUsb.releaseInterface(handle, 0)
            LibUsb.close(handle)
            println("AoaStreamer: Cleanup complete.")
        }
    }
}
