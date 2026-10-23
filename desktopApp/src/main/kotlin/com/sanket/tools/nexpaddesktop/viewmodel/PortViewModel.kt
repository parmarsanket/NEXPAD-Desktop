package com.sanket.tools.nexpaddesktop.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.sanket.tools.nexpaddesktop.core.base.BaseViewModel
import com.sanket.tools.nexpaddesktop.core.di.AppContainer
import com.sanket.tools.nexpaddesktop.data.repository.PortRepository

/**
 * ViewModel managing custom network port selection, validation against reserved ports,
 * and server port synchronization.
 */
class PortViewModel(
    private val portRepository: PortRepository = AppContainer.portRepository
) : BaseViewModel() {

    var streamingPort by mutableStateOf(portRepository.getStreamingPort())
        private set

    var onStreamingPortChanged: ((Int) -> Unit)? = null

    fun updateStreamingPort(newPort: Int): Boolean {
        if (portRepository.isValidPort(newPort)) {
            streamingPort = newPort
            portRepository.saveStreamingPort(newPort)
            onStreamingPortChanged?.invoke(newPort)
            return true
        }
        return false
    }

    fun isReservedPort(port: Int): Boolean = portRepository.isReservedPort(port)

    fun clearCallbacks() {
        onStreamingPortChanged = null
    }
}
