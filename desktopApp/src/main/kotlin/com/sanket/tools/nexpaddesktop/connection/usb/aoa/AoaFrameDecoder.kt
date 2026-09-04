package com.sanket.tools.nexpaddesktop.connection.usb.aoa

import java.nio.ByteBuffer

/**
 * Handles USB Bulk Transfer fragmentation and streaming reassembly.
 * USB bulk transfers are streams of bytes, not discrete packets. 
 * High-Speed USB endpoints frequently burst multiple 512-byte microframes in a single read.
 * This accumulates bytes into a 16KB linear compacting buffer and invokes an inline callback
 * for every fully-formed packet with ZERO memory allocations.
 */
class AoaFrameDecoder(private val packetSize: Int) {
    companion object {
        private const val RING_BUFFER_CAPACITY = 16 * 1024 // 16 KB
    }

    private val ringBuffer = ByteArray(RING_BUFFER_CAPACITY)
    private var readPos = 0
    private var writePos = 0

    /**
     * Appends incoming data and invokes [onPacket] for each complete [packetSize] frame.
     * ZERO allocations: no collections or intermediate byte arrays are created.
     */
    fun append(
        data: ByteArray,
        offset: Int = 0,
        length: Int,
        onPacket: (buffer: ByteArray, offset: Int) -> Unit
    ) {
        if (length <= 0) return

        // If remaining capacity is insufficient, shift unconsumed bytes to index 0
        if (ringBuffer.size - writePos < length) {
            val unread = writePos - readPos
            if (unread > 0) {
                System.arraycopy(ringBuffer, readPos, ringBuffer, 0, unread)
            }
            writePos = unread
            readPos = 0
        }

        // If still overflowing (corrupt stream or massive burst), safely reset
        if (ringBuffer.size - writePos < length) {
            println("[AOA/Decoder] WARNING: Frame accumulator overflow ($length bytes, unread: ${writePos - readPos}). Resetting buffer.")
            writePos = 0
            readPos = 0
            if (length > ringBuffer.size) return // Drop oversized corrupt payload
        }

        System.arraycopy(data, offset, ringBuffer, writePos, length)
        writePos += length

        while ((writePos - readPos) >= packetSize) {
            onPacket(ringBuffer, readPos)
            readPos += packetSize
        }

        // If completely consumed, reset positions to 0 to eliminate future compaction overhead
        if (readPos == writePos) {
            readPos = 0
            writePos = 0
        }
    }

    /**
     * Legacy/convenience allocating overload.
     */
    fun appendBytes(data: ByteArray): List<ByteArray> {
        val packets = mutableListOf<ByteArray>()
        append(data, 0, data.size) { buf, off ->
            packets.add(buf.copyOfRange(off, off + packetSize))
        }
        return packets
    }

    fun reset() {
        readPos = 0
        writePos = 0
    }
}
