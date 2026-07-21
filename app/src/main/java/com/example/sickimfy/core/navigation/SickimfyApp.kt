package com.example.sickimfy.core.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.sickimfy.features.chat.ui.ChatViewModel
import com.example.sickimfy.features.chat.ui.screens.ChatScreen
import com.example.sickimfy.features.chat.ui.screens.ConversationsScreen
import com.example.sickimfy.features.downloads.ui.DownloadsViewModel
import com.example.sickimfy.features.downloads.ui.screens.DownloadsScreen
import com.example.sickimfy.features.home.ui.HomeViewModel
import com.example.sickimfy.features.home.ui.TrackListMode
import com.example.sickimfy.features.home.ui.TrackListViewModel
import com.example.sickimfy.features.home.ui.screens.HomeScreen
import com.example.sickimfy.features.home.ui.screens.TrackListScreen
import com.example.sickimfy.features.player.ui.PlayerEvent
import com.example.sickimfy.features.player.ui.PlayerViewModel
import com.example.sickimfy.features.player.ui.screens.MiniPlayer
import com.example.sickimfy.features.player.ui.screens.NowPlayingScreen
import com.example.sickimfy.features.playlists.ui.PlaylistsViewModel
import com.example.sickimfy.features.playlists.ui.screens.PlaylistsScreen
import com.example.sickimfy.features.profile.ui.ProfileViewModel
import com.example.sickimfy.features.profile.ui.screens.ProfileScreen
import com.example.sickimfy.features.search.ui.SearchViewModel
import com.example.sickimfy.features.search.ui.screens.SearchScreen
import com.example.sickimfy.features.settings.ui.SettingsScreen
import com.example.sickimfy.features.settings.ui.SettingsViewModel
import com.example.sickimfy.features.social.ui.SocialScreen
import kotlinx.coroutines.launch

