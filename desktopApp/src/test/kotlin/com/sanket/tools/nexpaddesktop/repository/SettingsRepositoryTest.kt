package com.sanket.tools.nexpaddesktop.repository

import com.sanket.tools.nexpaddesktop.data.repository.SettingsRepository
import com.sanket.tools.nexpaddesktop.data.repository.SettingsRepositoryImpl
import com.sanket.tools.nexpaddesktop.storage.DesktopSharedPreferences
import com.sanket.tools.nexpaddesktop.ui.ControllerType
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SettingsRepositoryTest {

    private lateinit var tempFile: File
    private lateinit var prefs: DesktopSharedPreferences
    private lateinit var repository: SettingsRepository

    @Before
    fun setUp() {
        tempFile = File.createTempFile("settings_repo_test", ".json")
        prefs = DesktopSharedPreferences.getCustomInstance(tempFile)
        repository = SettingsRepositoryImpl(prefs)
    }

    @After
    fun tearDown() {
        if (tempFile.exists()) tempFile.delete()
        val tmpAux = File(tempFile.parentFile, "${tempFile.name}.tmp")
        if (tmpAux.exists()) tmpAux.delete()
    }

    @Test
    fun testControllerTypePersistence() {
        assertEquals(ControllerType.XBOX_360, repository.getControllerType())
        repository.saveControllerType(ControllerType.DUALSHOCK_4)
        assertEquals(ControllerType.DUALSHOCK_4, repository.getControllerType())
    }

    @Test
    fun testDsuEnabledPersistence() {
        assertFalse(repository.isDsuEnabled())
        repository.setDsuEnabled(true)
        assertTrue(repository.isDsuEnabled())
        repository.setDsuEnabled(false)
        assertFalse(repository.isDsuEnabled())
    }

    @Test
    fun testDeveloperOptionsPersistence() {
        assertFalse(repository.isDeveloperOptionsEnabled())
        repository.setDeveloperOptionsEnabled(true)
        assertTrue(repository.isDeveloperOptionsEnabled())
    }
}
