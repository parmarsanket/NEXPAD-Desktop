package com.sanket.tools.nexpaddesktop.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.window.ApplicationScope
import androidx.compose.ui.window.Notification
import androidx.compose.ui.window.Tray
import androidx.compose.ui.window.isTraySupported
import androidx.compose.ui.window.rememberTrayState
import com.sanket.tools.nexpaddesktop.viewmodel.DesktopViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.awt.SystemTray
import java.awt.TrayIcon

/**
 * Manages Windows System Tray icon, minimize-to-tray, and system notifications.
 */
object TrayManager {

    @Composable
    fun ApplicationScope.SetupTray(
        viewModel: DesktopViewModel,
        appIcon: Painter,
        scope: CoroutineScope,
        onExit: () -> Unit
    ) {
        if (!isTraySupported) return

        val trayState = rememberTrayState()

        DisposableEffect(trayState) {
            viewModel.onShowTrayNotification = { title, message ->
                scope.launch(Dispatchers.Main) {
                    delay(100)
                    try {
                        if (SystemTray.isSupported()) {
                            val tray = SystemTray.getSystemTray()
                            val icon = tray.trayIcons.firstOrNull()
                            if (icon != null) {
                                icon.displayMessage(title, message, TrayIcon.MessageType.INFO)
                            } else {
                                trayState.sendNotification(Notification(title, message, Notification.Type.Info))
                            }
                        } else {
                            trayState.sendNotification(Notification(title, message, Notification.Type.Info))
                        }
                    } catch (e: Exception) {
                        try {
                            trayState.sendNotification(Notification(title, message, Notification.Type.Info))
                        } catch (_: Exception) {}
                    }
                }
            }
            onDispose {
                viewModel.onShowTrayNotification = null
            }
        }

        Tray(
            icon = appIcon,
            state = trayState,
            tooltip = "NEXPAD is running in the background (Active)",
            onAction = { viewModel.restoreWindow() },
            menu = {
                Item("Open NEXPAD", onClick = { viewModel.restoreWindow() })
                Separator()
                Item("Run in Background", onClick = { viewModel.minimizeToTray() })
                Separator()
                Item("Exit NEXPAD", onClick = onExit)
            }
        )
    }
}
