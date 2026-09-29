package com.sanket.tools.nexpaddesktop

import com.sanket.tools.nexpad.nxprc.CanvasLayer
import com.sanket.tools.nexpad.nxprc.NxprcPackager
import com.sanket.tools.nexpaddesktop.plugins.NxprcAuditService
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

/* Subtle RGB breathing */
@keyframes rgbRotate {
  0% {
    transform: rotate(0deg) scale(1);
    filter: saturate(1.05) brightness(0.95);
  }

  50% {
    transform: rotate(180deg) scale(1.015);
    filter: saturate(1.3) brightness(1.08);
  }

  100% {
    transform: rotate(360deg) scale(1);
    filter: saturate(1.05) brightness(0.95);
  }
}

/* Tactile press */
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

        val brainDir = File("C:\\Users\\parma\\.gemini\\antigravity\\brain\\988b000e-5aeb-432b-aa81-d784a06545f7")
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
}
