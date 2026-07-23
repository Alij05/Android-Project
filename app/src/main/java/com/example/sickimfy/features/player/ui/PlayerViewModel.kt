package com.example.sickimfy.features.player.ui

import android.content.Context
import androidx.annotation.OptIn
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sickimfy.core.data.local.dao.LikedTrackDao
import com.example.sickimfy.core.data.local.dao.DownloadedTrackDao
import com.example.sickimfy.core.data.local.dao.RecentlyPlayedDao
import com.example.sickimfy.core.data.local.entity.LikedTrackEntity
import com.example.sickimfy.core.data.local.entity.RecentlyPlayedEntity
import com.example.sickimfy.core.playback.PlaybackManager
import com.example.sickimfy.features.downloads.data.worker.DownloadWorker
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlinx.coroutines.flow.firstOrNull
import java.io.File

@HiltViewModel
class PlayerViewModel @OptIn(UnstableApi::class)
@Inject constructor(
    private val playbackManager: PlaybackManager,
    private val likedTrackDao: LikedTrackDao,
    private val recentlyPlayedDao: RecentlyPlayedDao,
    private val downloadedTrackDao: DownloadedTrackDao,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    private var sleepTimerJob: Job? = null
    private var positionTrackingJob: Job? = null
    private var lastRecordedTrackId: String? = null

    init {
        observePlaybackState()
        startPositionTracking()
    }

    @OptIn(UnstableApi::class)
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
                playbackManager.setShuffleMode(!playbackManager.getShuffleEnabled())
            }
            PlayerEvent.ToggleRepeat -> {
                val repeatOne = playbackManager.getRepeatMode() == Player.REPEAT_MODE_ONE
                playbackManager.setRepeatMode(
                    if (repeatOne) Player.REPEAT_MODE_OFF else Player.REPEAT_MODE_ONE
                )
            }

            is PlayerEvent.SetSleepTimer -> {
                cancelSleepTimer()
                event.minutes?.let { startSleepTimer(it) }
            }
            PlayerEvent.CancelSleepTimer -> cancelSleepTimer()
            PlayerEvent.ToggleFavorite -> toggleFavorite()

            PlayerEvent.DownloadTrack -> {
                viewModelScope.launch {
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
//            is PlayerEvent.PlayTrack -> {
//                viewModelScope.launch {
//                    val downloaded = downloadedTrackDao.find(event.trackId)
//                    val playUrl = if (downloaded != null && File(downloaded.localFilePath).exists()) {
//                        "file://${downloaded.localFilePath}"
//                    } else {
//                        event.audioUrl
//                    }
//                    playbackManager.play(...)
//                }
//            }
            is PlayerEvent.PlayTrack -> {
                viewModelScope.launch {
                    val downloaded = downloadedTrackDao.find(event.trackId)
                    val playUrl = if (downloaded != null && File(downloaded.localFilePath).exists()) {
                        "file://${downloaded.localFilePath}"
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
                        audioUrl = state.currentAudioUrl,
                        isPlaying = state.isPlaying,
                        currentPositionMs = state.currentPositionMs,
                        durationMs = state.durationMs,
                        playbackSpeed = state.playbackSpeed,
                        isLoading = state.isLoading,
                        error = state.error,
                        shuffleEnabled = state.shuffleEnabled,
                        repeatMode = state.repeatMode
                    )
                }
                state.currentTrackId?.let {
                    checkFavorite(it)
                    recordRecentlyPlayed(state)
                }
            }
        }
    }

    @OptIn(UnstableApi::class)
    private fun startPositionTracking() {
        positionTrackingJob = viewModelScope.launch {
            while (true) {
                delay(500)
                if (playbackManager.isCurrentlyPlaying()) {
                    _uiState.update {
                        it.copy(
                            currentPositionMs = playbackManager.getCurrentPosition(),
                            durationMs = playbackManager.getDuration()
                        )
                    }
                }
            }
        }
    }

    @OptIn(UnstableApi::class)
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

    @OptIn(UnstableApi::class)
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
                        audioUrl = playbackManager.getCurrentAudioUrl()
                    )
                )
            }
            _uiState.update { it.copy(isFavorite = !isFav) }
        }
    }

    @OptIn(UnstableApi::class)
    private fun recordRecentlyPlayed(state: com.example.sickimfy.core.playback.PlaybackState) {
        if (state.currentTrackId == lastRecordedTrackId) return
        lastRecordedTrackId = state.currentTrackId
        viewModelScope.launch {
            recentlyPlayedDao.insert(
                RecentlyPlayedEntity(
                    trackId = state.currentTrackId ?: return@launch,
                    title = state.currentTitle,
                    artist = state.currentArtist,
                    imageUrl = state.currentCoverUrl,
                    audioUrl = playbackManager.getCurrentAudioUrl()
                )
            )
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
