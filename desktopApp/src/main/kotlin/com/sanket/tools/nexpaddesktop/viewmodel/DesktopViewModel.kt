package com.sanket.tools.nexpaddesktop.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.sanket.tools.nexpad.model.GamepadInput
import com.sanket.tools.nexpaddesktop.connection.ActiveTransport
import com.sanket.tools.nexpaddesktop.connection.DriverInstallState
import com.sanket.tools.nexpaddesktop.model.GyroSettings
import com.sanket.tools.nexpaddesktop.ui.ControllerType

/**
 * Single source of truth ViewModel for NEXPAD Desktop.
 *
 * Maintains reactive Compose Snapshot State (`mutableStateOf`) for:
 * - Transport connection status and device metadata
 * - ViGEmBus driver connection and installation state
 * - Virtual gamepad controller selection and real-time input telemetry
 * - Motion/gyro smoothing and sensitivity configuration
 *
 * This guarantees instant recomposition inside Navigation 3 (NavDisplay)
 * when phones connect/disconnect across all transports (USB ADB, USB AOA, Wi-Fi, Bluetooth).
 */
class DesktopViewModel {

    // ── Connection & Transport State ──
    var isDriverConnected by mutableStateOf(false)
    var connectedDeviceName by mutableStateOf<String?>(null)
    var activeTransport by mutableStateOf(ActiveTransport.NONE)
    var isAoaDriverNeeded by mutableStateOf(false)
    var driverInstallState by mutableStateOf(DriverInstallState.IDLE)
    var onInstallAoaDriver: () -> Unit = {}

    // ── Virtual Gamepad & Controller State ──
    var activeController by mutableStateOf(ControllerType.XBOX_360)
    var latestInput by mutableStateOf(GamepadInput())
    var dsuClientCount by mutableStateOf(0)

    // ── Sensitivity & Motion State ──
    var lsSensitivityX by mutableStateOf(1.0f)
    var lsSensitivityY by mutableStateOf(1.0f)
    var rsSensitivityX by mutableStateOf(1.0f)
    var rsSensitivityY by mutableStateOf(1.0f)
    var gyroSettings by mutableStateOf(GyroSettings())
    var processedYaw by mutableStateOf(0f)
    var processedPitch by mutableStateOf(0f)

    // ── Diagnostics & Actions ──
    var appError by mutableStateOf("")
    var onRecalibrate: () -> Unit = {}
    var onControllerChange: (ControllerType) -> Unit = {}

    /**
     * Updates the connected device metadata and active transport.
     * Automatically clears [isAoaDriverNeeded] when a valid transport is established.
     */
    fun updateConnectedDevice(name: String?, transport: ActiveTransport) {
        connectedDeviceName = name
        activeTransport = transport
        if (transport != ActiveTransport.NONE) {
            isAoaDriverNeeded = false
        }
    }

    /**
     * Resets the active device if the disconnecting transport matches the currently active transport.
     */
    fun updateDisconnectedDevice(transport: ActiveTransport) {
        if (activeTransport == transport) {
            activeTransport = ActiveTransport.NONE
            connectedDeviceName = null
        }
    }
}
