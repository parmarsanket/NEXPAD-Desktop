package com.sanket.tools.nexpaddesktop.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Refresh
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sanket.tools.nexpad.model.GamepadInput
import com.sanket.tools.nexpaddesktop.model.GyroSettings
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
    onRecalibrate: () -> Unit
) {
    val scrollState = rememberScrollState()

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // ── Top Title ──
            Column {
                Text(
                    text = "CONTROLLER & MOTION",
                    style = TextStyle(
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp,
                        brush = Brush.linearGradient(
                            colors = listOf(Color.White, NeonPalette.Cyan, NeonPalette.Purple)
                        )
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Select your target gamepad emulation profile and configure motion control parameters.",
                    color = NeonPalette.CardIdleText,
                    fontSize = 13.sp
                )
            }

            // ── Controller Mode Selector Cards ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ControllerProfileCard(
                    modifier = Modifier.weight(1f),
                    title = "Xbox 360",
                    subtitle = "Virtual XInput (ViGEm)",
                    badge = "Motion Translation Active",
                    accentColor = NeonPalette.Cyan,
                    isSelected = activeController == ControllerType.XBOX_360,
                    description = "Converts phone sensors to analog stick aiming & steering. Tailored for Windows PC gaming without native gyro.",
                    onClick = { onSaveController(ControllerType.XBOX_360) }
                )

                ControllerProfileCard(
                    modifier = Modifier.weight(1f),
                    title = "DualShock 4",
                    subtitle = "DirectInput & DSU Motion",
                    badge = "Native Passthrough",
                    accentColor = NeonPalette.Purple,
                    isSelected = activeController == ControllerType.DUALSHOCK_4,
                    description = "100% raw 6-axis motion stream directly to games, Steam Input, and emulators. In-game settings control sensitivity.",
                    onClick = { onSaveController(ControllerType.DUALSHOCK_4) }
                )
            }

            // ════════════════════════════════════════════════════
            //  BRANCH: DUALSHOCK 4 NATIVE MODE
            // ════════════════════════════════════════════════════
            if (activeController == ControllerType.DUALSHOCK_4) {
                DualShock4Dashboard(
                    latestInput = latestInput,
                    isDriverConnected = isDriverConnected,
                    dsuClientCount = dsuClientCount,
                    gyroSettings = gyroSettings,
                    processedYaw = processedYaw,
                    processedPitch = processedPitch,
                    onRecalibrate = onRecalibrate
                )
            } else {
                // ════════════════════════════════════════════════════
                //  BRANCH: XBOX 360 MOTION CONTROL CENTER
                // ════════════════════════════════════════════════════
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
                    onRecalibrate = onRecalibrate
                )

                // ── Gamepad Base Sticks Sensitivity ──
                ModernSectionCard(
                    title = "Analog Sticks Base Sensitivity",
                    icon = Icons.Default.Tune,
                    onReset = {
                        onRsSensitivityXChange(1.0f)
                        onRsSensitivityYChange(1.0f)
                        onLsSensitivityXChange(1.0f)
                        onLsSensitivityYChange(1.0f)
                    },
                    resetTooltip = "Reset Stick Sensitivities to 1.00×"
                ) {
                    Text(
                        "Hardware scaling applied to the physical on-screen thumbsticks from your phone.",
                        fontSize = 12.sp,
                        color = NeonPalette.CardIdleText
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Text("Right Stick (Camera / Look)", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = NeonPalette.Cyan)
                    ModernSlider(
                        label = "RS Horizontal Sensitivity",
                        value = rsSensitivityX,
                        valueRange = 0.1f..3.0f,
                        format = "%.2f×",
                        onValueChange = onRsSensitivityXChange
                    )
                    ModernSlider(
                        label = "RS Vertical Sensitivity",
                        value = rsSensitivityY,
                        valueRange = 0.1f..3.0f,
                        format = "%.2f×",
                        onValueChange = onRsSensitivityYChange
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Left Stick (Movement)", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = NeonPalette.Cyan)
                    ModernSlider(
                        label = "LS Horizontal Sensitivity",
                        value = lsSensitivityX,
                        valueRange = 0.1f..3.0f,
                        format = "%.2f×",
                        onValueChange = onLsSensitivityXChange
                    )
                    ModernSlider(
                        label = "LS Vertical Sensitivity",
                        value = lsSensitivityY,
                        valueRange = 0.1f..3.0f,
                        format = "%.2f×",
                        onValueChange = onLsSensitivityYChange
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

/**
 * Modern Material 3 Controller Profile Selection Card.
 */
@Composable
private fun ControllerProfileCard(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    badge: String,
    accentColor: Color,
    isSelected: Boolean,
    description: String,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    var cardModifier = modifier
        .hoverable(interactionSource)
        .clickable(interactionSource, indication = null, onClick = onClick)

    if (isSelected) {
        cardModifier = cardModifier.selectedGlow(cornerRadius = 16.dp)
    }

    Surface(
        modifier = cardModifier,
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f),
        border = BorderStroke(
            if (isSelected) 2.dp else 1.dp,
            if (isSelected) accentColor else if (isHovered) Color.White.copy(alpha = 0.3f) else NeonPalette.CardIdleBorder
        )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = accentColor.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, accentColor.copy(alpha = 0.35f))
                ) {
                    Text(
                        text = badge,
                        color = accentColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                if (isSelected) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    Icons.Default.SportsEsports,
                    contentDescription = null,
                    tint = if (isSelected) accentColor else Color.Gray,
                    modifier = Modifier.size(28.dp)
                )
                Column {
                    Text(
                        text = title,
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        color = Color.White
                    )
                    Text(
                        text = subtitle,
                        fontSize = 12.sp,
                        color = NeonPalette.CardIdleText
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = description,
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.75f),
                lineHeight = 16.sp
            )
        }
    }
}

/**
 * Dedicated DualShock 4 Native Mode Dashboard.
 *
 * Informs the user that raw motion is passed directly to games/emulators,
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
    onRecalibrate: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ── Native Passthrough Banner ──
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
            border = BorderStroke(1.dp, NeonPalette.Purple.copy(alpha = 0.4f))
        ) {
            Column(modifier = Modifier.padding(22.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = NeonPalette.Purple.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, NeonPalette.Purple.copy(alpha = 0.4f)),
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("🎮", fontSize = 20.sp)
                        }
                    }
                    Column {
                        Text(
                            text = "DualShock 4 Native Hardware Passthrough",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color.White
                        )
                        Text(
                            text = "Zero-manipulation raw 6-axis IMU stream to games and emulators",
                            fontSize = 12.sp,
                            color = NeonPalette.CardIdleText
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "DualShock 4 features native gyro & accelerometer support across modern PC gaming. " +
                            "NEXPAD delivers high-frequency uncompressed sensor packets directly into the virtual DS4 driver and DSU Server.\n\n" +
                            "• All aiming sensitivity, steering deadzones, and inverted axes are configured directly inside your PC game's settings, Steam Input, or emulator settings.\n" +
                            "• Emulation knobs (smoothing, curve acceleration) are bypassed to maintain bit-perfect hardware compliance.",
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.85f),
                    lineHeight = 18.sp
                )
            }
        }

        // ── Telemetry & Service Status Tiles ──
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatusTile(
                modifier = Modifier.weight(1f),
                title = "ViGEm DS4 Driver",
                value = if (isDriverConnected) "Connected" else "Waiting...",
                color = if (isDriverConnected) NeonPalette.ConnectedDot else NeonPalette.CardIdleText
            )

            StatusTile(
                modifier = Modifier.weight(1f),
                title = "DSU Server (26760)",
                value = "$dsuClientCount Active Client${if (dsuClientCount != 1) "s" else ""}",
                color = NeonPalette.Cyan
            )

            StatusTile(
                modifier = Modifier.weight(1f),
                title = "Sensor Mode",
                value = "0x00 (IMU 6-Axis)",
                color = NeonPalette.Purple
            )
        }

        // ── Real-Time Raw Sensor Readout ──
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
            border = BorderStroke(1.dp, NeonPalette.CardIdleBorder)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "Live Hardware Sensor Telemetry",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Calibrated Gyroscope (rad/s)", fontSize = 12.sp, color = NeonPalette.Cyan)
                        Text(
                            text = String.format("X: %+.2f   Y: %+.2f   Z: %+.2f", latestInput.gyroX, latestInput.gyroY, latestInput.gyroZ),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color.White
                        )
                    }

                    Column {
                        Text("Accelerometer (m/s²)", fontSize = 12.sp, color = NeonPalette.Purple)
                        Text(
                            text = String.format("X: %+.2f   Y: %+.2f   Z: %+.2f", latestInput.accelX, latestInput.accelY, latestInput.accelZ),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color.White
                        )
                    }
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
            processedPitch = processedPitch
        )

        // ── Sensor Zero Baseline Action ──
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f),
            border = BorderStroke(1.dp, NeonPalette.CardIdleBorder)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Sensor Zero Calibration", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
                    Text("Re-align resting attitude if phone was tilted when connecting.", fontSize = 12.sp, color = NeonPalette.CardIdleText)
                }
                Button(
                    onClick = onRecalibrate,
                    colors = ButtonDefaults.buttonColors(containerColor = NeonPalette.Purple.copy(alpha = 0.25f), contentColor = NeonPalette.Purple)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Recalibrate Zero")
                }
            }
        }
    }
}

@Composable
private fun StatusTile(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    color: Color
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        border = BorderStroke(1.dp, NeonPalette.CardIdleBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(title, fontSize = 11.sp, color = NeonPalette.CardIdleText)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = color)
        }
    }
}
