package com.sanket.tools.nexpaddesktop

import com.sanket.tools.nexpaddesktop.plugins.AiDesignOptions
import com.sanket.tools.nexpaddesktop.plugins.AnimationIntensity
import com.sanket.tools.nexpaddesktop.plugins.Complexity
import com.sanket.tools.nexpaddesktop.plugins.Creativity
import com.sanket.tools.nexpaddesktop.plugins.DesignFreedom
import com.sanket.tools.nexpaddesktop.plugins.DesignResolver
import com.sanket.tools.nexpaddesktop.plugins.DetailPriority
import com.sanket.tools.nexpaddesktop.plugins.Fidelity
import com.sanket.tools.nexpaddesktop.plugins.ModelCapability
import com.sanket.tools.nexpaddesktop.plugins.NxprcHtmlCssConverter
import com.sanket.tools.nexpaddesktop.plugins.Originality
import com.sanket.tools.nexpaddesktop.plugins.VisualDensity
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Comprehensive limits and stress test suite for NEXPAD AI Prompt Builder
 * and the NEXPAD Vector Engine HTML/CSS compiler.
 *
 * Evaluates:
 * 1. Model Capability Tiers (COMPACT, STANDARD, FRONTIER) across all parameters.
 * 2. Parameter boundary extremes (Complexity, Creativity, Fidelity, Density, Freedom, Animation, Detail, Originality).
 * 3. User Supremacy under extreme overrides (custom geometry, mobility waiver, custom palettes).
 * 4. Token budget & cognitive density limits.
 * 5. Real HTML/CSS compilation through NxprcHtmlCssConverter across all capability tiers.
 */
class NxprcLimitAndStressTest {

    // =========================================================================
    // 1. MODEL CAPABILITY TIERS MATRIX TESTING
    // =========================================================================

    @Test
    fun testCompactTierSmallModelLimits() {
        val options = AiDesignOptions(
            modelCapability = ModelCapability.COMPACT,
            userRequest = "Minimalist stealth black button with emerald dot"
        )
        val prompt = NxprcHtmlCssConverter.generateAiPrompt("A", "BUTTON", 96, 96, options)
        val standardPrompt = NxprcHtmlCssConverter.generateAiPrompt("A", "BUTTON", 96, 96, AiDesignOptions(modelCapability = ModelCapability.STANDARD))
        val frontierPrompt = NxprcHtmlCssConverter.generateAiPrompt("A", "BUTTON", 96, 96, AiDesignOptions(modelCapability = ModelCapability.FRONTIER))

        // 1. Strict size progression: Compact is significantly leaner than Standard, and Standard is leaner than Frontier
        assertTrue(prompt.length < standardPrompt.length, "Compact (${prompt.length} chars) must be smaller than Standard (${standardPrompt.length} chars)")
        assertTrue(standardPrompt.length < frontierPrompt.length, "Standard (${standardPrompt.length} chars) must be smaller than Frontier (${frontierPrompt.length} chars)")

        // 2. Must contain lean compiler contract
        assertTrue(prompt.contains("SECTION 1 — STRICT COMPILER & ENGINE CONTRACT (LEAN COMPACT MODE)"))

        // 3. Must NOT leak heavy Standard/Frontier sections to prevent small model confusion
        assertFalse(prompt.contains("SECTION 1 — INSTRUCTION PRIORITY & CONFLICT RESOLUTION (5 LAYERS OF AUTHORITY)"))
        assertFalse(prompt.contains("SECTION 10 — GEOMETRY & VISUAL QA CHECKLIST"))
        assertFalse(prompt.contains("SECTION F0 — UNLIMITED ARCHITECTURAL EXPRESSION"))
        assertFalse(prompt.contains("SECTION F7 — MANDATORY 5-STEP REASONING PROTOCOL"))

        // 4. Must place user request right at the top
        val userReqIdx = prompt.indexOf("Minimalist stealth black button")
        val contractIdx = prompt.indexOf("SECTION 1 — STRICT COMPILER")
        assertTrue(userReqIdx != -1 && userReqIdx < contractIdx, "User request must precede contract in Compact tier")

        // 5. Must enforce core compiler contract
        assertTrue(prompt.contains("Single Button Root"))
        assertTrue(prompt.contains("Explicit Dimensions"))
        assertTrue(prompt.contains("Real DOM Text"))
    }

