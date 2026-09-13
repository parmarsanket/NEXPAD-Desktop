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

    @Test
    fun joystickPromptExplicitlyInformsTwoZonePhysicalMechanism() {
        val lsPrompt = NxprcHtmlCssConverter.generateAiPrompt("LS", "JOYSTICK", 130, 130)
        val rsPrompt = NxprcHtmlCssConverter.generateAiPrompt("RS", "JOYSTICK", 130, 130)

        listOf(lsPrompt to "L3", rsPrompt to "R3").forEach { (prompt, clickKey) ->
            assertTrue(prompt.contains("JOYSTICK TWO-ZONE PHYSICAL MECHANISM:"), "Missing two-zone physical mechanism header")
            assertTrue(prompt.contains("Stationary Gimbal Base"), "Missing stationary gimbal base description")
            assertTrue(prompt.contains("Movable Analog Thumb Cap"), "Missing movable analog thumb cap description")
            assertTrue(prompt.contains("360° Analog Deflection"), "Missing 360 analog deflection instruction")
            assertTrue(prompt.contains("Axial $clickKey Click"), "Missing axial click instruction for $clickKey")
            assertTrue(prompt.contains("--spring-damping"), "Missing spring-damping property")
            assertTrue(prompt.contains("--spring-stiffness"), "Missing spring-stiffness property")
        }
    }

    @Test
    fun joystickPromptEnforcesConsoleIndustrialRealismAndVisualQa() {
        val lsPrompt = NxprcHtmlCssConverter.generateAiPrompt("LS", "JOYSTICK", 130, 130)
        val rsPrompt = NxprcHtmlCssConverter.generateAiPrompt("RS", "JOYSTICK", 130, 130)

        listOf(lsPrompt, rsPrompt).forEach { prompt ->
            assertTrue(prompt.contains("VISUAL TARGET — CONSOLE/XBOX INDUSTRIAL REALISM:"), "Missing console industrial realism section")
            assertTrue(prompt.contains("Matte Charcoal & Polycarbonate Plastic"), "Missing matte plastic specification")
            assertTrue(prompt.contains("Physical Material Contrast"), "Missing physical material contrast specification")
            assertTrue(prompt.contains("Mechanical Clearance & Proportions"), "Missing mechanical clearance rule")
            assertTrue(prompt.contains("VISUAL QA CHECKLIST (SELF-CHECK BEFORE OUTPUT):"), "Missing visual QA checklist")
            assertTrue(prompt.contains("<div class=\"stick-base\">"), "Missing stick-base DOM instruction")
            assertTrue(prompt.contains("<div class=\"stick-cap\">"), "Missing stick-cap DOM instruction")
            assertTrue(prompt.contains("Do not write JavaScript"), "Missing JS prohibition in analog movement")
            assertTrue(prompt.contains("Circle geometry is natural and authentic"), "Missing circle geometry nuance rule")
        }
    }

    @Test
    fun verifyPresetThumbstickLsTwoStagePartitioning() {
        val doc = com.sanket.tools.nexpad.nxprc.NxprcPackager.compile(
            NxprcHtmlCssConverter.PRESET_THUMBSTICK_LS,
            "rc.stick_ls",
            "Analog Stick LS",
            "JOYSTICK",
            "LS"
        )
        println("=== PRESET_THUMBSTICK_LS COMPILATION AUDIT ===")
        println("Category: ${doc.manifest.category}, Control: ${doc.manifest.defaultControl}")
        println("Dimensions: ${doc.manifest.widthDp}x${doc.manifest.heightDp}")
        println("Cap Layer Indices: ${doc.canvas.capLayerIndices}")
        doc.canvas.layers.forEachIndexed { i, layer ->
            val isCap = i in doc.canvas.capLayerIndices
            val typeStr = layer::class.simpleName
            println("  Layer #$i [$typeStr] isCap=$isCap: $layer")
        }

        assertTrue(doc.canvas.capLayerIndices.isNotEmpty(), "Thumbstick must have movable cap layers")
        assertFalse(0 in doc.canvas.capLayerIndices, "Base layer 0 must remain stationary")
        assertTrue(doc.canvas.capLayerIndices.size >= 2, "Thumb cap must include cap container and children")
    }

    @Test
    fun verifyPresetThumbstickRsTwoStagePartitioning() {
        val doc = com.sanket.tools.nexpad.nxprc.NxprcPackager.compile(
            NxprcHtmlCssConverter.PRESET_THUMBSTICK_RS,
            "rc.stick_rs",
            "Analog Stick RS",
            "JOYSTICK",
            "RS"
        )
        println("=== PRESET_THUMBSTICK_RS COMPILATION AUDIT ===")
        println("Category: ${doc.manifest.category}, Control: ${doc.manifest.defaultControl}")
        println("Dimensions: ${doc.manifest.widthDp}x${doc.manifest.heightDp}")
        println("Cap Layer Indices: ${doc.canvas.capLayerIndices}")

        assertTrue(doc.canvas.capLayerIndices.isNotEmpty(), "RS thumbstick must have movable cap layers")
        assertFalse(0 in doc.canvas.capLayerIndices, "RS base layer 0 must remain stationary")
        assertTrue(doc.canvas.capLayerIndices.size >= 2, "RS thumb cap must include cap container and children")
    }

    @Test
    fun verifyTwoZoneDomThumbstickCompilation() {
        val html = """
            <!DOCTYPE html>
            <html><head><style>
              .stick-btn { width: 130px; height: 130px; position: relative; background: transparent; }
              .stick-base { position: absolute; width: 130px; height: 130px; border-radius: 50%; background: #111; }
              .socket-well { position: absolute; width: 110px; height: 110px; border-radius: 50%; background: #050505; }
              .stick-cap { position: absolute; width: 78px; height: 78px; border-radius: 50%; background: #222; }
              .knurled-ring { position: absolute; width: 56px; height: 56px; border-radius: 50%; border: 2px dashed #444; }
              .stick-label { font-size: 16px; color: #fff; }
            </style></head>
            <body>
              <button class="stick-btn" data-control="LS" data-category="JOYSTICK" data-name="Two Zone Stick">
                <div class="stick-base">
                  <div class="socket-well"></div>
                </div>
                <div class="stick-cap">
                  <div class="knurled-ring"></div>
                  <span class="stick-label">L3</span>
                </div>
              </button>
            </body></html>
        """.trimIndent()

        val doc = com.sanket.tools.nexpad.nxprc.NxprcPackager.compile(
            html, "rc.two_zone_stick", "Two Zone Stick", "JOYSTICK", "LS"
        )
        println("=== TWO ZONE DOM STICK AUDIT ===")
        println("Cap Layer Indices: ${doc.canvas.capLayerIndices}")
        doc.canvas.layers.forEachIndexed { i, layer ->
            val isCap = i in doc.canvas.capLayerIndices
            println("  Layer #$i [${layer::class.simpleName}] isCap=$isCap: $layer")
        }

        // Must have at least 2 cap layers (stick-cap container, knurled-ring, and text)
        assertTrue(doc.canvas.capLayerIndices.size >= 2, "Thumb cap must contain multiple movable layers")
        // Base elements (index 0, 1) must be false
        assertFalse(0 in doc.canvas.capLayerIndices, "Base must not be in capLayerIndices")
    }
}


