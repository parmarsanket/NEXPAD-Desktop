package com.sanket.tools.nexpaddesktop.repository

import com.sanket.tools.nexpaddesktop.data.repository.GyroRepository
import com.sanket.tools.nexpaddesktop.data.repository.GyroRepositoryImpl
import com.sanket.tools.nexpaddesktop.model.GyroSettings
import com.sanket.tools.nexpaddesktop.model.InputMode
import com.sanket.tools.nexpaddesktop.storage.DesktopSharedPreferences
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class GyroRepositoryTest {

    private lateinit var tempFile: File
    private lateinit var prefs: DesktopSharedPreferences
    private lateinit var repository: GyroRepository

    @Before
    fun setUp() {
        tempFile = File.createTempFile("gyro_repo_test", ".json")
        prefs = DesktopSharedPreferences.getCustomInstance(tempFile)
        repository = GyroRepositoryImpl(prefs)
    }

    @After
    fun tearDown() {
        if (tempFile.exists()) tempFile.delete()
        val tmpAux = File(tempFile.parentFile, "${tempFile.name}.tmp")
        if (tmpAux.exists()) tmpAux.delete()
    }

    @Test
    fun testGyroSettingsPersistence() {
        val defaultSettings = repository.getGyroSettings()
        assertFalse(defaultSettings.enabled)

        val updatedSettings = defaultSettings.copy(
            enabled = true,
            inputMode = InputMode.ABSOLUTE_TILT,
            precisionAiming = defaultSettings.precisionAiming.copy(
                deadzoneThreshold = 0.8f,
                smoothingAmount = 0.95f
            )
        )
        repository.saveGyroSettings(updatedSettings)

        val loaded = repository.getGyroSettings()
        assertTrue(loaded.enabled)
        assertEquals(InputMode.ABSOLUTE_TILT, loaded.inputMode)
        assertEquals(0.8f, loaded.precisionAiming.deadzoneThreshold)
        assertEquals(0.95f, loaded.precisionAiming.smoothingAmount)
    }

    @Test
    fun testStickSensitivitiesPersistence() {
        val initial = repository.getStickSensitivities()
        assertEquals(1.0f, initial.lsX)
        assertEquals(1.0f, initial.lsY)

        repository.saveStickSensitivities(1.5f, 1.2f, 0.8f, 2.0f)
        val loaded = repository.getStickSensitivities()
        assertEquals(1.5f, loaded.lsX)
        assertEquals(1.2f, loaded.lsY)
        assertEquals(0.8f, loaded.rsX)
        assertEquals(2.0f, loaded.rsY)
    }
}
