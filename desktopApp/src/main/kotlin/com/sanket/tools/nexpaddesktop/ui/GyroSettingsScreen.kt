package com.sanket.tools.nexpaddesktop.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sanket.tools.nexpaddesktop.model.*
import com.sanket.tools.nexpaddesktop.ui.visualizers.*
import com.sanket.tools.nexpaddesktop.ui.theme.NeonPalette
import com.sanket.tools.nexpad.model.NexpadKeys

/**
 * Dedicated Xbox 360 Motion Settings Section.
 *
 * Provides a clean, modern Material 3 control center for configuring:
 * - 5 Sensor Modes (Gyro Aiming, Steering Wheel, Air Mouse, Gestures, Raw Uncalibrated)
 * - Quick Presets (FPS, Racing, Precision, Defaults)
 * - Contextual Mode Tuning (Deadzone, Smoothing, Curves, Anti-Deadzone)
 * - Activation Triggers (ADS Button Hold)
 * - Stick Assignment & Blending
 */
@Composable
fun XboxMotionSettingsSection(
    settings: GyroSettings,
    onSettingsChange: (GyroSettings) -> Unit,
    rawGyroX: Float,
    rawGyroY: Float,
    rawGyroZ: Float,
    rawAccelX: Float,
    rawAccelY: Float,
    rawAccelZ: Float,
    rawGravityX: Float = 0f,
    rawGravityY: Float = 0f,
    rawGravityZ: Float = 0f,
    linearAccelX: Float = 0f,
    linearAccelY: Float = 0f,
    linearAccelZ: Float = 0f,
    rawUncalGyroX: Float = 0f,
    rawUncalGyroY: Float = 0f,
    rawUncalGyroZ: Float = 0f,
    processedYaw: Float,
    processedPitch: Float,
    onRecalibrate: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ── Master Motion Switch ──
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
            border = BorderStroke(1.dp, NeonPalette.CardIdleBorder)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val infinite = rememberInfiniteTransition(label = "pulse")
                    val alpha by infinite.animateFloat(
                        initialValue = 0.4f,
                        targetValue = 1f,
                        animationSpec = infiniteRepeatable(tween(800, easing = FastOutSlowInEasing), RepeatMode.Reverse),
                        label = "dotAlpha"
                    )
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(
                                if (settings.enabled) NeonPalette.ConnectedDot.copy(alpha = alpha)
                                else NeonPalette.CardIdleText
                            )
                    )
                    Column {
                        Text(
                            "Xbox Motion Emulation",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color.White
                        )
                        Text(
                            if (settings.enabled) "Active • Mapping phone sensors to virtual Xbox stick controls"
                            else "Disabled • Traditional physical gamepad inputs only",
                            fontSize = 12.sp,
                            color = NeonPalette.CardIdleText
                        )
                    }
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (settings.enabled) {
                        OutlinedButton(
                            onClick = {
                                val defaults = GyroSettings()
                                val resetSettings = when (settings.inputMode) {
                                    InputMode.VELOCITY, InputMode.RAW_UNCALIBRATED -> settings.copy(
                                        maxDpsYaw = defaults.maxDpsYaw,
                                        maxDpsPitch = defaults.maxDpsPitch,
                                        maxDpsRoll = defaults.maxDpsRoll,
                                        horizontalAxis = defaults.horizontalAxis,
                                        invertX = defaults.invertX,
                                        invertRoll = defaults.invertRoll,
                                        invertY = defaults.invertY,
                                        stickResponseCurve = defaults.stickResponseCurve,
                                        accelerationEnabled = defaults.accelerationEnabled,
                                        maxSensitivity = defaults.maxSensitivity,
                                        maxThreshold = defaults.maxThreshold,
                                        minThreshold = defaults.minThreshold,
                                        gameDeadzoneCompensation = defaults.gameDeadzoneCompensation,
                                        deadzoneThreshold = defaults.deadzoneThreshold,
                                        tighteningEnabled = defaults.tighteningEnabled,
                                        tighteningThreshold = defaults.tighteningThreshold,
                                        lowSpeedAmplifierEnabled = defaults.lowSpeedAmplifierEnabled,
                                        lowSpeedAmplifierAmount = defaults.lowSpeedAmplifierAmount,
                                        lowSpeedAmplifierThreshold = defaults.lowSpeedAmplifierThreshold,
                                        smoothingEnabled = defaults.smoothingEnabled,
                                        smoothingAmount = defaults.smoothingAmount,
                                        adaptiveSmoothing = defaults.adaptiveSmoothing,
                                        smoothingThreshold = defaults.smoothingThreshold,
                                        xboxTargetStick = defaults.xboxTargetStick,
                                        xboxBlendMode = defaults.xboxBlendMode,
                                        activationButtons = defaults.activationButtons
                                    )
                                    InputMode.ABSOLUTE_TILT -> settings.copy(
                                        absoluteMaxTilt = defaults.absoluteMaxTilt,
                                        absoluteSensitivityX = defaults.absoluteSensitivityX,
                                        absoluteSensitivityY = defaults.absoluteSensitivityY,
                                        absoluteCurve = defaults.absoluteCurve,
                                        invertX = defaults.invertX,
                                        xboxTargetStick = XboxTargetStick.LEFT_STICK,
                                        activationButtons = defaults.activationButtons
                                    )
                                    InputMode.AIR_MOUSE -> settings.copy(
                                        airMousePitchTarget = defaults.airMousePitchTarget,
                                        airMousePitchInvert = defaults.airMousePitchInvert,
                                        airMouseMaxPitch = defaults.airMouseMaxPitch,
                                        airMouseRollTarget = defaults.airMouseRollTarget,
                                        airMouseRollInvert = defaults.airMouseRollInvert,
                                        airMouseMaxRoll = defaults.airMouseMaxRoll,
                                        airMouseMaxRollYaw = defaults.airMouseMaxRollYaw,
                                        airMouseYawTarget = defaults.airMouseYawTarget,
                                        airMouseYawInvert = defaults.airMouseYawInvert,
                                        airMouseMaxYaw = defaults.airMouseMaxYaw,
                                        airMouseDeadzone = defaults.airMouseDeadzone,
                                        airMouseCurve = defaults.airMouseCurve,
                                        activationButtons = defaults.activationButtons
                                    )
                                    InputMode.GESTURE -> settings.copy(
                                        gestureFlicksEnabled = defaults.gestureFlicksEnabled,
                                        gestureFlickUpAction = defaults.gestureFlickUpAction,
                                        gestureFlickDownAction = defaults.gestureFlickDownAction,
                                        gestureFlickLeftAction = defaults.gestureFlickLeftAction,
                                        gestureFlickRightAction = defaults.gestureFlickRightAction,
                                        gestureFlickThreshold = defaults.gestureFlickThreshold,
                                        gestureFlickCooldownMs = defaults.gestureFlickCooldownMs,
                                        gestureTiltEnabled = defaults.gestureTiltEnabled,
                                        gestureTiltPitchThreshold = defaults.gestureTiltPitchThreshold,
                                        gestureTiltRollThreshold = defaults.gestureTiltRollThreshold,
                                        gestureTiltUpAction = defaults.gestureTiltUpAction,
                                        gestureTiltDownAction = defaults.gestureTiltDownAction,
                                        gestureTiltLeftAction = defaults.gestureTiltLeftAction,
                                        gestureTiltRightAction = defaults.gestureTiltRightAction,
                                        gestureTiltHoldMode = defaults.gestureTiltHoldMode,
                                        gestureShakeEnabled = defaults.gestureShakeEnabled,
                                        gestureShakeThreshold = defaults.gestureShakeThreshold,
                                        gestureShakeAction = defaults.gestureShakeAction,
                                        gestureShakeCooldownMs = defaults.gestureShakeCooldownMs,
                                        gestureJoystickMode = defaults.gestureJoystickMode,
                                        gestureAimingEnabled = defaults.gestureAimingEnabled,
                                        activationButtons = defaults.activationButtons
                                    )
                                }
                                onSettingsChange(resetSettings)
                            },
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, NeonPalette.Cyan.copy(alpha = 0.5f)),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = NeonPalette.Cyan.copy(alpha = 0.08f),
                                contentColor = NeonPalette.Cyan
                            ),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Reset Current Mode Defaults",
                                modifier = Modifier.size(13.dp),
                                tint = NeonPalette.Cyan
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Reset Mode",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = NeonPalette.Cyan
                            )
                        }
                    }
                    Switch(
                        checked = settings.enabled,
                        onCheckedChange = { onSettingsChange(settings.copy(enabled = it)) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = NeonPalette.Cyan,
                            checkedTrackColor = NeonPalette.Cyan.copy(alpha = 0.5f)
                        )
                    )
                }
            }
        }

        if (!settings.enabled) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                border = BorderStroke(1.dp, NeonPalette.CardIdleBorder.copy(alpha = 0.3f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Motion controls are currently disabled. Toggle the switch above to configure Xbox gyro and tilt settings.",
                        color = NeonPalette.CardIdleText,
                        fontSize = 13.sp
                    )
                }
            }
            return
        }

        // ── 3D Visualizer Header ──
        DashboardHeader(
            settings = settings,
            rawGyroX = rawGyroX,
            rawGyroY = rawGyroY,
            rawGyroZ = rawGyroZ,
            rawAccelX = rawAccelX,
            rawAccelY = rawAccelY,
            rawAccelZ = rawAccelZ,
            rawGravityX = rawGravityX,
            rawGravityY = rawGravityY,
            rawGravityZ = rawGravityZ,
            linearAccelX = linearAccelX,
            linearAccelY = linearAccelY,
            linearAccelZ = linearAccelZ,
            rawUncalGyroX = rawUncalGyroX,
            rawUncalGyroY = rawUncalGyroY,
            rawUncalGyroZ = rawUncalGyroZ,
            processedYaw = processedYaw,
            processedPitch = processedPitch
        )

        // ── 1. Motion Dynamics & Sensor Input Modes ──
        ModernSectionCard(
            title = "Motion Dynamics & Sensor Input Modes",
            customIcon = {
                Canvas(modifier = Modifier.size(20.dp)) {
                    val cx = size.width / 2f
                    val cy = size.height / 2f
                    // Concentric gyro rings
                    drawCircle(
                        color = NeonPalette.Cyan.copy(alpha = 0.4f),
                        radius = 9.dp.toPx(),
                        center = Offset(cx, cy),
                        style = Stroke(width = 1.4f.dp.toPx())
                    )
                    drawCircle(
                        color = NeonPalette.Cyan,
                        radius = 5.5f.dp.toPx(),
                        center = Offset(cx, cy),
                        style = Stroke(width = 1.4f.dp.toPx())
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 2.dp.toPx(),
                        center = Offset(cx, cy)
                    )
                    // Cardinal tick axes
                    drawLine(
                        color = NeonPalette.Cyan,
                        start = Offset(cx, cy - 9.dp.toPx()),
                        end = Offset(cx, cy - 6.5f.dp.toPx()),
                        strokeWidth = 1.6f.dp.toPx()
                    )
                    drawLine(
                        color = NeonPalette.Cyan,
                        start = Offset(cx, cy + 6.5f.dp.toPx()),
                        end = Offset(cx, cy + 9.dp.toPx()),
                        strokeWidth = 1.6f.dp.toPx()
                    )
                    drawLine(
                        color = NeonPalette.Cyan,
                        start = Offset(cx - 9.dp.toPx(), cy),
                        end = Offset(cx - 6.5f.dp.toPx(), cy),
                        strokeWidth = 1.6f.dp.toPx()
                    )
                    drawLine(
                        color = NeonPalette.Cyan,
                        start = Offset(cx + 6.5f.dp.toPx(), cy),
                        end = Offset(cx + 9.dp.toPx(), cy),
                        strokeWidth = 1.6f.dp.toPx()
                    )
                }
            },
            onReset = { onSettingsChange(settings.copy(inputMode = InputMode.VELOCITY)) },
            resetTooltip = "Reset Motion Mode to Gyro Precision Aiming"
        ) {
            Text(
                "Choose your motion emulation engine. Transforms smartphone gyroscope, accelerometer, and gravity telemetry into specialized controller inputs.",
                fontSize = 12.sp,
                color = NeonPalette.CardIdleText
            )
            Spacer(modifier = Modifier.height(4.dp))
            InputModeCards(
                currentSettings = settings,
                onSettingsChange = onSettingsChange
            )
        }

        // ── 2. Contextual Tuning Per Mode ──
        when (settings.inputMode) {
            InputMode.ABSOLUTE_TILT -> {
                SteeringWheelDeepSettings(
                    settings = settings,
                    onSettingsChange = onSettingsChange
                )
            }

            InputMode.AIR_MOUSE -> {
                FlightStickDeepSettings(
                    settings = settings,
                    onSettingsChange = onSettingsChange
                )
            }

            InputMode.GESTURE -> {
                // ── Card 1: Directional Kinetic Flicks (Up, Down, Left, Right) ──
                ModernSectionCard(
                    title = "Directional Kinetic Flicks",
                    icon = Icons.Default.SportsEsports,
                    onReset = {
                        onSettingsChange(
                            settings.copy(
                                gestureFlicksEnabled = true,
                                gestureFlickUpAction = GestureButton.BTN_X,
                                gestureFlickDownAction = GestureButton.BTN_Y,
                                gestureFlickLeftAction = GestureButton.SNAP_TURN_180,
                                gestureFlickRightAction = GestureButton.TRIGGER_RT,
                                gestureFlickThreshold = 8.5f,
                                gestureFlickCooldownMs = 350L,
                                gestureThreshold = 8.5f,
                                gestureCooldownMs = 400L,
                                invertX = false
                            )
                        )
                    },
                    resetTooltip = "Reset Flicks to Defaults"
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Fast Directional Flicks", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
                            Text("Sharp physical flicks in 4 directions trigger individual controller buttons", fontSize = 11.sp, color = NeonPalette.CardIdleText)
                        }
                        Switch(
                            checked = settings.gestureFlicksEnabled,
                            onCheckedChange = { onSettingsChange(settings.copy(gestureFlicksEnabled = it)) }
                        )
                    }

                    if (settings.gestureFlicksEnabled) {
                        Spacer(modifier = Modifier.height(6.dp))

                        GestureButtonDropdown(
                            label = "⬆️ Fast Flick UP:",
                            selectedButton = settings.gestureFlickUpAction,
                            onSelect = { onSettingsChange(settings.copy(gestureFlickUpAction = it)) }
                        )

                        GestureButtonDropdown(
                            label = "⬇️ Fast Flick DOWN:",
                            selectedButton = settings.gestureFlickDownAction,
                            onSelect = { onSettingsChange(settings.copy(gestureFlickDownAction = it)) }
                        )

                        GestureButtonDropdown(
                            label = "⬅️ Fast Flick LEFT:",
                            selectedButton = settings.gestureFlickLeftAction,
                            onSelect = { onSettingsChange(settings.copy(gestureFlickLeftAction = it)) }
                        )

                        GestureButtonDropdown(
                            label = "➡️ Fast Flick RIGHT:",
                            selectedButton = settings.gestureFlickRightAction,
                            onSelect = { onSettingsChange(settings.copy(gestureFlickRightAction = it)) }
                        )

                        Spacer(modifier = Modifier.height(4.dp))
                        ModernSlider(
                            label = "Flick Trigger Force Threshold",
                            value = settings.gestureFlickThreshold,
                            valueRange = 4.0f..20.0f,
                            format = "%.1f m/s²",
                            onValueChange = { onSettingsChange(settings.copy(gestureFlickThreshold = it, gestureThreshold = it)) }
                        )

                        ModernSlider(
                            label = "Flick Debounce Cooldown",
                            value = settings.gestureFlickCooldownMs.toFloat(),
                            valueRange = 150.0f..800.0f,
                            format = "%.0f ms",
                            onValueChange = { onSettingsChange(settings.copy(gestureFlickCooldownMs = it.toLong(), gestureCooldownMs = it.toLong())) }
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Invert Flick / Snap Direction", fontSize = 13.sp, color = Color.White)
                            Switch(
                                checked = settings.invertX,
                                onCheckedChange = { onSettingsChange(settings.copy(invertX = it)) }
                            )
                        }
                    }
                }

                // ── Card 2: Gravity Tilt Posture (Angle Trigger) ──
                ModernSectionCard(
                    title = "Gravity Tilt Posture (Angle Trigger)",
                    icon = Icons.Default.SportsEsports,
                    onReset = {
                        onSettingsChange(
                            settings.copy(
                                gestureTiltEnabled = false,
                                gestureTiltPitchThreshold = 30.0f,
                                gestureTiltRollThreshold = 30.0f,
                                gestureTiltUpAction = GestureButton.BTN_X,
                                gestureTiltDownAction = GestureButton.BTN_Y,
                                gestureTiltLeftAction = GestureButton.BTN_LB,
                                gestureTiltRightAction = GestureButton.BTN_RB,
                                gestureTiltHoldMode = false,
                                gestureTiltAngleThreshold = 30.0f,
                                gestureTiltAction = GestureButton.BTN_X
                            )
                        )
                    },
                    resetTooltip = "Reset Tilt Posture to Defaults"
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Tilt Posture Triggers (Up, Down, Left, Right)", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
                            Text("Triggers buttons when phone is tilted past specified angles (tactical lean, raise/lower phone)", fontSize = 11.sp, color = NeonPalette.CardIdleText)
                        }
                        Switch(
                            checked = settings.gestureTiltEnabled,
                            onCheckedChange = { onSettingsChange(settings.copy(gestureTiltEnabled = it)) }
                        )
                    }

                    if (settings.gestureTiltEnabled) {
                        Spacer(modifier = Modifier.height(6.dp))

                        GestureButtonDropdown(
                            label = "⬆️ Tilt UP (Raise Phone):",
                            selectedButton = settings.gestureTiltUpAction,
                            onSelect = { onSettingsChange(settings.copy(gestureTiltUpAction = it, gestureTiltAction = it)) }
                        )

                        GestureButtonDropdown(
                            label = "⬇️ Tilt DOWN (Lower Phone):",
                            selectedButton = settings.gestureTiltDownAction,
                            onSelect = { onSettingsChange(settings.copy(gestureTiltDownAction = it)) }
                        )

                        GestureButtonDropdown(
                            label = "⬅️ Tilt LEFT (Bank Left / Lean):",
                            selectedButton = settings.gestureTiltLeftAction,
                            onSelect = { onSettingsChange(settings.copy(gestureTiltLeftAction = it)) }
                        )

                        GestureButtonDropdown(
                            label = "➡️ Tilt RIGHT (Bank Right / Lean):",
                            selectedButton = settings.gestureTiltRightAction,
                            onSelect = { onSettingsChange(settings.copy(gestureTiltRightAction = it)) }
                        )

                        Spacer(modifier = Modifier.height(4.dp))
                        ModernSlider(
                            label = "Pitch Tilt Angle (Up / Down)",
                            value = settings.gestureTiltPitchThreshold,
                            valueRange = 10.0f..80.0f,
                            format = "%.0f°",
                            onValueChange = { onSettingsChange(settings.copy(gestureTiltPitchThreshold = it, gestureTiltAngleThreshold = it)) }
                        )

                        ModernSlider(
                            label = "Roll Tilt Angle (Left / Right)",
                            value = settings.gestureTiltRollThreshold,
                            valueRange = 10.0f..80.0f,
                            format = "%.0f°",
                            onValueChange = { onSettingsChange(settings.copy(gestureTiltRollThreshold = it)) }
                        )

                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Trigger Behavior:", fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = Color.White)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(
                                selected = !settings.gestureTiltHoldMode,
                                onClick = { onSettingsChange(settings.copy(gestureTiltHoldMode = false)) },
                                label = { Text("Pulse on Tilt (Single Press)", fontSize = 11.sp) }
                            )
                            FilterChip(
                                selected = settings.gestureTiltHoldMode,
                                onClick = { onSettingsChange(settings.copy(gestureTiltHoldMode = true)) },
                                label = { Text("Hold while Tilted (Lean / Block)", fontSize = 11.sp) }
                            )
                        }
                    }
                }

                // ── Card 3: Shake Motion Detection ──
                ModernSectionCard(
                    title = "Shake Motion Detection (Vigorous Shaking)",
                    icon = Icons.Default.SportsEsports,
                    onReset = {
                        onSettingsChange(
                            settings.copy(
                                gestureShakeEnabled = false,
                                gestureShakeThreshold = 11.0f,
                                gestureShakeAction = GestureButton.BTN_X,
                                gestureShakeCooldownMs = 450L
                            )
                        )
                    },
                    resetTooltip = "Reset Shake Motion to Defaults"
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Vigorous Phone Shaking", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
                            Text("Detects rapid back-and-forth phone shaking (recharge flashlight, grab escape, reload)", fontSize = 11.sp, color = NeonPalette.CardIdleText)
                        }
                        Switch(
                            checked = settings.gestureShakeEnabled,
                            onCheckedChange = { onSettingsChange(settings.copy(gestureShakeEnabled = it)) }
                        )
                    }

                    if (settings.gestureShakeEnabled) {
                        Spacer(modifier = Modifier.height(6.dp))

                        GestureButtonDropdown(
                            label = "📳 Triggered Button / Action:",
                            selectedButton = settings.gestureShakeAction,
                            onSelect = { onSettingsChange(settings.copy(gestureShakeAction = it)) }
                        )

                        Spacer(modifier = Modifier.height(4.dp))
                        ModernSlider(
                            label = "Shake Sensitivity Threshold",
                            value = settings.gestureShakeThreshold,
                            valueRange = 6.0f..22.0f,
                            format = "%.1f m/s²",
                            onValueChange = { onSettingsChange(settings.copy(gestureShakeThreshold = it)) }
                        )

                        ModernSlider(
                            label = "Shake Debounce Cooldown",
                            value = settings.gestureShakeCooldownMs.toFloat(),
                            valueRange = 200.0f..1000.0f,
                            format = "%.0f ms",
                            onValueChange = { onSettingsChange(settings.copy(gestureShakeCooldownMs = it.toLong())) }
                        )
                    }
                }

                // ── Card 4: Joystick Gyro Settings ──
                ModernSectionCard(
                    title = "Joystick Gyro Settings",
                    icon = Icons.Default.Speed,
                    onReset = {
                        onSettingsChange(
                            settings.copy(
                                gestureJoystickMode = GestureJoystickMode.OFF,
                                gestureAimingEnabled = false
                            )
                        )
                    },
                    resetTooltip = "Reset Joystick Mode to Default (Off)"
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Attach Gyro to Joystick", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
                            Text(
                                if (settings.effectiveGestureJoystickMode != GestureJoystickMode.OFF)
                                    "Active • ${settings.effectiveGestureJoystickMode.displayName} attached to joysticks"
                                else
                                    "Off • Gyro is completely detached from joysticks. Physical gamepad inputs pass through 100%",
                                fontSize = 11.sp,
                                color = if (settings.effectiveGestureJoystickMode != GestureJoystickMode.OFF) NeonPalette.Cyan else NeonPalette.CardIdleText
                            )
                        }
                        Switch(
                            checked = settings.effectiveGestureJoystickMode != GestureJoystickMode.OFF,
                            onCheckedChange = { isChecked ->
                                val newMode = if (isChecked) {
                                    if (settings.gestureJoystickMode != GestureJoystickMode.OFF) settings.gestureJoystickMode
                                    else GestureJoystickMode.GYRO_AIMING
                                } else {
                                    GestureJoystickMode.OFF
                                }
                                onSettingsChange(
                                    settings.copy(
                                        gestureJoystickMode = newMode,
                                        gestureAimingEnabled = isChecked
                                    )
                                )
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    GestureJoystickModeDropdown(
                        selectedMode = settings.effectiveGestureJoystickMode,
                        onSelect = { newMode ->
                            onSettingsChange(
                                settings.copy(
                                    gestureJoystickMode = newMode,
                                    gestureAimingEnabled = (newMode != GestureJoystickMode.OFF)
                                )
                            )
                        }
                    )

                    if (settings.effectiveGestureJoystickMode == GestureJoystickMode.OFF) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.35f),
                            border = BorderStroke(1.dp, NeonPalette.CardIdleBorder)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    "🛑 Gyro Completely Detached from Joysticks",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = NeonPalette.Cyan
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    "Joysticks operate with 100% purity from physical gamepad inputs. Phone flicks, gravity tilts, and shakes trigger buttons with ZERO joystick interference.",
                                    fontSize = 11.sp,
                                    color = NeonPalette.CardIdleText
                                )
                            }
                        }
                    }
                }

                // Render Deep Settings for the selected joystick motion mode
                when (settings.effectiveGestureJoystickMode) {
                    GestureJoystickMode.OFF -> { /* Detached status confirmed in Card 4 */ }
                    GestureJoystickMode.GYRO_AIMING -> {
                        GyroAimingDeepSettings(settings, onSettingsChange)
                    }
                    GestureJoystickMode.FLIGHT_STICK -> {
                        FlightStickDeepSettings(settings, onSettingsChange)
                    }
                    GestureJoystickMode.RAW_SILICON -> {
                        RawSiliconDeepSettings(settings, onSettingsChange)
                    }
                }
            }

            InputMode.RAW_UNCALIBRATED -> {
                RawSiliconDeepSettings(settings, onSettingsChange)
            }

            else -> {
                // VELOCITY: Standard calibrated gyroscope
                GyroAimingDeepSettings(settings, onSettingsChange)
            }
        }

        // ── 4. Activation Trigger (ADS / Button Hold) ──
        ModernSectionCard(
            title = "Activation Trigger (Aim-Down-Sights)",
            icon = Icons.Default.SportsEsports,
            onReset = {
                onSettingsChange(settings.copy(activationButtons = emptySet()))
            },
            resetTooltip = "Reset Activation Trigger to Always Active"
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Require Button Hold", fontSize = 13.sp, color = Color.White)
                    Text("Only active while holding aim trigger (e.g. LT / Left Trigger)", fontSize = 11.sp, color = NeonPalette.CardIdleText)
                }
                Switch(
                    checked = settings.activationButtons.isNotEmpty(),
                    onCheckedChange = {
                        if (it) onSettingsChange(settings.copy(activationButtons = setOf(NexpadKeys.LT)))
                        else onSettingsChange(settings.copy(activationButtons = emptySet()))
                    }
                )
            }

            if (settings.activationButtons.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                val buttons = listOf(
                    NexpadKeys.LT, NexpadKeys.RT,
                    NexpadKeys.LB, NexpadKeys.RB,
                    NexpadKeys.A, NexpadKeys.B,
                    NexpadKeys.X, NexpadKeys.Y
                )
                @OptIn(ExperimentalLayoutApi::class)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    buttons.forEach { btn ->
                        FilterChip(
                            selected = settings.activationButtons.contains(btn),
                            onClick = {
                                val current = settings.activationButtons.toMutableSet()
                                if (current.contains(btn)) current.remove(btn) else current.add(btn)
                                if (current.isEmpty()) onSettingsChange(settings.copy(activationButtons = emptySet()))
                                else onSettingsChange(settings.copy(activationButtons = current))
                            },
                            label = { Text(btn) }
                        )
                    }
                }
            }
        }

        // ── 5. Recalibration Action ──
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f),
            border = BorderStroke(1.dp, NeonPalette.CardIdleBorder)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Sensor Zero Calibration", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
                    Text("Zeros out current resting tilt and resets drift filter baselines.", fontSize = 12.sp, color = NeonPalette.CardIdleText)
                }
                Button(
                    onClick = onRecalibrate,
                    colors = ButtonDefaults.buttonColors(containerColor = NeonPalette.Cyan.copy(alpha = 0.2f), contentColor = NeonPalette.Cyan)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Recalibrate Now")
                }
            }
        }
    }
}

