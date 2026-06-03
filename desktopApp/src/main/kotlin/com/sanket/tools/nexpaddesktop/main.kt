package com.sanket.tools.nexpaddesktop

import androidx.compose.runtime.*
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.sanket.tools.nexpaddesktop.driver.VirtualGamepadDriver
import com.sanket.tools.nexpaddesktop.driver.IGamepadDriver
import com.sanket.tools.nexpaddesktop.model.GamepadInput
import com.sanket.tools.nexpaddesktop.network.UdpServer
import com.sanket.tools.nexpaddesktop.ui.MainApplicationWindow
import kotlinx.coroutines.launch

fun main() = application {
    val scope = rememberCoroutineScope()
    var server by remember { mutableStateOf<UdpServer?>(null) }
    
    var activeController by remember { mutableStateOf(com.sanket.tools.nexpaddesktop.ui.ControllerType.XBOX_360) }
    
    // We hold a reference to the active driver so the UDP server can always access the latest one
    var activeDriver by remember { mutableStateOf<IGamepadDriver?>(null) }

    val dsuServer = remember { com.sanket.tools.nexpaddesktop.network.DsuServer() }
    var latestInput by remember { mutableStateOf(GamepadInput()) }

    var lsSensitivityX by remember { mutableStateOf(1.0f) }
    var lsSensitivityY by remember { mutableStateOf(1.0f) }
    var rsSensitivityX by remember { mutableStateOf(1.0f) }
    var rsSensitivityY by remember { mutableStateOf(1.0f) }
    var isAdvancedGyroEnabled by remember { mutableStateOf(true) }
    var gyroTargetStick by remember { mutableStateOf("LEFT_STICK") }
    var gyroBlendMode by remember { mutableStateOf("OVERRIDE") }
    var gyroSensX by remember { mutableStateOf(1.0f) }
    var gyroSensY by remember { mutableStateOf(1.0f) }
    var accelWeight by remember { mutableStateOf(1.0f) }
    var gyroWeight by remember { mutableStateOf(1.0f) }
    var gyroActivationButtons by remember { mutableStateOf(setOf<String>()) }

    var appError by remember { mutableStateOf("") }
    
    // Re-instantiate and connect the driver whenever activeController changes
    DisposableEffect(activeController) {
        val newDriver = if (activeController == com.sanket.tools.nexpaddesktop.ui.ControllerType.XBOX_360) {
            VirtualGamepadDriver(onRumble = { feedback -> 
                scope.launch { 
                    try { server?.sendFeedback(feedback) } 
                    catch (e: Throwable) { appError = "Xbox Rumble Error: ${e.message}" }
                }
            })
        } else {
            com.sanket.tools.nexpaddesktop.driver.VirtualDualShock4Driver(onRumble = { feedback -> 
                scope.launch { 
                    try { server?.sendFeedback(feedback) } 
                    catch (e: Throwable) { appError = "DS4 Rumble Error: ${e.message}" }
                } 
            })
        }
        newDriver.connect()
        activeDriver = newDriver
        
        onDispose {
            newDriver.disconnect()
        }
    }

    DisposableEffect(Unit) {
        dsuServer.start()
        
        server = UdpServer(9999) { input ->
            var finalLeftX = input.leftStickX
            var finalLeftY = input.leftStickY
            var finalRightX = input.rightStickX
            var finalRightY = input.rightStickY
            
            val isActivationMet = if (gyroActivationButtons.isEmpty()) {
                true
            } else {
                gyroActivationButtons.any { btn ->
                    when (btn) {
                        "LT" -> input.triggerL2 > 0.1f
                        "RT" -> input.triggerR2 > 0.1f
                        "LB" -> input.btnL1
                        "RB" -> input.btnR1
                        "A" -> input.btnA
                        "B" -> input.btnB
                        "X" -> input.btnX
                        "Y" -> input.btnY
                        "DPAD_UP" -> input.dpadUp
                        "DPAD_DOWN" -> input.dpadDown
                        "DPAD_LEFT" -> input.dpadLeft
                        "DPAD_RIGHT" -> input.dpadRight
                        else -> false
                    }
                }
            }
            
            if (isAdvancedGyroEnabled && isActivationMet && activeController == com.sanket.tools.nexpaddesktop.ui.ControllerType.XBOX_360) {
                val maxAccel = 9.8f
                val maxGyro = 900.0f // degrees per sec roughly
                
                // Usually AccelX maps to Horizontal tilt, AccelY to Vertical tilt.
                // Depending on phone orientation, we combine them.
                val motionX = ((input.accelX / maxAccel) * accelWeight) + ((input.gyroY / maxGyro) * gyroWeight)
                val motionY = ((input.accelY / maxAccel) * accelWeight) + ((input.gyroX / maxGyro) * gyroWeight)
                
                // Apply gyro sensitivity
                val finalMotionX = motionX * gyroSensX
                val finalMotionY = motionY * gyroSensY
                
                val threshold = 0.03f
                val isMotionActive = kotlin.math.abs(finalMotionX) > threshold || kotlin.math.abs(finalMotionY) > threshold
                
                if (gyroTargetStick == "LEFT_STICK") {
                    if (gyroBlendMode == "OVERRIDE" && isMotionActive) {
                        finalLeftX = finalMotionX
                        finalLeftY = finalMotionY
                    } else if (gyroBlendMode == "ADDITIVE") {
                        finalLeftX += finalMotionX
                        finalLeftY += finalMotionY
                    }
                } else { // RIGHT_STICK
                    if (gyroBlendMode == "OVERRIDE" && isMotionActive) {
                        finalRightX = finalMotionX
                        finalRightY = finalMotionY
                    } else if (gyroBlendMode == "ADDITIVE") {
                        finalRightX += finalMotionX
                        finalRightY += finalMotionY
                    }
                }
            }
            
            // Apply sensitivity multipliers and clamp
            val processedInput = input.copy(
                leftStickX = (finalLeftX * lsSensitivityX).coerceIn(-1.0f, 1.0f),
                leftStickY = (finalLeftY * lsSensitivityY).coerceIn(-1.0f, 1.0f),
                rightStickX = (finalRightX * rsSensitivityX).coerceIn(-1.0f, 1.0f),
                rightStickY = (finalRightY * rsSensitivityY).coerceIn(-1.0f, 1.0f)
            )
            
            latestInput = processedInput
            activeDriver?.updateInput(processedInput)
            dsuServer.updateInput(input) // send raw unaltered input to DSU
        }
        scope.launch {
            server?.start()
        }
        onDispose {
            dsuServer.stop()
        }
    }

    Window(
        onCloseRequest = ::exitApplication,
        title = "NEXPAD PC Companion",
    ) {
        MainApplicationWindow(
            driver = activeDriver ?: VirtualGamepadDriver(), // fallback for initial render
            latestInput = latestInput, 
            dsuClientCount = dsuServer.getClientCount(),
            activeController = activeController,
            lsSensitivityX = lsSensitivityX,
            lsSensitivityY = lsSensitivityY,
            rsSensitivityX = rsSensitivityX,
            rsSensitivityY = rsSensitivityY,
            onLsSensitivityXChange = { lsSensitivityX = it },
            onLsSensitivityYChange = { lsSensitivityY = it },
            onRsSensitivityXChange = { rsSensitivityX = it },
            onRsSensitivityYChange = { rsSensitivityY = it },
            
            isAdvancedGyroEnabled = isAdvancedGyroEnabled,
            gyroTargetStick = gyroTargetStick,
            gyroBlendMode = gyroBlendMode,
            gyroSensX = gyroSensX,
            gyroSensY = gyroSensY,
            accelWeight = accelWeight,
            gyroWeight = gyroWeight,
            gyroActivationButtons = gyroActivationButtons,
            onAdvancedGyroEnabledChange = { isAdvancedGyroEnabled = it },
            onGyroTargetStickChange = { gyroTargetStick = it },
            onGyroBlendModeChange = { gyroBlendMode = it },
            onGyroSensXChange = { gyroSensX = it },
            onGyroSensYChange = { gyroSensY = it },
            onAccelWeightChange = { accelWeight = it },
            onGyroWeightChange = { gyroWeight = it },
            onGyroActivationButtonsChange = { gyroActivationButtons = it },
            
            onControllerChange = { activeController = it }
        )
    }
}