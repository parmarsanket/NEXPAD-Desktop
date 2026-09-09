package com.sanket.tools.nexpaddesktop

import com.sanket.tools.nexpad.nxprc.*
import com.sanket.tools.nexpad.nxprc.engine.css.CssTokenizer
import com.sanket.tools.nexpad.nxprc.engine.dom.HtmlDomParser
import com.sanket.tools.nexpad.nxprc.engine.parsers.AnimationParser
import com.sanket.tools.nexpad.nxprc.engine.parsers.ColorParser
import com.sanket.tools.nexpad.nxprc.engine.parsers.GradientParser
import com.sanket.tools.nexpad.nxprc.engine.parsers.ShadowParser
import com.sanket.tools.nexpaddesktop.plugins.NxprcHtmlCssConverter
import org.junit.Assert.*
import org.junit.Test
import java.io.File

class WebEngineTest {

    @Test
    fun testCssTokenizerAndVariableResolution() {
        val css = """
            /* Industry grade test button */
            :root {
                --main-glow: #00f0ff;
                --base-bg: #10121a;
            }
            @keyframes pulseGlow {
                0% { opacity: 0.5; transform: scale(1); }
                50% { opacity: 1.0; transform: scale(1.05); }
                100% { opacity: 0.5; transform: scale(1); }
            }
            .cyber-btn {
                background: var(--base-bg);
                box-shadow: 0 0 15px var(--main-glow);
                border: 2px solid var(--main-glow);
            }
            .cyber-btn:active {
                transform: scale(0.92) translateY(2px);
            }
            .cyber-btn::before {
                width: 50%;
                height: 30%;
                transform: rotate(-15deg);
            }
        """.trimIndent()

        val stylesheet = CssTokenizer.parse(css)
        assertEquals(4, stylesheet.rules.size)
        assertTrue(stylesheet.keyframes.containsKey("pulseGlow"))
        assertEquals("#00f0ff", stylesheet.customProperties["--main-glow"])
        assertEquals("#10121a", stylesheet.customProperties["--base-bg"])

        val kf = stylesheet.keyframes["pulseGlow"]!!
        assertEquals(3, kf.steps.size)
        assertEquals(0.0f, kf.steps[0].percentage, 0.001f)
        assertEquals(0.5f, kf.steps[1].percentage, 0.001f)
        assertEquals(1.0f, kf.steps[2].percentage, 0.001f)
    }

    @Test
    fun testCssPropertyParsers() {
        // Colors
        assertEquals(0xFF00F0FFL, ColorParser.parse("#00f0ff"))
        assertEquals(0xFFFFFFFFL, ColorParser.parse("white"))
        assertEquals(0x73000000L, ColorParser.parse("rgba(0, 0, 0, 0.45)"))

        // Gradients
        val radial = GradientParser.parseFirst("radial-gradient(circle at 32% 24%, #d9ffd9 0%, #155c15 100%)")
        assertTrue(radial is FillBrush.RadialGradient)
        val rg = radial as FillBrush.RadialGradient
        assertEquals(0.32f, rg.centerXRatio, 0.01f)
        assertEquals(0.24f, rg.centerYRatio, 0.01f)
        assertEquals(2, rg.colors.size)

        // Box shadows
        val shadows = ShadowParser.parseBoxShadows("0 2px 4px rgba(255,255,255,0.18) inset, 0 8px 14px rgba(0,0,0,0.45)")
        assertEquals(2, shadows.size)
        assertTrue(shadows[0].isInset)
        assertFalse(shadows[1].isInset)
        assertEquals(2f, shadows[0].offsetY, 0.01f)
        assertEquals(8f, shadows[1].offsetY, 0.01f)

        // Transforms
        val transform = AnimationParser.parseTransforms("scale(0.94) translateY(2px) rotate(-18deg)")
        assertEquals(0.94f, transform.scaleX, 0.01f)
        assertEquals(2f, transform.translateY, 0.01f)
        assertEquals(-18f, transform.rotationDegrees, 0.01f)
    }