    @Test
    fun testStandardTierMidRangeModelLimits() {
        val options = AiDesignOptions(
            modelCapability = ModelCapability.STANDARD,
            userRequest = "Industrial brushed titanium button with orange chamfer"
        )
        val prompt = NxprcHtmlCssConverter.generateAiPrompt("A", "BUTTON", 96, 96, options)

        // 1. Must contain the 5 Layers of Instruction Authority
        assertTrue(prompt.contains("SECTION 1 — INSTRUCTION PRIORITY & CONFLICT RESOLUTION (5 LAYERS OF AUTHORITY)"))
        assertTrue(prompt.contains("Layer 1: Non-Negotiable Compiler Safety"))
        assertTrue(prompt.contains("Layer 2: Component Semantics"))
        assertTrue(prompt.contains("Layer 3: User's Explicit Customization"))
        assertTrue(prompt.contains("Layer 4: AI Creative Interpretation"))
        assertTrue(prompt.contains("Layer 5: Category Defaults & Fallback Hardware Profiles"))

        // 2. Must enshrine the Universal Law of User Supremacy
        assertTrue(prompt.contains("UNIVERSAL LAW OF USER SUPREMACY"))
        assertTrue(prompt.contains("User is God") || prompt.contains("User is Sovereign"))

        // 3. User request must appear before category semantics
        val userReqIdx = prompt.indexOf("Industrial brushed titanium button")
        val semanticsIdx = prompt.indexOf("CATEGORY SEMANTICS & INTERACTION MEANING")
        assertTrue(userReqIdx != -1 && userReqIdx < semanticsIdx, "User request must precede category semantics")

        // 4. Must include Visual QA checklist
        assertTrue(prompt.contains("SECTION 10 — GEOMETRY & VISUAL QA CHECKLIST"))
    }

    @Test
    fun testFrontierTierFlagshipModelLimits() {
        val options = AiDesignOptions(
            modelCapability = ModelCapability.FRONTIER,
            userRequest = "Cyberpunk high-frequency plasma reactor button with holographic runes"
        )
        val prompt = NxprcHtmlCssConverter.generateAiPrompt("A", "BUTTON", 96, 96, options)

        // 1. Must include Section F0 (Adaptive Architectural Expression with zero caps)
        assertTrue(prompt.contains("SECTION F0 — UNLIMITED ARCHITECTURAL EXPRESSION & ZERO COMPLEXITY CAPS"))
        assertTrue(prompt.contains("Adaptive Architectural Expression"))

        // 2. Must include Section F7 (Reasoning Protocol) marked explicitly as internal validation
        assertTrue(prompt.contains("SECTION F7 — MANDATORY 5-STEP REASONING PROTOCOL"))
        assertTrue(prompt.contains("INTERNAL THINKING ONLY") || prompt.contains("INTERNAL VALIDATION"))
        assertTrue(prompt.contains("DO NOT OUTPUT VISIBLY IN MARKDOWN"))

        // 3. Must contain advanced shader math and SVG guidelines
        assertTrue(prompt.contains("radial-gradient"))
        assertTrue(prompt.contains("conic-gradient"))
        assertTrue(prompt.contains("feGaussianBlur"))

        // 4. User request must be top-level
        val userReqIdx = prompt.indexOf("Cyberpunk high-frequency plasma reactor")
        val semanticsIdx = prompt.indexOf("CATEGORY SEMANTICS & INTERACTION MEANING")
        assertTrue(userReqIdx != -1 && userReqIdx < semanticsIdx, "User request must precede category semantics in Frontier tier")
    }

    // =========================================================================
    // 2. ALL 8 CATEGORY ARCHITECTURES LIMITS TESTING
    // =========================================================================

    @Test
    fun testAllEightCategoriesExposeCorrectRootAndTopLevelUserIntent() {
        val testMatrix = listOf(
            Triple("A", "BUTTON", "nexpad-btn"),
            Triple("UP", "DPAD", "dpad-btn"),
            Triple("LT", "TRIGGER", "trigger-btn"),
            Triple("LB", "BUMPER", "bumper-btn"),
            Triple("LS", "JOYSTICK", "stick-btn"),
            Triple("LSB", "BUTTON", "stick-btn-ctl"),
            Triple("LTP", "TOUCHPAD", "touchpad-ctl"),
            Triple("MENU", "SYSTEM", "system-btn")
        )

        testMatrix.forEach { (control, category, expectedClass) ->
            val userMsg = "CUSTOM_DEMAND_FOR_${control}_${category}"
            val prompt = NxprcHtmlCssConverter.generateAiPrompt(
                control = control,
                category = category,
                widthDp = 100,
                heightDp = 100,
                options = AiDesignOptions(userRequest = userMsg)
            )

            // Must contain class
            assertTrue(prompt.contains(expectedClass), "Category $category ($control) missing class $expectedClass")

            // User request must appear BEFORE category semantics
            val userIdx = prompt.indexOf(userMsg)
            val semanticsIdx = prompt.indexOf("CATEGORY SEMANTICS & INTERACTION MEANING")
            assertTrue(userIdx != -1, "User message missing for $category ($control)")
            assertTrue(semanticsIdx != -1, "Semantics missing for $category ($control)")
            assertTrue(userIdx < semanticsIdx, "User message must precede semantics in $category ($control)")

            // Precedence notice must be active
            assertTrue(prompt.contains("PRECEDENCE NOTICE"), "Category $category missing PRECEDENCE NOTICE")
        }
    }

