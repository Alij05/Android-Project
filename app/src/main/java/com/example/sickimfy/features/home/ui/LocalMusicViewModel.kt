package com.example.sickimfy.features.home.ui

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sickimfy.core.data.local.LocalMusicScanner
import com.example.sickimfy.core.playback.PlaybackManager
import com.example.sickimfy.features.home.domain.model.Track
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LocalMusicUiState(
    val tracks: List<Track> = emptyList(),
    val isLoading: Boolean = false,
    val hasPermission: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class LocalMusicViewModel @Inject constructor(
    private val localMusicScanner: LocalMusicScanner,
    private val playbackManager: PlaybackManager,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(LocalMusicUiState())
    val uiState: StateFlow<LocalMusicUiState> = _uiState.asStateFlow()

    init {
        checkPermission()
        // Always load music (bundled assets work without permission)
        loadLocalMusic()
    }

    fun checkPermission() {
        val hasPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.READ_MEDIA_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
        _uiState.update { it.copy(hasPermission = hasPermission) }
    }

    fun loadLocalMusic() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val tracks = localMusicScanner.scanLocalMusic()
                _uiState.update { it.copy(tracks = tracks, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
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
        val tracks = _uiState.value.tracks
        if (tracks.isNotEmpty()) {
            val list = tracks.map { Triple(it.id, it.title, it.audioUrl) }
            val safeList = list.map { triple ->
                Triple(triple.first, triple.second, triple.third ?: "")
            }
            playbackManager.playAll(safeList)
        }
    }
}
