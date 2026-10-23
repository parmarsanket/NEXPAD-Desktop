package com.sanket.tools.nexpaddesktop.app

import org.junit.Assert.assertFalse
import org.junit.Test

class DriverHeadlessHandlerTest {

    @Test
    fun `regular arguments return false and proceed to normal application launch`() {
        val regularArgs = arrayOf("--window-mode", "--debug")
        val handled = DriverHeadlessHandler.handleCommandLine(regularArgs)
        assertFalse(handled)
    }

    @Test
    fun `empty arguments return false`() {
        val emptyArgs = emptyArray<String>()
        val handled = DriverHeadlessHandler.handleCommandLine(emptyArgs)
        assertFalse(handled)
    }
}
