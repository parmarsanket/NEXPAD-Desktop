package com.sanket.tools.nexpaddesktop.driver

import com.sanket.tools.nexpad.model.GamepadInput
import com.sanket.tools.nexpad.model.NexpadKeys
import com.sanket.tools.nexpaddesktop.model.*
import com.sanket.tools.nexpaddesktop.ui.ControllerType
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

object InputPipeline {
    
    fun processInput(
        input: GamepadInput,
        activeController: ControllerType,
        gyroProcessor: GyroProcessor,
        gyroSettings: GyroSettings,
        lsSensitivityX: Float,
        lsSensitivityY: Float,
        rsSensitivityX: Float,
        rsSensitivityY: Float,
        onProcessedAngles: (yawDps: Float, pitchDps: Float) -> Unit
    ): GamepadInput {
        
        // 1. Check gyro activation buttons (Xbox only — PS4 games handle this)
        val isActivationButtonPressed = if (gyroSettings.activationButtons.isEmpty()) {
            true
        } else {
            gyroSettings.activationButtons.any { btn ->
                when (btn) {
                    NexpadKeys.LT -> input.triggerL2 > 0.1f
                    NexpadKeys.RT -> input.triggerR2 > 0.1f
                    NexpadKeys.LB -> input.btnL1
                    NexpadKeys.RB -> input.btnR1
                    NexpadKeys.A  -> input.btnA
                    NexpadKeys.B  -> input.btnB
                    NexpadKeys.X  -> input.btnX
                    NexpadKeys.Y  -> input.btnY
                    else          -> false
                }
            }
        }

        // 2. Run gyro through the processing pipeline
        val processed = gyroProcessor.process(
            rawGyroX = input.gyroX,
            rawGyroY = input.gyroY,
            rawGyroZ = input.gyroZ,
            rawAccelX = input.accelX,
            rawAccelY = input.accelY,
            rawAccelZ = input.accelZ,
            rawGravityX = input.gravityX,
            rawGravityY = input.gravityY,
            rawGravityZ = input.gravityZ,
            quatX = input.quatX,
            quatY = input.quatY,
            quatZ = input.quatZ,
            quatW = input.quatW,
            linearAccelX = input.linearAccelX,
            linearAccelY = input.linearAccelY,
            linearAccelZ = input.linearAccelZ,
            rawUncalGyroX = input.rawGyroX,
            rawUncalGyroY = input.rawGyroY,
            rawUncalGyroZ = input.rawGyroZ,
            gyroBiasX = input.gyroBiasX,
            gyroBiasY = input.gyroBiasY,
            gyroBiasZ = input.gyroBiasZ,
            sensorFlags = input.sensorFlags,
            settings = gyroSettings,
            isActivationButtonPressed = isActivationButtonPressed,
        )

        onProcessedAngles(processed.yawDps, processed.pitchDps)

        // 3. Build final stick values and button states
        var finalLeftX = input.leftStickX
        var finalLeftY = input.leftStickY
        var finalRightX = input.rightStickX
        var finalRightY = input.rightStickY
        var finalTriggerL2 = input.triggerL2
        var finalTriggerR2 = input.triggerR2
        var finalBtnA = input.btnA
        var finalBtnB = input.btnB
        var finalBtnX = input.btnX
        var finalBtnY = input.btnY
        var finalBtnL1 = input.btnL1
        var finalBtnR1 = input.btnR1
        var finalBtnL3 = input.btnL3
        var finalBtnR3 = input.btnR3
        var finalDpadUp = input.dpadUp
        var finalDpadDown = input.dpadDown
        var finalDpadLeft = input.dpadLeft
        var finalDpadRight = input.dpadRight

        if (activeController == ControllerType.XBOX_360 && gyroSettings.enabled) {
            if (gyroSettings.inputMode == InputMode.AIR_MOUSE ||
                (gyroSettings.inputMode == InputMode.GESTURE && gyroSettings.effectiveGestureJoystickMode == GestureJoystickMode.FLIGHT_STICK)) {
                // 3D Flight Stick: Direct independent axis routing to Left Stick & Right Stick
                fun blendFlight(physical: Float, flight: Float): Float {
                    val isPhysActive = abs(physical) > 0.05f
                    val deadzone = if (gyroSettings.inputMode == InputMode.AIR_MOUSE) {
                        0f
                    } else {
                        gyroSettings.gameDeadzoneCompensation.coerceIn(0f, 0.35f)
                    }
                    val activeFlight = if (abs(flight) > 0.0001f && deadzone > 0f) {
                        val clamped = abs(flight).coerceAtMost(1.0f)
                        kotlin.math.sign(flight) * (deadzone + (1.0f - deadzone) * clamped)
                    } else {
                        flight
                    }
                    return when (gyroSettings.xboxBlendMode) {
                        XboxBlendMode.JOYSTICK_SUPREME -> if (isPhysActive) physical else activeFlight
                        XboxBlendMode.ADDITIVE -> if (isPhysActive) (physical + activeFlight).coerceIn(-1f, 1f) else activeFlight
                        XboxBlendMode.MUTE_ON_STICK -> if (isPhysActive) physical else activeFlight
                    }
                }

                finalLeftX = blendFlight(input.leftStickX, processed.flightLsX)
                finalLeftY = blendFlight(input.leftStickY, processed.flightLsY)
                finalRightX = blendFlight(input.rightStickX, processed.flightRsX)
                finalRightY = blendFlight(input.rightStickY, processed.flightRsY)
            } else {
                val gyroToStickScale = 1.0f / 200.0f
                val rawStickX = (processed.yawDps * gyroToStickScale).coerceIn(-1f, 1f)
                val rawStickY = (processed.pitchDps * gyroToStickScale).coerceIn(-1f, 1f)

                // 1. Stick Response Curve Shaping (Per-Axis Independent - Free Gyro Unconstrained)
                // Gyroscope, gravity, and motion sensors operate with independent physical degrees of freedom.
                // Applying the response curve per-axis rather than clamping a 2D circular vector
                // ensures horizontal steering and vertical pitch never choke each other, and diagonal
                // tilts (e.g. top-right) achieve full 100% saturation (x=1.0, y=1.0).
                val exponent = gyroSettings.stickResponseCurve.exponent
                fun applyStickCurve(valIn: Float): Float {
                    val absVal = abs(valIn)
                    if (absVal <= 0.0001f) return 0f
                    val clamped = absVal.coerceAtMost(1.0f)
                    val curved = if (exponent != 1.0f) clamped.pow(exponent) else clamped
                    return (curved * kotlin.math.sign(valIn)).coerceIn(-1f, 1f)
                }

                val curvedStickX = applyStickCurve(rawStickX)
                val curvedStickY = applyStickCurve(rawStickY)

                // 2. In-Game Deadzone Bridge (Anti-Deadzone - Radial Inner Deadzone Disc)
                // PC games enforce a circular center deadzone (typically 10-18%) on Xbox thumbsticks.
                // The inner deadzone boundary is a true 360° circle of radius 'deadzoneOffset',
                // scaling outward along radial rays to the unconstrained box boundary [-1, 1] x [-1, 1]
                // (Chebyshev ray projection). This guarantees:
                //   1. Tiny motions departing from center start at radius deadzoneOffset in all directions (circular disc).
                //   2. High deflections reach full 100% saturation on all edges and corners (e.g. x=1.0, y=1.0).
                //   3. In ABSOLUTE_TILT (Virtual Steering Wheel) or when compensation is 0.00, it is a pure
                //      1:1 transparent pass-through with no jump, preserving micro-adjustments (0.01..0.10).
                val deadzoneOffset = if (gyroSettings.inputMode == InputMode.ABSOLUTE_TILT) {
                    0f
                } else {
                    gyroSettings.gameDeadzoneCompensation.coerceIn(0f, 0.35f)
                }

                val gyroStickX: Float
                val gyroStickY: Float

                val r = sqrt(curvedStickX * curvedStickX + curvedStickY * curvedStickY)
                if (r > 0.0001f && deadzoneOffset > 0f) {
                    val ux = curvedStickX / r
                    val uy = curvedStickY / r
                    val maxU = maxOf(abs(ux), abs(uy))
                    val rMax = if (maxU > 0.0001f) 1.0f / maxU else 1.0f
                    val t = (r / rMax).coerceIn(0f, 1f)
                    val rBoosted = deadzoneOffset + (rMax - deadzoneOffset) * t
                    gyroStickX = (ux * rBoosted).coerceIn(-1f, 1f)
                    gyroStickY = (uy * rBoosted).coerceIn(-1f, 1f)
                } else if (r > 0.0001f) {
                    gyroStickX = curvedStickX
                    gyroStickY = curvedStickY
                } else {
                    gyroStickX = 0f
                    gyroStickY = 0f
                }

                // 3. Physical Joystick Supremacy Blending
                // On-screen physical joystick from phone is supreme. If active, it drives the controller.
                // If idle (released/resting), motion takes over.
                val isTargetRightStick = gyroSettings.xboxTargetStick == XboxTargetStick.RIGHT_STICK
                val physicalStickX = if (isTargetRightStick) input.rightStickX else input.leftStickX
                val physicalStickY = if (isTargetRightStick) input.rightStickY else input.leftStickY
                val isStickActive = abs(physicalStickX) > 0.05f || abs(physicalStickY) > 0.05f

                var blendedX: Float
                var blendedY: Float

                when (gyroSettings.xboxBlendMode) {
                    XboxBlendMode.JOYSTICK_SUPREME -> {
                        if (isStickActive) {
                            blendedX = physicalStickX
                            blendedY = physicalStickY
                        } else {
                            blendedX = gyroStickX
                            blendedY = gyroStickY
                        }
                    }
                    XboxBlendMode.ADDITIVE -> {
                        if (isStickActive) {
                            blendedX = (physicalStickX + gyroStickX).coerceIn(-1f, 1f)
                            blendedY = (physicalStickY + gyroStickY).coerceIn(-1f, 1f)
                        } else {
                            blendedX = gyroStickX
                            blendedY = gyroStickY
                        }
                    }
                    XboxBlendMode.MUTE_ON_STICK -> {
                        if (isStickActive) {
                            blendedX = physicalStickX
                            blendedY = physicalStickY
                        } else {
                            blendedX = gyroStickX
                            blendedY = gyroStickY
                        }
                    }
                }

                // 180° Snap Turn Injection (Instant tactical turnaround)
                if (processed.snapTurnStickX != 0f) {
                    finalRightX = processed.snapTurnStickX
                } else if (gyroSettings.inputMode == InputMode.GESTURE && gyroSettings.effectiveGestureJoystickMode == GestureJoystickMode.OFF) {
                    // Joystick Gyro is completely OFF: physical gamepad sticks pass through 100% untouched!
                } else {
                    when (gyroSettings.xboxTargetStick) {
                        XboxTargetStick.RIGHT_STICK -> {
                            finalRightX = blendedX
                            finalRightY = blendedY
                        }
                        XboxTargetStick.LEFT_STICK -> {
                            finalLeftX = blendedX
                            finalLeftY = blendedY
                        }
                        XboxTargetStick.NONE -> {
                            // Gyro does NOT attach to any joystick!
                            // Physical gamepad thumbsticks remain 100% untouched.
                        }
                    }
                }
            }

            // Kinetic Gesture Action Injection (Directional Flicks, Gravity Tilt, Shake)
            for (action in processed.activeGestureButtons) {
                when (action) {
                    GestureButton.BTN_X -> finalBtnX = true
                    GestureButton.BTN_Y -> finalBtnY = true
                    GestureButton.BTN_A -> finalBtnA = true
                    GestureButton.BTN_B -> finalBtnB = true
                    GestureButton.BTN_LB -> finalBtnL1 = true
                    GestureButton.BTN_RB -> finalBtnR1 = true
                    GestureButton.TRIGGER_LT -> finalTriggerL2 = 1.0f
                    GestureButton.TRIGGER_RT -> finalTriggerR2 = 1.0f
                    GestureButton.BTN_LS -> finalBtnL3 = true
                    GestureButton.BTN_RS -> finalBtnR3 = true
                    GestureButton.DPAD_UP -> finalDpadUp = true
                    GestureButton.DPAD_DOWN -> finalDpadDown = true
                    GestureButton.DPAD_LEFT -> finalDpadLeft = true
                    GestureButton.DPAD_RIGHT -> finalDpadRight = true
                    GestureButton.SNAP_TURN_180, GestureButton.NONE -> {}
                }
            }

            // Legacy boolean flags fallback
            if (processed.gesturePulseRT) finalTriggerR2 = 1.0f
            if (processed.gesturePulseB) finalBtnB = true
            if (processed.gesturePulseX) finalBtnX = true
            if (processed.gesturePulseA) finalBtnA = true
        }
        
        return input.copy(
            leftStickX = (finalLeftX * lsSensitivityX).coerceIn(-1.0f, 1.0f),
            leftStickY = (finalLeftY * lsSensitivityY).coerceIn(-1.0f, 1.0f),
            rightStickX = (finalRightX * rsSensitivityX).coerceIn(-1.0f, 1.0f),
            rightStickY = (finalRightY * rsSensitivityY).coerceIn(-1.0f, 1.0f),
            triggerL2 = maxOf(input.triggerL2, finalTriggerL2),
            triggerR2 = maxOf(input.triggerR2, finalTriggerR2),
            btnA = input.btnA || finalBtnA,
            btnB = input.btnB || finalBtnB,
            btnX = input.btnX || finalBtnX,
            btnY = input.btnY || finalBtnY,
            btnL1 = input.btnL1 || finalBtnL1,
            btnR1 = input.btnR1 || finalBtnR1,
            btnL3 = input.btnL3 || finalBtnL3,
            btnR3 = input.btnR3 || finalBtnR3,
            dpadUp = input.dpadUp || finalDpadUp,
            dpadDown = input.dpadDown || finalDpadDown,
            dpadLeft = input.dpadLeft || finalDpadLeft,
            dpadRight = input.dpadRight || finalDpadRight
        )
    }
}
