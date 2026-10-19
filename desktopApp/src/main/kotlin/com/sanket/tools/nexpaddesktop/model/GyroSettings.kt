package com.sanket.tools.nexpaddesktop.model

import com.sanket.tools.nexpad.protocol.NexpadProtocol
import com.sanket.tools.nexpaddesktop.ui.ControllerType
import kotlinx.serialization.Serializable

/**
 * Complete configuration for NEXPAD's motion control system.
 *
 * This data class is the single source of truth for all gyro/accelerometer
 * processing parameters. It is read by [GyroProcessor] on every frame (~60 Hz)
 * and displayed/edited by the UI's GyroSettingsScreen.
 *
 * ## Settings Groups
 *
 * Settings are organized by which controller mode they affect:
 *
 * - **SHARED** — Apply to both Xbox 360 and PS4 (DualShock 4) modes.
 *   These control the internal processing pipeline (deadzone, smoothing, etc.)
 *   which affects Xbox stick output and the PS4 dashboard visualizer.
 *
 * - **XBOX GYRO AIMING** — Only used when the Xbox 360 controller is active.
 *   Xbox controllers have no native gyro, so NEXPAD converts processed gyro
 *   data into analog stick movement. These settings control that conversion.
 *
 * - **STEERING WHEEL** — Only used in Absolute Tilt (Steering Wheel) mode.
 *   Uses accelerometer data instead of gyroscope to map phone tilt angle
 *   directly to stick deflection.
 *
 * ## Processing Pipeline (Velocity / Gyro Aiming Mode)
 *
 * ```
 * Raw Gyro (rad/s) → °/s → Axis Remap → Inversion → Deadzone
 *   → Tightening → Smoothing → Acceleration → Low-Speed Amp → Sensitivity → Output
 * ```
 */
/**
 * Configuration for Precision Gyro Aiming (FPS / 1:1 Mouse-like Aiming).
 * Completely isolated from other modes.
 */
@Serializable
data class PrecisionAimingSettings(
    val targetStick: XboxTargetStick = XboxTargetStick.RIGHT_STICK,
    val maxDpsYaw: Float = 200.0f,
    val maxDpsPitch: Float = 200.0f,
    val maxDpsRoll: Float = 200.0f,
    val horizontalAxis: HorizontalAxis = HorizontalAxis.MIX,
    val invertX: Boolean = false,
    val invertY: Boolean = false,
    val invertRoll: Boolean = false,
    val deadzoneThreshold: Float = 0.8f,
    val antiDeadzone: Float = 0.0f,
    val stickResponseCurve: StickResponseCurve = StickResponseCurve.LINEAR,
    val tighteningEnabled: Boolean = false,
    val tighteningThreshold: Float = 10.0f,
    val smoothingEnabled: Boolean = true,
    val smoothingAmount: Float = 0.85f,
    val adaptiveSmoothing: Boolean = true,
    val smoothingThreshold: Float = 30.0f,
    val accelerationEnabled: Boolean = false,
    val accelerationType: AccelerationType = AccelerationType.LINEAR,
    val minSensitivity: Float = 1.0f,
    val maxSensitivity: Float = 2.0f,
    val minThreshold: Float = 0.0f,
    val maxThreshold: Float = 150.0f,
    val lowSpeedAmplifierEnabled: Boolean = false,
    val lowSpeedAmplifierAmount: Float = 2.0f,
    val lowSpeedAmplifierThreshold: Float = 20.0f,
    val lowSpeedAmplifierHarshness: Float = 2.0f,
    val blendMode: XboxBlendMode = XboxBlendMode.JOYSTICK_SUPREME,
    val activationMode: ActivationMode = ActivationMode.ALWAYS_ON,
    val activationButtons: Set<String> = emptySet(),
    val lsSensitivityX: Float = 1.0f,
    val lsSensitivityY: Float = 1.0f,
    val rsSensitivityX: Float = 1.0f,
    val rsSensitivityY: Float = 1.0f,
)

/**
 * Configuration for Virtual Steering Wheel (Gravity-based tilt emulation).
 * Completely isolated from other modes.
 */
@Serializable
data class SteeringWheelSettings(
    val targetStick: XboxTargetStick = XboxTargetStick.LEFT_STICK,
    val maxTiltAngle: Float = 35.0f,
    val sensitivityX: Float = 1.0f,
    val sensitivityY: Float = 1.0f,
    val responseCurve: Float = 1.0f,
    val invertSteering: Boolean = false,
    val invertPitch: Boolean = false,
    val deadzoneThreshold: Float = 2.0f,
    val blendMode: XboxBlendMode = XboxBlendMode.JOYSTICK_SUPREME,
    val lsSensitivityX: Float = 1.0f,
    val lsSensitivityY: Float = 1.0f,
    val rsSensitivityX: Float = 1.0f,
    val rsSensitivityY: Float = 1.0f,
)

