package com.sanket.tools.nexpaddesktop.model

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
data class GyroSettings(

    // ══════════════════════════════════════════════════════════
    //  SHARED — Affects both Xbox 360 and PS4 modes
    // ══════════════════════════════════════════════════════════

    /** Master on/off switch for all motion processing. When false, no gyro data is processed. */
    val enabled: Boolean = true,

    /** If true, horizontal (left/right) camera movement is reversed. Applies to both gyro and accel modes. */
    val invertX: Boolean = false,

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
    val deadzoneThreshold: Float = 3.0f,

    /** Whether EMA (Exponential Moving Average) smoothing is applied to gyro output. */
    val smoothingEnabled: Boolean = true,

    /**
     * EMA alpha coefficient. Controls how much each new frame blends with history.
     * - 0.1 = very heavy smoothing (laggy but silky smooth)
     * - 0.7 = balanced (default — good for gaming)
     * - 1.0 = no smoothing (raw, responsive but jittery)
     */
    val smoothingAmount: Float = 0.7f,

    /**
     * If true, only smooth slow movements. Fast flicks pass through unsmoothed
     * for maximum responsiveness. Slow movements get full smoothing to remove jitter.
     */
    val adaptiveSmoothing: Boolean = true,

    /** °/s speed below which adaptive smoothing applies full smoothing. Above this, smoothing fades out. */
    val smoothingThreshold: Float = 30.0f,

    /** Whether an acceleration curve is applied to gyro output (speed-dependent sensitivity). */
    val accelerationEnabled: Boolean = true,

    /** The shape of the acceleration curve interpolation between min/max thresholds. */
    val accelerationType: AccelerationType = AccelerationType.LINEAR,

    /** Sensitivity multiplier at very slow rotation speeds. E.g., 0.8 = 80% of base speed. */
    val minSensitivity: Float = 0.8f,

    /** Sensitivity multiplier at very fast rotation speeds. E.g., 2.5 = 250% of base speed. */
    val maxSensitivity: Float = 2.5f,

    /** °/s rotation speed at which [minSensitivity] applies (lower bound of the curve). */
    val minThreshold: Float = 0.0f,

    /** °/s rotation speed at which [maxSensitivity] applies (upper bound of the curve). */
    val maxThreshold: Float = 100.0f,

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
     * - OVERRIDE: Gyro replaces physical stick when motion is detected
     * - ADDITIVE: Gyro movement is added on top of physical stick input
     * - MUTE_ON_STICK: Gyro is disabled whenever you touch the physical stick
     */
    val xboxBlendMode: XboxBlendMode = XboxBlendMode.OVERRIDE,

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
     * Lower values = more sensitive steering. 45° is a natural comfortable range.
     */
    val absoluteMaxTilt: Float = 45.0f,

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

) {
    companion object {

        /** Best defaults for FPS / aiming games (high sensitivity, adaptive smoothing). */
        val FPS_PRESET = GyroSettings(
            maxDpsYaw = 133f,
            maxDpsRoll = 133f,
            maxDpsPitch = 238f,
            deadzoneThreshold = 3.0f,
            smoothingEnabled = true,
            smoothingAmount = 0.7f,
            adaptiveSmoothing = true,
            smoothingThreshold = 30.0f,
            accelerationEnabled = true,
            accelerationType = AccelerationType.LINEAR,
            minSensitivity = 0.8f,
            maxSensitivity = 2.5f,
            minThreshold = 0.0f,
            maxThreshold = 100.0f,
        )

        /** Best defaults for racing / steering games (switches to Steering Wheel mode). */
        val RACING_PRESET = GyroSettings(
            inputMode = InputMode.ABSOLUTE_TILT,
            absoluteMaxTilt = 40.0f,
            absoluteSensitivityX = 1.2f,
            absoluteSensitivityY = 1.0f,
            absoluteCurve = 1.5f,
            deadzoneThreshold = 5.0f,
            smoothingEnabled = true,
            smoothingAmount = 0.5f,
            adaptiveSmoothing = false,
        )

        /** High-precision preset for slow, accurate aiming (sniping / bow games). */
        val PRECISION_PRESET = GyroSettings(
            maxDpsYaw = 250f,
            maxDpsRoll = 250f,
            maxDpsPitch = 444f,
            deadzoneThreshold = 2.0f,
            smoothingEnabled = true,
            smoothingAmount = 0.6f,
            adaptiveSmoothing = true,
            smoothingThreshold = 25.0f,
            accelerationEnabled = false,
        )

        /** Factory defaults — all settings at their neutral/default values. */
        val DEFAULT = GyroSettings()
    }
}

// ══════════════════════════════════════════════════════════════
//  SUPPORTING ENUMS
// ══════════════════════════════════════════════════════════════

/** Which physical rotation axis maps to horizontal camera movement. */
enum class HorizontalAxis(val displayName: String) {
    YAW("Yaw (Turn wrist left/right)"),
    ROLL("Roll (Tilt controller sideways)"),
    MIX("Mix (Both Yaw and Roll)")
}

/** Type of acceleration curve applied to gyro input. */
enum class AccelerationType(val displayName: String) {
    LINEAR("Linear Interpolation"),
    POWER("Power Curve"),
    SMOOTH_STEP("Smooth Step (S-Curve)")
}

/** How the gyro is activated during gameplay. */
enum class ActivationMode(val displayName: String) {
    ALWAYS_ON("Always On"),
    BUTTON_HOLD("Button Hold"),
    TOGGLE("Toggle On/Off")
}

/** Determines how physical phone movement translates to stick movement. */
enum class InputMode(val displayName: String) {
    VELOCITY("Velocity (Aiming / Mouse-like)"),
    ABSOLUTE_TILT("Absolute Tilt (Steering Wheel)")
}

/** Which Xbox analog stick receives the converted gyro movement. */
enum class XboxTargetStick(val displayName: String) {
    LEFT_STICK("Left Stick"),
    RIGHT_STICK("Right Stick")
}

/** How gyro-generated stick values combine with physical stick input. */
enum class XboxBlendMode(val displayName: String) {
    OVERRIDE("Override"),
    ADDITIVE("Additive"),
    MUTE_ON_STICK("Mute on Stick")
}