@Composable
fun SickimfyApp(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val selectedRoute = backStackEntry?.destination?.route

    val playerViewModel: PlayerViewModel = hiltViewModel()
    val playerUiState by playerViewModel.uiState.collectAsStateWithLifecycle()
    val coroutineScope = rememberCoroutineScope()

    var showFullPlayer by remember { mutableStateOf(false) }

    // Root Box to overlay the full player on top of the entire application safely
    Box(modifier = modifier.fillMaxSize()) {

        // Main Application Scaffold
        Scaffold(
            bottomBar = {
                // Column to place MiniPlayer directly above the NavigationBar without overlapping
                Column(modifier = Modifier.fillMaxWidth()) {

                    // Mini Player
                    AnimatedVisibility(
                        visible = playerUiState.currentPositionMs > 0 || playerUiState.isPlaying // Show conditionally if a track is loaded or playing
                    ) {
                        MiniPlayer(
                            uiState = playerUiState,
                            onPlayPause = { playerViewModel.onEvent(PlayerEvent.PlayPause) },
                            onExpand = { showFullPlayer = true }
                        )
                    }

                    // Bottom Navigation Menu
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
                                label = {
                                    Text(
                                        text = stringResource(destination.labelRes),
                                        maxLines = 1,
                                        softWrap = false,
                                        overflow = TextOverflow.Ellipsis,
                                        style = MaterialTheme.typography.labelSmall // Use smaller font and strict text limits to prevent layout breakage
                                    )
                                }
                            )
                        }
                    }
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
                    HomeScreen(
                        uiState = uiState,
                        onEvent = viewModel::onEvent,
                        onNavigateToLikedSongs = { navController.navigate("liked_songs") },
                        onNavigateToRecentlyPlayed = { navController.navigate("recently_played") },
                        onSettingsClick = { navController.navigate("settings") },
                        onProfileClick = {
                            navController.navigate(AppDestination.PROFILE.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
                composable(AppDestination.SEARCH.route) {
                    val viewModel: SearchViewModel = hiltViewModel()
                    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                    SearchScreen(uiState = uiState, onEvent = viewModel::onEvent)
                }
                composable(AppDestination.DOWNLOADS.route) {
                    val viewModel: DownloadsViewModel = hiltViewModel()
                    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                    DownloadsScreen(
                        uiState = uiState,
                        onEvent = viewModel::onEvent,
                        onSettingsClick = { navController.navigate("settings") },
                        onProfileClick = {
                            navController.navigate(AppDestination.PROFILE.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
                composable(AppDestination.PLAYLISTS.route) {
                    val viewModel: PlaylistsViewModel = hiltViewModel()
                    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                    PlaylistsScreen(
                        uiState = uiState,
                        onEvent = viewModel::onEvent,
                        onSettingsClick = { navController.navigate("settings") },
                        onProfileClick = {
                            navController.navigate(AppDestination.PROFILE.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
                composable(AppDestination.PROFILE.route) {
                    val viewModel: ProfileViewModel = hiltViewModel()
                    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                    ProfileScreen(
                        uiState = uiState,
                        onEvent = viewModel::onEvent,
                        onNavigateToChat = { navController.navigate("conversations") }
                    )
                }
                composable("liked_songs") {
                    val viewModel: TrackListViewModel = hiltViewModel()
                    LaunchedEffect(Unit) {
                        viewModel.setMode(TrackListMode.LikedSongs)
                    }
                    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                    TrackListScreen(
                        title = uiState.title,
                        tracks = uiState.tracks,
                        onNavigateBack = { navController.popBackStack() },
                        onTrackSelected = viewModel::playTrack,
                        onRemoveTrack = viewModel::removeTrack,
                        onPlayAll = viewModel::playAll
                    )
                }
                composable("recently_played") {
                    val viewModel: TrackListViewModel = hiltViewModel()
                    LaunchedEffect(Unit) {
                        viewModel.setMode(TrackListMode.RecentlyPlayed)
                    }
                    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                    TrackListScreen(
                        title = uiState.title,
                        tracks = uiState.tracks,
                        onNavigateBack = { navController.popBackStack() },
                        onTrackSelected = viewModel::playTrack,
                        onRemoveTrack = viewModel::removeTrack,
                        onPlayAll = viewModel::playAll
                    )
                }
                composable("settings") {
                    val viewModel: SettingsViewModel = hiltViewModel()
                    val settingsState by viewModel.uiState.collectAsStateWithLifecycle()
                    SettingsScreen(
                        state = settingsState,
                        onThemeChange = viewModel::setThemeMode,
                        onLanguageChange = viewModel::setLanguage,
                        onFontScaleChange = viewModel::setFontScale,
                        onApiBaseUrlChange = viewModel::setApiBaseUrl,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
                composable("conversations") {
                    ConversationsScreen(
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToChat = { convoId, otherId, otherName ->
                            navController.navigate("chat/$convoId/$otherId/$otherName")
                        },
                        onNavigateToSocial = { navController.navigate("social") }
                    )
                }
                composable("social") {
                    SocialScreen(
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToChat = { convoId ->
                            navController.navigate("chat/$convoId/0/Friend")
                        }
                    )
                }
                composable(
                    route = "chat/{conversationId}/{userId}/{userName}",
                    arguments = listOf(
                        navArgument("conversationId") { type = NavType.IntType },
                        navArgument("userId") { type = NavType.StringType },
                        navArgument("userName") { type = NavType.StringType }
                    )
                ) { backStackEntry ->
                    val conversationId = backStackEntry.arguments?.getInt("conversationId") ?: -1
                    val otherUserId = backStackEntry.arguments?.getString("userId").orEmpty()

                    val viewModel: ChatViewModel = hiltViewModel()
                    LaunchedEffect(conversationId, otherUserId) {
                        viewModel.initialize(conversationId, otherUserId)
                    }

                    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                    ChatScreen(
                        uiState = uiState,
                        onEvent = { event ->
                            coroutineScope.launch {
                                viewModel.onEvent(event)
                            }
                        },
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
            }
        }

        // Full Player Screen with slide up/down animation
        AnimatedVisibility(
            visible = showFullPlayer,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it })
        ) {
            NowPlayingScreen(
                uiState = playerUiState,
                onEvent = playerViewModel::onEvent,
                onCollapse = { showFullPlayer = false }
            )
        }
    }
}