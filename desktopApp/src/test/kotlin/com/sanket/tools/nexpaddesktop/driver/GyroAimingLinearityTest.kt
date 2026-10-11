package com.sanket.tools.nexpaddesktop.driver

import com.sanket.tools.nexpad.model.GamepadInput
import com.sanket.tools.nexpad.protocol.NexpadProtocol
import com.sanket.tools.nexpaddesktop.model.GyroSettings
import com.sanket.tools.nexpaddesktop.model.HorizontalAxis
import com.sanket.tools.nexpaddesktop.model.InputMode
import com.sanket.tools.nexpaddesktop.model.StickResponseCurve
import com.sanket.tools.nexpaddesktop.ui.ControllerType
import org.junit.Assert.*
import org.junit.Test
import kotlin.math.abs

/**
 * Validates that gyro aiming adheres to JoyShockLibrary & Natural Sensitivity Scale standards:
 * - Linear 1:1 proportionality across slow (micro-aiming) and fast (flicking) movements.
 * - Zero exponential sensitivity blowups when acceleration is disabled (default).
 * - Stick response curve options (Linear, Linearized, Precision) operate correctly.
 */
class GyroAimingLinearityTest {

    @Test
    fun testPureLinearProportionalityAcrossSlowAndFastSpeeds() {
        val processor = GyroProcessor()
        val settings = GyroSettings(
            enabled = true,
            inputMode = InputMode.VELOCITY,
            horizontalAxis = HorizontalAxis.YAW,
            maxDpsYaw = 200.0f,
            accelerationEnabled = false,
            smoothingEnabled = false,
            deadzoneThreshold = 0f
        )

        // 1. Slow micro-movement: 20 deg/s (10% of max 200 dps)
        val slowRadPerSec = 20.0f / GyroProcessor.RAD_TO_DEG
        val slowResult = processor.process(
            rawGyroX = 0f, rawGyroY = 0f, rawGyroZ = slowRadPerSec,
            settings = settings
        )

        // 2. Medium movement: 100 deg/s (50% of max 200 dps)
        val medRadPerSec = 100.0f / GyroProcessor.RAD_TO_DEG
        val medResult = processor.process(
            rawGyroX = 0f, rawGyroY = 0f, rawGyroZ = medRadPerSec,
            settings = settings
        )

        // 3. Fast flick movement: 200 deg/s (100% of max 200 dps)
        val fastRadPerSec = 200.0f / GyroProcessor.RAD_TO_DEG
        val fastResult = processor.process(
            rawGyroX = 0f, rawGyroY = 0f, rawGyroZ = fastRadPerSec,
            settings = settings
        )

        // Ratio of medium to slow MUST be exactly 5.0 (100 / 20 = 5.0)
        val medToSlowRatio = medResult.yawDps / slowResult.yawDps
        assertEquals("Speed scaling from 20 to 100 dps must be exactly linear (5.0x)", 5.0f, medToSlowRatio, 0.05f)

        // Ratio of fast to slow MUST be exactly 10.0 (200 / 20 = 10.0)
        val fastToSlowRatio = fastResult.yawDps / slowResult.yawDps
        assertEquals("Speed scaling from 20 to 200 dps must be exactly linear (10.0x)", 10.0f, fastToSlowRatio, 0.05f)
    }

    @Test
    fun testInputPipelineLinearStickOutput() {
        val processor = GyroProcessor()
        val settings = GyroSettings(
            enabled = true,
            inputMode = InputMode.VELOCITY,
            horizontalAxis = HorizontalAxis.YAW,
            maxDpsYaw = 200.0f,
            accelerationEnabled = false,
            stickResponseCurve = StickResponseCurve.LINEAR,
            gameDeadzoneCompensation = 0.0f, // Test pure stick mapping without offset
            smoothingEnabled = false,
            deadzoneThreshold = 0f
        )

        // 50 deg/s at 200 dps max -> exactly 0.25 stick deflection (25%)
        val radPerSec = 50.0f / GyroProcessor.RAD_TO_DEG
        val input = GamepadInput(gyroZ = radPerSec, sensorFlags = NexpadProtocol.SENSOR_MODE_IMU_6AXIS)

        val output = InputPipeline.processInput(
            input = input,
            activeController = ControllerType.XBOX_360,
            gyroProcessor = processor,
            gyroSettings = settings,
            lsSensitivityX = 1f, lsSensitivityY = 1f,
            rsSensitivityX = 1f, rsSensitivityY = 1f,
            onProcessedAngles = { _, _ -> }
        )

        assertEquals("50 dps on 200 dps max must produce exactly 0.25 stick deflection", 0.25f, output.rightStickX, 0.01f)
    }

