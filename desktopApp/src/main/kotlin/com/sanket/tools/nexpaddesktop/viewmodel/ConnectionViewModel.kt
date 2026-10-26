package com.sanket.tools.nexpaddesktop.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.sanket.tools.nexpaddesktop.connection.ActiveTransport
import com.sanket.tools.nexpaddesktop.connection.DriverInstallState
import com.sanket.tools.nexpaddesktop.core.base.BaseViewModel
import com.sanket.tools.nexpaddesktop.plugins.UniversalPushManager

/**
 * ViewModel managing transport connectivity, ViGEmBus/AOA driver installation status,
 * and device discovery state.
 */
class ConnectionViewModel : BaseViewModel() {

    var isDriverConnected by mutableStateOf(false)
    var connectedDeviceName by mutableStateOf<String?>(null)
    var activeTransport by mutableStateOf(ActiveTransport.NONE)
    var isAoaDriverNeeded by mutableStateOf(false)

    var driverInstallState by mutableStateOf(DriverInstallState.IDLE)
    var driverInstallProgress by mutableStateOf(0f)
    var driverInstallStatusText by mutableStateOf("")

    var showDriverSuccessToast by mutableStateOf(false)
    var driverSuccessToastMessage by mutableStateOf("")
    var showDriverSuccessDialog by mutableStateOf(false)

    var onInstallAoaDriver: () -> Unit = {}
    var isViGEmInstalling by mutableStateOf(false)
    var vigemInstallProgress by mutableStateOf(0f)
    var vigemInstallStatusText by mutableStateOf("")
    var onInstallViGEmDriver: () -> Unit = {}

    fun dismissDriverSuccessToast() {
        showDriverSuccessToast = false
    }

    fun dismissDriverSuccessDialog() {
        showDriverSuccessDialog = false
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

    fun clearCallbacks() {
        onInstallAoaDriver = {}
    }
}
