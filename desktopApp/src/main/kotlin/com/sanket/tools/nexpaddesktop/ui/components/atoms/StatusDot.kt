package com.sanket.tools.nexpaddesktop.ui.components.atoms

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.sanket.tools.nexpaddesktop.ui.theme.NeonPalette

/**
 * Compact atomic status indicator dot with optional pulsing animation.
 */
@Composable
fun StatusDot(
    modifier: Modifier = Modifier,
    color: Color = NeonPalette.Green,
    size: Dp = 6.dp,
    pulsing: Boolean = false
) {
    if (pulsing) {
        val infiniteTransition = rememberInfiniteTransition()
        val scale by infiniteTransition.animateFloat(
            initialValue = 0.85f,
            targetValue = 1.25f,
            animationSpec = infiniteRepeatable(
                animation = tween(800, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            )
        )
        Box(
            modifier = modifier
                .size(size)
                .scale(scale)
                .clip(CircleShape)
                .background(color)
        )
    } else {
        Box(
            modifier = modifier
                .size(size)
                .clip(CircleShape)
                .background(color)
        )
    }
}