/**
 * Modern Material 3 Section Card container with optional top-right corner reset action.
 */
@Composable
fun ModernSectionCard(
    title: String,
    icon: ImageVector? = null,
    customIcon: (@Composable () -> Unit)? = null,
    modifier: Modifier = Modifier,
    onReset: (() -> Unit)? = null,
    resetTooltip: String = "Reset to Default",
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        border = BorderStroke(1.dp, NeonPalette.CardIdleBorder.copy(alpha = 0.6f))
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    if (customIcon != null) {
                        customIcon()
                    } else if (icon != null) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = NeonPalette.Cyan,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Text(
                        text = title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color.White
                    )
                }

                if (onReset != null) {
                    OutlinedButton(
                        onClick = onReset,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, NeonPalette.Cyan.copy(alpha = 0.45f)),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = NeonPalette.Cyan.copy(alpha = 0.08f),
                            contentColor = NeonPalette.Cyan
                        ),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = resetTooltip,
                            modifier = Modifier.size(13.dp),
                            tint = NeonPalette.Cyan
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Reset",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = NeonPalette.Cyan
                        )
                    }
                }
            }
            content()
        }
    }
}

/**
 * Modern Material 3 Slider with glow badge readout.
 */
@Composable
fun ModernSlider(
    label: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    format: String = "%.1f",
    displayValue: String? = null,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = NeonPalette.Cyan.copy(alpha = 0.12f),
                border = BorderStroke(1.dp, NeonPalette.Cyan.copy(alpha = 0.3f))
            ) {
                Text(
                    text = displayValue ?: String.format(format, value),
                    color = NeonPalette.Cyan,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            colors = SliderDefaults.colors(
                thumbColor = NeonPalette.Cyan,
                activeTrackColor = NeonPalette.Cyan,
                inactiveTrackColor = NeonPalette.CardIdleBorder
            )
        )
    }
}

