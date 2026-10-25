package com.sanket.tools.nexpaddesktop.app

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.toPainter
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.sanket.tools.nexpad.model.GamepadFeedback
import com.sanket.tools.nexpad.model.GamepadInput
import com.sanket.tools.nexpaddesktop.connection.ActiveTransport
import com.sanket.tools.nexpaddesktop.connection.ConnectionCoordinator
import com.sanket.tools.nexpaddesktop.connection.DriverInstallState
import com.sanket.tools.nexpaddesktop.connection.wifi.DsuServer
import com.sanket.tools.nexpaddesktop.driver.GamepadDriverManager
import com.sanket.tools.nexpaddesktop.driver.GyroProcessor
import com.sanket.tools.nexpaddesktop.driver.InputPipeline
import com.sanket.tools.nexpaddesktop.model.ControllerType
import com.sanket.tools.nexpaddesktop.model.computeSensorMode
import com.sanket.tools.nexpaddesktop.ui.MainApplicationWindow
import com.sanket.tools.nexpaddesktop.ui.SplashScreen
import com.sanket.tools.nexpaddesktop.ui.theme.NexpadDesktopTheme
import com.sanket.tools.nexpaddesktop.utils.ElevationTargetResolver
import com.sanket.tools.nexpaddesktop.utils.WindowsElevation
import com.sanket.tools.nexpaddesktop.utils.WindowsTitleBarTheme
import com.sanket.tools.nexpaddesktop.viewmodel.DesktopViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import javax.imageio.ImageIO

/**
 * Master Application Bootstrap.
 *
 * Coordinates initialization sequence:
 * 1. Global log redirection
 * 2. Headless driver install/revert commands
 * 3. Windows real-time QoS optimizations
 * 4. Compose Desktop application lifecycle and connection orchestration
 */
object AppBootstrap {

    fun start(args: Array<String>) {
        // 1. Initialize logging
        LogRedirector.init()

        // 2. Handle headless driver installer commands (elevated helper mode)
        if (DriverHeadlessHandler.handleCommandLine(args)) {
            return
        }

        // 3. Single Instance Guard (Prevents port conflicts and duplicate virtual gamepads)
        val appData = System.getenv("APPDATA")
        val baseDir = if (!appData.isNullOrBlank()) File(appData, "NEXPAD") else File(System.getProperty("user.home") ?: ".", ".nexpad")
        baseDir.mkdirs()
        val lockFile = File(baseDir, "app.lock")
        val lockChannel = java.io.RandomAccessFile(lockFile, "rw").channel
        val lock = try { lockChannel.tryLock() } catch (_: Throwable) { null }
        if (lock == null) {
            println("⚠️ [AppBootstrap] NEXPAD Desktop is already running. Exiting duplicate instance.")
            return
        }

        // 4. Set Windows QoS (Priority & 1ms Timer Resolution)
        QosManager.applyWindowsQos()

        // 5. Launch Desktop Compose App
        try {
            application {
                AppContent(appScope = this, onExit = ::exitApplication)
            }
        } finally {
            try { lock.release() } catch (_: Throwable) {}
            try { lockChannel.close() } catch (_: Throwable) {}
        }
    }

