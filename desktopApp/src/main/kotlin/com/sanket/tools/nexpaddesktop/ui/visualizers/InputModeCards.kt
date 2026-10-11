package com.sanket.tools.nexpaddesktop.ui.visualizers

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sanket.tools.nexpaddesktop.model.GyroSettings
import com.sanket.tools.nexpaddesktop.model.InputMode
import com.sanket.tools.nexpaddesktop.ui.theme.NeonPalette

@Composable
fun InputModeCards(
    currentSettings: GyroSettings,
    onSettingsChange: (GyroSettings) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Row 1: The two primary analog control modes (Aiming & Steering)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            InputModeCard(
                modifier = Modifier.weight(1f),
                mode = InputMode.VELOCITY,
                currentMode = currentSettings.inputMode,
                title = "Gyro Precision Aiming",
                badgeText = "FPS & TPS CAMERA",
                accentColor = NeonPalette.Cyan,
                description = "1:1 angular velocity translated directly into camera aiming. JoyShockLibrary standard with linear response curve.",
                icon = { isSelected -> ModeVectorIconGyroAiming(modifier = Modifier.fillMaxSize(), isSelected = isSelected) },
                onClick = { onSettingsChange(currentSettings.copy(inputMode = InputMode.VELOCITY)) }
            )

            InputModeCard(
                modifier = Modifier.weight(1f),
                mode = InputMode.ABSOLUTE_TILT,
                currentMode = currentSettings.inputMode,
                title = "Virtual Steering Wheel",
                badgeText = "RACING & SIMULATORS",
                accentColor = Color(0xFFFFB300),
                description = "Absolute gravity tilt angle mapped to virtual steering stick. Zero-drift auto-centering for racing games.",
                icon = { isSelected -> ModeVectorIconSteeringWheel(modifier = Modifier.fillMaxSize(), isSelected = isSelected) },
                onClick = { onSettingsChange(currentSettings.copy(inputMode = InputMode.ABSOLUTE_TILT)) }
            )
        }

        // Row 2: Specialized 3D, gesture, and raw stream modes
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            InputModeCard(
                modifier = Modifier.weight(1f),
                mode = InputMode.AIR_MOUSE,
                currentMode = currentSettings.inputMode,
                title = "3D Flight Stick",
                badgeText = "AVIATION YOKE",
                accentColor = Color(0xFF00E676),
                description = "6-DOF 3D attitude tracking. Independent pitch elevator, wing bank roll, and optional rudder for flight & space sims.",
                icon = { isSelected -> ModeVectorIconFlightStick(modifier = Modifier.fillMaxSize(), isSelected = isSelected) },
                onClick = { onSettingsChange(currentSettings.copy(inputMode = InputMode.AIR_MOUSE)) }
            )

            InputModeCard(
                modifier = Modifier.weight(1f),
                mode = InputMode.GESTURE,
                currentMode = currentSettings.inputMode,
                title = "Combat Gestures",
                badgeText = "KINETIC ACTIONS",
                accentColor = Color(0xFFFF1744),
                description = "Sharp physical flicks, posture tilts, and phone shakes trigger controller button strikes, dodges, or 180° snap turns.",
                icon = { isSelected -> ModeVectorIconCombatGestures(modifier = Modifier.fillMaxSize(), isSelected = isSelected) },
                onClick = { onSettingsChange(currentSettings.copy(inputMode = InputMode.GESTURE)) }
            )

            InputModeCard(
                modifier = Modifier.weight(1f),
                mode = InputMode.RAW_UNCALIBRATED,
                currentMode = currentSettings.inputMode,
                title = "Raw Silicon Gyro",
                badgeText = "ESPORTS DIRECT",
                accentColor = Color(0xFFD500F9),
                description = "Direct uncalibrated MEMS sensor stream with hardware bias removal. Zero Android OS filter lag or drift snaps.",
                icon = { isSelected -> ModeVectorIconRawSilicon(modifier = Modifier.fillMaxSize(), isSelected = isSelected) },
                onClick = { onSettingsChange(currentSettings.copy(inputMode = InputMode.RAW_UNCALIBRATED)) }
            )
        }
    }
}

