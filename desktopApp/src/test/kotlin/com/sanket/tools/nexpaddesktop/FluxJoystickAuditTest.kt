package com.sanket.tools.nexpaddesktop

import com.sanket.tools.nexpad.nxprc.CanvasLayer
import com.sanket.tools.nexpad.nxprc.NxprcPackager
import com.sanket.tools.nexpaddesktop.plugins.NxprcAuditService
import org.junit.Test
import java.awt.Color as AwtColor
import java.awt.Font
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import kotlin.test.assertTrue

class FluxJoystickAuditTest {

    val fluxJoystickHtml = """
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<style>
:root {
  --spring-damping: 0.68;
  --spring-stiffness: 440;
  --press-scale: 0.97;
  --glow: #2fd4b6;
}

* {
  box-sizing: border-box;
}

html, body {
  margin: 0;
  padding: 0;
  width: 100%;
  height: 100%;
  background: #08090a;
  display: flex;
  align-items: center;
  justify-content: center;
}

.lx-stick {
  position: relative;
  width: 150px;
  height: 150px;
  border-radius: 50%;
  color: var(--glow);
  cursor: pointer;
  touch-action: none;
  user-select: none;
  filter: drop-shadow(0 10px 12px rgba(0, 0, 0, 0.45));
}

.lx-body {
  position: absolute;
  inset: 0;
  border-radius: inherit;
  background:
    radial-gradient(
      circle at 50% 50%,
      #030304 0%,
      #090a0b 58%,
      #191b1e 100%
    );
  box-shadow:
    inset 0 8px 15px rgba(0,0,0,0.96),
    inset 0 -3px 5px rgba(255,255,255,0.025),
    0 5px 9px rgba(0,0,0,0.62),
    0 0 0 1px rgba(0,0,0,0.75);
  overflow: hidden;
}

.lx-ring {
  position: absolute;
  inset: 3px;
  border-radius: 50%;
  border: 2px solid var(--glow);
  opacity: 0.48;
  box-shadow:
    0 0 6px 1px var(--glow),
    inset 0 0 6px var(--glow);
  z-index: 10;
  pointer-events: none;
}

.lx-ticks {
  position: absolute;
  inset: 12px;
  border-radius: 50%;
  z-index: 1;
  pointer-events: none;
  background:
    repeating-conic-gradient(
      from -1deg,
      rgba(255,255,255,0.20) 0deg 2deg,
      transparent 2deg 30deg
    );
  -webkit-mask:
    radial-gradient(
      farthest-side,
      transparent calc(100% - 6px),
      #000 calc(100% - 5px)
    );
  mask:
    radial-gradient(
      farthest-side,
      transparent calc(100% - 6px),
      #000 calc(100% - 5px)
    );
}

.lx-cap {
  position: absolute;
  left: 50%;
  top: 50%;
  width: 96px;
  height: 96px;
  margin: -48px 0 0 -48px;
  border-radius: 50%;
  z-index: 4;
  background:
    radial-gradient(
      circle at 50% 36%,
      #3d4145 0%,
      #202327 38%,
      #111316 66%,
      #050506 100%
    );
  box-shadow:
    0 8px 12px rgba(0,0,0,0.82),
    inset 0 2px 2px rgba(255,255,255,0.12),
    inset 0 1px 8px rgba(255,255,255,0.025),
    inset 0 -8px 12px rgba(0,0,0,0.84),
    0 0 0 1px rgba(0,0,0,0.68);
}

.lx-cap-ring {
  position: absolute;
  inset: 13px;
  border-radius: 50%;
  border: 2px solid var(--glow);
  opacity: 0.7;
  box-shadow:
    0 0 6px 1px var(--glow),
    inset 0 0 6px var(--glow);
  pointer-events: none;
}

.lx-dish {
  position: absolute;
  left: 50%;
  top: 50%;
  width: 46px;
  height: 46px;
  margin: -23px 0 0 -23px;
  border-radius: 50%;
  overflow: hidden;
  background:
    radial-gradient(
      circle at 50% 58%,
      #020203 0%,
      #0b0c0d 55%,
      #161719 100%
    );
  box-shadow:
    inset 0 3px 7px rgba(0,0,0,0.95),
    inset 0 -2px 3px rgba(255,255,255,0.025),
    0 1px 0 rgba(255,255,255,0.055);
  display: flex;
  align-items: center;
  justify-content: center;
}

.lx-g {
  position: relative;
  z-index: 1;
  font-size: 26px;
  font-weight: 700;
  line-height: 1;
  color: var(--glow);
  text-shadow:
    0 0 4px var(--glow),
    0 0 11px rgba(47, 212, 182, 0.55);
}

.lx-cap-lens {
  position: absolute;
  inset: 0;
  border-radius: 50%;
  pointer-events: none;
  background:
    radial-gradient(
      ellipse 42% 24% at 50% 20%,
      rgba(255,255,255,0.22),
      transparent 75%
    ),
    radial-gradient(
      circle at 70% 82%,
      rgba(255,255,255,0.05),
      transparent 40%
    );
}

.lx-lens {
  position: absolute;
  inset: 0;
  border-radius: inherit;
  z-index: 20;
  pointer-events: none;
  box-shadow:
    inset 0 2px 2px rgba(255,255,255,0.09),
    inset 0 -3px 5px rgba(0,0,0,0.34);
}
</style>
</head>
<body>
<button
  class="lx-stick"
  data-stick="left"
  data-key="l3"
  data-category="JOYSTICK"
  data-control="LS"
  data-name="Flux Left Analog Stick"
  style="--glow:#2fd4b6"
>
  <div class="lx-body">
    <div class="lx-ticks"></div>
  </div>

  <div class="lx-ring"></div>

  <div class="lx-cap">
    <div class="lx-cap-ring"></div>
    <div class="lx-dish">
      <span class="lx-g">L</span>
    </div>
    <div class="lx-cap-lens"></div>
  </div>

  <div class="lx-lens"></div>
</button>
</body>
</html>
    """.trimIndent()

