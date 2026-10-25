package com.sanket.tools.nexpaddesktop

import com.sanket.tools.nexpaddesktop.utils.WindowsTitleBarTheme
import org.junit.Test
import javax.swing.JFrame
import kotlin.test.assertEquals

class WindowsDarkThemeTest {

    @Test
    fun testRgbToBgrConversion() {
        // Red: 0xFF0000 -> BGR: 0x0000FF
        assertEquals(0x0000FF, WindowsTitleBarTheme.rgbToBgr(0xFF0000))
        // Green: 0x00FF00 -> BGR: 0x00FF00
        assertEquals(0x00FF00, WindowsTitleBarTheme.rgbToBgr(0x00FF00))
        // Blue: 0x0000FF -> BGR: 0xFF0000
        assertEquals(0xFF0000, WindowsTitleBarTheme.rgbToBgr(0x0000FF))
        // NEXPAD SurfaceDim: 0x070B14 -> BGR: 0x140B07
        assertEquals(0x140B07, WindowsTitleBarTheme.rgbToBgr(0x070B14))
    }

    @Test
    fun testApplyDarkThemeToWindow() {
        val frame = JFrame("NEXPAD Test Window")
        frame.isUndecorated = false
        frame.pack()

        // Should execute cleanly on Windows without exceptions
        WindowsTitleBarTheme.applyDarkTheme(frame)

        frame.dispose()
    }
}
