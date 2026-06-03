package com.sanket.tools.nexpaddesktop.driver.jna

import com.sun.jna.Callback
import com.sun.jna.Library
import com.sun.jna.Native
import com.sun.jna.Pointer
import com.sun.jna.ptr.PointerByReference

interface ViGEmClientLibrary : Library {
    companion object {
        val INSTANCE: ViGEmClientLibrary by lazy {
            Native.load("vigemclient", ViGEmClientLibrary::class.java)
        }
    }

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

    open class VIGEM_DS4_LIGHTBAR_COLOR : com.sun.jna.Structure() {
        @JvmField var Red: Byte = 0
        @JvmField var Green: Byte = 0
        @JvmField var Blue: Byte = 0
        override fun getFieldOrder(): List<String> = listOf("Red", "Green", "Blue")
        
        class ByValue : VIGEM_DS4_LIGHTBAR_COLOR(), com.sun.jna.Structure.ByValue
    }

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

    @com.sun.jna.Structure.FieldOrder(
        "bThumbLX", "bThumbLY", "bThumbRX", "bThumbRY",
        "wButtons", "bSpecial", "bTriggerL", "bTriggerR",
        "wTimestamp", "bBatteryLvl", "wGyroX", "wGyroY", "wGyroZ",
        "wAccelX", "wAccelY", "wAccelZ", "padding"
    )
    class DS4_REPORT_EX : com.sun.jna.Structure(ALIGN_NONE) {
        @JvmField var bThumbLX: Byte = 128.toByte()
        @JvmField var bThumbLY: Byte = 128.toByte()
        @JvmField var bThumbRX: Byte = 128.toByte()
        @JvmField var bThumbRY: Byte = 128.toByte()
        @JvmField var wButtons: Short = 8 // Released D-Pad
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
        @JvmField var padding: ByteArray = ByteArray(39) // 63 - 24 = 39 bytes of padding
    }

    fun vigem_alloc(): Pointer?
    fun vigem_free(client: Pointer?)
    fun vigem_connect(client: Pointer?): Int
    fun vigem_disconnect(client: Pointer?)
    
    fun vigem_target_x360_alloc(): Pointer?
    fun vigem_target_ds4_alloc(): Pointer?
    fun vigem_target_add(client: Pointer?, target: Pointer?): Int
    fun vigem_target_remove(client: Pointer?, target: Pointer?): Int
    fun vigem_target_free(target: Pointer?)
    
    fun vigem_target_set_vid(target: Pointer?, vid: Short)
    fun vigem_target_set_pid(target: Pointer?, pid: Short)
    
    fun vigem_target_x360_update(client: Pointer?, target: Pointer?, report: XUSBReport): Int
    fun vigem_target_ds4_update_ex(client: Pointer?, target: Pointer?, report: DS4_REPORT_EX): Int
    
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
