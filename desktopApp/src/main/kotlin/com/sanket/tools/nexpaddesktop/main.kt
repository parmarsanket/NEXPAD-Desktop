package com.sanket.tools.nexpaddesktop

import androidx.compose.runtime.*
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.sanket.tools.nexpaddesktop.driver.GyroProcessor
import com.sanket.tools.nexpaddesktop.driver.VirtualGamepadDriver
import com.sanket.tools.nexpaddesktop.driver.IGamepadDriver
import com.sanket.tools.nexpaddesktop.model.GamepadInput
import com.sanket.tools.nexpaddesktop.model.GyroSettings
import com.sanket.tools.nexpaddesktop.network.UdpServer
import com.sanket.tools.nexpaddesktop.ui.MainApplicationWindow
import kotlinx.coroutines.launch
import kotlin.math.abs

fun main() = application {
    val scope = rememberCoroutineScope()
    var server by remember { mutableStateOf<UdpServer?>(null) }
    
    var activeController by remember { mutableStateOf(com.sanket.tools.nexpaddesktop.ui.ControllerType.XBOX_360) }
    
    // We hold a reference to the active driver so the UDP server can always access the latest one
    var activeDriver by remember { mutableStateOf<IGamepadDriver?>(null) }

    val dsuServer = remember { com.sanket.tools.nexpaddesktop.network.DsuServer() }
    var latestInput by remember { mutableStateOf(GamepadInput()) }

    // ── Stick sensitivity (Xbox only) ────────────────────
    var lsSensitivityX by remember { mutableStateOf(1.0f) }
    var lsSensitivityY by remember { mutableStateOf(1.0f) }
    var rsSensitivityX by remember { mutableStateOf(1.0f) }
    var rsSensitivityY by remember { mutableStateOf(1.0f) }

    // ── 6-Axis Gyro Settings (new unified system) ────────
    var gyroSettings by remember { mutableStateOf(GyroSettings()) }
    val gyroProcessor = remember { GyroProcessor() }
    var processedYaw by remember { mutableStateOf(0f) }
    var processedPitch by remember { mutableStateOf(0f) }

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

        // Reset gyro processor state when switching controllers
        gyroProcessor.reset()
        
        onDispose {
            newDriver.disconnect()
        }
    }

    DisposableEffect(Unit) {
        dsuServer.start()
        
        server = UdpServer(9999) { input ->
            // ── Check gyro activation buttons ────────────
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

            // ── Process gyro through the new pipeline ────
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

            // Update processed values for UI display
            processedYaw = processed.yawDps
            processedPitch = processed.pitchDps

            // ── Build final stick values ─────────────────
            var finalLeftX = input.leftStickX
            var finalLeftY = input.leftStickY
            var finalRightX = input.rightStickX
            var finalRightY = input.rightStickY

            // For Xbox mode: map processed gyro → stick values
            if (activeController == com.sanket.tools.nexpaddesktop.ui.ControllerType.XBOX_360 && gyroSettings.enabled) {
                // Convert °/s to stick range (-1..1).
                // At ~200°/s physical rotation we want full stick deflection.
                val gyroToStickScale = 1.0f / 200.0f
                val gyroStickX = (processed.yawDps * gyroToStickScale).coerceIn(-1f, 1f)
                val gyroStickY = (processed.pitchDps * gyroToStickScale).coerceIn(-1f, 1f)

                val threshold = 0.02f
                val isMotionActive = abs(gyroStickX) > threshold || abs(gyroStickY) > threshold

                if (gyroSettings.xboxTargetStick == "LEFT_STICK") {
                    if (gyroSettings.xboxBlendMode == "OVERRIDE" && isMotionActive) {
                        finalLeftX = gyroStickX
                        finalLeftY = gyroStickY
                    } else if (gyroSettings.xboxBlendMode == "ADDITIVE") {
                        finalLeftX += gyroStickX
                        finalLeftY += gyroStickY
                    }
                } else { // RIGHT_STICK
                    if (gyroSettings.xboxBlendMode == "OVERRIDE" && isMotionActive) {
                        finalRightX = gyroStickX
                        finalRightY = gyroStickY
                    } else if (gyroSettings.xboxBlendMode == "ADDITIVE") {
                        finalRightX += gyroStickX
                        finalRightY += gyroStickY
                    }
                }
            }
            
            // Apply stick sensitivity multipliers and clamp
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
            
            gyroSettings = gyroSettings,
            onGyroSettingsChange = { gyroSettings = it },
            processedYaw = processedYaw,
            processedPitch = processedPitch,
            onRecalibrate = {
                // Reset the DS4 driver's internal calibration
                activeDriver?.disconnect()
                activeDriver?.connect()
                gyroProcessor.reset()
            },
            
            onControllerChange = { activeController = it }
        )
    }
}