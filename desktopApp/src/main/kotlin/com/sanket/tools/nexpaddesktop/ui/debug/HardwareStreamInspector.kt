package com.sanket.tools.nexpaddesktop.ui.debug

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sanket.tools.nexpaddesktop.ui.theme.NeonPalette
import com.sanket.tools.nexpaddesktop.viewmodel.DesktopViewModel
import kotlin.math.PI
import kotlin.math.sqrt

@Composable
fun HudGlassCard(
    title: String,
    primaryText: String,
    secondaryText: String,
    statusColor: Color,
    icon: String
) {
    Box(
        modifier = Modifier
            .widthIn(min = 175.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF0A0F1D))
            .border(1.dp, Color(0xFF1B273F), RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 7.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$icon $title",
                    color = Color.White.copy(alpha = 0.55f),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(statusColor)
                )
            }
            Text(
                text = primaryText,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 11.5.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = secondaryText,
                color = statusColor,
                fontWeight = FontWeight.Medium,
                fontSize = 10.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Real-time Hardware Stream Inspector bench.
 * Renders live stick crosshairs, trigger pressures, pressed button matrix, 6-axis IMU, and haptic rumble gauges.
 */
@Composable
fun HardwareStreamInspector(viewModel: DesktopViewModel) {
    val input = viewModel.latestInput
    val feedback = viewModel.latestFeedback

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF090E1B))
            .border(1.dp, Color(0xFF1C2740), RoundedCornerShape(10.dp))
            .padding(11.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🎮", fontSize = 12.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "LIVE HARDWARE & SENSOR STREAM INSPECTOR",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.6.sp
                    )
                }
                Text(
                    text = "Sequence #${input.sequenceNumber} • Phone Stream: 200Hz (5ms Low-Latency)",
                    color = Color.White.copy(alpha = 0.45f),
                    fontSize = 9.5.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1. Dual Thumbstick Visualizers
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StickVisualizer(title = "LEFT STICK", x = input.leftStickX, y = input.leftStickY)
                    StickVisualizer(title = "RIGHT STICK", x = input.rightStickX, y = input.rightStickY)
                }

                // 2. Analog Triggers
                Column(
                    modifier = Modifier.width(90.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    TriggerBar(label = "LT TRIGGER", value = input.triggerL2)
                    TriggerBar(label = "RT TRIGGER", value = input.triggerR2)
                }

                // 3. Digital Buttons Matrix
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("BUTTON MATRIX", color = Color.White.copy(alpha = 0.6f), fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                        ButtonIndicatorChip(label = "A", isPressed = input.btnA)
                        ButtonIndicatorChip(label = "B", isPressed = input.btnB)
                        ButtonIndicatorChip(label = "X", isPressed = input.btnX)
                        ButtonIndicatorChip(label = "Y", isPressed = input.btnY)
                        ButtonIndicatorChip(label = "LB", isPressed = input.btnL1)
                        ButtonIndicatorChip(label = "RB", isPressed = input.btnR1)
                        ButtonIndicatorChip(label = "L3", isPressed = input.btnL3)
                        ButtonIndicatorChip(label = "R3", isPressed = input.btnR3)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                        ButtonIndicatorChip(label = "▲", isPressed = input.dpadUp)
                        ButtonIndicatorChip(label = "▼", isPressed = input.dpadDown)
                        ButtonIndicatorChip(label = "◀", isPressed = input.dpadLeft)
                        ButtonIndicatorChip(label = "▶", isPressed = input.dpadRight)
                        ButtonIndicatorChip(label = "Start", isPressed = input.btnStart)
                        ButtonIndicatorChip(label = "Select", isPressed = input.btnSelect)
                        ButtonIndicatorChip(label = "Guide", isPressed = input.btnGuide)
                    }
                }

                // 4. 6-Axis Motion / IMU Telemetry (Audit: X = Pitch, Y = Roll, Z = Yaw)
                val radToDeg = (180.0 / PI).toFloat()
                val safeGyroX = if (input.gyroX.isFinite()) input.gyroX else 0f
                val safeGyroY = if (input.gyroY.isFinite()) input.gyroY else 0f
                val safeGyroZ = if (input.gyroZ.isFinite()) input.gyroZ else 0f
                val gyroPitchDeg = safeGyroX * radToDeg
                val gyroRollDeg = safeGyroY * radToDeg
                val gyroYawDeg = safeGyroZ * radToDeg

                val safeAccelX = if (input.accelX.isFinite()) input.accelX else 0f
                val safeAccelY = if (input.accelY.isFinite()) input.accelY else 0f
                val safeAccelZ = if (input.accelZ.isFinite()) input.accelZ else 0f
                val sumSq = safeAccelX * safeAccelX + safeAccelY * safeAccelY + safeAccelZ * safeAccelZ
                val totalG = (sqrt(sumSq) / 9.80665f).coerceIn(0f, 99f)

                val safeYaw = if (viewModel.processedYaw.isFinite()) viewModel.processedYaw else 0f
                val safePitch = if (viewModel.processedPitch.isFinite()) viewModel.processedPitch else 0f

                Column(
                    modifier = Modifier.widthIn(min = 220.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text("6-AXIS IMU SENSORS", color = Color.White.copy(alpha = 0.6f), fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
                    Text(
                        text = "Gyro: P:${String.format(java.util.Locale.US, "%+.1f", gyroPitchDeg)}° Y:${String.format(java.util.Locale.US, "%+.1f", gyroYawDeg)}° R:${String.format(java.util.Locale.US, "%+.1f", gyroRollDeg)}°/s",
                        color = NeonPalette.Cyan,
                        fontSize = 9.5.sp,
                        fontFamily = FontFamily.Monospace,
                        maxLines = 1,
                        softWrap = false
                    )
                    Text(
                        text = "Accel: ${String.format(java.util.Locale.US, "%.2f", totalG)}G (X:${String.format(java.util.Locale.US, "%+.1f", safeAccelX)} Y:${String.format(java.util.Locale.US, "%+.1f", safeAccelY)} Z:${String.format(java.util.Locale.US, "%+.1f", safeAccelZ)})",
                        color = Color(0xFFA5F3FC),
                        fontSize = 9.5.sp,
                        fontFamily = FontFamily.Monospace,
                        maxLines = 1,
                        softWrap = false
                    )
                    Text(
                        text = "Fused Aim: Yaw ${String.format(java.util.Locale.US, "%+.1f°", safeYaw)} | Pitch ${String.format(java.util.Locale.US, "%+.1f°", safePitch)}",
                        color = NeonPalette.Green,
                        fontSize = 9.5.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        softWrap = false
                    )
                }

                // 5. Haptic Rumble Feedback
                Column(
                    modifier = Modifier.width(110.dp),
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Text("HAPTIC RUMBLE", color = Color.White.copy(alpha = 0.6f), fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
                    RumbleMotorBar(label = "HEAVY MOTOR", speed = feedback.leftMotorSpeed, barColor = Color(0xFFF472B6))
                    RumbleMotorBar(label = "LIGHT MOTOR", speed = feedback.rightMotorSpeed, barColor = Color(0xFF38BDF8))
                }
            }
        }
    }
}

