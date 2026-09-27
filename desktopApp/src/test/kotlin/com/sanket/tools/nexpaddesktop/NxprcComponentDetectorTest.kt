package com.sanket.tools.nexpaddesktop

import com.sanket.tools.nexpad.category.CategoryType
import com.sanket.tools.nexpad.category.ControlKey
import com.sanket.tools.nexpaddesktop.plugins.NxprcComponentDetector
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit test suite for [NxprcComponentDetector].
 *
 * Verifies that the Component Studio correctly detects target control, category,
 * IDs, and dimensions from raw HTML code, ensuring seamless synchronization between
 * user pasted AI code and the active studio tabs/sandbox.
 */
class NxprcComponentDetectorTest {

    @Test
    fun testPastingButtonACodeWhileOnTabB() {
        // User copied AI prompt for A, clicked Tab B (fallback is B), and pasted generated code for A:
        val htmlA = """
            <!DOCTYPE html>
            <html>
            <head>
              <style>
                .nexpad-btn { width: 96px; height: 96px; border-radius: 50%; }
              </style>
            </head>
            <body>
              <button class="nexpad-btn" data-control="A" data-category="BUTTON" data-name="Action A Button" id="rc.action_a">
                <span class="btn-label">A</span>
              </button>
            </body>
            </html>
        """.trimIndent()

        val detected = NxprcComponentDetector.detect(
            html = htmlA,
            fallbackCategory = "BUTTON",
            fallbackControl = "B",
            fallbackId = "rc.action_b",
            fallbackName = "Action B Button",
            fallbackWidthDp = 96,
            fallbackHeightDp = 96
        )

        assertTrue("Expected explicit detection", detected.isExplicitlyDefined)
        assertEquals(ControlKey.A, detected.controlKey)
        assertEquals("A", detected.defaultControl)
        assertEquals("BUTTON", detected.category)
        assertEquals(CategoryType.ABXY, detected.categoryType)
        assertEquals("rc.action_a", detected.componentId)
        assertEquals("Action A Button", detected.componentName)
        assertEquals(96, detected.widthDp)
        assertEquals(96, detected.heightDp)
    }

    @Test
    fun testPastingJoystickCodeWhileOnButtonTab() {
        val htmlJoystick = """
            <!DOCTYPE html>
            <html>
            <head>
              <style>
                .stick-housing { width: 130px; height: 130px; }
              </style>
            </head>
            <body>
              <button class="stick-housing" data-control="LS" data-category="JOYSTICK" data-name="Left Analog Stick" id="rc.stick_ls">
                <div class="stick-cap" data-layer-role="thumb-cap"></div>
              </button>
            </body>
            </html>
        """.trimIndent()

        val detected = NxprcComponentDetector.detect(
            html = htmlJoystick,
            fallbackCategory = "BUTTON",
            fallbackControl = "A",
            fallbackId = "rc.action_a",
            fallbackName = "Action A Button"
        )

        assertTrue(detected.isExplicitlyDefined)
        assertEquals(ControlKey.LS, detected.controlKey)
        assertEquals("LS", detected.defaultControl)
        assertEquals("JOYSTICK", detected.category)
        assertEquals(CategoryType.STICKS, detected.categoryType)
        assertEquals(130, detected.widthDp)
        assertEquals(130, detected.heightDp)
    }

    @Test
    fun testPastingDpadCodeWithExplicitDimensions() {
        val htmlDpad = """
            <!DOCTYPE html>
            <html>
            <head>
              <style>
                .dpad-cross { width: 140px; height: 140px; }
              </style>
            </head>
            <body>
              <button class="dpad-cross" data-control="DPAD" data-category="DPAD" id="rc.dpad">
                <span>❖</span>
              </button>
            </body>
            </html>
        """.trimIndent()

        val detected = NxprcComponentDetector.detect(
            html = htmlDpad,
            fallbackCategory = "BUTTON",
            fallbackControl = "A",
            fallbackId = "rc.action_a",
            fallbackName = "Action A Button"
        )

        assertTrue(detected.isExplicitlyDefined)
        assertEquals(ControlKey.DPAD, detected.controlKey)
        assertEquals("DPAD", detected.defaultControl)
        assertEquals("DPAD", detected.category)
        assertEquals(CategoryType.DPAD, detected.categoryType)
        assertEquals(140, detected.widthDp)
        assertEquals(140, detected.heightDp)
    }

