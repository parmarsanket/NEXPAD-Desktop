package com.sanket.tools.nexpaddesktop.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sanket.tools.nexpaddesktop.model.*
import com.sanket.tools.nexpaddesktop.ui.theme.ModeAccentPair
import com.sanket.tools.nexpaddesktop.ui.theme.NexpadTheme
import com.sanket.tools.nexpaddesktop.ui.visualizers.DashboardHeader
import kotlinx.coroutines.delay

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
    flightPitchDeg: Float = 0f,
    flightRollDeg: Float = 0f,
    flightYawDeg: Float = 0f,
    onRecalibrate: () -> Unit
) {
    val activePalette = NexpadTheme.colors.forMode(settings.inputMode)

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // 1. Master Motion Power Bar + Interactive Zero Calibration + Genre Presets
        MotionMasterControlBar(
            settings = settings,
            activePalette = activePalette,
            onSettingsChange = onSettingsChange,
            onRecalibrate = onRecalibrate
        )

        // 2. Live 3D Controller & Telemetry Header
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
            processedPitch = processedPitch,
            flightPitchDeg = flightPitchDeg,
            flightRollDeg = flightRollDeg,
            flightYawDeg = flightYawDeg
        )

        // 3. Expressive 5-Mode Selector Strip
        ExpressiveInputModeSelector(
            activeMode = settings.inputMode,
            onSelectMode = { onSettingsChange(settings.copy(inputMode = it)) }
        )

        // 4. Mode-Specific Configuration Panels (Crossfades smoothly on mode switch)
        AnimatedVisibility(
            visible = settings.enabled,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            AnimatedContent(
                targetState = settings.inputMode,
                transitionSpec = {
                    fadeIn(spring(stiffness = Spring.StiffnessMediumLow)) togetherWith
                        fadeOut(spring(stiffness = Spring.StiffnessMedium))
                },
                label = "xboxModeSettingsPanel"
            ) { mode ->
                val modePalette = NexpadTheme.colors.forMode(mode)
                when (mode) {
                    InputMode.VELOCITY -> PrecisionAimingPanel(
                        config = settings.precisionAiming,
                        palette = modePalette,
                        onUpdate = { onSettingsChange(settings.copy(precisionAiming = it)) }
                    )

                    InputMode.ABSOLUTE_TILT -> SteeringWheelPanel(
                        config = settings.steeringWheel,
                        palette = modePalette,
                        onUpdate = { onSettingsChange(settings.copy(steeringWheel = it)) }
                    )

                    InputMode.AIR_MOUSE -> FlightStickPanel(
                        config = settings.flightStick,
                        palette = modePalette,
                        onUpdate = { onSettingsChange(settings.copy(flightStick = it)) }
                    )

                    InputMode.GESTURE -> CombatGesturesPanel(
                        config = settings.combatGestures,
                        palette = modePalette,
                        onUpdate = { onSettingsChange(settings.copy(combatGestures = it)) }
                    )

                    InputMode.RAW_UNCALIBRATED -> RawSiliconPanel(
                        config = settings.rawSilicon,
                        palette = modePalette,
                        onUpdate = { onSettingsChange(settings.copy(rawSilicon = it)) }
                    )
                }
            }
        }
    }
}

// ═════════════════════════════════════════════════════════════════════════════
// 1. MASTER CONTROL BAR (ENABLE TOGGLE + PRESETS + INTERACTIVE CALIBRATE)
// ═════════════════════════════════════════════════════════════════════════════

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MotionMasterControlBar(
    settings: GyroSettings,
    activePalette: ModeAccentPair,
    onSettingsChange: (GyroSettings) -> Unit,
    onRecalibrate: () -> Unit
) {
    var justCalibrated by remember { mutableStateOf(false) }

    LaunchedEffect(justCalibrated) {
        if (justCalibrated) {
            delay(2000L)
            justCalibrated = false
        }
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
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
                    Column {
                        Text(
                            text = if (settings.enabled) "Motion Processing Active" else "Motion Processing Paused",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Real-time 60Hz sensor translation into virtual Xbox 360 inputs",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Morphing Recalibrate Button with 2-second confirmation feedback
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
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (justCalibrated) "Zero Baseline Locked" else "Recalibrate Zero",
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            // Quick-Load Genre Presets Strip
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
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

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    PresetPill("FPS Aiming") { onSettingsChange(GyroSettings.FPS_PRESET) }
                    PresetPill("Sniper Precision") { onSettingsChange(GyroSettings.PRECISION_PRESET) }
                    PresetPill("Sim Racing") { onSettingsChange(GyroSettings.RACING_PRESET) }
                    PresetPill("Flight Yoke") { onSettingsChange(GyroSettings.FLIGHT_SIM_PRESET) }
                    PresetPill("Action Combat") { onSettingsChange(GyroSettings.ACTION_COMBAT_PRESET) }
                }
            }
        }
    }
}

