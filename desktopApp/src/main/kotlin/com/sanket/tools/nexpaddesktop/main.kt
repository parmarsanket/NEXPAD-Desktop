package com.sanket.tools.nexpaddesktop

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.sanket.tools.nexpaddesktop.driver.VirtualGamepadDriver
import com.sanket.tools.nexpaddesktop.model.GamepadInput
import com.sanket.tools.nexpaddesktop.network.UdpServer
import kotlinx.coroutines.launch

fun main() = application {
    val scope = rememberCoroutineScope()
    lateinit var server: UdpServer
    
    val driver = remember { 
        VirtualGamepadDriver(onRumble = { feedback -> 
            scope.launch { server.sendFeedback(feedback) }
        }) 
    }
    var latestInput by remember { mutableStateOf(GamepadInput()) }

    DisposableEffect(Unit) {
        driver.connect()
        server = UdpServer(9999) { input ->
            latestInput = input
            driver.updateInput(input)
        }
        scope.launch {
            server.start()
        }
        onDispose {
            driver.disconnect()
        }
    }

    Window(
        onCloseRequest = ::exitApplication,
        title = "NEXPAD PC Companion",
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp), 
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("UDP Server Running on Port 9999")
            Text("Listening for NEXPAD Android App...")
            Spacer(modifier = Modifier.height(16.dp))
            Text("Latest A Button State: ${latestInput.btnA}")
            Text("Latest Guide Button State: ${latestInput.btnGuide}")
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Button(onClick = { driver.simulateCrash() }) {
                Text("💥 Simulate Off-Road Crash (Test Rumble) 💥")
            }
        }
    }
}