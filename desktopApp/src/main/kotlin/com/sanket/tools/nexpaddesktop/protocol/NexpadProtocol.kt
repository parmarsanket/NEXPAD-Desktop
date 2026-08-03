package com.sanket.tools.nexpaddesktop.protocol

import com.sanket.tools.nexpaddesktop.model.GamepadFeedback
import com.sanket.tools.nexpaddesktop.model.GamepadInput
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Universal Binary Protocol for NEXPAD.
 *
 * Byte layout for Input Packet (40 bytes):
 * | Offset | Size | Field           | Type    | Range / Notes                                |
 * |--------|------|-----------------|---------|----------------------------------------------|
 * | 0      | 1    | Version         | Byte    | Always 1                                     |
 * | 1      | 4    | Buttons         | Int32   | Bitmask (see below)                          |
 * | 5      | 2    | Left Stick X    | Int16   | -32768 to 32767                              |
 * | 7      | 2    | Left Stick Y    | Int16   | -32768 to 32767                              |
 * | 9      | 2    | Right Stick X   | Int16   | -32768 to 32767                              |
 * | 11     | 2    | Right Stick Y   | Int16   | -32768 to 32767                              |
 * | 13     | 1    | Trigger L2      | UInt8   | 0 to 255                                     |
 * | 14     | 1    | Trigger R2      | UInt8   | 0 to 255                                     |
 * | 15     | 24   | IMU Sensors     | Float*6 | gyroX, gyroY, gyroZ, accelX, accelY, accelZ  |
 * | 39     | 1    | Reserved        | Byte    | 0                                            |
 *
 * Byte layout for Feedback Packet (6 bytes):
 * | Offset | Size | Field           | Type    | Range / Notes                                |
 * |--------|------|-----------------|---------|----------------------------------------------|
 * | 0      | 1    | Version         | Byte    | Always 1                                     |
 * | 1      | 1    | Left Motor      | UInt8   | 0 to 255                                     |
 * | 2      | 1    | Right Motor     | UInt8   | 0 to 255                                     |
 * | 3      | 3    | Lightbar RGB    | UInt8*3 | R, G, B (0-255)                              |
 */
object NexpadProtocol {
    const val PROTOCOL_VERSION: Byte = 1
    const val INPUT_PACKET_SIZE = 40
    const val FEEDBACK_PACKET_SIZE = 6

    // Button bitmasks
    const val MASK_BTN_A = 1 shl 0
    const val MASK_BTN_B = 1 shl 1
    const val MASK_BTN_X = 1 shl 2
    const val MASK_BTN_Y = 1 shl 3
    const val MASK_DPAD_UP = 1 shl 4
    const val MASK_DPAD_DOWN = 1 shl 5
    const val MASK_DPAD_LEFT = 1 shl 6
    const val MASK_DPAD_RIGHT = 1 shl 7
    const val MASK_BTN_L1 = 1 shl 8
    const val MASK_BTN_R1 = 1 shl 9
    const val MASK_BTN_L3 = 1 shl 10
    const val MASK_BTN_R3 = 1 shl 11
    const val MASK_BTN_START = 1 shl 12
    const val MASK_BTN_SELECT = 1 shl 13
    const val MASK_BTN_GUIDE = 1 shl 14
    const val MASK_BTN_SHARE = 1 shl 15
    const val MASK_BTN_SCREENSHOT = 1 shl 16
    const val MASK_BTN_M1 = 1 shl 17
    const val MASK_BTN_M2 = 1 shl 18
    const val MASK_BTN_M3 = 1 shl 19
    const val MASK_BTN_M4 = 1 shl 20
    const val MASK_BTN_PROFILE = 1 shl 21
    const val MASK_BTN_TURBO = 1 shl 22

    /**
     * Converts a float value in the range [-1f, 1f] to a short [-32768, 32767].
     */
    fun floatToStick(value: Float): Short {
        val clamped = value.coerceIn(-1f, 1f)
        return if (clamped >= 0) {
            (clamped * 32767f).toInt().toShort()
        } else {
            (clamped * 32768f).toInt().toShort()
        }
    }

    /**
     * Converts a short value in the range [-32768, 32767] to a float [-1f, 1f].
     */
    fun stickToFloat(value: Short): Float {
        return if (value >= 0) {
            value.toFloat() / 32767f
        } else {
            value.toFloat() / 32768f
        }
    }

    /**
     * Converts a float value in the range [0f, 1f] to a byte [0, 255].
     */
    fun floatToTrigger(value: Float): Byte {
        val clamped = value.coerceIn(0f, 1f)
        return (clamped * 255f).toInt().toByte()
    }

    /**
     * Converts an unsigned byte value [0, 255] to a float [0f, 1f].
     */
    fun triggerToFloat(value: Byte): Float {
        val unsignedVal = value.toInt() and 0xFF
        return unsignedVal.toFloat() / 255f
    }

