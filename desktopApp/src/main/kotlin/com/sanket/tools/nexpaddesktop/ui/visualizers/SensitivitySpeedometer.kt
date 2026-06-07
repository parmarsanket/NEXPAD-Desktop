package com.sanket.tools.nexpaddesktop.ui.visualizers

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun SensitivitySpeedometer(sensitivity: Float) {
    // Map sensitivity (0.1 to 5.0) to an angle (180 to 0)
    // 0.1 -> 180 degrees (left)
    // 5.0 -> 0 degrees (right)
    val percentage = (sensitivity / 5.0f).coerceIn(0f, 1f)
    val angleDegrees = 180f - (percentage * 180f)
    val angleRad = Math.toRadians(angleDegrees.toDouble())

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(16.dp)
    ) {
        Canvas(modifier = Modifier.size(120.dp, 60.dp)) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val strokeWidth = 12.dp.toPx()
            val radius = canvasWidth / 2 - strokeWidth

            // Draw background arc (gray)
            drawArc(
                color = Color.LightGray.copy(alpha = 0.3f),
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = false,
                topLeft = Offset(strokeWidth, strokeWidth),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Draw colored arc based on value
            val color = when {
                percentage < 0.3f -> Color(0xFF03A9F4) // Low = Precision
                percentage < 0.7f -> Color(0xFF4CAF50) // Med = Balanced
                else -> Color(0xFFFF5722) // High = Fast
            }

            drawArc(
                color = color,
                startAngle = 180f,
                sweepAngle = percentage * 180f,
                useCenter = false,
                topLeft = Offset(strokeWidth, strokeWidth),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Draw Needle
            val centerX = canvasWidth / 2
            val centerY = canvasHeight
            val needleLength = radius * 0.8f
            
            val endX = centerX + (needleLength * cos(angleRad)).toFloat()
            val endY = centerY - (needleLength * sin(angleRad)).toFloat()

            drawLine(
                color = Color.White,
                start = Offset(centerX, centerY),
                end = Offset(endX, endY),
                strokeWidth = 4.dp.toPx(),
                cap = StrokeCap.Round
            )
            
            // Center pin
            drawCircle(
                color = Color.White,
                radius = 6.dp.toPx(),
                center = Offset(centerX, centerY)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "${String.format("%.1fx", sensitivity)}",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        )
        val label = when {
            sensitivity < 1.5f -> "🐌 Precision"
            sensitivity < 3.5f -> "⚖️ Balanced"
            else -> "🚀 Fast Flick"
        }
        Text(text = label, fontSize = 12.sp, color = Color.Gray)
    }
}
