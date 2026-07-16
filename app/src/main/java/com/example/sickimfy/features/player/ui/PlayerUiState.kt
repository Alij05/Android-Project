package com.example.sickimfy.features.player.ui

data class PlayerUiState(
    val trackId: String? = null,
    val title: String = "",
    val artist: String = "",
    val coverUrl: String = "",
    val isPlaying: Boolean = false,
    val currentPositionMs: Long = 0L,
    val durationMs: Long = 0L,
    val playbackSpeed: Float = 1f,
    val isLoading: Boolean = false,
    val isFavorite: Boolean = false,
    val sleepTimerMinutes: Int? = null,
    val sleepTimerRunning: Boolean = false,
    val error: String? = null
)
