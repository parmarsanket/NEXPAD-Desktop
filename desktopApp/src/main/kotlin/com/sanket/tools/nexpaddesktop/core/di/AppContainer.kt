package com.sanket.tools.nexpaddesktop.core.di

import com.sanket.tools.nexpaddesktop.data.repository.GyroRepository
import com.sanket.tools.nexpaddesktop.data.repository.GyroRepositoryImpl
import com.sanket.tools.nexpaddesktop.data.repository.PortRepository
import com.sanket.tools.nexpaddesktop.data.repository.PortRepositoryImpl
import com.sanket.tools.nexpaddesktop.data.repository.SettingsRepository
import com.sanket.tools.nexpaddesktop.data.repository.SettingsRepositoryImpl
import com.sanket.tools.nexpaddesktop.driver.GyroProcessor
import com.sanket.tools.nexpaddesktop.driver.InputPipeline
import com.sanket.tools.nexpaddesktop.storage.DesktopSharedPreferences

/**
 * Lightweight manual Dependency Injection container for NEXPAD Desktop.
 * Provides lazy-initialized, single-instance services across application layers.
 */
object AppContainer {
    val prefs: DesktopSharedPreferences by lazy {
        DesktopSharedPreferences.getInstance()
    }

    val settingsRepository: SettingsRepository by lazy {
        SettingsRepositoryImpl(prefs)
    }

    val gyroRepository: GyroRepository by lazy {
        GyroRepositoryImpl(prefs)
    }

    val portRepository: PortRepository by lazy {
        PortRepositoryImpl(prefs)
    }

    val gyroProcessor: GyroProcessor by lazy {
        GyroProcessor()
    }

    val inputPipeline: InputPipeline by lazy {
        InputPipeline
    }
}