@Composable
private fun InputModeCard(
    modifier: Modifier = Modifier,
    mode: InputMode,
    currentMode: InputMode,
    title: String,
    badgeText: String,
    accentColor: Color,
    description: String,
    icon: @Composable (isSelected: Boolean) -> Unit,
    onClick: () -> Unit
) {
    val isSelected = mode == currentMode

    val borderStroke = if (isSelected) {
        BorderStroke(
            width = 1.5.dp,
            brush = Brush.linearGradient(
                colors = listOf(
                    accentColor,
                    accentColor.copy(alpha = 0.4f)
                )
            )
        )
    } else {
        BorderStroke(
            width = 1.dp,
            color = NeonPalette.CardIdleBorder.copy(alpha = 0.45f)
        )
    }

    val cardBg = if (isSelected) {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFF131A2B),
                Color(0xFF0C101C)
            )
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFF10131B).copy(alpha = 0.75f),
                Color(0xFF0B0D14).copy(alpha = 0.75f)
            )
        )
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        color = Color.Transparent,
        border = borderStroke
    ) {
        Box(
            modifier = Modifier
                .background(cardBg)
                .padding(14.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Top Row: Custom Vector Icon + Active Status Pill
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier.size(54.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        icon(isSelected)
                    }

                    if (isSelected) {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = accentColor.copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, accentColor.copy(alpha = 0.6f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .background(accentColor, CircleShape)
                                )
                                Text(
                                    text = "ACTIVE",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = accentColor,
                                    letterSpacing = 0.8.sp
                                )
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .background(Color.Transparent, CircleShape)
                                .padding(1.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Tactical Badge
                Text(
                    text = badgeText,
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isSelected) accentColor else NeonPalette.CardIdleText.copy(alpha = 0.8f),
                    letterSpacing = 0.6.sp
                )

                Spacer(modifier = Modifier.height(2.dp))

                // Mode Title
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = if (isSelected) Color.White else Color(0xFFD6D6D6)
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Mode Description
                Text(
                    text = description,
                    fontSize = 11.sp,
                    color = if (isSelected) Color(0xFFB0BEC5) else NeonPalette.CardIdleText,
                    lineHeight = 15.sp,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// BESPOKE NATIVE VECTOR ICONS (Pure Canvas Mathematical Geometry)
// ─────────────────────────────────────────────────────────────────────────────

/**
 * 1. Gyro Precision Aiming: Tactical reticle with orbital gimbal ring and laser crosshair.
 */
@Composable
fun ModeVectorIconGyroAiming(
    modifier: Modifier = Modifier,
    isSelected: Boolean = false
) {
    val accent = NeonPalette.Cyan
    Canvas(modifier = modifier) {
        val cx = size.width / 2f
        val cy = size.height / 2f

        // Atmospheric Ambient Glow
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    accent.copy(alpha = if (isSelected) 0.35f else 0.12f),
                    Color.Transparent
                ),
                center = Offset(cx, cy),
                radius = size.minDimension * 0.46f
            )
        )

        // Outer Gimbal Track (Dual sweeping arcs with dash effect)
        val gimbalStroke = Stroke(
            width = 1.6f.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f))
        )
        val gimbalColor = if (isSelected) accent.copy(alpha = 0.85f) else Color(0xFF4A6572)
        drawArc(
            color = gimbalColor,
            startAngle = 135f,
            sweepAngle = 90f,
            useCenter = false,
            style = gimbalStroke,
            topLeft = Offset(cx - 24.dp.toPx(), cy - 24.dp.toPx()),
            size = Size(48.dp.toPx(), 48.dp.toPx())
        )
        drawArc(
            color = gimbalColor,
            startAngle = 315f,
            sweepAngle = 90f,
            useCenter = false,
            style = gimbalStroke,
            topLeft = Offset(cx - 24.dp.toPx(), cy - 24.dp.toPx()),
            size = Size(48.dp.toPx(), 48.dp.toPx())
        )

        // Tactical Reticle Rings
        drawCircle(
            color = if (isSelected) accent else accent.copy(alpha = 0.5f),
            radius = 16.dp.toPx(),
            center = Offset(cx, cy),
            style = Stroke(width = 1.5f.dp.toPx())
        )
        drawCircle(
            color = if (isSelected) accent.copy(alpha = 0.7f) else accent.copy(alpha = 0.3f),
            radius = 8.dp.toPx(),
            center = Offset(cx, cy),
            style = Stroke(width = 1.2f.dp.toPx())
        )

        // Cardinal Crosshair Ticks
        val tickColor = if (isSelected) Color.White else accent.copy(alpha = 0.85f)
        val tickWidth = 2.dp.toPx()
        drawLine(
            color = tickColor,
            start = Offset(cx, cy - 22.dp.toPx()),
            end = Offset(cx, cy - 11.dp.toPx()),
            strokeWidth = tickWidth,
            cap = StrokeCap.Round
        )
        drawLine(
            color = tickColor,
            start = Offset(cx, cy + 11.dp.toPx()),
            end = Offset(cx, cy + 22.dp.toPx()),
            strokeWidth = tickWidth,
            cap = StrokeCap.Round
        )
        drawLine(
            color = tickColor,
            start = Offset(cx - 22.dp.toPx(), cy),
            end = Offset(cx - 11.dp.toPx(), cy),
            strokeWidth = tickWidth,
            cap = StrokeCap.Round
        )
        drawLine(
            color = tickColor,
            start = Offset(cx + 11.dp.toPx(), cy),
            end = Offset(cx + 22.dp.toPx(), cy),
            strokeWidth = tickWidth,
            cap = StrokeCap.Round
        )

        // Center Laser Dot & Core
        drawCircle(
            color = accent.copy(alpha = 0.45f),
            radius = 4.dp.toPx(),
            center = Offset(cx, cy)
        )
        drawCircle(
            color = if (isSelected) Color.White else accent,
            radius = 2.2f.dp.toPx(),
            center = Offset(cx, cy)
        )
    }
}

