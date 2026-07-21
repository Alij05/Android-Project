package com.example.sickimfy.features.profile.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.sickimfy.R
import com.example.sickimfy.core.designsystem.Dimens
import com.example.sickimfy.features.profile.ui.ProfileEvent
import com.example.sickimfy.features.profile.ui.ProfileUiState

@Composable
fun ProfileScreen(
    uiState: ProfileUiState,
    onEvent: (ProfileEvent) -> Unit,
    onNavigateToChat: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (uiState.isLoading) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
        return
    }
    if (uiState.errorMessage != null && uiState.userName.isBlank()) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = stringResource(R.string.profile_sign_in_required),
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(Dimens.paddingMedium)
            )
        }
        return
    }
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(Dimens.paddingMedium),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(Dimens.paddingLarge))

            AvatarSection(
                avatarUrl = uiState.avatarUrl,
                onAvatarClick = { onEvent(ProfileEvent.OnAvatarClick) }
            )

            Spacer(modifier = Modifier.height(Dimens.paddingMedium))

            UserInfoSection(
                userName = uiState.userName,
                isPremium = uiState.isPremium
            )

            Spacer(modifier = Modifier.height(Dimens.paddingLarge))

            if (!uiState.isPremium) {
                PremiumUpgradeButton(
                    isUpgrading = uiState.isUpgrading,
                    onClick = { onEvent(ProfileEvent.OnUpgradePremiumClick) }
                )
            }

            Spacer(modifier = Modifier.height(Dimens.paddingLarge))
            Divider(color = MaterialTheme.colorScheme.surface)
            Spacer(modifier = Modifier.height(Dimens.paddingLarge))

            SettingsSection(
                onThemeClick = { onEvent(ProfileEvent.OnThemeSettingsClick) },
                onLanguageClick = { onEvent(ProfileEvent.OnLanguageSettingsClick) },
                onChatClick = onNavigateToChat,
                onLogoutClick = { onEvent(ProfileEvent.OnLogoutClick) }
            )
        }
    }
}

@Composable
private fun AvatarSection(
    avatarUrl: String?,
    onAvatarClick: () -> Unit
) {
    Box(contentAlignment = Alignment.BottomEnd) {
        if (avatarUrl != null) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current).data(avatarUrl).crossfade(true).build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface)
            )
        } else {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.secondary
                )
            }
        }

        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
                .clickable { onAvatarClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = stringResource(id = R.string.change_avatar),
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun UserInfoSection(
    userName: String,
    isPremium: Boolean
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = userName,
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(Dimens.paddingSmall))

        AnimatedContent(
            targetState = isPremium,
            transitionSpec = {
                fadeIn(animationSpec = tween(500)) togetherWith fadeOut(animationSpec = tween(500))
            },
            label = "PremiumStatusAnimation"
        ) { premiumState ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(MaterialTheme.shapes.large)
                    .background(if (premiumState) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface)
                    .padding(horizontal = Dimens.paddingMedium, vertical = Dimens.paddingSmall)
            ) {
                Icon(
                    imageVector = if (premiumState) Icons.Default.WorkspacePremium else Icons.Default.Person,
                    contentDescription = null,
                    tint = if (premiumState) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(Dimens.paddingSmall))
                Text(
                    text = stringResource(id = if (premiumState) R.string.premium_member else R.string.standard_member),
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = if (premiumState) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}

@Composable
private fun PremiumUpgradeButton(
    isUpgrading: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = !isUpgrading,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = MaterialTheme.shapes.large
    ) {
        if (isUpgrading) {
            CircularProgressIndicator(
                modifier = Modifier.size(Dimens.iconSizeNormal),
                color = MaterialTheme.colorScheme.onPrimary,
                strokeWidth = 2.dp
            )
            Spacer(modifier = Modifier.width(Dimens.paddingMedium))
            Text(text = stringResource(id = R.string.upgrading_wait))
        } else {
            Icon(imageVector = Icons.Default.WorkspacePremium, contentDescription = null)
            Spacer(modifier = Modifier.width(Dimens.paddingSmall))
            Text(
                text = stringResource(id = R.string.upgrade_premium),
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
            )
        }
    }
}

@Composable
private fun SettingsSection(
    onThemeClick: () -> Unit,
    onLanguageClick: () -> Unit,
    onChatClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(id = R.string.settings_title),
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = Dimens.paddingMedium)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column {
                SettingItem(
                    icon = Icons.Default.ColorLens,
                    title = stringResource(id = R.string.theme_settings),
                    onClick = onThemeClick
                )
                Divider(color = MaterialTheme.colorScheme.background, modifier = Modifier.padding(horizontal = Dimens.paddingMedium))
                SettingItem(
                    icon = Icons.Default.Language,
                    title = stringResource(id = R.string.language_settings),
                    onClick = onLanguageClick
                )
                Divider(color = MaterialTheme.colorScheme.background, modifier = Modifier.padding(horizontal = Dimens.paddingMedium))
                SettingItem(
                    icon = Icons.Default.Chat,
                    title = stringResource(id = R.string.direct_messages),
                    onClick = onChatClick
                )
                Divider(color = MaterialTheme.colorScheme.background, modifier = Modifier.padding(horizontal = Dimens.paddingMedium))
                SettingItem(
                    icon = Icons.Default.ExitToApp,
                    title = stringResource(id = R.string.log_out),
                    onClick = onLogoutClick
                )
            }
        }
    }
}

@Composable
private fun SettingItem(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(Dimens.paddingMedium),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(Dimens.iconSizeNormal)
        )
        Spacer(modifier = Modifier.width(Dimens.paddingMedium))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}