package com.sanket.tools.nexpaddesktop.driver

import com.sanket.tools.nexpaddesktop.model.*
import kotlin.math.*

/**
 * Stateful processor for gyroscope and accelerometer data.
 *
 * This class acts as the mathematical **State Layer**. It intercepts raw rad/s 
 * gyro data and applies a full deadzone, tightening, and exponential moving average 
 * (EMA) smoothing pipeline before passing the filtered data on to the driver.
 *
 * ## Coordinate Systems
 * - **Android Phone (Landscape):**
 *   - Gyro X (Pitch): Tilting phone forward
 *   - Gyro Y (Roll): Tilting phone sideways
 *   - Gyro Z (Yaw): Turning phone flat on a table
 */
class GyroProcessor {

    // =========================================================================
    // STATE VARIABLES
    // =========================================================================
    
    /** EMA History for X-axis */
    private var smoothedX = 0f
    
    /** EMA History for Y-axis */
    private var smoothedY = 0f

    private var toggleActive = false
    private var toggleButtonWasPressed = false

    /**
     * DTO representing the fully processed gyroscope data.
     * All output values are in degrees per second (°/s).
     */
    data class ProcessedGyro(
        val yawDps: Float,    
        val pitchDps: Float,  
        val rollDps: Float,   
        val rawYawDps: Float, 
        val rawPitchDps: Float,
    )

