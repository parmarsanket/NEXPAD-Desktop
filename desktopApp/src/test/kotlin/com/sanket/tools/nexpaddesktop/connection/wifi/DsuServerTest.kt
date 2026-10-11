package com.sanket.tools.nexpaddesktop.connection.wifi

import com.sanket.tools.nexpad.model.GamepadInput
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.SocketTimeoutException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.zip.CRC32
import kotlin.math.PI
import kotlin.test.*

class DsuServerTest {

    @Test
    fun testPadDataPacketExact100Bytes() {
        val server = DsuServer()
        val input = GamepadInput()
        val payload = server.buildDataPayload(input, 1)
        val packet = server.buildHeader(0x100002, payload)

        assertEquals(80, payload.size, "Cemuhook data payload must be exactly 80 bytes")
        assertEquals(100, packet.size, "Total Cemuhook 0x100002 packet must be exactly 100 bytes (20 header + 80 payload)")
    }

    @Test
    fun testHeaderStructureAndCrc32() {
        val server = DsuServer()
        val payload = ByteArray(80) { it.toByte() }
        val packet = server.buildHeader(0x100002, payload)

        val buffer = ByteBuffer.wrap(packet).order(ByteOrder.LITTLE_ENDIAN)
        
        // 0..3: Magic "DSUS"
        val magic = ByteArray(4)
        buffer.get(magic)
        assertEquals("DSUS", String(magic, Charsets.US_ASCII))

        // 4..5: Version 1001
        assertEquals(1001.toShort(), buffer.short)

        // 6..7: Length without 16-byte header (payload + 4 = 84)
        assertEquals(84.toShort(), buffer.short)

        // 8..11: CRC32
        val packetCrc = buffer.int

        // 12..15: Server ID 9999
        assertEquals(9999, buffer.int)

        // 16..19: Message type 0x100002
        assertEquals(0x100002, buffer.int)

        // Verify CRC32 calculation by re-computing with CRC field zeroed
        val copy = packet.clone()
        copy[8] = 0
        copy[9] = 0
        copy[10] = 0
        copy[11] = 0
        val crc32 = CRC32()
        crc32.update(copy)
        assertEquals(crc32.value.toInt(), packetCrc, "CRC32 in header must match computed CRC32 with field zeroed")
    }

    @Test
    fun testDigitalButtonsAndFaceButtonMapping() {
        val server = DsuServer()
        val input = GamepadInput(
            btnX = true, // DS4 Square (West)
            btnA = true, // DS4 Cross (South)
            btnB = false, // DS4 Circle (East)
            btnY = false, // DS4 Triangle (North)
            btnL1 = true,
            btnR1 = false,
            triggerL2 = 0.8f,
            triggerR2 = 0f,
            dpadUp = true,
            dpadDown = false,
            dpadLeft = true,
            dpadRight = false,
            btnStart = true,
            btnSelect = true,
            btnL3 = true,
            btnR3 = false,
            btnGuide = true,
            btnScreenshot = true
        )

        val payload = server.buildDataPayload(input, 42)
        val buffer = ByteBuffer.wrap(payload).order(ByteOrder.LITTLE_ENDIAN)

        // 0..11: Controller info
        assertEquals(0.toByte(), buffer.get(0)) // Slot 0
        assertEquals(2.toByte(), buffer.get(1)) // State connected
        assertEquals(2.toByte(), buffer.get(2)) // Model full gyro
        assertEquals(1.toByte(), buffer.get(3)) // USB

        // 12..15: Packet number
        buffer.position(12)
        assertEquals(42, buffer.int)

        // 16: Buttons bitmask 1 (Dpad L=0x80, D=0x40, R=0x20, U=0x10, Start=0x08, R3=0x04, L3=0x02, Select=0x01)
        val b1 = buffer.get(16).toInt() and 0xFF
        assertTrue((b1 and 0x80) != 0, "Dpad Left must be bit 7")
        assertFalse((b1 and 0x40) != 0, "Dpad Down must be 0")
        assertFalse((b1 and 0x20) != 0, "Dpad Right must be 0")
        assertTrue((b1 and 0x10) != 0, "Dpad Up must be bit 4")
        assertTrue((b1 and 0x08) != 0, "Start must be bit 3")
        assertFalse((b1 and 0x04) != 0, "R3 must be 0")
        assertTrue((b1 and 0x02) != 0, "L3 must be bit 1")
        assertTrue((b1 and 0x01) != 0, "Select must be bit 0")

        // 17: Buttons bitmask 2 (Square=0x80, Cross=0x40, Circle=0x20, Triangle=0x10, R1=0x08, L1=0x04, R2=0x02, L2=0x01)
        val b2 = buffer.get(17).toInt() and 0xFF
        assertTrue((b2 and 0x80) != 0, "Square (btnX) must be bit 7 (0x80)")
        assertTrue((b2 and 0x40) != 0, "Cross (btnA) must be bit 6 (0x40)")
        assertFalse((b2 and 0x20) != 0, "Circle (btnB) must be 0")
        assertFalse((b2 and 0x10) != 0, "Triangle (btnY) must be 0")
        assertFalse((b2 and 0x08) != 0, "R1 must be 0")
        assertTrue((b2 and 0x04) != 0, "L1 must be bit 2 (0x04)")
        assertFalse((b2 and 0x02) != 0, "R2 must be 0")
        assertTrue((b2 and 0x01) != 0, "L2 must be bit 0 (0x01)")

        // 18: PS / Home
        assertEquals(1.toByte(), buffer.get(18))
        // 19: Touchpad button
        assertEquals(1.toByte(), buffer.get(19))
    }

