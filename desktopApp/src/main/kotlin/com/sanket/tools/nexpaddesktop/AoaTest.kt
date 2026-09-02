package com.sanket.tools.nexpaddesktop

import org.usb4java.*
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * ============================================================
 *  AOA PROOF-OF-CONCEPT TEST
 * ============================================================
 *  This tiny script tests whether your Android phone supports
 *  the AOA (Android Open Accessory) protocol over WinUSB.
 *
 *  PREREQUISITES:
 *  1. Plug your phone into your laptop via USB
 *  2. Use Zadig to swap your phone's driver to WinUSB
 *  3. Run this test
 * ============================================================
 */

// Known Android Vendor IDs
val ANDROID_VIDS = setOf(
    0x22B8.toShort(), // Motorola
    0x18D1.toShort(), // Google
    0x04E8.toShort(), // Samsung
    0x2717.toShort(), // Xiaomi
    0x1004.toShort(), // LG
    0x0FCE.toShort(), // Sony
    0x2A70.toShort(), // OnePlus
    0x1949.toShort(), // Oppo
    0x2C7C.toShort(), // Realme
    0x0E8D.toShort(), // MediaTek
)

// AOA Constants
const val AOA_GET_PROTOCOL: Byte = 51
const val AOA_SEND_STRING: Byte = 52
const val AOA_START: Byte = 53

val AOA_STRINGS = mapOf(
    0 to "Nexpad",
    1 to "Nexpad Controller",
    2 to "NEXPAD Virtual Gamepad Bridge",
    3 to "1.0",
    4 to "https://nexpad.app",
    5 to "NEX-0001"
)

