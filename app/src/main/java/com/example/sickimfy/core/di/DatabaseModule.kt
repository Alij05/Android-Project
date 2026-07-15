package com.example.sickimfy.core.di

import android.content.Context
import androidx.room.Room
import com.example.sickimfy.core.data.local.AppDatabase
import com.example.sickimfy.core.data.local.dao.DownloadedTrackDao
import com.example.sickimfy.core.data.local.dao.LikedTrackDao
import com.example.sickimfy.core.data.local.dao.OfflineMessageDao
import com.example.sickimfy.core.data.local.dao.SearchHistoryDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "sickimfy.db").build()

    @Provides fun provideSearchHistoryDao(database: AppDatabase): SearchHistoryDao = database.searchHistoryDao()
    @Provides fun provideLikedTrackDao(database: AppDatabase): LikedTrackDao = database.likedTrackDao()
    @Provides fun provideDownloadedTrackDao(database: AppDatabase): DownloadedTrackDao = database.downloadedTrackDao()
    @Provides fun provideOfflineMessageDao(database: AppDatabase): OfflineMessageDao = database.offlineMessageDao()
}
