package com.sanket.tools.nexpaddesktop.driver

import com.sanket.tools.nexpaddesktop.model.GamepadInput
import com.sanket.tools.nexpaddesktop.model.GamepadFeedback
import com.sun.jna.Pointer
import com.sanket.tools.nexpaddesktop.driver.jna.ViGEmClientLibrary
import java.nio.ByteBuffer
import java.nio.ByteOrder

class VirtualDualShock4Driver(private val onRumble: (GamepadFeedback) -> Unit = {}) : IGamepadDriver {
    private var isConnected = false
    private var client: Pointer? = null
    private var target: Pointer? = null

    private var gyroBiasX = 0f
    private var gyroBiasY = 0f
    private var gyroBiasZ = 0f
    private var calibrationSamples = 0
    private var timestampCounter: Short = 0
    private val MAX_CALIBRATION_SAMPLES = 100 // Collect ~1-2 seconds of data to find the resting bias

    override fun connect() {
        try {
            val lib = ViGEmClientLibrary.INSTANCE
            client = lib.vigem_alloc()
            if (client == null) throw Exception("Failed to allocate ViGEm Client")
            
            val connectResult = lib.vigem_connect(client)
            if (connectResult != 0x20000000) throw Exception("Failed to connect to ViGEm Bus. Error: $connectResult")
            
            target = lib.vigem_target_ds4_alloc()
            lib.vigem_target_add(client, target)
            
            // Reset calibration on connect
            gyroBiasX = 0f
            gyroBiasY = 0f
            gyroBiasZ = 0f
            calibrationSamples = 0

            isConnected = true
            println("✅ Virtual Sony DualShock 4 Controller Connected successfully!")
            println("⏳ Auto-calibrating Gyroscope... Please keep the phone stationary for 2 seconds.")
        } catch (e: Exception) {
            System.err.println("⚠️ ViGEm DS4 init failed: ${e.message}")
            isConnected = false
        }
    }

    override fun disconnect() {
        if (client != null && target != null) {
            try {
                val lib = ViGEmClientLibrary.INSTANCE
                lib.vigem_target_remove(client, target)
                lib.vigem_target_free(target)
                lib.vigem_disconnect(client)
                lib.vigem_free(client)
            } catch (e: Exception) { }
        }
        isConnected = false
        client = null
        target = null
        println("Disconnected Virtual DS4 Controller.")
    }