    @Test
    fun testSticksMappingAndOrientation() {
        val server = DsuServer()
        
        // Neutral sticks (0.0f) -> 128
        val neutralInput = GamepadInput(leftStickX = 0f, leftStickY = 0f, rightStickX = 0f, rightStickY = 0f)
        val neutralPayload = server.buildDataPayload(neutralInput, 1)
        assertEquals(127.toByte(), neutralPayload[20], "Left Stick X neutral must be ~127-128")
        assertEquals(127.toByte(), neutralPayload[21], "Left Stick Y neutral must be ~127-128")
        assertEquals(127.toByte(), neutralPayload[22], "Right Stick X neutral must be ~127-128")
        assertEquals(127.toByte(), neutralPayload[23], "Right Stick Y neutral must be ~127-128")

        // Extreme sticks: Up (+1.0) and Right (+1.0) -> 255
        val maxInput = GamepadInput(leftStickX = 1f, leftStickY = 1f, rightStickX = 1f, rightStickY = 1f)
        val maxPayload = server.buildDataPayload(maxInput, 1)
        assertEquals(255.toByte(), maxPayload[20], "Left Stick X right must be 255")
        assertEquals(255.toByte(), maxPayload[21], "Left Stick Y up must be 255")
        assertEquals(255.toByte(), maxPayload[22], "Right Stick X right must be 255")
        assertEquals(255.toByte(), maxPayload[23], "Right Stick Y up must be 255")

        // Extreme sticks: Down (-1.0) and Left (-1.0) -> 0
        val minInput = GamepadInput(leftStickX = -1f, leftStickY = -1f, rightStickX = -1f, rightStickY = -1f)
        val minPayload = server.buildDataPayload(minInput, 1)
        assertEquals(0.toByte(), minPayload[20], "Left Stick X left must be 0")
        assertEquals(0.toByte(), minPayload[21], "Left Stick Y down must be 0")
        assertEquals(0.toByte(), minPayload[22], "Right Stick X left must be 0")
        assertEquals(0.toByte(), minPayload[23], "Right Stick Y down must be 0")
    }

