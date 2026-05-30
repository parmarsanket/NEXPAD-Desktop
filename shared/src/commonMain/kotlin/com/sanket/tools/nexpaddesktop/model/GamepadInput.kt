package com.sanket.tools.nexpaddesktop.model

import kotlinx.serialization.Serializable

@Serializable
data class GamepadInput(
    var btnA: Boolean = false,
    var btnB: Boolean = false,
    var btnX: Boolean = false,
    var btnY: Boolean = false,
    var dpadUp: Boolean = false,
    var dpadDown: Boolean = false,
    var dpadLeft: Boolean = false,
    var dpadRight: Boolean = false,
    var btnL1: Boolean = false,
    var btnR1: Boolean = false,
    var btnL3: Boolean = false,
    var btnR3: Boolean = false,
    var btnStart: Boolean = false,
    var btnSelect: Boolean = false,
    var btnGuide: Boolean = false,
    var triggerL2: Float = 0f,
    var triggerR2: Float = 0f,
    var leftStickX: Float = 0f,
    var leftStickY: Float = 0f,
    var rightStickX: Float = 0f,
    var rightStickY: Float = 0f,
    var gyroX: Float = 0f,
    var gyroY: Float = 0f,
    var gyroZ: Float = 0f
)
