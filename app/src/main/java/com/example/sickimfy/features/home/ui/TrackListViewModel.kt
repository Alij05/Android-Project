package com.example.sickimfy.features.home.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sickimfy.core.data.local.dao.LikedTrackDao
import com.example.sickimfy.core.data.local.entity.LikedTrackEntity
import com.example.sickimfy.core.playback.PlaybackManager
import com.example.sickimfy.features.home.domain.model.Track
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface TrackListMode {
    data object LikedSongs : TrackListMode
    data object RecentlyPlayed : TrackListMode
}

data class TrackListUiState(
    val title: String = "",
    val tracks: List<Track> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class TrackListViewModel @Inject constructor(
    private val likedTrackDao: LikedTrackDao,
    private val playbackManager: PlaybackManager
) : ViewModel() {

    private val _mode = MutableStateFlow<TrackListMode>(TrackListMode.LikedSongs)
    private val _recentlyPlayedTracks = MutableStateFlow<List<Track>>(
        listOf(
            Track("101", "Mock Recent Track 1", "Mock Artist 1", "https://picsum.photos/200", "3:10", ""),
            Track("102", "Mock Recent Track 2", "Mock Artist 2", "https://picsum.photos/200", "4:20", "")
        )
    )

    val uiState: StateFlow<TrackListUiState> = combine(
        _mode,
        likedTrackDao.observeAll(),
        _recentlyPlayedTracks
    ) { mode, likedEntities, recentTracks ->
        when (mode) {
            TrackListMode.LikedSongs -> {
                TrackListUiState(
                    title = "آهنگ‌های لایک شده / Liked Songs",
                    tracks = likedEntities.map { it.toDomain() },
                    isLoading = false
                )
            }
            TrackListMode.RecentlyPlayed -> {
                TrackListUiState(
                    title = "اخیراً شنیده شده / Recently Played",
                    tracks = recentTracks,
                    isLoading = false
                )
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TrackListUiState()
    )

    fun setMode(mode: TrackListMode) {
        _mode.value = mode
    }

    fun playTrack(track: Track) {
        playbackManager.play(
            trackId = track.id,
            title = track.title,
            artist = track.artist,
            coverUrl = track.imageUrl,
            audioUrl = track.audioUrl
        )
    }

    fun playAll() {
        val tracksList = uiState.value.tracks
        if (tracksList.isNotEmpty()) {
            val list = tracksList.map { Triple(it.id, it.title, it.audioUrl) }
            playbackManager.playAll(list)
        }
    }

    fun removeTrack(track: Track) {
        viewModelScope.launch {
            when (_mode.value) {
                TrackListMode.LikedSongs -> {
                    likedTrackDao.delete(track.id)
                }
                TrackListMode.RecentlyPlayed -> {
                    _recentlyPlayedTracks.value = _recentlyPlayedTracks.value.filterNot { it.id == track.id }
                }
            }
        }
    }

    private fun LikedTrackEntity.toDomain() = Track(
        id = trackId,
        title = title,
        artist = artist,
        imageUrl = imageUrl,
        duration = "3:30",
        albumName = ""
    )
}
