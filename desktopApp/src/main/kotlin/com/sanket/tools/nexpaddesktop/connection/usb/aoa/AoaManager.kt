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
    data class HandshakeSent(val timestamp: Long, val vid: Short, val pid: Short) : AoaState()
    data class InstallingDriver(val startTime: Long) : AoaState()
    data object Connected : AoaState()
}

/**
 * Outcome of attempting to open and handshake an Android device in MTP mode.
 *
 * - [HandshakeSent]     : AOA handshake was transmitted; phone is re-enumerating as accessory.
 * - [ElevationRequired] : A WinUSB driver must be installed via UAC before libusb can open the device.
 *                         [targetMi] = null     -> install on root composite device (no MI scoping) --
 *                                                  required when a proprietary OEM driver (e.g. Samsung
 *                                                  ssudbus.sys) blocks libusb at the composite level.
 *                         [targetMi] = non-null -> scope the install to that specific interface only.
 * - [None]              : No action taken (device did not respond to AOA or is not yet ready).
 *
 * Detection is purely protocol-based: no vendor ID lists are used. The libusb error code
 * LIBUSB_ERROR_NOT_SUPPORTED (-12) is the universal, brand-agnostic signal that a proprietary
 * OEM composite driver is blocking access and root WinUSB must be installed.
 */
sealed interface AndroidProcessOutcome {
    data object HandshakeSent : AndroidProcessOutcome
    data object None : AndroidProcessOutcome
    /** @param targetMi null = root composite WinUSB install; non-null = MI-scoped interface install */
    data class ElevationRequired(val targetMi: Int?) : AndroidProcessOutcome
}

class AoaManager {
    var onAoaConnected: ((String) -> Unit)? = null
    var onAoaDisconnected: (() -> Unit)? = null
    var onInputReceived: ((GamepadInput) -> Unit)? = null
    
    // Notifies UI when WinUSB driver is required for the attached phone
    var onDriverNeedChanged: ((needed: Boolean, vid: Short?, pid: Short?, mi: Int?) -> Unit)? = null
    
    // Callback to trigger UAC prompt. Requires VID, PID, and optionally the specific MTP Interface Number.
    var onRequestElevation: ((Short, Short, Int?) -> Unit)? = null

    // Prevents endless elevation prompt loops when user cancels
    @Volatile var userDismissedElevation = false

    var lastDetectedPhoneName: String? = null

    private var activeFeedbackChannel: kotlinx.coroutines.channels.Channel<com.sanket.tools.nexpad.model.GamepadFeedback>? = null
    private var activeFileSyncChannel: kotlinx.coroutines.channels.Channel<ByteArray>? = null

    fun sendFeedback(feedback: com.sanket.tools.nexpad.model.GamepadFeedback) {
        activeFeedbackChannel?.trySend(feedback)
    }

    fun pushNxprc(componentId: String, bytes: ByteArray): Boolean {
        val ch = activeFileSyncChannel ?: return false
        val checksum = com.sanket.tools.nexpad.protocol.NexpadProtocol.computeCrc32(bytes)
        val header = com.sanket.tools.nexpad.protocol.NexpadProtocol.encodeFileSyncHeader(componentId, bytes.size, checksum)
        val fullPayload = header + bytes
        return ch.trySend(fullPayload).isSuccess
    }

    private var context: Context? = null
    @Volatile
    private var currentState: AoaState = AoaState.Idle

    fun onDriverInstallStarted() {
        println("[AOA/Manager] Driver installation started.")
        userDismissedElevation = false
        currentState = AoaState.InstallingDriver(System.currentTimeMillis())
    }

