package com.example.sickimfy.features.playlists.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sickimfy.core.data.local.dao.UserPlaylistDao
import com.example.sickimfy.core.data.local.dao.UserPlaylistSummary
import com.example.sickimfy.core.data.local.entity.UserPlaylistEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PersonalPlaylistsViewModel @Inject constructor(
    private val playlistDao: UserPlaylistDao
) : ViewModel() {
    val playlists: StateFlow<List<UserPlaylistSummary>> = playlistDao.observePlaylists()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun createPlaylist(title: String) {
        val cleanTitle = title.trim()
        if (cleanTitle.isBlank()) return
        viewModelScope.launch { playlistDao.createPlaylist(UserPlaylistEntity(title = cleanTitle)) }
    }
}
