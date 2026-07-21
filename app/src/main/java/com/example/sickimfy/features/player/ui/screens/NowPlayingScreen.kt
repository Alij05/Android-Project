package com.example.sickimfy.features.player.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.palette.graphics.Palette
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.sickimfy.R
import com.example.sickimfy.core.designsystem.Black
import com.example.sickimfy.core.designsystem.Dimens
import com.example.sickimfy.core.designsystem.White
import com.example.sickimfy.features.player.ui.PlayerEvent
import com.example.sickimfy.features.player.ui.PlayerUiState
import com.example.sickimfy.features.player.ui.screens.components.AudioVisualizer

@Composable
fun NowPlayingScreen(
    uiState: PlayerUiState,
    onEvent: (PlayerEvent) -> Unit,
    onCollapse: () -> Unit,
    modifier: Modifier = Modifier
) {
    var dominantColor by remember { mutableStateOf(Color(0xFF1A1A2E)) }
    var secondaryColor by remember { mutableStateOf(Color(0xFF16213E)) }
    var isSeeking by remember { mutableStateOf(false) }
    var seekPosition by remember { mutableFloatStateOf(0f) }

    val context = LocalContext.current
    LaunchedEffect(uiState.coverUrl) {
        if (uiState.coverUrl.isNotBlank()) {
            try {
                val loader = coil.ImageLoader(context)
                val request = ImageRequest.Builder(context)
                    .data(uiState.coverUrl)
                    .allowHardware(false)
                    .build()
                val result = loader.execute(request)
                if (result is coil.request.SuccessResult) {
                    val drawable = result.drawable
                    val bitmap = (drawable as? android.graphics.drawable.BitmapDrawable)?.bitmap
                    if (bitmap != null) {
                        val palette = Palette.from(bitmap).generate()
                        val dominantInt = palette.getDarkMutedColor(0xFF1A1A2E.toInt())
                        val secondaryInt = palette.getDarkVibrantColor(0xFF16213E.toInt())

                        dominantColor = Color(dominantInt)
                        secondaryColor = Color(secondaryInt)
                    }
                }
            } catch (_: Exception) {
                // Keep default colors
            }
        }
    }

    val coverRotation by animateFloatAsState(
        targetValue = if (uiState.isPlaying) 360f else 0f,
        animationSpec = tween(
            durationMillis = 10000,
            easing = { if (uiState.isPlaying) it else 0f }
        ),
        label = "coverRotation"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(dominantColor, secondaryColor, dominantColor.copy(alpha = 0.8f))
                )
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {}
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(Dimens.paddingLarge),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(id = R.string.now_playing),
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = White
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.QueueMusic,
                    contentDescription = stringResource(id = R.string.cd_queue),
                    tint = White,
                    modifier = Modifier.size(Dimens.iconSizeNormal)
                )
            }

            Spacer(modifier = Modifier.height(Dimens.paddingLarge))

            Box(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .aspectRatio(1f)
                    .rotate(if (uiState.isPlaying) coverRotation else 0f)
                    .clip(CircleShape)
                    .background(Black.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(uiState.coverUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = stringResource(id = R.string.cd_track_cover),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize(0.85f)
                        .clip(CircleShape)
                )

                Box(
                    modifier = Modifier
                        .size(Dimens.iconSizeNormal)
                        .clip(CircleShape)
                        .background(Black)
                )
            }

            Spacer(modifier = Modifier.height(Dimens.paddingLarge))

            Text(
                text = uiState.title,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = uiState.artist,
                style = MaterialTheme.typography.bodyMedium,
                color = White.copy(alpha = 0.7f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(Dimens.paddingMedium))

            AudioVisualizer(
                isPlaying = uiState.isPlaying,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
            )

            Spacer(modifier = Modifier.height(Dimens.paddingMedium))

            Column(modifier = Modifier.fillMaxWidth()) {
                Slider(
                    value = if (isSeeking) seekPosition else {
                        if (uiState.durationMs > 0) uiState.currentPositionMs.toFloat() / uiState.durationMs.toFloat() else 0f
                    },
                    onValueChange = { value ->
                        isSeeking = true
                        seekPosition = value
                    },
                    onValueChangeFinished = {
                        isSeeking = false
                        val seekMs = (seekPosition * uiState.durationMs).toLong()
                        onEvent(PlayerEvent.SeekTo(seekMs))
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.primary,
                        activeTrackColor = MaterialTheme.colorScheme.primary,
                        inactiveTrackColor = White.copy(alpha = 0.3f)
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = formatDuration(uiState.currentPositionMs),
                        style = MaterialTheme.typography.labelSmall,
                        color = White.copy(alpha = 0.7f)
                    )
                    Text(
                        text = formatDuration(uiState.durationMs),
                        style = MaterialTheme.typography.labelSmall,
                        color = White.copy(alpha = 0.7f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(Dimens.paddingMedium))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { onEvent(PlayerEvent.ToggleShuffle) }) {
                    Icon(
                        imageVector = Icons.Default.Shuffle,
                        contentDescription = stringResource(id = R.string.cd_shuffle),
                        tint = if (uiState.shuffleEnabled) MaterialTheme.colorScheme.primary else White.copy(alpha = 0.5f),
                        modifier = Modifier.size(Dimens.iconSizeNormal)
                    )
                }

                IconButton(onClick = { onEvent(PlayerEvent.SkipPrevious) }) {
                    Icon(
                        imageVector = Icons.Default.SkipPrevious,
                        contentDescription = stringResource(id = R.string.cd_previous),
                        tint = White,
                        modifier = Modifier.size(36.dp)
                    )
                }

                IconButton(
                    onClick = { onEvent(PlayerEvent.PlayPause) },
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                ) {
                    Icon(
                        imageVector = if (uiState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (uiState.isPlaying) {
                            stringResource(id = R.string.cd_pause)
                        } else {
                            stringResource(id = R.string.cd_play)
                        },
                        tint = White,
                        modifier = Modifier.size(40.dp)
                    )
                }

                IconButton(onClick = { onEvent(PlayerEvent.SkipNext) }) {
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = stringResource(id = R.string.cd_next),
                        tint = White,
                        modifier = Modifier.size(36.dp)
                    )
                }

                IconButton(onClick = { onEvent(PlayerEvent.ToggleRepeat) }) {
                    Icon(
                        imageVector = if (uiState.repeatMode == 2) Icons.Default.RepeatOne else Icons.Default.Repeat,
                        contentDescription = stringResource(id = R.string.cd_repeat),
                        tint = if (uiState.repeatMode != 0) MaterialTheme.colorScheme.primary else White.copy(alpha = 0.5f),
                        modifier = Modifier.size(Dimens.iconSizeNormal)
                    )
                }
            }

            Spacer(modifier = Modifier.height(Dimens.paddingMedium))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = {
                        val newSpeed = when (uiState.playbackSpeed) {
                            1.0f -> 1.5f
                            1.5f -> 2.0f
                            else -> 1.0f
                        }
                        onEvent(PlayerEvent.SetSpeed(newSpeed))
                    }
                ) {
                    Text(
                        text = stringResource(id = R.string.speed_format, uiState.playbackSpeed),
                        color = White.copy(alpha = 0.8f),
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }

                TextButton(
                    onClick = {
                        if (uiState.sleepTimerRunning) {
                            onEvent(PlayerEvent.CancelSleepTimer)
                        } else {
                            onEvent(PlayerEvent.SetSleepTimer(15))
                        }
                    }
                ) {
                    Text(
                        text = if (uiState.sleepTimerRunning) {
                            stringResource(id = R.string.timer_active_short_format, uiState.sleepTimerMinutes ?: 0)
                        } else {
                            stringResource(id = R.string.cd_sleep_timer)
                        },
                        color = if (uiState.sleepTimerRunning) MaterialTheme.colorScheme.primary else White.copy(alpha = 0.8f),
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }

            Spacer(modifier = Modifier.height(Dimens.paddingMedium))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { onEvent(PlayerEvent.ToggleFavorite) },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = if (uiState.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = stringResource(id = R.string.cd_favorite),
                        tint = if (uiState.isFavorite) MaterialTheme.colorScheme.primary else White.copy(alpha = 0.7f),
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.width(Dimens.paddingLarge))

                IconButton(
                    onClick = { onEvent(PlayerEvent.DownloadTrack) },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = stringResource(id = R.string.cd_download),
                        tint = White.copy(alpha = 0.7f),
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }
}

private fun formatDuration(ms: Long): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}