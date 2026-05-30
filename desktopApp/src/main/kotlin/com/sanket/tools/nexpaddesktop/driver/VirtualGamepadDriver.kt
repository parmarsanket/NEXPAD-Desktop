package com.sanket.tools.nexpaddesktop.driver

import com.sanket.tools.nexpaddesktop.model.GamepadInput

import com.sanket.tools.nexpaddesktop.model.GamepadFeedback

import com.sun.jna.Pointer
import com.sanket.tools.nexpaddesktop.driver.jna.ViGEmClientLibrary
import com.sanket.tools.nexpaddesktop.driver.jna.XUSBReport

class VirtualGamepadDriver(private val onRumble: (GamepadFeedback) -> Unit = {}) {
    private var isConnected = false
    private var client: Pointer? = null
    private var target: Pointer? = null
    private var notificationCallback: ViGEmClientLibrary.PVIGEM_X360_NOTIFICATION? = null

    fun connect() {
        try {
            val lib = ViGEmClientLibrary.INSTANCE
            client = lib.vigem_alloc()
            if (client == null) throw Exception("Failed to allocate ViGEm Client")
            
            val connectResult = lib.vigem_connect(client)
            // ViGEmClient returns 0x20000000 (536870912) for SUCCESS (VIGEM_ERROR_NONE)
            if (connectResult != 0x20000000) throw Exception("Failed to connect to ViGEm Bus. Ensure it is installed. Error: $connectResult")
            
            target = lib.vigem_target_x360_alloc()
            lib.vigem_target_add(client, target)
            
            notificationCallback = object : ViGEmClientLibrary.PVIGEM_X360_NOTIFICATION {
                override fun callback(client: Pointer?, target: Pointer?, largeMotor: Byte, smallMotor: Byte, ledNumber: Byte, userData: Pointer?) {
                    val leftSpeed = largeMotor.toInt() and 0xFF
                    val rightSpeed = smallMotor.toInt() and 0xFF
                    onRumble(GamepadFeedback(leftSpeed, rightSpeed))
                }
            }
            
            lib.vigem_target_x360_register_notification(client, target, notificationCallback!!, null)
            
            isConnected = true
            println("✅ Virtual Xbox 360 Controller Connected successfully!")
        } catch (e: Exception) {
            System.err.println("⚠️ ViGEm init failed: ${e.message}")
            println("⚠️ Falling back to MOCK driver mode (buttons will print to console but not work in games).")
            isConnected = false
        }
    }

    fun disconnect() {
        if (client != null && target != null) {
            try {
                val lib = ViGEmClientLibrary.INSTANCE
                lib.vigem_target_x360_unregister_notification(target)
                lib.vigem_target_remove(client, target)
                lib.vigem_target_free(target)
                lib.vigem_disconnect(client)
                lib.vigem_free(client)
            } catch (e: Exception) { }
        }
        isConnected = false
        client = null
        target = null
        println("Disconnected Virtual Controller.")
    }

    fun updateInput(input: GamepadInput) {
        if (!isConnected || client == null || target == null) {
            // Fallback mock logic for testing without driver
            val active = mutableListOf<String>()
            if (input.btnA) active.add("A")
            if (input.btnB) active.add("B")
            if (input.dpadUp) active.add("UP")
            if (active.isNotEmpty()) println("MOCK -> $active")
            return
        }

        val report = XUSBReport()
        var buttons: Short = 0
        
        if (input.dpadUp) buttons = (buttons.toInt() or XUSBReport.DPAD_UP.toInt()).toShort()
        if (input.dpadDown) buttons = (buttons.toInt() or XUSBReport.DPAD_DOWN.toInt()).toShort()
        if (input.dpadLeft) buttons = (buttons.toInt() or XUSBReport.DPAD_LEFT.toInt()).toShort()
        if (input.dpadRight) buttons = (buttons.toInt() or XUSBReport.DPAD_RIGHT.toInt()).toShort()
        if (input.btnStart) buttons = (buttons.toInt() or XUSBReport.START.toInt()).toShort()
        if (input.btnSelect) buttons = (buttons.toInt() or XUSBReport.BACK.toInt()).toShort()
        if (input.btnL3) buttons = (buttons.toInt() or XUSBReport.LEFT_THUMB.toInt()).toShort()
        if (input.btnR3) buttons = (buttons.toInt() or XUSBReport.RIGHT_THUMB.toInt()).toShort()
        if (input.btnL1) buttons = (buttons.toInt() or XUSBReport.LEFT_SHOULDER.toInt()).toShort()
        if (input.btnR1) buttons = (buttons.toInt() or XUSBReport.RIGHT_SHOULDER.toInt()).toShort()
        if (input.btnGuide) buttons = (buttons.toInt() or XUSBReport.GUIDE.toInt()).toShort()
        if (input.btnA) buttons = (buttons.toInt() or XUSBReport.A.toInt()).toShort()
        if (input.btnB) buttons = (buttons.toInt() or XUSBReport.B.toInt()).toShort()
        if (input.btnX) buttons = (buttons.toInt() or XUSBReport.X.toInt()).toShort()
        if (input.btnY) buttons = (buttons.toInt() or XUSBReport.Y.toInt()).toShort()

        report.wButtons = buttons
        report.bLeftTrigger = (input.triggerL2 * 255).toInt().toByte()
        report.bRightTrigger = (input.triggerR2 * 255).toInt().toByte()
        
        report.sThumbLX = (input.leftStickX * 32767).toInt().toShort()
        report.sThumbLY = (input.leftStickY * -32767).toInt().toShort() // Y is usually inverted
        report.sThumbRX = (input.rightStickX * 32767).toInt().toShort()
        report.sThumbRY = (input.rightStickY * -32767).toInt().toShort()

        try {
            ViGEmClientLibrary.INSTANCE.vigem_target_x360_update(client, target, report)
        } catch (e: Exception) { }
    }

    // Simulate an off-road racing crash rumble from the Windows kernel
    fun simulateCrash() {
        println("💥 Simulating CRASH! Sending heavy rumble feedback...")
        onRumble(GamepadFeedback(leftMotorSpeed = 255, rightMotorSpeed = 200))
    }
}
