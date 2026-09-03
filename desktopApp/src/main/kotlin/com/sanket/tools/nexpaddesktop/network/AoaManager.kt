package com.sanket.tools.nexpaddesktop.network

import org.usb4java.*
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.IntBuffer

/**
 * AoaManager handles the complete AOA (Android Open Accessory) lifecycle:
 * 1. Scans USB devices for a connected Android phone
 * 2. Sends the AOA handshake (GET_PROTOCOL, SEND_STRING x5, START)
 * 3. Waits for the phone to re-enumerate as AOA device (VID 18D1, PID 2D00/2D01)
 * 4. Opens bulk endpoints for high-speed streaming
 *
 * NOTE: This currently requires WinUSB to already be bound to the phone's USB interface.
 * For development, use Zadig manually. The automated libwdi swap will come later.
 */
class AoaManager {

    var onAoaConnected: ((String) -> Unit)? = null
    var onAoaDisconnected: (() -> Unit)? = null
    var onInputReceived: ((com.sanket.tools.nexpad.model.GamepadInput) -> Unit)? = null
    var onRequestElevation: ((Short, Short) -> Unit)? = null

    private var context: Context? = null
    private var running = false
    private var hasPromptedElevation = false
    private var lastAttemptedPid: Short = 0

    // Known Android OEM VIDs
    private val ANDROID_VIDS = setOf<Short>(
        0x18D1.toShort(), // Google / AOA
        0x22B8.toShort(), // Motorola
        0x04E8.toShort(), // Samsung
        0x0BB4.toShort(), // HTC
        0x0FCE.toShort(), // Sony
        0x1004.toShort(), // LG
        0x2A70.toShort(), // OnePlus
        0x2717.toShort(), // Xiaomi
        0x12D1.toShort(), // Huawei
        0x22D9.toShort(), // Oppo/Realme
        0x2D95.toShort(), // Vivo
        0x0B05.toShort()  // Asus
    )

    // Google AOA after re-enumeration
    private val AOA_VID: Short = 0x18D1.toShort()
    private val AOA_PID_ACCESSORY: Short = 0x2D00.toShort()
    private val AOA_PID_ACCESSORY_ADB: Short = 0x2D01.toShort()

    // AOA USB control requests
    private val AOA_GET_PROTOCOL: Byte = 51
    private val AOA_SEND_STRING: Byte = 52
    private val AOA_START: Byte = 53

    init {
        val ctx = Context()
        val result = LibUsb.init(ctx)
        if (result == LibUsb.SUCCESS) {
            context = ctx
            println("AoaManager: libusb initialized successfully")
        } else {
            println("AoaManager ERROR: Failed to init libusb: $result")
        }
    }

