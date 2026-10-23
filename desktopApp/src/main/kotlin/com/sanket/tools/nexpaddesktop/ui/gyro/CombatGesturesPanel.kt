package com.sanket.tools.nexpaddesktop.ui.gyro

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sanket.tools.nexpaddesktop.model.CombatGesturesSettings
import com.sanket.tools.nexpaddesktop.model.GestureButton
import com.sanket.tools.nexpaddesktop.ui.*
import com.sanket.tools.nexpaddesktop.ui.theme.ModeAccentPair

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CombatGesturesPanel(
    config: CombatGesturesSettings,
    palette: ModeAccentPair,
    onUpdate: (CombatGesturesSettings) -> Unit
) {
    ExpressiveSectionCard(
        title = "Kinetic Strikes, Tilt Postures & Shake Triggers",
        subtitle = "Trigger physical button actions with phone flicks while thumbsticks stay 100% untouched.",
        icon = Icons.Default.Bolt,
        accentColor = palette.accent,
        onReset = { onUpdate(CombatGesturesSettings()) }
    ) {
        // ── 1. Directional Kinetic Flicks ──
        ExpandableFeatureBlock(
            title = "Directional Kinetic Flicks",
            subtitle = "Sharp physical wrist strikes trigger instant melee, reload, or 180° snap turns.",
            checked = config.flicksEnabled,
            accentColor = palette.accent,
            onCheckedChange = { onUpdate(config.copy(flicksEnabled = it)) }
        ) {
            SpatialGestureMappingGrid(
                title = "4-Way Kinetic Flick Bindings",
                centerBadgeText = "FLICK",
                upAction = config.flickUpAction,
                downAction = config.flickDownAction,
                leftAction = config.flickLeftAction,
                rightAction = config.flickRightAction,
                accentColor = palette.accent,
                onUpdateDirection = { direction, button ->
                    val updated = when (direction) {
                        GestureDirection.UP -> config.copy(flickUpAction = button)
                        GestureDirection.DOWN -> config.copy(flickDownAction = button)
                        GestureDirection.LEFT -> config.copy(flickLeftAction = button)
                        GestureDirection.RIGHT -> config.copy(flickRightAction = button)
                    }
                    onUpdate(updated)
                }
            )

            ExpressiveSlider(
                label = "Strike Impact Force Threshold",
                value = config.flickThreshold,
                valueRange = 3.0f..20.0f,
                format = "%.1f",
                unit = "m/s²",
                accentColor = palette.accent,
                onValueChange = { onUpdate(config.copy(flickThreshold = it)) }
            )
            ExpressiveSlider(
                label = "Retrigger Cooldown Window",
                value = config.flickCooldownMs.toFloat(),
                valueRange = 150f..1000f,
                unit = "ms",
                accentColor = palette.accent,
                onValueChange = { onUpdate(config.copy(flickCooldownMs = it.toLong())) }
            )
        }

        // ── 2. Tilt Posture Triggers ──
        ExpandableFeatureBlock(
            title = "Tilt Posture Triggers",
            subtitle = "Hold or toggle blocks, crouches, and leans by tilting past an angle threshold.",
            checked = config.tiltEnabled,
            accentColor = palette.accent,
            onCheckedChange = { onUpdate(config.copy(tiltEnabled = it)) }
        ) {
            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val isNarrow = maxWidth < 560.dp
                if (isNarrow) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Column {
                            Text(
                                text = "Posture Activation Behavior",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (config.tiltHoldMode) "Holds button continuously while tilted past angle"
                                else "Fires a single button press when entering tilt angle",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(
                                selected = !config.tiltHoldMode,
                                onClick = { onUpdate(config.copy(tiltHoldMode = false)) },
                                label = { Text("Single Tap", style = MaterialTheme.typography.labelSmall, maxLines = 1, softWrap = false) }
                            )
                            FilterChip(
                                selected = config.tiltHoldMode,
                                onClick = { onUpdate(config.copy(tiltHoldMode = true)) },
                                label = { Text("Hold Stance", style = MaterialTheme.typography.labelSmall, maxLines = 1, softWrap = false) }
                            )
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f, fill = false)) {
                            Text(
                                text = "Posture Activation Behavior",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (config.tiltHoldMode) "Holds button continuously while tilted past angle"
                                else "Fires a single button press when entering tilt angle",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(
                                selected = !config.tiltHoldMode,
                                onClick = { onUpdate(config.copy(tiltHoldMode = false)) },
                                label = { Text("Single Tap", style = MaterialTheme.typography.labelSmall, maxLines = 1, softWrap = false) }
                            )
                            FilterChip(
                                selected = config.tiltHoldMode,
                                onClick = { onUpdate(config.copy(tiltHoldMode = true)) },
                                label = { Text("Hold Stance", style = MaterialTheme.typography.labelSmall, maxLines = 1, softWrap = false) }
                            )
                        }
                    }
                }
            }

            SpatialGestureMappingGrid(
                title = "4-Way Tilt Posture Bindings",
                centerBadgeText = "TILT",
                upAction = config.tiltUpAction,
                downAction = config.tiltDownAction,
                leftAction = config.tiltLeftAction,
                rightAction = config.tiltRightAction,
                accentColor = palette.accent,
                onUpdateDirection = { direction, button ->
                    val updated = when (direction) {
                        GestureDirection.UP -> config.copy(tiltUpAction = button)
                        GestureDirection.DOWN -> config.copy(tiltDownAction = button)
                        GestureDirection.LEFT -> config.copy(tiltLeftAction = button)
                        GestureDirection.RIGHT -> config.copy(tiltRightAction = button)
                    }
                    onUpdate(updated)
                }
            )

            ExpressiveSlider(
                label = "Forward / Back Pitch Trigger Angle",
                value = config.tiltPitchThreshold,
                valueRange = 15f..60f,
                unit = "°",
                accentColor = palette.accent,
                onValueChange = { onUpdate(config.copy(tiltPitchThreshold = it)) }
            )
            ExpressiveSlider(
                label = "Left / Right Roll Trigger Angle",
                value = config.tiltRollThreshold,
                valueRange = 15f..60f,
                unit = "°",
                accentColor = palette.accent,
                onValueChange = { onUpdate(config.copy(tiltRollThreshold = it)) }
            )
        }

        // ── 3. Vigorous Shake Action ──
        ExpandableFeatureBlock(
            title = "Vigorous Shake Action",
            subtitle = "Shake the controller to reload, heal, or break free from stuns.",
            checked = config.shakeEnabled,
            accentColor = palette.accent,
            onCheckedChange = { onUpdate(config.copy(shakeEnabled = it)) }
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Bound Shake Action",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    HardwareKeycapBadge(button = config.shakeAction)
                }

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        GestureButton.BTN_X,
                        GestureButton.BTN_Y,
                        GestureButton.BTN_A,
                        GestureButton.BTN_B,
                        GestureButton.TRIGGER_RT,
                        GestureButton.BTN_RB,
                        GestureButton.SNAP_TURN_180
                    ).forEach { btn ->
                        val isSelected = config.shakeAction == btn
                        val btnColor = btn.toHardwareColor()
                        FilterChip(
                            selected = isSelected,
                            onClick = { onUpdate(config.copy(shakeAction = btn)) },
                            label = { Text(btn.displayName, style = MaterialTheme.typography.labelSmall) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = btnColor.copy(alpha = 0.2f),
                                selectedLabelColor = MaterialTheme.colorScheme.onSurface,
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                            )
                        )
                    }
                }
            }

            ExpressiveSlider(
                label = "Shake Force Threshold",
                value = config.shakeThreshold,
                valueRange = 5.0f..25.0f,
                format = "%.1f",
                unit = "m/s²",
                accentColor = palette.accent,
                onValueChange = { onUpdate(config.copy(shakeThreshold = it)) }
            )
            ExpressiveSlider(
                label = "Shake Retrigger Cooldown",
                value = config.shakeCooldownMs.toFloat(),
                valueRange = 200f..1500f,
                unit = "ms",
                accentColor = palette.accent,
                onValueChange = { onUpdate(config.copy(shakeCooldownMs = it.toLong())) }
            )
        }
    }
}
