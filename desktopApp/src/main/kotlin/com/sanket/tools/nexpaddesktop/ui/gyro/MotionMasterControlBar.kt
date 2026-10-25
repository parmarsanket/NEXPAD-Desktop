package com.sanket.tools.nexpaddesktop.ui.gyro

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sanket.tools.nexpaddesktop.model.GyroSettings
import com.sanket.tools.nexpaddesktop.ui.PresetPill
import com.sanket.tools.nexpaddesktop.ui.theme.ModeAccentPair
import com.sanket.tools.nexpaddesktop.ui.theme.NexpadTheme
import kotlinx.coroutines.delay

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MotionMasterControlBar(
    settings: GyroSettings,
    activePalette: ModeAccentPair,
    onSettingsChange: (GyroSettings) -> Unit,
    onRecalibrate: () -> Unit
) {
    var justCalibrated by remember { mutableStateOf(false) }
    var justReset by remember { mutableStateOf(false) }

    LaunchedEffect(justCalibrated) {
        if (justCalibrated) {
            delay(2000L)
            justCalibrated = false
        }
    }

    LaunchedEffect(justReset) {
        if (justReset) {
            delay(2000L)
            justReset = false
        }
    }

    val handleResetAllDefaults = {
        onSettingsChange(GyroSettings.DEFAULT)
        onRecalibrate()
        justReset = true
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainer
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val isNarrow = maxWidth < 760.dp

                val calButtonColor by animateColorAsState(
                    targetValue = if (justCalibrated) NexpadTheme.colors.statusConnected.copy(alpha = 0.2f)
                    else MaterialTheme.colorScheme.surfaceContainerHigh,
                    label = "calBtnBg"
                )
                val calContentColor by animateColorAsState(
                    targetValue = if (justCalibrated) NexpadTheme.colors.statusConnected
                    else MaterialTheme.colorScheme.onSurface,
                    label = "calBtnFg"
                )

                val resetBtnColor by animateColorAsState(
                    targetValue = if (justReset) NexpadTheme.colors.statusConnected.copy(alpha = 0.2f)
                    else MaterialTheme.colorScheme.surfaceContainerHigh,
                    label = "resetBtnBg"
                )
                val resetContentColor by animateColorAsState(
                    targetValue = if (justReset) NexpadTheme.colors.statusConnected
                    else MaterialTheme.colorScheme.onSurface,
                    label = "resetBtnFg"
                )

                if (isNarrow) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Switch(
                                checked = settings.enabled,
                                onCheckedChange = { onSettingsChange(settings.copy(enabled = it)) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                                    checkedTrackColor = activePalette.accent
                                )
                            )
                            Column {
                                Text(
                                    text = if (settings.enabled) "Motion Processing Active" else "Motion Processing Paused",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Real-time 200Hz sensor translation into virtual Xbox 360 inputs",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            FilledTonalButton(
                                onClick = handleResetAllDefaults,
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = resetBtnColor,
                                    contentColor = resetContentColor
                                ),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = if (justReset) Icons.Default.Check else Icons.Default.RestartAlt,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (justReset) "Defaults Applied" else "Reset Defaults",
                                    style = MaterialTheme.typography.labelMedium,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }

                            FilledTonalButton(
                                onClick = {
                                    onRecalibrate()
                                    justCalibrated = true
                                },
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = calButtonColor,
                                    contentColor = calContentColor
                                ),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = if (justCalibrated) Icons.Default.Check else Icons.Default.Refresh,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (justCalibrated) "Zero Locked" else "Recalibrate Zero",
                                    style = MaterialTheme.typography.labelMedium,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f, fill = false),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Switch(
                                checked = settings.enabled,
                                onCheckedChange = { onSettingsChange(settings.copy(enabled = it)) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                                    checkedTrackColor = activePalette.accent
                                )
                            )
                            Column(modifier = Modifier.padding(end = 12.dp)) {
                                Text(
                                    text = if (settings.enabled) "Motion Processing Active" else "Motion Processing Paused",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Real-time 200Hz sensor translation into virtual Xbox 360 inputs",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            FilledTonalButton(
                                onClick = handleResetAllDefaults,
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = resetBtnColor,
                                    contentColor = resetContentColor
                                )
                            ) {
                                Icon(
                                    imageVector = if (justReset) Icons.Default.Check else Icons.Default.RestartAlt,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (justReset) "Xbox 360 Defaults Applied" else "Reset All Defaults",
                                    style = MaterialTheme.typography.labelMedium,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }

                            FilledTonalButton(
                                onClick = {
                                    onRecalibrate()
                                    justCalibrated = true
                                },
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = calButtonColor,
                                    contentColor = calContentColor
                                )
                            ) {
                                Icon(
                                    imageVector = if (justCalibrated) Icons.Default.Check else Icons.Default.Refresh,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (justCalibrated) "Zero Baseline Locked" else "Recalibrate Zero",
                                    style = MaterialTheme.typography.labelMedium,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            // Quick-Load Genre Presets Strip
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = activePalette.accent,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Quick Presets:",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                PresetPill("Default (Xbox 360)") { handleResetAllDefaults() }
                PresetPill("FPS Aiming") { onSettingsChange(GyroSettings.FPS_PRESET) }
                PresetPill("Sniper Precision") { onSettingsChange(GyroSettings.PRECISION_PRESET) }
                PresetPill("Sim Racing") { onSettingsChange(GyroSettings.RACING_PRESET) }
                PresetPill("Flight Yoke") { onSettingsChange(GyroSettings.FLIGHT_SIM_PRESET) }
                PresetPill("Action Combat") { onSettingsChange(GyroSettings.ACTION_COMBAT_PRESET) }
            }
        }
    }
}
