package com.example.sickimfy.features.playlists.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sickimfy.core.data.local.dao.UserPlaylistDao
import com.example.sickimfy.core.playback.PlaybackManager
import com.example.sickimfy.features.home.domain.model.Track
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class PersonalPlaylistDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    playlistDao: UserPlaylistDao,
    private val playbackManager: PlaybackManager
) : ViewModel() {
    private val playlistId = checkNotNull(savedStateHandle.get<Long>("playlistId"))

    val tracks: StateFlow<List<Track>> = playlistDao.observeTracks(playlistId).map { entries ->
        entries.map {
            Track(it.trackId, it.title, it.artist, it.imageUrl, "", audioUrl = it.audioUrl)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun play(track: Track) {
        playbackManager.play(track.id, track.title, track.artist, track.imageUrl, track.audioUrl)
    }
}
