package com.sanket.tools.nexpaddesktop.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.sanket.tools.nexpaddesktop.model.InputMode

// ═══════════════════════════════════════════════════════
//  MATERIAL 3 CYBER-NEON COMPATIBILITY TOKENS
// ═══════════════════════════════════════════════════════
val md_theme_dark_primary = Color(0xFF00E5FF)
val md_theme_dark_onPrimary = Color(0xFF000000)
val md_theme_dark_primaryContainer = Color(0xFF00B4FF)
val md_theme_dark_onPrimaryContainer = Color(0xFFFFFFFF)
val md_theme_dark_secondary = Color(0xFF00E5FF)
val md_theme_dark_onSecondary = Color(0xFF000000)
val md_theme_dark_secondaryContainer = Color(0xFF0075FF)
val md_theme_dark_onSecondaryContainer = Color(0xFFFFFFFF)
val md_theme_dark_tertiary = Color(0xFFB400FF)
val md_theme_dark_onTertiary = Color(0xFFFFFFFF)
val md_theme_dark_tertiaryContainer = Color(0xFF9D00FF)
val md_theme_dark_onTertiaryContainer = Color(0xFFFFFFFF)
val md_theme_dark_error = Color(0xFFFF4D4D)
val md_theme_dark_errorContainer = Color(0xFF93000A)
val md_theme_dark_onError = Color(0xFF690005)
val md_theme_dark_onErrorContainer = Color(0xFFFFDAD6)
val md_theme_dark_background = Color(0xFF070B14)
val md_theme_dark_onBackground = Color(0xFFFFFFFF)
val md_theme_dark_surface = Color(0xFF0E1524)
val md_theme_dark_onSurface = Color(0xFFFFFFFF)
val md_theme_dark_surfaceVariant = Color(0xFF1A2135)
val md_theme_dark_onSurfaceVariant = Color(0xFFC4C7D0)
val md_theme_dark_outline = Color(0xFF859398)
val md_theme_dark_inverseOnSurface = Color(0xFF0E1524)
val md_theme_dark_inverseSurface = Color(0xFFFFFFFF)
val md_theme_dark_inversePrimary = Color(0xFF00E5FF)
val md_theme_dark_surfaceTint = Color(0xFF00E5FF)
val md_theme_dark_outlineVariant = Color(0xFF1A2135)
val md_theme_dark_scrim = Color(0xFF000000)

// ═══════════════════════════════════════════════════════
//  NEON PALETTE — Core Brand Tokens
// ═══════════════════════════════════════════════════════
object NeonPalette {
    val Cyan = Color(0xFF00E5FF)
    val Green = Color(0xFF39FF14)
    val Purple = Color(0xFFB400FF)
    val Red = Color(0xFFFF3366)
    val Amber = Color(0xFFFFB300)
    val Blue = Color(0xFF007FFF)
    val Magenta = Color(0xFFFF007F)
    val LightBlue = Color(0xFF00B4FF)
    val PureWhite = Color(0xFFFFFFFF)
    val MutedText = Color(0xFF859398)

    // Make in India Tricolor & Lion Tokens (1:1 parity with Android)
    val TricolorSaffron = Color(0xFFFF9933)
    val TricolorGreen = Color(0xFF138808)
    val LionBodyTop = Color(0xFF131D31)
    val LionBodyBottom = Color(0xFF090E1A)

    // Background & Card surfaces (1:1 parity with Android NeonPalette)
    val DarkCard = Color(0xFF141923)
    val BorderDark = Color(0x3300E5FF)
    val PanelBgTop = Color(0xFF0E1524)
    val PanelBgBottom = Color(0xFF070B14)
    val CardIdleBg = Color(0xFF111111)
    val CardIdleBorder = Color(0xFF333333)
    val CardIdleText = Color(0xFF888888)
    val ConnectedDot = Color(0xFF34D399)

    // Signature NEXPAD Brand Gradient Tokens (PureWhite -> Cyan -> LightBlue -> Purple)
    val BrandGradient = listOf(
        PureWhite,
        Cyan,
        LightBlue,
        Purple
    )

    /** Cyan→Purple gradient used for all "selected" borders */
    val GradientBorder = listOf(Cyan, Purple)

    /** Subtle tint fill for "selected" backgrounds: dark blue → dark purple at ~7% */
    val SelectedFillGradient = listOf(
        Color(0xFF0D2847).copy(alpha = 0.07f),
        Color(0xFF1A0A2E).copy(alpha = 0.07f)
    )
}

/**
 * 1. Core Material 3 Tonal Palette (Deep Cyber-Slate Hierarchy)
 * Every surface step is slightly lighter and cooler to create natural depth without borders.
 */
