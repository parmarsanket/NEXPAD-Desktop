package com.sanket.tools.nexpaddesktop.model

/**
 * Complete configuration for the 6-axis gyro aiming system.
 *
 * Settings are organized into logical groups that mirror the processing pipeline:
 *   Raw Gyro → Axis Remap → Deadzone → Smoothing → Acceleration → Sensitivity → Output
 */
data class GyroSettings(
    // ── Core ──────────────────────────────────────────────
    val enabled: Boolean = true,

    // ── Sensitivity ──────────────────────────────────────
    /** Horizontal sensitivity multiplier (0.1 – 5.0). Default 1.0 = 1:1 mapping. */
    val sensitivityX: Float = 1.0f,
    /** Vertical sensitivity multiplier. Default 1.0. */
    val sensitivityY: Float = 1.0f,

    // ── Axis Control ─────────────────────────────────────
    val invertX: Boolean = false,
    val invertY: Boolean = false,
    /** Which physical rotation maps to horizontal camera movement. */
    val horizontalAxis: HorizontalAxis = HorizontalAxis.MIX,

    // ── Deadzone ─────────────────────────────────────────
    /** Rotation speed (°/s) below which input is ignored. Smooth ramp exit. */
    val deadzoneThreshold: Float = 3.0f,

    // ── Smoothing ────────────────────────────────────────
    val smoothingEnabled: Boolean = true,
    /**
     * EMA alpha (0.0 = maximum smoothing/lag, 1.0 = no smoothing).
     * Default 0.7 is a good balance for gaming.
     */
    val smoothingAmount: Float = 0.7f,
    /** If true, only smooth slow movements; fast movements pass through raw. */
    val adaptiveSmoothing: Boolean = true,
    /** °/s threshold below which adaptive smoothing kicks in. */
    val smoothingThreshold: Float = 30.0f,

    // ── Acceleration Curve ───────────────────────────────
    val accelerationEnabled: Boolean = true,
    val accelerationType: AccelerationType = AccelerationType.LINEAR,
    /** Sensitivity multiplier at slow rotation speeds. */
    val minSensitivity: Float = 0.8f,
    /** Sensitivity multiplier at fast rotation speeds. */
    val maxSensitivity: Float = 2.5f,
    /** °/s rotation speed at which minSensitivity applies. */
    val minThreshold: Float = 0.0f,
    /** °/s rotation speed at which maxSensitivity applies. */
    val maxThreshold: Float = 100.0f,

    // ── Tightening (soft deadzone for micro-precision) ───
    val tighteningEnabled: Boolean = false,
    /** °/s below which inputs are smoothly reduced toward zero. */
    val tighteningThreshold: Float = 10.0f,

    // ── Activation ───────────────────────────────────────
    val activationMode: ActivationMode = ActivationMode.ALWAYS_ON,
    val activationButtons: Set<String> = emptySet(),

    // ── Xbox-specific (gyro-to-stick emulation) ──────────
    val xboxTargetStick: String = "RIGHT_STICK",
    val xboxBlendMode: String = "OVERRIDE",
) {
    companion object {
        /** Best defaults for FPS / aiming games. */
        val FPS_PRESET = GyroSettings(
            sensitivityX = 1.5f,
            sensitivityY = 0.84f,         // 56% of 1.5
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

        /** Best defaults for racing / steering games. */
        val RACING_PRESET = GyroSettings(
            sensitivityX = 2.0f,
            sensitivityY = 1.0f,
            deadzoneThreshold = 5.0f,
            smoothingEnabled = true,
            smoothingAmount = 0.5f,
            adaptiveSmoothing = false,
            accelerationEnabled = true,
            accelerationType = AccelerationType.LINEAR,
            minSensitivity = 1.0f,
            maxSensitivity = 3.0f,
            minThreshold = 0.0f,
            maxThreshold = 80.0f,
        )

        /** High-precision preset for slow, accurate aiming. */
        val PRECISION_PRESET = GyroSettings(
            sensitivityX = 0.8f,
            sensitivityY = 0.45f,
            deadzoneThreshold = 2.0f,
            smoothingEnabled = true,
            smoothingAmount = 0.6f,
            adaptiveSmoothing = true,
            smoothingThreshold = 25.0f,
            accelerationEnabled = false,
        )

        /** Factory defaults. */
        val DEFAULT = GyroSettings()
    }
}

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
