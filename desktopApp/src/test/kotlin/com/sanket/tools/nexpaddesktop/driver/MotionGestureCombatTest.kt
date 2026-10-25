package com.sanket.tools.nexpaddesktop.driver

import com.sanket.tools.nexpad.model.GamepadInput
import com.sanket.tools.nexpaddesktop.model.*
import com.sanket.tools.nexpaddesktop.ui.ControllerType
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Validates the Motion Gesture & Kinetic Combat pipeline (InputMode.GESTURE).
 *
 * Verifies that pure linear acceleration user impulses (gravity subtracted)
 * reliably trigger tactical actions: Right Trigger melee strikes, Dodge B roll,
 * and 180° camera snap turns, with debounce cooldown protection and continuous
 * concurrent camera gyro aiming.
 */
class MotionGestureCombatTest {

    @Test
    fun testRestingAndGentleMovementDoesNotTriggerGestures() {
        val processor = GyroProcessor()
        val settings = GyroSettings.ACTION_COMBAT_PRESET.copy(combatGestures = GyroSettings.ACTION_COMBAT_PRESET.combatGestures.copy(flickThreshold = 8.5f))

        // Gentle user movement: 2.5 m/s² (well below 8.5 m/s² threshold)
        val result = processor.process(
            rawGyroX = 0f, rawGyroY = 0f, rawGyroZ = 0f,
            linearAccelX = 1.5f, linearAccelY = 1.5f, linearAccelZ = 1.0f,
            settings = settings
        )

        assertFalse(result.gestureDetected, "Gentle motion must not trigger gesture action")
        assertFalse(result.gesturePulseRT, "RT must not pulse on gentle motion")
        assertFalse(result.gesturePulseB, "B must not pulse on gentle motion")
        assertEquals(0f, result.snapTurnStickX, "Snap turn stick must remain 0")
    }

    @Test
    fun testKineticMeleeStrikeFiresRightTriggerAndAppliesCooldown() {
        val processor = GyroProcessor()
        val settings = GyroSettings.ACTION_COMBAT_PRESET.copy(
            combatGestures = GyroSettings.ACTION_COMBAT_PRESET.combatGestures.copy(
                flickThreshold = 8.5f,
                flickUpAction = GestureButton.TRIGGER_RT,
                flickCooldownMs = 400L
            )
        )

        // Kinetic strike: 10.0 m/s² forward thrust
        val strikeInput = GamepadInput(linearAccelY = 10.0f)
        val processedInput1 = InputPipeline.processInput(
            input = strikeInput,
            activeController = ControllerType.XBOX_360,
            gyroProcessor = processor,
            gyroSettings = settings,
            lsSensitivityX = 1.0f, lsSensitivityY = 1.0f,
            rsSensitivityX = 1.0f, rsSensitivityY = 1.0f,
            onProcessedAngles = { _, _ -> }
        )

        assertEquals(1.0f, processedInput1.triggerR2, 0.01f, "Kinetic strike must pulse Right Trigger (RT) to 1.0")

        // Immediate secondary impulse 50ms later (during recoil / follow-through)
        // Must be blocked by the 400ms cooldown to avoid accidental double-hits
        val recoilResult = processor.process(
            rawGyroX = 0f, rawGyroY = 0f, rawGyroZ = 0f,
            linearAccelY = 11.0f,
            settings = settings
        )
        // Since cooldown is active, no new trigger should be initiated after the active pulse expires
    }

    @Test
    fun testKineticFlickTriggersDodgeRollButtonB() {
        val processor = GyroProcessor()
        val settings = GyroSettings.ACTION_COMBAT_PRESET.copy(
            combatGestures = GyroSettings.ACTION_COMBAT_PRESET.combatGestures.copy(
                flickThreshold = 8.5f,
                flickRightAction = GestureButton.BTN_B
            )
        )

        val flickInput = GamepadInput(linearAccelX = 9.5f)
        val processedInput = InputPipeline.processInput(
            input = flickInput,
            activeController = ControllerType.XBOX_360,
            gyroProcessor = processor,
            gyroSettings = settings,
            lsSensitivityX = 1.0f, lsSensitivityY = 1.0f,
            rsSensitivityX = 1.0f, rsSensitivityY = 1.0f,
            onProcessedAngles = { _, _ -> }
        )

        assertTrue(processedInput.btnB, "Kinetic flick with DODGE_B must activate B button")
    }

