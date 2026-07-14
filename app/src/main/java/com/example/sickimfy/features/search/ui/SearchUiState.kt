package com.example.sickimfy.features.search.ui

import com.example.sickimfy.features.home.domain.model.Track

/**
 * Encapsulates all data required to render the Search Screen.
 */
data class SearchUiState(
    val query: String = "",
    val isSearching: Boolean = false,
    val activeFilter: SearchFilter = SearchFilter.ALL,
    val searchHistory: List<String> = emptyList(),
    val searchResults: List<Track> = emptyList(), // Reusing the Track domain model
    val errorMessage: String? = null
)

enum class SearchFilter {
    ALL, TRACKS, ARTISTS, ALBUMS
}