    // =========================================================================
    // 3. PARAMETER BOUNDARIES & EXTREMES TESTING
    // =========================================================================

    @Test
    fun testComplexityBudgetLimits() {
        // Test Simple: strict 1-3 layers
        val simpleOpts = AiDesignOptions(complexity = Complexity.SIMPLE)
        val simplePrompt = NxprcHtmlCssConverter.generateAiPrompt("A", "BUTTON", 96, 96, simpleOpts)
        assertTrue(simplePrompt.contains("Target: 3 layers") || simplePrompt.contains("max 5 layers"))

        // Test Extreme: unlimited layers for flagship models
        val extremeOpts = AiDesignOptions(complexity = Complexity.EXTREME)
        val extremePrompt = NxprcHtmlCssConverter.generateAiPrompt("A", "BUTTON", 96, 96, extremeOpts)
        assertTrue(extremePrompt.contains("unlimited layers"))
        assertTrue(extremePrompt.contains("unlimited SVG"))
    }

    @Test
    fun testDesignFreedomSpectrumLimits() {
        // STRICT: Authentic console hardware adherence
        val strictOpts = AiDesignOptions(designFreedom = DesignFreedom.STRICT)
        val strictPrompt = NxprcHtmlCssConverter.generateAiPrompt("A", "BUTTON", 96, 96, strictOpts)
        assertTrue(strictPrompt.contains("Strict (authentic adherence to physical console gamepad hardware"))

        // OPEN: Wild avant-garde creative departure
        val openOpts = AiDesignOptions(designFreedom = DesignFreedom.OPEN)
        val openPrompt = NxprcHtmlCssConverter.generateAiPrompt("A", "BUTTON", 96, 96, openOpts)
        assertTrue(openPrompt.contains("Open (unconstrained creative freedom; bold stylistic departure"))
    }

    @Test
    fun testAnimationIntensitySpectrumLimits() {
        // NONE: Static design
        val noneOpts = AiDesignOptions(animationIntensity = AnimationIntensity.NONE)
        val nonePrompt = NxprcHtmlCssConverter.generateAiPrompt("A", "BUTTON", 96, 96, noneOpts)
        assertTrue(nonePrompt.contains("None (purely static design without idle animations)"))

        // DYNAMIC: High-energy 360° motion / reactor
        val dynamicOpts = AiDesignOptions(animationIntensity = AnimationIntensity.DYNAMIC)
        val dynamicPrompt = NxprcHtmlCssConverter.generateAiPrompt("A", "BUTTON", 96, 96, dynamicOpts)
        assertTrue(dynamicPrompt.contains("Dynamic (high-energy motion: fast rotating reactor, 360° RGB chromatic sweep)"))
    }

    @Test
    fun testDetailPrioritySpectrumLimits() {
        val priorities = listOf(
            DetailPriority.SILHOUETTE to "Silhouette (strong geometric contour",
            DetailPriority.MATERIAL to "Material (tactile surface shaders",
            DetailPriority.MECHANICS to "Mechanics (seams, bolts, socket wells",
            DetailPriority.GRAPHICS to "Graphics (intricate embedded SVG vector emblem"
        )

        priorities.forEach { (priority, expectedSubstring) ->
            val opts = AiDesignOptions(detailPriority = priority)
            val prompt = NxprcHtmlCssConverter.generateAiPrompt("A", "BUTTON", 96, 96, opts)
            assertTrue(prompt.contains(expectedSubstring), "Missing description for detail priority $priority")
        }
    }

    @Test
    fun testOriginalitySpectrumLimits() {
        val originalities = listOf(
            Originality.FAITHFUL to "Faithful (closely preserve authentic gamepad conventions",
            Originality.INSPIRED to "Inspired (creative reimagining",
            Originality.EXPERIMENTAL to "Experimental (radical, avant-garde synthesis"
        )

        originalities.forEach { (orig, expectedSubstring) ->
            val opts = AiDesignOptions(originality = orig)
            val prompt = NxprcHtmlCssConverter.generateAiPrompt("A", "BUTTON", 96, 96, opts)
            assertTrue(prompt.contains(expectedSubstring), "Missing description for originality $orig")
        }
    }

