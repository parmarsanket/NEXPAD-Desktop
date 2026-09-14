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
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun DeadzoneCrosshair(
    deadzone: Float,
    tighteningEnabled: Boolean,
    tighteningThreshold: Float,
    rawYawDps: Float,
    rawPitchDps: Float,
    processedYawDps: Float,
    processedPitchDps: Float
) {
    // Max scale for visualizing the DPS inside this box
    val maxDpsScale = 200f 

    Box(
        modifier = Modifier
            .size(150.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF1E1E1E))
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cx = size.width / 2
            val cy = size.height / 2
            
            // Draw grid / crosshair
            drawLine(
                color = Color.DarkGray,
                start = Offset(cx, 0f),
                end = Offset(cx, size.height),
                strokeWidth = 2.dp.toPx()
            )
            drawLine(
                color = Color.DarkGray,
                start = Offset(0f, cy),
                end = Offset(size.width, cy),
                strokeWidth = 2.dp.toPx()
            )

            // Calculate pixel scale (pixels per DPS)
            val pxPerDps = (size.width / 2) / maxDpsScale

            // Draw Deadzone Circle (Red)
            if (deadzone > 0) {
                val deadzoneRadiusPx = deadzone * pxPerDps
                drawCircle(
                    color = Color.Red.copy(alpha = 0.5f),
                    radius = deadzoneRadiusPx.coerceAtLeast(1f),
                    center = Offset(cx, cy),
                    style = Stroke(
                        width = 2.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                    )
                )
            }

            // Draw Tightening Circle (Blue)
            if (tighteningEnabled && tighteningThreshold > 0) {
                val tighteningRadiusPx = tighteningThreshold * pxPerDps
                drawCircle(
                    color = Color.Blue.copy(alpha = 0.3f),
                    radius = tighteningRadiusPx.coerceAtLeast(1f),
                    center = Offset(cx, cy),
                    style = Stroke(width = 1.dp.toPx())
                )
            }

            // Draw "Ghost" Cursor (Raw Input)
            val rawX = cx + (rawYawDps * pxPerDps).coerceIn(-cx, cx)
            val rawY = cy - (rawPitchDps * pxPerDps).coerceIn(-cy, cy)
            drawCircle(
                color = Color.White.copy(alpha = 0.4f),
                radius = 4.dp.toPx(),
                center = Offset(rawX, rawY)
            )

            // Draw "Solid" Cursor (Processed Output)
            val procX = cx + (processedYawDps * pxPerDps).coerceIn(-cx, cx)
            val procY = cy - (processedPitchDps * pxPerDps).coerceIn(-cy, cy)
            drawCircle(
                color = Color.Green,
                radius = 6.dp.toPx(),
                center = Offset(procX, procY)
            )
        }
    }
}
