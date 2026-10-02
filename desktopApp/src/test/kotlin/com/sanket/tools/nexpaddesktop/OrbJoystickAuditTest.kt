package com.sanket.tools.nexpaddesktop

import com.sanket.tools.nexpad.nxprc.NxprcPackager
import com.sanket.tools.nexpaddesktop.plugins.NxprcAuditService
import org.junit.Test
import java.awt.Color as AwtColor
import java.awt.Font
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import kotlin.test.assertTrue

class OrbJoystickAuditTest {

    val orbJoystickHtml = """
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
      #0a0b0c 62%,
      #1a1c1e 100%
    );
  box-shadow:
    inset 0 7px 14px rgba(0,0,0,0.95),
    0 4px 7px rgba(0,0,0,0.55),
    0 0 0 1px rgba(0,0,0,0.60);
  overflow: hidden;
}

.lx-ring {
  position: absolute;
  inset: 3px;
  border-radius: 50%;
  border: 2px solid var(--glow);
  opacity: 0.45;
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
      rgba(255,255,255,0.22) 0deg 2deg,
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
  inset: 25px;
  border-radius: 50%;
  z-index: 4;
  overflow: hidden;
  background:
    radial-gradient(
      circle at 50% 50%,
      #15171a 0%,
      #060607 100%
    );
  box-shadow:
    0 9px 14px rgba(0,0,0,0.80),
    inset 0 0 0 1px rgba(255,255,255,0.06),
    inset 0 -8px 14px rgba(0,0,0,0.85),
    inset 0 3px 3px rgba(255,255,255,0.10);
}

.orb-core {
  position: absolute;
  inset: 20px;
  border-radius: 50%;
  background:
    radial-gradient(
      circle,
      var(--glow) 0%,
      transparent 68%
    );
  filter: blur(4px);
  opacity: 0.70;
}

.orb-ring {
  position: absolute;
  inset: 5px;
  border-radius: 50%;
  pointer-events: none;
  border: 2px solid var(--glow);
  opacity: 0.50;
  box-shadow:
    inset 0 0 8px var(--glow),
    0 0 6px var(--glow);
}

.orb-spec {
  position: absolute;
  inset: 0;
  border-radius: 50%;
  pointer-events: none;
  background:
    radial-gradient(
      ellipse 34% 20% at 34% 22%,
      rgba(255,255,255,0.38),
      transparent 80%
    ),
    radial-gradient(
      ellipse 40% 14% at 55% 94%,
      rgba(255,255,255,0.10),
      transparent 80%
    );
}

.lx-lens {
  position: absolute;
  inset: 0;
  border-radius: inherit;
  z-index: 20;
  pointer-events: none;
  box-shadow:
    inset 0 2px 2px rgba(255,255,255,0.10),
    inset 0 -3px 5px rgba(0,0,0,0.35);
}
</style>
</head>
<body>
<button
  class="lx-stick"
  data-stick="orb"
  data-key="l3"
  data-category="JOYSTICK"
  data-control="LS"
  data-name="Orb Glass Analog Stick"
  style="--glow:#2fd4b6"
>
  <div class="lx-body">
    <div class="lx-ticks"></div>
  </div>

  <div class="lx-ring"></div>

  <div class="lx-cap">
    <div class="orb-core"></div>
    <div class="orb-ring"></div>
    <div class="orb-spec"></div>
  </div>

  <div class="lx-lens"></div>
</button>
</body>
</html>
    """.trimIndent()

    @Test
    fun testOrbJoystickCompilationAndVisualParity() {
        val brainDir = File("C:\\Users\\parma\\.gemini\\antigravity\\brain\\988b000e-5aeb-432b-aa81-d784a06545f7")
        if (!brainDir.exists()) {
            brainDir.mkdirs()
        }

        println("=== COMPILING ORB JOYSTICK VIA NXPRC ===")
        val doc = NxprcPackager.compile(
            html = orbJoystickHtml,
            id = "stick.orb_ls",
            name = "Orb Glass Analog Stick",
            category = "JOYSTICK",
            defaultControl = "LS"
        )

        println("Document canvas size: ${doc.canvas.viewBoxWidth} x ${doc.canvas.viewBoxHeight}")
        println("Compiled layers count: ${doc.canvas.layers.size}")
        doc.canvas.layers.forEachIndexed { i, layer ->
            println("Layer #$i [${layer::class.simpleName}]: $layer")
        }

        assertTrue(doc.canvas.layers.isNotEmpty(), "Orb Joystick should compile into at least 1 layer")

        val viewScale = minOf(280f / doc.canvas.viewBoxWidth.coerceAtLeast(1f), 280f / doc.canvas.viewBoxHeight.coerceAtLeast(1f))
        val chromeImg = NxprcAuditService.captureChromeScreenshot(orbJoystickHtml, 400, 400, viewScale)
        val nativeImg = NxprcAuditService.renderNxprcToImage(doc, 400, 400)

        if (chromeImg != null) {
            val score = NxprcAuditService.computeVisualParity(chromeImg, nativeImg)
            println("\n========================================================")
            println(">>> ORB JOYSTICK VISUAL PARITY SCORE: ${String.format("%.2f", score)}% <<<")
            println("========================================================\n")

            ImageIO.write(chromeImg, "PNG", File(brainDir, "orb_joystick_chrome.png"))
            ImageIO.write(nativeImg, "PNG", File(brainDir, "orb_joystick_native.png"))

            // Combine into side-by-side artifact image
            val sideBySide = BufferedImage(820, 460, BufferedImage.TYPE_INT_ARGB)
            val g2d = sideBySide.createGraphics()
            try {
                g2d.color = AwtColor(0x0E, 0x11, 0x17)
                g2d.fillRect(0, 0, 820, 460)

                // Header banner
                g2d.font = Font("Segoe UI", Font.BOLD, 15)
                g2d.color = AwtColor(0x2F, 0xD4, 0xB6)
                g2d.drawString("ORB JOYSTICK — CHROME HEADLESS", 50, 32)
                g2d.drawString("ORB JOYSTICK — NATIVE SKIA RENDERER", 460, 32)

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

            val sideBySideFile = File(brainDir, "orb_joystick_side_by_side.png")
            ImageIO.write(sideBySide, "PNG", sideBySideFile)
            println("Saved side-by-side comparison artifact: ${sideBySideFile.absolutePath}")
        } else {
            println("Chrome headless not available; writing native image only.")
            ImageIO.write(nativeImg, "PNG", File(brainDir, "orb_joystick_native.png"))
        }
    }
}
