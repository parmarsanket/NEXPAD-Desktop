package com.sanket.tools.nexpaddesktop.connection.usb.aoa

import com.sanket.tools.nexpaddesktop.connection.usb.ElevationRequiredException
import com.sanket.tools.nexpaddesktop.connection.usb.UsbScanner
import org.usb4java.Context
import org.usb4java.LibUsb
import kotlinx.coroutines.runBlocking

class AoaManager {

    var onAoaConnected: ((String) -> Unit)? = null
    var onAoaDisconnected: (() -> Unit)? = null
    var onInputReceived: ((com.sanket.tools.nexpad.model.GamepadInput) -> Unit)? = null
    var onRequestElevation: ((Short, Short) -> Unit)? = null

    private var context: Context? = null
    private var running = false
    private var hasPromptedElevation = false

    init {
        val ctx = Context()
        if (LibUsb.init(ctx) == LibUsb.SUCCESS) {
            context = ctx
            println("AoaManager: libusb initialized successfully")
        } else {
            println("AoaManager ERROR: Failed to init libusb")
        }
    }

    fun scanAndConnect() {
        running = true
        println("AoaManager: Starting continuous USB scanning loop...")
        val ctx = context ?: return

        while (running) {
            try {
                // 1. Check if AOA device is already re-enumerated and ready
                val aoaHandle = UsbScanner.findDevice(ctx, AoaProtocol.AOA_VID, AoaProtocol.AOA_PID_ACCESSORY)
                    ?: UsbScanner.findDevice(ctx, AoaProtocol.AOA_VID, AoaProtocol.AOA_PID_ACCESSORY_ADB)

                if (aoaHandle != null) {
                    println("AoaManager: Found AOA device! Starting stream...")
                    hasPromptedElevation = false
                    onAoaConnected?.invoke("Android Phone (USB Direct)")
                    
                    runBlocking {
                        AoaStreamer.startBulkStreaming(aoaHandle) { input ->
                            onInputReceived?.invoke(input)
                        }
                    }
                    
                    onAoaDisconnected?.invoke()
                    Thread.sleep(1000)
                    continue
                }

                // 2. Look for any Android phone in MTP mode
                val phonePair = UsbScanner.findAnyAndroidPhone(ctx)
                if (phonePair != null) {
                    val (phoneHandle, phoneDevice) = phonePair
                    println("AoaManager: Found phone! Attempting AOA handshake...")
                    
                    val success: Boolean
                    try {
                        success = AoaProtocol.sendHandshake(phoneHandle, phoneDevice)
                    } finally {
                        LibUsb.close(phoneHandle)
                        LibUsb.unrefDevice(phoneDevice)
                    }

                    if (success) {
                        hasPromptedElevation = false
                        println("AoaManager: Handshake successful, waiting for re-enumeration...")
                        Thread.sleep(3000)
                    } else {
                        println("AoaManager: Handshake failed. Retrying...")
                        Thread.sleep(2000)
                    }
                } else {
                    Thread.sleep(1000)
                }

            } catch (e: ElevationRequiredException) {
                if (!hasPromptedElevation) {
                    val vidHex = String.format("%04X", e.vid)
                    val pidHex = String.format("%04X", e.pid)
                    println("AoaManager: Caught ElevationRequiredException for VID:$vidHex PID:$pidHex")
                    hasPromptedElevation = true
                    onRequestElevation?.invoke(e.vid, e.pid)
                }
                Thread.sleep(2000)
            } catch (e: Exception) {
                println("AoaManager: Unexpected error: ${e.message}")
                Thread.sleep(2000)
            }
        }
    }

    fun cleanup() {
        running = false
        context?.let { LibUsb.exit(it) }
    }
}
