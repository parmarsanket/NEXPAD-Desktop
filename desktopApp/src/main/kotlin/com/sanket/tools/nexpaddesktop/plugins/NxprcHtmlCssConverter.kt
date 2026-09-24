package com.sanket.tools.nexpaddesktop.plugins

import com.sanket.tools.nexpad.model.NexpadKeys
import com.sanket.tools.nexpad.nxprc.CompileResult
import com.sanket.tools.nexpad.nxprc.NxprcCategory
import com.sanket.tools.nexpad.nxprc.NxprcDocument
import com.sanket.tools.nexpad.nxprc.NxprcPackager
import com.sanket.tools.nexpad.nxprc.engine.dom.DomNode
import com.sanket.tools.nexpad.nxprc.engine.dom.HtmlDomParser

/**
 * Intelligent HTML / CSS / SVG to .nxprc Converter Facade.
 * Delegates compilation to the shared multiplatform NxprcPackager in :protocol.
 */
object NxprcHtmlCssConverter {

    /**
     * Converts raw HTML/CSS/SVG text into an NxprcDocument via shared :protocol engine.
     * Automatically normalizes AI-generated code (code block extraction, root rectification,
     * z-index stacking hierarchy enforcement, and CSS prefix normalization).
     */
    fun convert(
        source: String,
        id: String,
        name: String,
        category: String = NxprcCategory.BUTTON.id,
        defaultControl: String = NexpadKeys.A
    ): NxprcDocument {
        val clean = normalizeAiHtml(source)
        return NxprcPackager.compile(
            html = clean,
            id = id,
            name = name,
            category = category,
            defaultControl = defaultControl
        )
    }

    /**
     * Converts raw HTML/CSS/SVG text into a [CompileResult] containing the [NxprcDocument]
     * and any compiler warnings for CSS properties that were dropped or approximated.
     * Automatically normalizes AI-generated code before compilation.
     */
    fun convertWithWarnings(
        source: String,
        id: String,
        name: String,
        category: String = "BUTTON",
        defaultControl: String = NexpadKeys.A
    ): CompileResult {
        val clean = normalizeAiHtml(source)
        return NxprcPackager.compileWithWarnings(
            html = clean,
            id = id,
            name = name,
            category = category,
            defaultControl = defaultControl
        )
    }

    /**
     * Comprehensive, industry-standard normalization pipeline for AI-generated HTML/CSS/SVG code:
     * 1. [extractCleanMarkup]: Strips Markdown fences, conversational envelope text, scripts, and unsafe handlers.
     * 2. [inlineCssCustomProperties]: Pre-evaluates :root CSS variables (var(--...)) into concrete values for styles.
     * 3. [normalizeCssVendorPrefixes]: Bi-directionally synchronizes vendor prefixes (-webkit-clip-path <-> clip-path).
     * 4. [normalizeSvgElements]: Auto-completes missing viewBox and namespace attributes on <svg> tags.
     * 5. [resolveStructuralStackingInversions]: AST/DOM-driven semantic occlusion remediation (NO hardcoded class names).
     * 6. [ensureRootComponentContract]: Guarantees a single root <button> with spring micro-physics and active state.
     */
    fun normalizeAiHtml(source: String, rootClassHint: String? = null): String {
        if (source.isBlank()) return source
        var clean = extractCleanMarkup(source)
        clean = inlineCssCustomProperties(clean)
        clean = normalizeCssVendorPrefixes(clean)
        clean = normalizeSvgElements(clean)
        clean = resolveStructuralStackingInversions(clean)
        clean = ensureRootComponentContract(clean, rootClassHint)
        return clean
    }

