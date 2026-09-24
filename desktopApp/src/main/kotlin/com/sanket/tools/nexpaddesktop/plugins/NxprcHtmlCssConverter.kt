package com.sanket.tools.nexpaddesktop.plugins

import com.sanket.tools.nexpad.category.CategoryManager
import com.sanket.tools.nexpad.category.CategoryType
import com.sanket.tools.nexpad.category.ComponentType
import com.sanket.tools.nexpad.category.ControlKey
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
    fun normalizeAiHtml(source: String): String {
        if (source.isBlank()) return source
        var clean = extractCleanMarkup(source)
        clean = inlineCssCustomProperties(clean)
        clean = normalizeCssVendorPrefixes(clean)
        clean = normalizeSvgElements(clean)
        clean = resolveStructuralStackingInversions(clean)
        clean = ensureRootComponentContract(clean)
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

    private fun ensureRootComponentContract(html: String): String {
        var res = html
        // If root is a <div>, convert top-level container to <button>
        if (!res.contains("<button", ignoreCase = true)) {
            val divBtnRegex = Regex("""<div(\s+[^>]*class\s*=\s*["'][^"']*(?:btn|button|pad|control)[^"']*["'][^>]*)>""", RegexOption.IGNORE_CASE)
            val anyDivRegex = Regex("""<div\b([^>]*)>""", RegexOption.IGNORE_CASE)
            val match = if (divBtnRegex.containsMatchIn(res)) divBtnRegex.find(res) else anyDivRegex.find(res)
            if (match != null) {
                res = res.replaceRange(match.range, "<button${match.groupValues[1]}>")
                val lastDivIdx = res.lastIndexOf("</div>", ignoreCase = true)
                if (lastDivIdx != -1) {
                    res = res.substring(0, lastDivIdx) + "</button>" + res.substring(lastDivIdx + 6)
                }
            }
        }

        // If no <style> block exists, inject one with default spring physics
        if (!res.contains("<style", ignoreCase = true)) {
            val springPhysicsBlock = "<style>\n  :root {\n    --spring-damping: 0.68;\n    --spring-stiffness: 440;\n    --press-scale: 0.92;\n  }\n  button:active { transform: scale(0.92) translateY(2px); }\n</style>\n"
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
            // If no :active rule is present in CSS, inject fallback button:active
            if (!res.contains(":active", ignoreCase = true)) {
                val styleEndRegex = Regex("""</style>""", RegexOption.IGNORE_CASE)
                val match = styleEndRegex.find(res)
                if (match != null) {
                    val insertIdx = match.range.first
                    val fallbackActive = "\n  button:active { transform: scale(var(--press-scale, 0.92)) translateY(2px); }\n"
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
     * Generates an in-depth, specialized AI Prompt tailored specifically to the target button type.
     * Delegates to dedicated generators for ABXY, D-PAD, TRIGGERS, BUMPERS, JOYSTICKS, STICK BUTTONS, TOUCHPADS, and SYSTEM buttons.
     */
    fun generateAiPrompt(
        control: String,
        category: String,
        widthDp: Int,
        heightDp: Int
    ): String {
        val ctrl = ControlKey.fromIdentifier(control)
        if (ctrl == ControlKey.LTP || ctrl == ControlKey.RTP ||
            (ctrl?.componentType == ComponentType.TOUCHPAD && ctrl.categoryType == CategoryType.STICKS)) {
            return generateTouchpadPrompt(control, widthDp, heightDp)
        }

        if (ctrl == ControlKey.LSB || ctrl == ControlKey.RSB ||
            (ctrl?.componentType == ComponentType.BUTTON && ctrl.categoryType == CategoryType.STICKS)) {
            return generateStickButtonPrompt(control, widthDp, heightDp)
        }

        val catType = CategoryType.fromIdentifier(category)
            ?: CategoryManager.findCategoryForControl(control)?.type
        return when (catType) {
            CategoryType.TRIGGERS -> generateTriggerPrompt(control, widthDp, heightDp)
            CategoryType.BUMPERS -> generateBumperPrompt(control, widthDp, heightDp)
            CategoryType.DPAD -> generateDpadPrompt(control, widthDp, heightDp)
            CategoryType.STICKS -> generateStickPrompt(control, widthDp, heightDp)
            CategoryType.SYSTEM,
            CategoryType.MACROS -> generateSystemPrompt(control, widthDp, heightDp)
            CategoryType.ABXY,
            null -> generateAbxyPrompt(control, widthDp, heightDp)
        }
    }

    private fun genAiHeader(): String = """
# NEXPAD VIRTUAL CONTROLLER COMPONENT SPECIFICATION
**Protocol Standard: NXPRC 10/10 Vector Engine Architecture**
**Engineered & Validated for Frontier & Compact AI Models:**
- OpenAI ChatGPT (GPT-4o, GPT-4, o1, o3-mini)
- Anthropic Claude (Claude 3.7 Sonnet, Claude 3.5 Sonnet)
- Google Gemini (Gemini 2.5 Flash / Pro, Gemini 2.0 Flash, Gemini 1.5 Pro)
- DeepSeek (DeepSeek-V3, DeepSeek-R1)
- xAI Grok (Grok 3, Grok 2)
- Local & Open Weights Models (Qwen, Llama, Mistral, Gemma)

## CORE RULES (QUICK SUMMARY FOR ALL MODELS):
1. Build ONE virtual controller component inside a single `<button>` element.
2. Follow the user's visual request first — user customization always wins within compiler boundaries.
3. Keep the component self-contained: one `<style>` block, system fonts, zero external assets.
4. Use only supported HTML/CSS/SVG primitives (no unsupported web page APIs).
5. Preserve the component's interaction meaning (category semantics), not a mandatory shape.
6. Make the design visually coherent with physically believable depth and lighting.
7. Use creativity when details are unspecified — never default to a generic circle unless requested or natural.
8. Design with restraint: avoid visual clutter; prefer the minimum number of layers required to achieve the requested aesthetic clearly; do not remove meaningful visual detail merely to reduce layer count.
9. Ensure the label/icon remains clearly readable with strong contrast in an unrotated DOM text node.
10. Return ONLY the complete, self-contained HTML/CSS inside one code block.
11. NEXPAD supports dual button labeling styles (Xbox: A, B, X, Y, LB, RB, LT, RT, LSB, RSB vs PlayStation: ✕, ○, □, △, L1, R1, L2, R2, L3, R3) and dynamically translates standard controller labels at runtime while preserving custom action text (e.g. ATTACK, DASH, JUMP).
""".trimIndent()

private fun engineBoundaries(rootClass: String): String = """
### SECTION 1 — INSTRUCTION PRIORITY & CONFLICT RESOLUTION
When instructions conflict, resolve them in this strict order of authority:
1. **Non-Negotiable Compiler Safety** [GLOBAL-REQUIRED] (Single button root, px bounds, DOM text, self-contained document, no external assets or scripts).
2. **User's Explicit Customization** [USER-OVERRIDE] [USER OVERRIDE] (Highest design authority — user's artistic style, shape, palette, and theme always supersede defaults).
3. **Component Semantics** [COMPONENT-REQUIRED] (Preserve interaction meaning: tappable, directional, analog, etc.).
4. **Accessibility & Readability** [GLOBAL-REQUIRED] [REQUIRED] (High-contrast label legibility, touch target visibility).
5. **Design Quality Principles** [RECOMMENDED] (Physical coherence, balanced hierarchy, believable depth).
6. **Category Defaults** [RECOMMENDED] (Color palette suggestions, default glyphs used when user specifies none).
7. **Optional Inspiration** [OPTIONAL] (Theme suggestions, optional decorative flair).
8. **Starter-Template Examples [NON-BINDING SYNTAX REFERENCE]** (Syntax structure only — never copy its geometry, proportions, colors, materials, layer count, visual hierarchy, or silhouette unless those properties are independently required by the component contract or explicitly requested by the user).

> **The Golden Rule**: The user's visual and artistic instructions always win over defaults and recommendations, provided they remain compatible with the required compiler contract and component semantics.
> **Conflict Rule**: User instructions always take precedence over optional recommendations or category defaults.
> **Template Rule**: Starter-template examples are illustrative syntax only and should never override explicit user choices. Never imitate their colors, shapes, gradients, or materials when fulfilling user requests.

### SECTION 2 — RULE CLASSIFICATION HIERARCHY
- **[GLOBAL-REQUIRED] / [REQUIRED]**: Platform/engine constraints. Violation causes compiler rejection.
- **[COMPONENT-REQUIRED]**: Required for this component's interaction model (e.g. active feedback, control key).
- **[USER-OVERRIDE] / [USER OVERRIDE]**: User's explicit aesthetic requests. Highest design authority within compiler boundaries.
- **[RECOMMENDED]**: Proven design patterns for quality, depth, and touch affordance. Use unless the user's concept calls for another approach.
- **[OPTIONAL]**: Primitives and effects (SVG paths, conic gradients, filter nodes) to use only when they enhance the requested aesthetic.
- **[NON-BINDING SYNTAX REFERENCE]**: Architectural syntax example only. Never use its aesthetic properties as design anchors.

### SECTION 3 — USER CREATIVE AUTHORITY & FREE-HAND MODE
**Strict on Code, Free on Design:**
```
Compiler Contract:    STRICT  (Single button, valid CSS/SVG primitives, explicit bounds)
User Visual Concept:  FREE    (Theme, character, style, geometry completely replace defaults)
AI Artistic Choice:   FREE    (Infer unspecified lighting, materials, palette, vector details)
Unsupported Details:  ADAPT   (Map impossible requests to nearest compilable representation)
Output Format:        STRICT  (Single ```html ... ``` block, zero markdown conversational text)
```
1. **Concept-First Generation [USER-OVERRIDE]**: When the user specifies an artistic style, anime theme, gaming universe, creature, hero, or decorative motif (e.g. "cute pastel anime heart button", "cyberpunk neon skull", "Naruto chakra burst", "Art Deco brass compass"), that concept COMPLETELY REPLACES default styles. Do not merely tint a standard dark matte Xbox button! Re-imagine the geometry, color scheme, materials, lighting, and vector artwork to embody the requested concept.
2. **Free-Hand Mode (Creative Inference)**: When user customization details are unspecified, exercise autonomous creative judgment aligned with the overall theme. Choose geometry that fits the concept (do not default to a circle unless requested or natural). Choose lighting and depth that support the material.
3. **DO NOT SIMPLIFY A DESIGN UNLESS NECESSARY**: Use the full expressive capability of supported CSS/SVG when the user's concept benefits from it. Compiler limitations are implementation boundaries, not aesthetic instructions.

### SECTION 4 — INTERPRETATION & FIDELITY MODES
When interpreting user themes or character requests:
- **FAITHFUL**: Closely reproduce the requested character or emblem's visual language using clean SVG vector paths.
- **INSPIRED**: Create an original design strongly inspired by the theme's motifs, colors, and aesthetics.
- **ABSTRACT**: Capture the concept's core visual essence (signature silhouette, colorway, energy signature).
- **SEMANTICS PRESERVATION**: The component must remain recognizable as the requested controller control. Visual shape may change freely; interaction meaning may not.

### SECTION 5 — HARD COMPILER CONTRACT & STRICT BOUNDARIES
1. **Single compiled component [GLOBAL-REQUIRED]**: `<body>` must contain exactly one root `<button class="$rootClass" data-control="..." data-category="..." data-name="...">`. Keep every visual child inside it. The compiler selects this button and does not render a general web page.
2. **Portable self-contained document [GLOBAL-REQUIRED]**: Include one `<style>` block, one root button, and no external dependencies (no external `<link>`, `@import`, remote font files, or external web scripts). System fonts only.
3. **Explicit geometry & positioning [GLOBAL-REQUIRED]**: Use `px` dimensions for the root and visual children. Set `position: relative` on the root. Set `position: absolute`, `left`, `top`, `width`, and `height` on decorative children as needed. Supports `calc()` dynamic sizing (e.g. `width: calc(100% - 16px)`) and `aspect-ratio: 1`.
4. **Arbitrary polygon shapes & free geometry [GLOBAL-REQUIRED]**: Use `border-radius` or `clip-path: polygon(...)` for circles, capsules, stars, diamonds, hexagons, octagons, handmade, asymmetric, and organic silhouettes with any vertex count. The compiler maps polygon coordinates directly into native GPU Skia vector paths. `data-category` is metadata, not a shape instruction. Preserve the user's requested shape, proportions, and aesthetic.
5. **Physical 3D Layer Hierarchy & Z-Index Anti-Occlusion [GLOBAL-REQUIRED]**:
   ┌────────────────────────────────────────────────────────────────────────┐
   │ PHYSICAL 3D LAYER HIERARCHY (From Bottom/Lowest to Top/Highest)       │
   ├──────────┬─────────────────────────────────────────────────────────────┤
   │ Tier     │ Structural Component Role & Z-Index Range                   │
   ├──────────┼─────────────────────────────────────────────────────────────┤
   │ 0 .. 2   │ Housing, Outer Chassis Ring, Ambient Glow, Recessed Socket  │
   │ 3 .. 4   │ Main Keycap Face Plate / Center Body Surface (Opaque Base)  │
   │ 5 .. 7   │ Vector Emblem, Insignia, SVG Icons & Optical Halo Glow      │
   │ 8 .. 9   │ Center Typography, Primary Letterform & Technical Labels    │
   │ 10+      │ Translucent Specular Gloss Reflections (::before/::after)   │
   └──────────┴─────────────────────────────────────────────────────────────┘
   ⚠️ **CRITICAL ANTI-OCCLUSION & DOM ORDER PRINCIPLE**:
   - Physical keycap surface plates MUST ALWAYS sit underneath vector artwork, emblems, and typography.
   - NEVER assign an opaque surface plate a higher `z-index` than an emblem (`<svg>`, `.button-emblem`) or label (`.btn-label`)!
   - In HTML document flow, always declare the keycap face plate FIRST, the embedded `<svg>` vector emblem SECOND, and the label `<span>` THIRD. This guarantees 100% visual parity across all web browsers and native graphics engines.
6. **Text must be real DOM text without rotation [GLOBAL-REQUIRED]**: Put labels, legends, and decorative symbols in actual `<span>`/`<div>` text nodes. Multi-label layouts are fully supported. Do not use pseudo-element text with icons or emoji; pseudo-elements `::before`/`::after` may use `content: ""` only for painted layers. Keep text elements unrotated (`transform: rotate(...)` must NOT be applied to text nodes; NO TEXT ROTATION) for ultra-crisp GPU typography.
7. **Stable CSS only [GLOBAL-REQUIRED]**: Do not use `@media`, `@supports`, `:hover`, `:focus`, or `:focus-visible` (these are browser page-state features). Do not use browser `@keyframes` animations; dynamic touch buttons use `.$rootClass:active` tactile spring micro-physics for press actuation. Use `.$rootClass:active` only for press feedback.
8. **Optical filter rule [GLOBAL-REQUIRED]**: Use GPU `filter: blur()`, `brightness()`, `contrast()`, `saturate()`, `hue-rotate()` on CSS layers. Do not use `backdrop-filter` or `mix-blend-mode`.
9. **Tactile active interaction [COMPONENT-REQUIRED]**: Always define `.$rootClass:active { transform: scale(...) translateY(...); }` using the exact root class.
10. **Tactile spring micro-physics [COMPONENT-REQUIRED]**: Declare spring physics custom properties in `:root`:
    `--spring-damping: 0.68;`, `--spring-stiffness: 440;`, `--press-scale: 0.92;`

### SECTION 6 — COMPILER CAPABILITIES — WHAT PRIMITIVES ARE BEST FOR:
The NXPRC engine compiles HTML/CSS/SVG into hardware-accelerated Compose Canvas layers.

#### ✅ FULLY SUPPORTED — Use freely:
- **`radial-gradient`**: Best for spherical/concave shading, directional specular highlights, ambient glow, and radial illumination wells. Supports `circle at X% Y%`, `ellipse`, `closest-side`, `farthest-corner`, explicit `px`/`%` radii, and multi-stop color arrays.
- **`linear-gradient`**: Best for rake angles, directional light slope, horizontal specular sheen, and chamfer bevels. Supports angle (`135deg`), direction keywords (`to right`, `to bottom left`), turns and radians.
- **`conic-gradient`**: Best for brushed metallic bezels, segmented rotary dials, directional sheen rings, and mechanical textures. Supports `from Ndeg at X% Y%` syntax and degree-position color stops.
- **`box-shadow`**: Outset shadows for physical socket elevation and ambient halos; Inset shadows for 3D spherical bevel rims and recessed sockets.
- **`border-radius`**: Full per-corner control (`border-radius: 50%`, `border-radius: 14px 8px 20px 8px`). Use for circles, capsules, squircles, rounded rects.
- **`clip-path: polygon(...)`**: Custom silhouettes — stars, hexagons, diamonds, arrows, organic shields, any vertex count.
- **`opacity`**: Full layer opacity (0.0–1.0).
- **`transform`**: `rotate()`, `scale()`, `translate()`, `skew()` — on root and child layers.
- **`filter: blur(Npx)`**: GPU Gaussian blur on individual elements. ⚠️ Single function only (see PARTIAL below).
- **`filter: brightness(N)` / `saturate(N)` / `hue-rotate(Ndeg)`**: Color adjustments. ⚠️ Single function only.
- **Embedded `<svg>` & Vector Nodes**: Best for custom vector iconography, chevrons, emblems, and technical markings (`<path d="...">`, `<circle>`, `<rect>`, `<polygon>`, `<g>`). Supports `<defs>` paint servers (`<linearGradient id="...">`, `<radialGradient id="...">` with `<stop offset="..." stop-color="..." stop-opacity="...">`) referenced via `fill: url(#id)` or `stroke: url(#id)` in both direct attributes and CSS classes (`.my-shape { fill: url(#grad); }`).
- **Vector Emblem Glow Rule ✅**: Vector layers compile into raw hardware GPU Skia draw paths and do NOT compile SVG filter graphs. **Do NOT rely on SVG `<filter>` graphs (`<feGaussianBlur>`, `<feDropShadow>`) on vector paths for glow.** To create an optical halo or glow behind a vector emblem, use an underlying HTML/CSS `<span class="emblem-glow">` with `filter: blur(4px)` or `box-shadow: 0 0 16px <color>` placed underneath the `<svg>`!
- **SVG Transforms & Group Matrices ✅**: Full native support for SVG transformations (`transform="translate(x, y)"`, `rotate(deg, cx, cy)`, `scale(sx, sy)`, `matrix(a,b,c,d,e,f)`). Nested `<g transform="...">` groups and direct `<path transform="...">` elements are automatically compiled and geometry-baked at compile time. Use this freely for rotational symmetry, radial petals, insignias, and emblems!
- **`::before` / `::after`**: Painted decoration layers (`content: ""` only — no pseudo-element text).
- **Flexbox Layout**: Best for grouped items (menu bars, grip ribs, multi-label stacks), flow, and alignment (`display: flex`, `flex-direction`, `flex-wrap: wrap`, `gap`, `row-gap`, `column-gap`, `justify-content`, `align-items`).
- **`position: absolute`** with `left`, `top`, `width`, `height` in `px`: Explicit layer stacking.
- **`z-index`**: Layer draw order.
- **`calc()` Expressions**: Dynamic dimension math resolved at compile time. Fully supported for `width` and `height`. Examples: `width: calc(100% - 16px)` (inset ring), `height: calc(100% - 20px)`. Supports `+`, `-`, `%`, and `px` operands.
- **`aspect-ratio`**: Auto-derives the missing dimension from the explicit one. Examples: `aspect-ratio: 1` (perfect square/circle), `aspect-ratio: 16 / 9`. Use this on inner rings, icons, and decorative elements to guarantee proportional geometry.
- **Typographic Auto-Wrapping**: Real DOM text formatting with `font-size`, `font-weight`, `letter-spacing`, `line-height`, `text-shadow`, and multi-line wrapping via `white-space: normal | pre-line` and explicit newlines.
- **Modern CSS Colors**: Hex (`#rrggbbaa`), `rgb()`, `rgba()`, `hsl()`, `hwb()`, `oklch()`, and `color(display-p3 ...)`.

#### ⚠️ PARTIALLY SUPPORTED — Use with care:
- **`filter:` with multiple functions** (`filter: blur(4px) brightness(1.2)`): **Only the first function is compiled.** Use a single clear function per layer (`filter: blur(4px)` on one layer, `filter: brightness(1.2)` on another).
- **`conic-gradient` with >8 color stops**: Compiles correctly but may reduce GPU rendering performance. Prefer an SVG `<radialGradient>` paint server for very complex sweep gradients.
- **CSS custom properties (`var(--color)`) inside gradient stops**: Not reliably interpolated. Use **literal hex values** inside gradient color stops. `var()` is supported at the `:root` declaration level for spring physics only.

#### ❌ NOT SUPPORTED — Do NOT use (will be silently dropped):
- **`mix-blend-mode`**: Not supported. Use standard alpha compositing and layered gradients.
- **`backdrop-filter`**: Not supported. Use `filter:` applied to the element itself.
- **`@keyframes` / `animation:`**: Not supported. Use `--spring-stiffness` / `--spring-damping` for physics-based press animation; use SVG animations for decorative motion.
- **`transition:`**: Not supported. Press interactions use spring micro-physics compiled from `:active` + `--spring-*` variables.
- **`mask` / `mask-image`**: Not supported. Use `clip-path: polygon(...)` or `border-radius` for shape masking.
- **`perspective` / `rotateX()` / `rotateZ()` / 3D transforms**: Not supported. Use 2D `transform` only.
- **`display: grid`**: Not supported. Use `position: absolute` with explicit `px` coordinates or Flexbox for child layers.
- **`@media`, `@supports`, `:hover`, `:focus`**: Browser page-state features — not compiled. Use `.$rootClass:active` for press feedback only.
- **External assets**: No `@import`, no `<link>`, no remote fonts. System fonts only (`-apple-system`, `BlinkMacSystemFont`, `Segoe UI`, `Roboto`, `sans-serif`).

### SECTION 7 — ARCHITECTURAL PATTERN: HTML/CSS BUTTON SHELL + EMBEDDED SVG VECTOR EMBLEM
When the user requests a character, hero, creature, vehicle, weapon, insignia, or intricate graphic (e.g. Iron Man helmet / Arc Reactor, Ben 10 Omnitrix badge, Batman crest, Dragon / Skull emblem, Sports Car silhouette, Cyberpunk crosshair, Anime insignia, Tribal crest):

⚠️ FORBIDDEN ANTI-PATTERN (NEVER DO THIS): Never build complex character faces, vehicle contours, or intricate emblems out of dozens of nested HTML <div> shapes or CSS clip-paths. They are brittle and hard to maintain.

✅ MANDATORY DUAL-ENGINE ARCHITECTURE (ALWAYS DO THIS):
1. The Outer HTML/CSS Button Shell (<button class="$rootClass" ...>):
   - Handles the 3D physical tactile housing, surface material, perimeter bevel, specular curvature arc (`::after`), recessed socket shadows (`box-shadow`), and tactile active spring micro-physics (`--spring-damping: 0.68; --spring-stiffness: 440;` with `.$rootClass:active`).
2. The Embedded `<svg class="button-emblem" viewBox="0 0 100 100">` Vector Emblem:
   - Embedded directly inside the `<button>`.
   - Uses clean SVG vector paths (`<path d="...">`, `<polygon points="...">`, `<circle>`, `<ellipse>`, `<line>`) to draw the exact character, emblem, or insignia.
   - Sized appropriately to sit centered or docked on the button face (e.g., `width: 50px; height: 50px; position: absolute;` or flexbox child).
   - Supports `<defs>` gradient paint servers (`<linearGradient id="...">`, `<radialGradient id="...">` with `<stop>`) or solid vector fills and strokes.
   - ⚠️ **VECTOR GLOW PATTERN**: Do NOT rely on SVG `<filter>` graphs (`<feGaussianBlur>`, `<feColorMatrix>`) on vector paths for glow. Instead, place an underlying HTML/CSS `<span class="emblem-ambient">` with `filter: blur(4px)` or `box-shadow: 0 0 16px var(--accent-glow)` underneath the `<svg>` to cast a luminous ambient aura!
   - ✅ **SVG TRANSFORMS & ROTATIONS**: You can freely use `<g transform="translate(x,y) rotate(deg)">` or direct `<path transform="...">` to distribute or rotate shapes (such as radial petals, emblems, gear teeth, or insignia rays) around a center point, or bake coordinates directly. Both are fully compiled.
3. The High-Contrast Control Typography (<span class="btn-label">):
   - Real DOM text for the gamepad key ensuring instantaneous legibility during high-speed gaming.
   - ⚠️ **NO TEXT ROTATION**: Keep labels, sub-labels, and hardware text markings in straight, unrotated `<span>` elements (avoid `transform: rotate(...)` on text nodes to ensure 100% crisp subpixel font rasterization on mobile displays).

### SECTION 8 — DESIGN QUALITY CRITERIA & CONDITIONAL RESTRAINT
**DESIGN QUALITY CRITERIA**:
1. *Recognizability*: Instantly identifiable key identity during gameplay.
2. *Legibility*: High contrast label readable at small handheld touch scales.
3. *Touch Affordance*: Visually communicates pressability, depth, and tactile actuation.
4. *Visual Hierarchy*: Primary glyph stands out above decorative bezels and ambient halos.
5. *Material Coherence*: Shading, highlights, and borders reflect a consistent material (matte, metallic, neon, glass).
6. *Appropriate Depth*: Multi-tier inset/outset shadows creating realistic tactile socket recess.

**DESIGN RESTRAINT & VISUAL BALANCE**:
- Use the minimum number of layers necessary to express the user's concept clearly; do not remove meaningful visual detail merely to reduce layer count.
- Avoid unnecessary glow, excessive shadows, or decorative elements that visually compete with the button label.

### SECTION 9 — ADAPTATION RULES (HANDLING IMPOSSIBLE REQUESTS)
When a user request exceeds compiler limits or platform capabilities (e.g. 200-character text on a 96px button, 3D meshes, WebGL canvas shaders, video backgrounds, unsupported scripts):
1. Preserve the user's primary visual, structural, and thematic intent.
2. Adapt unsupported or impractical details to the nearest supported CSS/SVG representation.
3. Do not abandon the concept.
4. Do not explain limitations or output conversational excuses.
5. Return the best compilable implementation.

### SECTION 10 — VISUAL QA CHECKLIST (SELF-CHECK BEFORE OUTPUT)
Self-check before output:
- [ ] Compiler Safety: Exactly one root `<button class="$rootClass"` with matching `data-control`, `data-category`, and `data-name`.
- [ ] Real DOM Text: Real DOM text labels with strong contrast and readable font size in unrotated `<span>` (`Text must be real DOM text`).
- [ ] Explicit Coordinates: Explicit px dimensions on root and layered children (`Set position: absolute, left, top, width, and height`).
- [ ] Tactile Physics: Valid active state `.$rootClass:active` with spring micro-physics (`--spring-damping`, `--spring-stiffness`) in `:root`.
- [ ] Clean Engine Profile: No forbidden properties (`Do not use @media`, no external fonts, no external scripts, no `mix-blend-mode`).
- [ ] Restraint & Coherence: Preserves the user's requested shape and applies appropriate design restraint without stripping meaningful detail.
- [ ] Complex Graphics Architecture: If a character, hero, creature, vehicle, weapon, emblem, or intricate graphic is requested, uses an embedded `<svg class="button-emblem" viewBox="0 0 100 100">` vector element with clean `<path d="...">` rather than brittle CSS `<div>` hacks. For vector glow, use an underlying CSS `<span>` with `filter: blur()` or `box-shadow` (no SVG `<filter>` graphs).
- [ ] Geometry & Dimensions: If inset child rings or padded inner layers are used, prefer `calc()` (e.g. `width: calc(100% - 16px)`) and `aspect-ratio: 1`.
- [ ] Vector Emblem Glow: Did you use an underlying CSS `<span>` with `filter: blur()` or `box-shadow` instead of an SVG `<filter>` graph (`feGaussianBlur`) for emblem glow?
- [ ] Typography: Are all text labels and markings (`<span>`) kept straight without `transform: rotate(...)` (unrotated)?

### SECTION 11 — AUTHORITATIVE OUTPUT CONTRACT
To ensure reliable programmatic compilation, return ONLY the complete, self-contained HTML/CSS inside a single ```html ... ``` code block. Do NOT include any markdown conversation, explanations, or extraneous text outside it.
""".trimIndent()

    private fun generateAbxyPrompt(control: String, widthDp: Int, heightDp: Int): String {
        val (colorName, hexCode, rgbGlow, coreGrad) = when (control.uppercase()) {
            NexpadKeys.X -> Quadruple("Vibrant Sapphire Blue", "#00B0FF", "rgba(0, 176, 255, 0.6)", "linear-gradient(145deg, #0284c7 0%, #0369a1 50%, #0c4a6e 100%)")
            NexpadKeys.Y -> Quadruple("Radiant Solar Yellow", "#FFCC00", "rgba(255, 204, 0, 0.6)", "linear-gradient(145deg, #eab308 0%, #ca8a04 50%, #713f12 100%)")
            NexpadKeys.B -> Quadruple("Vibrant Crimson Red", "#FF3366", "rgba(255, 51, 102, 0.6)", "linear-gradient(145deg, #f43f5e 0%, #e11d48 50%, #881337 100%)")
            else -> Quadruple("Vibrant Emerald Green", "#4ADE80", "rgba(74, 222, 128, 0.6)", "linear-gradient(145deg, #10b981 0%, #059669 50%, #047857 100%)")
        }

        return """
${genAiHeader()}

You are an expert gamepad UI/UX designer and CSS shader artist creating a custom virtual controller Face Action Button for NEXPAD.

### TARGET COMPONENT IDENTITY:
- **Button Key [COMPONENT-REQUIRED]**: $control (Standard Gamepad Face Button)
- **Category [GLOBAL-REQUIRED]**: BUTTON
- **Target Dimensions [GLOBAL-REQUIRED]**: width: ${widthDp}px; height: ${heightDp}px; (canvas bounding box)
- **Standard Color Profile [RECOMMENDED]**: $colorName (Accent: $hexCode, Glow: $rgbGlow)
- **Standard Core [RECOMMENDED]**: $coreGrad

### CATEGORY SEMANTICS & INTERACTION MEANING:
- **Interaction Meaning [COMPONENT-REQUIRED]**: Momentary discrete user actuation with tactile depression and instant spring release.
- **Visual Affordance [RECOMMENDED]**: Prominent elevation, tactile socket well, clear pressability, high-contrast center label.
- **Optional Visual Language [OPTIONAL]**: Multi-stop radial gradients, specular highlight arcs, metallic chamfer rings, neon edge halos.
- **Geometry [USER-OVERRIDE]**: `data-category` is metadata, not a shape instruction. The silhouette is completely yours: circle, hexagon, rounded rect, diamond, shield, or organic silhouette. Preserve the user's requested shape.

### DEFAULT VISUAL PROFILE / VISUAL TARGET — CONSOLE/XBOX INDUSTRIAL REALISM:
When no specific custom aesthetic or character theme is requested by the user, adopt an authentic console-grade hardware aesthetic:
1. **Matte Polycarbonate Body & Optical Depth**: Rich dual-cast molding — deep chassis base tones (`#14171e`, `#1c202a`, `#08090c`) with crisp perimeter chamfer highlights, NOT flat monochrome or pure `#000`.
2. **Physical Contact Shadows & Recessed Socket**: Elevated dome seated inside a subtle recessed socket well (`box-shadow: 0 8px 24px rgba(0,0,0,0.65), inset 0 2px 4px rgba(255,255,255,0.4), inset 0 -6px 12px rgba(0,0,0,0.7)`).
3. **Restrained Detailing & Tactile Lighting**: Avoid unsolicited cyberpunk/neon glow clutter unless explicitly requested. Authentic face buttons feature crisp embossed letterforms, restrained subsurface luminance, and authentic optical gloss arcs.
4. **Legible High-Contrast Letterform**: Prominent, highly legible center glyph ($control) with multi-stop 3D text shadow that remains clear at handheld phone touch scales.

${engineBoundaries("nexpad-btn")}

### USER CUSTOMIZATION SCHEMA:
The schema is a convenience, not a limitation. Users may describe any additional visual, structural, material, symbolic, or interaction concept in SPECIAL INSTRUCTIONS or free-form text. The AI follows explicit user customization above all defaults:
- **STYLE**: [e.g. Cyberpunk 2077 / Glassmorphism / Brushed Gunmetal / Retro Arcade / Minimal Flat / Anime Mecha / Custom]
- **COLOR / ACCENT**: [e.g. Neon cyan & dark obsidian / Crimson & carbon / Custom palette (Default: $hexCode)]
- **SHAPE / SILHOUETTE**: [e.g. Faceted octagon / Smooth capsule / Organic shield / Asymmetric shard (Default: Circular)]
- **EMBLEM / GRAPHIC (OPTIONAL)**: [e.g. Embedded SVG vector emblem (`<svg viewBox="0 0 100 100"><path d="..."/></svg>`) for character art, hero logos, vehicle silhouettes, or intricate crests]
- **LABEL / GLYPH**: [e.g. "$control" / Custom text / SVG icon emblem (Default: "$control")]
- **MATERIAL / TEXTURE**: [e.g. Matte polycarbonate / Anodized aluminum / Smoked translucent glass / Stippled rubber]
- **LIGHTING & DEPTH**: [e.g. Top-left specular directional / Under-glow neon edge / Deep recessed socket]
- **TACTILE PHYSICS**: [e.g. Snappy micro-switch / Heavy spring depression / Soft fluid damping]
- **SPECIAL INSTRUCTIONS**: [Any specific visual elements, vector markings, or creative intent]

### OPTIONAL STARTER TEMPLATE — SYNTAX SKELETON [NON-BINDING SYNTAX REFERENCE ONLY]:
This template demonstrates document syntax only. Do NOT treat its colors, geometry, gradients, shadows, layer arrangement, typography, or proportions as design defaults. Build the visual design independently from the user's request:
```html
${getSyntaxSkeleton(control, "BUTTON", widthDp, heightDp)}
```

### OUTPUT FORMAT CONTRACT:
Return ONLY the complete, self-contained HTML/CSS inside a single ```html ... ``` code block. Do NOT include any markdown conversation, explanations, or extraneous text.
""".trimIndent()
    }

    private fun generateDpadPrompt(control: String, widthDp: Int, heightDp: Int): String {
        val arrowGlyph = when (control.uppercase()) {
            NexpadKeys.DOWN -> "▼"
            NexpadKeys.LEFT -> "◀"
            NexpadKeys.RIGHT -> "▶"
            NexpadKeys.DPAD -> "❖"
            else -> "▲"
        }

        return """
${genAiHeader()}

You are an expert gamepad UI/UX designer and CSS shader artist creating a custom virtual controller D-Pad Component for NEXPAD.

### TARGET COMPONENT IDENTITY:
- **Button Key [COMPONENT-REQUIRED]**: $control (${if (control.uppercase() == NexpadKeys.DPAD) "Unified 4-Way Cross Pad" else "Directional Arrow Button"})
- **Category [GLOBAL-REQUIRED]**: DPAD
- **Target Dimensions [GLOBAL-REQUIRED]**: width: ${widthDp}px; height: ${heightDp}px;
- **Directional Glyph [RECOMMENDED]**: $arrowGlyph

### CATEGORY SEMANTICS & INTERACTION MEANING:
- **Interaction Meaning [COMPONENT-REQUIRED]**: Directional navigation with crisp actuation along cardinal or diagonal axes.
- **Visual Affordance [RECOMMENDED]**: Clear directional affordance, central rocker pivot affordance, distinct directional touch zones.
- **Optional Visual Language [OPTIONAL]**: Recessed pivot well, laser-etched chevron markings, sloped directional gradients, tactile nubs.
- **Geometry [USER-OVERRIDE]**: `data-category` is metadata, not a shape instruction. Cross, wedge, arrow, star, disc, or organic form are all valid. Preserve the user's requested shape.

### DEFAULT VISUAL PROFILE / VISUAL TARGET — CONSOLE/XBOX INDUSTRIAL REALISM:
When no specific custom aesthetic or character theme is requested by the user, adopt an authentic console-grade hardware aesthetic:
1. **Textured Matte ABS Plastic**: Deep chassis body tones (`#14171e`, `#1c202a`, `#08090c`) with subtle perimeter bevels, NOT flat grey or pure `#000`.
2. **Central Rocker Pivot Mechanics**: Authentic console D-pads rock around a central spherical pivot. When designing a 4-way cross or dish, include a recessed central pivot well (`::before` circular indent) simulating the physical rocker mechanism. When designing an individual directional button, slope the gradient along the direction of travel to communicate tactile inward tilt.
3. **Restrained Detailing & Tactile Lighting**: Avoid unsolicited cyberpunk/neon glow clutter unless explicitly requested. Authentic directional pads prioritize tactile finger purchase, molded cardinal bevels, and crisp physical contact shadows.
4. **High-Contrast Cardinal Directional Affordance**: Crisp directional indicators (arrow glyph $arrowGlyph, chevron, or vector path) with high contrast against the dark textured housing.

${engineBoundaries("dpad-btn")}

### USER CUSTOMIZATION SCHEMA:
The schema is a convenience, not a limitation. Users may describe any additional visual, structural, material, symbolic, or interaction concept in SPECIAL INSTRUCTIONS or free-form text. The AI follows explicit user customization above all defaults:
- **STYLE**: [e.g. Stealth Matte Black / Cyberpunk High-Contrast Hazard / Retro Game Boy / Clean Minimal]
- **COLOR / ACCENT**: [e.g. Electric Cyan / Neon Amber / Stealth Dark / Custom palette]
- **SHAPE / SILHOUETTE**: [e.g. 12-point faceted cross / Segmented arrows / Radial disc / Wedge]
- **DIRECTIONAL MARKINGS**: [e.g. Laser-etched arrows / Glowing chevrons / Raised tactile nubs]
- **EMBLEM / GRAPHIC (OPTIONAL)**: [e.g. Embedded SVG vector emblem (`<svg viewBox="0 0 100 100"><path d="..."/></svg>`) for custom center emblems, directional arrows, or intricate crests]
- **MATERIAL / TEXTURE**: [e.g. Textured ABS plastic / Brushed gunmetal / Rubberized grip]
- **SPECIAL INSTRUCTIONS**: [Any specific visual elements, vector markings, or creative intent]

### OPTIONAL STARTER TEMPLATE — SYNTAX SKELETON [NON-BINDING SYNTAX REFERENCE ONLY]:
This template demonstrates document syntax only. Do NOT treat its colors, geometry, gradients, shadows, layer arrangement, typography, or proportions as design defaults. Build the visual design independently from the user's request:
```html
${getSyntaxSkeleton(control, "DPAD", widthDp, heightDp)}
```

### OUTPUT FORMAT CONTRACT:
Return ONLY the complete, self-contained HTML/CSS inside a single ```html ... ``` code block. Do NOT include any markdown conversation, explanations, or extraneous text.
""".trimIndent()
    }

    private fun generateTriggerPrompt(control: String, widthDp: Int, heightDp: Int): String {
        return """
${genAiHeader()}

You are an expert gamepad UI/UX designer and CSS shader artist creating a custom virtual controller Analog Trigger for NEXPAD.

### TARGET COMPONENT IDENTITY:
- **Button Key [COMPONENT-REQUIRED]**: $control (${if (control.uppercase() == NexpadKeys.LT) "Left Trigger" else "Right Trigger"})
- **Category [GLOBAL-REQUIRED]**: TRIGGER
- **Target Dimensions [GLOBAL-REQUIRED]**: width: ${widthDp}px; height: ${heightDp}px; (canvas bounding box)
- **Labels [RECOMMENDED]**: Primary "$control"

### CATEGORY SEMANTICS & INTERACTION MEANING:
- **Interaction Meaning [COMPONENT-REQUIRED]**: Analog progressive pull, pressure, and travel communication (throttle, brake, aim, fire).
- **Visual Affordance [RECOMMENDED]**: Analog Travel Affordance — progressive travel, depth, directional pull cues, active travel displacement (`scaleY(0.94) translateY(4px)`).
- **Optional Visual Language [OPTIONAL]**: Horizontal friction ribs, stippling, curved rake paddle angle, digital pressure telemetry.
- **Geometry [USER-OVERRIDE]**: `data-category` is metadata, not a shape instruction. Ergonomic curved paddle, angular wedge, minimal capsule, or custom silhouette. Preserve the user's requested shape.

### DEFAULT VISUAL PROFILE / VISUAL TARGET — CONSOLE/XBOX INDUSTRIAL REALISM:
When no specific custom aesthetic or character theme is requested by the user, adopt an authentic console-grade hardware aesthetic:
1. **Progressive Analog Travel Mechanics**: Authentic analog triggers communicate progressive depth and travel within the bounding box (${widthDp}px x ${heightDp}px) with a gradient receding into the controller housing cavity, communicating analog travel and finger placement.
2. **Molded Traction Ribs**: Physical molded horizontal friction ridges (via Flexbox column or `::before` layered shadows) providing authentic fingertip grip for throttling, braking, or aiming.
3. **High-Contrast Clean Typography**: Prominent primary key indicator ("$control", font-size 26-30px, weight 900). Keep the typography clean and authentic to real console gamepads without artificial secondary sub-labels.
4. **Restrained Detailing & Tactile Lighting**: Avoid unsolicited cyberpunk/neon glow clutter unless explicitly requested. Authentic triggers focus on ergonomic paddle curvature, molded grip traction, and deep socket shadow wells.

${engineBoundaries("trigger-btn")}

### USER CUSTOMIZATION SCHEMA:
The schema is a convenience, not a limitation. Users may describe any additional visual, structural, material, symbolic, or interaction concept in SPECIAL INSTRUCTIONS or free-form text. The AI follows explicit user customization above all defaults:
- **STYLE**: [e.g. Carbon Fiber Racing / Brembo Red Performance / Cyberpunk Neon Telemetry / Tactical Military]
- **COLOR / ACCENT**: [e.g. Racing Red / Neon Magenta / Titanium Gray / Custom palette]
- **SHAPE / SILHOUETTE**: [e.g. Ergonomic curved paddle / Angular wedge / Modern capsule / Asymmetric blade / Custom contour]
- **TRACTION GRIP**: [e.g. Horizontal rubberized ribs / Stippled texture / Slotted heat vents]
- **EMBLEM / GRAPHIC (OPTIONAL)**: [e.g. Embedded SVG vector emblem (`<svg viewBox="0 0 100 100"><path d="..."/></svg>`) for weapon branding, team logos, vehicle silhouettes, or telemetry graphics]
- **LABELS**: [e.g. "$control" / Custom text / Icon only (Default: "$control")]
- **SPECIAL INSTRUCTIONS**: [Any specific visual elements, vector markings, or creative intent]

### OPTIONAL STARTER TEMPLATE — SYNTAX SKELETON [NON-BINDING SYNTAX REFERENCE ONLY]:
This template demonstrates document syntax only. Do NOT treat its colors, geometry, gradients, shadows, layer arrangement, typography, or proportions as design defaults. Build the visual design independently from the user's request:
```html
${getSyntaxSkeleton(control, "TRIGGER", widthDp, heightDp)}
```

### OUTPUT FORMAT CONTRACT:
Return ONLY the complete, self-contained HTML/CSS inside a single ```html ... ``` code block. Do NOT include any markdown conversation, explanations, or extraneous text.
""".trimIndent()
    }

    private fun generateBumperPrompt(control: String, widthDp: Int, heightDp: Int): String = """
${genAiHeader()}

You are an expert gamepad UI/UX designer and CSS shader artist creating a custom virtual controller Shoulder Bumper for NEXPAD.

### TARGET COMPONENT IDENTITY:
- **Button Key [COMPONENT-REQUIRED]**: $control (${if (control.uppercase() == NexpadKeys.LB) "Left Bumper / Secondary Weapon" else "Right Bumper / Primary Weapon"})
- **Category [GLOBAL-REQUIRED]**: BUMPER
- **Target Dimensions [GLOBAL-REQUIRED]**: width: ${widthDp}px; height: ${heightDp}px; (canvas bounding box)

### CATEGORY SEMANTICS & INTERACTION MEANING:
- **Interaction Meaning [COMPONENT-REQUIRED]**: Shallow tactile shoulder lever/rocker actuation with crisp microswitch click feedback.
- **Visual Affordance [RECOMMENDED]**: Physical shoulder lever profile seated in a chassis housing socket or seam, specular sheen highlight, shallow press displacement.
- **Optional Visual Language [OPTIONAL]**: Specular sheen arc, brushed metallic texture, chamfered housing seam, tactile ridge.
- **Geometry [USER-OVERRIDE]**: `data-category` is metadata, not a shape instruction. The silhouette is completely yours: curved shoulder lever, angular stealth wedge, faceted cyber wing, horizontal blade, or organic contour. Preserve the user's requested shape.

### DEFAULT VISUAL PROFILE / VISUAL TARGET — CONSOLE/XBOX INDUSTRIAL REALISM:
When no specific custom aesthetic or character theme is requested by the user, adopt an authentic console-grade hardware aesthetic:
1. **Physical Shoulder Lever/Rocker Architecture**: Authentic gamepad bumpers are physical shoulder levers seated directly in a recessed chassis housing seam or socket on the controller shell, rather than floating abstract pills. The lever surface catches ambient light along its top shoulder contour.
2. **Convex Curvature Specular Sheen**: Specular highlight arc communicating convex molded polycarbonate catching studio light.
3. **Microswitch Click Actuation**: Unlike analog triggers, shoulder bumpers use crisp tactile microswitches with shallow travel displacement (`scale(0.96) translateY(2px)`) and snappy spring return (`--spring-damping: 0.75; --spring-stiffness: 520; --press-scale: 0.96;`).
4. **Restrained Detailing & Tactile Lighting**: Avoid unsolicited cyberpunk/neon glow clutter unless explicitly requested. Authentic bumpers feature clean industrial dark tones (`#2c3342` to `#0c0e13`), chassis seam contact shadows, and crisp high-contrast labels.

${engineBoundaries("bumper-btn")}

### USER CUSTOMIZATION SCHEMA:
The schema is a convenience, not a limitation. Users may describe any additional visual, structural, material, symbolic, or interaction concept in SPECIAL INSTRUCTIONS or free-form text. The AI follows explicit user customization above all defaults:
- **STYLE**: [e.g. Brushed Gunmetal Aluminum / Matte Stealth Carbon / Sci-Fi Thruster / Minimalist]
- **COLOR / ACCENT**: [e.g. Electric Blue / Cyberpunk Yellow / Gunmetal / Custom palette]
- **SHAPE / SILHOUETTE**: [e.g. Ergonomic curved shoulder / Angled stealth wedge / Faceted cyber wing / Horizontal blade / Custom contour]
- **FINISH & SHEEN**: [e.g. Horizontal specular arc / Frosted matte / Edge illumination]
- **EMBLEM / GRAPHIC (OPTIONAL)**: [e.g. Embedded SVG vector emblem (`<svg viewBox="0 0 100 100"><path d="..."/></svg>`) for faction crests, wing markings, hero logos, or intricate insignias]
- **SPECIAL INSTRUCTIONS**: [Any specific visual elements, vector markings, or creative intent]

### OPTIONAL STARTER TEMPLATE — SYNTAX SKELETON [NON-BINDING SYNTAX REFERENCE ONLY]:
This template demonstrates document syntax only. Do NOT treat its colors, geometry, gradients, shadows, layer arrangement, typography, or proportions as design defaults. Build the visual design independently from the user's request:
```html
${getSyntaxSkeleton(control, "BUMPER", widthDp, heightDp)}
```

### OUTPUT FORMAT CONTRACT:
Return ONLY the complete, self-contained HTML/CSS inside a single ```html ... ``` code block. Do NOT include any markdown conversation, explanations, or extraneous text.
""".trimIndent()

    private fun generateStickPrompt(control: String, widthDp: Int, heightDp: Int): String {
        return """
${genAiHeader()}

You are an expert gamepad UI/UX designer and CSS shader artist creating a custom virtual controller Analog Thumbstick Component for NEXPAD.

### TARGET COMPONENT IDENTITY:
- **Control Key [COMPONENT-REQUIRED]**: $control (Analog Thumbstick - NO center click button)
- **Category [GLOBAL-REQUIRED]**: JOYSTICK
- **Target Dimensions [GLOBAL-REQUIRED]**: width: ${widthDp}px; height: ${heightDp}px; (canvas bounding box)

### CATEGORY SEMANTICS & INTERACTION MEANING:
- **Interaction Meaning [COMPONENT-REQUIRED]**: Continuous 360-degree analog navigation. IMPORTANT: In NEXPAD, Analog Joysticks have NO center button or click mechanism. Stick click is strictly separated into standalone LSB/RSB buttons to prevent accidental clicks while dragging.
- **Visual Affordance [RECOMMENDED]**: Outer gimbal socket well, concave thumb dome, concentric knurled grip texture for traction.
- **Optional Visual Language [OPTIONAL]**: Knurled dashed rings, radial tick marks, cross-hatch metal, rubberized stippling, cardinal directional markers (▲, ▼, ◀, ▶).
- **Geometry [USER-OVERRIDE]**: Circle geometry is natural and authentic for physical joystick gimbal, socket, and thumb cap. Do not make it look like a flat circular web button. `data-category` is metadata, not a shape instruction. Gimbal ring, dish, square housing, or stylized silhouette: preserve the user's requested shape.

### JOYSTICK TWO-ZONE PHYSICAL MECHANISM:
In physical gamepads (Xbox, PlayStation) and mobile gaming (CoD Mobile, Genshin, PUBG), an analog stick consists of TWO distinct physical parts:
1. **Stationary Gimbal Base (The Fixed Socket Housing)**:
   - Must remain 100% stationary at (0, 0) — never translates during thumb drag.
   - Contains: Outer bezel rim, deep recessed spherical socket well (`box-shadow: inset ...`), directional tick marks, axis lines, and directional markers (▲, ▼, ◀, ▶).
   - Use container `<div class="stick-base">` or element classes containing: `base`, `socket`, `bezel`, `outer-ring`, `ticks`, `marker`.
2. **Movable Analog Thumb Cap (The Inner Dome)**:
   - Sized at approximately 55%–65% of the base diameter (~${(widthDp * 0.58).toInt()}px to ${(widthDp * 0.65).toInt()}px) to provide mechanical clearance inside the socket.
   - **Only this part translates (x, y)** when the player drags their thumb, and springs back to center on release!
   - Contains: Concave thumb dish, knurled traction grip rings, custom vector emblems/graphics, or center $control marking (<span class="stick-label">$control</span>, NO center click button).
   - **MANDATORY DOM PLACEMENT**: Put ALL cap elements (dome background, knurled rings, graphics, label) inside `<div class="stick-cap">` or element classes containing: `stick-cap`, `thumb`, `grip`, `core`, `stick-label`. Never attach thumb cap elements directly to the root `<button>` or use `.stick-btn::before`/`::after` for the moving cap, as that causes the cap to freeze to the stationary socket!
3. **Continuous Analog Navigation (NO Center Button)**:
   - **360° Analog Deflection**: Handled dynamically at runtime by NEXPAD's touch vector engine with spring return physics when dragged. **Do not write JavaScript, CSS transitions/animations, or hover/pointer events for analog movement.**
   - **Zero Center Button Interference**: In NEXPAD, thumbsticks do NOT actuate L3/R3 on click or press. Stick click is strictly isolated in dedicated standalone LSB/RSB buttons.

### DEFAULT VISUAL PROFILE / VISUAL TARGET — CONSOLE/XBOX INDUSTRIAL REALISM:
When no specific custom aesthetic or character theme is requested by the user, adopt an authentic console-grade hardware aesthetic:
1. **Matte Charcoal & Polycarbonate Plastic**: Base chassis tones `#14171e`, `#1c202a`, `#08090c` with subtle surface specular rim highlights, NOT flat grey or pure `#000`.
2. **Physical Material Contrast**: The outer gimbal socket is a deep, recessed cavity (`box-shadow: inset 0 -8px 16px rgba(0,0,0,0.85)`). The inner thumb cap is textured molded rubber/elastomer with knurled traction rings or micro-ribs.
3. **Restrained Detailing & Tactile Lighting**: Avoid unsolicited cyberpunk/neon glow clutter unless explicitly requested. Authentic gamepads feature clean micro-textures, matte finishes, and crisp physical contact shadows.
4. **Mechanical Clearance & Proportions**: The thumb cap diameter must be approximately 55%–65% of the total socket diameter (~${(widthDp * 0.60).toInt()}px for ${widthDp}px socket) to provide authentic travel clearance inside the housing well. A 1:1 cap-to-socket ratio looks like a broken button, not an analog stick!

${engineBoundaries("stick-btn")}

### VISUAL QA CHECKLIST (SELF-CHECK BEFORE OUTPUT):
Before outputting, verify your component against this checklist:
- [ ] Two-Zone DOM Structure: Stationary base enclosed in `<div class="stick-base">`, movable cap enclosed in `<div class="stick-cap">`.
- [ ] Mechanical Clearance: Thumb cap diameter is ~55%–65% of socket diameter (~${(widthDp * 0.60).toInt()}px) for realistic travel clearance.
- [ ] No Frozen Cap Elements: Knurled rings, traction ridges, emblems, and label are placed INSIDE `<div class="stick-cap">`.
- [ ] NO Center Click Button: The analog stick has NO center click button or L3/R3 marking. It is labeled "$control" in an unrotated `<span>` (or clean vector/directional art). Stick click is handled separately by LSB/RSB.
- [ ] Complex Graphics Architecture: If a character, emblem, or complex graphic is requested on the thumb cap, uses an embedded `<svg>` vector element inside `<div class="stick-cap">` with clean `<path d="...">` rather than brittle CSS `<div>` hacks. For vector glow, use an underlying CSS `<span>` (no SVG `<filter>` graphs).
- [ ] Console Realism: Authentic industrial materials (matte charcoal, rubberized dish, physical shadows) rather than unsolicited neon glow.
- [ ] No Scripts or Page CSS: Zero JavaScript, zero CSS keyframes animations, zero hover/pointer event handlers.
- [ ] Compiler Safety: Exactly one root `<button class="stick-btn">` element; all px dimensions explicit.

### USER CUSTOMIZATION SCHEMA:
The schema is a convenience, not a limitation. Users may describe any additional visual, structural, material, symbolic, or interaction concept in SPECIAL INSTRUCTIONS or free-form text. The AI follows explicit user customization above all defaults:
- **STYLE**: [e.g. Tactical Rubber Dome / Xbox Elite Magnetic Swappable / DualSense Two-Tone / Arcade Flight-Sim]
- **COLOR / ACCENT**: [e.g. Neon Emerald Green / Cyberpunk Cyan / Stealth Black / Custom palette]
- **THUMB DOME**: [e.g. Deep concave dish / Convex textured dome / Cross-hatch metallic surface]
- **KNURLING & TRACTION**: [e.g. Concentric dashed rings / Radial tick marks / Diamond knurl texture]
- **EMBLEM / GRAPHIC (OPTIONAL)**: [e.g. Embedded SVG vector emblem (`<svg viewBox="0 0 100 100"><path d="..."/></svg>`) placed inside `<div class="stick-cap">` for anime icons, hero crests, or custom emblems]
- **SPECIAL INSTRUCTIONS**: [Any specific visual elements, vector markings, or creative intent]

### OPTIONAL STARTER TEMPLATE — SYNTAX SKELETON [NON-BINDING SYNTAX REFERENCE ONLY]:
This template demonstrates document syntax only. Do NOT treat its colors, geometry, gradients, shadows, layer arrangement, typography, or proportions as design defaults. Build the visual design independently from the user's request:
```html
${getSyntaxSkeleton(control, "JOYSTICK", widthDp, heightDp)}
```

### OUTPUT FORMAT CONTRACT:
Return ONLY the complete, self-contained HTML/CSS inside a single ```html ... ``` code block. Do NOT include any markdown conversation, explanations, or extraneous text.
""".trimIndent()
    }

    private fun generateSystemPrompt(control: String, widthDp: Int, heightDp: Int): String = """
${genAiHeader()}

You are an expert gamepad UI/UX designer and CSS shader artist creating a custom virtual controller System/Utility Button for NEXPAD.

### TARGET COMPONENT IDENTITY:
- **Button Key [COMPONENT-REQUIRED]**: $control (${when(control.uppercase()) { NexpadKeys.MENU, NexpadKeys.START -> "Menu / Pause / Start"; NexpadKeys.VIEW, NexpadKeys.BACK -> "View / Back / Select"; else -> "Home / Guide / Nexus" }})
- **Category [GLOBAL-REQUIRED]**: SYSTEM
- **Target Dimensions [GLOBAL-REQUIRED]**: width: ${widthDp}px; height: ${heightDp}px;

### CATEGORY SEMANTICS & INTERACTION MEANING:
- **Interaction Meaning [COMPONENT-REQUIRED]**: Secondary console utility actions (menu, pause, guide, view, options).
- **Visual Affordance [RECOMMENDED]**: Compact, flush or low-profile footprint, immediate iconography recognition, subtle tactile click.
- **Optional Visual Language [OPTIONAL]**: Flexbox hamburger pause bars, overlapping dual rectangles, glowing nexus guide emblem.
- **Geometry [USER-OVERRIDE]**: `data-category` is metadata, not a shape instruction. Pill, sphere, tile, emblem, or custom form are all valid. Preserve the user's requested shape.

### DEFAULT VISUAL PROFILE / VISUAL TARGET — CONSOLE/XBOX INDUSTRIAL REALISM:
When no specific custom aesthetic or character theme is requested by the user, adopt an authentic console-grade hardware aesthetic:
1. **Flush Low-Profile Utility Ergonomics**: System buttons (MENU, VIEW, HOME, SHARE) on authentic gamepads are secondary utility controls. They feature a compact, flush or slightly recessed profile to prevent accidental presses during intense gameplay.
2. **Crisp Authentic Iconography**:
   - `MENU`: 3 horizontal hamburger bars with clean vertical flexbox column spacing (`gap: 4px`), rounded pill ends, and clean white/silver contrast.
   - `VIEW`: Overlapping dual windows/rectangles symbol (`⧉`) or vector path.
   - `HOME` / `GUIDE`: Central nexus orb / emblem with subtle radial glow and chamfered bezel ring.
3. **Zero Text Collision on Graphic Buttons**: Iconographic system buttons (such as MENU hamburger bars or VIEW windows) must NEVER have automatic text stamped over their icons. The icon itself is the visual identity.
4. **Restrained Lighting & Tactile Click**: Subtle recessed socket well (`box-shadow: inset 0 1px 3px rgba(255,255,255,0.25), inset 0 -3px 6px rgba(0,0,0,0.75)`), matte chassis darks, and shallow tactile micro-travel (`scale(0.92) translateY(2px)`).

${engineBoundaries("system-btn")}

### USER CUSTOMIZATION SCHEMA:
The schema is a convenience, not a limitation. Users may describe any additional visual, structural, material, symbolic, or interaction concept in SPECIAL INSTRUCTIONS or free-form text. The AI follows explicit user customization above all defaults:
- **STYLE**: [e.g. Minimalist Matte Dark Pill / Cyberpunk Neon Toggle / Xbox Series Glass Guide / Brushed Steel Switch]
- **COLOR / ACCENT**: [e.g. Subtle Cool White / Neon Yellow / Amber / Custom palette]
- **SHAPE / SILHOUETTE**: [e.g. Compact pill / Circular guide emblem / Rounded tile]
- **ICONOGRAPHY**: [e.g. Hamburger bars / Dual overlapping rectangles / Nexus sphere emblem]
- **EMBLEM / GRAPHIC (OPTIONAL)**: [e.g. Embedded SVG vector emblem (`<svg viewBox="0 0 100 100"><path d="..."/></svg>`) for custom guide logos, nexus crests, or game symbols]
- **SPECIAL INSTRUCTIONS**: [Any specific visual elements, vector markings, or creative intent]

### OPTIONAL STARTER TEMPLATE — SYNTAX SKELETON [NON-BINDING SYNTAX REFERENCE ONLY]:
This template demonstrates document syntax only. Do NOT treat its colors, geometry, gradients, shadows, layer arrangement, typography, or proportions as design defaults. Build the visual design independently from the user's request:
```html
${getSyntaxSkeleton(control, "SYSTEM", widthDp, heightDp)}
```

### OUTPUT FORMAT CONTRACT:
Return ONLY the complete, self-contained HTML/CSS inside a single ```html ... ``` code block. Do NOT include any markdown conversation, explanations, or extraneous text.
""".trimIndent()

    private fun generateStickButtonPrompt(control: String, widthDp: Int, heightDp: Int): String {
        val clickLabel = if (control.uppercase() == NexpadKeys.RSB || control.uppercase() == "R3") "R3 (Melee/Crouch)" else "L3 (Sprint)"

        return """
${genAiHeader()}

You are an expert gamepad UI/UX designer and CSS shader artist creating a custom virtual controller Stick Click Button Component for NEXPAD.

### TARGET COMPONENT IDENTITY:
- **Button Key [COMPONENT-REQUIRED]**: $control ($clickLabel)
- **Category [GLOBAL-REQUIRED]**: BUTTON (Specialized Thumbstick Click Button under Sticks category)
- **Target Dimensions [GLOBAL-REQUIRED]**: width: ${widthDp}px; height: ${heightDp}px; (canvas bounding box)

### CATEGORY SEMANTICS & INTERACTION MEANING:
- **Interaction Meaning [COMPONENT-REQUIRED]**: Instant tactile thumbstick cap depression / axial click ($clickLabel action). Unlike continuous 360° analog sticks, this is a dedicated digital button for reliable, rapid stick clicks during high-intensity gameplay.
- **Visual Affordance [RECOMMENDED]**: Circular thumbstick cap profile with knurled perimeter grip ring, concave thumb dish, radial lighting, and tactile spring micro-physics.
- **Optional Visual Language [OPTIONAL]**: Dashed traction ring, radial tick notches, rubberized stippling, edge illumination.
- **Geometry [USER-OVERRIDE]**: Circular geometry authentic to console thumbsticks is recommended, but user's requested style or custom contour always takes precedence.

### DEFAULT VISUAL PROFILE / VISUAL TARGET — CONSOLE/XBOX INDUSTRIAL REALISM:
When no specific custom aesthetic or character theme is requested by the user, adopt an authentic console-grade hardware aesthetic:
1. **Single-Button Tactile Architecture**: Unlike analog joysticks which require a stationary base + moving cap two-zone split, this dedicated stick button is a single unified button (`<button class="stick-btn-ctl" data-control="$control" data-category="BUTTON">`). The entire cap depresses with spring return physics.
2. **Textured Thumbstick Cap Dish**: A recessed center dish with knurled perimeter rim communicating molded rubber/elastomer thumb grip.
3. **Restrained Detailing & Tactile Lighting**: Clean dark polycarbonate tones (`#333333` to `#141414`) with subtle accent glow and crisp high-contrast label.
4. **Tactile Spring Micro-Physics**: Configure in `:root`:
   `--spring-damping: 0.72; --spring-stiffness: 480; --press-scale: 0.90;`

${engineBoundaries("stick-btn-ctl")}

### VISUAL QA CHECKLIST (SELF-CHECK BEFORE OUTPUT):
Before outputting, verify your component against this checklist:
- [ ] No Two-Zone Analog Split: Dedicated digital button (<button class="stick-btn-ctl" data-control="$control" data-category="BUTTON">) with single unified surface (no base/socket split).
- [ ] Compiler Safety: Exactly one root `<button class="stick-btn-ctl" data-control="$control" data-category="BUTTON">`.
- [ ] Complex Graphics Architecture: If a character, emblem, or complex graphic is requested, uses an embedded `<svg class="button-emblem" viewBox="0 0 100 100">` vector element with clean `<path d="...">` rather than brittle CSS `<div>` hacks. For vector glow, use an underlying CSS `<span>` (no SVG `<filter>` graphs).
- [ ] Tactile Physics: Valid :active state with `--spring-damping` and `--spring-stiffness`.

### USER CUSTOMIZATION SCHEMA:
- **STYLE**: [e.g. Tactical Thumbstick / Xbox Elite Swappable Cap / Cyberpunk Neon / Stealth Carbon]
- **COLOR / ACCENT**: [e.g. Cyan / Magenta / Emerald / Amber / Custom palette]
- **TRACTION GRIP**: [e.g. Dashed knurled ring / Radial ticks / Stippled texture]
- **EMBLEM / GRAPHIC (OPTIONAL)**: [e.g. Embedded SVG vector emblem (`<svg viewBox="0 0 100 100"><path d="..."/></svg>`) for custom cap insignia]
- **LABELS**: [e.g. "$control" / Custom glyph (Default: "$control")]
- **SPECIAL INSTRUCTIONS**: [Any specific visual elements, vector markings, or creative intent]

### OPTIONAL STARTER TEMPLATE — SYNTAX SKELETON [NON-BINDING SYNTAX REFERENCE ONLY]:
```html
${getSyntaxSkeleton(control, "BUTTON", widthDp, heightDp)}
```

### OUTPUT FORMAT CONTRACT:
Return ONLY the complete, self-contained HTML/CSS inside a single ```html ... ``` code block. Do NOT include any markdown conversation, explanations, or extraneous text.
""".trimIndent()
    }

    private fun generateTouchpadPrompt(control: String, widthDp: Int, heightDp: Int): String {
        val isLeft = control.equals("LTP", ignoreCase = true) || control.contains("L", ignoreCase = true)
        val padRole = if (isLeft) "Left Touch Movement Pad (Floating Dynamic-Center Stick)" else "Right Touch Camera Look Pad (Free-Look Swipe Trackpad)"
        val interactionDesc = if (isLeft) {
            "Continuous 360° character locomotion via touch drag. Touching anywhere establishes a dynamic anchor pivot; dragging directs walking/sprinting with laptop-touchpad speed-to-distance transfer (2.0x default sensitivity)."
        } else {
            "Free-look camera panning via touch swipe deltas. Dragging converts instantaneous finger velocity into right-stick camera deflection with immediate stop when stationary and 2.0x default gaming ballistics."
        }

        return """
${genAiHeader()}

### TARGET COMPONENT IDENTITY:
- **Canonical Control Key [GLOBAL-REQUIRED]**: $control
- **Component Role**: $padRole
- **Category [GLOBAL-REQUIRED]**: JOYSTICK (Touchpad Surface under Sticks category)
- **Target Dimensions [GLOBAL-REQUIRED]**: width: ${widthDp}px; height: ${heightDp}px; (canvas bounding box)

### CATEGORY SEMANTICS & INTERACTION MEANING:
- **Interaction Meaning [COMPONENT-REQUIRED]**: $interactionDesc IMPORTANT: Touchpads are pure flat, stationary laptop-style trackpad surfaces. Strictly NO center button, NO center dot, NO movable ring, and NO tap-to-click mechanism. Stick click is strictly separated into standalone LSB/RSB buttons to prevent accidental sprint or melee triggers during camera panning or movement. The touchpad operates as a pure continuous speed-to-distance surface.
- **Visual Affordance [RECOMMENDED]**: Expansive rounded-rectangular trackpad surface with deep matte texture, smooth glass touch feel, subtle peripheral bevel/frame, and high-contrast technical typography. Strictly NO center button or dot, and NO joystick-style circular ring.
- **Optional Visual Language [OPTIONAL]**: Subtle corner alignment marks, inset glass framing, ambient edge illumination, carbon-fiber stippling.

### DEFAULT VISUAL PROFILE / VISUAL TARGET — CONSOLE/XBOX INDUSTRIAL REALISM (STEAM DECK):
1. **Single-Surface Trackpad Architecture**: One expansive root `<div class="touchpad-ctl" data-id="touch_${control.lowercase()}" data-control="$control" data-category="TOUCHPAD" data-name="Touchpad $control">`.
2. **Textured Recessed Dish**: Deep carbon/polycarbonate matte finish with inset drop shadow and smooth laser-etched touch feel.
3. **Stationary Trackpad Surface**: Subtle inner boundary frame (`<div class="touchpad-surface"></div>`) or corner alignment marks. Completely stationary surface with NO movable ring, NO sliding thumb cap, and NO center dot.
4. **Header and Subtext Markings**: Technical monospace typography denoting touch mode (e.g. "${if (isLeft) "Touch Move • LTP" else "Touch Look • RTP"}") and ballistics ("2.0X BALLISTICS"). Do NOT include stick click or tap click text.

${engineBoundaries("touchpad-ctl")}

### OPTIONAL STARTER TEMPLATE — SYNTAX SKELETON [NON-BINDING SYNTAX REFERENCE ONLY]:
```html
${getSyntaxSkeleton(control, "JOYSTICK", widthDp, heightDp)}
```

### OUTPUT FORMAT CONTRACT:
Return ONLY the complete, self-contained HTML/CSS inside a single ```html ... ``` code block. Do NOT include any markdown conversation, explanations, or extraneous text.
""".trimIndent()
    }

    private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
}
