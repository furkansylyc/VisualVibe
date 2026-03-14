package com.furkansoyleyici.visualvibe.ui

sealed class VibeUiState {
    object Idle : VibeUiState()
    object Loading : VibeUiState()
    data class Success(val suggestedSong: String) : VibeUiState()
    data class Error(val message: String) : VibeUiState()
}
