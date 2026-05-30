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

    fun vigem_alloc(): Pointer?
    fun vigem_free(client: Pointer?)
    fun vigem_connect(client: Pointer?): Int
    fun vigem_disconnect(client: Pointer?)
    
    fun vigem_target_x360_alloc(): Pointer?
    fun vigem_target_add(client: Pointer?, target: Pointer?): Int
    fun vigem_target_remove(client: Pointer?, target: Pointer?): Int
    fun vigem_target_free(target: Pointer?)
    
    fun vigem_target_x360_update(client: Pointer?, target: Pointer?, report: XUSBReport): Int
    
    fun vigem_target_x360_register_notification(
        client: Pointer?,
        target: Pointer?,
        notification: PVIGEM_X360_NOTIFICATION,
        userData: Pointer?
    ): Int
    
    fun vigem_target_x360_unregister_notification(target: Pointer?)
}