    // =========================================================================
    // 4. USER SUPREMACY & EXTREME BOUNDARY CONDITIONS
    // =========================================================================

    @Test
    fun testUserSupremacyOverMobileDisplacementRestriction() {
        val prompt = NxprcHtmlCssConverter.generateAiPrompt(
            control = "LT",
            category = "TRIGGER",
            widthDp = 110,
            heightDp = 140,
            options = AiDesignOptions(
                modelCapability = ModelCapability.FRONTIER,
                userRequest = "Trigger paddle must visibly plunge and translate down by 15px on pull"
            )
        )

        // Verify that while zero-displacement is the default, the Universal Law of User Supremacy explicitly waives it
        assertTrue(prompt.contains("Universal Law of User Supremacy"))
        assertTrue(prompt.contains("User is Sovereign") || prompt.contains("User is God"))
        assertTrue(prompt.contains("strictly waive this restriction and faithfully fulfill the user's demand"))
    }

    @Test
    fun testUserSupremacyOverCategoryGeometryDefault() {
        val resolved = DesignResolver.resolve(
            control = "DPAD",
            category = "DPAD",
            options = AiDesignOptions(
                shape = "Futuristic Hexagonal Shield",
                color = "#FF0055 Neon Magenta"
            )
        )
        val spec = resolved.toPromptSpecification()

        // User overrides must be tagged [USER] and override defaults
        assertTrue(spec.contains("Futuristic Hexagonal Shield [USER]"))
        assertTrue(spec.contains("#FF0055 Neon Magenta [USER]"))
        assertTrue(spec.contains("PRECEDENCE NOTICE"))
        assertTrue(spec.contains("zero override authority against user instructions"))
    }

    // =========================================================================
    // 5. REAL HTML/CSS COMPILATION STRESS TESTING (NEXPAD VECTOR ENGINE)
    // =========================================================================

    @Test
    fun testCompileCompactTierMinimalistHtml() {
        val compactHtml = """
            <style>
              :root { --spring-damping: 0.85; --spring-stiffness: 450; --press-scale: 0.94; }
              .nexpad-btn {
                position: relative; width: 96px; height: 96px;
                border-radius: 50%; background: #1a1a1a;
                border: 2px solid #333; outline: none; cursor: pointer;
              }
              .nexpad-btn:active { transform: scale(var(--press-scale)); }
              .btn-label {
                position: absolute; left: 0; top: 0; width: 96px; height: 96px;
                display: flex; align-items: center; justify-content: center;
                font-family: sans-serif; font-size: 32px; font-weight: bold; color: #fff;
              }
            </style>
            <button class="nexpad-btn" data-control="A" data-category="BUTTON" data-name="Compact A">
              <span class="btn-label">A</span>
            </button>
        """.trimIndent()

        val doc = NxprcHtmlCssConverter.convert(compactHtml, "compact_a", "Compact A")
        assertTrue(doc.canvas.layers.isNotEmpty(), "Compact HTML must produce canvas layers")
        assertEquals(96, doc.canvas.viewBoxWidth.toInt())
        assertEquals(96, doc.canvas.viewBoxHeight.toInt())
        assertTrue(doc.manifest.springPhysics.enabled, "Spring physics must be active")
        assertEquals(0.85f, doc.manifest.springPhysics.dampingRatio)
    }

    @Test
    fun testCompileStandardTierRichIndustrialHtml() {
        val standardHtml = """
            <style>
              :root { --spring-damping: 0.82; --spring-stiffness: 420; --press-scale: 0.93; }
              .nexpad-btn {
                position: relative; width: 96px; height: 96px;
                border-radius: 50%;
                background: radial-gradient(circle at 35% 35%, #2a2a2a, #0a0a0a);
                box-shadow: 0 4px 12px rgba(0,0,0,0.7), inset 0 1px 2px rgba(255,255,255,0.2);
                border: none; outline: none;
              }
              .nexpad-btn:active { transform: scale(var(--press-scale)); }
              .bezel-ring {
                position: absolute; left: 4px; top: 4px; width: 88px; height: 88px;
                border-radius: 50%; border: 2px solid #444;
              }
              .inner-dish {
                position: absolute; left: 12px; top: 12px; width: 72px; height: 72px;
                border-radius: 50%;
                background: linear-gradient(145deg, #10b981, #047857);
                box-shadow: inset 0 2px 4px rgba(0,0,0,0.5);
              }
              .label {
                position: absolute; left: 0; top: 0; width: 96px; height: 96px;
                display: flex; align-items: center; justify-content: center;
                font-family: system-ui; font-size: 28px; font-weight: 800; color: #ffffff;
              }
            </style>
            <button class="nexpad-btn" data-control="A" data-category="BUTTON" data-name="Industrial A">
              <span class="bezel-ring"></span>
              <span class="inner-dish"></span>
              <span class="label">A</span>
            </button>
        """.trimIndent()

        val doc = NxprcHtmlCssConverter.convert(standardHtml, "industrial_a", "Industrial A")
        assertTrue(doc.canvas.layers.size >= 3, "Standard component must produce 3+ visual layers")
        assertEquals(96, doc.canvas.viewBoxWidth.toInt())
        assertEquals(96, doc.canvas.viewBoxHeight.toInt())
        assertTrue(doc.manifest.springPhysics.enabled)
    }

