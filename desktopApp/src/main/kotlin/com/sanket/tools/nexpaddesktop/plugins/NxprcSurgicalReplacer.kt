package com.sanket.tools.nexpaddesktop.plugins

import com.sanket.tools.nexpad.nxprc.CanvasLayer
import com.sanket.tools.nexpad.nxprc.NxprcDocument

/**
 * Result of a surgical layer modification.
 */
data class SurgicalResult(
    val success: Boolean,
    val updatedHtml: String,
    val message: String,
    val affectedLayerIndex: Int
)

/**
 * Intelligent surgical code replacement engine for NEXPAD Layer Studio.
 *
 * Enables targeted modification of an individual button layer (e.g. Socket, Gloss reflection,
 * Cavity shadow, SVG emblem, Bezel, or Gradient shape) by safely splicing AI-generated
 * or hand-edited code snippets into the parent HTML/CSS document.
 *
 * Guarantees:
 * 1. Non-destructive: Does not destroy or reformat working layers.
 * 2. Multi-format tolerant: Accepts full HTML documents, SVG elements (<svg>, <path>, <circle>),
 *    full CSS rules (.class { ... }, ::before { ... }), or bare CSS declarations (background: ...).
 * 3. Selector mapping: Maps synthetic studio layer selectors (e.g. `.layer-1-gloss::before`) to the
 *    actual DOM element selectors in the user's template.
 */
object NxprcSurgicalReplacer {

    /**
     * Strips Markdown code fences (```html, ```css, ```xml, etc.) from AI output.
     */
    fun stripMarkdownFences(input: String): String {
        var text = input.trim()
        if (text.startsWith("```")) {
            val firstNewline = text.indexOf('\n')
            if (firstNewline != -1) {
                text = text.substring(firstNewline + 1)
            } else {
                text = text.removePrefix("```")
            }
        }
        if (text.endsWith("```")) {
            val lastFence = text.lastIndexOf("```")
            if (lastFence != -1) {
                text = text.substring(0, lastFence)
            }
        }
        return text.trim()
    }

    /**
     * Determines whether the input is a complete HTML document.
     */
    fun isFullHtml(input: String): Boolean {
        val lower = input.lowercase()
        return lower.contains("<!doctype html") ||
                (lower.contains("<html") && lower.contains("<body")) ||
                (lower.contains("<style") && lower.contains("<button"))
    }

    /**
     * Determines whether the input contains SVG elements or shapes.
     */
    fun isSvg(input: String): Boolean {
        val lower = input.lowercase()
        return lower.contains("<svg") ||
                lower.contains("<path") ||
                lower.contains("<circle") ||
                lower.contains("<polygon") ||
                lower.contains("<rect") ||
                lower.contains("<polyline") ||
                lower.contains("<g")
    }

    /**
     * Applies a surgical code replacement to [originalHtml] targeting [layerIndex].
     */
    fun applySurgicalChange(
        originalHtml: String,
        layerIndex: Int,
        layer: CanvasLayer?,
        doc: NxprcDocument,
        replacementInput: String
    ): SurgicalResult {
        val clean = stripMarkdownFences(replacementInput).trim()
        if (clean.isBlank()) {
            return SurgicalResult(false, originalHtml, "Replacement code cannot be empty.", layerIndex)
        }

        // Case 1: Full HTML Document provided
        if (isFullHtml(clean)) {
            return try {
                NxprcHtmlCssConverter.convert(clean, doc.manifest.id, doc.manifest.name)
                SurgicalResult(true, clean, "Full document successfully updated.", layerIndex)
            } catch (e: Exception) {
                SurgicalResult(false, originalHtml, "HTML syntax error: ${e.message}", layerIndex)
            }
        }

        // Case 2: SVG Emblem / Vector Shape replacement
        if (isSvg(clean) || layer is CanvasLayer.VectorPath) {
            val result = applySvgReplacement(originalHtml, clean)
            return if (result != null) {
                SurgicalResult(true, result, "Vector emblem (Layer #$layerIndex) surgically updated.", layerIndex)
            } else {
                SurgicalResult(false, originalHtml, "Could not inject SVG into HTML document.", layerIndex)
            }
        }

        // Case 3: CSS Rules or CSS Declarations
        val updatedHtml = applyCssReplacement(originalHtml, clean, layerIndex, layer)
        return if (updatedHtml != null) {
            SurgicalResult(true, updatedHtml, "CSS layer rules (Layer #$layerIndex) surgically updated.", layerIndex)
        } else {
            SurgicalResult(false, originalHtml, "Could not apply CSS modification to Layer #$layerIndex.", layerIndex)
        }
    }

