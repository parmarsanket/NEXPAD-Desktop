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

/**
 * Dedicated 6-Axis Motion Settings screen.
 * Redesigned with a gamer-centric dashboard layout and rich visualizers.
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
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(onClick = { onNavigate(Screen.HOME) }) {
                Text("< Back to Home")
            }
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
        //  DASHBOARD HEADER (3D Phone & Impact Meter)
        // ════════════════════════════════════════════════════
        DashboardHeader(
            settings = settings,
            rawGyroX = rawGyroX,
            rawGyroY = rawGyroY,
            rawGyroZ = rawGyroZ,
            processedYaw = processedYaw,
            processedPitch = processedPitch
        )

        Spacer(modifier = Modifier.height(16.dp))

        // ════════════════════════════════════════════════════
        //  PRESET CARDS
        // ════════════════════════════════════════════════════
        PresetCards(
            currentSettings = settings,
            onSettingsChange = onSettingsChange
        )

        Spacer(modifier = Modifier.height(16.dp))

        // ════════════════════════════════════════════════════
        //  INPUT MODE
        // ════════════════════════════════════════════════════
        SectionCard("🕹️ Input Mode (Xbox Only)") {
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
                    label = "Response Curve (Acceleration)",
                    value = settings.absoluteCurve,
                    valueRange = 1.0f..4.0f,
                    format = "%.1f",
                    onValueChange = { onSettingsChange(settings.copy(absoluteCurve = it)) },
                )
            } else if (settings.inputMode == InputMode.LINEAR_ACCELERATION) {
                Spacer(modifier = Modifier.height(12.dp))
                Text("Sliding (Linear Acceleration) Settings", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                LabeledSlider(
                    label = "Sliding Deadzone",
                    value = settings.slidingDeadzone,
                    valueRange = 0.0f..5.0f,
                    format = "%.1f m/s²",
                    onValueChange = { onSettingsChange(settings.copy(slidingDeadzone = it)) },
                )
                Spacer(modifier = Modifier.height(8.dp))
                LabeledSlider(
                    label = "Horizontal Sensitivity",
                    value = settings.slidingSensitivityX,
                    valueRange = 0.1f..5.0f,
                    format = "%.1fx",
                    onValueChange = { onSettingsChange(settings.copy(slidingSensitivityX = it)) },
                )
                Spacer(modifier = Modifier.height(8.dp))
                LabeledSlider(
                    label = "Vertical Sensitivity",
                    value = settings.slidingSensitivityY,
                    valueRange = 0.1f..5.0f,
                    format = "%.1fx",
                    onValueChange = { onSettingsChange(settings.copy(slidingSensitivityY = it)) },
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ════════════════════════════════════════════════════
        //  SENSITIVITY (Speedometer)
        // ════════════════════════════════════════════════════
        SectionCard("🎚️ Sensitivity") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
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
                }
                SensitivitySpeedometer(sensitivity = settings.sensitivityX)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ════════════════════════════════════════════════════
        //  PRECISION & DEADZONE (Crosshair Visualizer)
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
                    rawYawDps = rawGyroZ * 57.296f, // Approx raw Z
                    rawPitchDps = rawGyroX * 57.296f, // Approx raw X
                    processedYawDps = processedYaw,
                    processedPitchDps = processedPitch
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ════════════════════════════════════════════════════
        //  SMOOTHING (Waveform Visualizer)
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
        //  LOW-SPEED AMPLIFIER
        // ════════════════════════════════════════════════════
        SectionCard("⚡ Anti-Deadzone (Low-Speed Amplifier)") {
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

        // ════════════════════════════════════════════════════
        //  AXIS CONTROL & CALIBRATION
        // ════════════════════════════════════════════════════
        SectionCard("🧭 Axis & Calibration") {
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
