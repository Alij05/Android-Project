package com.example.sickimfy.features.player.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sickimfy.core.data.local.dao.LikedTrackDao
import com.example.sickimfy.core.data.local.dao.DownloadedTrackDao
import com.example.sickimfy.core.data.local.entity.LikedTrackEntity
import com.example.sickimfy.core.data.preferences.UserPreferencesDataStore
import com.example.sickimfy.core.playback.PlaybackManager
import com.example.sickimfy.features.downloads.data.worker.DownloadWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val playbackManager: PlaybackManager,
    private val likedTrackDao: LikedTrackDao,
    private val downloadedTrackDao: DownloadedTrackDao,
    private val preferences: UserPreferencesDataStore,
    @ApplicationContext private val context: Context
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

            PlayerEvent.DownloadTrack -> {
                viewModelScope.launch {
                    val isPremium = preferences.preferences.first().isPremium
                    if (!isPremium) {
                        _uiState.update { it.copy(error = "دانلود فقط برای کاربران ویژه فعال است / Download is only available for Premium members") }
                        return@launch
                    }
                    val trackId = _uiState.value.trackId ?: return@launch
                    val audioUrl = playbackManager.getCurrentAudioUrl() ?: return@launch
                    DownloadWorker.enqueue(
                        context = context,
                        trackId = trackId,
                        title = _uiState.value.title,
                        artist = _uiState.value.artist,
                        imageUrl = _uiState.value.coverUrl,
                        audioUrl = audioUrl,
                        durationSeconds = (_uiState.value.durationMs / 1000).toInt()
                    )
                }
            }

            is PlayerEvent.PlayTrack -> {
                viewModelScope.launch {
                    val downloaded = downloadedTrackDao.find(event.trackId)
                    val playUrl = if (downloaded != null && java.io.File(downloaded.localFilePath).exists()) {
                        downloaded.localFilePath
                    } else {
                        event.audioUrl
                    }
                    playbackManager.play(
                        trackId = event.trackId,
                        title = event.title,
                        artist = event.artist,
                        coverUrl = event.coverUrl,
                        audioUrl = playUrl
                    )
                }
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
            val exists = likedTrackDao.isLiked(trackId)
            _uiState.update { it.copy(isFavorite = exists) }
        }
    }

    override fun onCleared() {
        super.onCleared()
        sleepTimerJob?.cancel()
        positionTrackingJob?.cancel()
    }
}
