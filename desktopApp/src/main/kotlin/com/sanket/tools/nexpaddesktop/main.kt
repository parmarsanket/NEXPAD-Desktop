package com.sanket.tools.nexpaddesktop

import androidx.compose.runtime.*
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.sanket.tools.nexpaddesktop.driver.VirtualGamepadDriver
import com.sanket.tools.nexpaddesktop.driver.IGamepadDriver
import com.sanket.tools.nexpaddesktop.model.GamepadInput
import com.sanket.tools.nexpaddesktop.network.UdpServer
import com.sanket.tools.nexpaddesktop.ui.MainApplicationWindow
import kotlinx.coroutines.launch

fun main() = application {
    val scope = rememberCoroutineScope()
    lateinit var server: UdpServer
    
    var activeController by remember { mutableStateOf(com.sanket.tools.nexpaddesktop.ui.ControllerType.XBOX_360) }
    
    // We hold a reference to the active driver so the UDP server can always access the latest one
    var activeDriver by remember { mutableStateOf<IGamepadDriver?>(null) }

    val dsuServer = remember { com.sanket.tools.nexpaddesktop.network.DsuServer() }
    var latestInput by remember { mutableStateOf(GamepadInput()) }

    // Re-instantiate and connect the driver whenever activeController changes
    DisposableEffect(activeController) {
        val newDriver = if (activeController == com.sanket.tools.nexpaddesktop.ui.ControllerType.XBOX_360) {
            VirtualGamepadDriver(onRumble = { feedback -> scope.launch { server.sendFeedback(feedback) } })
        } else {
            com.sanket.tools.nexpaddesktop.driver.VirtualDualShock4Driver(onRumble = { feedback -> scope.launch { server.sendFeedback(feedback) } })
        }
        newDriver.connect()
        activeDriver = newDriver
        
        onDispose {
            newDriver.disconnect()
        }
    }

    DisposableEffect(Unit) {
        dsuServer.start()
        
        server = UdpServer(9999) { input ->
            latestInput = input
            activeDriver?.updateInput(input)
            dsuServer.updateInput(input)
        }
        scope.launch {
            server.start()
        }
        onDispose {
            dsuServer.stop()
        }
    }

    Window(
        onCloseRequest = ::exitApplication,
        title = "NEXPAD PC Companion",
    ) {
        MainApplicationWindow(
            driver = activeDriver ?: VirtualGamepadDriver(), // fallback for initial render
            latestInput = latestInput, 
            dsuClientCount = dsuServer.getClientCount(),
            activeController = activeController,
            onControllerChange = { activeController = it }
        )
    }
}