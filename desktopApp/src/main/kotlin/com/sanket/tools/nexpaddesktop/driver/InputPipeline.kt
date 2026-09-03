package com.sanket.tools.nexpaddesktop.driver

import com.sanket.tools.nexpad.model.GamepadInput
import com.sanket.tools.nexpaddesktop.model.GyroSettings
import com.sanket.tools.nexpaddesktop.model.XboxTargetStick
import com.sanket.tools.nexpaddesktop.model.XboxBlendMode
import com.sanket.tools.nexpaddesktop.ui.ControllerType
import kotlin.math.abs

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
                    "LT" -> input.triggerL2 > 0.1f
                    "RT" -> input.triggerR2 > 0.1f
                    "LB" -> input.btnL1
                    "RB" -> input.btnR1
                    "A" -> input.btnA
                    "B" -> input.btnB
                    "X" -> input.btnX
                    "Y" -> input.btnY
                    else -> false
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
            settings = gyroSettings,
            isActivationButtonPressed = isActivationButtonPressed,
        )

        onProcessedAngles(processed.yawDps, processed.pitchDps)

        // 3. Build final stick values
        var finalLeftX = input.leftStickX
        var finalLeftY = input.leftStickY
        var finalRightX = input.rightStickX
        var finalRightY = input.rightStickY

        if (activeController == ControllerType.XBOX_360 && gyroSettings.enabled) {
            val gyroToStickScale = 1.0f / 200.0f
            val gyroStickX = (processed.yawDps * gyroToStickScale).coerceIn(-1f, 1f)
            val gyroStickY = (processed.pitchDps * gyroToStickScale).coerceIn(-1f, 1f)
            val threshold = 0.02f
            val isMotionActive = abs(gyroStickX) > threshold || abs(gyroStickY) > threshold

            if (gyroSettings.xboxTargetStick == XboxTargetStick.LEFT_STICK) {
                when (gyroSettings.xboxBlendMode) {
                    XboxBlendMode.OVERRIDE -> if (isMotionActive) { finalLeftX = gyroStickX; finalLeftY = gyroStickY }
                    XboxBlendMode.ADDITIVE -> { finalLeftX += gyroStickX; finalLeftY += gyroStickY }
                    XboxBlendMode.MUTE_ON_STICK -> {
                        if (abs(input.leftStickX) > 0.05f || abs(input.leftStickY) > 0.05f) { finalLeftX = input.leftStickX; finalLeftY = input.leftStickY }
                        else { finalLeftX = gyroStickX; finalLeftY = gyroStickY }
                    }
                }
            } else {
                when (gyroSettings.xboxBlendMode) {
                    XboxBlendMode.OVERRIDE -> if (isMotionActive) { finalRightX = gyroStickX; finalRightY = gyroStickY }
                    XboxBlendMode.ADDITIVE -> { finalRightX += gyroStickX; finalRightY += gyroStickY }
                    XboxBlendMode.MUTE_ON_STICK -> {
                        if (abs(input.rightStickX) > 0.05f || abs(input.rightStickY) > 0.05f) { finalRightX = input.rightStickX; finalRightY = input.rightStickY }
                        else { finalRightX = gyroStickX; finalRightY = gyroStickY }
                    }
                }
            }
        }
        
        return input.copy(
            leftStickX = (finalLeftX * lsSensitivityX).coerceIn(-1.0f, 1.0f),
            leftStickY = (finalLeftY * lsSensitivityY).coerceIn(-1.0f, 1.0f),
            rightStickX = (finalRightX * rsSensitivityX).coerceIn(-1.0f, 1.0f),
            rightStickY = (finalRightY * rsSensitivityY).coerceIn(-1.0f, 1.0f)
        )
    }
}
