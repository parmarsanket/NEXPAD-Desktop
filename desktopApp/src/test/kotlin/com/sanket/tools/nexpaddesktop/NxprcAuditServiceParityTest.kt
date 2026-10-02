package com.sanket.tools.nexpaddesktop

import com.sanket.tools.nexpad.nxprc.NxprcPackager
import com.sanket.tools.nexpaddesktop.plugins.NxprcAuditService
import com.sanket.tools.nexpaddesktop.plugins.NxprcHtmlCssConverter
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import javax.imageio.ImageIO

class NxprcAuditServiceParityTest {

    @Test
    fun testNxprcAuditServiceAchievesHighParity() {
        val html = NxprcHtmlCssConverter.PRESET_NEO_TACTILE_A
        val doc = NxprcPackager.compile(
            html = html,
            id = "btn.action_a",
            name = "Action A",
            category = "BUTTON",
            defaultControl = "A"
        )

        val viewScale = minOf(280f / doc.canvas.viewBoxWidth.coerceAtLeast(1f), 280f / doc.canvas.viewBoxHeight.coerceAtLeast(1f))
        val chromeImg = NxprcAuditService.captureChromeScreenshot(html, 400, 400, viewScale)
        if (chromeImg != null) {
            val nativeImg = NxprcAuditService.renderNxprcToImage(doc, 400, 400)
            val score = NxprcAuditService.computeVisualParity(chromeImg, nativeImg)
            println(">>> NxprcAuditService DIRECT AUDIT PARITY SCORE: ${String.format("%.2f", score)}% <<<")

            val brainDir = File("C:\\Users\\parma\\.gemini\\antigravity\\brain\\988b000e-5aeb-432b-aa81-d784a06545f7")
            if (brainDir.exists()) {
                ImageIO.write(chromeImg, "PNG", File(brainDir, "audit_service_chrome.png"))
                ImageIO.write(nativeImg, "PNG", File(brainDir, "audit_service_native.png"))
            }

            assertTrue("Expected NxprcAuditService parity score >= 94.0%, got ${score}%", score >= 94.0)
        } else {
            println("Chrome not found on this system, skipping screenshot comparison.")
        }
    }
}
