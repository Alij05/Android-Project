package com.example.sickimfy.features.playlists.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sickimfy.core.data.local.dao.UserPlaylistDao
import com.example.sickimfy.core.data.local.dao.UserPlaylistSummary
import com.example.sickimfy.core.data.local.entity.UserPlaylistTrackEntity
import com.example.sickimfy.features.home.domain.model.Track
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlaylistPickerViewModel @Inject constructor(
    private val playlistDao: UserPlaylistDao
) : ViewModel() {
    private val _selectedTrack = MutableStateFlow<Track?>(null)
    val selectedTrack: StateFlow<Track?> = _selectedTrack.asStateFlow()
    val playlists: StateFlow<List<UserPlaylistSummary>> = playlistDao.observePlaylists()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun showFor(track: Track) { _selectedTrack.value = track }
    fun dismiss() { _selectedTrack.value = null }

    fun addTo(playlistId: Long) {
        val track = _selectedTrack.value ?: return
        viewModelScope.launch {
            playlistDao.addTrack(
                UserPlaylistTrackEntity(
                    playlistId = playlistId,
                    trackId = track.id,
                    title = track.title,
                    artist = track.artist,
                    imageUrl = track.imageUrl,
                    audioUrl = track.audioUrl
                )
            )
            dismiss()
        }
    }
}
