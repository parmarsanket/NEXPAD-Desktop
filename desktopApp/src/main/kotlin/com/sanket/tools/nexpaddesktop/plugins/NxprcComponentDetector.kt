package com.sanket.tools.nexpaddesktop.plugins

import com.sanket.tools.nexpad.category.CategoryManager
import com.sanket.tools.nexpad.category.CategoryType
import com.sanket.tools.nexpad.category.ControlKey

/**
 * Result of component metadata detection from raw HTML/CSS/SVG code.
 */
data class DetectedComponent(
    val controlKey: ControlKey?,
    val categoryType: CategoryType?,
    val category: String,
    val defaultControl: String,
    val componentId: String,
    val componentName: String,
    val widthDp: Int,
    val heightDp: Int,
    val isExplicitlyDefined: Boolean
)

/**
 * Intelligent HTML Code Component Detector.
 *
 * Inspects raw HTML/CSS/SVG markup to automatically identify the target gamepad control,
 * category, component ID, display name, and explicit bounds declared in the code.
 *
 * Ensures that Controller Component Studio uses the pasted HTML code as the authoritative
 * source of truth rather than whatever tab was previously open.
 */
object NxprcComponentDetector {

    /**
     * Inspects raw HTML/CSS/SVG code to detect the target gamepad control,
     * category, component ID, display name, and explicit dimensions.
     *
     * Returns a [DetectedComponent] with [DetectedComponent.isExplicitlyDefined] set to true
     * if the HTML explicitly declares or clearly identifies its component metadata.
     * Falls back safely to the provided fallback parameters if no metadata is defined.
     */
    fun detect(
        html: String,
        fallbackCategory: String,
        fallbackControl: String,
        fallbackId: String,
        fallbackName: String,
        fallbackWidthDp: Int = 96,
        fallbackHeightDp: Int = 96
    ): DetectedComponent {
        if (html.isBlank()) {
            return DetectedComponent(
                controlKey = CategoryManager.resolveControl(fallbackControl),
                categoryType = CategoryType.fromIdentifier(fallbackCategory),
                category = fallbackCategory,
                defaultControl = fallbackControl,
                componentId = fallbackId,
                componentName = fallbackName,
                widthDp = fallbackWidthDp,
                heightDp = fallbackHeightDp,
                isExplicitlyDefined = false
            )
        }

        // 1. Extract explicit data-* and standard attributes
        val rawControl = extractAttribute(html, "data-control") ?: extractAttribute(html, "data-key")
        val rawCategory = extractAttribute(html, "data-category")
        val rawId = extractAttribute(html, "data-id") ?: extractButtonId(html)
        val rawName = extractAttribute(html, "data-name")

        // 2. Extract explicit CSS pixel dimensions
        val parsedWidth = extractPixelDimension(html, "width")
        val parsedHeight = extractPixelDimension(html, "height")

        // 3. Attempt resolution via explicit data-control / data-key
        var resolvedControl: ControlKey? = rawControl?.let {
            CategoryManager.resolveControl(it) ?: CategoryManager.resolveControl(it.replace('-', '_'))
        }

        // 4. Attempt resolution via data-id or id (e.g. "rc.action_a" -> ControlKey.A)
        if (resolvedControl == null && rawId != null) {
            resolvedControl = CategoryManager.resolveControl(rawId) ?: CategoryManager.resolveControl(rawId.replace('-', '_'))
        }

        // 5. Attempt resolution via CSS class names on primary button / element or style selectors
        if (resolvedControl == null) {
            resolvedControl = detectControlFromClassNames(html)
        }

        // 6. Attempt resolution via text content in <span>, <div>, or <text> labels
        if (resolvedControl == null) {
            resolvedControl = detectControlFromLabels(html)
        }

        // 7. Attempt resolution via HTML / CSS comment directives
        if (resolvedControl == null) {
            resolvedControl = detectControlFromComments(html)
        }

        // 8. Attempt category resolution (explicit data-category, class hints, or from resolved control)
        val resolvedCategoryType: CategoryType? = when {
            resolvedControl != null -> resolvedControl.categoryType
            rawCategory != null -> CategoryType.fromIdentifier(rawCategory)
            else -> detectCategoryFromClassHints(html)
        }

        // If a control was resolved:
        if (resolvedControl != null) {
            val catType = resolvedCategoryType ?: resolvedControl.categoryType
            val compType = resolvedControl.componentType.name
            val effWidth = parsedWidth ?: resolvedControl.defaultWidthDp
            val effHeight = parsedHeight ?: resolvedControl.defaultHeightDp
            val effId = rawId ?: resolvedControl.defaultId
            val effName = rawName ?: resolvedControl.defaultName
            val effCategory = if (rawCategory != null && rawCategory.isNotBlank()) {
                val cat = CategoryType.fromIdentifier(rawCategory)
                when (cat) {
                    CategoryType.ABXY -> "BUTTON"
                    CategoryType.STICKS -> "JOYSTICK"
                    CategoryType.TRIGGERS -> "TRIGGER"
                    CategoryType.BUMPERS -> "BUMPER"
                    CategoryType.MACROS -> "MACRO"
                    else -> rawCategory.uppercase()
                }
            } else if (resolvedControl.categoryType == CategoryType.MACROS) {
                "MACRO"
            } else compType

            return DetectedComponent(
                controlKey = resolvedControl,
                categoryType = catType,
                category = effCategory,
                defaultControl = resolvedControl.key,
                componentId = effId,
                componentName = effName,
                widthDp = effWidth,
                heightDp = effHeight,
                isExplicitlyDefined = true
            )
        }

        // If only a category was resolved (e.g. data-category="JOYSTICK" or data-category="DPAD"):
        if (resolvedCategoryType != null || (rawCategory != null && rawCategory.isNotBlank())) {
            val catType = resolvedCategoryType ?: CategoryType.fromIdentifier(rawCategory)
            val primaryCtrl = catType?.let { ControlKey.of(it).firstOrNull() }
            val catName = rawCategory?.uppercase() ?: catType?.name ?: fallbackCategory
            val effControl = primaryCtrl?.key ?: fallbackControl
            val effId = rawId ?: primaryCtrl?.defaultId ?: fallbackId
            val effName = rawName ?: primaryCtrl?.defaultName ?: fallbackName
            val effWidth = parsedWidth ?: primaryCtrl?.defaultWidthDp ?: fallbackWidthDp
            val effHeight = parsedHeight ?: primaryCtrl?.defaultHeightDp ?: fallbackHeightDp

            return DetectedComponent(
                controlKey = primaryCtrl,
                categoryType = catType,
                category = catName,
                defaultControl = effControl,
                componentId = effId,
                componentName = effName,
                widthDp = effWidth,
                heightDp = effHeight,
                isExplicitlyDefined = true
            )
        }

        // Fallback: no explicit or inferred metadata detected in HTML
        return DetectedComponent(
            controlKey = CategoryManager.resolveControl(fallbackControl),
            categoryType = CategoryType.fromIdentifier(fallbackCategory),
            category = fallbackCategory,
            defaultControl = fallbackControl,
            componentId = fallbackId,
            componentName = fallbackName,
            widthDp = parsedWidth ?: fallbackWidthDp,
            heightDp = parsedHeight ?: fallbackHeightDp,
            isExplicitlyDefined = false
        )
    }

