package com.sanket.tools.nexpaddesktop.ui


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sanket.tools.nexpaddesktop.driver.IGamepadDriver
import com.sanket.tools.nexpaddesktop.model.GamepadInput
import com.sanket.tools.nexpaddesktop.model.GyroSettings
import com.sanket.tools.nexpaddesktop.utils.NetworkUtils

enum class Screen { HOME, CONTROLLER_SETTINGS, GYRO_SETTINGS }
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
    
    gyroSettings: GyroSettings,
    onGyroSettingsChange: (GyroSettings) -> Unit,
    processedYaw: Float,
    processedPitch: Float,
    onRecalibrate: () -> Unit,
    
    onControllerChange: (ControllerType) -> Unit
) {
    var currentScreen by remember { mutableStateOf(Screen.HOME) }

    when (currentScreen) {
        Screen.HOME -> HomeScreen(
            driver = driver,
            latestInput = latestInput,
            dsuClientCount = dsuClientCount,
            activeController = activeController,
            onNavigate = { currentScreen = it }
        )
        Screen.CONTROLLER_SETTINGS -> ControllerSettingsScreen(
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
            onNavigate = { currentScreen = it },
            onSaveController = onControllerChange
        )
        Screen.GYRO_SETTINGS -> GyroSettingsScreen(
            settings = gyroSettings,
            onSettingsChange = onGyroSettingsChange,
            activeController = activeController,
            rawGyroX = latestInput.gyroX,
            rawGyroY = latestInput.gyroY,
            rawGyroZ = latestInput.gyroZ,
            rawAccelX = latestInput.accelX,
            rawAccelY = latestInput.accelY,
            rawAccelZ = latestInput.accelZ,
            processedYaw = processedYaw,
            processedPitch = processedPitch,
            onRecalibrate = onRecalibrate,
            onNavigate = { currentScreen = it },
        )
    }
}

