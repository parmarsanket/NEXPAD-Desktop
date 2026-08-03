package com.sanket.tools.nexpaddesktop.driver

import com.sanket.tools.nexpaddesktop.model.GamepadInput
import com.sanket.tools.nexpaddesktop.driver.jna.ViGEmClientLibrary
import kotlin.math.roundToInt
import kotlin.math.abs

/**
 * The Translation Layer for DualShock 4 virtual controllers.
 *
 * This class handles the complex stateful mapping required to generate a 63-byte `DS4_REPORT_EX`.
 * Unlike the Xbox 360 mapper (which is a stateless object), the DS4 mapper requires persistent state for:
 * 1. Monotonic timestamps (for Steam Input motion processing)
 * 2. IMU auto-calibration (removing resting gyroscope bias)
 */
class DualShock4InputMapper {

    // =========================================================================
    // SENSOR STATE & TIMING
    // =========================================================================
    private var gyroBiasX = 0f
    private var gyroBiasY = 0f
    private var gyroBiasZ = 0f
    private var calibrationSamples = 0
    private var stillnessFrames = 0
    private var startTimeNanos = System.nanoTime()
    private var lastDs4DebugSignature = ""

    companion object {
        private const val CALIBRATION_COUNT = 100
        const val GYRO_SCALAR = 939.0f
        const val ACCEL_SCALAR = 835.3f
        const val STILLNESS_THRESHOLD = 0.015f
        const val CALIBRATION_ADJUST_RATE = 0.02f
        const val TIMESTAMP_DIVISOR = 188.0

        const val BATTERY_FULL: Byte = 0xFF.toByte()
        const val BATTERY_FULL_SPECIAL: Byte = 0x1A
        const val TOUCH_PACKET_COUNT: Byte = 0x01
        const val TOUCH_POINT_UP: Byte = 0x80.toByte()
    }

    /**
     * Resets the mapper's internal state. Should be called when the controller connects.
     */
    fun reset() {
        gyroBiasX = 0f
        gyroBiasY = 0f
        gyroBiasZ = 0f
        calibrationSamples = 0
        stillnessFrames = 0
        startTimeNanos = System.nanoTime()
    }

    /**
     * Translates a generic network payload into a native C memory block.
     */
    fun map(input: GamepadInput): ViGEmClientLibrary.DS4_REPORT_EX.ByValue {
        val report = ViGEmClientLibrary.DS4_REPORT_EX.ByValue()

        // 1. Analog Sticks
        report.bThumbLX = stickToByte(input.leftStickX)
        report.bThumbLY = stickToByte(-input.leftStickY)     
        report.bThumbRX = stickToByte(input.rightStickX)
        report.bThumbRY = stickToByte(-input.rightStickY)

        // 2. Buttons & D-Pad
        var btn = encodeDPad(input) 
        if (input.btnX)              btn = btn or 0x0010   
        if (input.btnA)              btn = btn or 0x0020   
        if (input.btnB)              btn = btn or 0x0040   
        if (input.btnY)              btn = btn or 0x0080   
        if (input.btnL1)             btn = btn or 0x0100   
        if (input.btnR1)             btn = btn or 0x0200   
        if (input.triggerL2 > 0.1f)  btn = btn or 0x0400   
        if (input.triggerR2 > 0.1f)  btn = btn or 0x0800   
        if (input.btnSelect || input.btnShare) btn = btn or 0x1000   
        if (input.btnStart)                    btn = btn or 0x2000   
        if (input.btnL3)                       btn = btn or 0x4000   
        if (input.btnR3)                       btn = btn or 0x8000   
        report.wButtons = btn.toShort()

        // 3. Special Buttons
        var special = 0
        if (input.btnGuide) special = special or 0x01      
        if (input.btnScreenshot) special = special or 0x02 
        report.bSpecial = special.toByte()

        // 4. Analog Triggers
        report.bTriggerL = triggerToByte(input.triggerL2)
        report.bTriggerR = triggerToByte(input.triggerR2)

        debugDs4Mapping(input, report)

        // 5. Hardware Timestamp
        val elapsedUs = (System.nanoTime() - startTimeNanos) / 1000L
        val tick = (elapsedUs / TIMESTAMP_DIVISOR).toLong()
        report.wTimestamp = tick.toShort()

        // 6. Extended Battery & Touchpad Padding
        report.bBatteryLvl = BATTERY_FULL
        report.padding[5] = BATTERY_FULL_SPECIAL
        report.padding[8] = TOUCH_PACKET_COUNT
        report.padding[10] = TOUCH_POINT_UP
        report.padding[14] = TOUCH_POINT_UP
        report.padding[18] = TOUCH_POINT_UP
        report.padding[22] = TOUCH_POINT_UP

        // 7. IMU Data (Gyroscope & Accelerometer)
        val (calX, calY, calZ) = calibrateGyro(input.gyroX, input.gyroY, input.gyroZ)
        
        report.wGyroX = toSafeShort( calX * GYRO_SCALAR)   
        report.wGyroY = toSafeShort(-calZ * GYRO_SCALAR)   
        report.wGyroZ = toSafeShort(-calY * GYRO_SCALAR)   

        var ax = input.accelX * ACCEL_SCALAR
        var ay = input.accelY * ACCEL_SCALAR
        var az = input.accelZ * ACCEL_SCALAR

        if (ax == 0f && ay == 0f && az == 0f) {
            az = -8192f 
        }

        report.wAccelX = toSafeShort(ax)
        report.wAccelY = toSafeShort(-az)
        report.wAccelZ = toSafeShort(-ay)

        return report
    }

