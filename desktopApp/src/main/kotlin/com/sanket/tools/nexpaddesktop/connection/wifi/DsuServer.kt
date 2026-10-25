package com.sanket.tools.nexpaddesktop.connection.wifi

import com.sanket.tools.nexpad.model.GamepadInput
import java.net.BindException
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.SocketException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import java.util.zip.CRC32
import kotlin.math.PI

/**
 * DsuServer implements the standard UDP Motion Protocol (also known as DSU - UDP Motion Provider).
 * This allows third-party games, tools, and motion-compatible applications to receive
 * high-frequency controller input (especially 6-axis motion data and analog buttons)
 * directly over UDP.
 *
 * ## Protocol Specification
 * The protocol operates on UDP port 26760 by default. It consists of:
 * 1. **Protocol Info (0x100000)**: Server reports protocol version 1001.
 * 2. **Ports Info (0x100001)**: Server reports connected controllers per slot (0..3).
 *    - When DualShock 4 is active, Slot 0 reports Connected (State=2, Model=2 FullGyro, USB=1).
 *    - When Xbox 360 is active (or slots 1..3), it reports Disconnected (State=0).
 * 3. **Pad Data (0x100002)**: Server streams 100-byte packets (20-byte header + 80-byte payload):
 *    - Header (16 bytes): "DSUS", version 1001, length 84, CRC32, server ID
 *    - Message type (4 bytes): 0x100002
 *    - Payload (80 bytes):
 *      - Controller info (slot, state, model, connection, MAC, battery, isConnected)
 *      - Packet counter (uint32 LE)
 *      - Buttons bitmask 1 (Dpad L/D/R/U, Start, R3, L3, Select)
 *      - Buttons bitmask 2 (Square, Cross, Circle, Triangle, R1, L1, R2 digital, L2 digital)
 *      - Special buttons (PS/Home, Touchpad click)
 *      - Sticks (Left Stick X/Y, Right Stick X/Y: 0..255, 128 center, Y positive upward)
 *      - Analog buttons (Dpad L/D/R/U, Square, Cross, Circle, Triangle, R1, L1, R2, L2: 0..255)
 *      - Touchpad Point 1 & Point 2 (6 bytes each: active, id, x uint16 LE, y uint16 LE)
 *      - Timestamp (uint64 LE microseconds)
 *      - Accelerometer (3x Float LE in G-force units: X, Y, Z)
 *      - Gyroscope (3x Float LE in deg/s: Pitch, Yaw, Roll)
 *
 * Gating: DSU server ONLY reports connected controller status and ONLY streams pad data
 * when [isDs4Active] is true (ControllerType.DUALSHOCK_4).
 */
class DsuServer {
    companion object {
        const val DEFAULT_PORT = 26760
        const val PROTOCOL_VERSION: Short = 1001
        const val SERVER_ID = 9999
        val FAKE_MAC = byteArrayOf(0x00, 0x11, 0x22, 0x33, 0x44, 0x55)
        val ZERO_MAC = ByteArray(6)
    }

    private var socket: DatagramSocket? = null
    private var isRunning = false
    private var thread: Thread? = null
    var onError: ((Exception) -> Unit)? = null

    /**
     * Active controller gating: DSU server ONLY operates when DS4 mode is active.
     * When switched off (e.g. Xbox 360 mode), client subscriptions are cleared
     * and ports query reports slot 0 as disconnected.
     */
    @Volatile
    var isDs4Active: Boolean = false
        set(value) {
            field = value
            if (!value) {
                clients.clear()
            }
        }

    // Track connected clients who have requested data
    private val clients = ConcurrentHashMap<String, ClientInfo>()

    internal data class ClientInfo(val ip: InetAddress, val port: Int, var lastSeen: Long)

    private val packetCount = AtomicInteger(0)

    @Volatile
    private var lastInput: GamepadInput = GamepadInput()

