package com.sanket.tools.nexpaddesktop.driver

import com.sanket.tools.nexpad.model.GamepadInput
import com.sanket.tools.nexpad.protocol.NexpadProtocol
import com.sanket.tools.nexpaddesktop.model.*
import com.sanket.tools.nexpaddesktop.ui.ControllerType
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Validates that physical analog stick base sensitivity (lsSensitivityX/Y, rsSensitivityX/Y)
 * is decoupled from motion processing:
 * 1. Physical stick inputs on phone are scaled by their base sensitivities.
 * 2. Motion modes (Precision Aiming, Steering Wheel, Flight Stick, Snap Turn) are NEVER scaled
 *    or cross-contaminated by physical stick base sensitivities.
 * 3. Additive blending sums scaled physical input with calibrated motion deflection cleanly.
 * 4. Target stick switching (RS <-> LS) preserves identical motion deflection regardless of
 *    physical stick sensitivity differences.
 */
class StickSensitivityDecouplingTest {

    @Test
    fun testPhysicalStickSensitivityScalesPhysicalStickInputs() {
        val processor = GyroProcessor()
        val settings = GyroSettings(enabled = false)

        val input = GamepadInput(
            leftStickX = 0.50f,
            leftStickY = -0.40f,
            rightStickX = 0.30f,
            rightStickY = 0.60f
        )

        val output = InputPipeline.processInput(
            input = input,
            activeController = ControllerType.XBOX_360,
            gyroProcessor = processor,
            gyroSettings = settings,
            lsSensitivityX = 1.5f,
            lsSensitivityY = 2.0f,
            rsSensitivityX = 2.5f,
            rsSensitivityY = 0.5f,
            onProcessedAngles = { _, _ -> }
        )

        assertEquals("Physical LS X must be scaled by 1.5x (0.50 * 1.5 = 0.75)", 0.75f, output.leftStickX, 0.001f)
        assertEquals("Physical LS Y must be scaled by 2.0x (-0.40 * 2.0 = -0.80)", -0.80f, output.leftStickY, 0.001f)
        assertEquals("Physical RS X must be scaled by 2.5x (0.30 * 2.5 = 0.75)", 0.75f, output.rightStickX, 0.001f)
        assertEquals("Physical RS Y must be scaled by 0.5x (0.60 * 0.5 = 0.30)", 0.30f, output.rightStickY, 0.001f)
    }

    @Test
    fun testMotionAimingIsImmuneToRightStickBaseSensitivity() {
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

        // 50 deg/s on 200 max dps -> exactly 0.25 stick deflection (25%)
        val radPerSec = 50.0f / GyroProcessor.RAD_TO_DEG
        val input = GamepadInput(gyroZ = radPerSec, sensorFlags = NexpadProtocol.SENSOR_MODE_IMU_6AXIS)

        // Baseline: rsSensitivityX = 1.0f
        val outputBase = InputPipeline.processInput(
            input = input,
            activeController = ControllerType.XBOX_360,
            gyroProcessor = processor,
            gyroSettings = settings,
            lsSensitivityX = 1f, lsSensitivityY = 1f,
            rsSensitivityX = 1.0f, rsSensitivityY = 1f,
            onProcessedAngles = { _, _ -> }
        )
        assertEquals("Baseline: 50 dps on 200 max dps must produce 0.25 deflection", 0.25f, outputBase.rightStickX, 0.01f)

        // User cranks physical RS sensitivity slider to 2.5x
        val outputBoostedRS = InputPipeline.processInput(
            input = input,
            activeController = ControllerType.XBOX_360,
            gyroProcessor = processor,
            gyroSettings = settings,
            lsSensitivityX = 1f, lsSensitivityY = 1f,
            rsSensitivityX = 2.5f, rsSensitivityY = 1f,
            onProcessedAngles = { _, _ -> }
        )
        assertEquals("Gyro aiming must REMAIN 0.25 deflection even when RS sensitivity is 2.5x", 0.25f, outputBoostedRS.rightStickX, 0.01f)

        // User lowers physical RS sensitivity slider to 0.2x
        val outputLoweredRS = InputPipeline.processInput(
            input = input,
            activeController = ControllerType.XBOX_360,
            gyroProcessor = processor,
            gyroSettings = settings,
            lsSensitivityX = 1f, lsSensitivityY = 1f,
            rsSensitivityX = 0.2f, rsSensitivityY = 1f,
            onProcessedAngles = { _, _ -> }
        )
        assertEquals("Gyro aiming must REMAIN 0.25 deflection even when RS sensitivity is 0.2x", 0.25f, outputLoweredRS.rightStickX, 0.01f)
    }

