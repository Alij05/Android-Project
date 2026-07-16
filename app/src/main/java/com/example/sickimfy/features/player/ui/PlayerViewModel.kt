package com.example.sickimfy.features.player.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sickimfy.core.data.local.dao.LikedTrackDao
import com.example.sickimfy.core.data.local.entity.LikedTrackEntity
import com.example.sickimfy.core.playback.PlaybackManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val playbackManager: PlaybackManager,
    private val likedTrackDao: LikedTrackDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    private var sleepTimerJob: Job? = null
    private var positionTrackingJob: Job? = null

    init {
        observePlaybackState()
        startPositionTracking()
    }

    fun onEvent(event: PlayerEvent) {
        when (event) {
            PlayerEvent.PlayPause -> playbackManager.togglePlayPause()

            PlayerEvent.SkipNext -> playbackManager.skipToNext()

            PlayerEvent.SkipPrevious -> playbackManager.skipToPrevious()

            PlayerEvent.SeekForward -> playbackManager.seekForward()

            PlayerEvent.SeekBackward -> playbackManager.seekBackward()

            is PlayerEvent.SeekTo -> playbackManager.seekTo(event.positionMs)

            is PlayerEvent.SetSpeed -> playbackManager.setPlaybackSpeed(event.speed)

            PlayerEvent.ToggleShuffle -> {
                val currentState = playbackManager.playbackState.value
                playbackManager.setShuffleMode(!currentState.isPlaying)
            }

            PlayerEvent.ToggleRepeat -> {
                val player = playbackManager
                val currentRepeat = 0
                player.setRepeatMode(
                    when (currentRepeat) {
                        0 -> 1
                        1 -> 2
                        else -> 0
                    }
                )
            }

            is PlayerEvent.SetSleepTimer -> {
                cancelSleepTimer()
                event.minutes?.let { minutes ->
                    startSleepTimer(minutes)
                }
            }

            PlayerEvent.CancelSleepTimer -> cancelSleepTimer()

            PlayerEvent.ToggleFavorite -> toggleFavorite()

            is PlayerEvent.PlayTrack -> {
                playbackManager.play(
                    trackId = event.trackId,
                    title = event.title,
                    artist = event.artist,
                    coverUrl = event.coverUrl,
                    audioUrl = event.audioUrl
                )
            }
        }
    }

    private fun observePlaybackState() {
        viewModelScope.launch {
            playbackManager.playbackState.collect { state ->
                _uiState.update {
                    it.copy(
                        trackId = state.currentTrackId,
                        title = state.currentTitle,
                        artist = state.currentArtist,
                        coverUrl = state.currentCoverUrl,
                        isPlaying = state.isPlaying,
                        currentPositionMs = state.currentPositionMs,
                        durationMs = state.durationMs,
                        playbackSpeed = state.playbackSpeed,
                        isLoading = state.isLoading,
                        error = state.error
                    )
                }

                state.currentTrackId?.let { trackId ->
                    checkFavorite(trackId)
                }
            }
        }
    }

    private fun startPositionTracking() {
        positionTrackingJob = viewModelScope.launch {
            while (true) {
                delay(500)
                if (playbackManager.isCurrentlyPlaying()) {
                    val position = playbackManager.getCurrentPosition()
                    val duration = playbackManager.getDuration()
                    _uiState.update {
                        it.copy(
                            currentPositionMs = position,
                            durationMs = duration
                        )
                    }
                }
            }
        }
    }

    private fun startSleepTimer(minutes: Int) {
        _uiState.update { it.copy(sleepTimerMinutes = minutes, sleepTimerRunning = true) }
        sleepTimerJob = viewModelScope.launch {
            delay(minutes * 60L * 1000L)
            playbackManager.pause()
            _uiState.update { it.copy(sleepTimerMinutes = null, sleepTimerRunning = false) }
        }
    }

    private fun cancelSleepTimer() {
        sleepTimerJob?.cancel()
        sleepTimerJob = null
        _uiState.update { it.copy(sleepTimerMinutes = null, sleepTimerRunning = false) }
    }

    private fun toggleFavorite() {
        val trackId = _uiState.value.trackId ?: return
        viewModelScope.launch {
            val isFav = _uiState.value.isFavorite
            if (isFav) {
                likedTrackDao.delete(trackId)
            } else {
                likedTrackDao.upsert(
                    LikedTrackEntity(
                        trackId = trackId,
                        title = _uiState.value.title,
                        artist = _uiState.value.artist,
                        imageUrl = _uiState.value.coverUrl,
                        audioUrl = null
                    )
                )
            }
            _uiState.update { it.copy(isFavorite = !isFav) }
        }
    }

    private fun checkFavorite(trackId: String) {
        viewModelScope.launch {
            val exists = likedTrackDao.observeAll().value.any { it.trackId == trackId }
            _uiState.update { it.copy(isFavorite = exists) }
        }
    }

    override fun onCleared() {
        super.onCleared()
        sleepTimerJob?.cancel()
        positionTrackingJob?.cancel()
    }
}
