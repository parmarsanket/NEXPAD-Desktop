package com.sanket.tools.nexpaddesktop

import androidx.compose.runtime.*
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.sanket.tools.nexpaddesktop.driver.VirtualGamepadDriver
import com.sanket.tools.nexpaddesktop.model.GamepadInput
import com.sanket.tools.nexpaddesktop.network.UdpServer
import com.sanket.tools.nexpaddesktop.ui.MainApplicationWindow
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
        MainApplicationWindow(driver, latestInput)
    }
}