package com.example.sickimfy.features.downloads.data.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.ForegroundInfo
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.NetworkType
import androidx.work.WorkerParameters
import com.example.sickimfy.core.data.local.dao.DownloadedTrackDao
import com.example.sickimfy.core.data.local.entity.DownloadedTrackEntity
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.net.URL

@HiltWorker
class DownloadWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted private val params: WorkerParameters,
    private val downloadedTrackDao: DownloadedTrackDao
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val trackId = inputData.getString(KEY_TRACK_ID) ?: return Result.failure()
        val title = inputData.getString(KEY_TITLE) ?: return Result.failure()
        val artist = inputData.getString(KEY_ARTIST) ?: return Result.failure()
        val imageUrl = inputData.getString(KEY_IMAGE_URL) ?: ""
        val audioUrl = inputData.getString(KEY_AUDIO_URL) ?: return Result.failure()
        val durationSeconds = inputData.getInt(KEY_DURATION_SECONDS, 0)

        return withContext(Dispatchers.IO) {
            try {
                val downloadsDir = File(context.filesDir, "downloads")
                if (!downloadsDir.exists()) downloadsDir.mkdirs()

                val fileName = "track_${trackId}.mp3"
                val outputFile = File(downloadsDir, fileName)

                if (outputFile.exists()) {
                    saveToDatabase(trackId, title, artist, imageUrl, outputFile.absolutePath, durationSeconds)
                    return@withContext Result.success()
                }

                val url = URL(audioUrl)
                val connection = url.openConnection()
                connection.connectTimeout = 30_000
                connection.readTimeout = 60_000
                connection.connect()

                val inputStream = connection.getInputStream()
                val outputStream = outputFile.outputStream()

                val buffer = ByteArray(8192)
                var bytesRead: Int
                var totalBytes = 0L

                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    outputStream.write(buffer, 0, bytesRead)
                    totalBytes += bytesRead
                }

                outputStream.flush()
                outputStream.close()
                inputStream.close()

                saveToDatabase(trackId, title, artist, imageUrl, outputFile.absolutePath, durationSeconds)

                Result.success()
            } catch (e: Exception) {
                if (runAttemptCount < 3) {
                    Result.retry()
                } else {
                    Result.failure()
                }
            }
        }
    }

    private suspend fun saveToDatabase(
        trackId: String,
        title: String,
        artist: String,
        imageUrl: String,
        localFilePath: String,
        durationSeconds: Int
    ) {
        downloadedTrackDao.upsert(
            DownloadedTrackEntity(
                trackId = trackId,
                title = title,
                artist = artist,
                imageUrl = imageUrl,
                localFilePath = localFilePath,
                durationSeconds = durationSeconds
            )
        )
    }

    companion object {
        const val KEY_TRACK_ID = "track_id"
        const val KEY_TITLE = "title"
        const val KEY_ARTIST = "artist"
        const val KEY_IMAGE_URL = "image_url"
        const val KEY_AUDIO_URL = "audio_url"
        const val KEY_DURATION_SECONDS = "duration_seconds"

        fun enqueue(
            context: Context,
            trackId: String,
            title: String,
            artist: String,
            imageUrl: String,
            audioUrl: String,
            durationSeconds: Int = 0
        ) {
            val inputData = Data.Builder()
                .putString(KEY_TRACK_ID, trackId)
                .putString(KEY_TITLE, title)
                .putString(KEY_ARTIST, artist)
                .putString(KEY_IMAGE_URL, imageUrl)
                .putString(KEY_AUDIO_URL, audioUrl)
                .putInt(KEY_DURATION_SECONDS, durationSeconds)
                .build()

            val workRequest = OneTimeWorkRequestBuilder<DownloadWorker>()
                .setInputData(inputData)
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .build()

            WorkManager.getInstance(context)
                .enqueueUniqueWork(
                    "download_track_$trackId",
                    ExistingWorkPolicy.REPLACE,
                    workRequest
                )
        }
    }
}
