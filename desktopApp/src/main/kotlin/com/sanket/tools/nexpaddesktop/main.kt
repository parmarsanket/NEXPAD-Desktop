package com.sanket.tools.nexpaddesktop

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.sanket.tools.nexpaddesktop.driver.VirtualGamepadDriver
import com.sanket.tools.nexpaddesktop.model.GamepadInput
import com.sanket.tools.nexpaddesktop.network.UdpServer
import kotlinx.coroutines.launch

fun main() = application {
    val driver = remember { VirtualGamepadDriver() }
    var latestInput by remember { mutableStateOf(GamepadInput()) }
    val scope = rememberCoroutineScope()

    DisposableEffect(Unit) {
        driver.connect()
        val server = UdpServer(9999) { input ->
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
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("UDP Server Running on Port 9999\nListening for NEXPAD Android App...\nLatest A Button State: ${latestInput.btnA}")
        }
    }
}