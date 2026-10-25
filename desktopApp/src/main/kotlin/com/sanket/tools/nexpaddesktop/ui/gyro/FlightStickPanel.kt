package com.sanket.tools.nexpaddesktop.ui.gyro

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sanket.tools.nexpaddesktop.model.FlightStickAxisTarget
import com.sanket.tools.nexpaddesktop.model.FlightStickSettings
import com.sanket.tools.nexpaddesktop.model.XboxBlendMode
import com.sanket.tools.nexpaddesktop.ui.EnumChipSelector
import com.sanket.tools.nexpaddesktop.ui.ExpandableFeatureBlock
import com.sanket.tools.nexpaddesktop.ui.ExpressiveSectionCard
import com.sanket.tools.nexpaddesktop.ui.ExpressiveSlider
import com.sanket.tools.nexpaddesktop.ui.theme.ModeAccentPair

@Composable
fun FlightStickPanel(
    config: FlightStickSettings,
    palette: ModeAccentPair,
    onUpdate: (FlightStickSettings) -> Unit
) {
    ExpressiveSectionCard(
        title = "6-DoF Aviation Yoke & Attitude Mapping",
        subtitle = "Assign physical pitch, roll, and yaw angles to independent aircraft axes.",
        icon = Icons.Default.Flight,
        accentColor = palette.accent,
        onReset = { onUpdate(FlightStickSettings()) }
    ) {
        EnumChipSelector(
            label = "Pitch (Elevator) Target Axis",
            options = FlightStickAxisTarget.entries,
            selected = config.pitchTarget,
            optionLabel = { it.displayName },
            accentColor = palette.accent,
            onSelect = { onUpdate(config.copy(pitchTarget = it)) }
        )
        ExpressiveSlider(
            label = "Max Pitch Deflection Angle",
            value = config.maxPitchAngle,
            valueRange = 15f..75f,
            unit = "°",
            accentColor = palette.accent,
            onValueChange = { onUpdate(config.copy(maxPitchAngle = it)) }
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f, fill = false)) {
                Text(
                    text = "Invert Pitch Axis",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (config.pitchInvert) "Inverted (Pull back for nose up / push forward to dive)" else "Normal (Push forward for nose up)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Switch(
                checked = config.pitchInvert,
                onCheckedChange = { onUpdate(config.copy(pitchInvert = it)) },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                    checkedTrackColor = palette.accent
                )
            )
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        EnumChipSelector(
            label = "Roll (Aileron) Target Axis",
            options = FlightStickAxisTarget.entries,
            selected = config.rollTarget,
            optionLabel = { it.displayName },
            accentColor = palette.accent,
            onSelect = { onUpdate(config.copy(rollTarget = it)) }
        )
        ExpressiveSlider(
            label = "Max Roll Bank Angle",
            value = config.maxRollAngle,
            valueRange = 15f..75f,
            unit = "°",
            accentColor = palette.accent,
            onValueChange = { onUpdate(config.copy(maxRollAngle = it)) }
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f, fill = false)) {
                Text(
                    text = "Invert Roll Axis",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (config.rollInvert) "Inverted (Reversed wing banking)" else "Normal (Direct wing banking)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Switch(
                checked = config.rollInvert,
                onCheckedChange = { onUpdate(config.copy(rollInvert = it)) },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                    checkedTrackColor = palette.accent
                )
            )
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        // Yaw (Rudder) Card (Default: Disabled / Off)
        ExpandableFeatureBlock(
            title = "Yaw (Rudder) Axis Mapping",
            subtitle = "Flat desk swivel or compass rotation mapped to aircraft rudder pedals (Default: Disabled).",
            checked = config.yawTarget != FlightStickAxisTarget.NONE,
            accentColor = palette.accent,
            onCheckedChange = { enabled ->
                onUpdate(config.copy(yawTarget = if (enabled) FlightStickAxisTarget.RS_X else FlightStickAxisTarget.NONE))
            }
        ) {
            Spacer(modifier = Modifier.height(12.dp))
            EnumChipSelector(
                label = "Yaw Target Axis",
                options = FlightStickAxisTarget.entries,
                selected = config.yawTarget,
                optionLabel = { it.displayName },
                accentColor = palette.accent,
                onSelect = { onUpdate(config.copy(yawTarget = it)) }
            )
            Spacer(modifier = Modifier.height(8.dp))
            ExpressiveSlider(
                label = "Max Yaw Swivel Angle",
                value = config.maxYawAngle,
                valueRange = 15f..75f,
                unit = "°",
                accentColor = palette.accent,
                onValueChange = { onUpdate(config.copy(maxYawAngle = it)) }
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Invert Yaw Axis",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Switch(
                    checked = config.yawInvert,
                    onCheckedChange = { onUpdate(config.copy(yawInvert = it)) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                        checkedTrackColor = palette.accent
                    )
                )
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        ExpressiveSlider(
            label = "Center Hands-Off Deadzone",
            value = config.deadzone,
            valueRange = 0f..10f,
            format = "%.1f",
            unit = "°",
            accentColor = palette.accent,
            onValueChange = { onUpdate(config.copy(deadzone = it)) }
        )
        ExpressiveSlider(
            label = "Yoke Precision Curve Exponent",
            value = config.curveExponent,
            valueRange = 0.8f..2.5f,
            format = "%.2f",
            unit = "exp",
            accentColor = palette.accent,
            onValueChange = { onUpdate(config.copy(curveExponent = it)) }
        )

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        EnumChipSelector(
            label = "Physical Stick + Flight Attitude Blending",
            options = XboxBlendMode.entries,
            selected = config.blendMode,
            optionLabel = { it.displayName },
            accentColor = palette.accent,
            onSelect = { onUpdate(config.copy(blendMode = it)) }
        )
    }
}
