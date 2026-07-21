package com.example.sickimfy.features.home.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.sickimfy.R
import com.example.sickimfy.core.designsystem.Dimens
import com.example.sickimfy.core.designsystem.components.MusicTopBar
import com.example.sickimfy.features.home.ui.HomeEvent
import com.example.sickimfy.features.home.ui.HomeUiState
import com.example.sickimfy.features.home.ui.screens.components.HomeCarousel
import com.example.sickimfy.features.home.ui.screens.components.QuickActionsGrid
import com.example.sickimfy.features.home.ui.screens.components.TrackSlider

@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onEvent: (HomeEvent) -> Unit,
    onNavigateToLikedSongs: () -> Unit,
    onNavigateToRecentlyPlayed: () -> Unit,
    onSettingsClick: () -> Unit,
    onProfileClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            MusicTopBar(
                onNotificationClick = { /* Handle navigation to notifications */ },
                onSettingsClick = onSettingsClick,
                onProfileClick = onProfileClick
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (uiState) {
                is HomeUiState.Loading -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(Dimens.paddingLarge),
                        contentPadding = PaddingValues(
                            top = Dimens.paddingMedium,
                            bottom = Dimens.paddingLarge
                        )
                    ) {
                        item {
                            TrackSlider(
                                title = stringResource(id = R.string.section_popular),
                                tracks = emptyList(),
                                onTrackClick = {},
                                isLoading = true
                            )
                        }
                        item {
                            TrackSlider(
                                title = stringResource(id = R.string.section_new_releases),
                                tracks = emptyList(),
                                onTrackClick = {},
                                isLoading = true
                            )
                        }
                    }
                }
                is HomeUiState.Success -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(Dimens.paddingLarge),
                        contentPadding = PaddingValues(
                            top = Dimens.paddingMedium,
                            bottom = 100.dp
                        )
                    ) {
                        item {
                            HomeCarousel(
                                tracks = uiState.carouselTracks,
                                onTrackClick = { onEvent(HomeEvent.OnTrackSelected(it)) }
                            )
                        }
                        item {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(Dimens.paddingSmall)
                            ) {
                                Text(
                                    text = stringResource(id = R.string.section_quick_actions),
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onBackground,
                                    modifier = Modifier.padding(horizontal = Dimens.paddingMedium)
                                )
                                QuickActionsGrid(
                                    onActionClick = { actionId ->
                                        when (actionId) {
                                            "liked_songs" -> onNavigateToLikedSongs()
                                            "recently_played" -> onNavigateToRecentlyPlayed()
                                        }
                                    }
                                )
                            }
                        }
                        item {
                            TrackSlider(
                                title = stringResource(id = R.string.section_popular),
                                tracks = uiState.popularTracks,
                                onTrackClick = { onEvent(HomeEvent.OnTrackSelected(it)) }
                            )
                        }
                        item {
                            TrackSlider(
                                title = stringResource(id = R.string.section_new_releases),
                                tracks = uiState.newReleases,
                                onTrackClick = { onEvent(HomeEvent.OnTrackSelected(it)) }
                            )
                        }
                        item {
                            TrackSlider(
                                title = stringResource(id = R.string.section_global_playlists),
                                tracks = uiState.globalPlaylists,
                                onTrackClick = { onEvent(HomeEvent.OnTrackSelected(it)) }
                            )
                        }
                        item {
                            TrackSlider(
                                title = stringResource(id = R.string.section_local_playlists),
                                tracks = uiState.localPlaylists,
                                onTrackClick = { onEvent(HomeEvent.OnTrackSelected(it)) }
                            )
                        }
                    }
                }
                is HomeUiState.Error -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(Dimens.paddingLarge),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.error.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.ErrorOutline,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(Dimens.iconSizeLarge)
                            )
                        }

                        Spacer(modifier = Modifier.height(Dimens.paddingMedium))

                        Text(
                            text = uiState.message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(Dimens.paddingLarge))

                        Button(
                            onClick = { onEvent(HomeEvent.OnRetryClicked) },
                            shape = MaterialTheme.shapes.medium,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                modifier = Modifier.size(Dimens.iconSizeNormal)
                            )
                            Spacer(modifier = Modifier.width(Dimens.paddingSmall))
                            Text(text = stringResource(R.string.retry))
                        }
                    }
                }
            }
        }
    }
}