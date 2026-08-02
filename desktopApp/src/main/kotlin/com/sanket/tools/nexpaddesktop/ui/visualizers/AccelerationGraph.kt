package com.sanket.tools.nexpaddesktop.ui.visualizers

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.sanket.tools.nexpaddesktop.model.AccelerationType
import kotlin.math.pow

@Composable
fun AccelerationGraph(
    curveType: AccelerationType,
    minThreshold: Float,
    maxThreshold: Float,
    minSens: Float,
    maxSens: Float
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF1E1E1E))
    ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            val w = size.width
            val h = size.height

            // Draw axes
            drawLine(
                color = Color.DarkGray,
                start = Offset(0f, 0f),
                end = Offset(0f, h),
                strokeWidth = 2.dp.toPx()
            )
            drawLine(
                color = Color.DarkGray,
                start = Offset(0f, h),
                end = Offset(w, h),
                strokeWidth = 2.dp.toPx()
            )

            // Calculate scales
            // Let's assume max possible physical rotation speed we care about is 300 DPS for the graph
            val maxGraphDps = 300f 
            val maxGraphSens = 5.0f

            val pxPerDps = w / maxGraphDps
            val pxPerSens = h / maxGraphSens

            // Draw Threshold Guidelines
            val minX = minThreshold * pxPerDps
            val maxX = maxThreshold * pxPerDps
            
            val dashed = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)

            drawLine(
                color = Color.Gray.copy(alpha = 0.5f),
                start = Offset(minX, 0f),
                end = Offset(minX, h),
                strokeWidth = 1.dp.toPx(),
                pathEffect = dashed
            )
            drawLine(
                color = Color.Gray.copy(alpha = 0.5f),
                start = Offset(maxX, 0f),
                end = Offset(maxX, h),
                strokeWidth = 1.dp.toPx(),
                pathEffect = dashed
            )

            // Draw the Response Curve
            val path = Path()
            val points = 50
            val dx = maxGraphDps / points

            for (i in 0..points) {
                val currentSpeed = i * dx
                val currentX = currentSpeed * pxPerDps

                val sens = when {
                    currentSpeed <= minThreshold -> minSens
                    currentSpeed >= maxThreshold -> maxSens
                    else -> {
                        val progress = (currentSpeed - minThreshold) / (maxThreshold - minThreshold)
                        when (curveType) {
                            AccelerationType.LINEAR -> minSens + progress * (maxSens - minSens)
                            AccelerationType.POWER -> minSens + progress.pow(2) * (maxSens - minSens)
                            AccelerationType.SMOOTH_STEP -> {
                                val smooth = progress * progress * (3 - 2 * progress)
                                minSens + smooth * (maxSens - minSens)
                            }
                        }
                    }
                }

                val currentY = h - (sens * pxPerSens)

                if (i == 0) {
                    path.moveTo(currentX, currentY)
                } else {
                    path.lineTo(currentX, currentY)
                }
            }

            drawPath(
                path = path,
                color = Color(0xFFFF5722),
                style = Stroke(width = 3.dp.toPx())
            )
        }
    }
}
