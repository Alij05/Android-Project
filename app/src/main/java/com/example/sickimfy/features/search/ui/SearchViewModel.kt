package com.example.sickimfy.features.search.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sickimfy.features.home.domain.model.Track
import com.example.sickimfy.features.search.domain.repository.SearchRepository
import com.example.sickimfy.features.search.domain.usecase.SearchTracksUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Manages the UI state and logical flows for the Search feature.
 * Incorporates flow debouncing to optimize network requests.
 */
@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchTracks: SearchTracksUseCase,
    private val repository: SearchRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()
    private var activeSearch: Job? = null

    init {
        viewModelScope.launch {
            repository.observeHistory().collect { history ->
                _uiState.update { it.copy(searchHistory = history) }
            }
        }
        viewModelScope.launch {
            _searchQuery
                .debounce(500L)
                .distinctUntilChanged()
                .collect { query ->
                    if (query.isBlank()) {
                        _uiState.update { it.copy(isSearching = false, searchResults = emptyList(), errorMessage = null) }
                    } else {
                        performSearch(query)
                    }
                }
        }
    }

    fun onEvent(event: SearchEvent) {
        when (event) {
            is SearchEvent.OnQueryChange -> {
                _searchQuery.value = event.newQuery
                _uiState.update { it.copy(query = event.newQuery, errorMessage = null) }
            }
            is SearchEvent.OnFilterSelected -> {
                _uiState.update { it.copy(activeFilter = event.filter) }
                _searchQuery.value.takeIf { it.isNotBlank() }?.let(::performSearch)
            }
            is SearchEvent.OnDeleteHistoryItem -> {
                viewModelScope.launch { repository.deleteQuery(event.historyQuery) }
            }
            SearchEvent.OnClearSearchHistory -> {
                viewModelScope.launch { repository.clearHistory() }
            }
            is SearchEvent.OnTrackSelected -> Unit
        }
    }

    private fun performSearch(query: String) {
        activeSearch?.cancel()
        activeSearch = viewModelScope.launch {
            _uiState.update { it.copy(isSearching = true, errorMessage = null) }
            runCatching { searchTracks(query) }
                .onSuccess { results ->
                    repository.saveQuery(query)
                    _uiState.update { state ->
                        state.copy(
                            isSearching = false,
                            searchResults = results.applyFilter(state.activeFilter)
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isSearching = false, searchResults = emptyList(), errorMessage = error.message) }
                }
        }
    }

    private fun List<Track>.applyFilter(filter: SearchFilter): List<Track> = when (filter) {
        SearchFilter.ALL, SearchFilter.TRACKS -> this
        SearchFilter.ARTISTS -> distinctBy { it.artist }
        SearchFilter.ALBUMS -> filter { !it.albumName.isNullOrBlank() }.distinctBy { it.albumName }
    }
}