    private fun extractAttribute(html: String, attributeName: String): String? {
        val regex = Regex("""\b$attributeName=["']([^"']+)["']""", RegexOption.IGNORE_CASE)
        return regex.find(html)?.groupValues?.get(1)?.trim()
    }

    private fun extractButtonId(html: String): String? {
        val btnRegex = Regex("""<(?:button|div\b[^>]*data-component=["']button["'])[^>]*\bid=["']([^"']+)["']""", RegexOption.IGNORE_CASE)
        return btnRegex.find(html)?.groupValues?.get(1)?.trim()
    }

    private fun extractPixelDimension(html: String, propName: String): Int? {
        val regex = Regex("""\b$propName:\s*(\d+)px""", RegexOption.IGNORE_CASE)
        return regex.find(html)?.groupValues?.get(1)?.toIntOrNull()
    }

    private fun detectControlFromClassNames(html: String): ControlKey? {
        // 1. Inspect class attributes on elements
        val classAttrRegex = Regex("""\bclass=["']([^"']+)["']""", RegexOption.IGNORE_CASE)
        val pattern = Regex("""(?:^|[_\-])(?:btn|button|ctl|action|stick|dpad|trigger|bumper)[_\-]([a-zA-Z0-9]+)$""", RegexOption.IGNORE_CASE)

        for (match in classAttrRegex.findAll(html)) {
            val classAttr = match.groupValues[1]
            val tokens = classAttr.split(Regex("""\s+""")).filter { it.isNotBlank() }
            for (token in tokens) {
                val m = pattern.find(token)
                if (m != null) {
                    val candidate = m.groupValues[1]
                    val ctrl = CategoryManager.resolveControl(candidate)
                    if (ctrl != null) return ctrl
                }
                // Direct resolution on token itself (e.g. "dpad_up", "action_a", "btn_a")
                val directCtrl = CategoryManager.resolveControl(token.replace('-', '_'))
                if (directCtrl != null) return directCtrl
            }
        }

        // 2. Inspect CSS class selector rules in <style>
        val cssClassRegex = Regex("""\.(?:[a-zA-Z0-9_-]*(?:btn|button|ctl|action|stick|dpad|trigger|bumper)[_\-]([a-zA-Z0-9]+))\b""", RegexOption.IGNORE_CASE)
        for (m in cssClassRegex.findAll(html)) {
            val candidate = m.groupValues[1]
            val ctrl = CategoryManager.resolveControl(candidate)
            if (ctrl != null) return ctrl
        }

        return null
    }