/**
 * 2D Circular radar visualizer for thumbstick coordinates.
 */
@Composable
fun StickVisualizer(
    title: String,
    x: Float,
    y: Float,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Text(
            text = title,
            color = Color.White.copy(alpha = 0.65f),
            fontSize = 9.5.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(3.dp))
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(CircleShape)
                .background(Color(0xFF060912))
                .border(1.dp, Color(0xFF1E2D4A), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val radius = size.width / 2f - 3f

                drawLine(
                    color = Color(0xFF1A263D),
                    start = Offset(center.x, 3f),
                    end = Offset(center.x, size.height - 3f),
                    strokeWidth = 1f
                )
                drawLine(
                    color = Color(0xFF1A263D),
                    start = Offset(3f, center.y),
                    end = Offset(size.width - 3f, center.y),
                    strokeWidth = 1f
                )

                drawCircle(
                    color = Color(0xFF1A263D),
                    radius = radius * 0.45f,
                    style = Stroke(width = 1f)
                )

                val safeX = if (x.isFinite()) x.coerceIn(-1f, 1f) else 0f
                val safeY = if (y.isFinite()) y.coerceIn(-1f, 1f) else 0f
                val puckOffset = Offset(
                    center.x + safeX * (radius - 5f),
                    center.y - safeY * (radius - 5f)
                )
                drawCircle(
                    color = NeonPalette.Cyan.copy(alpha = 0.4f),
                    radius = 7f,
                    center = puckOffset
                )
                drawCircle(
                    color = NeonPalette.Cyan,
                    radius = 4f,
                    center = puckOffset
                )
            }
        }
        Spacer(modifier = Modifier.height(2.dp))
        val safeX = if (x.isFinite()) x.coerceIn(-1f, 1f) else 0f
        val safeY = if (y.isFinite()) y.coerceIn(-1f, 1f) else 0f
        Text(
            text = "X:${String.format(java.util.Locale.US, "%+.2f", safeX)} Y:${String.format(java.util.Locale.US, "%+.2f", safeY)}",
            color = NeonPalette.Cyan,
            fontSize = 9.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.SemiBold
        )
    }
}

