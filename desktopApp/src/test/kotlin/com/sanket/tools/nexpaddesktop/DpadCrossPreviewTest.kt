package com.sanket.tools.nexpaddesktop

import com.sanket.tools.nexpad.nxprc.NxprcPackager
import com.sanket.tools.nexpaddesktop.plugins.NxprcAuditService
import com.sanket.tools.nexpaddesktop.plugins.NxprcHtmlCssConverter
import com.sanket.tools.nexpaddesktop.plugins.NxprcPresetStyle
import org.junit.Test
import java.io.File
import javax.imageio.ImageIO
import kotlin.test.assertNotNull

class DpadCrossPreviewTest {

    @Test
    fun generateFixedDpadCrossPreview() {
        val html = NxprcHtmlCssConverter.getReferenceTemplate("DPAD", "DPAD", NxprcPresetStyle.XBOX_ELITE)
        val doc = NxprcPackager.compile(html, "rc.dpad_cross", "D-Pad Cross Fixed", "DPAD", "DPAD")
        assertNotNull(doc)

        val rendered = NxprcAuditService.renderNxprcToImage(doc, 400, 400)
        val brainDir = File("C:\\Users\\parma\\.gemini\\antigravity\\brain\\a824e443-f213-4341-be2a-a3d88d7f26af")
        val outFile = File(brainDir, "fixed_dpad_cross_preview.png")
        ImageIO.write(rendered, "PNG", outFile)
        println("Generated fixed D-Pad preview: ${outFile.absolutePath} (${outFile.length()} bytes)")
    }
}
