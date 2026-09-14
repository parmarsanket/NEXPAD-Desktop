package com.sanket.tools.nexpaddesktop.plugins

import com.sanket.tools.nexpad.nxprc.*

/**
 * Details and decomposed markup for an individual canvas layer.
 */
data class LayerDetails(
    val index: Int,
    val title: String,
    val categoryBadge: String,
    val badgeColor: Long,
    val summary: String,
    val codeSnippet: String,
    val layer: CanvasLayer
)

/**
 * Utility for decomposing [CanvasLayer] instances into readable CSS/HTML/SVG snippets,
 * computing layer metadata, and constructing surgical single-layer AI prompts.
 */
object NxprcLayerCodeGenerator {

    fun formatHexColor(argb: Long): String {
        val a = ((argb shr 24) and 0xFF).toInt()
        val r = ((argb shr 16) and 0xFF).toInt()
        val g = ((argb shr 8) and 0xFF).toInt()
        val b = (argb and 0xFF).toInt()
        return if (a == 255) {
            String.format("#%02X%02X%02X", r, g, b)
        } else {
            val alphaFloat = a / 255.0f
            String.format("rgba(%d, %d, %d, %.2f)", r, g, b, alphaFloat)
        }
    }

    private fun formatFill(fill: FillBrush): String {
        return when (fill) {
            is FillBrush.Solid -> formatHexColor(fill.color)
            is FillBrush.LinearGradient -> {
                val stops = fill.colors.joinToString(", ") { formatHexColor(it) }
                "linear-gradient(${fill.angleDegrees.toInt()}deg, $stops)"
            }
            is FillBrush.RadialGradient -> {
                val stops = fill.colors.joinToString(", ") { formatHexColor(it) }
                "radial-gradient(circle at ${(fill.centerXRatio * 100).toInt()}% ${(fill.centerYRatio * 100).toInt()}%, $stops)"
            }
            is FillBrush.SweepGradient -> {
                val stops = fill.colors.joinToString(", ") { formatHexColor(it) }
                "conic-gradient(from ${fill.startAngleDegrees.toInt()}deg, $stops)"
            }
        }
    }