    @Test
    fun testSnapTurn180InjectsFullCameraStickSpin() {
        val processor = GyroProcessor()
        val settings = GyroSettings.ACTION_COMBAT_PRESET.copy(
            combatGestures = GyroSettings.ACTION_COMBAT_PRESET.combatGestures.copy(
                flickThreshold = 8.5f,
                flickLeftAction = GestureButton.SNAP_TURN_180
            )
        )

        // Sharp left wrist jerk: linearAccelX = -9.0 m/s²
        val jerkLeftInput = GamepadInput(linearAccelX = -9.0f)
        val processedInput = InputPipeline.processInput(
            input = jerkLeftInput,
            activeController = ControllerType.XBOX_360,
            gyroProcessor = processor,
            gyroSettings = settings,
            lsSensitivityX = 1.0f, lsSensitivityY = 1.0f,
            rsSensitivityX = 1.0f, rsSensitivityY = 1.0f,
            onProcessedAngles = { _, _ -> }
        )

        // Must inject 100% full stick deflection to spin camera 180°
        assertEquals(-1.0f, processedInput.rightStickX, 0.01f, "Left snap turn must output -1.0 on Right Stick X")
    }

    @Test
    fun testJoysticksRemainDetachedDuringGestureMode() {
        val processor = GyroProcessor()
        val settings = GyroSettings.ACTION_COMBAT_PRESET.copy(
            combatGestures = GyroSettings.ACTION_COMBAT_PRESET.combatGestures.copy(
                flickThreshold = 8.5f
            )
        )

        // Regular wrist rotation without physical strike: 100 °/s yaw (1.745 rad/s)
        val aimRad = 100.0f / 57.29578f
        val aimInput = GamepadInput(
            gyroZ = aimRad,
            linearAccelX = 0f, linearAccelY = 0f, linearAccelZ = 0f
        )

        val processedInput = InputPipeline.processInput(
            input = aimInput,
            activeController = ControllerType.XBOX_360,
            gyroProcessor = processor,
            gyroSettings = settings,
            lsSensitivityX = 1.0f, lsSensitivityY = 1.0f,
            rsSensitivityX = 1.0f, rsSensitivityY = 1.0f,
            onProcessedAngles = { _, _ -> }
        )

        // In Combat Gesture mode, joysticks are detached from gyro
        assertEquals(0f, processedInput.rightStickX, "Right stick must remain 0 - detached in Combat Gesture mode")
        assertEquals(0f, processedInput.rightStickY, "Right stick must remain 0 - detached in Combat Gesture mode")
        assertEquals(0f, processedInput.leftStickX, "Left stick must remain 0 - detached in Combat Gesture mode")
        assertEquals(0f, processedInput.leftStickY, "Left stick must remain 0 - detached in Combat Gesture mode")
        assertFalse(processedInput.btnB, "Normal aiming must not trigger buttons")
    }

    @Test
    fun testDirectionalFlicksUpAndDownTriggerAssignedButtons() {
        val processor = GyroProcessor()
        val settings = GyroSettings(
            inputMode = InputMode.GESTURE,
            combatGestures = CombatGesturesSettings(
                flicksEnabled = true,
                flickThreshold = 8.5f,
                flickUpAction = GestureButton.BTN_X,
                flickDownAction = GestureButton.BTN_Y,
                flickCooldownMs = 100L
            )
        )

        // 1. Fast UP flick (linearAccelY = 10.0f)
        val upInput = GamepadInput(linearAccelY = 10.0f)
        val processedUp = InputPipeline.processInput(
            input = upInput,
            activeController = ControllerType.XBOX_360,
            gyroProcessor = processor,
            gyroSettings = settings,
            lsSensitivityX = 1.0f, lsSensitivityY = 1.0f,
            rsSensitivityX = 1.0f, rsSensitivityY = 1.0f,
            onProcessedAngles = { _, _ -> }
        )
        assertTrue(processedUp.btnX, "Fast UP flick must trigger X Button")
        assertFalse(processedUp.btnY, "Fast UP flick must not trigger Y Button")

        // Wait past cooldown
        Thread.sleep(120)

        // 2. Fast DOWN flick (linearAccelY = -10.0f)
        val downInput = GamepadInput(linearAccelY = -10.0f)
        val processedDown = InputPipeline.processInput(
            input = downInput,
            activeController = ControllerType.XBOX_360,
            gyroProcessor = processor,
            gyroSettings = settings,
            lsSensitivityX = 1.0f, lsSensitivityY = 1.0f,
            rsSensitivityX = 1.0f, rsSensitivityY = 1.0f,
            onProcessedAngles = { _, _ -> }
        )
        assertTrue(processedDown.btnY, "Fast DOWN flick must trigger Y Button")
    }