/**
 * Configuration for 3D Flight Stick & Aviation Yoke (6-DoF attitude emulation).
 * Completely isolated from other modes.
 */
@Serializable
data class FlightStickSettings(
    val pitchTarget: FlightStickAxisTarget = FlightStickAxisTarget.LS_Y,
    val pitchInvert: Boolean = true,
    val maxPitchAngle: Float = 35.0f,
    val rollTarget: FlightStickAxisTarget = FlightStickAxisTarget.LS_X,
    val rollInvert: Boolean = false,
    val maxRollAngle: Float = 35.0f,
    val yawTarget: FlightStickAxisTarget = FlightStickAxisTarget.NONE,
    val yawInvert: Boolean = false,
    val maxYawAngle: Float = 35.0f,
    val deadzone: Float = 0.0f,
    val curveExponent: Float = 1.0f,
    val blendMode: XboxBlendMode = XboxBlendMode.JOYSTICK_SUPREME,
    val lsSensitivityX: Float = 1.0f,
    val lsSensitivityY: Float = 1.0f,
    val rsSensitivityX: Float = 1.0f,
    val rsSensitivityY: Float = 1.0f,
)

/**
 * Configuration for Combat Gestures (Kinetic flicks, tilt postures, and shakes).
 * Completely isolated from other modes; joysticks remain 100% untouched.
 */
@Serializable
data class CombatGesturesSettings(
    val flicksEnabled: Boolean = true,
    val flickThreshold: Float = 8.5f,
    val flickCooldownMs: Long = 350L,
    val flickUpAction: GestureButton = GestureButton.BTN_X,
    val flickDownAction: GestureButton = GestureButton.BTN_Y,
    val flickLeftAction: GestureButton = GestureButton.SNAP_TURN_180,
    val flickRightAction: GestureButton = GestureButton.TRIGGER_RT,
    val tiltEnabled: Boolean = false,
    val tiltPitchThreshold: Float = 30.0f,
    val tiltRollThreshold: Float = 30.0f,
    val tiltUpAction: GestureButton = GestureButton.BTN_X,
    val tiltDownAction: GestureButton = GestureButton.BTN_Y,
    val tiltLeftAction: GestureButton = GestureButton.BTN_LB,
    val tiltRightAction: GestureButton = GestureButton.BTN_RB,
    val tiltHoldMode: Boolean = false,
    val shakeEnabled: Boolean = false,
    val shakeThreshold: Float = 11.0f,
    val shakeAction: GestureButton = GestureButton.BTN_X,
    val shakeCooldownMs: Long = 450L,
    val lsSensitivityX: Float = 1.0f,
    val lsSensitivityY: Float = 1.0f,
    val rsSensitivityX: Float = 1.0f,
    val rsSensitivityY: Float = 1.0f,
)

/**
 * Configuration for Esports Direct Raw Silicon Gyro (Uncalibrated MEMS sensor stream).
 * Completely isolated from other modes.
 */
@Serializable
data class RawSiliconSettings(
    val targetStick: XboxTargetStick = XboxTargetStick.RIGHT_STICK,
    val maxDpsYaw: Float = 200.0f,
    val maxDpsPitch: Float = 200.0f,
    val maxDpsRoll: Float = 200.0f,
    val horizontalAxis: HorizontalAxis = HorizontalAxis.MIX,
    val invertX: Boolean = false,
    val invertY: Boolean = false,
    val invertRoll: Boolean = false,
    val deadzoneThreshold: Float = 0.8f,
    val antiDeadzone: Float = 0.0f,
    val stickResponseCurve: StickResponseCurve = StickResponseCurve.LINEAR,
    val tighteningEnabled: Boolean = false,
    val tighteningThreshold: Float = 10.0f,
    val lowSpeedAmplifierEnabled: Boolean = false,
    val lowSpeedAmplifierAmount: Float = 2.0f,
    val lowSpeedAmplifierThreshold: Float = 20.0f,
    val lowSpeedAmplifierHarshness: Float = 2.0f,
    val blendMode: XboxBlendMode = XboxBlendMode.JOYSTICK_SUPREME,
    val activationMode: ActivationMode = ActivationMode.ALWAYS_ON,
    val activationButtons: Set<String> = emptySet(),
    val lsSensitivityX: Float = 1.0f,
    val lsSensitivityY: Float = 1.0f,
    val rsSensitivityX: Float = 1.0f,
    val rsSensitivityY: Float = 1.0f,
)

