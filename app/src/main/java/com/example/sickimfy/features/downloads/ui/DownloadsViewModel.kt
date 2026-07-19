package com.example.sickimfy.features.downloads.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sickimfy.features.home.domain.model.Track
import com.example.sickimfy.core.data.local.dao.DownloadedTrackDao
import com.example.sickimfy.core.data.local.entity.DownloadedTrackEntity
import com.example.sickimfy.core.playback.PlaybackManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class DownloadsViewModel @Inject constructor(
    private val downloadedTrackDao: DownloadedTrackDao,
    private val playbackManager: PlaybackManager
) : ViewModel() {

    private val _sortOption = MutableStateFlow(SortOption.DATE_ADDED)

    val uiState: StateFlow<DownloadsUiState> = combine(
        downloadedTrackDao.observeAll(),
        _sortOption
    ) { entities, sortOption ->
        val tracks = entities.map { it.toDomain() }
        val sortedTracks = when (sortOption) {
            SortOption.TITLE -> tracks.sortedBy { it.title }
            SortOption.ARTIST -> tracks.sortedBy { it.artist }
            SortOption.DATE_ADDED -> tracks
        }
        DownloadsUiState(
            downloadedTracks = sortedTracks,
            sortOption = sortOption,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DownloadsUiState(isLoading = true)
    )

    fun onEvent(event: DownloadsEvent) {
        when (event) {
            is DownloadsEvent.OnSortOptionChanged -> {
                _sortOption.value = event.option
            }
            is DownloadsEvent.OnDeleteTrack -> {
                viewModelScope.launch {
                    val downloaded = downloadedTrackDao.find(event.track.id)
                    if (downloaded != null) {
                        val file = File(downloaded.localFilePath)
                        if (file.exists()) {
                            file.delete()
                        }
                        downloadedTrackDao.delete(event.track.id)
                    }
                }
            }
            is DownloadsEvent.OnTrackSelected -> {
                viewModelScope.launch {
                    val downloaded = downloadedTrackDao.find(event.track.id)
                    val playUrl = if (downloaded != null && File(downloaded.localFilePath).exists()) {
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

    private fun DownloadedTrackEntity.toDomain() = Track(
        id = trackId,
        title = title,
        artist = artist,
        imageUrl = imageUrl,
        duration = "%d:%02d".format(durationSeconds?.div(60), durationSeconds?.rem(60)),
        albumName = ""
    )
}