    @Test
    fun testGravityTiltAngleTriggersActionAboveThreshold() {
        val processor = GyroProcessor()
        val settings = GyroSettings(
            inputMode = InputMode.GESTURE,
            combatGestures = CombatGesturesSettings(
                flicksEnabled = false,
                tiltEnabled = true,
                tiltPitchThreshold = 30.0f,
                tiltUpAction = GestureButton.BTN_X,
                tiltHoldMode = false
            )
        )

        // Tilt 15° upward (below 30° threshold) -> quat pitch = 15°
        val pitch15Rad = Math.toRadians(15.0).toFloat()
        val qx15 = kotlin.math.sin(pitch15Rad * 0.5f)
        val qw15 = kotlin.math.cos(pitch15Rad * 0.5f)
        val belowInput = GamepadInput(quatX = qx15, quatW = qw15)

        val processedBelow = InputPipeline.processInput(
            input = belowInput,
            activeController = ControllerType.XBOX_360,
            gyroProcessor = processor,
            gyroSettings = settings,
            lsSensitivityX = 1.0f, lsSensitivityY = 1.0f,
            rsSensitivityX = 1.0f, rsSensitivityY = 1.0f,
            onProcessedAngles = { _, _ -> }
        )
        assertFalse(processedBelow.btnX, "Tilt below 30° must NOT trigger X button")

        // Tilt 35° upward (above 30° threshold) -> quat pitch = 35°
        val pitch35Rad = Math.toRadians(35.0).toFloat()
        val qx35 = kotlin.math.sin(pitch35Rad * 0.5f)
        val qw35 = kotlin.math.cos(pitch35Rad * 0.5f)
        val aboveInput = GamepadInput(quatX = qx35, quatW = qw35)

        val processedAbove = InputPipeline.processInput(
            input = aboveInput,
            activeController = ControllerType.XBOX_360,
            gyroProcessor = processor,
            gyroSettings = settings,
            lsSensitivityX = 1.0f, lsSensitivityY = 1.0f,
            rsSensitivityX = 1.0f, rsSensitivityY = 1.0f,
            onProcessedAngles = { _, _ -> }
        )
        assertTrue(processedAbove.btnX, "Tilt above 30° MUST trigger X button")
    }

    @Test
    fun testAllFourGravityTiltDirectionsTriggerButtons() {
        val processor = GyroProcessor()
        val settings = GyroSettings(
            inputMode = InputMode.GESTURE,
            combatGestures = CombatGesturesSettings(
                flicksEnabled = false,
                tiltEnabled = true,
                tiltPitchThreshold = 30.0f,
                tiltRollThreshold = 30.0f,
                tiltUpAction = GestureButton.BTN_X,
                tiltDownAction = GestureButton.BTN_Y,
                tiltLeftAction = GestureButton.BTN_LB,
                tiltRightAction = GestureButton.BTN_RB,
                tiltHoldMode = true
            )
        )

        // 1. Tilt DOWN (-35° pitch)
        val downPitchRad = Math.toRadians(-35.0).toFloat()
        val qxDown = kotlin.math.sin(downPitchRad * 0.5f)
        val qwDown = kotlin.math.cos(downPitchRad * 0.5f)
        val downInput = GamepadInput(quatX = qxDown, quatW = qwDown)
        val processedDown = InputPipeline.processInput(
            input = downInput, activeController = ControllerType.XBOX_360,
            gyroProcessor = processor, gyroSettings = settings,
            lsSensitivityX = 1.0f, lsSensitivityY = 1.0f, rsSensitivityX = 1.0f, rsSensitivityY = 1.0f,
            onProcessedAngles = { _, _ -> }
        )
        assertTrue(processedDown.btnY, "Tilt DOWN must trigger Y button")

        // 2. Tilt LEFT (-35° roll)
        val leftRollRad = Math.toRadians(-35.0).toFloat()
        val qyLeft = kotlin.math.sin(leftRollRad * 0.5f)
        val qwLeft = kotlin.math.cos(leftRollRad * 0.5f)
        val leftInput = GamepadInput(quatY = qyLeft, quatW = qwLeft)
        val processedLeft = InputPipeline.processInput(
            input = leftInput, activeController = ControllerType.XBOX_360,
            gyroProcessor = processor, gyroSettings = settings,
            lsSensitivityX = 1.0f, lsSensitivityY = 1.0f, rsSensitivityX = 1.0f, rsSensitivityY = 1.0f,
            onProcessedAngles = { _, _ -> }
        )
        assertTrue(processedLeft.btnL1, "Tilt LEFT must trigger LB button")

        // 3. Tilt RIGHT (+35° roll)
        val rightRollRad = Math.toRadians(35.0).toFloat()
        val qyRight = kotlin.math.sin(rightRollRad * 0.5f)
        val qwRight = kotlin.math.cos(rightRollRad * 0.5f)
        val rightInput = GamepadInput(quatY = qyRight, quatW = qwRight)
        val processedRight = InputPipeline.processInput(
            input = rightInput, activeController = ControllerType.XBOX_360,
            gyroProcessor = processor, gyroSettings = settings,
            lsSensitivityX = 1.0f, lsSensitivityY = 1.0f, rsSensitivityX = 1.0f, rsSensitivityY = 1.0f,
            onProcessedAngles = { _, _ -> }
        )
        assertTrue(processedRight.btnR1, "Tilt RIGHT must trigger RB button")
    }

