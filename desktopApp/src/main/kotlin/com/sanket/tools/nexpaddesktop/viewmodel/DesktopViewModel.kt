package com.sanket.tools.nexpaddesktop.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.sanket.tools.nexpad.model.GamepadFeedback
import com.sanket.tools.nexpad.model.GamepadInput
import com.sanket.tools.nexpaddesktop.connection.ActiveTransport
import com.sanket.tools.nexpaddesktop.connection.DriverInstallState
import com.sanket.tools.nexpaddesktop.core.base.BaseViewModel
import com.sanket.tools.nexpaddesktop.data.repository.GyroRepositoryImpl
import com.sanket.tools.nexpaddesktop.data.repository.PortRepositoryImpl
import com.sanket.tools.nexpaddesktop.data.repository.SettingsRepositoryImpl
import com.sanket.tools.nexpaddesktop.model.GyroSettings
import com.sanket.tools.nexpaddesktop.storage.DesktopSharedPreferences
import com.sanket.tools.nexpaddesktop.ui.ControllerType

/**
 * Composite orchestrator ViewModel for NEXPAD Desktop.
 *
 * Coordinates domain sub-ViewModels:
 * - [HomeViewModel]: Controller selection, DSU server, developer mode, tray visibility
 * - [GyroViewModel]: Motion/gyro sensitivity, processing parameters, and flight telemetry
 * - [ConnectionViewModel]: Transport connection state, device discovery, driver installation
 * - [PortViewModel]: Network port configuration and validation
 *
 * Exposes backwards-compatible properties and delegates for seamless integration
 * with existing Navigation 3 screens and testing harnesses.
 */
