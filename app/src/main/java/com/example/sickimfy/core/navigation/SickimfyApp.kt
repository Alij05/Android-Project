package com.example.sickimfy.core.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.sickimfy.features.downloads.ui.DownloadsViewModel
import com.example.sickimfy.features.downloads.ui.screens.DownloadsScreen
import com.example.sickimfy.features.home.ui.HomeViewModel
import com.example.sickimfy.features.home.ui.screens.HomeScreen
import com.example.sickimfy.features.playlists.ui.PlaylistsViewModel
import com.example.sickimfy.features.playlists.ui.screens.PlaylistsScreen
import com.example.sickimfy.features.player.ui.PlayerViewModel
import com.example.sickimfy.features.player.ui.screens.MiniPlayer
import com.example.sickimfy.features.player.ui.screens.NowPlayingScreen
import com.example.sickimfy.features.profile.ui.ProfileViewModel
import com.example.sickimfy.features.profile.ui.screens.ProfileScreen
import com.example.sickimfy.features.search.ui.SearchViewModel
import com.example.sickimfy.features.search.ui.screens.SearchScreen

@Composable
fun SickimfyApp(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val selectedRoute = backStackEntry?.destination?.route

    val playerViewModel: PlayerViewModel = hiltViewModel()
    val playerUiState by playerViewModel.uiState.collectAsStateWithLifecycle()

    var showFullPlayer by remember { mutableStateOf(false) }

    if (showFullPlayer) {
        NowPlayingScreen(
            uiState = playerUiState,
            onEvent = playerViewModel::onEvent,
            onCollapse = { showFullPlayer = false }
        )
    } else {
        Scaffold(
            modifier = modifier,
            bottomBar = {
                Box {
                    NavigationBar {
                        AppDestination.entries.forEach { destination ->
                            NavigationBarItem(
                                selected = selectedRoute == destination.route,
                                onClick = {
                                    navController.navigate(destination.route) {
                                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                icon = {
                                    Icon(
                                        imageVector = destination.icon,
                                        contentDescription = stringResource(destination.labelRes)
                                    )
                                },
                                label = { Text(stringResource(destination.labelRes)) }
                            )
                        }
                    }

                    // Mini Player floating above nav bar
                    MiniPlayer(
                        uiState = playerUiState,
                        onPlayPause = { playerViewModel.onEvent(com.example.sickimfy.features.player.ui.PlayerEvent.PlayPause) },
                        onExpand = { showFullPlayer = true },
                        modifier = Modifier.align(Alignment.TopCenter)
                    )
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = AppDestination.HOME.route,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(AppDestination.HOME.route) {
                    val viewModel: HomeViewModel = hiltViewModel()
                    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                    HomeScreen(uiState = uiState, onEvent = viewModel::onEvent)
                }
                composable(AppDestination.SEARCH.route) {
                    val viewModel: SearchViewModel = hiltViewModel()
                    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                    SearchScreen(uiState = uiState, onEvent = viewModel::onEvent)
                }
                composable(AppDestination.DOWNLOADS.route) {
                    val viewModel: DownloadsViewModel = hiltViewModel()
                    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                    DownloadsScreen(uiState = uiState, onEvent = viewModel::onEvent)
                }
                composable(AppDestination.PLAYLISTS.route) {
                    val viewModel: PlaylistsViewModel = hiltViewModel()
                    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                    PlaylistsScreen(uiState = uiState, onEvent = viewModel::onEvent)
                }
                composable(AppDestination.PROFILE.route) {
                    val viewModel: ProfileViewModel = hiltViewModel()
                    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                    ProfileScreen(uiState = uiState, onEvent = viewModel::onEvent)
                }
            }
        }
    }
}
