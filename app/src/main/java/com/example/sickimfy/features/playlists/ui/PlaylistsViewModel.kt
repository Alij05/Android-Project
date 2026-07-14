package com.example.sickimfy.features.playlists.ui

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sickimfy.features.playlists.domain.model.Playlist
import com.example.sickimfy.features.playlists.domain.model.PlaylistType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlaylistsViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(PlaylistsUiState(isLoading = true))
    val uiState: StateFlow<PlaylistsUiState> = _uiState.asStateFlow()

    init {
        loadPlaylists()
    }

    private fun loadPlaylists() {
        viewModelScope.launch {
            // Mocking data logic. In production, this would flow from your Repository layer.
            val mockPlaylists = listOf(
                // International Music Gradients
                Playlist("1", "Global Top 50", 50, PlaylistType.INTERNATIONAL, listOf(Color(0xFF8E2DE2), Color(0xFF4A00E0))),
                Playlist("2", "Synthwave Neon", 34, PlaylistType.INTERNATIONAL, listOf(Color(0xFFF12711), Color(0xFFF5AF19))),
                Playlist("3", "Chill Lo-Fi Beat", 25, PlaylistType.INTERNATIONAL, listOf(Color(0xFF00C9FF), Color(0xFF92FE9D))),

                // Domestic Music Gradients
                Playlist("4", "پاپ سنتی", 42, PlaylistType.DOMESTIC, listOf(Color(0xFF11998E), Color(0xFF38EF7D))),
                Playlist("5", "نوستالژی دهه ۸۰", 100, PlaylistType.DOMESTIC, listOf(Color(0xFFFF416C), Color(0xFFFF4B2B))),
                Playlist("6", "راک و آلترناتیو فارسی", 18, PlaylistType.DOMESTIC, listOf(Color(0xFF7F00FF), Color(0xFFE100FF))),

                // User Playlists Gradients
                Playlist("7", "My Night Ride", 12, PlaylistType.USER, listOf(Color(0xFF396AFC), Color(0xFF2948FF))),
                Playlist("8", "تمرین و ورزش", 45, PlaylistType.USER, listOf(Color(0xFFE65C00), Color(0xFFF9D423)))
            )
            _uiState.value = PlaylistsUiState(
                playlists = mockPlaylists,
                isLoading = false
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
        }
    }
}