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

@Composable
fun MainApplicationWindow(
    driver: IGamepadDriver,
    latestInput: GamepadInput,
    dsuClientCount: Int
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(scrollState), 
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
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
        
        Text("🎮 Input Debugger", fontWeight = FontWeight.Bold, fontSize = 20.sp)
        
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
                    // Convert m/s^2 to degrees for simple visualization
                    rotationZ = -(latestInput.accelX / 9.8f) * 90f
                    rotationX = (latestInput.accelY / 9.8f) * 90f
                }
                .background(MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text("Your Phone", color = Color.White, fontWeight = FontWeight.Bold)
        }
        // ----------------------------
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(onClick = { driver.simulateCrash() }) {
            Text("💥 Simulate Off-Road Crash (Test Rumble) 💥")
        }
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}
