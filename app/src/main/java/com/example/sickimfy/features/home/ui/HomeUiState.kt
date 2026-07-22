package com.example.sickimfy.features.home.ui

import com.example.sickimfy.features.home.domain.model.Track

/**
 * Unified UI State representation for the Home screen following UDF patterns.
 */
sealed interface HomeUiState {
    object Loading : HomeUiState

    data class Success(
        val carouselTracks: List<Track>,
        val popularTracks: List<Track>,
        val newReleases: List<Track>,
        val topArtists: List<Track>,
        val globalPlaylists: List<Track>,
        val localPlaylists: List<Track>
    ) : HomeUiState

    data class Error(val message: String) : HomeUiState
}