fun main() {
    println("╔══════════════════════════════════════════════════╗")
    println("║     AOA PROOF-OF-CONCEPT TEST                   ║")
    println("║     Testing Android Open Accessory over WinUSB  ║")
    println("╚══════════════════════════════════════════════════╝")
    println()

    val context = Context()
    val result = LibUsb.init(context)
    if (result != LibUsb.SUCCESS) {
        println("❌ FATAL: Failed to initialize libusb: ${LibUsb.strError(result)}")
        return
    }
    println("✅ libusb initialized successfully")

    try {
        val deviceList = DeviceList()
        val count = LibUsb.getDeviceList(context, deviceList)
        if (count < 0) {
            println("❌ FATAL: Failed to enumerate USB devices: ${LibUsb.strError(count.toInt())}")
            return
        }
        println("📡 Found $count USB devices. Scanning for Android phones...")
        println()

        var androidDevice: Device? = null
        var androidVid: Short = 0
        var androidPid: Short = 0

        for (device in deviceList) {
            val descriptor = DeviceDescriptor()
            val descResult = LibUsb.getDeviceDescriptor(device, descriptor)
            if (descResult != LibUsb.SUCCESS) continue

            if (descriptor.idVendor() in ANDROID_VIDS) {
                androidDevice = device
                androidVid = descriptor.idVendor()
                androidPid = descriptor.idProduct()
                println("📱 Found Android device!")
                println("   VID: 0x${String.format("%04X", androidVid.toInt() and 0xFFFF)}")
                println("   PID: 0x${String.format("%04X", androidPid.toInt() and 0xFFFF)}")
                break
            }
        }

        if (androidDevice == null) {
            println("❌ FATAL: No Android device found!")
            println("   Make sure:")
            println("   1. Your phone is plugged in via USB")
            println("   2. You used Zadig to swap the driver to WinUSB")
            LibUsb.freeDeviceList(deviceList, true)
            return
        }

        println()
        println("🔌 Opening USB device handle...")
        val handle = DeviceHandle()
        val openResult = LibUsb.open(androidDevice, handle)
        if (openResult != LibUsb.SUCCESS) {
            println("❌ FATAL: Failed to open device: ${LibUsb.strError(openResult)}")
            when (openResult) {
                LibUsb.ERROR_ACCESS -> println("   → ACCESS DENIED. WinUSB not installed correctly.")
                LibUsb.ERROR_NOT_FOUND -> println("   → NOT FOUND. Device may have disconnected.")
                LibUsb.ERROR_NOT_SUPPORTED -> println("   → NOT SUPPORTED. Wrong driver loaded.")
            }
            LibUsb.freeDeviceList(deviceList, true)
            return
        }
        println("✅ Device opened successfully!")

        // ═══════════════════════════════════════════
        //  CRITICAL TEST: GET_PROTOCOL (Request 51)
        // ═══════════════════════════════════════════
        println()
        println("═══════════════════════════════════════════")
        println("  CRITICAL TEST: Sending AOA GET_PROTOCOL")
        println("═══════════════════════════════════════════")
        println()

        val protocolBuffer = ByteBuffer.allocateDirect(2)
        protocolBuffer.order(ByteOrder.LITTLE_ENDIAN)

        val protocolResult = LibUsb.controlTransfer(
            handle,
            (LibUsb.ENDPOINT_IN.toInt() or LibUsb.REQUEST_TYPE_VENDOR.toInt()).toByte(),
            AOA_GET_PROTOCOL,
            0.toShort(), 0.toShort(),
            protocolBuffer, 5000
        )

        if (protocolResult < 0) {
            println("❌ FAILED: GET_PROTOCOL error: ${LibUsb.strError(protocolResult)}")
            when (protocolResult) {
                LibUsb.ERROR_NOT_FOUND -> println("   → WinUSB loaded but cannot reach EP0. Composite device blocking.")
                LibUsb.ERROR_NOT_SUPPORTED -> println("   → Driver does not support vendor control transfers.")
                LibUsb.ERROR_PIPE -> println("   → Phone REJECTED the request. AOA NOT supported.")
                LibUsb.ERROR_TIMEOUT -> println("   → Phone did not respond. Check cable.")
            }
            println()
            println("⚠️ VERDICT: AOA is BLOCKED. Full implementation NOT viable.")
            LibUsb.close(handle)
            LibUsb.freeDeviceList(deviceList, true)
            return
        }

        val aoaVersion = protocolBuffer.getShort(0).toInt() and 0xFFFF
        println("✅ GET_PROTOCOL SUCCESS! AOA Version: $aoaVersion")

        if (aoaVersion == 0) {
            println("⚠️ VERDICT: Phone returned version 0. AOA NOT supported by this phone.")
            LibUsb.close(handle)
            LibUsb.freeDeviceList(deviceList, true)
            return
        }

        println("🎉 YOUR PHONE SUPPORTS AOA VERSION $aoaVersion!")
        println()

        // Send Identification Strings (Request 52)
        println("📝 Sending AOA identification strings...")
        for ((index, value) in AOA_STRINGS) {
            val bytes = value.toByteArray(Charsets.UTF_8)
            val buf = ByteBuffer.allocateDirect(bytes.size + 1)
            buf.put(bytes); buf.put(0.toByte()); buf.rewind()

            val r = LibUsb.controlTransfer(
                handle,
                (LibUsb.ENDPOINT_OUT.toInt() or LibUsb.REQUEST_TYPE_VENDOR.toInt()).toByte(),
                AOA_SEND_STRING, 0.toShort(), index.toShort(), buf, 5000
            )
            if (r < 0) {
                println("   ❌ String[$index] failed: ${LibUsb.strError(r)}")
                LibUsb.close(handle); LibUsb.freeDeviceList(deviceList, true)
                return
            }
            println("   ✅ [$index] '$value'")
        }

        println()
        println("🚀 Sending AOA START (phone will re-enumerate)...")
        val startResult = LibUsb.controlTransfer(
            handle,
            (LibUsb.ENDPOINT_OUT.toInt() or LibUsb.REQUEST_TYPE_VENDOR.toInt()).toByte(),
            AOA_START, 0.toShort(), 0.toShort(),
            ByteBuffer.allocateDirect(0), 5000
        )

        if (startResult < 0) {
            println("❌ AOA START failed: ${LibUsb.strError(startResult)}")
            println("⚠️ Phone supports AOA but refused to enter accessory mode.")
        } else {
            println()
            println("╔══════════════════════════════════════════════════╗")
            println("║  🎉 AOA PROOF-OF-CONCEPT: PASSED! 🎉            ║")
            println("║                                                  ║")
            println("║  Your phone entered AOA Accessory Mode!          ║")
            println("║  Check Device Manager for VID:18D1 PID:2D00      ║")
            println("║                                                  ║")
            println("║  ✅ SAFE TO PROCEED with full implementation.    ║")
            println("╚══════════════════════════════════════════════════╝")
            println()
            println("📋 Check your phone screen for the AOA popup!")
        }

        LibUsb.close(handle)
        LibUsb.freeDeviceList(deviceList, true)

    } finally {
        LibUsb.exit(context)
        println()
        println("🧹 Test complete.")
    }
}
