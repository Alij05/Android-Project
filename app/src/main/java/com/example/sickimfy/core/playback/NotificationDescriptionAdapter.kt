package com.example.sickimfy.core.playback

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import androidx.annotation.OptIn
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaDescription
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import kotlinx.coroutines.runBlocking

@OptIn(UnstableApi::class)
class NotificationDescriptionAdapter(
    private val context: Context
) : MediaSessionService.MediaNotification.Provider {

    override fun getMediaNotification(
        session: MediaSession,
        player: Player
    ): MediaSessionService.MediaNotification? {
        val metadata = player.currentMediaItem?.mediaMetadata ?: return null
        val description = MediaDescription.Builder()
            .setMediaId(player.currentMediaItem?.mediaId ?: "")
            .setTitle(metadata.title)
            .setArtist(metadata.artist)
            .setIconUri(metadata.artworkUri)
            .build()

        return MediaSessionService.MediaNotification(
            player.notificationCompatActions(),
            listOf(description)
        )
    }

    private fun Player.notificationCompatActions(): List<androidx.core.app.NotificationCompat.Action> {
        return emptyList()
    }
}

suspend fun loadBitmapFromUrl(context: Context, url: String): Bitmap? {
    return try {
        val loader = ImageLoader(context)
        val request = ImageRequest.Builder(context)
            .data(url)
            .allowHardware(false)
            .build()
        val result = loader.execute(request)
        if (result is SuccessResult) {
            (result.drawable as? BitmapDrawable)?.bitmap
        } else null
    } catch (_: Exception) {
        null
    }
}
