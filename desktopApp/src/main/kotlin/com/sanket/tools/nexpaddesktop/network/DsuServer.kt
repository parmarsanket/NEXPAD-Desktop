package com.sanket.tools.nexpaddesktop.network

import com.sanket.tools.nexpaddesktop.model.GamepadInput
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.SocketException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.concurrent.ConcurrentHashMap
import java.util.zip.CRC32
import kotlin.math.PI

class DsuServer {
    private var socket: DatagramSocket? = null
    private var isRunning = false
    private var thread: Thread? = null
    
    // Track emulator clients who have requested data
    private val clients = ConcurrentHashMap<String, ClientInfo>()
    
    private data class ClientInfo(val ip: InetAddress, val port: Int, var lastSeen: Long)

    private var packetCount = 0

    fun start(port: Int = 26760) {
        if (isRunning) return
        
        try {
            socket = DatagramSocket(port)
            isRunning = true
            thread = Thread {
                val buffer = ByteArray(1024)
                while (isRunning) {
                    try {
                        val packet = DatagramPacket(buffer, buffer.size)
                        socket?.receive(packet)
                        handleIncomingPacket(packet)
                    } catch (e: SocketException) {
                        if (isRunning) e.printStackTrace()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }.apply {
                isDaemon = true
                start()
            }
            println("🎮 DSU Motion Server started on port $port")
        } catch (e: Exception) {
            println("⚠️ Failed to start DSU Server: ${e.message}")
        }
    }

    fun stop() {
        isRunning = false
        socket?.close()
        socket = null
    }

    private fun handleIncomingPacket(packet: DatagramPacket) {
        val buffer = ByteBuffer.wrap(packet.data, 0, packet.length).order(ByteOrder.LITTLE_ENDIAN)
        if (buffer.remaining() < 16) return
        
        val magic = ByteArray(4)
        buffer.get(magic)
        if (String(magic, Charsets.US_ASCII) != "DSUC") return // Not a client message
        
        buffer.short // version
        buffer.short // length
        buffer.int   // crc32
        buffer.int   // client id
        
        val messageType = buffer.int
        
        val clientId = "${packet.address.hostAddress}:${packet.port}"
        clients[clientId] = ClientInfo(packet.address, packet.port, System.currentTimeMillis())

        when (messageType) {
            0x100000 -> {
                // Protocol Info Request -> Reply with Protocol Version
                sendProtocolResponse(packet.address, packet.port)
            }
            0x100001 -> {
                // Ports Info Request -> Reply with Controller Info
                sendPortsResponse(packet.address, packet.port)
            }
            0x100002 -> {
                // Pad Data Request -> Registers the client (already handled above)
                // We don't reply immediately; the `updateInput` loop will spam the client
            }
        }
    }

    // ... (rest of the file stays same, but we add sendProtocolResponse and rename sendInfoResponse to sendPortsResponse)

    fun updateInput(input: GamepadInput) {
        if (!isRunning || clients.isEmpty()) return
        
        // Cleanup inactive clients (timeout after 10 seconds of no requests)
        val now = System.currentTimeMillis()
        clients.entries.removeIf { now - it.value.lastSeen > 10000 }

        if (clients.isEmpty()) return

        packetCount++
        val payload = buildDataPayload(input, packetCount)
        val response = buildHeader(0x100002, payload)

        for (client in clients.values) {
            try {
                val packet = DatagramPacket(response, response.size, client.ip, client.port)
                socket?.send(packet)
            } catch (e: Exception) {
                // Ignore send errors
            }
        }
    }

    private fun sendProtocolResponse(ip: InetAddress, port: Int) {
        val buffer = ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN)
        buffer.putShort(1001) // Version
        val response = buildHeader(0x100000, buffer.array())
        try {
            socket?.send(DatagramPacket(response, response.size, ip, port))
        } catch (e: Exception) {}
    }

    private fun sendPortsResponse(ip: InetAddress, port: Int) {
        val payload = buildInfoPayload()
        val response = buildHeader(0x100001, payload)
        try {
            socket?.send(DatagramPacket(response, response.size, ip, port))
        } catch (e: Exception) {}
    }

    private fun buildHeader(messageType: Int, payload: ByteArray): ByteArray {
        val buffer = ByteBuffer.allocate(16 + 4 + payload.size).order(ByteOrder.LITTLE_ENDIAN)
        buffer.put("DSUS".toByteArray(Charsets.US_ASCII))
        buffer.putShort(1001) // Version
        buffer.putShort((payload.size + 4).toShort()) // Length (payload + message type)
        buffer.putInt(0) // CRC32 placeholder
        buffer.putInt(9999) // Server ID
        
        buffer.putInt(messageType)
        buffer.put(payload)
        
        val packet = buffer.array()
        
        // Calculate CRC32 (with placeholder as 0)
        val crc32 = CRC32()
        crc32.update(packet)
        val crc = crc32.value.toInt()
        
        // Insert CRC32 at offset 8
        val crcBuffer = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(crc).array()
        System.arraycopy(crcBuffer, 0, packet, 8, 4)
        
        return packet
    }

    private fun buildInfoPayload(): ByteArray {
        val buffer = ByteBuffer.allocate(12).order(ByteOrder.LITTLE_ENDIAN)
        buffer.put(0.toByte()) // Slot
        buffer.put(2.toByte()) // State (2 = Connected)
        buffer.put(2.toByte()) // Model (2 = Full Gyro)
        buffer.put(1.toByte()) // Connection Type (1 = USB)
        buffer.put(byteArrayOf(0x00, 0x11, 0x22, 0x33, 0x44, 0x55)) // MAC address fake
        buffer.put(5.toByte()) // Battery status (5 = Full)
        buffer.put(0.toByte()) // Null terminator
        return buffer.array()
    }

    private fun buildDataPayload(input: GamepadInput, pCount: Int): ByteArray {
        // CemuHook data payload is exactly 68 bytes
        val buffer = ByteBuffer.allocate(68).order(ByteOrder.LITTLE_ENDIAN)
        
        buffer.put(0.toByte()) // Slot
        buffer.put(2.toByte()) // State (Connected)
        buffer.put(2.toByte()) // Model (Full Gyro)
        buffer.put(1.toByte()) // Connection type
        buffer.put(byteArrayOf(0x00, 0x11, 0x22, 0x33, 0x44, 0x55)) // MAC
        buffer.put(5.toByte()) // Battery
        buffer.put(1.toByte()) // Is Active (1)
        
        buffer.putInt(pCount) // Packet number
        
        // Buttons 1 & 2
        var buttons1 = 0
        var buttons2 = 0
        
        // Mapping NEXPAD buttons to DSU layout
        if (input.dpadLeft) buttons1 = buttons1 or 0x80
        if (input.dpadDown) buttons1 = buttons1 or 0x40
        if (input.dpadRight) buttons1 = buttons1 or 0x20
        if (input.dpadUp) buttons1 = buttons1 or 0x10
        if (input.btnStart) buttons1 = buttons1 or 0x08
        if (input.btnR3) buttons1 = buttons1 or 0x04
        if (input.btnL3) buttons1 = buttons1 or 0x02
        if (input.btnSelect) buttons1 = buttons1 or 0x01
        
        if (input.btnX) buttons2 = buttons2 or 0x80 // Y in switch
        if (input.btnB) buttons2 = buttons2 or 0x40 // A in switch
        if (input.btnA) buttons2 = buttons2 or 0x20 // B in switch
        if (input.btnY) buttons2 = buttons2 or 0x10 // X in switch
        if (input.btnR1) buttons2 = buttons2 or 0x08
        if (input.btnL1) buttons2 = buttons2 or 0x04
        if (input.triggerR2 > 0.5f) buttons2 = buttons2 or 0x02
        if (input.triggerL2 > 0.5f) buttons2 = buttons2 or 0x01

        buffer.put(buttons1.toByte())
        buffer.put(buttons2.toByte())
        buffer.put(0.toByte()) // PS
        buffer.put(0.toByte()) // Touch
        
        // Left stick X/Y (0-255, 128 center)
        buffer.put(((input.leftStickX + 1.0f) * 127.5f).toInt().toByte())
        buffer.put(((input.leftStickY + 1.0f) * 127.5f).toInt().toByte())
        
        // Right stick X/Y
        buffer.put(((input.rightStickX + 1.0f) * 127.5f).toInt().toByte())
        buffer.put(((input.rightStickY + 1.0f) * 127.5f).toInt().toByte())
        
        // Analog buttons (DPad, Face buttons, Bumpers, Triggers)
        // Skip setting exact analog values for all of them except triggers to save bytes
        val analogArray = ByteArray(12)
        analogArray[10] = (input.triggerR2 * 255).toInt().toByte()
        analogArray[11] = (input.triggerL2 * 255).toInt().toByte()
        buffer.put(analogArray)
        
        // Timestamp (Microseconds since start)
        buffer.putLong(System.nanoTime() / 1000L)
        
        // Accelerometer data (G's)
        // Android returns m/s^2, so divide by 9.80665f
        buffer.putFloat(input.accelX / 9.80665f)
        buffer.putFloat(input.accelY / 9.80665f)
        buffer.putFloat(input.accelZ / 9.80665f)
        
        // Gyroscope data (deg/s)
        // Android returns rad/s, so multiply by (180 / PI)
        buffer.putFloat(input.gyroX * (180f / PI.toFloat()))
        buffer.putFloat(input.gyroY * (180f / PI.toFloat()))
        buffer.putFloat(input.gyroZ * (180f / PI.toFloat()))
        
        return buffer.array()
    }
}