    private fun extractCleanMarkup(source: String): String {
        var clean = source.trim()

        // Extract from Markdown code fence if present
        if (clean.contains("```")) {
            val codeBlockRegex = Regex("""```(?:html|xml)?\s*([\s\S]*?)\s*```""", RegexOption.IGNORE_CASE)
            val match = codeBlockRegex.find(clean)
            if (match != null) {
                clean = match.groupValues[1].trim()
            } else {
                clean = clean.replace(Regex("""^```(?:html|xml)?\s*""", RegexOption.IGNORE_CASE), "")
                    .replace(Regex("""\s*```$"""), "").trim()
            }
        }

        // Strip conversational text preceding first valid markup start
        val docStartIdx = listOf(
            clean.indexOf("<!DOCTYPE", ignoreCase = true),
            clean.indexOf("<!--", ignoreCase = true),
            clean.indexOf("<html", ignoreCase = true),
            clean.indexOf("<head", ignoreCase = true),
            clean.indexOf("<button", ignoreCase = true),
            clean.indexOf("<style", ignoreCase = true),
            clean.indexOf("<div", ignoreCase = true),
            clean.indexOf("<svg", ignoreCase = true)
        ).filter { it >= 0 }.minOrNull()

        if (docStartIdx != null && docStartIdx > 0) {
            clean = clean.substring(docStartIdx).trim()
        }

        // Strip trailing conversational chatter after </html>, </button>, or </svg>
        val htmlEndIdx = clean.lastIndexOf("</html>", ignoreCase = true)
        if (htmlEndIdx != -1) {
            clean = clean.substring(0, htmlEndIdx + "</html>".length).trim()
        } else {
            val btnEndIdx = clean.lastIndexOf("</button>", ignoreCase = true)
            if (btnEndIdx != -1) {
                clean = clean.substring(0, btnEndIdx + "</button>".length).trim()
            } else {
                val svgEndIdx = clean.lastIndexOf("</svg>", ignoreCase = true)
                if (svgEndIdx != -1) {
                    clean = clean.substring(0, svgEndIdx + "</svg>".length).trim()
                }
            }
        }

        // Remove <script> tags and inline script event handlers
        clean = clean.replace(Regex("""<script\b[^<]*(?:(?!<\/script>)<[^<]*)*<\/script>""", RegexOption.IGNORE_CASE), "")
        clean = clean.replace(Regex("""\son\w+\s*=\s*(["'][^"']*["']|[^\s>]+)""", RegexOption.IGNORE_CASE), "")

        return clean
    }

    private fun inlineCssCustomProperties(html: String): String {
        val varDefRegex = Regex("""--([a-zA-Z0-9_-]+)\s*:\s*([^;]+);""")
        val variables = mutableMapOf<String, String>()

        for (match in varDefRegex.findAll(html)) {
            val name = match.groupValues[1].trim()
            val value = match.groupValues[2].trim()
            // Do not inline spring physics properties (compiler parses them directly from :root)
            if (!name.startsWith("spring-") && name != "press-scale") {
                variables[name] = value
            }
        }

        if (variables.isEmpty()) return html

        var resolvedVars = variables.toMutableMap()
        for (pass in 0 until 5) {
            var changed = false
            for ((k, v) in resolvedVars) {
                if (v.contains("var(--")) {
                    var newV = v
                    for ((subK, subV) in resolvedVars) {
                        if (!subV.contains("var(--$subK)")) {
                            val pattern = Regex("""var\(\s*--${Regex.escape(subK)}(?:\s*,\s*[^)]+)?\s*\)""")
                            if (pattern.containsMatchIn(newV)) {
                                newV = newV.replace(pattern, subV)
                                changed = true
                            }
                        }
                    }
                    if (newV != v) {
                        resolvedVars[k] = newV
                        changed = true
                    }
                }
            }
            if (!changed) break
        }

        val styleTagRegex = Regex("""<style[^>]*>([\s\S]*?)</style>""", RegexOption.IGNORE_CASE)
        val fallbackRegex = Regex("""var\(\s*--[a-zA-Z0-9_-]+\s*,\s*([^)]+)\)""")
        return styleTagRegex.replace(html) { match ->
            var css = match.groupValues[1]
            for ((k, v) in resolvedVars) {
                if (!v.contains("var(--")) {
                    val varRefRegex = Regex("""var\(\s*--${Regex.escape(k)}(?:\s*,\s*[^)]+)?\s*\)""")
                    css = css.replace(varRefRegex, v)
                }
            }
            // Resolve any remaining undefined variables that provided fallback values
            css = css.replace(fallbackRegex) { it.groupValues[1].trim() }
            "<style>${css}</style>"
        }
    }

