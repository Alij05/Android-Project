package com.example.sickimfy.features.home.ui

import com.example.sickimfy.features.home.domain.model.Track

/**
 * User intents and interactions triggered from the Home UI feed.
 */
sealed interface HomeEvent {
    object LoadHomeFeed : HomeEvent
    data class OnTrackSelected(val track: Track) : HomeEvent
    data class OnDownloadTrack(val track: Track) : HomeEvent
    object OnRetryClicked : HomeEvent
}
