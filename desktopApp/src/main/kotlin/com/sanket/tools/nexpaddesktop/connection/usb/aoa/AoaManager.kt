package com.sanket.tools.nexpaddesktop.connection.usb.aoa

import com.sanket.tools.nexpad.model.GamepadInput
import org.usb4java.Context
import org.usb4java.DeviceHandle
import org.usb4java.LibUsb
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.coroutineScope

sealed class AoaState {
    data object Idle : AoaState()
    data class InstallingDriver(val startTime: Long) : AoaState()
    data object Connected : AoaState()
}

class AoaManager {
    var onAoaConnected: ((String) -> Unit)? = null
    var onAoaDisconnected: (() -> Unit)? = null
    var onInputReceived: ((GamepadInput) -> Unit)? = null
    
    // Callback to trigger UAC prompt. Requires VID, PID, and optionally the specific MTP Interface Number.
    var onRequestElevation: ((Short, Short, Int?) -> Unit)? = null

    // Prevents endless elevation prompt loops when user cancels
    @Volatile var userDismissedElevation = false

    private var activeFeedbackChannel: kotlinx.coroutines.channels.Channel<com.sanket.tools.nexpad.model.GamepadFeedback>? = null

    fun sendFeedback(feedback: com.sanket.tools.nexpad.model.GamepadFeedback) {
        activeFeedbackChannel?.trySend(feedback)
    }

    private var context: Context? = null
    private var currentState: AoaState = AoaState.Idle

    fun refreshContext() {
        println("[AOA/Manager] Refreshing LibUsb context to clear Windows PnP cache...")
        context?.let { LibUsb.exit(it) }
        val newCtx = Context()
        if (LibUsb.init(newCtx) == LibUsb.SUCCESS) {
            this.context = newCtx
        }
    }

    fun onDriverInstallCompleted(success: Boolean) {
        println("[AOA/Manager] Driver installation finished (success=$success). Refreshing USB context.")
        currentState = AoaState.Idle
        refreshContext()
    }

    fun resetDismissedElevation() {
        userDismissedElevation = false
    }

    init {
        val ctx = Context()
        val result = LibUsb.init(ctx)
        if (result != LibUsb.SUCCESS) {
            println("[AOA/Manager] ERROR: Unable to initialize libusb.")
        } else {
            this.context = ctx
            println("[AOA/Manager] libusb initialized successfully")
        }
    }

    // Mutual exclusion: when ADB device is present / USB debugging is ON, AOA is suppressed
    var isAdbActive: () -> Boolean = { false }

    suspend fun scanAndConnect() = coroutineScope {
        if (context == null) return@coroutineScope
        println("[AOA/Manager] Starting continuous USB scanning loop...")

        while (isActive) {
            // If USB Debugging is ON and an ADB device is present, AOA must completely back off!
            if (isAdbActive()) {
                delay(1500)
                continue
            }

            // While Windows is actively installing a driver, DO NOT query or open USB devices!
            // Probing device descriptors during driver installation causes PNP_VetoOutstandingOpen (-11 error)!
            if (currentState is AoaState.InstallingDriver) {
                val state = currentState as AoaState.InstallingDriver
                if (System.currentTimeMillis() - state.startTime > 30_000) {
                    println("[AOA/Manager] WinUSB installation timed out or user cancelled. Resetting to Idle state.")
                    currentState = AoaState.Idle
                    refreshContext()
                }
                delay(1000)
                continue
            }

            val candidates = UsbDeviceDetector.findCandidates(context!!)
            
            try {
                val aoaCandidate = candidates.filterIsInstance<CandidateUsbDevice.AoaDevice>().firstOrNull()
                if (aoaCandidate != null) {
                    if (userDismissedElevation) {
                        delay(2000)
                        continue
                    }
                    val elevationRequired = processAoaDevice(aoaCandidate)
                    if (elevationRequired && currentState is AoaState.Idle && !userDismissedElevation) {
                        println("[AOA/Manager] AOA Device requires WinUSB (Phase 2). Transitioning to InstallingDriver state.")
                        currentState = AoaState.InstallingDriver(System.currentTimeMillis())
                        onRequestElevation?.invoke(aoaCandidate.vid, aoaCandidate.pid, null)
                    } else if (!elevationRequired && currentState is AoaState.InstallingDriver) {
                        println("[AOA/Manager] Phase 2 AOA connection successful. Resetting state to Connected.")
                        currentState = AoaState.Connected
                    }
                    continue
                }

                val androidCandidate = candidates.filterIsInstance<CandidateUsbDevice.AndroidDevice>().firstOrNull()
                if (androidCandidate != null) {
                    if (userDismissedElevation) {
                        delay(3000)
                        continue
                    }
                    val elevationRequired = processAndroidDevice(androidCandidate)
                    if (elevationRequired && currentState is AoaState.Idle && !userDismissedElevation) {
                        println("[AOA/Manager] Device requires WinUSB driver (Phase 1). Transitioning to InstallingDriver state.")
                        currentState = AoaState.InstallingDriver(System.currentTimeMillis())
                        onRequestElevation?.invoke(androidCandidate.vid, androidCandidate.pid, androidCandidate.mtpInterfaceNumber)
                    } else if (!elevationRequired && currentState is AoaState.InstallingDriver) {
                        println("[AOA/Manager] Phase 1 Handshake successful! Resetting state to Idle.")
                        currentState = AoaState.Idle
                    }
                    delay(2000)
                    continue
                }
                
                delay(1000)
            } finally {
                // CRITICAL: Prevent memory leak of unused USB handles!
                candidates.forEach { it.unref() }
            }
        }
        cleanup()
    }