    private fun normalizeCssVendorPrefixes(html: String): String {
        var res = html
        if (res.contains("-webkit-clip-path", ignoreCase = true) && !res.contains("clip-path:", ignoreCase = true)) {
            res = res.replace(Regex("""-webkit-clip-path\s*:\s*([^;]+);""", RegexOption.IGNORE_CASE)) {
                "-webkit-clip-path: ${it.groupValues[1]}; clip-path: ${it.groupValues[1]};"
            }
        } else if (res.contains("clip-path:", ignoreCase = true) && !res.contains("-webkit-clip-path:", ignoreCase = true)) {
            res = res.replace(Regex("""(?<!-webkit-)clip-path\s*:\s*([^;]+);""", RegexOption.IGNORE_CASE)) {
                "clip-path: ${it.groupValues[1]}; -webkit-clip-path: ${it.groupValues[1]};"
            }
        }
        return res
    }

    private fun normalizeSvgElements(html: String): String {
        val svgTagRegex = Regex("""<svg\b([^>]*)>""", RegexOption.IGNORE_CASE)
        return svgTagRegex.replace(html) { match ->
            var attrs = match.groupValues[1]
            if (!attrs.contains("xmlns", ignoreCase = true)) {
                attrs = "$attrs xmlns=\"http://www.w3.org/2000/svg\""
            }
            if (!attrs.contains("viewBox", ignoreCase = true)) {
                val wMatch = Regex("""width\s*=\s*["']?(\d+(?:\.\d+)?)p?x?["']?""", RegexOption.IGNORE_CASE).find(attrs)
                val hMatch = Regex("""height\s*=\s*["']?(\d+(?:\.\d+)?)p?x?["']?""", RegexOption.IGNORE_CASE).find(attrs)
                val w = wMatch?.groupValues?.get(1) ?: "100"
                val h = hMatch?.groupValues?.get(1) ?: "100"
                attrs = "$attrs viewBox=\"0 0 $w $h\""
            }
            "<svg$attrs>"
        }
    }

