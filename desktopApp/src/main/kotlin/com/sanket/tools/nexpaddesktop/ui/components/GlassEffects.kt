package com.sanket.tools.nexpaddesktop.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.sanket.tools.nexpaddesktop.ui.theme.NeonPalette

// ═══════════════════════════════════════════════════════
//  UNIFIED GLASS EFFECTS SYSTEM
//  Every "selected/active" state in the app uses the same
//  cyan→purple gradient border + soft outer glow + tinted fill.
// ═══════════════════════════════════════════════════════

/**
 * Draws a faint cyan grid overlay (graph-paper texture) for background depth.
 * Call this as a Canvas or drawBehind modifier.
 */
fun DrawScope.drawCyberGrid(
    gridSpacing: Float = 40f,
    lineColor: Color = NeonPalette.Cyan.copy(alpha = 0.03f)
) {
    // Vertical lines
    var x = 0f
    while (x < size.width) {
        drawLine(lineColor, Offset(x, 0f), Offset(x, size.height), strokeWidth = 1f)
        x += gridSpacing
    }
    // Horizontal lines
    var y = 0f
    while (y < size.height) {
        drawLine(lineColor, Offset(0f, y), Offset(size.width, y), strokeWidth = 1f)
        y += gridSpacing
    }
}

/**
 * Modifier that draws a gradient border (cyan→purple, 1.5dp) with a soft outer glow
 * and a subtle gradient-tinted fill behind the content.
 */
fun Modifier.selectedGlow(
    cornerRadius: Dp = 12.dp,
    glowAlpha: Float = 0.12f,
    borderWidth: Dp = 1.5.dp
): Modifier = this
    .drawBehind {
        val cr = cornerRadius.toPx()
        val bw = borderWidth.toPx()
        val maxSpread = 8.dp.toPx()
        val blurSteps = 5

        // Simulated soft outer glow using concentric filled rects
        // This ensures the glow perfectly respects the border radius and doesn't clip unevenly
        for (i in 1..blurSteps) {
            val expand = maxSpread * (i.toFloat() / blurSteps)
            val alpha = glowAlpha / blurSteps
            drawRoundRect(
                brush = Brush.linearGradient(
                    colors = listOf(
                        NeonPalette.Cyan.copy(alpha = alpha),
                        NeonPalette.Purple.copy(alpha = alpha)
                    )
                ),
                topLeft = Offset(-expand, -expand),
                size = Size(size.width + expand * 2, size.height + expand * 2),
                cornerRadius = CornerRadius(cr + expand)
            )
        }

        // Gradient fill tint
        drawRoundRect(
            brush = Brush.linearGradient(
                colors = NeonPalette.SelectedFillGradient,
                start = Offset.Zero,
                end = Offset(size.width, size.height)
            ),
            cornerRadius = CornerRadius(cr),
        )

        // Gradient border
        drawRoundRect(
            brush = Brush.linearGradient(
                colors = NeonPalette.GradientBorder,
                start = Offset.Zero,
                end = Offset(size.width, size.height)
            ),
            cornerRadius = CornerRadius(cr),
            style = Stroke(width = bw)
        )
    }

/**
 * Modifier for glass-style cards (idle state): slightly lighter than background,
 * very faint gradient border, subtle inner glow that brightens on hover.
 */
fun Modifier.glassCard(
    cornerRadius: Dp = 14.dp,
    borderAlpha: Float = 0.07f
): Modifier = this
    .drawBehind {
        val cr = cornerRadius.toPx()

        // Subtle outer glass glow
        val maxSpread = 6.dp.toPx()
        val blurSteps = 3
        for (i in 1..blurSteps) {
            val expand = maxSpread * (i.toFloat() / blurSteps)
            val alpha = 0.03f / blurSteps // incredibly faint
            drawRoundRect(
                brush = Brush.linearGradient(
                    colors = listOf(
                        NeonPalette.Cyan.copy(alpha = alpha),
                        NeonPalette.Purple.copy(alpha = alpha)
                    )
                ),
                topLeft = Offset(-expand, -expand),
                size = Size(size.width + expand * 2, size.height + expand * 2),
                cornerRadius = CornerRadius(cr + expand)
            )
        }

        // Glass fill: slightly lighter than background
        drawRoundRect(
            brush = Brush.linearGradient(
                colors = listOf(
                    NeonPalette.Cyan.copy(alpha = 0.03f),
                    NeonPalette.Purple.copy(alpha = 0.02f)
                )
            ),
            cornerRadius = CornerRadius(cr),
        )
        drawRoundRect(
            color = NeonPalette.PanelBgTop.copy(alpha = 0.75f),
            cornerRadius = CornerRadius(cr),
        )

        // Faint gradient border
        drawRoundRect(
            brush = Brush.linearGradient(
                colors = listOf(
                    Color.White.copy(alpha = borderAlpha),
                    NeonPalette.Cyan.copy(alpha = borderAlpha * 0.5f),
                    NeonPalette.Purple.copy(alpha = borderAlpha * 0.3f),
                    Color.White.copy(alpha = borderAlpha * 0.5f)
                )
            ),
            cornerRadius = CornerRadius(cr),
            style = Stroke(width = 1.dp.toPx())
        )
    }
    .clip(RoundedCornerShape(cornerRadius))

