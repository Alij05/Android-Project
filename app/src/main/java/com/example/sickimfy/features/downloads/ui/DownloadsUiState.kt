package com.example.sickimfy.features.downloads.ui

import com.example.sickimfy.features.home.domain.model.Track

data class DownloadsUiState(
    val downloadedTracks: List<Track> = emptyList(),
    val sortOption: SortOption = SortOption.DATE_ADDED,
    val isLoading: Boolean = false
)

enum class SortOption {
    DATE_ADDED, TITLE, ARTIST
}