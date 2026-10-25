package com.sanket.tools.nexpaddesktop.data.repository

import com.sanket.tools.nexpaddesktop.storage.DesktopSharedPreferences
import com.sanket.tools.nexpaddesktop.ui.ControllerType

/**
 * Domain repository contract for general desktop companion configuration settings.
 */
interface SettingsRepository {
    fun getControllerType(): ControllerType
    fun saveControllerType(controller: ControllerType)

    fun isDsuEnabled(): Boolean
    fun setDsuEnabled(enabled: Boolean)

    fun isDeveloperOptionsEnabled(): Boolean
    fun setDeveloperOptionsEnabled(enabled: Boolean)
}

class SettingsRepositoryImpl(
    private val prefs: DesktopSharedPreferences = DesktopSharedPreferences.getInstance()
) : SettingsRepository {

    override fun getControllerType(): ControllerType = prefs.loadControllerType()

    override fun saveControllerType(controller: ControllerType) = prefs.saveControllerType(controller)

    override fun isDsuEnabled(): Boolean = prefs.loadDsuEnabled()

    override fun setDsuEnabled(enabled: Boolean) = prefs.saveDsuEnabled(enabled)

    override fun isDeveloperOptionsEnabled(): Boolean = prefs.loadDeveloperOptionsEnabled()

    override fun setDeveloperOptionsEnabled(enabled: Boolean) = prefs.saveDeveloperOptionsEnabled(enabled)
}
