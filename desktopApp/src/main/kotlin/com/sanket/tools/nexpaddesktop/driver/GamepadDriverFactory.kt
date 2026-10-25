package com.sanket.tools.nexpaddesktop.driver

import com.sanket.tools.nexpad.model.GamepadFeedback
import com.sanket.tools.nexpaddesktop.model.ControllerType

/**
 * Factory pattern for creating polymorphic [IGamepadDriver] instances.
 */
object GamepadDriverFactory {
    fun createDriver(
        controllerType: ControllerType,
        onRumble: (GamepadFeedback) -> Unit
    ): IGamepadDriver {
        return when (controllerType) {
            ControllerType.XBOX_360 -> VirtualGamepadDriver(onRumble = onRumble)
            ControllerType.DUALSHOCK_4 -> VirtualDualShock4Driver(onRumble = onRumble)
        }
    }
}