/**
 * Backward compatibility wrapper for [XboxMotionSettingsSection].
 */
@Composable
fun GyroSettingsSection(
    settings: GyroSettings,
    onSettingsChange: (GyroSettings) -> Unit,
    activeController: ControllerType,
    rawGyroX: Float,
    rawGyroY: Float,
    rawGyroZ: Float,
    rawAccelX: Float,
    rawAccelY: Float,
    rawAccelZ: Float,
    processedYaw: Float,
    processedPitch: Float,
    onRecalibrate: () -> Unit
) {
    if (activeController == ControllerType.XBOX_360) {
        XboxMotionSettingsSection(
            settings = settings,
            onSettingsChange = onSettingsChange,
            rawGyroX = rawGyroX,
            rawGyroY = rawGyroY,
            rawGyroZ = rawGyroZ,
            rawAccelX = rawAccelX,
            rawAccelY = rawAccelY,
            rawAccelZ = rawAccelZ,
            processedYaw = processedYaw,
            processedPitch = processedPitch,
            onRecalibrate = onRecalibrate
        )
    }
}

/**
 * Modern individual axis mapping card for 3D Flight Stick controls.
 * Allows independent selection of:
 * - Assigned joystick (Left Stick LS, Right Stick RS, or Disabled)
 * - Axis direction (Up/Down [Y] or Left/Right [X])
 * - Axis inversion switch (Normal vs Inverted)
 * - Max Lock Angle slider (Sensitivity in degrees for 100% deflection)
 */
