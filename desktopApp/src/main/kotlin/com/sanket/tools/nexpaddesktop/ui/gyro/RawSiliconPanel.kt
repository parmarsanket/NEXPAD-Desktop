package com.sanket.tools.nexpaddesktop.ui.gyro

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Memory
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
fun RawSiliconPanel(
    config: RawSiliconSettings,
    palette: ModeAccentPair,
    onUpdate: (RawSiliconSettings) -> Unit
) {
    var linkYawPitch by remember { mutableStateOf(config.maxDpsYaw == config.maxDpsPitch) }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Card 1: Esports Direct Raw MEMS Stream & Sensitivity
        ExpressiveSectionCard(
            title = "Esports Direct Raw MEMS Stream",
            subtitle = "Zero OS calibration, zero smoothing filters — direct silicon angular velocity to stick output.",
            icon = Icons.Default.Memory,
            accentColor = palette.accent,
            onReset = { onUpdate(RawSiliconSettings()) }
        ) {
            EnumChipSelector(
                label = "Target Stick",
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
                    text = "Raw Velocity Sensitivity (°/s for 100% Stick)",
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
                    label = "Unified Raw Speed (Yaw & Pitch)",
                    value = config.maxDpsYaw,
                    valueRange = 40f..500f,
                    unit = "°/s",
                    accentColor = palette.accent,
                    onValueChange = { onUpdate(config.copy(maxDpsYaw = it, maxDpsPitch = it)) }
                )
            } else {
                ExpressiveSlider(
                    label = "Raw Yaw Sensitivity (100% Stick Rate)",
                    value = config.maxDpsYaw,
                    valueRange = 40f..500f,
                    unit = "°/s",
                    accentColor = palette.accent,
                    onValueChange = { onUpdate(config.copy(maxDpsYaw = it)) }
                )
                ExpressiveSlider(
                    label = "Raw Pitch Sensitivity (100% Stick Rate)",
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

        // Card 2: Deadzone Compensation, Response Curve & Blending
        ExpressiveSectionCard(
            title = "Hardware Deadzone & Response Curve",
            subtitle = "Direct silicon noise gate, anti-deadzone bridge, and stick blending.",
            icon = Icons.Default.Tune,
            accentColor = palette.accent
        ) {
            ExpressiveSlider(
                label = "Hardware Noise Gate (Sensor Deadzone)",
                value = config.deadzoneThreshold,
                valueRange = 0.0f..10.0f,
                format = "%.1f",
                unit = "°/s",
                accentColor = palette.accent,
                onValueChange = { onUpdate(config.copy(deadzoneThreshold = it)) }
            )

            ExpressiveSlider(
                label = "In-Game Deadzone Bridge (Anti-Deadzone)",
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

        // Card 3: Activation Trigger (Aim-Down-Sights)
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
