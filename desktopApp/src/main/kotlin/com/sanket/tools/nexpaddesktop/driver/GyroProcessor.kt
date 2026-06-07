package com.sanket.tools.nexpaddesktop.driver

import com.sanket.tools.nexpaddesktop.model.*
import kotlin.math.*

/**
 * Processes raw gyroscope data through a multi-stage pipeline:
 *
 *   Raw (rad/s) → °/s → Axis Remap → Deadzone → Smoothing → Acceleration → Sensitivity → Output
 *
 * This is a stateful class — it maintains smoothing history between frames.
 * Create ONE instance and call [process] every frame (~60 Hz).
 *
 * Based on best practices from GyroWiki / JibbSmart / Steam Input / JoyShockMapper.
 */
class GyroProcessor {

    // ── Smoothing state (EMA previous values) ────────────
    private var smoothedX = 0f
    private var smoothedY = 0f

    // ── Toggle state for TOGGLE activation mode ──────────
    private var toggleActive = false
    private var toggleButtonWasPressed = false

    /** Result of processing one frame of gyro data. Values are in °/s after full pipeline. */
    data class ProcessedGyro(
        val yawDps: Float,    // Horizontal camera movement (°/s)
        val pitchDps: Float,  // Vertical camera movement (°/s)
        val rollDps: Float,   // Roll (rarely used for camera, but available)
        val rawYawDps: Float,   // Pre-pipeline values for debug display
        val rawPitchDps: Float,
    )

