package com.sanket.tools.nexpaddesktop.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LinkOff
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.sanket.tools.nexpad.model.GamepadInput
import com.sanket.tools.nexpaddesktop.model.GyroSettings
import com.sanket.tools.nexpaddesktop.ui.components.DesktopVerticalScrollbar
import com.sanket.tools.nexpaddesktop.ui.components.glassCard
import com.sanket.tools.nexpaddesktop.ui.components.selectedGlow
import com.sanket.tools.nexpaddesktop.ui.theme.NeonPalette
import com.sanket.tools.nexpaddesktop.ui.visualizers.DashboardHeader
import com.sanket.tools.nexpaddesktop.viewmodel.DesktopViewModel

@Composable
fun ControllerScreen(
    viewModel: DesktopViewModel
) {
    ControllerScreen(
        latestInput = viewModel.latestInput,
        activeController = viewModel.activeController,
        isDriverConnected = viewModel.isDriverConnected,
        dsuClientCount = viewModel.dsuClientCount,
        lsSensitivityX = viewModel.lsSensitivityX,
        lsSensitivityY = viewModel.lsSensitivityY,
        rsSensitivityX = viewModel.rsSensitivityX,
        rsSensitivityY = viewModel.rsSensitivityY,
        onLsSensitivityXChange = { viewModel.updateLsSensitivityX(it) },
        onLsSensitivityYChange = { viewModel.updateLsSensitivityY(it) },
        onRsSensitivityXChange = { viewModel.updateRsSensitivityX(it) },
        onRsSensitivityYChange = { viewModel.updateRsSensitivityY(it) },
        onSaveController = { viewModel.updateActiveController(it) },
        gyroSettings = viewModel.gyroSettings,
        onGyroSettingsChange = { viewModel.updateGyroSettings(it) },
        processedYaw = viewModel.processedYaw,
        processedPitch = viewModel.processedPitch,
        flightPitchDeg = viewModel.flightPitchDeg,
        flightRollDeg = viewModel.flightRollDeg,
        flightYawDeg = viewModel.flightYawDeg,
        onRecalibrate = viewModel.onRecalibrate
    )
}

