package com.example.sickimfy.features.home.ui.screens.components

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.sickimfy.R
import com.example.sickimfy.core.designsystem.Dimens
import com.example.sickimfy.core.designsystem.components.LocalAnimatedVisibilityScope
import com.example.sickimfy.core.designsystem.components.LocalSharedTransitionScope
import com.example.sickimfy.core.designsystem.components.shimmerEffect
import com.example.sickimfy.features.home.domain.model.Track
import com.example.sickimfy.features.playlists.ui.screens.components.AddToPlaylistButton

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun TrackSlider(
    title: String,
    tracks: List<Track>,
    onTrackClick: (Track) -> Unit,
    onDownload: ((Track) -> Unit)? = null,
    onShare: ((Track) -> Unit)? = null,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(horizontal = Dimens.paddingMedium, vertical = Dimens.paddingSmall)
        )

        LazyRow(
            contentPadding = PaddingValues(horizontal = Dimens.paddingMedium),
            horizontalArrangement = Arrangement.spacedBy(Dimens.paddingSmall)
        ) {
            if (isLoading) {
                // Render Shimmer placeholders during loading state
                items(5) {
                    ShimmerTrackItem()
                }
            } else {
                items(tracks, key = { it.id }) { track ->
                    TrackItemCard(track = track, onTrackClick = onTrackClick, onDownload = onDownload, onShare = onShare)
                }
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun TrackItemCard(
    track: Track,
    onTrackClick: (Track) -> Unit,
    onDownload: ((Track) -> Unit)?,
    onShare: ((Track) -> Unit)?
) {
    val sharedTransitionScope = LocalSharedTransitionScope.current
    val animatedVisibilityScope = LocalAnimatedVisibilityScope.current

    Column(
        modifier = Modifier
            .width(130.dp)
            .clickable { onTrackClick(track) }
    ) {
        val artworkModifier = if (sharedTransitionScope != null && animatedVisibilityScope != null) {
            with(sharedTransitionScope) {
                Modifier
                    .size(130.dp)
                    .clip(MaterialTheme.shapes.medium)
                    .sharedElement(
                        rememberSharedContentState(key = "artwork_${track.id}"),
                        animatedVisibilityScope = animatedVisibilityScope
                    )
            }
        } else {
            Modifier
                .size(130.dp)
                .clip(MaterialTheme.shapes.medium)
        }

        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(track.imageUrl)
                .crossfade(true)
                .build(),
            contentDescription = track.title,
            placeholder = painterResource(id = android.R.drawable.ic_menu_gallery),
            contentScale = ContentScale.Crop,
            modifier = artworkModifier
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = track.title,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = track.artist,
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = MaterialTheme.colorScheme.secondary
        )
        androidx.compose.foundation.layout.Row {
            AddToPlaylistButton(track = track)
            if (onDownload != null && !track.audioUrl.isNullOrBlank()) {
                IconButton(onClick = { onDownload(track) }) {
                    Icon(Icons.Default.Download, contentDescription = "Download")
                }
            }
            if (onShare != null) {
                IconButton(onClick = { onShare(track) }) {
                    Icon(Icons.Default.Share, contentDescription = "Share")
                }
            }
        }
    }
}

@Composable
private fun ShimmerTrackItem() {
    Column(modifier = Modifier.width(130.dp)) {
        Column(
            modifier = Modifier
                .size(130.dp)
                .clip(MaterialTheme.shapes.medium)
                .shimmerEffect()
        ) {}
        Spacer(modifier = Modifier.height(Dimens.paddingSmall))
        Column(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(16.dp)
                .clip(MaterialTheme.shapes.small)
                .shimmerEffect()
        ) {}
        Spacer(modifier = Modifier.height(4.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth(0.5f)
                .height(12.dp)
                .clip(MaterialTheme.shapes.small)
                .shimmerEffect()
        ) {}
    }
}
