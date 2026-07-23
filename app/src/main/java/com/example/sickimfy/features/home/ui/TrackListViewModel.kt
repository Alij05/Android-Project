package com.example.sickimfy.features.home.ui

import androidx.annotation.OptIn
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.util.UnstableApi
import com.example.sickimfy.core.data.local.dao.LikedTrackDao
import com.example.sickimfy.core.data.local.dao.RecentlyPlayedDao
import com.example.sickimfy.core.data.local.entity.LikedTrackEntity
import com.example.sickimfy.core.data.local.entity.RecentlyPlayedEntity
import com.example.sickimfy.core.data.preferences.UserPreferencesDataStore
import com.example.sickimfy.core.network.resolveMediaUrl
import com.example.sickimfy.core.playback.PlaybackManager
import com.example.sickimfy.core.playback.PlaybackQueueItem
import com.example.sickimfy.features.home.domain.model.Track
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
//import com.example.sickimfy.core.data.local.dao.DownloadedTrackDao

sealed interface TrackListMode {
    data object LikedSongs : TrackListMode
    data object RecentlyPlayed : TrackListMode
}

// UI state now exposes the mode instead of a hardcoded/bilingual title string.
// The actual localized title is resolved in the Composable via stringResource.
data class TrackListUiState(
    val mode: TrackListMode = TrackListMode.LikedSongs,
    val tracks: List<Track> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class TrackListViewModel @OptIn(UnstableApi::class)
@Inject constructor(
    private val likedTrackDao: LikedTrackDao,
    private val recentlyPlayedDao: RecentlyPlayedDao,
    private val playbackManager: PlaybackManager,
    private val preferences: UserPreferencesDataStore
) : ViewModel() {

    private val _mode = MutableStateFlow<TrackListMode>(TrackListMode.LikedSongs)
    val uiState: StateFlow<TrackListUiState> = combine(
        _mode,
        likedTrackDao.observeAll(),
        recentlyPlayedDao.observeAll(),
        preferences.preferences
    ) { mode, likedEntities, recentEntities, prefs ->
        val apiBaseUrl = prefs.apiBaseUrl
        when (mode) {
            TrackListMode.LikedSongs -> {
                TrackListUiState(
                    mode = mode,
                    tracks = likedEntities.map { it.toDomain(apiBaseUrl) },
                    isLoading = false
                )
            }
            TrackListMode.RecentlyPlayed -> {
                TrackListUiState(
                    mode = mode,
                    tracks = recentEntities.map { it.toDomain(apiBaseUrl) },
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
        playQueue(track)
    }

    fun playAll() {
        val tracksList = uiState.value.tracks
        if (tracksList.isNotEmpty()) {
            playQueue(tracksList.first())
        }
    }

    fun removeTrack(track: Track) {
        viewModelScope.launch {
            when (_mode.value) {
                TrackListMode.LikedSongs -> {
                    likedTrackDao.delete(track.id)
                }
                TrackListMode.RecentlyPlayed -> recentlyPlayedDao.delete(track.id)
            }
        }
    }

    private fun LikedTrackEntity.toDomain(apiBaseUrl: String) = Track(
        id = trackId,
        title = title,
        artist = artist,
        imageUrl = resolveMediaUrl(imageUrl, apiBaseUrl).orEmpty(),
        duration = "3:30",
        albumName = "",
        audioUrl = resolveMediaUrl(audioUrl, apiBaseUrl)
    )

    private fun RecentlyPlayedEntity.toDomain(apiBaseUrl: String) = Track(
        id = trackId,
        title = title,
        artist = artist,
        imageUrl = resolveMediaUrl(imageUrl, apiBaseUrl).orEmpty(),
        duration = "3:30",
        albumName = "",
        audioUrl = resolveMediaUrl(audioUrl, apiBaseUrl)
    )

    @OptIn(UnstableApi::class)
    private fun playQueue(selected: Track) {
        val queue = uiState.value.tracks.mapNotNull { track ->
            track.audioUrl?.takeIf { it.isNotBlank() }?.let {
                PlaybackQueueItem(track.id, track.title, track.artist, track.imageUrl, it)
            }
        }
        val startIndex = queue.indexOfFirst { it.id == selected.id }
        if (startIndex >= 0) playbackManager.playQueue(queue, startIndex)
    }
}