    @Test
    fun testFluxJoystickCompilationAndVisualParity() {
        val brainDir = File("C:\\Users\\parma\\.gemini\\antigravity\\brain\\988b000e-5aeb-432b-aa81-d784a06545f7")
        if (!brainDir.exists()) {
            brainDir.mkdirs()
        }

        println("=== COMPILING FLUX JOYSTICK VIA NXPRC ===")
        val doc = NxprcPackager.compile(
            html = fluxJoystickHtml,
            id = "stick.flux_ls",
            name = "Flux Left Analog Stick",
            category = "JOYSTICK",
            defaultControl = "LS"
        )

        println("Document canvas size: ${doc.canvas.viewBoxWidth} x ${doc.canvas.viewBoxHeight}")
        println("Compiled layers count: ${doc.canvas.layers.size}")
        doc.canvas.layers.forEachIndexed { i, layer ->
            println("Layer #$i [${layer::class.simpleName}]: $layer")
        }

        assertTrue(doc.canvas.layers.isNotEmpty(), "Flux Joystick should compile into at least 1 layer")

        val viewScale = minOf(280f / doc.canvas.viewBoxWidth.coerceAtLeast(1f), 280f / doc.canvas.viewBoxHeight.coerceAtLeast(1f))
        val chromeImg = NxprcAuditService.captureChromeScreenshot(fluxJoystickHtml, 400, 400, viewScale)
        val nativeImg = NxprcAuditService.renderNxprcToImage(doc, 400, 400)

        if (chromeImg != null) {
            val score = NxprcAuditService.computeVisualParity(chromeImg, nativeImg)
            println("\n========================================================")
            println(">>> FLUX JOYSTICK VISUAL PARITY SCORE: ${String.format("%.2f", score)}% <<<")
            println("========================================================\n")

            ImageIO.write(chromeImg, "PNG", File(brainDir, "flux_joystick_chrome.png"))
            ImageIO.write(nativeImg, "PNG", File(brainDir, "flux_joystick_native.png"))

            // Combine into side-by-side artifact image
            val sideBySide = BufferedImage(820, 460, BufferedImage.TYPE_INT_ARGB)
            val g2d = sideBySide.createGraphics()
            try {
                g2d.color = AwtColor(0x0E, 0x11, 0x17)
                g2d.fillRect(0, 0, 820, 460)

                // Header banner
                g2d.font = Font("Segoe UI", Font.BOLD, 15)
                g2d.color = AwtColor(0x2F, 0xD4, 0xB6)
                g2d.drawString("FLUX JOYSTICK — CHROME HEADLESS", 50, 32)
                g2d.drawString("FLUX JOYSTICK — NATIVE SKIA RENDERER", 460, 32)

                // Draw Chrome on left, Native on right
                g2d.drawImage(chromeImg, 10, 45, 390, 390, null)
                g2d.drawImage(nativeImg, 420, 45, 390, 390, null)

                // Score banner
                g2d.color = AwtColor(0x1F, 0x24, 0x2D)
                g2d.fillRoundRect(310, 410, 200, 34, 12, 12)
                g2d.color = if (score >= 90.0) AwtColor(0x42, 0xFF, 0x8A) else AwtColor(0xFF, 0xD7, 0x00)
                g2d.font = Font("Segoe UI", Font.BOLD, 14)
                val scoreText = "PARITY: ${String.format("%.2f", score)}%"
                val textW = g2d.fontMetrics.stringWidth(scoreText)
                g2d.drawString(scoreText, 310 + (200 - textW) / 2, 432)
            } finally {
                g2d.dispose()
            }

            val sideBySideFile = File(brainDir, "flux_joystick_side_by_side.png")
            ImageIO.write(sideBySide, "PNG", sideBySideFile)
            println("Saved side-by-side comparison artifact: ${sideBySideFile.absolutePath}")
        } else {
            println("Chrome headless not available; writing native image only.")
            ImageIO.write(nativeImg, "PNG", File(brainDir, "flux_joystick_native.png"))
        }
    }
}