    @Test
    fun testAnalogButtonsParityWithStandard() {
        val server = DsuServer()
        val input = GamepadInput(
            dpadLeft = true,
            dpadDown = false,
            dpadRight = false,
            dpadUp = true,
            btnX = true, // Square
            btnA = false, // Cross
            btnB = true, // Circle
            btnY = false, // Triangle
            btnR1 = true,
            btnL1 = false,
            triggerR2 = 0.75f,
            triggerL2 = 0.25f
        )

        val payload = server.buildDataPayload(input, 1)
        
        // Analog buttons are at offsets 24..35
        assertEquals(0xFF.toByte(), payload[24], "Analog DPad Left must be 255 when pressed")
        assertEquals(0.toByte(), payload[25], "Analog DPad Down must be 0 when released")
        assertEquals(0.toByte(), payload[26], "Analog DPad Right must be 0 when released")
        assertEquals(0xFF.toByte(), payload[27], "Analog DPad Up must be 255 when pressed")
        assertEquals(0xFF.toByte(), payload[28], "Analog Square (btnX) must be 255 when pressed")
        assertEquals(0.toByte(), payload[29], "Analog Cross (btnA) must be 0 when released")
        assertEquals(0xFF.toByte(), payload[30], "Analog Circle (btnB) must be 255 when pressed")
        assertEquals(0.toByte(), payload[31], "Analog Triangle (btnY) must be 0 when released")
        assertEquals(0xFF.toByte(), payload[32], "Analog R1 must be 255 when pressed")
        assertEquals(0.toByte(), payload[33], "Analog L1 must be 0 when released")
        assertEquals((0.75f * 255f).toInt().toByte(), payload[34], "Analog R2 must scale from 0 to 255")
        assertEquals((0.25f * 255f).toInt().toByte(), payload[35], "Analog L2 must scale from 0 to 255")
    }

    @Test
    fun testTouchpadStructures() {
        val server = DsuServer()
        val touchActiveInput = GamepadInput(btnScreenshot = true)
        val payloadActive = server.buildDataPayload(touchActiveInput, 1)
        
        // Touch 1 at 36..41
        assertEquals(1.toByte(), payloadActive[36], "Touch 1 active must be 1")
        assertEquals(1.toByte(), payloadActive[37], "Touch 1 ID must be 1")

        // Touch 2 at 42..47
        assertEquals(0.toByte(), payloadActive[42], "Touch 2 active must be 0")
        assertEquals(0.toByte(), payloadActive[43], "Touch 2 ID must be 0")

        val touchInactiveInput = GamepadInput(btnScreenshot = false)
        val payloadInactive = server.buildDataPayload(touchInactiveInput, 1)
        assertEquals(0.toByte(), payloadInactive[36], "Touch 1 active must be 0 when inactive")
    }

    @Test
    fun testMotionSensorsOffsetsAndUnits() {
        val server = DsuServer()
        val input = GamepadInput(
            accelX = 4.903325f, // ~0.5G
            accelY = -4.903325f, // ~ -0.5G
            accelZ = 9.80665f,  // 1.0G
            gyroX = (PI / 2.0).toFloat(), // 90 deg/s Pitch
            gyroY = (PI / 4.0).toFloat(), // 45 deg/s Roll
            gyroZ = PI.toFloat()          // 180 deg/s Yaw
        )

        val payload = server.buildDataPayload(input, 1)
        val buffer = ByteBuffer.wrap(payload).order(ByteOrder.LITTLE_ENDIAN)

        // Accelerometer at offsets 56..67
        buffer.position(56)
        val ax = buffer.float
        val ay = buffer.float
        val az = buffer.float
        assertEquals(0.5f, ax, 0.01f, "Accel X in G-force units")
        assertEquals(-0.5f, ay, 0.01f, "Accel Y in G-force units")
        assertEquals(1.0f, az, 0.01f, "Accel Z in G-force units")

        // Gyroscope at offsets 68..79 (Pitch, Yaw, Roll)
        buffer.position(68)
        val pitch = buffer.float
        val yaw = buffer.float
        val roll = buffer.float
        assertEquals(90.0f, pitch, 0.1f, "Gyro Pitch (X)")
        assertEquals(180.0f, yaw, 0.1f, "Gyro Yaw (Z)")
        assertEquals(45.0f, roll, 0.1f, "Gyro Roll (Y)")
    }