    private fun resolveStructuralStackingInversions(html: String): String {
        val parsed = try { HtmlDomParser.parse(html) } catch (e: Throwable) { return html }
        val root = parsed.root

        // 1. Identify all SVG vector elements in the DOM tree
        val svgNodes = root.findByTag("svg")
        if (svgNodes.isEmpty()) return html

        val svgIdentifiers = mutableSetOf<String>()
        svgIdentifiers.add("svg")
        for (svg in svgNodes) {
            svg.id?.let { svgIdentifiers.add("#${it.lowercase()}") }
            svg.classNames.forEach { svgIdentifiers.add(".${it.lowercase()}") }
        }

        // 2. Identify pure surface / container elements (elements with no SVG children and no direct text)
        fun isDescendantOfAny(node: DomNode, targets: List<DomNode>): Boolean {
            var curr = node.parent
            while (curr != null) {
                if (targets.contains(curr)) return true
                curr = curr.parent
            }
            return false
        }

        val surfaceIdentifiers = mutableSetOf<String>()
        fun scanSurfaces(node: DomNode) {
            val isRoot = node.tag.equals("button", true) || node.tag.equals("root", true) || node.tag.equals("body", true)
            val isSvgOrDescendant = node.tag.equals("svg", true) || isDescendantOfAny(node, svgNodes)
            val hasSvgChild = node.findByTag("svg").isNotEmpty()
            val hasDirectText = node.textContent.isNotBlank()

            if (!isRoot && !isSvgOrDescendant && !hasSvgChild && !hasDirectText) {
                node.id?.let { surfaceIdentifiers.add("#${it.lowercase()}") }
                node.classNames.forEach { surfaceIdentifiers.add(".${it.lowercase()}") }
            }
            node.children.forEach { scanSurfaces(it) }
        }
        scanSurfaces(root)

        // 3. Scan CSS rules across all <style> blocks
        val styleTagRegex = Regex("""<style[^>]*>([\s\S]*?)</style>""", RegexOption.IGNORE_CASE)
        val styleBlocks = styleTagRegex.findAll(html).toList()
        if (styleBlocks.isEmpty()) return html

        val ruleRegex = Regex("""([^{]+)\{([^}]+)\}""")
        data class RuleInfo(val selector: String, val body: String, val zIndex: Int?)
        val allRules = mutableListOf<RuleInfo>()
        for (block in styleBlocks) {
            val css = block.groupValues[1]
            for (match in ruleRegex.findAll(css)) {
                val sel = match.groupValues[1].trim()
                val body = match.groupValues[2]
                val z = Regex("""z-index\s*:\s*(\d+)""").find(body)?.groupValues?.get(1)?.toIntOrNull()
                allRules.add(RuleInfo(sel, body, z))
            }
        }

        // Find the lowest explicit z-index among SVG vector layers
        val minSvgZ = allRules.filter { r ->
            r.zIndex != null && svgIdentifiers.any { r.selector.lowercase().contains(it) }
        }.mapNotNull { it.zIndex }.minOrNull() ?: 6

        // Find non-SVG container rules with a background and z-index >= minSvgZ
        val inversionSelectors = mutableSetOf<String>()
        for (r in allRules) {
            if (r.zIndex != null && r.zIndex >= minSvgZ) {
                val hasBg = r.body.contains("background", ignoreCase = true) || r.body.contains("background-color", ignoreCase = true)
                val matchesSvg = svgIdentifiers.any { r.selector.lowercase().contains(it) }
                val isPseudoGloss = r.selector.contains("::before") || r.selector.contains("::after")
                if (!matchesSvg && !isPseudoGloss && hasBg) {
                    val isSurface = surfaceIdentifiers.any { r.selector.lowercase().contains(it) }
                    val isClassOrId = r.selector.trim().startsWith(".") || r.selector.trim().startsWith("#")
                    if (isSurface || isClassOrId) {
                        inversionSelectors.add(r.selector.trim())
                    }
                }
            }
        }

        if (inversionSelectors.isEmpty()) return html

        val targetZ = maxOf(2, minSvgZ - 2)
        return styleTagRegex.replace(html) { match ->
            var css = match.groupValues[1]
            for (invSel in inversionSelectors) {
                val escaped = Regex.escape(invSel)
                val selRulePattern = Regex("""(${escaped}\s*\{[^}]*?z-index\s*:\s*)(\d+)([^}]*\})""")
                css = css.replace(selRulePattern) { m ->
                    "${m.groupValues[1]}$targetZ${m.groupValues[3]}"
                }
            }
            "<style>${css}</style>"
        }
    }

