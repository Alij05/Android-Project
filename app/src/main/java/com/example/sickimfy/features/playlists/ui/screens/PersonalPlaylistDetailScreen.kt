package com.example.sickimfy.features.playlists.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.sickimfy.R
import com.example.sickimfy.core.designsystem.Dimens
import com.example.sickimfy.features.home.domain.model.Track
import com.example.sickimfy.features.playlists.ui.PersonalPlaylistDetailViewModel
import com.example.sickimfy.features.playlists.ui.screens.components.AddToPlaylistButton

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun PersonalPlaylistDetailScreen(
    onNavigateBack: () -> Unit,
    viewModel: PersonalPlaylistDetailViewModel = hiltViewModel()
) {
    val tracks by viewModel.tracks.collectAsStateWithLifecycle()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.playlist_tracks_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.cd_back))
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
            items(tracks, key = { it.id }) { track ->
                PlaylistTrackRow(track, onClick = { viewModel.play(track) })
            }
        }
    }
}

@Composable
private fun PlaylistTrackRow(track: Track, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(Dimens.paddingMedium),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.PlayArrow, null, tint = MaterialTheme.colorScheme.primary)
        Column(modifier = Modifier.weight(1f).padding(start = Dimens.paddingMedium)) {
            Text(track.title, style = MaterialTheme.typography.titleSmall)
            Text(track.artist, style = MaterialTheme.typography.bodySmall)
        }
        AddToPlaylistButton(track)
    }
}
