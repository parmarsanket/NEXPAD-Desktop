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

/**
 * Dedicated 6-Axis Motion Settings screen.
 *
 * Organized into collapsible sections matching the processing pipeline:
 *   Sensitivity → Axis → Deadzone → Smoothing → Acceleration → Tightening → Activation → Presets
 */
@Composable
fun GyroSettingsScreen(
    settings: GyroSettings,
    onSettingsChange: (GyroSettings) -> Unit,
    activeController: ControllerType,
    rawGyroX: Float,
    rawGyroY: Float,
    rawGyroZ: Float,
    processedYaw: Float,
    processedPitch: Float,
    onRecalibrate: () -> Unit,
    onNavigate: (Screen) -> Unit,
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Top,
    ) {
        // ── Navigation ──
        Button(onClick = { onNavigate(Screen.HOME) }) {
            Text("< Back to Home")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ── Header ──
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                "🎯 6-Axis Motion Settings",
                fontWeight = FontWeight.Bold,
                fontSize = 26.sp,
                modifier = Modifier.weight(1f),
            )
            Switch(
                checked = settings.enabled,
                onCheckedChange = { onSettingsChange(settings.copy(enabled = it)) },
            )
        }
        Text(
            "Configure gyroscope behavior for precise motion-controlled aiming.",
            color = Color.Gray,
            fontSize = 14.sp,
        )

        if (!settings.enabled) {
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                "Gyro processing is disabled. Enable it above to configure settings.",
                color = Color.Gray,
                fontSize = 16.sp,
            )
            return@Column
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ════════════════════════════════════════════════════
        //  LIVE PREVIEW
        // ════════════════════════════════════════════════════
        SectionCard("📊 Live Preview") {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Raw Gyro (°/s)", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.Gray)
                    Text("X: ${String.format("%+7.1f", rawGyroX * 57.296f)}", fontSize = 13.sp)
                    Text("Y: ${String.format("%+7.1f", rawGyroY * 57.296f)}", fontSize = 13.sp)
                    Text("Z: ${String.format("%+7.1f", rawGyroZ * 57.296f)}", fontSize = 13.sp)
                }
                Column {
                    Text("Processed (°/s)", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                    Text("Horizontal: ${String.format("%+7.1f", processedYaw)}", fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                    Text("Vertical: ${String.format("%+7.1f", processedPitch)}", fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ════════════════════════════════════════════════════
        //  PRESETS
        // ════════════════════════════════════════════════════
        SectionCard("⚡ Quick Presets") {
            Text("Apply optimized settings for common game types.", fontSize = 13.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { onSettingsChange(GyroSettings.FPS_PRESET) }) {
                    Text("🎯 FPS")
                }
                Button(onClick = { onSettingsChange(GyroSettings.RACING_PRESET) }) {
                    Text("🏎️ Racing")
                }
                Button(onClick = { onSettingsChange(GyroSettings.PRECISION_PRESET) }) {
                    Text("🔬 Precision")
                }
                OutlinedButton(onClick = { onSettingsChange(GyroSettings.DEFAULT) }) {
                    Text("↺ Reset")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ════════════════════════════════════════════════════
        //  INPUT MODE
        // ════════════════════════════════════════════════════
        SectionCard("🕹️ Input Mode (Xbox Only)") {
            Text("Determines how physical phone movement translates to virtual stick movement.", fontSize = 13.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                InputMode.entries.forEach { mode ->
                    RadioButton(
                        selected = settings.inputMode == mode,
                        onClick = { onSettingsChange(settings.copy(inputMode = mode)) },
                    )
                    Text(mode.displayName, fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(16.dp))
                }
            }

            if (settings.inputMode == InputMode.ABSOLUTE_TILT) {
                Spacer(modifier = Modifier.height(12.dp))
                LabeledSlider(
                    label = "Max Steering Angle",
                    value = settings.absoluteMaxTilt,
                    valueRange = 10.0f..90.0f,
                    format = "%.0f°",
                    onValueChange = { onSettingsChange(settings.copy(absoluteMaxTilt = it)) },
                )
                Text(
                    "The physical angle where the joystick reaches 100% deflection. Lower = more sensitive.",
                    fontSize = 12.sp, color = Color.Gray,
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ════════════════════════════════════════════════════
        //  SENSITIVITY
        // ════════════════════════════════════════════════════
        SectionCard("🎚️ Sensitivity") {
            Text(
                "Controls how much camera movement results from physical rotation.",
                fontSize = 13.sp, color = Color.Gray,
            )
            Spacer(modifier = Modifier.height(8.dp))

            LabeledSlider(
                label = "Horizontal (Yaw)",
                value = settings.sensitivityX,
                valueRange = 0.1f..5.0f,
                format = "%.2f",
                onValueChange = { onSettingsChange(settings.copy(sensitivityX = it)) },
            )
            LabeledSlider(
                label = "Vertical (Pitch)",
                value = settings.sensitivityY,
                valueRange = 0.1f..5.0f,
                format = "%.2f",
                onValueChange = { onSettingsChange(settings.copy(sensitivityY = it)) },
            )
            Text(
                "💡 Tip: Vertical should be ~56% of Horizontal (16:9 ratio) for natural feel.",
                fontSize = 12.sp, color = Color.Gray,
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ════════════════════════════════════════════════════
        //  AXIS CONTROL
        // ════════════════════════════════════════════════════
        SectionCard("🧭 Axis Control") {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Invert Horizontal (X)", modifier = Modifier.weight(1f))
                Switch(
                    checked = settings.invertX,
                    onCheckedChange = { onSettingsChange(settings.copy(invertX = it)) },
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Invert Vertical (Y)", modifier = Modifier.weight(1f))
                Switch(
                    checked = settings.invertY,
                    onCheckedChange = { onSettingsChange(settings.copy(invertY = it)) },
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text("Horizontal Axis Mapping:", fontWeight = FontWeight.Bold)
            Text("How physical rotation maps to left/right camera movement.", fontSize = 12.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                HorizontalAxis.entries.forEach { axis ->
                    RadioButton(
                        selected = settings.horizontalAxis == axis,
                        onClick = { onSettingsChange(settings.copy(horizontalAxis = axis)) },
                    )
                    Text(axis.displayName, fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(16.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ════════════════════════════════════════════════════
        //  DEADZONE
        // ════════════════════════════════════════════════════
        SectionCard("🎯 Deadzone") {
            Text(
                "Ignore micro-movements below this threshold to prevent drift/jitter.",
                fontSize = 13.sp, color = Color.Gray,
            )
            Spacer(modifier = Modifier.height(8.dp))
            LabeledSlider(
                label = "Threshold",
                value = settings.deadzoneThreshold,
                valueRange = 0.0f..20.0f,
                format = "%.1f °/s",
                onValueChange = { onSettingsChange(settings.copy(deadzoneThreshold = it)) },
            )
            Text(
                "💡 0 = no deadzone (most responsive). 3-5 = good for reducing hand tremor.",
                fontSize = 12.sp, color = Color.Gray,
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ════════════════════════════════════════════════════
        //  SMOOTHING
        // ════════════════════════════════════════════════════
        SectionCard("🌊 Smoothing") {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Enable Smoothing", modifier = Modifier.weight(1f))
                Switch(
                    checked = settings.smoothingEnabled,
                    onCheckedChange = { onSettingsChange(settings.copy(smoothingEnabled = it)) },
                )
            }
            Text(
                "Reduces jitter at the cost of slight input lag.",
                fontSize = 13.sp, color = Color.Gray,
            )

            if (settings.smoothingEnabled) {
                Spacer(modifier = Modifier.height(8.dp))
                LabeledSlider(
                    label = "Smoothing Amount",
                    value = settings.smoothingAmount,
                    valueRange = 0.1f..1.0f,
                    format = "%.2f",
                    onValueChange = { onSettingsChange(settings.copy(smoothingAmount = it)) },
                )
                Text(
                    "Low = heavy smoothing (smooth but laggy). High = light smoothing (responsive).",
                    fontSize = 12.sp, color = Color.Gray,
                )

                Spacer(modifier = Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Adaptive Smoothing", modifier = Modifier.weight(1f))
                    Switch(
                        checked = settings.adaptiveSmoothing,
                        onCheckedChange = { onSettingsChange(settings.copy(adaptiveSmoothing = it)) },
                    )
                }
                Text(
                    "Only smooth slow movements. Fast movements pass through raw for responsiveness.",
                    fontSize = 12.sp, color = Color.Gray,
                )

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
        //  ACCELERATION CURVE
        // ════════════════════════════════════════════════════
        SectionCard("📈 Acceleration Curve") {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Enable Acceleration", modifier = Modifier.weight(1f))
                Switch(
                    checked = settings.accelerationEnabled,
                    onCheckedChange = { onSettingsChange(settings.copy(accelerationEnabled = it)) },
                )
            }
            Text(
                "Slow rotation = precise micro-aiming. Fast rotation = amplified quick turns.",
                fontSize = 13.sp, color = Color.Gray,
            )

            if (settings.accelerationEnabled) {
                Spacer(modifier = Modifier.height(12.dp))
                Text("Curve Type:", fontWeight = FontWeight.Bold)
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
                    label = "Min Sensitivity (slow rotation)",
                    value = settings.minSensitivity,
                    valueRange = 0.1f..3.0f,
                    format = "%.2f×",
                    onValueChange = { onSettingsChange(settings.copy(minSensitivity = it)) },
                )
                LabeledSlider(
                    label = "Max Sensitivity (fast rotation)",
                    value = settings.maxSensitivity,
                    valueRange = 0.5f..5.0f,
                    format = "%.2f×",
                    onValueChange = { onSettingsChange(settings.copy(maxSensitivity = it)) },
                )
                LabeledSlider(
                    label = "Min Speed Threshold",
                    value = settings.minThreshold,
                    valueRange = 0.0f..50.0f,
                    format = "%.0f °/s",
                    onValueChange = { onSettingsChange(settings.copy(minThreshold = it)) },
                )
                LabeledSlider(
                    label = "Max Speed Threshold",
                    value = settings.maxThreshold,
                    valueRange = 20.0f..300.0f,
                    format = "%.0f °/s",
                    onValueChange = { onSettingsChange(settings.copy(maxThreshold = it)) },
                )
                Text(
                    "Example: Slow tilt 45° → camera moves 25°. Fast flick 45° → camera moves 90°.",
                    fontSize = 12.sp, color = Color.Gray,
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ════════════════════════════════════════════════════
        //  TIGHTENING
        // ════════════════════════════════════════════════════
        SectionCard("🔒 Tightening (Precision Zone)") {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Enable Tightening", modifier = Modifier.weight(1f))
                Switch(
                    checked = settings.tighteningEnabled,
                    onCheckedChange = { onSettingsChange(settings.copy(tighteningEnabled = it)) },
                )
            }
            Text(
                "Soft deadzone that smoothly reduces very small inputs for ultra-precise aiming.",
                fontSize = 13.sp, color = Color.Gray,
            )

            if (settings.tighteningEnabled) {
                Spacer(modifier = Modifier.height(8.dp))
                LabeledSlider(
                    label = "Precision Threshold",
                    value = settings.tighteningThreshold,
                    valueRange = 1.0f..30.0f,
                    format = "%.1f °/s",
                    onValueChange = { onSettingsChange(settings.copy(tighteningThreshold = it)) },
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ════════════════════════════════════════════════════
        //  ACTIVATION
        // ════════════════════════════════════════════════════
        SectionCard("🕹️ Activation") {
            Text("How the gyro is activated during gameplay.", fontSize = 13.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                ActivationMode.entries.forEach { mode ->
                    RadioButton(
                        selected = settings.activationMode == mode,
                        onClick = { onSettingsChange(settings.copy(activationMode = mode)) },
                    )
                    Text(mode.displayName, fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                }
            }

            if (settings.activationMode != ActivationMode.ALWAYS_ON) {
                Spacer(modifier = Modifier.height(8.dp))
                Text("Activation Buttons:", fontWeight = FontWeight.Bold)

                var buttonMenuExpanded by remember { mutableStateOf(false) }
                Box {
                    Button(onClick = { buttonMenuExpanded = true }) {
                        val label = if (settings.activationButtons.isEmpty()) "None selected" else "${settings.activationButtons.size} selected"
                        Text("$label ▼")
                    }
                    DropdownMenu(
                        expanded = buttonMenuExpanded,
                        onDismissRequest = { buttonMenuExpanded = false },
                    ) {
                        val options = listOf("LT", "RT", "LB", "RB", "A", "B", "X", "Y")
                        val names = listOf("Left Trigger", "Right Trigger", "Left Bumper", "Right Bumper", "Button A", "Button B", "Button X", "Button Y")
                        options.forEachIndexed { index, opt ->
                            val isSelected = settings.activationButtons.contains(opt)
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Checkbox(checked = isSelected, onCheckedChange = null)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(names[index])
                                    }
                                },
                                onClick = {
                                    val updated = if (isSelected) settings.activationButtons - opt else settings.activationButtons + opt
                                    onSettingsChange(settings.copy(activationButtons = updated))
                                },
                            )
                        }
                    }
                }
            }
        }

        // ════════════════════════════════════════════════════
        //  XBOX GYRO-TO-STICK (only shown for Xbox controller)
        // ════════════════════════════════════════════════════
        if (activeController == ControllerType.XBOX_360) {
            Spacer(modifier = Modifier.height(16.dp))
            SectionCard("🎮 Xbox Gyro → Stick Mapping") {
                Text(
                    "Xbox controllers don't have native gyro. Motion is mapped to analog stick movement.",
                    fontSize = 13.sp, color = Color.Gray,
                )
                Spacer(modifier = Modifier.height(8.dp))

                Text("Target Stick:", fontWeight = FontWeight.Bold)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = settings.xboxTargetStick == "RIGHT_STICK",
                        onClick = { onSettingsChange(settings.copy(xboxTargetStick = "RIGHT_STICK")) },
                    )
                    Text("Right Stick (Camera/Aim)")
                    Spacer(modifier = Modifier.width(16.dp))
                    RadioButton(
                        selected = settings.xboxTargetStick == "LEFT_STICK",
                        onClick = { onSettingsChange(settings.copy(xboxTargetStick = "LEFT_STICK")) },
                    )
                    Text("Left Stick (Movement)")
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text("Blend Mode:", fontWeight = FontWeight.Bold)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = settings.xboxBlendMode == "OVERRIDE",
                        onClick = { onSettingsChange(settings.copy(xboxBlendMode = "OVERRIDE")) },
                    )
                    Text("Override (Replaces stick)")
                    Spacer(modifier = Modifier.width(16.dp))
                    RadioButton(
                        selected = settings.xboxBlendMode == "ADDITIVE",
                        onClick = { onSettingsChange(settings.copy(xboxBlendMode = "ADDITIVE")) },
                    )
                    Text("Additive (Combines with stick)")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ════════════════════════════════════════════════════
        //  CALIBRATION
        // ════════════════════════════════════════════════════
        SectionCard("🔧 Calibration") {
            Text(
                "Place your phone on a flat, stable surface and press Calibrate. Keep it still for ~2 seconds.",
                fontSize = 13.sp, color = Color.Gray,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = onRecalibrate) {
                Text("🔄 Recalibrate Gyro")
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}


// ════════════════════════════════════════════════════════════
//  REUSABLE UI COMPONENTS
// ════════════════════════════════════════════════════════════

/** Card wrapper for each settings section. */
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

/** Labeled slider with formatted value display. */
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
        Text("$label: ", modifier = Modifier.width(220.dp), fontSize = 14.sp)
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
