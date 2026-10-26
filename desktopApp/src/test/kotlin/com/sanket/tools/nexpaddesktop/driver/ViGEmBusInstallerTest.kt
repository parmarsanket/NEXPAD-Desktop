package com.sanket.tools.nexpaddesktop.driver

import org.junit.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ViGEmBusInstallerTest {

    @Test
    fun testResolveInstallerFileFindsBundledBinary() {
        val installerFile = ViGEmBusInstaller.resolveInstallerFile()
        assertNotNull(installerFile, "ViGEmBusSetup.exe must be resolved from redist or project structure")
        assertTrue(installerFile.exists(), "Resolved installer binary must exist on disk")
        assertTrue(installerFile.length() > 1_000_000, "ViGEmBus installer size should be greater than 1 MB")
    }
}