    /**
     * Processes one frame of raw gyroscope data through the mathematical pipeline.
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

        if (!settings.enabled || !isGyroActive(settings, isActivationButtonPressed)) {
            smoothedX = 0f
            smoothedY = 0f
            return ProcessedGyro(0f, 0f, 0f, 0f, 0f)
        }

        // ---------------------------------------------------------------------
        // Absolute Tilt Mode (e.g., Steering Wheel emulation)
        // ---------------------------------------------------------------------
        if (settings.inputMode == InputMode.ABSOLUTE_TILT) {
            val maxAccel = 9.8f
            val targetStickRange = 200.0f

            val horizontalAngleDeg = asin((rawAccelX / maxAccel).coerceIn(-1.0f, 1.0f)) * RAD_TO_DEG
            val verticalAngleDeg = asin((rawAccelY / maxAccel).coerceIn(-1.0f, 1.0f)) * RAD_TO_DEG

            var mappedH = (horizontalAngleDeg / settings.absoluteMaxTilt)
            var mappedV = (verticalAngleDeg / settings.absoluteMaxTilt)

            val curveH = abs(mappedH).pow(settings.absoluteCurve) * sign(mappedH)
            val curveV = abs(mappedV).pow(settings.absoluteCurve) * sign(mappedV)

            mappedH = curveH * targetStickRange
            mappedV = curveV * targetStickRange

            var hVal = if (settings.invertX) -mappedH else mappedH
            var vVal = if (settings.invertY) -mappedV else mappedV

            hVal *= settings.absoluteSensitivityX
            vVal *= settings.absoluteSensitivityY

            return ProcessedGyro(hVal, vVal, 0f, mappedH, mappedV)
        }

        // ---------------------------------------------------------------------
        // Gyroscope Mode (Standard View Controller)
        // ---------------------------------------------------------------------
        val pitchDps = rawGyroX * RAD_TO_DEG   
        val yawDps   = rawGyroZ * RAD_TO_DEG   
        val rollDps  = rawGyroY * RAD_TO_DEG   

        val horizontalDps = when (settings.horizontalAxis) {
            HorizontalAxis.YAW  -> -yawDps
            HorizontalAxis.ROLL -> rollDps
            HorizontalAxis.MIX  -> -yawDps + rollDps 
        }
        val verticalDps = -pitchDps   

        val rawH = horizontalDps
        val rawV = verticalDps

        var hVal = if (settings.invertX) -horizontalDps else horizontalDps
        var vVal = if (settings.invertY) -verticalDps else verticalDps

        // Applying mathematical filters...
        hVal = applyDeadzone(hVal, settings.deadzoneThreshold)
        vVal = applyDeadzone(vVal, settings.deadzoneThreshold)

        if (settings.tighteningEnabled) {
            hVal = applyTightening(hVal, settings.tighteningThreshold)
            vVal = applyTightening(vVal, settings.tighteningThreshold)
        }

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

        if (settings.accelerationEnabled) {
            hVal *= computeAccelerationSensitivity(abs(hVal), settings)
            vVal *= computeAccelerationSensitivity(abs(vVal), settings)
        }

        hVal = applyLowSpeedAmplifier(hVal, settings)
        vVal = applyLowSpeedAmplifier(vVal, settings)

        val effectiveSensX = when (settings.horizontalAxis) {
            HorizontalAxis.YAW -> 200.0f / settings.maxDpsYaw
            HorizontalAxis.ROLL -> 200.0f / settings.maxDpsRoll
            HorizontalAxis.MIX -> 200.0f / settings.maxDpsYaw
        }
        
        hVal *= effectiveSensX
        vVal *= (200.0f / settings.maxDpsPitch)

        return ProcessedGyro(hVal, vVal, rollDps, rawH, rawV)
    }

    fun reset() {
        smoothedX = 0f
        smoothedY = 0f
        toggleActive = false
        toggleButtonWasPressed = false
    }

    // =========================================================================
    // PRIVATE MATHEMATICAL UTILITIES
    // =========================================================================

    private fun applyDeadzone(value: Float, threshold: Float): Float {
        if (threshold <= 0f) return value
        val absVal = abs(value)
        if (absVal < threshold) return 0f
        return sign(value) * (absVal - threshold)
    }

    private fun applyTightening(value: Float, threshold: Float): Float {
        if (threshold <= 0f) return value
        val absVal = abs(value)
        if (absVal >= threshold) return value
        val t = absVal / threshold 
        return sign(value) * (t * t * threshold)
    }

    private fun applyLowSpeedAmplifier(value: Float, settings: GyroSettings): Float {
        if (!settings.lowSpeedAmplifierEnabled) return value
        val speed = abs(value)
        if (speed >= settings.lowSpeedAmplifierThreshold || settings.lowSpeedAmplifierThreshold <= 0f) {
            return value
        }
        val slownessRatio = 1f - (speed / settings.lowSpeedAmplifierThreshold)
        val curve = slownessRatio.pow(settings.lowSpeedAmplifierHarshness)
        val multiplier = 1.0f + (settings.lowSpeedAmplifierAmount - 1.0f) * curve
        return value * multiplier
    }

    private fun computeAdaptiveAlpha(speed: Float, threshold: Float, baseAlpha: Float): Float {
        if (threshold <= 0f) return baseAlpha
        val alphaMin = baseAlpha * 0.5f 
        val alphaMax = 1.0f              
        return when {
            speed >= threshold -> alphaMax
            speed <= threshold / 2f -> alphaMin
            else -> {
                val t = (speed - threshold / 2f) / (threshold / 2f)
                alphaMin + t * (alphaMax - alphaMin)
            }
        }
    }

    private fun computeAccelerationSensitivity(speed: Float, settings: GyroSettings): Float {
        val minT = settings.minThreshold
        val maxT = settings.maxThreshold
        if (maxT <= minT) return settings.minSensitivity
        return when {
            speed <= minT -> settings.minSensitivity
            speed >= maxT -> settings.maxSensitivity
            else -> {
                val t = (speed - minT) / (maxT - minT) 
                val curved = when (settings.accelerationType) {
                    AccelerationType.LINEAR -> t
                    AccelerationType.POWER -> t.pow(2.0f)
                    AccelerationType.SMOOTH_STEP -> t * t * (3f - 2f * t)
                }
                settings.minSensitivity + curved * (settings.maxSensitivity - settings.minSensitivity)
            }
        }
    }

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

    companion object {
        const val RAD_TO_DEG = 57.2957795f
    }
}