    // =========================================================================
    // PRIVATE UTILITIES
    // =========================================================================

    private fun stickToByte(value: Float): Byte {
        return ((value.coerceIn(-1f, 1f) + 1f) * 127.5f).roundToInt().coerceIn(0, 255).toByte()
    }

    private fun triggerToByte(value: Float): Byte {
        return (value * 255f).toInt().coerceIn(0, 255).toByte()
    }

    private fun toSafeShort(value: Float): Short {
        return value.toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
    }

    private fun encodeDPad(input: GamepadInput): Int = when {
        input.dpadUp   && input.dpadRight -> 1   
        input.dpadDown && input.dpadRight -> 3   
        input.dpadDown && input.dpadLeft  -> 5   
        input.dpadUp   && input.dpadLeft  -> 7   
        input.dpadUp                      -> 0   
        input.dpadRight                   -> 2   
        input.dpadDown                    -> 4   
        input.dpadLeft                    -> 6   
        else                              -> 8   
    }

    private fun calibrateGyro(rawX: Float, rawY: Float, rawZ: Float): Triple<Float, Float, Float> {
        if (calibrationSamples < CALIBRATION_COUNT) {
            if (rawX != 0f || rawY != 0f || rawZ != 0f) {
                gyroBiasX += rawX; gyroBiasY += rawY; gyroBiasZ += rawZ
                calibrationSamples++
                if (calibrationSamples == CALIBRATION_COUNT) {
                    gyroBiasX /= CALIBRATION_COUNT
                    gyroBiasY /= CALIBRATION_COUNT
                    gyroBiasZ /= CALIBRATION_COUNT
                }
            }
            return Triple(0f, 0f, 0f)
        }
        val calX = rawX - gyroBiasX
        val calY = rawY - gyroBiasY
        val calZ = rawZ - gyroBiasZ

        if (abs(calX) < STILLNESS_THRESHOLD && abs(calY) < STILLNESS_THRESHOLD && abs(calZ) < STILLNESS_THRESHOLD) {
            stillnessFrames++
            if (stillnessFrames > 120) {
                gyroBiasX += calX * CALIBRATION_ADJUST_RATE
                gyroBiasY += calY * CALIBRATION_ADJUST_RATE
                gyroBiasZ += calZ * CALIBRATION_ADJUST_RATE
            }
        } else {
            stillnessFrames = 0
        }
        return Triple(calX, calY, calZ)
    }

    private fun debugDs4Mapping(input: GamepadInput, report: ViGEmClientLibrary.DS4_REPORT_EX) {
        val lx = report.bThumbLX.toInt() and 0xFF
        val ly = report.bThumbLY.toInt() and 0xFF
        val rx = report.bThumbRX.toInt() and 0xFF
        val ry = report.bThumbRY.toInt() and 0xFF
        val buttons = report.wButtons.toInt() and 0xFFFF
        val special = report.bSpecial.toInt() and 0xFF

        val signature = listOf(
            (input.leftStickX * 10f).roundToInt(),
            (input.leftStickY * 10f).roundToInt(),
            (input.rightStickX * 10f).roundToInt(),
            (input.rightStickY * 10f).roundToInt(),
            buttons,
            special
        ).joinToString("|")

        if (signature == lastDs4DebugSignature) return
        lastDs4DebugSignature = signature

        println(
            "NEXPAD_DS4_DEBUG " +
                "DS4 LX=$lx LY=$ly RX=$rx RY=$ry | " +
                "buttons=0x${buttons.toString(16).padStart(4, '0')} " +
                "special=0x${special.toString(16).padStart(2, '0')}"
        )
    }
}