@Composable
fun ControllerScreen(
    latestInput: GamepadInput,
    activeController: ControllerType,
    isDriverConnected: Boolean = true,
    dsuClientCount: Int = 0,
    lsSensitivityX: Float,
    lsSensitivityY: Float,
    rsSensitivityX: Float,
    rsSensitivityY: Float,
    onLsSensitivityXChange: (Float) -> Unit,
    onLsSensitivityYChange: (Float) -> Unit,
    onRsSensitivityXChange: (Float) -> Unit,
    onRsSensitivityYChange: (Float) -> Unit,
    onSaveController: (ControllerType) -> Unit,
    gyroSettings: GyroSettings,
    onGyroSettingsChange: (GyroSettings) -> Unit,
    processedYaw: Float,
    processedPitch: Float,
    flightPitchDeg: Float = 0f,
    flightRollDeg: Float = 0f,
    flightYawDeg: Float = 0f,
    onRecalibrate: () -> Unit
) {
    val scrollState = rememberScrollState()

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isCompact = maxWidth < 640.dp
        val startPadding = if (isCompact) 16.dp else 32.dp
        val topPadding = if (isCompact) 16.dp else 32.dp
        val bottomPadding = if (isCompact) 20.dp else 32.dp
        val endPadding = if (isCompact) 12.dp else 24.dp

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = startPadding, top = topPadding, bottom = bottomPadding, end = endPadding)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(if (isCompact) 16.dp else 24.dp)
        ) {
            // ── Top Title ──
            Column {
                Text(
                    text = "CONTROLLER & MOTION",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        brush = Brush.linearGradient(
                            colors = listOf(Color.White, MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary)
                        )
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Select your target gamepad emulation profile and configure motion control parameters.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // ── Material 3 Expressive Controller Selector ──
            ExpressiveControllerSelector(
                activeController = activeController,
                onSelect = onSaveController
            )

            // ════════════════════════════════════════════════════
            //  DYNAMIC ANIMATED CONTROLLER PROFILE VIEWPORT
            // ════════════════════════════════════════════════════
            AnimatedContent(
                targetState = activeController,
                transitionSpec = {
                    (slideInVertically(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) { height -> height / 8 } + fadeIn())
                        .togetherWith(slideOutVertically(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) { height -> -height / 8 } + fadeOut())
                },
                label = "controller_mode_transition"
            ) { targetController ->
                if (targetController == ControllerType.DUALSHOCK_4) {
                    DualShock4Dashboard(
                        latestInput = latestInput,
                        isDriverConnected = isDriverConnected,
                        dsuClientCount = dsuClientCount,
                        gyroSettings = gyroSettings,
                        processedYaw = processedYaw,
                        processedPitch = processedPitch,
                        flightPitchDeg = flightPitchDeg,
                        flightRollDeg = flightRollDeg,
                        flightYawDeg = flightYawDeg,
                        onRecalibrate = onRecalibrate
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
                        XboxMotionSettingsSection(
                            settings = gyroSettings,
                            onSettingsChange = onGyroSettingsChange,
                            rawGyroX = latestInput.gyroX,
                            rawGyroY = latestInput.gyroY,
                            rawGyroZ = latestInput.gyroZ,
                            rawAccelX = latestInput.accelX,
                            rawAccelY = latestInput.accelY,
                            rawAccelZ = latestInput.accelZ,
                            rawGravityX = latestInput.gravityX,
                            rawGravityY = latestInput.gravityY,
                            rawGravityZ = latestInput.gravityZ,
                            linearAccelX = latestInput.linearAccelX,
                            linearAccelY = latestInput.linearAccelY,
                            linearAccelZ = latestInput.linearAccelZ,
                            rawUncalGyroX = latestInput.rawGyroX,
                            rawUncalGyroY = latestInput.rawGyroY,
                            rawUncalGyroZ = latestInput.rawGyroZ,
                            processedYaw = processedYaw,
                            processedPitch = processedPitch,
                            flightPitchDeg = flightPitchDeg,
                            flightRollDeg = flightRollDeg,
                            flightYawDeg = flightYawDeg,
                            onRecalibrate = onRecalibrate
                        )

                        // ── Gamepad Base Sticks Sensitivity ──
                        ModernSectionCard(
                            title = "Analog Sticks Base Sensitivity (${gyroSettings.inputMode.displayName})",
                            icon = Icons.Default.Tune,
                            onReset = {
                                onGyroSettingsChange(gyroSettings.updateActiveStickSensitivities(1.0f, 1.0f, 1.0f, 1.0f))
                            },
                            resetTooltip = "Reset Stick Sensitivities for ${gyroSettings.inputMode.displayName} to 1.00×"
                        ) {
                            Text(
                                "Hardware scaling applied to the physical on-screen thumbsticks specifically for ${gyroSettings.inputMode.displayName}.",
                                fontSize = 12.sp,
                                color = NeonPalette.CardIdleText
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            StickSensitivityGroup(
                                title = "Right Stick (Camera / Look)",
                                subtitle = "Thumbstick aiming sensitivity for ${gyroSettings.inputMode.displayName}",
                                sensX = gyroSettings.rsSensitivityX,
                                sensY = gyroSettings.rsSensitivityY,
                                onUpdate = { x, y ->
                                    onGyroSettingsChange(gyroSettings.updateActiveStickSensitivities(rsX = x, rsY = y))
                                }
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            StickSensitivityGroup(
                                title = "Left Stick (Movement)",
                                subtitle = "Thumbstick movement sensitivity for ${gyroSettings.inputMode.displayName}",
                                sensX = gyroSettings.lsSensitivityX,
                                sensY = gyroSettings.lsSensitivityY,
                                onUpdate = { x, y ->
                                    onGyroSettingsChange(gyroSettings.updateActiveStickSensitivities(lsX = x, lsY = y))
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }

        DesktopVerticalScrollbar(
            adapter = rememberScrollbarAdapter(scrollState),
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .fillMaxHeight()
                .padding(vertical = 6.dp, horizontal = 4.dp),
            thickness = 10.dp,
            trackWidth = 14.dp,
            showTrackGutter = true
        )
    }
}

/**
 * Material 3 Expressive Controller Selector (Replaces bulky text-heavy profile cards).
 */
@Composable
fun ExpressiveControllerSelector(
    activeController: ControllerType,
    onSelect: (ControllerType) -> Unit
) {
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val isNarrow = maxWidth < 560.dp
        if (isNarrow) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ControllerOptionCard(
                    modifier = Modifier.fillMaxWidth(),
                    title = "Xbox 360",
                    badge = "Stick Emulation",
                    summary = "Translates motion into analog thumbstick movement for any Windows PC game.",
                    accentColor = NeonPalette.Cyan,
                    isSelected = activeController == ControllerType.XBOX_360,
                    onClick = { onSelect(ControllerType.XBOX_360) }
                )
                ControllerOptionCard(
                    modifier = Modifier.fillMaxWidth(),
                    title = "DualShock 4",
                    badge = "Native 6-Axis Gyro",
                    summary = "Streams raw 1:1 motion directly to Steam Input & native gyro games without software knobs.",
                    accentColor = NeonPalette.Purple,
                    isSelected = activeController == ControllerType.DUALSHOCK_4,
                    onClick = { onSelect(ControllerType.DUALSHOCK_4) }
                )
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ControllerOptionCard(
                    modifier = Modifier.weight(1f),
                    title = "Xbox 360",
                    badge = "Stick Emulation",
                    summary = "Translates motion into analog thumbstick movement for any Windows PC game.",
                    accentColor = NeonPalette.Cyan,
                    isSelected = activeController == ControllerType.XBOX_360,
                    onClick = { onSelect(ControllerType.XBOX_360) }
                )
                ControllerOptionCard(
                    modifier = Modifier.weight(1f),
                    title = "DualShock 4",
                    badge = "Native 6-Axis Gyro",
                    summary = "Streams raw 1:1 motion directly to Steam Input & native gyro games without software knobs.",
                    accentColor = NeonPalette.Purple,
                    isSelected = activeController == ControllerType.DUALSHOCK_4,
                    onClick = { onSelect(ControllerType.DUALSHOCK_4) }
                )
            }
        }
    }
}

@Composable
private fun ControllerOptionCard(
    modifier: Modifier = Modifier,
    title: String,
    badge: String,
    summary: String,
    accentColor: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    // M3 Expressive Spring Animations
    val cornerRadius by animateDpAsState(
        targetValue = if (isSelected) 24.dp else 16.dp,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
    )
    val containerColor by animateColorAsState(
        targetValue = when {
            isSelected -> accentColor.copy(alpha = 0.14f)
            isHovered -> MaterialTheme.colorScheme.surfaceContainerHigh
            else -> MaterialTheme.colorScheme.surfaceContainerLow
        }
    )

    Surface(
        onClick = onClick,
        interactionSource = interactionSource,
        modifier = modifier,
        shape = RoundedCornerShape(cornerRadius),
        color = containerColor,
        border = if (isSelected) BorderStroke(1.5.dp, accentColor) else null
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
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
                    // Tonal Icon Container
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) accentColor.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.05f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.SportsEsports,
                            contentDescription = null,
                            tint = if (isSelected) accentColor else NeonPalette.CardIdleText,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f, fill = false)) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = badge,
                            style = MaterialTheme.typography.labelMedium,
                            color = if (isSelected) accentColor else NeonPalette.CardIdleText,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                RadioButton(
                    selected = isSelected,
                    onClick = null,
                    colors = RadioButtonDefaults.colors(
                        selectedColor = accentColor,
                        unselectedColor = Color.White.copy(alpha = 0.3f)
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = summary,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.75f),
                lineHeight = 18.sp
            )
        }
    }
}

/**
 * Simplified Stick Sensitivity Group with "Link X & Y" Toggle (Reduces slider clutter by 50%).
 */
@Composable
private fun StickSensitivityGroup(
    title: String,
    subtitle: String,
    sensX: Float,
    sensY: Float,
    onUpdate: (Float, Float) -> Unit
) {
    var linkAxes by remember(sensX, sensY) { mutableStateOf(kotlin.math.abs(sensX - sensY) < 0.001f) }

    Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            val isNarrow = maxWidth < 480.dp
            Column {
                if (isNarrow) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Column {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleSmall,
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = subtitle,
                                fontSize = 11.sp,
                                color = NeonPalette.CardIdleText
                            )
                        }
                        FilterChip(
                            selected = linkAxes,
                            onClick = {
                                linkAxes = !linkAxes
                                if (linkAxes) onUpdate(sensX, sensX)
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = if (linkAxes) Icons.Default.Link else Icons.Default.LinkOff,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            label = { Text("Link X & Y", fontSize = 11.sp, fontWeight = FontWeight.Medium, maxLines = 1, softWrap = false) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = NeonPalette.Cyan.copy(alpha = 0.2f),
                                selectedLabelColor = NeonPalette.Cyan,
                                selectedLeadingIconColor = NeonPalette.Cyan
                            )
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f, fill = false)) {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleSmall,
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = subtitle,
                                fontSize = 11.sp,
                                color = NeonPalette.CardIdleText
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        FilterChip(
                            selected = linkAxes,
                            onClick = {
                                linkAxes = !linkAxes
                                if (linkAxes) onUpdate(sensX, sensX)
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = if (linkAxes) Icons.Default.Link else Icons.Default.LinkOff,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            label = { Text("Link X & Y", fontSize = 11.sp, fontWeight = FontWeight.Medium, maxLines = 1, softWrap = false) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = NeonPalette.Cyan.copy(alpha = 0.2f),
                                selectedLabelColor = NeonPalette.Cyan,
                                selectedLeadingIconColor = NeonPalette.Cyan
                            )
                        )
                    }
                }

            Spacer(modifier = Modifier.height(8.dp))

            AnimatedContent(
                targetState = linkAxes,
                transitionSpec = {
                    (fadeIn(tween(200)) + expandVertically())
                        .togetherWith(fadeOut(tween(200)) + shrinkVertically())
                },
                label = "stick_sensitivity_expansion"
            ) { linked ->
                if (linked) {
                    ModernSlider(
                        label = "Overall Sensitivity",
                        value = sensX,
                        valueRange = 0.1f..3.0f,
                        format = "%.2f×",
                        onValueChange = { onUpdate(it, it) }
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        ModernSlider(
                            label = "Horizontal (X) Sensitivity",
                            value = sensX,
                            valueRange = 0.1f..3.0f,
                            format = "%.2f×",
                            onValueChange = { onUpdate(it, sensY) }
                        )
                        ModernSlider(
                            label = "Vertical (Y) Sensitivity",
                            value = sensY,
                            valueRange = 0.1f..3.0f,
                            format = "%.2f×",
                            onValueChange = { onUpdate(sensX, it) }
                        )
                    }
                }
            }
        }
    }
}
}

