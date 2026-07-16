package com.example.sickimfy.features.home.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sickimfy.features.home.domain.usecase.GetHomeDataUseCase
import com.example.sickimfy.core.playback.PlaybackManager
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
                    playbackManager.play(
                        trackId = event.track.id,
                        title = event.track.title,
                        artist = event.track.artist,
                        coverUrl = event.track.imageUrl,
                        audioUrl = playUrl
                    )
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

