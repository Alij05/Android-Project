package com.example.sickimfy.features.search.ui

/**
 * Defines all possible user interactions on the Search Screen.
 */
sealed interface SearchEvent {
    data class OnQueryChange(val newQuery: String) : SearchEvent
    data class OnFilterSelected(val filter: SearchFilter) : SearchEvent
    data class OnDeleteHistoryItem(val historyQuery: String) : SearchEvent
    data class OnTrackSelected(val trackId: String) : SearchEvent
    object OnClearSearchHistory : SearchEvent
}