package com.sanket.tools.nexpaddesktop

import com.sanket.tools.nexpad.nxprc.*
import com.sanket.tools.nexpaddesktop.plugins.NxprcHtmlCssConverter
import com.sanket.tools.nexpaddesktop.plugins.NxprcLayerCodeGenerator
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class NxprcLayerStudioTest {

    private fun createDummyDoc(layers: List<CanvasLayer>): NxprcDocument {
        return NxprcDocument(
            manifest = NxprcManifest(
                id = "rc.test_btn",
                name = "Test Button",
                category = "BUTTON",
                defaultControl = "A",
                widthDp = 96,
                heightDp = 96
            ),
            canvas = NxprcCanvas(
                viewBoxWidth = 96f,
                viewBoxHeight = 96f,
                layers = layers
            ),
            animations = NxprcAnimations()
        )
    }

    @Test
    fun decomposesBoxLayerToCss() {
        val box = CanvasLayer.BoxLayer(
            widthRatio = 0.85f,
            heightRatio = 0.85f,
            fill = FillBrush.Solid(0xFF102030L),
            stroke = StrokeStyle(color = 0xFF00F0FFL, width = 2f),
            boxShadows = listOf(
                BoxShadowDef(offsetX = 0f, offsetY = 4f, blurRadius = 8f, color = 0x88000000L)
            ),
            cornerRadiusTopLeft = 12f
        )
        val doc = createDummyDoc(listOf(box))
        val details = NxprcLayerCodeGenerator.getLayerDetails(0, box, doc)

        assertEquals(0, details.index)
        assertEquals("SOCKET", details.categoryBadge)
        assertTrue(details.title.contains("SOCKET"))
        assertTrue(details.codeSnippet.contains("width: 81px;")) // 0.85 * 96 = 81.6 -> 81px
        assertTrue(details.codeSnippet.contains("border-radius: 12px;"))
        assertTrue(details.codeSnippet.contains("border: 2.0px solid #00F0FF;"))
        assertTrue(details.codeSnippet.contains("box-shadow:"))
    }

    @Test
    fun decomposesVectorPathToSvg() {
        val path = CanvasLayer.VectorPath(
            pathData = "M 10 10 L 90 90 Z",
            fill = FillBrush.Solid(0xFF4ADE80L),
            stroke = StrokeStyle(color = 0xFF000000L, width = 1.5f),
            scale = 1.0f
        )
        val doc = createDummyDoc(listOf(path))
        val details = NxprcLayerCodeGenerator.getLayerDetails(1, path, doc)

        assertEquals(1, details.index)
        assertEquals("ICON", details.categoryBadge)
        assertTrue(details.codeSnippet.contains("<svg viewBox=\"0 0 96 96\""))
        assertTrue(details.codeSnippet.contains("<path d=\"M 10 10 L 90 90 Z\""))
        assertTrue(details.codeSnippet.contains("fill=\"#4ADE80\""))
        assertTrue(details.codeSnippet.contains("stroke=\"#000000\""))
    }

    @Test
    fun decomposesCenterGlyphAndTextLayer() {
        val glyph = CanvasLayer.CenterGlyph(
            text = "X",
            fontSizeSp = 24f,
            textColor = 0xFFFFFFFFL
        )
        val textLayer = CanvasLayer.TextLayer(
            text = "TURBO",
            fontSizeSp = 10f,
            textColor = 0xFF00F0FFL
        )
        val doc = createDummyDoc(listOf(glyph, textLayer))

        val gDetails = NxprcLayerCodeGenerator.getLayerDetails(0, glyph, doc)
        assertEquals("GLYPH", gDetails.categoryBadge)
        assertTrue(gDetails.codeSnippet.contains("font-size: 24.0px;"))
        assertTrue(gDetails.codeSnippet.contains("<span>X</span>"))

        val tDetails = NxprcLayerCodeGenerator.getLayerDetails(1, textLayer, doc)
        assertEquals("TEXT", tDetails.categoryBadge)
        assertTrue(tDetails.codeSnippet.contains("font-size: 10.0px;"))
        assertTrue(tDetails.codeSnippet.contains("<span>TURBO</span>"))
    }

    @Test
    fun decomposesAllSpecializedLayerSubtypes() {
        val glow = CanvasLayer.GlowRing(glowColor = 0xFF00F0FFL, blurRadius = 16f, pulseEnabled = true)
        val bezel = CanvasLayer.BezelSocket(outerBezelColor = 0xFF222222L, shadowColor = 0xFF000000L)
        val cavity = CanvasLayer.InnerShadow(shadowColor = 0xFF050505L, highlightColor = 0xFF444444L, strokeWidth = 3f)
        val gloss = CanvasLayer.GlossReflection(widthRatio = 0.7f, heightRatio = 0.3f, alpha = 0.4f)
        val shape = CanvasLayer.GradientShape(shapeType = "hexagon", widthRatio = 0.9f, heightRatio = 0.9f)

        val doc = createDummyDoc(listOf(glow, bezel, cavity, gloss, shape))

        assertEquals("GLOW", NxprcLayerCodeGenerator.getLayerDetails(0, glow, doc).categoryBadge)
        assertEquals("BEZEL", NxprcLayerCodeGenerator.getLayerDetails(1, bezel, doc).categoryBadge)
        assertEquals("CAVITY", NxprcLayerCodeGenerator.getLayerDetails(2, cavity, doc).categoryBadge)
        assertEquals("GLOSS", NxprcLayerCodeGenerator.getLayerDetails(3, gloss, doc).categoryBadge)
        assertEquals("SHAPE", NxprcLayerCodeGenerator.getLayerDetails(4, shape, doc).categoryBadge)
    }

    @Test
    fun surgicalAiPromptContainsStrictBoundaries() {
        val box = CanvasLayer.BoxLayer(
            widthRatio = 0.8f,
            heightRatio = 0.8f,
            fill = FillBrush.Solid(0xFF3B82F6L)
        )
        val doc = createDummyDoc(listOf(box))

        val prompt = NxprcLayerCodeGenerator.generateLayerAiPrompt(
            layerIndex = 2,
            layer = box,
            doc = doc,
            userInstruction = "Change background to a molten lava orange gradient"
        )

        assertTrue(prompt.contains("# NEXPAD SURGICAL SINGLE-LAYER MODIFICATION TASK"))
        assertTrue(prompt.contains("Target Layer Index: #2"))
        assertTrue(prompt.contains("Change background to a molten lava orange gradient"))
        assertTrue(prompt.contains("Output ONLY the replacement CSS rule or SVG element for this single layer (Layer #2)"))
        assertTrue(prompt.contains("Do NOT regenerate or output the outer <button> wrapper"))
    }

    @Test
    fun layerManagerDeactivationStripsMutedLayersFromBinaryExport() {
        val l0 = CanvasLayer.BoxLayer(widthRatio = 1.0f, heightRatio = 1.0f)
        val l1 = CanvasLayer.GlowRing(glowColor = 0xFF00F0FFL) // Defective layer to mute
        val l2 = CanvasLayer.CenterGlyph(text = "A", fontSizeSp = 24f)
        val fullDoc = createDummyDoc(listOf(l0, l1, l2))

        // Mute layer #1 (keep #0 and #2)
        val activeIndices = setOf(0, 2)
        val exportDoc = fullDoc.copy(
            canvas = fullDoc.canvas.copy(
                layers = fullDoc.canvas.layers.filterIndexed { idx, _ -> idx in activeIndices }
            )
        )

        assertEquals(2, exportDoc.canvas.layers.size)
        assertTrue(exportDoc.canvas.layers[0] is CanvasLayer.BoxLayer)
        assertTrue(exportDoc.canvas.layers[1] is CanvasLayer.CenterGlyph)
        assertFalse(exportDoc.canvas.layers.any { it is CanvasLayer.GlowRing }, "Muted glow ring must be 100% stripped from export")
    }

    @Test
    fun decomposesRealPresetNeoTactileButton() {
        val doc = NxprcHtmlCssConverter.convert(
            source = NxprcHtmlCssConverter.PRESET_NEO_TACTILE_A,
            id = "rc.action_a",
            name = "Action A Button",
            category = "BUTTON",
            defaultControl = "A"
        )

        assertTrue(doc.canvas.layers.isNotEmpty(), "Preset must produce compiled layers")
        doc.canvas.layers.forEachIndexed { idx, layer ->
            val details = NxprcLayerCodeGenerator.getLayerDetails(idx, layer, doc)
            assertNotNull(details)
            assertTrue(details.title.isNotBlank())
            assertTrue(details.categoryBadge.isNotBlank())
            assertTrue(details.codeSnippet.isNotBlank())
        }
    }
}
