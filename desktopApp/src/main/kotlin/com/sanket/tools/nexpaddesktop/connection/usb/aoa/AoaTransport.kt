package com.sanket.tools.nexpaddesktop.connection.usb.aoa

import org.usb4java.DeviceHandle
import org.usb4java.LibUsb
import java.nio.ByteBuffer
import java.nio.IntBuffer
import kotlinx.coroutines.isActive
import kotlinx.coroutines.coroutineScope

object AoaTransport {

    private const val BULK_READ_BUFFER_SIZE = 4096

    /**
     * Borrows the DeviceHandle (opened by AoaManager). 
     * Claims the correct interface, reads/writes using the frame decoder, and releases the interface.
     * Does NOT close the DeviceHandle (AoaManager owns it).
     */
    suspend fun startBulkStreaming(
        handle: DeviceHandle,
        onInputReceived: (com.sanket.tools.nexpad.model.GamepadInput) -> Unit
    ) = coroutineScope {
        
        val endpoints = AoaInterfaceDiscovery.discover(handle)
        if (endpoints == null) {
            println("AoaTransport ERROR: Could not dynamically discover AOA endpoints!")
            return@coroutineScope
        }
        
        val interfaceNum = endpoints.interfaceNumber
        
        val claimResult = LibUsb.claimInterface(handle, interfaceNum)
        if (claimResult != LibUsb.SUCCESS) {
            println("AoaTransport ERROR: Failed to claim interface $interfaceNum: ${LibUsb.errorName(claimResult)}")
            return@coroutineScope
        }
        println("AoaTransport: Claimed interface $interfaceNum. IN:${String.format("0x%02X", endpoints.bulkIn)} OUT:${String.format("0x%02X", endpoints.bulkOut)}")

        val buffer = ByteBuffer.allocateDirect(BULK_READ_BUFFER_SIZE)
        val transferred = IntBuffer.allocate(1)
        
        val packetSize = com.sanket.tools.nexpad.protocol.NexpadProtocol.INPUT_PACKET_SIZE
        val decoder = AoaFrameDecoder(packetSize)

        try {
            while (isActive) {
                buffer.clear()
                transferred.clear()
                val result = LibUsb.bulkTransfer(handle, endpoints.bulkIn, buffer, transferred, 1000)

                if (result == LibUsb.SUCCESS && transferred.get(0) > 0) {
                    val bytesRead = transferred.get(0)
                    val data = ByteArray(bytesRead)
                    buffer.get(data)
                    
                    val completePackets = decoder.appendBytes(data)
                    for (packetData in completePackets) {
                        val input = com.sanket.tools.nexpad.protocol.NexpadProtocol.decodeInput(packetData)
                        if (input != null) {
                            onInputReceived(input)
                        }
                    }
                } else if (result == LibUsb.ERROR_TIMEOUT) {
                    // Normal timeout, keep polling
                } else if (result == LibUsb.ERROR_PIPE || result == LibUsb.ERROR_NO_DEVICE) {
                    println("AoaTransport: Device disconnected (${LibUsb.errorName(result)}).")
                    break
                } else if (result < 0) {
                    println("AoaTransport: Bulk read error: ${LibUsb.errorName(result)}")
                    break
                }
            }
        } finally {
            LibUsb.releaseInterface(handle, interfaceNum)
            println("AoaTransport: Released interface $interfaceNum. Stream closed.")
            // NOTE: We do NOT LibUsb.close(handle) here! AoaManager is the owner!
        }
    }
}