    @Test
    fun testHtmlDomParser() {
        val html = """
            <!DOCTYPE html>
            <html>
            <head>
                <style>.button-a { width: 80px; }</style>
            </head>
            <body>
                <div class="button-a" id="btn1">
                    <span>A</span>
                </div>
            </body>
            </html>
        """.trimIndent()

        val parsed = HtmlDomParser.parse(html)
        assertTrue(parsed.embeddedCss.contains(".button-a"))
        val buttonNode = parsed.root.findByTag("div")[0]
        assertEquals("button-a", buttonNode.classNames[0])
        assertEquals("btn1", buttonNode.id)
        assertEquals("A", buttonNode.findFirstText())
    }

    @Test
    fun testNxprcCompilerWithRealisticButton() {
        val realisticHtml = """
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <style>
                    .button-a {
                        width: 80px;
                        height: 80px;
                        border-radius: 50%;
                        background: radial-gradient(
                            circle at 32% 24%,
                            #d9ffd9 0%,
                            #9be99b 18%,
                            #52b952 48%,
                            #238423 78%,
                            #155c15 100%
                        );
                        border: 3px solid #292a30;
                        box-shadow:
                            0 2px 4px rgba(255,255,255,0.18) inset,
                            0 -7px 14px rgba(0,0,0,0.45) inset,
                            0 0 0 2px rgba(255,255,255,0.08),
                            0 0 0 5px rgba(0,0,0,0.30),
                            0 8px 14px rgba(0,0,0,0.45);
                    }
                    .button-a::before {
                        width: 55%;
                        height: 32%;
                        top: 7%;
                        left: 14%;
                        transform: rotate(-18deg);
                    }
                    .button-a::after {
                        box-shadow: inset 0 0 8px rgba(0,0,0,0.45), inset 0 2px 4px rgba(255,255,255,0.18);
                    }
                    .button-a span {
                        font-size: 39px;
                        color: #f5f5f5;
                        text-shadow: 0 3px 2px rgba(0,0,0,0.45), 0 1px 0 rgba(255,255,255,0.7);
                    }
                    .button-a:active {
                        transform: scale(0.94) translateY(2px);
                    }
                </style>
            </head>
            <body>
                <div class="button-a">
                    <span>A</span>
                </div>
            </body>
            </html>
        """.trimIndent()

        val doc = NxprcPackager.compile(
            html = realisticHtml,
            id = "rc.sanket_btn_a",
            name = "Sanket Realistic A",
            category = "BUTTON",
            defaultControl = "A"
        )

        assertEquals("rc.sanket_btn_a", doc.manifest.id)
        assertEquals(5, doc.canvas.layers.size)

        // Validate layers
        assertTrue(doc.canvas.layers[0] is CanvasLayer.BezelSocket)
        assertTrue(doc.canvas.layers[1] is CanvasLayer.GradientShape)
        assertTrue(doc.canvas.layers[2] is CanvasLayer.InnerShadow)
        assertTrue(doc.canvas.layers[3] is CanvasLayer.GlossReflection)
        assertTrue(doc.canvas.layers[4] is CanvasLayer.CenterGlyph)

        // Validate active animations
        assertEquals(0.94f, doc.animations.pressScale, 0.01f)
        assertEquals(2.0f, doc.animations.pressOffsetY, 0.01f)

        // Validate glyph text and shadows
        val glyph = doc.canvas.layers[4] as CanvasLayer.CenterGlyph
        assertEquals("A", glyph.text)
        assertEquals(39f, glyph.fontSizeSp, 0.01f)
        assertTrue(glyph.textShadows.isNotEmpty())

        // Validate binary serialization roundtrip
        val bytes = NxprcDocument.encodeToBytes(doc)
        val decodedResult = NxprcDocument.decodeFromBytes(bytes)
        assertTrue(decodedResult.isSuccess)
        val decoded = decodedResult.getOrThrow()
        assertEquals(5, decoded.canvas.layers.size)

        // Update Desktop export file
        val outFile = File("C:\\Users\\parma\\OneDrive\\Desktop\\sanket.nxprc")
        outFile.writeBytes(bytes)
        val dlFile = File("C:\\Users\\parma\\Downloads\\sanket.nxprc")
        dlFile.writeBytes(bytes)
        println("Generated and exported industry-grade sanket.nxprc (${bytes.size} bytes)")
    }

