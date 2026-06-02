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
    dsuClientCount: Int
) {
    var currentScreen by remember { mutableStateOf(Screen.HOME) }
    var activeController by remember { mutableStateOf(ControllerType.XBOX_360) }

    when (currentScreen) {
        Screen.HOME -> HomeScreen(
            driver = driver,
            latestInput = latestInput,
            dsuClientCount = dsuClientCount,
            activeController = activeController,
            onNavigate = { currentScreen = it }
        )
        Screen.CONTROLLER_SETTINGS -> ControllerSettingsScreen(
            activeController = activeController,
            onNavigate = { currentScreen = it },
            onSaveController = { activeController = it }
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
    activeController: ControllerType,
    onNavigate: (Screen) -> Unit,
    onSaveController: (ControllerType) -> Unit
) {
    var selectedType by remember { mutableStateOf(activeController) }
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp), 
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
            Spacer(modifier = Modifier.height(8.dp))
            Text("Xbox settings (like trigger sensitivity and ABXY mapping) will be added here.")
        } else if (selectedType == ControllerType.DUALSHOCK_4) {
            Text("DualShock 4 Specific Settings", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = MaterialTheme.colorScheme.secondary)
            Spacer(modifier = Modifier.height(8.dp))
            Text("PlayStation settings (like lightbar color, touchpad mapping, and gyro sensitivity) will be added here.")
        }
    }
}