    // =========================================================================
    // SVG SURGICAL REPLACEMENT
    // =========================================================================

    private fun applySvgReplacement(html: String, svgInput: String): String? {
        val trimmed = svgInput.trim()
        val svgRegex = Regex("""<svg[\s\S]*?<\/svg>""", RegexOption.IGNORE_CASE)

        if (trimmed.startsWith("<svg", ignoreCase = true) && trimmed.contains("</svg>", ignoreCase = true)) {
            // Full <svg>...</svg> provided
            return if (svgRegex.containsMatchIn(html)) {
                svgRegex.replaceFirst(html, trimmed)
            } else {
                insertInsideButton(html, trimmed)
            }
        }

        // Bare SVG shape(s) provided, e.g. <path .../> or <circle .../>
        if (svgRegex.containsMatchIn(html)) {
            // Replace inner contents of existing <svg>
            val innerSvgRegex = Regex("""(<svg[^>]*>)([\s\S]*?)(<\/svg>)""", RegexOption.IGNORE_CASE)
            return innerSvgRegex.replace(html) { match ->
                "${match.groupValues[1]}\n        $trimmed\n    ${match.groupValues[3]}"
            }
        }

        // No <svg> tag in HTML: wrap the shapes in a standard 24x24 scalable SVG and insert
        val wrappedSvg = """
    <svg viewBox="0 0 24 24" width="36" height="36" style="position: absolute; pointer-events: none;">
        $trimmed
    </svg>
""".trimIndent()
        return insertInsideButton(html, wrappedSvg)
    }

    private fun insertInsideButton(html: String, elementToInsert: String): String {
        val buttonCloseRegex = Regex("""(<\/button>)""", RegexOption.IGNORE_CASE)
        if (buttonCloseRegex.containsMatchIn(html)) {
            return buttonCloseRegex.replaceFirst(html, "\n        $elementToInsert\n$1")
        }
        val bodyCloseRegex = Regex("""(<\/body>)""", RegexOption.IGNORE_CASE)
        if (bodyCloseRegex.containsMatchIn(html)) {
            return bodyCloseRegex.replaceFirst(html, "\n    $elementToInsert\n$1")
        }
        return "$html\n$elementToInsert"
    }

    // =========================================================================
    // CSS SURGICAL REPLACEMENT
    // =========================================================================

    private fun applyCssReplacement(
        html: String,
        cssInput: String,
        layerIndex: Int,
        layer: CanvasLayer?
    ): String? {
        val styleRegex = Regex("""(<style[^>]*>)([\s\S]*?)(<\/style>)""", RegexOption.IGNORE_CASE)
        val styleMatch = styleRegex.find(html)

        val originalCss = styleMatch?.groupValues?.get(2) ?: ""
        val isInputBlockRule = cssInput.contains("{") && cssInput.contains("}")

        val primaryButtonSelector = findPrimaryButtonSelector(html)

        val updatedCss = if (isInputBlockRule) {
            applyCssBlockRules(originalCss, cssInput, layerIndex, layer, primaryButtonSelector)
        } else {
            // Bare declarations, e.g. "background: #ff0055; border: 2px solid white;"
            applyBareCssDeclarations(originalCss, cssInput, layerIndex, layer, primaryButtonSelector)
        }

        return if (styleMatch != null) {
            styleRegex.replaceFirst(html, "${styleMatch.groupValues[1]}$updatedCss${styleMatch.groupValues[3]}")
        } else {
            // No <style> tag, insert into <head> or at top
            val headRegex = Regex("""(<\/head>)""", RegexOption.IGNORE_CASE)
            if (headRegex.containsMatchIn(html)) {
                headRegex.replaceFirst(html, "<style>\n$updatedCss\n</style>\n$1")
            } else {
                "<style>\n$updatedCss\n</style>\n$html"
            }
        }
    }

