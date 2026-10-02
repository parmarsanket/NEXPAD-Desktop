package com.sanket.tools.nexpaddesktop

import com.sanket.tools.nexpad.nxprc.CanvasLayer
import com.sanket.tools.nexpad.nxprc.NxprcDocument
import com.sanket.tools.nexpaddesktop.plugins.NxprcHtmlCssConverter
import com.sanket.tools.nexpaddesktop.plugins.NxprcLayerCodeGenerator
import com.sanket.tools.nexpaddesktop.plugins.NxprcPresets
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

    @Test
    fun testUserExactCenterGlyphModification() {
        val neoTactileHtml = NxprcHtmlCssConverter.PRESET_NEO_TACTILE_A
        val initialDoc = NxprcHtmlCssConverter.convert(neoTactileHtml, "rc.action_a", "Action A")

        // Find the initial CenterGlyph layer
        val initialGlyph = initialDoc.canvas.layers.filterIsInstance<CanvasLayer.CenterGlyph>().firstOrNull()
        assertNotNull(initialGlyph, "PRESET_NEO_TACTILE_A must have a CenterGlyph")
        assertEquals(34.0f, initialGlyph.fontSizeSp)

        // Exact input user provided:
        val userModifiedCode = """
/* Layer #14: Center Glyph */
.layer-14-glyph {
  font-size: 50.0px;
  color: #FFFAFF;
  text-shadow: 0px 1px 0px rgba(255, 255, 255, 0.82), 0px -1px 0px rgba(92, 36, 122, 0.82), 0px 2px 5px rgba(55, 20, 89, 0.78), 0px 0px 8px rgba(255, 217, 255, 0.58);
}
<span>A</span>
""".trimIndent()

        val result = NxprcSurgicalReplacer.applySurgicalChange(
            originalHtml = neoTactileHtml,
            layerIndex = 14,
            layer = initialGlyph,
            doc = initialDoc,
            replacementInput = userModifiedCode
        )

        assertTrue(result.success, "Surgical change should succeed: ${result.message}")
        println("=== UPDATED HTML ===")
        println(result.updatedHtml)
        println("====================")
        assertTrue(result.updatedHtml.contains("font-size: 50.0px"))

        // Recompile and assert CenterGlyph was updated to 50.0f
        val updatedDoc = NxprcHtmlCssConverter.convert(result.updatedHtml, "rc.action_a", "Action A")
        val updatedGlyph = updatedDoc.canvas.layers.filterIsInstance<CanvasLayer.CenterGlyph>().firstOrNull()
        println("=== UPDATED GLYPH fontSizeSp: ${updatedGlyph?.fontSizeSp} ===")
        assertNotNull(updatedGlyph, "Updated doc must retain CenterGlyph")
        assertEquals(50.0f, updatedGlyph.fontSizeSp, "CenterGlyph font-size must be updated to 50.0sp!")
        assertEquals("A", updatedGlyph.text)
    }

    @Test
    fun testCenterGlyphTextChange() {
        val neoTactileHtml = NxprcHtmlCssConverter.PRESET_NEO_TACTILE_A
        val initialDoc = NxprcHtmlCssConverter.convert(neoTactileHtml, "rc.action_a", "Action A")
        val glyphLayer = initialDoc.canvas.layers.filterIsInstance<CanvasLayer.CenterGlyph>().first()

        val userTextChange = """
.layer-14-glyph {
  font-size: 44px;
}
<span>X</span>
""".trimIndent()

        val result = NxprcSurgicalReplacer.applySurgicalChange(
            originalHtml = neoTactileHtml,
            layerIndex = 14,
            layer = glyphLayer,
            doc = initialDoc,
            replacementInput = userTextChange
        )

        assertTrue(result.success)
        val updatedDoc = NxprcHtmlCssConverter.convert(result.updatedHtml, "rc.action_a", "Action A")
        val updatedGlyph = updatedDoc.canvas.layers.filterIsInstance<CanvasLayer.CenterGlyph>().first()
        assertEquals("X", updatedGlyph.text, "CenterGlyph text must update from 'A' to 'X'")
        assertEquals(44.0f, updatedGlyph.fontSizeSp)
    }

    @Test
    fun testApplyDirectEditSnippetOnAllLayersOfPreset() {
        val html = NxprcPresets.PRESET_NEO_TACTILE_A
        val initialDoc = NxprcHtmlCssConverter.convert(html, "rc.action_a", "Action A")
        for (i in initialDoc.canvas.layers.indices) {
            val layer = initialDoc.canvas.layers[i]
            val details = NxprcLayerCodeGenerator.getLayerDetails(i, layer, initialDoc)
            val modifiedSnippet = if (details.codeSnippet.contains("background:")) {
                details.codeSnippet.replace(Regex("""background:\s*[^;]+;"""), "background: #FF0055;")
            } else if (details.codeSnippet.contains("color:")) {
                details.codeSnippet.replace(Regex("""color:\s*[^;]+;"""), "color: #FF0055;")
            } else if (details.codeSnippet.contains("box-shadow:")) {
                details.codeSnippet.replace(Regex("""box-shadow:\s*[^;]+;"""), "box-shadow: 0 0 50px #FF0055;")
            } else {
                details.codeSnippet
            }

            val result = NxprcSurgicalReplacer.applySurgicalChange(
                originalHtml = html,
                layerIndex = i,
                layer = layer,
                doc = initialDoc,
                replacementInput = modifiedSnippet
            )
            println("Layer $i (${details.title}): success=${result.success}, msg=${result.message}")
            assertTrue(result.success, "Layer $i (${details.title}) surgical apply failed: ${result.message}")
            if (modifiedSnippet.contains("#FF0055")) {
                assertTrue(
                    result.updatedHtml.contains("#FF0055"),
                    "Layer $i (${details.title}) updatedHtml did not contain #FF0055!\nSnippet:\n$modifiedSnippet\nUpdated HTML:\n${result.updatedHtml}"
                )
            }
            val recompiled = NxprcHtmlCssConverter.convert(result.updatedHtml, "rc.action_a", "Action A")
            assertNotNull(recompiled, "Recompiled doc must not be null for layer $i")
        }
    }

    @Test
    fun testShinobiButtonSurgicalLayerUpdates() {
        val shinobiHtml = """
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="utf-8">
<style>
  :root {
    --spring-damping: 0.68;
    --spring-stiffness: 440;
    --press-scale: 0.92;
    --nx-glow: rgba(249, 115, 22, 0.65);
  }
  .nexpad-btn {
    position: relative;
    width: 96px;
    height: 96px;
  }
  .btn-base {
    position: absolute;
    inset: 0;
    border-radius: 50%;
    background: radial-gradient(circle at 50% 36%, #3a3f4d 0%, #20242c 55%, #13151b 100%);
    box-shadow: 0 14px 28px rgba(0, 0, 0, 0.78);
  }
  .btn-base::before {
    content: "";
    position: absolute;
    inset: 3px;
    border-radius: 50%;
    background: #2b303c;
  }
  .btn-base::after {
    content: "";
    position: absolute;
    inset: 7px;
    border-radius: 50%;
    background: #0a0b0e;
    box-shadow: 0 0 22px var(--nx-glow);
  }
  .btn-core {
    position: absolute;
    inset: 12px;
    border-radius: 50%;
    background: radial-gradient(circle at 45% 35%, #2a2e39 0%, #15171d 70%, #0d0e12 100%);
    border: 1px solid rgba(255, 255, 255, 0.12);
  }
  .btn-core::after {
    content: "";
    position: absolute;
    top: 5%;
    left: 14%;
    width: 72%;
    height: 38%;
    border-radius: 50%;
    background: radial-gradient(ellipse at 50% 30%, rgba(255, 255, 255, 0.55) 0%, transparent 70%);
  }
  .shuriken-emblem {
    position: absolute;
    width: 60px;
    height: 60px;
  }
  .btn-label {
    position: absolute;
    font-size: 34px;
    color: #ffffff;
  }
  .nexpad-btn:active {
    transform: scale(0.92) translateY(3px);
  }
</style>
</head>
<body>
  <button class="nexpad-btn" data-control="A" data-category="BUTTON" data-name="Shinobi Face Button A">
    <div class="btn-base"></div>
    <div class="btn-core"></div>
    <svg class="shuriken-emblem" viewBox="0 0 100 100">
      <path d="M 50 8 C 53 26 56 36 68 40 Z" fill="#fdba74"/>
    </svg>
    <span class="btn-label">A</span>
  </button>
</body>
</html>
""".trimIndent()

        val initialDoc = NxprcHtmlCssConverter.convert(shinobiHtml, "rc.shinobi_a", "Shinobi A")
        for (i in initialDoc.canvas.layers.indices) {
            val layer = initialDoc.canvas.layers[i]
            val details = NxprcLayerCodeGenerator.getLayerDetails(i, layer, initialDoc)
            val modifiedSnippet = if (details.codeSnippet.contains("background:")) {
                details.codeSnippet.replace(Regex("""background:\s*[^;]+;"""), "background: #00E5FF;")
            } else if (details.codeSnippet.contains("color:")) {
                details.codeSnippet.replace(Regex("""color:\s*[^;]+;"""), "color: #00E5FF;")
            } else if (details.codeSnippet.contains("fill=")) {
                details.codeSnippet.replace(Regex("""fill="[^"]+""""), "fill=\"#00E5FF\"")
            } else {
                details.codeSnippet
            }

            val result = NxprcSurgicalReplacer.applySurgicalChange(
                originalHtml = shinobiHtml,
                layerIndex = i,
                layer = layer,
                doc = initialDoc,
                replacementInput = modifiedSnippet
            )
            println("Shinobi Layer $i (${details.title}): success=${result.success}, msg=${result.message}")
            assertTrue(result.success, "Shinobi Layer $i (${details.title}) surgical apply failed: ${result.message}")
            if (modifiedSnippet.contains("#00E5FF")) {
                assertTrue(
                    result.updatedHtml.contains("#00E5FF"),
                    "Shinobi Layer $i (${details.title}) updatedHtml did not contain #00E5FF!\nSnippet:\n$modifiedSnippet\nUpdated HTML:\n${result.updatedHtml}"
                )
            }
            val recompiled = NxprcHtmlCssConverter.convert(result.updatedHtml, "rc.shinobi_a", "Shinobi A")
            assertNotNull(recompiled, "Recompiled doc must not be null for Shinobi layer $i")
        }
    }

    @Test
    fun testRgbButtonSurgicalDiffusionLayerUpdate() {
        val rgbHtml = """
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="utf-8">
<style>
:root {
  --spring-damping: 0.68;
  --spring-stiffness: 440;
  --press-scale: 0.94;

  --rgb-red: #ff3158;
  --rgb-green: #42ff8a;
  --rgb-blue: #3d8bff;
}

.nexpad-btn {
  position: relative;
  width: 96px;
  height: 96px;
  padding: 0;
  border: 0;
  border-radius: 50%;
  overflow: hidden;
  cursor: pointer;

  background:
    radial-gradient(
      circle at 32% 28%,
      rgba(255,255,255,0.38) 0%,
      rgba(255,255,255,0.08) 18%,
      transparent 35%
    ),
    radial-gradient(
      circle at 50% 45%,
      rgba(255,255,255,0.08) 0%,
      transparent 48%
    ),
    linear-gradient(
      145deg,
      #20252d 0%,
      #11151b 52%,
      #080a0e 100%
    );

  box-shadow:
    0 8px 18px rgba(0,0,0,0.65),
    0 2px 5px rgba(0,0,0,0.5),
    inset 0 2px 3px rgba(255,255,255,0.28),
    inset 0 -7px 12px rgba(0,0,0,0.75);

  transform-origin: center;
}

/* RGB illumination ring */
.nexpad-btn::before {
  content: "";
  position: absolute;
  left: 5px;
  top: 5px;
  width: 86px;
  height: 86px;
  border-radius: 50%;

  background:
    conic-gradient(
      from 0deg,
      var(--rgb-red),
      var(--rgb-green),
      var(--rgb-blue),
      var(--rgb-red)
    );

  box-shadow:
    0 0 5px rgba(255,255,255,0.35),
    0 0 14px rgba(65,145,255,0.32),
    inset 0 1px 2px rgba(255,255,255,0.4);

  animation: rgbRotate 5s linear infinite;
  opacity: 0.9;
}

/* Main circular button face */
.nexpad-btn::after {
  content: "";
  position: absolute;
  left: 10px;
  top: 10px;
  width: 76px;
  height: 76px;
  border-radius: 50%;

  background:
    radial-gradient(
      circle at 34% 26%,
      rgba(255,255,255,0.46) 0%,
      rgba(255,255,255,0.1) 15%,
      transparent 32%
    ),
    linear-gradient(
      145deg,
      #4d5560 0%,
      #252b34 35%,
      #11151c 72%,
      #090b10 100%
    );

  box-shadow:
    inset 0 3px 4px rgba(255,255,255,0.24),
    inset 0 -8px 10px rgba(0,0,0,0.72),
    0 2px 5px rgba(0,0,0,0.65);

  z-index: 2;
}

/* Real DOM label */
.btn-label {
  position: absolute;
  left: 0;
  top: 0;
  width: 96px;
  height: 96px;

  display: flex;
  align-items: center;
  justify-content: center;

  z-index: 3;

  font-family:
    system-ui,
    -apple-system,
    "Segoe UI",
    sans-serif;

  font-size: 30px;
  font-weight: 800;
  line-height: 1;

  color: #ffffff;

  text-shadow:
    0 1px 1px rgba(0,0,0,0.95),
    0 2px 4px rgba(0,0,0,0.8),
    0 0 8px rgba(255,255,255,0.18);
}

.nexpad-btn:active {
  transform: scale(var(--press-scale)) translateY(2px);
}
</style>
</head>
<body>
<button
  class="nexpad-btn"
  data-control="A"
  data-category="BUTTON"
  data-name="RGB A Button"
>
  <span class="btn-label">A</span>
</button>
</body>
</html>
""".trimIndent()

        val doc = NxprcHtmlCssConverter.convert(rgbHtml, "rc.rgb_a", "RGB A")

        // 1. Verify prompt generation includes full component source code context & target selector
        val prompt = NxprcLayerCodeGenerator.generateLayerAiPrompt(
            layerIndex = 1,
            layer = doc.canvas.layers[1],
            doc = doc,
            userInstruction = "make this color only green shaders",
            htmlSource = rgbHtml
        )
        assertTrue(prompt.contains("FULL COMPONENT SOURCE CODE"), "Prompt must contain full source code context")
        assertTrue(prompt.contains(".nexpad-btn::before"), "Prompt must identify .nexpad-btn::before as target selector")

        // 2. Apply the AI's green conic-gradient replacement for Layer 1 (.layer-1-DIFFUSION)
        val aiSnippet = """
.layer-1-DIFFUSION {
  position: absolute;
  width: 86px;
  height: 86px;
  border-radius: 43px;
  background: conic-gradient(
    from 0deg,
    #0B3D25,
    #16A05D,
    #42FF8A,
    #0F6B3C,
    #7AFFA8,
    #168A4D,
    #0B3D25
  );
  box-shadow:
    0px 0px 5px 0px rgba(74, 222, 128, 0.38),
    0px 0px 14px 0px rgba(34, 197, 94, 0.34),
    inset 0px 1px 2px 0px rgba(210, 255, 225, 0.42);
  opacity: 0.9;
}
""".trimIndent()

        val result = NxprcSurgicalReplacer.applySurgicalChange(
            originalHtml = rgbHtml,
            layerIndex = 1,
            layer = doc.canvas.layers[1],
            doc = doc,
            replacementInput = aiSnippet
        )

        assertTrue(result.success, "Surgical apply of green shaders failed: ${result.message}")
        assertTrue(result.updatedHtml.contains("#0B3D25"), "Updated HTML must contain new green gradient stop")
        assertTrue(result.updatedHtml.contains(".nexpad-btn::before"), "Updated HTML must preserve .nexpad-btn::before rule")

        // 3. Verify recompiled doc compiles cleanly and Layer 1 contains the new fills
        val recompiled = NxprcHtmlCssConverter.convert(result.updatedHtml, "rc.rgb_a", "RGB A")
        assertNotNull(recompiled, "Recompiled document must not be null")
        val updatedLayer = recompiled.canvas.layers[1] as CanvasLayer.BoxLayer
        println("Updated Layer 1 fills: ${updatedLayer.fills}")
    }
}

