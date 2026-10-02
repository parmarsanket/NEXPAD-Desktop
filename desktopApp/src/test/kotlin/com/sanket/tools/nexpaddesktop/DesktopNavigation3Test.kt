package com.sanket.tools.nexpaddesktop

import androidx.compose.runtime.mutableStateListOf
import androidx.navigation3.runtime.NavKey
import com.sanket.tools.nexpaddesktop.ui.*
import org.junit.Assert.*
import org.junit.Test

class DesktopNavigation3Test {

    @Test
    fun testInitialBackStackState() {
        val backStack = mutableStateListOf<NavKey>(DesktopScreenKey.Home)
        val navigator = DesktopNav3Navigator(backStack)

        assertEquals(1, navigator.backStack.size)
        assertEquals(DesktopScreenKey.Home, navigator.currentKey)
        assertEquals(Screen.HOME, navigator.currentKey.toScreen())
    }

    @Test
    fun testNavigateTypedKey() {
        val backStack = mutableStateListOf<NavKey>(DesktopScreenKey.Home)
        val navigator = DesktopNav3Navigator(backStack)

        navigator.navigate(DesktopScreenKey.Controller)
        assertEquals(2, navigator.backStack.size)
        assertEquals(DesktopScreenKey.Controller, navigator.currentKey)
        assertEquals(Screen.CONTROLLER, navigator.currentKey.toScreen())

        navigator.navigate(DesktopScreenKey.Plugins)
        assertEquals(3, navigator.backStack.size)
        assertEquals(DesktopScreenKey.Plugins, navigator.currentKey)

        // Duplicate navigation to the active top destination should be a no-op
        navigator.navigate(DesktopScreenKey.Plugins)
        assertEquals(3, navigator.backStack.size)
    }

    @Test
    fun testNavigateLegacyScreenEnum() {
        val backStack = mutableStateListOf<NavKey>(DesktopScreenKey.Home)
        val navigator = DesktopNav3Navigator(backStack)

        navigator.navigate(Screen.OUTPUT)
        assertEquals(2, navigator.backStack.size)
        assertEquals(DesktopScreenKey.Output, navigator.currentKey)
        assertEquals(Screen.OUTPUT, navigator.currentKey.toScreen())
    }

    @Test
    fun testPopBackStack() {
        val backStack = mutableStateListOf<NavKey>(DesktopScreenKey.Home)
        val navigator = DesktopNav3Navigator(backStack)

        navigator.navigate(DesktopScreenKey.Controller)
        navigator.navigate(DesktopScreenKey.Plugins)
        assertEquals(3, navigator.backStack.size)

        val popped1 = navigator.popBackStack()
        assertTrue(popped1)
        assertEquals(DesktopScreenKey.Controller, navigator.currentKey)

        val popped2 = navigator.popBackStack()
        assertTrue(popped2)
        assertEquals(DesktopScreenKey.Home, navigator.currentKey)

        // Cannot pop root Home destination
        val poppedRoot = navigator.popBackStack()
        assertFalse(poppedRoot)
        assertEquals(1, navigator.backStack.size)
        assertEquals(DesktopScreenKey.Home, navigator.currentKey)
    }

    @Test
    fun testBidirectionalScreenKeyMapping() {
        for (screen in Screen.values()) {
            val key = screen.toNavKey()
            assertEquals(screen, key.toScreen())
        }
    }

    @Test
    fun testDesktopViewModelInitialState() {
        val vm = com.sanket.tools.nexpaddesktop.viewmodel.DesktopViewModel()
        assertFalse(vm.isDriverConnected)
        assertNull(vm.connectedDeviceName)
        assertEquals(com.sanket.tools.nexpaddesktop.connection.ActiveTransport.NONE, vm.activeTransport)
        assertFalse(vm.isAoaDriverNeeded)
        assertEquals(com.sanket.tools.nexpaddesktop.ui.ControllerType.XBOX_360, vm.activeController)
    }

    @Test
    fun testDesktopViewModelUpdateConnectedDevice() {
        val vm = com.sanket.tools.nexpaddesktop.viewmodel.DesktopViewModel()
        vm.isAoaDriverNeeded = true

        vm.updateConnectedDevice("moto g85 5G", com.sanket.tools.nexpaddesktop.connection.ActiveTransport.USB_ADB)
        assertEquals("moto g85 5G", vm.connectedDeviceName)
        assertEquals(com.sanket.tools.nexpaddesktop.connection.ActiveTransport.USB_ADB, vm.activeTransport)
        // Establishing active transport automatically clears isAoaDriverNeeded
        assertFalse(vm.isAoaDriverNeeded)
    }

    @Test
    fun testDesktopViewModelUpdateDisconnectedDevice() {
        val vm = com.sanket.tools.nexpaddesktop.viewmodel.DesktopViewModel()
        vm.updateConnectedDevice("moto g85 5G", com.sanket.tools.nexpaddesktop.connection.ActiveTransport.USB_ADB)

        // Disconnecting a DIFFERENT transport should not clear the active USB_ADB connection
        vm.updateDisconnectedDevice(com.sanket.tools.nexpaddesktop.connection.ActiveTransport.BLUETOOTH)
        assertEquals("moto g85 5G", vm.connectedDeviceName)
        assertEquals(com.sanket.tools.nexpaddesktop.connection.ActiveTransport.USB_ADB, vm.activeTransport)

        // Disconnecting the ACTIVE transport clears the device state
        vm.updateDisconnectedDevice(com.sanket.tools.nexpaddesktop.connection.ActiveTransport.USB_ADB)
        assertNull(vm.connectedDeviceName)
        assertEquals(com.sanket.tools.nexpaddesktop.connection.ActiveTransport.NONE, vm.activeTransport)
    }
}
