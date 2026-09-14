package com.sanket.tools.nexpaddesktop

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.sanket.tools.nexpad.nxprc.NxprcPackager
import com.sanket.tools.nexpaddesktop.plugins.NxprcHtmlCssConverter
import com.sanket.tools.nexpaddesktop.ui.PluginsScreen
import com.sanket.tools.nexpaddesktop.ui.designer.FullAuditPreviewScreen
import com.sanket.tools.nexpaddesktop.ui.designer.LayerStudioFullScreen
import org.jetbrains.skia.EncodedImageFormat
import org.junit.Test
import java.io.File

class UiMultiSizeAuditTest {

    private val brainDir = File("C:\\Users\\parma\\.gemini\\antigravity\\brain\\988b000e-5aeb-432b-aa81-d784a06545f7")
    private val scratchDir = File(brainDir, "scratch").apply { mkdirs() }

    data class Resolution(val name: String, val width: Int, val height: Int)

    private val resolutions = listOf(
        Resolution("880x580_compact", 880, 580),
        Resolution("960x640_default", 960, 640),
        Resolution("1280x720", 1280, 720),
        Resolution("1440x900", 1440, 900),
        Resolution("1920x1080", 1920, 1080),
        Resolution("2560x1440", 2560, 1440),
        Resolution("3440x1440", 3440, 1440)
    )

    @Test
    fun testRenderAllScreensAtMultipleResolutions() {
        val htmlSource = NxprcHtmlCssConverter.PRESET_NEO_TACTILE_A
        val doc = NxprcPackager.compile(
            html = htmlSource,
            id = "rc.action_a",
            name = "Action A Button",
            category = "BUTTON",
            defaultControl = "A"
        )

        for (res in resolutions) {
            println("Rendering screens at ${res.name} (${res.width}x${res.height})...")

            // 1. PluginsScreen
            renderAndSave(
                name = "plugins_screen_${res.name}.png",
                width = res.width,
                height = res.height
            ) {
                Box(modifier = Modifier.fillMaxSize().background(Color(0xFF060910))) {
                    PluginsScreen()
                }
            }

            // 2. FullAuditPreviewScreen
            renderAndSave(
                name = "audit_screen_${res.name}.png",
                width = res.width,
                height = res.height
            ) {
                Box(modifier = Modifier.fillMaxSize().background(Color(0xFF06080C))) {
                    FullAuditPreviewScreen(
                        document = doc,
                        htmlSource = htmlSource,
                        onClose = {}
                    )
                }
            }

            // 3. LayerStudioFullScreen
            val activeIndices = (0 until doc.canvas.layers.size).toSet()
            renderAndSave(
                name = "layer_studio_${res.name}.png",
                width = res.width,
                height = res.height
            ) {
                Box(modifier = Modifier.fillMaxSize().background(Color(0xFF060910))) {
                    LayerStudioFullScreen(
                        document = doc,
                        htmlSource = htmlSource,
                        activeLayerIndices = activeIndices,
                        onActiveLayersChange = {},
                        soloLayerIndex = null,
                        onSoloLayerChange = {},
                        selectedLayerIndex = 0,
                        onSelectedLayerChange = {},
                        onExportDoc = {},
                        onPushAdbDoc = {},
                        onFeedback = {},
                        onClose = {}
                    )
                }
            }
        }
        println("All multi-size UI renders completed successfully.")
    }

    private fun renderAndSave(
        name: String,
        width: Int,
        height: Int,
        content: @androidx.compose.runtime.Composable () -> Unit
    ) {
        try {
            val scene = ImageComposeScene(width = width, height = height) {
                content()
            }
            val image = scene.render()
            val data = image.encodeToData(EncodedImageFormat.PNG)
            if (data != null) {
                val outFile = File(scratchDir, name)
                outFile.writeBytes(data.bytes)
                println("  Saved: ${outFile.absolutePath} (${data.bytes.size} bytes)")
            }
        } catch (e: Throwable) {
            println("  FAILED to render $name: ${e.message}")
            e.printStackTrace()
        }
    }
}