/**
 * Root composite configuration for NEXPAD motion controls.
 *
 * Each motion mode has its own dedicated, encapsulated configuration class.
 * Adjusting settings in one mode will NEVER leak, collide, or overwrite settings
 * in any other mode!
 */
@Serializable
data class GyroSettings(
    val enabled: Boolean = true,
    val inputMode: InputMode = InputMode.VELOCITY,
    val precisionAiming: PrecisionAimingSettings = PrecisionAimingSettings(),
    val steeringWheel: SteeringWheelSettings = SteeringWheelSettings(),
    val flightStick: FlightStickSettings = FlightStickSettings(),
    val combatGestures: CombatGesturesSettings = CombatGesturesSettings(),
    val rawSilicon: RawSiliconSettings = RawSiliconSettings(),
) {
    // ══════════════════════════════════════════════════════════
    //  BACKWARD-COMPATIBLE ACCESSORS (DELEGATES TO ACTIVE MODE)
    // ══════════════════════════════════════════════════════════

    val lsSensitivityX: Float
        get() = when (inputMode) {
            InputMode.VELOCITY -> precisionAiming.lsSensitivityX
            InputMode.ABSOLUTE_TILT -> steeringWheel.lsSensitivityX
            InputMode.AIR_MOUSE -> flightStick.lsSensitivityX
            InputMode.GESTURE -> combatGestures.lsSensitivityX
            InputMode.RAW_UNCALIBRATED -> rawSilicon.lsSensitivityX
        }

    val lsSensitivityY: Float
        get() = when (inputMode) {
            InputMode.VELOCITY -> precisionAiming.lsSensitivityY
            InputMode.ABSOLUTE_TILT -> steeringWheel.lsSensitivityY
            InputMode.AIR_MOUSE -> flightStick.lsSensitivityY
            InputMode.GESTURE -> combatGestures.lsSensitivityY
            InputMode.RAW_UNCALIBRATED -> rawSilicon.lsSensitivityY
        }

    val rsSensitivityX: Float
        get() = when (inputMode) {
            InputMode.VELOCITY -> precisionAiming.rsSensitivityX
            InputMode.ABSOLUTE_TILT -> steeringWheel.rsSensitivityX
            InputMode.AIR_MOUSE -> flightStick.rsSensitivityX
            InputMode.GESTURE -> combatGestures.rsSensitivityX
            InputMode.RAW_UNCALIBRATED -> rawSilicon.rsSensitivityX
        }

    val rsSensitivityY: Float
        get() = when (inputMode) {
            InputMode.VELOCITY -> precisionAiming.rsSensitivityY
            InputMode.ABSOLUTE_TILT -> steeringWheel.rsSensitivityY
            InputMode.AIR_MOUSE -> flightStick.rsSensitivityY
            InputMode.GESTURE -> combatGestures.rsSensitivityY
            InputMode.RAW_UNCALIBRATED -> rawSilicon.rsSensitivityY
        }

    fun updateActiveStickSensitivities(
        lsX: Float = lsSensitivityX,
        lsY: Float = lsSensitivityY,
        rsX: Float = rsSensitivityX,
        rsY: Float = rsSensitivityY
    ): GyroSettings {
        return when (inputMode) {
            InputMode.VELOCITY -> copy(precisionAiming = precisionAiming.copy(lsSensitivityX = lsX, lsSensitivityY = lsY, rsSensitivityX = rsX, rsSensitivityY = rsY))
            InputMode.ABSOLUTE_TILT -> copy(steeringWheel = steeringWheel.copy(lsSensitivityX = lsX, lsSensitivityY = lsY, rsSensitivityX = rsX, rsSensitivityY = rsY))
            InputMode.AIR_MOUSE -> copy(flightStick = flightStick.copy(lsSensitivityX = lsX, lsSensitivityY = lsY, rsSensitivityX = rsX, rsSensitivityY = rsY))
            InputMode.GESTURE -> copy(combatGestures = combatGestures.copy(lsSensitivityX = lsX, lsSensitivityY = lsY, rsSensitivityX = rsX, rsSensitivityY = rsY))
            InputMode.RAW_UNCALIBRATED -> copy(rawSilicon = rawSilicon.copy(lsSensitivityX = lsX, lsSensitivityY = lsY, rsSensitivityX = rsX, rsSensitivityY = rsY))
        }
    }
    // ══════════════════════════════════════════════════════════

    val xboxTargetStick: XboxTargetStick
        get() = when (inputMode) {
            InputMode.VELOCITY -> precisionAiming.targetStick
            InputMode.ABSOLUTE_TILT -> steeringWheel.targetStick
            InputMode.RAW_UNCALIBRATED -> rawSilicon.targetStick
            else -> XboxTargetStick.RIGHT_STICK
        }

    val xboxBlendMode: XboxBlendMode
        get() = when (inputMode) {
            InputMode.VELOCITY -> precisionAiming.blendMode
            InputMode.ABSOLUTE_TILT -> steeringWheel.blendMode
            InputMode.AIR_MOUSE -> flightStick.blendMode
            InputMode.RAW_UNCALIBRATED -> rawSilicon.blendMode
            else -> XboxBlendMode.JOYSTICK_SUPREME
        }

    val activationButtons: Set<String>
        get() = when (inputMode) {
            InputMode.VELOCITY -> precisionAiming.activationButtons
            InputMode.RAW_UNCALIBRATED -> rawSilicon.activationButtons
            else -> emptySet()
        }

    val activationMode: ActivationMode
        get() = when (inputMode) {
            InputMode.VELOCITY -> precisionAiming.activationMode
            InputMode.RAW_UNCALIBRATED -> rawSilicon.activationMode
            else -> ActivationMode.ALWAYS_ON
        }

    val invertX: Boolean
        get() = when (inputMode) {
            InputMode.VELOCITY -> precisionAiming.invertX
            InputMode.ABSOLUTE_TILT -> steeringWheel.invertSteering
            InputMode.RAW_UNCALIBRATED -> rawSilicon.invertX
            else -> false
        }

    val invertY: Boolean
        get() = when (inputMode) {
            InputMode.VELOCITY -> precisionAiming.invertY
            InputMode.ABSOLUTE_TILT -> steeringWheel.invertPitch
            InputMode.RAW_UNCALIBRATED -> rawSilicon.invertY
            else -> false
        }

    val invertRoll: Boolean
        get() = when (inputMode) {
            InputMode.VELOCITY -> precisionAiming.invertRoll
            InputMode.RAW_UNCALIBRATED -> rawSilicon.invertRoll
            else -> false
        }

    val gameDeadzoneCompensation: Float
        get() = when (inputMode) {
            InputMode.VELOCITY -> precisionAiming.antiDeadzone
            InputMode.RAW_UNCALIBRATED -> rawSilicon.antiDeadzone
            else -> 0f
        }

    val deadzoneThreshold: Float
        get() = when (inputMode) {
            InputMode.VELOCITY -> precisionAiming.deadzoneThreshold
            InputMode.ABSOLUTE_TILT -> steeringWheel.deadzoneThreshold
            InputMode.AIR_MOUSE -> flightStick.deadzone
            InputMode.RAW_UNCALIBRATED -> rawSilicon.deadzoneThreshold
            else -> 0f
        }

    val stickResponseCurve: StickResponseCurve
        get() = when (inputMode) {
            InputMode.VELOCITY -> precisionAiming.stickResponseCurve
            InputMode.RAW_UNCALIBRATED -> rawSilicon.stickResponseCurve
            else -> StickResponseCurve.LINEAR
        }

    val horizontalAxis: HorizontalAxis
        get() = when (inputMode) {
            InputMode.VELOCITY -> precisionAiming.horizontalAxis
            InputMode.RAW_UNCALIBRATED -> rawSilicon.horizontalAxis
            else -> HorizontalAxis.MIX
        }

    val maxDpsYaw: Float
        get() = when (inputMode) {
            InputMode.VELOCITY -> precisionAiming.maxDpsYaw
            InputMode.RAW_UNCALIBRATED -> rawSilicon.maxDpsYaw
            else -> 200.0f
        }

    val maxDpsPitch: Float
        get() = when (inputMode) {
            InputMode.VELOCITY -> precisionAiming.maxDpsPitch
            InputMode.RAW_UNCALIBRATED -> rawSilicon.maxDpsPitch
            else -> 200.0f
        }

    val maxDpsRoll: Float
        get() = when (inputMode) {
            InputMode.VELOCITY -> precisionAiming.maxDpsRoll
            InputMode.RAW_UNCALIBRATED -> rawSilicon.maxDpsRoll
            else -> 200.0f
        }

    val smoothingEnabled: Boolean
        get() = when (inputMode) {
            InputMode.VELOCITY -> precisionAiming.smoothingEnabled
            else -> true
        }

    val smoothingAmount: Float
        get() = when (inputMode) {
            InputMode.VELOCITY -> precisionAiming.smoothingAmount
            else -> 0.85f
        }

    val adaptiveSmoothing: Boolean
        get() = when (inputMode) {
            InputMode.VELOCITY -> precisionAiming.adaptiveSmoothing
            else -> true
        }

    val accelerationEnabled: Boolean
        get() = when (inputMode) {
            InputMode.VELOCITY -> precisionAiming.accelerationEnabled
            else -> false
        }

    val maxSensitivity: Float
        get() = when (inputMode) {
            InputMode.VELOCITY -> precisionAiming.maxSensitivity
            else -> 2.0f
        }

    val lowSpeedAmplifierEnabled: Boolean
        get() = when (inputMode) {
            InputMode.VELOCITY -> precisionAiming.lowSpeedAmplifierEnabled
            InputMode.RAW_UNCALIBRATED -> rawSilicon.lowSpeedAmplifierEnabled
            else -> false
        }

    val lowSpeedAmplifierAmount: Float
        get() = when (inputMode) {
            InputMode.VELOCITY -> precisionAiming.lowSpeedAmplifierAmount
            InputMode.RAW_UNCALIBRATED -> rawSilicon.lowSpeedAmplifierAmount
            else -> 2.0f
        }

    val absoluteMaxTilt: Float get() = steeringWheel.maxTiltAngle
    val absoluteSensitivityX: Float get() = steeringWheel.sensitivityX
    val absoluteSensitivityY: Float get() = steeringWheel.sensitivityY
    val absoluteCurve: Float get() = steeringWheel.responseCurve

    val airMouseMaxPitch: Float get() = flightStick.maxPitchAngle
    val airMouseMaxRoll: Float get() = flightStick.maxRollAngle
    val airMouseMaxYaw: Float get() = flightStick.maxYawAngle
    val airMouseDeadzone: Float get() = flightStick.deadzone
    val airMouseCurve: Float get() = flightStick.curveExponent
    val airMousePitchInvert: Boolean get() = flightStick.pitchInvert
    val airMouseRollInvert: Boolean get() = flightStick.rollInvert
    val airMouseYawInvert: Boolean get() = flightStick.yawInvert
    val airMousePitchTarget: FlightStickAxisTarget get() = flightStick.pitchTarget
    val airMouseRollTarget: FlightStickAxisTarget get() = flightStick.rollTarget
    val airMouseYawTarget: FlightStickAxisTarget get() = flightStick.yawTarget

    val gestureFlicksEnabled: Boolean get() = combatGestures.flicksEnabled
    val gestureFlickThreshold: Float get() = combatGestures.flickThreshold
    val gestureFlickCooldownMs: Long get() = combatGestures.flickCooldownMs
    val gestureFlickUpAction: GestureButton get() = combatGestures.flickUpAction
    val gestureFlickDownAction: GestureButton get() = combatGestures.flickDownAction
    val gestureFlickLeftAction: GestureButton get() = combatGestures.flickLeftAction
    val gestureFlickRightAction: GestureButton get() = combatGestures.flickRightAction
    val gestureTiltEnabled: Boolean get() = combatGestures.tiltEnabled
    val gestureTiltPitchThreshold: Float get() = combatGestures.tiltPitchThreshold
    val gestureTiltRollThreshold: Float get() = combatGestures.tiltRollThreshold
    val gestureTiltHoldMode: Boolean get() = combatGestures.tiltHoldMode
    val gestureTiltUpAction: GestureButton get() = combatGestures.tiltUpAction
    val gestureTiltDownAction: GestureButton get() = combatGestures.tiltDownAction
    val gestureTiltLeftAction: GestureButton get() = combatGestures.tiltLeftAction
    val gestureTiltRightAction: GestureButton get() = combatGestures.tiltRightAction
    val gestureShakeEnabled: Boolean get() = combatGestures.shakeEnabled
    val gestureShakeAction: GestureButton get() = combatGestures.shakeAction
    val gestureShakeThreshold: Float get() = combatGestures.shakeThreshold
    val gestureShakeCooldownMs: Long get() = combatGestures.shakeCooldownMs

    val tighteningEnabled: Boolean
        get() = when (inputMode) {
            InputMode.VELOCITY -> precisionAiming.tighteningEnabled
            InputMode.RAW_UNCALIBRATED -> rawSilicon.tighteningEnabled
            else -> false
        }

    val tighteningThreshold: Float
        get() = when (inputMode) {
            InputMode.VELOCITY -> precisionAiming.tighteningThreshold
            InputMode.RAW_UNCALIBRATED -> rawSilicon.tighteningThreshold
            else -> 10.0f
        }

    val smoothingThreshold: Float
        get() = precisionAiming.smoothingThreshold

    val minSensitivity: Float
        get() = precisionAiming.minSensitivity

    val minThreshold: Float
        get() = precisionAiming.minThreshold

    val maxThreshold: Float
        get() = precisionAiming.maxThreshold

    val accelerationType: AccelerationType
        get() = precisionAiming.accelerationType

    val lowSpeedAmplifierThreshold: Float
        get() = when (inputMode) {
            InputMode.VELOCITY -> precisionAiming.lowSpeedAmplifierThreshold
            InputMode.RAW_UNCALIBRATED -> rawSilicon.lowSpeedAmplifierThreshold
            else -> 20.0f
        }

    val lowSpeedAmplifierHarshness: Float
        get() = when (inputMode) {
            InputMode.VELOCITY -> precisionAiming.lowSpeedAmplifierHarshness
            InputMode.RAW_UNCALIBRATED -> rawSilicon.lowSpeedAmplifierHarshness
            else -> 2.0f
        }

    companion object {

        /** Best defaults for FPS / aiming games (high sensitivity, responsive adaptive smoothing, pure linear 1:1 aiming). */
        val FPS_PRESET = GyroSettings(
            inputMode = InputMode.VELOCITY,
            precisionAiming = PrecisionAimingSettings(
                maxDpsYaw = 160f,
                maxDpsRoll = 160f,
                maxDpsPitch = 240f,
                deadzoneThreshold = 0.8f,
                smoothingEnabled = true,
                smoothingAmount = 0.85f,
                adaptiveSmoothing = true,
                smoothingThreshold = 30.0f,
                accelerationEnabled = false,
                stickResponseCurve = StickResponseCurve.LINEAR,
                antiDeadzone = 0.0f,
                blendMode = XboxBlendMode.JOYSTICK_SUPREME,
            )
        )

        /** Best defaults for racing / steering games (switches to Steering Wheel mode). */
        val RACING_PRESET = GyroSettings(
            inputMode = InputMode.ABSOLUTE_TILT,
            steeringWheel = SteeringWheelSettings(
                targetStick = XboxTargetStick.LEFT_STICK,
                maxTiltAngle = 35.0f,
                sensitivityX = 1.2f,
                sensitivityY = 1.0f,
                responseCurve = 1.5f,
                deadzoneThreshold = 3.0f,
                blendMode = XboxBlendMode.JOYSTICK_SUPREME,
            )
        )

        /** Best defaults for flight simulators & space combat (3D Flight Yoke / Attitude Stick). */
        val FLIGHT_SIM_PRESET = GyroSettings(
            inputMode = InputMode.AIR_MOUSE,
            flightStick = FlightStickSettings(
                pitchTarget = FlightStickAxisTarget.LS_Y,
                pitchInvert = true,
                maxPitchAngle = 35.0f,
                rollTarget = FlightStickAxisTarget.LS_X,
                rollInvert = false,
                maxRollAngle = 35.0f,
                yawTarget = FlightStickAxisTarget.NONE,
                yawInvert = false,
                maxYawAngle = 35.0f,
                deadzone = 0.0f,
                curveExponent = 1.0f,
                blendMode = XboxBlendMode.JOYSTICK_SUPREME,
            )
        )

        /** Best defaults for action / combat games (kinetic weapon swings, directional flicks & 180° snap turns). */
        val ACTION_COMBAT_PRESET = GyroSettings(
            inputMode = InputMode.GESTURE,
            combatGestures = CombatGesturesSettings(
                flicksEnabled = true,
                flickThreshold = 8.5f,
                flickCooldownMs = 350L,
                flickUpAction = GestureButton.BTN_X,
                flickDownAction = GestureButton.BTN_Y,
                flickLeftAction = GestureButton.SNAP_TURN_180,
                flickRightAction = GestureButton.TRIGGER_RT,
                tiltEnabled = false,
                shakeEnabled = false,
            )
        )

        /** High-precision preset for slow, accurate aiming (sniping / bow games). */
        val PRECISION_PRESET = GyroSettings(
            inputMode = InputMode.VELOCITY,
            precisionAiming = PrecisionAimingSettings(
                maxDpsYaw = 250f,
                maxDpsRoll = 250f,
                maxDpsPitch = 444f,
                deadzoneThreshold = 0.5f,
                smoothingEnabled = true,
                smoothingAmount = 0.80f,
                adaptiveSmoothing = true,
                smoothingThreshold = 25.0f,
                accelerationEnabled = false,
                stickResponseCurve = StickResponseCurve.LINEAR,
                antiDeadzone = 0.12f,
                blendMode = XboxBlendMode.JOYSTICK_SUPREME,
            )
        )

        /** Factory defaults — all settings at their neutral/default values. */
        val DEFAULT = GyroSettings()
    }
}

