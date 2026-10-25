package com.sanket.tools.nexpaddesktop.core.base

/**
 * Common reactive UI events delivered from ViewModels to Compose screens.
 */
sealed interface UiEvent {
    data class ShowSnackbar(val message: String) : UiEvent
    data class ShowToast(val title: String, val message: String) : UiEvent
    data object Dismiss : UiEvent
}
