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
import com.sanket.tools.nexpaddesktop.utils.NetworkUtils

enum class Screen { HOME, CONTROLLER_SETTINGS }
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
    
    isAdvancedGyroEnabled: Boolean,
    gyroTargetStick: String,
    gyroBlendMode: String,
    gyroSensX: Float,
    gyroSensY: Float,
    accelWeight: Float,
    gyroWeight: Float,
    gyroActivationButtons: Set<String>,
    onAdvancedGyroEnabledChange: (Boolean) -> Unit,
    onGyroTargetStickChange: (String) -> Unit,
    onGyroBlendModeChange: (String) -> Unit,
    onGyroSensXChange: (Float) -> Unit,
    onGyroSensYChange: (Float) -> Unit,
    onAccelWeightChange: (Float) -> Unit,
    onGyroWeightChange: (Float) -> Unit,
    onGyroActivationButtonsChange: (Set<String>) -> Unit,
    
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
            
            isAdvancedGyroEnabled = isAdvancedGyroEnabled,
            gyroTargetStick = gyroTargetStick,
            gyroBlendMode = gyroBlendMode,
            gyroSensX = gyroSensX,
            gyroSensY = gyroSensY,
            accelWeight = accelWeight,
            gyroWeight = gyroWeight,
            gyroActivationButtons = gyroActivationButtons,
            onAdvancedGyroEnabledChange = onAdvancedGyroEnabledChange,
            onGyroTargetStickChange = onGyroTargetStickChange,
            onGyroBlendModeChange = onGyroBlendModeChange,
            onGyroSensXChange = onGyroSensXChange,
            onGyroSensYChange = onGyroSensYChange,
            onAccelWeightChange = onAccelWeightChange,
            onGyroWeightChange = onGyroWeightChange,
            onGyroActivationButtonsChange = onGyroActivationButtonsChange,
            
            onNavigate = { currentScreen = it },
            onSaveController = onControllerChange
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
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            Button(onClick = { onNavigate(Screen.CONTROLLER_SETTINGS) }) {
                Text("⚙️ Controller Settings")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        
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
                    rotationX = (latestInput.accelY / 9.8f) * 90f
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
    
    isAdvancedGyroEnabled: Boolean,
    gyroTargetStick: String,
    gyroBlendMode: String,
    gyroSensX: Float,
    gyroSensY: Float,
    accelWeight: Float,
    gyroWeight: Float,
    gyroActivationButtons: Set<String>,
    onAdvancedGyroEnabledChange: (Boolean) -> Unit,
    onGyroTargetStickChange: (String) -> Unit,
    onGyroBlendModeChange: (String) -> Unit,
    onGyroSensXChange: (Float) -> Unit,
    onGyroSensYChange: (Float) -> Unit,
    onAccelWeightChange: (Float) -> Unit,
    onGyroWeightChange: (Float) -> Unit,
    onGyroActivationButtonsChange: (Set<String>) -> Unit,
    
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

        // Conditional UI Block
        if (selectedType == ControllerType.XBOX_360) {
            Text("Xbox 360 Specific Settings", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = MaterialTheme.colorScheme.primary)
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
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // --- Advanced Gyro Section ---
            Text("Advanced Gyro Steering", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = MaterialTheme.colorScheme.tertiary)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Enable Advanced Gyro Steering")
                Spacer(modifier = Modifier.width(16.dp))
                androidx.compose.material3.Switch(checked = isAdvancedGyroEnabled, onCheckedChange = onAdvancedGyroEnabledChange)
            }
            
            if (isAdvancedGyroEnabled) {
                Spacer(modifier = Modifier.height(8.dp))
                
                var activationExpanded by remember { mutableStateOf(false) }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Activation Button:", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(8.dp))
                    Box {
                        Button(onClick = { activationExpanded = true }) {
                            val displayName = if (gyroActivationButtons.isEmpty()) "Always On (None)" else "${gyroActivationButtons.size} Selected"
                            Text("$displayName ▼")
                        }
                        androidx.compose.material3.DropdownMenu(
                            expanded = activationExpanded,
                            onDismissRequest = { activationExpanded = false }
                        ) {
                            androidx.compose.material3.DropdownMenuItem(
                                text = { Text("Always On (Clear All)", fontWeight = if (gyroActivationButtons.isEmpty()) FontWeight.Bold else FontWeight.Normal) },
                                onClick = { 
                                    onGyroActivationButtonsChange(emptySet())
                                }
                            )
                            
                            val options = listOf("LT", "RT", "LB", "RB", "A", "B", "X", "Y", "DPAD_UP", "DPAD_DOWN", "DPAD_LEFT", "DPAD_RIGHT")
                            val names = listOf("Left Trigger (LT)", "Right Trigger (RT)", "Left Bumper (LB)", "Right Bumper (RB)", "Button A", "Button B", "Button X", "Button Y", "D-Pad Up", "D-Pad Down", "D-Pad Left", "D-Pad Right")
                            options.forEachIndexed { index, opt ->
                                val isSelected = gyroActivationButtons.contains(opt)
                                androidx.compose.material3.DropdownMenuItem(
                                    text = { 
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            androidx.compose.material3.Checkbox(
                                                checked = isSelected,
                                                onCheckedChange = null
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(names[index])
                                        }
                                    },
                                    onClick = { 
                                        if (isSelected) {
                                            onGyroActivationButtonsChange(gyroActivationButtons - opt)
                                        } else {
                                            onGyroActivationButtonsChange(gyroActivationButtons + opt)
                                        }
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text("Target Stick:", fontWeight = FontWeight.Bold)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    androidx.compose.material3.RadioButton(
                        selected = gyroTargetStick == "LEFT_STICK",
                        onClick = { onGyroTargetStickChange("LEFT_STICK") }
                    )
                    Text("Left Stick (LS)")
                    Spacer(modifier = Modifier.width(16.dp))
                    androidx.compose.material3.RadioButton(
                        selected = gyroTargetStick == "RIGHT_STICK",
                        onClick = { onGyroTargetStickChange("RIGHT_STICK") }
                    )
                    Text("Right Stick (RS)")
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                Text("Blend Mode:", fontWeight = FontWeight.Bold)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    androidx.compose.material3.RadioButton(
                        selected = gyroBlendMode == "OVERRIDE",
                        onClick = { onGyroBlendModeChange("OVERRIDE") }
                    )
                    Text("Override (Replaces stick)")
                    Spacer(modifier = Modifier.width(16.dp))
                    androidx.compose.material3.RadioButton(
                        selected = gyroBlendMode == "ADDITIVE",
                        onClick = { onGyroBlendModeChange("ADDITIVE") }
                    )
                    Text("Additive (Merges with stick)")
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                Text("Gyro Horizontal Sensitivity: ${String.format("%.2f", gyroSensX)}")
                androidx.compose.material3.Slider(value = gyroSensX, onValueChange = onGyroSensXChange, valueRange = 0.1f..3.0f)
                
                Text("Gyro Vertical Sensitivity: ${String.format("%.2f", gyroSensY)}")
                androidx.compose.material3.Slider(value = gyroSensY, onValueChange = onGyroSensYChange, valueRange = 0.1f..3.0f)
                
                Spacer(modifier = Modifier.height(16.dp))
                Text("Accelerometer Weight (Tilt Bias): ${String.format("%.2f", accelWeight)}")
                androidx.compose.material3.Slider(value = accelWeight, onValueChange = onAccelWeightChange, valueRange = 0.0f..2.0f)
                
                Text("Gyroscope Weight (Rotation Bias): ${String.format("%.2f", gyroWeight)}")
                androidx.compose.material3.Slider(value = gyroWeight, onValueChange = onGyroWeightChange, valueRange = 0.0f..2.0f)
            }
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
        }
    }
}