    /**
     * Resolves the primary button CSS selector used in the document (e.g. `.nexpad-btn` or `.button-a`).
     */
    fun findPrimaryButtonSelector(html: String): String {
        val btnClassRegex = Regex("""<button[^>]*class=["']([^"']+)["']""", RegexOption.IGNORE_CASE)
        val match = btnClassRegex.find(html)
        if (match != null) {
            val firstClass = match.groupValues[1].trim().split(Regex("""\s+""")).firstOrNull()
            if (!firstClass.isNullOrBlank()) {
                return ".$firstClass"
            }
        }
        val divBtnRegex = Regex("""<div[^>]*class=["']([^"']+)["'][^>]*data-component=["']button["']""", RegexOption.IGNORE_CASE)
        val divMatch = divBtnRegex.find(html)
        if (divMatch != null) {
            val firstClass = divMatch.groupValues[1].trim().split(Regex("""\s+""")).firstOrNull()
            if (!firstClass.isNullOrBlank()) {
                return ".$firstClass"
            }
        }
        return ".nexpad-btn"
    }

    /**
     * Handles CSS input formatted as full rules: `selector { ... }`.
     */
    private fun applyCssBlockRules(
        css: String,
        rulesInput: String,
        layerIndex: Int,
        layer: CanvasLayer?,
        primaryButtonSelector: String
    ): String {
        val parsedRules = parseCssRules(rulesInput)
        if (parsedRules.isEmpty()) {
            return "$css\n\n$rulesInput"
        }

        var workingCss = css
        for ((rawSelector, ruleBody) in parsedRules) {
            val targetSelector = resolveTargetSelector(rawSelector, layerIndex, layer, primaryButtonSelector, workingCss)

            val existingRuleRegex = Regex("""(${Regex.escape(targetSelector)}\s*\{)([\s\S]*?)(\})""")
            if (existingRuleRegex.containsMatchIn(workingCss)) {
                // Rule with this selector exists: replace its declarations cleanly
                workingCss = existingRuleRegex.replace(workingCss) { m ->
                    "${m.groupValues[1]}\n  ${ruleBody.trim()}\n${m.groupValues[3]}"
                }
            } else {
                // Append the new rule cleanly
                workingCss = workingCss.trimEnd() + "\n\n$targetSelector {\n  ${ruleBody.trim()}\n}\n"
            }
        }
        return workingCss
    }

    /**
     * Maps user/AI provided selector (which might be synthetic like `.layer-1-gloss::before` or `.layer-0-SOCKET`)
     * to the actual selector that exists in the CSS stylesheet.
     */
    private fun resolveTargetSelector(
        rawSelector: String,
        layerIndex: Int,
        layer: CanvasLayer?,
        primaryButtonSelector: String,
        existingCss: String
    ): String {
        val trimmed = rawSelector.trim()

        // If the selector already directly exists in the stylesheet, keep it as is
        if (existingCss.contains(trimmed)) {
            return trimmed
        }

        // Check for pseudo-elements (::before or ::after)
        if (trimmed.contains("::before")) {
            val beforeMatch = Regex("""([^\s{]+)::before""").find(existingCss)
            if (beforeMatch != null) {
                return beforeMatch.value
            }
            return "$primaryButtonSelector::before"
        }
        if (trimmed.contains("::after")) {
            val afterMatch = Regex("""([^\s{]+)::after""").find(existingCss)
            if (afterMatch != null) {
                return afterMatch.value
            }
            return "$primaryButtonSelector::after"
        }

        // Synthetic studio selectors like `.layer-0-SOCKET`, `.layer-0-bezel`, `.layer-X`
        if (trimmed.startsWith(".layer-")) {
            if (layerIndex == 0 || layer is CanvasLayer.BezelSocket || layer is CanvasLayer.BoxLayer) {
                return primaryButtonSelector
            }
        }

        return trimmed
    }