/**
 * 2. Virtual Steering Wheel: GT racing steering wheel with 12 o'clock center stripe and tachometer arc.
 */
@Composable
fun ModeVectorIconSteeringWheel(
    modifier: Modifier = Modifier,
    isSelected: Boolean = false
) {
    val accent = Color(0xFFFFB300) // Amber / Gold
    Canvas(modifier = modifier) {
        val cx = size.width / 2f
        val cy = size.height / 2f

        // Ambient Glow
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    accent.copy(alpha = if (isSelected) 0.35f else 0.12f),
                    Color.Transparent
                ),
                center = Offset(cx, cy),
                radius = size.minDimension * 0.46f
            )
        )

        // Tachometer / Steering Angle Limiter Arc
        drawArc(
            color = accent.copy(alpha = if (isSelected) 0.6f else 0.25f),
            startAngle = 200f,
            sweepAngle = 140f,
            useCenter = false,
            style = Stroke(
                width = 1.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f))
            ),
            topLeft = Offset(cx - 25.dp.toPx(), cy - 25.dp.toPx()),
            size = Size(50.dp.toPx(), 50.dp.toPx())
        )

        // Outer Steering Grip Rim
        val rimRadius = 19.dp.toPx()
        drawCircle(
            color = if (isSelected) accent else accent.copy(alpha = 0.5f),
            radius = rimRadius,
            center = Offset(cx, cy),
            style = Stroke(width = 3.8f.dp.toPx(), cap = StrokeCap.Round)
        )

        // Top 12 O'Clock Racing Alignment Center Stripe
        drawArc(
            color = if (isSelected) Color.White else Color(0xFFFFE082),
            startAngle = 260f,
            sweepAngle = 20f,
            useCenter = false,
            style = Stroke(width = 4.2f.dp.toPx(), cap = StrokeCap.Round),
            topLeft = Offset(cx - rimRadius, cy - rimRadius),
            size = Size(rimRadius * 2f, rimRadius * 2f)
        )

        // Three GT Steering Spokes
        val spokeWidth = 2.4f.dp.toPx()
        val spokeColor = if (isSelected) accent.copy(alpha = 0.9f) else accent.copy(alpha = 0.45f)
        val hubRadius = 6.dp.toPx()
        // Left Spoke
        drawLine(
            color = spokeColor,
            start = Offset(cx - hubRadius, cy),
            end = Offset(cx - rimRadius + 2.dp.toPx(), cy),
            strokeWidth = spokeWidth,
            cap = StrokeCap.Round
        )
        // Right Spoke
        drawLine(
            color = spokeColor,
            start = Offset(cx + hubRadius, cy),
            end = Offset(cx + rimRadius - 2.dp.toPx(), cy),
            strokeWidth = spokeWidth,
            cap = StrokeCap.Round
        )
        // Bottom Spoke
        drawLine(
            color = spokeColor,
            start = Offset(cx, cy + hubRadius),
            end = Offset(cx, cy + rimRadius - 2.dp.toPx()),
            strokeWidth = spokeWidth,
            cap = StrokeCap.Round
        )

        // Center Hub Boss
        drawCircle(
            color = if (isSelected) Color(0xFF1E1705) else Color(0xFF141414),
            radius = hubRadius,
            center = Offset(cx, cy)
        )
        drawCircle(
            color = if (isSelected) accent else accent.copy(alpha = 0.6f),
            radius = hubRadius,
            center = Offset(cx, cy),
            style = Stroke(width = 1.4f.dp.toPx())
        )
        drawCircle(
            color = if (isSelected) Color.White else accent,
            radius = 2.dp.toPx(),
            center = Offset(cx, cy)
        )
    }
}

