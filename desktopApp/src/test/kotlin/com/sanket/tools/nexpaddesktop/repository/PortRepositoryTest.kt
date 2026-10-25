package com.sanket.tools.nexpaddesktop.repository

import com.sanket.tools.nexpaddesktop.data.repository.PortRepository
import com.sanket.tools.nexpaddesktop.data.repository.PortRepositoryImpl
import com.sanket.tools.nexpaddesktop.storage.DesktopSharedPreferences
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PortRepositoryTest {

    private lateinit var tempFile: File
    private lateinit var prefs: DesktopSharedPreferences
    private lateinit var repository: PortRepository

    @Before
    fun setUp() {
        tempFile = File.createTempFile("port_repo_test", ".json")
        prefs = DesktopSharedPreferences.getCustomInstance(tempFile)
        repository = PortRepositoryImpl(prefs)
    }

    @After
    fun tearDown() {
        if (tempFile.exists()) tempFile.delete()
        val tmpAux = File(tempFile.parentFile, "${tempFile.name}.tmp")
        if (tmpAux.exists()) tmpAux.delete()
    }

    @Test
    fun testDefaultStreamingPort() {
        assertEquals(9999, repository.getStreamingPort())
    }

    @Test
    fun testPortPersistence() {
        repository.saveStreamingPort(9996)
        assertEquals(9996, repository.getStreamingPort())
    }

    @Test
    fun testReservedPorts() {
        assertTrue(repository.isReservedPort(9995))
        assertTrue(repository.isReservedPort(9998))
        assertFalse(repository.isReservedPort(9999))
        assertFalse(repository.isReservedPort(8080))
    }

    @Test
    fun testValidPort() {
        assertTrue(repository.isValidPort(9999))
        assertTrue(repository.isValidPort(8080))
        assertFalse(repository.isValidPort(9995)) // reserved
        assertFalse(repository.isValidPort(9998)) // reserved
        assertFalse(repository.isValidPort(80))   // privileged (< 1024)
        assertFalse(repository.isValidPort(70000)) // > 65535
    }
}
