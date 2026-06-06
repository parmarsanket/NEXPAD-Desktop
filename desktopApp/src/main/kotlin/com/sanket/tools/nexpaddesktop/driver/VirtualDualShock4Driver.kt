package com.sanket.tools.nexpaddesktop.driver

import com.sanket.tools.nexpaddesktop.model.GamepadInput
import com.sanket.tools.nexpaddesktop.model.GamepadFeedback
import com.sun.jna.Pointer
import com.sanket.tools.nexpaddesktop.driver.jna.ViGEmClientLibrary
import kotlin.math.roundToInt

/**
 * Virtual DualShock 4 controller driver using ViGEm Bus.
 *
 * Implements the full DS4_REPORT_EX (63-byte) report including:
 *   - Analog sticks, D-pad, face/shoulder/system buttons, analog triggers
 *   - 6-axis IMU (Gyroscope + Accelerometer) with auto-calibration
 *   - Monotonic timestamp for Steam Input gyro compatibility
 *   - Battery status (always full / USB-powered)
 *   - Rumble feedback (large + small motor) via ViGEm notification callback
 *
 * ──────────────────────────────────────────────────────────
 *  DS4_REPORT_EX Byte Map (63 bytes total, packed, LE)
 * ──────────────────────────────────────────────────────────
 *   [0]      bThumbLX         Left Stick X   (0=left, 128=center, 255=right)
 *   [1]      bThumbLY         Left Stick Y   (0=up,   128=center, 255=down)
 *   [2]      bThumbRX         Right Stick X
 *   [3]      bThumbRY         Right Stick Y
 *   [4-5]    wButtons         D-pad (bits 0-3) + buttons (USHORT LE)
 *   [6]      bSpecial         PS button (bit 0), Touchpad click (bit 1)
 *   [7]      bTriggerL        L2 trigger (0–255)
 *   [8]      bTriggerR        R2 trigger (0–255)
 *   [9-10]   wTimestamp       16-bit monotonic counter (~188 µs / tick)
 *   [11]     bBatteryLvl      Battery level
 *   [12-13]  wGyroX           Gyro Pitch  (INT16 signed LE)
 *   [14-15]  wGyroY           Gyro Yaw    (INT16 signed LE)
 *   [16-17]  wGyroZ           Gyro Roll   (INT16 signed LE)
 *   [18-19]  wAccelX          Accel X     (INT16 signed LE)
 *   [20-21]  wAccelY          Accel Y     (INT16 signed LE)
 *   [22-23]  wAccelZ          Accel Z     (INT16 signed LE)
 *   [24-28]  padding
 *   [29]     bBatterySpecial  Alternate battery byte
 *   [30-31]  padding
 *   [32]     bTouchPacketsN   Touch packet count
 *   [33-62]  touch data
 * ──────────────────────────────────────────────────────────
 *
 * Axis mapping (Android phone in landscape → DS4 held normally):
 *   DS4 Pitch (wGyroX) =  Android gyroX
 *   DS4 Yaw   (wGyroY) = −Android gyroZ   (swapped + negated)
 *   DS4 Roll  (wGyroZ) = −Android gyroY   (negated)
 *
 * Scaling:
 *   Gyro:  Android rad/s  × 939.0  → INT16  (BMI055 ±2000°/s ≈ 16.38 LSB/°/s × 57.296 °/rad)
 *   Accel: Android m/s²   × 835.3  → INT16  (8192 LSB/g ÷ 9.80665 m/s²/g)
 */
