package com.sanket.tools.nexpaddesktop.driver

import com.sanket.tools.nexpad.model.GamepadFeedback
import com.sanket.tools.nexpad.model.GamepadInput
import com.sanket.tools.nexpaddesktop.model.ControllerType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Manages the lifecycle, background connection polling, and input updating
 * for the virtual gamepad driver ([VirtualGamepadDriver] or [VirtualDualShock4Driver]).
 */
class GamepadDriverManager(
    private val onRumble: (GamepadFeedback) -> Unit
) {
    @Volatile
    var activeDriver: IGamepadDriver? = null
        private set

    private var connectionJob: Job? = null

    val isDriverConnected: Boolean
        get() = activeDriver?.isDriverConnected() ?: false

    /**
     * Switches the active virtual controller to [controllerType], cleanly disconnecting
     * the old driver, instantiating the new one, and initiating connection.
     */
    fun switchController(
        controllerType: ControllerType,
        scope: CoroutineScope,
        onStateChanged: (Boolean) -> Unit = {}
    ) {
        connectionJob?.cancel()
        connectionJob = null

        activeDriver?.disconnect()
        activeDriver = null

        val driver = GamepadDriverFactory.createDriver(controllerType, onRumble)
        try {
            driver.connect()
        } catch (e: Exception) {
            println("⚠️ [GamepadDriverManager] Initial connect failed: ${e.message}")
        }
        activeDriver = driver
        onStateChanged(driver.isDriverConnected())

        // If driver is not yet connected (e.g. ViGEmBus still initializing), poll in background
        connectionJob = scope.launch(Dispatchers.IO) {
            while (!driver.isDriverConnected()) {
                delay(2000)
                try {
                    driver.connect()
                } catch (_: Exception) {}
                onStateChanged(driver.isDriverConnected())
            }
        }
    }

    /**
     * Submits a processed input packet to the native gamepad driver.
     */
    fun updateInput(input: GamepadInput) {
        activeDriver?.updateInput(input)
    }

    /**
     * Recalibrates internal hardware offsets or gyro bias.
     */
    fun recalibrate() {
        activeDriver?.recalibrate()
    }

    /**
     * Disconnects and cleans up the active driver session.
     */
    fun disconnect() {
        connectionJob?.cancel()
        connectionJob = null
        activeDriver?.disconnect()
        activeDriver = null
    }
}
