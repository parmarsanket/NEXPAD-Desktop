package com.sanket.tools.nexpaddesktop.driver

import com.sanket.tools.nexpad.model.GamepadInput
import com.sanket.tools.nexpad.model.GamepadFeedback
import com.sun.jna.Pointer
import com.sanket.tools.nexpaddesktop.driver.jna.ViGEmClientLibrary

/**
 * The Device Manager and Protocol Layer for a Virtual Sony DualShock 4 Controller.
 *
 * This class handles the initialization and lifecycle of the C-heap pointers for the 
 * ViGEm DualShock 4 target. The actual byte-mapping and IMU calibration logic 
 * is delegated to the [DualShock4InputMapper] to maintain architectural consistency 
 * with the Xbox 360 driver.
 */
class VirtualDualShock4Driver(
    private val onRumble: (GamepadFeedback) -> Unit = {}
) : IGamepadDriver {

    // =========================================================================
    // NATIVE STATE
    // =========================================================================
    private var isConnected = false
    private var client: Pointer? = null
    private var target: Pointer? = null
    
    /** Must hold a hard reference to prevent the GC from destroying the native C++ callback. */
    private var notificationCallback: ViGEmClientLibrary.PVIGEM_DS4_NOTIFICATION? = null

    /** Stateful mapper that translates inputs and auto-calibrates the Gyroscope. */
    private val mapper = DualShock4InputMapper()

    companion object {
        // Official Sony DualShock 4 USB identifiers
        const val DS4_VID: Short = 0x054C
        const val DS4_PID: Short = 0x05C4
    }

    override fun connect() {
        // Prevent memory leaks during retry loops
        disconnect()

        try {
            val lib = ViGEmClientLibrary.INSTANCE

            // 1. Allocate Master Client
            client = lib.vigem_alloc() ?: throw Exception("Failed to allocate ViGEm Client")
            
            // 2. Connect to Bus
            val result = lib.vigem_connect(client)
            if (result != 0x20000000) {
                throw Exception("ViGEm Bus connect failed: 0x${result.toString(16)}")
            }

            // 3. Allocate Virtual DS4 Target
            target = lib.vigem_target_ds4_alloc()

            // 4. Inject Official Sony USB Identifiers
            lib.vigem_target_set_vid(target, DS4_VID)
            lib.vigem_target_set_pid(target, DS4_PID)

            // 5. Plug Device Into OS
            lib.vigem_target_add(client, target)

            // 6. Register Native Callbacks for Rumble and Lightbar
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
            lib.vigem_target_ds4_register_notification(client, target, notificationCallback!!, null)

            // Reset the mapper state for fresh gyro calibration and timestamps
            mapper.reset()

            isConnected = true
            println("✅ Virtual Sony DualShock 4 Controller Connected successfully!")
            println("⏳ Auto-calibrating Gyroscope... Keep the phone still for ~2 seconds.")

        } catch (e: Exception) {
            System.err.println("⚠️ ViGEm DS4 init failed: ${e.message}")
            isConnected = false
        }
    }

    override fun disconnect() {
        try {
            val lib = ViGEmClientLibrary.INSTANCE
            if (target != null) lib.vigem_target_ds4_unregister_notification(target)
            if (client != null && target != null) {
                lib.vigem_target_remove(client, target)
            }
            // Free Native Memory Pointers
            if (target != null) lib.vigem_target_free(target)
            if (client != null) {
                lib.vigem_disconnect(client)
                lib.vigem_free(client)
            }
        } catch (e: Exception) { }

        isConnected = false
        client = null
        target = null
        println("Disconnected Virtual DS4 Controller.")
    }

    override fun updateInput(input: GamepadInput) {
        if (!isConnected || client == null || target == null) return

        // Translate the input via the stateful mapper
        val report = mapper.map(input)

        // Submit to Windows Kernel
        try {
            ViGEmClientLibrary.INSTANCE.vigem_target_ds4_update_ex(client, target, report)
        } catch (e: Exception) { 
            System.err.println("⚠️ DS4 update error: ${e.message}") 
        }
    }

    override fun simulateCrash() {
        try {
            println("💥 Simulating DS4 CRASH! Sending heavy rumble feedback...")
            onRumble(GamepadFeedback(leftMotorSpeed = 255, rightMotorSpeed = 200))
        } catch (e: Throwable) {
            println("⚠️ simulateCrash error: ${e.message}")
        }
    }

    override fun isDriverConnected(): Boolean {
        return isConnected
    }
}
