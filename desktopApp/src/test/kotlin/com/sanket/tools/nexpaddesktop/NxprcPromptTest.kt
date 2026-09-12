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

    @Test
    fun allCategoriesExposeUniversalGenerativeDesignArchitecture() {
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

            // 1. Core Rules for Small Models
            assertTrue(prompt.contains("CORE RULES (QUICK SUMMARY FOR ALL MODELS):"), "$tag missing Core Rules anchor")

            // 2. Instruction Priority & Conflict Resolution
            assertTrue(prompt.contains("INSTRUCTION PRIORITY & CONFLICT RESOLUTION"), "$tag missing Instruction Priority")
            assertTrue(prompt.contains("The Golden Rule"), "$tag missing The Golden Rule")
            assertTrue(prompt.contains("User's Explicit Customization"), "$tag missing User's Explicit Customization rule")
            assertTrue(prompt.contains("Conflict Rule"), "$tag missing Conflict Rule")

            // 3. Rule Classification Tags
            assertTrue(prompt.contains("[GLOBAL-REQUIRED]"), "$tag missing [GLOBAL-REQUIRED] classification")
            assertTrue(prompt.contains("[COMPONENT-REQUIRED]"), "$tag missing [COMPONENT-REQUIRED] classification")
            assertTrue(prompt.contains("[RECOMMENDED]"), "$tag missing [RECOMMENDED] classification")
            assertTrue(prompt.contains("[USER-OVERRIDE]"), "$tag missing [USER-OVERRIDE] classification")
            assertTrue(prompt.contains("[OPTIONAL]"), "$tag missing [OPTIONAL] classification")

            // 4. Category Semantics & Meaning
            assertTrue(prompt.contains("CATEGORY SEMANTICS & INTERACTION MEANING:"), "$tag missing Category Semantics")
            assertTrue(prompt.contains("Visual Affordance"), "$tag missing Visual Affordance")
            assertTrue(prompt.contains("Interaction Meaning"), "$tag missing Interaction Meaning")

            // 5. Capability-Driven Utility Guidance
            assertTrue(prompt.contains("COMPILER CAPABILITIES — WHAT PRIMITIVES ARE BEST FOR:"), "$tag missing Capabilities section")
            assertTrue(prompt.contains("radial-gradient"), "$tag missing radial-gradient utility")
            assertTrue(prompt.contains("conic-gradient"), "$tag missing conic-gradient utility")

            // 6. Design Quality & Restraint
            assertTrue(prompt.contains("DESIGN QUALITY CRITERIA"), "$tag missing Design Quality Criteria")
            assertTrue(prompt.contains("DESIGN RESTRAINT"), "$tag missing Design Restraint")
            assertTrue(prompt.contains("minimum number of layers"), "$tag missing minimum layers rule")

            // 7. Structured User Customization Schema
            assertTrue(prompt.contains("USER CUSTOMIZATION SCHEMA:"), "$tag missing Structured Customization Schema")
            assertTrue(prompt.contains("The schema is a convenience, not a limitation"), "$tag missing schema convenience note")
            assertTrue(prompt.contains("STYLE"), "$tag missing STYLE slot")

            // 8. Syntax-Only Starter Template Anti-Copy Protection
            assertTrue(prompt.contains("This template demonstrates document syntax only"), "$tag missing anti-copy template warning")
        }
    }
}


