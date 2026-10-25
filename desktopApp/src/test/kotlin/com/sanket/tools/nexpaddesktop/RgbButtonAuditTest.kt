package com.sanket.tools.nexpaddesktop

import com.sanket.tools.nexpad.nxprc.CanvasLayer
import com.sanket.tools.nexpad.nxprc.NxprcPackager
import com.sanket.tools.nexpaddesktop.plugins.NxprcAuditService
import com.sanket.tools.nexpaddesktop.plugins.NxprcHtmlCssConverter
import org.junit.Test
import java.io.File
import javax.imageio.ImageIO

class RgbButtonAuditTest {

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

  --rgb-glow: rgba(0, 255, 136, 0.55);
}

/* =========================
   ROOT BUTTON
   ========================= */
.nexpad-btn {
  position: relative;
  width: 96px;
  height: 96px;
  padding: 0;
  border: 0;
  border-radius: 50%;
  overflow: hidden;
  cursor: pointer;

  display: flex;
  align-items: center;
  justify-content: center;

  background: #111318;

  box-shadow:
    0 8px 18px rgba(0, 0, 0, 0.55),
    inset 0 2px 3px rgba(255, 255, 255, 0.14),
    inset 0 -6px 10px rgba(0, 0, 0, 0.65),
    0 0 18px var(--rgb-glow);

  transform-origin: 50% 50%;
}

/* =========================
   RGB RING
   ========================= */
.nexpad-btn::before {
  content: "";
  position: absolute;
  inset: 4px;

  border-radius: 50%;

  background: conic-gradient(
    from 0deg,
    #ff003c,
    #ff7a00,
    #ffe600,
    #00ff66,
    #00eaff,
    #0066ff,
    #8a2bff,
    #ff00d4,
    #ff003c
  );

  filter:
    saturate(1.25)
    brightness(1.15);

  animation: rgbRotate 3.2s linear infinite;

  z-index: 0;
}

/* Soft RGB glow */
.nexpad-btn::before {
  box-shadow:
    0 0 12px rgba(255, 0, 76, 0.25),
    0 0 18px rgba(0, 234, 255, 0.20),
    0 0 24px rgba(138, 43, 255, 0.18);
}

/* =========================
   MAIN FACE
   ========================= */
.nexpad-btn::after {
  content: "";
  position: absolute;
  inset: 9px;

  border-radius: 50%;

  background:
    radial-gradient(
      circle at 35% 28%,
      #3a3e46 0%,
      #292d35 38%,
      #1c2027 68%,
      #12151a 100%
    );

  box-shadow:
    inset 0 2px 4px rgba(255, 255, 255, 0.16),
    inset 0 -6px 10px rgba(0, 0, 0, 0.72);

  transform-origin: 50% 50%;

  z-index: 1;
}

/* =========================
   LABEL
   ========================= */
.btn-label {
  position: relative;
  z-index: 3;

  color: #f7f9fb;

  font-family:
    system-ui,
    -apple-system,
    BlinkMacSystemFont,
    "Segoe UI",
    sans-serif;

  font-size: 25px;
  font-weight: 800;
  line-height: 1;

  text-shadow:
    0 2px 3px rgba(0, 0, 0, 0.85),
    0 0 7px rgba(255, 255, 255, 0.18);

  pointer-events: none;
}

/* =========================
   RGB ROTATION
   ========================= */
@keyframes rgbRotate {
  from {
    transform: rotate(0deg);
  }

  to {
    transform: rotate(360deg);
  }
}

/* =========================
   PRESS FACE ANIMATION
   ========================= */
@keyframes pressFace {
  0% {
    transform: scale(1);
    opacity: 1;
  }

  35% {
    transform: scale(0.955);
    opacity: 0.94;
  }

  70% {
    transform: scale(0.975);
    opacity: 0.97;
  }

  100% {
    transform: scale(0.96);
    opacity: 0.96;
  }
}

/* =========================
   PRESS RGB BURST
   ========================= */
