package com.sanket.tools.nexpaddesktop.utils

import com.sun.jna.Library
import com.sun.jna.Memory
import com.sun.jna.Native
import com.sun.jna.Pointer
import com.sun.jna.win32.W32APIOptions
import java.awt.Window

/**
 * Applies native Windows DWM (Desktop Window Manager) Dark Mode styling to the window
 * title bar and caption, eliminating the light/white title bar on Windows 10 and Windows 11.
 */
object WindowsTitleBarTheme {

    private interface Dwmapi : Library {
        fun DwmSetWindowAttribute(
            hwnd: Pointer,
            dwAttribute: Int,
            pvAttribute: Pointer,
            cbAttribute: Int
        ): Int

        companion object {
            val INSTANCE: Dwmapi? by lazy {
                try {
                    Native.load("dwmapi", Dwmapi::class.java, W32APIOptions.DEFAULT_OPTIONS)
                } catch (_: Throwable) {
                    null
                }
            }
        }
    }

    // Windows 10 (1809 - 1909)
    private const val DWMWA_USE_IMMERSIVE_DARK_MODE_BEFORE_20H1 = 19
    // Windows 10 (20H1+ / build 19041+) and Windows 11
    private const val DWMWA_USE_IMMERSIVE_DARK_MODE = 20
    // Windows 11 (build 22000+)
    private const val DWMWA_BORDER_COLOR = 34
    private const val DWMWA_CAPTION_COLOR = 35
    private const val DWMWA_TEXT_COLOR = 36

    /**
     * Applies dark theme to the provided AWT / Compose Window title bar.
     *
     * @param window The target window.
     * @param captionColorHex RGB hex color for caption (e.g. 0x070B14 for SurfaceDim). Format: 0x00RRGGBB.
     * @param textColorHex RGB hex color for title text (e.g. 0xF0F4F8 for OnSurfacePrimary). Format: 0x00RRGGBB.
     */
    fun applyDarkTheme(
        window: Window,
        captionColorHex: Int = 0x070B14,
        textColorHex: Int = 0xF0F4F8
    ) {
        if (!System.getProperty("os.name").lowercase().contains("win")) return

        try {
            val hwnd = Native.getWindowPointer(window) ?: return
            val dwmapi = Dwmapi.INSTANCE ?: return

            // 1. Enable Immersive Dark Mode (BOOL = 1)
            val trueMem = Memory(4).apply { setInt(0, 1) }

            // Windows 10 (20H1+ / build 19041+) and Windows 11
            dwmapi.DwmSetWindowAttribute(hwnd, DWMWA_USE_IMMERSIVE_DARK_MODE, trueMem, 4)

            // Windows 10 (1809 - 1909) fallback
            dwmapi.DwmSetWindowAttribute(hwnd, DWMWA_USE_IMMERSIVE_DARK_MODE_BEFORE_20H1, trueMem, 4)

            // 2. On Windows 11 (build 22000+), customize title bar background and text color
            // COLORREF in Win32 is 0x00BBGGRR
            val bbggrrCaption = rgbToBgr(captionColorHex)
            val captionMem = Memory(4).apply { setInt(0, bbggrrCaption) }
            dwmapi.DwmSetWindowAttribute(hwnd, DWMWA_CAPTION_COLOR, captionMem, 4)

            val bbggrrText = rgbToBgr(textColorHex)
            val textMem = Memory(4).apply { setInt(0, bbggrrText) }
            dwmapi.DwmSetWindowAttribute(hwnd, DWMWA_TEXT_COLOR, textMem, 4)

            // Subtle border color matching the dark frame
            val borderMem = Memory(4).apply { setInt(0, rgbToBgr(0x1E2738)) }
            dwmapi.DwmSetWindowAttribute(hwnd, DWMWA_BORDER_COLOR, borderMem, 4)

        } catch (e: Throwable) {
            System.err.println("⚠️ [WindowsTitleBarTheme] Failed to apply dark title bar: ${e.message}")
        }
    }

    /**
     * Converts a standard 0x00RRGGBB color integer into Win32 COLORREF format (0x00BBGGRR).
     */
    fun rgbToBgr(rgb: Int): Int {
        val r = (rgb shr 16) and 0xFF
        val g = (rgb shr 8) and 0xFF
        val b = rgb and 0xFF
        return (b shl 16) or (g shl 8) or r
    }
}
