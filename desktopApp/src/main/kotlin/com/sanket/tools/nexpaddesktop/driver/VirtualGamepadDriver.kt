package com.sanket.tools.nexpaddesktop.driver

import com.sanket.tools.nexpaddesktop.model.GamepadInput

class VirtualGamepadDriver {
    private var isConnected = false

    fun connect() {
        // In a full implementation, this calls:
        // ViGEmClient.vigem_alloc()
        // ViGEmClient.vigem_connect()
        // ViGEmClient.vigem_target_x360_alloc()
        // ViGEmClient.vigem_target_add()
        println("Connecting to ViGEmBus Driver...")
        isConnected = true
        println("Virtual Xbox 360 Controller Connected successfully!")
    }

    fun disconnect() {
        if (!isConnected) return
        // In a full implementation, this calls:
        // ViGEmClient.vigem_target_remove()
        // ViGEmClient.vigem_disconnect()
        println("Disconnecting Virtual Controller...")
        isConnected = false
    }

    fun updateInput(input: GamepadInput) {
        if (!isConnected) return

        // In a full implementation, this maps the Kotlin data class 
        // to the C++ XUSB_REPORT struct and sends it via JNA.
        // For example:
        // val report = XUSB_REPORT()
        // if (input.btnA) report.wButtons = report.wButtons or XUSB_GAMEPAD_A
        // ViGEmClient.vigem_target_x360_update(client, target, report)

        // For now, we will log a simplified readout to the console for testing
        val activeButtons = mutableListOf<String>()
        if (input.btnA) activeButtons.add("A")
        if (input.btnB) activeButtons.add("B")
        if (input.btnX) activeButtons.add("X")
        if (input.btnY) activeButtons.add("Y")
        if (input.dpadUp) activeButtons.add("UP")
        if (input.dpadDown) activeButtons.add("DOWN")
        
        if (activeButtons.isNotEmpty() || input.leftStickX != 0f || input.triggerL2 != 0f) {
            println("🎮 Driver Injected -> Buttons: $activeButtons | LeftStick: ${input.leftStickX} | L2: ${input.triggerL2}")
        }
    }
}