/**
 * Modifier for glass-style cards with hover state: brightens border on hover.
 */
fun Modifier.glassCardHovered(
    cornerRadius: Dp = 14.dp,
    isHovered: Boolean = false
): Modifier = this
    .drawBehind {
        val cr = cornerRadius.toPx()
        val borderAlpha = if (isHovered) 0.20f else 0.07f

        // Subtle outer glass glow
        val maxSpread = 6.dp.toPx()
        val blurSteps = 3
        for (i in 1..blurSteps) {
            val expand = maxSpread * (i.toFloat() / blurSteps)
            val alpha = (if(isHovered) 0.05f else 0.03f) / blurSteps
            drawRoundRect(
                brush = Brush.linearGradient(
                    colors = listOf(
                        NeonPalette.Cyan.copy(alpha = alpha),
                        NeonPalette.Purple.copy(alpha = alpha)
                    )
                ),
                topLeft = Offset(-expand, -expand),
                size = Size(size.width + expand * 2, size.height + expand * 2),
                cornerRadius = CornerRadius(cr + expand)
            )
        }

        drawRoundRect(
            brush = Brush.linearGradient(
                colors = listOf(
                    NeonPalette.Cyan.copy(alpha = 0.03f),
                    NeonPalette.Purple.copy(alpha = 0.02f)
                )
            ),
            cornerRadius = CornerRadius(cr),
        )
        drawRoundRect(
            color = NeonPalette.PanelBgTop.copy(alpha = 0.75f),
            cornerRadius = CornerRadius(cr),
        )
        drawRoundRect(
            brush = Brush.linearGradient(
                colors = listOf(
                    NeonPalette.Cyan.copy(alpha = borderAlpha),
                    NeonPalette.Purple.copy(alpha = borderAlpha * 0.6f),
                    NeonPalette.Cyan.copy(alpha = borderAlpha * 0.8f)
                )
            ),
            cornerRadius = CornerRadius(cr),
            style = Stroke(width = 1.dp.toPx())
        )
    }
    .clip(RoundedCornerShape(cornerRadius))

/**
 * Modifier for status pills to give them a colored glow that perfectly matches their border radius.
 */
fun Modifier.statusPillGlow(
    color: Color,
    cornerRadius: Dp = 20.dp,
    glowAlpha: Float = 0.15f
): Modifier = this.drawBehind {
    val cr = cornerRadius.toPx()
    val maxSpread = 8.dp.toPx()
    val blurSteps = 4
    
    for (i in 1..blurSteps) {
        val expand = maxSpread * (i.toFloat() / blurSteps)
        val alpha = glowAlpha / blurSteps
        drawRoundRect(
            color = color.copy(alpha = alpha),
            topLeft = Offset(-expand, -expand),
            size = Size(size.width + expand * 2, size.height + expand * 2),
            cornerRadius = CornerRadius(cr + expand)
        )
    }
}

/**
 * Draws a gradient divider line: transparent → cyan → purple → cyan → transparent
 */
@Composable
fun GradientDivider(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(
                Brush.horizontalGradient(
                    colors = listOf(
                        Color.Transparent,
                        NeonPalette.Cyan.copy(alpha = 0.3f),
                        NeonPalette.Purple.copy(alpha = 0.3f),
                        NeonPalette.Cyan.copy(alpha = 0.3f),
                        Color.Transparent
                    )
                )
            )
    )
}
