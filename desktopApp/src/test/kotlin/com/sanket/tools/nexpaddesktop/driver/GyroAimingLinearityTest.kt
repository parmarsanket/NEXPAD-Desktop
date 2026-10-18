package com.sanket.tools.nexpaddesktop.driver

import com.sanket.tools.nexpad.model.GamepadInput
import com.sanket.tools.nexpad.protocol.NexpadProtocol
import com.sanket.tools.nexpaddesktop.model.*
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
            precisionAiming = PrecisionAimingSettings(
                horizontalAxis = HorizontalAxis.YAW,
                maxDpsYaw = 200.0f,
                accelerationEnabled = false,
                smoothingEnabled = false,
                deadzoneThreshold = 0f
            )
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
            precisionAiming = PrecisionAimingSettings(
                horizontalAxis = HorizontalAxis.YAW,
                maxDpsYaw = 200.0f,
                accelerationEnabled = false,
                stickResponseCurve = StickResponseCurve.LINEAR,
                antiDeadzone = 0.0f,
                smoothingEnabled = false,
                deadzoneThreshold = 0f
            )
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
            precisionAiming = PrecisionAimingSettings(
                horizontalAxis = HorizontalAxis.YAW,
                maxDpsYaw = 200.0f,
                accelerationEnabled = false,
                stickResponseCurve = StickResponseCurve.LINEARIZED,
                antiDeadzone = 0.0f,
                smoothingEnabled = false,
                deadzoneThreshold = 0f
            )
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
            precisionAiming = PrecisionAimingSettings(
                horizontalAxis = HorizontalAxis.ROLL,
                invertRoll = false,
                invertX = false,
                deadzoneThreshold = 0f,
                smoothingEnabled = false,
                accelerationEnabled = false
            )
        )
        val normalRollRes = processor.process(
            rawGyroX = 0f, rawGyroY = rollRadPerSec, rawGyroZ = 0f,
            settings = normalRollSettings
        )
        assertTrue("Normal roll should be positive", normalRollRes.yawDps > 0f)

        // 2. ROLL axis with invertRoll = true
        val invertedRollSettings = normalRollSettings.copy(precisionAiming = normalRollSettings.precisionAiming.copy(invertRoll = true))
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
            precisionAiming = PrecisionAimingSettings(
                horizontalAxis = HorizontalAxis.MIX,
                invertX = false,
                invertRoll = false,
                deadzoneThreshold = 0f,
                smoothingEnabled = false,
                accelerationEnabled = false
            )
        )
        val mixNormalRes = processor.process(
            rawGyroX = 0f, rawGyroY = rollRadPerSec, rawGyroZ = yawRadPerSec,
            settings = mixNormal
        )
        assertTrue("Mix with both positive should be positive", mixNormalRes.yawDps > 0f)

        // Invert Roll only in MIX: roll cancels yaw because hYaw is +50, hRoll is -50 -> sum = 0
        val mixInvertRoll = mixNormal.copy(precisionAiming = mixNormal.precisionAiming.copy(invertRoll = true))
        val mixInvertRollRes = processor.process(
            rawGyroX = 0f, rawGyroY = rollRadPerSec, rawGyroZ = yawRadPerSec,
            settings = mixInvertRoll
        )
        assertEquals("Inverted roll cancels yaw when magnitudes are identical in MIX mode", 0f, mixInvertRollRes.yawDps, 0.01f)
    }

    @Test
    fun testSteeringWheelFullLockUnconstrainedByPitchTilt() {
        val processor = GyroProcessor()
        val settings = GyroSettings(
            enabled = true,
            inputMode = InputMode.ABSOLUTE_TILT,
            steeringWheel = SteeringWheelSettings(
                targetStick = XboxTargetStick.LEFT_STICK,
                maxTiltAngle = 35.0f,
                sensitivityX = 1.0f,
                responseCurve = 1.0f,
                deadzoneThreshold = 0.0f
            )
        )

        // 35° steering lock on gravity: 9.8 * sin(35°) ≈ 5.62 m/s²
        val maxTurnGravityX = 5.62f
        // Player holds phone naturally tilted towards them (e.g. 35° viewing pitch angle):
        val viewingTiltGravityY = 5.62f

        // Case 1: Steer full right while phone has viewing pitch tilt (non-zero Y)
        val inputTiltedPhone = GamepadInput(
            gravityX = maxTurnGravityX,
            gravityY = viewingTiltGravityY,
            sensorFlags = NexpadProtocol.SENSOR_MODE_GRAVITY
        )
        val outputRight = InputPipeline.processInput(
            input = inputTiltedPhone,
            activeController = ControllerType.XBOX_360,
            gyroProcessor = processor,
            gyroSettings = settings,
            lsSensitivityX = 1f, lsSensitivityY = 1f,
            rsSensitivityX = 1f, rsSensitivityY = 1f,
            onProcessedAngles = { _, _ -> }
        )
        assertEquals("Virtual steering wheel must reach 100% full lock (1.0) on X even when phone Y tilt is non-zero",
            1.0f, outputRight.leftStickX, 0.01f)

        // Case 2: Steer full left while phone has viewing pitch tilt (non-zero Y)
        val inputTiltedPhoneLeft = GamepadInput(
            gravityX = -maxTurnGravityX,
            gravityY = viewingTiltGravityY,
            sensorFlags = NexpadProtocol.SENSOR_MODE_GRAVITY
        )
        val outputLeft = InputPipeline.processInput(
            input = inputTiltedPhoneLeft,
            activeController = ControllerType.XBOX_360,
            gyroProcessor = processor,
            gyroSettings = settings,
            lsSensitivityX = 1f, lsSensitivityY = 1f,
            rsSensitivityX = 1f, rsSensitivityY = 1f,
            onProcessedAngles = { _, _ -> }
        )
        assertEquals("Virtual steering wheel must reach 100% full lock (-1.0) on X when steered left",
            -1.0f, outputLeft.leftStickX, 0.01f)

        // Case 3: Phone held flat (Y == 0)
        val inputFlatPhone = GamepadInput(
            gravityX = maxTurnGravityX,
            gravityY = 0f,
            sensorFlags = NexpadProtocol.SENSOR_MODE_GRAVITY
        )
        val outputFlat = InputPipeline.processInput(
            input = inputFlatPhone,
            activeController = ControllerType.XBOX_360,
            gyroProcessor = processor,
            gyroSettings = settings,
            lsSensitivityX = 1f, lsSensitivityY = 1f,
            rsSensitivityX = 1f, rsSensitivityY = 1f,
            onProcessedAngles = { _, _ -> }
        )
        assertEquals("Steering wheel must also reach 100% (1.0) when Y is 0",
            1.0f, outputFlat.leftStickX, 0.01f)

        // Case 4: Tiny micro-angle steering near zero (e.g. 1° tilt = 0.0286 deflection)
        // Must NOT snap to 0.14 / -0.14; must smoothly output small numbers close to zero!
        val tinyTurnGravityX = 9.8f * kotlin.math.sin(1.0f * (Math.PI.toFloat() / 180f)) // ~0.171 m/s²
        val inputTinyTurn = GamepadInput(
            gravityX = tinyTurnGravityX,
            gravityY = viewingTiltGravityY,
            sensorFlags = NexpadProtocol.SENSOR_MODE_GRAVITY
        )
        val outputTiny = InputPipeline.processInput(
            input = inputTinyTurn,
            activeController = ControllerType.XBOX_360,
            gyroProcessor = processor,
            gyroSettings = settings,
            lsSensitivityX = 1f, lsSensitivityY = 1f,
            rsSensitivityX = 1f, rsSensitivityY = 1f,
            onProcessedAngles = { _, _ -> }
        )
        assertTrue("Steering stick must be able to output small values < 0.10 (actual: ${outputTiny.leftStickX})",
            outputTiny.leftStickX in 0.01f..0.06f)

        // Case 5: Tiny micro-angle left (-1° tilt)
        val inputTinyTurnLeft = GamepadInput(
            gravityX = -tinyTurnGravityX,
            gravityY = viewingTiltGravityY,
            sensorFlags = NexpadProtocol.SENSOR_MODE_GRAVITY
        )
        val outputTinyLeft = InputPipeline.processInput(
            input = inputTinyTurnLeft,
            activeController = ControllerType.XBOX_360,
            gyroProcessor = processor,
            gyroSettings = settings,
            lsSensitivityX = 1f, lsSensitivityY = 1f,
            rsSensitivityX = 1f, rsSensitivityY = 1f,
            onProcessedAngles = { _, _ -> }
        )
        assertTrue("Steering stick must be able to output small negative values > -0.10 (actual: ${outputTinyLeft.leftStickX})",
            outputTinyLeft.leftStickX in -0.06f..-0.01f)
    }

    @Test
    fun testDiagonalTiltFullSaturationFreeAxes() {
        val processor = GyroProcessor()
        val settings = GyroSettings(
            enabled = true,
            inputMode = InputMode.VELOCITY,
            precisionAiming = PrecisionAimingSettings(
                horizontalAxis = HorizontalAxis.YAW,
                maxDpsYaw = 200.0f,
                maxDpsPitch = 200.0f,
                accelerationEnabled = false,
                smoothingEnabled = false,
                deadzoneThreshold = 0f,
                antiDeadzone = 0.14f
            )
        )

        // Full 200 dps rotation on both Yaw (X) and Pitch (Y) -> Top-Right diagonal tilt
        val rad200 = 200.0f / GyroProcessor.RAD_TO_DEG
        // rawGyroX is pitch (negated to vertical), rawGyroZ is yaw
        val inputDiagonal = GamepadInput(
            gyroZ = rad200,
            gyroX = -rad200,
            sensorFlags = NexpadProtocol.SENSOR_MODE_IMU_6AXIS
        )

        val output = InputPipeline.processInput(
            input = inputDiagonal,
            activeController = ControllerType.XBOX_360,
            gyroProcessor = processor,
            gyroSettings = settings,
            lsSensitivityX = 1f, lsSensitivityY = 1f,
            rsSensitivityX = 1f, rsSensitivityY = 1f,
            onProcessedAngles = { _, _ -> }
        )

        assertEquals("Diagonal tilt must reach 1.0f full saturation on X without circular gating",
            1.0f, output.rightStickX, 0.01f)
        assertEquals("Diagonal tilt must reach 1.0f full saturation on Y without circular gating",
            1.0f, output.rightStickY, 0.01f)
    }

    @Test
    fun testDefaultZeroDeadzoneLinearPassThrough() {
        assertEquals("Default gameDeadzoneCompensation must be 0.0f", 0.0f, GyroSettings().gameDeadzoneCompensation, 0.0001f)
        assertEquals("GyroSettings.DEFAULT gameDeadzoneCompensation must be 0.0f", 0.0f, GyroSettings.DEFAULT.gameDeadzoneCompensation, 0.0001f)
        assertEquals("FPS_PRESET gameDeadzoneCompensation must be 0.0f", 0.0f, GyroSettings.FPS_PRESET.gameDeadzoneCompensation, 0.0001f)

        // Micro-aiming at 1.0°/s with default settings yields 1/200 = 0.005 without jumping to 0.14
        val processor = GyroProcessor()
        val defaultSettings = GyroSettings(
            enabled = true,
            inputMode = InputMode.VELOCITY,
            precisionAiming = PrecisionAimingSettings(
                horizontalAxis = HorizontalAxis.YAW,
                maxDpsYaw = 200.0f,
                accelerationEnabled = false,
                smoothingEnabled = false,
                deadzoneThreshold = 0f
            )
        )
        val rad1 = 1.0f / GyroProcessor.RAD_TO_DEG
        val inputSlow = GamepadInput(
            gyroZ = rad1,
            sensorFlags = NexpadProtocol.SENSOR_MODE_IMU_6AXIS
        )
        val outSlow = InputPipeline.processInput(
            input = inputSlow,
            activeController = ControllerType.XBOX_360,
            gyroProcessor = processor,
            gyroSettings = defaultSettings,
            lsSensitivityX = 1f, lsSensitivityY = 1f,
            rsSensitivityX = 1f, rsSensitivityY = 1f,
            onProcessedAngles = { _, _ -> }
        )
        // With 0.00 deadzone, output must smoothly pass through ~0.005 without snapping to 0.14!
        assertTrue("Output should be tiny micro-aiming (~0.005) without deadzone jump (actual: ${outSlow.rightStickX})",
            outSlow.rightStickX in 0.004f..0.01f)
    }

    @Test
    fun testCircularInnerDeadzoneDisc() {
        val processor = GyroProcessor()
        val settings = GyroSettings(
            enabled = true,
            inputMode = InputMode.VELOCITY,
            precisionAiming = PrecisionAimingSettings(
                horizontalAxis = HorizontalAxis.YAW,
                maxDpsYaw = 200.0f,
                maxDpsPitch = 200.0f,
                accelerationEnabled = false,
                smoothingEnabled = false,
                deadzoneThreshold = 0f,
                antiDeadzone = 0.14f
            )
        )

        // 1. Pure Horizontal motion (departure from center)
        val radTiny = 0.5f / GyroProcessor.RAD_TO_DEG
        val inputPureX = GamepadInput(
            gyroZ = radTiny,
            gyroX = 0f,
            sensorFlags = NexpadProtocol.SENSOR_MODE_IMU_6AXIS
        )
        val outPureX = InputPipeline.processInput(
            input = inputPureX,
            activeController = ControllerType.XBOX_360,
            gyroProcessor = processor,
            gyroSettings = settings,
            lsSensitivityX = 1f, lsSensitivityY = 1f,
            rsSensitivityX = 1f, rsSensitivityY = 1f,
            onProcessedAngles = { _, _ -> }
        )
        val magX = kotlin.math.sqrt(outPureX.rightStickX * outPureX.rightStickX + outPureX.rightStickY * outPureX.rightStickY)
        assertEquals("Pure X departure magnitude must be ~0.14 (radius = 0.14)", 0.14f, magX, 0.01f)

        // 2. Pure Vertical motion (departure from center)
        val inputPureY = GamepadInput(
            gyroZ = 0f,
            gyroX = -radTiny,
            sensorFlags = NexpadProtocol.SENSOR_MODE_IMU_6AXIS
        )
        val outPureY = InputPipeline.processInput(
            input = inputPureY,
            activeController = ControllerType.XBOX_360,
            gyroProcessor = processor,
            gyroSettings = settings,
            lsSensitivityX = 1f, lsSensitivityY = 1f,
            rsSensitivityX = 1f, rsSensitivityY = 1f,
            onProcessedAngles = { _, _ -> }
        )
        val magY = kotlin.math.sqrt(outPureY.rightStickX * outPureY.rightStickX + outPureY.rightStickY * outPureY.rightStickY)
        assertEquals("Pure Y departure magnitude must be ~0.14 (radius = 0.14)", 0.14f, magY, 0.01f)

        // 3. 45° Diagonal motion (departure from center)
        // In a circular deadzone of radius 0.14, the vector magnitude in diagonal direction must be 0.14,
        // which means (x ≈ 0.099, y ≈ 0.099). A square box would produce (x=0.14, y=0.14) with magnitude 0.198.
        val inputDiag = GamepadInput(
            gyroZ = radTiny,
            gyroX = -radTiny,
            sensorFlags = NexpadProtocol.SENSOR_MODE_IMU_6AXIS
        )
        val outDiag = InputPipeline.processInput(
            input = inputDiag,
            activeController = ControllerType.XBOX_360,
            gyroProcessor = processor,
            gyroSettings = settings,
            lsSensitivityX = 1f, lsSensitivityY = 1f,
            rsSensitivityX = 1f, rsSensitivityY = 1f,
            onProcessedAngles = { _, _ -> }
        )
        val magDiag = kotlin.math.sqrt(outDiag.rightStickX * outDiag.rightStickX + outDiag.rightStickY * outDiag.rightStickY)
        assertEquals("Diagonal departure magnitude must be circular (~0.14 radius, NOT square 0.198)",
            0.14f, magDiag, 0.015f)
        assertTrue("Diagonal components must be < 0.12 (actual: ${outDiag.rightStickX})",
            outDiag.rightStickX in 0.08f..0.12f)
        assertTrue("Diagonal components must be < 0.12 (actual: ${outDiag.rightStickY})",
            outDiag.rightStickY in 0.08f..0.12f)
    }
}
