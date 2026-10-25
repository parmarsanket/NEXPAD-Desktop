package com.sanket.tools.nexpaddesktop.viewmodel

import com.sanket.tools.nexpaddesktop.data.repository.GyroRepositoryImpl
import com.sanket.tools.nexpaddesktop.model.InputMode
import com.sanket.tools.nexpaddesktop.storage.DesktopSharedPreferences
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class GyroViewModelTest {

    private lateinit var tempFile: File
    private lateinit var prefs: DesktopSharedPreferences
    private lateinit var viewModel: GyroViewModel

    @Before
    fun setUp() {
        tempFile = File.createTempFile("gyro_vm_test", ".json")
        prefs = DesktopSharedPreferences.getCustomInstance(tempFile)
        viewModel = GyroViewModel(GyroRepositoryImpl(prefs))
    }

    @After
    fun tearDown() {
        viewModel.onCleared()
        if (tempFile.exists()) tempFile.delete()
        val tmpAux = File(tempFile.parentFile, "${tempFile.name}.tmp")
        if (tmpAux.exists()) tmpAux.delete()
    }

    @Test
    fun testUpdateGyroSettingsAndDebouncedSave() = runBlocking {
        assertFalse(viewModel.gyroSettings.enabled)

        val updated = viewModel.gyroSettings.copy(
            enabled = true,
            inputMode = InputMode.ABSOLUTE_TILT
        )
        viewModel.updateGyroSettings(updated)
        assertTrue(viewModel.gyroSettings.enabled)

        // Wait for 300ms debounce save
        delay(400L)
        val loaded = prefs.loadGyroSettings()
        assertTrue(loaded.enabled)
        assertEquals(InputMode.ABSOLUTE_TILT, loaded.inputMode)
    }

    @Test
    fun testSensitivityUpdates() = runBlocking {
        viewModel.updateLsSensitivityX(1.75f)
        assertEquals(1.75f, viewModel.lsSensitivityX)

        viewModel.updateRsSensitivityY(2.25f)
        assertEquals(2.25f, viewModel.rsSensitivityY)
    }
}