/**
 * Analog trigger progress bar.
 */
@Composable
fun TriggerBar(
    label: String,
    value: Float,
    modifier: Modifier = Modifier
) {
    val safeValue = if (value.isFinite()) value.coerceIn(0f, 1f) else 0f
    val percent = (safeValue * 100).toInt()
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, color = Color.White.copy(alpha = 0.6f), fontSize = 9.sp, fontWeight = FontWeight.Bold)
            Text("$percent%", color = if (percent > 0) NeonPalette.Green else Color.Gray, fontSize = 9.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.SemiBold)
        }
        Spacer(modifier = Modifier.height(2.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(5.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(Color(0xFF0F172A))
                .border(0.5.dp, Color(0xFF1E293B), RoundedCornerShape(3.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(safeValue)
                    .clip(RoundedCornerShape(3.dp))
                    .background(if (percent > 50) NeonPalette.Green else NeonPalette.Cyan)
            )
        }
    }
}

/**
 * Digital button active chip.
 */
@Composable
fun ButtonIndicatorChip(
    label: String,
    isPressed: Boolean
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(if (isPressed) NeonPalette.Green.copy(alpha = 0.28f) else Color(0xFF080D19))
            .border(0.8.dp, if (isPressed) NeonPalette.Green else Color(0xFF1C2740), RoundedCornerShape(4.dp))
            .padding(horizontal = 5.dp, vertical = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (isPressed) NeonPalette.Green else Color.White.copy(alpha = 0.45f),
            fontWeight = if (isPressed) FontWeight.Bold else FontWeight.Normal,
            fontSize = 8.5.sp,
            fontFamily = FontFamily.Monospace
        )
    }
}

/**
 * Haptic rumble motor gauge bar.
 */
@Composable
fun RumbleMotorBar(
    label: String,
    speed: Int,
    barColor: Color,
    modifier: Modifier = Modifier
) {
    val fraction = speed.coerceIn(0, 255) / 255f
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, color = Color.White.copy(alpha = 0.6f), fontSize = 8.5.sp, fontWeight = FontWeight.Bold)
            Text("$speed", color = if (speed > 0) barColor else Color.Gray, fontSize = 8.5.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.SemiBold)
        }
        Spacer(modifier = Modifier.height(2.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.5.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(Color(0xFF0F172A))
                .border(0.5.dp, Color(0xFF1E293B), RoundedCornerShape(3.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(fraction)
                    .clip(RoundedCornerShape(3.dp))
                    .background(barColor)
            )
        }
    }
}
