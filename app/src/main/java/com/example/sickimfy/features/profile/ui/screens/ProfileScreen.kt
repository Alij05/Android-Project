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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ColorLens
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
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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

// Define a distinct golden color for the Premium badge
val GoldenPremium = Color(0xFFFFD700)

@Composable
fun ProfileScreen(
    uiState: ProfileUiState,
    onEvent: (ProfileEvent) -> Unit,
    modifier: Modifier = Modifier
) {
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

            // Avatar Section
            AvatarSection(
                avatarUrl = uiState.avatarUrl,
                onAvatarClick = { onEvent(ProfileEvent.OnAvatarClick) }
            )

            Spacer(modifier = Modifier.height(Dimens.paddingMedium))

            // Name and Premium Badge Section
            UserInfoSection(
                userName = uiState.userName,
                isPremium = uiState.isPremium
            )

            Spacer(modifier = Modifier.height(Dimens.paddingLarge))

            // Premium Upgrade Action
            if (!uiState.isPremium) {
                PremiumUpgradeButton(
                    isUpgrading = uiState.isUpgrading,
                    onClick = { onEvent(ProfileEvent.OnUpgradePremiumClick) }
                )
            }

            Spacer(modifier = Modifier.height(Dimens.paddingLarge))
            Divider(color = MaterialTheme.colorScheme.surfaceVariant)
            Spacer(modifier = Modifier.height(Dimens.paddingLarge))

            // Settings Section
            SettingsSection(
                onThemeClick = { onEvent(ProfileEvent.OnThemeSettingsClick) },
                onLanguageClick = { onEvent(ProfileEvent.OnLanguageSettingsClick) }
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
        // Main Avatar Profile Picture
        if (avatarUrl != null) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current).data(avatarUrl).crossfade(true).build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )
        } else {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Edit Icon Badge
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
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(Dimens.paddingSmall))

        // Animated transition between Standard and Premium badges
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
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (premiumState) GoldenPremium.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant)
                    .padding(horizontal = Dimens.paddingMedium, vertical = Dimens.paddingSmall)
            ) {
                Icon(
                    imageVector = if (premiumState) Icons.Default.WorkspacePremium else Icons.Default.Person,
                    contentDescription = null,
                    tint = if (premiumState) GoldenPremium else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(Dimens.paddingSmall))
                Text(
                    text = stringResource(id = if (premiumState) R.string.premium_member else R.string.standard_member),
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = if (premiumState) GoldenPremium else MaterialTheme.colorScheme.onSurfaceVariant
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
            containerColor = GoldenPremium,
            contentColor = Color.Black // High contrast text on golden button
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        if (isUpgrading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = Color.Black,
                strokeWidth = 2.dp
            )
            Spacer(modifier = Modifier.width(Dimens.paddingMedium))
            Text(text = stringResource(id = R.string.upgrading_wait))
        } else {
            Icon(imageVector = Icons.Default.WorkspacePremium, contentDescription = null)
            Spacer(modifier = Modifier.width(Dimens.paddingSmall))
            Text(
                text = stringResource(id = R.string.upgrade_premium),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        }
    }
}

@Composable
private fun SettingsSection(
    onThemeClick: () -> Unit,
    onLanguageClick: () -> Unit
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
            shape = RoundedCornerShape(16.dp),
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
            }
        }
    }
}

@Composable
private fun SettingItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
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
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(Dimens.paddingMedium))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}