    @Test
    fun testPortPayloadGatingOnlyDualShock4() {
        val server = DsuServer()

        // 1. When DS4 is active:
        server.isDs4Active = true
        val slot0Ds4 = server.buildPortPayload(0)
        assertEquals(0.toByte(), slot0Ds4[0], "Slot 0")
        assertEquals(2.toByte(), slot0Ds4[1], "Slot 0 must be Connected (2)")
        assertEquals(2.toByte(), slot0Ds4[2], "Model must be Full Gyro (2)")
        assertEquals(1.toByte(), slot0Ds4[3], "Connection must be USB (1)")
        assertEquals(5.toByte(), slot0Ds4[10], "Battery must be Full (5)")

        // Slots 1..3 must report disconnected even if DS4 is active
        val slot1Ds4 = server.buildPortPayload(1)
        assertEquals(1.toByte(), slot1Ds4[0], "Slot 1")
        assertEquals(0.toByte(), slot1Ds4[1], "Slot 1 must be Disconnected (0)")

        // 2. When Xbox 360 mode is active (isDs4Active = false):
        server.isDs4Active = false
        val slot0Xbox = server.buildPortPayload(0)
        assertEquals(0.toByte(), slot0Xbox[0], "Slot 0")
        assertEquals(0.toByte(), slot0Xbox[1], "Slot 0 must be Disconnected (0) when not DS4")
        assertEquals(0.toByte(), slot0Xbox[2], "Model must be N/A (0)")
    }

    @Test
    fun testHandleIncomingPortsRequest() {
        val server = DsuServer()
        server.isDs4Active = true

        // Simulate client querying 4 ports: [0, 1, 2, 3]
        val reqPayload = ByteBuffer.allocate(4 + 4).order(ByteOrder.LITTLE_ENDIAN)
            .putInt(4) // Count of ports
            .put(0.toByte())
            .put(1.toByte())
            .put(2.toByte())
            .put(3.toByte())
            .array()

        val reqPacketBytes = buildTestClientPacket(0x100001, reqPayload)
        val datagramPacket = DatagramPacket(reqPacketBytes, reqPacketBytes.size, InetAddress.getByName("127.0.0.1"), 55555)
        
        // Calling handleIncomingPacket should process without error
        server.handleIncomingPacket(datagramPacket)
    }

    @Test
    fun testPadRegistrationRejectionWhenNotDs4() {
        val server = DsuServer()
        server.isDs4Active = false

        val reqPayload = ByteArray(1) { 0 } // Subscribe to all controllers
        val reqPacketBytes = buildTestClientPacket(0x100002, reqPayload)
        val datagramPacket = DatagramPacket(reqPacketBytes, reqPacketBytes.size, InetAddress.getByName("127.0.0.1"), 55555)

        server.handleIncomingPacket(datagramPacket)
        assertEquals(0, server.getClientCount(), "DSU client registration must be rejected when DS4 is not active")
    }

