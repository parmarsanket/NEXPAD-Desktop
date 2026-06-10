package com.sanket.tools.nexpaddesktop.driver

import com.sanket.tools.nexpaddesktop.model.GamepadInput

/**
 * Scalable interface for gamepad drivers (ViGEmBus, vJoy, etc.)
 * This enables Phase 2 architecture.
 */
interface IGamepadDriver {
    fun connect()
    fun disconnect()
    fun updateInput(input: GamepadInput)
    fun simulateCrash()
    fun isDriverConnected(): Boolean
}