    private fun ensureRootComponentContract(html: String, rootClassHint: String? = null): String {
        var res = html
        // If root is a <div>, convert top-level container to <button>
        if (!res.contains("<button", ignoreCase = true)) {
            val hintRegex = rootClassHint?.let { Regex("""<div(\s+[^>]*class\s*=\s*["'][^"']*${Regex.escape(it)}[^"']*["'][^>]*)>""", RegexOption.IGNORE_CASE) }
            val divBtnRegex = Regex("""<div(\s+[^>]*class\s*=\s*["'][^"']*(?:btn|button|pad|control|ctl)[^"']*["'][^>]*)>""", RegexOption.IGNORE_CASE)
            val anyDivRegex = Regex("""<div\b([^>]*)>""", RegexOption.IGNORE_CASE)
            val match = hintRegex?.find(res) ?: if (divBtnRegex.containsMatchIn(res)) divBtnRegex.find(res) else anyDivRegex.find(res)
            if (match != null) {
                // Find matching closing </div> using depth tracking starting from match.range.last + 1
                val openTagEnd = match.range.last + 1
                val divTagRegex = Regex("""</?div\b[^>]*>""", RegexOption.IGNORE_CASE)
                var depth = 1
                var matchingEndDivRange: IntRange? = null
                for (divMatch in divTagRegex.findAll(res, openTagEnd)) {
                    val tagText = divMatch.value
                    if (tagText.startsWith("</", ignoreCase = true)) {
                        depth--
                        if (depth == 0) {
                            matchingEndDivRange = divMatch.range
                            break
                        }
                    } else if (!tagText.endsWith("/>")) {
                        depth++
                    }
                }

                if (matchingEndDivRange != null) {
                    res = res.substring(0, matchingEndDivRange.first) + "</button>" + res.substring(matchingEndDivRange.last + 1)
                    res = res.substring(0, match.range.first) + "<button${match.groupValues[1]}>" + res.substring(match.range.last + 1)
                } else {
                    val lastDivIdx = res.lastIndexOf("</div>", ignoreCase = true)
                    if (lastDivIdx != -1) {
                        res = res.substring(0, lastDivIdx) + "</button>" + res.substring(lastDivIdx + 6)
                    }
                    res = res.replaceRange(match.range, "<button${match.groupValues[1]}>")
                }
            }
        }

        // Discover root class name from <button>
        val rootClassRegex = Regex("""<button\b[^>]*class\s*=\s*["']([^"'\s]+)[^"']*["']""", RegexOption.IGNORE_CASE)
        val discoveredClass = rootClassRegex.find(res)?.groupValues?.get(1) ?: rootClassHint

        val activeSelector = if (!discoveredClass.isNullOrBlank() && discoveredClass != "button") {
            ".$discoveredClass:active, button:active"
        } else {
            "button:active"
        }

        // If no <style> block exists, inject one with default spring physics
        if (!res.contains("<style", ignoreCase = true)) {
            val springPhysicsBlock = "<style>\n  :root {\n    --spring-damping: 0.68;\n    --spring-stiffness: 440;\n    --press-scale: 0.92;\n  }\n  $activeSelector { transform: scale(0.92) translateY(2px); }\n</style>\n"
            val bodyIdx = res.indexOf("<body", ignoreCase = true)
            res = if (bodyIdx != -1) {
                val afterBody = res.indexOf(">", bodyIdx) + 1
                res.substring(0, afterBody) + "\n" + springPhysicsBlock + res.substring(afterBody)
            } else {
                springPhysicsBlock + res
            }
        } else {
            // If :root does not contain spring physics, inject them into the first <style> block
            if (!res.contains("--spring-damping")) {
                val styleTagRegex = Regex("""<style[^>]*>""", RegexOption.IGNORE_CASE)
                val match = styleTagRegex.find(res)
                if (match != null) {
                    val insertIdx = match.range.last + 1
                    val springPhysicsBlock = "\n    :root {\n      --spring-damping: 0.68;\n      --spring-stiffness: 440;\n      --press-scale: 0.92;\n    }\n"
                    res = res.substring(0, insertIdx) + springPhysicsBlock + res.substring(insertIdx)
                }
            }
            // If no :active rule is present in CSS, inject fallback active selector
            if (!res.contains(":active", ignoreCase = true)) {
                val styleEndRegex = Regex("""</style>""", RegexOption.IGNORE_CASE)
                val match = styleEndRegex.find(res)
                if (match != null) {
                    val insertIdx = match.range.first
                    val fallbackActive = "\n  $activeSelector { transform: scale(var(--press-scale, 0.92)) translateY(2px); }\n"
                    res = res.substring(0, insertIdx) + fallbackActive + res.substring(insertIdx)
                }
            }
        }

        return res
    }