    /**
     * Main entry point. Scans for the phone, attempts handshake.
     * This is meant to be called from a background coroutine.
     */
    fun scanAndConnect() {
        running = true
        println("AoaManager: Starting continuous USB scanning loop...")

        while (running) {
            // Step 1: Check if an AOA device is already connected (phone was already switched)
            val aoaHandle = findDevice(AOA_VID, AOA_PID_ACCESSORY)
                ?: findDevice(AOA_VID, AOA_PID_ACCESSORY_ADB)
    
            if (aoaHandle != null) {
                println("AoaManager: Found existing AOA device! Skipping handshake.")
                onAoaConnected?.invoke("Android Phone (USB Direct)")
                startBulkStreaming(aoaHandle)
                // When startBulkStreaming returns, it means we disconnected.
                // We'll loop back and start scanning again.
                Thread.sleep(1000)
                continue
            }
    
            // Step 2: Dynamically look for any Android phone
            var phoneHandle: DeviceHandle? = null
            var phoneDevice: org.usb4java.Device? = null
            var foundVid: Short = 0
            var foundPid: Short = 0
            
            val deviceList = DeviceList()
            if (LibUsb.getDeviceList(context, deviceList) >= 0) {
                for (device in deviceList) {
                    val desc = DeviceDescriptor()
                    LibUsb.getDeviceDescriptor(device, desc)
                    if (ANDROID_VIDS.contains(desc.idVendor())) {
                        // Skip if it's already in AOA mode (handled above)
                        if (desc.idVendor() == AOA_VID && (desc.idProduct() == AOA_PID_ACCESSORY || desc.idProduct() == AOA_PID_ACCESSORY_ADB)) {
                            continue
                        }
                        
                        // We found a supported Android OEM! Try to open it.
                        val handle = DeviceHandle()
                        val res = LibUsb.open(device, handle)
                        if (res == LibUsb.SUCCESS) {
                            phoneHandle = handle
                            phoneDevice = device
                            LibUsb.refDevice(device) // Keep a reference
                            foundVid = desc.idVendor()
                            foundPid = desc.idProduct()
                            break
                        } else if (res == LibUsb.ERROR_ACCESS || res == LibUsb.ERROR_NOT_SUPPORTED) {
                            // Needs elevation!
                            if (!hasPromptedElevation) {
                                println("AoaManager: Found Android device (VID:${String.format("%04X", desc.idVendor())} PID:${String.format("%04X", desc.idProduct())}) but access denied.")
                                println("AoaManager: Requesting elevation to swap WinUSB driver...")
                                hasPromptedElevation = true
                                onRequestElevation?.invoke(desc.idVendor(), desc.idProduct())
                            }
                        }
                    }
                }
                LibUsb.freeDeviceList(deviceList, true)
            }
                
            if (phoneHandle == null) {
                // Not found or elevation pending, wait 2 seconds and check again
                Thread.sleep(2000)
                continue
            }
            
            // If findDevice returns a special 'mock' handle when elevation is needed, we handle it:
            // But we actually handle elevation inside findDevice, so if phoneHandle is valid, we proceed.
    
            println("AoaManager: Found phone! Attempting AOA handshake...")
    
            // Step 3: Send AOA handshake
            val success = sendAoaHandshake(phoneHandle, phoneDevice!!)
            LibUsb.close(phoneHandle)
    
            if (!success) {
                println("AoaManager ERROR: Handshake failed! Retrying in 2s...")
                
                // If the handshake fails with NOT_FOUND on Windows, it usually means the device was opened 
                // but the underlying driver is MTP, not WinUSB. We must trigger the elevation swap!
                if (!hasPromptedElevation) {
                    val desc = DeviceDescriptor()
                    LibUsb.getDeviceDescriptor(phoneDevice, desc)
                    println("AoaManager: Requesting elevation to swap WinUSB driver because handshake failed...")
                    hasPromptedElevation = true
                    onRequestElevation?.invoke(desc.idVendor(), desc.idProduct())
                }
                
                LibUsb.unrefDevice(phoneDevice)
                Thread.sleep(2000)
                continue
            }
            LibUsb.unrefDevice(phoneDevice)
    
            println("AoaManager: Handshake sent! Waiting for phone to re-enumerate as AOA...")
            hasPromptedElevation = false // Reset here, because handshake succeeded!
    
            // Step 4: Wait for the phone to re-enumerate (it physically disconnects and reconnects)
            Thread.sleep(3000)
    
            val newAoaHandle = findDevice(AOA_VID, AOA_PID_ACCESSORY)
                ?: findDevice(AOA_VID, AOA_PID_ACCESSORY_ADB)
    
            if (newAoaHandle == null) {
                println("AoaManager ERROR: Phone did not re-enumerate as AOA device! Retrying...")
                Thread.sleep(1000)
                continue
            }
    
            println("AoaManager: Phone re-enumerated as AOA device!")
            onAoaConnected?.invoke("Moto G85 (USB Direct)")
            startBulkStreaming(newAoaHandle)
            Thread.sleep(1000)
        }
    }

    private fun findDevice(vid: Short, pid: Short): DeviceHandle? {
        val ctx = context ?: return null
        val deviceList = DeviceList()
        val count = LibUsb.getDeviceList(ctx, deviceList)
        if (count < 0) return null

        try {
            for (device in deviceList) {
                val descriptor = DeviceDescriptor()
                if (LibUsb.getDeviceDescriptor(device, descriptor) == LibUsb.SUCCESS) {
                    if (descriptor.idVendor() == vid && descriptor.idProduct() == pid) {
                        val handle = DeviceHandle()
                        val openResult = LibUsb.open(device, handle)
                        if (openResult == LibUsb.SUCCESS) {
                            println("AoaManager: Opened device VID:${String.format("%04X", vid)} PID:${String.format("%04X", pid)}")
                            // Try to detach any kernel driver holding the interface
                            LibUsb.setAutoDetachKernelDriver(handle, true)
                            return handle
                        } else if (openResult == LibUsb.ERROR_ACCESS || openResult == LibUsb.ERROR_NOT_SUPPORTED) {
                            if (!hasPromptedElevation) {
                                println("AoaManager: ACCESS DENIED (error $openResult). Phone is likely in MTP mode.")
                                println("AoaManager: Requesting elevation to swap WinUSB driver...")
                                hasPromptedElevation = true
                                onRequestElevation?.invoke(vid, pid)
                            }
                        } else {
                            println("AoaManager: Found device but can't open it (error $openResult: ${LibUsb.errorName(openResult)})")
                        }
                    }
                }
            }
        } finally {
            LibUsb.freeDeviceList(deviceList, true)
        }
        return null
    }

