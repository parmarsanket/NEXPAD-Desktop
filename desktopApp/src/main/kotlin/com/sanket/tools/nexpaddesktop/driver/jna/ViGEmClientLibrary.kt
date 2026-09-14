package com.sanket.tools.nexpaddesktop.driver.jna

import com.sun.jna.Callback
import com.sun.jna.Library
import com.sun.jna.Native
import com.sun.jna.Pointer

/**
 * The JNA (Java Native Access) Interop Layer for `vigemclient.dll`.
 *
 * This interface bridges the gap between the Kotlin JVM and the native Windows kernel.
 * It defines the exact functions, memory structures, and callbacks exported by the
 * ViGEm C++ library, allowing Kotlin to allocate virtual gamepads directly on the OS.
 *
 * **Memory Management Warning:**
 * Functions that return a `Pointer?` allocate raw memory on the C-heap.
 * The JVM Garbage Collector (GC) cannot see or clean up this memory.
 * The caller *must* explicitly call the corresponding `_free` methods, or
 * ghost devices and severe memory leaks will occur.
 */
interface ViGEmClientLibrary : Library {
    companion object {
        /**
         * The singleton instance of the loaded native library.
         * JNA automatically searches the `PATH` and classpath for `vigemclient.dll`.
         */
        val INSTANCE: ViGEmClientLibrary by lazy {
            Native.load("vigemclient", ViGEmClientLibrary::class.java)
        }
    }

    // =========================================================================
    // NATIVE CALLBACKS (Invoked by C++ Background Threads)
    // =========================================================================

    /**
     * Native Callback for Xbox 360 Rumble Events.
     * 
     * **Thread Safety Warning:** This is invoked from a native C++ thread, NOT a Kotlin thread.
     * Any data accessed inside this callback must be strictly synchronized.
     */
    interface PVIGEM_X360_NOTIFICATION : Callback {
        fun callback(
            Client: Pointer?,
            Target: Pointer?,
            LargeMotor: Byte,
            SmallMotor: Byte,
            LedNumber: Byte,
            UserData: Pointer?
        )
    }

    /**
     * Native Callback for DualShock 4 Rumble & Lightbar Events.
     * 
     * **Thread Safety Warning:** Invoked by a native C++ background thread.
     */
    interface PVIGEM_DS4_NOTIFICATION : Callback {
        fun callback(
            Client: Pointer?,
            Target: Pointer?,
            LargeMotor: Byte,
            SmallMotor: Byte,
            LightbarColor: Int,
            UserData: Pointer?
        )
    }

    // =========================================================================
    // NATIVE STRUCTURES (Memory Layouts)
    // =========================================================================

    /**
     * 3-byte Native Structure defining RGB LED colors for the DS4 Lightbar.
     */
    open class VIGEM_DS4_LIGHTBAR_COLOR : com.sun.jna.Structure() {
        @JvmField var Red: Byte = 0
        @JvmField var Green: Byte = 0
        @JvmField var Blue: Byte = 0
        override fun getFieldOrder(): List<String> = listOf("Red", "Green", "Blue")
        
        class ByValue : VIGEM_DS4_LIGHTBAR_COLOR(), com.sun.jna.Structure.ByValue
    }