object NexpadTokens {
    // Tonal Surface Steps (Darkest -> Lightest, 1:1 Parity with Android AMOLED Cyber Palette)
    val SurfaceLowest   = Color(0xFF070B14) // 3D Viewport wells, Recessed HUDs, Track backgrounds
    val SurfaceDim      = Color(0xFF070B14) // Root Window / App Scaffold Background (Android md_theme_dark_background)
    val SurfaceLow      = Color(0xFF0E1524) // Unselected Profile Cards, Secondary Sections (Android PanelBgTop)
    val Surface         = Color(0xFF141923) // Standard Section Cards (Android DarkCard)
    val SurfaceHigh     = Color(0xFF1A2135) // Hovered Cards, Telemetry Sub-rows, Metric Chips (Android surfaceVariant)
    val SurfaceHighest  = Color(0xFF242F46) // Active Toggles, Sliders, Pressed States

    // Brand & Accent Primaries
    val ElectricCyan        = Color(0xFF00E5FF)
    val OnElectricCyan      = Color(0xFF00363D)
    val ElectricCyanContainer = Color(0xFF004F58)
    val OnCyanContainer     = Color(0xFF97F0FF)

    val HyperPurple         = Color(0xFFB400FF) // Android Signature Purple
    val OnHyperPurple       = Color(0xFF3B0047)
    val HyperPurpleContainer = Color(0xFF550065)
    val OnPurpleContainer   = Color(0xFFFAD7FF)

    // Text & Outline Hierarchy
    val OnSurfacePrimary    = Color(0xFFF0F4F8) // High-emphasis titles & live telemetry values
    val OnSurfaceSecondary  = Color(0xFF94A3B8) // Labels, subtitles, inactive descriptions
    val OutlineSubtle       = Color(0xFF2E3A52) // Dividers & subtle structural hairlines
    val OutlineVariant      = Color(0xFF1E2738) // Recessed container borders
}

val NexpadDarkColorScheme = darkColorScheme(
    primary = NexpadTokens.ElectricCyan,
    onPrimary = NexpadTokens.OnElectricCyan,
    primaryContainer = NexpadTokens.ElectricCyanContainer,
    onPrimaryContainer = NexpadTokens.OnCyanContainer,

    secondary = NexpadTokens.HyperPurple,
    onSecondary = NexpadTokens.OnHyperPurple,
    secondaryContainer = NexpadTokens.HyperPurpleContainer,
    onSecondaryContainer = NexpadTokens.OnPurpleContainer,

    background = NexpadTokens.SurfaceDim,
    onBackground = NexpadTokens.OnSurfacePrimary,

    surface = NexpadTokens.Surface,
    onSurface = NexpadTokens.OnSurfacePrimary,
    onSurfaceVariant = NexpadTokens.OnSurfaceSecondary,

    surfaceContainerLowest = NexpadTokens.SurfaceLowest,
    surfaceContainerLow = NexpadTokens.SurfaceLow,
    surfaceContainer = NexpadTokens.Surface,
    surfaceContainerHigh = NexpadTokens.SurfaceHigh,
    surfaceContainerHighest = NexpadTokens.SurfaceHighest,

    outline = NexpadTokens.OutlineSubtle,
    outlineVariant = NexpadTokens.OutlineVariant
)

/**
 * 2. Expressive Semantic Accents for NexPad's 5 Motion Modes + Hardware Telemetry.
 * Keeps mode-specific colors centralized instead of scattered in `when (inputMode)` blocks.
 */
@Immutable
data class ModeAccentPair(
    val accent: Color,
    val container: Color,
    val onContainer: Color
) {
    companion object {
        fun fromSeed(seed: Color) = ModeAccentPair(
            accent = seed,
            container = seed.copy(alpha = 0.14f),
            onContainer = seed
        )
    }
}

@Immutable
data class NexpadExtendedColors(
    val velocityAim: ModeAccentPair = ModeAccentPair.fromSeed(Color(0xFF00E5FF)),   // Electric Cyan
    val steeringWheel: ModeAccentPair = ModeAccentPair.fromSeed(Color(0xFFFFB300)), // Racing Amber
    val flightStick: ModeAccentPair = ModeAccentPair.fromSeed(Color(0xFF00E676)),   // Avionics Emerald
    val combatGesture: ModeAccentPair = ModeAccentPair.fromSeed(Color(0xFFFF1744)), // Kinetic Crimson
    val rawSilicon: ModeAccentPair = ModeAccentPair.fromSeed(Color(0xFFD500F9)),    // Silicon Purple
    val statusConnected: Color = Color(0xFF00E676),
    val statusWarning: Color = Color(0xFFFF9100),
    val pitchAxis: Color = Color(0xFFFF9100),
    val rollAxis: Color = Color(0xFF00E5FF),
    val yawAxis: Color = Color(0xFFD500F9)
) {
    /** Directly resolves the expressive color pair for any active InputMode. */
    fun forMode(mode: InputMode): ModeAccentPair = when (mode) {
        InputMode.VELOCITY -> velocityAim
        InputMode.ABSOLUTE_TILT -> steeringWheel
        InputMode.AIR_MOUSE -> flightStick
        InputMode.GESTURE -> combatGesture
        InputMode.RAW_UNCALIBRATED -> rawSilicon
    }
}

val LocalNexpadColors = staticCompositionLocalOf { NexpadExtendedColors() }
