package com.example.sickimfy.features.downloads.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sickimfy.features.home.domain.model.Track
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DownloadsViewModel @Inject constructor(
    // Dependencies like GetDownloadedTracksUseCase and DeleteDownloadedTrackUseCase go here
) : ViewModel() {

    private val _sortOption = MutableStateFlow(SortOption.DATE_ADDED)

    // Mocked static data for UI implementation. This will be replaced by Room DB Flow.
    private val _downloadedTracks = MutableStateFlow(
        listOf(
            Track("1", "Bohemian Rhapsody", "Queen", "https://mock.url/1", "5:55", "A Night at the Opera"),
            Track("2", "Shape of You", "Ed Sheeran", "https://mock.url/2", "3:53", "Divide"),
            Track("3", "Blinding Lights", "The Weeknd", "https://mock.url/3", "3:20", "After Hours")
        )
    )

    val uiState: StateFlow<DownloadsUiState> = combine(
        _downloadedTracks,
        _sortOption
    ) { tracks, sortOption ->
        val sortedTracks = when (sortOption) {
            SortOption.TITLE -> tracks.sortedBy { it.title }
            SortOption.ARTIST -> tracks.sortedBy { it.artist }
            SortOption.DATE_ADDED -> tracks // Assuming incoming flow is ordered by date
        }
        DownloadsUiState(
            downloadedTracks = sortedTracks,
            sortOption = sortOption,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DownloadsUiState(isLoading = true)
    )

    fun onEvent(event: DownloadsEvent) {
        when (event) {
            is DownloadsEvent.OnSortOptionChanged -> {
                _sortOption.value = event.option
            }
            is DownloadsEvent.OnDeleteTrack -> {
                // Here you would call deleteUseCase(event.track.id)
                // For UI state reflection:
                _downloadedTracks.update { currentList ->
                    currentList.filterNot { it.id == event.track.id }
                }
            }
            is DownloadsEvent.OnTrackSelected -> {
                // Navigate to player
            }
        }
    }
}