    /**
     * JNA memory mapping for the ViGEm `DS4_REPORT_EX` native C structure.
     * 
     * This perfectly models the 63-byte DualShock 4 hardware report required by the HID spec.
     * `ALIGN_NONE` is strictly required to prevent the JVM from inserting padding bytes,
     * which would misalign the C++ memory reader and crash the driver.
     * 
     * ### Memory Layout (63 Bytes Total)
     * | Bytes | Field | Description |
     * | :--- | :--- | :--- |
     * | 0-3 | `bThumb...` | Left/Right Analog Stick X/Y (0-255, Center = 128) |
     * | 4-5 | `wButtons` | DS4 Face and D-Pad Buttons |
     * | 6 | `bSpecial` | PS Button, Touchpad Click, Options, Share |
     * | 7-8 | `bTrigger...` | L2 / R2 Analog Triggers (0-255) |
     * | 9-10 | `wTimestamp` | Microsecond counter for motion processing |
     * | 11 | `bBatteryLvl` | Battery State |
     * | 12-17 | `wGyro...` | 3-Axis Gyroscope (Pitch, Yaw, Roll) |
     * | 18-23 | `wAccel...` | 3-Axis Accelerometer (X, Y, Z) |
     * | 24-62 | `padding` | 39 Bytes of padding forced to reach exactly 63 bytes |
     */
    @com.sun.jna.Structure.FieldOrder(
        "bThumbLX", "bThumbLY", "bThumbRX", "bThumbRY",
        "wButtons", "bSpecial", "bTriggerL", "bTriggerR",
        "wTimestamp", "bBatteryLvl", "wGyroX", "wGyroY", "wGyroZ",
        "wAccelX", "wAccelY", "wAccelZ", "padding"
    )
    open class DS4_REPORT_EX : com.sun.jna.Structure(ALIGN_NONE) {
        @JvmField var bThumbLX: Byte = 128.toByte()
        @JvmField var bThumbLY: Byte = 128.toByte()
        @JvmField var bThumbRX: Byte = 128.toByte()
        @JvmField var bThumbRY: Byte = 128.toByte()
        @JvmField var wButtons: Short = 8 // Default state: Released D-Pad
        @JvmField var bSpecial: Byte = 0
        @JvmField var bTriggerL: Byte = 0
        @JvmField var bTriggerR: Byte = 0
        @JvmField var wTimestamp: Short = 0
        @JvmField var bBatteryLvl: Byte = 0
        @JvmField var wGyroX: Short = 0
        @JvmField var wGyroY: Short = 0
        @JvmField var wGyroZ: Short = 0
        @JvmField var wAccelX: Short = 0
        @JvmField var wAccelY: Short = 0
        @JvmField var wAccelZ: Short = 0
        
        /** Ensures the structure allocates exactly 63 bytes in the JVM heap. */
        @JvmField var padding: ByteArray = ByteArray(39) 

        class ByValue : DS4_REPORT_EX(), com.sun.jna.Structure.ByValue
    }

    // =========================================================================
    // NATIVE FUNCTIONS: Client Lifecycle
    // =========================================================================

    /** Allocates a ViGEm Client pointer on the C-Heap. Caller MUST call [vigem_free]. */
    fun vigem_alloc(): Pointer?
    
    /** Connects the allocated client to the underlying Windows bus driver. */
    fun vigem_connect(client: Pointer?): Int
    
    /** Disconnects the client from the Windows bus driver. */
    fun vigem_disconnect(client: Pointer?)
    
    /** Frees the C-Heap memory allocated by [vigem_alloc]. */
    fun vigem_free(client: Pointer?)
    
    // =========================================================================
    // NATIVE FUNCTIONS: Target (Device) Lifecycle
    // =========================================================================

    /** Allocates an Xbox 360 virtual device pointer. Caller MUST call [vigem_target_free]. */
    fun vigem_target_x360_alloc(): Pointer?
    
    /** Allocates a DualShock 4 virtual device pointer. Caller MUST call [vigem_target_free]. */
    fun vigem_target_ds4_alloc(): Pointer?
    
    /** Plugs the virtual device into Windows via the Client bus. */
    fun vigem_target_add(client: Pointer?, target: Pointer?): Int
    
    /** Unplugs the virtual device from Windows. */
    fun vigem_target_remove(client: Pointer?, target: Pointer?): Int
    
    /** Frees the C-Heap memory allocated by target allocation functions. */
    fun vigem_target_free(target: Pointer?)
    
    // =========================================================================
    // NATIVE FUNCTIONS: Device Configuration
    // =========================================================================

    fun vigem_target_set_vid(target: Pointer?, vid: Short)
    fun vigem_target_set_pid(target: Pointer?, pid: Short)
    
    // =========================================================================
    // NATIVE FUNCTIONS: Input Updates
    // =========================================================================

    /** Submits an Xbox 360 hardware report to the Windows kernel. */
    fun vigem_target_x360_update(client: Pointer?, target: Pointer?, report: XUSBReport): Int
    
    /** Submits a packed 63-byte DualShock 4 hardware report to the Windows kernel. */
    fun vigem_target_ds4_update_ex(client: Pointer?, target: Pointer?, report: DS4_REPORT_EX.ByValue): Int
    
    // =========================================================================
    // NATIVE FUNCTIONS: Notification Registration
    // =========================================================================

    fun vigem_target_x360_register_notification(
        client: Pointer?,
        target: Pointer?,
        notification: PVIGEM_X360_NOTIFICATION,
        userData: Pointer?
    ): Int
    
    fun vigem_target_x360_unregister_notification(target: Pointer?)
    
    fun vigem_target_ds4_register_notification(
        client: Pointer?,
        target: Pointer?,
        notification: PVIGEM_DS4_NOTIFICATION,
        userData: Pointer?
    ): Int
    
    fun vigem_target_ds4_unregister_notification(target: Pointer?)
}
