package com.sanket.tools.nexpaddesktop.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sanket.tools.nexpaddesktop.model.*
import com.sanket.tools.nexpaddesktop.ui.visualizers.*
import com.sanket.tools.nexpaddesktop.ui.theme.NeonPalette

/**
 * Dedicated 6-Axis Motion Settings screen.
 *
 * Settings are conditionally shown based on the active controller type:
 * - **Xbox 360**: All sections visible (gyro → stick mapping, sensitivity, etc.)
 * - **PS4 (DualShock 4)**: Only shared sections visible (deadzone, smoothing, etc.)
 *   PS4 sensitivity is controlled by the PC game, not NEXPAD.
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
    val isXbox = activeController == ControllerType.XBOX_360

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Top,
    ) {
        // ── Settings Header ──
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("GYRO Active", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = NeonPalette.CardIdleText)
            Switch(
                checked = settings.enabled,
                onCheckedChange = { onSettingsChange(settings.copy(enabled = it)) },
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (!settings.enabled) {
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                "Gyro processing is disabled. Enable it above to configure settings.",
                color = Color.Gray,
                fontSize = 16.sp,
            )
            return@Column
        }

        // ════════════════════════════════════════════════════
        //  PS4 INFO BANNER (shown when PS4 controller is active)
        // ════════════════════════════════════════════════════
        if (!isXbox) {
            SectionCard("ℹ️ PS4 Controller Mode") {
                Text(
                    "You are using the DualShock 4 (PS4) controller. " +
                    "NEXPAD sends raw sensor data directly to the game — sensitivity " +
                    "and aiming speed are controlled by the PC game itself, not NEXPAD.\n\n" +
                    "The shared settings below (deadzone, smoothing, acceleration) " +
                    "affect the dashboard visualizer and any future processing features.",
                    fontSize = 13.sp,
                    color = Color.Gray,
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // ════════════════════════════════════════════════════
        //  DASHBOARD HEADER (3D Phone & Impact Meter) — SHARED
        // ════════════════════════════════════════════════════
        DashboardHeader(
            settings = settings,
            rawGyroX = rawGyroX,
            rawGyroY = rawGyroY,
            rawGyroZ = rawGyroZ,
            rawAccelX = rawAccelX,
            rawAccelY = rawAccelY,
            rawAccelZ = rawAccelZ,
            processedYaw = processedYaw,
            processedPitch = processedPitch
        )

        Spacer(modifier = Modifier.height(16.dp))

        // ════════════════════════════════════════════════════
        //  PRESET CARDS — SHARED
        // ════════════════════════════════════════════════════
        PresetCards(
            currentSettings = settings,
            onSettingsChange = onSettingsChange
        )

        Spacer(modifier = Modifier.height(16.dp))

        // ════════════════════════════════════════════════════
        //  INPUT MODE — XBOX ONLY
        //  (Xbox needs gyro→stick conversion; PS4 sends raw sensors)
        // ════════════════════════════════════════════════════
        if (isXbox) {
            SectionCard("🕹️ Input Mode (Xbox Only)") {
                InputModeCards(
                    currentSettings = settings,
                    onSettingsChange = onSettingsChange
                )

                if (settings.inputMode == InputMode.ABSOLUTE_TILT) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Steering Wheel Settings", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    LabeledSlider(
                        label = "Max Steering Angle",
                        value = settings.absoluteMaxTilt,
                        valueRange = 10.0f..90.0f,
                        format = "%.0f°",
                        onValueChange = { onSettingsChange(settings.copy(absoluteMaxTilt = it)) },
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LabeledSlider(
                        label = "Steering Sensitivity H",
                        value = settings.absoluteSensitivityX,
                        valueRange = 0.1f..3.0f,
                        format = "%.2f",
                        onValueChange = { onSettingsChange(settings.copy(absoluteSensitivityX = it)) },
                    )
                    LabeledSlider(
                        label = "Steering Sensitivity V",
                        value = settings.absoluteSensitivityY,
                        valueRange = 0.1f..3.0f,
                        format = "%.2f",
                        onValueChange = { onSettingsChange(settings.copy(absoluteSensitivityY = it)) },
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LabeledSlider(
                        label = "Response Curve",
                        value = settings.absoluteCurve,
                        valueRange = 1.0f..4.0f,
                        format = "%.1f",
                        onValueChange = { onSettingsChange(settings.copy(absoluteCurve = it)) },
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ════════════════════════════════════════════════════
            //  ACTIVATION BUTTONS — XBOX ONLY
            //  (PS4 games handle gyro activation themselves)
            // ════════════════════════════════════════════════════
            SectionCard("🔘 Activation (Xbox Only)") {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Require Button Hold to Activate", modifier = Modifier.weight(1f))
                    Switch(
                        checked = settings.activationButtons.isNotEmpty(),
                        onCheckedChange = { 
                            if (it) onSettingsChange(settings.copy(activationButtons = setOf("LT")))
                            else onSettingsChange(settings.copy(activationButtons = emptySet()))
                        },
                    )
                }
                if (settings.activationButtons.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    val buttons = listOf("LT", "RT", "LB", "RB", "A", "B", "X", "Y")
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

            Spacer(modifier = Modifier.height(16.dp))

            // ════════════════════════════════════════════════════
            //  XBOX GYRO-TO-STICK MAPPING — XBOX ONLY
            // ════════════════════════════════════════════════════
            SectionCard("🎮 Xbox Gyro → Stick Mapping") {
                Text(
                    "Xbox controllers don't have native gyro. Motion is mapped to analog stick movement.",
                    fontSize = 13.sp, color = Color.Gray,
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                Text("Target Stick:", fontWeight = FontWeight.Bold)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = settings.xboxTargetStick == XboxTargetStick.LEFT_STICK,
                        onClick = { onSettingsChange(settings.copy(xboxTargetStick = XboxTargetStick.LEFT_STICK)) }
                    )
                    Text("Left Stick", fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(16.dp))
                    RadioButton(
                        selected = settings.xboxTargetStick == XboxTargetStick.RIGHT_STICK,
                        onClick = { onSettingsChange(settings.copy(xboxTargetStick = XboxTargetStick.RIGHT_STICK)) }
                    )
                    Text("Right Stick", fontSize = 14.sp)
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                Text("Blend Mode:", fontWeight = FontWeight.Bold)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    XboxBlendMode.entries.forEach { mode ->
                        RadioButton(
                            selected = settings.xboxBlendMode == mode,
                            onClick = { onSettingsChange(settings.copy(xboxBlendMode = mode)) }
                        )
                        Text(mode.displayName, fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                    }
                }
                Text(
                    text = when(settings.xboxBlendMode) {
                        XboxBlendMode.OVERRIDE -> "Gyro overrides physical stick input completely."
                        XboxBlendMode.ADDITIVE -> "Gyro movement is added on top of physical stick input."
                        XboxBlendMode.MUTE_ON_STICK -> "Gyro is disabled whenever you touch the physical stick."
                    },
                    fontSize = 12.sp, color = Color.Gray, modifier = Modifier.padding(top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ════════════════════════════════════════════════════
            //  MAX ROTATION SPEED (Aiming Sensitivity) — XBOX ONLY
            // ════════════════════════════════════════════════════
            SectionCard("🎚️ Max Rotation Speed (Xbox Aiming)") {
                Text(
                    "Lower values mean higher sensitivity (you don't have to rotate as fast to hit 100% stick speed).",
                    fontSize = 12.sp, color = Color.Gray, modifier = Modifier.padding(bottom = 8.dp)
                )
                Column {
                    LabeledSlider(
                        label = "Max Yaw Speed (Left/Right)",
                        value = settings.maxDpsYaw,
                        valueRange = 50.0f..1000.0f,
                        format = "%.0f °/s",
                        onValueChange = { onSettingsChange(settings.copy(maxDpsYaw = it)) },
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    LabeledSlider(
                        label = "Max Pitch Speed (Up/Down)",
                        value = settings.maxDpsPitch,
                        valueRange = 50.0f..1000.0f,
                        format = "%.0f °/s",
                        onValueChange = { onSettingsChange(settings.copy(maxDpsPitch = it)) },
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    LabeledSlider(
                        label = "Max Roll Speed (Twist)",
                        value = settings.maxDpsRoll,
                        valueRange = 50.0f..1000.0f,
                        format = "%.0f °/s",
                        onValueChange = { onSettingsChange(settings.copy(maxDpsRoll = it)) },
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        } // end Xbox-only sections

        // ════════════════════════════════════════════════════
        //  PRECISION & DEADZONE (Crosshair Visualizer) — SHARED
        // ════════════════════════════════════════════════════
        SectionCard("🎯 Precision & Deadzone") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f).padding(end = 16.dp)) {
                    LabeledSlider(
                        label = "Hard Deadzone",
                        value = settings.deadzoneThreshold,
                        valueRange = 0.0f..20.0f,
                        format = "%.1f °/s",
                        onValueChange = { onSettingsChange(settings.copy(deadzoneThreshold = it)) },
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Enable Tightening (Soft Deadzone)", modifier = Modifier.weight(1f))
                        Switch(
                            checked = settings.tighteningEnabled,
                            onCheckedChange = { onSettingsChange(settings.copy(tighteningEnabled = it)) },
                        )
                    }
                    if (settings.tighteningEnabled) {
                        LabeledSlider(
                            label = "Tightening Threshold",
                            value = settings.tighteningThreshold,
                            valueRange = 1.0f..30.0f,
                            format = "%.1f °/s",
                            onValueChange = { onSettingsChange(settings.copy(tighteningThreshold = it)) },
                        )
                    }
                }
                
                DeadzoneCrosshair(
                    deadzone = settings.deadzoneThreshold,
                    tighteningEnabled = settings.tighteningEnabled,
                    tighteningThreshold = settings.tighteningThreshold,
                    rawYawDps = rawGyroZ * 57.296f,
                    rawPitchDps = rawGyroX * 57.296f,
                    processedYawDps = processedYaw,
                    processedPitchDps = processedPitch
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ════════════════════════════════════════════════════
        //  SMOOTHING (Waveform Visualizer) — SHARED
        // ════════════════════════════════════════════════════
        SectionCard("🌊 Smoothing") {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Enable Smoothing", modifier = Modifier.weight(1f))
                Switch(
                    checked = settings.smoothingEnabled,
                    onCheckedChange = { onSettingsChange(settings.copy(smoothingEnabled = it)) },
                )
            }
            
            if (settings.smoothingEnabled) {
                SmoothingWaveform(
                    smoothingEnabled = settings.smoothingEnabled,
                    smoothingAmount = settings.smoothingAmount
                )
                Spacer(modifier = Modifier.height(12.dp))
                LabeledSlider(
                    label = "Smoothing Amount",
                    value = settings.smoothingAmount,
                    valueRange = 0.1f..1.0f,
                    format = "%.2f",
                    onValueChange = { onSettingsChange(settings.copy(smoothingAmount = it)) },
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Adaptive Smoothing", modifier = Modifier.weight(1f))
                    Switch(
                        checked = settings.adaptiveSmoothing,
                        onCheckedChange = { onSettingsChange(settings.copy(adaptiveSmoothing = it)) },
                    )
                }
                if (settings.adaptiveSmoothing) {
                    LabeledSlider(
                        label = "Speed Threshold",
                        value = settings.smoothingThreshold,
                        valueRange = 5.0f..100.0f,
                        format = "%.0f °/s",
                        onValueChange = { onSettingsChange(settings.copy(smoothingThreshold = it)) },
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ════════════════════════════════════════════════════
        //  ACCELERATION CURVE — SHARED
        // ════════════════════════════════════════════════════
        SectionCard("📈 Acceleration Curve") {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Enable Acceleration", modifier = Modifier.weight(1f))
                Switch(
                    checked = settings.accelerationEnabled,
                    onCheckedChange = { onSettingsChange(settings.copy(accelerationEnabled = it)) },
                )
            }
            
            if (settings.accelerationEnabled) {
                AccelerationGraph(
                    curveType = settings.accelerationType,
                    minThreshold = settings.minThreshold,
                    maxThreshold = settings.maxThreshold,
                    minSens = settings.minSensitivity,
                    maxSens = settings.maxSensitivity
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AccelerationType.entries.forEach { type ->
                        RadioButton(
                            selected = settings.accelerationType == type,
                            onClick = { onSettingsChange(settings.copy(accelerationType = type)) },
                        )
                        Text(type.displayName, fontSize = 13.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                LabeledSlider(
                    label = "Min Sensitivity",
                    value = settings.minSensitivity,
                    valueRange = 0.1f..3.0f,
                    format = "%.2f×",
                    onValueChange = { onSettingsChange(settings.copy(minSensitivity = it)) },
                )
                LabeledSlider(
                    label = "Max Sensitivity",
                    value = settings.maxSensitivity,
                    valueRange = 0.5f..5.0f,
                    format = "%.2f×",
                    onValueChange = { onSettingsChange(settings.copy(maxSensitivity = it)) },
                )
                LabeledSlider(
                    label = "Min Threshold",
                    value = settings.minThreshold,
                    valueRange = 0.0f..50.0f,
                    format = "%.0f °/s",
                    onValueChange = { onSettingsChange(settings.copy(minThreshold = it)) },
                )
                LabeledSlider(
                    label = "Max Threshold",
                    value = settings.maxThreshold,
                    valueRange = 20.0f..300.0f,
                    format = "%.0f °/s",
                    onValueChange = { onSettingsChange(settings.copy(maxThreshold = it)) },
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ════════════════════════════════════════════════════
        //  LOW-SPEED AMPLIFIER — XBOX ONLY
        //  (PS4 games control their own deadzones)
        // ════════════════════════════════════════════════════
        if (isXbox) {
            SectionCard("⚡ Anti-Deadzone (Xbox Only)") {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Enable Anti-Deadzone", modifier = Modifier.weight(1f))
                    Switch(
                        checked = settings.lowSpeedAmplifierEnabled,
                        onCheckedChange = { onSettingsChange(settings.copy(lowSpeedAmplifierEnabled = it)) },
                    )
                }
                if (settings.lowSpeedAmplifierEnabled) {
                    Spacer(modifier = Modifier.height(8.dp))
                    LabeledSlider(
                        label = "Multiplier",
                        value = settings.lowSpeedAmplifierAmount,
                        valueRange = 1.0f..10.0f,
                        format = "%.1fx",
                        onValueChange = { onSettingsChange(settings.copy(lowSpeedAmplifierAmount = it)) },
                    )
                    LabeledSlider(
                        label = "Max Speed",
                        value = settings.lowSpeedAmplifierThreshold,
                        valueRange = 1.0f..50.0f,
                        format = "%.1f °/s",
                        onValueChange = { onSettingsChange(settings.copy(lowSpeedAmplifierThreshold = it)) },
                    )
                    LabeledSlider(
                        label = "Harshness",
                        value = settings.lowSpeedAmplifierHarshness,
                        valueRange = 0.5f..5.0f,
                        format = "%.1f",
                        onValueChange = { onSettingsChange(settings.copy(lowSpeedAmplifierHarshness = it)) },
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // ════════════════════════════════════════════════════
        //  AXIS CONTROL & CALIBRATION — SHARED
        // ════════════════════════════════════════════════════
        SectionCard("🧭 Axis & Calibration") {
            // ── Horizontal Axis Selector ──
            Text("Horizontal Axis Mapping:", fontWeight = FontWeight.Bold)
            Text(
                "Which physical rotation drives left/right camera movement.",
                fontSize = 12.sp, color = Color.Gray
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                HorizontalAxis.entries.forEach { axis ->
                    RadioButton(
                        selected = settings.horizontalAxis == axis,
                        onClick = { onSettingsChange(settings.copy(horizontalAxis = axis)) },
                    )
                    Text(axis.displayName, fontSize = 13.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // ── Inversion Toggles ──
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Invert Horizontal", modifier = Modifier.weight(1f))
                Switch(checked = settings.invertX, onCheckedChange = { onSettingsChange(settings.copy(invertX = it)) })
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Invert Vertical", modifier = Modifier.weight(1f))
                Switch(checked = settings.invertY, onCheckedChange = { onSettingsChange(settings.copy(invertY = it)) })
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = onRecalibrate, modifier = Modifier.fillMaxWidth()) {
                Text("🔄 Recalibrate Gyro")
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

// ════════════════════════════════════════════════════════════
//  REUSABLE UI COMPONENTS
// ════════════════════════════════════════════════════════════

@Composable
private fun SectionCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(16.dp),
    ) {
        Text(title, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Spacer(modifier = Modifier.height(8.dp))
        content()
    }
}

@Composable
private fun LabeledSlider(
    label: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    format: String,
    onValueChange: (Float) -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text("$label: ", modifier = Modifier.width(180.dp), fontSize = 14.sp)
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            modifier = Modifier.weight(1f),
        )
        Text(
            String.format(format, value),
            modifier = Modifier.width(70.dp),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}