    @Test
    fun testCompileFrontierTierExtremeVectorGraphicHtml() {
        val frontierHtml = """
            <style>
              :root { --spring-damping: 0.80; --spring-stiffness: 400; --press-scale: 0.92; }
              .nexpad-btn {
                position: relative; width: 100px; height: 100px;
                border-radius: 20px;
                background: radial-gradient(circle at 50% 50%, #1e1b4b, #09090b);
                box-shadow: 0 8px 24px rgba(99,102,241,0.3), inset 0 1px 3px rgba(255,255,255,0.4);
                border: 1px solid rgba(99,102,241,0.5); outline: none;
              }
              .nexpad-btn:active { transform: scale(var(--press-scale)); }
              .ambient-glow {
                position: absolute; left: 10px; top: 10px; width: 80px; height: 80px;
                border-radius: 50%;
                background: #6366f1;
                filter: blur(12px);
                opacity: 0.4;
              }
              .button-emblem {
                position: absolute; left: 20px; top: 20px; width: 60px; height: 60px;
              }
              .button-emblem path {
                fill: #ffffff;
              }
              .label {
                position: absolute; left: 0; top: 0; width: 100px; height: 100px;
                display: flex; align-items: center; justify-content: center;
                font-family: monospace; font-size: 24px; font-weight: 900; color: #a5b4fc;
              }
            </style>
            <button class="nexpad-btn" data-control="X" data-category="BUTTON" data-name="Frontier Cyberpunk X">
              <span class="ambient-glow"></span>
              <svg class="button-emblem" viewBox="0 0 100 100">
                <path d="M 20 20 L 80 80 M 80 20 L 20 80" stroke="#a5b4fc" stroke-width="8" stroke-linecap="round"/>
              </svg>
              <span class="label">X</span>
            </button>
        """.trimIndent()

        val doc = NxprcHtmlCssConverter.convert(frontierHtml, "frontier_x", "Frontier Cyberpunk X")
        assertTrue(doc.canvas.layers.isNotEmpty(), "Frontier component must produce canvas layers")
        assertEquals(100, doc.canvas.viewBoxWidth.toInt())
        assertEquals(100, doc.canvas.viewBoxHeight.toInt())
        assertTrue(doc.manifest.springPhysics.enabled)
    }

    @Test
    fun testCompilePolygonClipPathGeometry() {
        val polygonHtml = """
            <style>
              :root { --spring-damping: 0.85; --spring-stiffness: 450; --press-scale: 0.94; }
              .nexpad-btn {
                position: relative; width: 96px; height: 96px;
                clip-path: polygon(50% 0%, 100% 25%, 100% 75%, 50% 100%, 0% 75%, 0% 25%);
                background: linear-gradient(135deg, #f59e0b, #b45309);
                border: none; outline: none;
              }
              .nexpad-btn:active { transform: scale(var(--press-scale)); }
              .label {
                position: absolute; left: 0; top: 0; width: 96px; height: 96px;
                display: flex; align-items: center; justify-content: center;
                font-size: 24px; font-weight: bold; color: #ffffff;
              }
            </style>
            <button class="nexpad-btn" data-control="Y" data-category="BUTTON" data-name="Hexagon Y">
              <span class="label">Y</span>
            </button>
        """.trimIndent()

        val doc = NxprcHtmlCssConverter.convert(polygonHtml, "hexagon_y", "Hexagon Y")
        assertTrue(doc.canvas.layers.isNotEmpty(), "Hexagonal polygon component must compile cleanly")
        assertEquals(96, doc.canvas.viewBoxWidth.toInt())
    }
}