    /**
     * Process one frame of raw gyroscope data through the full pipeline.
     *
     * @param rawGyroX  Android gyroX in rad/s (pitch — tilting phone forward/back)
     * @param rawGyroY  Android gyroY in rad/s (roll — tilting phone sideways)
     * @param rawGyroZ  Android gyroZ in rad/s (yaw — rotating phone flat on table)
     * @param settings  Current user settings
     * @param isActivationButtonPressed  Whether the activation button(s) are currently held
     * @return Processed gyro values in °/s ready for controller mapping
     */
    fun process(
        rawGyroX: Float,
        rawGyroY: Float,
        rawGyroZ: Float,
        rawAccelX: Float = 0f,
        rawAccelY: Float = 0f,
        rawAccelZ: Float = 0f,
        settings: GyroSettings,
        isActivationButtonPressed: Boolean = true,
    ): ProcessedGyro {

        // ── 0. Check if gyro is enabled and activated ────
        if (!settings.enabled) {
            return ProcessedGyro(0f, 0f, 0f, 0f, 0f)
        }

        if (!isGyroActive(settings, isActivationButtonPressed)) {
            // Reset smoothing state when gyro is deactivated to prevent stale data
            smoothedX = 0f
            smoothedY = 0f
            return ProcessedGyro(0f, 0f, 0f, 0f, 0f)
        }

        // ── 0.5. Absolute Tilt Mode (Steering Wheel) ─────
        if (settings.inputMode == InputMode.ABSOLUTE_TILT) {
            val maxAccel = 9.8f
            // In NEXPAD main.kt, stick = (processed.yawDps / 200.0f)
            val targetStickRange = 200.0f

            // Calculate actual tilt angles in degrees using arcsin(g / 9.8)
            val horizontalAngleDeg = asin((rawAccelX / maxAccel).coerceIn(-1.0f, 1.0f)) * 57.2957795f
            val verticalAngleDeg = asin((rawAccelY / maxAccel).coerceIn(-1.0f, 1.0f)) * 57.2957795f

            // Map the angle relative to the user's max tilt threshold (e.g. 45 degrees)
            // This maps X and Y independently (Square boundary) so you can get full speed easily
            val mappedH = (horizontalAngleDeg / settings.absoluteMaxTilt) * targetStickRange
            val mappedV = (verticalAngleDeg / settings.absoluteMaxTilt) * targetStickRange

            // Apply inversion
            var hVal = if (settings.invertX) -mappedH else mappedH
            var vVal = if (settings.invertY) -mappedV else mappedV

            // Apply deadzone
            hVal = applyDeadzone(hVal, settings.deadzoneThreshold)
            vVal = applyDeadzone(vVal, settings.deadzoneThreshold)

            // Apply simple sensitivities
            hVal *= settings.sensitivityX
            vVal *= settings.sensitivityY

            return ProcessedGyro(
                yawDps = hVal,
                pitchDps = vVal,
                rollDps = 0f,
                rawYawDps = mappedH,
                rawPitchDps = mappedV,
            )
        }

        // ── 1. Convert rad/s → °/s ──────────────────────
        val RAD_TO_DEG = 57.2957795f
        val pitchDps = rawGyroX * RAD_TO_DEG   // Tilt forward/back
        val yawDps   = rawGyroZ * RAD_TO_DEG   // Turn left/right (flat rotation)
        val rollDps  = rawGyroY * RAD_TO_DEG   // Tilt sideways

        // ── 2. Axis remapping ───────────────────────────
        // Horizontal camera = Yaw (default) or Roll (user preference)
        val horizontalDps = when (settings.horizontalAxis) {
            HorizontalAxis.YAW  -> -yawDps   // Negated: turning phone right → camera goes right
            HorizontalAxis.ROLL -> rollDps
            HorizontalAxis.MIX  -> -yawDps + rollDps // Both turning and tilting contribute
        }
        val verticalDps = -pitchDps   // Negated: tilting phone forward → camera goes down

        // Save raw values for debug display (before further processing)
        val rawH = horizontalDps
        val rawV = verticalDps

        // ── 3. Apply inversion ──────────────────────────
        var hVal = if (settings.invertX) -horizontalDps else horizontalDps
        var vVal = if (settings.invertY) -verticalDps else verticalDps

        // ── 4. Deadzone with smooth ramp ────────────────
        hVal = applyDeadzone(hVal, settings.deadzoneThreshold)
        vVal = applyDeadzone(vVal, settings.deadzoneThreshold)

        // ── 5. Tightening (soft deadzone for micro-precision) ──
        if (settings.tighteningEnabled) {
            hVal = applyTightening(hVal, settings.tighteningThreshold)
            vVal = applyTightening(vVal, settings.tighteningThreshold)
        }

        // ── 6. Smoothing (EMA filter) ───────────────────
        if (settings.smoothingEnabled) {
            val speedH = abs(hVal)
            val speedV = abs(vVal)
            val maxSpeed = max(speedH, speedV)

            val alpha = if (settings.adaptiveSmoothing) {
                computeAdaptiveAlpha(maxSpeed, settings.smoothingThreshold, settings.smoothingAmount)
            } else {
                settings.smoothingAmount
            }

            smoothedX = alpha * hVal + (1f - alpha) * smoothedX
            smoothedY = alpha * vVal + (1f - alpha) * smoothedY
            hVal = smoothedX
            vVal = smoothedY
        }

        // ── 7. Acceleration curve ───────────────────────
        if (settings.accelerationEnabled) {
            val speedH = abs(hVal)
            val speedV = abs(vVal)
            val sensH = computeAccelerationSensitivity(speedH, settings)
            val sensV = computeAccelerationSensitivity(speedV, settings)
            hVal *= sensH
            vVal *= sensV
        }

        // ── 7.5 Low-Speed Amplifier (Velocity Mode) ─────
        hVal = applyLowSpeedAmplifier(hVal, settings)
        vVal = applyLowSpeedAmplifier(vVal, settings)

        // ── 8. Final sensitivity multiplier ─────────────
        hVal *= settings.sensitivityX
        vVal *= settings.sensitivityY

        return ProcessedGyro(
            yawDps = hVal,
            pitchDps = vVal,
            rollDps = rollDps,
            rawYawDps = rawH,
            rawPitchDps = rawV,
        )
    }

    /**
     * Reset all internal state (smoothing history, toggle state).
     * Call when reconnecting or recalibrating.
     */
    fun reset() {
        smoothedX = 0f
        smoothedY = 0f
        toggleActive = false
        toggleButtonWasPressed = false
    }


    // ════════════════════════════════════════════════════════
    //  PRIVATE HELPERS
    // ════════════════════════════════════════════════════════

    /**
     * Apply deadzone with smooth linear ramp exit.
     * Values below threshold → 0. Values above → smoothly ramped from 0.
     */
    private fun applyDeadzone(value: Float, threshold: Float): Float {
        if (threshold <= 0f) return value
        val absVal = abs(value)
        if (absVal < threshold) return 0f
        // Smooth ramp: remap [threshold, ∞) → [0, ∞)
        return sign(value) * (absVal - threshold)
    }

