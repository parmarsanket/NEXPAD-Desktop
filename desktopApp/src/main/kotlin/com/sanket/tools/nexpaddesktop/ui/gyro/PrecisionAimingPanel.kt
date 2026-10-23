package com.sanket.tools.nexpaddesktop.ui.gyro

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sanket.tools.nexpaddesktop.model.*
import com.sanket.tools.nexpaddesktop.ui.*
import com.sanket.tools.nexpaddesktop.ui.theme.ModeAccentPair

@Composable
fun PrecisionAimingPanel(
    config: PrecisionAimingSettings,
    palette: ModeAccentPair,
    onUpdate: (PrecisionAimingSettings) -> Unit
) {
    var linkYawPitch by remember { mutableStateOf(config.maxDpsYaw == config.maxDpsPitch) }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Group A: Output Mapping & Sensitivity
        ExpressiveSectionCard(
            title = "Camera Aiming & Full-Scale Sensitivity",
            subtitle = "Lower °/s = Faster camera movement (less wrist rotation required for full stick deflection).",
            icon = Icons.Default.GpsFixed,
            accentColor = palette.accent,
            onReset = { onUpdate(PrecisionAimingSettings()) }
        ) {
            EnumChipSelector(
                label = "Target Xbox Stick",
                options = XboxTargetStick.entries,
                selected = config.targetStick,
                optionLabel = { it.displayName },
                accentColor = palette.accent,
                onSelect = { onUpdate(config.copy(targetStick = it)) }
            )

            EnumChipSelector(
                label = "Horizontal Camera Axis",
                options = HorizontalAxis.entries,
                selected = config.horizontalAxis,
                optionLabel = { it.displayName },
                accentColor = palette.accent,
                onSelect = { onUpdate(config.copy(horizontalAxis = it)) }
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Angular Velocity Range (°/s for 100% Stick)",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f, fill = false)
                )
                Spacer(modifier = Modifier.width(8.dp))
                FilterChip(
                    selected = linkYawPitch,
                    onClick = {
                        linkYawPitch = !linkYawPitch
                        if (linkYawPitch) onUpdate(config.copy(maxDpsPitch = config.maxDpsYaw))
                    },
                    label = { Text("Link Yaw & Pitch", style = MaterialTheme.typography.labelSmall, maxLines = 1, softWrap = false) }
                )
            }

            if (linkYawPitch) {
                ExpressiveSlider(
                    label = "Unified Aim Speed (Yaw & Pitch)",
                    value = config.maxDpsYaw,
                    valueRange = 40f..500f,
                    unit = "°/s",
                    accentColor = palette.accent,
                    onValueChange = { onUpdate(config.copy(maxDpsYaw = it, maxDpsPitch = it)) }
                )
            } else {
                ExpressiveSlider(
                    label = "Horizontal Full-Scale Speed (Yaw)",
                    value = config.maxDpsYaw,
                    valueRange = 40f..500f,
                    unit = "°/s",
                    accentColor = palette.accent,
                    onValueChange = { onUpdate(config.copy(maxDpsYaw = it)) }
                )
                ExpressiveSlider(
                    label = "Vertical Full-Scale Speed (Pitch)",
                    value = config.maxDpsPitch,
                    valueRange = 40f..500f,
                    unit = "°/s",
                    accentColor = palette.accent,
                    onValueChange = { onUpdate(config.copy(maxDpsPitch = it)) }
                )
            }

            AxisInvertRow(
                invertX = config.invertX,
                invertY = config.invertY,
                invertRoll = config.invertRoll,
                onInvertX = { onUpdate(config.copy(invertX = it)) },
                onInvertY = { onUpdate(config.copy(invertY = it)) },
                onInvertRoll = { onUpdate(config.copy(invertRoll = it)) }
            )
        }

        // Group B: Deadzone, Response Curve & Stick Blending
        ExpressiveSectionCard(
            title = "Deadzone Compensation & Response Curve",
            subtitle = "Eliminate hand tremor while bypassing the game's internal stick deadzone.",
            icon = Icons.Default.Tune,
            accentColor = palette.accent
        ) {
            ExpressiveSlider(
                label = "Sensor Tremor Deadzone",
                value = config.deadzoneThreshold,
                valueRange = 0.0f..10.0f,
                format = "%.1f",
                unit = "°/s",
                accentColor = palette.accent,
                onValueChange = { onUpdate(config.copy(deadzoneThreshold = it)) }
            )

            ExpressiveSlider(
                label = "In-Game Anti-Deadzone Compensation",
                value = config.antiDeadzone * 100f,
                valueRange = 0f..35f,
                format = "%.0f",
                unit = "%",
                accentColor = palette.accent,
                onValueChange = { onUpdate(config.copy(antiDeadzone = it / 100f)) }
            )

            EnumChipSelector(
                label = "Stick Response Curve",
                options = StickResponseCurve.entries,
                selected = config.stickResponseCurve,
                optionLabel = { it.displayName },
                accentColor = palette.accent,
                onSelect = { onUpdate(config.copy(stickResponseCurve = it)) }
            )

            EnumChipSelector(
                label = "Thumbstick + Gyro Blending",
                options = XboxBlendMode.entries,
                selected = config.blendMode,
                optionLabel = { it.displayName },
                accentColor = palette.accent,
                onSelect = { onUpdate(config.copy(blendMode = it)) }
            )

            ExpandableFeatureBlock(
                title = "Soft Tightening Deadzone",
                subtitle = "Smooth quadratic dampening curve for microscopic adjustments below threshold.",
                checked = config.tighteningEnabled,
                accentColor = palette.accent,
                onCheckedChange = { onUpdate(config.copy(tighteningEnabled = it)) }
            ) {
                ExpressiveSlider(
                    label = "Tightening Threshold",
                    value = config.tighteningThreshold,
                    valueRange = 1.0f..25.0f,
                    format = "%.1f",
                    unit = "°/s",
                    accentColor = palette.accent,
                    onValueChange = { onUpdate(config.copy(tighteningThreshold = it)) }
                )
            }

            ExpandableFeatureBlock(
                title = "Anti-Deadzone Low-Speed Boost",
                subtitle = "Amplifies slow tracking movements to overcome sticky in-game deadzones.",
                checked = config.lowSpeedAmplifierEnabled,
                accentColor = palette.accent,
                onCheckedChange = { onUpdate(config.copy(lowSpeedAmplifierEnabled = it)) }
            ) {
                ExpressiveSlider(
                    label = "Amplifier Multiplier",
                    value = config.lowSpeedAmplifierAmount,
                    valueRange = 1.0f..5.0f,
                    format = "%.1f",
                    unit = "×",
                    accentColor = palette.accent,
                    onValueChange = { onUpdate(config.copy(lowSpeedAmplifierAmount = it)) }
                )
                ExpressiveSlider(
                    label = "Amplifier Cutoff Speed",
                    value = config.lowSpeedAmplifierThreshold,
                    valueRange = 5.0f..40.0f,
                    format = "%.0f",
                    unit = "°/s",
                    accentColor = palette.accent,
                    onValueChange = { onUpdate(config.copy(lowSpeedAmplifierThreshold = it)) }
                )
            }
        }

        // Group C: Progressive Disclosure for Smoothing & Acceleration
        ExpressiveSectionCard(
            title = "Signal Filtering & Velocity Acceleration",
            subtitle = "Optional micro-jitter smoothing and dynamic flick multipliers.",
            icon = Icons.Default.Speed,
            accentColor = palette.accent
        ) {
            ExpandableFeatureBlock(
                title = "Adaptive Signal Smoothing",
                subtitle = "Filters out resting hand tremor while keeping fast flicks 100% responsive.",
                checked = config.smoothingEnabled,
                accentColor = palette.accent,
                onCheckedChange = { onUpdate(config.copy(smoothingEnabled = it)) }
            ) {
                ExpressiveSlider(
                    label = "Smoothing Intensity",
                    value = config.smoothingAmount * 100f,
                    valueRange = 0f..98f,
                    format = "%.0f",
                    unit = "%",
                    accentColor = palette.accent,
                    onValueChange = { onUpdate(config.copy(smoothingAmount = it / 100f)) }
                )
                ExpressiveSlider(
                    label = "Fast-Flick Bypass Threshold",
                    value = config.smoothingThreshold,
                    valueRange = 5f..120f,
                    unit = "°/s",
                    accentColor = palette.accent,
                    onValueChange = { onUpdate(config.copy(smoothingThreshold = it)) }
                )
            }

            ExpandableFeatureBlock(
                title = "Velocity Acceleration Curve",
                subtitle = "Boosts sensitivity during rapid wrist flicks while keeping slow tracking steady.",
                checked = config.accelerationEnabled,
                accentColor = palette.accent,
                onCheckedChange = { onUpdate(config.copy(accelerationEnabled = it)) }
            ) {
                EnumChipSelector(
                    label = "Curve Profile",
                    options = AccelerationType.entries,
                    selected = config.accelerationType,
                    optionLabel = { it.displayName },
                    accentColor = palette.accent,
                    onSelect = { onUpdate(config.copy(accelerationType = it)) }
                )
                ExpressiveSlider(
                    label = "Maximum Flick Multiplier",
                    value = config.maxSensitivity,
                    valueRange = 1.0f..5.0f,
                    format = "%.2f",
                    unit = "×",
                    accentColor = palette.accent,
                    onValueChange = { onUpdate(config.copy(maxSensitivity = it)) }
                )
            }
        }

        // Group D: Activation Trigger (Aim-Down-Sights)
        ActivationTriggerCard(
            activationButtons = config.activationButtons,
            accentColor = palette.accent,
            onButtonsChange = { buttons ->
                onUpdate(
                    config.copy(
                        activationButtons = buttons,
                        activationMode = if (buttons.isNotEmpty()) ActivationMode.BUTTON_HOLD else ActivationMode.ALWAYS_ON
                    )
                )
            }
        )
    }
}
