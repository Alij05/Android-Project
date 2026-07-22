package com.example.sickimfy.features.playlists.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sickimfy.features.playlists.domain.repository.PlaylistsRepository
import com.example.sickimfy.core.playback.PlaybackManager
import com.example.sickimfy.core.data.local.dao.DownloadedTrackDao
import com.example.sickimfy.core.data.local.dao.UserPlaylistDao
import com.example.sickimfy.core.data.local.dao.UserPlaylistSummary
import com.example.sickimfy.features.playlists.domain.model.Playlist
import com.example.sickimfy.features.playlists.domain.model.PlaylistType
import androidx.compose.ui.graphics.Color
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlaylistsViewModel @Inject constructor(
    private val repository: PlaylistsRepository,
    private val playbackManager: PlaybackManager,
    private val downloadedTrackDao: DownloadedTrackDao,
    private val userPlaylistDao: UserPlaylistDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlaylistsUiState(isLoading = true))
    val uiState: StateFlow<PlaylistsUiState> = _uiState.asStateFlow()
    private var serverPlaylists: List<Playlist> = emptyList()
    private var localPlaylists: List<UserPlaylistSummary> = emptyList()

    init {
        viewModelScope.launch {
            userPlaylistDao.observePlaylists().collect { playlists ->
                localPlaylists = playlists
                publishPlaylists()
            }
        }
        loadPlaylists()
    }

    private fun loadPlaylists() {
        viewModelScope.launch {
            _uiState.value = PlaylistsUiState(isLoading = true)
            runCatching { repository.getPlaylists() }
                .onSuccess {
                    serverPlaylists = it
                    publishPlaylists()
                }
                .onFailure { error ->
                    _uiState.value = PlaylistsUiState(
                        playlists = localPlaylists.map(::localPlaylist),
                        errorMessage = if (localPlaylists.isEmpty()) error.message.orEmpty() else null
                    )
                }
        }
    }

    fun onEvent(event: PlaylistsEvent) {
        when (event) {
            is PlaylistsEvent.OnPlaylistSelected -> {
                viewModelScope.launch {
                    runCatching { repository.getPlaylistTracks(event.playlist.id) }
                        .onSuccess { tracks ->
                            if (tracks.isNotEmpty()) {
                                val first = tracks.first()
                                val downloaded = downloadedTrackDao.find(first.id)
                                val playUrl = if (downloaded != null && java.io.File(downloaded.localFilePath).exists()) {
                                    downloaded.localFilePath
                                } else {
                                    first.audioUrl
                                }
                                playbackManager.play(
                                    trackId = first.id,
                                    title = first.title,
                                    artist = first.artist,
                                    coverUrl = first.imageUrl,
                                    audioUrl = playUrl
                                )
                            }
                        }
                }
            }
            PlaylistsEvent.OnCreatePlaylistClick -> {
                // Logical handle for floating action button / dynamic database creation
            }
            PlaylistsEvent.OnRetryClick -> loadPlaylists()
        }
    }

    private fun publishPlaylists() {
        _uiState.value = PlaylistsUiState(
            playlists = (serverPlaylists + localPlaylists.map(::localPlaylist)).distinctBy { it.id }
        )
    }

    private fun localPlaylist(summary: UserPlaylistSummary) = Playlist(
        id = "local:${summary.id}",
        title = summary.title,
        trackCount = summary.trackCount,
        type = PlaylistType.USER,
        gradientColors = listOf(Color(0xFF009688), Color(0xFF26A69A))
    )
}

