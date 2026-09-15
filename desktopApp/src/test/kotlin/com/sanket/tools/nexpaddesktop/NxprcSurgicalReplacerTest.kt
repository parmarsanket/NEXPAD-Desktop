package com.sanket.tools.nexpaddesktop

import com.sanket.tools.nexpad.nxprc.CanvasLayer
import com.sanket.tools.nexpad.nxprc.NxprcDocument
import com.sanket.tools.nexpaddesktop.plugins.NxprcHtmlCssConverter
import com.sanket.tools.nexpaddesktop.plugins.NxprcSurgicalReplacer
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class NxprcSurgicalReplacerTest {

    private val sampleHtml = """
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <style>
        .nexpad-btn {
            position: relative;
            width: 96px;
            height: 96px;
            border-radius: 24px;
            background: linear-gradient(145deg, #1e293b, #0f172a);
            border: 2px solid #334155;
            box-shadow: 0 8px 16px rgba(0, 0, 0, 0.6);
        }
        .nexpad-btn::before {
            content: "";
            position: absolute;
            width: 60%;
            height: 35%;
            top: 8%;
            left: 20%;
            border-radius: 50%;
            background: radial-gradient(ellipse at center, rgba(255,255,255,0.4) 0%, transparent 70%);
            transform: rotate(-15deg);
        }
        .emblem-svg {
            position: absolute;
            width: 40px;
            height: 40px;
        }
    </style>
</head>
<body>
    <button class="nexpad-btn">
        <svg class="emblem-svg" viewBox="0 0 24 24">
            <path d="M12 2L2 7l10 5 10-5-10-5z" fill="#00FF99" />
        </svg>
    </button>
</body>
</html>
""".trimIndent()

    @Test
    fun testStripMarkdownFences() {
        val fencedCss = "```css\n.button { background: red; }\n```"
        assertEquals(".button { background: red; }", NxprcSurgicalReplacer.stripMarkdownFences(fencedCss))

        val fencedHtml = "```html\n<div>hello</div>\n```"
        assertEquals("<div>hello</div>", NxprcSurgicalReplacer.stripMarkdownFences(fencedHtml))

        val bare = ".btn { color: white; }"
        assertEquals(".btn { color: white; }", NxprcSurgicalReplacer.stripMarkdownFences(bare))
    }

    @Test
    fun testIsFullHtmlAndIsSvg() {
        assertTrue(NxprcSurgicalReplacer.isFullHtml(sampleHtml))
        assertFalse(NxprcSurgicalReplacer.isFullHtml(".btn { color: red; }"))

        assertTrue(NxprcSurgicalReplacer.isSvg("<svg><path d=\"M0 0\"/></svg>"))
        assertTrue(NxprcSurgicalReplacer.isSvg("<path d=\"M 10 10 L 90 90\" fill=\"cyan\"/>"))
        assertTrue(NxprcSurgicalReplacer.isSvg("<circle cx=\"12\" cy=\"12\" r=\"10\"/>"))
        assertFalse(NxprcSurgicalReplacer.isSvg("background: #00F0FF;"))
    }

    @Test
    fun testFindPrimaryButtonSelector() {
        val selector = NxprcSurgicalReplacer.findPrimaryButtonSelector(sampleHtml)
        assertEquals(".nexpad-btn", selector)
    }

    @Test
    fun testFullHtmlReplacement() {
        val doc = NxprcHtmlCssConverter.convert(sampleHtml, "rc.test", "Test")
        val newHtml = sampleHtml.replace("#00FF99", "#FF0055")

        val result = NxprcSurgicalReplacer.applySurgicalChange(
            originalHtml = sampleHtml,
            layerIndex = 0,
            layer = null,
            doc = doc,
            replacementInput = "```html\n$newHtml\n```"
        )

        assertTrue(result.success)
        assertTrue(result.updatedHtml.contains("#FF0055"))
    }

    @Test
    fun testSvgPathEmblemReplacement() {
        val doc = NxprcHtmlCssConverter.convert(sampleHtml, "rc.test", "Test")
        val newPath = "<path d=\"M12 2a9 9 0 0 0-9 9c0 3.1 1.6 5.8 4 7.4V21h8v-2.6\" fill=\"#00F0FF\" />"

        val result = NxprcSurgicalReplacer.applySurgicalChange(
            originalHtml = sampleHtml,
            layerIndex = 2,
            layer = CanvasLayer.VectorPath(pathData = "M12 2L2 7l10 5 10-5-10-5z"),
            doc = doc,
            replacementInput = newPath
        )

        assertTrue(result.success)
        assertTrue(result.updatedHtml.contains("M12 2a9 9 0 0 0-9 9c0 3.1 1.6 5.8 4 7.4V21h8v-2.6"))
        assertTrue(result.updatedHtml.contains("fill=\"#00F0FF\""))
        assertFalse(result.updatedHtml.contains("M12 2L2 7l10 5 10-5-10-5z")) // Old path removed

        // Ensure resulting HTML recompiles cleanly
        val updatedDoc = NxprcHtmlCssConverter.convert(result.updatedHtml, "rc.test", "Test")
        assertTrue(updatedDoc.canvas.layers.any { it is CanvasLayer.VectorPath })
    }

    @Test
    fun testSvgInjectionWhenNoSvgExists() {
        val htmlWithoutSvg = """
<!DOCTYPE html>
<html>
<head><style>.nexpad-btn { width: 80px; height: 80px; background: #222; }</style></head>
<body><button class="nexpad-btn"></button></body>
</html>
""".trimIndent()
        val doc = NxprcHtmlCssConverter.convert(htmlWithoutSvg, "rc.test", "Test")
        val circleSvg = "<circle cx=\"12\" cy=\"12\" r=\"8\" fill=\"#FF0055\" />"

        val result = NxprcSurgicalReplacer.applySurgicalChange(
            originalHtml = htmlWithoutSvg,
            layerIndex = 0,
            layer = null,
            doc = doc,
            replacementInput = circleSvg
        )

        assertTrue(result.success)
        assertTrue(result.updatedHtml.contains("<svg"))
        assertTrue(result.updatedHtml.contains("<circle cx=\"12\" cy=\"12\" r=\"8\" fill=\"#FF0055\" />"))
        assertTrue(result.updatedHtml.contains("</button>"))

        val recompiled = NxprcHtmlCssConverter.convert(result.updatedHtml, "rc.test", "Test")
        assertNotNull(recompiled)
    }

    @Test
    fun testSyntheticGlossLayerSelectorResolution() {
        val doc = NxprcHtmlCssConverter.convert(sampleHtml, "rc.test", "Test")
        val glossLayer = doc.canvas.layers.firstOrNull { it is CanvasLayer.GlossReflection }

        val aiGlossReplacement = """
/* Layer #1: Specular Gloss Arc (::before) */
.layer-1-gloss::before {
  width: 75%;
  height: 45%;
  top: 5%;
  left: 12%;
  border-radius: 50%;
  background: radial-gradient(ellipse at center, rgba(0, 240, 255, 0.75) 0%, transparent 80%);
}
""".trimIndent()

        val result = NxprcSurgicalReplacer.applySurgicalChange(
            originalHtml = sampleHtml,
            layerIndex = 1,
            layer = glossLayer,
            doc = doc,
            replacementInput = aiGlossReplacement
        )

        assertTrue(result.success)
        assertTrue(result.updatedHtml.contains(".nexpad-btn::before"))
        assertTrue(result.updatedHtml.contains("rgba(0, 240, 255, 0.75)"))

        val recompiled = NxprcHtmlCssConverter.convert(result.updatedHtml, "rc.test", "Test")
        assertNotNull(recompiled)
    }

    @Test
    fun testBareCssDeclarationsMergedIntoPrimaryButton() {
        val doc = NxprcHtmlCssConverter.convert(sampleHtml, "rc.test", "Test")
        val baseLayer = doc.canvas.layers[0]

        val bareDeclarations = """
background: linear-gradient(180deg, #FF0055 0%, #770022 100%);
border: 3px solid #00F0FF;
box-shadow: 0 0 25px #FF0055;
""".trimIndent()

        val result = NxprcSurgicalReplacer.applySurgicalChange(
            originalHtml = sampleHtml,
            layerIndex = 0,
            layer = baseLayer,
            doc = doc,
            replacementInput = bareDeclarations
        )

        assertTrue(result.success)
        // Check merged properties
        assertTrue(result.updatedHtml.contains("linear-gradient(180deg, #FF0055 0%, #770022 100%)"))
        assertTrue(result.updatedHtml.contains("border: 3px solid #00F0FF;"))
        assertTrue(result.updatedHtml.contains("box-shadow: 0 0 25px #FF0055;"))
        // Check preserved properties (width, height, border-radius were untouched)
        assertTrue(result.updatedHtml.contains("width: 96px;"))
        assertTrue(result.updatedHtml.contains("height: 96px;"))
        assertTrue(result.updatedHtml.contains("border-radius: 24px;"))

        val recompiled = NxprcHtmlCssConverter.convert(result.updatedHtml, "rc.test", "Test")
        assertNotNull(recompiled)
    }

    @Test
    fun testMergeDeclarationsHelper() {
        val existing = """
  width: 96px;
  height: 96px;
  border-radius: 20px;
  background: #111;
  border: 1px solid #333;
""".trimIndent()

        val incoming = """
background: #FF0000;
box-shadow: 0 4px 8px black;
""".trimIndent()

        val merged = NxprcSurgicalReplacer.mergeDeclarations(existing, incoming)

        assertTrue(merged.contains("width: 96px;"))
        assertTrue(merged.contains("height: 96px;"))
        assertTrue(merged.contains("border-radius: 20px;"))
        assertTrue(merged.contains("border: 1px solid #333;"))
        assertTrue(merged.contains("background: #FF0000;"))
        assertTrue(merged.contains("box-shadow: 0 4px 8px black;"))
        assertFalse(merged.contains("background: #111;"))
    }
}