    /**
     * Handles bare CSS declarations (e.g. `background: radial-gradient(...); border: 2px solid cyan;`).
     * Merges these declarations into the target layer's CSS rule without disturbing other declarations.
     */
    private fun applyBareCssDeclarations(
        css: String,
        declarationsInput: String,
        layerIndex: Int,
        layer: CanvasLayer?,
        primaryButtonSelector: String
    ): String {
        val targetSelector = when {
            layer is CanvasLayer.GlossReflection || layer?.javaClass?.simpleName?.contains("Gloss") == true -> {
                val beforeMatch = Regex("""([^\s{]+)::before""").find(css)
                beforeMatch?.value ?: "$primaryButtonSelector::before"
            }
            layer is CanvasLayer.InnerShadow || layer?.javaClass?.simpleName?.contains("Shadow") == true -> {
                val afterMatch = Regex("""([^\s{]+)::after""").find(css)
                afterMatch?.value ?: primaryButtonSelector
            }
            else -> primaryButtonSelector
        }

        val ruleRegex = Regex("""(${Regex.escape(targetSelector)}\s*\{)([\s\S]*?)(\})""")
        val match = ruleRegex.find(css)

        if (match != null) {
            val existingBody = match.groupValues[2]
            val mergedBody = mergeDeclarations(existingBody, declarationsInput)
            return ruleRegex.replaceFirst(css, "${match.groupValues[1]}\n$mergedBody\n${match.groupValues[3]}")
        }

        // Rule does not exist yet: create it
        val newRule = buildString {
            append("\n\n").append(targetSelector).append(" {\n")
            if (targetSelector.contains("::")) {
                append("  content: \"\";\n  position: absolute;\n")
            }
            for (line in declarationsInput.lines()) {
                if (line.isNotBlank()) append("  ").append(line.trim()).append("\n")
            }
            append("}\n")
        }
        return css.trimEnd() + newRule
    }

    /**
     * Merges new CSS declarations into existing CSS declarations.
     * Overwrites matching properties (e.g. `background`, `border`, `box-shadow`) while
     * preserving untouched properties (e.g. `width`, `height`, `border-radius`).
     */
    fun mergeDeclarations(existingBody: String, newDeclarations: String): String {
        val newProps = parseDeclarations(newDeclarations)
        val existingLines = existingBody.lines().toMutableList()
        val processedKeys = mutableSetOf<String>()

        val resultLines = mutableListOf<String>()

        for (line in existingLines) {
            val trimmed = line.trim()
            val colonIdx = trimmed.indexOf(':')
            if (colonIdx != -1 && !trimmed.startsWith("/*")) {
                val propName = trimmed.substring(0, colonIdx).trim()
                if (newProps.containsKey(propName)) {
                    resultLines.add("  $propName: ${newProps[propName]};")
                    processedKeys.add(propName)
                    continue
                }
            }
            if (trimmed.isNotBlank()) {
                resultLines.add(line)
            }
        }

        // Append any brand new properties that were not present in existingBody
        for ((prop, value) in newProps) {
            if (prop !in processedKeys) {
                resultLines.add("  $prop: $value;")
            }
        }

        return resultLines.joinToString("\n")
    }

    /**
     * Parses CSS rules of the form `selector { body }`.
     */
    private fun parseCssRules(cssText: String): List<Pair<String, String>> {
        val rules = mutableListOf<Pair<String, String>>()
        var i = 0
        while (i < cssText.length) {
            val braceOpen = cssText.indexOf('{', i)
            if (braceOpen == -1) break
            val selector = cssText.substring(i, braceOpen).trim()
            val braceClose = findMatchingCloseBrace(cssText, braceOpen)
            if (braceClose == -1) break
            val body = cssText.substring(braceOpen + 1, braceClose).trim()
            if (selector.isNotBlank() && !selector.startsWith("/*")) {
                rules.add(selector to body)
            }
            i = braceClose + 1
        }
        return rules
    }

    private fun findMatchingCloseBrace(text: String, openIndex: Int): Int {
        var depth = 0
        for (idx in openIndex until text.length) {
            if (text[idx] == '{') depth++
            else if (text[idx] == '}') {
                depth--
                if (depth == 0) return idx
            }
        }
        return -1
    }

    /**
     * Parses key-value declarations into a map.
     */
    private fun parseDeclarations(declarationsText: String): Map<String, String> {
        val map = linkedMapOf<String, String>()
        val statements = declarationsText.split(';')
        for (statement in statements) {
            val trimmed = statement.trim()
            if (trimmed.isBlank() || trimmed.startsWith("/*")) continue
            val colonIdx = trimmed.indexOf(':')
            if (colonIdx != -1) {
                val key = trimmed.substring(0, colonIdx).trim()
                val value = trimmed.substring(colonIdx + 1).trim()
                map[key] = value
            }
        }
        return map
    }
}