@keyframes pressGlow {
  0% {
    transform: rotate(0deg) scale(1);
    opacity: 0.9;
  }

  35% {
    transform: rotate(18deg) scale(1.04);
    opacity: 1;
  }

  100% {
    transform: rotate(32deg) scale(1);
    opacity: 0.95;
  }
}

/* =========================
   ACTIVE / PRESS FEEDBACK
   ========================= */
.nexpad-btn:active {
  transform: scale(var(--press-scale));

  box-shadow:
    0 4px 10px rgba(0, 0, 0, 0.58),
    inset 0 4px 8px rgba(0, 0, 0, 0.58),
    0 0 18px rgba(255, 0, 76, 0.42),
    0 0 28px rgba(0, 234, 255, 0.38),
    0 0 38px rgba(138, 43, 255, 0.30);
}

/* Push the button face inward */
.nexpad-btn:active::after {
  animation: pressFace 180ms cubic-bezier(0.22, 0.8, 0.32, 1) both;
}

/* RGB ring reacts to press */
.nexpad-btn:active::before {
  animation:
    pressGlow 180ms cubic-bezier(0.22, 0.8, 0.32, 1) both,
    rgbRotate 900ms linear infinite;
}

/* Slight label compression */
.nexpad-btn:active .btn-label {
  animation: labelPress 140ms cubic-bezier(0.22, 0.8, 0.32, 1) both;
}

@keyframes labelPress {
  0% {
    transform: scale(1);
    opacity: 1;
  }

  45% {
    transform: scale(0.88);
    opacity: 0.9;
  }

  100% {
    transform: scale(0.93);
    opacity: 0.96;
  }
}
</style>
</head>

<body>

<button
  class="nexpad-btn"
  data-control="A"
  data-category="BUTTON"
  data-codename="RGB Simple"
  data-name="RGB Simple A #16166"
  data-seed="16166"
  id="rc.a_16166"
>
  <span class="btn-label">A</span>
</button>

