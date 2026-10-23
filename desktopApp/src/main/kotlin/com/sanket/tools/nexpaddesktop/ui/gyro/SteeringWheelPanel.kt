package com.sanket.tools.nexpaddesktop.ui.gyro

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sanket.tools.nexpaddesktop.model.SteeringWheelSettings
import com.sanket.tools.nexpaddesktop.model.XboxBlendMode
import com.sanket.tools.nexpaddesktop.model.XboxTargetStick
import com.sanket.tools.nexpaddesktop.ui.EnumChipSelector
import com.sanket.tools.nexpaddesktop.ui.ExpressiveSectionCard
import com.sanket.tools.nexpaddesktop.ui.ExpressiveSlider
import com.sanket.tools.nexpaddesktop.ui.theme.ModeAccentPair

@Composable
fun SteeringWheelPanel(
    config: SteeringWheelSettings,
    palette: ModeAccentPair,
    onUpdate: (SteeringWheelSettings) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        // Axis Enable/Disable Channels Card
        ExpressiveSectionCard(
            title = "Tilt Axis Channels",
            subtitle = "Enable or disable horizontal steering and vertical pitch input independently.",
            icon = Icons.Default.Tune,
            accentColor = palette.accent
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f, fill = false)) {
                    Text(
                        text = "Horizontal Steering Input (X-Axis)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = if (config.enableHorizontal) "Enabled (Tilt left/right turns analog stick left/right)" else "Disabled (Horizontal steering ignored)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Switch(
                    checked = config.enableHorizontal,
                    onCheckedChange = { onUpdate(config.copy(enableHorizontal = it)) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                        checkedTrackColor = palette.accent
                    )
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f, fill = false)) {
                    Text(
                        text = "Vertical Pitch Input (Y-Axis)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = if (config.enableVertical) "Enabled (Tilt forward/backward moves analog stick up/down)" else "Disabled (Vertical tilt ignored - recommended for pure racing)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Switch(
                    checked = config.enableVertical,
                    onCheckedChange = { onUpdate(config.copy(enableVertical = it)) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                        checkedTrackColor = palette.accent
                    )
                )
            }
        }

        // Virtual Steering Wheel Calibration Card
        ExpressiveSectionCard(
            title = "Virtual Steering Wheel Calibration",
            subtitle = "Maps gravity tilt angle directly to analog steering lock.",
            icon = Icons.Default.DirectionsCar,
            accentColor = palette.accent,
            onReset = { onUpdate(SteeringWheelSettings()) }
        ) {
            EnumChipSelector(
                label = "Steering Output Stick",
                options = XboxTargetStick.entries,
                selected = config.targetStick,
                optionLabel = { it.displayName },
                accentColor = palette.accent,
                onSelect = { onUpdate(config.copy(targetStick = it)) }
            )

            ExpressiveSlider(
                label = "Maximum Steering Lock Angle",
                value = config.maxTiltAngle,
                valueRange = 15f..80f,
                unit = "°",
                accentColor = palette.accent,
                onValueChange = { onUpdate(config.copy(maxTiltAngle = it)) }
            )

            ExpressiveSlider(
                label = "Center Steering Authority (X Sensitivity)",
                value = config.sensitivityX,
                valueRange = 0.2f..3.0f,
                format = "%.2f",
                unit = "×",
                accentColor = palette.accent,
                onValueChange = { onUpdate(config.copy(sensitivityX = it)) }
            )

            if (config.enableVertical) {
                ExpressiveSlider(
                    label = "Vertical Pitch Authority (Y Sensitivity)",
                    value = config.sensitivityY,
                    valueRange = 0.2f..3.0f,
                    format = "%.2f",
                    unit = "×",
                    accentColor = palette.accent,
                    onValueChange = { onUpdate(config.copy(sensitivityY = it)) }
                )
            }

            ExpressiveSlider(
                label = "Steering Non-Linearity (Response Curve)",
                value = config.responseCurve,
                valueRange = 0.5f..3.0f,
                format = "%.2f",
                unit = "exp",
                accentColor = palette.accent,
                onValueChange = { onUpdate(config.copy(responseCurve = it)) }
            )

            ExpressiveSlider(
                label = "Center Deadzone",
                value = config.deadzoneThreshold,
                valueRange = 0f..12f,
                format = "%.1f",
                unit = "°",
                accentColor = palette.accent,
                onValueChange = { onUpdate(config.copy(deadzoneThreshold = it)) }
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f, fill = false)) {
                    Text(
                        text = "Invert Steering Direction",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (config.invertSteering) "Inverted (Tilt right steers left)" else "Normal (Tilt right steers right)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Switch(
                    checked = config.invertSteering,
                    onCheckedChange = { onUpdate(config.copy(invertSteering = it)) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                        checkedTrackColor = palette.accent
                    )
                )
            }

            if (config.enableVertical) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f, fill = false)) {
                        Text(
                            text = "Invert Pitch Direction",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (config.invertPitch) "Inverted (Tilt forward moves stick down)" else "Normal (Tilt forward moves stick up)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Switch(
                        checked = config.invertPitch,
                        onCheckedChange = { onUpdate(config.copy(invertPitch = it)) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                            checkedTrackColor = palette.accent
                        )
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            EnumChipSelector(
                label = "Physical Stick + Steering Wheel Blending",
                options = XboxBlendMode.entries,
                selected = config.blendMode,
                optionLabel = { it.displayName },
                accentColor = palette.accent,
                onSelect = { onUpdate(config.copy(blendMode = it)) }
            )
        }
    }
}
