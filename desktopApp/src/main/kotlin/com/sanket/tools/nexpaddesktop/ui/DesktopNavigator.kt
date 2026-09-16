package com.sanket.tools.nexpaddesktop.ui

import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.navigation3.runtime.NavKey

/**
 * Modern Navigation 3 Navigator for NEXPAD Desktop.
 * Provides type-safe navigation via [DesktopScreenKey] and backward-compatible [Screen] navigation.
 */
interface DesktopNavigator {
    val backStack: List<NavKey>
    val currentKey: DesktopScreenKey
    fun navigate(key: DesktopScreenKey)
    fun navigate(screen: Screen)
    fun popBackStack(): Boolean
}

class DesktopNav3Navigator(
    private val backStackState: SnapshotStateList<NavKey>
) : DesktopNavigator {
    override val backStack: List<NavKey> get() = backStackState

    override val currentKey: DesktopScreenKey
        get() = (backStackState.lastOrNull() as? DesktopScreenKey) ?: DesktopScreenKey.Home

    override fun navigate(key: DesktopScreenKey) {
        if (currentKey != key) {
            backStackState.add(key)
        }
    }

    override fun navigate(screen: Screen) {
        navigate(screen.toNavKey())
    }

    override fun popBackStack(): Boolean {
        if (backStackState.size > 1) {
            backStackState.removeAt(backStackState.lastIndex)
            return true
        }
        return false
    }
}
