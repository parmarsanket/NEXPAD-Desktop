package com.sanket.tools.nexpaddesktop.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.unit.dp

val NexpadShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),  // Metric chips & mini status badges
    small = RoundedCornerShape(10.dp),      // Telemetry rows & buttons
    medium = RoundedCornerShape(16.dp),     // Standard section cards & 3D viewport
    large = RoundedCornerShape(24.dp),      // Active expressive profile cards
    extraLarge = RoundedCornerShape(32.dp)  // Pill containers & modal dialogs
)

@Composable
fun NexpadTheme(
    content: @Composable () -> Unit
) {
    val extendedColors = NexpadExtendedColors()

    CompositionLocalProvider(LocalNexpadColors provides extendedColors) {
        MaterialTheme(
            colorScheme = NexpadDarkColorScheme,
            typography = NexpadTypography,
            shapes = NexpadShapes,
            content = content
        )
    }
}

/** Backward-compatible theme alias for application root and previews */
@Composable
fun NexpadDesktopTheme(
    content: @Composable () -> Unit
) {
    NexpadTheme(content = content)
}

object NexpadTheme {
    val colors: NexpadExtendedColors
        @Composable
        @ReadOnlyComposable
        get() = LocalNexpadColors.current
}
