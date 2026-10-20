package com.sanket.tools.nexpaddesktop.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.sanket.tools.nexpad.model.GamepadFeedback
import com.sanket.tools.nexpad.model.GamepadInput
import com.sanket.tools.nexpaddesktop.connection.ActiveTransport
import com.sanket.tools.nexpaddesktop.connection.DriverInstallState
import com.sanket.tools.nexpaddesktop.model.GyroSettings
import com.sanket.tools.nexpaddesktop.plugins.UniversalPushManager
import com.sanket.tools.nexpaddesktop.storage.DesktopSharedPreferences
import com.sanket.tools.nexpaddesktop.ui.ControllerType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Single source of truth ViewModel for NEXPAD Desktop.
 *
 * Maintains reactive Compose Snapshot State (`mutableStateOf`) for:
 * - Transport connection status and device metadata
 * - ViGEmBus driver connection and installation state
 * - Virtual gamepad controller selection and real-time input telemetry
 * - Motion/gyro smoothing and sensitivity configuration
 * - Persistent preferences via DesktopSharedPreferences
 *
 * This guarantees instant recomposition inside Navigation 3 (NavDisplay)
 * when phones connect/disconnect across all transports (USB ADB, USB AOA, Wi-Fi, Bluetooth).
 */
class DesktopViewModel(
    private val prefs: DesktopSharedPreferences = DesktopSharedPreferences.getInstance()
) {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var gyroSaveJob: Job? = null

    // ── Connection & Transport State ──
    var isDriverConnected by mutableStateOf(false)
    var connectedDeviceName by mutableStateOf<String?>(null)
    var activeTransport by mutableStateOf(ActiveTransport.NONE)
    var isAoaDriverNeeded by mutableStateOf(false)
    var driverInstallState by mutableStateOf(DriverInstallState.IDLE)
    var onInstallAoaDriver: () -> Unit = {}

    // ── Virtual Gamepad & Controller State ──
    var activeController by mutableStateOf(prefs.loadControllerType())
    var latestInput by mutableStateOf(GamepadInput())
    var latestFeedback by mutableStateOf(GamepadFeedback(leftMotorSpeed = 0, rightMotorSpeed = 0))
    var dsuClientCount by mutableStateOf(0)
    var isDsuServerEnabled by mutableStateOf(prefs.loadDsuEnabled())
    var isDeveloperOptionsEnabled by mutableStateOf(prefs.loadDeveloperOptionsEnabled())
    var streamingPort by mutableStateOf(prefs.getStreamingPort())
        private set

    var onStreamingPortChanged: ((Int) -> Unit)? = null
    var isWindowVisible by mutableStateOf(true)
    var onShowTrayNotification: ((title: String, message: String) -> Unit)? = null

    // ── Sensitivity & Motion State ──
    var gyroSettings by mutableStateOf(prefs.loadGyroSettings())
    var lsSensitivityX: Float
        get() = gyroSettings.lsSensitivityX
        set(value) { updateLsSensitivityX(value) }
    var lsSensitivityY: Float
        get() = gyroSettings.lsSensitivityY
        set(value) { updateLsSensitivityY(value) }
    var rsSensitivityX: Float
        get() = gyroSettings.rsSensitivityX
        set(value) { updateRsSensitivityX(value) }
    var rsSensitivityY: Float
        get() = gyroSettings.rsSensitivityY
        set(value) { updateRsSensitivityY(value) }
    var processedYaw by mutableStateOf(0f)
    var processedPitch by mutableStateOf(0f)
    var flightPitchDeg by mutableStateOf(0f)
    var flightRollDeg by mutableStateOf(0f)
    var flightYawDeg by mutableStateOf(0f)

    // ── Diagnostics & Actions ──
    var appError by mutableStateOf("")
    var onRecalibrate: () -> Unit = {}
    var onControllerChange: (ControllerType) -> Unit = { updateActiveController(it) }

    fun clearCallbacks() {
        onInstallAoaDriver = {}
        onShowTrayNotification = null
        onRecalibrate = {}
        onControllerChange = { updateActiveController(it) }
    }

    fun updateGyroSettings(newSettings: GyroSettings) {
        gyroSettings = newSettings
        gyroSaveJob?.cancel()
        gyroSaveJob = scope.launch {
            delay(300L)
            prefs.saveGyroSettings(newSettings)
        }
    }

    fun updateLsSensitivityX(value: Float) {
        val updated = gyroSettings.updateActiveStickSensitivities(lsX = value)
        updateGyroSettings(updated)
    }

    fun updateLsSensitivityY(value: Float) {
        val updated = gyroSettings.updateActiveStickSensitivities(lsY = value)
        updateGyroSettings(updated)
    }

    fun updateRsSensitivityX(value: Float) {
        val updated = gyroSettings.updateActiveStickSensitivities(rsX = value)
        updateGyroSettings(updated)
    }

    fun updateRsSensitivityY(value: Float) {
        val updated = gyroSettings.updateActiveStickSensitivities(rsY = value)
        updateGyroSettings(updated)
    }

    fun updateActiveController(controller: ControllerType) {
        activeController = controller
        prefs.saveControllerType(controller)
    }

    fun updateDsuServerEnabled(enabled: Boolean) {
        isDsuServerEnabled = enabled
        prefs.saveDsuEnabled(enabled)
    }

    fun updateDeveloperOptionsEnabled(enabled: Boolean) {
        isDeveloperOptionsEnabled = enabled
        prefs.saveDeveloperOptionsEnabled(enabled)
    }

    fun updateStreamingPort(newPort: Int) {
        if (newPort in 1024..65535) {
            streamingPort = newPort
            prefs.saveStreamingPort(newPort)
            onStreamingPortChanged?.invoke(newPort)
        }
    }

    fun minimizeToTray() {
        isWindowVisible = false
        onShowTrayNotification?.invoke(
            "NEXPAD Desktop",
            "NEXPAD is running in the background. Click the tray icon to restore."
        )
    }

    fun restoreWindow() {
        isWindowVisible = true
    }

    /**
     * Updates the connected device metadata and active transport.
     * Automatically clears [isAoaDriverNeeded] when a valid transport is established.
     */
    fun updateConnectedDevice(name: String?, transport: ActiveTransport) {
        connectedDeviceName = name
        activeTransport = transport
        UniversalPushManager.currentTransport = transport
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
            UniversalPushManager.currentTransport = ActiveTransport.NONE
        }
    }
}