    @Composable
    private fun AppContent(
        appScope: androidx.compose.ui.window.ApplicationScope,
        onExit: () -> Unit
    ) {
        val scope = rememberCoroutineScope()
        val viewModel = remember { DesktopViewModel() }

        // Motion smoothing and DSU server
        val gyroProcessor = remember { GyroProcessor() }
        val dsuServer = remember {
            DsuServer().apply {
                isDs4Active = (viewModel.activeController == ControllerType.DUALSHOCK_4 && viewModel.isDsuServerEnabled)
            }
        }

        // Multi-transport connection coordinator
        val connectionCoordinator = remember {
            ConnectionCoordinator(
                streamingPort = viewModel.streamingPort,
                getActiveTransport = { viewModel.activeTransport }
            )
        }

        // Virtual Gamepad Driver Manager (Xbox 360 / DualShock 4)
        val sendRumbleFeedback: (GamepadFeedback) -> Unit = { feedback ->
            viewModel.latestFeedback = feedback
            scope.launch {
                try {
                    val currentMode = viewModel.gyroSettings.computeSensorMode(viewModel.activeController)
                    val feedbackWithMode = feedback.copy(requestedSensorMode = currentMode)
                    connectionCoordinator.sendFeedback(feedbackWithMode)
                } catch (e: Throwable) {
                    viewModel.appError = "Rumble Error: ${e.message}"
                }
            }
        }
        val driverManager = remember { GamepadDriverManager(onRumble = sendRumbleFeedback) }

        // Throttle UI telemetry updates to 30 FPS
        var lastUiUpdateTime by remember { mutableStateOf(0L) }

        // AOA WinUSB Driver installation parameters
        var requiredVidHex by remember { mutableStateOf("") }
        var requiredPidHex by remember { mutableStateOf("") }
        var requiredMi by remember { mutableStateOf("") }

        val onInstallAoaDriver: () -> Unit = onInstall@ {
            if (requiredVidHex.isBlank() || requiredPidHex.isBlank()) {
                println("Main: Cannot install AOA driver: VID/PID is empty.")
                viewModel.driverInstallState = DriverInstallState.IDLE
                return@onInstall
            }
            connectionCoordinator.aoaManager.onDriverInstallStarted()
            viewModel.driverInstallState = DriverInstallState.INSTALLING
            viewModel.driverInstallProgress = 0.05f
            viewModel.driverInstallStatusText = "Requesting Administrator approval..."
            scope.launch {
                println("Main: Requesting UAC elevation via native ShellExecuteEx...")
                val args = if (requiredMi != "none") {
                    "--install-driver $requiredVidHex $requiredPidHex $requiredMi"
                } else {
                    "--install-driver $requiredVidHex $requiredPidHex"
                }
                val target = ElevationTargetResolver.resolveHelper(args)
                when (val result = WindowsElevation.runElevated(target, onProgress = { progress, status ->
                    viewModel.driverInstallProgress = progress
                    viewModel.driverInstallStatusText = status
                })) {
                    is WindowsElevation.Result.Success -> {
                        val logFile = File("C:\\Users\\Public\\nexpad_driver_install.log")
                        if (logFile.exists()) {
                            try { logFile.readLines().forEach { println("Main [DriverLog]: $it") } } catch (_: Exception) {}
                        }
                        val success = result.exitCode == 0
                        connectionCoordinator.aoaManager.onDriverInstallCompleted(success)
                        if (success) {
                            viewModel.isAoaDriverNeeded = false
                            viewModel.driverInstallProgress = 1.0f
                            viewModel.driverInstallStatusText = "Driver Installed Successfully!"
                            viewModel.driverInstallState = DriverInstallState.FINISHED
                            viewModel.showDriverSuccessToast = true
                            viewModel.showDriverSuccessDialog = true
                            viewModel.driverSuccessToastMessage = "WinUSB driver installed successfully! Please open or restart the NEXPAD app on your phone to connect."
                            viewModel.onShowTrayNotification?.invoke(
                                "NEXPAD Driver Installed",
                                "WinUSB driver installed! Please restart or open NEXPAD on your phone."
                            )
                            delay(4000)
                            viewModel.driverInstallState = DriverInstallState.IDLE
                            delay(8000)
                            viewModel.showDriverSuccessToast = false
                        } else {
                            viewModel.driverInstallProgress = 0f
                            viewModel.driverInstallStatusText = ""
                            viewModel.driverInstallState = DriverInstallState.IDLE
                        }
                    }
                    is WindowsElevation.Result.UserCancelled -> {
                        connectionCoordinator.aoaManager.onDriverInstallCompleted(false)
                        connectionCoordinator.aoaManager.userDismissedElevation = true
                        viewModel.driverInstallProgress = 0f
                        viewModel.driverInstallStatusText = ""
                        viewModel.driverInstallState = DriverInstallState.IDLE
                    }
                    is WindowsElevation.Result.Error -> {
                        connectionCoordinator.aoaManager.onDriverInstallCompleted(false)
                        viewModel.driverInstallProgress = 0f
                        viewModel.driverInstallStatusText = ""
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
            driverManager.switchController(viewModel.activeController, scope) { connected ->
                viewModel.isDriverConnected = connected
            }
            gyroProcessor.reset()
            dsuServer.isDs4Active = (viewModel.activeController == ControllerType.DUALSHOCK_4 && viewModel.isDsuServerEnabled)

            onDispose {
                driverManager.disconnect()
            }
        }

        // ══════════════════════════════════════════════════════════
        //  SENSOR MODE SYNC — Dynamic sensor multiplexing (IMU vs Gravity)
        // ══════════════════════════════════════════════════════════
        LaunchedEffect(viewModel.activeController, viewModel.gyroSettings.enabled, viewModel.gyroSettings.inputMode, viewModel.activeTransport) {
            val targetSensorMode = viewModel.gyroSettings.computeSensorMode(viewModel.activeController)
            val feedback = GamepadFeedback(
                leftMotorSpeed = 0,
                rightMotorSpeed = 0,
                requestedSensorMode = targetSensorMode
            )
            connectionCoordinator.sendFeedback(feedback)
        }

        // ══════════════════════════════════════════════════════════
        //  MAIN PIPELINE & SERVER LIFECYCLE
        // ══════════════════════════════════════════════════════════
        DisposableEffect(Unit) {
            dsuServer.isDs4Active = (viewModel.activeController == ControllerType.DUALSHOCK_4 && viewModel.isDsuServerEnabled)
            dsuServer.start()

            var lastPacketReceivedTime = 0L
            val inputProcessingLock = Any()
            val resetToNeutral = {
                val neutral = GamepadInput()
                driverManager.updateInput(neutral)
                dsuServer.updateInput(neutral)
                gyroProcessor.reset()
                viewModel.latestInput = neutral
            }

            val inputHandler: (GamepadInput) -> Unit = { input ->
                lastPacketReceivedTime = System.currentTimeMillis()
                synchronized(inputProcessingLock) {
                    InputPipeline.processAndDispatch(
                        input = input,
                        driver = driverManager.activeDriver,
                        dsuServer = dsuServer,
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
                                viewModel.latestInput = input
                            }
                        },
                        onFlightAttitude = { pitch, roll, yaw ->
                            val now = System.currentTimeMillis()
                            if ((now - lastUiUpdateTime) <= 33L) {
                                viewModel.flightPitchDeg = pitch
                                viewModel.flightRollDeg = roll
                                viewModel.flightYawDeg = yaw
                            }
                        }
                    )
                }
            }

            connectionCoordinator.onTransportConnected = { transport, name ->
                viewModel.updateConnectedDevice(name, transport)
            }
            // Silence watchdog: if transport is active but no packets received for > 500ms, push neutral report once
            val watchdogJob = scope.launch(Dispatchers.Default) {
                var wasNeutralized = true
                while (isActive) {
                    delay(100)
                    val timeSinceLastPacket = System.currentTimeMillis() - lastPacketReceivedTime
                    if (viewModel.activeTransport != ActiveTransport.NONE && lastPacketReceivedTime > 0L && timeSinceLastPacket > 500L) {
                        if (!wasNeutralized) {
                            resetToNeutral()
                            wasNeutralized = true
                        }
                    } else if (timeSinceLastPacket <= 500L) {
                        wasNeutralized = false
                    }
                }
            }

            connectionCoordinator.onTransportDisconnected = { transport ->
                viewModel.updateDisconnectedDevice(transport)
                resetToNeutral()
            }
            connectionCoordinator.onInputReceived = inputHandler

            connectionCoordinator.onDriverNeedChanged = { needed, vid, pid, mi ->
                if (viewModel.activeTransport != ActiveTransport.NONE || connectionCoordinator.adbBridgeManager.hasActiveAdb()) {
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

            viewModel.onStreamingPortChanged = { newPort ->
                connectionCoordinator.updateStreamingPort(newPort, scope)
            }


            connectionCoordinator.start(scope)

            onDispose {
                watchdogJob.cancel()
                resetToNeutral()
                viewModel.flushSettings()
                connectionCoordinator.stop()
                dsuServer.stop()
                viewModel.clearCallbacks()
            }
        }

        LaunchedEffect(viewModel.activeTransport) {
            connectionCoordinator.syncUniversalPush()
        }

        viewModel.onRecalibrate = {
            if (viewModel.activeController == ControllerType.DUALSHOCK_4) {
                driverManager.recalibrate()
            }
            gyroProcessor.recalibrateZeroAttitude()
            gyroProcessor.reset()
        }

        viewModel.onControllerChange = {
            viewModel.updateActiveController(it)
            dsuServer.isDs4Active = (it == ControllerType.DUALSHOCK_4 && viewModel.isDsuServerEnabled)
        }
        viewModel.dsuClientCount = dsuServer.getClientCount()

        LaunchedEffect(dsuServer, viewModel.activeController, viewModel.isDsuServerEnabled) {
            dsuServer.isDs4Active = (viewModel.activeController == ControllerType.DUALSHOCK_4 && viewModel.isDsuServerEnabled)
            while (true) {
                viewModel.dsuClientCount = dsuServer.getClientCount()
                delay(1000)
            }
        }

        val appIcon = remember {
            Thread.currentThread().contextClassLoader.getResourceAsStream("images/nexpad_icon.png")?.use {
                ImageIO.read(it).toPainter()
            } ?: error("Failed to load images/nexpad_icon.png from classpath")
        }

        with(TrayManager) {
            appScope.SetupTray(
                viewModel = viewModel,
                appIcon = appIcon,
                scope = scope,
                onExit = onExit
            )
        }

        Window(
            onCloseRequest = onExit,
            visible = viewModel.isWindowVisible,
            title = "NEXPAD Desktop",
            icon = appIcon
        ) {
            LaunchedEffect(window, viewModel.isWindowVisible) {
                if (viewModel.isWindowVisible) {
                    WindowsTitleBarTheme.applyDarkTheme(window)
                }
            }

            NexpadDesktopTheme {
                var showSplash by remember { mutableStateOf(true) }
                Crossfade(
                    targetState = showSplash,
                    animationSpec = tween(400)
                ) { isSplash ->
                    if (isSplash) {
                        SplashScreen(
                            onSplashFinished = { showSplash = false }
                        )
                    } else {
                        MainApplicationWindow(viewModel = viewModel)
                    }
                }
            }
        }
    }
}
