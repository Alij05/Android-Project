package com.example.sickimfy.core.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.sickimfy.R

enum class AppDestination(
    val route: String,
    @StringRes val labelRes: Int,
    val icon: ImageVector
) {
    HOME("home", R.string.nav_home, Icons.Default.Home),
    SEARCH("search", R.string.nav_search, Icons.Default.Search),
    DOWNLOADS("downloads", R.string.nav_downloads, Icons.Default.Download),
    PLAYLISTS("playlists", R.string.nav_playlists, Icons.Default.LibraryMusic),
    PROFILE("profile", R.string.nav_profile, Icons.Default.Person)
}
