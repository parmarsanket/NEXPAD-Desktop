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
 * Cavity shadow, SVG emblem, Bezel, or Center Glyph / Text) by safely splicing AI-generated
 * or hand-edited code snippets into the parent HTML/CSS document.
 *
 * Guarantees:
 * 1. Non-destructive: Does not destroy or reformat working layers.
 * 2. Multi-format tolerant: Accepts full HTML documents, SVG elements (<svg>, <path>, <circle>),
 *    full CSS rules (.class { ... }, ::before { ... }, .layer-X-glyph { ... }),
 *    bare CSS declarations (font-size: ...; color: ...; background: ...),
 *    and decomposed markup snippets (e.g. <span>A</span>).
 * 3. Selector mapping: Maps synthetic studio layer selectors (e.g. `.layer-14-glyph`, `.layer-1-gloss::before`)
 *    to the actual DOM element selectors in the user's template (e.g. `.nexpad-btn .btn-label`, `.nexpad-btn::before`).
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
        if ((isSvg(clean) && !clean.contains("<span")) || layer is CanvasLayer.VectorPath) {
            val result = applySvgReplacement(originalHtml, clean)
            return if (result != null) {
                try {
                    val compiled = NxprcHtmlCssConverter.convert(result, doc.manifest.id, doc.manifest.name)
                    SurgicalResult(true, result, "Vector emblem (Layer #$layerIndex) surgically updated (${compiled.canvas.layers.size} layers).", layerIndex)
                } catch (e: Exception) {
                    SurgicalResult(false, originalHtml, "SVG syntax or compilation error: ${e.message}", layerIndex)
                }
            } else {
                SurgicalResult(false, originalHtml, "Could not inject SVG into HTML document.", layerIndex)
            }
        }

        // Case 3: CSS Rules, CSS Declarations, and/or DOM Markup (e.g. <span>A</span>)
        // Step 3A: Apply any markup changes (e.g. changing <span> text content)
        val htmlWithMarkup = applyMarkupUpdates(originalHtml, clean, layerIndex, layer)

        // Strip HTML markup and comments from the CSS input so no <span> or HTML tags enter <style>
        val cssOnly = clean
            .replace(Regex("""<!--[\s\S]*?-->"""), "")
            .replace(Regex("""<[^>]+>[\s\S]*?<\/[^>]+>"""), "")
            .replace(Regex("""<[^>]+>"""), "")
            .trim()

        // Step 3B: Apply CSS rules or declarations
        val updatedHtml = if (cssOnly.isNotBlank()) {
            applyCssReplacement(htmlWithMarkup, cssOnly, layerIndex, layer, doc)
        } else {
            htmlWithMarkup
        }

        if (updatedHtml != null) {
            return try {
                val compiled = NxprcHtmlCssConverter.convert(updatedHtml, doc.manifest.id, doc.manifest.name)
                SurgicalResult(true, updatedHtml, "Layer #$layerIndex surgically updated (${compiled.canvas.layers.size} layers).", layerIndex)
            } catch (e: Exception) {
                SurgicalResult(false, originalHtml, "Recompilation failed: ${e.message}", layerIndex)
            }
        }

        return SurgicalResult(false, originalHtml, "Could not apply modification to Layer #$layerIndex.", layerIndex)
    }

    // =========================================================================
    // DOM MARKUP UPDATES (e.g. <span>Text</span>)
    // =========================================================================

    private fun applyMarkupUpdates(
        html: String,
        input: String,
        layerIndex: Int,
        layer: CanvasLayer?
    ): String {
        val spanMatch = Regex("""<span[^>]*>([\s\S]*?)<\/span>""", RegexOption.IGNORE_CASE).find(input)
        if (spanMatch != null) {
            val newText = spanMatch.groupValues[1].trim()
            if (newText.isNotBlank()) {
                // If HTML already has a <span>...</span> inside the button, replace its text content
                val existingSpanRegex = Regex("""(<span[^>]*>)([\s\S]*?)(<\/span>)""", RegexOption.IGNORE_CASE)
                val existingMatch = existingSpanRegex.find(html)
                if (existingMatch != null) {
                    val replacement = "${existingMatch.groupValues[1]}$newText${existingMatch.groupValues[3]}"
                    return html.replaceRange(existingMatch.range, replacement)
                } else {
                    // Button has no <span>: insert <span class="btn-label">$newText</span>
                    return insertInsideButton(html, "<span class=\"btn-label\">$newText</span>")
                }
            }
        }
        return html
    }

    // =========================================================================
    // SVG SURGICAL REPLACEMENT
    // =========================================================================

    private fun applySvgReplacement(html: String, svgInput: String): String? {
        val trimmed = svgInput.trim()
        val svgBlockRegex = Regex("""<svg[\s\S]*?<\/svg>""", RegexOption.IGNORE_CASE)
        val fullSvgInInput = svgBlockRegex.find(trimmed)

        if (fullSvgInInput != null) {
            val newSvg = fullSvgInInput.value
            // If the original HTML already has an <svg>...</svg>, replace it
            val existingSvgMatch = svgBlockRegex.find(html)
            if (existingSvgMatch != null) {
                // If existing <svg> has class="..." and new <svg> doesn't, preserve class
                val existingTag = Regex("""<svg([^>]*)>""", RegexOption.IGNORE_CASE).find(existingSvgMatch.value)
                val newTag = Regex("""<svg([^>]*)>""", RegexOption.IGNORE_CASE).find(newSvg)
                val finalSvg = if (existingTag != null && newTag != null) {
                    val existingAttrs = existingTag.groupValues[1]
                    val newAttrs = newTag.groupValues[1]
                    val classMatch = Regex("""class=["']([^"']+)["']""", RegexOption.IGNORE_CASE).find(existingAttrs)
                    if (classMatch != null && !newAttrs.contains("class=", ignoreCase = true)) {
                        newSvg.replaceFirst(newTag.value, "<svg ${classMatch.value}$newAttrs>")
                    } else {
                        newSvg
                    }
                } else {
                    newSvg
                }
                return html.replaceRange(existingSvgMatch.range, finalSvg)
            } else {
                return insertInsideButton(html, newSvg)
            }
        }

        // Bare SVG shape(s) provided (e.g. <path .../> or <circle .../>)
        val cleanShapes = trimmed
            .replace(Regex("""<!--[\s\S]*?-->"""), "")
            .trim()

        val existingSvgMatch = svgBlockRegex.find(html)
        if (existingSvgMatch != null) {
            val innerSvgRegex = Regex("""(<svg[^>]*>)([\s\S]*?)(<\/svg>)""", RegexOption.IGNORE_CASE)
            return innerSvgRegex.replace(html) { match ->
                "${match.groupValues[1]}\n        $cleanShapes\n    ${match.groupValues[3]}"
            }
        }

        // No <svg> tag in HTML: wrap the shapes in a standard scalable SVG and insert
        val wrappedSvg = """
    <svg viewBox="0 0 96 96" width="96px" height="96px" style="position: absolute; pointer-events: none;">
        $cleanShapes
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
        layer: CanvasLayer?,
        doc: NxprcDocument? = null
    ): String? {
        val styleRegex = Regex("""(<style[^>]*>)([\s\S]*?)(<\/style>)""", RegexOption.IGNORE_CASE)
        val styleMatch = styleRegex.find(html)

        val originalCss = styleMatch?.groupValues?.get(2) ?: ""
        val isInputBlockRule = cssInput.contains("{") && cssInput.contains("}")

        val primaryButtonSelector = findPrimaryButtonSelector(html)

        val updatedCss = if (isInputBlockRule) {
            applyCssBlockRules(originalCss, cssInput, layerIndex, layer, primaryButtonSelector, html, doc)
        } else {
            // Bare declarations, e.g. "background: #ff0055; border: 2px solid white;"
            applyBareCssDeclarations(originalCss, cssInput, layerIndex, layer, primaryButtonSelector, html, doc)
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
     * Resolves the CSS selector for text/glyph elements (e.g. `.nexpad-btn .btn-label`).
     */
    fun findTextSelector(existingCss: String, html: String, primaryButtonSelector: String): String {
        // 1. Check if HTML has a <span> with a class
        val spanClassMatch = Regex("""<span[^>]*class=["']([^"']+)["']""", RegexOption.IGNORE_CASE).find(html)
        if (spanClassMatch != null) {
            val cls = spanClassMatch.groupValues[1].trim().split(Regex("""\s+""")).firstOrNull { it.isNotBlank() }
            if (cls != null) {
                // Check if this class is styled in CSS
                val ruleMatch = Regex("""([^\r\n{}]*\.$cls[^\r\n{}]*)\s*\{""").find(existingCss)
                if (ruleMatch != null) {
                    return ruleMatch.groupValues[1].trim()
                }
                return "$primaryButtonSelector .$cls"
            }
        }

        // 2. Check existing CSS for common text/glyph selectors
        val cssMatch = Regex("""([^\r\n{}]*(?:btn-label|label|glyph|text)[^\r\n{}]*)\s*\{""", RegexOption.IGNORE_CASE).find(existingCss)
        if (cssMatch != null) {
            return cssMatch.groupValues[1].trim()
        }

        // 3. Check for span rule in CSS
        val spanMatch = Regex("""([^\r\n{}]*span[^\r\n{}]*)\s*\{""", RegexOption.IGNORE_CASE).find(existingCss)
        if (spanMatch != null) {
            return spanMatch.groupValues[1].trim()
        }

        // 4. Default to child .btn-label on primary button
        return "$primaryButtonSelector .btn-label"
    }

    fun findAfterSelectors(css: String): List<String> {
        val rules = parseCssRules(css)
        return rules.map { it.first }.filter { it.contains("::after") }
    }

    fun findBeforeSelectors(css: String): List<String> {
        val rules = parseCssRules(css)
        return rules.map { it.first }.filter { it.contains("::before") }
    }

    fun findSelectorsWithInsetShadow(css: String): List<String> {
        val rules = parseCssRules(css)
        val result = mutableListOf<String>()
        for ((sel, body) in rules) {
            val lowerBody = body.lowercase()
            if (lowerBody.contains("box-shadow") && lowerBody.contains("inset")) {
                result.add(sel)
            }
        }
        return result
    }

    fun collectCandidateBoxSelectors(html: String, css: String, primaryButtonSelector: String): List<String> {
        val candidates = mutableListOf<String>()
        val rules = parseCssRules(css)
        val ruleMap = rules.toMap()

        fun hasBoxStyles(body: String?): Boolean {
            if (body == null) return false
            val lower = body.lowercase()
            return lower.contains("background") || lower.contains("border") || lower.contains("box-shadow")
        }

        fun findRuleFor(selector: String): String? {
            if (ruleMap.containsKey(selector)) return selector
            val escaped = Regex.escape(selector)
            return rules.firstOrNull { Regex("""(?:^|\s|,)$escaped(?:\s|,|$)""").containsMatchIn(it.first) }?.first
        }

        // 1. Root button ::before
        val rootBefore = findRuleFor("$primaryButtonSelector::before")
            ?: rules.firstOrNull { it.first.contains("::before") && !it.first.contains(" ") }?.first
        if (rootBefore != null && hasBoxStyles(ruleMap[rootBefore])) {
            candidates.add(rootBefore)
        }

        // 2. Root button itself
        val rootRule = findRuleFor(primaryButtonSelector)
        if (rootRule != null && hasBoxStyles(ruleMap[rootRule])) {
            candidates.add(rootRule)
        }

        // 3. Child elements inside <button>...</button>
        val buttonContentMatch = Regex("""<button[^>]*>([\s\S]*?)<\/button>""", RegexOption.IGNORE_CASE).find(html)
        val innerHtml = buttonContentMatch?.groupValues?.get(1) ?: html

        val elementRegex = Regex("""<(div|span|section|i|b|p)\b[^>]*class=["']([^"']+)["'][^>]*>""", RegexOption.IGNORE_CASE)
        val childClasses = mutableListOf<String>()
        for (m in elementRegex.findAll(innerHtml)) {
            val tag = m.groupValues[1].lowercase()
            val classes = m.groupValues[2].trim().split(Regex("""\s+""")).filter { it.isNotBlank() }
            for (cls in classes) {
                if (tag == "span" && (cls.contains("label") || cls.contains("text") || cls.contains("glyph"))) continue
                if (!childClasses.contains(cls)) {
                    childClasses.add(cls)
                }
            }
        }

        for (cls in childClasses) {
            val classSelector = ".$cls"
            // Child element itself
            val childRule = rules.firstOrNull { it.first.contains(cls) && !it.first.contains("::") && !it.first.contains(":") }?.first
                ?: classSelector
            if (hasBoxStyles(ruleMap[childRule]) || childRule == classSelector) {
                if (!candidates.contains(childRule)) candidates.add(childRule)
            }

            // Child ::before
            val childBefore = rules.firstOrNull { it.first.contains(cls) && it.first.contains("::before") }?.first
            if (childBefore != null && hasBoxStyles(ruleMap[childBefore])) {
                if (!candidates.contains(childBefore)) candidates.add(childBefore)
            }

            // Child ::after (only if NOT gloss)
            val childAfter = rules.firstOrNull { it.first.contains(cls) && it.first.contains("::after") }?.first
            if (childAfter != null && hasBoxStyles(ruleMap[childAfter])) {
                val body = ruleMap[childAfter] ?: ""
                val isGloss = body.contains("ellipse", ignoreCase = true) || body.contains("rotate", ignoreCase = true)
                if (!isGloss && !candidates.contains(childAfter)) {
                    candidates.add(childAfter)
                }
            }
        }

        // 4. Root button ::after (only if NOT gloss)
        val rootAfter = findRuleFor("$primaryButtonSelector::after")
        if (rootAfter != null && hasBoxStyles(ruleMap[rootAfter])) {
            val body = ruleMap[rootAfter] ?: ""
            val isGloss = body.contains("ellipse", ignoreCase = true) || body.contains("rotate", ignoreCase = true)
            if (!isGloss && !candidates.contains(rootAfter)) {
                candidates.add(rootAfter)
            }
        }

        return candidates
    }

    /**
     * Handles CSS input formatted as full rules: `selector { ... }`.
     */
    private fun applyCssBlockRules(
        css: String,
        rulesInput: String,
        layerIndex: Int,
        layer: CanvasLayer?,
        primaryButtonSelector: String,
        html: String,
        doc: NxprcDocument? = null
    ): String {
        val parsedRules = parseCssRules(rulesInput)
        if (parsedRules.isEmpty()) {
            return applyBareCssDeclarations(css, rulesInput, layerIndex, layer, primaryButtonSelector, html, doc)
        }

        var workingCss = css
        for ((rawSelector, ruleBody) in parsedRules) {
            val targetSelector = resolveTargetSelector(rawSelector, layerIndex, layer, primaryButtonSelector, workingCss, html, doc)

            // Flexible regex matching the selector even if whitespace / indentation differs
            val escapedParts = targetSelector.trim().split(Regex("""\s+""")).map { Regex.escape(it) }
            val selectorPattern = escapedParts.joinToString("""\s+""")
            val existingRuleRegex = Regex("""([^\r\n{}]*$selectorPattern\s*\{)([\s\S]*?)(\})""")

            var matchedRegex: Regex? = null
            if (existingRuleRegex.containsMatchIn(workingCss)) {
                matchedRegex = existingRuleRegex
            } else {
                // Try matching just the last segment of the selector, e.g. ".btn-label" inside ".nexpad-btn .btn-label"
                val lastSegment = targetSelector.trim().split(Regex("""\s+""")).lastOrNull()
                if (lastSegment != null && lastSegment.isNotBlank() && (lastSegment.startsWith(".") || lastSegment.startsWith("#") || lastSegment.startsWith("::"))) {
                    val fallbackPattern = Regex("""([^\r\n{}]*${Regex.escape(lastSegment)}\s*\{)([\s\S]*?)(\})""")
                    if (fallbackPattern.containsMatchIn(workingCss)) {
                        matchedRegex = fallbackPattern
                    }
                }
            }

            if (matchedRegex != null) {
                // Rule with this selector exists: merge declarations cleanly
                workingCss = matchedRegex.replace(workingCss) { m ->
                    val existingBody = m.groupValues[2]
                    val mergedBody = mergeDeclarations(existingBody, ruleBody)
                    "${m.groupValues[1]}\n$mergedBody\n${m.groupValues[3]}"
                }
            } else {
                // Append the new rule cleanly
                val formattedBody = mergeDeclarations("", ruleBody)
                workingCss = workingCss.trimEnd() + "\n\n$targetSelector {\n$formattedBody\n}\n"
            }
        }
        return workingCss
    }

    /**
     * Maps user/AI provided selector (which might be synthetic like `.layer-14-glyph` or `.layer-0-SOCKET`)
     * to the actual selector that exists in the CSS stylesheet.
     */
    fun resolveTargetSelector(
        rawSelector: String,
        layerIndex: Int,
        layer: CanvasLayer?,
        primaryButtonSelector: String,
        existingCss: String,
        html: String,
        doc: NxprcDocument? = null
    ): String {
        val trimmed = rawSelector.trim()

        // 1. If non-synthetic selector directly exists in stylesheet, keep it
        if (trimmed.isNotBlank() && !trimmed.startsWith(".layer-")) {
            val rules = parseCssRules(existingCss)
            val directMatch = rules.firstOrNull { it.first == trimmed }?.first
                ?: rules.firstOrNull { it.first.endsWith(trimmed) || it.first.contains(trimmed) }?.first
            if (directMatch != null) {
                return directMatch
            }
        }

        val lower = trimmed.lowercase()

        // 2. SVG element / VectorPath
        if (layer is CanvasLayer.VectorPath || lower.contains("icon") || lower.contains("emblem") || lower.contains("svg")) {
            val svgClassMatch = Regex("""<svg[^>]*class=["']([^"']+)["']""", RegexOption.IGNORE_CASE).find(html)
            if (svgClassMatch != null) {
                val cls = svgClassMatch.groupValues[1].trim().split(Regex("""\s+""")).firstOrNull { it.isNotBlank() }
                if (cls != null) {
                    val cssRule = Regex("""([^\r\n{}]*\.$cls[^\r\n{}]*)\s*\{""").find(existingCss)
                    if (cssRule != null) return cssRule.groupValues[1].trim()
                    return ".$cls"
                }
            }
            return "$primaryButtonSelector svg"
        }

        // 3. Text / Glyph layer
        if (lower.contains("glyph") || lower.contains("text") || layer is CanvasLayer.CenterGlyph || layer is CanvasLayer.TextLayer) {
            return findTextSelector(existingCss, html, primaryButtonSelector)
        }

        // 4. Gloss Reflection / ::after
        if (lower.contains("::after") || layer is CanvasLayer.GlossReflection) {
            val afterSelectors = findAfterSelectors(existingCss)
            if (afterSelectors.isNotEmpty()) {
                if (afterSelectors.size == 1) return afterSelectors.first()

                val rules = parseCssRules(existingCss).toMap()
                val glossAfter = afterSelectors.firstOrNull { sel ->
                    val b = rules[sel]?.lowercase() ?: ""
                    b.contains("ellipse") || b.contains("radial-gradient") || b.contains("rotate")
                }
                if (glossAfter != null) return glossAfter

                return afterSelectors.last()
            }
            return "$primaryButtonSelector::after"
        }

        // 5. Explicit ::before
        if (lower.contains("::before")) {
            val beforeSelectors = findBeforeSelectors(existingCss)
            if (beforeSelectors.isNotEmpty()) {
                return beforeSelectors.first()
            }
            return "$primaryButtonSelector::before"
        }

        // 6. Cavity / InnerShadow
        if (lower.contains("cavity") || layer is CanvasLayer.InnerShadow) {
            val insetSelectors = findSelectorsWithInsetShadow(existingCss)
            if (insetSelectors.isNotEmpty()) {
                val innerRank = doc?.canvas?.layers?.take(layerIndex + 1)?.count { it is CanvasLayer.InnerShadow }?.minus(1)?.coerceAtLeast(0) ?: 0
                return insetSelectors.getOrNull(innerRank.coerceIn(0, insetSelectors.lastIndex)) ?: insetSelectors.first()
            }
            val boxes = collectCandidateBoxSelectors(html, existingCss, primaryButtonSelector)
            if (boxes.isNotEmpty()) {
                return boxes.lastOrNull { it.contains("core", ignoreCase = true) } ?: boxes.first()
            }
            return primaryButtonSelector
        }

        // 7. Glow ring layer
        if (lower.contains("glow") || layer is CanvasLayer.GlowRing) {
            val glowMatch = Regex("""([^\r\n{}]*(?:btn-glow|glow)[^\r\n{}]*)\s*\{""", RegexOption.IGNORE_CASE).find(existingCss)
            if (glowMatch != null) {
                return glowMatch.groupValues[1].trim()
            }
            return primaryButtonSelector
        }

        // 8. Molded Bezel Socket
        if (lower.contains("bezel") || lower.contains("socket") || layer is CanvasLayer.BezelSocket) {
            val bezelMatch = Regex("""([^\r\n{}]*(?:bezel|socket)[^\r\n{}]*)\s*\{""", RegexOption.IGNORE_CASE).find(existingCss)
            if (bezelMatch != null) {
                return bezelMatch.groupValues[1].trim()
            }
            val boxes = collectCandidateBoxSelectors(html, existingCss, primaryButtonSelector)
            if (boxes.isNotEmpty()) return boxes.first()
            return primaryButtonSelector
        }

        // 9. BoxLayer, GradientShape, or generic .layer- selector
        val candidateBoxes = collectCandidateBoxSelectors(html, existingCss, primaryButtonSelector)
        if (candidateBoxes.isNotEmpty()) {
            val boxRank = doc?.canvas?.layers?.take(layerIndex + 1)?.count {
                it is CanvasLayer.BoxLayer || it is CanvasLayer.GradientShape
            }?.minus(1)?.coerceAtLeast(0) ?: layerIndex.coerceIn(0, candidateBoxes.lastIndex)

            val matchedBox = candidateBoxes.getOrNull(boxRank.coerceIn(0, candidateBoxes.lastIndex))
            if (matchedBox != null) {
                return matchedBox
            }
        }

        // 10. Fallback heuristics if candidate boxes was empty
        if (lower.contains("keycap") || lower.contains("core") || lower.contains("cap") || lower.contains("surface")) {
            val coreMatch = Regex("""([^\r\n{}]*(?:btn-core|core|keycap|cap|surface)[^\r\n{}]*)\s*\{""", RegexOption.IGNORE_CASE).find(existingCss)
            if (coreMatch != null) {
                return coreMatch.groupValues[1].trim()
            }
            return "$primaryButtonSelector .btn-core"
        }

        return primaryButtonSelector
    }

    /**
     * Handles bare CSS declarations (e.g. `font-size: 50px; color: #FFF;` or `background: radial-gradient(...);`).
     * Merges these declarations into the target layer's CSS rule without disturbing other declarations.
     */
    private fun applyBareCssDeclarations(
        css: String,
        declarationsInput: String,
        layerIndex: Int,
        layer: CanvasLayer?,
        primaryButtonSelector: String,
        html: String,
        doc: NxprcDocument? = null
    ): String {
        val targetSelector = resolveTargetSelector("", layerIndex, layer, primaryButtonSelector, css, html, doc)

        val escapedParts = targetSelector.trim().split(Regex("""\s+""")).map { Regex.escape(it) }
        val selectorPattern = escapedParts.joinToString("""\s+""")
        val ruleRegex = Regex("""([^\r\n{}]*$selectorPattern\s*\{)([\s\S]*?)(\})""")

        var finalRegex = ruleRegex
        var match = ruleRegex.find(css)
        if (match == null) {
            val lastSegment = targetSelector.trim().split(Regex("""\s+""")).lastOrNull()
            if (lastSegment != null && lastSegment.isNotBlank() && (lastSegment.startsWith(".") || lastSegment.startsWith("#") || lastSegment.startsWith("::"))) {
                val fallbackRegex = Regex("""([^\r\n{}]*${Regex.escape(lastSegment)}\s*\{)([\s\S]*?)(\})""")
                val fallbackMatch = fallbackRegex.find(css)
                if (fallbackMatch != null) {
                    finalRegex = fallbackRegex
                    match = fallbackMatch
                }
            }
        }

        if (match != null) {
            val existingBody = match.groupValues[2]
            val mergedBody = mergeDeclarations(existingBody, declarationsInput)
            return finalRegex.replaceFirst(css, "${match.groupValues[1]}\n$mergedBody\n${match.groupValues[3]}")
        }

        // Rule does not exist yet: create it
        val mergedBody = mergeDeclarations("", declarationsInput)
        val newRule = buildString {
            append("\n\n").append(targetSelector).append(" {\n")
            if (targetSelector.contains("::")) {
                append("  content: \"\";\n  position: absolute;\n")
            }
            append(mergedBody).append("\n}\n")
        }
        return css.trimEnd() + newRule
    }

    /**
     * Merges incoming CSS declarations into existing CSS declarations.
     * Overwrites matching properties (e.g. `font-size`, `color`, `background`, `text-shadow`)
     * while preserving untouched properties (e.g. `font-weight`, `width`, `height`).
     *
     * Handles multi-line statements (like complex text-shadows or gradients) safely without syntax corruption.
     */
    fun mergeDeclarations(existingBody: String, newDeclarations: String): String {
        val existingMap = parseDeclarations(existingBody)
        val newMap = parseDeclarations(newDeclarations)

        for ((prop, value) in newMap) {
            existingMap[prop] = value
        }

        return existingMap.entries.joinToString("\n") { (prop, value) ->
            "    $prop: $value;"
        }
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
            val cleanSelector = selector.replace(Regex("""/\*[\s\S]*?\*/"""), "").trim()
            if (cleanSelector.isNotBlank()) {
                rules.add(cleanSelector to body)
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
     * Parses semicolon-separated key-value declarations into a LinkedHashMap.
     * Safely handles multi-line property values (e.g. multi-line text-shadows or multi-tier gradients).
     */
    fun parseDeclarations(declarationsText: String): LinkedHashMap<String, String> {
        val map = linkedMapOf<String, String>()
        val clean = declarationsText.replace(Regex("""/\*[\s\S]*?\*/"""), "")
        val statements = clean.split(';')
        for (statement in statements) {
            val trimmed = statement.trim()
            if (trimmed.isBlank()) continue
            val colonIdx = trimmed.indexOf(':')
            if (colonIdx != -1) {
                val key = trimmed.substring(0, colonIdx).trim().lowercase()
                val value = trimmed.substring(colonIdx + 1).trim()
                map[key] = value
            }
        }
        return map
    }
}
