package com.sanket.tools.nexpaddesktop.model

import com.sanket.tools.nexpad.category.ControlKey
import com.sanket.tools.nexpad.model.GamepadInput
import com.sanket.tools.nexpad.model.NexpadKeys
import com.sanket.tools.nexpaddesktop.driver.DualShock4InputMapper
import com.sanket.tools.nexpaddesktop.driver.InputPipeline
import com.sanket.tools.nexpaddesktop.driver.ViGEmInputMapper
import com.sanket.tools.nexpaddesktop.driver.jna.XUSBReport
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

class ControllerEnumsTest {

    @Test
    fun testControllerTypePropertiesAndResolution() {
        assertEquals(ControllerType.XBOX_360, ControllerType.fromId("xbox"))
        assertEquals(ControllerType.XBOX_360, ControllerType.fromId("XBOX_360"))
        assertEquals(ControllerType.DUALSHOCK_4, ControllerType.fromId("ds4"))
        assertEquals(ControllerType.DUALSHOCK_4, ControllerType.fromId("DUALSHOCK_4"))
        assertEquals(ControllerType.DEFAULT, ControllerType.fromId(null))
        assertEquals(ControllerType.DEFAULT, ControllerType.fromId("unknown_mode"))

        assertTrue(ControllerType.XBOX_360.isXbox)
        assertFalse(ControllerType.XBOX_360.isDualShock4)
        assertTrue(ControllerType.DUALSHOCK_4.isDualShock4)
        assertFalse(ControllerType.DUALSHOCK_4.isXbox)
    }

    @Test
    fun testBuiltinComponentIds() {
        assertTrue(BuiltinComponentIds.isInbuildTouchpad("builtin.inbuild_ltp"))
        assertTrue(BuiltinComponentIds.isInbuildTouchpad("builtin.inbuild_rtp"))
        assertFalse(BuiltinComponentIds.isInbuildTouchpad("rc.custom_button"))

        assertTrue(BuiltinComponentIds.isSliderTrigger(BuiltinComponentIds.SLIDER_LT))
        assertTrue(BuiltinComponentIds.isSliderTrigger(BuiltinComponentIds.SLIDER_RT))
        assertTrue(BuiltinComponentIds.isSliderTrigger("custom_slider_left"))
        assertFalse(BuiltinComponentIds.isSliderTrigger("rc.button_a"))
    }

    @Test
    fun testControllerTouchClassifierSticksAndTouchpads() {
        // Joysticks
        assertTrue(ControllerTouchClassifier.isJoystick("LS"))
        assertTrue(ControllerTouchClassifier.isJoystick("RS"))
        assertTrue(ControllerTouchClassifier.isJoystick(null, "builtin.joystick_01"))
        assertFalse(ControllerTouchClassifier.isJoystick("A"))

        // Touchpads
        assertTrue(ControllerTouchClassifier.isTouchpad("LTP"))
        assertTrue(ControllerTouchClassifier.isTouchpad("RTP"))
        assertTrue(ControllerTouchClassifier.isTouchpad(null, "builtin.inbuild_ltp"))
        assertFalse(ControllerTouchClassifier.isTouchpad("B"))

        // Left vs Right touchpads
        assertTrue(ControllerTouchClassifier.isLeftTouchpad("LTP"))
        assertTrue(ControllerTouchClassifier.isLeftTouchpad(null, "builtin.inbuild_ltp"))
        assertFalse(ControllerTouchClassifier.isLeftTouchpad("RTP"))

        assertTrue(ControllerTouchClassifier.isRightTouchpad("RTP"))
        assertTrue(ControllerTouchClassifier.isRightTouchpad(null, "builtin.inbuild_rtp"))
        assertFalse(ControllerTouchClassifier.isRightTouchpad("LTP"))

        // Right stick or touchpad
        assertTrue(ControllerTouchClassifier.isRightStickOrTouchpad("RS"))
        assertTrue(ControllerTouchClassifier.isRightStickOrTouchpad("RTP"))
        assertTrue(ControllerTouchClassifier.isRightStickOrTouchpad(null, "custom_rtp_layer"))
        assertFalse(ControllerTouchClassifier.isRightStickOrTouchpad("LS"))

        // Non-button controls
        assertTrue(ControllerTouchClassifier.isNonButton("LS"))
        assertTrue(ControllerTouchClassifier.isNonButton("RS"))
        assertTrue(ControllerTouchClassifier.isNonButton("LTP"))
        assertTrue(ControllerTouchClassifier.isNonButton("RTP"))
        assertTrue(ControllerTouchClassifier.isNonButton("L3"))
        assertFalse(ControllerTouchClassifier.isNonButton("A"))

        // Non-sweepable
        assertTrue(ControllerTouchClassifier.isNonSweepable(ControlKey.START.key))
        assertTrue(ControllerTouchClassifier.isNonSweepable(ControlKey.BACK.key))
        assertTrue(ControllerTouchClassifier.isNonSweepable(ControlKey.GUIDE.key))
        assertFalse(ControllerTouchClassifier.isNonSweepable(ControlKey.A.key))
    }