/**
 * Dedicated DualShock 4 Native Mode Dashboard (Material 3 Expressive).
 *
 * Informs the user that raw motion is passed directly to games/applications,
 * avoiding unnecessary conversion settings that don't apply to native gyro.
 */
@Composable
private fun DualShock4Dashboard(
    latestInput: GamepadInput,
    isDriverConnected: Boolean,
    dsuClientCount: Int,
    gyroSettings: GyroSettings,
    processedYaw: Float,
    processedPitch: Float,
    flightPitchDeg: Float = 0f,
    flightRollDeg: Float = 0f,
    flightYawDeg: Float = 0f,
    onRecalibrate: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ── Native Passthrough Banner ──
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.35f))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(NeonPalette.Purple.copy(alpha = 0.18f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.SportsEsports,
                            contentDescription = null,
                            tint = NeonPalette.Purple,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f, fill = false)) {
                        Text(
                            text = "DualShock 4 Native Hardware Passthrough",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color.White
                        )
                        Text(
                            text = "Zero-manipulation raw 6-axis IMU stream to games and applications",
                            fontSize = 12.sp,
                            color = NeonPalette.CardIdleText
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Scannable side-by-side Info Chips / Callout Pills
                BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                    if (maxWidth < 540.dp) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            InfoCalloutPill(
                                modifier = Modifier.fillMaxWidth(),
                                icon = Icons.Default.Tune,
                                title = "In-Game Sensitivity",
                                subtitle = "Deadzones, curves, and sensitivity are tuned directly inside PC game settings or Steam Input.",
                                accentColor = NeonPalette.Cyan
                            )
                            InfoCalloutPill(
                                modifier = Modifier.fillMaxWidth(),
                                icon = Icons.Default.Speed,
                                title = "1:1 Bit-Perfect Stream",
                                subtitle = "High-frequency uncompressed sensor packets; software conversion knobs are bypassed.",
                                accentColor = NeonPalette.Purple
                            )
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            InfoCalloutPill(
                                modifier = Modifier.weight(1f),
                                icon = Icons.Default.Tune,
                                title = "In-Game Sensitivity",
                                subtitle = "Deadzones, curves, and sensitivity are tuned directly inside PC game settings or Steam Input.",
                                accentColor = NeonPalette.Cyan
                            )
                            InfoCalloutPill(
                                modifier = Modifier.weight(1f),
                                icon = Icons.Default.Speed,
                                title = "1:1 Bit-Perfect Stream",
                                subtitle = "High-frequency uncompressed sensor packets; software conversion knobs are bypassed.",
                                accentColor = NeonPalette.Purple
                            )
                        }
                    }
                }
            }
        }

        // ── Humanized Telemetry & Service Status Tiles ──
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            if (maxWidth < 620.dp) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatusTile(
                        modifier = Modifier.fillMaxWidth(),
                        title = "ViGEm DS4 Driver",
                        value = if (isDriverConnected) "Connected" else "Standby",
                        subtitle = if (isDriverConnected) "Virtual Gamepad Active" else "Awaiting Driver",
                        color = if (isDriverConnected) NeonPalette.ConnectedDot else NeonPalette.CardIdleText,
                        showIndicator = true
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        StatusTile(
                            modifier = Modifier.weight(1f),
                            title = "DSU Motion Server",
                            value = "$dsuClientCount Active Client${if (dsuClientCount != 1) "s" else ""}",
                            subtitle = "UDP Port 26760",
                            color = NeonPalette.Cyan
                        )
                        StatusTile(
                            modifier = Modifier.weight(1f),
                            title = "Sensor Pipeline",
                            value = "6-Axis IMU Active",
                            subtitle = "Mode 0x00 Direct Passthrough",
                            color = NeonPalette.Purple
                        )
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatusTile(
                        modifier = Modifier.weight(1f),
                        title = "ViGEm DS4 Driver",
                        value = if (isDriverConnected) "Connected" else "Standby",
                        subtitle = if (isDriverConnected) "Virtual Gamepad Active" else "Awaiting Driver",
                        color = if (isDriverConnected) NeonPalette.ConnectedDot else NeonPalette.CardIdleText,
                        showIndicator = true
                    )
                    StatusTile(
                        modifier = Modifier.weight(1f),
                        title = "DSU Motion Server",
                        value = "$dsuClientCount Active Client${if (dsuClientCount != 1) "s" else ""}",
                        subtitle = "UDP Port 26760 (Cemuhook)",
                        color = NeonPalette.Cyan
                    )
                    StatusTile(
                        modifier = Modifier.weight(1f),
                        title = "Sensor Pipeline",
                        value = "6-Axis IMU Active",
                        subtitle = "Mode 0x00 Direct Passthrough",
                        color = NeonPalette.Purple
                    )
                }
            }
        }

        // ── 3D Live Orientation Visualizer ──
        DashboardHeader(
            settings = gyroSettings,
            rawGyroX = latestInput.gyroX,
            rawGyroY = latestInput.gyroY,
            rawGyroZ = latestInput.gyroZ,
            rawAccelX = latestInput.accelX,
            rawAccelY = latestInput.accelY,
            rawAccelZ = latestInput.accelZ,
            rawGravityX = latestInput.gravityX,
            rawGravityY = latestInput.gravityY,
            rawGravityZ = latestInput.gravityZ,
            linearAccelX = latestInput.linearAccelX,
            linearAccelY = latestInput.linearAccelY,
            linearAccelZ = latestInput.linearAccelZ,
            rawUncalGyroX = latestInput.rawGyroX,
            rawUncalGyroY = latestInput.rawGyroY,
            rawUncalGyroZ = latestInput.rawGyroZ,
            processedYaw = processedYaw,
            processedPitch = processedPitch,
            flightPitchDeg = flightPitchDeg,
            flightRollDeg = flightRollDeg,
            flightYawDeg = flightYawDeg
        )

        // ── Sensor Zero Baseline Action with Interactive Morphing Feedback ──
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            BoxWithConstraints(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                val isNarrow = maxWidth < 520.dp
                if (isNarrow) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Column {
                            Text("Sensor Zero Calibration", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("Re-align resting attitude if phone was tilted when connecting.", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        RecalibrateZeroButton(onRecalibrate = onRecalibrate)
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f, fill = false)) {
                            Text("Sensor Zero Calibration", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("Re-align resting attitude if phone was tilted when connecting.", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        RecalibrateZeroButton(onRecalibrate = onRecalibrate)
                    }
                }
            }
        }

        // ── Optional Advanced Raw Sensor Readout (Progressive Disclosure) ──
        var showRawTelemetry by remember { mutableStateOf(false) }
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showRawTelemetry = !showRawTelemetry },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = null,
                            tint = NeonPalette.CardIdleText,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Advanced Raw Sensor Telemetry",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                    Icon(
                        imageVector = if (showRawTelemetry) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = NeonPalette.CardIdleText,
                        modifier = Modifier.size(20.dp)
                    )
                }

                AnimatedVisibility(
                    visible = showRawTelemetry,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    BoxWithConstraints(modifier = Modifier.padding(top = 12.dp).fillMaxWidth()) {
                        val isNarrow = maxWidth < 460.dp
                        if (isNarrow) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Column {
                                    Text("Calibrated Gyroscope (rad/s)", fontSize = 11.sp, color = NeonPalette.Cyan)
                                    Text(
                                        text = String.format("X: %+.2f   Y: %+.2f   Z: %+.2f", latestInput.gyroX, latestInput.gyroY, latestInput.gyroZ),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = Color.White
                                    )
                                }
                                Column {
                                    Text("Accelerometer (m/s²)", fontSize = 11.sp, color = NeonPalette.Purple)
                                    Text(
                                        text = String.format("X: %+.2f   Y: %+.2f   Z: %+.2f", latestInput.accelX, latestInput.accelY, latestInput.accelZ),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = Color.White
                                    )
                                }
                            }
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f, fill = false)) {
                                    Text("Calibrated Gyroscope (rad/s)", fontSize = 11.sp, color = NeonPalette.Cyan)
                                    Text(
                                        text = String.format("X: %+.2f   Y: %+.2f   Z: %+.2f", latestInput.gyroX, latestInput.gyroY, latestInput.gyroZ),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = Color.White
                                    )
                                }
                                Column(modifier = Modifier.weight(1f, fill = false)) {
                                    Text("Accelerometer (m/s²)", fontSize = 11.sp, color = NeonPalette.Purple)
                                    Text(
                                        text = String.format("X: %+.2f   Y: %+.2f   Z: %+.2f", latestInput.accelX, latestInput.accelY, latestInput.accelZ),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoCalloutPill(
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    accentColor: Color
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainer,
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.25f))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(18.dp)
                )
            }
            Column {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = NeonPalette.CardIdleText,
                    lineHeight = 15.sp
                )
            }
        }
    }
}