/**
 * 3. 3D Flight Stick: Supersonic delta-wing jet fighter with attitude horizon and roll banking gauge.
 */
@Composable
fun ModeVectorIconFlightStick(
    modifier: Modifier = Modifier,
    isSelected: Boolean = false
) {
    val accent = Color(0xFF00E676) // Electric Emerald / Aerodynamic Sky
    Canvas(modifier = modifier) {
        val cx = size.width / 2f
        val cy = size.height / 2f

        // Ambient Glow
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    accent.copy(alpha = if (isSelected) 0.35f else 0.12f),
                    Color.Transparent
                ),
                center = Offset(cx, cy),
                radius = size.minDimension * 0.46f
            )
        )

        // Roll Attitude Banking Gauge (top arc)
        val bankRadius = 22.dp.toPx()
        drawArc(
            color = accent.copy(alpha = if (isSelected) 0.75f else 0.35f),
            startAngle = 225f,
            sweepAngle = 90f,
            useCenter = false,
            style = Stroke(width = 1.4f.dp.toPx(), cap = StrokeCap.Round),
            topLeft = Offset(cx - bankRadius, cy - bankRadius),
            size = Size(bankRadius * 2f, bankRadius * 2f)
        )
        // Bank tick marks at 0° (top center)
        drawLine(
            color = if (isSelected) Color.White else accent,
            start = Offset(cx, cy - bankRadius - 3.dp.toPx()),
            end = Offset(cx, cy - bankRadius + 2.dp.toPx()),
            strokeWidth = 1.6f.dp.toPx()
        )

        // Artificial Horizon Pitch Lines
        val horizonColor = accent.copy(alpha = if (isSelected) 0.6f else 0.3f)
        drawLine(
            color = horizonColor,
            start = Offset(cx - 24.dp.toPx(), cy),
            end = Offset(cx - 10.dp.toPx(), cy),
            strokeWidth = 1.4f.dp.toPx(),
            cap = StrokeCap.Round
        )
        drawLine(
            color = horizonColor,
            start = Offset(cx + 10.dp.toPx(), cy),
            end = Offset(cx + 24.dp.toPx(), cy),
            strokeWidth = 1.4f.dp.toPx(),
            cap = StrokeCap.Round
        )
        // Upper pitch ladder rung (+10°)
        drawLine(
            color = accent.copy(alpha = 0.35f),
            start = Offset(cx - 7.dp.toPx(), cy - 9.dp.toPx()),
            end = Offset(cx + 7.dp.toPx(), cy - 9.dp.toPx()),
            strokeWidth = 1.dp.toPx()
        )
        // Lower pitch ladder rung (-10°)
        drawLine(
            color = accent.copy(alpha = 0.35f),
            start = Offset(cx - 7.dp.toPx(), cy + 9.dp.toPx()),
            end = Offset(cx + 7.dp.toPx(), cy + 9.dp.toPx()),
            strokeWidth = 1.dp.toPx()
        )

        // Delta-Wing Supersonic Fighter Jet Symbol
        val jetPath = Path().apply {
            moveTo(cx, cy - 11.dp.toPx()) // Nose cone
            lineTo(cx - 4.dp.toPx(), cy - 2.dp.toPx()) // Left fuselage strake
            lineTo(cx - 17.dp.toPx(), cy + 6.dp.toPx()) // Left delta wingtip
            lineTo(cx - 8.dp.toPx(), cy + 5.dp.toPx()) // Left trailing edge notch
            lineTo(cx - 3.dp.toPx(), cy + 10.dp.toPx()) // Left nozzle
            lineTo(cx, cy + 9.dp.toPx()) // Engine center
            lineTo(cx + 3.dp.toPx(), cy + 10.dp.toPx()) // Right nozzle
            lineTo(cx + 8.dp.toPx(), cy + 5.dp.toPx()) // Right trailing edge notch
            lineTo(cx + 17.dp.toPx(), cy + 6.dp.toPx()) // Right delta wingtip
            lineTo(cx + 4.dp.toPx(), cy - 2.dp.toPx()) // Right fuselage strake
            close()
        }

        drawPath(
            path = jetPath,
            brush = Brush.verticalGradient(
                colors = listOf(
                    if (isSelected) Color.White else accent,
                    accent.copy(alpha = 0.5f)
                ),
                startY = cy - 11.dp.toPx(),
                endY = cy + 10.dp.toPx()
            )
        )
        drawPath(
            path = jetPath,
            color = if (isSelected) Color.White else accent,
            style = Stroke(width = 1.2f.dp.toPx(), join = StrokeJoin.Round)
        )
    }
}

