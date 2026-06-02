package com.sanket.tools.nexpaddesktop.driver

import com.sanket.tools.nexpaddesktop.model.GamepadInput

import com.sanket.tools.nexpaddesktop.model.GamepadFeedback

import com.sun.jna.Pointer
import com.sanket.tools.nexpaddesktop.driver.jna.ViGEmClientLibrary
import com.sanket.tools.nexpaddesktop.driver.jna.XUSBReport

class VirtualGamepadDriver(private val onRumble: (GamepadFeedback) -> Unit = {}) : IGamepadDriver {
    private var isConnected = false
    private var client: Pointer? = null
    private var target: Pointer? = null
    private var notificationCallback: ViGEmClientLibrary.PVIGEM_X360_NOTIFICATION? = null
    private var lastL3 = false
    private var lastR3 = false

    override fun connect() {
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

    override fun disconnect() {
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

    override fun updateInput(input: GamepadInput) {
        if (!isConnected || client == null || target == null) {
            // Fallback mock logic for testing without driver
            val active = mutableListOf<String>()
            if (input.btnA) active.add("A")
            if (input.btnB) active.add("B")
            if (input.dpadUp) active.add("UP")
            if (active.isNotEmpty()) println("MOCK -> $active")
            return
        }

        val report = ViGEmInputMapper.map(input)

        try {
            ViGEmClientLibrary.INSTANCE.vigem_target_x360_update(client, target, report)
        } catch (e: Exception) { }
    }

    // Simulate an off-road racing crash rumble from the Windows kernel
    override fun simulateCrash() {
        println("💥 Simulating CRASH! Sending heavy rumble feedback...")
        onRumble(GamepadFeedback(leftMotorSpeed = 255, rightMotorSpeed = 200))
    }
}