    /**
     * Inspects a [CanvasLayer] and extracts its classification, badge, summary, and decomposed code.
     */
    fun getLayerDetails(index: Int, layer: CanvasLayer, doc: NxprcDocument): LayerDetails {
        val w = doc.manifest.widthDp
        val h = doc.manifest.heightDp

        return when (layer) {
            is CanvasLayer.BoxLayer -> {
                val badge = when {
                    index == 0 || (layer.widthRatio >= 0.95f && layer.heightRatio >= 0.95f) -> "SOCKET"
                    layer.rotationDegrees != 0f && layer.heightRatio < 0.4f -> "GLOSS"
                    layer.widthRatio in 0.65f..0.88f -> "KEYCAP"
                    layer.widthRatio < 0.4f && layer.heightRatio < 0.2f -> "SPECULAR"
                    layer.boxShadows.any { it.isInset } -> "DIFFUSION"
                    else -> "BOX"
                }
                val badgeColor = when (badge) {
                    "SOCKET" -> 0xFF64748B
                    "GLOSS" -> 0xFF38BDF8
                    "KEYCAP" -> 0xFF4ADE80
                    "SPECULAR" -> 0xFFF472B6
                    "DIFFUSION" -> 0xFFA78BFA
                    else -> 0xFF00F0FF
                }
                val shadowCount = layer.boxShadows.size
                val summary = "${(layer.widthRatio * 100).toInt()}% width • ${layer.shapeType} • $shadowCount shadow(s)"

                val cssBuilder = StringBuilder()
                cssBuilder.append("/* Layer #$index: $badge (${layer.shapeType}) */\n")
                cssBuilder.append(".layer-$index-$badge {\n")
                cssBuilder.append("  position: absolute;\n")
                cssBuilder.append("  width: ${(layer.widthRatio * w).toInt()}px;\n")
                cssBuilder.append("  height: ${(layer.heightRatio * h).toInt()}px;\n")
                if (layer.cornerRadiusTopLeft > 0f) {
                    cssBuilder.append("  border-radius: ${layer.cornerRadiusTopLeft.toInt()}px;\n")
                }
                if (layer.fills.isNotEmpty()) {
                    layer.fills.forEachIndexed { i, f ->
                        cssBuilder.append("  /* Fill #${i + 1} */\n")
                        cssBuilder.append("  background: ${formatFill(f)};\n")
                    }
                } else {
                    cssBuilder.append("  background: ${formatFill(layer.fill)};\n")
                }
                val stroke = layer.stroke
                if (stroke != null) {
                    cssBuilder.append("  border: ${stroke.width}px solid ${formatHexColor(stroke.color)};\n")
                }
                if (layer.boxShadows.isNotEmpty()) {
                    val shadows = layer.boxShadows.joinToString(",\n    ") { s ->
                        val inset = if (s.isInset) "inset " else ""
                        "${inset}${s.offsetX.toInt()}px ${s.offsetY.toInt()}px ${s.blurRadius.toInt()}px ${s.spreadRadius.toInt()}px ${formatHexColor(s.color)}"
                    }
                    cssBuilder.append("  box-shadow:\n    $shadows;\n")
                }
                if (layer.effectiveEffects.opacity < 1.0f) {
                    cssBuilder.append("  opacity: ${layer.effectiveEffects.opacity};\n")
                }
                if (layer.effectiveTransform.rotationDegrees != 0f) {
                    cssBuilder.append("  transform: rotate(${layer.effectiveTransform.rotationDegrees}deg);\n")
                }
                cssBuilder.append("}")

                LayerDetails(
                    index = index,
                    title = "L$index: $badge (${layer.shapeType})",
                    categoryBadge = badge,
                    badgeColor = badgeColor,
                    summary = summary,
                    codeSnippet = cssBuilder.toString(),
                    layer = layer
                )
            }

            is CanvasLayer.VectorPath -> {
                val badge = "ICON"
                val badgeColor = 0xFFF59E0B
                val summary = "SVG Path • scale ${layer.scale} • rotation ${layer.rotationDegrees}°"

                val svgCode = buildString {
                    append("<!-- Layer #$index: Vector Path / Icon -->\n")
                    append("<svg viewBox=\"0 0 $w $h\" width=\"${w}px\" height=\"${h}px\">\n")
                    append("  <path d=\"${layer.pathData}\"\n")
                    append("        fill=\"${formatFill(layer.fill)}\"\n")
                    val vStroke = layer.stroke
                    if (vStroke != null) {
                        append("        stroke=\"${formatHexColor(vStroke.color)}\"\n")
                        append("        stroke-width=\"${vStroke.width}\"\n")
                    }
                    if (layer.scale != 1.0f || layer.rotationDegrees != 0f) {
                        append("        transform=\"scale(${layer.scale}) rotate(${layer.rotationDegrees})\"\n")
                    }
                    append("  />\n")
                    append("</svg>")
                }

                LayerDetails(
                    index = index,
                    title = "L$index: Vector Emblem / Icon",
                    categoryBadge = badge,
                    badgeColor = badgeColor,
                    summary = summary,
                    codeSnippet = svgCode,
                    layer = layer
                )
            }

            is CanvasLayer.TextLayer -> {
                val badge = "TEXT"
                val badgeColor = 0xFFEC4899
                val summary = "Text '${layer.text}' • ${layer.fontSizeSp}sp • weight ${layer.fontWeight}"

                val textCode = buildString {
                    append("/* Layer #$index: Text Label */\n")
                    append(".layer-$index-text {\n")
                    append("  font-size: ${layer.fontSizeSp}px;\n")
                    append("  font-weight: ${layer.fontWeight};\n")
                    append("  color: ${formatHexColor(layer.textColor)};\n")
                    if (layer.textShadows.isNotEmpty()) {
                        val shadows = layer.textShadows.joinToString(", ") {
                            "${it.offsetX.toInt()}px ${it.offsetY.toInt()}px ${it.blurRadius.toInt()}px ${formatHexColor(it.color)}"
                        }
                        append("  text-shadow: $shadows;\n")
                    }
                    append("}\n")
                    append("<!-- Markup -->\n")
                    append("<span>${layer.text}</span>")
                }

                LayerDetails(
                    index = index,
                    title = "L$index: Label '${layer.text}'",
                    categoryBadge = badge,
                    badgeColor = badgeColor,
                    summary = summary,
                    codeSnippet = textCode,
                    layer = layer
                )
            }

            is CanvasLayer.CenterGlyph -> {
                val badge = "GLYPH"
                val badgeColor = 0xFFEC4899
                val text = layer.text ?: doc.manifest.defaultControl
                val summary = "Glyph '$text' • ${layer.fontSizeSp}sp"

                val glyphCode = buildString {
                    append("/* Layer #$index: Center Glyph */\n")
                    append(".layer-$index-glyph {\n")
                    append("  font-size: ${layer.fontSizeSp}px;\n")
                    append("  color: ${formatHexColor(layer.textColor)};\n")
                    if (layer.textShadows.isNotEmpty()) {
                        val shadows = layer.textShadows.joinToString(", ") {
                            "${it.offsetX.toInt()}px ${it.offsetY.toInt()}px ${it.blurRadius.toInt()}px ${formatHexColor(it.color)}"
                        }
                        append("  text-shadow: $shadows;\n")
                    }
                    append("}\n")
                    append("<span>$text</span>")
                }

                LayerDetails(
                    index = index,
                    title = "L$index: Center Glyph '$text'",
                    categoryBadge = badge,
                    badgeColor = badgeColor,
                    summary = summary,
                    codeSnippet = glyphCode,
                    layer = layer
                )
            }

            is CanvasLayer.GlowRing -> {
                val badge = "GLOW"
                val badgeColor = 0xFF06B6D4
                val summary = "Atmospheric Glow • blur ${layer.blurRadius}px • pulse: ${layer.pulseEnabled}"

                val glowCode = buildString {
                    append("/* Layer #$index: Atmospheric Glow Ring */\n")
                    append(".layer-$index-glow {\n")
                    append("  box-shadow: 0 0 ${layer.blurRadius.toInt()}px ${formatHexColor(layer.glowColor)};\n")
                    if (layer.pulseEnabled) {
                        append("  animation: pulse 2.5s ease-in-out infinite;\n")
                    }
                    append("}")
                }

                LayerDetails(
                    index = index,
                    title = "L$index: Outer Glow Ring",
                    categoryBadge = badge,
                    badgeColor = badgeColor,
                    summary = summary,
                    codeSnippet = glowCode,
                    layer = layer
                )
            }

            is CanvasLayer.BezelSocket -> {
                val badge = "BEZEL"
                val badgeColor = 0xFF64748B
                val summary = "Bezel Socket • inset ${(layer.insetRatio * 100).toInt()}%"

                val bezelCode = buildString {
                    append("/* Layer #$index: Molded Bezel Socket */\n")
                    append(".layer-$index-bezel {\n")
                    append("  background: ${formatHexColor(layer.outerBezelColor)};\n")
                    append("  border: 1px solid ${formatHexColor(layer.outerBevelStroke)};\n")
                    append("  box-shadow: 0 4px 8px ${formatHexColor(layer.shadowColor)};\n")
                    append("}")
                }

                LayerDetails(
                    index = index,
                    title = "L$index: Molded Bezel Socket",
                    categoryBadge = badge,
                    badgeColor = badgeColor,
                    summary = summary,
                    codeSnippet = bezelCode,
                    layer = layer
                )
            }

            is CanvasLayer.InnerShadow -> {
                val badge = "CAVITY"
                val badgeColor = 0xFF8B5CF6
                val summary = "Inner Bevel Shadow • stroke ${layer.strokeWidth}px"

                val shadowCode = buildString {
                    append("/* Layer #$index: Inner Bevel Cavity Shadow */\n")
                    append(".layer-$index-cavity {\n")
                    append("  box-shadow:\n")
                    append("    inset 0 2px ${layer.strokeWidth.toInt()}px ${formatHexColor(layer.shadowColor)},\n")
                    append("    inset 0 -2px ${layer.strokeWidth.toInt()}px ${formatHexColor(layer.highlightColor)};\n")
                    append("}")
                }

                LayerDetails(
                    index = index,
                    title = "L$index: Inner Bevel Cavity",
                    categoryBadge = badge,
                    badgeColor = badgeColor,
                    summary = summary,
                    codeSnippet = shadowCode,
                    layer = layer
                )
            }

            is CanvasLayer.GlossReflection -> {
                val badge = "GLOSS"
                val badgeColor = 0xFF38BDF8
                val summary = "Specular Gloss Arc • ${(layer.widthRatio * 100).toInt()}% width • ${layer.rotationDegrees}°"

                val glossCode = buildString {
                    append("/* Layer #$index: Specular Gloss Arc (::after) */\n")
                    append(".layer-$index-gloss::after {\n")
                    append("  content: \"\";\n")
                    append("  position: absolute;\n")
                    append("  width: ${(layer.widthRatio * 100).toInt()}%;\n")
                    append("  height: ${(layer.heightRatio * 100).toInt()}%;\n")
                    append("  top: ${(layer.offsetYRatio * 100).toInt()}%;\n")
                    append("  left: ${(layer.offsetXRatio * 100).toInt()}%;\n")
                    append("  transform: rotate(${layer.rotationDegrees}deg);\n")
                    append("  border-radius: 50%;\n")
                    append("  background: radial-gradient(ellipse at center, rgba(255,255,255,${layer.alpha}) 0%, transparent 75%);\n")
                    append("}")
                }

                LayerDetails(
                    index = index,
                    title = "L$index: Specular Arc Reflection",
                    categoryBadge = badge,
                    badgeColor = badgeColor,
                    summary = summary,
                    codeSnippet = glossCode,
                    layer = layer
                )
            }

            is CanvasLayer.GradientShape -> {
                val badge = "SHAPE"
                val badgeColor = 0xFF10B981
                val summary = "${layer.shapeType} • ${(layer.widthRatio * 100).toInt()}% width"

                val shapeCode = buildString {
                    append("/* Layer #$index: Gradient Shape (${layer.shapeType}) */\n")
                    append(".layer-$index-shape {\n")
                    append("  width: ${(layer.widthRatio * w).toInt()}px;\n")
                    append("  height: ${(layer.heightRatio * h).toInt()}px;\n")
                    append("  border-radius: ${layer.cornerRadius.toInt()}px;\n")
                    append("  background: ${formatFill(layer.fill)};\n")
                    val stroke = layer.stroke
                    if (stroke != null) {
                        append("  border: ${stroke.width}px solid ${formatHexColor(stroke.color)};\n")
                    }
                    append("}")
                }

                LayerDetails(
                    index = index,
                    title = "L$index: Gradient Fill Shape",
                    categoryBadge = badge,
                    badgeColor = badgeColor,
                    summary = summary,
                    codeSnippet = shapeCode,
                    layer = layer
                )
            }
        }
    }

