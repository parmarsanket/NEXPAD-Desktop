package com.sanket.tools.nexpaddesktop.driver

import com.sanket.tools.nexpaddesktop.model.GamepadInput
import com.sanket.tools.nexpaddesktop.driver.jna.XUSBReport

object ViGEmInputMapper {
    private var lastL3 = false
    private var lastR3 = false

    fun map(input: GamepadInput): XUSBReport {
        val report = XUSBReport()
        var buttons: Short = 0
        
        if (input.dpadUp) buttons = (buttons.toInt() or XUSBReport.DPAD_UP.toInt()).toShort()
        if (input.dpadDown) buttons = (buttons.toInt() or XUSBReport.DPAD_DOWN.toInt()).toShort()
        if (input.dpadLeft) buttons = (buttons.toInt() or XUSBReport.DPAD_LEFT.toInt()).toShort()
        if (input.dpadRight) buttons = (buttons.toInt() or XUSBReport.DPAD_RIGHT.toInt()).toShort()
        if (input.btnStart) buttons = (buttons.toInt() or XUSBReport.START.toInt()).toShort()
        if (input.btnSelect) buttons = (buttons.toInt() or XUSBReport.BACK.toInt()).toShort()
        if (input.btnL3) buttons = (buttons.toInt() or XUSBReport.LEFT_THUMB.toInt()).toShort()
        if (input.btnR3) buttons = (buttons.toInt() or XUSBReport.RIGHT_THUMB.toInt()).toShort()
        
        if (input.btnL3 != lastL3) {
            println("NEXPAD_DEBUG Desktop: L3 changed to ${input.btnL3}")
            lastL3 = input.btnL3
        }
        if (input.btnR3 != lastR3) {
            println("NEXPAD_DEBUG Desktop: R3 changed to ${input.btnR3}")
            lastR3 = input.btnR3
        }
        if (input.btnL1) buttons = (buttons.toInt() or XUSBReport.LEFT_SHOULDER.toInt()).toShort()
        if (input.btnR1) buttons = (buttons.toInt() or XUSBReport.RIGHT_SHOULDER.toInt()).toShort()
        if (input.btnGuide) buttons = (buttons.toInt() or XUSBReport.GUIDE.toInt()).toShort()
        if (input.btnA) buttons = (buttons.toInt() or XUSBReport.A.toInt()).toShort()
        if (input.btnB) buttons = (buttons.toInt() or XUSBReport.B.toInt()).toShort()
        if (input.btnX) buttons = (buttons.toInt() or XUSBReport.X.toInt()).toShort()
        if (input.btnY) buttons = (buttons.toInt() or XUSBReport.Y.toInt()).toShort()

        report.wButtons = buttons
        report.bLeftTrigger = (input.triggerL2 * 255).toInt().toByte()
        report.bRightTrigger = (input.triggerR2 * 255).toInt().toByte()
        
        report.sThumbLX = (input.leftStickX * 32767).toInt().toShort()
        report.sThumbLY = (input.leftStickY * 32767).toInt().toShort() // XInput expects +32767 for UP
        report.sThumbRX = (input.rightStickX * 32767).toInt().toShort()
        report.sThumbRY = (input.rightStickY * 32767).toInt().toShort() // XInput expects +32767 for UP
        return report
    }
}
