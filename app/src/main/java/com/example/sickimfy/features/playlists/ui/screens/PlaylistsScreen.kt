package com.example.sickimfy.features.playlists.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.sickimfy.R
import com.example.sickimfy.core.designsystem.Dimens
import com.example.sickimfy.core.designsystem.White
import com.example.sickimfy.core.designsystem.components.MusicTopBar
import com.example.sickimfy.features.playlists.domain.model.Playlist
import com.example.sickimfy.features.playlists.domain.model.PlaylistType
import com.example.sickimfy.features.playlists.ui.PlaylistsEvent
import com.example.sickimfy.features.playlists.ui.PlaylistsUiState
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height

@Composable
fun PlaylistsScreen(
    uiState: PlaylistsUiState,
    onEvent: (PlaylistsEvent) -> Unit,
    onSettingsClick: () -> Unit,
    onProfileClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            MusicTopBar(
                onNotificationClick = { },
                onSettingsClick = onSettingsClick,
                onProfileClick = onProfileClick
            )
        }
    ) { innerPadding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else if (uiState.errorMessage != null) {
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.playlists_error),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.height(Dimens.paddingMedium))
                Button(
                    onClick = { onEvent(PlaylistsEvent.OnRetryClick) },
                    shape = MaterialTheme.shapes.medium,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text(stringResource(R.string.retry))
                }
            }
        } else {
            val groupedPlaylists = uiState.playlists.groupBy { it.type }

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(
                    start = Dimens.paddingMedium,
                    end = Dimens.paddingMedium,
                    top = Dimens.paddingSmall,
                    bottom = 100.dp
                ),
                horizontalArrangement = Arrangement.spacedBy(Dimens.paddingMedium),
                verticalArrangement = Arrangement.spacedBy(Dimens.paddingMedium),
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
            ) {
                groupedPlaylists[PlaylistType.INTERNATIONAL]?.let { list ->
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        SectionHeader(title = stringResource(id = R.string.section_intl_music))
                    }
                    items(list, key = { it.id }) { playlist ->
                        PlaylistGridCard(playlist = playlist, onClick = { onEvent(PlaylistsEvent.OnPlaylistSelected(playlist)) })
                    }
                }

                groupedPlaylists[PlaylistType.DOMESTIC]?.let { list ->
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        SectionHeader(title = stringResource(id = R.string.section_local_music))
                    }
                    items(list, key = { it.id }) { playlist ->
                        PlaylistGridCard(playlist = playlist, onClick = { onEvent(PlaylistsEvent.OnPlaylistSelected(playlist)) })
                    }
                }

                groupedPlaylists[PlaylistType.USER]?.let { list ->
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        SectionHeader(title = stringResource(id = R.string.section_user_playlists))
                    }
                    items(list, key = { it.id }) { playlist ->
                        PlaylistGridCard(playlist = playlist, onClick = { onEvent(PlaylistsEvent.OnPlaylistSelected(playlist)) })
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.onBackground,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = Dimens.paddingSmall)
    )
}

@Composable
private fun PlaylistGridCard(
    playlist: Playlist,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1.5f)
            .clip(MaterialTheme.shapes.large)
            .background(Brush.linearGradient(playlist.gradientColors))
            .clickable { onClick() }
            .padding(Dimens.paddingMedium),
        contentAlignment = Alignment.BottomStart
    ) {
        Column {
            Text(
                text = playlist.title,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = stringResource(id = R.string.tracks_count_format, playlist.trackCount),
                style = MaterialTheme.typography.labelSmall,
                color = White.copy(alpha = 0.8f)
            )
        }
    }
}