class DesktopViewModel(
    prefs: DesktopSharedPreferences = DesktopSharedPreferences.getInstance(),
    val homeViewModel: HomeViewModel = HomeViewModel(SettingsRepositoryImpl(prefs)),
    val gyroViewModel: GyroViewModel = GyroViewModel(GyroRepositoryImpl(prefs)),
    val connectionViewModel: ConnectionViewModel = ConnectionViewModel(),
    val portViewModel: PortViewModel = PortViewModel(PortRepositoryImpl(prefs))
) : BaseViewModel() {

    // ── Connection & Transport Delegation ──
    var isDriverConnected: Boolean
        get() = connectionViewModel.isDriverConnected
        set(value) { connectionViewModel.isDriverConnected = value }

    var connectedDeviceName: String?
        get() = connectionViewModel.connectedDeviceName
        set(value) { connectionViewModel.connectedDeviceName = value }

    var activeTransport: ActiveTransport
        get() = connectionViewModel.activeTransport
        set(value) { connectionViewModel.activeTransport = value }

    var isAoaDriverNeeded: Boolean
        get() = connectionViewModel.isAoaDriverNeeded
        set(value) { connectionViewModel.isAoaDriverNeeded = value }

    var driverInstallState: DriverInstallState
        get() = connectionViewModel.driverInstallState
        set(value) { connectionViewModel.driverInstallState = value }

    var driverInstallProgress: Float
        get() = connectionViewModel.driverInstallProgress
        set(value) { connectionViewModel.driverInstallProgress = value }

    var driverInstallStatusText: String
        get() = connectionViewModel.driverInstallStatusText
        set(value) { connectionViewModel.driverInstallStatusText = value }

    var showDriverSuccessToast: Boolean
        get() = connectionViewModel.showDriverSuccessToast
        set(value) { connectionViewModel.showDriverSuccessToast = value }

    var driverSuccessToastMessage: String
        get() = connectionViewModel.driverSuccessToastMessage
        set(value) { connectionViewModel.driverSuccessToastMessage = value }

    var showDriverSuccessDialog: Boolean
        get() = connectionViewModel.showDriverSuccessDialog
        set(value) { connectionViewModel.showDriverSuccessDialog = value }

    var onInstallAoaDriver: () -> Unit
        get() = connectionViewModel.onInstallAoaDriver
        set(value) { connectionViewModel.onInstallAoaDriver = value }

    fun dismissDriverSuccessToast() = connectionViewModel.dismissDriverSuccessToast()
    fun dismissDriverSuccessDialog() = connectionViewModel.dismissDriverSuccessDialog()
    fun updateConnectedDevice(name: String?, transport: ActiveTransport) =
        connectionViewModel.updateConnectedDevice(name, transport)
    fun updateDisconnectedDevice(transport: ActiveTransport) =
        connectionViewModel.updateDisconnectedDevice(transport)

    // ── Virtual Gamepad & Home Delegation ──
    var activeController: ControllerType
        get() = homeViewModel.activeController
        set(value) { homeViewModel.activeController = value }

    var latestInput by mutableStateOf(GamepadInput())
    var latestFeedback by mutableStateOf(GamepadFeedback(leftMotorSpeed = 0, rightMotorSpeed = 0))

    var dsuClientCount: Int
        get() = homeViewModel.dsuClientCount
        set(value) { homeViewModel.dsuClientCount = value }

    var isDsuServerEnabled: Boolean
        get() = homeViewModel.isDsuServerEnabled
        set(value) { homeViewModel.isDsuServerEnabled = value }

    var isDeveloperOptionsEnabled: Boolean
        get() = homeViewModel.isDeveloperOptionsEnabled
        set(value) { homeViewModel.isDeveloperOptionsEnabled = value }

    var isWindowVisible: Boolean
        get() = homeViewModel.isWindowVisible
        set(value) { homeViewModel.isWindowVisible = value }

    var appError: String
        get() = homeViewModel.appError
        set(value) { homeViewModel.appError = value }

    var onShowTrayNotification: ((title: String, message: String) -> Unit)?
        get() = homeViewModel.onShowTrayNotification
        set(value) { homeViewModel.onShowTrayNotification = value }

    var onControllerChange: (ControllerType) -> Unit
        get() = homeViewModel.onControllerChange
        set(value) { homeViewModel.onControllerChange = value }

    fun updateActiveController(controller: ControllerType) = homeViewModel.updateActiveController(controller)
    fun updateDsuServerEnabled(enabled: Boolean) = homeViewModel.updateDsuServerEnabled(enabled)
    fun updateDeveloperOptionsEnabled(enabled: Boolean) = homeViewModel.updateDeveloperOptionsEnabled(enabled)
    fun minimizeToTray() = homeViewModel.minimizeToTray()
    fun restoreWindow() = homeViewModel.restoreWindow()

    // ── Port Delegation ──
    val streamingPort: Int
        get() = portViewModel.streamingPort

    var onStreamingPortChanged: ((Int) -> Unit)?
        get() = portViewModel.onStreamingPortChanged
        set(value) { portViewModel.onStreamingPortChanged = value }

    fun updateStreamingPort(newPort: Int) = portViewModel.updateStreamingPort(newPort)

    // ── Sensitivity & Motion Delegation ──
    var gyroSettings: GyroSettings
        get() = gyroViewModel.gyroSettings
        set(value) { gyroViewModel.gyroSettings = value }

    var lsSensitivityX: Float
        get() = gyroViewModel.lsSensitivityX
        set(value) { gyroViewModel.lsSensitivityX = value }

    var lsSensitivityY: Float
        get() = gyroViewModel.lsSensitivityY
        set(value) { gyroViewModel.lsSensitivityY = value }

    var rsSensitivityX: Float
        get() = gyroViewModel.rsSensitivityX
        set(value) { gyroViewModel.rsSensitivityX = value }

    var rsSensitivityY: Float
        get() = gyroViewModel.rsSensitivityY
        set(value) { gyroViewModel.rsSensitivityY = value }

    var processedYaw: Float
        get() = gyroViewModel.processedYaw
        set(value) { gyroViewModel.processedYaw = value }

    var processedPitch: Float
        get() = gyroViewModel.processedPitch
        set(value) { gyroViewModel.processedPitch = value }

    var flightPitchDeg: Float
        get() = gyroViewModel.flightPitchDeg
        set(value) { gyroViewModel.flightPitchDeg = value }

    var flightRollDeg: Float
        get() = gyroViewModel.flightRollDeg
        set(value) { gyroViewModel.flightRollDeg = value }

    var flightYawDeg: Float
        get() = gyroViewModel.flightYawDeg
        set(value) { gyroViewModel.flightYawDeg = value }

    var onRecalibrate: () -> Unit
        get() = gyroViewModel.onRecalibrate
        set(value) { gyroViewModel.onRecalibrate = value }

    fun updateGyroSettings(newSettings: GyroSettings) = gyroViewModel.updateGyroSettings(newSettings)
    fun updateLsSensitivityX(value: Float) = gyroViewModel.updateLsSensitivityX(value)
    fun updateLsSensitivityY(value: Float) = gyroViewModel.updateLsSensitivityY(value)
    fun updateRsSensitivityX(value: Float) = gyroViewModel.updateRsSensitivityX(value)
    fun updateRsSensitivityY(value: Float) = gyroViewModel.updateRsSensitivityY(value)

    fun flushSettings() = gyroViewModel.flushSettings()

    fun clearCallbacks() {
        homeViewModel.clearCallbacks()
        gyroViewModel.clearCallbacks()
        connectionViewModel.clearCallbacks()
        portViewModel.clearCallbacks()
    }

    override fun onCleared() {
        super.onCleared()
        homeViewModel.onCleared()
        gyroViewModel.onCleared()
        connectionViewModel.onCleared()
        portViewModel.onCleared()
    }
}
