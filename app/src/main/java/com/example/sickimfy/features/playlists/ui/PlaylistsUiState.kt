package com.example.sickimfy.features.playlists.ui

import com.example.sickimfy.features.playlists.domain.model.Playlist

data class PlaylistsUiState(
    val playlists: List<Playlist> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)