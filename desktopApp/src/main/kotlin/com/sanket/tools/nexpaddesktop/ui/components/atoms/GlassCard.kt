package com.sanket.tools.nexpaddesktop.ui.components.atoms

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.sanket.tools.nexpaddesktop.ui.components.GlassCache
import com.sanket.tools.nexpaddesktop.ui.theme.NeonPalette

/**
 * Atomic glassmorphism card container with dynamic border and background brush.
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 14.dp,
    isSelected: Boolean = false,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(cornerRadius),
        color = if (isSelected) Color(0xFF0E1524) else NeonPalette.DarkCard,
        border = BorderStroke(
            1.dp,
            if (isSelected) GlassCache.gradientBorderBrush else GlassCache.defaultBorderBrush
        )
    ) {
        content()
    }
}
