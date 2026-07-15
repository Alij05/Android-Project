package com.example.sickimfy.features.search.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.sickimfy.R
import com.example.sickimfy.core.designsystem.Dimens
import com.example.sickimfy.features.search.ui.SearchEvent
import com.example.sickimfy.features.search.ui.SearchFilter
import com.example.sickimfy.features.search.ui.SearchUiState
import com.example.sickimfy.features.home.domain.model.Track
import coil.compose.AsyncImage
import coil.request.ImageRequest

@Composable
fun SearchScreen(
    uiState: SearchUiState,
    onEvent: (SearchEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            // Search Input Field
            SearchInputField(
                query = uiState.query,
                onQueryChange = { onEvent(SearchEvent.OnQueryChange(it)) },
                isSearching = uiState.isSearching
            )

            // Dynamic Filter Chips
            FilterChipsRow(
                activeFilter = uiState.activeFilter,
                onFilterSelected = { onEvent(SearchEvent.OnFilterSelected(it)) }
            )

            // Content Area (History vs Results)
            if (uiState.query.isBlank()) {
                SearchHistorySection(
                    history = uiState.searchHistory,
                    onHistoryItemClick = { onEvent(SearchEvent.OnQueryChange(it)) },
                    onDeleteClick = { onEvent(SearchEvent.OnDeleteHistoryItem(it)) },
                    onClearAllClick = { onEvent(SearchEvent.OnClearSearchHistory) }
                )
            } else {
                when {
                    uiState.isSearching -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                    uiState.errorMessage != null -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = stringResource(R.string.search_error),
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    uiState.searchResults.isEmpty() -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = stringResource(R.string.no_results_format, uiState.query),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    else -> SearchResults(
                        tracks = uiState.searchResults,
                        onTrackClick = { onEvent(SearchEvent.OnTrackSelected(it.id)) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchInputField(
    query: String,
    onQueryChange: (String) -> Unit,
    isSearching: Boolean
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(Dimens.paddingMedium),
        placeholder = { Text(text = stringResource(id = R.string.search_hint)) },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface
            )
        },
        trailingIcon = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AnimatedVisibility(visible = isSearching, enter = fadeIn(), exit = fadeOut()) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                if (query.isNotEmpty()) {
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = stringResource(id = R.string.cd_clear_search)
                        )
                    }
                }
            }
        },
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.surface,
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface
        ),
        singleLine = true
    )
}

@Composable
private fun FilterChipsRow(
    activeFilter: SearchFilter,
    onFilterSelected: (SearchFilter) -> Unit
) {
    val filters = listOf(
        SearchFilter.ALL to R.string.filter_all,
        SearchFilter.TRACKS to R.string.filter_tracks,
        SearchFilter.ARTISTS to R.string.filter_artists,
        SearchFilter.ALBUMS to R.string.filter_albums
    )

    LazyRow(
        contentPadding = PaddingValues(horizontal = Dimens.paddingMedium),
        horizontalArrangement = Arrangement.spacedBy(Dimens.paddingSmall)
    ) {
        items(filters) { (filter, stringRes) ->
            FilterChip(
                selected = activeFilter == filter,
                onClick = { onFilterSelected(filter) },
                label = { Text(text = stringResource(id = stringRes)) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                ),
                shape = RoundedCornerShape(16.dp)
            )
        }
    }
}

@Composable
private fun SearchHistorySection(
    history: List<String>,
    onHistoryItemClick: (String) -> Unit,
    onDeleteClick: (String) -> Unit,
    onClearAllClick: () -> Unit
) {
    if (history.isEmpty()) return

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimens.paddingMedium, vertical = Dimens.paddingSmall),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(id = R.string.recent_searches),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
            TextButton(onClick = onClearAllClick) {
                Text(
                    text = stringResource(id = R.string.clear_history),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        LazyColumn {
            items(history, key = { it }) { query ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onHistoryItemClick(query) }
                        .padding(horizontal = Dimens.paddingMedium, vertical = Dimens.paddingSmall),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.width(Dimens.paddingMedium))
                    Text(
                        text = query,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { onDeleteClick(query) }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(id = R.string.cd_delete_history_item),
                            tint = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchResults(
    tracks: List<Track>,
    onTrackClick: (Track) -> Unit
) {
    LazyColumn(contentPadding = PaddingValues(vertical = Dimens.paddingSmall)) {
        items(tracks, key = { it.id }) { track ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onTrackClick(track) }
                    .padding(horizontal = Dimens.paddingMedium, vertical = Dimens.paddingSmall),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current).data(track.imageUrl).crossfade(true).build(),
                    contentDescription = stringResource(R.string.cd_track_cover),
                    placeholder = painterResource(android.R.drawable.ic_menu_gallery),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(56.dp).clip(RoundedCornerShape(8.dp))
                )
                Spacer(modifier = Modifier.width(Dimens.paddingMedium))
                Column(modifier = Modifier.weight(1f)) {
                    Text(track.title, style = MaterialTheme.typography.bodyLarge, maxLines = 1)
                    Text(track.artist, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.secondary, maxLines = 1)
                }
                Text(track.duration, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}
