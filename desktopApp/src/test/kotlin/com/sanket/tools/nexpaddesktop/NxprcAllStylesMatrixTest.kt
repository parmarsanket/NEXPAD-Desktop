package com.sanket.tools.nexpaddesktop

import com.sanket.tools.nexpad.category.CategoryType
import com.sanket.tools.nexpad.nxprc.NxprcPackager
import com.sanket.tools.nexpaddesktop.plugins.NxprcAuditService
import com.sanket.tools.nexpaddesktop.plugins.NxprcHtmlCssConverter
import com.sanket.tools.nexpaddesktop.plugins.NxprcPresetStyle
import org.junit.Test
import java.awt.Color
import java.awt.Font
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class NxprcAllStylesMatrixTest {

    data class ControlItem(
        val category: String,
        val control: String,
        val label: String
    )

    @Test
    fun testAllStylesAcrossAllCategoriesAndRenderShowroom() {
        val styles = NxprcPresetStyle.entries
        val controls = listOf(
            ControlItem("BUTTON", "A", "Face Button A"),
            ControlItem("TRIGGER", "LT", "Analog Trigger LT"),
            ControlItem("BUMPER", "LB", "Shoulder Bumper LB"),
            ControlItem("DPAD", "UP", "D-Pad Direction UP"),
            ControlItem("DPAD", "DPAD", "D-Pad Cross Full"),
            ControlItem("JOYSTICK", "LS", "Thumbstick LS"),
            ControlItem("SYSTEM", "START", "System Menu / Start"),
            ControlItem("MACRO", "M1", "Rear Paddle M1")
        )

        val cellW = 200
        val cellH = 200
        val headerH = 100
        val rowLabelW = 220
        val totalW = rowLabelW + cellW * styles.size + 40
        val totalH = headerH + cellH * controls.size + 40

        val showroom = BufferedImage(totalW, totalH, BufferedImage.TYPE_INT_ARGB)
        val g2 = showroom.createGraphics()
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)

        // Dark luxury canvas background
        g2.color = Color(0x0E, 0x11, 0x17)
        g2.fillRect(0, 0, totalW, totalH)

        // Title banner
        g2.color = Color.WHITE
        g2.font = Font("Segoe UI", Font.BOLD, 22)
        g2.drawString("NEXPAD NXPRC Multi-Style Full-Controller Engine", 30, 42)

        g2.color = Color(0x94, 0xA3, 0xB8)
        g2.font = Font("Segoe UI", Font.PLAIN, 13)
        g2.drawString("Complete Controller Parity: Triggers, Bumpers, Joysticks, D-Pad, System Buttons & Macros across all 4 Design Presets", 30, 68)

        // Draw Column Headers (Styles)
        styles.forEachIndexed { colIdx, style ->
            val colX = rowLabelW + colIdx * cellW + 20
            g2.color = Color(0x1F, 0x24, 0x30)
            g2.fillRoundRect(colX + 5, 80, cellW - 10, 36, 12, 12)

            val badgeColor = when (style) {
                NxprcPresetStyle.XBOX_ELITE -> Color(0x00, 0xFF, 0xA3)
                NxprcPresetStyle.ARCADE -> Color(0xFF, 0x33, 0x66)
                NxprcPresetStyle.CYBERPUNK -> Color(0x00, 0xE5, 0xFF)
                NxprcPresetStyle.SATIN -> Color(0x94, 0xA3, 0xB8)
            }
            g2.color = badgeColor
            g2.fillOval(colX + 16, 94, 8, 8)

            g2.color = Color.WHITE
            g2.font = Font("Segoe UI", Font.BOLD, 13)
            g2.drawString(style.displayName, colX + 30, 103)
        }

        // Draw Each Row and Component Cell
        controls.forEachIndexed { rowIdx, item ->
            val rowY = headerH + rowIdx * cellH + 20

            // Row header label
            g2.color = Color(0x16, 0x1B, 0x22)
            g2.fillRoundRect(20, rowY + 10, rowLabelW - 30, cellH - 20, 12, 12)

            g2.color = Color(0xF1, 0xF5, 0xF9)
            g2.font = Font("Segoe UI", Font.BOLD, 15)
            g2.drawString(item.label, 36, rowY + 90)

            g2.color = Color(0x64, 0x74, 0x8B)
            g2.font = Font("Segoe UI", Font.PLAIN, 12)
            g2.drawString("Category: ${item.category}", 36, rowY + 112)
            g2.drawString("Control: ${item.control}", 36, rowY + 128)

            styles.forEachIndexed { colIdx, style ->
                val colX = rowLabelW + colIdx * cellW + 20

                // 1. Resolve template HTML
                val html = NxprcHtmlCssConverter.getReferenceTemplate(item.control, item.category, style)
                assertNotNull(html, "Template for ${item.control} (${item.category}) in ${style.displayName} must not be null")
                assertTrue(html.isNotBlank(), "Template must not be blank")

                // 2. Compile to NxprcDocument
                val doc = NxprcPackager.compile(
                    html,
                    "rc.${item.control.lowercase()}",
                    "${style.displayName} ${item.control}",
                    item.category,
                    item.control
                )
                assertNotNull(doc, "Compilation must succeed")
                assertTrue(doc.canvas.layers.isNotEmpty(), "Must have layers")

                // 3. Render Component
                val rendered = NxprcAuditService.renderNxprcToImage(doc, cellW - 20, cellH - 20)

                // Background slot container
                g2.color = Color(0x13, 0x17, 0x20)
                g2.fillRoundRect(colX + 5, rowY + 5, cellW - 10, cellH - 10, 14, 14)

                // Draw rendered button centered
                g2.drawImage(rendered, colX + 10, rowY + 10, null)
            }
        }

        g2.dispose()

        val brainDir = File("C:\\Users\\parma\\.gemini\\antigravity\\brain\\a824e443-f213-4341-be2a-a3d88d7f26af")
        if (!brainDir.exists()) brainDir.mkdirs()
        val outFile = File(brainDir, "all_categories_four_styles_matrix.png")
        ImageIO.write(showroom, "PNG", outFile)
        println("SAVED ALL-CATEGORY FOUR-STYLE MATRIX SHOWROOM: ${outFile.absolutePath} (${outFile.length()} bytes)")
    }
}
