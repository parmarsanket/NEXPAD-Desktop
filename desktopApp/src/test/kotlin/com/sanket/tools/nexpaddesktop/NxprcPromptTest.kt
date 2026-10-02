package com.sanket.tools.nexpaddesktop

import com.sanket.tools.nexpaddesktop.plugins.AbxyPromptStrategy
import com.sanket.tools.nexpaddesktop.plugins.AiDesignOptions
import com.sanket.tools.nexpaddesktop.plugins.BumperPromptStrategy
import com.sanket.tools.nexpaddesktop.plugins.ColorProfile
import com.sanket.tools.nexpaddesktop.plugins.Complexity
import com.sanket.tools.nexpaddesktop.plugins.ComplexityBudget
import com.sanket.tools.nexpaddesktop.plugins.ComponentPromptRegistry
import com.sanket.tools.nexpaddesktop.plugins.Creativity
import com.sanket.tools.nexpaddesktop.plugins.DpadPromptStrategy
import com.sanket.tools.nexpaddesktop.plugins.Fidelity
import com.sanket.tools.nexpaddesktop.plugins.GeometryOptions
import com.sanket.tools.nexpaddesktop.plugins.ModelCapability
import com.sanket.tools.nexpaddesktop.plugins.NxprcAiPromptBuilder
import com.sanket.tools.nexpaddesktop.plugins.NxprcHtmlCssConverter
import com.sanket.tools.nexpaddesktop.plugins.SpringPhysics
import com.sanket.tools.nexpaddesktop.plugins.CategoryDefaultsRegistry
import com.sanket.tools.nexpaddesktop.plugins.DesignResolver
import com.sanket.tools.nexpaddesktop.plugins.DesignSource
import com.sanket.tools.nexpaddesktop.plugins.StickButtonPromptStrategy
import com.sanket.tools.nexpaddesktop.plugins.StickPromptStrategy
import com.sanket.tools.nexpaddesktop.plugins.SystemPromptStrategy
import com.sanket.tools.nexpaddesktop.plugins.TouchpadPromptStrategy
import com.sanket.tools.nexpaddesktop.plugins.TriggerPromptStrategy
import com.sanket.tools.nexpaddesktop.plugins.VisualDensity
import kotlin.test.Test
import kotlin.test.assertEquals
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
            assertFalse(prompt.contains("OPTIONAL STARTER TEMPLATE"), "Starter template should be omitted by default")
            val promptWithSkeleton = NxprcHtmlCssConverter.generateAiPrompt(
                control = if (category == "DPAD") "UP" else "A",
                category = category,
                widthDp = 96,
                heightDp = 96,
                options = AiDesignOptions(includeSyntaxSkeleton = true)
            )
            assertTrue(promptWithSkeleton.contains("OPTIONAL STARTER TEMPLATE"))
            assertTrue(promptWithSkeleton.contains("REFERENCE ONLY"))
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
            assertFalse(prompt.contains("OPTIONAL STARTER TEMPLATE"), "$tag should omit starter template by default")
            assertFalse(prompt.contains("REFERENCE ONLY"), "$tag should omit REFERENCE ONLY by default")

            // Template included when explicitly requested
            val promptWithSkeleton = NxprcHtmlCssConverter.generateAiPrompt(
                control = control,
                category = category,
                widthDp = 96,
                heightDp = 96,
                options = AiDesignOptions(includeSyntaxSkeleton = true)
            )
            assertTrue(promptWithSkeleton.contains("OPTIONAL STARTER TEMPLATE"), "$tag missing starter template when requested")
            assertTrue(promptWithSkeleton.contains("REFERENCE ONLY"), "$tag missing REFERENCE ONLY label when requested")
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

            // 8. Syntax-Only Starter Template Anti-Copy Protection (when requested)
            val promptWithSkeleton = NxprcHtmlCssConverter.generateAiPrompt(
                control = control,
                category = category,
                widthDp = 96,
                heightDp = 96,
                options = AiDesignOptions(includeSyntaxSkeleton = true)
            )
            assertTrue(promptWithSkeleton.contains("This template demonstrates document syntax only"), "$tag missing anti-copy template warning")
        }
    }

    @Test
    fun joystickPromptExplicitlyInformsTwoZonePhysicalMechanism() {
        val lsPrompt = NxprcHtmlCssConverter.generateAiPrompt("LS", "JOYSTICK", 130, 130)
        val rsPrompt = NxprcHtmlCssConverter.generateAiPrompt("RS", "JOYSTICK", 130, 130)

        listOf(lsPrompt to "LS", rsPrompt to "RS").forEach { (prompt, key) ->
            assertTrue(prompt.contains("JOYSTICK TWO-ZONE PHYSICAL MECHANISM:"), "Missing two-zone physical mechanism header")
            assertTrue(prompt.contains("Stationary Gimbal Base"), "Missing stationary gimbal base description")
            assertTrue(prompt.contains("Movable Analog Thumb Cap"), "Missing movable analog thumb cap description")
            assertTrue(prompt.contains("360° Analog Deflection"), "Missing 360 analog deflection instruction")
            assertTrue(prompt.contains("Continuous Analog Navigation (NO Center Button)"), "Missing NO center button instruction for $key")
            assertTrue(prompt.contains("<span class=\"stick-label\">$key</span>"), "Cap must be labeled with $key")
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

    @Test
    fun verifyUserAnimeStickLayers() {
        val html = """
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<style>
  :root {
    --spring-damping: 0.68;
    --spring-stiffness: 440;
    --press-scale: 0.92;
    --accent-pink: #ff5cc8;
    --accent-purple: #9b5cff;
    --accent-cyan: #5ce1ff;
    --accent-yellow: #ffe45c;
  }

  .stick-btn {
    width: 130px;
    height: 130px;
    position: relative;
    background: transparent;
    border: none;
    padding: 0;
    outline: none;
  }

  /* ---- Stationary Gimbal Base ---- */
  .stick-base {
    position: absolute;
    left: 0;
    top: 0;
    width: 100%;
    height: 100%;
    border-radius: 50%;
    background: radial-gradient(circle at 40% 32%, #4a2f7a 0%, #2a1a4a 55%, #170f2b 100%);
    box-shadow:
      0 12px 26px rgba(0,0,0,0.55),
      inset 0 3px 8px rgba(255,255,255,0.18),
      inset 0 -10px 18px rgba(0,0,0,0.75),
      0 0 22px rgba(155, 92, 255, 0.45);
  }

  /* rainbow rim ring painted with conic-gradient */
  .rim-ring {
    position: absolute;
    left: 4px;
    top: 4px;
    width: 122px;
    height: 122px;
    border-radius: 50%;
    background: conic-gradient(
      var(--accent-pink) 0deg,
      var(--accent-purple) 90deg,
      var(--accent-cyan) 180deg,
      var(--accent-yellow) 270deg,
      var(--accent-pink) 360deg
    );
    -webkit-mask: radial-gradient(circle, transparent 55px, #000 57px, #000 61px, transparent 63px);
    mask: radial-gradient(circle, transparent 55px, #000 57px, #000 61px, transparent 63px);
    opacity: 0.9;
  }

  /* directional tick marks on the base */
  .tick {
    position: absolute;
    width: 4px;
    height: 10px;
    border-radius: 2px;
    background: rgba(255,255,255,0.55);
  }
  .tick-n { left: 63px; top: 8px; }
  .tick-e { left: 112px; top: 61px; transform: rotate(90deg); }
  .tick-s { left: 63px; top: 112px; }
  .tick-w { left: 8px; top: 61px; transform: rotate(90deg); }

  /* sparkle accents (anime flair), real SVG, purely decorative, stationary on base */
  .sparkle {
    position: absolute;
    width: 14px;
    height: 14px;
  }
  .sparkle-a { left: 12px; top: 18px; }
  .sparkle-b { left: 100px; top: 92px; width: 10px; height: 10px; }

  /* ---- Movable Analog Thumb Cap ---- */
  .stick-cap {
    position: absolute;
    left: 23px;
    top: 23px;
    width: 84px;
    height: 84px;
    border-radius: 50%;
    background: radial-gradient(circle at 38% 30%, #ff8ee0 0%, #ff5cc8 30%, #b13fef 65%, #6a2bd9 100%);
    box-shadow:
      inset 0 0 10px rgba(0,0,0,0.35),
      inset 0 4px 8px rgba(255,255,255,0.45),
      0 0 0 3px rgba(255,255,255,0.25),
      0 0 16px rgba(255, 92, 200, 0.6);
    display: flex;
    align-items: center;
    justify-content: center;
  }

  /* glossy highlight blob for anime "shiny plastic" look */
  .cap-shine {
    position: absolute;
    left: 14px;
    top: 10px;
    width: 30px;
    height: 18px;
    border-radius: 50%;
    background: radial-gradient(circle, rgba(255,255,255,0.85) 0%, rgba(255,255,255,0) 70%);
  }

  /* concentric knurled traction ring inside cap */
  .knurled-ring {
    position: absolute;
    width: 57px;
    height: 57px;
    border-radius: 50%;
    border: 2px dashed rgba(255, 255, 255, 0.65);
  }
  .knurled-ring-inner {
    position: absolute;
    width: 40px;
    height: 40px;
    border-radius: 50%;
    border: 2px dashed rgba(255, 255, 255, 0.4);
  }

  .stick-label {
    position: relative;
    z-index: 5;
    font-family: -apple-system, "Segoe UI", sans-serif;
    font-size: 20px;
    font-weight: 900;
    color: #ffffff;
    text-shadow: 0 0 6px rgba(155,92,255,0.9), 0 2px 2px rgba(0,0,0,0.35);
  }

  .stick-btn:active .stick-cap {
    transform: scale(0.92);
  }
</style>
</head>
<body>
  <button class="stick-btn" data-control="LS" data-category="JOYSTICK" data-name="Analog Stick LS">
    <div class="stick-base">
      <div class="rim-ring"></div>
      <div class="tick tick-n"></div>
      <div class="tick tick-e"></div>
      <div class="tick tick-s"></div>
      <div class="tick tick-w"></div>
      <svg class="sparkle sparkle-a" viewBox="0 0 24 24">
        <path d="M12 0 L14 10 L24 12 L14 14 L12 24 L10 14 L0 12 L10 10 Z" fill="#ffe45c"/>
      </svg>
      <svg class="sparkle sparkle-b" viewBox="0 0 24 24">
        <path d="M12 0 L14 10 L24 12 L14 14 L12 24 L10 14 L0 12 L10 10 Z" fill="#5ce1ff"/>
      </svg>
    </div>
    <div class="stick-cap">
      <div class="cap-shine"></div>
      <div class="knurled-ring"></div>
      <div class="knurled-ring-inner"></div>
      <span class="stick-label">L3</span>
    </div>
  </button>
</body>
</html>
        """.trimIndent()

        val doc = com.sanket.tools.nexpad.nxprc.NxprcPackager.compile(
            html, "rc.anime_stick", "Anime Stick", "JOYSTICK", "LS"
        )
        println("=== USER ANIME STICK AUDIT ===")
        println("Cap Layer Indices: ${doc.canvas.capLayerIndices}")
        doc.canvas.layers.forEachIndexed { i, layer ->
            val isCap = i in doc.canvas.capLayerIndices
            println("  Layer #$i [${layer::class.simpleName}] isCap=$isCap: $layer")
        }

        // Base tick marks (layers 4, 5, 6, 7) must remain 100% stationary on base!
        assertFalse(4 in doc.canvas.capLayerIndices, "Layer #4 (.tick-n) must be stationary on base")
        assertFalse(5 in doc.canvas.capLayerIndices, "Layer #5 (.tick-e) must be stationary on base")
        assertFalse(6 in doc.canvas.capLayerIndices, "Layer #6 (.tick-s) must be stationary on base")
        assertFalse(7 in doc.canvas.capLayerIndices, "Layer #7 (.tick-w) must be stationary on base")

        // Sparkles (layers 11, 12) must remain stationary on base
        assertFalse(11 in doc.canvas.capLayerIndices, "Layer #11 (.sparkle-a) must be stationary on base")
        assertFalse(12 in doc.canvas.capLayerIndices, "Layer #12 (.sparkle-b) must be stationary on base")

        // Thumb cap elements (layers 2, 8, 9, 10, 13) must be in capLayerIndices
        assertTrue(2 in doc.canvas.capLayerIndices, "Layer #2 (.stick-cap) must move")
        assertTrue(8 in doc.canvas.capLayerIndices, "Layer #8 (.cap-shine) must move")
        assertTrue(9 in doc.canvas.capLayerIndices, "Layer #9 (.knurled-ring) must move")
        assertTrue(10 in doc.canvas.capLayerIndices, "Layer #10 (.knurled-ring-inner) must move")
        assertTrue(13 in doc.canvas.capLayerIndices, "Layer #13 (CenterGlyph) must move")
    }

    @Test
    fun allCategoriesContainConsoleIndustrialRealismAndVisualQaChecklist() {
        val categories = listOf(
            "BUTTON" to "A",
            "DPAD" to "UP",
            "TRIGGER" to "LT",
            "BUMPER" to "LB",
            "JOYSTICK" to "LS",
            "SYSTEM" to "MENU"
        )

        categories.forEach { (category, control) ->
            val prompt = NxprcHtmlCssConverter.generateAiPrompt(
                control = control,
                category = category,
                widthDp = 96,
                heightDp = 96
            )
            val tag = "[$category/$control]"

            assertTrue(
                prompt.contains("VISUAL TARGET — CONSOLE/XBOX INDUSTRIAL REALISM"),
                "$tag missing VISUAL TARGET — CONSOLE/XBOX INDUSTRIAL REALISM"
            )
            assertTrue(
                prompt.contains("VISUAL QA CHECKLIST (SELF-CHECK BEFORE OUTPUT)"),
                "$tag missing VISUAL QA CHECKLIST"
            )
            assertTrue(
                prompt.contains("Compiler Safety"),
                "$tag missing Compiler Safety checklist item"
            )
        }
    }

    @Test
    fun systemMenuCompilesWithoutUnwantedCenterGlyphOverlay() {
        val doc = com.sanket.tools.nexpad.nxprc.NxprcPackager.compile(
            NxprcHtmlCssConverter.PRESET_SYSTEM_MENU,
            "rc.menu",
            "System Menu",
            "SYSTEM",
            "MENU"
        )

        val glyphs = doc.canvas.layers.filterIsInstance<com.sanket.tools.nexpad.nxprc.CanvasLayer.CenterGlyph>()
        val textLayers = doc.canvas.layers.filterIsInstance<com.sanket.tools.nexpad.nxprc.CanvasLayer.TextLayer>()
        val boxLayers = doc.canvas.layers.filterIsInstance<com.sanket.tools.nexpad.nxprc.CanvasLayer.BoxLayer>()

        // Must NOT stamp an automatic "MENU" glyph over the hamburger bars
        assertTrue(
            glyphs.none { it.text.equals("MENU", ignoreCase = true) },
            "System Menu should NOT have an automatic CenterGlyph MENU stamped over burger bars"
        )
        assertTrue(
            textLayers.none { it.text.equals("MENU", ignoreCase = true) },
            "System Menu should NOT have an automatic TextLayer MENU stamped over burger bars"
        )

        // Must contain at least 3 distinct hamburger bar layers
        val bars = boxLayers.filter { it.heightRatio in 0.04f..0.15f }
        assertTrue(bars.size >= 3, "System Menu must contain 3 distinct burger bars, found: ${bars.size}")
    }

    @Test
    fun allPresetCategoriesCompileCleanly() {
        val presets = listOf(
            Triple("DPAD", "UP", NxprcHtmlCssConverter.PRESET_DPAD_UP),
            Triple("DPAD", "DOWN", NxprcHtmlCssConverter.PRESET_DPAD_DOWN),
            Triple("DPAD", "LEFT", NxprcHtmlCssConverter.PRESET_DPAD_LEFT),
            Triple("DPAD", "RIGHT", NxprcHtmlCssConverter.PRESET_DPAD_RIGHT),
            Triple("DPAD", "DPAD", NxprcHtmlCssConverter.PRESET_DPAD_CROSS),
            Triple("TRIGGER", "LT", NxprcHtmlCssConverter.PRESET_TRIGGER_LT),
            Triple("TRIGGER", "RT", NxprcHtmlCssConverter.PRESET_TRIGGER_RT),
            Triple("BUMPER", "LB", NxprcHtmlCssConverter.PRESET_BUMPER_LB),
            Triple("BUMPER", "RB", NxprcHtmlCssConverter.PRESET_BUMPER_RB),
            Triple("SYSTEM", "VIEW", NxprcHtmlCssConverter.PRESET_SYSTEM_VIEW),
            Triple("SYSTEM", "HOME", NxprcHtmlCssConverter.PRESET_SYSTEM_HOME)
        )

        presets.forEach { (cat, ctrl, html) ->
            val doc = com.sanket.tools.nexpad.nxprc.NxprcPackager.compile(
                html, "rc.${ctrl.lowercase()}", "$cat $ctrl", cat, ctrl
            )
            assertTrue(doc.canvas.layers.isNotEmpty(), "Preset [$cat/$ctrl] must produce canvas layers")
            assertTrue(doc.canvas.viewBoxWidth > 0, "Preset [$cat/$ctrl] viewBoxWidth must be > 0")
            assertTrue(doc.canvas.viewBoxHeight > 0, "Preset [$cat/$ctrl] viewBoxHeight must be > 0")
        }
    }

    @Test
    fun promptsUseNonBindingSyntaxSkeleton() {
        val categories = listOf(
            "BUTTON" to "A",
            "DPAD" to "UP",
            "TRIGGER" to "RT",
            "BUMPER" to "RB",
            "JOYSTICK" to "LS",
            "SYSTEM" to "MENU"
        )

        categories.forEach { (category, control) ->
            val prompt = NxprcHtmlCssConverter.generateAiPrompt(
                control = control,
                category = category,
                widthDp = 96,
                heightDp = 96,
                options = AiDesignOptions(includeSyntaxSkeleton = true)
            )
            val tag = "[$category/$control]"

            assertTrue(prompt.contains("NON-BINDING SYNTAX REFERENCE"), "$tag missing NON-BINDING SYNTAX REFERENCE label")
            assertTrue(prompt.contains("OPTIONAL STARTER TEMPLATE"), "$tag missing starter template heading")
            assertTrue(prompt.contains("REFERENCE ONLY"), "$tag missing REFERENCE ONLY heading label")

            // The template inside the prompt must be a clean syntax skeleton, not a pre-baked aesthetic design
            val templateSection = prompt.substringAfter("### OPTIONAL STARTER TEMPLATE")
            assertFalse(templateSection.contains("radial-gradient(circle at 40% 32%"), "$tag template leaked preset colors")
            assertFalse(templateSection.contains("linear-gradient(145deg"), "$tag template leaked preset colors")
        }
    }

    @Test
    fun bumperPromptEnforcesShoulderRockerSemanticsWithoutPrescribedAspectRatio() {
        val prompt = NxprcHtmlCssConverter.generateAiPrompt("LB", "BUMPER", 120, 50)

        assertTrue(prompt.contains("Physical Shoulder Lever/Rocker Architecture"), "Missing shoulder lever/rocker architecture")
        assertTrue(prompt.contains("chassis housing socket or seam"), "Missing chassis seam/socket specification")
        assertTrue(prompt.contains("Shallow tactile shoulder lever/rocker actuation"), "Missing shoulder lever semantics")

        // De-biased: Must NOT enforce aspect ratio ~2:1 to 2.5:1 or fixed border-radius: 18px
        assertFalse(prompt.contains("aspect ratio ~2:1 to 2.5:1"), "Prescriptive aspect ratio leaked into bumper prompt")
        assertFalse(prompt.contains("border-radius: 18px"), "Prescriptive border-radius leaked into bumper prompt")
    }

    @Test
    fun triggerPromptAllowsFreeformGeometryWithoutFixedBorderRadius() {
        val prompt = NxprcHtmlCssConverter.generateAiPrompt("RT", "TRIGGER", 110, 140)

        assertTrue(prompt.contains("Progressive Analog Travel Mechanics"), "Missing progressive analog travel mechanics")
        assertTrue(prompt.contains("Analog Travel Affordance"), "Missing analog travel affordance in QA checklist")

        // De-biased: Must NOT mandate border-radius: 20px or mandate elongated vertical paddle in QA checklist
        assertFalse(prompt.contains("border-radius: 20px"), "Prescriptive border-radius leaked into trigger prompt")
        assertFalse(prompt.contains("Elongated vertical paddle silhouette"), "Prescriptive elongated paddle leaked into QA checklist")
        assertFalse(prompt.contains("PULL"), "Prescriptive PULL sub-label leaked into trigger prompt")
        assertFalse(prompt.contains("BRAKE"), "Prescriptive BRAKE sub-label leaked into trigger prompt")
    }

    @Test
    fun allCategoriesExposeEmbeddedSvgVectorEmblemArchitecture() {
        val categories = listOf(
            "BUTTON" to "A",
            "DPAD" to "UP",
            "TRIGGER" to "RT",
            "BUMPER" to "RB",
            "JOYSTICK" to "LS",
            "SYSTEM" to "MENU"
        )

        categories.forEach { (category, control) ->
            val prompt = NxprcHtmlCssConverter.generateAiPrompt(
                control = control,
                category = category,
                widthDp = 96,
                heightDp = 96
            )
            val tag = "[$category/$control]"

            assertTrue(
                prompt.contains("ARCHITECTURAL PATTERN: HTML/CSS BUTTON SHELL + EMBEDDED SVG VECTOR EMBLEM"),
                "$tag missing embedded SVG vector emblem architectural pattern header"
            )
            assertTrue(
                prompt.contains("FORBIDDEN ANTI-PATTERN (NEVER DO THIS)"),
                "$tag missing forbidden anti-pattern warning"
            )
            assertTrue(
                prompt.contains("MANDATORY DUAL-ENGINE ARCHITECTURE (ALWAYS DO THIS)"),
                "$tag missing mandatory dual-engine architecture instruction"
            )
            assertTrue(
                prompt.contains("The Outer HTML/CSS Button Shell"),
                "$tag missing outer button shell instruction"
            )
            assertTrue(
                prompt.contains("The Embedded `<svg class=\"button-emblem\" viewBox=\"0 0 100 100\">` Vector Emblem"),
                "$tag missing embedded SVG vector emblem instruction"
            )
            assertTrue(
                prompt.contains("The High-Contrast Control Typography"),
                "$tag missing high-contrast control typography instruction"
            )
            assertTrue(
                prompt.contains("EMBLEM / GRAPHIC (OPTIONAL)"),
                "$tag missing EMBLEM / GRAPHIC in customization schema"
            )
            assertTrue(
                prompt.contains("Complex Graphics Architecture"),
                "$tag missing Complex Graphics Architecture in visual QA checklist"
            )
        }
    }

    @Test
    fun stickButtonPromptInformsTactileClickAndSingleButtonContract() {
        val lsbPrompt = NxprcHtmlCssConverter.generateAiPrompt("LSB", "STICKS", 80, 80)
        val rsbPrompt = NxprcHtmlCssConverter.generateAiPrompt("RSB", "STICKS", 80, 80)

        listOf(lsbPrompt to "LSB", rsbPrompt to "RSB").forEach { (prompt, key) ->
            assertTrue(prompt.contains("Stick Click Button Component"), "Missing stick click button header")
            assertTrue(prompt.contains("Single-Button Tactile Architecture"), "Missing single-button architecture section")
            assertTrue(prompt.contains("Textured Thumbstick Cap Dish"), "Missing textured thumbstick cap dish section")
            assertTrue(prompt.contains("data-category=\"BUTTON\""), "Missing BUTTON category attribute in prompt")
            assertTrue(prompt.contains("<button class=\"stick-btn-ctl\""), "Missing stick-btn-ctl class in prompt")
            assertTrue(prompt.contains("--spring-damping"), "Missing spring damping")
            assertTrue(prompt.contains("--spring-stiffness"), "Missing spring stiffness")
            assertTrue(prompt.contains("No Two-Zone Analog Split"), "Missing no two-zone split rule in QA checklist")
        }

        // Also test template resolution for LSB and RSB
        val lsbTmpl = NxprcHtmlCssConverter.getReferenceTemplate("LSB")
        assertTrue(lsbTmpl.contains("data-control=\"LSB\""))
        assertTrue(lsbTmpl.contains("data-category=\"BUTTON\""))

        val rsbTmpl = NxprcHtmlCssConverter.getReferenceTemplate("RSB")
        assertTrue(rsbTmpl.contains("data-control=\"RSB\""))
        assertTrue(rsbTmpl.contains("data-category=\"BUTTON\""))

        // Test syntax skeleton for LSB
        val skeleton = NxprcHtmlCssConverter.getSyntaxSkeleton("LSB", "STICKS", 80, 80)
        assertTrue(skeleton.contains("class=\"stick-btn-ctl\""))
        assertTrue(skeleton.contains("data-control=\"LSB\""))
    }

    @Test
    fun touchpadPromptInformsTrackpadAndDynamicCenterContract() {
        val ltpPrompt = NxprcHtmlCssConverter.generateAiPrompt("LTP", "STICKS", 180, 180)
        val rtpPrompt = NxprcHtmlCssConverter.generateAiPrompt("RTP", "STICKS", 180, 180)

        assertTrue(ltpPrompt.contains("Left Touch Movement Pad"), "Missing Left Touch Movement Pad in LTP prompt")
        assertTrue(ltpPrompt.contains("data-category=\"STICKS\"") || ltpPrompt.contains("data-category=\"TOUCHPAD\""), "Missing category attribute in LTP prompt")
        assertTrue(ltpPrompt.contains("<button class=\"touchpad-ctl\""), "Missing touchpad-ctl root button in LTP prompt")
        assertTrue(ltpPrompt.contains("NO center button"), "Missing NO center button in LTP prompt")
        assertTrue(ltpPrompt.contains("NO movable ring"), "Missing NO movable ring in LTP prompt")
        assertTrue(ltpPrompt.contains("NO tap-to-click mechanism"), "Missing NO tap-to-click mechanism in LTP prompt")
        assertTrue(ltpPrompt.contains("2.0X BALLISTICS"), "Missing 2.0X BALLISTICS in LTP prompt")
        assertFalse(ltpPrompt.contains("TAP: L3"), "LTP prompt must NOT contain TAP: L3")

        assertTrue(rtpPrompt.contains("Right Touch Camera Look Pad"), "Missing Right Touch Camera Look Pad in RTP prompt")
        assertTrue(rtpPrompt.contains("Free-look camera panning"), "Missing Free-look camera panning description in RTP prompt")
        assertTrue(rtpPrompt.contains("NO center button"), "Missing NO center button in RTP prompt")
        assertTrue(rtpPrompt.contains("NO movable ring"), "Missing NO movable ring in RTP prompt")
        assertTrue(rtpPrompt.contains("NO tap-to-click mechanism"), "Missing NO tap-to-click mechanism in RTP prompt")
        assertTrue(rtpPrompt.contains("2.0X BALLISTICS"), "Missing 2.0X BALLISTICS in RTP prompt")
        assertFalse(rtpPrompt.contains("TAP: R3"), "RTP prompt must NOT contain TAP: R3")

        // Test template resolution for LTP and RTP
        val ltpTmpl = NxprcHtmlCssConverter.getReferenceTemplate("LTP")
        assertTrue(ltpTmpl.contains("data-control=\"LTP\""))
        assertTrue(ltpTmpl.contains("data-category=\"TOUCHPAD\""))
        assertTrue(ltpTmpl.contains("Touch Move • LTP"))
        assertTrue(ltpTmpl.contains("2.0X BALLISTICS"))
        assertFalse(ltpTmpl.contains("touchpad-ring"), "LTP preset must NOT contain touchpad-ring")
        assertFalse(ltpTmpl.contains("touchpad-center-dot"), "LTP preset must NOT contain touchpad-center-dot")
        assertFalse(ltpTmpl.contains("TAP: L3"))

        val rtpTmpl = NxprcHtmlCssConverter.getReferenceTemplate("RTP")
        assertTrue(rtpTmpl.contains("data-control=\"RTP\""))
        assertTrue(rtpTmpl.contains("data-category=\"TOUCHPAD\""))
        assertTrue(rtpTmpl.contains("Touch Look • RTP"))
        assertTrue(rtpTmpl.contains("2.0X BALLISTICS"))
        assertFalse(rtpTmpl.contains("touchpad-ring"), "RTP preset must NOT contain touchpad-ring")
        assertFalse(rtpTmpl.contains("touchpad-center-dot"), "RTP preset must NOT contain touchpad-center-dot")
        assertFalse(rtpTmpl.contains("TAP: R3"))

        // Verify compiler compiles touchpads with empty capLayerIndices
        val ltpDoc = com.sanket.tools.nexpad.nxprc.engine.compiler.NxprcCompiler.compile(
            ltpTmpl, "rc.ltp", "Touchpad LTP", "TOUCHPAD", "LTP"
        )
        val rtpDoc = com.sanket.tools.nexpad.nxprc.engine.compiler.NxprcCompiler.compile(
            rtpTmpl, "rc.rtp", "Touchpad RTP", "TOUCHPAD", "RTP"
        )
        assertTrue(ltpDoc.canvas.capLayerIndices.isEmpty(), "LTP capLayerIndices must be empty for stationary touchpad")
        assertTrue(rtpDoc.canvas.capLayerIndices.isEmpty(), "RTP capLayerIndices must be empty for stationary touchpad")

        // Test syntax skeleton for LTP and RTP
        val skeleton = NxprcHtmlCssConverter.getSyntaxSkeleton("LTP", "STICKS", 180, 180)
        assertTrue(skeleton.contains("class=\"touchpad-ctl\""))
        assertTrue(skeleton.contains("data-control=\"LTP\""))
    }

    @Test
    fun verifyNoCenterButtonOnJoysticksAndTouchpads() {
        val lsTmpl = NxprcHtmlCssConverter.getReferenceTemplate("LS", "JOYSTICK")
        val rsTmpl = NxprcHtmlCssConverter.getReferenceTemplate("RS", "JOYSTICK")
        assertTrue(lsTmpl.contains("<span class=\"stick-label\">LS</span>"), "LS cap must be labeled LS")
        assertFalse(lsTmpl.contains("<span class=\"stick-label\">L3</span>"), "LS cap must NOT be labeled L3")
        assertTrue(rsTmpl.contains("<span class=\"stick-label\">RS</span>"), "RS cap must be labeled RS")
        assertFalse(rsTmpl.contains("<span class=\"stick-label\">R3</span>"), "RS cap must NOT be labeled R3")

        val ltpTmpl = NxprcHtmlCssConverter.getReferenceTemplate("LTP")
        val rtpTmpl = NxprcHtmlCssConverter.getReferenceTemplate("RTP")
        assertFalse(ltpTmpl.contains("TAP: L3"), "LTP preset must NOT contain TAP: L3")
        assertFalse(rtpTmpl.contains("TAP: R3"), "RTP preset must NOT contain TAP: R3")
        assertTrue(ltpTmpl.contains("2.0X BALLISTICS"), "LTP preset must denote 2.0x ballistics")
        assertTrue(rtpTmpl.contains("2.0X BALLISTICS"), "RTP preset must denote 2.0x ballistics")
    }

    @Test
    fun allCategoriesEnforceCalcAndAspectRatioCapabilities() {
        val categories = listOf(
            "BUTTON" to "A",
            "DPAD" to "UP",
            "TRIGGER" to "RT",
            "BUMPER" to "RB",
            "JOYSTICK" to "LS",
            "SYSTEM" to "MENU"
        )

        categories.forEach { (category, control) ->
            val prompt = NxprcHtmlCssConverter.generateAiPrompt(
                control = control,
                category = category,
                widthDp = 96,
                heightDp = 96
            )
            val tag = "[$category/$control]"

            assertTrue(prompt.contains("calc()"), "$tag missing calc() capability instruction")
            assertTrue(prompt.contains("aspect-ratio"), "$tag missing aspect-ratio capability instruction")
        }
    }

    @Test
    fun allCategoriesEnforceUnrotatedTextAndCssVectorEmblemGlow() {
        val categories = listOf(
            "BUTTON" to "A",
            "DPAD" to "UP",
            "TRIGGER" to "RT",
            "BUMPER" to "RB",
            "JOYSTICK" to "LS",
            "SYSTEM" to "MENU"
        )

        categories.forEach { (category, control) ->
            val prompt = NxprcHtmlCssConverter.generateAiPrompt(
                control = control,
                category = category,
                widthDp = 96,
                heightDp = 96
            )
            val tag = "[$category/$control]"

            // Rule 1: Vector emblem glow via underlying CSS span, not SVG filter graphs
            assertTrue(
                prompt.contains("Vector Emblem Glow Rule"),
                "$tag missing Vector Emblem Glow Rule"
            )
            assertTrue(
                prompt.contains("Do NOT rely on SVG `<filter>` graphs"),
                "$tag missing ban on SVG filter graphs for glow"
            )
            assertTrue(
                prompt.contains("filter: blur("),
                "$tag missing CSS blur instruction for emblem glow"
            )

            // Rule 2: Unrotated DOM text
            assertTrue(
                prompt.contains("NO TEXT ROTATION") || prompt.contains("without rotation"),
                "$tag missing NO TEXT ROTATION guidance"
            )
            assertTrue(
                prompt.contains("unrotated"),
                "$tag missing unrotated text instruction"
            )

            // Anti-regression: Ensure no prompt erroneously claims full support for SVG filter graphs
            assertFalse(
                prompt.contains("Full support for vector iconography, paths, and SVG `<filter>` graphs"),
                "$tag must NOT claim full support for SVG filter graphs"
            )
        }
    }

    @Test
    fun parameterDrivenPromptCustomization() {
        val options = AiDesignOptions(
            creativity = Creativity.HIGH,
            complexity = Complexity.DETAILED,
            fidelity = Fidelity.FAITHFUL,
            visualDensity = VisualDensity.BALANCED,
            style = "Cyberpunk Neo",
            color = "Electric Cyan",
            shape = "Hexagonal Diamond",
            material = "Brushed Titanium",
            lighting = "Backlit Neon",
            texture = "Carbon Fiber",
            emblem = "Samurai Oni Mask",
            label = "TURBO",
            tactilePhysics = "Ultra-snappy microswitch with high resistance",
            specialInstructions = "Add pulsating energy rings",
            userRequest = "Make it look like Cyberpunk 2077"
        )

        val prompt = NxprcHtmlCssConverter.generateAiPrompt("A", "BUTTON", 96, 96, options)

        assertTrue(prompt.contains("**STYLE**: Cyberpunk Neo"))
        assertTrue(prompt.contains("**COLOR / PALETTE**: Electric Cyan"))
        assertTrue(prompt.contains("**SHAPE / SILHOUETTE**: Hexagonal Diamond"))
        assertTrue(prompt.contains("**MATERIAL / SURFACE**: Brushed Titanium"))
        assertTrue(prompt.contains("**LIGHTING / SHADING**: Backlit Neon"))
        assertTrue(prompt.contains("**TEXTURE / PATTERN**: Carbon Fiber"))
        assertTrue(prompt.contains("**EMBLEM / ICONOGRAPHY**: Samurai Oni Mask"))
        assertTrue(prompt.contains("**LABEL TEXT**: TURBO"))
        assertTrue(prompt.contains("**TACTILE PHYSICS**: Ultra-snappy microswitch with high resistance"))
        assertTrue(prompt.contains("**SPECIAL INSTRUCTIONS**: Add pulsating energy rings"))
        assertTrue(prompt.contains("<user_request>"))
        assertTrue(prompt.contains("Make it look like Cyberpunk 2077"))
        assertTrue(prompt.contains("High (bold reinterpretation, unusual geometry, materials, and visual treatment while preserving the user's concept)"))
        assertTrue(prompt.contains("Faithful (preserve recognizable motifs and visual relationships)"))
        assertTrue(prompt.contains("Detailed (use multiple meaningful layers, material transitions, secondary detailing, and moderately complex SVG geometry)"))
        assertTrue(prompt.contains("Balanced (moderate secondary detail while preserving readability)"))
    }

    @Test
    fun generateRepairPromptContract() {
        val previousCode = "<button class=\"nexpad-btn\" style=\"mix-blend-mode: multiply;\"><span>A</span></button>"
        val repairPrompt = NxprcHtmlCssConverter.generateRepairPrompt(
            previousHtml = previousCode,
            warnings = listOf("Approximating filter: blur(4px)"),
            errors = listOf("CSS mix-blend-mode is unsupported"),
            control = "A",
            category = "BUTTON"
        )

        assertTrue(repairPrompt.contains("# NEXPAD COMPONENT COMPILER REPAIR PROTOCOL"))
        assertTrue(repairPrompt.contains("CSS mix-blend-mode is unsupported"))
        assertTrue(repairPrompt.contains("Approximating filter: blur(4px)"))
        assertTrue(repairPrompt.contains("Control Key**: A"))
        assertTrue(repairPrompt.contains("Category**: BUTTON"))
        assertTrue(repairPrompt.contains("SURGICAL REPAIR CONTRACT"))
        assertTrue(repairPrompt.contains(previousCode))
        assertTrue(repairPrompt.contains("Return ONLY the complete, self-contained HTML/CSS"))
    }

    @Test
    fun normalizerDepthTrackingWithNestedDivs() {
        val rawHtml = """
            <div class="nexpad-btn" data-control="A">
                <div class="outer-bevel">
                    <div class="inner-plate">
                        <div class="label-box">
                            <span class="label">A</span>
                        </div>
                    </div>
                </div>
            </div>
        """.trimIndent()

        val normalized = NxprcHtmlCssConverter.normalizeAiHtml(rawHtml, rootClassHint = "nexpad-btn")

        // Root <div> should be converted to <button>, but inner <div>s must remain <div>!
        assertTrue(normalized.contains("<button class=\"nexpad-btn\""), "Root must be converted to <button>")
        assertFalse(normalized.contains("<div class=\"nexpad-btn\""), "Root <div> must be replaced")
        assertTrue(normalized.trimEnd().endsWith("</button>"), "Root closing tag must be </button>")
        assertTrue(normalized.contains("<div class=\"outer-bevel\">"), "Nested outer-bevel <div> preserved")
        assertTrue(normalized.contains("<div class=\"inner-plate\">"), "Nested inner-plate <div> preserved")
        assertTrue(normalized.contains("<div class=\"label-box\">"), "Nested label-box <div> preserved")
        // Check that inner closing tags remain </div>
        val divCloseCount = Regex("""</div>""").findAll(normalized).count()
        assertEquals(3, divCloseCount, "All 3 inner closing </div> tags must be preserved")
    }

    @Test
    fun normalizerDiscoveredRootClassActiveInjection() {
        val rawHtml = """
            <button class="custom-holo-pad" data-control="X">
                <span>X</span>
            </button>
        """.trimIndent()

        val normalized = NxprcHtmlCssConverter.normalizeAiHtml(rawHtml)
        assertTrue(normalized.contains(".custom-holo-pad:active"), "Fallback active state must target discovered root class")
        assertTrue(normalized.contains("--spring-damping"), "Spring physics must be injected into style block")
    }

    @Test
    fun touchpadRootButtonAndDynamicCategory() {
        val prompt = NxprcHtmlCssConverter.generateAiPrompt("LTP", "TOUCHPAD", 180, 180)
        assertTrue(prompt.contains("Single-Surface Trackpad Architecture"), "Must describe single-surface trackpad")
        assertTrue(prompt.contains("<button class=\"touchpad-ctl\""), "Root skeleton must be <button>")
        assertTrue(prompt.contains("data-category=\"TOUCHPAD\""), "Must carry dynamic category TOUCHPAD")

        // Also test sticks category pass-through
        val sticksPrompt = NxprcHtmlCssConverter.generateAiPrompt("RTP", "STICKS", 180, 180)
        assertTrue(sticksPrompt.contains("data-category=\"STICKS\""), "Must carry dynamic category STICKS")

        // Normalizer test on touchpad
        val rawDivTouchpad = """
            <div class="touchpad-ctl" data-control="LTP">
                <div class="touchpad-surface"></div>
            </div>
        """.trimIndent()
        val normalized = NxprcHtmlCssConverter.normalizeAiHtml(rawDivTouchpad, "touchpad-ctl")
        assertTrue(normalized.contains("<button class=\"touchpad-ctl\""), "Touchpad root div converted to button")
        assertFalse(normalized.contains("<div class=\"touchpad-ctl\""), "Touchpad root div must be replaced")
        assertTrue(normalized.contains("<div class=\"touchpad-surface\"></div>"), "Inner surface div preserved")
        assertTrue(normalized.trimEnd().endsWith("</button>"), "Closing button tag matched")
        assertTrue(normalized.contains(".touchpad-ctl:active"), "Touchpad active state injected")
    }

    @Test
    fun normalizerResolvesNestedCssVariableFallbacks() {
        val cssWithFallbacks = """
            <style>
              :root {
                --defined-color: #00ffcc;
              }
              .btn {
                background: var(--accent, rgba(255, 0, 0, 0.5));
                width: var(--custom-width, calc(100% - 20px));
                border-color: var(--primary, var(--secondary, #ffffff));
                color: var(--defined-color, #000000);
              }
            </style>
            <button class="nexpad-btn" data-control="A"><span>A</span></button>
        """.trimIndent()

        val normalized = NxprcHtmlCssConverter.normalizeAiHtml(cssWithFallbacks, "nexpad-btn")
        assertTrue(normalized.contains("rgba(255, 0, 0, 0.5)"), "Nested rgba() fallback must be preserved")
        assertTrue(normalized.contains("calc(100% - 20px)"), "Nested calc() fallback must be preserved")
        assertTrue(normalized.contains("#ffffff"), "Deeply nested fallback #ffffff must be resolved")
        assertTrue(normalized.contains("#00ffcc"), "Defined variable --defined-color must be resolved")
        assertFalse(normalized.contains("var(--accent"), "Unresolved var(--accent) must be substituted")
    }

    @Test
    fun normalizerVendorPrefixBidirectionalSync() {
        // Case 1: Unprefixed only
        val unprefixedHtml = """
            <style>
              .shape { clip-path: polygon(0 0, 100% 0, 100% 100%); }
            </style>
            <button class="nexpad-btn" data-control="A"><span>A</span></button>
        """.trimIndent()
        val normalized1 = NxprcHtmlCssConverter.normalizeAiHtml(unprefixedHtml, "nexpad-btn")
        assertTrue(normalized1.contains("-webkit-clip-path: polygon(0 0, 100% 0, 100% 100%)"), "Must inject -webkit-clip-path")
        assertTrue(normalized1.contains("clip-path: polygon(0 0, 100% 0, 100% 100%)"), "Must preserve clip-path")
        assertFalse(normalized1.contains("-webkit--webkit"), "Must not double prefix")

        // Case 2: Prefixed only
        val prefixedHtml = """
            <style>
              .shape { -webkit-clip-path: polygon(50% 0%, 0% 100%, 100% 100%); }
            </style>
            <button class="nexpad-btn" data-control="A"><span>A</span></button>
        """.trimIndent()
        val normalized2 = NxprcHtmlCssConverter.normalizeAiHtml(prefixedHtml, "nexpad-btn")
        assertTrue(normalized2.contains("clip-path: polygon(50% 0%, 0% 100%, 100% 100%)"), "Must inject unprefixed clip-path")
        assertTrue(normalized2.contains("-webkit-clip-path: polygon(50% 0%, 0% 100%, 100% 100%)"), "Must preserve -webkit-clip-path")
        assertFalse(normalized2.contains("-webkit--webkit"), "Must not double prefix")
    }

    @Test
    fun normalizerPreservesTranslucentAndGlassStackingOverlays() {
        val htmlWithGlassOverlay = """
            <style>
              .base-plate { position: absolute; z-index: 10; background: #222; }
              .glass-shine { position: absolute; z-index: 5; background: rgba(255, 255, 255, 0.15); pointer-events: none; }
              .text-label { position: absolute; z-index: 8; color: #fff; }
            </style>
            <button class="nexpad-btn" data-control="A">
              <div class="base-plate"></div>
              <div class="glass-shine"></div>
              <span class="text-label">A</span>
            </button>
        """.trimIndent()

        val normalized = NxprcHtmlCssConverter.normalizeAiHtml(htmlWithGlassOverlay, "nexpad-btn")
        // The translucent glass-shine overlay should NOT force arbitrary z-index destruction
        assertTrue(normalized.contains(".glass-shine"), "Glass overlay must be preserved")
        assertTrue(normalized.contains("rgba(255, 255, 255, 0.15)"), "Overlay opacity/rgba must be intact")
    }

    @Test
    fun normalizerMultiClassRootDiscoveryAndSafeClosing() {
        val rawHtml = """
            <div class="control-container nexpad-btn elevated" data-control="B">
                <div class="bevel">
                    <span class="label">B</span>
                </div>
            </div>
        """.trimIndent()

        val normalized = NxprcHtmlCssConverter.normalizeAiHtml(rawHtml)
        assertTrue(normalized.contains("<button class=\"control-container nexpad-btn elevated\""), "Must preserve multi-class root")
        assertTrue(normalized.trimEnd().endsWith("</button>"), "Must cleanly close root button")
        assertTrue(normalized.contains(".nexpad-btn:active"), "Active state must target known nexpad-btn class token")
    }

    @Test
    fun normalizerConservativeSvgViewBox() {
        // Explicit pixel dimensions should synthesize viewBox
        val svgWithPixels = """
            <button class="nexpad-btn" data-control="A">
              <svg width="48px" height="48px"><circle cx="24" cy="24" r="20"/></svg>
            </button>
        """.trimIndent()
        val normalizedPx = NxprcHtmlCssConverter.normalizeAiHtml(svgWithPixels, "nexpad-btn")
        assertTrue(normalizedPx.contains("viewBox=\"0 0 48 48\""), "Should synthesize viewBox for explicit pixel dimensions")

        // Percentage or missing dimensions should NOT synthesize arbitrary 0 0 100 100
        val svgWithPercent = """
            <button class="nexpad-btn" data-control="A">
              <svg width="100%" height="100%"><circle cx="50" cy="50" r="40"/></svg>
            </button>
        """.trimIndent()
        val normalizedPct = NxprcHtmlCssConverter.normalizeAiHtml(svgWithPercent, "nexpad-btn")
        assertFalse(normalizedPct.contains("viewBox=\"0 0 100 100\""), "Must NOT synthesize arbitrary 100 100 viewBox for percentage widths")
    }

    @Test
    fun repairPromptPreservesAiDesignOptions() {
        val options = AiDesignOptions(
            creativity = Creativity.HIGH,
            complexity = Complexity.EXTREME,
            tactilePhysics = "Clicky microswitch with sharp snap",
            userRequest = "Neon cyberpunk samurai emblem"
        )

        val repair = NxprcHtmlCssConverter.generateRepairPrompt(
            previousHtml = "<button class=\"nexpad-btn\"></button>",
            warnings = listOf("Missing label"),
            errors = listOf("No text layer found"),
            control = "A",
            category = "BUTTON",
            options = options
        )

        assertTrue(repair.contains("Clicky microswitch with sharp snap"), "Repair prompt must include tactile physics")
        assertTrue(repair.contains("Neon cyberpunk samurai emblem"), "Repair prompt must include user request")
        assertTrue(repair.contains("Extreme (use the full supported CSS/SVG expressive range"), "Repair prompt must include complexity description")
    }

    @Test
    fun domainModelEncapsulationAndZeroHardcodeTest() {
        // 1. SpringPhysics domain model encapsulation and CSS emission
        val defaultPhysics = SpringPhysics.DEFAULT
        assertEquals(0.68f, defaultPhysics.damping)
        assertEquals(440f, defaultPhysics.stiffness)
        assertEquals(0.92f, defaultPhysics.pressScale)
        assertEquals("0.92", defaultPhysics.pressScaleFormatted)
        assertTrue(defaultPhysics.toDeclarations().contains("--spring-damping: 0.68;"))
        assertTrue(defaultPhysics.toDeclarations().contains("--spring-stiffness: 440;"))
        assertTrue(defaultPhysics.toDeclarations().contains("--press-scale: 0.92;"))
        assertTrue(defaultPhysics.toRootBlock().contains(":root {"))

        val bumperPhysics = SpringPhysics.BUMPER
        assertEquals(0.75f, bumperPhysics.damping)
        assertEquals(520f, bumperPhysics.stiffness)
        assertEquals(0.96f, bumperPhysics.pressScale)

        val stickButtonPhysics = SpringPhysics.STICK_BUTTON
        assertEquals(0.72f, stickButtonPhysics.damping)
        assertEquals(480f, stickButtonPhysics.stiffness)
        assertEquals("0.90", stickButtonPhysics.pressScaleFormatted)

        // Bridge to :protocol SpringPhysicsDef
        val protocolDef = defaultPhysics.toSpringPhysicsDef()
        assertEquals(0.68f, protocolDef.dampingRatio)
        assertEquals(440f, protocolDef.stiffness)
        assertEquals(0.92f, protocolDef.pressedScale)
        assertTrue(protocolDef.enabled)

        // 2. Enum self-encapsulation (no anemic switch/when dependencies)
        Creativity.values().forEach {
            assertTrue(it.promptDescription.isNotBlank(), "Creativity.${it.name} must encapsulate its promptDescription")
        }
        Complexity.values().forEach {
            assertTrue(it.promptDescription.isNotBlank(), "Complexity.${it.name} must encapsulate its promptDescription")
        }
        Fidelity.values().forEach {
            assertTrue(it.promptDescription.isNotBlank(), "Fidelity.${it.name} must encapsulate its promptDescription")
        }
        VisualDensity.values().forEach {
            assertTrue(it.promptDescription.isNotBlank(), "VisualDensity.${it.name} must encapsulate its promptDescription")
        }

        // 3. AiDesignOptions domain method encapsulation
        val defaultOpts = AiDesignOptions()
        assertFalse(defaultOpts.hasCustomParameters(), "Default options should have hasCustomParameters() == false")

        val customizedOpts = AiDesignOptions(style = "Cyberpunk Neo", tactilePhysics = "Snap switch")
        assertTrue(customizedOpts.hasCustomParameters(), "Customized options should have hasCustomParameters() == true")
        val formattedParams = customizedOpts.formatDesignParameters()
        assertTrue(formattedParams.contains("**STYLE**: Cyberpunk Neo"))
        assertTrue(formattedParams.contains("**TACTILE PHYSICS**: Snap switch"))

        val customReq = customizedOpts.formatUserRequest()
        assertTrue(customReq.contains("<user_request>"))
    }

    @Test
    fun testComplexityBudgetDomainModel() {
        Complexity.values().forEach { c ->
            val budget = c.budget
            assertTrue(budget.minLayers > 0, "Complexity ${c.name} minLayers must be > 0")
            assertTrue(budget.maxLayers >= budget.minLayers, "Complexity ${c.name} maxLayers must be >= minLayers")
            assertTrue(budget.maxSvgNodes > 0, "Complexity ${c.name} maxSvgNodes must be > 0")
            assertTrue(budget.guidance.isNotBlank(), "Complexity ${c.name} guidance must be non-blank")
        }

        val options = AiDesignOptions(complexity = Complexity.DETAILED)
        val formatted = options.formatDesignParameters()
        assertTrue(formatted.contains("Budget: 5..9 layers"), "Must include detailed budget range")
        assertTrue(formatted.contains("up to 10 SVG nodes"), "Must include detailed SVG node budget")
    }

    @Test
    fun testModelCapabilityAndGeometryOptions() {
        // Compact models automatically receive syntax skeleton guidance
        val compactPrompt = NxprcAiPromptBuilder.buildPrompt(
            control = "A",
            category = "BUTTON",
            widthDp = 96,
            heightDp = 96,
            options = AiDesignOptions(modelCapability = ModelCapability.COMPACT)
        )
        assertTrue(compactPrompt.contains("OPTIONAL STARTER TEMPLATE"), "Compact model must have syntax skeleton enabled")

        // GeometryOptions overrides default width/height
        val customGeomPrompt = NxprcAiPromptBuilder.buildPrompt(
            control = "RT",
            category = "TRIGGER",
            widthDp = 96,
            heightDp = 96,
            options = AiDesignOptions(geometryOptions = GeometryOptions(widthDp = 140, heightDp = 180))
        )
        assertTrue(customGeomPrompt.contains("width: 140px; height: 180px;"), "Must use geometryOptions dimensions")
    }

    @Test
    fun testComponentPromptStrategyRegistry() {
        val abxyStrategy = ComponentPromptRegistry.resolveStrategy("A", "BUTTON")
        assertTrue(abxyStrategy is AbxyPromptStrategy, "A/BUTTON should resolve to AbxyPromptStrategy")

        val dpadStrategy = ComponentPromptRegistry.resolveStrategy("UP", "DPAD")
        assertTrue(dpadStrategy is DpadPromptStrategy, "UP/DPAD should resolve to DpadPromptStrategy")

        val triggerStrategy = ComponentPromptRegistry.resolveStrategy("RT", "TRIGGER")
        assertTrue(triggerStrategy is TriggerPromptStrategy, "RT/TRIGGER should resolve to TriggerPromptStrategy")

        val bumperStrategy = ComponentPromptRegistry.resolveStrategy("LB", "BUMPER")
        assertTrue(bumperStrategy is BumperPromptStrategy, "LB/BUMPER should resolve to BumperPromptStrategy")

        val stickStrategy = ComponentPromptRegistry.resolveStrategy("LS", "JOYSTICK")
        assertTrue(stickStrategy is StickPromptStrategy, "LS/JOYSTICK should resolve to StickPromptStrategy")

        val stickButtonStrategy = ComponentPromptRegistry.resolveStrategy("LSB", "JOYSTICK")
        assertTrue(stickButtonStrategy is StickButtonPromptStrategy, "LSB/JOYSTICK should resolve to StickButtonPromptStrategy")

        val touchpadStrategy = ComponentPromptRegistry.resolveStrategy("LTP", "JOYSTICK")
        assertTrue(touchpadStrategy is TouchpadPromptStrategy, "LTP/JOYSTICK should resolve to TouchpadPromptStrategy")

        val systemStrategy = ComponentPromptRegistry.resolveStrategy("MENU", "SYSTEM")
        assertTrue(systemStrategy is SystemPromptStrategy, "MENU/SYSTEM should resolve to SystemPromptStrategy")

        assertEquals(8, ComponentPromptRegistry.getAllStrategies().size)
    }

    @Test
    fun testKeyframesContractHarmonization() {
        val prompt = NxprcAiPromptBuilder.buildPrompt("A", "BUTTON", 96, 96)
        assertTrue(prompt.contains("CSS transitions and layout animations are prohibited"), "Must prohibit layout transitions/animations")
        assertTrue(prompt.contains("standard CSS `@keyframes` on transform/opacity properties are supported by the engine"), "Must clarify supported property keyframes")
    }

    @Test
    fun testDataLayerRoleStackingRemediation() {
        val htmlWithInversion = """
            <html>
            <head>
              <style>
                .nexpad-btn { position: relative; width: 96px; height: 96px; }
                .artwork-layer { position: absolute; width: 50px; height: 50px; z-index: 10; }
                .surface-plate { position: absolute; width: 96px; height: 96px; z-index: 20; background: #222; }
              </style>
            </head>
            <body>
              <button class="nexpad-btn" data-control="A" data-category="BUTTON">
                <div class="surface-plate" data-layer-role="surface"></div>
                <div class="artwork-layer" data-layer-role="artwork"></div>
              </button>
            </body>
            </html>
        """.trimIndent()

        val normalized = NxprcHtmlCssConverter.normalize(htmlWithInversion)
        // Stacking remediation must have lowered the surface plate's z-index below the artwork
        assertTrue(normalized.contains("z-index: 8") || normalized.contains("z-index: 9") || normalized.contains("z-index: 7"),
            "Opaque surface with data-layer-role='surface' must be lowered below artwork")
    }

    @Test
    fun testBuildRepairPromptRetainsAllCustomParameters() {
        // Audit issue #31: buildRepairPrompt was omitting shape, material, emblem, lighting, texture, label
        val options = AiDesignOptions(
            shape = "Hexagonal Diamond",
            material = "Brushed Titanium",
            emblem = "Valkyrie Wing Crest",
            lighting = "Bioluminescent Edge",
            texture = "Carbon Honeycomb",
            label = "BOOST"
        )

        val repair = NxprcHtmlCssConverter.generateRepairPrompt(
            previousHtml = "<button class=\"nexpad-btn\"></button>",
            warnings = listOf("Missing explicit px width"),
            errors = listOf(),
            control = "X",
            category = "BUTTON",
            options = options
        )

        assertTrue(repair.contains("Hexagonal Diamond"), "Repair prompt must retain shape")
        assertTrue(repair.contains("Brushed Titanium"), "Repair prompt must retain material")
        assertTrue(repair.contains("Valkyrie Wing Crest"), "Repair prompt must retain emblem")
        assertTrue(repair.contains("Bioluminescent Edge"), "Repair prompt must retain lighting")
        assertTrue(repair.contains("Carbon Honeycomb"), "Repair prompt must retain texture")
        assertTrue(repair.contains("BOOST"), "Repair prompt must retain label")
    }

    @Test
    fun testPromptEliminatesDefaultBiasWhenCustomOptionsProvided() {
        // Audit issue #24: AI Prompt "Default Bias" elimination
        val categories = listOf(
            "BUTTON" to "A",
            "DPAD"   to "UP",
            "TRIGGER" to "RT",
            "BUMPER"  to "RB",
            "JOYSTICK" to "LS",
            "SYSTEM"  to "MENU"
        )

        categories.forEach { (category, control) ->
            // 1. With custom options: MUST contain authoritative user directive and omit Xbox defaults
            val customPrompt = NxprcHtmlCssConverter.generateAiPrompt(
                control = control,
                category = category,
                widthDp = 96,
                heightDp = 96,
                options = AiDesignOptions(
                    style = "Cyberpunk Neo-Tokyo",
                    color = "Hot Magenta & Cyan",
                    shape = "Asymmetric Shard"
                )
            )

            assertTrue(
                customPrompt.contains("USER CUSTOM DESIGN DIRECTIVE [AUTHORITATIVE]"),
                "[$category/$control] Must include authoritative custom directive when custom parameters active"
            )
            assertFalse(
                customPrompt.contains("### DEFAULT VISUAL PROFILE / VISUAL TARGET — CONSOLE/XBOX INDUSTRIAL REALISM:"),
                "[$category/$control] Must NOT contain default Xbox profile when custom parameters active"
            )

            // 2. Without custom options: MUST contain default console realism profile
            val defaultPrompt = NxprcHtmlCssConverter.generateAiPrompt(
                control = control,
                category = category,
                widthDp = 96,
                heightDp = 96,
                options = AiDesignOptions()
            )

            assertTrue(
                defaultPrompt.contains("### DEFAULT VISUAL PROFILE / VISUAL TARGET — CONSOLE/XBOX INDUSTRIAL REALISM:"),
                "[$category/$control] Must include default console profile when no custom parameters active"
            )
            assertFalse(
                defaultPrompt.contains("USER CUSTOM DESIGN DIRECTIVE [AUTHORITATIVE]"),
                "[$category/$control] Must NOT include authoritative custom directive when options are default"
            )
        }
    }

    @Test
    fun testSectionTenGeometryAndVisualQaChecklistIncluded() {
        // Audit issue #27: Geometry & Visual QA checklist
        val prompt = NxprcHtmlCssConverter.generateAiPrompt("A", "BUTTON", 110, 110)
        assertTrue(prompt.contains("SECTION 10 — GEOMETRY & VISUAL QA CHECKLIST"), "Must include Section 10 checklist")
        assertTrue(prompt.contains("width: 110px; height: 110px;"), "Must include explicit dimensions in checklist")
        assertTrue(prompt.contains("Transform Origin Intent"), "Must include Transform Origin Intent check")
        assertTrue(prompt.contains("Visual Stacking & Occlusion"), "Must include Visual Stacking check")
    }

    @Test
    fun testDesignResolverProvenanceAndDefaults() {
        // 1. When options are empty, all fields resolve with CATEGORY_DEFAULT source
        val defaultResolved = DesignResolver.resolve("A", "BUTTON", AiDesignOptions())
        assertEquals(DesignSource.CATEGORY_DEFAULT, defaultResolved.style.source)
        assertEquals(DesignSource.CATEGORY_DEFAULT, defaultResolved.shape.source)
        assertEquals(DesignSource.CATEGORY_DEFAULT, defaultResolved.color.source)
        assertEquals(DesignSource.CATEGORY_DEFAULT, defaultResolved.material.source)
        assertEquals(DesignSource.CATEGORY_DEFAULT, defaultResolved.lighting.source)
        assertEquals(DesignSource.CATEGORY_DEFAULT, defaultResolved.texture.source)
        assertEquals(DesignSource.CATEGORY_DEFAULT, defaultResolved.label.source)
        assertEquals("A", defaultResolved.label.value)
        assertEquals(null, defaultResolved.emblem)

        // Verify explicit default shapes for each category
        val abxy = DesignResolver.resolve("A", "BUTTON", AiDesignOptions())
        assertTrue(abxy.shape.value.contains("Circular Dome"), "ABXY default shape should be circular dome")
        assertTrue(abxy.shape.value.contains("50%"), "ABXY default shape should specify border-radius: 50%")

        val dpad = DesignResolver.resolve("UP", "DPAD", AiDesignOptions())
        assertTrue(dpad.shape.value.contains("Directional Cross"), "Dpad default shape should be directional cross")

        val trigger = DesignResolver.resolve("RT", "TRIGGER", AiDesignOptions())
        assertTrue(trigger.shape.value.contains("Curved Paddle"), "Trigger default shape should be curved paddle")

        val bumper = DesignResolver.resolve("RB", "BUMPER", AiDesignOptions())
        assertTrue(bumper.shape.value.contains("Shoulder Lever"), "Bumper default shape should be shoulder lever")

        val stick = DesignResolver.resolve("LS", "JOYSTICK", AiDesignOptions())
        assertTrue(stick.shape.value.contains("Two-Zone Concentric"), "Stick default shape should be two-zone concentric")

        val stickButton = DesignResolver.resolve("LSB", "STICKS", AiDesignOptions())
        assertTrue(stickButton.shape.value.contains("Circular Thumb Cap Dish"), "Stick button default shape should be circular thumb cap dish")

        val touchpad = DesignResolver.resolve("LTP", "STICKS", AiDesignOptions())
        assertTrue(touchpad.shape.value.contains("Rounded Rectangle"), "Touchpad default shape should be rounded rectangle")

        val system = DesignResolver.resolve("MENU", "SYSTEM", AiDesignOptions())
        assertTrue(system.shape.value.contains("Rounded Squircle / Pill"), "System default shape should be rounded squircle / pill")

        // 2. When user provides explicit overrides, source becomes USER
        val customResolved = DesignResolver.resolve(
            "X",
            "BUTTON",
            AiDesignOptions(
                style = "Anime Cyberpunk",
                shape = "Octagonal Shield",
                color = "Neon Violet",
                material = "Smoked Polycarbonate",
                emblem = "Valkyrie Wing",
                label = "FIRE"
            )
        )
        assertEquals(DesignSource.USER, customResolved.style.source)
        assertEquals("Anime Cyberpunk", customResolved.style.value)
        assertEquals(DesignSource.USER, customResolved.shape.source)
        assertEquals("Octagonal Shield", customResolved.shape.value)
        assertEquals(DesignSource.USER, customResolved.color.source)
        assertEquals(DesignSource.USER, customResolved.material.source)
        assertEquals(DesignSource.USER, customResolved.emblem?.source)
        assertEquals("Valkyrie Wing", customResolved.emblem?.value)
        assertEquals(DesignSource.USER, customResolved.label.source)
        assertEquals("FIRE", customResolved.label.value)
    }

    @Test
    fun testRequiresCustomVisualDirectiveSeparation() {
        // Tuning parameters alone must NOT trigger custom visual directive
        val tuningOnly = AiDesignOptions(
            complexity = Complexity.DETAILED,
            creativity = Creativity.LOW,
            fidelity = Fidelity.ABSTRACT,
            visualDensity = VisualDensity.CLEAN,
            modelCapability = ModelCapability.COMPACT
        )
        assertFalse(
            tuningOnly.requiresCustomVisualDirective(),
            "Non-visual tuning parameters must not require custom visual directive"
        )

        // Visual intent triggers custom visual directive
        val visualIntent = AiDesignOptions(shape = "Hexagon")
        assertTrue(
            visualIntent.requiresCustomVisualDirective(),
            "Visual shape override must require custom visual directive"
        )

        val userPrompt = AiDesignOptions(userRequest = "Cyberpunk glowing shield")
        assertTrue(
            userPrompt.requiresCustomVisualDirective(),
            "User free-form request must require custom visual directive"
        )
    }

    @Test
    fun testModelCapabilityPromptTiering() {
        val compactPrompt = NxprcHtmlCssConverter.generateAiPrompt(
            control = "A",
            category = "BUTTON",
            widthDp = 96,
            heightDp = 96,
            options = AiDesignOptions(modelCapability = ModelCapability.COMPACT)
        )
        assertTrue(
            compactPrompt.contains("SECTION 1 — STRICT COMPILER & ENGINE CONTRACT (LEAN COMPACT MODE)"),
            "Compact prompt must use lean contract"
        )
        assertFalse(
            compactPrompt.contains("SECTION 1 — INSTRUCTION PRIORITY & CONFLICT RESOLUTION"),
            "Compact prompt must not include full 11-section text"
        )

        val standardPrompt = NxprcHtmlCssConverter.generateAiPrompt(
            control = "A",
            category = "BUTTON",
            widthDp = 96,
            heightDp = 96,
            options = AiDesignOptions(modelCapability = ModelCapability.STANDARD)
        )
        assertTrue(
            standardPrompt.contains("SECTION 1 — INSTRUCTION PRIORITY & CONFLICT RESOLUTION"),
            "Standard prompt must include full 11-section boundaries"
        )
        assertTrue(
            standardPrompt.contains("SECTION 10 — GEOMETRY & VISUAL QA CHECKLIST"),
            "Standard prompt must include QA checklist"
        )
    }

    @Test
    fun testRepairPromptProtectsKeyframes() {
        val repair = NxprcHtmlCssConverter.generateRepairPrompt(
            previousHtml = "<button class=\"nexpad-btn\"></button>",
            warnings = listOf("Missing explicit px width"),
            errors = listOf(),
            control = "A",
            category = "BUTTON"
        )
        assertTrue(
            repair.contains("@keyframes"),
            "Repair prompt must mention @keyframes support"
        )
        assertTrue(
            repair.contains("do NOT remove valid keyframes"),
            "Repair prompt must instruct model to preserve valid @keyframes"
        )
    }

    @Test
    fun testAllCategoryPromptsContainPreResolvedIntent() {
        val testCases = listOf(
            "BUTTON" to "A",
            "DPAD" to "DPAD",
            "TRIGGER" to "LT",
            "BUMPER" to "LB",
            "JOYSTICK" to "LS",
            "SYSTEM" to "MENU"
        )

        testCases.forEach { (cat, ctrl) ->
            val prompt = NxprcHtmlCssConverter.generateAiPrompt(
                control = ctrl,
                category = cat,
                widthDp = 96,
                heightDp = 96
            )
            assertTrue(
                prompt.contains("### RESOLVED DESIGN SPECIFICATION (PRE-RESOLVED INTENT):"),
                "[$cat/$ctrl] Must contain pre-resolved design specification"
            )
            assertTrue(
                prompt.contains("[CATEGORY_DEFAULT]"),
                "[$cat/$ctrl] Must state [CATEGORY_DEFAULT] provenance"
            )
        }
    }

    @Test
    fun testFrontierTierContainsPipelineTransparency() {
        val frontierPrompt = NxprcHtmlCssConverter.generateAiPrompt(
            control = "A",
            category = "BUTTON",
            widthDp = 96,
            heightDp = 96,
            options = AiDesignOptions(modelCapability = ModelCapability.FRONTIER)
        )

        assertTrue(
            frontierPrompt.contains("SECTION F1 — COMPILATION PIPELINE TRANSPARENCY"),
            "Frontier prompt must contain Section F1 on compilation pipeline transparency"
        )
        assertTrue(
            frontierPrompt.contains("DomTreeCompiler"),
            "Frontier prompt must explain DomTreeCompiler depth-first walk"
        )
        assertTrue(
            frontierPrompt.contains("CssCascadeResolver"),
            "Frontier prompt must explain CssCascadeResolver specificity"
        )
        assertTrue(
            frontierPrompt.contains("FlexLayoutEngine"),
            "Frontier prompt must explain FlexLayoutEngine W3C layout"
        )
        assertTrue(
            frontierPrompt.contains("LayerStack"),
            "Frontier prompt must explain LayerStack deterministic z-slots"
        )
    }

    @Test
    fun testFrontierTierContainsLayerTypeMapping() {
        val frontierPrompt = NxprcHtmlCssConverter.generateAiPrompt(
            control = "A",
            category = "BUTTON",
            widthDp = 96,
            heightDp = 96,
            options = AiDesignOptions(modelCapability = ModelCapability.FRONTIER)
        )

        assertTrue(
            frontierPrompt.contains("SECTION F2 — LAYER TYPE MAPPING"),
            "Frontier prompt must contain Section F2 on layer type mapping"
        )

        val expectedLayers = listOf(
            "BoxLayer",
            "GradientShape",
            "GlowRing",
            "BezelSocket",
            "InnerShadow",
            "GlossReflection",
            "VectorPath",
            "CenterGlyph",
            "TextLayer"
        )
        expectedLayers.forEach { layer ->
            assertTrue(
                frontierPrompt.contains(layer),
                "Frontier prompt must document compiled layer type: $layer"
            )
        }

        assertTrue(
            frontierPrompt.contains("GlowRing(~1000)"),
            "Frontier prompt must document GlowRing zone"
        )
        assertTrue(
            frontierPrompt.contains("ThumbCap(+20000)"),
            "Frontier prompt must document ThumbCap priority zone"
        )
    }

    @Test
    fun testFrontierTierContainsSvgPreBakingAndAffine() {
        val frontierPrompt = NxprcHtmlCssConverter.generateAiPrompt(
            control = "A",
            category = "BUTTON",
            widthDp = 96,
            heightDp = 96,
            options = AiDesignOptions(modelCapability = ModelCapability.FRONTIER)
        )

        assertTrue(
            frontierPrompt.contains("SECTION F3 — SVG PATH PRE-BAKING PIPELINE"),
            "Frontier prompt must contain Section F3 on SVG pre-baking"
        )
        assertTrue(
            frontierPrompt.contains("AffineMatrix2D"),
            "Frontier prompt must detail AffineMatrix2D matrix accumulation"
        )
        assertTrue(
            frontierPrompt.contains("zero runtime transform overhead"),
            "Frontier prompt must clarify zero runtime transform overhead"
        )

        assertTrue(
            frontierPrompt.contains("SECTION F4 — AFFINE TRANSFORM DECOMPOSITION"),
            "Frontier prompt must contain Section F4 on transform decomposition"
        )
        assertTrue(
            frontierPrompt.contains("TransformDef"),
            "Frontier prompt must describe TransformDef structure"
        )
        assertTrue(
            frontierPrompt.contains("rotationDegrees"),
            "Frontier prompt must document TransformDef scalar fields"
        )
    }

    @Test
    fun testFrontierTierContainsSpringKinematicsAndClassifier() {
        val frontierPrompt = NxprcHtmlCssConverter.generateAiPrompt(
            control = "A",
            category = "BUTTON",
            widthDp = 96,
            heightDp = 96,
            options = AiDesignOptions(modelCapability = ModelCapability.FRONTIER)
        )

        assertTrue(
            frontierPrompt.contains("SECTION F5 — SPRING PHYSICS KINEMATICS"),
            "Frontier prompt must contain Section F5 on spring physics kinematics"
        )
        assertTrue(
            frontierPrompt.contains("damped harmonic oscillator"),
            "Frontier prompt must explain damped harmonic oscillator"
        )
        assertTrue(
            frontierPrompt.contains("x(t) = A · e^(-ζωₙt) · cos(ωd·t + φ)"),
            "Frontier prompt must provide the exact differential physics formula"
        )

        assertTrue(
            frontierPrompt.contains("SECTION F6 — CLASSIFIER INTELLIGENCE"),
            "Frontier prompt must contain Section F6 on compiler classifiers"
        )
        assertTrue(
            frontierPrompt.contains("NodeRoleClassifier"),
            "Frontier prompt must document NodeRoleClassifier"
        )
        assertTrue(
            frontierPrompt.contains("ShapeClassifier"),
            "Frontier prompt must document ShapeClassifier"
        )
        assertTrue(
            frontierPrompt.contains("data-layer-role"),
            "Frontier prompt must recommend data-layer-role attributes"
        )
    }

    @Test
    fun testFrontierTierContainsReasoningProtocol() {
        val frontierPrompt = NxprcHtmlCssConverter.generateAiPrompt(
            control = "A",
            category = "BUTTON",
            widthDp = 96,
            heightDp = 96,
            options = AiDesignOptions(modelCapability = ModelCapability.FRONTIER)
        )

        assertTrue(
            frontierPrompt.contains("SECTION F7 — MANDATORY 5-STEP REASONING PROTOCOL"),
            "Frontier prompt must mandate the 5-step reasoning protocol"
        )
        assertTrue(
            frontierPrompt.contains("Step 1 — LAYER PLAN"),
            "Reasoning protocol must require Step 1 Layer Plan"
        )
        assertTrue(
            frontierPrompt.contains("Step 2 — Z-ORDER VERIFY"),
            "Reasoning protocol must require Step 2 Z-Order Verify"
        )
        assertTrue(
            frontierPrompt.contains("Step 3 — SHAPE AUDIT"),
            "Reasoning protocol must require Step 3 Shape Audit"
        )
        assertTrue(
            frontierPrompt.contains("Step 4 — TRANSFORM CHECK"),
            "Reasoning protocol must require Step 4 Transform Check"
        )
        assertTrue(
            frontierPrompt.contains("Step 5 — BUDGET CHECK"),
            "Reasoning protocol must require Step 5 Budget Check"
        )
    }

    @Test
    fun testAllThreeTiersProduceStrictTokenProgression() {
        val compactPrompt = NxprcHtmlCssConverter.generateAiPrompt(
            control = "A",
            category = "BUTTON",
            widthDp = 96,
            heightDp = 96,
            options = AiDesignOptions(modelCapability = ModelCapability.COMPACT)
        )

        val standardPrompt = NxprcHtmlCssConverter.generateAiPrompt(
            control = "A",
            category = "BUTTON",
            widthDp = 96,
            heightDp = 96,
            options = AiDesignOptions(modelCapability = ModelCapability.STANDARD)
        )

        val frontierPrompt = NxprcHtmlCssConverter.generateAiPrompt(
            control = "A",
            category = "BUTTON",
            widthDp = 96,
            heightDp = 96,
            options = AiDesignOptions(modelCapability = ModelCapability.FRONTIER)
        )

        // Strict size progression
        assertTrue(
            compactPrompt.length < standardPrompt.length,
            "Compact (${compactPrompt.length} chars) must be significantly smaller than Standard (${standardPrompt.length} chars)"
        )
        assertTrue(
            standardPrompt.length < frontierPrompt.length,
            "Standard (${standardPrompt.length} chars) must be significantly smaller than Frontier (${frontierPrompt.length} chars)"
        )

        // Content containment boundaries
        assertFalse(
            compactPrompt.contains("SECTION F1"),
            "Compact prompt must NOT contain Frontier depth sections"
        )
        assertFalse(
            compactPrompt.contains("SECTION 1 — INSTRUCTION PRIORITY & CONFLICT RESOLUTION"),
            "Compact prompt must NOT contain Standard 11-section text"
        )

        assertFalse(
            standardPrompt.contains("SECTION F1"),
            "Standard prompt must NOT contain Frontier depth sections"
        )
        assertTrue(
            standardPrompt.contains("SECTION 1 — INSTRUCTION PRIORITY & CONFLICT RESOLUTION"),
            "Standard prompt MUST contain Standard 11-section text"
        )

        assertTrue(
            frontierPrompt.contains("SECTION 1 — INSTRUCTION PRIORITY & CONFLICT RESOLUTION"),
            "Frontier prompt MUST contain Standard foundational boundaries"
        )
        assertTrue(
            frontierPrompt.contains("SECTION F1 — COMPILATION PIPELINE TRANSPARENCY"),
            "Frontier prompt MUST contain Frontier depth sections"
        )
    }

    @Test
    fun testShinobiButtonVectorEmblemCentering() {
        val userHtml = """
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="utf-8">
<title>NEXPAD — Face Button A (Shinobi)</title>
<style>
  :root{
    --spring-damping: 0.68;
    --spring-stiffness: 440;
    --press-scale: 0.92;

    --nx-orange-hi: #fdba74;
    --nx-orange:    #f97316;
    --nx-orange-mid:#ea580c;
    --nx-orange-deep:#b45309;
    --nx-ink:       #7a2c00;
    --nx-glow:      rgba(249, 115, 22, 0.65);
  }

  html, body{
    height: 100%;
    margin: 0;
  }

  body{
    display: flex;
    align-items: center;
    justify-content: center;
    background: radial-gradient(circle at 50% 45%, #1a1c22 0%, #0a0b0e 72%);
    font-family: "Trebuchet MS", "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
  }

  /* ============ ROOT BUTTON ============ */
  .nexpad-btn{
    position: relative;
    width: 96px;
    height: 96px;
    margin: 0;
    padding: 0;
    border: 0;
    outline: 0;
    background: transparent;
    cursor: pointer;
    transform-origin: 50% 50%;
    transform: scale(1) translateY(0);
    -webkit-tap-highlight-color: transparent;
  }

  /* ---------- 0 : AMBIENT GLOW RING ---------- */
  .nx-glow{
    position: absolute;
    left: -10px;
    top: -8px;
    width: 116px;
    height: 116px;
    border-radius: 40px;
    background: radial-gradient(circle at 50% 50%,
                  rgba(249, 115, 22, 0.85) 0%,
                  rgba(234, 88, 12, 0.42) 46%,
                  rgba(194, 65, 12, 0) 74%);
    filter: blur(9px);
    opacity: 0.7;
    z-index: 0;
    pointer-events: none;
    transform-origin: 50% 50%;
    animation: nx-breathe 3.6s ease-in-out infinite;
  }

  @keyframes nx-breathe{
    0%, 100%{ opacity: 0.52; transform: scale(0.97); }
    50%     { opacity: 0.86; transform: scale(1.03); }
  }

  /* ---------- 1 : DARK CHASSIS / SOCKET WELL ---------- */
  .nx-housing{
    position: absolute;
    left: 0;
    top: 0;
    width: 96px;
    height: 96px;
    border-radius: 26px;
    background:
      linear-gradient(160deg, #3b2a1b 0%, #1d1309 44%, #080503 100%);
    box-shadow:
      0 10px 24px rgba(0, 0, 0, 0.68),
      0 2px 0 rgba(255, 255, 255, 0.07),
      inset 0 -5px 12px rgba(0, 0, 0, 0.85),
      inset 0 2px 3px rgba(255, 190, 130, 0.20);
    z-index: 1;
    pointer-events: none;
  }

  /* ---------- 2 : ORANGE KEYCAP FACE PLATE ---------- */
  .nx-face{
    position: absolute;
    left: 5px;
    top: 5px;
    width: 86px;
    height: 86px;
    border-radius: 22px;
    background:
      radial-gradient(circle at 32% 20%,
        rgba(255, 255, 255, 0.60) 0%,
        rgba(255, 255, 0.10) 38%,
        rgba(255, 255, 255, 0) 58%),
      radial-gradient(circle at 68% 88%,
        rgba(120, 45, 0, 0.55) 0%,
        rgba(120, 45, 0, 0) 62%),
      linear-gradient(148deg,
        var(--nx-orange-hi) 0%,
        var(--nx-orange) 34%,
        var(--nx-orange-mid) 66%,
        var(--nx-orange-deep) 100%);
    box-shadow:
      inset 0 3px 6px rgba(255, 245, 230, 0.55),
      inset 0 -9px 16px rgba(110, 38, 0, 0.55),
      inset 0 0 0 2px rgba(70, 22, 0, 0.35),
      0 4px 10px rgba(0, 0, 0, 0.55);
    z-index: 2;
    pointer-events: none;
  }

  /* ---------- 3 : KONOHA SWIRL EMBLEM ---------- */
  .button-emblem{
    position: absolute;
    left: 50%;
    top: 50%;
    width: 58px;
    height: 58px;
    transform: translate(-50%, -50%);
    opacity: 0.55;
    z-index: 3;
    pointer-events: none;
  }

  /* ---------- 4 : SPECULAR GLOSS ARC ---------- */
  .nexpad-btn::after{
    content: "";
    position: absolute;
    left: 12px;
    top: 9px;
    width: 72px;
    height: 30px;
    border-radius: 50% 50% 48% 48% / 62% 62% 38% 38%;
    background: linear-gradient(180deg,
      rgba(255, 255, 255, 0.55) 0%,
      rgba(255, 255, 255, 0.16) 52%,
      rgba(255, 255, 255, 0) 100%);
    filter: blur(0.4px);
    z-index: 4;
    pointer-events: none;
  }

  /* ---------- 5 : HIGH-CONTRAST GLYPH ---------- */
  .btn-label{
    position: absolute;
    left: 0;
    top: 0;
    width: 96px;
    height: 96px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 42px;
    font-weight: 900;
    line-height: 1;
    letter-spacing: 0.01em;
    color: #fff8ef;
    text-shadow:
      -1.5px -1.5px 0 #3a1200,
       1.5px -1.5px 0 #3a1200,
      -1.5px  1.5px 0 #3a1200,
       1.5px  1.5px 0 #3a1200,
       0 3px 6px rgba(40, 10, 0, 0.75),
       0 0 14px rgba(255, 226, 190, 0.45);
    z-index: 9;
    pointer-events: none;
    user-select: none;
  }

  /* ---------- TACTILE SPRING PRESS ---------- */
  .nexpad-btn:active{
    transform: scale(var(--press-scale)) translateY(2px);
  }
</style>
</head>
<body>

  <button class="nexpad-btn" data-control="A" data-category="BUTTON" data-name="A">
    <span class="nx-glow" aria-hidden="true"></span>
    <span class="nx-housing" aria-hidden="true"></span>
    <span class="nx-face" data-layer-role="base" aria-hidden="true"></span>

    <svg class="button-emblem" viewBox="0 0 100 100" data-layer-role="emblem" aria-hidden="true">
      <!-- leaf tip -->
      <path d="M50 16 C55 8 64 5 78 7 C72 15 63 19 50 16 Z"
            fill="#7a2c00"/>
      <!-- uzumaki swirl stem -->
      <path d="M50 16 C70 16 84 32 84 52 C84 72 70 86 50 86 C34 86 22 74 22 58 C22 45 32 35 45 35 C55 35 63 43 63 53 C63 60 57 66 50 66"
            fill="none"
            stroke="#7a2c00"
            stroke-width="7.5"
            stroke-linecap="round"
            stroke-linejoin="round"/>
    </svg>

    <span class="btn-label" data-layer-role="label">A</span>
  </button>

</body>
</html>
        """.trimIndent()

        val result = NxprcHtmlCssConverter.convertWithWarnings(
            source = userHtml,
            id = "rc.shinobi_test",
            name = "Shinobi A",
            category = "BUTTON",
            defaultControl = "A"
        )

        val doc = result.document
        assertEquals(8, doc.canvas.layers.size, "Document must have exactly 8 compiled layers")

        // Layer 4: Leaf tip vector path
        val leafTip = doc.canvas.layers[4] as com.sanket.tools.nexpad.nxprc.CanvasLayer.VectorPath
        assertEquals(0.1979f, leafTip.offsetXRatio, 0.001f, "Leaf tip must be offset correctly by translate(-50%, -50%)")
        assertEquals(0.1979f, leafTip.offsetYRatio, 0.001f, "Leaf tip must be offset correctly by translate(-50%, -50%)")

        // Layer 5: Uzumaki swirl stem vector path
        val swirlStem = doc.canvas.layers[5] as com.sanket.tools.nexpad.nxprc.CanvasLayer.VectorPath
        assertEquals(0.1979f, swirlStem.offsetXRatio, 0.001f, "Swirl stem must be offset correctly by translate(-50%, -50%)")
        assertEquals(0.1979f, swirlStem.offsetYRatio, 0.001f, "Swirl stem must be offset correctly by translate(-50%, -50%)")
        assertEquals(0.6041f, swirlStem.scale, 0.001f, "Swirl stem scale must match 58px / 96px bounds")

        // Layer 7: Center glyph 'A'
        val glyph = doc.canvas.layers[7] as com.sanket.tools.nexpad.nxprc.CanvasLayer.CenterGlyph
        assertEquals("A", glyph.text, "Glyph text must be 'A'")
    }
}