    @Test
    fun testDetectionFromClassNamesOnly() {
        val htmlWithClassOnly = """
            <div class="nexpad-btn-b">
              <div>B Button Content</div>
            </div>
        """.trimIndent()

        val detected = NxprcComponentDetector.detect(
            html = htmlWithClassOnly,
            fallbackCategory = "DPAD",
            fallbackControl = "UP",
            fallbackId = "rc.dpad_up",
            fallbackName = "D-Pad Up"
        )

        assertTrue(detected.isExplicitlyDefined)
        assertEquals(ControlKey.B, detected.controlKey)
        assertEquals("B", detected.defaultControl)
        assertEquals("BUTTON", detected.category)
        assertEquals(CategoryType.ABXY, detected.categoryType)
    }

    @Test
    fun testDetectionFromCssSelectorInStyle() {
        val htmlWithCssSelector = """
            <style>
              .button-x {
                width: 96px;
                height: 96px;
              }
            </style>
            <div class="button-x">
              <span>X</span>
            </div>
        """.trimIndent()

        val detected = NxprcComponentDetector.detect(
            html = htmlWithCssSelector,
            fallbackCategory = "BUTTON",
            fallbackControl = "A",
            fallbackId = "rc.action_a",
            fallbackName = "Action A Button"
        )

        assertTrue(detected.isExplicitlyDefined)
        assertEquals(ControlKey.X, detected.controlKey)
        assertEquals("X", detected.defaultControl)
    }

    @Test
    fun testDetectionFromLabelSpan() {
        val htmlWithLabel = """
            <button class="cyber-btn">
              <span class="btn-label">Y</span>
            </button>
        """.trimIndent()

        val detected = NxprcComponentDetector.detect(
            html = htmlWithLabel,
            fallbackCategory = "BUTTON",
            fallbackControl = "A",
            fallbackId = "rc.action_a",
            fallbackName = "Action A Button"
        )

        assertTrue(detected.isExplicitlyDefined)
        assertEquals(ControlKey.Y, detected.controlKey)
        assertEquals("Y", detected.defaultControl)
    }

    @Test
    fun testDetectionFromCommentDirective() {
        val htmlWithComment = """
            <!-- Gamepad Button: LT -->
            <button class="trigger-paddle">
              <span>LT</span>
            </button>
        """.trimIndent()

        val detected = NxprcComponentDetector.detect(
            html = htmlWithComment,
            fallbackCategory = "BUTTON",
            fallbackControl = "A",
            fallbackId = "rc.action_a",
            fallbackName = "Action A Button"
        )

        assertTrue(detected.isExplicitlyDefined)
        assertEquals(ControlKey.LT, detected.controlKey)
        assertEquals("LT", detected.defaultControl)
        assertEquals("TRIGGER", detected.category)
        assertEquals(CategoryType.TRIGGERS, detected.categoryType)
    }

    @Test
    fun testFallbackSafetyWhenNoMetadataOrControlInHtml() {
        val genericHtml = """
            <div class="custom-card">
              <h3>Unrelated Component</h3>
              <p>Just some text without any gamepad controls.</p>
            </div>
        """.trimIndent()

        val detected = NxprcComponentDetector.detect(
            html = genericHtml,
            fallbackCategory = "BUTTON",
            fallbackControl = "B",
            fallbackId = "rc.action_b",
            fallbackName = "Action B Button",
            fallbackWidthDp = 96,
            fallbackHeightDp = 96
        )

        assertFalse("Expected isExplicitlyDefined to be false for generic HTML", detected.isExplicitlyDefined)
        assertEquals("B", detected.defaultControl)
        assertEquals("BUTTON", detected.category)
        assertEquals("rc.action_b", detected.componentId)
        assertEquals("Action B Button", detected.componentName)
        assertEquals(96, detected.widthDp)
        assertEquals(96, detected.heightDp)
    }

    @Test
    fun testBlankHtmlSafeFallback() {
        val detected = NxprcComponentDetector.detect(
            html = "   \n\t  ",
            fallbackCategory = "JOYSTICK",
            fallbackControl = "RS",
            fallbackId = "rc.stick_rs",
            fallbackName = "Right Stick",
            fallbackWidthDp = 130,
            fallbackHeightDp = 130
        )

        assertFalse(detected.isExplicitlyDefined)
        assertEquals("RS", detected.defaultControl)
        assertEquals("JOYSTICK", detected.category)
        assertEquals("rc.stick_rs", detected.componentId)
        assertEquals(130, detected.widthDp)
    }
}