    @Test
    fun testLinearizedResponseCurveCompensatesPowerCurvedGames() {
        val processor = GyroProcessor()
        val settings = GyroSettings(
            enabled = true,
            inputMode = InputMode.VELOCITY,
            horizontalAxis = HorizontalAxis.YAW,
            maxDpsYaw = 200.0f,
            accelerationEnabled = false,
            stickResponseCurve = StickResponseCurve.LINEARIZED, // exponent = 0.72
            gameDeadzoneCompensation = 0.0f,
            smoothingEnabled = false,
            deadzoneThreshold = 0f
        )

        // 20 deg/s at 200 dps max -> raw is 0.10. With exponent 0.72: 0.10^0.72 ≈ 0.190
        val radPerSec = 20.0f / GyroProcessor.RAD_TO_DEG
        val input = GamepadInput(gyroZ = radPerSec, sensorFlags = NexpadProtocol.SENSOR_MODE_IMU_6AXIS)

        val output = InputPipeline.processInput(
            input = input,
            activeController = ControllerType.XBOX_360,
            gyroProcessor = processor,
            gyroSettings = settings,
            lsSensitivityX = 1f, lsSensitivityY = 1f,
            rsSensitivityX = 1f, rsSensitivityY = 1f,
            onProcessedAngles = { _, _ -> }
        )

        // Linearized boost at low speeds counteracts games with aggressive x^2 stick curve
        assertTrue("Linearized curve must elevate low speed stick deflection above raw 0.10", output.rightStickX > 0.15f)
        assertTrue("Linearized curve must remain below 0.30", output.rightStickX < 0.30f)
    }

    @Test
    fun testFpsPresetHasAccelerationDisabledByDefault() {
        val fps = GyroSettings.FPS_PRESET
        assertFalse("FPS Preset must have acceleration disabled by default for 1:1 muscle memory", fps.accelerationEnabled)
        assertEquals("FPS Preset must use Linear response curve by default", StickResponseCurve.LINEAR, fps.stickResponseCurve)
    }

    @Test
    fun testInvertRollInVelocityAndMixModes() {
        val processor = GyroProcessor()
        val rollRadPerSec = 50.0f / GyroProcessor.RAD_TO_DEG
        val yawRadPerSec = 50.0f / GyroProcessor.RAD_TO_DEG

        // 1. ROLL axis with invertRoll = false
        val normalRollSettings = GyroSettings(
            enabled = true,
            inputMode = InputMode.VELOCITY,
            horizontalAxis = HorizontalAxis.ROLL,
            invertRoll = false,
            invertX = false,
            deadzoneThreshold = 0f,
            smoothingEnabled = false,
            accelerationEnabled = false
        )
        val normalRollRes = processor.process(
            rawGyroX = 0f, rawGyroY = rollRadPerSec, rawGyroZ = 0f,
            settings = normalRollSettings
        )
        assertTrue("Normal roll should be positive", normalRollRes.yawDps > 0f)

        // 2. ROLL axis with invertRoll = true
        val invertedRollSettings = normalRollSettings.copy(invertRoll = true)
        val invertedRollRes = processor.process(
            rawGyroX = 0f, rawGyroY = rollRadPerSec, rawGyroZ = 0f,
            settings = invertedRollSettings
        )
        assertTrue("Inverted roll should be negative", invertedRollRes.yawDps < 0f)
        assertEquals(normalRollRes.yawDps, -invertedRollRes.yawDps, 0.01f)

        // 3. MIX axis with independent invertRoll
        val mixNormal = GyroSettings(
            enabled = true,
            inputMode = InputMode.VELOCITY,
            horizontalAxis = HorizontalAxis.MIX,
            invertX = false,
            invertRoll = false,
            deadzoneThreshold = 0f,
            smoothingEnabled = false,
            accelerationEnabled = false
        )
        val mixNormalRes = processor.process(
            rawGyroX = 0f, rawGyroY = rollRadPerSec, rawGyroZ = yawRadPerSec,
            settings = mixNormal
        )
        assertTrue("Mix with both positive should be positive", mixNormalRes.yawDps > 0f)

        // Invert Roll only in MIX: roll cancels yaw because hYaw is +50, hRoll is -50 -> sum = 0
        val mixInvertRoll = mixNormal.copy(invertRoll = true)
        val mixInvertRollRes = processor.process(
            rawGyroX = 0f, rawGyroY = rollRadPerSec, rawGyroZ = yawRadPerSec,
            settings = mixInvertRoll
        )
        assertEquals("Inverted roll cancels yaw when magnitudes are identical in MIX mode", 0f, mixInvertRollRes.yawDps, 0.01f)
    }
}
