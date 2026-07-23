package com.example.sickimfy.core.data.local

import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore
import com.example.sickimfy.features.home.domain.model.Track
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalMusicScanner @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun scanLocalMusic(): List<Track> {
        val tracks = mutableListOf<Track>()

        // 1. Scan bundled assets/music/ files
        tracks.addAll(scanAssetMusic())

        // 2. Scan device MediaStore (requires permission)
        try {
            tracks.addAll(scanMediaStore())
        } catch (_: SecurityException) {
            // Permission not granted, skip MediaStore scan
        }

        return tracks
    }

    private fun scanAssetMusic(): List<Track> {
        val tracks = mutableListOf<Track>()
        try {
            val assetFiles = context.assets.list("music") ?: emptyArray()
            for (fileName in assetFiles) {
                if (!fileName.lowercase().endsWith(".mp3")) continue

                val displayName = fileName.removeSuffix(".mp3")
                val parts = displayName.split(" - ", limit = 2)
                val artist = if (parts.size == 2) parts[0].trim() else "Unknown Artist"
                val title = if (parts.size == 2) parts[1].trim() else displayName

                val assetUri = "file:///android_asset/music/$fileName"

                tracks.add(
                    Track(
                        id = "asset-$fileName",
                        title = title,
                        artist = artist,
                        imageUrl = "",
                        duration = "--:--",
                        albumName = "Local",
                        audioUrl = assetUri
                    )
                )
            }
        } catch (_: Exception) {
            // Assets directory doesn't exist or can't be read
        }
        return tracks
    }

    private fun scanMediaStore(): List<Track> {
        val tracks = mutableListOf<Track>()
        val collection = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DISPLAY_NAME
        )

        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
        val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"

        context.contentResolver.query(
            collection,
            projection,
            selection,
            null,
            sortOrder
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val title = cursor.getString(titleColumn) ?: "Unknown"
                val artist = cursor.getString(artistColumn) ?: "Unknown Artist"
                val album = cursor.getString(albumColumn) ?: ""
                val durationMs = cursor.getLong(durationColumn)

                val contentUri = ContentUris.withAppendedId(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    id
                )

                val durationLabel = formatDuration(durationMs)

                tracks.add(
                    Track(
                        id = "local-$id",
                        title = title,
                        artist = artist,
                        imageUrl = "",
                        duration = durationLabel,
                        albumName = album,
                        audioUrl = contentUri.toString()
                    )
                )
            }
        }

        return tracks
    }

    private fun formatDuration(durationMs: Long): String {
        val totalSeconds = durationMs / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return "%d:%02d".format(minutes, seconds)
    }
}
