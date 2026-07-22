package com.example.sickimfy.features.chat.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sickimfy.core.network.SickimfyApi
import com.example.sickimfy.core.network.dto.ConversationSummaryDto
import com.example.sickimfy.core.network.dto.CreateConversationRequestDto
import com.example.sickimfy.core.network.dto.PublicProfileDto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ConversationsUiState(
    val conversations: List<ConversationSummaryDto> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val userResults: List<PublicProfileDto> = emptyList(),
    val isSearchingUsers: Boolean = false,
    val userSearchError: String? = null
)

@HiltViewModel
class ConversationsViewModel @Inject constructor(
    private val api: SickimfyApi
) : ViewModel() {

    private val _uiState = MutableStateFlow(ConversationsUiState())
    val uiState: StateFlow<ConversationsUiState> = _uiState.asStateFlow()

    init {
        loadConversations()
    }

    fun loadConversations() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            runCatching {
                api.getConversations()
            }.onSuccess { list ->
                _uiState.update { it.copy(isLoading = false, conversations = list) }
            }.onFailure { err ->
                _uiState.update { it.copy(isLoading = false, error = err.message) }
            }
        }
    }

    fun searchUsers(query: String) {
        val cleanQuery = query.trim()
        if (cleanQuery.isBlank()) {
            _uiState.update { it.copy(userResults = emptyList(), userSearchError = null, isSearchingUsers = false) }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSearchingUsers = true, userSearchError = null) }
            runCatching { api.searchUsers(cleanQuery) }
                .onSuccess { users ->
                    _uiState.update { it.copy(isSearchingUsers = false, userResults = users) }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isSearchingUsers = false, userResults = emptyList(), userSearchError = error.message) }
                }
        }
    }

    fun startConversation(user: PublicProfileDto, onStarted: (ConversationSummaryDto) -> Unit) {
        viewModelScope.launch {
            runCatching { api.getOrCreateConversation(CreateConversationRequestDto(user.id)) }
                .onSuccess { conversation ->
                    _uiState.update { current ->
                        current.copy(conversations = (listOf(conversation) + current.conversations).distinctBy { it.id })
                    }
                    onStarted(conversation)
                }
                .onFailure { error ->
                    _uiState.update { it.copy(userSearchError = error.message) }
                }
        }
    }
}
