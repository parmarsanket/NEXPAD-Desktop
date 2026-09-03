package com.sanket.tools.nexpaddesktop.connection.usb.aoa

import java.nio.ByteBuffer

/**
 * Handles USB Bulk Transfer fragmentation.
 * USB bulk transfers are streams of bytes, not discrete packets. 
 * This accumulates bytes until a complete packet is received.
 */
class AoaFrameDecoder(private val packetSize: Int) {
    private val buffer = ByteBuffer.allocate(packetSize * 4) // Buffer to hold multiple packets
    
    fun appendBytes(data: ByteArray): List<ByteArray> {
        val packets = mutableListOf<ByteArray>()
        
        // Prevent buffer overflow in edge cases of corrupt streams
        if (buffer.remaining() < data.size) {
            buffer.clear()
            println("AoaFrameDecoder WARNING: Buffer overflow, clearing frame accumulator.")
        }
        
        buffer.put(data)
        buffer.flip()
        
        while (buffer.remaining() >= packetSize) {
            val packet = ByteArray(packetSize)
            buffer.get(packet)
            packets.add(packet)
        }
        
        // Compact the buffer to keep remaining partial bytes at the beginning
        buffer.compact()
        
        return packets
    }
    
    fun reset() {
        buffer.clear()
    }
}
