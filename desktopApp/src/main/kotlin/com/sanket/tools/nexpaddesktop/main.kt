package com.sanket.tools.nexpaddesktop

import androidx.compose.runtime.*
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.sanket.tools.nexpaddesktop.driver.GyroProcessor
import com.sanket.tools.nexpaddesktop.driver.VirtualDualShock4Driver
import com.sanket.tools.nexpaddesktop.driver.VirtualGamepadDriver
import com.sanket.tools.nexpaddesktop.driver.IGamepadDriver
import com.sanket.tools.nexpaddesktop.model.GamepadInput
import com.sanket.tools.nexpaddesktop.model.GyroSettings
import com.sanket.tools.nexpaddesktop.model.XboxTargetStick
import com.sanket.tools.nexpaddesktop.model.XboxBlendMode
import com.sanket.tools.nexpaddesktop.network.DsuServer
import com.sanket.tools.nexpaddesktop.network.UdpServer
import com.sanket.tools.nexpaddesktop.ui.ControllerType
import com.sanket.tools.nexpaddesktop.ui.MainApplicationWindow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.abs

import com.sun.jna.platform.win32.Kernel32
import com.sun.jna.platform.win32.WinBase

/**
 * NEXPAD Desktop — Application Entry Point
 *
 * ## Data Flow
 *
 * ```
 * Phone Sensors (raw rad/s, m/s²)
 *   → UDP Server (port 9999) → GamepadInput
 *   → GyroProcessor.process() → ProcessedGyro (°/s)
 *   ├── XBOX PATH: ProcessedGyro → stick values → VirtualGamepadDriver → ViGEm X360
 *   ├── PS4 PATH:  Raw input → VirtualDualShock4Driver (own calibration) → ViGEm DS4
 *   └── DSU PATH:  Raw input → DsuServer (CemuHook protocol) → Emulators
 * ```
 *
 * ## Key Architecture Decisions
 *
 * - **Xbox mode**: NEXPAD does all gyro math (deadzone, smoothing, acceleration,
 *   sensitivity) and converts the result to analog stick values because Xbox
 *   controllers have no native gyro support.
 *
 * - **PS4 mode**: The DS4 driver sends raw sensor data with its own bias calibration.
 *   The GyroProcessor still runs for the UI dashboard visualizer, but its output
 *   is NOT sent to the PS4 driver. Games and emulators do their own math.
 *
 * - **DSU Server**: Always running on port 26760. Sends raw, unprocessed sensor data
 *   to any connected CemuHook-compatible emulator (Cemu, Yuzu, Ryujinx, etc.).
 *   The DSU protocol requires raw sensor data — the emulator does its own processing.
 */