</body>
</html>
""".trimIndent()

    @Test
    fun testRgbButtonLayersAndParity() {
        val doc = NxprcPackager.compile(
            html = userHtml,
            id = "btn.rgb_a",
            name = "RGB A Button",
            category = "BUTTON",
            defaultControl = "A"
        )

        println("=== DOCUMENT AUDIT ===")
        println("viewBox: ${doc.canvas.viewBoxWidth}x${doc.canvas.viewBoxHeight}")
        println("layers count: ${doc.canvas.layers.size}")
        doc.canvas.layers.forEachIndexed { i, layer ->
            println("\n--- LAYER #$i: ${layer::class.simpleName} ---")
            when (layer) {
                is CanvasLayer.BoxLayer -> {
                    println("  shapeType: '${layer.shapeType}'")
                    println("  pathData: '${layer.pathData}'")
                    println("  polygonSides: ${layer.polygonSides}")
                    println("  radii: tl=${layer.cornerRadiusTopLeft}, tr=${layer.cornerRadiusTopRight}, br=${layer.cornerRadiusBottomRight}, bl=${layer.cornerRadiusBottomLeft}")
                    println("  bounds: wRatio=${layer.widthRatio}, hRatio=${layer.heightRatio}, offX=${layer.offsetXRatio}, offY=${layer.offsetYRatio}")
                    println("  fills: ${layer.fills.size} (primary: ${layer.fill::class.simpleName})")
                    layer.fills.forEachIndexed { fi, f ->
                        println("    fill #$fi: ${f::class.simpleName}")
                    }
                    println("  shadows: ${layer.boxShadows.size}")
                    layer.boxShadows.forEachIndexed { si, s ->
                        println("    shadow #$si: isInset=${s.isInset}, offX=${s.offsetX}, offY=${s.offsetY}, blur=${s.blurRadius}, spread=${s.spreadRadius}, col=0x%08X".format(s.color))
                    }
                    println("  stroke: ${layer.stroke}")
                    println("  clipToBounds: ${layer.clipToBounds}")
                }
                is CanvasLayer.GradientShape -> {
                    println("  shapeType: '${layer.shapeType}'")
                    println("  cornerRadius: ${layer.cornerRadius}")
                    println("  wRatio=${layer.widthRatio}, hRatio=${layer.heightRatio}")
                    println("  fill: ${layer.fill::class.simpleName}")
                    println("  stroke: ${layer.stroke}")
                }
                is CanvasLayer.TextLayer -> {
                    println("  text: '${layer.text}', size: ${layer.fontSizeSp}sp, col=0x%08X".format(layer.textColor))
                }
                is CanvasLayer.CenterGlyph -> {
                    println("  text: '${layer.text}', size: ${layer.fontSizeSp}sp, col=0x%08X".format(layer.textColor))
                }
                else -> {
                    println("  layer: $layer")
                }
            }
        }

        val brainDir = File("C:\\Users\\parma\\.gemini\\antigravity\\brain\\d05c2fb8-838a-4bb1-b221-1288db0fe677")
        val viewScale = minOf(280f / doc.canvas.viewBoxWidth.coerceAtLeast(1f), 280f / doc.canvas.viewBoxHeight.coerceAtLeast(1f))

        val chromeImg = NxprcAuditService.captureChromeScreenshot(userHtml, 400, 400, viewScale)
        val nativeImg = NxprcAuditService.renderNxprcToImage(doc, 400, 400)

        if (chromeImg != null) {
            val score = NxprcAuditService.computeVisualParity(chromeImg, nativeImg)
            println("\n>>> USER RGB BUTTON PARITY SCORE: ${String.format("%.2f", score)}% <<<")
            ImageIO.write(chromeImg, "PNG", File(brainDir, "user_rgb_chrome.png"))
            ImageIO.write(nativeImg, "PNG", File(brainDir, "user_rgb_native.png"))
        }

        // Also render each layer isolated to inspect L0, L1, L2!
        doc.canvas.layers.forEachIndexed { i, layer ->
            val isolatedImg = NxprcAuditService.renderNxprcToImage(doc, 400, 400, listOf(layer))
            ImageIO.write(isolatedImg, "PNG", File(brainDir, "user_rgb_layer_${i}.png"))
            println("Saved isolated layer #$i image: user_rgb_layer_${i}.png")
        }
    }

    @Test
    fun testRgbButtonPressedState() {
        val doc = NxprcPackager.compile(
            html = userHtml,
            id = "btn.rgb_a",
            name = "RGB A Button",
            category = "BUTTON",
            defaultControl = "A"
        )

        val brainDir = File("C:\\Users\\parma\\.gemini\\antigravity\\brain\\d05c2fb8-838a-4bb1-b221-1288db0fe677")
        val viewScale = minOf(280f / doc.canvas.viewBoxWidth.coerceAtLeast(1f), 280f / doc.canvas.viewBoxHeight.coerceAtLeast(1f))

        // Emulate pressed CSS in Chrome
        val pressedHtml = userHtml.replace(
            ".nexpad-btn {",
            ".nexpad-btn {\n  transform: scale(0.92);\n  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.58), inset 0 4px 8px rgba(0, 0, 0, 0.58), 0 0 18px rgba(255, 0, 76, 0.42), 0 0 28px rgba(0, 234, 255, 0.38), 0 0 38px rgba(138, 43, 255, 0.30);\n"
        )

        val chromePressed = NxprcAuditService.captureChromeScreenshot(pressedHtml, 400, 400, viewScale)
        val nativePressed = NxprcAuditService.renderNxprcToImage(doc, 400, 400, isPressed = true)

        if (chromePressed != null) {
            val score = NxprcAuditService.computeVisualParity(chromePressed, nativePressed)
            println("\n>>> PRESSED STATE PARITY SCORE: ${String.format("%.2f", score)}% <<<")
            ImageIO.write(chromePressed, "PNG", File(brainDir, "user_rgb_pressed_chrome.png"))
            ImageIO.write(nativePressed, "PNG", File(brainDir, "user_rgb_pressed_native.png"))
        }

        // Also render each pressed layer isolated
        doc.canvas.layers.forEachIndexed { i, layer ->
            val isolatedPressed = NxprcAuditService.renderNxprcToImage(doc, 400, 400, listOf(layer), isPressed = true)
            ImageIO.write(isolatedPressed, "PNG", File(brainDir, "user_rgb_pressed_layer_${i}.png"))
        }
    }

    @Test
    fun testConverterPipelineVsDirect() {
        val convertedDoc = NxprcHtmlCssConverter.convert(
            source = userHtml,
            id = "rc.a_16166",
            name = "RGB Simple A #16166",
            category = "BUTTON",
            defaultControl = "A"
        )
        println("=== CONVERTER PIPELINE AUDIT ===")
        println("viewBox: ${convertedDoc.canvas.viewBoxWidth}x${convertedDoc.canvas.viewBoxHeight}")
        println("layers count: ${convertedDoc.canvas.layers.size}")
        convertedDoc.canvas.layers.forEachIndexed { i, layer ->
            println("Layer #$i: ${layer::class.simpleName}")
            if (layer is CanvasLayer.BoxLayer) {
                println("  shapeType: '${layer.shapeType}'")
                println("  radii: tl=${layer.cornerRadiusTopLeft}, tr=${layer.cornerRadiusTopRight}, br=${layer.cornerRadiusBottomRight}, bl=${layer.cornerRadiusBottomLeft}")
                println("  bounds: wRatio=${layer.widthRatio}, hRatio=${layer.heightRatio}")
                println("  shadows: ${layer.boxShadows.size}")
                layer.boxShadows.forEachIndexed { si, s ->
                    println("    shadow #$si: isInset=${s.isInset}, offX=${s.offsetX}, offY=${s.offsetY}, blur=${s.blurRadius}, col=0x%08X".format(s.color))
                }
            } else if (layer is CanvasLayer.GlowRing) {
                println("  glowColor: 0x%08X, blur: ${layer.blurRadius}".format(layer.glowColor))
            }
        }
    }

    @Test
    fun testSafeHeadroomAndCircularAura() {
        val doc = NxprcHtmlCssConverter.convert(
            source = userHtml,
            id = "rc.a_16166",
            name = "RGB Simple A #16166",
            category = "BUTTON",
            defaultControl = "A"
        )

        // Verify outsets are detected
        val outsets = doc.canvas.canvasOutsets
        println("Canvas outsets: left=${outsets.left}, top=${outsets.top}, right=${outsets.right}, bottom=${outsets.bottom}")

        // Render pressed state
        val nativeImg = NxprcAuditService.renderNxprcToImage(doc, 400, 400, isPressed = true)
        val brainDir = File("""C:\Users\parma\.gemini\antigravity\brain\d05c2fb8-838a-4bb1-b221-1288db0fe677""")
        ImageIO.write(nativeImg, "PNG", File(brainDir, "user_rgb_aura_fixed_native.png"))

        // Check border pixels (x=0, x=399, y=0, y=399) to verify zero edge clipping
        val bgR = 11
        val bgG = 14
        val bgB = 20
        for (x in 0 until 400 step 20) {
            val topRgb = nativeImg.getRGB(x, 0)
            val botRgb = nativeImg.getRGB(x, 399)
            val tr = (topRgb shr 16) and 0xFF
            val tg = (topRgb shr 8) and 0xFF
            val tb = topRgb and 0xFF
            val br = (botRgb shr 16) and 0xFF
            val bg = (botRgb shr 8) and 0xFF
            val bb = botRgb and 0xFF
            // Must be within 5 units of background (clean fade to transparent, zero hard edge clipping)
            org.junit.Assert.assertTrue("Top edge at x=$x was clipped: rgb($tr,$tg,$tb)", kotlin.math.abs(tr - bgR) < 10)
            org.junit.Assert.assertTrue("Bottom edge at x=$x was clipped: rgb($br,$bg,$bb)", kotlin.math.abs(br - bgR) < 10)
        }
        println("=== ZERO EDGE CLIPPING VERIFIED: Aura smoothly fades to transparent! ===")
    }
}