@Composable
private fun RecalibrateZeroButton(
    onRecalibrate: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var isCalibrated by remember { mutableStateOf(false) }

    val containerColor by animateColorAsState(
        targetValue = if (isCalibrated) NeonPalette.ConnectedDot.copy(alpha = 0.25f) else NeonPalette.Purple.copy(alpha = 0.20f),
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
    )
    val contentColor by animateColorAsState(
        targetValue = if (isCalibrated) NeonPalette.ConnectedDot else NeonPalette.Purple
    )

    Button(
        onClick = {
            if (!isCalibrated) {
                onRecalibrate()
                scope.launch {
                    isCalibrated = true
                    delay(2500)
                    isCalibrated = false
                }
            }
        },
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        border = BorderStroke(1.dp, contentColor.copy(alpha = 0.4f)),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 10.dp)
    ) {
        AnimatedContent(
            targetState = isCalibrated,
            transitionSpec = {
                (fadeIn() + scaleIn(initialScale = 0.85f))
                    .togetherWith(fadeOut() + scaleOut(targetScale = 0.85f))
            },
            label = "recalibrate_button_morph"
        ) { calibrated ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = if (calibrated) Icons.Default.CheckCircle else Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = if (calibrated) "✓ Calibrated to Current Posture" else "Recalibrate Zero",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    maxLines = 1,
                    softWrap = false
                )
            }
        }
    }
}

@Composable
private fun StatusTile(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    subtitle: String? = null,
    color: Color,
    showIndicator: Boolean = false
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(title, fontSize = 11.sp, color = NeonPalette.CardIdleText)
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (showIndicator) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(color)
                    )
                }
                Text(value, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = color)
            }
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(subtitle, fontSize = 11.sp, color = Color.White.copy(alpha = 0.5f))
            }
        }
    }
}
