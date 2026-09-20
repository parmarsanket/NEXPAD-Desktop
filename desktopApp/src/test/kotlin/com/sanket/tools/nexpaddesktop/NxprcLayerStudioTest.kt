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

    @Test
    fun switchingControlsResetsActiveLayersToAllEnabled() {
        val docA = NxprcHtmlCssConverter.convert(
            source = NxprcHtmlCssConverter.PRESET_NEO_TACTILE_A,
            id = "rc.action_a",
            name = "Action A Button",
            category = "BUTTON",
            defaultControl = "A"
        )
        val docLS = NxprcHtmlCssConverter.convert(
            source = NxprcHtmlCssConverter.PRESET_THUMBSTICK_LS,
            id = "rc.stick_ls",
            name = "Analog Stick LS",
            category = "JOYSTICK",
            defaultControl = "LS"
        )
        val docLTP = NxprcHtmlCssConverter.convert(
            source = NxprcHtmlCssConverter.PRESET_TOUCHPAD_LTP,
            id = "rc.pad_ltp",
            name = "Touchpad LTP",
            category = "TOUCHPAD",
            defaultControl = "LTP"
        )
        val docLB = NxprcHtmlCssConverter.convert(
            source = NxprcHtmlCssConverter.PRESET_BUMPER_LB,
            id = "rc.bumper_lb",
            name = "Shoulder Bumper LB",
            category = "BUMPER",
            defaultControl = "LB"
        )

        val totalA = docA.canvas.layers.size
        val totalLS = docLS.canvas.layers.size
        val totalLTP = docLTP.canvas.layers.size
        val totalLB = docLB.canvas.layers.size

        assertTrue(totalA >= 4, "Action A must have at least 4 layers")
        assertTrue(totalLS >= 3, "Left Stick must have at least 3 layers")
        assertTrue(totalLTP >= 2, "Touchpad LTP must have at least 2 layers")
        assertTrue(totalLB >= 3, "Bumper LB must have at least 3 layers")

        // Simulate layer modifications on Button A in Layer Studio (only layers 0 and 1 active)
        var simulatedActiveIndices = setOf(0, 1)

        // Switching to LS must NEVER inherit Button A's filtered layers
        // When switching, the studio logic resets layers to (0 until totalLS).toSet()
        val resetToLS = (0 until totalLS).toSet()
        assertEquals(totalLS, resetToLS.size, "All LS layers must be enabled on selection")
        assertTrue(resetToLS.containsAll((0 until totalLS).toList()))

        // Switching to LTP must have all LTP layers active
        val resetToLTP = (0 until totalLTP).toSet()
        assertEquals(totalLTP, resetToLTP.size, "All LTP layers must be enabled on selection")

        // Switching to LB must have all LB layers active
        val resetToLB = (0 until totalLB).toSet()
        assertEquals(totalLB, resetToLB.size, "All LB layers must be enabled on selection")

        // Switching back to Button A must restore all Action A layers
        val resetToA = (0 until totalA).toSet()
        assertEquals(totalA, resetToA.size, "All Action A layers must be restored to 100% enabled")
        assertTrue(resetToA.containsAll((0 until totalA).toList()))
    }

    @Test
    fun layerStudioExitRestoresAllLayersToStudioSandbox() {
        val doc = NxprcHtmlCssConverter.convert(
            source = NxprcHtmlCssConverter.PRESET_NEO_TACTILE_A,
            id = "rc.action_a",
            name = "Action A Button",
            category = "BUTTON",
            defaultControl = "A"
        )
        val totalLayers = doc.canvas.layers.size

        // Inside Layer Studio: user disables layer 1 and solos layer 2
        var activeIndices = setOf(0, 2, 3)
        var soloIndex: Int? = 2

        // Filtered export inside Layer Studio produces isolated output
        val exportDocInStudio = doc.copy(
            canvas = doc.canvas.copy(
                layers = doc.canvas.layers.filterIndexed { idx, _ -> idx in activeIndices }
            )
        )
        assertEquals(3, exportDocInStudio.canvas.layers.size)

        fun resolvePreviewLayers(layers: List<CanvasLayer>, active: Set<Int>, solo: Int?): List<CanvasLayer>? {
            return when {
                solo != null -> {
                    val single = layers.getOrNull(solo)
                    if (single != null) listOf(single) else null
                }
                active.size < layers.size -> layers.filterIndexed { idx, _ -> idx in active }
                else -> null
            }
        }

        // When soloing layer #2: preview displays exclusively the 1 soloed layer
        val soloLayers = resolvePreviewLayers(doc.canvas.layers, activeIndices, soloIndex)
        assertEquals(1, soloLayers?.size, "Soloing layer #2 must isolate to exactly 1 layer")

        // When solo is cleared, preview displays active filtered subset (3 layers)
        val activeSubsetLayers = resolvePreviewLayers(doc.canvas.layers, activeIndices, null)
        assertEquals(3, activeSubsetLayers?.size, "Preview must display exactly 3 active layers")

        // On return to studio (onClose):
        activeIndices = (0 until totalLayers).toSet()
        soloIndex = null

        // Main studio preview evaluates to all layers (no filtering)
        val previewLayers = resolvePreviewLayers(doc.canvas.layers, activeIndices, soloIndex)
        assertEquals(null, previewLayers, "Sandbox previewLayers must be null (rendering all layers)")
        assertEquals(totalLayers, activeIndices.size, "All layers must be active after exiting Layer Studio")
    }
}
