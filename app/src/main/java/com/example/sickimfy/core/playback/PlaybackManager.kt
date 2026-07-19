package com.example.sickimfy.core.playback

import android.content.Context
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.Tracks
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

data class PlaybackState(
    val isPlaying: Boolean = false,
    val currentTrackId: String? = null,
    val currentTitle: String = "",
    val currentArtist: String = "",
    val currentCoverUrl: String = "",
    val currentPositionMs: Long = 0L,
    val durationMs: Long = 0L,
    val playbackSpeed: Float = 1f,
    val isLoading: Boolean = false,
    val error: String? = null,
    val shuffleEnabled: Boolean = false,
    val repeatMode: Int = Player.REPEAT_MODE_OFF
)

@Singleton
class PlaybackManager @Inject constructor() {

    private var _exoplayer: ExoPlayer? = null
    private var _simpleCache: SimpleCache? = null

    private val _playbackState = MutableStateFlow(PlaybackState())
    val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

    private val _playlist = MutableStateFlow<List<MediaItem>>(emptyList())
    val playlist: StateFlow<List<MediaItem>> = _playlist.asStateFlow()

    @UnstableApi
    fun initialize(context: Context) {
        if (_exoplayer != null) return

        val cacheDir = File(context.cacheDir, "media_cache")
        val databaseProvider = StandaloneDatabaseProvider(context)
        val evictor = androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor(100 * 1024 * 1024L) // 100MB
        val simpleCache = SimpleCache(cacheDir, evictor, databaseProvider)
        _simpleCache = simpleCache

        val cacheDataSourceFactory = CacheDataSource.Factory()
            .setCache(simpleCache)
            .setUpstreamDataSourceFactory(DefaultHttpDataSource.Factory())
            .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)

        val mediaSourceFactory = DefaultMediaSourceFactory(cacheDataSourceFactory)

        val audioAttributes = AudioAttributes.Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .build()