    private suspend fun processAndroidDevice(candidate: CandidateUsbDevice.AndroidDevice): Boolean {
        val handle = DeviceHandle()
        val openResult = LibUsb.open(candidate.device, handle)
        
        if (openResult == LibUsb.SUCCESS) {
            if (currentState is AoaState.Idle) {
                println("[AOA/Manager] Opened Android device [VID:${String.format("%04X", candidate.vid)} PID:${String.format("%04X", candidate.pid)}]")
            }
            LibUsb.setAutoDetachKernelDriver(handle, true)
            
            try {
                AoaHandshake.sendHandshake(handle, candidate.device)
                return false
            } catch (e: UsbHandshakeException) {
                if (currentState is AoaState.Idle) {
                    println("[AOA/Manager] Handshake exception: ${e.message}")
                }
                if (e.failure == UsbOpenFailure.AccessDenied) {
                    return true
                }
                return false
            } finally {
                LibUsb.close(handle)
            }
        } else {
            if (openResult == LibUsb.ERROR_ACCESS || openResult == LibUsb.ERROR_NOT_SUPPORTED) {
                return true
            }
            return false
        }
    }

    private suspend fun processAoaDevice(candidate: CandidateUsbDevice.AoaDevice): Boolean {
        val handle = DeviceHandle()
        val openResult = LibUsb.open(candidate.device, handle)
        
        if (openResult == LibUsb.SUCCESS) {
            println("[AOA/Manager] Opened AOA device [VID:${String.format("%04X", candidate.vid)} PID:${String.format("%04X", candidate.pid)}]")
            LibUsb.setAutoDetachKernelDriver(handle, true)
            
            onAoaConnected?.invoke("Android Phone (USB Direct)")
            userDismissedElevation = false // Reset on successful connection
            
            try {
                val feedbackChannel = kotlinx.coroutines.channels.Channel<com.sanket.tools.nexpad.model.GamepadFeedback>(kotlinx.coroutines.channels.Channel.CONFLATED)
                activeFeedbackChannel = feedbackChannel

                AoaTransport.startBulkStreaming(handle, feedbackChannel) { input ->
                    onInputReceived?.invoke(input)
                }
            } finally {
                activeFeedbackChannel = null
                LibUsb.close(handle)
                onAoaDisconnected?.invoke()
                currentState = AoaState.Idle
            }
            return false
        } else {
            if (openResult == LibUsb.ERROR_ACCESS || openResult == LibUsb.ERROR_NOT_SUPPORTED) {
                return true
            }
            return false
        }
    }
    
    fun cleanup() {
        context?.let { LibUsb.exit(it) }
    }
}
