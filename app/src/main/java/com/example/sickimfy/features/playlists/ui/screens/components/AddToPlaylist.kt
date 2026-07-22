package com.example.sickimfy.features.playlists.ui.screens.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.sickimfy.R
import com.example.sickimfy.features.home.domain.model.Track
import com.example.sickimfy.features.playlists.ui.PlaylistPickerViewModel

@Composable
fun AddToPlaylistButton(
    track: Track,
    viewModel: PlaylistPickerViewModel = hiltViewModel()
) {
    val selectedTrack by viewModel.selectedTrack.collectAsStateWithLifecycle()
    val playlists by viewModel.playlists.collectAsStateWithLifecycle()

    IconButton(onClick = { viewModel.showFor(track) }) {
        Icon(Icons.Default.PlaylistAdd, stringResource(R.string.cd_add_to_playlist))
    }

    if (selectedTrack != null) {
        AlertDialog(
            onDismissRequest = viewModel::dismiss,
            title = { Text(stringResource(R.string.add_to_playlist)) },
            text = {
                if (playlists.isEmpty()) {
                    Text(stringResource(R.string.no_personal_playlists))
                } else {
                    androidx.compose.foundation.layout.Column {
                        playlists.forEach { playlist ->
                            TextButton(onClick = { viewModel.addTo(playlist.id) }) {
                                Text(playlist.title)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = viewModel::dismiss) { Text(stringResource(R.string.close)) }
            }
        )
    }
}