// ══════════════════════════════════════════════════════════════
//  SUPPORTING ENUMS
// ══════════════════════════════════════════════════════════════

/** Which physical rotation axis maps to horizontal camera movement. */
@Serializable
enum class HorizontalAxis(val displayName: String) {
    YAW("Yaw (Turn wrist left/right)"),
    ROLL("Roll (Tilt controller sideways)"),
    MIX("Mix (Both Yaw and Roll)")
}

/** Stick response curve applied when converting gyro velocity to Xbox analog stick deflection. */
@Serializable
enum class StickResponseCurve(val displayName: String, val exponent: Float) {
    LINEAR("Linear 1:1 (Recommended - True Mouse-like)", 1.0f),
    LINEARIZED("Linearized (Counters Game Stick Exponential Curve)", 0.72f),
    PRECISION("Precision Center (Gentle Slow Aim)", 1.35f)
}

/** Type of acceleration curve applied to gyro input. */
@Serializable
enum class AccelerationType(val displayName: String) {
    LINEAR("Linear Interpolation"),
    POWER("Power Curve"),
    SMOOTH_STEP("Smooth Step (S-Curve)")
}

/** How the gyro is activated during gameplay. */
@Serializable
enum class ActivationMode(val displayName: String) {
    ALWAYS_ON("Always On"),
    BUTTON_HOLD("Button Hold"),
    TOGGLE("Toggle On/Off")
}

