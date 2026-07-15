package com.example.sickimfy.features.home.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.sickimfy.R
import com.example.sickimfy.core.designsystem.Dimens
import com.example.sickimfy.core.designsystem.components.MusicTopBar
import com.example.sickimfy.features.home.domain.model.Track
import com.example.sickimfy.features.home.ui.HomeEvent
import com.example.sickimfy.features.home.ui.HomeUiState
import com.example.sickimfy.features.home.ui.screens.components.HomeCarousel
import com.example.sickimfy.features.home.ui.screens.components.QuickActionsGrid
import com.example.sickimfy.features.home.ui.screens.components.TrackSlider

@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onEvent: (HomeEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            MusicTopBar(
                onNotificationClick = { /* Handle navigation to notifications */ },
                onSettingsClick = { /* Handle navigation to settings */ },
                onProfileClick = { /* Handle navigation to profile */ }
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
                    // Render dynamic full shimmer loading placeholders
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(Dimens.paddingMedium)
                    ) {
                        item {
                            Spacer(modifier = Modifier.height(Dimens.paddingSmall))
                        }
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
                        verticalArrangement = Arrangement.spacedBy(Dimens.paddingMedium),
                        contentPadding = PaddingValues(bottom = 100.dp) // Provide space so miniplayer doesn't overlap items
                    ) {
                        item {
                            Spacer(modifier = Modifier.height(Dimens.paddingSmall))
                        }
                        // Featured Horizontal Carousel
                        item {
                            HomeCarousel(
                                tracks = uiState.carouselTracks,
                                onTrackClick = { onEvent(HomeEvent.OnTrackSelected(it)) }
                            )
                        }
                        // 2x2 Quick Actions Buttons
                        item {
                            QuickActionsGrid(
                                onActionClick = { /* Handle specific quick action navigation */ }
                            )
                        }
                        // Bottom Sliders Driven by Domain lists
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
                    // Centralized error state UI with retry capability
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = uiState.message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(Dimens.paddingMedium))
                        Button(onClick = { onEvent(HomeEvent.OnRetryClicked) }) {
                            Text(text = stringResource(R.string.retry))
                        }
                    }
                }
            }
        }
    }
}
