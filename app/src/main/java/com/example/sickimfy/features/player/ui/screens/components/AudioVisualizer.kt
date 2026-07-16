package com.example.sickimfy.features.player.ui.screens.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import kotlin.math.sin

@Composable
fun AudioVisualizer(
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    barColor: Color = Color.White.copy(alpha = 0.7f),
    waveColor: Color = Color.White.copy(alpha = 0.4f)
) {
    val infiniteTransition = rememberInfiniteTransition(label = "visualizer")

    val animationProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 2000,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "progress"
    )

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(60.dp)
    ) {
        val width = size.width
        val height = size.height
        val centerY = height / 2

        if (!isPlaying) {
            // Static line when paused
            drawLine(
                color = waveColor,
                start = Offset(0f, centerY),
                end = Offset(width, centerY),
                strokeWidth = 2.dp.toPx()
            )
            return@Canvas
        }

        // Draw waveform
        val path = Path()
        val amplitude = height * 0.35f
        val frequency = 0.02f
        val phaseShift = animationProgress * width * 0.5f

        path.moveTo(0f, centerY)

        for (x in 0..width.toInt() step 2) {
            val xFloat = x.toFloat()
            val y = centerY + amplitude * sin(
                (xFloat * frequency) + (phaseShift * 0.01f)
            ).toFloat() * (0.5f + 0.5f * sin(xFloat * 0.005f + animationProgress * 10f).toFloat())

            path.lineTo(xFloat, y)
        }

        drawPath(
            path = path,
            color = waveColor,
            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
        )

        // Draw secondary wave (slightly offset)
        val path2 = Path()
        path2.moveTo(0f, centerY)

        for (x in 0..width.toInt() step 2) {
            val xFloat = x.toFloat()
            val y = centerY + amplitude * 0.6f * sin(
                (xFloat * frequency * 1.3f) + (phaseShift * 0.008f) + 1.5f
            ).toFloat() * (0.3f + 0.7f * sin(xFloat * 0.003f + animationProgress * 8f).toFloat())

            path2.lineTo(xFloat, y)
        }

        drawPath(
            path = path2,
            color = barColor,
            style = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round)
        )

        // Draw vertical bars
        val barCount = 40
        val barWidth = width / (barCount * 3)
        val spacing = width / barCount

        for (i in 0 until barCount) {
            val x = i * spacing + spacing / 2
            val barHeight = amplitude * (0.2f + 0.8f * sin(
                x * 0.01f + animationProgress * 15f + i * 0.5f
            ).toFloat().coerceIn(0f, 1f))

            drawLine(
                color = barColor.copy(alpha = 0.3f),
                start = Offset(x, centerY - barHeight / 2),
                end = Offset(x, centerY + barHeight / 2),
                strokeWidth = barWidth,
                cap = StrokeCap.Round
            )
        }
    }
}