/** Determines how physical phone movement translates to stick movement. */
@Serializable
enum class InputMode(val displayName: String) {
    VELOCITY("Gyro Precision Aiming (FPS Camera)"),
    ABSOLUTE_TILT("Virtual Steering Wheel (Tilt Emulation)"),
    AIR_MOUSE("3D Flight Stick (Aviation Yoke)"),
    GESTURE("Combat Gestures & Kinetic Actions"),
    RAW_UNCALIBRATED("Raw Silicon Gyro (Esports Direct)")
}

/** Which Xbox analog stick receives the converted gyro movement. */
@Serializable
enum class XboxTargetStick(val displayName: String) {
    RIGHT_STICK("Right Stick (Aiming)"),
    LEFT_STICK("Left Stick (Movement / Steering)"),
    NONE("Disabled (Off - No Joystick Attachment)")
}

/** How gyro-generated stick values combine with physical stick input. */
@Serializable
enum class XboxBlendMode(val displayName: String) {
    JOYSTICK_SUPREME("Joystick Supreme (Stick First, Gyro Idle)"),
    ADDITIVE("Additive (Stick + Gyro Aiming)"),
    MUTE_ON_STICK("Mute on Stick Deflection")
}

/** Tactical action triggered when a physical motion gesture (linear acceleration strike) is detected. */
@Serializable
enum class GestureAction(val displayName: String, val description: String) {
    SNAP_TURN_180("180° Snap Turn", "Instant 180° camera turnaround for fast rear defense"),
    MELEE_RT("Melee Attack (RT)", "Swings weapon or fires heavy attack (Right Trigger)"),
    DODGE_B("Dodge / Evade (B)", "Executes dodge roll or dash (B button)"),
    RELOAD_X("Reload / Action (X)", "Reloads weapon or performs action (X button)"),
    JUMP_A("Jump (A)", "Physical upward flick triggers jump (A button)")
}

