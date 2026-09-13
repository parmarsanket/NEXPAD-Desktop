package com.sanket.tools.nexpaddesktop

import com.sanket.tools.nexpad.nxprc.NxprcDocument
import com.sanket.tools.nexpaddesktop.plugins.NxprcHtmlCssConverter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class NxprcSerializationTest {

    @Test
    fun testNxprcEncodeAndDecode() {
        val doc = NxprcHtmlCssConverter.convert(
            source = NxprcHtmlCssConverter.PRESET_CYBER_REACTOR,
            id = "rc.cyber_reactor_a",
            name = "Cyber Reactor A",
            category = "BUTTON",
            defaultControl = "A"
        )

        val bytes = NxprcDocument.encodeToBytes(doc)
        assertTrue("Encoded bytes should be greater than 10", bytes.size > 10)

        val decodedResult = NxprcDocument.decodeFromBytes(bytes)
        assertTrue("Decoded result should be success", decodedResult.isSuccess)

        val decodedDoc = decodedResult.getOrThrow()
        assertEquals("rc.cyber_reactor_a", decodedDoc.manifest.id)
        assertEquals("Cyber Reactor A", decodedDoc.manifest.name)
        assertEquals("BUTTON", decodedDoc.manifest.category)
        assertEquals("A", decodedDoc.manifest.defaultControl)
    }

    @Test
    fun testDecodeExistingDesktopFile() {
        val file = File("C:\\Users\\parma\\OneDrive\\Desktop\\cyber_reactor_a.nxprc")
        if (file.exists()) {
            val bytes = file.readBytes()
            val result = NxprcDocument.decodeFromBytes(bytes)
            assertTrue("Should decode existing file without error: ${result.exceptionOrNull()?.message}", result.isSuccess)
            val doc = result.getOrThrow()
            assertEquals("rc.cyber_reactor_a", doc.manifest.id)
            assertEquals("Cyber Reactor A", doc.manifest.name)
            println("Successfully decoded existing file! Layers count: ${doc.canvas.layers.size}")
        }
    }

    @Test
    fun testGenerateAndExportSanketNxprc() {
        val doc = NxprcHtmlCssConverter.convert(
            source = NxprcHtmlCssConverter.PRESET_CYBER_REACTOR,
            id = "rc.sanket_btn_a",
            name = "Sanket Realistic A",
            category = "BUTTON",
            defaultControl = "A"
        )
        val bytes = NxprcDocument.encodeToBytes(doc)
        val outFile = File("C:\\Users\\parma\\OneDrive\\Desktop\\sanket.nxprc")
        outFile.writeBytes(bytes)
        assertTrue("sanket.nxprc must be written", outFile.exists())
        println("Generated sanket.nxprc (${bytes.size} bytes) with ${doc.canvas.layers.size} layers:")
        doc.canvas.layers.forEachIndexed { i, layer ->
            println("  Layer $i: ${layer::class.simpleName}")
        }

        // Also verify that it decodes back properly
        val decoded = NxprcDocument.decodeFromBytes(bytes).getOrThrow()
        assertEquals(doc.canvas.layers.size, decoded.canvas.layers.size)
    }

    @Test
    fun testUserSvgHtmlCompilation() {
        val userHtml = """
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<style>
  :root {
    --btn-width: 96px;
    --btn-height: 96px;
    --accent-core: #67e8f9;
    --accent-bright: #cffafe;
    --accent-dark: #155e75;
    --accent-glow: rgba(34, 211, 238, 0.55);
    --spring-damping: 0.68;
    --spring-stiffness: 440;
    --press-scale: 0.92;
  }
  .nexpad-btn {
    position: relative;
    width: var(--btn-width);
    height: var(--btn-height);
    border-radius: 22px;
    background: radial-gradient(circle at 30% 20%, rgba(255,255,255,0.22) 0%, transparent 34%), linear-gradient(145deg, #18343d 0%, #0c1d23 46%, #061116 100%);
    box-shadow: 0 10px 22px rgba(0,0,0,0.72), 0 0 18px var(--accent-glow);
    display: flex;
    align-items: center;
    justify-content: center;
  }
  .a-shape {
    position: absolute;
    left: 7px;
    top: 7px;
    width: 82px;
    height: 82px;
  }
  .a-glow {
    fill: rgba(34,211,238,0.22);
    opacity: 0.9;
  }
  .a-body {
    fill: url(#aCore);
    stroke: url(#aEdge);
    stroke-width: 1.7;
  }
  .a-inner {
    fill: none;
    stroke: rgba(207,250,254,0.42);
    stroke-width: 1.2;
  }
  .a-highlight {
    fill: url(#aHighlight);
    opacity: 0.72;
  }
  .a-shadow {
    fill: rgba(0,0,0,0.26);
    opacity: 0.9;
  }
  .a-label {
    font-size: 36px;
    font-weight: 900;
    color: #ffffff;
  }
  .nexpad-btn:active {
    transform: scale(0.93) translateY(3px);
  }
</style>
</head>
<body>
<button class="nexpad-btn" data-control="A" data-category="BUTTON" data-name="Crystal Mecha A">
  <svg class="a-shape" viewBox="0 0 82 82">
    <defs>
      <radialGradient id="aCore" cx="30%" cy="24%" r="78%">
        <stop offset="0%" stop-color="#cffafe" />
        <stop offset="100%" stop-color="#164e63" />
      </radialGradient>
      <linearGradient id="aEdge" x1="0" y1="0" x2="1" y2="1">
        <stop offset="0%" stop-color="#ecfeff" />
        <stop offset="100%" stop-color="#082f49" />
      </linearGradient>
      <linearGradient id="aHighlight" x1="0" y1="0" x2="1" y2="1">
        <stop offset="0%" stop-color="#ffffff" />
        <stop offset="100%" stop-color="#ffffff" stop-opacity="0" />
      </linearGradient>
    </defs>
    <path class="a-glow" d="M41 3 L65 10 L79 29 L74 53 L59 73 L36 79 L15 66 L4 45 L10 22 L25 7 Z" />
    <path class="a-body" d="M41 5 L63 12 L76 30 L71 51 L56 71 L35 76 L16 64 L7 44 L12 23 L26 9 Z" />
    <path class="a-shadow" d="M16 50 L35 76 L56 71 L71 51 L62 49 L51 63 L32 66 Z" />
    <path class="a-inner" d="M41 15 L57 20 L67 34 L63 49 L52 62 L36 66 L23 56 L17 43 L21 28 L31 19 Z" />
    <path class="a-highlight" d="M22 26 L31 17 L50 14 L62 20 L51 28 L35 34 L24 31 Z" />
    <path d="M39 25 L48 25 L43 35 L51 35 L37 52 L40 41 L32 41 Z" fill="#ffffff" fill-opacity="0.34" />
  </svg>
  <span class="a-label">A</span>
</button>
</body>
</html>
        """.trimIndent()

        val doc = NxprcHtmlCssConverter.convert(
            source = userHtml,
            id = "rc.crystal_mecha_a",
            name = "Crystal Mecha A",
            category = "BUTTON",
            defaultControl = "A"
        )

        println("=== COMPILED USER SVG BUTTON ===")
        println("Layers count: ${doc.canvas.layers.size}")
        doc.canvas.layers.forEachIndexed { i, layer ->
            when (layer) {
                is com.sanket.tools.nexpad.nxprc.CanvasLayer.VectorPath -> {
                    println("  Layer $i [VectorPath]: path='${layer.pathData.take(25)}...' fill=${layer.fill} stroke=${layer.stroke} scale=${layer.scale}")
                }
                is com.sanket.tools.nexpad.nxprc.CanvasLayer.CenterGlyph -> {
                    println("  Layer $i [CenterGlyph]: text='${layer.text}' fontSize=${layer.fontSizeSp} color=0x${java.lang.Long.toHexString(layer.textColor)}")
                }
                is com.sanket.tools.nexpad.nxprc.CanvasLayer.TextLayer -> {
                    println("  Layer $i [TextLayer]: text='${layer.text}' fontSize=${layer.fontSizeSp}")
                }
                is com.sanket.tools.nexpad.nxprc.CanvasLayer.BoxLayer -> {
                    println("  Layer $i [BoxLayer]: shape=${layer.shapeType} fills=${layer.fills.size} shadows=${layer.boxShadows.size}")
                }
                else -> {
                    println("  Layer $i [${layer::class.simpleName}]")
                }
            }
        }
        println("Spring Physics: damping=${doc.manifest.springPhysics.dampingRatio}, stiffness=${doc.manifest.springPhysics.stiffness}, pressedScale=${doc.manifest.springPhysics.pressedScale}")

        // Assert all 6 vector path layers are extracted with full styling fidelity
        val vectorLayers = doc.canvas.layers.filterIsInstance<com.sanket.tools.nexpad.nxprc.CanvasLayer.VectorPath>()
        assertEquals(6, vectorLayers.size)

        // Layer 0 in vectorLayers: .a-glow (solid non-zero fill)
        assertTrue(vectorLayers[0].fill is com.sanket.tools.nexpad.nxprc.FillBrush.Solid)
        val glowFill = vectorLayers[0].fill as com.sanket.tools.nexpad.nxprc.FillBrush.Solid
        assertTrue("Glow layer must have non-zero color", glowFill.color != 0L)

        // Layer 1 in vectorLayers: .a-body (radial gradient fill from #aCore, stroke from #aEdge)
        assertTrue("Body layer must resolve radialGradient from defs", vectorLayers[1].fill is com.sanket.tools.nexpad.nxprc.FillBrush.RadialGradient)
        val bodyFill = vectorLayers[1].fill as com.sanket.tools.nexpad.nxprc.FillBrush.RadialGradient
        assertEquals(2, bodyFill.colors.size)
        assertEquals(0.78f, bodyFill.radiusRatio, 0.01f)
        assertEquals(0.30f, bodyFill.centerXRatio, 0.01f)
        assertEquals(0.24f, bodyFill.centerYRatio, 0.01f)
        assertNotNull(vectorLayers[1].stroke)
        assertEquals(1.7f, vectorLayers[1].stroke!!.width, 0.01f)

        // Layer 2 in vectorLayers: .a-shadow (solid non-zero fill)
        assertTrue(vectorLayers[2].fill is com.sanket.tools.nexpad.nxprc.FillBrush.Solid)
        val shadowFill = vectorLayers[2].fill as com.sanket.tools.nexpad.nxprc.FillBrush.Solid
        assertTrue("Shadow layer must have non-zero color", shadowFill.color != 0L)

        // Layer 3 in vectorLayers: .a-inner (stroke with rgba, fill none)
        assertNotNull(vectorLayers[3].stroke)
        assertEquals(1.2f, vectorLayers[3].stroke!!.width, 0.01f)

        // Layer 4 in vectorLayers: .a-highlight (linear gradient fill from #aHighlight)
        assertTrue("Highlight layer must resolve linearGradient from defs", vectorLayers[4].fill is com.sanket.tools.nexpad.nxprc.FillBrush.LinearGradient)
        val highlightFill = vectorLayers[4].fill as com.sanket.tools.nexpad.nxprc.FillBrush.LinearGradient
        assertEquals(2, highlightFill.colors.size)
        assertEquals(135.0f, highlightFill.angleDegrees, 0.1f)

        // Layer 5 in vectorLayers: inline fill with opacity
        assertTrue(vectorLayers[5].fill is com.sanket.tools.nexpad.nxprc.FillBrush.Solid)
        val boltFill = vectorLayers[5].fill as com.sanket.tools.nexpad.nxprc.FillBrush.Solid
        assertTrue("Inline fill layer must have non-zero color", boltFill.color != 0L)

        // Glyph layer
        val glyphLayer = doc.canvas.layers.filterIsInstance<com.sanket.tools.nexpad.nxprc.CanvasLayer.CenterGlyph>().firstOrNull()
        assertNotNull(glyphLayer)
        assertEquals("A", glyphLayer!!.text)
        assertEquals(36.0f, glyphLayer.fontSizeSp, 0.1f)

        // Manifest spring physics
        assertEquals(0.68f, doc.manifest.springPhysics.dampingRatio, 0.01f)
        assertEquals(440.0f, doc.manifest.springPhysics.stiffness, 0.1f)
        assertEquals(0.92f, doc.manifest.springPhysics.pressedScale, 0.01f)

        // Binary Codec roundtrip verification
        val encodedBytes = NxprcDocument.encodeToBytes(doc)
        val decodedDoc = NxprcDocument.decodeFromBytes(encodedBytes).getOrThrow()
        assertEquals(doc.canvas.layers.size, decodedDoc.canvas.layers.size)
        assertEquals(6, decodedDoc.canvas.layers.filterIsInstance<com.sanket.tools.nexpad.nxprc.CanvasLayer.VectorPath>().size)
    }
}