    @Test
    fun testUserNexpadAButtonCompilation() {
        val userHtml = """
            <!DOCTYPE html>
            <html lang="en">
            <head>
            <meta charset="UTF-8">

            <style>
            .nexpad-a {
                position: relative;
                width: 96px;
                height: 96px;
                border-radius: 50%;
                background:
                    radial-gradient(
                        circle at 30% 22%,
                        rgba(255,255,255,0.75) 0%,
                        rgba(255,255,255,0.20) 14%,
                        transparent 32%
                    ),
                    radial-gradient(
                        circle at 68% 76%,
                        rgba(0,0,0,0.32) 0%,
                        transparent 55%
                    ),
                    radial-gradient(
                        circle at 50% 45%,
                        #d8ffdf 0%,
                        #69d980 38%,
                        #35ac50 68%,
                        #12652b 100%
                    );
                border: 2px solid rgba(255,255,255,0.22);
                box-shadow:
                    0 18px 28px rgba(0,0,0,0.48),
                    0 0 0 3px rgba(0,0,0,0.42),
                    0 0 0 5px rgba(255,255,255,0.045),
                    inset 0 5px 10px rgba(255,255,255,0.28),
                    inset 0 -10px 18px rgba(0,0,0,0.38),
                    inset 5px 0 12px rgba(255,255,255,0.07),
                    inset -5px 0 12px rgba(0,0,0,0.10);
            }
            .nexpad-a::before {
                content: "";
                position: absolute;
                left: 6px; top: 6px; right: 6px; bottom: 6px;
                border-radius: 50%;
                background:
                    radial-gradient(ellipse at 32% 18%, rgba(255,255,255,0.52) 0%, rgba(255,255,255,0.16) 22%, transparent 45%),
                    radial-gradient(circle at 50% 45%, rgba(255,255,255,0.12), transparent 65%),
                    radial-gradient(ellipse at 50% 100%, rgba(0,0,0,0.20), transparent 62%);
                box-shadow: inset 0 0 10px rgba(255,255,255,0.18), inset 0 -7px 12px rgba(0,0,0,0.16);
            }
            .nexpad-a::after {
                content: "";
                position: absolute;
                left: 8px; top: 8px; right: 8px; bottom: 8px;
                border-radius: 50%;
                border-top: 1px solid rgba(255,255,255,0.48);
                border-left: 1px solid rgba(255,255,255,0.20);
                border-right: 1px solid rgba(255,255,255,0.06);
                border-bottom: 1px solid rgba(0,0,0,0.20);
            }
            .nexpad-a-label {
                position: absolute;
                left: 0; top: 0; right: 0; bottom: 0;
                display: flex; align-items: center; justify-content: center;
                z-index: 5;
                font-family: Arial, "Segoe UI", sans-serif;
                font-size: 44px;
                font-weight: 900;
                line-height: 1;
                color: rgba(255,255,255,0.97);
                letter-spacing: -2px;
                text-shadow:
                    0 -1px 0 rgba(255,255,255,0.95),
                    0 2px 0 rgba(255,255,255,0.20),
                    0 3px 3px rgba(0,0,0,0.32),
                    0 7px 10px rgba(0,0,0,0.28);
            }
            .nexpad-a-highlight {
                position: absolute;
                left: 25px; top: 13px;
                width: 30px; height: 11px;
                border-radius: 50%;
                background: radial-gradient(ellipse, rgba(255,255,255,0.82) 0%, rgba(255,255,255,0.32) 42%, transparent 75%);
                transform: rotate(-20deg);
                filter: blur(1px);
                z-index: 4;
            }
            .nexpad-a-reflection {
                position: absolute;
                right: 14px; bottom: 16px;
                width: 30px; height: 10px;
                border-radius: 50%;
                background: radial-gradient(ellipse, rgba(255,255,255,0.16), transparent 72%);
                transform: rotate(-20deg);
                filter: blur(2px);
                z-index: 3;
            }
            <button>
            </style>
            </head>
            <body>
            <div class="nexpad-a">
                <div class="nexpad-a-highlight"></div>
                <div class="nexpad-a-reflection"></div>
                <span class="nexpad-a-label">A</span>
            </div>
            </body>
            </html>
        """.trimIndent()

        val doc = NxprcPackager.compile(
            html = userHtml,
            id = "rc.nexpad_a",
            name = "Nexpad A Button",
            category = "BUTTON",
            defaultControl = "A"
        )

        println("Compiled doc layers count: " + doc.canvas.layers.size)
        doc.canvas.layers.forEachIndexed { i, layer ->
            println("Layer $i: ${layer::class.simpleName} -> $layer")
        }

        assertEquals("rc.nexpad_a", doc.manifest.id)
        assertEquals("Nexpad A Button", doc.manifest.name)
        assertTrue("Must have multiple layers", doc.canvas.layers.size >= 5)

        // Verify BezelSocket exists
        assertTrue(doc.canvas.layers.any { it is CanvasLayer.BezelSocket })

        // Verify GradientShape with green base exists
        val gradShapes = doc.canvas.layers.filterIsInstance<CanvasLayer.GradientShape>()
        assertTrue("Must have multiple gradient shapes", gradShapes.size >= 3)
        val greenGrad = gradShapes.firstOrNull { gs ->
            val fill = gs.fill
            fill is FillBrush.RadialGradient && fill.stops.isNotEmpty()
        }
        assertNotNull("Should have radial gradient with stops", greenGrad)

        // Verify InnerShadow exists
        assertTrue(doc.canvas.layers.any { it is CanvasLayer.InnerShadow })

        // Verify GlossReflections exist (child highlights)
        val glosses = doc.canvas.layers.filterIsInstance<CanvasLayer.GlossReflection>()
        assertTrue("Should have gloss reflections for child highlights", glosses.isNotEmpty())

        // Verify CenterGlyph has 4 text shadows
        val glyph = doc.canvas.layers.filterIsInstance<CanvasLayer.CenterGlyph>().firstOrNull()
        assertNotNull("Must have CenterGlyph", glyph)
        assertEquals("A", glyph!!.text)
        assertEquals(44f, glyph.fontSizeSp, 0.01f)
        assertEquals(4, glyph.textShadows.size)

        // Export directly to Desktop and Downloads as sanket2.nxprc
        val bytes = NxprcDocument.encodeToBytes(doc)
        val outFile = File("C:\\Users\\parma\\OneDrive\\Desktop\\sanket2.nxprc")
        outFile.writeBytes(bytes)
        val dlFile = File("C:\\Users\\parma\\Downloads\\sanket2.nxprc")
        dlFile.writeBytes(bytes)
        println("Exported updated sanket2.nxprc (${bytes.size} bytes)")

        // Verify decode
        val decoded = NxprcDocument.decodeFromBytes(bytes).getOrThrow()
        assertEquals(doc.canvas.layers.size, decoded.canvas.layers.size)
    }

