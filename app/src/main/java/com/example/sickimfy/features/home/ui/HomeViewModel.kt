package com.example.sickimfy.features.home.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sickimfy.features.home.domain.usecase.GetHomeDataUseCase
import com.example.sickimfy.core.playback.PlaybackManager
import com.example.sickimfy.core.playback.PlaybackQueueItem
import com.example.sickimfy.core.data.local.dao.DownloadedTrackDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getHomeData: GetHomeDataUseCase,
    private val playbackManager: PlaybackManager,
    private val downloadedTrackDao: DownloadedTrackDao
) : ViewModel() {
    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadHome()
    }

    fun onEvent(event: HomeEvent) {
        when (event) {
            HomeEvent.LoadHomeFeed, HomeEvent.OnRetryClicked -> loadHome()
            is HomeEvent.OnTrackSelected -> {
                viewModelScope.launch {
                    val downloaded = downloadedTrackDao.find(event.track.id)
                    val playUrl = if (downloaded != null && java.io.File(downloaded.localFilePath).exists()) {
                        downloaded.localFilePath
                    } else {
                        event.track.audioUrl
                    }
                    val feed = _uiState.value as? HomeUiState.Success ?: return@launch
                    val queue = listOf(
                        feed.carouselTracks,
                        feed.popularTracks,
                        feed.newReleases,
                        feed.globalPlaylists,
                        feed.localPlaylists
                    ).flatten().mapNotNull { track ->
                        val url = if (track.id == event.track.id) playUrl else track.audioUrl
                        url?.takeIf { it.isNotBlank() }?.let {
                            PlaybackQueueItem(track.id, track.title, track.artist, track.imageUrl, it)
                        }
                    }
                    val startIndex = queue.indexOfFirst { it.id == event.track.id }
                    if (startIndex >= 0) playbackManager.playQueue(queue, startIndex)
                }
            }
        }
    }

    private fun loadHome() {
        viewModelScope.launch {
            _uiState.value = HomeUiState.Loading
            _uiState.value = runCatching { getHomeData() }
                .fold(
                    onSuccess = { feed ->
                        HomeUiState.Success(
                            carouselTracks = feed.featured,
                            popularTracks = feed.popular,
                            newReleases = feed.latest,
                            globalPlaylists = feed.globalPlaylists,
                            localPlaylists = feed.localPlaylists
                        )
                    },
                    onFailure = { HomeUiState.Error(it.message.orEmpty()) }
                )
        }
    }
}