        _exoplayer = ExoPlayer.Builder(context)
            .setMediaSourceFactory(mediaSourceFactory)
            .setHandleAudioBecomingNoisy(true)
            .setWakeMode(C.WAKE_MODE_NETWORK)
            .setAudioAttributes(audioAttributes, /* handleAudioFocus= */ true)
            .build()
            .apply {
                addListener(playerListener)
                repeatMode = Player.REPEAT_MODE_OFF
            }
    }

    fun play(trackId: String, title: String, artist: String, coverUrl: String, audioUrl: String?) {
        val player = _exoplayer ?: return
        val mediaItem = MediaItem.Builder()
            .setMediaId(trackId)
            .setUri(audioUrl)
            .setMediaMetadata(
                androidx.media3.common.MediaMetadata.Builder()
                    .setTitle(title)
                    .setArtist(artist)
                    .setArtworkUri(android.net.Uri.parse(coverUrl))
                    .build()
            )
            .build()

        val trackIndex = _playlist.value.indexOfFirst { it.mediaId == trackId }
        if (trackIndex >= 0) {
            player.seekTo(trackIndex, 0)
            player.play()
        } else {
            val currentList = _playlist.value.toMutableList()
            currentList.add(mediaItem)
            _playlist.value = currentList
            player.setMediaItems(currentList, currentList.size - 1, 0)
            player.prepare()
            player.play()
        }

        _playbackState.update {
            it.copy(
                currentTrackId = trackId,
                currentTitle = title,
                currentArtist = artist,
                currentCoverUrl = coverUrl,
                isLoading = true
            )
        }
    }

    fun playTrackList(tracks: List<Triple<String, String, String>>, startIndex: Int = 0) {
        val player = _exoplayer ?: return
        val mediaItems = tracks.map { (id, title, audioUrl) ->
            MediaItem.Builder()
                .setMediaId(id)
                .setUri(audioUrl)
                .build()
        }
        _playlist.value = mediaItems
        player.setMediaItems(mediaItems, startIndex, 0)
        player.prepare()
        player.play()
    }

    fun playAll(tracks: List<Triple<String, String, String>>) {
        playTrackList(tracks, 0)
    }

    fun pause() {
        _exoplayer?.pause()
        _playbackState.update { it.copy(isPlaying = false) }
    }

    fun resume() {
        _exoplayer?.play()
        _playbackState.update { it.copy(isPlaying = true) }
    }

    fun togglePlayPause() {
        val player = _exoplayer ?: return
        if (player.isPlaying) pause() else resume()
    }

    fun seekTo(positionMs: Long) {
        _exoplayer?.seekTo(positionMs)
        _playbackState.update { it.copy(currentPositionMs = positionMs) }
    }

    fun seekForward(ms: Long = 10_000L) {
        val player = _exoplayer ?: return
        val newPos = (player.currentPosition + ms).coerceAtMost(player.duration)
        player.seekTo(newPos)
    }

    fun seekBackward(ms: Long = 10_000L) {
        val player = _exoplayer ?: return
        val newPos = (player.currentPosition - ms).coerceAtLeast(0)
        player.seekTo(newPos)
    }

    fun skipToNext() {
        val player = _exoplayer ?: return
        if (player.hasNextMediaItem()) {
            player.seekToNext()
        }
    }

    fun skipToPrevious() {
        val player = _exoplayer ?: return
        if (player.currentPosition > 3000) {
            player.seekTo(0)
        } else if (player.hasPreviousMediaItem()) {
            player.seekToPrevious()
        }
    }

    fun setPlaybackSpeed(speed: Float) {
        _exoplayer?.setPlaybackSpeed(speed)
        _playbackState.update { it.copy(playbackSpeed = speed) }
    }

    fun setShuffleMode(enabled: Boolean) {
        _exoplayer?.shuffleModeEnabled = enabled
        _playbackState.update { it.copy(shuffleEnabled = enabled) }
    }

    fun setRepeatMode(mode: Int) {
        _exoplayer?.repeatMode = mode
        _playbackState.update { it.copy(repeatMode = mode) }
    }

    fun getShuffleEnabled(): Boolean = _exoplayer?.shuffleModeEnabled == true
    fun getRepeatMode(): Int = _exoplayer?.repeatMode ?: Player.REPEAT_MODE_OFF

    fun getCurrentPosition(): Long = _exoplayer?.currentPosition ?: 0L
    fun getDuration(): Long = _exoplayer?.duration ?: 0L
    fun isCurrentlyPlaying(): Boolean = _exoplayer?.isPlaying == true

    fun getCurrentTrackMediaId(): String? = _exoplayer?.currentMediaItem?.mediaId

    fun getCurrentAudioUrl(): String? = _exoplayer?.currentMediaItem?.localConfiguration?.uri?.toString()

    fun getExoplayer(): ExoPlayer? = _exoplayer

    fun release() {
        _exoplayer?.removeListener(playerListener)
        _exoplayer?.release()
        _exoplayer = null
        _simpleCache?.release()
        _simpleCache = null
        _playbackState.value = PlaybackState()
        _playlist.value = emptyList()
    }

    private val playerListener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _playbackState.update { it.copy(isPlaying = isPlaying) }
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            when (playbackState) {
                Player.STATE_BUFFERING -> {
                    _playbackState.update { it.copy(isLoading = true, error = null) }
                }
                Player.STATE_READY -> {
                    _playbackState.update {
                        it.copy(
                            isLoading = false,
                            durationMs = _exoplayer?.duration ?: 0L,
                            currentPositionMs = _exoplayer?.currentPosition ?: 0L
                        )
                    }
                }
                Player.STATE_ENDED -> {
                    _playbackState.update { it.copy(isPlaying = false) }
                }
                Player.STATE_IDLE -> {
                    _playbackState.update { it.copy(isLoading = false) }
                }
            }
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            val item = _exoplayer?.currentMediaItem ?: return
            _playbackState.update {
                it.copy(
                    currentTrackId = item.mediaId,
                    currentTitle = item.mediaMetadata.title?.toString().orEmpty(),
                    currentArtist = item.mediaMetadata.artist?.toString().orEmpty(),
                    currentCoverUrl = item.mediaMetadata.artworkUri?.toString().orEmpty(),
                    currentPositionMs = 0L,
                    durationMs = _exoplayer?.duration ?: 0L
                )
            }
        }

        override fun onTracksChanged(tracks: Tracks) {
            // No-op, handled by media item transition
        }

        override fun onPlayerError(error: PlaybackException) {
            _playbackState.update {
                it.copy(
                    error = error.message ?: "Playback error",
                    isLoading = false
                )
            }
        }
    }
}