    @Test
    fun testGamepadControlClassifierInputMatching() {
        // Triggers
        val ltActive = GamepadInput(triggerL2 = 0.5f)
        assertTrue(GamepadControlClassifier.isInputActive("LT", ltActive))
        assertTrue(GamepadControlClassifier.isInputActive("L2", ltActive))
        assertTrue(GamepadControlClassifier.isInputActive("LEFT_TRIGGER", ltActive))
        assertFalse(GamepadControlClassifier.isInputActive("RT", ltActive))

        val rtActive = GamepadInput(triggerR2 = 0.9f)
        assertTrue(GamepadControlClassifier.isInputActive("RT", rtActive))
        assertTrue(GamepadControlClassifier.isInputActive("R2", rtActive))
        assertTrue(GamepadControlClassifier.isInputActive("RIGHT_TRIGGER", rtActive))

        // Bumpers
        val lbActive = GamepadInput(btnL1 = true)
        assertTrue(GamepadControlClassifier.isInputActive("LB", lbActive))
        assertTrue(GamepadControlClassifier.isInputActive("L1", lbActive))
        assertTrue(GamepadControlClassifier.isInputActive("LEFT_BUMPER", lbActive))

        val rbActive = GamepadInput(btnR1 = true)
        assertTrue(GamepadControlClassifier.isInputActive("RB", rbActive))
        assertTrue(GamepadControlClassifier.isInputActive("R1", rbActive))

        // Face buttons & PS aliases
        val aActive = GamepadInput(btnA = true)
        assertTrue(GamepadControlClassifier.isInputActive("A", aActive))
        assertTrue(GamepadControlClassifier.isInputActive("CROSS", aActive))

        val bActive = GamepadInput(btnB = true)
        assertTrue(GamepadControlClassifier.isInputActive("B", bActive))
        assertTrue(GamepadControlClassifier.isInputActive("CIRCLE", bActive))

        val xActive = GamepadInput(btnX = true)
        assertTrue(GamepadControlClassifier.isInputActive("X", xActive))
        assertTrue(GamepadControlClassifier.isInputActive("SQUARE", xActive))

        val yActive = GamepadInput(btnY = true)
        assertTrue(GamepadControlClassifier.isInputActive("Y", yActive))
        assertTrue(GamepadControlClassifier.isInputActive("TRIANGLE", yActive))

        // Stick clicks
        val l3Active = GamepadInput(btnL3 = true)
        assertTrue(GamepadControlClassifier.isInputActive("LS", l3Active))
        assertTrue(GamepadControlClassifier.isInputActive("LSB", l3Active))
        assertTrue(GamepadControlClassifier.isInputActive("L3", l3Active))

        val r3Active = GamepadInput(btnR3 = true)
        assertTrue(GamepadControlClassifier.isInputActive("RS", r3Active))
        assertTrue(GamepadControlClassifier.isInputActive("RSB", r3Active))
        assertTrue(GamepadControlClassifier.isInputActive("R3", r3Active))

        // Empty activation buttons set defaults to always active
        assertTrue(GamepadControlClassifier.isActivationButtonPressed(emptySet(), GamepadInput()))

        // Gated activation buttons
        val aimGate = setOf("LT")
        assertFalse(GamepadControlClassifier.isActivationButtonPressed(aimGate, GamepadInput(triggerL2 = 0f)))
        assertTrue(GamepadControlClassifier.isActivationButtonPressed(aimGate, GamepadInput(triggerL2 = 0.5f)))
    }

    @Test
    fun testViGEmInputMapperZeroAllocationReuse() {
        val input = GamepadInput(
            btnA = true,
            btnB = true,
            triggerL2 = 0.8f,
            triggerR2 = 0.4f,
            leftStickX = 0.5f,
            leftStickY = -0.5f
        )

        val destination = XUSBReport()
        val returned = ViGEmInputMapper.map(input, destination)

        assertSame(destination, returned, "ViGEmInputMapper must mutate and return the passed destination instance")
        assertEquals((XUSBReport.A.toInt() or XUSBReport.B.toInt()).toShort(), destination.wButtons)
        assertEquals((0.8f * 255).toInt().toByte(), destination.bLeftTrigger)
        assertEquals((0.4f * 255).toInt().toByte(), destination.bRightTrigger)
    }

    @Test
    fun testDualShock4InputMapperZeroAllocationReuse() {
        val mapper = DualShock4InputMapper()
        val input = GamepadInput(
            btnA = true,
            btnX = true,
            triggerL2 = 1.0f,
            leftStickX = 1.0f
        )

        val report1 = mapper.map(input)
        assertNotNull(report1)

        val report2 = mapper.map(input)
        assertSame(report1, report2, "DualShock4InputMapper must reuse cached native report instance on hot path")
    }
}
