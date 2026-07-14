package com.example.sickimfy.core.designsystem.components

import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.IntSize

/**
 * A reusable Modifier that applies a fluid shimmer effect to any composable.
 * Optimized for both Light and Dark themes by utilizing the surface colors.
 */
fun Modifier.shimmerEffect(): Modifier = composed {
    // Stores the size of the composable to correctly scale the gradient
    var size by remember { mutableStateOf(IntSize.Zero) }

    // Creates an infinite transition loop for the fluid animation
    val transition = rememberInfiniteTransition(label = "shimmer_transition")

    // Animates the X-axis offset from 0 to twice the width to complete the sweep
    val startOffsetX by transition.animateFloat(
        initialValue = -2 * size.width.toFloat(),
        targetValue = 2 * size.width.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1300)
        ),
        label = "shimmer_offset"
    )

    this
        .onGloballyPositioned { size = it.size }
        .background(
            brush = Brush.linearGradient(
                colors = listOf(
                    Color.LightGray.copy(alpha = 0.3f),
                    Color.LightGray.copy(alpha = 0.7f),
                    Color.LightGray.copy(alpha = 0.3f),
                ),
                start = Offset(startOffsetX, 0f),
                end = Offset(startOffsetX + size.width.toFloat(), size.height.toFloat())
            )
        )
}