    fun start(port: Int = DEFAULT_PORT) {
        if (isRunning) return
        
        var createdSocket: DatagramSocket? = null
        try {
            createdSocket = DatagramSocket(null).apply {
                reuseAddress = false
                bind(InetSocketAddress(port))
            }
            socket = createdSocket
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
                name = "NexpadDsuServerThread"
                start()
            }
            println("🎮 DSU Motion Server started on port $port (DS4 Active: $isDs4Active)")
        } catch (e: BindException) {
            println("⚠️ Failed to bind DSU Server on port $port (Port Conflict): ${e.message}")
            try { createdSocket?.close() } catch (_: Exception) {}
            socket = null
            isRunning = false
            onError?.invoke(e)
        } catch (e: Exception) {
            println("⚠️ Failed to start DSU Server: ${e.message}")
            try { createdSocket?.close() } catch (_: Exception) {}
            socket = null
            isRunning = false
            onError?.invoke(e)
        }
    }

    fun stop() {
        isRunning = false
        socket?.close()
        socket = null
        clients.clear()
    }

    fun getClientCount(): Int {
        return if (isDs4Active) clients.size else 0
    }

    internal fun handleIncomingPacket(packet: DatagramPacket) {
        val buffer = ByteBuffer.wrap(packet.data, 0, packet.length).order(ByteOrder.LITTLE_ENDIAN)
        if (buffer.remaining() < 16) return
        
        val magic = ByteArray(4)
        buffer.get(magic)
        if (String(magic, Charsets.US_ASCII) != "DSUC") return // Not a client message
        
        buffer.short // version
        val length = buffer.short.toInt() and 0xFFFF // length
        buffer.int   // crc32
        buffer.int   // client id
        
        if (buffer.remaining() < 4) return
        val messageType = buffer.int
        
        val clientId = "${packet.address.hostAddress}:${packet.port}"

        when (messageType) {
            0x100000 -> {
                // Protocol Info Request -> Reply with Protocol Version (1001)
                sendProtocolResponse(packet.address, packet.port)
            }
            0x100001 -> {
                // Ports Info Request -> Reply for each requested port
                if (buffer.remaining() >= 4) {
                    val count = buffer.int
                    if (count in 1..4) {
                        for (i in 0 until count) {
                            if (buffer.hasRemaining()) {
                                val slot = buffer.get().toInt() and 0xFF
                                sendPortResponse(packet.address, packet.port, slot)
                            }
                        }
                    } else {
                        sendPortResponse(packet.address, packet.port, 0)
                    }
                } else if (buffer.hasRemaining()) {
                    val slot = buffer.get().toInt() and 0xFF
                    sendPortResponse(packet.address, packet.port, slot)
                } else {
                    sendPortResponse(packet.address, packet.port, 0)
                }
            }
            0x100002 -> {
                // Pad Data Request -> Registers the client
                if (!isDs4Active) {
                    // DSU server only serves when DS4 mode is active
                    clients.remove(clientId)
                    return
                }

                var shouldRegister = true
                if (buffer.hasRemaining()) {
                    val actionFlag = buffer.get().toInt() and 0xFF
                    if (actionFlag == 1) { // Slot-based registration
                        if (buffer.hasRemaining()) {
                            val requestedSlot = buffer.get().toInt() and 0xFF
                            // We only host slot 0
                            if (requestedSlot != 0) {
                                shouldRegister = false
                            }
                        }
                    } else if (actionFlag == 2) { // MAC-based registration
                        if (buffer.remaining() >= 6) {
                            val reqMac = ByteArray(6)
                            buffer.get(reqMac)
                            if (!reqMac.contentEquals(FAKE_MAC)) {
                                shouldRegister = false
                            }
                        }
                    }
                }

                if (shouldRegister) {
                    val isNewClient = !clients.containsKey(clientId)
                    val clientInfo = ClientInfo(packet.address, packet.port, System.currentTimeMillis())
                    clients[clientId] = clientInfo
                    if (isNewClient) {
                        val localPort = socket?.localPort ?: DEFAULT_PORT
                        println("🎯 [DSU MOTION SERVER] Client subscribed from $clientId (Streaming 6-Axis Gyro/Accel on UDP $localPort)")
                    }
                    // Immediately send a response packet so client calibration handshakes complete instantly
                    sendImmediateDataResponse(clientInfo)
                }
            }
        }
    }

    fun updateInput(input: GamepadInput) {
        lastInput = input
        if (!isRunning || !isDs4Active || clients.isEmpty()) return
        
        // Cleanup inactive clients (timeout after 10 seconds of no requests)
        val now = System.currentTimeMillis()
        clients.entries.removeIf { now - it.value.lastSeen > 10000 }

        if (clients.isEmpty()) return

        val currentCount = packetCount.getAndIncrement()
        val payload = buildDataPayload(input, currentCount)
        val response = buildHeader(0x100002, payload)

        for (client in clients.values) {
            try {
                val packet = DatagramPacket(response, response.size, client.ip, client.port)
                socket?.send(packet)
            } catch (_: Exception) {
                // Ignore transient send errors
            }
        }
    }

    private fun sendImmediateDataResponse(client: ClientInfo) {
        if (!isRunning || !isDs4Active) return
        val currentCount = packetCount.getAndIncrement()
        val payload = buildDataPayload(lastInput, currentCount)
        val response = buildHeader(0x100002, payload)
        try {
            socket?.send(DatagramPacket(response, response.size, client.ip, client.port))
        } catch (_: Exception) {}
    }

    private fun sendProtocolResponse(ip: InetAddress, port: Int) {
        val buffer = ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN)
        buffer.putShort(PROTOCOL_VERSION)
        val response = buildHeader(0x100000, buffer.array())
        try {
            socket?.send(DatagramPacket(response, response.size, ip, port))
        } catch (_: Exception) {}
    }

    private fun sendPortResponse(ip: InetAddress, port: Int, slot: Int) {
        val payload = buildPortPayload(slot)
        val response = buildHeader(0x100001, payload)
        try {
            socket?.send(DatagramPacket(response, response.size, ip, port))
        } catch (_: Exception) {}
    }

    internal fun buildHeader(messageType: Int, payload: ByteArray): ByteArray {
        val totalLength = 16 + 4 + payload.size
        val buffer = ByteBuffer.allocate(totalLength).order(ByteOrder.LITTLE_ENDIAN)
        buffer.put("DSUS".toByteArray(Charsets.US_ASCII))
        buffer.putShort(PROTOCOL_VERSION) // Version 1001
        buffer.putShort((payload.size + 4).toShort()) // Length without header (payload + message type)
        buffer.putInt(0) // CRC32 placeholder
        buffer.putInt(SERVER_ID) // Server ID
        
        buffer.putInt(messageType)
        buffer.put(payload)
        
        val packet = buffer.array()
        
        // Calculate CRC32 (with CRC field as 0)
        val crc32 = CRC32()
        crc32.update(packet)
        val crc = crc32.value.toInt()
        
        // Insert CRC32 at offset 8 in Little Endian
        val crcBuffer = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(crc).array()
        System.arraycopy(crcBuffer, 0, packet, 8, 4)
        
        return packet
    }

    internal fun buildPortPayload(slot: Int): ByteArray {
        val buffer = ByteBuffer.allocate(12).order(ByteOrder.LITTLE_ENDIAN)
        buffer.put(slot.toByte()) // Slot
        if (isDs4Active && slot == 0) {
            buffer.put(2.toByte()) // State (2 = Connected)
            buffer.put(2.toByte()) // Model (2 = Full Gyro)
            buffer.put(1.toByte()) // Connection Type (1 = USB)
            buffer.put(FAKE_MAC)   // MAC address
            buffer.put(5.toByte()) // Battery status (5 = Full)
            buffer.put(0.toByte()) // Null terminator
        } else {
            buffer.put(0.toByte()) // State (0 = Disconnected)
            buffer.put(0.toByte()) // Model (0 = N/A)
            buffer.put(0.toByte()) // Connection Type (0 = N/A)
            buffer.put(ZERO_MAC)   // MAC address (all zeros)
            buffer.put(0.toByte()) // Battery status (0 = N/A)
            buffer.put(0.toByte()) // Null terminator
        }
        return buffer.array()
    }

    internal fun buildDataPayload(input: GamepadInput, pCount: Int): ByteArray {
        // Standard DSU motion data payload is exactly 80 bytes (total packet is 100 bytes)
        val buffer = ByteBuffer.allocate(80).order(ByteOrder.LITTLE_ENDIAN)
        
        // 0..11: Controller Info
        buffer.put(0.toByte()) // 0: Slot (0)
        buffer.put(2.toByte()) // 1: State (2 = Connected)
        buffer.put(2.toByte()) // 2: Model (2 = Full Gyro)
        buffer.put(1.toByte()) // 3: Connection type (1 = USB)
        buffer.put(FAKE_MAC)   // 4..9: MAC (6 bytes)
        buffer.put(5.toByte()) // 10: Battery status (5 = Full)
        buffer.put(1.toByte()) // 11: Is Connected / Active (1 = Connected)
        
        // 12..15: Packet number (uint32 LE)
        buffer.putInt(pCount)
        
        // 16: Button bitmask 1 (Dpad L/D/R/U, Start, R3, L3, Select)
        var buttons1 = 0
        if (input.dpadLeft) buttons1 = buttons1 or 0x80
        if (input.dpadDown) buttons1 = buttons1 or 0x40
        if (input.dpadRight) buttons1 = buttons1 or 0x20
        if (input.dpadUp) buttons1 = buttons1 or 0x10
        if (input.btnStart) buttons1 = buttons1 or 0x08
        if (input.btnR3) buttons1 = buttons1 or 0x04
        if (input.btnL3) buttons1 = buttons1 or 0x02
        if (input.btnSelect || input.btnShare) buttons1 = buttons1 or 0x01
        buffer.put(buttons1.toByte())
        
        // Button mapping: West, South, East, North -> Square, Cross, Circle, Triangle
        var buttons2 = 0
        if (input.btnX) buttons2 = buttons2 or 0x80 // Square (West)
        if (input.btnA) buttons2 = buttons2 or 0x40 // Cross (South)
        if (input.btnB) buttons2 = buttons2 or 0x20 // Circle (East)
        if (input.btnY) buttons2 = buttons2 or 0x10 // Triangle (North)
        if (input.btnR1) buttons2 = buttons2 or 0x08 // R1
        if (input.btnL1) buttons2 = buttons2 or 0x04 // L1
        if (input.triggerR2 > 0.1f) buttons2 = buttons2 or 0x02 // R2 digital click
        if (input.triggerL2 > 0.1f) buttons2 = buttons2 or 0x01 // L2 digital click
        buffer.put(buttons2.toByte())

        // 18: PS / Home button (1 or 0)
        buffer.put(if (input.btnGuide) 1.toByte() else 0.toByte())

        // 19: Touchpad click button (1 or 0)
        buffer.put(if (input.btnScreenshot) 1.toByte() else 0.toByte())
        
        // 20..23: Analog Sticks (0..255, 128 center, Y positive upward)
        buffer.put(((input.leftStickX.coerceIn(-1f, 1f) + 1.0f) * 127.5f).toInt().coerceIn(0, 255).toByte())
        buffer.put(((input.leftStickY.coerceIn(-1f, 1f) + 1.0f) * 127.5f).toInt().coerceIn(0, 255).toByte())
        buffer.put(((input.rightStickX.coerceIn(-1f, 1f) + 1.0f) * 127.5f).toInt().coerceIn(0, 255).toByte())
        buffer.put(((input.rightStickY.coerceIn(-1f, 1f) + 1.0f) * 127.5f).toInt().coerceIn(0, 255).toByte())
        
        // 24..35: Analog Buttons (12 bytes, 0..255; analog face/dpad pressure values)
        buffer.put(if (input.dpadLeft) 0xFF.toByte() else 0.toByte())  // 24: Analog D-Pad Left
        buffer.put(if (input.dpadDown) 0xFF.toByte() else 0.toByte())  // 25: Analog D-Pad Down
        buffer.put(if (input.dpadRight) 0xFF.toByte() else 0.toByte()) // 26: Analog D-Pad Right
        buffer.put(if (input.dpadUp) 0xFF.toByte() else 0.toByte())    // 27: Analog D-Pad Up
        buffer.put(if (input.btnX) 0xFF.toByte() else 0.toByte())      // 28: Analog Square (Y)
        buffer.put(if (input.btnA) 0xFF.toByte() else 0.toByte())      // 29: Analog Cross (B)
        buffer.put(if (input.btnB) 0xFF.toByte() else 0.toByte())      // 30: Analog Circle (A)
        buffer.put(if (input.btnY) 0xFF.toByte() else 0.toByte())      // 31: Analog Triangle (X)
        buffer.put(if (input.btnR1) 0xFF.toByte() else 0.toByte())     // 32: Analog R1
        buffer.put(if (input.btnL1) 0xFF.toByte() else 0.toByte())     // 33: Analog L1
        buffer.put((input.triggerR2.coerceIn(0f, 1f) * 255f).toInt().coerceIn(0, 255).toByte()) // 34: Analog R2
        buffer.put((input.triggerL2.coerceIn(0f, 1f) * 255f).toInt().coerceIn(0, 255).toByte()) // 35: Analog L2

        // 36..41: First Touch (6 bytes: active, id, x uint16 LE, y uint16 LE)
        val isTouchActive = input.btnScreenshot
        buffer.put(if (isTouchActive) 1.toByte() else 0.toByte())
        buffer.put((if (isTouchActive) 1 else 0).toByte())
        buffer.putShort(0.toShort())
        buffer.putShort(0.toShort())

        // 42..47: Second Touch (6 bytes)
        buffer.put(0.toByte())
        buffer.put(0.toByte())
        buffer.putShort(0.toShort())
        buffer.putShort(0.toShort())
        
        // 48..55: Timestamp (Microseconds since start)
        buffer.putLong(System.nanoTime() / 1000L)
        
        // 56..67: Accelerometer data (in G-force units: 1G ≈ 9.80665 m/s²)
        val hasAccel = (input.accelX != 0f || input.accelY != 0f || input.accelZ != 0f)
        val ax = if (hasAccel) input.accelX / 9.80665f else 0f
        val ay = if (hasAccel) input.accelY / 9.80665f else 0f
        val az = if (hasAccel) input.accelZ / 9.80665f else 1.0f // Default neutral 1G resting
        buffer.putFloat(ax) // 56..59: Accel X
        buffer.putFloat(ay) // 60..63: Accel Y
        buffer.putFloat(az) // 64..67: Accel Z
        
        // 68..79: Gyroscope data (in deg/s: Pitch around X, Yaw around Z, Roll around Y)
        val radToDeg = (180f / PI.toFloat())
        buffer.putFloat(input.gyroX * radToDeg) // 68..71: Gyro Pitch (rotation around X)
        buffer.putFloat(input.gyroZ * radToDeg) // 72..75: Gyro Yaw (rotation around Z)
        buffer.putFloat(input.gyroY * radToDeg) // 76..79: Gyro Roll (rotation around Y)
        
        return buffer.array()
    }
}
