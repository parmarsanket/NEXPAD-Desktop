package com.sanket.tools.nexpaddesktop.ui.visualizers

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sanket.tools.nexpaddesktop.model.GyroSettings
import kotlin.math.abs

@Composable
fun DashboardHeader(
    settings: GyroSettings,
    rawGyroX: Float,
    rawGyroY: Float,
    rawGyroZ: Float,
    rawAccelX: Float,
    rawAccelY: Float,
    rawAccelZ: Float,
    processedYaw: Float,
    processedPitch: Float
) {
    val RAD_TO_DEG = 57.2957795f
    var pitchDeg = rawGyroX * RAD_TO_DEG
    var rollDeg = rawGyroY * RAD_TO_DEG
    var yawDeg = rawGyroZ * RAD_TO_DEG

    if (settings.inputMode == com.sanket.tools.nexpaddesktop.model.InputMode.ABSOLUTE_TILT) {
        pitchDeg = 0f 
        rollDeg = 0f
        yawDeg = (rawAccelX / 9.8f) * 90f // Steering wheel motion (rotationZ)
    }

    // Apply Axis Inversion to Visualizer
    if (settings.invertX) {
        yawDeg = -yawDeg
        rollDeg = -rollDeg // Roll is often part of horizontal depending on HorizontalAxis setting
    }
    if (settings.invertY) {
        pitchDeg = -pitchDeg
    }

    // Settings Impact Meter logic
    val equivalentSensitivity = 200.0f / settings.maxDpsYaw
    val responsiveness = (equivalentSensitivity / 5.0f).coerceIn(0f, 1f)
    val deadzonePrecision = 1.0f - (settings.deadzoneThreshold / 20f).coerceIn(0f, 1f)
    val tighteningPrecision = if (settings.tighteningEnabled) (settings.tighteningThreshold / 30f).coerceIn(0f, 1f) else 0f
    val precision = maxOf(deadzonePrecision, tighteningPrecision)
    val stability = if (settings.smoothingEnabled) settings.smoothingAmount.coerceIn(0f, 1f) else 0f

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier.padding(24.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Side: Live Data
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "🎯 GYRO ACTIVE",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                val isAbsolute = settings.inputMode == com.sanket.tools.nexpaddesktop.model.InputMode.ABSOLUTE_TILT
                Text(if (isAbsolute) "Steering Angle" else "Processed Speed", fontSize = 12.sp, color = Color.Gray)
                
                if (isAbsolute) {
                    val steeringAngle = yawDeg
                    Text(
                        text = "${String.format("%.0f", steeringAngle)} °",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Light
                    )
                } else {
                    Text(
                        text = "${String.format("%.1f", abs(processedYaw))} °/s",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Light
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Impact Meter
                ImpactBar("Responsiveness", responsiveness, Color(0xFFFF5722))
                ImpactBar("Precision", precision, Color(0xFF03A9F4))
                ImpactBar("Stability", stability, Color(0xFF4CAF50))
            }

            // Right Side: 3D Phone Visualization
            Box(
                modifier = Modifier
                    .size(150.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.Black.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                // The "3D" Phone
                Canvas(
                    modifier = Modifier
                        .size(120.dp, 60.dp)
                        .graphicsLayer {
                            // Convert angular velocity to an absolute rotation for visualization
                            // In a real scenario, you'd integrate velocity to get position, 
                            // but for a live preview of velocity/tilt, we just use the raw values directly.
                            rotationX = pitchDeg
                            rotationY = rollDeg
                            rotationZ = yawDeg
                            cameraDistance = 8f
                        }
                ) {
                    val width = size.width
                    val height = size.height
                    val cornerRadius = CornerRadius(12.dp.toPx())

                    // Draw Phone Body
                    drawRoundRect(
                        color = Color.DarkGray,
                        size = Size(width, height),
                        cornerRadius = cornerRadius
                    )
                    
                    // Draw Screen
                    drawRoundRect(
                        color = Color(0xFF2196F3),
                        topLeft = Offset(width * 0.05f, height * 0.05f),
                        size = Size(width * 0.9f, height * 0.9f),
                        cornerRadius = CornerRadius(8.dp.toPx())
                    )
                }
            }
        }
    }
}

@Composable
private fun ImpactBar(label: String, value: Float, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label, fontSize = 11.sp, modifier = Modifier.width(90.dp), color = Color.Gray)
        LinearProgressIndicator(
            progress = value,
            modifier = Modifier.width(80.dp).height(6.dp).clip(RoundedCornerShape(3.dp)),
            color = color,
            trackColor = Color.LightGray.copy(alpha = 0.3f)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text("${(value * 100).toInt()}%", fontSize = 11.sp)
    }
}
