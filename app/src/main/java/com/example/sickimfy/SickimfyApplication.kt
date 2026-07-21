package com.example.sickimfy

import android.app.Application
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.example.sickimfy.core.playback.PlaybackManager
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class SickimfyApplication : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory
    @Inject lateinit var playbackManager: PlaybackManager

    @OptIn(UnstableApi::class)
    override fun onCreate() {
        super.onCreate()
        playbackManager.initialize(this)
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}