@Composable
private fun FlightAxisCard(
    axisTitle: String,
    icon: String,
    description: String,
    currentTarget: FlightStickAxisTarget,
    currentInvert: Boolean,
    maxAngle: Float,
    onTargetChange: (FlightStickAxisTarget) -> Unit,
    onInvertChange: (Boolean) -> Unit,
    onMaxAngleChange: (Float) -> Unit,
    onReset: (() -> Unit)? = null,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.4f),
        border = BorderStroke(1.dp, NeonPalette.CardIdleBorder.copy(alpha = 0.7f))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    Text(icon, fontSize = 20.sp)
                    Column {
                        Text(axisTitle, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
                        Text(description, fontSize = 11.sp, color = NeonPalette.CardIdleText)
                    }
                }

                if (onReset != null) {
                    OutlinedButton(
                        onClick = onReset,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, NeonPalette.Cyan.copy(alpha = 0.4f)),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = NeonPalette.Cyan.copy(alpha = 0.08f),
                            contentColor = NeonPalette.Cyan
                        ),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.height(26.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reset Axis",
                            modifier = Modifier.size(12.dp),
                            tint = NeonPalette.Cyan
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Reset",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = NeonPalette.Cyan
                        )
                    }
                }
            }

            // 1. Joystick Choice (LS / RS / Off)
            Text("Assigned Joystick:", fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = Color.White)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FlightStickChoice.entries.forEach { choice ->
                    val isSelected = currentTarget.stick == choice
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            val newTarget = FlightStickAxisTarget.from(choice, currentTarget.direction)
                            onTargetChange(newTarget)
                        },
                        label = { Text(choice.displayName, fontSize = 11.sp) }
                    )
                }
            }

            // 2. Direction Assignment (Up/Down [Y] vs Left/Right [X])
            if (currentTarget.stick != FlightStickChoice.DISABLED) {
                Text("Stick Direction / Axis:", fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = Color.White)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FlightStickDirection.entries.forEach { dir ->
                        val isSelected = currentTarget.direction == dir
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                val newTarget = FlightStickAxisTarget.from(currentTarget.stick, dir)
                                onTargetChange(newTarget)
                            },
                            label = { Text(dir.displayName, fontSize = 11.sp) }
                        )
                    }
                }

                // 3. Direction Inversion
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Invert Direction", fontSize = 12.sp, color = Color.White)
                        Text(
                            if (currentInvert) "Inverted (Reversed stick deflection)" else "Normal (Direct stick deflection)",
                            fontSize = 10.sp,
                            color = NeonPalette.CardIdleText
                        )
                    }
                    Switch(
                        checked = currentInvert,
                        onCheckedChange = onInvertChange
                    )
                }

                // 4. Max Lock Angle Slider
                ModernSlider(
                    label = "Max Lock Angle (100% Deflection)",
                    value = maxAngle.coerceIn(1.0f, 179.0f),
                    valueRange = 1.0f..179.0f,
                    format = "%.0f°",
                    onValueChange = onMaxAngleChange
                )
            }
        }
    }
}

