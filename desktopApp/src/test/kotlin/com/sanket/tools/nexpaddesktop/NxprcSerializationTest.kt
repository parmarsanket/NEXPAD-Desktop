package com.sanket.tools.nexpaddesktop

import com.sanket.tools.nexpad.nxprc.NxprcDocument
import com.sanket.tools.nexpaddesktop.plugins.NxprcHtmlCssConverter
import org.junit.Assert.assertEquals
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
}
