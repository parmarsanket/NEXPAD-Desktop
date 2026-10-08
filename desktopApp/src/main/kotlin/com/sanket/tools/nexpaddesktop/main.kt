package com.sanket.tools.nexpaddesktop

import androidx.compose.runtime.*
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.sanket.tools.nexpaddesktop.connection.wifi.DsuServer
import com.sanket.tools.nexpaddesktop.connection.wifi.UdpServer
import com.sanket.tools.nexpaddesktop.driver.IGamepadDriver
import com.sanket.tools.nexpaddesktop.driver.VirtualGamepadDriver
import com.sanket.tools.nexpaddesktop.driver.VirtualDualShock4Driver
import com.sanket.tools.nexpaddesktop.model.GyroSettings
import com.sanket.tools.nexpad.model.GamepadInput
import com.sanket.tools.nexpaddesktop.ui.MainApplicationWindow
import com.sanket.tools.nexpaddesktop.ui.theme.NexpadDesktopTheme
import com.sanket.tools.nexpaddesktop.connection.ActiveTransport
import com.sanket.tools.nexpaddesktop.connection.DriverInstallState
import com.sanket.tools.nexpad.model.GamepadFeedback
import com.sanket.tools.nexpaddesktop.ui.ControllerType
import com.sanket.tools.nexpaddesktop.plugins.UniversalPushManager
import com.sanket.tools.nexpaddesktop.viewmodel.DesktopViewModel
import com.sun.jna.platform.win32.Kernel32
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Main Entry Point for Desktop App
 * - Launches a UDP server on 9999 for gamepad data
 * - Launches a UDP broadcast server on 9998 for auto-discovery
 * - Launches DSU server on 26760 for motion controls
 * - Processes inputs and delegates to ViGEm (Xbox360 or DS4 emulation)
 */