    @Test
    fun testNeoTactileAbxyButtonCompilationAndTransforms() {
        // 1. Stress-test arbitrary CSS transforms parser
        val chained = AnimationParser.parseTransforms("translateY(3px) translateX(-4px) scale(0.93) rotate(-10deg)")
        assertEquals(0.93f, chained.scaleX, 0.001f)
        assertEquals(0.93f, chained.scaleY, 0.001f)
        assertEquals(3f, chained.translateY, 0.001f)
        assertEquals(-4f, chained.translateX, 0.001f)
        assertEquals(-10f, chained.rotationDegrees, 0.001f)

        val turnTransform = AnimationParser.parseTransforms("rotate(0.25turn) scale(1.1, 0.9)")
        assertEquals(90f, turnTransform.rotationDegrees, 0.001f)
        assertEquals(1.1f, turnTransform.scaleX, 0.001f)
        assertEquals(0.9f, turnTransform.scaleY, 0.001f)

        // 2. Stress-test exact DOM hierarchy compilation:
        // Button -> ::before -> ::after -> .btn-core -> .btn-label
        val doc = NxprcPackager.compile(
            html = NxprcHtmlCssConverter.PRESET_NEO_TACTILE_A,
            id = "rc.neo_abxy_a",
            name = "Neo Tactile A",
            category = "BUTTON",
            defaultControl = "A"
        )

        println("Neo Tactile A layers count: ${doc.canvas.layers.size}")
        doc.canvas.layers.forEachIndexed { i, layer ->
            println("  Layer $i: ${layer::class.simpleName} -> $layer")
        }

        // Validate manifest & active dynamics
        assertEquals("rc.neo_abxy_a", doc.manifest.id)
        assertEquals("Neo Tactile A", doc.manifest.name)
        assertEquals(0.93f, doc.animations.pressScale, 0.01f)
        assertEquals(3.0f, doc.animations.pressOffsetY, 0.01f)

        // Validate Outer Glow Ring
        assertTrue(doc.canvas.layers.any { it is CanvasLayer.GlowRing })

        // Validate Mechanical Bezel Socket
        assertTrue(doc.canvas.layers.any { it is CanvasLayer.BezelSocket })

        // Validate GradientShapes (Base + Conic metallic rim + Core multi-gradients)
        val gradShapes = doc.canvas.layers.filterIsInstance<CanvasLayer.GradientShape>()
        assertTrue("Must have at least 4 gradient shapes (base, ::before conic rim, core fills)", gradShapes.size >= 4)

        // Validate Conic Gradient parsed in ::before
        val sweep = gradShapes.firstOrNull { it.fill is FillBrush.SweepGradient }
        assertNotNull("Must contain SweepGradient/conic-gradient metallic rim", sweep)

        // Validate Inset Shadows
        val innerShadows = doc.canvas.layers.filterIsInstance<CanvasLayer.InnerShadow>()
        assertTrue("Must have multiple inner shadows (socket groove + core bevel)", innerShadows.size >= 2)

        // Validate Gloss Reflection (::after specular arc)
        val gloss = doc.canvas.layers.filterIsInstance<CanvasLayer.GlossReflection>().firstOrNull()
        assertNotNull("Must contain GlossReflection from ::after specular arc", gloss)
        assertEquals(-10f, gloss!!.rotationDegrees, 0.01f)

        // Validate CenterGlyph with multi-tier text shadows
        val glyph = doc.canvas.layers.filterIsInstance<CanvasLayer.CenterGlyph>().firstOrNull()
        assertNotNull("Must contain CenterGlyph for .btn-label", glyph)
        assertEquals("A", glyph!!.text)
        assertEquals(34f, glyph.fontSizeSp, 0.01f)
        assertEquals(4, glyph.textShadows.size)

        // Export directly to Desktop and Downloads as neo_abxy_a.nxprc
        val bytes = NxprcDocument.encodeToBytes(doc)
        val outFile = File("C:\\Users\\parma\\OneDrive\\Desktop\\neo_abxy_a.nxprc")
        outFile.writeBytes(bytes)
        val dlFile = File("C:\\Users\\parma\\Downloads\\neo_abxy_a.nxprc")
        dlFile.writeBytes(bytes)
        println("Exported verified neo_abxy_a.nxprc (${bytes.size} bytes, ${doc.canvas.layers.size} layers)")

        // Validate binary deserialization roundtrip
        val decoded = NxprcDocument.decodeFromBytes(bytes).getOrThrow()
        assertEquals(doc.canvas.layers.size, decoded.canvas.layers.size)
        assertEquals(doc.manifest.id, decoded.manifest.id)
    }
}
