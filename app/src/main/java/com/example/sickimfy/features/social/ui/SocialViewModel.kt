package com.example.sickimfy.features.social.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sickimfy.core.network.SickimfyApi
import com.example.sickimfy.core.network.dto.PublicProfileDto
import com.example.sickimfy.core.network.dto.CreateConversationRequestDto
import com.example.sickimfy.core.network.dto.PlaylistSummaryDto
import com.example.sickimfy.core.network.dto.toDomain
import com.example.sickimfy.core.data.preferences.UserPreferencesDataStore
import com.example.sickimfy.core.playback.PlaybackManager
import com.example.sickimfy.core.playback.PlaybackQueueItem
import kotlinx.coroutines.flow.first
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SocialUiState(
    val searchQuery: String = "",
    val searchResults: List<PublicProfileDto> = emptyList(),
    val followedFriends: List<PublicProfileDto> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedUserPlaylists: List<com.example.sickimfy.core.network.dto.PlaylistSummaryDto> = emptyList(),
    val selectedUser: PublicProfileDto? = null
)

@HiltViewModel
class SocialViewModel @Inject constructor(
    private val api: SickimfyApi,
    private val preferences: UserPreferencesDataStore,
    private val playbackManager: PlaybackManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(SocialUiState())
    val uiState: StateFlow<SocialUiState> = _uiState.asStateFlow()

    init {
        loadFollowedUsers()
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        if (query.isNotBlank()) {
            performSearch(query)
        } else {
            _uiState.update { it.copy(searchResults = emptyList()) }
        }
    }

    fun loadFollowedUsers() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            runCatching {
                api.getFollowedUsers()
            }.onSuccess { list ->
                _uiState.update { it.copy(isLoading = false, followedFriends = list.map { it.user }) }
            }.onFailure { err ->
                _uiState.update { it.copy(isLoading = false, error = err.message) }
            }
        }
    }

    private fun performSearch(query: String) {
        viewModelScope.launch {
            runCatching {
                api.searchUsers(query)
            }.onSuccess { list ->
                _uiState.update { it.copy(searchResults = list) }
            }
        }
    }

    fun toggleFollow(user: PublicProfileDto) {
        val isFollowing = _uiState.value.followedFriends.any { it.id == user.id }
        viewModelScope.launch {
            runCatching {
                if (isFollowing) {
                    api.unfollowUser(user.id)
                } else {
                    api.followUser(user.id)
                }
            }.onSuccess {
                loadFollowedUsers()
            }
        }
    }

    fun selectUser(user: PublicProfileDto?) {
        _uiState.update { it.copy(selectedUser = user, selectedUserPlaylists = emptyList()) }
        if (user != null) {
            loadUserPlaylists(user.id)
        }
    }

    private fun loadUserPlaylists(userId: Int) {
        viewModelScope.launch {
            runCatching {
                // Since api.getPublicPlaylists returns all public playlists, we filter those belonging to this user
                api.getPublicPlaylists().filter { it.owner.id == userId }
            }.onSuccess { list ->
                _uiState.update { it.copy(selectedUserPlaylists = list) }
            }
        }
    }

    fun startChat(user: PublicProfileDto, onChatStarted: (conversationId: Int, user: PublicProfileDto) -> Unit) {
        viewModelScope.launch {
            runCatching {
                api.getOrCreateConversation(CreateConversationRequestDto(user.id))
            }.onSuccess { convo ->
                onChatStarted(convo.id, user)
            }
        }
    }

    fun playPublicPlaylist(playlist: PlaylistSummaryDto) {
        viewModelScope.launch {
            runCatching {
                val baseUrl = preferences.preferences.first().apiBaseUrl
                api.getPlaylistDetails(playlist.id).tracks.map { it.toDomain(baseUrl) }
            }.onSuccess { tracks ->
                val queue = tracks.mapNotNull { track ->
                    track.audioUrl?.takeIf { it.isNotBlank() }?.let {
                        PlaybackQueueItem(track.id, track.title, track.artist, track.imageUrl, it)
                    }
                }
                if (queue.isNotEmpty()) playbackManager.playQueue(queue, 0)
            }
        }
    }
}
