package com.example.sickimfy.features.downloads.ui

import com.example.sickimfy.features.home.domain.model.Track

sealed interface DownloadsEvent {
    data class OnDeleteTrack(val track: Track) : DownloadsEvent
    data class OnSortOptionChanged(val option: SortOption) : DownloadsEvent
    data class OnTrackSelected(val track: Track) : DownloadsEvent
}