package com.sanket.tools.nexpaddesktop.connection.adb

import org.junit.Test
import kotlin.test.assertNotNull

class AdbPathResolverTest {

    @Test
    fun testResolveAdbPath() {
        val path = AdbPathResolver.resolveAdbPath()
        println("Resolved ADB path: $path")
        assertNotNull(path, "ADB executable should be resolved via bundled tools, PATH, or Android SDK")
    }
}
