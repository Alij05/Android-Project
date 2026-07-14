package com.example.sickimfy.core.designsystem.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.sickimfy.R
import com.example.sickimfy.core.designsystem.Dimens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MusicTopBar(
    onNotificationClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onProfileClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        modifier = modifier,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            titleContentColor = MaterialTheme.colorScheme.onBackground,
            actionIconContentColor = MaterialTheme.colorScheme.onBackground
        ),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Application Branding Logo
                Image(
                    painter = painterResource(id = android.R.drawable.ic_media_play), // Placeholder, replace with custom adaptive icon later
                    contentDescription = null,
                    modifier = Modifier.size(Dimens.iconSizeLarge)
                )
                Spacer(modifier = Modifier.width(Dimens.paddingSmall))
                // Application Name Typography driven
                Text(
                    text = stringResource(id = R.string.app_name),
                    style = MaterialTheme.typography.titleLarge
                )
            }
        },
        actions = {
            // Notification Action Button
            IconButton(onClick = onNotificationClick) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = stringResource(id = R.string.cd_notification_bell),
                    modifier = Modifier.size(Dimens.iconSizeNormal)
                )
            }
            // Settings Action Button
            IconButton(onClick = onSettingsClick) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = stringResource(id = R.string.cd_settings_gear),
                    modifier = Modifier.size(Dimens.iconSizeNormal)
                )
            }
            // User Profile Image Component
            IconButton(
                onClick = onProfileClick,
                modifier = Modifier.padding(end = Dimens.paddingSmall)
            ) {
                Image(
                    painter = painterResource(id = android.R.drawable.sym_def_app_icon), // Placeholder for profile image
                    contentDescription = stringResource(id = R.string.cd_profile_picture),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(Dimens.profileImageSize)
                        .clip(CircleShape)
                        .border(1.dp, MaterialTheme.colorScheme.primary, CircleShape)
                )
            }
        }
    )
}