    fun onDriverInstallCompleted(success: Boolean) {
        println("[AOA/Manager] Driver installation finished (success=$success).")
        currentState = AoaState.Idle
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
    // Single Active Transport Guard: pause scanning when any other connection is active
    var isScanningPaused: () -> Boolean = { false }
    // Startup sync: wait for ADB initial probe to complete before evaluating USB candidates
    var isAdbInitialScanCompleted: () -> Boolean = { true }

    suspend fun scanAndConnect() = coroutineScope {
        if (context == null) return@coroutineScope
        println("[AOA/Manager] Starting continuous USB scanning loop...")

        // Grace period on startup: wait up to 2500ms for ADB initial probe to finish
        val startupWaitStart = System.currentTimeMillis()
        while (isActive && !isAdbInitialScanCompleted() && (System.currentTimeMillis() - startupWaitStart < 2500L)) {
            delay(100)
        }

        while (isActive) {
            // If another transport is active (Bluetooth, Wi-Fi, ADB) or scanning is paused, completely back off!
            if (isScanningPaused() || isAdbActive()) {
                onDriverNeedChanged?.invoke(false, null, null, null)
                delay(1500)
                continue
            }

            // While Windows is actively installing a driver, DO NOT query or open USB devices!
            // Probing device descriptors during driver installation causes PNP_VetoOutstandingOpen (-11 error)!
            if (currentState is AoaState.InstallingDriver) {
                val state = currentState as AoaState.InstallingDriver
                if (System.currentTimeMillis() - state.startTime > 90_000) {
                    println("[AOA/Manager] WinUSB installation timed out or user cancelled. Resetting to Idle state.")
                    currentState = AoaState.Idle
                }
                delay(1000)
                continue
            }

            val ctx = context ?: break
            val candidates = UsbDeviceDetector.findCandidates(ctx)
            
            try {
                // If handshake was recently sent, wait for phone to complete re-enumeration as 0x18D1
                if (currentState is AoaState.HandshakeSent) {
                    val hs = currentState as AoaState.HandshakeSent
                    val elapsed = System.currentTimeMillis() - hs.timestamp
                    val aoaFound = candidates.any { it is CandidateUsbDevice.AoaDevice }

                    if (aoaFound) {
                        println("[AOA/Manager] AOA Accessory re-enumeration detected! Proceeding to connect.")
                        currentState = AoaState.Idle
                    } else if (elapsed < 6000) {
                        // Phone is still rebooting its USB controller into accessory mode; do not spam START(53)
                        delay(1200)
                        continue
                    } else {
                        println("[AOA/Manager] AOA re-enumeration timed out after 6s. Resetting state.")
                        currentState = AoaState.Idle
                    }
                }

                val aoaCandidate = candidates.filterIsInstance<CandidateUsbDevice.AoaDevice>().firstOrNull()
                if (aoaCandidate != null) {
                    if (!aoaCandidate.friendlyName.isNullOrBlank()) {
                        lastDetectedPhoneName = aoaCandidate.friendlyName
                    }
                    val elevationRequired = processAoaDevice(aoaCandidate)
                    if (elevationRequired && !userDismissedElevation) {
                        onDriverNeedChanged?.invoke(true, aoaCandidate.vid, aoaCandidate.pid, null)
                    } else {
                        onDriverNeedChanged?.invoke(false, null, null, null)
                    }
                    delay(1000)
                    continue
                }

                val androidCandidate = candidates.filterIsInstance<CandidateUsbDevice.AndroidDevice>().firstOrNull()
                if (androidCandidate != null) {
                    if (!androidCandidate.friendlyName.isNullOrBlank()) {
                        lastDetectedPhoneName = androidCandidate.friendlyName
                    }
                    val outcome = processAndroidDevice(androidCandidate)
                    if (outcome is AndroidProcessOutcome.ElevationRequired && !userDismissedElevation) {
                        // Pass the MI determined by protocol behaviour — null means root composite install
                        onDriverNeedChanged?.invoke(true, androidCandidate.vid, androidCandidate.pid, outcome.targetMi)
                    } else {
                        onDriverNeedChanged?.invoke(false, null, null, null)
                    }
                    delay(2000)
                    continue
                }
                
                // No candidate needing driver
                onDriverNeedChanged?.invoke(false, null, null, null)
                delay(1000)
            } finally {
                // CRITICAL: Prevent memory leak of unused USB handles!
                candidates.forEach { it.unref() }
            }
        }
    }

    /**
     * Attempts to open [candidate] (an Android device in MTP mode) and send the AOA 2.0 handshake.
     *
     * Driver-selection logic is **entirely protocol-driven** — no vendor-ID list is used:
     *
     *  • LIBUSB_ERROR_NOT_SUPPORTED (-12): libusb cannot open the device at all because a proprietary
     *    OEM composite driver (e.g. Samsung ssudbus.sys, Vivo/OPPO MTP composite driver, etc.) owns
     *    the root hub node.  WinUSB must be installed on the ROOT composite device (targetMi = null).
     *    This is the universal, brand-agnostic signal — it applies to any OEM that ships its own
     *    composite USB driver on Windows.
     *
     *  • LIBUSB_ERROR_ACCESS (-3) / UsbOpenFailure.AccessDenied: libusb can see the device but a
     *    standard Windows UAC permission boundary prevents direct access.  Installing WinUSB scoped
     *    to the specific MTP interface (targetMi = mtpInterfaceNumber) is sufficient.
     */
    private suspend fun processAndroidDevice(candidate: CandidateUsbDevice.AndroidDevice): AndroidProcessOutcome {
        val handle = DeviceHandle()
        val openResult = LibUsb.open(candidate.device, handle)

        if (openResult == LibUsb.SUCCESS) {
            if (currentState is AoaState.Idle) {
                println("[AOA/Manager] Opened Android device [VID:${String.format("%04X", candidate.vid)} PID:${String.format("%04X", candidate.pid)}]")
            }
            LibUsb.setAutoDetachKernelDriver(handle, true)

            try {
                val handshakeOk = AoaHandshake.sendHandshake(handle, candidate.device)
                if (handshakeOk) {
                    currentState = AoaState.HandshakeSent(System.currentTimeMillis(), candidate.vid, candidate.pid)
                    return AndroidProcessOutcome.HandshakeSent
                }
                return AndroidProcessOutcome.None
            } catch (e: UsbHandshakeException) {
                if (currentState is AoaState.Idle) {
                    println("[AOA/Manager] Handshake exception: ${e.message}")
                }
                if (e.failure == UsbOpenFailure.AccessDenied) {
                    // Standard UAC access denial — scope WinUSB to the MTP interface if known.
                    return AndroidProcessOutcome.ElevationRequired(targetMi = candidate.mtpInterfaceNumber)
                }
                return AndroidProcessOutcome.None
            } finally {
                LibUsb.close(handle)
            }
        } else {
            // LIBUSB_ERROR_NOT_SUPPORTED (-12): a proprietary OEM composite driver is blocking libusb
            // at the root-composite level.  This is the universal signal (brand-agnostic) that the
            // WinUSB driver must replace the composite root — no MI scoping.
            if (openResult == LibUsb.ERROR_NOT_SUPPORTED) {
                println("[AOA/Manager] LibUsb.open returned ERROR_NOT_SUPPORTED for VID:${String.format("%04X", candidate.vid)} — proprietary composite driver detected. Requesting root WinUSB install.")
                return AndroidProcessOutcome.ElevationRequired(targetMi = null)
            }
            // LIBUSB_ERROR_ACCESS (-3): standard permission boundary — same as the caught exception path.
            if (openResult == LibUsb.ERROR_ACCESS) {
                return AndroidProcessOutcome.ElevationRequired(targetMi = candidate.mtpInterfaceNumber)
            }
            return AndroidProcessOutcome.None
        }
    }

    private suspend fun processAoaDevice(candidate: CandidateUsbDevice.AoaDevice): Boolean {
        val handle = DeviceHandle()
        val openResult = LibUsb.open(candidate.device, handle)
        
        if (openResult == LibUsb.SUCCESS) {
            println("[AOA/Manager] Opened AOA device [VID:${String.format("%04X", candidate.vid)} PID:${String.format("%04X", candidate.pid)}]")
            LibUsb.setAutoDetachKernelDriver(handle, true)
            
            var displayName = candidate.friendlyName 
                ?: lastDetectedPhoneName 
                ?: UsbDeviceDetector.cachedLastPhoneName 
                ?: "Android Phone"

            if (displayName == "Android Phone") {
                val desc = org.usb4java.DeviceDescriptor()
                if (LibUsb.getDeviceDescriptor(candidate.device, desc) == LibUsb.SUCCESS) {
                    val iProd = desc.iProduct()
                    if (iProd > 0) {
                        val sb = StringBuffer()
                        if (LibUsb.getStringDescriptorAscii(handle, iProd, sb) >= 0) {
                            val prodStr = sb.toString().trim()
                            if (prodStr.isNotBlank() && prodStr != "Android" && prodStr != "Nexpad Controller") {
                                displayName = prodStr
                                lastDetectedPhoneName = prodStr
                            }
                        }
                    }
                }
            }

            try {
                val feedbackChannel = kotlinx.coroutines.channels.Channel<com.sanket.tools.nexpad.model.GamepadFeedback>(kotlinx.coroutines.channels.Channel.CONFLATED)
                val fileSyncChannel = kotlinx.coroutines.channels.Channel<ByteArray>(kotlinx.coroutines.channels.Channel.UNLIMITED)
                activeFeedbackChannel = feedbackChannel
                activeFileSyncChannel = fileSyncChannel

                AoaTransport.startBulkStreaming(
                    handle = handle,
                    feedbackChannel = feedbackChannel,
                    fileSyncChannel = fileSyncChannel,
                    onSessionStarted = {
                        currentState = AoaState.Connected
                        userDismissedElevation = false
                        onAoaConnected?.invoke(displayName)
                    },
                    onSessionEnded = {
                        if (currentState is AoaState.Connected) {
                            currentState = AoaState.Idle
                            onAoaDisconnected?.invoke()
                        }
                    },
                    onInputReceived = { input ->
                        onInputReceived?.invoke(input)
                    }
                )
            } finally {
                activeFeedbackChannel = null
                activeFileSyncChannel = null
                LibUsb.close(handle)
                if (currentState is AoaState.Connected) {
                    onAoaDisconnected?.invoke()
                }
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
        context?.let {
            try {
                LibUsb.exit(it)
            } catch (_: Throwable) {}
        }
        context = null
    }
}