/** Controller button or tactical action triggered by a motion gesture. */
@Serializable
enum class GestureButton(val displayName: String, val shortName: String) {
    NONE("Disabled (Off)", "Off"),
    BTN_X("X Button (Reload / Action)", "X"),
    BTN_Y("Y Button (Switch Weapon)", "Y"),
    BTN_A("A Button (Jump / Confirm)", "A"),
    BTN_B("B Button (Dodge / Crouch)", "B"),
    BTN_LB("LB (Left Bumper)", "LB"),
    BTN_RB("RB (Right Bumper)", "RB"),
    TRIGGER_LT("LT (Left Trigger / Aim)", "LT"),
    TRIGGER_RT("RT (Right Trigger / Melee)", "RT"),
    BTN_LS("LS Click / L3 (Sprint)", "LS"),
    BTN_RS("RS Click / R3 (Melee / Zoom)", "RS"),
    SNAP_TURN_180("180° Snap Turn", "180° Turn"),
    DPAD_UP("D-Pad Up", "D-Up"),
    DPAD_DOWN("D-Pad Down", "D-Down"),
    DPAD_LEFT("D-Pad Left", "D-Left"),
    DPAD_RIGHT("D-Pad Right", "D-Right");

    companion object {
        fun fromLegacy(action: GestureAction): GestureButton = when (action) {
            GestureAction.SNAP_TURN_180 -> SNAP_TURN_180
            GestureAction.MELEE_RT -> TRIGGER_RT
            GestureAction.DODGE_B -> BTN_B
            GestureAction.RELOAD_X -> BTN_X
            GestureAction.JUMP_A -> BTN_A
        }
    }
}