fun main(args: Array<String>) {
    com.sanket.tools.nexpaddesktop.utils.AppLogger.initGlobalRedirect()
    
    // 1. Check for headless driver installation mode
    val installDriverIndex = args.indexOf("--install-driver")
    if (installDriverIndex != -1) {
        val vidHex = if (installDriverIndex + 1 < args.size) args[installDriverIndex + 1] else ""
        val pidHex = if (installDriverIndex + 2 < args.size) args[installDriverIndex + 2] else ""
        val rawMi = if (installDriverIndex + 3 < args.size) args[installDriverIndex + 3] else "none"
        val miParam = if (rawMi.all { it.isDigit() }) rawMi else "none"
        
        println("Main: Running in headless driver installation mode for VID: $vidHex PID: $pidHex MI: $miParam")
        val exitCode = com.sanket.tools.nexpaddesktop.connection.usb.winusb.WinUsbDriverManager.installWinUsb(vidHex, pidHex, miParam)
        kotlin.system.exitProcess(exitCode)
    }

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

    application {

    val scope = rememberCoroutineScope()
    val viewModel = remember { DesktopViewModel() }
    var server by remember { mutableStateOf<UdpServer?>(null) }
    val aoaManager = remember { com.sanket.tools.nexpaddesktop.connection.usb.aoa.AoaManager() }
    val adbBridgeManager = remember { com.sanket.tools.nexpaddesktop.connection.adb.AdbBridgeManager() }
    val btServer = remember { com.sanket.tools.nexpaddesktop.connection.bt.BluetoothRfcommServer() }
    
    // The active ViGEm driver (Xbox 360 or DualShock 4). Updated when controller type changes.
    var activeDriver by remember { mutableStateOf<IGamepadDriver?>(null) }

    // DSU (CemuHook) motion server — always active for emulator compatibility
    val dsuServer = remember { DsuServer() }
    val discoveryServer = remember { com.sanket.tools.nexpaddesktop.connection.wifi.DiscoveryServer() }
    
    // Motion Smoothing State
    val gyroProcessor = remember { com.sanket.tools.nexpaddesktop.driver.GyroProcessor() }

    // Throttle UI updates to 30 FPS
    var lastUiUpdateTime by remember { mutableStateOf(0L) }
    
    // AOA Driver Installation State
    var requiredVidHex by remember { mutableStateOf("") }
    var requiredPidHex by remember { mutableStateOf("") }
    var requiredMi by remember { mutableStateOf("") }
    
    val onInstallAoaDriver: () -> Unit = {
        viewModel.driverInstallState = DriverInstallState.INSTALLING
        scope.launch {
            println("Main: Requesting UAC elevation via native ShellExecuteEx...")
            
            val args = if (requiredMi != "none") {
                "--install-driver $requiredVidHex $requiredPidHex $requiredMi"
            } else {
                "--install-driver $requiredVidHex $requiredPidHex"
            }
            
            val target = com.sanket.tools.nexpaddesktop.utils.ElevationTargetResolver.resolveHelper(args)
            
            when (val result = com.sanket.tools.nexpaddesktop.utils.WindowsElevation.runElevated(target)) {
                is com.sanket.tools.nexpaddesktop.utils.WindowsElevation.Result.Success -> {
                    println("Main: Background driver install finished with exit code ${result.exitCode}")
                    
                    // Display driver installer logs for transparency
                    val logFile = java.io.File("C:\\Users\\Public\\nexpad_driver_install.log")
                    if (logFile.exists()) {
                        try {
                            logFile.readLines().forEach { println("Main [DriverLog]: $it") }
                        } catch (_: Exception) {}
                    }
                    
                    val success = result.exitCode == 0
                    aoaManager.onDriverInstallCompleted(success)
                    if (success) {
                        println("Main: WinUSB driver installed successfully. Scanner loop will retry handshake.")
                        viewModel.isAoaDriverNeeded = false
                        viewModel.driverInstallState = DriverInstallState.FINISHED
                        kotlinx.coroutines.delay(2000)
                        viewModel.driverInstallState = DriverInstallState.IDLE
                    } else {
                        println("Main: Driver installation failed (Code ${result.exitCode}).")
                        viewModel.driverInstallState = DriverInstallState.IDLE
                    }
                }
                is com.sanket.tools.nexpaddesktop.utils.WindowsElevation.Result.UserCancelled -> {
                    println("Main: User cancelled UAC prompt.")
                    viewModel.driverInstallState = DriverInstallState.IDLE
                }
                is com.sanket.tools.nexpaddesktop.utils.WindowsElevation.Result.Error -> {
                    println("Main: Failed to launch elevated process. Win32 Error: ${result.errorCode} - ${result.message}")
                    viewModel.driverInstallState = DriverInstallState.IDLE
                }
            }
        }
    }
    viewModel.onInstallAoaDriver = onInstallAoaDriver
    
    // ══════════════════════════════════════════════════════════
    //  DRIVER LIFECYCLE — Re-create driver when controller type changes
    // ══════════════════════════════════════════════════════════
    DisposableEffect(viewModel.activeController) {
        val sendRumbleFeedback: (GamepadFeedback) -> Unit = { feedback ->
            scope.launch {
                try {
                    when (viewModel.activeTransport) {
                        ActiveTransport.WIFI, ActiveTransport.USB_TETHERING -> server?.sendFeedback(feedback)
                        ActiveTransport.USB_AOA -> aoaManager.sendFeedback(feedback)
                        ActiveTransport.USB_ADB -> adbBridgeManager.sendFeedback(feedback)
                        ActiveTransport.BLUETOOTH -> btServer.sendFeedback(feedback)
                        ActiveTransport.NONE -> {}
                    }
                } catch (e: Throwable) {
                    viewModel.appError = "Rumble Error: ${e.message}"
                }
            }
        }

        val newDriver = if (viewModel.activeController == ControllerType.XBOX_360) {
            VirtualGamepadDriver(onRumble = sendRumbleFeedback)
        } else {
            VirtualDualShock4Driver(onRumble = sendRumbleFeedback)
        }
        newDriver.connect()
        activeDriver = newDriver
        viewModel.isDriverConnected = newDriver.isDriverConnected()

        // If the driver isn't installed yet, launch a coroutine to keep trying in the background
        val connectionJob = scope.launch(Dispatchers.IO) {
            while (!newDriver.isDriverConnected()) {
                kotlinx.coroutines.delay(2000) // check every 2 seconds
                try {
                    newDriver.connect()
                } catch (e: Exception) {
                    // Ignore errors during polling
                }
                viewModel.isDriverConnected = newDriver.isDriverConnected()
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
                activeController = viewModel.activeController,
                gyroProcessor = gyroProcessor,
                gyroSettings = viewModel.gyroSettings,
                lsSensitivityX = viewModel.lsSensitivityX,
                lsSensitivityY = viewModel.lsSensitivityY,
                rsSensitivityX = viewModel.rsSensitivityX,
                rsSensitivityY = viewModel.rsSensitivityY,
                onProcessedAngles = { yaw, pitch ->
                    val now = System.currentTimeMillis()
                    if ((now - lastUiUpdateTime) > 33L) {
                        lastUiUpdateTime = now
                        viewModel.processedYaw = yaw
                        viewModel.processedPitch = pitch
                        viewModel.latestInput = input // We update latestInput here for the UI to prevent excessive recomposition
                    }
                }
            )

            if (activeDriver is VirtualDualShock4Driver) {
                (activeDriver as VirtualDualShock4Driver).gyroSettings = viewModel.gyroSettings
            }
            activeDriver?.updateInput(processedInput)
            dsuServer.updateInput(processedInput)
        }

        server = UdpServer(
            port = 9999,
            onClientConnected = { name, connType -> 
                val transport = if (connType == 2) ActiveTransport.USB_TETHERING else ActiveTransport.WIFI
                viewModel.updateConnectedDevice(name, transport)
                btServer.pause()
            },
            onClientDisconnected = { 
                viewModel.updateDisconnectedDevice(ActiveTransport.WIFI)
                viewModel.updateDisconnectedDevice(ActiveTransport.USB_TETHERING)
            },
            onInputReceived = inputHandler
        ).apply {
            isExternalTransportActive = { viewModel.activeTransport != ActiveTransport.NONE && viewModel.activeTransport != ActiveTransport.WIFI && viewModel.activeTransport != ActiveTransport.USB_TETHERING }
        }

        aoaManager.onAoaConnected = { name -> 
            viewModel.updateConnectedDevice(name, ActiveTransport.USB_AOA)
            btServer.pause()
        }
        aoaManager.onAoaDisconnected = { 
            viewModel.updateDisconnectedDevice(ActiveTransport.USB_AOA)
        }
        aoaManager.onInputReceived = inputHandler

        // Mutual exclusion: If USB Debugging is ON and ADB is detected, AOA is suppressed completely
        aoaManager.isAdbActive = { adbBridgeManager.hasActiveAdb() }
        aoaManager.isAdbInitialScanCompleted = { adbBridgeManager.isInitialScanCompleted.get() }
        // Single Active Transport Guard: pause USB scanning when another transport is active
        aoaManager.isScanningPaused = { viewModel.activeTransport != ActiveTransport.NONE && viewModel.activeTransport != ActiveTransport.USB_AOA }

        aoaManager.onDriverNeedChanged = { needed, vid, pid, mi -> 
            if (viewModel.activeTransport != ActiveTransport.NONE || adbBridgeManager.hasActiveAdb()) {
                viewModel.isAoaDriverNeeded = false
            } else {
                viewModel.isAoaDriverNeeded = needed
                if (needed && vid != null && pid != null) {
                    requiredVidHex = String.format("%04X", vid)
                    requiredPidHex = String.format("%04X", pid)
                    requiredMi = mi?.toString() ?: "none"
                }
            }
        }
        
        adbBridgeManager.onAdbConnected = { name -> 
            viewModel.updateConnectedDevice(name, ActiveTransport.USB_ADB)
            btServer.pause()
        }
        adbBridgeManager.onAdbDisconnected = { 
            viewModel.updateDisconnectedDevice(ActiveTransport.USB_ADB)
        }
        adbBridgeManager.onInputReceived = inputHandler
        // Single Active Transport Guard: pause ADB process polling when another transport is active
        adbBridgeManager.isScanningPaused = { viewModel.activeTransport != ActiveTransport.NONE && viewModel.activeTransport != ActiveTransport.USB_ADB }
        adbBridgeManager.startScanner(scope)

        // Bluetooth RFCOMM Server
        btServer.isExternalTransportActive = { viewModel.activeTransport != ActiveTransport.NONE && viewModel.activeTransport != ActiveTransport.BLUETOOTH }
        btServer.onBtConnected = { name ->
            viewModel.updateConnectedDevice(name, ActiveTransport.BLUETOOTH)
        }
        btServer.onBtDisconnected = { 
            viewModel.updateDisconnectedDevice(ActiveTransport.BLUETOOTH)
        }
        btServer.onInputReceived = inputHandler
        btServer.start(scope)

        // Discovery server remains unpaused so phones can always discover the PC on LAN
        discoveryServer.isPaused = { false }

        scope.launch(Dispatchers.IO) { aoaManager.scanAndConnect() }

        scope.launch { server?.start() }
        
        scope.launch { discoveryServer.start() }
        onDispose {
            btServer.stop()
            adbBridgeManager.stop()
            server?.stop()
            discoveryServer.stop()
            dsuServer.stop()
        }
    }

    LaunchedEffect(viewModel.activeTransport, server) {
        UniversalPushManager.currentTransport = viewModel.activeTransport
        UniversalPushManager.udpServer = server
        UniversalPushManager.aoaManager = aoaManager
        UniversalPushManager.btServer = btServer
    }

    viewModel.onRecalibrate = {
        // Reset the driver's internal gyro calibration in-memory without disconnecting the virtual USB controller
        activeDriver?.recalibrate()
        gyroProcessor.reset()
    }
    viewModel.onControllerChange = {
        viewModel.activeController = it
    }
    viewModel.dsuClientCount = dsuServer.getClientCount()

    Window(
        onCloseRequest = ::exitApplication,
        title = "NEXPAD Desktop Server"
    ) {
        NexpadDesktopTheme {
            MainApplicationWindow(viewModel = viewModel)
        }
    }
    }
}
