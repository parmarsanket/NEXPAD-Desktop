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
                heightDp = 96
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
}


