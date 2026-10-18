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
@Serializable
data class GyroSettings(

    // ══════════════════════════════════════════════════════════
    //  SHARED — Affects both Xbox 360 and PS4 modes
    // ══════════════════════════════════════════════════════════

    /** Master on/off switch for all motion processing. When false, no gyro data is processed. */
    val enabled: Boolean = true,

    /** If true, horizontal (left/right) camera movement is reversed. Applies to both gyro and accel modes. */
    val invertX: Boolean = false,

    /** If true, roll (sideways tilt / bank) camera movement is reversed. */
    val invertRoll: Boolean = false,

    /** If true, vertical (up/down) camera movement is reversed. Applies to both gyro and accel modes. */
    val invertY: Boolean = false,

    /**
     * Which physical rotation axis drives horizontal camera movement.
     * - YAW: Rotating the phone flat on a table (turning wrist left/right)
     * - ROLL: Tilting the phone sideways (like a steering wheel)
     * - MIX: Both yaw and roll contribute to horizontal movement
     */
    val horizontalAxis: HorizontalAxis = HorizontalAxis.MIX,

    /**
     * Hard deadzone threshold in °/s. Rotation speeds below this value are
     * completely ignored (output = 0). This prevents micro-vibrations and
     * sensor noise from moving the crosshair when the phone is resting still.
     * Typical values: 2.0–5.0 °/s.
     */
    val deadzoneThreshold: Float = 0.8f,

    /** Whether EMA (Exponential Moving Average) smoothing is applied to gyro output. */
    val smoothingEnabled: Boolean = true,

    /**
     * EMA alpha coefficient. Controls how much each new frame blends with history.
     * - 0.1 = very heavy smoothing (laggy but silky smooth)
     * - 0.85 = crisp & responsive (zero lag, eliminates jitter)
     * - 1.0 = no smoothing (raw, immediate)
     */
    val smoothingAmount: Float = 0.85f,

    /**
     * If true, only smooth slow movements. Fast flicks pass through unsmoothed
     * for maximum responsiveness. Slow movements get full smoothing to remove jitter.
     */
    val adaptiveSmoothing: Boolean = true,

    /** °/s speed below which adaptive smoothing applies full smoothing. Above this, smoothing fades out. */
    val smoothingThreshold: Float = 30.0f,

    /**
     * Whether an acceleration curve is applied to gyro output (speed-dependent sensitivity).
     * Defaults to false for consistent, predictable 1:1 mouse-like muscle memory (JoyShockLibrary standard).
     */
    val accelerationEnabled: Boolean = false,

    /** The shape of the acceleration curve interpolation between min/max thresholds. */
    val accelerationType: AccelerationType = AccelerationType.LINEAR,

    /** Sensitivity multiplier at very slow rotation speeds. 1.0 = 100% of base speed. */
    val minSensitivity: Float = 1.0f,

    /** Sensitivity multiplier at very fast rotation speeds. E.g., 2.0 = 200% of base speed. */
    val maxSensitivity: Float = 2.0f,

    /** °/s rotation speed at which [minSensitivity] applies (lower bound of the curve). */
    val minThreshold: Float = 0.0f,

    /** °/s rotation speed at which [maxSensitivity] applies (upper bound of the curve). */
    val maxThreshold: Float = 150.0f,

    /**
     * Stick response curve applied when converting gyro velocity to Xbox analog stick deflection.
     * - LINEAR: 1:1 direct proportional mapping (JoyShockLibrary/Steam Input standard).
     * - LINEARIZED: Counteracts games with aggressive in-game quadratic stick curves.
     * - PRECISION: Gentle quadratic center curve for fine sniping adjustments.
     */
    val stickResponseCurve: StickResponseCurve = StickResponseCurve.LINEAR,

    /** Whether tightening (soft quadratic deadzone) is enabled. Reduces very small movements smoothly. */
    val tighteningEnabled: Boolean = false,

    /** °/s threshold below which tightening smoothly reduces inputs toward zero using a quadratic curve. */
    val tighteningThreshold: Float = 10.0f,


    // ══════════════════════════════════════════════════════════
    //  XBOX GYRO AIMING — Only used when Xbox 360 controller is active
    // ══════════════════════════════════════════════════════════

    /**
     * Whether the motion system uses rotation speed (gyro aiming / mouse-like)
     * or absolute tilt angle (steering wheel). This only applies to Xbox mode
     * where gyro data must be converted to stick movement. PS4 mode sends raw
     * sensor data directly to the game.
     */
    val inputMode: InputMode = InputMode.VELOCITY,

    /**
     * Max rotation speed (°/s) for 100% stick output on the Yaw axis (turning left/right).
     * Lower values = higher sensitivity. At this speed, the analog stick hits ±1.0.
     * Example: 200 means you must rotate at 200°/s to get full stick deflection.
     */
    val maxDpsYaw: Float = 200.0f,

    /**
     * Max rotation speed (°/s) for 100% stick output on the Roll axis (tilting sideways).
     * Only meaningful when [horizontalAxis] includes ROLL or MIX.
     */
    val maxDpsRoll: Float = 200.0f,

    /**
     * Max rotation speed (°/s) for 100% stick output on the Pitch axis (tilting forward/back).
     * Controls vertical aiming sensitivity.
     */
    val maxDpsPitch: Float = 200.0f,

    /**
     * Which Xbox analog stick receives the converted gyro movement.
     * RIGHT_STICK is standard for FPS aiming. LEFT_STICK for driving games.
     */
    val xboxTargetStick: XboxTargetStick = XboxTargetStick.RIGHT_STICK,

    /**
     * How gyro-generated stick values combine with physical stick input from the phone.
     * - JOYSTICK_SUPREME: Physical on-screen joystick is supreme; gyro only takes over when stick is idle
     * - ADDITIVE: Gyro fine aiming is added on top of physical thumbstick deflection
     * - MUTE_ON_STICK: Gyro is disabled whenever you touch the physical thumbstick
     */
    val xboxBlendMode: XboxBlendMode = XboxBlendMode.JOYSTICK_SUPREME,

    /**
     * In-game stick deadzone compensation (Anti-Deadzone).
     * Bypasses the minimum 10-18% deadzone enforced by PC games on Xbox thumbsticks.
     * When motion is detected, output begins immediately above the in-game deadzone threshold,
     * delivering 1:1 mouse-like micro-aiming responsiveness that matches DualShock 4.
     */
    val gameDeadzoneCompensation: Float = 0.0f,

    /**
     * How the gyro is activated during gameplay (Xbox mode only).
     * PS4 games handle activation themselves since they receive raw sensor data.
     */
    val activationMode: ActivationMode = ActivationMode.ALWAYS_ON,

    /** Set of button names that must be held to activate gyro (e.g., "LT", "RT"). Empty = always active. */
    val activationButtons: Set<String> = emptySet(),

    /** Whether the Low-Speed Amplifier is enabled. Boosts tiny movements to overcome in-game deadzones. */
    val lowSpeedAmplifierEnabled: Boolean = false,

    /** Multiplier applied to very slow movements. E.g., 2.0 = double speed at near-zero rotation. */
    val lowSpeedAmplifierAmount: Float = 2.0f,

    /** °/s threshold below which the amplifier kicks in. Above this speed, no amplification. */
    val lowSpeedAmplifierThreshold: Float = 20.0f,

    /** How sharply the amplifier transitions from boosted to normal. Higher = sharper drop-off. */
    val lowSpeedAmplifierHarshness: Float = 2.0f,


    // ══════════════════════════════════════════════════════════
    //  STEERING WHEEL — Only used in Absolute Tilt (InputMode.ABSOLUTE_TILT)
    // ══════════════════════════════════════════════════════════

    /**
     * The physical tilt angle (in degrees) that results in 100% stick deflection.
     * Lower values = more sensitive steering. 35° is a natural comfortable range.
     */
    val absoluteMaxTilt: Float = 35.0f,

    /** Horizontal sensitivity multiplier for Steering Wheel mode. 1.0 = 1:1 mapping. */
    val absoluteSensitivityX: Float = 1.0f,

    /** Vertical sensitivity multiplier for Steering Wheel mode. 1.0 = 1:1 mapping. */
    val absoluteSensitivityY: Float = 1.0f,

    /**
     * Non-linear response curve for Steering Wheel mode.
     * 1.0 = linear, 2.0 = quadratic (precise center, fast edges), 3.0 = cubic.
     * Higher values give you more precision near the center of the tilt range.
     */
    val absoluteCurve: Float = 1.0f,


    // ══════════════════════════════════════════════════════════
    //  AIR MOUSE (3D FLIGHT YOKE / ATTITUDE STICK) — InputMode.AIR_MOUSE
    // ══════════════════════════════════════════════════════════

    /**
     * Physical forward/backward tilt angle (pitch) in degrees for 100% stick deflection.
     * 35° is standard for flight simulators (pull back to climb, push forward to dive).
     */
    val airMouseMaxPitch: Float = 35.0f,

    /**
     * Physical sideways banking angle (roll) in degrees for 100% stick deflection.
     * 35° allows natural wing banking turns.
     */
    val airMouseMaxRoll: Float = 35.0f,

    /**
     * Physical flat wrist turn angle (yaw) in degrees for 100% stick deflection.
     */
    val airMouseMaxYaw: Float = 35.0f,

    /** Legacy alias for roll/yaw max angle */
    val airMouseMaxRollYaw: Float = 35.0f,

    /**
     * Center resting deadzone (in degrees) to eliminate hand tremors while holding phone.
     */
    val airMouseDeadzone: Float = 1.5f,

    /**
     * Response curve exponent for flight stick center stability (1.0 = linear, 1.3 = smooth center).
     */
    val airMouseCurve: Float = 1.3f,

    // ── Individual 3D Flight Stick Axis Assignments ──

    /** Target stick and direction for Pitch (forward/back tilt). Default: LS Up/Down (Y). */
    val airMousePitchTarget: FlightStickAxisTarget = FlightStickAxisTarget.LS_Y,

    /** Whether Pitch deflection is inverted. Default: true (Aviation standard: pull back to climb). */
    val airMousePitchInvert: Boolean = true,

    /** Target stick and direction for Roll (sideways tilt / wing bank). Default: LS Left/Right (X). */
    val airMouseRollTarget: FlightStickAxisTarget = FlightStickAxisTarget.LS_X,

    /** Whether Roll deflection is inverted. Default: false. */
    val airMouseRollInvert: Boolean = false,

    /** Target stick and direction for Yaw (flat wrist turn / rudder). Default: NONE (Disabled / Off). */
    val airMouseYawTarget: FlightStickAxisTarget = FlightStickAxisTarget.NONE,

    /** Whether Yaw deflection is inverted. Default: false. */
    val airMouseYawInvert: Boolean = false,

    // Legacy fields preserved for backward compatibility
    val airMouseHorizontalAxis: HorizontalAxis = HorizontalAxis.ROLL,
    val airMouseTargetStick: XboxTargetStick = XboxTargetStick.LEFT_STICK,
    val airMouseInvertPitch: Boolean = true,


    // ══════════════════════════════════════════════════════════
    //  MOTION GESTURES & KINETIC COMBAT — InputMode.GESTURE
    // ══════════════════════════════════════════════════════════

    // ── Card 1: Directional Kinetic Flicks (Up, Down, Left, Right) ──
    /** Master on/off toggle for directional kinetic flicks. */
    val gestureFlicksEnabled: Boolean = true,

    /** Linear acceleration threshold (m/s²) required to trigger a flick. */
    val gestureFlickThreshold: Float = 8.5f,

    /** Debounce cooldown in milliseconds for directional flicks. */
    val gestureFlickCooldownMs: Long = 350L,

    /** Action triggered on Fast UP flick (e.g. flick phone upward). */
    val gestureFlickUpAction: GestureButton = GestureButton.BTN_X,

    /** Action triggered on Fast DOWN flick (e.g. flick phone downward). */
    val gestureFlickDownAction: GestureButton = GestureButton.BTN_Y,

    /** Action triggered on Fast LEFT flick. */
    val gestureFlickLeftAction: GestureButton = GestureButton.SNAP_TURN_180,

    /** Action triggered on Fast RIGHT flick. */
    val gestureFlickRightAction: GestureButton = GestureButton.TRIGGER_RT,

    // ── Card 2: Gravity Tilt Posture (Angle Trigger - Up, Down, Left, Right) ──
    /** Master on/off toggle for gravity tilt posture trigger. */
    val gestureTiltEnabled: Boolean = false,

    /** Tilt angle threshold in degrees for Up/Down pitch postures (default: 30°). */
    val gestureTiltPitchThreshold: Float = 30.0f,

    /** Tilt angle threshold in degrees for Left/Right roll postures (default: 30°). */
    val gestureTiltRollThreshold: Float = 30.0f,

    /** Action triggered when phone is tilted UP (raised past threshold angle). */
    val gestureTiltUpAction: GestureButton = GestureButton.BTN_X,

    /** Action triggered when phone is tilted DOWN (lowered past threshold angle). */
    val gestureTiltDownAction: GestureButton = GestureButton.BTN_Y,

    /** Action triggered when phone is tilted LEFT (banked left past threshold angle). */
    val gestureTiltLeftAction: GestureButton = GestureButton.BTN_LB,

    /** Action triggered when phone is tilted RIGHT (banked right past threshold angle). */
    val gestureTiltRightAction: GestureButton = GestureButton.BTN_RB,

    /** If true, holds button while phone is tilted; if false, pulses button once with hysteresis reset. */
    val gestureTiltHoldMode: Boolean = false,

    // Legacy aliases for backward compatibility
    val gestureTiltAngleThreshold: Float = 30.0f,
    val gestureTiltAction: GestureButton = GestureButton.BTN_X,

    // ── Card 3: Shake Motion Detection ──
    /** Master on/off toggle for vigorous phone shaking detection. */
    val gestureShakeEnabled: Boolean = false,

    /** Acceleration threshold (m/s²) required to register shake oscillation peaks. */
    val gestureShakeThreshold: Float = 11.0f,

    /** Action triggered when phone is shaken vigorously. */
    val gestureShakeAction: GestureButton = GestureButton.BTN_X,

    /** Debounce cooldown in milliseconds between shake triggers. */
    val gestureShakeCooldownMs: Long = 450L,

    /** Mode for joystick motion control while in GESTURE mode. Default: OFF (no joystick deflection). */
    val gestureJoystickMode: GestureJoystickMode = GestureJoystickMode.OFF,

    /** Legacy boolean flag for whether gyro aiming is active in Gesture mode. */
    val gestureAimingEnabled: Boolean = false,

    // ── Legacy Fields Preserved for Backward Compatibility ──
    val gestureThreshold: Float = 8.5f,
    val gestureAction: GestureAction = GestureAction.SNAP_TURN_180,
    val gestureCooldownMs: Long = 400L,
    val gestureSnapTurnDurationMs: Long = 140L,

) {
    /** Resolved effective joystick motion mode for Combat Gestures. */
    val effectiveGestureJoystickMode: GestureJoystickMode
        get() = if (gestureJoystickMode != GestureJoystickMode.OFF) {
            gestureJoystickMode
        } else if (gestureAimingEnabled) {
            GestureJoystickMode.GYRO_AIMING
        } else {
            GestureJoystickMode.OFF
        }
    companion object {

        /** Best defaults for FPS / aiming games (high sensitivity, responsive adaptive smoothing, pure linear 1:1 aiming). */
        val FPS_PRESET = GyroSettings(
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
            gameDeadzoneCompensation = 0.0f,
            xboxBlendMode = XboxBlendMode.JOYSTICK_SUPREME,
        )

        /** Best defaults for racing / steering games (switches to Steering Wheel mode). */
        val RACING_PRESET = GyroSettings(
            inputMode = InputMode.ABSOLUTE_TILT,
            absoluteMaxTilt = 35.0f,
            absoluteSensitivityX = 1.2f,
            absoluteSensitivityY = 1.0f,
            absoluteCurve = 1.5f,
            deadzoneThreshold = 3.0f,
            smoothingEnabled = true,
            smoothingAmount = 0.7f,
            adaptiveSmoothing = false,
            gameDeadzoneCompensation = 0.0f,
            xboxTargetStick = XboxTargetStick.LEFT_STICK,
        )

        /** Best defaults for flight simulators & space combat (3D Flight Yoke / Attitude Stick). */
        val FLIGHT_SIM_PRESET = GyroSettings(
            inputMode = InputMode.AIR_MOUSE,
            airMouseMaxPitch = 35.0f,
            airMouseMaxRoll = 35.0f,
            airMouseMaxYaw = 35.0f,
            airMouseMaxRollYaw = 35.0f,
            airMouseDeadzone = 1.5f,
            airMouseCurve = 1.3f,
            airMousePitchTarget = FlightStickAxisTarget.LS_Y,
            airMousePitchInvert = true,
            airMouseRollTarget = FlightStickAxisTarget.LS_X,
            airMouseRollInvert = false,
            airMouseYawTarget = FlightStickAxisTarget.NONE,
            airMouseYawInvert = false,
            airMouseTargetStick = XboxTargetStick.LEFT_STICK,
            airMouseInvertPitch = true,
            gameDeadzoneCompensation = 0.0f,
        )

        /** Best defaults for action / combat games (kinetic weapon swings, directional flicks & 180° snap turns). */
        val ACTION_COMBAT_PRESET = GyroSettings(
            inputMode = InputMode.GESTURE,
            gestureFlicksEnabled = true,
            gestureFlickThreshold = 8.5f,
            gestureFlickCooldownMs = 350L,
            gestureFlickUpAction = GestureButton.BTN_X,
            gestureFlickDownAction = GestureButton.BTN_Y,
            gestureFlickLeftAction = GestureButton.SNAP_TURN_180,
            gestureFlickRightAction = GestureButton.TRIGGER_RT,
            gestureTiltEnabled = false,
            gestureTiltPitchThreshold = 30.0f,
            gestureTiltRollThreshold = 30.0f,
            gestureTiltUpAction = GestureButton.BTN_X,
            gestureTiltDownAction = GestureButton.BTN_Y,
            gestureTiltLeftAction = GestureButton.BTN_LB,
            gestureTiltRightAction = GestureButton.BTN_RB,
            gestureTiltAngleThreshold = 30.0f,
            gestureTiltAction = GestureButton.BTN_X,
            gestureShakeEnabled = false,
            gestureShakeThreshold = 11.0f,
            gestureShakeAction = GestureButton.BTN_X,
            gestureShakeCooldownMs = 450L,
            gestureThreshold = 8.5f,
            gestureAction = GestureAction.MELEE_RT,
            gestureCooldownMs = 400L,
            maxDpsYaw = 180.0f,
            maxDpsPitch = 220.0f,
            gameDeadzoneCompensation = 0.12f,
            stickResponseCurve = StickResponseCurve.LINEAR,
        )

        /** High-precision preset for slow, accurate aiming (sniping / bow games). */
        val PRECISION_PRESET = GyroSettings(
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
            gameDeadzoneCompensation = 0.12f,
            xboxBlendMode = XboxBlendMode.JOYSTICK_SUPREME,
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

/** Mode for joystick motion control while Combat Gestures (InputMode.GESTURE) is active. */
@Serializable
enum class GestureJoystickMode(val displayName: String, val shortName: String, val description: String) {
    OFF(
        "Off (No Gyro on Joystick)",
        "Off",
        "Joysticks are completely detached from gyro. Pure touch/gamepad sticks only."
    ),
    GYRO_AIMING(
        "Gyro Aiming (Standard 1:1 FPS)",
        "Gyro Aiming",
        "Translates physical wrist velocity into camera stick speed. 1:1 mouse-like aiming."
    ),
    FLIGHT_STICK(
        "3D Flight Stick (Attitude Yoke)",
        "Flight Stick",
        "3D orientation quaternion mapped to analog sticks for aviation and space flight."
    ),
    RAW_SILICON(
        "Raw Silicon Gyro (Esports Direct)",
        "Raw Silicon",
        "Pure MEMS silicon sensor stream without software filtering."
    )
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
        InputMode.GESTURE -> {
            when (effectiveGestureJoystickMode) {
                GestureJoystickMode.FLIGHT_STICK -> NexpadProtocol.SENSOR_MODE_GAME_ROTATION
                GestureJoystickMode.RAW_SILICON -> NexpadProtocol.SENSOR_MODE_RAW_UNCALIBRATED
                else -> NexpadProtocol.SENSOR_MODE_LINEAR_GESTURE
            }
        }
        InputMode.RAW_UNCALIBRATED -> NexpadProtocol.SENSOR_MODE_RAW_UNCALIBRATED
    }
}

