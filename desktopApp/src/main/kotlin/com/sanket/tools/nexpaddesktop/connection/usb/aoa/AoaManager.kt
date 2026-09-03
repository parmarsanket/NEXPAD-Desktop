package com.sanket.tools.nexpaddesktop.connection.usb.aoa

import com.sanket.tools.nexpad.model.GamepadInput
import org.usb4java.Context
import org.usb4java.DeviceHandle
import org.usb4java.LibUsb
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers

enum class AoaState {
    IDLE,
    SCANNING,
    ANDROID_DEVICE_FOUND,
    OPEN_HANDSHAKE_INTERFACE,
    HANDSHAKE_IN_PROGRESS,
    WAIT_FOR_REENUMERATION,
    AOA_DEVICE_FOUND,
    OPEN_ACCESSORY_INTERFACE,
    STREAMING,
    NEEDS_DRIVER,
    DRIVER_INSTALL_IN_PROGRESS
}

class AoaManager {
    var onAoaConnected: ((String) -> Unit)? = null
    var onAoaDisconnected: (() -> Unit)? = null
    var onInputReceived: ((GamepadInput) -> Unit)? = null
    
    // Callback to trigger UAC prompt. Requires VID, PID, and optionally the specific MTP Interface Number.
    var onRequestElevation: ((Short, Short, Int?) -> Unit)? = null

    private var context: Context? = null
    var currentState = AoaState.IDLE
        private set

    init {
        val ctx = Context()
        val result = LibUsb.init(ctx)
        if (result != LibUsb.SUCCESS) {
            println("AoaManager ERROR: Unable to initialize libusb. ")
        } else {
            this.context = ctx
            println("AoaManager: libusb initialized successfully")
        }
    }

    suspend fun scanAndConnect() = coroutineScope {
        if (context == null) return@coroutineScope
        currentState = AoaState.SCANNING
        println("AoaManager: Starting continuous USB scanning loop...")

        while (isActive) {
            when (currentState) {
                AoaState.SCANNING -> {
                    val candidates = UsbDeviceDetector.findCandidates(context!!)
                    
                    val aoaCandidate = candidates.filterIsInstance<CandidateUsbDevice.AoaDevice>().firstOrNull()
                    if (aoaCandidate != null) {
                        currentState = AoaState.AOA_DEVICE_FOUND
                        processAoaDevice(aoaCandidate)
                        continue
                    }

                    val androidCandidate = candidates.filterIsInstance<CandidateUsbDevice.AndroidDevice>().firstOrNull()
                    if (androidCandidate != null) {
                        currentState = AoaState.ANDROID_DEVICE_FOUND
                        processAndroidDevice(androidCandidate)
                        continue
                    }
                    
                    delay(1000)
                }
                
                AoaState.WAIT_FOR_REENUMERATION -> {
                    delay(500)
                    currentState = AoaState.SCANNING // Let the scanning loop find the new AOA device
                }

                AoaState.NEEDS_DRIVER -> {
                    // Waiting for the UI to resolve the driver issue via UAC
                    delay(1000)
                }

                AoaState.DRIVER_INSTALL_IN_PROGRESS -> {
                    // UAC has been requested, waiting for the result
                    delay(1000)
                }
                
                else -> {
                    delay(1000)
                }
            }
        }
        cleanup()
    }

    private suspend fun processAndroidDevice(candidate: CandidateUsbDevice.AndroidDevice) {
        currentState = AoaState.OPEN_HANDSHAKE_INTERFACE
        
        val handle = DeviceHandle()
        val openResult = LibUsb.open(candidate.device, handle)
        
        if (openResult == LibUsb.SUCCESS) {
            println("AoaManager: Opened Android device [VID:${String.format("%04X", candidate.vid)} PID:${String.format("%04X", candidate.pid)}]")
            LibUsb.setAutoDetachKernelDriver(handle, true)
            
            currentState = AoaState.HANDSHAKE_IN_PROGRESS
            try {
                // Borrow handle to handshake
                val handshakeSuccess = AoaHandshake.sendHandshake(handle)
                if (handshakeSuccess) {
                    currentState = AoaState.WAIT_FOR_REENUMERATION
                } else {
                    currentState = AoaState.SCANNING
                }
            } catch (e: UsbHandshakeException) {
                println("AoaManager: Handshake exception: ${e.message}")
                if (e.failure == UsbOpenFailure.AccessDenied) {
                    println("AoaManager: Access Denied to EP0. Requesting WinUSB Driver Installation...")
                    currentState = AoaState.NEEDS_DRIVER
                    onRequestElevation?.invoke(candidate.vid, candidate.pid, candidate.mtpInterfaceNumber)
                } else {
                    currentState = AoaState.SCANNING
                }
            } finally {
                LibUsb.close(handle)
                LibUsb.unrefDevice(candidate.device)
            }
        } else {
            LibUsb.unrefDevice(candidate.device)
            if (openResult == LibUsb.ERROR_ACCESS || openResult == LibUsb.ERROR_NOT_SUPPORTED) {
                println("AoaManager: LibUsb.open Access Denied. Requesting WinUSB Driver Installation...")
                currentState = AoaState.NEEDS_DRIVER
                onRequestElevation?.invoke(candidate.vid, candidate.pid, candidate.mtpInterfaceNumber)
            } else {
                currentState = AoaState.SCANNING
            }
        }
    }

    private suspend fun processAoaDevice(candidate: CandidateUsbDevice.AoaDevice) {
        currentState = AoaState.OPEN_ACCESSORY_INTERFACE
        
        val handle = DeviceHandle()
        val openResult = LibUsb.open(candidate.device, handle)
        
        if (openResult == LibUsb.SUCCESS) {
            println("AoaManager: Opened AOA device [VID:${String.format("%04X", candidate.vid)} PID:${String.format("%04X", candidate.pid)}]")
            LibUsb.setAutoDetachKernelDriver(handle, true)
            
            currentState = AoaState.STREAMING
            onAoaConnected?.invoke("Android Phone (USB Direct)")
            
            try {
                // Hand over streaming to transport. It borrows the handle but we own it.
                AoaTransport.startBulkStreaming(handle) { input ->
                    onInputReceived?.invoke(input)
                }
            } finally {
                LibUsb.close(handle)
                LibUsb.unrefDevice(candidate.device)
                onAoaDisconnected?.invoke()
                currentState = AoaState.SCANNING
            }
        } else {
            LibUsb.unrefDevice(candidate.device)
            if (openResult == LibUsb.ERROR_ACCESS || openResult == LibUsb.ERROR_NOT_SUPPORTED) {
                println("AoaManager: LibUsb.open Access Denied on AOA Device! Requesting WinUSB for Phase 2...")
                currentState = AoaState.NEEDS_DRIVER
                onRequestElevation?.invoke(candidate.vid, candidate.pid, null)
            } else {
                currentState = AoaState.SCANNING
            }
        }
    }
    
    /**
     * Called by the UI when it initiates the UAC prompt.
     */
    fun notifyDriverInstallStarted() {
        if (currentState == AoaState.NEEDS_DRIVER) {
            currentState = AoaState.DRIVER_INSTALL_IN_PROGRESS
        }
    }

    /**
     * Called by the UI when the driver installation finishes (success or fail).
     */
    fun notifyDriverInstallFinished() {
        if (currentState == AoaState.DRIVER_INSTALL_IN_PROGRESS) {
            currentState = AoaState.SCANNING // Retries finding the device
        }
    }

    fun cleanup() {
        context?.let { LibUsb.exit(it) }
    }
}