@Composable
fun HomeScreen(
    driver: IGamepadDriver,
    latestInput: GamepadInput,
    dsuClientCount: Int,
    activeController: ControllerType,
    onNavigate: (Screen) -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(scrollState), 
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)) {
            Button(onClick = { onNavigate(Screen.GYRO_SETTINGS) }) {
                Text("🎯 6-Axis Gyro Settings")
            }
            Button(onClick = { onNavigate(Screen.CONTROLLER_SETTINGS) }) {
                Text("⚙️ Controller Settings")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (!driver.isDriverConnected()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFD32F2F), shape = RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Text(
                        text = "⚠️ Virtual Controller Driver (ViGEmBus) Missing!",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "NEXPAD cannot emulate physical game controllers without the ViGEmBus kernel driver. Please run the installer or install ViGEmBusSetup.exe from the redist folder.",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 14.sp
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        val localIp = remember { NetworkUtils.getLocalIpAddress() }
        Text("Your PC IP Address:", fontSize = 18.sp)
        Text(
            text = localIp, 
            fontWeight = FontWeight.Bold, 
            fontSize = 24.sp, 
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        Text("UDP Server Running on Port 9999")
        Text("Listening for NEXPAD Android App...")
        Spacer(modifier = Modifier.height(16.dp))
        
        Text("🎮 Input Debugger (${activeController.displayName})", fontWeight = FontWeight.Bold, fontSize = 20.sp)
        
        // Face Buttons
        Text("Face Buttons: A: ${latestInput.btnA} | B: ${latestInput.btnB} | X: ${latestInput.btnX} | Y: ${latestInput.btnY}")
        
        // D-Pad
        Text("D-Pad: UP: ${latestInput.dpadUp} | DOWN: ${latestInput.dpadDown} | LEFT: ${latestInput.dpadLeft} | RIGHT: ${latestInput.dpadRight}")
        
        // Bumpers & Clicks
        Text("Bumpers: LB: ${latestInput.btnL1} | RB: ${latestInput.btnR1} | LS Click: ${latestInput.btnL3} | RS Click: ${latestInput.btnR3}")
        
        // System Buttons
        Text("System: Start: ${latestInput.btnStart} | Select: ${latestInput.btnSelect} | Guide: ${latestInput.btnGuide} | Share: ${latestInput.btnShare} | Screenshot: ${latestInput.btnScreenshot}")
        
        // Advanced / Elite
        Text("Macro/Elite: M1: ${latestInput.btnM1} | M2: ${latestInput.btnM2} | M3: ${latestInput.btnM3} | M4: ${latestInput.btnM4} | Profile: ${latestInput.btnProfile} | Turbo: ${latestInput.btnTurbo}")
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Triggers
        Text("Triggers:", fontWeight = FontWeight.Bold)
        Text("LT: ${String.format("%.2f", latestInput.triggerL2)} | RT: ${String.format("%.2f", latestInput.triggerR2)}")
        
        // Joysticks
        Text("Joysticks:", fontWeight = FontWeight.Bold)
        Text("Left Stick: X=${String.format("%.2f", latestInput.leftStickX)} Y=${String.format("%.2f", latestInput.leftStickY)}")
        Text("Right Stick: X=${String.format("%.2f", latestInput.rightStickX)} Y=${String.format("%.2f", latestInput.rightStickY)}")
        
        Spacer(modifier = Modifier.height(8.dp))
        Text("📱 6-Axis Motion Data (CemuHook Server)", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Text("Connected Emulators/Clients: $dsuClientCount", fontWeight = FontWeight.Bold, color = if(dsuClientCount > 0) Color(0xFF00C853) else Color.Red)
        Text("Accel: X=${String.format("%.2f", latestInput.accelX)} | Y=${String.format("%.2f", latestInput.accelY)} | Z=${String.format("%.2f", latestInput.accelZ)}")
        Text("Gyro: X=${String.format("%.2f", latestInput.gyroX)} | Y=${String.format("%.2f", latestInput.gyroY)} | Z=${String.format("%.2f", latestInput.gyroZ)}")
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // --- 3D MOTION VISUALIZER ---
        Text("Phone Tilt Visualizer", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
        Box(
            modifier = Modifier
                .size(150.dp, 80.dp)
                .graphicsLayer {
                    rotationZ = -(latestInput.accelX / 9.8f) * 90f
                    rotationX = (latestInput.accelZ / 9.8f) * 90f
                }
                .background(MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text("Your Phone", color = Color.White, fontWeight = FontWeight.Bold)
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(onClick = { driver.simulateCrash() }) {
            Text("💥 Simulate Off-Road Crash (Test Rumble) 💥")
        }
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun ControllerSettingsScreen(
    latestInput: GamepadInput,
    activeController: ControllerType,
    lsSensitivityX: Float,
    lsSensitivityY: Float,
    rsSensitivityX: Float,
    rsSensitivityY: Float,
    onLsSensitivityXChange: (Float) -> Unit,
    onLsSensitivityYChange: (Float) -> Unit,
    onRsSensitivityXChange: (Float) -> Unit,
    onRsSensitivityYChange: (Float) -> Unit,
    onNavigate: (Screen) -> Unit,
    onSaveController: (ControllerType) -> Unit
) {
    var selectedType by remember { mutableStateOf(activeController) }
    var expanded by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp).verticalScroll(scrollState), 
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Top
    ) {
        Button(onClick = { onNavigate(Screen.HOME) }) {
            Text("< Back to Home")
        }

        Spacer(modifier = Modifier.height(24.dp))
        
        Text("Controller Emulation Type", fontWeight = FontWeight.Bold, fontSize = 24.sp)
        Text("Select which virtual controller NEXPAD should emulate to the PC.", color = Color.Gray)
        
        Spacer(modifier = Modifier.height(16.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Box {
                Button(onClick = { expanded = true }) {
                    Text(selectedType.displayName + " ▼")
                }
                androidx.compose.material3.DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    androidx.compose.material3.DropdownMenuItem(
                        text = { Text(ControllerType.XBOX_360.displayName) },
                        onClick = { 
                            selectedType = ControllerType.XBOX_360 
                            expanded = false 
                        }
                    )
                    androidx.compose.material3.DropdownMenuItem(
                        text = { Text(ControllerType.DUALSHOCK_4.displayName) },
                        onClick = { 
                            selectedType = ControllerType.DUALSHOCK_4 
                            expanded = false 
                        }
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Button(
                onClick = { 
                    onSaveController(selectedType) 
                    onNavigate(Screen.HOME)
                },
                enabled = selectedType != activeController
            ) {
                Text("Save & Apply")
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Conditional UI Block — Stick sensitivity (Xbox only) and DS4 debugger
        if (selectedType == ControllerType.XBOX_360) {
            Text("Xbox 360 Stick Settings", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(16.dp))
            
            Text("Left Stick Horizontal Sensitivity: ${String.format("%.2f", lsSensitivityX)}")
            androidx.compose.material3.Slider(value = lsSensitivityX, onValueChange = onLsSensitivityXChange, valueRange = 0.1f..3.0f)
            
            Text("Left Stick Vertical Sensitivity: ${String.format("%.2f", lsSensitivityY)}")
            androidx.compose.material3.Slider(value = lsSensitivityY, onValueChange = onLsSensitivityYChange, valueRange = 0.1f..3.0f)
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text("Right Stick Horizontal Sensitivity: ${String.format("%.2f", rsSensitivityX)}")
            androidx.compose.material3.Slider(value = rsSensitivityX, onValueChange = onRsSensitivityXChange, valueRange = 0.1f..3.0f)
            
            Text("Right Stick Vertical Sensitivity: ${String.format("%.2f", rsSensitivityY)}")
            androidx.compose.material3.Slider(value = rsSensitivityY, onValueChange = onRsSensitivityYChange, valueRange = 0.1f..3.0f)

            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                "💡 For gyro/motion settings, use the dedicated 🎯 6-Axis Gyro Settings screen.",
                fontSize = 14.sp, color = Color.Gray,
            )
        } else if (selectedType == ControllerType.DUALSHOCK_4) {
            Text("DualShock 4 Specific Settings", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = MaterialTheme.colorScheme.secondary)
            Spacer(modifier = Modifier.height(16.dp))
            
            Text("🎮 Live PS4 Joystick Debugger", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Text("Watch the raw values from your phone convert to PS4 USB format (0-255).", fontSize = 14.sp, color = Color.Gray)
            
            Spacer(modifier = Modifier.height(8.dp))
            
            val lx = ((latestInput.leftStickX + 1f) / 2f * 255f).toInt().coerceIn(0, 255)
            val ly = ((-latestInput.leftStickY + 1f) / 2f * 255f).toInt().coerceIn(0, 255)
            val rx = ((latestInput.rightStickX + 1f) / 2f * 255f).toInt().coerceIn(0, 255)
            val ry = ((-latestInput.rightStickY + 1f) / 2f * 255f).toInt().coerceIn(0, 255)
            
            Box(modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp)).padding(16.dp)) {
                Column {
                    Text("Left Stick (L3)", color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                    Text("Raw X: ${String.format("%.2f", latestInput.leftStickX)} ➔ DS4 Byte: $lx", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Raw Y: ${String.format("%.2f", latestInput.leftStickY)} ➔ DS4 Byte: $ly", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text("Right Stick (R3)", color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                    Text("Raw X: ${String.format("%.2f", latestInput.rightStickX)} ➔ DS4 Byte: $rx", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Raw Y: ${String.format("%.2f", latestInput.rightStickY)} ➔ DS4 Byte: $ry", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "💡 For gyro/motion settings, use the dedicated 🎯 6-Axis Gyro Settings screen.",
                fontSize = 14.sp, color = Color.Gray,
            )
        }
    }
}
