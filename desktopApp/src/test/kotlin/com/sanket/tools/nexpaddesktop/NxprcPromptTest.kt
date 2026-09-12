package com.sanket.tools.nexpaddesktop

import com.sanket.tools.nexpaddesktop.plugins.NxprcHtmlCssConverter
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class NxprcPromptTest {
    @Test
    fun starterTemplateUsesSelectedCategory() {
        val ls = NxprcHtmlCssConverter.getReferenceTemplate("LS", "JOYSTICK")
        val rs = NxprcHtmlCssConverter.getReferenceTemplate("RS", "JOYSTICK")

        assertTrue(ls.contains("class=\"stick-btn\""))
        assertTrue(ls.contains("data-control=\"LS\""))
        assertTrue(rs.contains("class=\"stick-btn\""))
        assertTrue(rs.contains("data-control=\"RS\""))
    }

    @Test
    fun everyCategoryTargetsItsActualRootClass() {
        val cases = listOf(
            "BUTTON" to "nexpad-btn",
            "DPAD" to "dpad-btn",
            "TRIGGER" to "trigger-btn",
            "BUMPER" to "bumper-btn",
            "JOYSTICK" to "stick-btn",
            "SYSTEM" to "system-btn"
        )

        cases.forEach { (category, rootClass) ->
            val prompt = NxprcHtmlCssConverter.generateAiPrompt(
                control = if (category == "DPAD") "UP" else "A",
                category = category,
                widthDp = 96,
                heightDp = 96
            )

            assertTrue(prompt.contains("class=\"$rootClass\""), "Missing $rootClass root for $category")
            assertTrue(prompt.contains(".$rootClass:active"), "Missing active selector for $category")
            if (rootClass != "nexpad-btn") {
                assertFalse(prompt.contains(".nexpad-btn:active"), "Wrong shared active selector leaked into $category")
            }
            assertTrue(prompt.contains("clip-path: polygon"))
            assertTrue(prompt.contains("data-category` is metadata, not a shape instruction"))
            assertTrue(prompt.contains("OPTIONAL STARTER TEMPLATE"))
            assertTrue(prompt.contains("REFERENCE ONLY"))
            assertTrue(prompt.contains("Do not use `@media`"))
            assertTrue(prompt.contains("Text must be real DOM text"))
            assertTrue(prompt.contains("Self-check before output"))
            assertTrue(prompt.contains("Return ONLY the complete, self-contained HTML/CSS"))
        }
    }

    @Test
    fun promptProtectsCreativeFreedomWithoutPromisingUnsupportedBrowserFeatures() {
        val prompt = NxprcHtmlCssConverter.generateAiPrompt("A", "BUTTON", 96, 96)

        assertTrue(prompt.contains("data-category` is metadata, not a shape instruction"))
        assertTrue(prompt.contains("Preserve the user's requested shape"))
        assertTrue(prompt.contains("filter: blur()"))
        assertTrue(prompt.contains("exactly one root `<button"))
        assertTrue(prompt.contains("Set `position: absolute`, `left`, `top`, `width`, and `height`"))
    }

    /**
     * Guards against future regressions where a new prompt generator forgets to call engineBoundaries().
     * Every category must independently carry the full NXPRC compiler contract rules.
     */
    @Test
    fun allCategoriesEnforceCompatibilityContract() {
        val categories = listOf(
            "BUTTON" to "A",
            "DPAD"   to "UP",
            "TRIGGER" to "RT",
            "BUMPER"  to "RB",
            "JOYSTICK" to "LS",
            "SYSTEM"  to "MENU"
        )

        categories.forEach { (category, control) ->
            val prompt = NxprcHtmlCssConverter.generateAiPrompt(
                control = control,
                category = category,
                widthDp = 96,
                heightDp = 96
            )
            val tag = "[$category/$control]"

            // Compiler contract rules shared via engineBoundaries()
            assertTrue(prompt.contains("Do not use `@media`"), "$tag missing @media ban")
            assertTrue(prompt.contains("filter: blur()"), "$tag missing blur ban")
            assertTrue(prompt.contains("Text must be real DOM text"), "$tag missing DOM-text rule")
            assertTrue(prompt.contains("Self-check before output"), "$tag missing self-check instruction")
            assertTrue(prompt.contains("Return ONLY the complete, self-contained HTML/CSS"), "$tag missing output contract")
            assertTrue(prompt.contains("data-category` is metadata, not a shape instruction"), "$tag missing creative freedom rule")
            assertTrue(prompt.contains("Preserve the user's requested shape"), "$tag missing shape preservation rule")
            assertTrue(prompt.contains("Set `position: absolute`, `left`, `top`, `width`, and `height`"), "$tag missing explicit position rule")
            assertTrue(prompt.contains("OPTIONAL STARTER TEMPLATE"), "$tag missing starter template section")
            assertTrue(prompt.contains("REFERENCE ONLY"), "$tag missing REFERENCE ONLY label")
        }
    }

    @Test
    fun allCategoriesExposeTenOutOfTenEngineCapabilities() {
        val categories = listOf(
            "BUTTON" to "A",
            "DPAD"   to "UP",
            "TRIGGER" to "RT",
            "BUMPER"  to "RB",
            "JOYSTICK" to "LS",
            "SYSTEM"  to "MENU"
        )

        categories.forEach { (category, control) ->
            val prompt = NxprcHtmlCssConverter.generateAiPrompt(
                control = control,
                category = category,
                widthDp = 96,
                heightDp = 96
            )
            val tag = "[$category/$control]"

            assertTrue(prompt.contains("--spring-damping"), "$tag missing spring-damping")
            assertTrue(prompt.contains("--spring-stiffness"), "$tag missing spring-stiffness")
            assertTrue(prompt.contains("<svg>"), "$tag missing SVG capability specification")
            assertTrue(prompt.contains("10/10"), "$tag missing 10/10 specification badge")
            assertTrue(prompt.contains("feGaussianBlur"), "$tag missing SVG filter graph specification")
        }
    }

    @Test
    fun starterTemplatesIncludeTactileSpringPhysics() {
        val templateA = NxprcHtmlCssConverter.getReferenceTemplate("A", "BUTTON")
        val templateDpad = NxprcHtmlCssConverter.getReferenceTemplate("DPAD", "DPAD")
        val templateLT = NxprcHtmlCssConverter.getReferenceTemplate("LT", "TRIGGER")

        assertTrue(templateA.contains("--spring-damping"))
        assertTrue(templateA.contains("--spring-stiffness"))
        assertTrue(templateDpad.contains("--spring-damping"))
        assertTrue(templateLT.contains("--spring-damping"))
    }
}

