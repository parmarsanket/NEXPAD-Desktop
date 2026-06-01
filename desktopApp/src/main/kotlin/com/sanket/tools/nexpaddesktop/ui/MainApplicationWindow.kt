package com.sanket.tools.nexpaddesktop.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sanket.tools.nexpaddesktop.driver.IGamepadDriver
import com.sanket.tools.nexpaddesktop.model.GamepadInput
import com.sanket.tools.nexpaddesktop.utils.NetworkUtils

@Composable
fun MainApplicationWindow(
    driver: IGamepadDriver,
    latestInput: GamepadInput
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp), 
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
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
        Text("A: ${latestInput.btnA} | Guide: ${latestInput.btnGuide}")
        Text("Left Stick: X=${String.format("%.2f", latestInput.leftStickX)} Y=${String.format("%.2f", latestInput.leftStickY)}")
        
        Spacer(modifier = Modifier.height(8.dp))
        Text("📱 6-Axis Motion Data (CemuHook Server)", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Text("Accel: X=${String.format("%.2f", latestInput.accelX)} | Y=${String.format("%.2f", latestInput.accelY)} | Z=${String.format("%.2f", latestInput.accelZ)}")
        Text("Gyro: X=${String.format("%.2f", latestInput.gyroX)} | Y=${String.format("%.2f", latestInput.gyroY)} | Z=${String.format("%.2f", latestInput.gyroZ)}")
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(onClick = { driver.simulateCrash() }) {
            Text("💥 Simulate Off-Road Crash (Test Rumble) 💥")
        }
    }
}
