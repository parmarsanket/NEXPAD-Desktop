package com.sanket.tools.nexpaddesktop.viewmodel

import com.sanket.tools.nexpaddesktop.data.repository.PortRepositoryImpl
import com.sanket.tools.nexpaddesktop.storage.DesktopSharedPreferences
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PortViewModelTest {

    private lateinit var tempFile: File
    private lateinit var prefs: DesktopSharedPreferences
    private lateinit var viewModel: PortViewModel

    @Before
    fun setUp() {
        tempFile = File.createTempFile("port_vm_test", ".json")
        prefs = DesktopSharedPreferences.getCustomInstance(tempFile)
        viewModel = PortViewModel(PortRepositoryImpl(prefs))
    }

    @After
    fun tearDown() {
        viewModel.onCleared()
        if (tempFile.exists()) tempFile.delete()
        val tmpAux = File(tempFile.parentFile, "${tempFile.name}.tmp")
        if (tmpAux.exists()) tmpAux.delete()
    }

    @Test
    fun testDefaultPort() {
        assertEquals(9999, viewModel.streamingPort)
    }

    @Test
    fun testUpdatePortValid() {
        var notifiedPort = 0
        viewModel.onStreamingPortChanged = { notifiedPort = it }

        val success = viewModel.updateStreamingPort(10005)
        assertTrue(success)
        assertEquals(10005, viewModel.streamingPort)
        assertEquals(10005, notifiedPort)
        assertEquals(10005, prefs.getStreamingPort())
    }

    @Test
    fun testUpdatePortReservedFails() {
        var notified = false
        viewModel.onStreamingPortChanged = { notified = true }

        // 9995 is reserved
        val success9995 = viewModel.updateStreamingPort(9995)
        assertFalse(success9995)
        assertEquals(9999, viewModel.streamingPort)
        assertFalse(notified)

        // 9998 is reserved
        val success9998 = viewModel.updateStreamingPort(9998)
        assertFalse(success9998)
        assertEquals(9999, viewModel.streamingPort)
    }

    @Test
    fun testUpdatePortOutOfRangeFails() {
        assertFalse(viewModel.updateStreamingPort(80))
        assertFalse(viewModel.updateStreamingPort(70000))
        assertEquals(9999, viewModel.streamingPort)
    }
}