    override fun updateInput(input: GamepadInput) {
        if (!isConnected || client == null || target == null) return

        val reportEx = ViGEmClientLibrary.DS4_REPORT_EX()
        val buffer = ByteBuffer.wrap(reportEx.Report).order(ByteOrder.LITTLE_ENDIAN)
        
        // 0: Left Stick X
        buffer.put(0, mapStick(input.leftStickX))
        // 1: Left Stick Y (Inverted for DS4)
        buffer.put(1, mapStick(-input.leftStickY))
        // 2: Right Stick X
        buffer.put(2, mapStick(input.rightStickX))
        // 3: Right Stick Y (Inverted for DS4)
        buffer.put(3, mapStick(-input.rightStickY))
        
        // 4: D-Pad & Face Buttons
        var dpad = 8 // Default released
        if (input.dpadUp && input.dpadRight) dpad = 1
        else if (input.dpadDown && input.dpadRight) dpad = 3
        else if (input.dpadDown && input.dpadLeft) dpad = 5
        else if (input.dpadUp && input.dpadLeft) dpad = 7
        else if (input.dpadUp) dpad = 0
        else if (input.dpadRight) dpad = 2
        else if (input.dpadDown) dpad = 4
        else if (input.dpadLeft) dpad = 6
        
        var faceBtns = 0
        if (input.btnX) faceBtns = faceBtns or 0x10 // Square
        if (input.btnA) faceBtns = faceBtns or 0x20 // Cross
        if (input.btnB) faceBtns = faceBtns or 0x40 // Circle
        if (input.btnY) faceBtns = faceBtns or 0x80 // Triangle
        buffer.put(4, (dpad or faceBtns).toByte())
        
        // 5: Special Buttons
        var specialBtns = 0
        if (input.btnL1) specialBtns = specialBtns or 0x01
        if (input.btnR1) specialBtns = specialBtns or 0x02
        if (input.triggerL2 > 0.1f) specialBtns = specialBtns or 0x04
        if (input.triggerR2 > 0.1f) specialBtns = specialBtns or 0x08
        if (input.btnSelect) specialBtns = specialBtns or 0x10 // Share
        if (input.btnStart) specialBtns = specialBtns or 0x20 // Options
        if (input.btnL3) specialBtns = specialBtns or 0x40
        if (input.btnR3) specialBtns = specialBtns or 0x80
        buffer.put(5, specialBtns.toByte())
        
        // 6: PS Button
        var psBtn = 0
        if (input.btnGuide) psBtn = psBtn or 0x01
        buffer.put(6, psBtn.toByte())
        
        // 7 & 8: Triggers
        buffer.put(7, (input.triggerL2 * 255).toInt().toByte())
        buffer.put(8, (input.triggerR2 * 255).toInt().toByte())
        
        // 9: Timestamp (Required for Steam Input Gyro)
        buffer.putShort(9, timestampCounter)
        timestampCounter++
        
        // --- MOTION DATA ---
        if (calibrationSamples < MAX_CALIBRATION_SAMPLES) {
            // Collect resting bias
            gyroBiasX += input.gyroX
            gyroBiasY += input.gyroY
            gyroBiasZ += input.gyroZ
            calibrationSamples++
            
            if (calibrationSamples == MAX_CALIBRATION_SAMPLES) {
                gyroBiasX /= MAX_CALIBRATION_SAMPLES
                gyroBiasY /= MAX_CALIBRATION_SAMPLES
                gyroBiasZ /= MAX_CALIBRATION_SAMPLES
                println("✅ Gyro Calibration Complete! Bias removed: X=$gyroBiasX, Y=$gyroBiasY, Z=$gyroBiasZ")
            }
            
            // Send perfect 0 while calibrating to lock it dead center
            buffer.putShort(12, 0)
            buffer.putShort(14, 0)
            buffer.putShort(16, 0)
        } else {
            // Apply bias correction to actual output
            val calX = input.gyroX - gyroBiasX
            val calY = input.gyroY - gyroBiasY
            val calZ = input.gyroZ - gyroBiasZ

            val gyroScalar = 939.0f
            // Gyro: Pitch, Yaw, Roll
            buffer.putShort(12, (calX * gyroScalar).toInt().toShort()) // Pitch
            buffer.putShort(14, (-calZ * gyroScalar).toInt().toShort()) // Yaw (Swapped Z and Y for DS4)
            buffer.putShort(16, (-calY * gyroScalar).toInt().toShort()) // Roll
        }
        
        // Accelerometer scaling (m/s^2 to G-force 16-bit)
        val accelScalar = 835.3f
        buffer.putShort(18, (input.accelX * accelScalar).toInt().toShort())
        buffer.putShort(20, (input.accelY * accelScalar).toInt().toShort())
        buffer.putShort(22, (input.accelZ * accelScalar).toInt().toShort())

        try {
            ViGEmClientLibrary.INSTANCE.vigem_target_ds4_update_ex(client, target, reportEx)
        } catch (e: Exception) { }
    }

    private fun mapStick(value: Float): Byte {
        // Value goes -1.0 to 1.0. Map to 0..255
        val mapped = ((value + 1.0f) / 2.0f * 255.0f).toInt()
        return mapped.coerceIn(0, 255).toByte()
    }

    override fun simulateCrash() {
        // DS4 Rumble simulation
        onRumble(GamepadFeedback(leftMotorSpeed = 255, rightMotorSpeed = 200))
    }
}
