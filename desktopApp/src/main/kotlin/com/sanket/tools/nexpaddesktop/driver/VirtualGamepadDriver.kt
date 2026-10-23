package com.sanket.tools.nexpaddesktop.driver

import com.sanket.tools.nexpad.model.GamepadInput
import com.sanket.tools.nexpad.model.GamepadFeedback
import com.sun.jna.Pointer
import com.sanket.tools.nexpaddesktop.driver.jna.ViGEmClientLibrary
import com.sanket.tools.nexpaddesktop.driver.jna.XUSBReport

/**
 * The Device Manager and Implementation Layer for a Virtual Xbox 360 Controller.
 *
 * This class handles the complete lifecycle (Connect, Update, Disconnect) of a native 
 * Windows controller via the ViGEm bus. It also handles receiving Rumble (Force Feedback) 
 * requests originating from PC games.
 *
 * @property onRumble Callback invoked when the native driver requests haptic feedback.
 */
class VirtualGamepadDriver(private val onRumble: (GamepadFeedback) -> Unit = {}) : IGamepadDriver {
    
    private var isConnected = false
    
    /** Pointer to the master ViGEm bus connection on the C-Heap. Must be freed! */
    private var client: Pointer? = null
    
    /** Pointer to the specific Xbox 360 virtual device on the C-Heap. Must be freed! */
    private var target: Pointer? = null
    
    /** 
     * We MUST keep a hard reference to the JNA Callback object. 
     * If we don't, the JVM Garbage Collector will destroy it, causing the native 
     * C++ library to crash when it tries to invoke the callback pointer. 
     */
    private var notificationCallback: ViGEmClientLibrary.PVIGEM_X360_NOTIFICATION? = null
    private val cachedReport = XUSBReport()

    override fun connect(): Unit = synchronized(this) {
        // Prevent memory leaks during retry loops: completely clear old state before attempting
        disconnect()

        try {
            val lib = ViGEmClientLibrary.INSTANCE
            
            // 1. Allocate Master Client
            client = lib.vigem_alloc() ?: throw Exception("Failed to allocate ViGEm Client")
            
            // 2. Connect to Bus
            // ViGEmClient returns 0x20000000 (536870912) for SUCCESS (VIGEM_ERROR_NONE)
            val connectResult = lib.vigem_connect(client)
            if (connectResult != 0x20000000) {
                throw Exception("Failed to connect to ViGEm Bus. Ensure it is installed. Error: 0x${connectResult.toString(16)}")
            }
            
            // 3. Allocate Virtual Device Target
            target = lib.vigem_target_x360_alloc() ?: throw Exception("Failed to allocate ViGEm X360 Target")
            
            // 4. Plug Device Into OS (Triggers Windows USB insertion sound)
            val addResult = lib.vigem_target_add(client, target)
            if (addResult != 0x20000000) {
                throw Exception("Failed to plug in X360 Target. Error: 0x${addResult.toString(16)}")
            }
            
            // 5. Register Rumble Callbacks
            notificationCallback = object : ViGEmClientLibrary.PVIGEM_X360_NOTIFICATION {
                /**
                 * **THREAD SAFETY WARNING:**
                 * This method is invoked by a native C++ background thread inside `vigemclient.dll`.
                 * Any state modification inside here MUST be thread-safe.
                 */
                override fun callback(
                    client: Pointer?, 
                    target: Pointer?, 
                    largeMotor: Byte, 
                    smallMotor: Byte, 
                    ledNumber: Byte, 
                    userData: Pointer?
                ) {
                    try {
                        val leftSpeed = largeMotor.toInt() and 0xFF
                        val rightSpeed = smallMotor.toInt() and 0xFF
                        onRumble(GamepadFeedback(leftSpeed, rightSpeed))
                    } catch (t: Throwable) {
                        System.err.println("⚠️ Error in X360 notification callback: ${t.message}")
                    }
                }
            }
            
            lib.vigem_target_x360_register_notification(client, target, notificationCallback!!, null)
            
            isConnected = true
            println("✅ [DRIVER] Virtual Xbox 360 Controller mounted successfully into ViGEm Bus")
        } catch (e: Throwable) {
            System.err.println("⚠️ ViGEm init failed: ${e.message}")
            println("⚠️ Falling back to MOCK driver mode (virtual controller inputs will not work in games).")
            isConnected = false
            target?.let { t ->
                try { ViGEmClientLibrary.INSTANCE.vigem_target_free(t) } catch (_: Throwable) {}
                target = null
            }
            client?.let { c ->
                try {
                    ViGEmClientLibrary.INSTANCE.vigem_disconnect(c)
                    ViGEmClientLibrary.INSTANCE.vigem_free(c)
                } catch (_: Throwable) {}
                client = null
            }
        }
    }

    override fun disconnect(): Unit = synchronized(this) {
        val lib = ViGEmClientLibrary.INSTANCE
        target?.let { t ->
            try {
                // Unregister callbacks before destroying device to prevent C++ null-pointer crashes
                lib.vigem_target_x360_unregister_notification(t)
                client?.let { c ->
                    // Unplug device from Windows
                    lib.vigem_target_remove(c, t)
                }
                // Free Native Memory Pointers
                lib.vigem_target_free(t)
            } catch (e: Throwable) {
                System.err.println("⚠️ Xbox target disconnect error: ${e.message}")
            } finally {
                target = null
            }
        }

        client?.let { c ->
            try {
                lib.vigem_disconnect(c)
                lib.vigem_free(c)
            } catch (e: Throwable) {
                System.err.println("⚠️ Xbox client disconnect error: ${e.message}")
            } finally {
                client = null
            }
        }
        
        isConnected = false
        println("Disconnected Virtual Controller.")
    }

    override fun updateInput(input: GamepadInput) {
        synchronized(this) {
            if (!isConnected || client == null || target == null) return

            // 1. Pack data into native 12-byte struct
            val report = ViGEmInputMapper.map(input, cachedReport)

            try {
                // 2. Submit C-Struct to Windows Kernel
                ViGEmClientLibrary.INSTANCE.vigem_target_x360_update(client, target, report)
            } catch (e: Throwable) { 
                System.err.println("⚠️ Xbox input update error: ${e.message}") 
            }
        }
    }

    override fun recalibrate(): Unit = synchronized(this) {
        // Xbox 360 virtual device does not have gyro bias state
    }

    override fun simulateCrash() {
        println("💥 Simulating CRASH! Sending heavy rumble feedback...")
        onRumble(GamepadFeedback(leftMotorSpeed = 255, rightMotorSpeed = 200))
    }

    override fun isDriverConnected(): Boolean = synchronized(this) {
        return isConnected
    }
}

