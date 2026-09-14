package com.sanket.tools.nexpaddesktop.ui.visualizers

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import kotlin.math.sin

@Composable
fun SmoothingWaveform(
    smoothingEnabled: Boolean,
    smoothingAmount: Float
) {
    // We create a simulated infinite animation
    val infiniteTransition = rememberInfiniteTransition()
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF1E1E1E))
    ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(8.dp)) {
            val w = size.width
            val h = size.height
            val centerY = h / 2

            // Simulate a base sine wave
            val points = 100
            val dx = w / points

            val rawPath = Path()
            val smoothPath = Path()

            var prevSmoothY = centerY

            for (i in 0..points) {
                val x = i * dx
                // Base smooth wave
                val baseWave = sin(phase + i * 0.1f) * (h * 0.3f)
                
                // Add "jitter" for raw wave
                // Deterministic jitter based on x position
                val jitter = if (i % 2 == 0) 10f else -10f
                val rawY = centerY + baseWave + jitter

                if (i == 0) {
                    rawPath.moveTo(x, rawY)
                    smoothPath.moveTo(x, rawY)
                    prevSmoothY = rawY
                } else {
                    rawPath.lineTo(x, rawY)

                    // Apply EMA smoothing mathematically similar to our GyroProcessor
                    val alpha = if (smoothingEnabled) smoothingAmount else 1.0f
                    val currentSmoothY = prevSmoothY + alpha * (rawY - prevSmoothY)
                    smoothPath.lineTo(x, currentSmoothY)
                    prevSmoothY = currentSmoothY
                }
            }

            // Draw Raw Jittery Path (Faint Red)
            drawPath(
                path = rawPath,
                color = Color.Red.copy(alpha = 0.3f),
                style = Stroke(width = 1.dp.toPx())
            )

            // Draw Smoothed Path (Bright Green)
            drawPath(
                path = smoothPath,
                color = Color.Green,
                style = Stroke(width = 3.dp.toPx())
            )
        }
    }
}
