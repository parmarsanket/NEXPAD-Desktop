package com.sanket.tools.nexpaddesktop.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.sanket.tools.nexpaddesktop.driver.IGamepadDriver
import com.sanket.tools.nexpad.model.GamepadInput
import com.sanket.tools.nexpaddesktop.model.GyroSettings
import com.sanket.tools.nexpaddesktop.ui.components.drawCyberGrid
import com.sanket.tools.nexpaddesktop.ui.theme.NeonPalette

enum class Screen { HOME, CONTROLLER, NODE, CONVERTER, OUTPUT, KBM }
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
    connectionType: Int?,
    gyroSettings: GyroSettings,
    onGyroSettingsChange: (GyroSettings) -> Unit,
    processedYaw: Float,
    processedPitch: Float,
    onRecalibrate: () -> Unit,
    onControllerChange: (ControllerType) -> Unit
) {
    var currentScreen by remember { mutableStateOf(Screen.HOME) }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(NeonPalette.PanelBgBottom)
            .drawBehind { drawCyberGrid() }
    ) {
        // Left Sidebar Navigation
        Sidebar(
            currentScreen = currentScreen,
            onNavigate = { currentScreen = it }
        )
        
        // Vertical Divider
        Box(modifier = Modifier.fillMaxHeight().width(1.dp).background(NeonPalette.CardIdleBorder))
        
        // Main Content Area
        Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
            when (currentScreen) {
                Screen.HOME -> HomeScreen(
                    isDriverConnected = isDriverConnected,
                    connectedDeviceName = connectedDeviceName,
                    connectionType = connectionType
                )
                Screen.CONTROLLER -> ControllerScreen(
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
                else -> {
                    // Placeholder for NODE, CONVERTER, OUTPUT, KBM
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("${currentScreen.name} SCREEN - Coming Soon", color = NeonPalette.CardIdleText)
                    }
                }
            }
        }
    }
}