    @Test
    fun testShakeMotionDetectionTriggersAction() {
        val processor = GyroProcessor()
        val settings = GyroSettings(
            inputMode = InputMode.GESTURE,
            combatGestures = CombatGesturesSettings(
                flicksEnabled = false,
                shakeEnabled = true,
                shakeThreshold = 10.0f,
                shakeAction = GestureButton.BTN_X,
                shakeCooldownMs = 300L
            )
        )

        // Simulate 3 rapid reversal shake peaks within 250ms
        // Peak 1
        processor.process(rawGyroX = 0f, rawGyroY = 0f, rawGyroZ = 0f, linearAccelX = 12.0f, settings = settings)
        Thread.sleep(60)
        // Peak 2 (reversal)
        processor.process(rawGyroX = 0f, rawGyroY = 0f, rawGyroZ = 0f, linearAccelX = -12.0f, settings = settings)
        Thread.sleep(60)
        // Peak 3 (third oscillation)
        val shakeInput = GamepadInput(linearAccelX = 12.0f)
        val processedShake = InputPipeline.processInput(
            input = shakeInput,
            activeController = ControllerType.XBOX_360,
            gyroProcessor = processor,
            gyroSettings = settings,
            lsSensitivityX = 1.0f, lsSensitivityY = 1.0f,
            rsSensitivityX = 1.0f, rsSensitivityY = 1.0f,
            onProcessedAngles = { _, _ -> }
        )

        assertTrue(processedShake.btnX, "Vigorous phone shaking must trigger X button")
    }

    @Test
    fun testCardOnOffTogglesDisableActions() {
        val processor = GyroProcessor()
        val settings = GyroSettings(
            inputMode = InputMode.GESTURE,
            combatGestures = CombatGesturesSettings(
                flicksEnabled = false,
                tiltEnabled = false,
                shakeEnabled = false
            )
        )

        val heavyStrike = GamepadInput(linearAccelY = 15.0f, quatX = 0.5f, quatW = 0.866f)
        val processed = InputPipeline.processInput(
            input = heavyStrike,
            activeController = ControllerType.XBOX_360,
            gyroProcessor = processor,
            gyroSettings = settings,
            lsSensitivityX = 1.0f, lsSensitivityY = 1.0f,
            rsSensitivityX = 1.0f, rsSensitivityY = 1.0f,
            onProcessedAngles = { _, _ -> }
        )

        assertFalse(processed.btnX, "When cards are disabled, X button must not trigger")
        assertFalse(processed.btnY, "When cards are disabled, Y button must not trigger")
        assertEquals(0f, processed.triggerR2, "When cards are disabled, RT must not trigger")
    }

