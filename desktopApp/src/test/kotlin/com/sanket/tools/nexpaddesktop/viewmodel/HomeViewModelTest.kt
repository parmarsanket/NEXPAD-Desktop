package com.sanket.tools.nexpaddesktop.viewmodel

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

class HomeViewModelTest {

    private lateinit var tempFile: File
    private lateinit var prefs: DesktopSharedPreferences
    private lateinit var viewModel: HomeViewModel

    @Before
    fun setUp() {
        tempFile = File.createTempFile("home_vm_test", ".json")
        prefs = DesktopSharedPreferences.getCustomInstance(tempFile)
        viewModel = HomeViewModel(SettingsRepositoryImpl(prefs))
    }

    @After
    fun tearDown() {
        viewModel.onCleared()
        if (tempFile.exists()) tempFile.delete()
        val tmpAux = File(tempFile.parentFile, "${tempFile.name}.tmp")
        if (tmpAux.exists()) tmpAux.delete()
    }

    @Test
    fun testUpdateActiveController() {
        assertEquals(ControllerType.XBOX_360, viewModel.activeController)
        viewModel.updateActiveController(ControllerType.DUALSHOCK_4)
        assertEquals(ControllerType.DUALSHOCK_4, viewModel.activeController)
        assertEquals(ControllerType.DUALSHOCK_4, prefs.loadControllerType())
    }

    @Test
    fun testUpdateDsuServer() {
        assertFalse(viewModel.isDsuServerEnabled)
        viewModel.updateDsuServerEnabled(true)
        assertTrue(viewModel.isDsuServerEnabled)
        assertTrue(prefs.loadDsuEnabled())
    }

    @Test
    fun testUpdateDeveloperOptions() {
        assertFalse(viewModel.isDeveloperOptionsEnabled)
        viewModel.updateDeveloperOptionsEnabled(true)
        assertTrue(viewModel.isDeveloperOptionsEnabled)
        assertTrue(prefs.loadDeveloperOptionsEnabled())
    }

    @Test
    fun testMinimizeAndRestoreWindow() {
        assertTrue(viewModel.isWindowVisible)
        viewModel.minimizeToTray()
        assertFalse(viewModel.isWindowVisible)
        viewModel.restoreWindow()
        assertTrue(viewModel.isWindowVisible)
    }
}
