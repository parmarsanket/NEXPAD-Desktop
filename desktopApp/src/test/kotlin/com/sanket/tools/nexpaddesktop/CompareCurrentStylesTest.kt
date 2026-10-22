package com.sanket.tools.nexpaddesktop

import com.sanket.tools.nexpad.nxprc.NxprcPackager
import com.sanket.tools.nexpaddesktop.plugins.NxprcAuditService
import com.sanket.tools.nexpaddesktop.plugins.NxprcHtmlCssConverter
import com.sanket.tools.nexpaddesktop.plugins.NxprcPresetStyle
import org.junit.Test
import java.awt.Color
import java.awt.Font
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

class CompareCurrentStylesTest {

    @Test
    fun renderCurrentFourStyles() {
        val styles = listOf(
            NxprcPresetStyle.XBOX_ELITE,
            NxprcPresetStyle.ARCADE,
            NxprcPresetStyle.CYBERPUNK,
            NxprcPresetStyle.SATIN
        )

        val btnSize = 220
        val imgW = btnSize * 4 + 100
        val imgH = btnSize + 120

        val canvas = BufferedImage(imgW, imgH, BufferedImage.TYPE_INT_ARGB)
        val g2 = canvas.createGraphics()
        g2.color = Color(0x0B, 0x0E, 0x14)
        g2.fillRect(0, 0, imgW, imgH)

        styles.forEachIndexed { i, style ->
            val html = NxprcHtmlCssConverter.getReferenceTemplate("A", "BUTTON", style)
            val doc = NxprcPackager.compile(html, "rc.a_${style.id}", style.displayName, "BUTTON", "A")
            val rendered = NxprcAuditService.renderNxprcToImage(doc, btnSize, btnSize)

            val x = 20 + i * (btnSize + 20)
            val y = 60
            g2.drawImage(rendered, x, y, null)

            g2.color = Color.WHITE
            g2.font = Font("Segoe UI", Font.BOLD, 14)
            g2.drawString(style.displayName, x + 20, 40)
        }

        g2.dispose()
        val brainDir = File("C:\\Users\\parma\\.gemini\\antigravity\\brain\\a824e443-f213-4341-be2a-a3d88d7f26af")
        val outFile = File(brainDir, "current_four_styles_rendered.png")
        ImageIO.write(canvas, "PNG", outFile)
        println("Rendered current styles: ${outFile.absolutePath}")
    }
}