    @Test
    fun testTargetStickSwitchMaintainsIdenticalMotionDeflection() {
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

        val radPerSec = 50.0f / GyroProcessor.RAD_TO_DEG
        val input = GamepadInput(gyroZ = radPerSec, sensorFlags = NexpadProtocol.SENSOR_MODE_IMU_6AXIS)

        // Player has asymmetric base sensitivities: LS is 0.5x, RS is 2.5x
        val lsSens = 0.5f
        val rsSens = 2.5f

        // Gyro routed to Right Stick
        val outputRS = InputPipeline.processInput(
            input = input,
            activeController = ControllerType.XBOX_360,
            gyroProcessor = processor,
            gyroSettings = settings.copy(precisionAiming = settings.precisionAiming.copy(targetStick = XboxTargetStick.RIGHT_STICK)),
            lsSensitivityX = lsSens, lsSensitivityY = 1f,
            rsSensitivityX = rsSens, rsSensitivityY = 1f,
            onProcessedAngles = { _, _ -> }
        )

        // Gyro routed to Left Stick
        val outputLS = InputPipeline.processInput(
            input = input,
            activeController = ControllerType.XBOX_360,
            gyroProcessor = processor,
            gyroSettings = settings.copy(precisionAiming = settings.precisionAiming.copy(targetStick = XboxTargetStick.LEFT_STICK)),
            lsSensitivityX = lsSens, lsSensitivityY = 1f,
            rsSensitivityX = rsSens, rsSensitivityY = 1f,
            onProcessedAngles = { _, _ -> }
        )

        assertEquals("RS target deflection must be 0.25f", 0.25f, outputRS.rightStickX, 0.01f)
        assertEquals("LS target deflection must be 0.25f (identical to RS)", 0.25f, outputLS.leftStickX, 0.01f)
        assertEquals("Switching target stick must produce zero deflection divergence", outputRS.rightStickX, outputLS.leftStickX, 0.001f)
    }

