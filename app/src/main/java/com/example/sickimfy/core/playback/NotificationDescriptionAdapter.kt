////package com.example.sickimfy.core.playback
////
////import android.content.Context
////import android.graphics.Bitmap
////import android.graphics.drawable.BitmapDrawable
////import androidx.annotation.OptIn
////import androidx.media3.common.Player
////import androidx.media3.common.util.UnstableApi
////import androidx.media3.session.MediaDescription
////import androidx.media3.session.MediaSession
////import androidx.media3.session.MediaSessionService
////import coil.ImageLoader
////import coil.request.ImageRequest
////import coil.request.SuccessResult
////import kotlinx.coroutines.runBlocking
////
////@OptIn(UnstableApi::class)
////class NotificationDescriptionAdapter(
////    private val context: Context
////) : MediaSessionService.MediaNotification.Provider {
////
////    override fun getMediaNotification(
////        session: MediaSession,
////        player: Player
////    ): MediaSessionService.MediaNotification? {
////        val metadata = player.currentMediaItem?.mediaMetadata ?: return null
////        val description = MediaDescription.Builder()
////            .setMediaId(player.currentMediaItem?.mediaId ?: "")
////            .setTitle(metadata.title)
////            .setArtist(metadata.artist)
////            .setIconUri(metadata.artworkUri)
////            .build()
////
////        return MediaSessionService.MediaNotification(
////            player.notificationCompatActions(),
////            listOf(description)
////        )
////    }
////
////    private fun Player.notificationCompatActions(): List<androidx.core.app.NotificationCompat.Action> {
////        return emptyList()
////    }
////}
////
////suspend fun loadBitmapFromUrl(context: Context, url: String): Bitmap? {
////    return try {
////        val loader = ImageLoader(context)
////        val request = ImageRequest.Builder(context)
////            .data(url)
////            .allowHardware(false)
////            .build()
////        val result = loader.execute(request)
////        if (result is SuccessResult) {
////            (result.drawable as? BitmapDrawable)?.bitmap
////        } else null
////    } catch (_: Exception) {
////        null
////    }
////}
//package com.example.sickimfy.core.playback
//
//import android.app.Notification
//import android.content.Context
//import android.graphics.Bitmap
//import android.graphics.drawable.BitmapDrawable
//import androidx.annotation.OptIn
//import androidx.core.app.NotificationCompat
//import androidx.media3.common.util.UnstableApi
//import androidx.media3.session.CommandButton
//import androidx.media3.session.MediaNotification
//import androidx.media3.session.MediaSession
//import coil.ImageLoader
//import coil.request.ImageRequest
//import coil.request.SuccessResult
//import com.google.common.collect.ImmutableList
//
//@OptIn(UnstableApi::class)
//class NotificationDescriptionAdapter(
//    private val context: Context
//) : MediaNotification.Provider {
//
//    override fun createNotification(
//        mediaSession: MediaSession,
//        mediaButtonPreferences: ImmutableList<CommandButton>,
//        actionFactory: MediaNotification.ActionFactory,
//        onNotificationChangedCallback: MediaNotification.Provider.Callback
//    ): MediaNotification {
//
//        val notificationBuilder = NotificationCompat.Builder(context, "playback_channel_id")
//            .setContentTitle(mediaSession.player.currentMediaItem?.mediaMetadata?.title ?: "Unknown Title")
//            .setContentText(mediaSession.player.currentMediaItem?.mediaMetadata?.artist ?: "Unknown Artist")
//            .setSmallIcon(android.R.drawable.ic_media_play)
//
//        val notificationId = 101
//
//        return MediaNotification(notificationId, notificationBuilder.build())
//    }
//
//    override fun handleCustomCommand(
//        session: MediaSession,
//        action: String,
//        extras: android.os.Bundle
//    ): Boolean {
//        return false
//    }
//}
//
//suspend fun loadBitmapFromUrl(context: Context, url: String): Bitmap? {
//    return try {
//        val loader = ImageLoader(context)
//        val request = ImageRequest.Builder(context)
//            .data(url)
//            .allowHardware(false)
//            .build()
//        val result = loader.execute(request)
//        if (result is SuccessResult) {
//            (result.drawable as? BitmapDrawable)?.bitmap
//        } else null
//    } catch (_: Exception) {
//        null
//    }
//}