    /**
     * Constructs a surgical single-layer AI prompt with strict boundaries.
     * The model is instructed to modify or replace ONLY this layer without hallucinating
     * changes to working layers or returning a monolithic button rewrite.
     */
    fun generateLayerAiPrompt(
        layerIndex: Int,
        layer: CanvasLayer,
        doc: NxprcDocument,
        userInstruction: String
    ): String {
        val details = getLayerDetails(layerIndex, layer, doc)
        val w = doc.manifest.widthDp
        val h = doc.manifest.heightDp

        return """
# NEXPAD SURGICAL SINGLE-LAYER MODIFICATION TASK

## TARGET COMPONENT CONTEXT
- Gamepad Button: ${doc.manifest.defaultControl} (${doc.manifest.category})
- Canvas Size: ${w}x${h}dp (ViewBox: ${doc.canvas.viewBoxWidth.toInt()}x${doc.canvas.viewBoxHeight.toInt()})
- Target Layer Index: #$layerIndex
- Layer Name: ${details.title}
- Layer Classification: ${details.categoryBadge}

## CURRENT LAYER DECOMPOSED CODE
```css
${details.codeSnippet}
```

## USER MODIFICATION REQUEST
"${userInstruction.ifBlank { "Refine and improve this layer with higher visual fidelity matching cyberpunk/tactile gamepad style" }}"

## STRICT CONTRACT & OUTPUT CONSTRAINTS
1. Output ONLY the replacement CSS rule or SVG element for this single layer (Layer #$layerIndex).
2. Do NOT regenerate or output the outer <button> wrapper, the root layout, or any other layers.
3. Keep coordinates and dimensions compatible with the parent ${w}x${h}dp button container.
4. If modifying SVG paths, use standard SVG path syntax (M, L, C, Q, Z).
5. Output pure code without conversational markdown filler.
""".trimIndent()
    }
}