/**
 * Modern Material 3 Dropdown selector for mapping controller actions to gestures.
 */
@Composable
fun GestureButtonDropdown(
    label: String,
    selectedButton: GestureButton,
    onSelect: (GestureButton) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 13.sp, color = Color.White, fontWeight = FontWeight.Medium)
        Box {
            OutlinedButton(
                onClick = { expanded = true },
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, NeonPalette.Cyan.copy(alpha = 0.5f)),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = NeonPalette.Cyan.copy(alpha = 0.1f),
                    contentColor = NeonPalette.Cyan
                ),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = selectedButton.displayName,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("▼", fontSize = 9.sp)
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                GestureButton.entries.forEach { btn ->
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    btn.displayName,
                                    fontSize = 12.sp,
                                    fontWeight = if (btn == selectedButton) FontWeight.Bold else FontWeight.Normal,
                                    color = if (btn == selectedButton) NeonPalette.Cyan else Color.White
                                )
                                if (btn == selectedButton) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("✓", fontSize = 12.sp, color = NeonPalette.Cyan)
                                }
                            }
                        },
                        onClick = {
                            onSelect(btn)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

/**
 * Modern Material 3 Dropdown selector for mapping joystick gyro modes in Combat Gestures.
 */
@Composable
fun GestureJoystickModeDropdown(
    selectedMode: GestureJoystickMode,
    onSelect: (GestureJoystickMode) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text("Joystick Gyro Mode:", fontSize = 13.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
            Text(selectedMode.description, fontSize = 11.sp, color = NeonPalette.CardIdleText)
        }
        Spacer(modifier = Modifier.width(8.dp))
        Box {
            OutlinedButton(
                onClick = { expanded = true },
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, NeonPalette.Cyan.copy(alpha = 0.5f)),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = NeonPalette.Cyan.copy(alpha = 0.1f),
                    contentColor = NeonPalette.Cyan
                ),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = selectedMode.displayName,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("▼", fontSize = 9.sp)
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                GestureJoystickMode.entries.forEach { mode ->
                    DropdownMenuItem(
                        text = {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        mode.displayName,
                                        fontSize = 12.sp,
                                        fontWeight = if (mode == selectedMode) FontWeight.Bold else FontWeight.Normal,
                                        color = if (mode == selectedMode) NeonPalette.Cyan else Color.White
                                    )
                                    if (mode == selectedMode) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("✓", fontSize = 12.sp, color = NeonPalette.Cyan)
                                    }
                                }
                                Text(mode.description, fontSize = 10.sp, color = NeonPalette.CardIdleText)
                            }
                        },
                        onClick = {
                            onSelect(mode)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

/**
 * Deep tuning parameters for Steering Wheel mode (Gravity Vector).
 */
@Composable
fun SteeringWheelDeepSettings(
    settings: GyroSettings,
    onSettingsChange: (GyroSettings) -> Unit
) {
    ModernSectionCard(
        title = "Steering Wheel Parameters (Gravity Vector)",
        icon = Icons.Default.DirectionsCar,
        onReset = {
            onSettingsChange(
                settings.copy(
                    absoluteMaxTilt = 35.0f,
                    absoluteSensitivityX = 1.0f,
                    absoluteSensitivityY = 1.0f,
                    absoluteCurve = 1.0f,
                    invertX = false,
                    xboxTargetStick = XboxTargetStick.LEFT_STICK
                )
            )
        },
        resetTooltip = "Reset Steering Wheel to Defaults"
    ) {
        Text(
            "Uses Earth's filtered gravity vector to eliminate 100% of steering jitter caused by pressing buttons or triggers.",
            fontSize = 12.sp,
            color = NeonPalette.CardIdleText
        )
        Spacer(modifier = Modifier.height(6.dp))

        ModernSlider(
            label = "Max Steering Lock Angle",
            value = settings.absoluteMaxTilt.coerceIn(1.0f, 179.0f),
            valueRange = 1.0f..179.0f,
            format = "%.0f°",
            onValueChange = { onSettingsChange(settings.copy(absoluteMaxTilt = it)) }
        )

        ModernSlider(
            label = "Steering Sensitivity",
            value = settings.absoluteSensitivityX,
            valueRange = 0.1f..3.0f,
            format = "%.2f×",
            onValueChange = { onSettingsChange(settings.copy(absoluteSensitivityX = it)) }
        )

        ModernSlider(
            label = "Response Curve (Center Stability)",
            value = settings.absoluteCurve,
            valueRange = 1.0f..3.0f,
            format = "%.1f",
            onValueChange = { onSettingsChange(settings.copy(absoluteCurve = it)) }
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Invert Steering Direction", fontSize = 13.sp, color = Color.White)
            Switch(
                checked = settings.invertX,
                onCheckedChange = { onSettingsChange(settings.copy(invertX = it)) }
            )
        }

        Spacer(modifier = Modifier.height(4.dp))
        Text("Target Analog Stick", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(
                selected = settings.xboxTargetStick == XboxTargetStick.LEFT_STICK,
                onClick = { onSettingsChange(settings.copy(xboxTargetStick = XboxTargetStick.LEFT_STICK)) }
            )
            Text("Left Stick (Standard Steering)", fontSize = 13.sp, color = Color.White)
            Spacer(modifier = Modifier.width(16.dp))
            RadioButton(
                selected = settings.xboxTargetStick == XboxTargetStick.RIGHT_STICK,
                onClick = { onSettingsChange(settings.copy(xboxTargetStick = XboxTargetStick.RIGHT_STICK)) }
            )
            Text("Right Stick", fontSize = 13.sp, color = Color.White)
        }
    }
}

/**
 * Deep tuning parameters for 3D Flight Stick & Aviation Yoke mode.
 */
@Composable
fun FlightStickDeepSettings(
    settings: GyroSettings,
    onSettingsChange: (GyroSettings) -> Unit
) {
    ModernSectionCard(
        title = "3D Flight Stick & Aviation Yoke (Independent Axis Mapping)",
        icon = Icons.Default.SportsEsports,
        onReset = {
            onSettingsChange(
                settings.copy(
                    airMousePitchTarget = FlightStickAxisTarget.LS_Y,
                    airMousePitchInvert = true,
                    airMouseMaxPitch = 35.0f,
                    airMouseRollTarget = FlightStickAxisTarget.LS_X,
                    airMouseRollInvert = false,
                    airMouseMaxRoll = 35.0f,
                    airMouseMaxRollYaw = 35.0f,
                    airMouseYawTarget = FlightStickAxisTarget.NONE,
                    airMouseYawInvert = false,
                    airMouseMaxYaw = 35.0f,
                    airMouseDeadzone = 1.5f,
                    airMouseCurve = 1.3f
                )
            )
        },
        resetTooltip = "Reset All Flight Stick Settings to Defaults"
    ) {
        Text(
            "Independent joystick assignment for Pitch, Roll, and Yaw! Map any rotation axis to Left Stick (LS) or Right Stick (RS), choose direction (Up/Down or Left/Right), and configure direction inversion.",
            fontSize = 12.sp,
            color = NeonPalette.CardIdleText
        )
        Spacer(modifier = Modifier.height(4.dp))

        // ── 1. Pitch Axis (Tilt Forward / Backward) ──
        FlightAxisCard(
            axisTitle = "Pitch Axis (Tilt Forward / Backward)",
            icon = "📐",
            description = "Nose up/down. Standard: Left Stick Up/Down (Y), inverted pull-back.",
            currentTarget = settings.airMousePitchTarget,
            currentInvert = settings.airMousePitchInvert,
            maxAngle = settings.airMouseMaxPitch,
            onTargetChange = { onSettingsChange(settings.copy(airMousePitchTarget = it)) },
            onInvertChange = { onSettingsChange(settings.copy(airMousePitchInvert = it)) },
            onMaxAngleChange = { onSettingsChange(settings.copy(airMouseMaxPitch = it)) },
            onReset = {
                onSettingsChange(
                    settings.copy(
                        airMousePitchTarget = FlightStickAxisTarget.LS_Y,
                        airMousePitchInvert = true,
                        airMouseMaxPitch = 35.0f
                    )
                )
            }
        )

        // ── 2. Roll Axis (Tilt Sideways / Wing Bank) ──
        FlightAxisCard(
            axisTitle = "Roll Axis (Tilt Sideways / Wing Bank)",
            icon = "🔄",
            description = "Ailerons / wing banking turns. Standard: Left Stick Left/Right (X).",
            currentTarget = settings.airMouseRollTarget,
            currentInvert = settings.airMouseRollInvert,
            maxAngle = settings.airMouseMaxRoll,
            onTargetChange = { onSettingsChange(settings.copy(airMouseRollTarget = it)) },
            onInvertChange = { onSettingsChange(settings.copy(airMouseRollInvert = it)) },
            onMaxAngleChange = { onSettingsChange(settings.copy(airMouseMaxRoll = it, airMouseMaxRollYaw = it)) },
            onReset = {
                onSettingsChange(
                    settings.copy(
                        airMouseRollTarget = FlightStickAxisTarget.LS_X,
                        airMouseRollInvert = false,
                        airMouseMaxRoll = 35.0f,
                        airMouseMaxRollYaw = 35.0f
                    )
                )
            }
        )

        // ── 3. Yaw Axis (Flat Wrist Turn / Rudder) ──
        FlightAxisCard(
            axisTitle = "Yaw Axis (Flat Wrist Turn / Rudder)",
            icon = "🧭",
            description = "Rudder steering or camera turn. Default: Disabled (Off) / Optional: Right Stick (RS) or Left Stick (LS).",
            currentTarget = settings.airMouseYawTarget,
            currentInvert = settings.airMouseYawInvert,
            maxAngle = settings.airMouseMaxYaw,
            onTargetChange = { onSettingsChange(settings.copy(airMouseYawTarget = it)) },
            onInvertChange = { onSettingsChange(settings.copy(airMouseYawInvert = it)) },
            onMaxAngleChange = { onSettingsChange(settings.copy(airMouseMaxYaw = it)) },
            onReset = {
                onSettingsChange(
                    settings.copy(
                        airMouseYawTarget = FlightStickAxisTarget.NONE,
                        airMouseYawInvert = false,
                        airMouseMaxYaw = 35.0f
                    )
                )
            }
        )

        Spacer(modifier = Modifier.height(6.dp))
        Text("Common Flight Stability:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)

        ModernSlider(
            label = "Center Neutral Deadzone (Tremor Filter)",
            value = settings.airMouseDeadzone,
            valueRange = 0.0f..5.0f,
            format = "%.1f°",
            onValueChange = { onSettingsChange(settings.copy(airMouseDeadzone = it)) }
        )

        ModernSlider(
            label = "Center Response Curve (Stability Exponent)",
            value = settings.airMouseCurve,
            valueRange = 1.0f..3.0f,
            format = "%.1f",
            onValueChange = { onSettingsChange(settings.copy(airMouseCurve = it)) }
        )
    }
}

/**
 * Deep tuning parameters for 1:1 Gyro Aiming and Raw Silicon modes.
 */
@Composable
fun GyroAimingDeepSettings(
    settings: GyroSettings,
    onSettingsChange: (GyroSettings) -> Unit,
    isRawSilicon: Boolean = false
) {
    ModernSectionCard(
        title = if (isRawSilicon) "Raw Silicon Sensor Sensitivity (Esports Direct)" else "Aiming Speed & Sensitivity",
        icon = Icons.Default.Speed,
        onReset = {
            onSettingsChange(
                settings.copy(
                    maxDpsYaw = 200.0f,
                    maxDpsPitch = 200.0f,
                    maxDpsRoll = 200.0f,
                    horizontalAxis = HorizontalAxis.MIX,
                    invertX = false,
                    invertRoll = false,
                    invertY = false
                )
            )
        },
        resetTooltip = "Reset Sensitivity to Defaults (200 °/s, 1:1 Direct)"
    ) {
        if (isRawSilicon) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = NeonPalette.Cyan.copy(alpha = 0.12f),
                border = BorderStroke(1.dp, NeonPalette.Cyan.copy(alpha = 0.35f))
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("⚡", fontSize = 16.sp)
                    Text(
                        "Direct Hardware Sensor Stream: Unfiltered raw silicon angular velocity with bias compensation. Zero Android OS smoothing or drift suppression.",
                        fontSize = 11.sp,
                        color = Color.White
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
        }

        Text(
            "1:1 Linear gyro aiming. 200 °/s represents standard 1.00× sensitivity. Lower °/s increases camera turn speed, higher °/s slows it down.",
            fontSize = 12.sp,
            color = NeonPalette.CardIdleText
        )
        Spacer(modifier = Modifier.height(6.dp))

        val horizontalAxisLabel = when (settings.horizontalAxis) {
            HorizontalAxis.YAW -> "Horizontal Aim Speed (Yaw)"
            HorizontalAxis.ROLL -> "Horizontal Aim Speed (Roll)"
            HorizontalAxis.MIX -> "Horizontal Aim Speed (Yaw & Roll)"
        }
        val hSensMult = 200.0f / settings.maxDpsYaw
        val vSensMult = 200.0f / settings.maxDpsPitch
        val hSpeedLabel = when {
            hSensMult >= 1.7f -> "Fast"
            hSensMult <= 0.65f -> "Slow"
            else -> "Normal"
        }
        val vSpeedLabel = when {
            vSensMult >= 1.7f -> "Fast"
            vSensMult <= 0.65f -> "Slow"
            else -> "Normal"
        }

        ModernSlider(
            label = horizontalAxisLabel,
            value = settings.maxDpsYaw,
            valueRange = 50.0f..600.0f,
            format = "%.0f °/s",
            displayValue = "${settings.maxDpsYaw.toInt()} °/s (${String.format("%.2f", hSensMult)}× $hSpeedLabel)",
            onValueChange = { onSettingsChange(settings.copy(maxDpsYaw = it, maxDpsRoll = it)) }
        )

        ModernSlider(
            label = "Vertical Aim Speed (Pitch)",
            value = settings.maxDpsPitch,
            valueRange = 50.0f..600.0f,
            format = "%.0f °/s",
            displayValue = "${settings.maxDpsPitch.toInt()} °/s (${String.format("%.2f", vSensMult)}× $vSpeedLabel)",
            onValueChange = { onSettingsChange(settings.copy(maxDpsPitch = it)) }
        )

        Spacer(modifier = Modifier.height(4.dp))
        Text("Horizontal Rotation Axis", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
        Row(verticalAlignment = Alignment.CenterVertically) {
            HorizontalAxis.entries.forEach { axis ->
                RadioButton(
                    selected = settings.horizontalAxis == axis,
                    onClick = { onSettingsChange(settings.copy(horizontalAxis = axis)) }
                )
                Text(axis.displayName, fontSize = 12.sp, color = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
            }
        }

        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Invert Horizontal (Yaw)", fontSize = 13.sp, color = Color.White)
            Switch(checked = settings.invertX, onCheckedChange = { onSettingsChange(settings.copy(invertX = it)) })
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Invert Roll (Tilt / Bank)", fontSize = 13.sp, color = Color.White)
            Switch(checked = settings.invertRoll, onCheckedChange = { onSettingsChange(settings.copy(invertRoll = it)) })
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Invert Vertical (Pitch)", fontSize = 13.sp, color = Color.White)
            Switch(checked = settings.invertY, onCheckedChange = { onSettingsChange(settings.copy(invertY = it)) })
        }
    }

    ModernSectionCard(
        title = "Stick Response Curve & Acceleration",
        icon = Icons.Default.Speed,
        onReset = {
            onSettingsChange(
                settings.copy(
                    stickResponseCurve = StickResponseCurve.LINEAR,
                    accelerationEnabled = false,
                    maxSensitivity = 2.0f,
                    maxThreshold = 150.0f,
                    minThreshold = 0.0f
                )
            )
        },
        resetTooltip = "Reset Curve to Linear 1:1"
    ) {
        Text(
            "Translates physical gyro velocity into virtual analog stick deflection. Linear 1:1 is recommended so slow aiming and rapid turns feel consistent without unexpected exponential spikes.",
            fontSize = 12.sp,
            color = NeonPalette.CardIdleText
        )
        Spacer(modifier = Modifier.height(4.dp))

        Text("Stick Response Curve:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            StickResponseCurve.entries.forEach { curve ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onSettingsChange(settings.copy(stickResponseCurve = curve)) }
                ) {
                    RadioButton(
                        selected = settings.stickResponseCurve == curve,
                        onClick = { onSettingsChange(settings.copy(stickResponseCurve = curve)) }
                    )
                    Text(curve.displayName, fontSize = 12.sp, color = Color.White)
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Dynamic Gyro Acceleration", fontSize = 13.sp, color = Color.White)
                Text(
                    if (settings.accelerationEnabled) "Non-linear sensitivity scaling active (accelerates rapid flicks)"
                    else "Disabled (Pure Linear 1:1 — Slow and fast wrist turns scale with 100% predictability)",
                    fontSize = 11.sp,
                    color = if (settings.accelerationEnabled) NeonPalette.Cyan else NeonPalette.ConnectedDot
                )
            }
            Switch(
                checked = settings.accelerationEnabled,
                onCheckedChange = { onSettingsChange(settings.copy(accelerationEnabled = it)) }
            )
        }

        if (settings.accelerationEnabled) {
            ModernSlider(
                label = "Max Flick Multiplier",
                value = settings.maxSensitivity,
                valueRange = 1.0f..4.0f,
                format = "%.1f×",
                onValueChange = { onSettingsChange(settings.copy(maxSensitivity = it)) }
            )
            ModernSlider(
                label = "Acceleration Ramp Speed",
                value = settings.maxThreshold,
                valueRange = 50.0f..300.0f,
                format = "%.0f °/s",
                onValueChange = { onSettingsChange(settings.copy(maxThreshold = it)) }
            )
        }
    }

    ModernSectionCard(
        title = "Deadzone & Anti-Deadzone",
        icon = Icons.Default.SportsEsports,
        onReset = {
            onSettingsChange(
                settings.copy(
                    gameDeadzoneCompensation = 0.0f,
                    deadzoneThreshold = 0.8f,
                    tighteningEnabled = false,
                    tighteningThreshold = 10.0f,
                    lowSpeedAmplifierEnabled = false,
                    lowSpeedAmplifierAmount = 2.0f,
                    lowSpeedAmplifierThreshold = 20.0f
                )
            )
        },
        resetTooltip = "Reset Deadzones to Defaults"
    ) {
        ModernSlider(
            label = "In-Game Deadzone Bridge (Anti-Deadzone)",
            value = settings.gameDeadzoneCompensation,
            valueRange = 0.0f..0.30f,
            format = "%.2f",
            onValueChange = { onSettingsChange(settings.copy(gameDeadzoneCompensation = it)) }
        )
        Text(
            "Bypasses PC games' internal stick deadzones (typically 0.10–0.18). " +
            "Enables instant 1:1 micro-aiming matching DualShock 4 responsiveness.",
            fontSize = 11.sp,
            color = NeonPalette.CardIdleText
        )
        Spacer(modifier = Modifier.height(6.dp))

        ModernSlider(
            label = "Hardware Sensor Deadzone",
            value = settings.deadzoneThreshold,
            valueRange = 0.0f..10.0f,
            format = "%.1f °/s",
            onValueChange = { onSettingsChange(settings.copy(deadzoneThreshold = it)) }
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Soft Tightening Deadzone", fontSize = 13.sp, color = Color.White)
                Text("Smooth quadratic curve for microscopic adjustments", fontSize = 11.sp, color = NeonPalette.CardIdleText)
            }
            Switch(
                checked = settings.tighteningEnabled,
                onCheckedChange = { onSettingsChange(settings.copy(tighteningEnabled = it)) }
            )
        }

        if (settings.tighteningEnabled) {
            ModernSlider(
                label = "Tightening Threshold",
                value = settings.tighteningThreshold,
                valueRange = 1.0f..25.0f,
                format = "%.1f °/s",
                onValueChange = { onSettingsChange(settings.copy(tighteningThreshold = it)) }
            )
        }

        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Anti-Deadzone (Low-Speed Boost)", fontSize = 13.sp, color = Color.White)
                Text("Amplifies slow movements to overcome in-game stick deadzones", fontSize = 11.sp, color = NeonPalette.CardIdleText)
            }
            Switch(
                checked = settings.lowSpeedAmplifierEnabled,
                onCheckedChange = { onSettingsChange(settings.copy(lowSpeedAmplifierEnabled = it)) }
            )
        }

        if (settings.lowSpeedAmplifierEnabled) {
            ModernSlider(
                label = "Amplifier Multiplier",
                value = settings.lowSpeedAmplifierAmount,
                valueRange = 1.0f..5.0f,
                format = "%.1f×",
                onValueChange = { onSettingsChange(settings.copy(lowSpeedAmplifierAmount = it)) }
            )
            ModernSlider(
                label = "Amplifier Cutoff Speed",
                value = settings.lowSpeedAmplifierThreshold,
                valueRange = 5.0f..40.0f,
                format = "%.0f °/s",
                onValueChange = { onSettingsChange(settings.copy(lowSpeedAmplifierThreshold = it)) }
            )
        }
    }

    ModernSectionCard(
        title = "Smoothing & Responsiveness",
        icon = Icons.Default.Speed,
        onReset = {
            onSettingsChange(
                settings.copy(
                    smoothingEnabled = true,
                    smoothingAmount = 0.85f,
                    adaptiveSmoothing = true,
                    smoothingThreshold = 30.0f
                )
            )
        },
        resetTooltip = "Reset Smoothing to Crisp EMA (0.85)"
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Enable EMA Smoothing", fontSize = 13.sp, color = Color.White)
            Switch(
                checked = settings.smoothingEnabled,
                onCheckedChange = { onSettingsChange(settings.copy(smoothingEnabled = it)) }
            )
        }

        if (settings.smoothingEnabled) {
            ModernSlider(
                label = "Smoothing Blend (0.1 heavy - 1.0 raw)",
                value = settings.smoothingAmount,
                valueRange = 0.1f..1.0f,
                format = "%.2f",
                onValueChange = { onSettingsChange(settings.copy(smoothingAmount = it)) }
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Adaptive Smoothing", fontSize = 13.sp, color = Color.White)
                    Text("Automatically disables smoothing during fast flicks", fontSize = 11.sp, color = NeonPalette.CardIdleText)
                }
                Switch(
                    checked = settings.adaptiveSmoothing,
                    onCheckedChange = { onSettingsChange(settings.copy(adaptiveSmoothing = it)) }
                )
            }

            if (settings.adaptiveSmoothing) {
                ModernSlider(
                    label = "Flick Threshold Speed",
                    value = settings.smoothingThreshold,
                    valueRange = 10.0f..80.0f,
                    format = "%.0f °/s",
                    onValueChange = { onSettingsChange(settings.copy(smoothingThreshold = it)) }
                )
            }
        }
    }

    ModernSectionCard(
        title = "Stick Mapping & Blending",
        icon = Icons.Default.SportsEsports,
        onReset = {
            onSettingsChange(
                settings.copy(
                    xboxTargetStick = XboxTargetStick.RIGHT_STICK,
                    xboxBlendMode = XboxBlendMode.JOYSTICK_SUPREME
                )
            )
        },
        resetTooltip = "Reset Stick Mapping to Right Stick (Aiming)"
    ) {
        Text("Target Analog Stick:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            RadioButton(
                selected = settings.xboxTargetStick == XboxTargetStick.RIGHT_STICK,
                onClick = { onSettingsChange(settings.copy(xboxTargetStick = XboxTargetStick.RIGHT_STICK)) }
            )
            Text("Right Stick (Aiming)", fontSize = 12.sp, color = Color.White)

            RadioButton(
                selected = settings.xboxTargetStick == XboxTargetStick.LEFT_STICK,
                onClick = { onSettingsChange(settings.copy(xboxTargetStick = XboxTargetStick.LEFT_STICK)) }
            )
            Text("Left Stick", fontSize = 12.sp, color = Color.White)

            RadioButton(
                selected = settings.xboxTargetStick == XboxTargetStick.NONE,
                onClick = { onSettingsChange(settings.copy(xboxTargetStick = XboxTargetStick.NONE)) }
            )
            Text("Off (Detached)", fontSize = 12.sp, color = NeonPalette.Cyan)
        }

        Spacer(modifier = Modifier.height(6.dp))
        Text("Physical Stick Supremacy & Blending:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
        Text(
            "Controls priority between physical touch joystick and gyro motion. In 'Joystick Supreme', physical touches always override gyro.",
            fontSize = 11.sp,
            color = NeonPalette.CardIdleText
        )
        Spacer(modifier = Modifier.height(4.dp))
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            XboxBlendMode.entries.forEach { mode ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onSettingsChange(settings.copy(xboxBlendMode = mode)) }
                ) {
                    RadioButton(
                        selected = settings.xboxBlendMode == mode,
                        onClick = { onSettingsChange(settings.copy(xboxBlendMode = mode)) }
                    )
                    Text(mode.displayName, fontSize = 12.sp, color = Color.White)
                }
            }
        }
    }
}

/**
 * Deep tuning parameters for Esports Direct Raw Silicon Gyro.
 */
@Composable
fun RawSiliconDeepSettings(
    settings: GyroSettings,
    onSettingsChange: (GyroSettings) -> Unit
) {
    GyroAimingDeepSettings(
        settings = settings,
        onSettingsChange = onSettingsChange,
        isRawSilicon = true
    )
}

