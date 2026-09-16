package com.sanket.tools.nexpaddesktop.ui

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/**
 * Type-safe navigation keys for NEXPAD Desktop (Navigation 3).
 * Represents all top-level destinations and modular views in Compose Multiplatform.
 */
@Serializable
sealed interface DesktopScreenKey : NavKey {

    @Serializable
    data object Home : DesktopScreenKey

    @Serializable
    data object Controller : DesktopScreenKey

    @Serializable
    data object Plugins : DesktopScreenKey

    @Serializable
    data object Output : DesktopScreenKey

    @Serializable
    data object Node : DesktopScreenKey

    @Serializable
    data object Converter : DesktopScreenKey

    @Serializable
    data object Kbm : DesktopScreenKey
}

/**
 * Extension mapper from legacy [Screen] enum to strongly typed [DesktopScreenKey].
 */
fun Screen.toNavKey(): DesktopScreenKey = when (this) {
    Screen.HOME -> DesktopScreenKey.Home
    Screen.CONTROLLER -> DesktopScreenKey.Controller
    Screen.PLUGINS -> DesktopScreenKey.Plugins
    Screen.OUTPUT -> DesktopScreenKey.Output
    Screen.NODE -> DesktopScreenKey.Node
    Screen.CONVERTER -> DesktopScreenKey.Converter
    Screen.KBM -> DesktopScreenKey.Kbm
}

/**
 * Extension mapper from [DesktopScreenKey] to legacy [Screen] enum for UI indicators.
 */
fun DesktopScreenKey.toScreen(): Screen = when (this) {
    is DesktopScreenKey.Home -> Screen.HOME
    is DesktopScreenKey.Controller -> Screen.CONTROLLER
    is DesktopScreenKey.Plugins -> Screen.PLUGINS
    is DesktopScreenKey.Output -> Screen.OUTPUT
    is DesktopScreenKey.Node -> Screen.NODE
    is DesktopScreenKey.Converter -> Screen.CONVERTER
    is DesktopScreenKey.Kbm -> Screen.KBM
}
