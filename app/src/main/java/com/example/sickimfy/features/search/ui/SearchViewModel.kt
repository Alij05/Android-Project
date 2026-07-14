package com.example.sickimfy.features.search.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sickimfy.features.home.domain.model.Track
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
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
    // Note: Assuming these use-cases/repositories are implemented by your teammates in the Domain layer
    // private val searchTracksUseCase: SearchTracksUseCase,
    // private val getSearchHistoryUseCase: GetSearchHistoryUseCase,
    // private val deleteSearchHistoryUseCase: DeleteSearchHistoryUseCase
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _activeFilter = MutableStateFlow(SearchFilter.ALL)
    private val _isSearching = MutableStateFlow(false)

    // Mocked history flow from Room DB (Your teammate will provide the actual Flow from Room)
    private val _searchHistory = MutableStateFlow(listOf("Imagine Dragons", "Eminem", "Interstellar OST"))

    // The single source of truth for the UI
    val uiState: StateFlow<SearchUiState> = combine(
        _searchQuery,
        _activeFilter,
        _isSearching,
        _searchHistory
    ) { query, filter, isSearching, history ->
        SearchUiState(
            query = query,
            activeFilter = filter,
            isSearching = isSearching,
            searchHistory = history
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SearchUiState()
    )

    init {
        // Setup the reactive Debounce stream for intelligent searching
        viewModelScope.launch {
            _searchQuery
                .debounce(500L) // Wait for 500ms of user inactivity before proceeding
                .distinctUntilChanged() // Only trigger if the query actually changed
                .filter { it.isNotBlank() } // Don't search for empty strings
                .flatMapLatest { query ->
                    _isSearching.update { true }
                    // Perform the actual network search here via UseCase
                    // searchTracksUseCase(query, _activeFilter.value)

                    // MOCKING the API response for now
                    kotlinx.coroutines.delay(1000) // Simulate network delay
                    flowOf(emptyList<Track>())
                }
                .collect { results ->
                    _isSearching.update { false }
                    // Update state with actual results (omitted in mock to keep state clean)
                }
        }
    }

    fun onEvent(event: SearchEvent) {
        when (event) {
            is SearchEvent.OnQueryChange -> {
                _searchQuery.value = event.newQuery
                if (event.newQuery.isBlank()) {
                    _isSearching.update { false }
                }
            }
            is SearchEvent.OnFilterSelected -> {
                _activeFilter.value = event.filter
                // Re-trigger search with new filter if query isn't empty
            }
            is SearchEvent.OnDeleteHistoryItem -> {
                // deleteSearchHistoryUseCase(event.historyQuery)
                _searchHistory.update { it.filterNot { item -> item == event.historyQuery } }
            }
            SearchEvent.OnClearSearchHistory -> {
                // clearSearchHistoryUseCase()
                _searchHistory.update { emptyList() }
            }
            is SearchEvent.OnTrackSelected -> {
                // Handle navigation or playback
            }
        }
    }
}