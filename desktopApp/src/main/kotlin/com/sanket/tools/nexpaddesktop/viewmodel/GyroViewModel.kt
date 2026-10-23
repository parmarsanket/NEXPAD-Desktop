package com.sanket.tools.nexpaddesktop.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.sanket.tools.nexpaddesktop.core.base.BaseViewModel
import com.sanket.tools.nexpaddesktop.core.di.AppContainer
import com.sanket.tools.nexpaddesktop.data.repository.GyroRepository
import com.sanket.tools.nexpaddesktop.model.GyroSettings
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * ViewModel managing motion controls, gyro calibration, sensitivity tuning,
 * and flight/steering telemetry state.
 */
class GyroViewModel(
    private val gyroRepository: GyroRepository = AppContainer.gyroRepository
) : BaseViewModel() {

    private var gyroSaveJob: Job? = null

    var gyroSettings by mutableStateOf(gyroRepository.getGyroSettings())

    var lsSensitivityX: Float
        get() = gyroSettings.lsSensitivityX
        set(value) { updateLsSensitivityX(value) }

    var lsSensitivityY: Float
        get() = gyroSettings.lsSensitivityY
        set(value) { updateLsSensitivityY(value) }

    var rsSensitivityX: Float
        get() = gyroSettings.rsSensitivityX
        set(value) { updateRsSensitivityX(value) }

    var rsSensitivityY: Float
        get() = gyroSettings.rsSensitivityY
        set(value) { updateRsSensitivityY(value) }

    var processedYaw by mutableStateOf(0f)
    var processedPitch by mutableStateOf(0f)
    var flightPitchDeg by mutableStateOf(0f)
    var flightRollDeg by mutableStateOf(0f)
    var flightYawDeg by mutableStateOf(0f)

    var onRecalibrate: () -> Unit = {}

    fun updateGyroSettings(newSettings: GyroSettings) {
        gyroSettings = newSettings
        gyroSaveJob?.cancel()
        gyroSaveJob = viewModelScope.launch {
            delay(300L)
            gyroRepository.saveGyroSettings(newSettings)
        }
    }

    fun updateLsSensitivityX(value: Float) {
        val updated = gyroSettings.updateActiveStickSensitivities(lsX = value)
        updateGyroSettings(updated)
    }

    fun updateLsSensitivityY(value: Float) {
        val updated = gyroSettings.updateActiveStickSensitivities(lsY = value)
        updateGyroSettings(updated)
    }

    fun updateRsSensitivityX(value: Float) {
        val updated = gyroSettings.updateActiveStickSensitivities(rsX = value)
        updateGyroSettings(updated)
    }

    fun updateRsSensitivityY(value: Float) {
        val updated = gyroSettings.updateActiveStickSensitivities(rsY = value)
        updateGyroSettings(updated)
    }

    fun clearCallbacks() {
        onRecalibrate = {}
    }
}
