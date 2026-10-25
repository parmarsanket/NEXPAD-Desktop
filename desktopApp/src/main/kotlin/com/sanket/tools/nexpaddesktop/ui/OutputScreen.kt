package com.sanket.tools.nexpaddesktop.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sanket.tools.nexpaddesktop.ui.components.DesktopVerticalScrollbar
import com.sanket.tools.nexpaddesktop.ui.theme.NeonPalette
import com.sanket.tools.nexpaddesktop.viewmodel.DesktopViewModel
import kotlin.math.PI

/**
 * OutputScreen is the dedicated UDP Motion & Sensor Output Control Hub.
 *
 * Provides complete control, status indicators, live telemetry, and client setup guides
 * for the standard UDP motion server protocol on port 26760 for motion-aware games and applications.
 *
 * Fully responsive across all desktop window dimensions using adaptive layout breakpoints.
 */
@Composable
fun OutputScreen(
    viewModel: DesktopViewModel
) {
    val scrollState = rememberScrollState()
    val clipboardManager = LocalClipboardManager.current
    var isCopied by remember { mutableStateOf(false) }
    var selectedGuideTab by remember { mutableStateOf(0) }

    LaunchedEffect(isCopied) {
        if (isCopied) {
            kotlinx.coroutines.delay(2000)
            isCopied = false
        }
    }

    val isDsuRunning = viewModel.isDsuServerEnabled && viewModel.activeController == ControllerType.DUALSHOCK_4

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isNarrow = maxWidth < 700.dp
        val horizontalPadding = if (isNarrow) 16.dp else 24.dp

        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(NeonPalette.PanelBgBottom)
                    .padding(horizontal = horizontalPadding, vertical = 20.dp)
                    .padding(end = 16.dp)
                    .verticalScroll(scrollState)
            ) {
            // ── Top Screen Header ──
            if (isNarrow) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HeaderIconBox()
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "UDP MOTION & SENSOR HUB",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "UDP Motion Provider Server (Port 26760)",
                                color = NeonPalette.CardIdleText,
                                fontSize = 11.sp
                            )
                        }
                    }
                    StatusPill(isDsuRunning = isDsuRunning, isEnabled = viewModel.isDsuServerEnabled)
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HeaderIconBox()
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = "UDP MOTION & SENSOR OUTPUT HUB",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Standard UDP Motion Provider Server (Port 26760) for Motion-Aware Applications & Games",
                                color = NeonPalette.CardIdleText,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))
                    StatusPill(isDsuRunning = isDsuRunning, isEnabled = viewModel.isDsuServerEnabled)
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // ── Main UDP Motion Server Switch & Configuration Card ──
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF101524))
                    .border(
                        1.dp,
                        if (isDsuRunning) NeonPalette.Cyan.copy(alpha = 0.4f) else NeonPalette.CardIdleBorder,
                        RoundedCornerShape(14.dp)
                    )
                    .padding(if (isNarrow) 14.dp else 20.dp)
            ) {
                Column {
                    // Header with Title & Master Switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "UDP Motion & Sensor Server",
                                    color = Color.White,
                                    fontSize = if (isNarrow) 14.sp else 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(NeonPalette.Purple.copy(alpha = 0.2f))
                                        .border(1.dp, NeonPalette.Purple.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                                        .padding(horizontal = 7.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "Port 26760",
                                        color = NeonPalette.Purple,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Streams real-time 6-axis gyroscope, accelerometer, analog triggers, and gamepad inputs over low-latency UDP.",
                                color = NeonPalette.CardIdleText,
                                fontSize = 11.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        // Motion Server ON / OFF Master Switch
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF161E30))
                                .border(1.dp, Color(0xFF222E4A), RoundedCornerShape(10.dp))
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = if (viewModel.isDsuServerEnabled) "ON" else "OFF",
                                color = if (viewModel.isDsuServerEnabled) NeonPalette.Cyan else Color.Gray,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Switch(
                                checked = viewModel.isDsuServerEnabled,
                                onCheckedChange = { viewModel.updateDsuServerEnabled(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = NeonPalette.Cyan,
                                    checkedTrackColor = NeonPalette.Cyan.copy(alpha = 0.35f),
                                    uncheckedThumbColor = Color.Gray,
                                    uncheckedTrackColor = Color(0xFF111726)
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Server Status Details: Adaptive Row / Column
                    if (isNarrow) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            ServerAddressCard(
                                isCopied = isCopied,
                                onCopy = {
                                    clipboardManager.setText(buildAnnotatedString { append("127.0.0.1:26760") })
                                    isCopied = true
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                            ConnectedClientsCard(
                                clientCount = viewModel.dsuClientCount,
                                modifier = Modifier.fillMaxWidth()
                            )
                            ActiveProfileCard(
                                activeController = viewModel.activeController,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            ServerAddressCard(
                                isCopied = isCopied,
                                onCopy = {
                                    clipboardManager.setText(buildAnnotatedString { append("127.0.0.1:26760") })
                                    isCopied = true
                                },
                                modifier = Modifier.weight(1f)
                            )
                            ConnectedClientsCard(
                                clientCount = viewModel.dsuClientCount,
                                modifier = Modifier.weight(1f)
                            )
                            ActiveProfileCard(
                                activeController = viewModel.activeController,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // Gating Warning Banner if not in DualShock 4 mode
                    if (viewModel.activeController != ControllerType.DUALSHOCK_4 && viewModel.isDsuServerEnabled) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFFFB300).copy(alpha = 0.1f))
                                .border(1.dp, Color(0xFFFFB300).copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                                .padding(12.dp)
                        ) {
                            if (isNarrow) {
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Warning, contentDescription = "Warning", tint = Color(0xFFFFB300), modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Motion Server requires 6-Axis Motion Controller mode",
                                            color = Color(0xFFFFB300),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Text(
                                        text = "Connected applications require 6-axis motion telemetry. Switch active controller mode to Motion Gamepad to begin streaming.",
                                        color = Color(0xFFE0E0E0),
                                        fontSize = 11.sp
                                    )
                                    Button(
                                        onClick = { viewModel.updateActiveController(ControllerType.DUALSHOCK_4) },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFB300), contentColor = Color.Black),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Enable Motion Mode", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            } else {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Warning, contentDescription = "Warning", tint = Color(0xFFFFB300), modifier = Modifier.size(20.dp))
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = "Motion Server requires 6-Axis Motion Controller mode",
                                                color = Color(0xFFFFB300),
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "Connected applications require 6-axis motion telemetry. Switch active controller mode to Motion Gamepad to begin streaming.",
                                                color = Color(0xFFE0E0E0),
                                                fontSize = 11.sp
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Button(
                                        onClick = { viewModel.updateActiveController(ControllerType.DUALSHOCK_4) },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFB300), contentColor = Color.Black),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                                    ) {
                                        Text("Enable Motion Mode", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // ── Live Motion & Telemetry Preview Card ──
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF101524))
                    .border(1.dp, Color(0xFF1A2338), RoundedCornerShape(14.dp))
                    .padding(if (isNarrow) 14.dp else 20.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Sensors, contentDescription = "Telemetry", tint = NeonPalette.Cyan, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Live UDP Motion Stream Telemetry",
                                color = Color.White,
                                fontSize = if (isNarrow) 13.sp else 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Text(
                            text = "100-Byte Wire Standard",
                            color = NeonPalette.Green,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    val input = viewModel.latestInput
                    val radToDeg = (180f / PI.toFloat())
                    val pitchDps = input.gyroX * radToDeg
                    val yawDps = input.gyroZ * radToDeg
                    val rollDps = input.gyroY * radToDeg

                    val axG = input.accelX / 9.80665f
                    val ayG = input.accelY / 9.80665f
                    val azG = if (input.accelX != 0f || input.accelY != 0f || input.accelZ != 0f) input.accelZ / 9.80665f else 1.0f

                    if (isNarrow) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            TelemetryItem(
                                title = "GYROSCOPE (deg/s)",
                                color = NeonPalette.Cyan,
                                values = listOf(
                                    "Pitch (X)" to String.format("%+.1f°/s", pitchDps),
                                    "Yaw (Z)" to String.format("%+.1f°/s", yawDps),
                                    "Roll (Y)" to String.format("%+.1f°/s", rollDps)
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                            TelemetryItem(
                                title = "ACCELEROMETER (G)",
                                color = NeonPalette.Purple,
                                values = listOf(
                                    "Accel X" to String.format("%+.2f G", axG),
                                    "Accel Y" to String.format("%+.2f G", ayG),
                                    "Accel Z" to String.format("%+.2f G", azG)
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                            TelemetryItem(
                                title = "ANALOG CONTROLS (0-255)",
                                color = NeonPalette.Green,
                                values = listOf(
                                    "Left Stick" to "${((input.leftStickX + 1f) * 127.5f).toInt()}, ${((input.leftStickY + 1f) * 127.5f).toInt()}",
                                    "Right Stick" to "${((input.rightStickX + 1f) * 127.5f).toInt()}, ${((input.rightStickY + 1f) * 127.5f).toInt()}",
                                    "Triggers (L2/R2)" to "${(input.triggerL2 * 255).toInt()}, ${(input.triggerR2 * 255).toInt()}"
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    } else {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            TelemetryItem(
                                title = "GYROSCOPE (deg/s)",
                                color = NeonPalette.Cyan,
                                values = listOf(
                                    "Pitch (X)" to String.format("%+.1f°/s", pitchDps),
                                    "Yaw (Z)" to String.format("%+.1f°/s", yawDps),
                                    "Roll (Y)" to String.format("%+.1f°/s", rollDps)
                                ),
                                modifier = Modifier.weight(1f)
                            )
                            TelemetryItem(
                                title = "ACCELEROMETER (G)",
                                color = NeonPalette.Purple,
                                values = listOf(
                                    "Accel X" to String.format("%+.2f G", axG),
                                    "Accel Y" to String.format("%+.2f G", ayG),
                                    "Accel Z" to String.format("%+.2f G", azG)
                                ),
                                modifier = Modifier.weight(1f)
                            )
                            TelemetryItem(
                                title = "ANALOG CONTROLS (0-255)",
                                color = NeonPalette.Green,
                                values = listOf(
                                    "Left Stick" to "${((input.leftStickX + 1f) * 127.5f).toInt()}, ${((input.leftStickY + 1f) * 127.5f).toInt()}",
                                    "Right Stick" to "${((input.rightStickX + 1f) * 127.5f).toInt()}, ${((input.rightStickY + 1f) * 127.5f).toInt()}",
                                    "Triggers (L2/R2)" to "${(input.triggerL2 * 255).toInt()}, ${(input.triggerR2 * 255).toInt()}"
                                ),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // ── Client Configuration & Integration Guide ──
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF101524))
                    .border(1.dp, Color(0xFF1A2338), RoundedCornerShape(14.dp))
                    .padding(if (isNarrow) 14.dp else 20.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, contentDescription = "Guide", tint = NeonPalette.Purple, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Client Configuration & Integration Guide",
                            color = Color.White,
                            fontSize = if (isNarrow) 13.sp else 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    val guideTabs = listOf(
                        "Connection Setup",
                        "6-Axis Motion Sensors",
                        "Analog & Controls",
                        "Diagnostics"
                    )

                    if (isNarrow) {
                        // 2x2 grid of tabs on narrow viewports
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                TabButton(title = guideTabs[0], isSelected = selectedGuideTab == 0, onClick = { selectedGuideTab = 0 }, modifier = Modifier.weight(1f))
                                TabButton(title = guideTabs[1], isSelected = selectedGuideTab == 1, onClick = { selectedGuideTab = 1 }, modifier = Modifier.weight(1f))
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                TabButton(title = guideTabs[2], isSelected = selectedGuideTab == 2, onClick = { selectedGuideTab = 2 }, modifier = Modifier.weight(1f))
                                TabButton(title = guideTabs[3], isSelected = selectedGuideTab == 3, onClick = { selectedGuideTab = 3 }, modifier = Modifier.weight(1f))
                            }
                        }
                    } else {
                        // 1 row of 4 tabs on normal viewports
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            guideTabs.forEachIndexed { index, name ->
                                TabButton(
                                    title = name,
                                    isSelected = selectedGuideTab == index,
                                    onClick = { selectedGuideTab = index },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Guide Content Box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF090D18))
                            .border(1.dp, Color(0xFF182035), RoundedCornerShape(10.dp))
                            .padding(14.dp)
                    ) {
                        when (selectedGuideTab) {
                            0 -> GuideSection(
                                title = "Client Application Configuration",
                                steps = listOf(
                                    "Set the client's Motion Server IP address to 127.0.0.1 (local loopback) or your PC's local LAN IP for networked devices.",
                                    "Set the Server Port to 26760 (the standard open UDP motion server port).",
                                    "Select Controller Slot 0 (or Pad 1 / Device 1 in your client application).",
                                    "Ensure NEXPAD's active controller profile is set to Motion Gamepad mode with the server switch turned ON above.",
                                    "Once your target game or tool initiates motion polling, the active connected clients counter will increment in real time."
                                )
                            )
                            1 -> GuideSection(
                                title = "Rotational & Gravitational Telemetry",
                                steps = listOf(
                                    "Gyroscope streams real-time angular velocity across Pitch (X), Yaw (Z), and Roll (Y) in degrees per second (°/s).",
                                    "Accelerometer streams 3D linear and gravitational acceleration normalized to standard Earth gravity (1.0 G at rest).",
                                    "Packets include microsecond-precision hardware timestamps to allow connected applications to calculate jitter-free orientation.",
                                    "Use the Recalibrate Sensors function on the Controller screen whenever you need to zero out drift while playing."
                                )
                            )
                            2 -> GuideSection(
                                title = "Input Channels & Sensitivity",
                                steps = listOf(
                                    "Dual analog thumbsticks stream full 8-bit precision (0 to 255), with rest deadzone centered at 127 / 128.",
                                    "Analog triggers (L2 / R2) output continuous pressure curves from 0 (released) to 255 (fully depressed).",
                                    "Digital actions include directional D-Pad, primary action buttons, shoulder bumpers, stick clicks, and system buttons.",
                                    "Digital button states map directly to standard controller conventions for immediate drop-in compatibility."
                                )
                            )
                            3 -> GuideSection(
                                title = "Socket Communication & Troubleshooting",
                                steps = listOf(
                                    "Local communication between NEXPAD and client software on the same PC has < 1ms near-zero network latency.",
                                    "If connecting from another device on the local network, ensure incoming UDP port 26760 is allowed in Windows Firewall.",
                                    "Connected clients send periodic heartbeat handshakes; inactive clients are automatically pruned after 5 seconds.",
                                    "The live telemetry readout above monitors raw packet data directly before it is transmitted across the UDP socket."
                                )
                            )
                        }
                    }
                }
            }
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
}

@Composable
private fun HeaderIconBox() {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(NeonPalette.Cyan.copy(alpha = 0.12f))
            .border(1.dp, NeonPalette.Cyan.copy(alpha = 0.4f), RoundedCornerShape(10.dp)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Output,
            contentDescription = "Output Hub",
            tint = NeonPalette.Cyan,
            modifier = Modifier.size(22.dp)
        )
    }
}

@Composable
private fun StatusPill(
    isDsuRunning: Boolean,
    isEnabled: Boolean
) {
    Box(
        modifier = Modifier
            .wrapContentWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(
                when {
                    isDsuRunning -> NeonPalette.Green.copy(alpha = 0.15f)
                    !isEnabled -> Color.White.copy(alpha = 0.08f)
                    else -> Color(0xFFFFB300).copy(alpha = 0.15f)
                }
            )
            .border(
                1.dp,
                when {
                    isDsuRunning -> NeonPalette.Green.copy(alpha = 0.5f)
                    !isEnabled -> NeonPalette.CardIdleBorder
                    else -> Color(0xFFFFB300).copy(alpha = 0.5f)
                },
                RoundedCornerShape(20.dp)
            )
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.wrapContentWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            isDsuRunning -> NeonPalette.Green
                            !isEnabled -> Color.Gray
                            else -> Color(0xFFFFB300)
                        }
                    )
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = when {
                    isDsuRunning -> "SERVER BROADCASTING"
                    !isEnabled -> "SERVER DISABLED"
                    else -> "MOTION MODE REQUIRED"
                },
                color = when {
                    isDsuRunning -> NeonPalette.Green
                    !isEnabled -> Color.LightGray
                    else -> Color(0xFFFFB300)
                },
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                softWrap = false
            )
        }
    }
}

@Composable
private fun ServerAddressCard(
    isCopied: Boolean,
    onCopy: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF0C101C))
            .border(1.dp, Color(0xFF1E283E), RoundedCornerShape(10.dp))
            .padding(14.dp)
    ) {
        Column {
            Text(text = "SERVER ADDRESS", color = NeonPalette.CardIdleText, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "127.0.0.1 : 26760", color = NeonPalette.Cyan, fontSize = 14.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .clickable(onClick = onCopy)
                    .padding(vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Copy", tint = NeonPalette.Cyan, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = if (isCopied) "Copied!" else "Copy IP & Port", color = NeonPalette.Cyan, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun ConnectedClientsCard(
    clientCount: Int,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF0C101C))
            .border(1.dp, Color(0xFF1E283E), RoundedCornerShape(10.dp))
            .padding(14.dp)
    ) {
        Column {
            Text(text = "CONNECTED CLIENTS", color = NeonPalette.CardIdleText, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "$clientCount Client${if (clientCount != 1) "s" else ""}",
                color = if (clientCount > 0) NeonPalette.Green else Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (clientCount > 0) "Streaming 100-byte motion packets" else "Waiting for client connection...",
                color = NeonPalette.CardIdleText,
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun ActiveProfileCard(
    activeController: ControllerType,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF0C101C))
            .border(1.dp, Color(0xFF1E283E), RoundedCornerShape(10.dp))
            .padding(14.dp)
    ) {
        Column {
            Text(text = "ACTIVE PROFILE", color = NeonPalette.CardIdleText, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = activeController.displayName.substringBefore(" ("),
                color = if (activeController == ControllerType.DUALSHOCK_4) NeonPalette.Cyan else Color(0xFFFFB300),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (activeController == ControllerType.DUALSHOCK_4) "Full 6-Axis Motion Stream Active" else "Motion Gated (Switch to Motion Mode)",
                color = NeonPalette.CardIdleText,
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun TabButton(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) NeonPalette.Cyan.copy(alpha = 0.15f) else Color(0xFF161E30))
            .border(1.dp, if (isSelected) NeonPalette.Cyan else Color(0xFF222E4A), RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            color = if (isSelected) NeonPalette.Cyan else Color.LightGray,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            maxLines = 1,
            softWrap = false
        )
    }
}

@Composable
private fun TelemetryItem(
    title: String,
    color: Color,
    values: List<Pair<String, String>>,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF090D18))
            .border(1.dp, Color(0xFF182035), RoundedCornerShape(10.dp))
            .padding(12.dp)
    ) {
        Column {
            Text(text = title, color = color, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            values.forEach { (label, value) ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = label, color = NeonPalette.CardIdleText, fontSize = 11.sp)
                    Text(text = value, color = Color.White, fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun GuideSection(
    title: String,
    steps: List<String>
) {
    Column {
        Text(text = title, color = NeonPalette.Cyan, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(10.dp))
        steps.forEachIndexed { index, step ->
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                Text(text = "${index + 1}. ", color = NeonPalette.Purple, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text(text = step, color = Color(0xFFD0D7E5), fontSize = 12.sp, lineHeight = 17.sp)
            }
        }
    }
}
