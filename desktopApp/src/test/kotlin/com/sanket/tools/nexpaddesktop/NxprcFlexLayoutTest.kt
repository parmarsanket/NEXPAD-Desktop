package com.sanket.tools.nexpaddesktop

import com.sanket.tools.nexpad.nxprc.CanvasLayer
import com.sanket.tools.nexpad.nxprc.NxprcPackager
import com.sanket.tools.nexpaddesktop.plugins.NxprcHtmlCssConverter
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NxprcFlexLayoutTest {
    @Test
    fun bumperStarterMatchesBrowserGeometryAndLayers() {
        val doc = NxprcPackager.compile(
            NxprcHtmlCssConverter.PRESET_BUMPER_RB,
            "rc.bumper_rb",
            "Shoulder Bumper RB",
            "BUMPER",
            "RB"
        )
        val root = doc.canvas.layers.filterIsInstance<CanvasLayer.BoxLayer>().first { it.widthRatio == 1f }
        val sheen = doc.canvas.layers.filterIsInstance<CanvasLayer.BoxLayer>().first { it.widthRatio in 0.75f..0.77f }
        val glyph = doc.canvas.layers.filterIsInstance<CanvasLayer.CenterGlyph>().single()

        assertEquals(120f / 120f, root.widthRatio, 0.01f)
        assertEquals(52f / 52f, root.heightRatio, 0.01f)
        assertEquals("ROUNDED_RECT", root.shapeType)
        assertTrue(root.fills.isNotEmpty())
        assertTrue(root.boxShadows.any { it.isInset })
        assertTrue(root.boxShadows.any { !it.isInset })
        assertEquals(0.76f, sheen.widthRatio, 0.01f)
        assertEquals(0.35f, sheen.heightRatio, 0.01f)
        assertEquals(0.12f, sheen.offsetXRatio, 0.01f)
        assertEquals(0.10f, sheen.offsetYRatio, 0.01f)
        assertEquals(0.95f, doc.animations.pressScale, 0.01f)
        assertEquals(2f, doc.animations.pressOffsetY, 0.01f)
        assertEquals("RB", glyph.text)
    }

    @Test
    fun everyStarterCategoryCompilesThroughTheSamePipeline() {
        val starters = listOf(
            Triple("A", "BUTTON", "nexpad-btn"),
            Triple("UP", "DPAD", "dpad-btn"),
            Triple("LT", "TRIGGER", "trigger-btn"),
            Triple("LB", "BUMPER", "bumper-btn"),
            Triple("LS", "JOYSTICK", "stick-btn"),
            Triple("MENU", "SYSTEM", "system-btn")
        )

        starters.forEach { (control, category, rootClass) ->
            val source = NxprcHtmlCssConverter.getReferenceTemplate(control, category)
            assertTrue(source.contains("class=\"$rootClass\""), "Missing root class for $category")
            val doc = NxprcPackager.compile(source, "rc.$control", control, category, control)
            assertEquals(category, doc.manifest.category)
            assertEquals(control, doc.manifest.defaultControl)
            assertTrue(doc.canvas.layers.isNotEmpty(), "No layers for $category")
        }
    }

    @Test
    fun cssParityMatrixPreservesCoreVisualFeatures() {
        val html = """
            <!doctype html><html><head><style>
              .matrix-btn {
                width: 100px; height: 80px;
                border-radius: 10px 20px 30px 40px;
                background: linear-gradient(135deg, #101820 0%, #263b52 100%), radial-gradient(circle at 30% 20%, #ffffff 0%, transparent 60%);
                box-shadow: 0 8px 16px rgba(0,0,0,.6), inset 0 2px 4px rgba(255,255,255,.3);
                opacity: .82; transform: rotate(12deg) translateY(3px); transform-origin: 25% 75%;
                overflow: hidden;
              }
              .matrix-btn::before {
                content: ""; position: absolute; left: 10px; top: 5px; width: 30px; height: 20px;
                border-radius: 50%; background: radial-gradient(ellipse at center, #ffffff 0%, transparent 80%);
                opacity: .5; transform: rotate(-15deg); transform-origin: left top;
              }
              .matrix-btn::after {
                content: ""; position: absolute; left: 20px; top: 10px; width: 50px; height: 30px;
                border: 2px solid #4ade80; opacity: .7;
              }
              .matrix-btn span { color: #ffffff; font-size: 24px; text-shadow: 0 2px 4px #000; }
              .matrix-btn:active { transform: scale(.9) translateY(4px); }
            </style></head><body><button class="matrix-btn" data-control="A" data-category="BUTTON"><span>A</span></button></body></html>
        """.trimIndent()

        val doc = NxprcPackager.compile(html, "rc.css_matrix", "CSS Matrix", "BUTTON", "A")
        val root = doc.canvas.layers.filterIsInstance<CanvasLayer.BoxLayer>().first { it.widthRatio == 1f && it.heightRatio == 1f }
        val before = doc.canvas.layers.filterIsInstance<CanvasLayer.BoxLayer>().first { it.widthRatio in 0.29f..0.31f }
        val after = doc.canvas.layers.filterIsInstance<CanvasLayer.BoxLayer>().first { it.widthRatio in 0.49f..0.51f }
        val glyph = doc.canvas.layers.filterIsInstance<CanvasLayer.CenterGlyph>().single()

        assertEquals("ROUNDED_RECT", root.shapeType)
        assertEquals(0.82f, root.opacity, 0.01f)
        assertEquals(12f, root.rotationDegrees, 0.01f)
        assertEquals(0.25f, root.originXRatio, 0.01f)
        assertEquals(0.75f, root.originYRatio, 0.01f)
        assertTrue(root.fills.size >= 2)
        assertTrue(root.boxShadows.any { it.isInset })
        assertTrue(root.boxShadows.any { !it.isInset })
        assertTrue(root.clipToBounds)
        assertEquals(0.5f, before.opacity, 0.01f)
        assertEquals(-15f, before.rotationDegrees, 0.01f)
        assertEquals(2f, after.stroke!!.width, 0.01f)
        assertEquals("A", glyph.text)
        assertEquals(0.9f, doc.animations.pressScale, 0.01f)
        assertEquals(4f, doc.animations.pressOffsetY, 0.01f)
    }

    @Test
    fun centersLsThumbstickLayersLikeBrowserPreview() {
        val doc = NxprcPackager.compile(
            NxprcHtmlCssConverter.PRESET_THUMBSTICK_LS,
            "rc.stick_ls",
            "Analog Stick LS",
            "JOYSTICK",
            "LS"
        )
        val boxes = doc.canvas.layers.filterIsInstance<CanvasLayer.BoxLayer>()
        val innerGimbal = boxes.first { it.widthRatio in 0.64f..0.68f }
        val gripRing = boxes.first { it.widthRatio in 0.42f..0.46f }

        assertEquals(17f / 100f, innerGimbal.offsetXRatio, 0.01f)
        assertEquals(17f / 100f, innerGimbal.offsetYRatio, 0.01f)
        assertEquals(28f / 100f, gripRing.offsetXRatio, 0.01f)
        assertEquals(28f / 100f, gripRing.offsetYRatio, 0.01f)
    }

    @Test
    fun centersNestedOctagonalCoreLikeBrowserPreview() {
        val html = """
            <!doctype html>
            <html><head><style>
              .nexpad-btn {
                position: relative;
                width: 96px;
                height: 96px;
                display: flex;
                align-items: center;
                justify-content: center;
                background: #0d0f12;
              }
              .nexpad-btn .btn-core {
                position: relative;
                width: 62px;
                height: 62px;
                clip-path: polygon(28% 0%, 72% 0%, 100% 28%, 100% 72%, 72% 100%, 28% 100%, 0% 72%, 0% 28%);
                background: #4ade80;
              }
              .nexpad-btn .btn-core::before {
                content: "";
                position: absolute;
                inset: 2px;
                clip-path: polygon(28% 0%, 72% 0%, 100% 28%, 100% 72%, 72% 100%, 28% 100%, 0% 72%, 0% 28%);
                background: repeating-linear-gradient(-45deg, #111 0 3px, transparent 3px 6px);
              }
            </style></head>
            <body><button class="nexpad-btn" data-control="A" data-category="BUTTON"><div class="btn-core"><span>A</span></div></button></body>
            </html>
        """.trimIndent()

        val doc = NxprcPackager.compile(html, "rc.flex_octagon", "Flex Octagon", "BUTTON", "A")
        val core = doc.canvas.layers
            .filterIsInstance<CanvasLayer.BoxLayer>()
            .first { it.widthRatio < 0.8f }

        assertEquals("OCTAGON", core.shapeType)
        assertEquals(62f / 96f, core.widthRatio, 0.01f)
        assertEquals(17f / 96f, core.offsetXRatio, 0.01f)
        assertEquals(17f / 96f, core.offsetYRatio, 0.01f)
        assertTrue(doc.canvas.layers.any { it is CanvasLayer.CenterGlyph && it.text == "A" })
    }
}