/** Joystick selection for individual flight stick mapping. */
@Serializable
enum class FlightStickChoice(val displayName: String) {
    LEFT("Left Stick (LS)"),
    RIGHT("Right Stick (RS)"),
    DISABLED("Disabled (Off)")
}

/** Joystick deflection axis/direction for individual flight stick mapping. */
@Serializable
enum class FlightStickDirection(val displayName: String) {
    UP_DOWN("Up / Down (Y-Axis)"),
    LEFT_RIGHT("Left / Right (X-Axis)")
}

/** Target stick and axis for individual 3D Flight Stick rotation mapping. */
@Serializable
enum class FlightStickAxisTarget(val displayName: String) {
    NONE("Disabled"),
    LS_X("LS Left / Right (X)"),
    LS_Y("LS Up / Down (Y)"),
    RS_X("RS Left / Right (X)"),
    RS_Y("RS Up / Down (Y)");

    val stick: FlightStickChoice get() = when (this) {
        LS_X, LS_Y -> FlightStickChoice.LEFT
        RS_X, RS_Y -> FlightStickChoice.RIGHT
        NONE -> FlightStickChoice.DISABLED
    }

    val direction: FlightStickDirection get() = when (this) {
        LS_Y, RS_Y -> FlightStickDirection.UP_DOWN
        LS_X, RS_X -> FlightStickDirection.LEFT_RIGHT
        NONE -> FlightStickDirection.LEFT_RIGHT
    }