    @Test
    fun testLiveUdpSocketEndToEndWithClient() {
        val testPort = 26777
        val server = DsuServer()
        server.isDs4Active = true
        server.start(testPort)

        val client = DatagramSocket()
        client.soTimeout = 2500 // 2.5s timeout for tests
        val serverAddress = InetAddress.getByName("127.0.0.1")

        try {
            // Step 1: Version Request (0x100000)
            val verReq = buildTestClientPacket(0x100000, ByteArray(0))
            client.send(DatagramPacket(verReq, verReq.size, serverAddress, testPort))

            val verRespBuf = ByteArray(64)
            val verPacket = DatagramPacket(verRespBuf, verRespBuf.size)
            client.receive(verPacket)

            val verResp = ByteBuffer.wrap(verPacket.data, 0, verPacket.length).order(ByteOrder.LITTLE_ENDIAN)
            val verMagic = ByteArray(4)
            verResp.get(verMagic)
            assertEquals("DSUS", String(verMagic, Charsets.US_ASCII))
            verResp.short // version
            verResp.short // length
            verResp.int   // crc
            verResp.int   // server ID
            assertEquals(0x100000, verResp.int)
            assertEquals(1001.toShort(), verResp.short, "Server must report protocol version 1001")

            // Step 2: Ports Request (0x100001) for slots [0, 1]
            val portsReqPayload = ByteBuffer.allocate(6).order(ByteOrder.LITTLE_ENDIAN)
                .putInt(2)
                .put(0.toByte())
                .put(1.toByte())
                .array()
            val portsReq = buildTestClientPacket(0x100001, portsReqPayload)
            client.send(DatagramPacket(portsReq, portsReq.size, serverAddress, testPort))

            // Slot 0 response
            val slot0Buf = ByteArray(64)
            val slot0Packet = DatagramPacket(slot0Buf, slot0Buf.size)
            client.receive(slot0Packet)
            val slot0 = ByteBuffer.wrap(slot0Packet.data, 0, slot0Packet.length).order(ByteOrder.LITTLE_ENDIAN)
            slot0.position(16)
            assertEquals(0x100001, slot0.int)
            assertEquals(0.toByte(), slot0.get(), "Slot 0")
            assertEquals(2.toByte(), slot0.get(), "State must be Connected (2)")
            assertEquals(2.toByte(), slot0.get(), "Model must be Full Gyro (2)")
            assertEquals(1.toByte(), slot0.get(), "Connection must be USB (1)")

            // Slot 1 response
            val slot1Buf = ByteArray(64)
            val slot1Packet = DatagramPacket(slot1Buf, slot1Buf.size)
            client.receive(slot1Packet)
            val slot1 = ByteBuffer.wrap(slot1Packet.data, 0, slot1Packet.length).order(ByteOrder.LITTLE_ENDIAN)
            slot1.position(16)
            assertEquals(0x100001, slot1.int)
            assertEquals(1.toByte(), slot1.get(), "Slot 1")
            assertEquals(0.toByte(), slot1.get(), "State must be Disconnected (0)")

            // Step 3: Pad Data Subscription (0x100002)
            val subPayload = byteArrayOf(0x00) // Subscribe to all controllers
            val subReq = buildTestClientPacket(0x100002, subPayload)
            client.send(DatagramPacket(subReq, subReq.size, serverAddress, testPort))

            // Server immediately replies with an initial pad data packet for instant handshake
            val initPadBuf = ByteArray(128)
            val initPacket = DatagramPacket(initPadBuf, initPadBuf.size)
            client.receive(initPacket)
            assertEquals(100, initPacket.length, "Initial handshake pad packet must be exactly 100 bytes")

            // Step 4: Live Data Stream (updateInput)
            val liveInput = GamepadInput(
                btnA = true, // Cross
                btnX = true, // Square
                btnR1 = true,
                triggerL2 = 0.85f,
                leftStickY = 1.0f, // Upward
                accelZ = 9.80665f, // 1.0G
                gyroX = (PI / 4.0).toFloat() // 45 deg/s Pitch
            )
            server.updateInput(liveInput)

            val livePadBuf = ByteArray(128)
            val livePacket = DatagramPacket(livePadBuf, livePadBuf.size)
            client.receive(livePacket)
            assertEquals(100, livePacket.length, "Streamed pad packet must be exactly 100 bytes")

            val live = ByteBuffer.wrap(livePacket.data, 0, livePacket.length).order(ByteOrder.LITTLE_ENDIAN)
            live.position(20) // Start of payload (after 16 header + 4 messageType)
            assertEquals(0.toByte(), live.get(), "Slot 0")
            assertEquals(2.toByte(), live.get(), "Connected state")
            assertEquals(2.toByte(), live.get(), "Full gyro")
            assertEquals(1.toByte(), live.get(), "USB")
            val mac = ByteArray(6)
            live.get(mac)
            assertContentEquals(byteArrayOf(0x00, 0x11, 0x22, 0x33, 0x44, 0x55), mac)
            assertEquals(5.toByte(), live.get(), "Full battery")
            assertEquals(1.toByte(), live.get(), "Is active")
            val pCount = live.int
            assertTrue(pCount > 0)

            live.position(36) // Buttons 1 & 2
            val buttons1 = live.get().toInt() and 0xFF
            val buttons2 = live.get().toInt() and 0xFF
            // btnX (0x80) and btnA (0x40) and btnR1 (0x08) and L2 digital (0x01)
            assertTrue((buttons2 and 0x80) != 0, "Square (btnX) active")
            assertTrue((buttons2 and 0x40) != 0, "Cross (btnA) active")
            assertTrue((buttons2 and 0x08) != 0, "R1 active")
            assertTrue((buttons2 and 0x01) != 0, "L2 digital active")

            // Sticks at 40..43 (offsets 20..23 in payload)
            live.position(40)
            val lx = live.get().toInt() and 0xFF
            val ly = live.get().toInt() and 0xFF
            assertEquals(255, ly, "Left stick Y pushed up must be 255")

            // Analog buttons at 44..55 (offsets 24..35 in payload)
            live.position(44)
            val analogSquare = (live.get(48).toInt() and 0xFF) // Offset 28: Square
            val analogCross = (live.get(49).toInt() and 0xFF)  // Offset 29: Cross
            val analogL2 = (live.get(55).toInt() and 0xFF)     // Offset 35: L2
            assertEquals(255, analogSquare, "Analog Square must be 255")
            assertEquals(255, analogCross, "Analog Cross must be 255")
            assertEquals((0.85f * 255f).toInt(), analogL2, "Analog L2 must be scaled")

            // Gyroscope Pitch at 88..91 (offset 68 in payload)
            live.position(88)
            val pitch = live.float
            assertEquals(45.0f, pitch, 0.2f, "Gyro Pitch must be ~45 deg/s")

            // Step 5: Switch to Xbox 360 mode (isDs4Active = false)
            server.isDs4Active = false
            assertEquals(0, server.getClientCount(), "Active clients must be cleared when switching away from DS4")

            // Gated streaming: updateInput must NOT send any packets to clients
            server.updateInput(liveInput)
            client.soTimeout = 300
            try {
                client.receive(DatagramPacket(ByteArray(128), 128))
                fail("Expected SocketTimeoutException: DSU server must NOT stream packets when DS4 is not active!")
            } catch (e: SocketTimeoutException) {
                // Expected: No packet was sent
            }

            // Gated ports query: Slot 0 must now report Disconnected (0)
            client.soTimeout = 2500
            client.send(DatagramPacket(portsReq, portsReq.size, serverAddress, testPort))
            val gatedSlot0Buf = ByteArray(64)
            client.receive(DatagramPacket(gatedSlot0Buf, gatedSlot0Buf.size))
            val gatedSlot0 = ByteBuffer.wrap(gatedSlot0Buf).order(ByteOrder.LITTLE_ENDIAN)
            gatedSlot0.position(16)
            assertEquals(0x100001, gatedSlot0.int)
            assertEquals(0.toByte(), gatedSlot0.get(), "Slot 0")
            assertEquals(0.toByte(), gatedSlot0.get(), "State must be Disconnected (0) in Xbox mode")

        } finally {
            client.close()
            server.stop()
        }
    }

    private fun buildTestClientPacket(messageType: Int, payload: ByteArray): ByteArray {
        val totalLength = 16 + 4 + payload.size
        val buffer = ByteBuffer.allocate(totalLength).order(ByteOrder.LITTLE_ENDIAN)
        buffer.put("DSUC".toByteArray(Charsets.US_ASCII))
        buffer.putShort(1001)
        buffer.putShort((payload.size + 4).toShort())
        buffer.putInt(0) // CRC placeholder
        buffer.putInt(1234) // Client ID
        buffer.putInt(messageType)
        buffer.put(payload)
        return buffer.array()
    }
}