@Composable
private fun PresetPill(label: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceContainerHigh
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

// ═════════════════════════════════════════════════════════════════════════════
// 2. EXPRESSIVE 5-MODE SELECTOR STRIP
// ═════════════════════════════════════════════════════════════════════════════

@Composable
private fun ExpressiveInputModeSelector(
    activeMode: InputMode,
    onSelectMode: (InputMode) -> Unit
) {
    val modes = remember {
        listOf(
            Triple(InputMode.VELOCITY, "Precision Aim", Icons.Default.GpsFixed),
            Triple(InputMode.ABSOLUTE_TILT, "Steering Wheel", Icons.Default.DirectionsCar),
            Triple(InputMode.AIR_MOUSE, "3D Flight Stick", Icons.Default.Flight),
            Triple(InputMode.GESTURE, "Combat Gestures", Icons.Default.Bolt),
            Triple(InputMode.RAW_UNCALIBRATED, "Raw Silicon", Icons.Default.Memory)
        )
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        modes.forEach { (mode, shortTitle, icon) ->
            val isSelected = activeMode == mode
            val palette = NexpadTheme.colors.forMode(mode)

            val cornerRadius by animateDpAsState(
                targetValue = if (isSelected) 20.dp else 12.dp,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                label = "modeCorner"
            )
            val bgColor by animateColorAsState(
                targetValue = if (isSelected) palette.container else MaterialTheme.colorScheme.surfaceContainerLow,
                label = "modeBg"
            )

            Surface(
                onClick = { onSelectMode(mode) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(cornerRadius),
                color = bgColor,
                border = if (isSelected) BorderStroke(1.5.dp, palette.accent) else null
            ) {
                Column(
                    modifier = Modifier.padding(vertical = 14.dp, horizontal = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) palette.accent.copy(alpha = 0.22f)
                                else MaterialTheme.colorScheme.surfaceContainerHigh
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = if (isSelected) palette.accent else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Text(
                        text = shortTitle,
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isSelected) MaterialTheme.colorScheme.onSurface
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

// ═════════════════════════════════════════════════════════════════════════════
// 3. MODE-SPECIFIC CONFIGURATION PANELS
// ═════════════════════════════════════════════════════════════════════════════

@Composable
private fun PrecisionAimingPanel(
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
                    color = MaterialTheme.colorScheme.onSurface
                )
                FilterChip(
                    selected = linkYawPitch,
                    onClick = {
                        linkYawPitch = !linkYawPitch
                        if (linkYawPitch) onUpdate(config.copy(maxDpsPitch = config.maxDpsYaw))
                    },
                    label = { Text("Link Yaw & Pitch", style = MaterialTheme.typography.labelSmall) }
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
    }
}

@Composable
private fun SteeringWheelPanel(
    config: SteeringWheelSettings,
    palette: ModeAccentPair,
    onUpdate: (SteeringWheelSettings) -> Unit
) {
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
    }
}

@Composable
private fun FlightStickPanel(
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

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        // ── Yaw (Rudder) Card (Default: Disabled / Off) ──
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
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CombatGesturesPanel(
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
            // Posture Output Behavior Toggle (Tap vs Hold while tilted)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
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
                        label = { Text("Single Tap", style = MaterialTheme.typography.labelSmall) }
                    )
                    FilterChip(
                        selected = config.tiltHoldMode,
                        onClick = { onUpdate(config.copy(tiltHoldMode = true)) },
                        label = { Text("Hold Stance", style = MaterialTheme.typography.labelSmall) }
                    )
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

@Composable
private fun RawSiliconPanel(
    config: RawSiliconSettings,
    palette: ModeAccentPair,
    onUpdate: (RawSiliconSettings) -> Unit
) {
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

        ExpressiveSlider(
            label = "Hardware Noise Gate (Deadzone)",
            value = config.deadzoneThreshold,
            valueRange = 0.0f..5.0f,
            format = "%.1f",
            unit = "°/s",
            accentColor = palette.accent,
            onValueChange = { onUpdate(config.copy(deadzoneThreshold = it)) }
        )

        ExpressiveSlider(
            label = "In-Game Anti-Deadzone",
            value = config.antiDeadzone * 100f,
            valueRange = 0f..35f,
            format = "%.0f",
            unit = "%",
            accentColor = palette.accent,
            onValueChange = { onUpdate(config.copy(antiDeadzone = it / 100f)) }
        )
    }
}

// ═════════════════════════════════════════════════════════════════════════════
// 4. REUSABLE M3 EXPRESSIVE BUILDING BLOCKS
// ═════════════════════════════════════════════════════════════════════════════

@Composable
fun ExpressiveSectionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    onReset: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainer
    ) {
        Column(
            modifier = Modifier.padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = accentColor.copy(alpha = 0.14f),
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = accentColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Column {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (onReset != null) {
                    TextButton(onClick = onReset) {
                        Icon(
                            imageVector = Icons.Default.RestartAlt,
                            contentDescription = "Reset section to defaults",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Reset Defaults", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            content()
        }
    }
}

@Composable
fun ExpressiveSlider(
    label: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    format: String = "%.0f",
    unit: String = "",
    accentColor: Color,
    onValueChange: (Float) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            // Tonal Value Readout Pill with Tabular Numerals
            Surface(
                shape = MaterialTheme.shapes.extraSmall,
                color = MaterialTheme.colorScheme.surfaceContainerHigh
            ) {
                Text(
                    text = "${String.format(format, value)} $unit".trim(),
                    style = MaterialTheme.typography.labelSmall,
                    color = accentColor,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
        }

        Slider(
            value = value.coerceIn(valueRange.start, valueRange.endInclusive),
            onValueChange = onValueChange,
            valueRange = valueRange,
            colors = SliderDefaults.colors(
                thumbColor = accentColor,
                activeTrackColor = accentColor,
                inactiveTrackColor = MaterialTheme.colorScheme.surfaceContainerLowest
            )
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun <T> EnumChipSelector(
    label: String,
    options: List<T>,
    selected: T,
    optionLabel: (T) -> String,
    accentColor: Color,
    onSelect: (T) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            options.forEach { item ->
                val isSelected = item == selected
                FilterChip(
                    selected = isSelected,
                    onClick = { onSelect(item) },
                    label = {
                        Text(
                            text = optionLabel(item),
                            style = MaterialTheme.typography.labelMedium
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = accentColor.copy(alpha = 0.18f),
                        selectedLabelColor = accentColor,
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isSelected,
                        borderColor = Color.Transparent,
                        selectedBorderColor = accentColor.copy(alpha = 0.6f)
                    )
                )
            }
        }
    }
}

@Composable
private fun ExpandableFeatureBlock(
    title: String,
    subtitle: String,
    checked: Boolean,
    accentColor: Color,
    onCheckedChange: (Boolean) -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = checked,
                    onCheckedChange = onCheckedChange,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                        checkedTrackColor = accentColor
                    )
                )
            }

            AnimatedVisibility(
                visible = checked,
                enter = fadeIn() + expandVertically(spring(stiffness = Spring.StiffnessMediumLow)),
                exit = fadeOut() + shrinkVertically(spring(stiffness = Spring.StiffnessMedium))
            ) {
                Column(
                    modifier = Modifier.padding(top = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    content = content
                )
            }
        }
    }
}

@Composable
private fun AxisInvertRow(
    invertX: Boolean,
    invertY: Boolean,
    invertRoll: Boolean,
    onInvertX: (Boolean) -> Unit,
    onInvertY: (Boolean) -> Unit,
    onInvertRoll: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Invert Axes:",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        FilterChip(
            selected = invertX,
            onClick = { onInvertX(!invertX) },
            label = { Text("Horizontal (Yaw)", style = MaterialTheme.typography.labelSmall) }
        )
        FilterChip(
            selected = invertY,
            onClick = { onInvertY(!invertY) },
            label = { Text("Vertical (Pitch)", style = MaterialTheme.typography.labelSmall) }
        )
        FilterChip(
            selected = invertRoll,
            onClick = { onInvertRoll(!invertRoll) },
            label = { Text("Tilt (Roll)", style = MaterialTheme.typography.labelSmall) }
        )
    }
}

// ═════════════════════════════════════════════════════════════════════════════
// 5. LEGACY / BACKWARD COMPATIBILITY ADAPTERS
// ═════════════════════════════════════════════════════════════════════════════

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
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainer,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (icon != null) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    } else if (customIcon != null) {
                        customIcon()
                    }
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                if (onReset != null) {
                    FilledTonalButton(
                        onClick = onReset,
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = resetTooltip,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Reset",
                            style = MaterialTheme.typography.labelSmall
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
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodyMedium
            )
            Surface(
                shape = MaterialTheme.shapes.extraSmall,
                color = MaterialTheme.colorScheme.surfaceContainerHigh
            ) {
                Text(
                    text = displayValue ?: String.format(format, value),
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary,
                inactiveTrackColor = MaterialTheme.colorScheme.surfaceContainerLowest
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