    private fun detectControlFromLabels(html: String): ControlKey? {
        // 1. Look for <span ...>Label</span> with class containing label/glyph/text/key/btn
        val spanClassRegex = Regex("""<span\b[^>]*class=["'][^"']*(?:label|glyph|text|key|btn)[^"']*["'][^>]*>\s*([A-Za-z0-9_]{1,6})\s*<\/span>""", RegexOption.IGNORE_CASE)
        val spanClassMatch = spanClassRegex.find(html)
        if (spanClassMatch != null) {
            val text = spanClassMatch.groupValues[1].trim()
            val ctrl = CategoryManager.resolveControl(text)
            if (ctrl != null) return ctrl
        }

        // 2. Look for <div ...>Label</div> with class containing label/glyph/text/key/btn
        val divClassRegex = Regex("""<div\b[^>]*class=["'][^"']*(?:label|glyph|text|key|btn)[^"']*["'][^>]*>\s*([A-Za-z0-9_]{1,6})\s*<\/div>""", RegexOption.IGNORE_CASE)
        val divClassMatch = divClassRegex.find(html)
        if (divClassMatch != null) {
            val text = divClassMatch.groupValues[1].trim()
            val ctrl = CategoryManager.resolveControl(text)
            if (ctrl != null) return ctrl
        }

        // 3. Look for SVG <text> element (e.g. <text ...>A</text>)
        val svgTextRegex = Regex("""<text\b[^>]*>\s*([A-Za-z0-9_]{1,4})\s*<\/text>""", RegexOption.IGNORE_CASE)
        for (m in svgTextRegex.findAll(html)) {
            val text = m.groupValues[1].trim()
            val ctrl = CategoryManager.resolveControl(text)
            if (ctrl != null) return ctrl
        }

        // 4. Generic short span text (e.g. <span>A</span>)
        val genericSpanRegex = Regex("""<span\b[^>]*>\s*([A-Za-z0-9_]{1,4})\s*<\/span>""", RegexOption.IGNORE_CASE)
        for (m in genericSpanRegex.findAll(html)) {
            val text = m.groupValues[1].trim()
            val ctrl = CategoryManager.resolveControl(text)
            if (ctrl != null) return ctrl
        }

        return null
    }

    private fun detectControlFromComments(html: String): ControlKey? {
        val commentRegex = Regex("""(?:Gamepad Button|Target Control|Control|Component|Key):\s*([A-Za-z0-9_]+)""", RegexOption.IGNORE_CASE)
        val match = commentRegex.find(html) ?: return null
        return CategoryManager.resolveControl(match.groupValues[1].trim())
    }

    private fun detectCategoryFromClassHints(html: String): CategoryType? {
        val lower = html.lowercase()
        return when {
            lower.contains("stick-cap") || lower.contains("joystick") || lower.contains("stick-btn") || lower.contains("thumbstick") -> CategoryType.STICKS
            lower.contains("dpad") || lower.contains("cross-btn") -> CategoryType.DPAD
            lower.contains("trigger") -> CategoryType.TRIGGERS
            lower.contains("bumper") -> CategoryType.BUMPERS
            lower.contains("touchpad") || lower.contains("trackpad") -> CategoryType.SYSTEM
            else -> null
        }
    }
}
