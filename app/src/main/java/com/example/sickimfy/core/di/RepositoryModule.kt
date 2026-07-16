package com.example.sickimfy.core.di

import com.example.sickimfy.features.chat.data.repository.ChatRepositoryImpl
import com.example.sickimfy.features.chat.domain.repository.ChatRepository
import com.example.sickimfy.features.home.data.repository.HomeRepositoryImpl
import com.example.sickimfy.features.home.domain.repository.HomeRepository
import com.example.sickimfy.features.playlists.data.repository.PlaylistsRepositoryImpl
import com.example.sickimfy.features.playlists.domain.repository.PlaylistsRepository
import com.example.sickimfy.features.profile.data.repository.ProfileRepositoryImpl
import com.example.sickimfy.features.profile.domain.repository.ProfileRepository
import com.example.sickimfy.features.search.data.repository.SearchRepositoryImpl
import com.example.sickimfy.features.search.domain.repository.SearchRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds abstract fun bindHomeRepository(implementation: HomeRepositoryImpl): HomeRepository
    @Binds abstract fun bindSearchRepository(implementation: SearchRepositoryImpl): SearchRepository
    @Binds abstract fun bindPlaylistsRepository(implementation: PlaylistsRepositoryImpl): PlaylistsRepository
    @Binds abstract fun bindProfileRepository(implementation: ProfileRepositoryImpl): ProfileRepository
    @Binds abstract fun bindChatRepository(implementation: ChatRepositoryImpl): ChatRepository
}
