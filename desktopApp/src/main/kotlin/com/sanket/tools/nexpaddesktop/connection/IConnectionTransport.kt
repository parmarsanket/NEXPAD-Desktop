package com.sanket.tools.nexpaddesktop.connection

import com.sanket.tools.nexpad.model.GamepadFeedback

/**
 * Common contract for low-level gamepad transport channels
 * (Wi-Fi UDP, USB Direct AOA, USB ADB Bridge, Bluetooth RFCOMM).
 */
interface IConnectionTransport {
    val transportType: ActiveTransport
    val isConnected: Boolean
    val connectedDeviceName: String?
    suspend fun sendFeedback(feedback: GamepadFeedback)
}
