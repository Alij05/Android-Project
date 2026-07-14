package com.example.sickimfy.features.home.ui.screens.components

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.sickimfy.R
import com.example.sickimfy.core.designsystem.Dimens
import com.example.sickimfy.core.designsystem.components.LocalAnimatedVisibilityScope
import com.example.sickimfy.core.designsystem.components.LocalSharedTransitionScope
import com.example.sickimfy.features.home.domain.model.Track
import kotlin.math.absoluteValue

@OptIn(ExperimentalFoundationApi::class, ExperimentalSharedTransitionApi::class)
@Composable
fun HomeCarousel(
    tracks: List<Track>,
    onTrackClick: (Track) -> Unit,
    modifier: Modifier = Modifier
) {
    val pagerState = rememberPagerState(pageCount = { tracks.size })

    // Retrieve Shared Element transition scopes
    val sharedTransitionScope = LocalSharedTransitionScope.current
    val animatedVisibilityScope = LocalAnimatedVisibilityScope.current

    HorizontalPager(
        state = pagerState,
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 32.dp),
        pageSpacing = Dimens.paddingSmall
    ) { page ->
        val track = tracks[page]

        // Calculate dynamic page offset to apply a fluid visual zoom effect
        val pageOffset = ((pagerState.currentPage - page) + pagerState.currentPageOffsetFraction).absoluteValue

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .graphicsLayer {
                    // Smoothly scale down non-focused items inside the carousel
                    val scale = lerp(
                        start = 0.85f,
                        stop = 1.0f,
                        fraction = 1f - pageOffset.coerceIn(0f, 1f)
                    )
                    scaleX = scale
                    scaleY = scale
                    alpha = lerp(
                        start = 0.5f,
                        stop = 1.0f,
                        fraction = 1f - pageOffset.coerceIn(0f, 1f)
                    )
                }
                .clickable { onTrackClick(track) },
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {

                // Track Artwork with conditional Shared Element Transition
                val artworkModifier = if (sharedTransitionScope != null && animatedVisibilityScope != null) {
                    with(sharedTransitionScope) {
                        Modifier
                            .fillMaxSize()
                            .sharedElement(
                                rememberSharedContentState(key = "artwork_${track.id}"),
                                animatedVisibilityScope = animatedVisibilityScope
                            )
                    }
                } else {
                    Modifier.fillMaxSize()
                }

                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(track.imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = stringResource(id = R.string.cd_track_cover),
                    placeholder = painterResource(id = android.R.drawable.ic_menu_gallery),
                    contentScale = ContentScale.Crop,
                    modifier = artworkModifier
                )

                // Bottom gradient scrim for text readability
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f)),
                                startY = 300f
                            )
                        )
                )

                // Track Information
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(Dimens.paddingMedium)
                ) {
                    Text(
                        text = track.title,
                        style = MaterialTheme.typography.titleLarge.copy(color = Color.White, fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = track.artist,
                        style = MaterialTheme.typography.bodyMedium.copy(color = Color.LightGray)
                    )
                }
            }
        }
    }
}