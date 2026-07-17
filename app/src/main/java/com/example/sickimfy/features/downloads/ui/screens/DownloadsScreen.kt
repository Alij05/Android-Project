package com.example.sickimfy.features.downloads.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.sickimfy.R
import com.example.sickimfy.core.designsystem.Dimens
import com.example.sickimfy.core.designsystem.components.MusicTopBar
import com.example.sickimfy.features.downloads.ui.DownloadsEvent
import com.example.sickimfy.features.downloads.ui.DownloadsUiState
import com.example.sickimfy.features.downloads.ui.SortOption
import com.example.sickimfy.features.home.domain.model.Track
import kotlinx.coroutines.delay

@Composable
fun DownloadsScreen(
    uiState: DownloadsUiState,
    onEvent: (DownloadsEvent) -> Unit,
    onSettingsClick: () -> Unit,
    onProfileClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            MusicTopBar(
                onNotificationClick = { },
                onSettingsClick = onSettingsClick,
                onProfileClick = onProfileClick
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            // Header & Sorting Controls
            DownloadsHeader(
                activeSort = uiState.sortOption,
                onSortChanged = { onEvent(DownloadsEvent.OnSortOptionChanged(it)) }
            )

            // Tracks List
            if (uiState.downloadedTracks.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = stringResource(id = R.string.empty_downloads),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(bottom = 100.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(
                        items = uiState.downloadedTracks,
                        key = { it.id }
                    ) { track ->
                        DismissibleTrackItem(
                            track = track,
                            onDelete = { onEvent(DownloadsEvent.OnDeleteTrack(it)) },
                            onClick = { onEvent(DownloadsEvent.OnTrackSelected(it)) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DownloadsHeader(
    activeSort: SortOption,
    onSortChanged: (SortOption) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.paddingMedium, vertical = Dimens.paddingSmall),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(id = R.string.downloads_title),
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground
        )

        Box {
            TextButton(onClick = { expanded = true }) {
                Text(
                    text = "${stringResource(id = R.string.sort_by)} ${getSortLabel(activeSort)}",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelSmall
                )
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(MaterialTheme.colorScheme.surface)
            ) {
                SortOption.entries.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(getSortLabel(option), color = MaterialTheme.colorScheme.onSurface) },
                        onClick = {
                            onSortChanged(option)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun getSortLabel(option: SortOption): String {
    return when (option) {
        SortOption.DATE_ADDED -> stringResource(id = R.string.sort_date)
        SortOption.TITLE -> stringResource(id = R.string.sort_title)
        SortOption.ARTIST -> stringResource(id = R.string.sort_artist)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DismissibleTrackItem(
    track: Track,
    onDelete: (Track) -> Unit,
    onClick: (Track) -> Unit
) {
    // State to manage the UI component's awareness of being swiped
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { dismissValue ->
            if (dismissValue == SwipeToDismissBoxValue.EndToStart || dismissValue == SwipeToDismissBoxValue.StartToEnd) {
                true // Allow dismiss
            } else {
                false
            }
        }
    )

    // Trigger logical deletion only when the animation is fully confirmed
    LaunchedEffect(dismissState.currentValue) {
        if (dismissState.currentValue != SwipeToDismissBoxValue.Settled) {
            delay(300) // Allow animation to finish gracefully
            onDelete(track)
        }
    }

    AnimatedVisibility(
        visible = dismissState.currentValue == SwipeToDismissBoxValue.Settled,
        exit = shrinkVertically(animationSpec = tween(durationMillis = 300)) + fadeOut()
    ) {
        SwipeToDismissBox(
            state = dismissState,
            enableDismissFromStartToEnd = true, // LTR swipe
            enableDismissFromEndToStart = true, // RTL swipe
            backgroundContent = {
                val color = MaterialTheme.colorScheme.error
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = Dimens.paddingMedium, vertical = Dimens.paddingSmall)
                        .clip(RoundedCornerShape(8.dp))
                        .background(color)
                        .padding(horizontal = Dimens.paddingMedium),
                    contentAlignment = if (dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart) Alignment.CenterEnd else Alignment.CenterStart
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = stringResource(id = R.string.cd_delete_track),
                        tint = MaterialTheme.colorScheme.onError
                    )
                }
            },
            content = {
                // The actual track card
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Dimens.paddingMedium, vertical = Dimens.paddingSmall)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.background)
                        .clickable { onClick(track) },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current).data(track.imageUrl).crossfade(true).build(),
                        contentDescription = null,
                        placeholder = painterResource(id = android.R.drawable.ic_menu_gallery),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                    Spacer(modifier = Modifier.width(Dimens.paddingMedium))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = track.title,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onBackground,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = track.artist,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.secondary,
                            maxLines = 1
                        )
                    }
                }
            }
        )
    }
}