class VirtualDualShock4Driver(
    private val onRumble: (GamepadFeedback) -> Unit = {}
) : IGamepadDriver {

    // ── ViGEm handles ──────────────────────────────────────
    private var isConnected = false
    private var client: Pointer? = null
    private var target: Pointer? = null
    private var notificationCallback: ViGEmClientLibrary.PVIGEM_DS4_NOTIFICATION? = null

    // ── Gyro auto-calibration ──────────────────────────────
    private var gyroBiasX = 0f
    private var gyroBiasY = 0f
    private var gyroBiasZ = 0f
    private var calibrationSamples = 0
    private val CALIBRATION_COUNT = 100   // ~1.7 s at 60 Hz

    // ── Timestamp base ─────────────────────────────────────
    private var startTimeNanos = System.nanoTime()
    private var lastLogTime = 0L
    private var lastDs4DebugSignature = ""

    // ── Constants ──────────────────────────────────────────
    companion object {
        // DS4 gyro (BMI055): ±2000°/s over INT16
        //   32 767 / 2000 ≈ 16.38 LSB per °/s
        //   Android sends rad/s → × (180/π) → °/s → × 16.38 ≈ 939.0
        const val GYRO_SCALAR = 939.0f

        // DS4 accel: 1 g ≈ 8192 raw
        //   Android sends m/s² → 8192 / 9.80665 ≈ 835.3
        const val ACCEL_SCALAR = 835.3f

        // Timestamp: real DS4 ticks at ~188 µs (5.33 kHz)
        //   ticks = elapsed_µs / 1.3333
        const val TIMESTAMP_DIVISOR = 1.3333

        // Neutral DS4 extended-report values used by ViGEm-compatible DS4 packets.
        const val BATTERY_FULL: Byte = 0xFF.toByte()
        const val BATTERY_FULL_SPECIAL: Byte = 0x1A
        const val TOUCH_PACKET_COUNT: Byte = 0x01
        const val TOUCH_POINT_UP: Byte = 0x80.toByte()

        // Official Sony DualShock 4 USB identifiers
        const val DS4_VID: Short = 0x054C
        const val DS4_PID: Short = 0x05C4
    }


    // ════════════════════════════════════════════════════════
    //  CONNECT
    // ════════════════════════════════════════════════════════
    override fun connect() {
        try {
            val lib = ViGEmClientLibrary.INSTANCE

            client = lib.vigem_alloc()
                ?: throw Exception("Failed to allocate ViGEm Client")

            val result = lib.vigem_connect(client)
            if (result != 0x20000000) {
                throw Exception("ViGEm Bus connect failed: 0x${result.toString(16)}")
            }

            target = lib.vigem_target_ds4_alloc()

            // Official Sony VID/PID so Steam + Windows see a real PS4 controller
            lib.vigem_target_set_vid(target, DS4_VID)
            lib.vigem_target_set_pid(target, DS4_PID)

            lib.vigem_target_add(client, target)

            // Register rumble/lightbar notification callback
            notificationCallback = object : ViGEmClientLibrary.PVIGEM_DS4_NOTIFICATION {
                override fun callback(
                    client: Pointer?,
                    target: Pointer?,
                    largeMotor: Byte,
                    smallMotor: Byte,
                    lightbarColor: Int,
                    userData: Pointer?
                ) {
                    try {
                        val left  = largeMotor.toInt() and 0xFF
                        val right = smallMotor.toInt() and 0xFF
                        onRumble(GamepadFeedback(left, right))
                    } catch (e: Throwable) {
                        System.err.println("⚠️ DS4 rumble callback error: ${e.message}")
                    }
                }
            }
            lib.vigem_target_ds4_register_notification(
                client, target, notificationCallback!!, null
            )

            // Reset calibration
            gyroBiasX = 0f
            gyroBiasY = 0f
            gyroBiasZ = 0f
            calibrationSamples = 0
            startTimeNanos = System.nanoTime()

            isConnected = true
            println("✅ Virtual Sony DualShock 4 Controller Connected successfully!")
            println("⏳ Auto-calibrating Gyroscope... Keep the phone still for ~2 seconds.")

        } catch (e: Exception) {
            System.err.println("⚠️ ViGEm DS4 init failed: ${e.message}")
            isConnected = false
        }
    }


    // ════════════════════════════════════════════════════════
    //  DISCONNECT
    // ════════════════════════════════════════════════════════
    override fun disconnect() {
        try {
            val lib = ViGEmClientLibrary.INSTANCE
            if (target != null) lib.vigem_target_ds4_unregister_notification(target)
            if (client != null && target != null) {
                lib.vigem_target_remove(client, target)
                lib.vigem_target_free(target)
                lib.vigem_disconnect(client)
                lib.vigem_free(client)
            }
        } catch (_: Exception) { }

        isConnected = false
        client = null
        target = null
        println("Disconnected Virtual DS4 Controller.")
    }


    // ════════════════════════════════════════════════════════
    //  UPDATE INPUT  (called ~60 Hz from UDP receiver)
    // ════════════════════════════════════════════════════════
    override fun updateInput(input: GamepadInput) {
        if (!isConnected || client == null || target == null) return

        val report = ViGEmClientLibrary.DS4_REPORT_EX.ByValue()

        // ── STICKS (bytes 0-3) ─────────────────────────────
        report.bThumbLX = stickToByte(input.leftStickX)
        report.bThumbLY = stickToByte(-input.leftStickY)     // Android UP = +1.0 -> inverted to -1.0 -> 0 (DS4 UP)
        report.bThumbRX = stickToByte(input.rightStickX)
        report.bThumbRY = stickToByte(-input.rightStickY)

        // ── BUTTONS (wButtons USHORT LE) ───────────────────
        var btn = encodeDPad(input)                     // bits 0-3 = hat

        if (input.btnX)              btn = btn or 0x0010   // □ Square
        if (input.btnA)              btn = btn or 0x0020   // ✕ Cross
        if (input.btnB)              btn = btn or 0x0040   // ○ Circle
        if (input.btnY)              btn = btn or 0x0080   // △ Triangle
        if (input.btnL1)             btn = btn or 0x0100   // L1
        if (input.btnR1)             btn = btn or 0x0200   // R1
        if (input.triggerL2 > 0.1f)  btn = btn or 0x0400   // L2 digital
        if (input.triggerR2 > 0.1f)  btn = btn or 0x0800   // R2 digital
        
        if (input.btnSelect || input.btnShare) btn = btn or 0x1000   // Share
        if (input.btnStart)                    btn = btn or 0x2000   // Options
        if (input.btnL3)                       btn = btn or 0x4000   // L3
        if (input.btnR3)                       btn = btn or 0x8000   // R3

        report.wButtons = btn.toShort()

        // ── SPECIAL (bSpecial) ─────────────────────────────
        var special = 0
        if (input.btnGuide) special = special or 0x01      // PS button (Bit 0)
        
        // Map the phone's Screenshot/Capture button to the giant PS4 Touchpad Click!
        if (input.btnScreenshot) special = special or 0x02 // Touchpad click (Bit 1)
        
        report.bSpecial = special.toByte()

        // ── TRIGGERS (bytes 7-8) ───────────────────────────
        report.bTriggerL = triggerToByte(input.triggerL2)
        report.bTriggerR = triggerToByte(input.triggerR2)

        debugDs4Mapping(input, report)

        // ── TIMESTAMP ──────────────────────────────────────
        val elapsedUs = (System.nanoTime() - startTimeNanos) / 1000L
        val tick = (elapsedUs / TIMESTAMP_DIVISOR).toLong()
        report.wTimestamp = tick.toShort()

        // ── BATTERY ────────────────────────────────────────
        report.bBatteryLvl = BATTERY_FULL
        report.padding[5] = BATTERY_FULL_SPECIAL
        report.padding[8] = TOUCH_PACKET_COUNT
        report.padding[10] = TOUCH_POINT_UP
        report.padding[14] = TOUCH_POINT_UP
        report.padding[18] = TOUCH_POINT_UP
        report.padding[22] = TOUCH_POINT_UP

        // ── GYROSCOPE ──────────────────────────────────────
        val (calX, calY, calZ) = calibrateGyro(
            input.gyroX, input.gyroY, input.gyroZ
        )

        //  Android (Landscape) → DS4 physical axis mapping:
        //    Phone X (Right)   -> DS4 X (Right)
        //    Phone Y (Forward) -> DS4 Z (Forward)
        //    Phone Z (Up)      -> DS4 Y (-Down)

        report.wGyroX = toSafeShort( calX * GYRO_SCALAR)   // Pitch
        report.wGyroY = toSafeShort(-calZ * GYRO_SCALAR)   // Yaw
        report.wGyroZ = toSafeShort( calY * GYRO_SCALAR)   // Roll (Positive!)

        // ── ACCELEROMETER ──────────────────────────────────
        var ax = input.accelX * ACCEL_SCALAR
        var ay = input.accelY * ACCEL_SCALAR
        var az = input.accelZ * ACCEL_SCALAR

        // If no accel data at all, fake 1 G downward so sensor-fusion doesn't break
        if (ax == 0f && ay == 0f && az == 0f) {
            // DS4 Y is Down, resting flat means a reaction force of -1G on Y
            report.wAccelX = 0
            report.wAccelY = -8192
            report.wAccelZ = 0
        } else {
            report.wAccelX = toSafeShort(ax)
            report.wAccelY = toSafeShort(-az)  // DS4 Y (Down) = -Phone Z (Up)
            report.wAccelZ = toSafeShort(ay)   // DS4 Z (Forward) = Phone Y (Forward)
        }

        // ── SUBMIT ─────────────────────────────────────────
        try {
            ViGEmClientLibrary.INSTANCE.vigem_target_ds4_update_ex(
                client, target, report
            )
        } catch (_: Exception) { }
    }


    // ════════════════════════════════════════════════════════
    //  SIMULATE CRASH  (test rumble from desktop UI)
    // ════════════════════════════════════════════════════════
    override fun simulateCrash() {
        try {
            println("💥 Simulating DS4 CRASH! Sending heavy rumble feedback...")
            onRumble(GamepadFeedback(leftMotorSpeed = 255, rightMotorSpeed = 200))
        } catch (e: Throwable) {
            println("⚠️ simulateCrash error: ${e.message}")
        }
    }


    // ════════════════════════════════════════════════════════
    //  PRIVATE HELPERS
    // ════════════════════════════════════════════════════════

    /** Map stick float (−1 … +1) → DS4 byte (0 … 255, 128 = center) */
    private fun stickToByte(value: Float): Byte {
        return ((value.coerceIn(-1f, 1f) + 1f) * 127.5f)
            .roundToInt()
            .coerceIn(0, 255)
            .toByte()
    }

    private fun debugDs4Mapping(input: GamepadInput, report: ViGEmClientLibrary.DS4_REPORT_EX) {
        val lx = report.bThumbLX.toUnsignedInt()
        val ly = report.bThumbLY.toUnsignedInt()
        val rx = report.bThumbRX.toUnsignedInt()
        val ry = report.bThumbRY.toUnsignedInt()
        val buttons = report.wButtons.toInt() and 0xFFFF
        val special = report.bSpecial.toUnsignedInt()

        val signature = listOf(
            input.leftStickX.quantizeDebug(),
            input.leftStickY.quantizeDebug(),
            input.rightStickX.quantizeDebug(),
            input.rightStickY.quantizeDebug(),
            buttons,
            special
        ).joinToString("|")

        if (signature == lastDs4DebugSignature) return
        lastDs4DebugSignature = signature

        println(
            "NEXPAD_DS4_DEBUG " +
                "RAW LS=(${input.leftStickX.formatDebug()}, ${input.leftStickY.formatDebug()}) " +
                "RS=(${input.rightStickX.formatDebug()}, ${input.rightStickY.formatDebug()}) | " +
                "DS4 LX=$lx(${axisName(lx, true)}) " +
                "LY=$ly(${axisName(ly, false)}) " +
                "RX=$rx(${axisName(rx, true)}) " +
                "RY=$ry(${axisName(ry, false)}) | " +
                "buttons=0x${buttons.toString(16).padStart(4, '0')} " +
                "special=0x${special.toString(16).padStart(2, '0')}"
        )
    }

    private fun axisName(value: Int, isX: Boolean): String = when {
        value < 96 -> if (isX) "LEFT" else "UP" // Fixed: < 96 is UP for Y-axis (0 is UP)
        value > 160 -> if (isX) "RIGHT" else "DOWN" // Fixed: > 160 is DOWN for Y-axis (255 is DOWN)
        else -> "CENTER"
    }

    private fun Byte.toUnsignedInt(): Int = toInt() and 0xFF

    private fun Float.quantizeDebug(): Int = (this * 10f).roundToInt()

    private fun Float.formatDebug(): String = "%.2f".format(this)

    /** Map trigger float (0 … 1) → DS4 byte (0 … 255) */
    private fun triggerToByte(value: Float): Byte {
        return (value * 255f).toInt().coerceIn(0, 255).toByte()
    }

    /** Float → clamped Int16 (Short) without overflow */
    private fun toSafeShort(value: Float): Short {
        return value.toInt()
            .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
            .toShort()
    }

    /** Encode D-pad booleans into DS4 hat switch value (0–8). */
    private fun encodeDPad(input: GamepadInput): Int = when {
        input.dpadUp   && input.dpadRight -> 1   // ↗
        input.dpadDown && input.dpadRight -> 3   // ↘
        input.dpadDown && input.dpadLeft  -> 5   // ↙
        input.dpadUp   && input.dpadLeft  -> 7   // ↖
        input.dpadUp                      -> 0   // ↑
        input.dpadRight                   -> 2   // →
        input.dpadDown                    -> 4   // ↓
        input.dpadLeft                    -> 6   // ←
        else                              -> 8   // released
    }

    /**
     * Auto-calibrate gyro bias from the first [CALIBRATION_COUNT] samples.
     * While calibrating, returns (0, 0, 0) to prevent initial drift.
     * After calibration, subtracts the measured resting bias.
     */
    private fun calibrateGyro(
        rawX: Float, rawY: Float, rawZ: Float
    ): Triple<Float, Float, Float> {

        if (calibrationSamples < CALIBRATION_COUNT) {
            gyroBiasX += rawX
            gyroBiasY += rawY
            gyroBiasZ += rawZ
            calibrationSamples++

            if (calibrationSamples == CALIBRATION_COUNT) {
                gyroBiasX /= CALIBRATION_COUNT
                gyroBiasY /= CALIBRATION_COUNT
                gyroBiasZ /= CALIBRATION_COUNT
                println("✅ Gyro calibration complete — bias removed: " +
                        "X=%.4f  Y=%.4f  Z=%.4f".format(gyroBiasX, gyroBiasY, gyroBiasZ))
            }

            return Triple(0f, 0f, 0f)   // dead-still during calibration
        }

        return Triple(
            rawX - gyroBiasX,
            rawY - gyroBiasY,
            rawZ - gyroBiasZ
        )
    }
}
