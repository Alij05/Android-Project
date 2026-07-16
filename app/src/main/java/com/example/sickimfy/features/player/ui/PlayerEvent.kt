package com.example.sickimfy.features.player.ui

sealed interface PlayerEvent {
    data object PlayPause : PlayerEvent
    data object SkipNext : PlayerEvent
    data object SkipPrevious : PlayerEvent
    data object SeekForward : PlayerEvent
    data object SeekBackward : PlayerEvent
    data class SeekTo(val positionMs: Long) : PlayerEvent
    data class SetSpeed(val speed: Float) : PlayerEvent
    data object ToggleShuffle : PlayerEvent
    data object ToggleRepeat : PlayerEvent
    data class SetSleepTimer(val minutes: Int?) : PlayerEvent
    data object CancelSleepTimer : PlayerEvent
    data object ToggleFavorite : PlayerEvent
    data class PlayTrack(
        val trackId: String,
        val title: String,
        val artist: String,
        val coverUrl: String,
        val audioUrl: String
    ) : PlayerEvent
}
