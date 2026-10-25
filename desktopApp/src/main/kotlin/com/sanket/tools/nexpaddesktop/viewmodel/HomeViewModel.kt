package com.sanket.tools.nexpaddesktop.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.sanket.tools.nexpaddesktop.core.base.BaseViewModel
import com.sanket.tools.nexpaddesktop.core.di.AppContainer
import com.sanket.tools.nexpaddesktop.data.repository.SettingsRepository
import com.sanket.tools.nexpaddesktop.ui.ControllerType
import com.sanket.tools.nexpaddesktop.utils.AppLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * ViewModel managing home dashboard state, controller type selection,
 * developer mode, DSU state, and system tray visibility.
 */
class HomeViewModel(
    private val settingsRepository: SettingsRepository = AppContainer.settingsRepository
) : BaseViewModel() {

    var activeController by mutableStateOf(settingsRepository.getControllerType())
    var dsuClientCount by mutableStateOf(0)
    var isDsuServerEnabled by mutableStateOf(settingsRepository.isDsuEnabled())
    var isDeveloperOptionsEnabled by mutableStateOf(settingsRepository.isDeveloperOptionsEnabled())

    var isWindowVisible by mutableStateOf(true)
    var appError by mutableStateOf("")

    var onShowTrayNotification: ((title: String, message: String) -> Unit)? = null
    var onControllerChange: (ControllerType) -> Unit = { updateActiveController(it) }

    init {
        AppLogger.isEnabled = isDeveloperOptionsEnabled
    }

    fun updateActiveController(controller: ControllerType) {
        activeController = controller
        settingsRepository.saveControllerType(controller)
    }

    fun updateDsuServerEnabled(enabled: Boolean) {
        isDsuServerEnabled = enabled
        settingsRepository.setDsuEnabled(enabled)
    }

    fun updateDeveloperOptionsEnabled(enabled: Boolean) {
        isDeveloperOptionsEnabled = enabled
        AppLogger.isEnabled = enabled
        if (!enabled) {
            AppLogger.clearLogs()
        }
        settingsRepository.setDeveloperOptionsEnabled(enabled)
    }

    fun minimizeToTray() {
        isWindowVisible = false
        onShowTrayNotification?.invoke(
            "NEXPAD Desktop",
            "NEXPAD is running in the background. Click the tray icon to restore."
        )
    }

    fun restoreWindow() {
        isWindowVisible = true
    }

    fun clearCallbacks() {
        onShowTrayNotification = null
        onControllerChange = { updateActiveController(it) }
    }
}
