package com.sanket.tools.nexpaddesktop.ui

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.ui.NavDisplay
import com.sanket.tools.nexpaddesktop.driver.IGamepadDriver
import com.sanket.tools.nexpad.model.GamepadInput
import com.sanket.tools.nexpaddesktop.model.GyroSettings
import com.sanket.tools.nexpaddesktop.ui.components.drawCyberGrid
import com.sanket.tools.nexpaddesktop.ui.theme.NeonPalette

enum class Screen { HOME, CONTROLLER, PLUGINS, NODE, CONVERTER, OUTPUT, KBM }
enum class ControllerType(val displayName: String) { 
    XBOX_360("Microsoft Xbox 360"), 
    DUALSHOCK_4("Sony PlayStation 4 (DualShock 4)") 
}

@Composable
fun MainApplicationWindow(
    driver: IGamepadDriver,
    latestInput: GamepadInput,
    dsuClientCount: Int,
    activeController: ControllerType,
    lsSensitivityX: Float,
    lsSensitivityY: Float,
    rsSensitivityX: Float,
    rsSensitivityY: Float,
    onLsSensitivityXChange: (Float) -> Unit,
    onLsSensitivityYChange: (Float) -> Unit,
    onRsSensitivityXChange: (Float) -> Unit,
    onRsSensitivityYChange: (Float) -> Unit,
    isDriverConnected: Boolean,
    connectedDeviceName: String?,
    activeTransport: com.sanket.tools.nexpaddesktop.connection.ActiveTransport = com.sanket.tools.nexpaddesktop.connection.ActiveTransport.NONE,
    isAoaDriverNeeded: Boolean = false,
    driverInstallState: com.sanket.tools.nexpaddesktop.connection.DriverInstallState = com.sanket.tools.nexpaddesktop.connection.DriverInstallState.IDLE,
    onInstallAoaDriver: () -> Unit = {},
    gyroSettings: GyroSettings,
    onGyroSettingsChange: (GyroSettings) -> Unit,
    processedYaw: Float,
    processedPitch: Float,
    onRecalibrate: () -> Unit,
    onControllerChange: (ControllerType) -> Unit
) {
    val backStack = remember { mutableStateListOf<NavKey>(DesktopScreenKey.Home) }
    val navigator = remember(backStack) { DesktopNav3Navigator(backStack) }
    val currentScreen = navigator.currentKey.toScreen()

    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(NeonPalette.PanelBgBottom)
            .drawBehind { drawCyberGrid() }
    ) {
        // Left Sidebar Navigation
        Sidebar(
            currentScreen = currentScreen,
            onNavigate = { screen -> navigator.navigate(screen) }
        )
        
        // Vertical Divider
        Box(modifier = Modifier.fillMaxHeight().width(1.dp).background(NeonPalette.CardIdleBorder))
        
        // Main Content Area (Navigation 3 NavDisplay)
        Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
            NavDisplay(
                backStack = backStack,
                onBack = { navigator.popBackStack() },
                transitionSpec = {
                    fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(150))
                },
                popTransitionSpec = {
                    fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(150))
                }
            ) { key ->
                NavEntry(key) {
                    when (key) {
                        is DesktopScreenKey.Home -> HomeScreen(
                            isDriverConnected = isDriverConnected,
                            connectedDeviceName = connectedDeviceName,
                            activeTransport = activeTransport,
                            isAoaDriverNeeded = isAoaDriverNeeded,
                            driverInstallState = driverInstallState,
                            onInstallAoaDriver = onInstallAoaDriver
                        )
                        is DesktopScreenKey.Controller -> ControllerScreen(
                            latestInput = latestInput,
                            activeController = activeController,
                            lsSensitivityX = lsSensitivityX,
                            lsSensitivityY = lsSensitivityY,
                            rsSensitivityX = rsSensitivityX,
                            rsSensitivityY = rsSensitivityY,
                            onLsSensitivityXChange = onLsSensitivityXChange,
                            onLsSensitivityYChange = onLsSensitivityYChange,
                            onRsSensitivityXChange = onRsSensitivityXChange,
                            onRsSensitivityYChange = onRsSensitivityYChange,
                            onSaveController = onControllerChange,
                            gyroSettings = gyroSettings,
                            onGyroSettingsChange = onGyroSettingsChange,
                            processedYaw = processedYaw,
                            processedPitch = processedPitch,
                            onRecalibrate = onRecalibrate
                        )
                        is DesktopScreenKey.Plugins -> PluginsScreen(activeTransport = activeTransport)
                        is DesktopScreenKey.Output -> OutputScreen()
                        else -> {
                            val screenName = (key as? DesktopScreenKey)?.toScreen()?.name ?: "UNKNOWN"
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("$screenName SCREEN - Coming Soon", color = NeonPalette.CardIdleText)
                            }
                        }
                    }
                }
            }
        }
    }
}