    /**
     * Tightening: smoothly reduce very small inputs toward zero without a hard cutoff.
     * Uses a power curve on the normalized value within the tightening range.
     */
    private fun applyTightening(value: Float, threshold: Float): Float {
        if (threshold <= 0f) return value
        val absVal = abs(value)
        if (absVal >= threshold) return value
        // Quadratic reduction within the tightening range
        val t = absVal / threshold  // 0..1
        val reduced = t * t * threshold  // Quadratic: small values get much smaller
        return sign(value) * reduced
    }

    /**
     * Low-Speed Amplifier: Boosts very small movements to help overcome in-game deadzones.
     */
    private fun applyLowSpeedAmplifier(value: Float, settings: GyroSettings): Float {
        if (!settings.lowSpeedAmplifierEnabled) return value
        val speed = abs(value)
        if (speed >= settings.lowSpeedAmplifierThreshold || settings.lowSpeedAmplifierThreshold <= 0f) {
            return value
        }
        
        // Ratio of how close we are to 0 speed (1.0 = completely still, 0.0 = at threshold)
        val slownessRatio = 1f - (speed / settings.lowSpeedAmplifierThreshold)
        
        // Apply harshness curve (e.g. if harshness is 2.0, it's a squared curve)
        val curve = slownessRatio.pow(settings.lowSpeedAmplifierHarshness)
        
        // Multiplier smoothly goes from `Amount` (at 0 speed) down to 1.0 (at threshold)
        val multiplier = 1.0f + (settings.lowSpeedAmplifierAmount - 1.0f) * curve
        
        return value * multiplier
    }

    /**
     * Adaptive smoothing: heavy smoothing for slow movements (where jitter is visible),
     * no smoothing for fast movements (where responsiveness matters).
     *
     * @param speed     Current rotation speed in °/s
     * @param threshold Speed below which full smoothing kicks in
     * @param baseAlpha The user's configured smoothing amount
     * @return Alpha value for EMA (closer to 1.0 = less smoothing)
     */
    private fun computeAdaptiveAlpha(speed: Float, threshold: Float, baseAlpha: Float): Float {
        if (threshold <= 0f) return baseAlpha
        val alphaMin = baseAlpha * 0.5f  // Maximum smoothing
        val alphaMax = 1.0f              // No smoothing

        return when {
            speed >= threshold -> alphaMax
            speed <= threshold / 2f -> alphaMin
            else -> {
                val t = (speed - threshold / 2f) / (threshold / 2f)
                alphaMin + t * (alphaMax - alphaMin)
            }
        }
    }

    /**
     * Compute acceleration sensitivity based on current rotation speed.
     * Interpolates between [minSensitivity] at [minThreshold] and [maxSensitivity] at [maxThreshold].
     */
    private fun computeAccelerationSensitivity(speed: Float, settings: GyroSettings): Float {
        val minT = settings.minThreshold
        val maxT = settings.maxThreshold
        val minS = settings.minSensitivity
        val maxS = settings.maxSensitivity

        if (maxT <= minT) return minS

        return when {
            speed <= minT -> minS
            speed >= maxT -> maxS
            else -> {
                val t = (speed - minT) / (maxT - minT)  // 0..1
                val curved = when (settings.accelerationType) {
                    AccelerationType.LINEAR -> t
                    AccelerationType.POWER -> t.pow(2.0f)
                    AccelerationType.SMOOTH_STEP -> t * t * (3f - 2f * t)  // Hermite smoothstep
                }
                minS + curved * (maxS - minS)
            }
        }
    }

    /**
     * Check whether gyro should be active based on activation mode.
     */
    private fun isGyroActive(settings: GyroSettings, isButtonPressed: Boolean): Boolean {
        return when (settings.activationMode) {
            ActivationMode.ALWAYS_ON -> true
            ActivationMode.BUTTON_HOLD -> isButtonPressed
            ActivationMode.TOGGLE -> {
                if (isButtonPressed && !toggleButtonWasPressed) {
                    toggleActive = !toggleActive
                }
                toggleButtonWasPressed = isButtonPressed
                toggleActive
            }
        }
    }
}