fun main() = application {
    // Optimize Windows Process Priority for minimal jitter
    if (System.getProperty("os.name").lowercase().contains("win")) {
        try {
            val currentProcess = Kernel32.INSTANCE.GetCurrentProcess()
            // WinBase.HIGH_PRIORITY_CLASS = 0x00000080 (128)
            Kernel32.INSTANCE.SetPriorityClass(currentProcess, com.sun.jna.platform.win32.WinDef.DWORD(128))
            println("⚡ [QoS] Windows Process Priority set to HIGH_PRIORITY_CLASS.")
        } catch (e: Exception) {
            println("⚠️ [QoS] Failed to set process priority: ${e.message}")
        }
    }

    val scope = rememberCoroutineScope()
    var server by remember { mutableStateOf<UdpServer?>(null) }
    
    var activeController by remember { mutableStateOf(ControllerType.XBOX_360) }
    
    // The active ViGEm driver (Xbox 360 or DualShock 4). Updated when controller type changes.
    var activeDriver by remember { mutableStateOf<IGamepadDriver?>(null) }

    // DSU (CemuHook) motion server — always active for emulator compatibility
    val dsuServer = remember { DsuServer() }
    val discoveryServer = remember { com.sanket.tools.nexpaddesktop.network.DiscoveryServer() }
    var latestInput by remember { mutableStateOf(GamepadInput()) }

    // ── Xbox Stick Sensitivity (multipliers applied to physical + gyro stick values) ──
    var lsSensitivityX by remember { mutableStateOf(1.0f) }
    var lsSensitivityY by remember { mutableStateOf(1.0f) }
    var rsSensitivityX by remember { mutableStateOf(1.0f) }
    var rsSensitivityY by remember { mutableStateOf(1.0f) }

    // ── 6-Axis Gyro Settings (shared config read by GyroProcessor) ──
    var gyroSettings by remember { mutableStateOf(GyroSettings()) }
    val gyroProcessor = remember { GyroProcessor() }
    var processedYaw by remember { mutableStateOf(0f) }
    var processedPitch by remember { mutableStateOf(0f) }

    var appError by remember { mutableStateOf("") }
    
    // ── Driver Connection State ──
    var isDriverConnected by remember { mutableStateOf(false) }
    
    // ══════════════════════════════════════════════════════════
    //  DRIVER LIFECYCLE — Re-create driver when controller type changes
    // ══════════════════════════════════════════════════════════
    DisposableEffect(activeController) {
        val newDriver = if (activeController == ControllerType.XBOX_360) {
            VirtualGamepadDriver(onRumble = { feedback -> 
                scope.launch { 
                    try { server?.sendFeedback(feedback) } 
                    catch (e: Throwable) { appError = "Xbox Rumble Error: ${e.message}" }
                }
            })
        } else {
            VirtualDualShock4Driver(onRumble = { feedback -> 
                scope.launch { 
                    try { server?.sendFeedback(feedback) } 
                    catch (e: Throwable) { appError = "DS4 Rumble Error: ${e.message}" } 
                } 
            })
        }
        newDriver.connect()
        activeDriver = newDriver
        isDriverConnected = newDriver.isDriverConnected()

        // If the driver isn't installed yet, launch a coroutine to keep trying in the background
        val connectionJob = scope.launch(Dispatchers.IO) {
            while (!newDriver.isDriverConnected()) {
                kotlinx.coroutines.delay(2000) // check every 2 seconds
                try {
                    newDriver.connect()
                } catch (e: Exception) {
                    // Ignore errors during polling
                }
                isDriverConnected = newDriver.isDriverConnected()
            }
        }

        // Reset gyro processor smoothing state when switching controllers
        gyroProcessor.reset()
        
        onDispose {
            connectionJob.cancel()
            newDriver.disconnect()
        }
    }

    // ══════════════════════════════════════════════════════════
    //  UDP SERVER + DSU SERVER — Receive phone input, dispatch to drivers
    // ══════════════════════════════════════════════════════════
    var lastUiUpdateTime = 0L
    DisposableEffect(Unit) {
        dsuServer.start()
        
        server = UdpServer(9999) { input ->

            // ── Check gyro activation buttons (Xbox only — PS4 games handle this) ──
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

            // ── Run gyro through the processing pipeline ──
            // This runs for BOTH controller types:
            //   - Xbox: output is mapped to stick values below
            //   - PS4:  output is only used for the UI dashboard visualizer
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

            val now = System.currentTimeMillis()
            val shouldUpdateUi = (now - lastUiUpdateTime) > 33L
            if (shouldUpdateUi) {
                lastUiUpdateTime = now
                // Update processed values for UI display (both controller types)
                processedYaw = processed.yawDps
                processedPitch = processed.pitchDps
            }

            // ── Build final stick values ─────────────────
            var finalLeftX = input.leftStickX
            var finalLeftY = input.leftStickY
            var finalRightX = input.rightStickX
            var finalRightY = input.rightStickY

            // ══════════════════════════════════════════════
            //  XBOX PATH — Map processed gyro → analog stick
            // ══════════════════════════════════════════════
            if (activeController == ControllerType.XBOX_360 && gyroSettings.enabled) {
                // Convert °/s to stick range (-1..1).
                // The GyroProcessor outputs values in a normalized range where
                // ±200 °/s equals full stick deflection. This constant must match
                // the maxDps normalization in GyroProcessor step 8.
                val gyroToStickScale = 1.0f / 200.0f
                val gyroStickX = (processed.yawDps * gyroToStickScale).coerceIn(-1f, 1f)
                val gyroStickY = (processed.pitchDps * gyroToStickScale).coerceIn(-1f, 1f)

                // Small threshold to determine if motion is "active" (not just noise)
                val threshold = 0.02f
                val isMotionActive = abs(gyroStickX) > threshold || abs(gyroStickY) > threshold

                if (gyroSettings.xboxTargetStick == XboxTargetStick.LEFT_STICK) {
                    when (gyroSettings.xboxBlendMode) {
                        XboxBlendMode.OVERRIDE -> if (isMotionActive) {
                            finalLeftX = gyroStickX
                            finalLeftY = gyroStickY
                        }
                        XboxBlendMode.ADDITIVE -> {
                            finalLeftX += gyroStickX
                            finalLeftY += gyroStickY
                        }
                        XboxBlendMode.MUTE_ON_STICK -> {
                            if (abs(input.leftStickX) > 0.05f || abs(input.leftStickY) > 0.05f) {
                                finalLeftX = input.leftStickX
                                finalLeftY = input.leftStickY
                            } else {
                                finalLeftX = gyroStickX
                                finalLeftY = gyroStickY
                            }
                        }
                    }
                } else { // RIGHT_STICK
                    when (gyroSettings.xboxBlendMode) {
                        XboxBlendMode.OVERRIDE -> if (isMotionActive) {
                            finalRightX = gyroStickX
                            finalRightY = gyroStickY
                        }
                        XboxBlendMode.ADDITIVE -> {
                            finalRightX += gyroStickX
                            finalRightY += gyroStickY
                        }
                        XboxBlendMode.MUTE_ON_STICK -> {
                            if (abs(input.rightStickX) > 0.05f || abs(input.rightStickY) > 0.05f) {
                                finalRightX = input.rightStickX
                                finalRightY = input.rightStickY
                            } else {
                                finalRightX = gyroStickX
                                finalRightY = gyroStickY
                            }
                        }
                    }
                }
            }
            
            // Apply stick sensitivity multipliers and clamp to valid range
            val processedInput = input.copy(
                leftStickX = (finalLeftX * lsSensitivityX).coerceIn(-1.0f, 1.0f),
                leftStickY = (finalLeftY * lsSensitivityY).coerceIn(-1.0f, 1.0f),
                rightStickX = (finalRightX * rsSensitivityX).coerceIn(-1.0f, 1.0f),
                rightStickY = (finalRightY * rsSensitivityY).coerceIn(-1.0f, 1.0f)
            )
            if (shouldUpdateUi) {
                latestInput = processedInput
            }

            // ══════════════════════════════════════════════
            //  DRIVER OUTPUT — Send to ViGEm virtual controller
            // ══════════════════════════════════════════════
            activeDriver?.updateInput(processedInput)

            // ══════════════════════════════════════════════
            //  DSU PATH — Send RAW unaltered input to DSU server
            // ══════════════════════════════════════════════
            // The CemuHook/DSU protocol requires raw sensor data. Emulators
            // (Cemu, Yuzu, Ryujinx) perform their own gyro processing.
            // We intentionally send `input` (not `processedInput`) here.
            dsuServer.updateInput(input)
        }
        scope.launch {
            server?.start()
        }
        scope.launch {
            discoveryServer.start()
        }
        onDispose {
            server?.stop()
            discoveryServer.stop()
            dsuServer.stop()
        }
    }

    Window(
        onCloseRequest = ::exitApplication,
        title = "NEXPAD PC Companion",
    ) {
        MainApplicationWindow(
            driver = activeDriver ?: VirtualGamepadDriver(),
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
            
            isDriverConnected = isDriverConnected,

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