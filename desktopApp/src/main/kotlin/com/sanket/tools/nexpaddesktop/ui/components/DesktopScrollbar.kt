package com.sanket.tools.nexpaddesktop.ui.components

import androidx.compose.foundation.ScrollbarStyle
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.v2.ScrollbarAdapter
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.sanket.tools.nexpaddesktop.ui.theme.NeonPalette

/**
 * High-visibility Windows Fluent Desktop Scrollbar.
 *
 * Designed specifically for desktop screens & laptop touchpads:
 * - 12-14dp wide track with rounded pill thumb (easy to see and grab)
 * - Clear contrast thumb (visible against dark theme even when idle)
 * - Glowing accent on hover / drag
 * - Built-in subtle track gutter
 */
@Composable
fun DesktopVerticalScrollbar(
    adapter: ScrollbarAdapter,
    modifier: Modifier = Modifier,
    thickness: Dp = 10.dp,
    trackWidth: Dp = 12.dp,
    showTrackGutter: Boolean = true,
    unhoverColor: Color = Color(0xFF64748B),
    hoverColor: Color = NeonPalette.Cyan
) {
    val scrollbarStyle = ScrollbarStyle(
        minimalHeight = 36.dp,
        thickness = thickness,
        shape = RoundedCornerShape(6.dp),
        hoverDurationMillis = 150,
        unhoverColor = unhoverColor, // Slate-500: crisp, high contrast against dark UI
        hoverColor = hoverColor       // Vibrant neon cyan glow when hovered / dragged
    )

    Box(
        modifier = modifier
            .fillMaxHeight()
            .width(trackWidth)
            .then(
                if (showTrackGutter) {
                    Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF070B14).copy(alpha = 0.65f))
                        .border(0.8.dp, Color(0xFF1E293B).copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                } else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        VerticalScrollbar(
            adapter = adapter,
            style = scrollbarStyle,
            modifier = Modifier.fillMaxHeight()
        )
    }
}
