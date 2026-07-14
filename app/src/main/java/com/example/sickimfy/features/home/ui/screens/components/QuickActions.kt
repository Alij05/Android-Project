package com.example.sickimfy.features.home.ui.screens.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.sickimfy.R
import com.example.sickimfy.core.designsystem.Dimens

@Composable
fun QuickActionsGrid(
    onActionClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        QuickActionItem(stringResource(id = R.string.qa_liked_songs), Icons.Default.Favorite, "liked_songs"),
        QuickActionItem(stringResource(id = R.string.qa_recently_played), Icons.Default.PlayArrow, "recently_played"),
        QuickActionItem(stringResource(id = R.string.qa_my_playlists), Icons.Default.List, "my_playlists"),
        QuickActionItem(stringResource(id = R.string.qa_top_artists), Icons.Default.Person, "top_artists")
    )

    Column(
        modifier = modifier.padding(horizontal = Dimens.paddingMedium),
        verticalArrangement = Arrangement.spacedBy(Dimens.paddingSmall)
    ) {
        // Build 2x2 grid elegantly using Compose Rows
        for (i in items.indices step 2) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Dimens.paddingSmall)
            ) {
                QuickActionButton(
                    item = items[i],
                    onClick = { onActionClick(items[i].actionId) },
                    modifier = Modifier.weight(1f)
                )
                if (i + 1 < items.size) {
                    QuickActionButton(
                        item = items[i + 1],
                        onClick = { onActionClick(items[i + 1].actionId) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickActionButton(
    item: QuickActionItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Smooth press-scaling feedback (Springy scale-down on click)
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.93f else 1.0f,
        animationSpec = tween(durationMillis = 100),
        label = "press_scale_anim"
    )

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(Dimens.paddingSmall))
            .background(MaterialTheme.colorScheme.surface)
            .clickable(
                interactionSource = interactionSource,
                indication = null, // Disable default ripple to enforce the unique scaling design language
                onClick = onClick
            )
            .padding(Dimens.paddingMedium),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(Dimens.iconSizeNormal)
            )
            Spacer(modifier = Modifier.size(Dimens.paddingSmall))
            Text(
                text = item.label,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

private data class QuickActionItem(
    val label: String,
    val icon: ImageVector,
    val actionId: String
)