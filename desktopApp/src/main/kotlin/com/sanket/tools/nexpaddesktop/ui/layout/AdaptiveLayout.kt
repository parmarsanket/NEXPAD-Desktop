package com.sanket.tools.nexpaddesktop.ui.layout

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Desktop Adaptive Width Classes modeled after Material 3 WindowWidthSizeClass,
 * tailored for desktop workstation creative tools.
 */
enum class AdaptiveWidthClass {
    /** < 600 dp — Very narrow window or small embedded panel */
    Compact,
    /** 600–899 dp — Narrow window, half-split screen, or small desktop window */
    Medium,
    /** 900–1399 dp — Standard desktop workstation / 1080p laptop window */
    Expanded,
    /** ≥ 1400 dp — QHD, 4K, or Ultrawide monitor */
    Ultrawide
}

/**
 * Describes the desktop posture and spatial density.
 */
enum class AdaptivePosture {
    /** Narrow window — single pane with tab switcher */
    NarrowWindow,
    /** Medium window — compact two-pane or stacked panes */
    MediumWindow,
    /** Standard desktop — full two-pane side-by-side */
    DesktopStandard,
    /** Ultrawide display — wide stage with docked panels */
    DesktopUltrawide
}

@Immutable
data class AdaptiveLayoutSpec(
    val widthClass: AdaptiveWidthClass,
    val posture: AdaptivePosture,
    val horizontalPadding: Dp,
    val verticalPadding: Dp,
    val paneSpacing: Dp,
    val contentSpacing: Dp,
    /**
     * True when width is sufficient to show two panes side-by-side without crushing controls.
     * Requires width >= 880 dp AND height >= 480 dp.
     */
    val useTwoPaneLayout: Boolean,
    /**
     * True when screen height is compressed (< 620 dp), meaning headers and controls must be compact.
     */
    val isShortScreen: Boolean,
    /**
     * True when the column/pane width is narrow (< 480 dp) and cannot fit 3+ action buttons on the title row.
     */
    val isNarrowColumn: Boolean
)

/**
 * Resolves layout metrics based on BOTH available width and height.
 */
fun adaptiveLayoutSpec(maxWidth: Dp, maxHeight: Dp): AdaptiveLayoutSpec {
    val isShort = maxHeight < 620.dp

    val (widthClass, posture) = when {
        maxWidth < 600.dp -> Pair(AdaptiveWidthClass.Compact, AdaptivePosture.NarrowWindow)
        maxWidth < 900.dp -> Pair(AdaptiveWidthClass.Medium, AdaptivePosture.MediumWindow)
        maxWidth < 1400.dp -> Pair(AdaptiveWidthClass.Expanded, AdaptivePosture.DesktopStandard)
        else -> Pair(AdaptiveWidthClass.Ultrawide, AdaptivePosture.DesktopUltrawide)
    }

    val canUseTwoPane = maxWidth >= 880.dp && maxHeight >= 480.dp

    return AdaptiveLayoutSpec(
        widthClass = widthClass,
        posture = posture,
        horizontalPadding = when (widthClass) {
            AdaptiveWidthClass.Compact -> 8.dp
            AdaptiveWidthClass.Medium -> 10.dp
            AdaptiveWidthClass.Expanded -> 12.dp
            AdaptiveWidthClass.Ultrawide -> 16.dp
        },
        verticalPadding = if (isShort) 8.dp else 12.dp,
        paneSpacing = when (widthClass) {
            AdaptiveWidthClass.Compact -> 8.dp
            AdaptiveWidthClass.Medium -> 8.dp
            AdaptiveWidthClass.Expanded -> 12.dp
            AdaptiveWidthClass.Ultrawide -> 16.dp
        },
        contentSpacing = if (isShort) 6.dp else 8.dp,
        useTwoPaneLayout = canUseTwoPane,
        isShortScreen = isShort,
        isNarrowColumn = (maxWidth / (if (canUseTwoPane) 2f else 1f)) < 480.dp
    )
}
