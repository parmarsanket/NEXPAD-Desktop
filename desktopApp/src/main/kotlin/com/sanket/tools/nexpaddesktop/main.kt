package com.sanket.tools.nexpaddesktop

import androidx.compose.runtime.*
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.sanket.tools.nexpaddesktop.driver.GyroProcessor
import com.sanket.tools.nexpaddesktop.driver.VirtualDualShock4Driver
import com.sanket.tools.nexpaddesktop.driver.VirtualGamepadDriver
import com.sanket.tools.nexpaddesktop.driver.IGamepadDriver
import com.sanket.tools.nexpad.model.GamepadInput
import com.sanket.tools.nexpaddesktop.model.GyroSettings
import com.sanket.tools.nexpaddesktop.model.XboxTargetStick
import com.sanket.tools.nexpaddesktop.model.XboxBlendMode
import com.sanket.tools.nexpaddesktop.connection.wifi.DsuServer
import com.sanket.tools.nexpaddesktop.connection.wifi.UdpServer
import com.sanket.tools.nexpaddesktop.ui.ControllerType
import com.sanket.tools.nexpaddesktop.ui.MainApplicationWindow
import com.sanket.tools.nexpaddesktop.ui.theme.NexpadDesktopTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.isActive
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
fun main(args: Array<String>) {
    val installDriverIndex = args.indexOf("--install-driver")
    if (installDriverIndex != -1) {
        val vidHex = if (installDriverIndex + 1 < args.size) args[installDriverIndex + 1] else ""
        val pidHex = if (installDriverIndex + 2 < args.size) args[installDriverIndex + 2] else ""
        com.sanket.tools.nexpaddesktop.connection.usb.winusb.DriverInstaller.installWinUsb(vidHex, pidHex)
        return
    }
    if (args.contains("--restore-driver")) {
        com.sanket.tools.nexpaddesktop.connection.usb.winusb.DriverInstaller.restoreMtp()
        return
    }

    application {
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
    val discoveryServer = remember { com.sanket.tools.nexpaddesktop.connection.wifi.DiscoveryServer() }
    var latestInput by remember { mutableStateOf(GamepadInput()) }
    
    // ── Driver Connection State ──
    var isDriverConnected by remember { mutableStateOf(false) }
    
    // ── Network Client State ──
    var connectedDeviceName by remember { mutableStateOf<String?>(null) }
    var connectionType by remember { mutableStateOf<Int?>(null) }

    var lastUiUpdateTime = 0L
    var processedYaw by remember { mutableStateOf(0f) }
    var processedPitch by remember { mutableStateOf(0f) }
    val gyroProcessor = remember { GyroProcessor() }
    var gyroSettings by remember { mutableStateOf(GyroSettings()) }
    var lsSensitivityX by remember { mutableStateOf(1f) }
    var lsSensitivityY by remember { mutableStateOf(1f) }
    var rsSensitivityX by remember { mutableStateOf(1f) }
    var rsSensitivityY by remember { mutableStateOf(1f) }
    
    var appError by remember { mutableStateOf("") }
    
    // AOA Elevation State
    var aoaRequiresElevation by remember { mutableStateOf(false) }
    var onRequestAoaElevation: (() -> Unit)? = null
    
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

    DisposableEffect(Unit) {

        dsuServer.start()
        
        val inputHandler: (GamepadInput) -> Unit = { input ->
            val processedInput = com.sanket.tools.nexpaddesktop.driver.InputPipeline.processInput(
                input = input,
                activeController = activeController,
                gyroProcessor = gyroProcessor,
                gyroSettings = gyroSettings,
                lsSensitivityX = lsSensitivityX,
                lsSensitivityY = lsSensitivityY,
                rsSensitivityX = rsSensitivityX,
                rsSensitivityY = rsSensitivityY,
                onProcessedAngles = { yaw, pitch ->
                    val now = System.currentTimeMillis()
                    if ((now - lastUiUpdateTime) > 33L) {
                        lastUiUpdateTime = now
                        processedYaw = yaw
                        processedPitch = pitch
                        latestInput = input // We update latestInput here for the UI to prevent excessive recomposition
                    }
                }
            )

            activeDriver?.updateInput(processedInput)
            dsuServer.updateInput(input)
        }

        server = UdpServer(
            port = 9999,
            onClientConnected = { name, type -> connectedDeviceName = name; connectionType = type },
            onClientDisconnected = { connectedDeviceName = null; connectionType = null },
            onInputReceived = inputHandler
        )

        val aoaManager = com.sanket.tools.nexpaddesktop.connection.usb.aoa.AoaManager()
        aoaManager.onAoaConnected = { name -> connectedDeviceName = name; connectionType = 1; aoaRequiresElevation = false } // 1 is USB in this app
        aoaManager.onAoaDisconnected = { connectedDeviceName = null; connectionType = null }
        aoaManager.onInputReceived = inputHandler
        var requiredVidHex = ""
        var requiredPidHex = ""
        aoaManager.onRequestElevation = { vid, pid -> 
            requiredVidHex = String.format("%04X", vid)
            requiredPidHex = String.format("%04X", pid)
            aoaRequiresElevation = true 
        }
        
        onRequestAoaElevation = {
            aoaRequiresElevation = false
            scope.launch {
                val (exec, args) = com.sanket.tools.nexpaddesktop.utils.WindowsElevation.getElevationTarget("--install-driver $requiredVidHex $requiredPidHex")
                println("AoaManager: Requesting UAC elevation via native ShellExecuteEx...")

                when (val result = com.sanket.tools.nexpaddesktop.utils.WindowsElevation.runElevated(exec, args)) {
                    is com.sanket.tools.nexpaddesktop.utils.WindowsElevation.Result.Success -> {
                        if (result.exitCode == 0) {
                            println("AoaManager: Driver installed successfully! Re-scanning USB...")
                        } else {
                            println("AoaManager ERROR: Driver installer exited with code ${result.exitCode}")
                        }
                    }
                    is com.sanket.tools.nexpaddesktop.utils.WindowsElevation.Result.UserCancelled -> {
                        println("AoaManager: User rejected UAC prompt.")
                    }
                    is com.sanket.tools.nexpaddesktop.utils.WindowsElevation.Result.Error -> {
                        println("AoaManager ERROR: Elevation failed: ${result.message}")
                    }
                }
            }
        }
        
        // Temporarily, we start scanning on load for this branch
        scope.launch(Dispatchers.IO) { aoaManager.scanAndConnect() }

        scope.launch { server?.start() }
        
        scope.launch { discoveryServer.start() }
        onDispose {
            server?.stop()
            discoveryServer.stop()
            dsuServer.stop()
        }
    }

    Window(
        onCloseRequest = ::exitApplication,
        title = "NEXPAD Desktop Server"
    ) {
        NexpadDesktopTheme {
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
                connectedDeviceName = connectedDeviceName,
                connectionType = connectionType,
                
                aoaRequiresElevation = aoaRequiresElevation,
                onRequestAoaElevation = onRequestAoaElevation ?: {},

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
}

}