    @Test
    fun testAdditiveBlendingCombinesScaledPhysicalAndCalibratedMotionCleanly() {
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
                deadzoneThreshold = 0f,
                targetStick = XboxTargetStick.RIGHT_STICK,
                blendMode = XboxBlendMode.ADDITIVE
            )
        )

        // Physical right stick is at 0.30f
        // Gyro motion is at 50 dps (0.25f deflection)
        val radPerSec = 50.0f / GyroProcessor.RAD_TO_DEG
        val input = GamepadInput(
            rightStickX = 0.30f,
            gyroZ = radPerSec,
            sensorFlags = NexpadProtocol.SENSOR_MODE_IMU_6AXIS
        )

        // RS Sensitivity is 1.5x -> physical contribution becomes 0.30 * 1.5 = 0.45f
        // Gyro contribution remains 0.25f
        // Expected blended output = 0.45f + 0.25f = 0.70f (NOT (0.30 + 0.25) * 1.5 = 0.825)
        val output = InputPipeline.processInput(
            input = input,
            activeController = ControllerType.XBOX_360,
            gyroProcessor = processor,
            gyroSettings = settings,
            lsSensitivityX = 1f, lsSensitivityY = 1f,
            rsSensitivityX = 1.5f, rsSensitivityY = 1f,
            onProcessedAngles = { _, _ -> }
        )

        assertEquals("Additive blend must sum scaled physical (0.45) and motion (0.25) to exactly 0.70",
            0.70f, output.rightStickX, 0.015f)
    }

    @Test
    fun testVirtualSteeringWheelImmuneToLeftStickSensitivity() {
        val processor = GyroProcessor()
        val settings = GyroSettings(
            enabled = true,
            inputMode = InputMode.ABSOLUTE_TILT,
            steeringWheel = SteeringWheelSettings(
                maxTiltAngle = 45f,
                deadzoneThreshold = 0f,
                responseCurve = 1.0f,
                targetStick = XboxTargetStick.LEFT_STICK,
                sensitivityX = 1.0f
            )
        )

        // 45 degree tilt -> gravityX = 9.8 * sin(45 deg) = 6.93 m/s²
        val fullTurnGravityX = 6.93f
        val input = GamepadInput(
            gravityX = fullTurnGravityX,
            sensorFlags = NexpadProtocol.SENSOR_MODE_GRAVITY
        )

        // User has LS base sensitivity set to 0.4x (for slow walking in RPGs)
        val output = InputPipeline.processInput(
            input = input,
            activeController = ControllerType.XBOX_360,
            gyroProcessor = processor,
            gyroSettings = settings,
            lsSensitivityX = 0.4f, lsSensitivityY = 1f,
            rsSensitivityX = 1f, rsSensitivityY = 1f,
            onProcessedAngles = { _, _ -> }
        )

        assertEquals("Virtual steering wheel must achieve full lock (1.0f) even if LS sensitivity is 0.4x",
            1.0f, output.leftStickX, 0.01f)
    }

    @Test
    fun testPrecisionAimingAndSteeringWheelSettingsNeverAffectEachOther() {
        val initialSettings = GyroSettings()

        // 1. Customize Precision Aiming:
        // Set target stick to LEFT_STICK, custom maxDpsYaw to 500f, blend mode to ADDITIVE
        val customAiming = initialSettings.copy(
            precisionAiming = initialSettings.precisionAiming.copy(
                targetStick = XboxTargetStick.LEFT_STICK,
                maxDpsYaw = 500.0f,
                blendMode = XboxBlendMode.ADDITIVE,
                deadzoneThreshold = 5.0f
            )
        )

        // Verify steeringWheel settings are 100% UNTOUCHED
        assertEquals("Steering wheel target stick must remain its own default (RIGHT_STICK)",
            XboxTargetStick.RIGHT_STICK, customAiming.steeringWheel.targetStick)
        assertEquals("Steering wheel sensitivityX must remain untouched at 1.0f",
            1.0f, customAiming.steeringWheel.sensitivityX, 0.001f)
        assertEquals("Steering wheel maxTiltAngle must remain untouched at 35.0f",
            35.0f, customAiming.steeringWheel.maxTiltAngle, 0.001f)
        assertEquals("Steering wheel blendMode must remain its own default (JOYSTICK_SUPREME)",
            XboxBlendMode.JOYSTICK_SUPREME, customAiming.steeringWheel.blendMode)

        // 2. Now customize Steering Wheel:
        // Set target stick to LEFT_STICK, sensitivityX to 2.5f, maxTiltAngle to 60f, blendMode to MUTE_ON_STICK
        val customBoth = customAiming.copy(
            steeringWheel = customAiming.steeringWheel.copy(
                targetStick = XboxTargetStick.LEFT_STICK,
                sensitivityX = 2.5f,
                maxTiltAngle = 60.0f,
                blendMode = XboxBlendMode.MUTE_ON_STICK
            )
        )

        // Verify precisionAiming settings are 100% UNTOUCHED
        assertEquals("Precision aiming targetStick must still be LEFT_STICK",
            XboxTargetStick.LEFT_STICK, customBoth.precisionAiming.targetStick)
        assertEquals("Precision aiming maxDpsYaw must still be 500.0f",
            500.0f, customBoth.precisionAiming.maxDpsYaw, 0.001f)
        assertEquals("Precision aiming blendMode must still be ADDITIVE",
            XboxBlendMode.ADDITIVE, customBoth.precisionAiming.blendMode)
        assertEquals("Precision aiming deadzoneThreshold must still be 5.0f",
            5.0f, customBoth.precisionAiming.deadzoneThreshold, 0.001f)

        // 3. Verify delegator properties resolve strictly according to active inputMode:
        val velocityMode = customBoth.copy(inputMode = InputMode.VELOCITY)
        assertEquals("In VELOCITY mode, xboxTargetStick must resolve to precisionAiming (LEFT_STICK)",
            XboxTargetStick.LEFT_STICK, velocityMode.xboxTargetStick)
        assertEquals("In VELOCITY mode, xboxBlendMode must resolve to precisionAiming (ADDITIVE)",
            XboxBlendMode.ADDITIVE, velocityMode.xboxBlendMode)

        val tiltMode = customBoth.copy(inputMode = InputMode.ABSOLUTE_TILT)
        assertEquals("In ABSOLUTE_TILT mode, xboxTargetStick must resolve to steeringWheel (LEFT_STICK)",
            XboxTargetStick.LEFT_STICK, tiltMode.xboxTargetStick)
        assertEquals("In ABSOLUTE_TILT mode, xboxBlendMode must resolve to steeringWheel (MUTE_ON_STICK)",
            XboxBlendMode.MUTE_ON_STICK, tiltMode.xboxBlendMode)
    }

    @Test
    fun testPipelineExecutionDecouplingBetweenPrecisionAimingAndSteeringWheel() {
        val processor = GyroProcessor()

        val settings = GyroSettings(
            enabled = true,
            precisionAiming = PrecisionAimingSettings(
                targetStick = XboxTargetStick.RIGHT_STICK,
                maxDpsYaw = 200.0f,
                horizontalAxis = HorizontalAxis.YAW,
                accelerationEnabled = false,
                stickResponseCurve = StickResponseCurve.LINEAR,
                antiDeadzone = 0f,
                smoothingEnabled = false,
                deadzoneThreshold = 0f
            ),
            steeringWheel = SteeringWheelSettings(
                targetStick = XboxTargetStick.LEFT_STICK,
                sensitivityX = 2.0f,
                maxTiltAngle = 45.0f,
                deadzoneThreshold = 0f,
                responseCurve = 1.0f
            )
        )

        // Case A: Mode is VELOCITY (Precision Aiming).
        val inputVelocity = GamepadInput(
            gyroZ = 50.0f / GyroProcessor.RAD_TO_DEG,
            gravityX = 4.9f,
            sensorFlags = NexpadProtocol.SENSOR_MODE_IMU_6AXIS
        )
        val outVelocity = InputPipeline.processInput(
            input = inputVelocity,
            activeController = ControllerType.XBOX_360,
            gyroProcessor = processor,
            gyroSettings = settings.copy(inputMode = InputMode.VELOCITY),
            lsSensitivityX = 1f, lsSensitivityY = 1f,
            rsSensitivityX = 1f, rsSensitivityY = 1f,
            onProcessedAngles = { _, _ -> }
        )

        // Precision Aiming maps to RIGHT_STICK at 50/200 = 0.25f
        assertEquals("Precision Aiming must output 0.25f on Right Stick", 0.25f, outVelocity.rightStickX, 0.01f)
        // Left stick must remain 0f - Steering Wheel gravity tilt MUST NOT leak into VELOCITY mode!
        assertEquals("Left stick must remain untouched (0.0f) in VELOCITY mode", 0.0f, outVelocity.leftStickX, 0.001f)

        // Case B: Mode is ABSOLUTE_TILT (Virtual Steering Wheel).
        val inputTilt = GamepadInput(
            gyroZ = 50.0f / GyroProcessor.RAD_TO_DEG,
            gravityX = 4.9f,
            sensorFlags = NexpadProtocol.SENSOR_MODE_GRAVITY
        )
        val outTilt = InputPipeline.processInput(
            input = inputTilt,
            activeController = ControllerType.XBOX_360,
            gyroProcessor = processor,
            gyroSettings = settings.copy(inputMode = InputMode.ABSOLUTE_TILT),
            lsSensitivityX = 1f, lsSensitivityY = 1f,
            rsSensitivityX = 1f, rsSensitivityY = 1f,
            onProcessedAngles = { _, _ -> }
        )

        // Steering wheel maps to LEFT_STICK
        assertEquals("Steering wheel must output on Left Stick", 1.0f, outTilt.leftStickX, 0.05f)
        // Right stick must remain 0f - Gyro yaw must NOT leak into ABSOLUTE_TILT mode!
        assertEquals("Right stick must remain untouched (0.0f) in ABSOLUTE_TILT mode", 0.0f, outTilt.rightStickX, 0.001f)
    }

    @Test
    fun testChangingPrecisionAimingSensitivityDoesNotAffectSteeringWheel() {
        val processor = GyroProcessor()

        val baseSettings = GyroSettings(
            enabled = true,
            inputMode = InputMode.ABSOLUTE_TILT,
            precisionAiming = PrecisionAimingSettings(
                maxDpsYaw = 200.0f
            ),
            steeringWheel = SteeringWheelSettings(
                targetStick = XboxTargetStick.LEFT_STICK,
                sensitivityX = 1.0f,
                maxTiltAngle = 45.0f,
                deadzoneThreshold = 0f,
                responseCurve = 1.0f
            )
        )

        val input = GamepadInput(
            gravityX = 3.5f,
            sensorFlags = NexpadProtocol.SENSOR_MODE_GRAVITY
        )

        val outBase = InputPipeline.processInput(
            input = input,
            activeController = ControllerType.XBOX_360,
            gyroProcessor = processor,
            gyroSettings = baseSettings,
            lsSensitivityX = 1f, lsSensitivityY = 1f,
            rsSensitivityX = 1f, rsSensitivityY = 1f,
            onProcessedAngles = { _, _ -> }
        )

        // Drastically change precision aiming sensitivity and target stick:
        val modifiedSettings = baseSettings.copy(
            precisionAiming = baseSettings.precisionAiming.copy(
                maxDpsYaw = 50.0f,
                maxDpsPitch = 50.0f,
                targetStick = XboxTargetStick.LEFT_STICK,
                blendMode = XboxBlendMode.ADDITIVE
            )
        )

        val outModified = InputPipeline.processInput(
            input = input,
            activeController = ControllerType.XBOX_360,
            gyroProcessor = processor,
            gyroSettings = modifiedSettings,
            lsSensitivityX = 1f, lsSensitivityY = 1f,
            rsSensitivityX = 1f, rsSensitivityY = 1f,
            onProcessedAngles = { _, _ -> }
        )

        assertEquals("Steering wheel output must be 100% identical regardless of changes to precision aiming settings",
            outBase.leftStickX, outModified.leftStickX, 0.0001f)
    }

    @Test
    fun testChangingSteeringWheelSensitivityDoesNotAffectPrecisionAiming() {
        val processor = GyroProcessor()

        val baseSettings = GyroSettings(
            enabled = true,
            inputMode = InputMode.VELOCITY,
            precisionAiming = PrecisionAimingSettings(
                targetStick = XboxTargetStick.RIGHT_STICK,
                maxDpsYaw = 200.0f,
                horizontalAxis = HorizontalAxis.YAW,
                accelerationEnabled = false,
                stickResponseCurve = StickResponseCurve.LINEAR,
                antiDeadzone = 0f,
                smoothingEnabled = false,
                deadzoneThreshold = 0f
            ),
            steeringWheel = SteeringWheelSettings(
                targetStick = XboxTargetStick.RIGHT_STICK,
                sensitivityX = 1.0f
            )
        )

        val input = GamepadInput(
            gyroZ = 50.0f / GyroProcessor.RAD_TO_DEG,
            sensorFlags = NexpadProtocol.SENSOR_MODE_IMU_6AXIS
        )

        val outBase = InputPipeline.processInput(
            input = input,
            activeController = ControllerType.XBOX_360,
            gyroProcessor = processor,
            gyroSettings = baseSettings,
            lsSensitivityX = 1f, lsSensitivityY = 1f,
            rsSensitivityX = 1f, rsSensitivityY = 1f,
            onProcessedAngles = { _, _ -> }
        )

        // Drastically change steering wheel settings (sensitivity 3.0x, tilt angle, target stick, etc.)
        val modifiedSettings = baseSettings.copy(
            steeringWheel = baseSettings.steeringWheel.copy(
                sensitivityX = 3.0f,
                sensitivityY = 3.0f,
                maxTiltAngle = 10.0f,
                targetStick = XboxTargetStick.LEFT_STICK,
                blendMode = XboxBlendMode.ADDITIVE
            )
        )

        val outModified = InputPipeline.processInput(
            input = input,
            activeController = ControllerType.XBOX_360,
            gyroProcessor = processor,
            gyroSettings = modifiedSettings,
            lsSensitivityX = 1f, lsSensitivityY = 1f,
            rsSensitivityX = 1f, rsSensitivityY = 1f,
            onProcessedAngles = { _, _ -> }
        )

        assertEquals("Precision Aiming output must be 100% identical regardless of changes to steering wheel settings",
            outBase.rightStickX, outModified.rightStickX, 0.0001f)
    }

    @Test
    fun testEachModeEncapsulatesOwnStickSensitivities() {
        val initialSettings = GyroSettings()

        // 1. Configure Precision Aiming with its own stick sensitivities
        val aimingMode = initialSettings.copy(
            inputMode = InputMode.VELOCITY
        ).updateActiveStickSensitivities(
            lsX = 0.85f, lsY = 0.90f, rsX = 1.65f, rsY = 1.40f
        )

        assertEquals(0.85f, aimingMode.precisionAiming.lsSensitivityX, 0.001f)
        assertEquals(0.90f, aimingMode.precisionAiming.lsSensitivityY, 0.001f)
        assertEquals(1.65f, aimingMode.precisionAiming.rsSensitivityX, 0.001f)
        assertEquals(1.40f, aimingMode.precisionAiming.rsSensitivityY, 0.001f)

        // Verify Steering Wheel stick sensitivities remain untouched defaults
        assertEquals(1.00f, aimingMode.steeringWheel.lsSensitivityX, 0.001f)
        assertEquals(1.00f, aimingMode.steeringWheel.rsSensitivityX, 0.001f)

        // 2. Switch to Virtual Steering Wheel and configure its own stick sensitivities
        val steeringMode = aimingMode.copy(
            inputMode = InputMode.ABSOLUTE_TILT
        ).updateActiveStickSensitivities(
            lsX = 0.50f, lsY = 0.60f, rsX = 1.10f, rsY = 1.10f
        )

        // Verify Steering Wheel received its new sensitivities
        assertEquals(0.50f, steeringMode.steeringWheel.lsSensitivityX, 0.001f)
        assertEquals(0.60f, steeringMode.steeringWheel.lsSensitivityY, 0.001f)
        assertEquals(1.10f, steeringMode.steeringWheel.rsSensitivityX, 0.001f)
        assertEquals(1.10f, steeringMode.steeringWheel.rsSensitivityY, 0.001f)

        // Verify Precision Aiming sensitivities were NEVER modified
        assertEquals(0.85f, steeringMode.precisionAiming.lsSensitivityX, 0.001f)
        assertEquals(0.90f, steeringMode.precisionAiming.lsSensitivityY, 0.001f)
        assertEquals(1.65f, steeringMode.precisionAiming.rsSensitivityX, 0.001f)
        assertEquals(1.40f, steeringMode.precisionAiming.rsSensitivityY, 0.001f)

        // 3. Switch back to Velocity mode: dynamic accessors resolve directly to Precision Aiming
        val backToAiming = steeringMode.copy(inputMode = InputMode.VELOCITY)
        assertEquals(0.85f, backToAiming.lsSensitivityX, 0.001f)
        assertEquals(1.65f, backToAiming.rsSensitivityX, 0.001f)

        // Switch to Absolute Tilt: dynamic accessors resolve directly to Steering Wheel
        val backToTilt = steeringMode.copy(inputMode = InputMode.ABSOLUTE_TILT)
        assertEquals(0.50f, backToTilt.lsSensitivityX, 0.001f)
        assertEquals(1.10f, backToTilt.rsSensitivityX, 0.001f)

        // 4. InputPipeline automatically applies the mode-specific stick sensitivities without manual overrides
        val processor = GyroProcessor()
        val stickInput = GamepadInput(leftStickX = 1.0f, rightStickX = 0.5f)

        // In Velocity mode: LS is scaled by 0.85, RS is scaled by 1.65 (0.5 * 1.65 = 0.825)
        val outAiming = InputPipeline.processInput(
            input = stickInput,
            activeController = ControllerType.XBOX_360,
            gyroProcessor = processor,
            gyroSettings = backToAiming.copy(enabled = false),
            onProcessedAngles = { _, _ -> }
        )
        assertEquals(0.85f, outAiming.leftStickX, 0.01f)
        assertEquals(0.825f, outAiming.rightStickX, 0.01f)

        // In Steering Wheel mode: LS is scaled by 0.50, RS is scaled by 1.10 (0.5 * 1.10 = 0.55)
        val outSteering = InputPipeline.processInput(
            input = stickInput,
            activeController = ControllerType.XBOX_360,
            gyroProcessor = processor,
            gyroSettings = backToTilt.copy(enabled = false),
            onProcessedAngles = { _, _ -> }
        )
        assertEquals(0.50f, outSteering.leftStickX, 0.01f)
        assertEquals(0.55f, outSteering.rightStickX, 0.01f)
    }
}
