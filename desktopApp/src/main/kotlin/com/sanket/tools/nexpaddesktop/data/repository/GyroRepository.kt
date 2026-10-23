package com.sanket.tools.nexpaddesktop.data.repository

import com.sanket.tools.nexpaddesktop.model.GyroSettings
import com.sanket.tools.nexpaddesktop.storage.DesktopSharedPreferences
import com.sanket.tools.nexpaddesktop.storage.DesktopSharedPreferences.StickSensitivities

/**
 * Domain repository contract for motion aiming, gyro calibration, and analog stick configurations.
 */
interface GyroRepository {
    fun getGyroSettings(): GyroSettings
    fun saveGyroSettings(settings: GyroSettings)

    fun getStickSensitivities(): StickSensitivities
    fun saveStickSensitivities(lsX: Float, lsY: Float, rsX: Float, rsY: Float)
}

class GyroRepositoryImpl(
    private val prefs: DesktopSharedPreferences = DesktopSharedPreferences.getInstance()
) : GyroRepository {

    override fun getGyroSettings(): GyroSettings = prefs.loadGyroSettings()

    override fun saveGyroSettings(settings: GyroSettings) = prefs.saveGyroSettings(settings)

    override fun getStickSensitivities(): StickSensitivities = prefs.loadStickSensitivities()

    override fun saveStickSensitivities(lsX: Float, lsY: Float, rsX: Float, rsY: Float) =
        prefs.saveStickSensitivities(lsX, lsY, rsX, rsY)
}
