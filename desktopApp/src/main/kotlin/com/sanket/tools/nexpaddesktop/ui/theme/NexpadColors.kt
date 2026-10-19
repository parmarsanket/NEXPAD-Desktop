package com.sanket.tools.nexpaddesktop.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.sanket.tools.nexpaddesktop.model.InputMode

/**
 * 1. Core Material 3 Tonal Palette (Deep Cyber-Slate Hierarchy)
 * Every surface step is slightly lighter and cooler to create natural depth without borders.
 */
object NexpadTokens {
    // Tonal Surface Steps (Darkest -> Lightest)
    val SurfaceLowest   = Color(0xFF070A12) // 3D Viewport wells, Recessed HUDs, Track backgrounds
    val SurfaceDim      = Color(0xFF0B0F19) // Root Window / App Scaffold Background
    val SurfaceLow      = Color(0xFF101624) // Unselected Profile Cards, Secondary Sections
    val Surface         = Color(0xFF151C2E) // Standard Section Cards (ModernSectionCard)
    val SurfaceHigh     = Color(0xFF1C253B) // Hovered Cards, Telemetry Sub-rows, Metric Chips
    val SurfaceHighest  = Color(0xFF25304C) // Active Toggles, Sliders, Pressed States

    // Brand & Accent Primaries
    val ElectricCyan        = Color(0xFF00E5FF)
    val OnElectricCyan      = Color(0xFF00363D)
    val ElectricCyanContainer = Color(0xFF004F58)
    val OnCyanContainer     = Color(0xFF97F0FF)

    val HyperPurple         = Color(0xFFD500F9)
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