    /** Pre-built HTML/CSS templates for instant testing (Delegated to [NxprcPresets]) */
    val PRESET_ULTRA_NEXPAD_A get() = NxprcPresets.PRESET_ULTRA_NEXPAD_A
    val PRESET_CYBER_REACTOR get() = NxprcPresets.PRESET_CYBER_REACTOR
    val PRESET_CRIMSON_OCTA get() = NxprcPresets.PRESET_CRIMSON_OCTA
    val PRESET_SPEED_TURBO get() = NxprcPresets.PRESET_SPEED_TURBO
    val PRESET_NEO_TACTILE_A get() = NxprcPresets.PRESET_NEO_TACTILE_A
    val PRESET_NEO_TACTILE_B get() = NxprcPresets.PRESET_NEO_TACTILE_B
    val PRESET_NEO_TACTILE_X get() = NxprcPresets.PRESET_NEO_TACTILE_X
    val PRESET_NEO_TACTILE_Y get() = NxprcPresets.PRESET_NEO_TACTILE_Y
    val PRESET_DPAD_UP get() = NxprcPresets.PRESET_DPAD_UP
    val PRESET_DPAD_DOWN get() = NxprcPresets.PRESET_DPAD_DOWN
    val PRESET_DPAD_LEFT get() = NxprcPresets.PRESET_DPAD_LEFT
    val PRESET_DPAD_RIGHT get() = NxprcPresets.PRESET_DPAD_RIGHT
    val PRESET_DPAD_CROSS get() = NxprcPresets.PRESET_DPAD_CROSS
    val PRESET_TRIGGER_LT get() = NxprcPresets.PRESET_TRIGGER_LT
    val PRESET_TRIGGER_RT get() = NxprcPresets.PRESET_TRIGGER_RT
    val PRESET_BUMPER_LB get() = NxprcPresets.PRESET_BUMPER_LB
    val PRESET_BUMPER_RB get() = NxprcPresets.PRESET_BUMPER_RB
    val PRESET_THUMBSTICK_LS get() = NxprcPresets.PRESET_THUMBSTICK_LS
    val PRESET_THUMBSTICK_RS get() = NxprcPresets.PRESET_THUMBSTICK_RS
    val PRESET_SYSTEM_MENU get() = NxprcPresets.PRESET_SYSTEM_MENU
    val PRESET_SYSTEM_VIEW get() = NxprcPresets.PRESET_SYSTEM_VIEW
    val PRESET_SYSTEM_HOME get() = NxprcPresets.PRESET_SYSTEM_HOME
    val PRESET_STICK_BUTTON_LSB get() = NxprcPresets.PRESET_STICK_BUTTON_LSB
    val PRESET_STICK_BUTTON_RSB get() = NxprcPresets.PRESET_STICK_BUTTON_RSB
    val PRESET_TOUCHPAD_LTP get() = NxprcPresets.PRESET_TOUCHPAD_LTP
    val PRESET_TOUCHPAD_RTP get() = NxprcPresets.PRESET_TOUCHPAD_RTP

    /** Reference templates (structure guide only) delegated to [NxprcPresets]. */
    fun getReferenceTemplate(control: String): String = NxprcPresets.getReferenceTemplate(control)
    fun getReferenceTemplate(control: String, category: String): String = NxprcPresets.getReferenceTemplate(control, category)

    /** Compiler syntax skeletons delegated to [NxprcPresets]. */
    fun getSyntaxSkeleton(control: String, category: String, widthDp: Int, heightDp: Int): String =
        NxprcPresets.getSyntaxSkeleton(control, category, widthDp, heightDp)

    /**
     * Generates an in-depth, parameter-driven AI prompt tailored specifically to the target button type.
     * Delegates to [NxprcAiPromptBuilder.buildPrompt].
     */
    fun generateAiPrompt(
        control: String,
        category: String,
        widthDp: Int,
        heightDp: Int,
        options: AiDesignOptions = AiDesignOptions()
    ): String = NxprcAiPromptBuilder.buildPrompt(control, category, widthDp, heightDp, options)

    /**
     * Generates a targeted repair/rectification prompt to guide an AI model in fixing
     * compiler warnings or syntax errors from a previous generation.
     * Delegates to [NxprcAiPromptBuilder.buildRepairPrompt].
     */
    fun generateRepairPrompt(
        previousHtml: String,
        warnings: List<String> = emptyList(),
        errors: List<String> = emptyList(),
        control: String? = null,
        category: String? = null,
        options: AiDesignOptions = AiDesignOptions()
    ): String = NxprcAiPromptBuilder.buildRepairPrompt(
        previousHtml = previousHtml,
        warnings = warnings,
        errors = errors,
        control = control,
        category = category,
        options = options
    )
}
