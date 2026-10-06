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

    @Test
    fun testMacroDetectionAndReferenceTemplate() {
        // Verify reference template for M1 returns Macro paddle code
        val template = com.sanket.tools.nexpaddesktop.plugins.NxprcPresets.getReferenceTemplate("M1", "MACROS")
        assertTrue("Template should contain M1", template.contains("M1"))
        assertTrue("Template should declare MACRO category", template.contains("data-category=\"MACRO\""))

        // Verify detection on Macro template
        val detected = NxprcComponentDetector.detect(
            html = template,
            fallbackCategory = "BUTTON",
            fallbackControl = "A",
            fallbackId = "rc.action_a",
            fallbackName = "Action A Button"
        )

        assertTrue(detected.isExplicitlyDefined)
        assertEquals(ControlKey.M1, detected.controlKey)
        assertEquals("M1", detected.defaultControl)
        assertEquals("MACRO", detected.category)
        assertEquals(CategoryType.MACROS, detected.categoryType)
        assertEquals("rc.macro_m1", detected.componentId)
    }

    @Test
    fun testProceduralSeedDetectionAndAntiDuplication() {
        val htmlSeed1 = """
            <button class="nexpad-btn" data-control="A" data-category="BUTTON" data-seed="74829">
              <span class="btn-label">A</span>
            </button>
        """.trimIndent()

        val htmlSeed2 = """
            <button class="nexpad-btn" data-control="A" data-category="BUTTON" data-seed="12458">
              <span class="btn-label">A</span>
            </button>
        """.trimIndent()

        val d1 = NxprcComponentDetector.detect(htmlSeed1, "BUTTON", "A", "rc.action_a", "Action A Button")
        val d2 = NxprcComponentDetector.detect(htmlSeed2, "BUTTON", "A", "rc.action_a", "Action A Button")

        // 1. Both are recognized as button A
        assertEquals("A", d1.defaultControl)
        assertEquals("A", d2.defaultControl)

        // 2. Seeds are parsed accurately
        assertEquals(74829L, d1.seed)
        assertEquals(12458L, d2.seed)

        // 3. IDs are completely unique — preventing Wi-Fi push overwrite!
        assertEquals("rc.a_74829", d1.componentId)
        assertEquals("rc.a_12458", d2.componentId)
        assertNotEquals(d1.componentId, d2.componentId)

        // 4. Procedural skin names are deterministic and unique based strictly on number
        assertEquals("A #74829", d1.componentName)
        assertEquals("A #12458", d2.componentName)
        assertNotEquals(d1.componentName, d2.componentName)
    }

    @Test
    fun testUserXboxButtonWithSeed46090() {
        val userHtml = """
            <!DOCTYPE html>
            <html lang="en">
            <head>
            <meta charset="UTF-8">
            <style>
            :root {
              --spring-damping: 0.68;
              --spring-stiffness: 440;
              --press-scale: 0.92;
              --xbox-green: #5dc21e;
              --xbox-green-dark: #35850f;
              --xbox-black: #111418;
            }

            * {
              box-sizing: border-box;
            }

            body {
              margin: 0;
              width: 96px;
              height: 96px;
              overflow: hidden;
              background: transparent;
            }

            .nexpad-btn {
              position: relative;
              width: 96px;
              height: 96px;
              padding: 0;
              border: 0;
              border-radius: 50%;
              cursor: pointer;
              appearance: none;
              overflow: hidden;

              background:
                radial-gradient(
                  circle at 34% 28%,
                  rgba(255,255,255,0.18) 0%,
                  rgba(255,255,255,0.05) 22%,
                  transparent 48%
                ),
                linear-gradient(
                  145deg,
                  #252a30 0%,
                  #161a1f 48%,
                  #0c0f13 100%
                );

              box-shadow:
                0 7px 12px rgba(0,0,0,0.55),
                0 2px 4px rgba(0,0,0,0.7),
                inset 0 2px 3px rgba(255,255,255,0.12),
                inset 0 -7px 10px rgba(0,0,0,0.65);

              transform-origin: center;
            }

            /* Recessed Xbox-style socket */
            .nexpad-btn::before {
              content: "";
              position: absolute;
              left: 7px;
              top: 7px;
              width: 82px;
              height: 82px;
              border-radius: 50%;
              z-index: 0;

              background:
                radial-gradient(
                  circle at 40% 34%,
                  rgba(255,255,255,0.08) 0%,
                  transparent 38%
                ),
                linear-gradient(
                  145deg,
                  #080a0d 0%,
                  #15191e 52%,
                  #272c32 100%
                );

              box-shadow:
                inset 0 5px 9px rgba(0,0,0,0.8),
                inset 0 -2px 4px rgba(255,255,255,0.08);
            }

            /* Simple green button cap */
            .face {
              position: absolute;
              left: 13px;
              top: 13px;
              width: 70px;
              height: 70px;
              border-radius: 50%;
              z-index: 2;

              background:
                radial-gradient(
                  circle at 34% 24%,
                  #9df04f 0%,
                  #69d52c 24%,
                  #4caf19 55%,
                  #2f7d10 100%
                );

              box-shadow:
                0 5px 8px rgba(0,0,0,0.5),
                inset 0 2px 3px rgba(255,255,255,0.28),
                inset 0 -7px 10px rgba(0,0,0,0.28);
            }

            /* Soft top specular */
            .specular {
              position: absolute;
              left: 23px;
              top: 18px;
              width: 40px;
              height: 15px;
              border-radius: 50%;
              z-index: 3;
              background: rgba(255,255,255,0.13);
              filter: blur(3px);
            }

            /* Xbox A label */
            .btn-label {
              position: absolute;
              left: 0;
              top: 0;
              width: 96px;
              height: 96px;
              z-index: 4;

              display: flex;
              align-items: center;
              justify-content: center;

              color: #ffffff;
              font-family: Arial, Helvetica, sans-serif;
              font-size: 34px;
              font-weight: 700;
              line-height: 1;
              letter-spacing: -1px;
              text-shadow:
                0 2px 1px rgba(0,0,0,0.75),
                0 3px 5px rgba(0,0,0,0.5);
              pointer-events: none;
            }

            /* Physical press */
            .nexpad-btn:active {
              transform: scale(var(--press-scale)) translateY(2px);

              box-shadow:
                0 3px 6px rgba(0,0,0,0.65),
                inset 0 5px 8px rgba(0,0,0,0.55),
                inset 0 -2px 4px rgba(255,255,255,0.05);
            }

            .nexpad-btn:active .face {
              box-shadow:
                0 2px 4px rgba(0,0,0,0.55),
                inset 0 5px 9px rgba(0,0,0,0.35),
                inset 0 -2px 3px rgba(255,255,255,0.12);
            }

            .nexpad-btn:active .specular {
              opacity: 0.55;
            }
            </style>
            </head>

            <body>

            <button
              class="nexpad-btn"
              data-control="A"
              data-category="BUTTON"
              data-name="Xbox Simple A"
              data-seed="46090"
            >
              <span class="face"></span>
              <span class="specular"></span>
              <span class="btn-label">A</span>
            </button>

            </body>
            </html>
        """.trimIndent()

        // 1. Test Component Detection
        val detected = NxprcComponentDetector.detect(
            html = userHtml,
            fallbackCategory = "BUTTON",
            fallbackControl = "A",
            fallbackId = "rc.action_a",
            fallbackName = "Action A"
        )

        assertEquals("A", detected.defaultControl)
        assertEquals("BUTTON", detected.category)
        assertEquals(46090L, detected.seed)
        assertEquals("Xbox Simple A", detected.componentName)
        assertEquals("rc.a_46090", detected.componentId)
        assertEquals(96, detected.widthDp)
        assertEquals(96, detected.heightDp)

        // 2. Test Compilation to NxprcDocument
        val doc = com.sanket.tools.nexpaddesktop.plugins.NxprcHtmlCssConverter.convert(
            source = userHtml,
            id = detected.componentId,
            name = detected.componentName,
            category = detected.category,
            defaultControl = detected.defaultControl
        )

        assertEquals("rc.a_46090", doc.manifest.id)
        assertEquals("Xbox Simple A", doc.manifest.name)
        assertEquals(46090L, doc.manifest.seed)
        assertEquals(96, doc.manifest.widthDp)
        assertEquals(96, doc.manifest.heightDp)

        // Verify spring physics parsed from :root variables
        assertEquals(0.68f, doc.manifest.springPhysics.dampingRatio, 0.01f)
        assertEquals(440f, doc.manifest.springPhysics.stiffness, 0.01f)
        assertEquals(0.92f, doc.manifest.springPhysics.pressedScale, 0.01f)

        // Verify layers generated
        assertTrue("Must have multiple layers", doc.canvas.layers.size >= 4)
        println("Generated ${doc.canvas.layers.size} layers for Seed 46090:")
        doc.canvas.layers.forEachIndexed { i, layer ->
            println("  Layer $i: $layer")
        }
    }

    @Test
    fun testMultipleRerollsChangeColors() {
        val userHtml = """
            <!DOCTYPE html>
            <html lang="en">
            <head>
            <meta charset="UTF-8">
            <style>
            :root {
              --spring-damping: 0.68;
              --spring-stiffness: 440;
              --press-scale: 0.92;
              --xbox-green: #5dc21e;
              --xbox-green-dark: #35850f;
              --xbox-black: #111418;
            }

            * {
              box-sizing: border-box;
            }

            body {
              margin: 0;
              width: 96px;
              height: 96px;
              overflow: hidden;
              background: transparent;
            }

            .nexpad-btn {
              position: relative;
              width: 96px;
              height: 96px;
              padding: 0;
              border: 0;
              border-radius: 50%;
              cursor: pointer;
              appearance: none;
              overflow: hidden;

              background:
                radial-gradient(
                  circle at 34% 28%,
                  rgba(255,255,255,0.18) 0%,
                  rgba(255,255,255,0.05) 22%,
                  transparent 48%
                ),
                linear-gradient(
                  145deg,
                  #252a30 0%,
                  #161a1f 48%,
                  #0c0f13 100%
                );

              box-shadow:
                0 7px 12px rgba(0,0,0,0.55),
                0 2px 4px rgba(0,0,0,0.7),
                inset 0 2px 3px rgba(255,255,255,0.12),
                inset 0 -7px 10px rgba(0,0,0,0.65);

              transform-origin: center;
            }

            /* Recessed Xbox-style socket */
            .nexpad-btn::before {
              content: "";
              position: absolute;
              left: 7px;
              top: 7px;
              width: 82px;
              height: 82px;
              border-radius: 50%;
              z-index: 0;

              background:
                radial-gradient(
                  circle at 40% 34%,
                  rgba(255,255,255,0.08) 0%,
                  transparent 38%
                ),
                linear-gradient(
                  145deg,
                  #080a0d 0%,
                  #15191e 52%,
                  #272c32 100%
                );

              box-shadow:
                inset 0 5px 9px rgba(0,0,0,0.8),
                inset 0 -2px 4px rgba(255,255,255,0.08);
            }

            /* Simple green button cap */
            .face {
              position: absolute;
              left: 13px;
              top: 13px;
              width: 70px;
              height: 70px;
              border-radius: 50%;
              z-index: 2;

              background:
                radial-gradient(
                  circle at 34% 24%,
                  #9df04f 0%,
                  #69d52c 24%,
                  #4caf19 55%,
                  #2f7d10 100%
                );

              box-shadow:
                0 5px 8px rgba(0,0,0,0.5),
                inset 0 2px 3px rgba(255,255,255,0.28),
                inset 0 -7px 10px rgba(0,0,0,0.28);
            }

            /* Soft top specular */
            .specular {
              position: absolute;
              left: 23px;
              top: 18px;
              width: 40px;
              height: 15px;
              border-radius: 50%;
              z-index: 3;
              background: rgba(255,255,255,0.13);
              filter: blur(3px);
            }

            /* Xbox A label */
            .btn-label {
              position: absolute;
              left: 0;
              top: 0;
              width: 96px;
              height: 96px;
              z-index: 4;

              display: flex;
              align-items: center;
              justify-content: center;

              color: #ffffff;
              font-family: Arial, Helvetica, sans-serif;
              font-size: 34px;
              font-weight: 700;
              line-height: 1;
              letter-spacing: -1px;
              text-shadow:
                0 2px 1px rgba(0,0,0,0.75),
                0 3px 5px rgba(0,0,0,0.5);
              pointer-events: none;
            }

            /* Physical press */
            .nexpad-btn:active {
              transform: scale(var(--press-scale)) translateY(2px);

              box-shadow:
                0 3px 6px rgba(0,0,0,0.65),
                inset 0 5px 8px rgba(0,0,0,0.55),
                inset 0 -2px 4px rgba(255,255,255,0.05);
            }

            .nexpad-btn:active .face {
              box-shadow:
                0 2px 4px rgba(0,0,0,0.55),
                inset 0 5px 9px rgba(0,0,0,0.35),
                inset 0 -2px 3px rgba(255,255,255,0.12);
            }

            .nexpad-btn:active .specular {
              opacity: 0.55;
            }
            </style>
            </head>

            <body>

            <button
              class="nexpad-btn"
              data-control="A"
              data-category="BUTTON"
              data-name="Xbox Simple A"
              data-seed="46090"
            >
              <span class="face"></span>
              <span class="specular"></span>
              <span class="btn-label">A</span>
            </button>

            </body>
            </html>
        """.trimIndent()

        // Reroll 1: Seed 11111 (e.g. Electric Cyber Cyan, #00F0FF)
        val reroll1Html = com.sanket.tools.nexpaddesktop.plugins.NxprcSeedEngine.applySeedToHtml(
            currentHtml = userHtml,
            control = "A",
            category = "BUTTON",
            seed = 11111L
        )
        val profile1 = com.sanket.tools.nexpaddesktop.plugins.NxprcSeedEngine.resolve("A", "BUTTON", 11111L)
        println("Reroll 1 Palette: ${profile1.palette.name} (${profile1.palette.hexCode})")

        // Reroll 2: Seed 22222 (e.g. Radiant Solar Amber or another palette)
        val reroll2Html = com.sanket.tools.nexpaddesktop.plugins.NxprcSeedEngine.applySeedToHtml(
            currentHtml = reroll1Html,
            control = "A",
            category = "BUTTON",
            seed = 22222L
        )
        val profile2 = com.sanket.tools.nexpaddesktop.plugins.NxprcSeedEngine.resolve("A", "BUTTON", 22222L)
        println("Reroll 2 Palette: ${profile2.palette.name} (${profile2.palette.hexCode})")

        // Reroll 3: Seed 33333
        val reroll3Html = com.sanket.tools.nexpaddesktop.plugins.NxprcSeedEngine.applySeedToHtml(
            currentHtml = reroll2Html,
            control = "A",
            category = "BUTTON",
            seed = 33333L
        )
        val profile3 = com.sanket.tools.nexpaddesktop.plugins.NxprcSeedEngine.resolve("A", "BUTTON", 33333L)
        println("Reroll 3 Palette: ${profile3.palette.name} (${profile3.palette.hexCode})")

        assertNotEquals("Reroll 1 must be different from initial userHtml", userHtml, reroll1Html)
        assertNotEquals("Reroll 2 must be different from Reroll 1", reroll1Html, reroll2Html)
        assertNotEquals("Reroll 3 must be different from Reroll 2", reroll2Html, reroll3Html)

        // Check if colors changed in the compiled doc
        val doc1 = com.sanket.tools.nexpaddesktop.plugins.NxprcHtmlCssConverter.convert(
            source = reroll1Html,
            id = profile1.componentId,
            name = profile1.skinName,
            category = "BUTTON",
            defaultControl = "A"
        )
        val doc2 = com.sanket.tools.nexpaddesktop.plugins.NxprcHtmlCssConverter.convert(
            source = reroll2Html,
            id = profile2.componentId,
            name = profile2.skinName,
            category = "BUTTON",
            defaultControl = "A"
        )
        val doc3 = com.sanket.tools.nexpaddesktop.plugins.NxprcHtmlCssConverter.convert(
            source = reroll3Html,
            id = profile3.componentId,
            name = profile3.skinName,
            category = "BUTTON",
            defaultControl = "A"
        )

        println("Doc1 layers:")
        doc1.canvas.layers.forEachIndexed { i, layer -> println("  Doc1 Layer $i: $layer") }
        println("Doc2 layers:")
        doc2.canvas.layers.forEachIndexed { i, layer -> println("  Doc2 Layer $i: $layer") }
        println("Doc3 layers:")
        doc3.canvas.layers.forEachIndexed { i, layer -> println("  Doc3 Layer $i: $layer") }

        val face1 = doc1.canvas.layers[2]
        val face2 = doc2.canvas.layers[2]
        val face3 = doc3.canvas.layers[2]

        assertNotEquals("Face fill in Reroll 1 must differ from Reroll 2", face1, face2)
        assertNotEquals("Face fill in Reroll 2 must differ from Reroll 3", face2, face3)
    }

    @Test
    fun testCodenameAndStyleWithManualSeed() {
        // 1. Test Base Style Extraction
        assertEquals(
            "Simple Btn Style",
            com.sanket.tools.nexpaddesktop.plugins.NxprcSeedEngine.extractBaseStyle("Simple Btn Style A #25678", "A")
        )
        assertEquals(
            "Xbox Simple",
            com.sanket.tools.nexpaddesktop.plugins.NxprcSeedEngine.extractBaseStyle("Xbox Simple A", "A")
        )
        assertEquals(
            "",
            com.sanket.tools.nexpaddesktop.plugins.NxprcSeedEngine.extractBaseStyle("A #39747", "A")
        )

        // 2. Test formatComponentName
        assertEquals(
            "Simple Btn Style A #25678",
            com.sanket.tools.nexpaddesktop.plugins.NxprcSeedEngine.formatComponentName("Simple Btn Style", "A", 25678L)
        )
        // Changing only seed number preserves the style title!
        assertEquals(
            "Simple Btn Style A #39747",
            com.sanket.tools.nexpaddesktop.plugins.NxprcSeedEngine.formatComponentName("Simple Btn Style A #25678", "A", 39747L)
        )

        // 3. Test Detection with data-codename & data-seed
        val htmlWithCodename = """
            <!DOCTYPE html>
            <html>
            <head><style>.nexpad-btn { width: 96px; height: 96px; }</style></head>
            <body>
              <button
                class="nexpad-btn"
                data-control="A"
                data-category="BUTTON"
                data-codename="Simple Btn Style"
                data-name="Simple Btn Style A #25678"
                data-seed="25678"
                id="rc.a_25678"
              >
                <span>A</span>
              </button>
            </body>
            </html>
        """.trimIndent()

        val detected = com.sanket.tools.nexpaddesktop.plugins.NxprcComponentDetector.detect(
            html = htmlWithCodename,
            fallbackCategory = "BUTTON",
            fallbackControl = "A",
            fallbackId = "rc.action_a",
            fallbackName = "Action A"
        )

        assertEquals("A", detected.defaultControl)
        assertEquals("BUTTON", detected.category)
        assertEquals(25678L, detected.seed)
        assertEquals("Simple Btn Style A #25678", detected.componentName)
        assertEquals("rc.a_25678", detected.componentId)

        // 4. Test applySeedToHtml with manual seed change from 25678 to 39747
        val updatedHtml = com.sanket.tools.nexpaddesktop.plugins.NxprcSeedEngine.applySeedToHtml(
            currentHtml = htmlWithCodename,
            control = "A",
            category = "BUTTON",
            seed = 39747L
        )

        assertTrue("Must update data-seed to 39747", updatedHtml.contains("data-seed=\"39747\""))
        assertTrue("Must preserve codename", updatedHtml.contains("data-codename=\"Simple Btn Style\""))
        assertTrue("Must update data-name to 39747", updatedHtml.contains("data-name=\"Simple Btn Style A #39747\""))
        assertTrue("Must update id to rc.a_39747", updatedHtml.contains("id=\"rc.a_39747\""))
    }

    @Test
    fun testDecoupledSeedWorkflowAndResetLogic() {
        // User's provided HTML code with original seed 46090
        val originalHtmlCode = """
            <!DOCTYPE html>
            <html>
            <head>
            <style>
            :root {
              --xbox-green: #5dc21e;
            }
            .nexpad-btn {
              width: 96px;
              height: 96px;
              background: #252a30;
            }
            </style>
            </head>
            <body>
            <button
              class="nexpad-btn"
              data-control="A"
              data-category="BUTTON"
              data-name="Xbox Simple A"
              data-seed="46090"
            >
              <span>A</span>
            </button>
            </body>
            </html>
        """.trimIndent()

        // 1. Initial detection of user's HTML code
        val detected = NxprcComponentDetector.detect(
            html = originalHtmlCode,
            fallbackCategory = "BUTTON",
            fallbackControl = "A",
            fallbackId = "rc.a_001",
            fallbackName = "Action A"
        )
        assertEquals(46090L, detected.seed)
        assertEquals("Xbox Simple A", detected.componentName)

        // 2. Sandbox Reroll: Generate preview HTML for Sandbox without modifying originalHtmlCode
        val rerolledSeed = 98765L
        val sandboxPreviewHtml = com.sanket.tools.nexpaddesktop.plugins.NxprcSeedEngine.applySeedToHtml(
            currentHtml = originalHtmlCode,
            control = "A",
            category = "BUTTON",
            seed = rerolledSeed
        )

        // Verify the sandbox preview got the new seed and resolved name
        assertTrue(sandboxPreviewHtml.contains("data-seed=\"98765\""))
        assertTrue(sandboxPreviewHtml.contains("data-name=\"Xbox Simple A #98765\""))
        assertTrue(sandboxPreviewHtml.contains("id=\"rc.a_98765\""))

        // CRITICAL REQUIREMENT: Verify originalHtmlCode was NOT modified!
        assertTrue(originalHtmlCode.contains("data-seed=\"46090\""))
        assertFalse(originalHtmlCode.contains("data-seed=\"98765\""))

        // 3. Reset Button Action: Reads originalHtmlCode and restores preview to original code seed
        val detectedFromOriginal = NxprcComponentDetector.detect(
            html = originalHtmlCode,
            fallbackCategory = "BUTTON",
            fallbackControl = "A",
            fallbackId = "rc.a_001",
            fallbackName = "Action A"
        )
        val resetSeed = detectedFromOriginal.seed!!
        assertEquals(46090L, resetSeed)

        // 4. Direct Export & Push Action: Compiling for export/push uses the active sandbox seed
        // without ever modifying originalHtmlCode in the editor!
        val exportDetected = NxprcComponentDetector.detect(
            html = sandboxPreviewHtml,
            fallbackCategory = "BUTTON",
            fallbackControl = "A",
            fallbackId = "rc.a_001",
            fallbackName = "Action A"
        )
        val exportDoc = com.sanket.tools.nexpaddesktop.plugins.NxprcHtmlCssConverter.convert(
            source = sandboxPreviewHtml,
            id = exportDetected.componentId,
            name = exportDetected.componentName,
            category = exportDetected.category,
            defaultControl = exportDetected.defaultControl
        )

        assertEquals("rc.a_98765", exportDoc.manifest.id)
        assertEquals("Xbox Simple A #98765", exportDoc.manifest.name)
        assertEquals("BUTTON", exportDoc.manifest.category)
        assertEquals("A", exportDoc.manifest.defaultControl)

        // Original HTML remains completely untouched
        assertTrue("originalHtmlCode still contains 46090", originalHtmlCode.contains("data-seed=\"46090\""))
        assertFalse("originalHtmlCode never contains 98765", originalHtmlCode.contains("data-seed=\"98765\""))
    }

    @Test
    fun testButtonSwitchingPreservesOriginalColorsAndSeeds() {
        val presetA = com.sanket.tools.nexpaddesktop.plugins.NxprcPresets.PRESET_NEO_TACTILE_A
        val presetB = com.sanket.tools.nexpaddesktop.plugins.NxprcPresets.PRESET_NEO_TACTILE_B
        val presetX = com.sanket.tools.nexpaddesktop.plugins.NxprcPresets.PRESET_NEO_TACTILE_X
        val presetY = com.sanket.tools.nexpaddesktop.plugins.NxprcPresets.PRESET_NEO_TACTILE_Y

        // 1. Verify Button A preset has Emerald Green and canonical seed 16166
        val detectedA = NxprcComponentDetector.detect(presetA)
        assertEquals(16166L, detectedA.seed)
        assertEquals("A", detectedA.defaultControl)
        assertTrue(presetA.contains("--accent-core: #00FFA3;"))
        val profileA = com.sanket.tools.nexpaddesktop.plugins.NxprcSeedEngine.resolve("A", "BUTTON", detectedA.seed)
        assertEquals("#00FFA3", profileA.palette.hexCode)

        // 2. Verify Button B preset has Crimson Red and canonical seed 24892 (NOT overwritten by A's green)
        val detectedB = NxprcComponentDetector.detect(presetB)
        assertEquals(24892L, detectedB.seed)
        assertEquals("B", detectedB.defaultControl)
        assertTrue(presetB.contains("--accent-core: #FF2A6D;"))
        val profileB = com.sanket.tools.nexpaddesktop.plugins.NxprcSeedEngine.resolve("B", "BUTTON", detectedB.seed)
        assertEquals("#FF2A6D", profileB.palette.hexCode)

        // 3. Verify Button X preset has Cyber Cyan and canonical seed 38120
        val detectedX = NxprcComponentDetector.detect(presetX)
        assertEquals(38120L, detectedX.seed)
        assertEquals("X", detectedX.defaultControl)
        assertTrue(presetX.contains("--accent-core: #00E5FF;"))
        val profileX = com.sanket.tools.nexpaddesktop.plugins.NxprcSeedEngine.resolve("X", "BUTTON", detectedX.seed)
        assertEquals("#00E5FF", profileX.palette.hexCode)

        // 4. Verify Button Y preset has Solar Amber and canonical seed 49551
        val detectedY = NxprcComponentDetector.detect(presetY)
        assertEquals(49551L, detectedY.seed)
        assertEquals("Y", detectedY.defaultControl)
        assertTrue(presetY.contains("--accent-core: #FFD600;"))
        val profileY = com.sanket.tools.nexpaddesktop.plugins.NxprcSeedEngine.resolve("Y", "BUTTON", detectedY.seed)
        assertEquals("#FFD600", profileY.palette.hexCode)

        // 5. Verify Reroll on B creates dynamic preview without modifying original presetB
        val rerolledSeed = 77777L
        val sandboxB = com.sanket.tools.nexpaddesktop.plugins.NxprcSeedEngine.applySeedToHtml(
            currentHtml = presetB,
            control = "B",
            category = "BUTTON",
            seed = rerolledSeed
        )
        assertTrue(sandboxB.contains("data-seed=\"77777\""))
        assertTrue(presetB.contains("data-seed=\"24892\""))
        // Multi-layer styling of .btn-core is preserved (not destroyed by capSelectors)
        assertTrue(sandboxB.contains("linear-gradient(145deg, #221217 0%, #12060a 40%, #060203 100%)"))
    }

    @Test
    fun testTrueProceduralMathematicalColorsFromSeed() {
        val seed1 = 23564L
        val seed2 = 23563L

        val palette1 = com.sanket.tools.nexpaddesktop.plugins.NxprcSeedEngine.proceduralPaletteFromSeed(seed1)
        val palette2 = com.sanket.tools.nexpaddesktop.plugins.NxprcSeedEngine.proceduralPaletteFromSeed(seed2)

        // Verify each seed generates a distinct, mathematically calculated hex color
        assertNotEquals("Seeds 23564 and 23563 must generate distinct colors", palette1.hexCode, palette2.hexCode)
        assertNotEquals("Glow colors must be distinct", palette1.glowRgba, palette2.glowRgba)
        assertNotEquals("Gradients must be distinct", palette1.coreGradient, palette2.coreGradient)

        // Verify valid Hex and RGBA formats
        assertTrue("Palette 1 hex must match hex format", palette1.hexCode.matches(Regex("""#[0-9A-Fa-f]{6}""")))
        assertTrue("Palette 2 hex must match hex format", palette2.hexCode.matches(Regex("""#[0-9A-Fa-f]{6}""")))
        assertTrue("Palette 1 glow must match rgba format", palette1.glowRgba.startsWith("rgba(") && palette1.glowRgba.endsWith(", 0.65)"))
        assertTrue("Palette 1 gradient must have 3 stops", palette1.coreGradient.contains("0%") && palette1.coreGradient.contains("50%") && palette1.coreGradient.contains("100%"))

        // Verify name includes the hue degree
        assertTrue(palette1.name.contains("°"))
        assertTrue(palette2.name.contains("°"))

        // Verify deterministic reproducibility: same seed always generates the exact same color
        val palette1Again = com.sanket.tools.nexpaddesktop.plugins.NxprcSeedEngine.proceduralPaletteFromSeed(seed1)
        assertEquals(palette1.hexCode, palette1Again.hexCode)
        assertEquals(palette1.glowRgba, palette1Again.glowRgba)
        assertEquals(palette1.coreGradient, palette1Again.coreGradient)
    }
}

