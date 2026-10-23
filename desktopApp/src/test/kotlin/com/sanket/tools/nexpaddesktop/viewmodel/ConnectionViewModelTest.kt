package com.sanket.tools.nexpaddesktop.viewmodel

import com.sanket.tools.nexpaddesktop.connection.ActiveTransport
import com.sanket.tools.nexpaddesktop.connection.DriverInstallState
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ConnectionViewModelTest {

    private lateinit var viewModel: ConnectionViewModel

    @Before
    fun setUp() {
        viewModel = ConnectionViewModel()
    }

    @Test
    fun testInitialState() {
        assertFalse(viewModel.isDriverConnected)
        assertNull(viewModel.connectedDeviceName)
        assertEquals(ActiveTransport.NONE, viewModel.activeTransport)
        assertEquals(DriverInstallState.IDLE, viewModel.driverInstallState)
    }

    @Test
    fun testUpdateConnectedDevice() {
        viewModel.isAoaDriverNeeded = true
        viewModel.updateConnectedDevice("Pixel 8 Pro", ActiveTransport.WIFI)

        assertEquals("Pixel 8 Pro", viewModel.connectedDeviceName)
        assertEquals(ActiveTransport.WIFI, viewModel.activeTransport)
        assertFalse(viewModel.isAoaDriverNeeded)
    }

    @Test
    fun testUpdateDisconnectedDevice() {
        viewModel.updateConnectedDevice("Pixel 8 Pro", ActiveTransport.WIFI)
        assertEquals(ActiveTransport.WIFI, viewModel.activeTransport)

        // Disconnecting a different transport should not clear state
        viewModel.updateDisconnectedDevice(ActiveTransport.BLUETOOTH)
        assertEquals(ActiveTransport.WIFI, viewModel.activeTransport)

        // Disconnecting the active transport should clear state
        viewModel.updateDisconnectedDevice(ActiveTransport.WIFI)
        assertEquals(ActiveTransport.NONE, viewModel.activeTransport)
        assertNull(viewModel.connectedDeviceName)
    }

    @Test
    fun testDriverToastAndDialogDismiss() {
        viewModel.showDriverSuccessToast = true
        viewModel.showDriverSuccessDialog = true

        viewModel.dismissDriverSuccessToast()
        assertFalse(viewModel.showDriverSuccessToast)

        viewModel.dismissDriverSuccessDialog()
        assertFalse(viewModel.showDriverSuccessDialog)
    }
}
