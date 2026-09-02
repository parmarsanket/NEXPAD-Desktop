package com.sanket.tools.nexpaddesktop.network

import org.usb4java.DeviceHandle
import org.usb4java.LibUsb
import java.nio.ByteBuffer

class AoaTransport(private val handle: DeviceHandle) {

    private val endpointIn = 0x81.toByte()
    private val endpointOut = 0x02.toByte()

    fun startStreaming() {
        val claimResult = LibUsb.claimInterface(handle, 0)
        if (claimResult == LibUsb.SUCCESS) {
            println("AoaTransport: Claimed interface, ready for bulk streaming!")
        }
    }
    
    fun sendFeedback(data: ByteArray) {
        val buffer = ByteBuffer.allocateDirect(data.size)
        buffer.put(data)
        buffer.rewind()
        
        val transferred = java.nio.IntBuffer.allocate(1)
        LibUsb.bulkTransfer(handle, endpointOut, buffer, transferred, 500)
    }
}