    /**
     * Decodes a binary packet into a GamepadInput object.
     * Returns null if the packet size is incorrect or the protocol version does not match.
     */
    fun decodeInput(data: ByteArray): GamepadInput? {
        if (data.size != INPUT_PACKET_SIZE) return null
        
        val buffer = ByteBuffer.wrap(data).order(ByteOrder.BIG_ENDIAN)
        val version = buffer.get()
        if (version != PROTOCOL_VERSION) return null

        val buttons = buffer.getInt()
        
        val leftStickX = buffer.getShort()
        val leftStickY = buffer.getShort()
        val rightStickX = buffer.getShort()
        val rightStickY = buffer.getShort()
        
        val triggerL2 = buffer.get()
        val triggerR2 = buffer.get()
        
        val gyroX = buffer.getFloat()
        val gyroY = buffer.getFloat()
        val gyroZ = buffer.getFloat()
        
        val accelX = buffer.getFloat()
        val accelY = buffer.getFloat()
        val accelZ = buffer.getFloat()
        
        // Skip reserved byte
        // buffer.get()

        return GamepadInput(
            btnA = (buttons and MASK_BTN_A) != 0,
            btnB = (buttons and MASK_BTN_B) != 0,
            btnX = (buttons and MASK_BTN_X) != 0,
            btnY = (buttons and MASK_BTN_Y) != 0,
            dpadUp = (buttons and MASK_DPAD_UP) != 0,
            dpadDown = (buttons and MASK_DPAD_DOWN) != 0,
            dpadLeft = (buttons and MASK_DPAD_LEFT) != 0,
            dpadRight = (buttons and MASK_DPAD_RIGHT) != 0,
            btnL1 = (buttons and MASK_BTN_L1) != 0,
            btnR1 = (buttons and MASK_BTN_R1) != 0,
            btnL3 = (buttons and MASK_BTN_L3) != 0,
            btnR3 = (buttons and MASK_BTN_R3) != 0,
            btnStart = (buttons and MASK_BTN_START) != 0,
            btnSelect = (buttons and MASK_BTN_SELECT) != 0,
            btnGuide = (buttons and MASK_BTN_GUIDE) != 0,
            btnShare = (buttons and MASK_BTN_SHARE) != 0,
            btnScreenshot = (buttons and MASK_BTN_SCREENSHOT) != 0,
            btnM1 = (buttons and MASK_BTN_M1) != 0,
            btnM2 = (buttons and MASK_BTN_M2) != 0,
            btnM3 = (buttons and MASK_BTN_M3) != 0,
            btnM4 = (buttons and MASK_BTN_M4) != 0,
            btnProfile = (buttons and MASK_BTN_PROFILE) != 0,
            btnTurbo = (buttons and MASK_BTN_TURBO) != 0,
            triggerL2 = triggerToFloat(triggerL2),
            triggerR2 = triggerToFloat(triggerR2),
            leftStickX = stickToFloat(leftStickX),
            leftStickY = stickToFloat(leftStickY),
            rightStickX = stickToFloat(rightStickX),
            rightStickY = stickToFloat(rightStickY),
            gyroX = gyroX,
            gyroY = gyroY,
            gyroZ = gyroZ,
            accelX = accelX,
            accelY = accelY,
            accelZ = accelZ
        )
    }

    /**
     * Encodes a GamepadFeedback object into a 6-byte array using the binary protocol.
     */
    fun encodeFeedback(feedback: GamepadFeedback): ByteArray {
        val buffer = ByteBuffer.allocate(FEEDBACK_PACKET_SIZE).order(ByteOrder.BIG_ENDIAN)
        buffer.put(PROTOCOL_VERSION)
        buffer.put(feedback.leftMotorSpeed.coerceIn(0, 255).toByte())
        buffer.put(feedback.rightMotorSpeed.coerceIn(0, 255).toByte())
        // Lightbar RGB: Default to 0 as it's not present in the current GamepadFeedback model
        buffer.put(0) // R
        buffer.put(0) // G
        buffer.put(0) // B
        return buffer.array()
    }

    /**
     * Decodes a 6-byte array into a GamepadFeedback object using the binary protocol.
     */
    fun decodeFeedback(data: ByteArray): GamepadFeedback? {
        if (data.size != FEEDBACK_PACKET_SIZE) return null
        
        val buffer = ByteBuffer.wrap(data).order(ByteOrder.BIG_ENDIAN)
        val version = buffer.get()
        if (version != PROTOCOL_VERSION) return null

        val leftMotorSpeed = buffer.get().toInt() and 0xFF
        val rightMotorSpeed = buffer.get().toInt() and 0xFF
        // Ignore the RGB bytes
        
        return GamepadFeedback(
            leftMotorSpeed = leftMotorSpeed,
            rightMotorSpeed = rightMotorSpeed
        )
    }
}
