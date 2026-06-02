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

    override fun connect() {
        try {
            val lib = ViGEmClientLibrary.INSTANCE
            client = lib.vigem_alloc()
            if (client == null) throw Exception("Failed to allocate ViGEm Client")
            
            val connectResult = lib.vigem_connect(client)
            if (connectResult != 0x20000000) throw Exception("Failed to connect to ViGEm Bus. Error: $connectResult")
            
            target = lib.vigem_target_ds4_alloc()
            lib.vigem_target_add(client, target)
            
            isConnected = true
            println("✅ Virtual Sony DualShock 4 Controller Connected successfully!")
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
        
        // --- MOTION DATA ---
        // DS4_REPORT_EX Motion Byte Offsets:
        // 12: Gyro X (Pitch)
        // 14: Gyro Y (Yaw)
        // 16: Gyro Z (Roll)
        // 18: Accel X
        // 20: Accel Y
        // 22: Accel Z
        
        // Gyro Scaling:
        // DS4 firmware uses 2000 deg/s max range mapped to 16-bit signed (-32768 to 32767).
        // 1 deg/s = 16.384 raw units.
        // Android is rad/s. 1 rad/s = 57.2958 deg/s.
        // Scalar = 57.2958 * 16.384 ≈ 938.7
        val gyroScalar = 939.0f
        
        // Android X (Pitch) -> DS4 X
        buffer.putShort(12, (input.gyroX * gyroScalar).toInt().toShort())
        // Android Z (Yaw) -> DS4 Y (Assuming Y is Yaw in DS4)
        buffer.putShort(14, (-input.gyroZ * gyroScalar).toInt().toShort()) 
        // Android Y (Roll) -> DS4 Z (Assuming Z is Roll in DS4)
        buffer.putShort(16, (-input.gyroY * gyroScalar).toInt().toShort()) 
        
        // Accelerometer Scaling:
        // DS4 firmware uses 4G max range mapped to 16-bit signed.
        // 1G = 8192 raw units.
        // Android is m/s^2. 1 m/s^2 = 1G / 9.80665 ≈ 835.3 raw units.
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