/**
 * 4. Combat Gestures: Crossed tactical blades with 4-way kinetic directional flick chevrons.
 */
@Composable
fun ModeVectorIconCombatGestures(
    modifier: Modifier = Modifier,
    isSelected: Boolean = false
) {
    val accent = Color(0xFFFF1744) // Crimson Red / Laser Flame
    Canvas(modifier = modifier) {
        val cx = size.width / 2f
        val cy = size.height / 2f

        // Ambient Glow
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    accent.copy(alpha = if (isSelected) 0.35f else 0.12f),
                    Color.Transparent
                ),
                center = Offset(cx, cy),
                radius = size.minDimension * 0.46f
            )
        )

        // 4 Directional Kinetic Flick Chevrons (Up, Down, Left, Right)
        val chevronColor = if (isSelected) accent else accent.copy(alpha = 0.5f)
        val chevronStroke = Stroke(width = 1.6f.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        // Up
        drawPath(
            path = Path().apply {
                moveTo(cx - 4.5f.dp.toPx(), cy - 19.dp.toPx())
                lineTo(cx, cy - 23.5f.dp.toPx())
                lineTo(cx + 4.5f.dp.toPx(), cy - 19.dp.toPx())
            },
            color = chevronColor,
            style = chevronStroke
        )
        // Down
        drawPath(
            path = Path().apply {
                moveTo(cx - 4.5f.dp.toPx(), cy + 19.dp.toPx())
                lineTo(cx, cy + 23.5f.dp.toPx())
                lineTo(cx + 4.5f.dp.toPx(), cy + 19.dp.toPx())
            },
            color = chevronColor,
            style = chevronStroke
        )
        // Left
        drawPath(
            path = Path().apply {
                moveTo(cx - 19.dp.toPx(), cy - 4.5f.dp.toPx())
                lineTo(cx - 23.5f.dp.toPx(), cy)
                lineTo(cx - 19.dp.toPx(), cy + 4.5f.dp.toPx())
            },
            color = chevronColor,
            style = chevronStroke
        )
        // Right
        drawPath(
            path = Path().apply {
                moveTo(cx + 19.dp.toPx(), cy - 4.5f.dp.toPx())
                lineTo(cx + 23.5f.dp.toPx(), cy)
                lineTo(cx + 19.dp.toPx(), cy + 4.5f.dp.toPx())
            },
            color = chevronColor,
            style = chevronStroke
        )

        // Crossed Energy Combat Blades
        val bladeColor = if (isSelected) Color.White else accent.copy(alpha = 0.9f)
        val bladeWidth = 2.4f.dp.toPx()
        // Blade 1: Top-Left to Bottom-Right
        drawLine(
            color = bladeColor,
            start = Offset(cx - 13.dp.toPx(), cy - 13.dp.toPx()),
            end = Offset(cx + 13.dp.toPx(), cy + 13.dp.toPx()),
            strokeWidth = bladeWidth,
            cap = StrokeCap.Round
        )
        // Crossguard 1
        drawLine(
            color = accent,
            start = Offset(cx + 8.5f.dp.toPx(), cy + 13.5f.dp.toPx()),
            end = Offset(cx + 13.5f.dp.toPx(), cy + 8.5f.dp.toPx()),
            strokeWidth = 2.8f.dp.toPx(),
            cap = StrokeCap.Round
        )

        // Blade 2: Top-Right to Bottom-Left
        drawLine(
            color = bladeColor,
            start = Offset(cx + 13.dp.toPx(), cy - 13.dp.toPx()),
            end = Offset(cx - 13.dp.toPx(), cy + 13.dp.toPx()),
            strokeWidth = bladeWidth,
            cap = StrokeCap.Round
        )
        // Crossguard 2
        drawLine(
            color = accent,
            start = Offset(cx - 13.5f.dp.toPx(), cy + 8.5f.dp.toPx()),
            end = Offset(cx - 8.5f.dp.toPx(), cy + 13.5f.dp.toPx()),
            strokeWidth = 2.8f.dp.toPx(),
            cap = StrokeCap.Round
        )

        // Center Kinetic Burst Node & Slashing Streak
        drawCircle(
            color = if (isSelected) Color(0xFFFFD54F) else accent,
            radius = 2.6f.dp.toPx(),
            center = Offset(cx, cy)
        )
        drawArc(
            color = accent.copy(alpha = if (isSelected) 0.85f else 0.4f),
            startAngle = 40f,
            sweepAngle = 100f,
            useCenter = false,
            style = Stroke(1.5f.dp.toPx(), cap = StrokeCap.Round),
            topLeft = Offset(cx - 9.dp.toPx(), cy - 9.dp.toPx()),
            size = Size(18.dp.toPx(), 18.dp.toPx())
        )
    }
}

