package com.sanket.tools.nexpaddesktop.connection.usb.aoa

import org.usb4java.DeviceHandle
import java.nio.ByteBuffer
import java.nio.ByteOrder
import org.usb4java.LibUsb

sealed interface UsbOpenFailure {
    data object AccessDenied : UsbOpenFailure
    data object DeviceGone : UsbOpenFailure
    data object NotSupported : UsbOpenFailure
    data class Other(val code: Int, val name: String) : UsbOpenFailure
}

class UsbHandshakeException(val failure: UsbOpenFailure) : Exception("AOA Handshake failed: $failure")

object AoaHandshake {
    private const val AOA_GET_PROTOCOL: Byte = 51
    private const val AOA_SEND_STRING: Byte = 52
    private const val AOA_START: Byte = 53

    /**
     * Sends the Android Open Accessory Handshake.
     * Tells the phone to switch from MTP to AOA mode.
     *
     * Note: Control transfers on EP0 do NOT require claiming an interface.
     * This avoids locking the device unnecessarily.
     *
     * @throws UsbHandshakeException if the handshake fails due to backend/permissions
     */
    fun sendHandshake(handle: DeviceHandle): Boolean {
        // 1. GET_PROTOCOL (Request 51)
        val protocolBuf = ByteBuffer.allocateDirect(2).order(ByteOrder.LITTLE_ENDIAN)
        val getProtoResult = LibUsb.controlTransfer(
            handle,
            (LibUsb.ENDPOINT_IN.toInt() or LibUsb.REQUEST_TYPE_VENDOR.toInt()).toByte(),
            AOA_GET_PROTOCOL,
            0, 0,
            protocolBuf,
            1000
        )
        
        if (getProtoResult < 0) {
            println("AoaHandshake: GET_PROTOCOL failed: ${LibUsb.errorName(getProtoResult)}")
            throw UsbHandshakeException(mapLibUsbError(getProtoResult))
        }
        
        val protocol = protocolBuf.getShort(0)
        println("AoaHandshake: AOA Protocol version = $protocol")
        
        // AOA Specification: protocol version must be > 0. Usually 1 or 2.
        if (protocol <= 0) {
            println("AoaHandshake ERROR: Invalid or unsupported AOA protocol version ($protocol).")
            return false
        }

        // 2. SEND_STRING (Request 52)
        val strings = arrayOf(
            "Nexpad",              // manufacturer
            "Nexpad Controller",   // model
            "NEXPAD USB Gamepad",  // description
            "1.0",                 // version
            "https://nexpad.dev",  // URI
            "nexpad-serial-001"    // serial
        )

        for ((index, str) in strings.withIndex()) {
            val strBytes = str.toByteArray(Charsets.UTF_8)
            val strBuf = ByteBuffer.allocateDirect(strBytes.size + 1)
            strBuf.put(strBytes).put(0).rewind()

            val sendResult = LibUsb.controlTransfer(
                handle,
                (LibUsb.ENDPOINT_OUT.toInt() or LibUsb.REQUEST_TYPE_VENDOR.toInt()).toByte(),
                AOA_SEND_STRING,
                0, index.toShort(),
                strBuf,
                1000
            )
            
            if (sendResult < 0) {
                println("AoaHandshake: SEND_STRING[$index] failed: ${LibUsb.errorName(sendResult)}")
                throw UsbHandshakeException(mapLibUsbError(sendResult))
            }
        }

        // 3. START (Request 53)
        val startResult = LibUsb.controlTransfer(
            handle,
            (LibUsb.ENDPOINT_OUT.toInt() or LibUsb.REQUEST_TYPE_VENDOR.toInt()).toByte(),
            AOA_START,
            0, 0,
            ByteBuffer.allocateDirect(0),
            1000
        )
        
        if (startResult < 0) {
            println("AoaHandshake: START failed: ${LibUsb.errorName(startResult)}")
            throw UsbHandshakeException(mapLibUsbError(startResult))
        }
        
        println("AoaHandshake: START(53) sent successfully! Phone should re-enumerate now.")
        return true
    }
    
    private fun mapLibUsbError(errorCode: Int): UsbOpenFailure {
        return when (errorCode) {
            LibUsb.ERROR_ACCESS, LibUsb.ERROR_NOT_FOUND -> UsbOpenFailure.AccessDenied
            LibUsb.ERROR_NO_DEVICE, LibUsb.ERROR_IO -> UsbOpenFailure.DeviceGone
            LibUsb.ERROR_NOT_SUPPORTED -> UsbOpenFailure.NotSupported
            else -> UsbOpenFailure.Other(errorCode, LibUsb.errorName(errorCode))
        }
    }
}
