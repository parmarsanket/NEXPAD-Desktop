package com.sanket.tools.nexpaddesktop

import com.sanket.tools.nexpad.nxprc.engine.css.CssCascadeResolver
import com.sanket.tools.nexpad.nxprc.engine.css.CssTokenizer
import com.sanket.tools.nexpad.nxprc.engine.dom.HtmlDomParser
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NxprcParserCorrectnessTest {
    @Test
    fun ignoresDoctypeCommentsAndKeepsVoidTagsFromCorruptingTheTree() {
        val parsed = HtmlDomParser.parse("""
            <!DOCTYPE html><!-- comment -->
            <button class="nexpad-btn" data-control="A" data-category="BUTTON">
                <img src="x"><span>A</span><source src="x"><span>B</span>
            </button>
        """.trimIndent())
        val button = parsed.root.findByTag("button").single()

        assertEquals(listOf("img", "span", "source", "span"), button.children.map { it.tag })
        assertTrue(button.textContent.isBlank())
    }

    @Test
    fun supportsCompoundClassesChildSelectorsAndImportantValues() {
        val parsed = HtmlDomParser.parse("""
            <style>
              .parent .child { opacity: .2; }
              .nexpad-btn.primary { color: #00ff00 !important; }
              .parent > .child { opacity: .5 !important; }
            </style>
            <button class="nexpad-btn primary"><span class="parent"><span class="child">A</span></span></button>
        """.trimIndent())
        val stylesheet = CssTokenizer.parse(parsed.embeddedCss)
        val button = parsed.root.findByTag("button").single()
        val parent = button.children.single()
        val child = parent.children.single()

        assertEquals("#00ff00", CssCascadeResolver.computeStyle(button, stylesheet).base["color"])
        assertEquals(".5", CssCascadeResolver.computeStyle(child, stylesheet).base["opacity"])
    }
}