/**
 * 5. Raw Silicon Gyro: MEMS microchip IC package with exposed silicon die and vibrating ring resonator.
 */
@Composable
fun ModeVectorIconRawSilicon(
    modifier: Modifier = Modifier,
    isSelected: Boolean = false
) {
    val accent = Color(0xFFD500F9) // Neon Purple / Ultraviolet
    Canvas(modifier = modifier) {
        val cx = size.width / 2f
        val cy = size.height / 2f

        // Ambient Glow
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    accent.copy(alpha = if (isSelected) 0.35f else 0.12f),
                    Color.Transparent
                ),
                center = Offset(cx, cy),
                radius = size.minDimension * 0.46f
            )
        )

        // Integrated Circuit (IC) Package Body
        val chipSize = 28.dp.toPx()
        val chipRadius = 4.dp.toPx()
        drawRoundRect(
            color = if (isSelected) Color(0xFF1E0A2A) else Color(0xFF16101F),
            topLeft = Offset(cx - chipSize / 2f, cy - chipSize / 2f),
            size = Size(chipSize, chipSize),
            cornerRadius = CornerRadius(chipRadius, chipRadius)
        )
        drawRoundRect(
            color = if (isSelected) accent else accent.copy(alpha = 0.5f),
            topLeft = Offset(cx - chipSize / 2f, cy - chipSize / 2f),
            size = Size(chipSize, chipSize),
            cornerRadius = CornerRadius(chipRadius, chipRadius),
            style = Stroke(width = 1.5f.dp.toPx())
        )

        // IC Lead Pins (2 pins per side = 8 total pins)
        val pinColor = if (isSelected) Color.White.copy(0.9f) else accent.copy(0.6f)
        val pinWidth = 1.6f.dp.toPx()
        val pinExt = 5.dp.toPx()
        val pinSpacing = 6.dp.toPx()

        // Top Pins
        drawLine(pinColor, Offset(cx - pinSpacing, cy - chipSize / 2f), Offset(cx - pinSpacing, cy - chipSize / 2f - pinExt), pinWidth, StrokeCap.Round)
        drawLine(pinColor, Offset(cx + pinSpacing, cy - chipSize / 2f), Offset(cx + pinSpacing, cy - chipSize / 2f - pinExt), pinWidth, StrokeCap.Round)
        // Bottom Pins
        drawLine(pinColor, Offset(cx - pinSpacing, cy + chipSize / 2f), Offset(cx - pinSpacing, cy + chipSize / 2f + pinExt), pinWidth, StrokeCap.Round)
        drawLine(pinColor, Offset(cx + pinSpacing, cy + chipSize / 2f), Offset(cx + pinSpacing, cy + chipSize / 2f + pinExt), pinWidth, StrokeCap.Round)
        // Left Pins
        drawLine(pinColor, Offset(cx - chipSize / 2f, cy - pinSpacing), Offset(cx - chipSize / 2f - pinExt, cy - pinSpacing), pinWidth, StrokeCap.Round)
        drawLine(pinColor, Offset(cx - chipSize / 2f, cy + pinSpacing), Offset(cx - chipSize / 2f - pinExt, cy + pinSpacing), pinWidth, StrokeCap.Round)
        // Right Pins
        drawLine(pinColor, Offset(cx + chipSize / 2f, cy - pinSpacing), Offset(cx + chipSize / 2f + pinExt, cy - pinSpacing), pinWidth, StrokeCap.Round)
        drawLine(pinColor, Offset(cx + chipSize / 2f, cy + pinSpacing), Offset(cx + chipSize / 2f + pinExt, cy + pinSpacing), pinWidth, StrokeCap.Round)

        // Exposed Silicon Wafer Window
        val waferSize = 16.dp.toPx()
        val waferRadius = 2.dp.toPx()
        drawRoundRect(
            color = if (isSelected) Color(0xFF330C49) else Color(0xFF231433),
            topLeft = Offset(cx - waferSize / 2f, cy - waferSize / 2f),
            size = Size(waferSize, waferSize),
            cornerRadius = CornerRadius(waferRadius, waferRadius)
        )
        drawRoundRect(
            color = if (isSelected) Color.White.copy(alpha = 0.7f) else accent.copy(alpha = 0.4f),
            topLeft = Offset(cx - waferSize / 2f, cy - waferSize / 2f),
            size = Size(waferSize, waferSize),
            cornerRadius = CornerRadius(waferRadius, waferRadius),
            style = Stroke(width = 1.dp.toPx())
        )

        // MEMS Gyroscope Vibrating Ring Resonator
        drawCircle(
            color = if (isSelected) Color.White else accent,
            radius = 4.5f.dp.toPx(),
            center = Offset(cx, cy),
            style = Stroke(width = 1.2f.dp.toPx())
        )
        // Core Frequency Dot
        drawCircle(
            color = if (isSelected) Color.White else accent,
            radius = 1.8f.dp.toPx(),
            center = Offset(cx, cy)
        )
        // 4 Micro Flexure Spokes
        drawLine(accent.copy(0.6f), Offset(cx - 4.5f.dp.toPx(), cy), Offset(cx - waferSize / 2f + 1.dp.toPx(), cy), 1.dp.toPx())
        drawLine(accent.copy(0.6f), Offset(cx + 4.5f.dp.toPx(), cy), Offset(cx + waferSize / 2f - 1.dp.toPx(), cy), 1.dp.toPx())
        drawLine(accent.copy(0.6f), Offset(cx, cy - 4.5f.dp.toPx()), Offset(cx, cy - waferSize / 2f + 1.dp.toPx()), 1.dp.toPx())
        drawLine(accent.copy(0.6f), Offset(cx, cy + 4.5f.dp.toPx()), Offset(cx, cy + waferSize / 2f - 1.dp.toPx()), 1.dp.toPx())
    }
}
