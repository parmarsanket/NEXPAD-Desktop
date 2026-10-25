package com.sanket.tools.nexpaddesktop.driver

import com.sanket.tools.nexpaddesktop.model.ControllerType
import org.junit.Assert.*
import org.junit.Test

class GamepadDriverManagerTest {

    @Test
    fun `factory creates VirtualGamepadDriver for Xbox 360`() {
        val driver = GamepadDriverFactory.createDriver(ControllerType.XBOX_360) {}
        assertTrue(driver is VirtualGamepadDriver)
    }

    @Test
    fun `factory creates VirtualDualShock4Driver for DualShock 4`() {
        val driver = GamepadDriverFactory.createDriver(ControllerType.DUALSHOCK_4) {}
        assertTrue(driver is VirtualDualShock4Driver)
    }

    @Test
    fun `manager disconnect cleans up active driver reference`() {
        val manager = GamepadDriverManager {}
        assertNull(manager.activeDriver)
        assertFalse(manager.isDriverConnected)

        manager.disconnect()
        assertNull(manager.activeDriver)
    }
}
