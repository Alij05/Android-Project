package com.example.sickimfy.features.playlists.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sickimfy.features.playlists.domain.repository.PlaylistsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlaylistsViewModel @Inject constructor(
    private val repository: PlaylistsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlaylistsUiState(isLoading = true))
    val uiState: StateFlow<PlaylistsUiState> = _uiState.asStateFlow()

    init {
        loadPlaylists()
    }

    private fun loadPlaylists() {
        viewModelScope.launch {
            _uiState.value = PlaylistsUiState(isLoading = true)
            _uiState.value = runCatching { repository.getPlaylists() }
                .fold(
                    onSuccess = { PlaylistsUiState(playlists = it) },
                    onFailure = { PlaylistsUiState(errorMessage = it.message.orEmpty()) }
                )
        }
    }

    fun onEvent(event: PlaylistsEvent) {
        when (event) {
            is PlaylistsEvent.OnPlaylistSelected -> {
                // Trigger navigation sequence
            }
            PlaylistsEvent.OnCreatePlaylistClick -> {
                // Logical handle for floating action button / dynamic database creation
            }
            PlaylistsEvent.OnRetryClick -> loadPlaylists()
        }
    }
}