    private fun sendAoaHandshake(handle: DeviceHandle, device: org.usb4java.Device): Boolean {
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
            // Fallback to 0
            LibUsb.claimInterface(handle, 0)
            claimedInterface = 0
        }

        
        // 1. GET_PROTOCOL (Request 51) - Check if AOA is supported
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
            println("AoaManager: GET_PROTOCOL failed: ${LibUsb.errorName(getProtoResult)}")
            return false
        }
        val protocol = protocolBuf.getShort(0)
        println("AoaManager: AOA Protocol version = $protocol")

        // 2. SEND_STRING (Request 52) - Send accessory identity (5 strings)
        val strings = arrayOf(
            "Nexpad",              // manufacturer (index 0)
            "Nexpad Controller",   // model (index 1)
            "NEXPAD USB Gamepad",  // description (index 2)
            "1.0",                 // version (index 3)
            "https://nexpad.dev",  // URI (index 4)
            "nexpad-serial-001"    // serial (index 5)
        )

        for ((index, str) in strings.withIndex()) {
            val strBytes = str.toByteArray(Charsets.UTF_8)
            val strBuf = ByteBuffer.allocateDirect(strBytes.size + 1)
            strBuf.put(strBytes)
            strBuf.put(0) // null terminator
            strBuf.rewind()

            val sendResult = LibUsb.controlTransfer(
                handle,
                (LibUsb.ENDPOINT_OUT.toInt() or LibUsb.REQUEST_TYPE_VENDOR.toInt()).toByte(),
                AOA_SEND_STRING,
                0, index.toShort(),
                strBuf,
                1000
            )
            if (sendResult < 0) {
                println("AoaManager: SEND_STRING[$index] failed: ${LibUsb.errorName(sendResult)}")
                return false
            }
            println("AoaManager: Sent string[$index] = \"$str\"")
        }

        // 3. START (Request 53) - Tell the phone to enter AOA mode
        val startResult = LibUsb.controlTransfer(
            handle,
            (LibUsb.ENDPOINT_OUT.toInt() or LibUsb.REQUEST_TYPE_VENDOR.toInt()).toByte(),
            AOA_START,
            0, 0,
            ByteBuffer.allocateDirect(0),
            1000
        )
        if (startResult < 0) {
            println("AoaManager: START failed: ${LibUsb.errorName(startResult)}")
            return false
        }
        println("AoaManager: START(53) sent successfully! Phone should re-enumerate now.")
        LibUsb.releaseInterface(handle, 0)
        return true
    }

    private fun startBulkStreaming(handle: DeviceHandle) {
        val claimResult = LibUsb.claimInterface(handle, 0)
        if (claimResult != LibUsb.SUCCESS) {
            println("AoaManager ERROR: Failed to claim interface: ${LibUsb.errorName(claimResult)}")
            return
        }
        println("AoaManager: Claimed USB interface 0. Starting bulk read loop...")

        val endpointIn: Byte = 0x81.toByte()
        val buffer = ByteBuffer.allocateDirect(64)
        val transferred = IntBuffer.allocate(1)

        while (running) {
            buffer.clear()
            transferred.clear()
            val result = LibUsb.bulkTransfer(handle, endpointIn, buffer, transferred, 1000)

            if (result == LibUsb.SUCCESS && transferred.get(0) > 0) {
                val bytesRead = transferred.get(0)
                val data = ByteArray(bytesRead)
                buffer.get(data)
                if (bytesRead == com.sanket.tools.nexpad.protocol.NexpadProtocol.INPUT_PACKET_SIZE) {
                    val input = com.sanket.tools.nexpad.protocol.NexpadProtocol.decodeInput(data)
                    if (input != null) {
                        onInputReceived?.invoke(input)
                    }
                }
            } else if (result == LibUsb.ERROR_TIMEOUT) {
                // Normal timeout, just keep polling
            } else if (result == LibUsb.ERROR_PIPE || result == LibUsb.ERROR_NO_DEVICE) {
                println("AoaManager: Device disconnected or pipe broken (${LibUsb.errorName(result)}).")
                break
            } else if (result < 0) {
                println("AoaManager: Bulk read error: ${LibUsb.errorName(result)}")
                break
            }
        }

        LibUsb.releaseInterface(handle, 0)
        LibUsb.close(handle)
        onAoaDisconnected?.invoke()
        println("AoaManager: Disconnected.")
    }

    fun cleanup() {
        running = false
        context?.let { LibUsb.exit(it) }
    }
}