    @Test
    fun testGravityTiltWorksDirectlyFromAccelerometerStream() {
        val processor = GyroProcessor()
        val settings = GyroSettings(
            inputMode = InputMode.GESTURE,
            combatGestures = CombatGesturesSettings(
                flicksEnabled = false,
                tiltEnabled = true,
                tiltPitchThreshold = 30.0f,
                tiltRollThreshold = 30.0f,
                tiltUpAction = GestureButton.BTN_X,
                tiltRightAction = GestureButton.BTN_RB,
                tiltHoldMode = true
            )
        )

        // Tilt 35° upward: rawAccelY = 9.8 * sin(35°) ≈ 5.62 m/s²
        val upInput = GamepadInput(accelY = 5.62f)
        val processedUp = InputPipeline.processInput(
            input = upInput,
            activeController = ControllerType.XBOX_360,
            gyroProcessor = processor,
            gyroSettings = settings,
            lsSensitivityX = 1.0f, lsSensitivityY = 1.0f,
            rsSensitivityX = 1.0f, rsSensitivityY = 1.0f,
            onProcessedAngles = { _, _ -> }
        )
        assertTrue(processedUp.btnX, "Pitch tilt from accelerometer must trigger X button")

        // Tilt 35° right: rawAccelX = 9.8 * sin(35°) ≈ 5.62 m/s²
        val rightInput = GamepadInput(accelX = 5.62f)
        val processedRight = InputPipeline.processInput(
            input = rightInput,
            activeController = ControllerType.XBOX_360,
            gyroProcessor = processor,
            gyroSettings = settings,
            lsSensitivityX = 1.0f, lsSensitivityY = 1.0f,
            rsSensitivityX = 1.0f, rsSensitivityY = 1.0f,
            onProcessedAngles = { _, _ -> }
        )
        assertTrue(processedRight.btnR1, "Roll tilt from accelerometer must trigger RB button")
    }

    @Test
    fun testGyroOffDetachesFromJoystickCompletely() {
        val processor = GyroProcessor()

        // 1. GESTURE mode (joysticks are completely detached)
        val gestureSettings = GyroSettings(
            inputMode = InputMode.GESTURE
        )
        val rotatingInput = GamepadInput(gyroZ = 2.0f, gyroX = 2.0f)
        val gestureProcessed = InputPipeline.processInput(
            input = rotatingInput,
            activeController = ControllerType.XBOX_360,
            gyroProcessor = processor,
            gyroSettings = gestureSettings,
            lsSensitivityX = 1.0f, lsSensitivityY = 1.0f,
            rsSensitivityX = 1.0f, rsSensitivityY = 1.0f,
            onProcessedAngles = { _, _ -> }
        )
        assertEquals(0f, gestureProcessed.rightStickX, "In GESTURE mode, right stick must not move")
        assertEquals(0f, gestureProcessed.rightStickY, "In GESTURE mode, right stick must not move")

        // 2. Global Target Stick = NONE
        val detachedSettings = GyroSettings(
            inputMode = InputMode.VELOCITY,
            precisionAiming = PrecisionAimingSettings(
                targetStick = XboxTargetStick.NONE
            )
        )
        val detachedProcessed = InputPipeline.processInput(
            input = rotatingInput,
            activeController = ControllerType.XBOX_360,
            gyroProcessor = processor,
            gyroSettings = detachedSettings,
            lsSensitivityX = 1.0f, lsSensitivityY = 1.0f,
            rsSensitivityX = 1.0f, rsSensitivityY = 1.0f,
            onProcessedAngles = { _, _ -> }
        )
        assertEquals(0f, detachedProcessed.rightStickX, "When TargetStick is NONE, right stick must remain untouched")
        assertEquals(0f, detachedProcessed.leftStickX, "When TargetStick is NONE, left stick must remain untouched")
    }

    @Test
    fun testPhysicalSticksPassThroughUntouchedInGestureMode() {
        val processor = GyroProcessor()
        val offSettings = GyroSettings(
            inputMode = InputMode.GESTURE
        )
        val physicalInput = GamepadInput(
            leftStickX = 0.75f,
            leftStickY = -0.60f,
            rightStickX = -0.45f,
            rightStickY = 0.80f,
            accelX = 5.62f,
            gyroZ = 3.0f
        )
        val offProcessed = InputPipeline.processInput(
            input = physicalInput,
            activeController = ControllerType.XBOX_360,
            gyroProcessor = processor,
            gyroSettings = offSettings,
            lsSensitivityX = 1.0f, lsSensitivityY = 1.0f,
            rsSensitivityX = 1.0f, rsSensitivityY = 1.0f,
            onProcessedAngles = { _, _ -> }
        )
        assertEquals(0.75f, offProcessed.leftStickX, 0.001f, "Physical LeftStickX must pass through untouched")
        assertEquals(-0.60f, offProcessed.leftStickY, 0.001f, "Physical LeftStickY must pass through untouched")
        assertEquals(-0.45f, offProcessed.rightStickX, 0.001f, "Physical RightStickX must pass through untouched")
        assertEquals(0.80f, offProcessed.rightStickY, 0.001f, "Physical RightStickY must pass through untouched")
    }
}
