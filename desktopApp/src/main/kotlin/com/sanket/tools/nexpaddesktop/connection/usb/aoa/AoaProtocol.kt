package com.sanket.tools.nexpaddesktop.connection.usb.aoa

import org.usb4java.DeviceHandle
import java.nio.ByteBuffer
import java.nio.ByteOrder
import org.usb4java.LibUsb

object AoaProtocol {
    const val AOA_VID: Short = 0x18D1.toShort()
    const val AOA_PID_ACCESSORY: Short = 0x2D00.toShort()
    const val AOA_PID_ACCESSORY_ADB: Short = 0x2D01.toShort()

    private const val AOA_GET_PROTOCOL: Byte = 51
    private const val AOA_SEND_STRING: Byte = 52
    private const val AOA_START: Byte = 53

    /**
     * Sends the complete Android Open Accessory Handshake.
     * Tells the phone to switch from MTP to AOA mode.
     */
    fun sendHandshake(handle: DeviceHandle, device: org.usb4java.Device): Boolean {
        // On Windows, if WinUSB is bound to a specific interface (like ADB), we must claim it.
        val config = org.usb4java.ConfigDescriptor()
        var claimedInterface = -1
        if (LibUsb.getActiveConfigDescriptor(device, config) >= 0) {
            val ifaces = config.iface()
            if (ifaces != null) {
                for (iface in ifaces) {
                    val alts = iface.altsetting()
                    if (alts != null && alts.size > 0) {
                        val ifaceNum = alts[0].bInterfaceNumber().toInt()
                        if (LibUsb.claimInterface(handle, ifaceNum) == LibUsb.SUCCESS) {
                            claimedInterface = ifaceNum
                            break // Just need one to send control requests
                        }
                    }
                }
            }
            LibUsb.freeConfigDescriptor(config)
        }
        if (claimedInterface == -1) {
            LibUsb.claimInterface(handle, 0)
            claimedInterface = 0
        }

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
            println("AoaProtocol: GET_PROTOCOL failed: ${LibUsb.errorName(getProtoResult)}")
            if (getProtoResult == LibUsb.ERROR_NOT_FOUND || getProtoResult == LibUsb.ERROR_ACCESS || getProtoResult == LibUsb.ERROR_NOT_SUPPORTED) {
                val desc = org.usb4java.DeviceDescriptor()
                LibUsb.getDeviceDescriptor(device, desc)
                throw com.sanket.tools.nexpaddesktop.connection.usb.ElevationRequiredException(desc.idVendor(), desc.idProduct())
            }
            return false
        }
        val protocol = protocolBuf.getShort(0)
        println("AoaProtocol: AOA Protocol version = $protocol")

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
                println("AoaProtocol: SEND_STRING[$index] failed: ${LibUsb.errorName(sendResult)}")
                return false
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
            println("AoaProtocol: START failed: ${LibUsb.errorName(startResult)}")
            return false
        }
        println("AoaProtocol: START(53) sent successfully! Phone should re-enumerate now.")
        LibUsb.releaseInterface(handle, claimedInterface)
        return true
    }
}
