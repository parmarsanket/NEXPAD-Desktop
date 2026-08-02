package com.sanket.tools.nexpaddesktop.driver

import com.sanket.tools.nexpaddesktop.model.GamepadInput
import com.sanket.tools.nexpaddesktop.driver.jna.XUSBReport

/**
 * The Translation Layer for Xbox 360 virtual controllers.
 *
 * This object is a stateless mapper responsible for converting the high-level, human-readable 
 * [GamepadInput] received from the Android network packet into the low-level, densely packed 
 * [XUSBReport] required by the Windows XInput API.
 * 
 * ### Mapping Constraints:
 * * **Analog Sticks**: XInput uses 16-bit signed integers (`-32768` to `32767`). 
 *   The Android app sends floating-point values from `-1.0` to `1.0`. We multiply by `32767`.
 * * **Analog Triggers**: XInput uses 8-bit unsigned integers (`0` to `255`).
 *   The Android app sends floats from `0.0` to `1.0`. We multiply by `255`.
 * * **Buttons**: 14 boolean flags are compressed into a single 16-bit `wButtons` integer 
 *   using bitwise OR (`or`) operations.
 */
object ViGEmInputMapper {

    /**
     * Translates a generic network payload into a native C memory block.
     * 
     * @param input The deserialized network payload from the Android controller.
     * @return An [XUSBReport] fully populated and ready for [ViGEmClientLibrary.vigem_target_x360_update].
     */
    fun map(input: GamepadInput): XUSBReport {
        val report = XUSBReport()
        var buttons: Short = 0
        
        // ---------------------------------------------------------------------
        // 1. Bitwise Button Packing
        // If a button is pressed (true), we flip the corresponding bit to 1.
        // ---------------------------------------------------------------------
        if (input.dpadUp) buttons = (buttons.toInt() or XUSBReport.DPAD_UP.toInt()).toShort()
        if (input.dpadDown) buttons = (buttons.toInt() or XUSBReport.DPAD_DOWN.toInt()).toShort()
        if (input.dpadLeft) buttons = (buttons.toInt() or XUSBReport.DPAD_LEFT.toInt()).toShort()
        if (input.dpadRight) buttons = (buttons.toInt() or XUSBReport.DPAD_RIGHT.toInt()).toShort()
        
        if (input.btnStart) buttons = (buttons.toInt() or XUSBReport.START.toInt()).toShort()
        if (input.btnSelect) buttons = (buttons.toInt() or XUSBReport.BACK.toInt()).toShort()
        if (input.btnGuide) buttons = (buttons.toInt() or XUSBReport.GUIDE.toInt()).toShort()
        
        if (input.btnL3) buttons = (buttons.toInt() or XUSBReport.LEFT_THUMB.toInt()).toShort()
        if (input.btnR3) buttons = (buttons.toInt() or XUSBReport.RIGHT_THUMB.toInt()).toShort()
        if (input.btnL1) buttons = (buttons.toInt() or XUSBReport.LEFT_SHOULDER.toInt()).toShort()
        if (input.btnR1) buttons = (buttons.toInt() or XUSBReport.RIGHT_SHOULDER.toInt()).toShort()
        
        if (input.btnA) buttons = (buttons.toInt() or XUSBReport.A.toInt()).toShort()
        if (input.btnB) buttons = (buttons.toInt() or XUSBReport.B.toInt()).toShort()
        if (input.btnX) buttons = (buttons.toInt() or XUSBReport.X.toInt()).toShort()
        if (input.btnY) buttons = (buttons.toInt() or XUSBReport.Y.toInt()).toShort()

        // ---------------------------------------------------------------------
        // 2. Commit structure values
        // ---------------------------------------------------------------------
        report.wButtons = buttons
        
        // Triggers map from [0f .. 1f] to [0 .. 255]
        report.bLeftTrigger = (input.triggerL2 * 255).toInt().toByte()
        report.bRightTrigger = (input.triggerR2 * 255).toInt().toByte()
        
        // Joysticks map from [-1f .. 1f] to [-32768 .. 32767]
        // Note: XInput expects positive Y values when pushing the stick UP.
        report.sThumbLX = (input.leftStickX * 32767).toInt().toShort()
        report.sThumbLY = (input.leftStickY * 32767).toInt().toShort() 
        report.sThumbRX = (input.rightStickX * 32767).toInt().toShort()
        report.sThumbRY = (input.rightStickY * 32767).toInt().toShort() 
        
        return report
    }
}