    companion object {
        fun from(stick: FlightStickChoice, direction: FlightStickDirection): FlightStickAxisTarget {
            if (stick == FlightStickChoice.DISABLED) return NONE
            return when (stick) {
                FlightStickChoice.LEFT -> if (direction == FlightStickDirection.UP_DOWN) LS_Y else LS_X
                FlightStickChoice.RIGHT -> if (direction == FlightStickDirection.UP_DOWN) RS_Y else RS_X
                FlightStickChoice.DISABLED -> NONE
            }
        }
    }
}

/**
 * Resolves the protocol sensor mode (0x00..0x04) required from the mobile client
 * based on the active controller and gyro configuration.
 *
 * CRITICAL RULE: DualShock 4 emulation ALWAYS requires Mode 0x00 (IMU_6AXIS) because
 * the PS4 driver requires raw calibrated gyroscope and accelerometer streams.
 * Advanced sensor multiplexing (Gravity, Quaternion, Linear Gesture, Raw Uncalibrated)
 * is activated only in Xbox 360 mode when Gyro is enabled.
 */
fun GyroSettings.computeSensorMode(controller: ControllerType): Byte {
    if (controller == ControllerType.DUALSHOCK_4) {
        return NexpadProtocol.SENSOR_MODE_IMU_6AXIS
    }
    if (!enabled) {
        return NexpadProtocol.SENSOR_MODE_IMU_6AXIS
    }
    return when (inputMode) {
        InputMode.VELOCITY -> NexpadProtocol.SENSOR_MODE_IMU_6AXIS
        InputMode.ABSOLUTE_TILT -> NexpadProtocol.SENSOR_MODE_GRAVITY
        InputMode.AIR_MOUSE -> NexpadProtocol.SENSOR_MODE_GAME_ROTATION
        InputMode.GESTURE -> NexpadProtocol.SENSOR_MODE_LINEAR_GESTURE
        InputMode.RAW_UNCALIBRATED -> NexpadProtocol.SENSOR_MODE_RAW_UNCALIBRATED
    }
}

