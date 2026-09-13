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

    @Test
    fun testCyberpunkButtonCompilation() {
        val cyberpunkHtml = """
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<style>
  :root {
    --btn-width: 96px;
    --btn-height: 96px;
    --cyan-glow: rgba(0, 240, 255, 0.65);
    --cyan-core: #00f0ff;
    --magenta-glow: rgba(255, 0, 85, 0.7);
    --magenta-core: #ff0055;
    --spring-damping: 0.68;
    --spring-stiffness: 440;
    --press-scale: 0.92;
  }

  .nexpad-btn {
    position: relative;
    width: var(--btn-width);
    height: var(--btn-height);
    clip-path: polygon(25% 0%, 75% 0%, 100% 25%, 100% 75%, 75% 100%, 25% 100%, 0% 75%, 0% 25%);
    background: 
      radial-gradient(circle at 35% 25%, rgba(0, 240, 255, 0.25) 0%, transparent 45%),
      radial-gradient(circle at 75% 75%, rgba(255, 0, 85, 0.2) 0%, transparent 50%),
      linear-gradient(145deg, #161a24 0%, #0d1017 48%, #06070a 100%);
    border: 2px solid #00f0ff;
    box-shadow: 
      0 10px 24px rgba(0, 0, 0, 0.8),
      0 0 20px var(--cyan-glow),
      inset 0 2px 4px rgba(255, 255, 255, 0.3),
      inset 0 -5px 12px rgba(0, 0, 0, 0.9);
    display: flex;
    align-items: center;
    justify-content: center;
  }

  .tech-bracket {
    position: absolute;
    left: 8px;
    top: 8px;
    width: 80px;
    height: 80px;
  }

  .tech-accent {
    fill: none;
    stroke: url(#cyberGrad);
    stroke-width: 1.8;
  }

  .circuit-tick {
    fill: #ff0055;
    opacity: 0.85;
  }

  .btn-label {
    position: relative;
    font-size: 38px;
    font-weight: 900;
    color: #ffffff;
    text-shadow: 
      0 0 10px var(--cyan-core),
      0 0 20px var(--cyan-glow),
      0 2px 4px rgba(0, 0, 0, 0.9);
    z-index: 5;
  }

  .nexpad-btn:active {
    transform: scale(0.92) translateY(2px);
  }
</style>
</head>
<body>
  <button class="nexpad-btn" data-control="A" data-category="BUTTON" data-name="Cyberpunk Neon A">
    <svg class="tech-bracket" viewBox="0 0 80 80">
      <defs>
        <linearGradient id="cyberGrad" x1="0" y1="0" x2="1" y2="1">
          <stop offset="0%" stop-color="#00f0ff" />
          <stop offset="100%" stop-color="#ff0055" />
        </linearGradient>
      </defs>
      <!-- Cyber corner framing brackets -->
      <path class="tech-accent" d="M22 6 L12 16 L12 26 M58 6 L68 16 L68 26 M12 54 L12 64 L22 74 M68 54 L68 64 L58 74" />
      <!-- High-tech circuit accent marks -->
      <polygon class="circuit-tick" points="37 8 43 8 41 12 39 12" />
      <polygon class="circuit-tick" points="37 72 43 72 41 68 39 68" />
    </svg>
    <span class="btn-label">A</span>
  </button>
</body>
</html>
        """.trimIndent()

        val doc = NxprcHtmlCssConverter.convert(
            source = cyberpunkHtml,
            id = "rc.cyberpunk_neon_a",
            name = "Cyberpunk Neon A",
            category = "BUTTON",
            defaultControl = "A"
        )

        println("=== COMPILED CYBERPUNK NEON A BUTTON ===")
        println("Total Layers count: ${doc.canvas.layers.size}")
        doc.canvas.layers.forEachIndexed { i, layer ->
            when (layer) {
                is com.sanket.tools.nexpad.nxprc.CanvasLayer.VectorPath -> {
                    println("  Layer $i [VectorPath]: path='${layer.pathData.take(30)}...' fill=${layer.fill} stroke=${layer.stroke}")
                }
                is com.sanket.tools.nexpad.nxprc.CanvasLayer.BoxLayer -> {
                    println("  Layer $i [BoxLayer]: shape=${layer.shapeType} fills=${layer.fills.size} shadows=${layer.boxShadows.size}")
                }
                is com.sanket.tools.nexpad.nxprc.CanvasLayer.CenterGlyph -> {
                    println("  Layer $i [CenterGlyph]: text='${layer.text}' fontSize=${layer.fontSizeSp} color=0x${java.lang.Long.toHexString(layer.textColor)}")
                }
                else -> println("  Layer $i [${layer::class.simpleName}]")
            }
        }
        val encodedBytes = NxprcDocument.encodeToBytes(doc)
        val decoded = NxprcDocument.decodeFromBytes(encodedBytes).getOrThrow()
        assertEquals(doc.canvas.layers.size, decoded.canvas.layers.size)
    }

    @Test
    fun testSpidermanButtonCompilation() {
        val spidermanHtml = """
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<style>
  :root {
    --btn-size: 96px;
    --mask-red-core: #e62424;
    --mask-red-shadow: #8a0b0b;
    --mask-red-highlight: #ff5e5e;
    --suit-blue-socket: #0b2242;
    --spring-damping: 0.65;
    --spring-stiffness: 420;
    --press-scale: 0.90;
  }

  .nexpad-btn {
    position: relative;
    width: var(--btn-size);
    height: var(--btn-size);
    border-radius: 50%;
    background: 
      radial-gradient(circle at 50% 15%, rgba(255, 255, 255, 0.15) 0%, transparent 35%),
      radial-gradient(circle at 50% 50%, #152d4f 0%, #081221 65%, #03070d 100%);
    border: none;
    box-shadow: 
      0 12px 24px rgba(0, 0, 0, 0.8),
      0 0 0 4px #06101d,
      0 0 16px rgba(230, 36, 36, 0.4),
      inset 0 3px 6px rgba(255, 255, 255, 0.15),
      inset 0 -8px 16px rgba(0, 0, 0, 0.9);
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    outline: none;
    padding: 0;
    transition: transform 0.1s cubic-bezier(0.2, 0.8, 0.2, 1);
  }

  .mask-core {
    position: relative;
    width: 74px;
    height: 78px;
    border-radius: 50% 50% 45% 45% / 40% 40% 60% 60%; /* Spiderman head silhouette */
    background: 
      radial-gradient(circle at 40% 25%, var(--mask-red-highlight) 0%, transparent 40%),
      linear-gradient(160deg, var(--mask-red-core) 0%, #bd1515 50%, var(--mask-red-shadow) 100%);
    box-shadow: 
      0 6px 12px rgba(0, 0, 0, 0.7),
      inset 0 2px 4px rgba(255, 255, 255, 0.5),
      inset 0 -4px 10px rgba(0, 0, 0, 0.6);
    overflow: hidden;
    display: flex;
    align-items: center;
    justify-content: center;
  }

  /* SVG Webbing Background */
  .webbing-layer {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    opacity: 0.65;
    pointer-events: none;
  }

  /* Spidey Eyes layer */
  .eyes-layer {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    pointer-events: none;
    z-index: 2;
  }

  /* Ambient mask gloss reflection */
  .mask-gloss {
    content: "";
    position: absolute;
    top: 2%;
    left: 15%;
    width: 70%;
    height: 45%;
    border-radius: 50% 50% 45% 45% / 50% 50% 30% 30%;
    background: linear-gradient(180deg, rgba(255, 255, 255, 0.4) 0%, rgba(255, 255, 255, 0.05) 50%, transparent 100%);
    pointer-events: none;
    z-index: 3;
  }

  /* Button Label (The 'A') */
  .btn-label {
    position: relative;
    z-index: 4;
    font-family: system-ui, -apple-system, sans-serif;
    font-size: 32px;
    font-weight: 900;
    color: #ffffff;
    margin-top: -10px; /* Shift slightly up between the eyes */
    text-shadow: 
      0 2px 0 #1a1a1a,
      0 -2px 0 #1a1a1a,
      2px 0 0 #1a1a1a,
      -2px 0 0 #1a1a1a,
      0 4px 6px rgba(0, 0, 0, 0.8),
      0 0 10px rgba(255, 255, 255, 0.5);
  }

  /* NEXPAD Active State Physics */
  .nexpad-btn:active {
    transform: scale(var(--press-scale)) translateY(4px);
    box-shadow: 
      0 4px 8px rgba(0, 0, 0, 0.8),
      0 0 0 4px #06101d,
      0 0 10px rgba(230, 36, 36, 0.3),
      inset 0 3px 6px rgba(255, 255, 255, 0.15),
      inset 0 -4px 8px rgba(0, 0, 0, 0.9);
  }
  
  .nexpad-btn:active .mask-core {
    box-shadow: 
      0 2px 4px rgba(0, 0, 0, 0.7),
      inset 0 4px 8px rgba(0, 0, 0, 0.5),
      inset 0 -2px 5px rgba(255, 255, 255, 0.3);
  }
</style>
</head>
<body>
  <button class="nexpad-btn" data-control="A" data-category="BUTTON" data-name="Web Slinger A">
    <div class="mask-core">
      <!-- Web Pattern -->
      <svg class="webbing-layer" viewBox="0 0 74 78">
        <g stroke="#1a1a1a" stroke-width="1.5" fill="none">
          <!-- Concentric Webs -->
          <circle cx="37" cy="35" r="10" />
          <circle cx="37" cy="35" r="22" />
          <circle cx="37" cy="35" r="36" />
          <circle cx="37" cy="35" r="50" />
          <!-- Radiating Web Lines -->
          <line x1="37" y1="35" x2="37" y2="-20" />
          <line x1="37" y1="35" x2="37" y2="100" />
          <line x1="37" y1="35" x2="-20" y2="35" />
          <line x1="37" y1="35" x2="100" y2="35" />
          <line x1="37" y1="35" x2="-5" y2="-5" />
          <line x1="37" y1="35" x2="80" y2="-5" />
          <line x1="37" y1="35" x2="-5" y2="75" />
          <line x1="37" y1="35" x2="80" y2="75" />
        </g>
      </svg>
      
      <!-- Spiderman Eyes -->
      <svg class="eyes-layer" viewBox="0 0 74 78">
        <defs>
          <linearGradient id="eye-glow" x1="0%" y1="0%" x2="0%" y2="100%">
            <stop offset="0%" stop-color="#ffffff" />
            <stop offset="100%" stop-color="#d9d9d9" />
          </linearGradient>
        </defs>
        <!-- Left Eye (Quadratic Beziers for the aggressive angular look) -->
        <path d="M 6 32 Q 24 22, 32 50 Q 14 47, 6 32 Z" 
              fill="url(#eye-glow)" 
              stroke="#0a0a0a" 
              stroke-width="3.5" 
              stroke-linejoin="round" />
        
        <!-- Right Eye -->
        <path d="M 68 32 Q 50 22, 42 50 Q 60 47, 68 32 Z" 
              fill="url(#eye-glow)" 
              stroke="#0a0a0a" 
              stroke-width="3.5" 
              stroke-linejoin="round" />
      </svg>

      <div class="mask-gloss"></div>
      <span class="btn-label">A</span>
    </div>
  </button>
</body>
</html>
        """.trimIndent()

        val doc = NxprcHtmlCssConverter.convert(
            source = spidermanHtml,
            id = "rc.spiderman_a",
            name = "Web Slinger A",
            category = "BUTTON",
            defaultControl = "A"
        )

        println("=== COMPILED SPIDERMAN MASK BUTTON ===")
        println("Total Layers count: ${doc.canvas.layers.size}")
        doc.canvas.layers.forEachIndexed { i, layer ->
            when (layer) {
                is com.sanket.tools.nexpad.nxprc.CanvasLayer.VectorPath -> {
                    println("  Layer $i [VectorPath]: path='${layer.pathData.take(30)}...' fill=${layer.fill} stroke=${layer.stroke}")
                }
                is com.sanket.tools.nexpad.nxprc.CanvasLayer.BoxLayer -> {
                    println("  Layer $i [BoxLayer]: shape=${layer.shapeType} fills=${layer.fills.size} shadows=${layer.boxShadows.size}")
                }
                is com.sanket.tools.nexpad.nxprc.CanvasLayer.CenterGlyph -> {
                    println("  Layer $i [CenterGlyph]: text='${layer.text}' fontSize=${layer.fontSizeSp} color=0x${java.lang.Long.toHexString(layer.textColor)}")
                }
                else -> println("  Layer $i [${layer::class.simpleName}]")
            }
        }

        // Layer hierarchy assertions
        assertEquals(22, doc.canvas.layers.size)

        // Mask face (BoxLayer) must appear before web vector paths
        val maskCoreIdx = doc.canvas.layers.indexOfFirst { it is com.sanket.tools.nexpad.nxprc.CanvasLayer.BoxLayer && it.fills.size == 2 }
        val firstWebIdx = doc.canvas.layers.indexOfFirst { it is com.sanket.tools.nexpad.nxprc.CanvasLayer.VectorPath && it.stroke?.width == 1.5f }
        assertTrue("Mask core layer ($maskCoreIdx) must be drawn under the web lines ($firstWebIdx)", maskCoreIdx in 0..<firstWebIdx)

        // All 12 web layers must inherit stroke #1a1a1a with 0.65 opacity (0xA61A1A1A) and width 1.5
        val webLayers = doc.canvas.layers.filterIsInstance<com.sanket.tools.nexpad.nxprc.CanvasLayer.VectorPath>()
            .filter { it.stroke?.width == 1.5f }
        assertEquals("Must have exactly 12 web circles and radiating lines", 12, webLayers.size)
        webLayers.forEach { web ->
            assertNotNull(web.stroke)
            assertEquals("Web stroke must have 0.65 opacity applied to #1a1a1a", 0xA61A1A1AL, web.stroke!!.color)
        }

        // Eye layers must have stroke width 3.5 and LinearGradient fill
        val eyeLayers = doc.canvas.layers.filterIsInstance<com.sanket.tools.nexpad.nxprc.CanvasLayer.VectorPath>()
            .filter { it.stroke?.width == 3.5f }
        assertEquals("Must have 2 eyes", 2, eyeLayers.size)
        eyeLayers.forEach { eye ->
            assertTrue("Eye fill must be LinearGradient", eye.fill is com.sanket.tools.nexpad.nxprc.FillBrush.LinearGradient)
        }

        // CenterGlyph 'A' must be present
        val glyph = doc.canvas.layers.filterIsInstance<com.sanket.tools.nexpad.nxprc.CanvasLayer.CenterGlyph>().firstOrNull()
        assertNotNull(glyph)
        assertEquals("A", glyph!!.text)

        // Full binary encode and decode round-trip
        val bytes = NxprcDocument.encodeToBytes(doc)
        val decoded = NxprcDocument.decodeFromBytes(bytes).getOrThrow()
        assertEquals(doc.canvas.layers.size, decoded.canvas.layers.size)
    }
}
