package com.furkansoyleyici.visualvibe.ui

data class VibeResult(
    val suggestedSong: String,
    val explanation: String,
    val artistsUsed: String
)

sealed class VibeUiState {
    object Idle : VibeUiState()
    object Loading : VibeUiState()
    data class Success(val result: VibeResult) : VibeUiState()
    data class Error(val message: String) : VibeUiState()
}
