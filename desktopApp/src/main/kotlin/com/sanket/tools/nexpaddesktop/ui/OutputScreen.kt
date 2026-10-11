package com.sanket.tools.nexpaddesktop.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sanket.tools.nexpaddesktop.ui.theme.NeonPalette
import com.sanket.tools.nexpaddesktop.viewmodel.DesktopViewModel
import kotlin.math.PI

/**
 * OutputScreen is the dedicated Emulator & Motion Output Control Hub.
 *
 * Provides complete control, status indicators, live telemetry, and setup guides
 * for the Cemuhook DSU (DualShock UDP) protocol on port 26760 across all major emulators.
 */
@Composable
fun OutputScreen(
    viewModel: DesktopViewModel
) {
    val scrollState = rememberScrollState()
    val clipboardManager = LocalClipboardManager.current
    var isCopied by remember { mutableStateOf(false) }
    var selectedEmulatorTab by remember { mutableStateOf(0) }

    LaunchedEffect(isCopied) {
        if (isCopied) {
            kotlinx.coroutines.delay(2000)
            isCopied = false
        }
    }

    val isDsuRunning = viewModel.isDsuServerEnabled && viewModel.activeController == ControllerType.DUALSHOCK_4

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NeonPalette.PanelBgBottom)
            .padding(24.dp)
            .verticalScroll(scrollState)
    ) {
        // ── Top Screen Header ──
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(NeonPalette.Cyan.copy(alpha = 0.12f))
                        .border(1.dp, NeonPalette.Cyan.copy(alpha = 0.4f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Output,
                        contentDescription = "Output Hub",
                        tint = NeonPalette.Cyan,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = "EMULATOR & MOTION OUTPUT HUB",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Cemuhook DSU UDP Protocol Server (Port 26760) for RPCS3, Cemu, Dolphin, Yuzu & Ryujinx",
                        color = NeonPalette.CardIdleText,
                        fontSize = 12.sp
                    )
                }
            }

            // Quick Status Pill
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        when {
                            isDsuRunning -> NeonPalette.Green.copy(alpha = 0.15f)
                            !viewModel.isDsuServerEnabled -> Color.White.copy(alpha = 0.08f)
                            else -> Color(0xFFFFB300).copy(alpha = 0.15f)
                        }
                    )
                    .border(
                        1.dp,
                        when {
                            isDsuRunning -> NeonPalette.Green.copy(alpha = 0.5f)
                            !viewModel.isDsuServerEnabled -> NeonPalette.CardIdleBorder
                            else -> Color(0xFFFFB300).copy(alpha = 0.5f)
                        },
                        RoundedCornerShape(20.dp)
                    )
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    isDsuRunning -> NeonPalette.Green
                                    !viewModel.isDsuServerEnabled -> Color.Gray
                                    else -> Color(0xFFFFB300)
                                }
                            )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = when {
                            isDsuRunning -> "SERVER BROADCASTING"
                            !viewModel.isDsuServerEnabled -> "SERVER DISABLED"
                            else -> "DS4 MODE REQUIRED"
                        },
                        color = when {
                            isDsuRunning -> NeonPalette.Green
                            !viewModel.isDsuServerEnabled -> Color.LightGray
                            else -> Color(0xFFFFB300)
                        },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ── Main DSU Server Switch & Configuration Card ──
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
                .padding(20.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "DSU Motion Server (Cemuhook)",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(NeonPalette.Purple.copy(alpha = 0.2f))
                                    .border(1.dp, NeonPalette.Purple.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "UDP Port 26760",
                                    color = NeonPalette.Purple,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Streams 6-axis gyroscope, accelerometer, analog triggers, and full button packets to emulators over low-latency UDP.",
                            color = NeonPalette.CardIdleText,
                            fontSize = 12.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // DSU Server ON / OFF Master Switch
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF161E30))
                            .border(1.dp, Color(0xFF222E4A), RoundedCornerShape(10.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = if (viewModel.isDsuServerEnabled) "ON" else "OFF",
                            color = if (viewModel.isDsuServerEnabled) NeonPalette.Cyan else Color.Gray,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(10.dp))
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

                Spacer(modifier = Modifier.height(18.dp))

                // Server Status Details Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Metric 1: IP & Port
                    Box(
                        modifier = Modifier
                            .weight(1f)
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
                                    .clickable {
                                        clipboardManager.setText(buildAnnotatedString { append("127.0.0.1:26760") })
                                        isCopied = true
                                    }
                                    .padding(vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Copy", tint = NeonPalette.Cyan, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = if (isCopied) "Copied!" else "Copy IP & Port", color = NeonPalette.Cyan, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }

                    // Metric 2: Active Clients
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF0C101C))
                            .border(1.dp, Color(0xFF1E283E), RoundedCornerShape(10.dp))
                            .padding(14.dp)
                    ) {
                        Column {
                            Text(text = "CONNECTED EMULATORS", color = NeonPalette.CardIdleText, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${viewModel.dsuClientCount} Client${if (viewModel.dsuClientCount != 1) "s" else ""}",
                                color = if (viewModel.dsuClientCount > 0) NeonPalette.Green else Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (viewModel.dsuClientCount > 0) "Streaming 100-byte motion packets" else "Waiting for emulator handshake...",
                                color = NeonPalette.CardIdleText,
                                fontSize = 11.sp
                            )
                        }
                    }

                    // Metric 3: Active Controller Mode
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF0C101C))
                            .border(1.dp, Color(0xFF1E283E), RoundedCornerShape(10.dp))
                            .padding(14.dp)
                    ) {
                        Column {
                            Text(text = "ACTIVE CONTROLLER", color = NeonPalette.CardIdleText, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = viewModel.activeController.displayName.substringBefore(" ("),
                                color = if (viewModel.activeController == ControllerType.DUALSHOCK_4) NeonPalette.Cyan else Color(0xFFFFB300),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (viewModel.activeController == ControllerType.DUALSHOCK_4) "Full 6-Axis Motion Supported" else "DSU Gated (Switch to DS4)",
                                color = NeonPalette.CardIdleText,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                // Gating Warning Banner if in Xbox 360 mode
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
                                        text = "DSU Server is gated to DualShock 4 only",
                                        color = Color(0xFFFFB300),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Emulators require DualShock 4 mode to receive motion sensor telemetry. Switch active controller to DualShock 4 to begin streaming.",
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
                                Text("Switch to DS4", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ── Live Motion & Telemetry Preview Card ──
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFF101524))
                .border(1.dp, Color(0xFF1A2338), RoundedCornerShape(14.dp))
                .padding(20.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Sensors, contentDescription = "Telemetry", tint = NeonPalette.Cyan, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Live UDP Motion Stream Telemetry",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = "100-Byte Cemuhook Wire Standard",
                        color = NeonPalette.Green,
                        fontSize = 11.sp,
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

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Gyroscope Card
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

                    // Accelerometer Card
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

                    // Sticks & Triggers Card
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

        Spacer(modifier = Modifier.height(20.dp))

        // ── Emulator Quick Setup Guide ──
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFF101524))
                .border(1.dp, Color(0xFF1A2338), RoundedCornerShape(14.dp))
                .padding(20.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.SportsEsports, contentDescription = "Guide", tint = NeonPalette.Purple, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Emulator Configuration Guide",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Emulator Selector Tabs
                val emulators = listOf("RPCS3 (PS3)", "Cemu (Wii U)", "Dolphin (Wii/GC)", "Yuzu / Ryujinx (Switch)", "Citra (3DS)", "PCSX2 (PS2)")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    emulators.forEachIndexed { index, name ->
                        val isSelected = selectedEmulatorTab == index
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) NeonPalette.Cyan.copy(alpha = 0.15f) else Color(0xFF161E30))
                                .border(1.dp, if (isSelected) NeonPalette.Cyan else Color(0xFF222E4A), RoundedCornerShape(8.dp))
                                .clickable { selectedEmulatorTab = index }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = name.substringBefore(" "),
                                color = if (isSelected) NeonPalette.Cyan else Color.LightGray,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Guide Content Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF090D18))
                        .border(1.dp, Color(0xFF182035), RoundedCornerShape(10.dp))
                        .padding(16.dp)
                ) {
                    when (selectedEmulatorTab) {
                        0 -> EmulatorInstruction(
                            emulator = "RPCS3 (PlayStation 3)",
                            steps = listOf(
                                "Open RPCS3 and navigate to Manage > Gamepad Settings (Controllers).",
                                "Select Player 1 and set Controller Handler to 'DualShock 4' or 'Sixaxis (Cemuhook UDP)'.",
                                "Under DSU / Cemuhook settings, enter Server IP: 127.0.0.1 and Port: 26760.",
                                "Click 'Test Motion' — you will immediately see the controller pitch and roll in RPCS3!"
                            )
                        )
                        1 -> EmulatorInstruction(
                            emulator = "Cemu (Nintendo Wii U)",
                            steps = listOf(
                                "Open Cemu and go to Options > GamePad motion source > DSU1.",
                                "Select By slot: 0 or enter IP: 127.0.0.1 with Port: 26760.",
                                "Go to Options > Input Settings and ensure your Wii U GamePad profile is selected.",
                                "Breath of the Wild shrines and motion aiming will now work with your phone!"
                            )
                        )
                        2 -> EmulatorInstruction(
                            emulator = "Dolphin (Nintendo GameCube & Wii)",
                            steps = listOf(
                                "Open Dolphin and go to Options > Controller Settings > Alternate Input Sources.",
                                "Check 'Enable DSU Client'.",
                                "Add Server Address: 127.0.0.1 and Port: 26760.",
                                "In Emulated Wiimote configuration, map Motion Input to DSU/0/DualShock 4."
                            )
                        )
                        3 -> EmulatorInstruction(
                            emulator = "Yuzu & Ryujinx (Nintendo Switch)",
                            steps = listOf(
                                "Open Yuzu or Ryujinx and navigate to Emulation > Configure > Controls.",
                                "Under Motion tab, check 'Enable Cemuhook UDP'.",
                                "Set Server IP: 127.0.0.1 and Port: 26760.",
                                "Save and launch your favorite Switch game (Mario Kart, Splatoon, Zelda)."
                            )
                        )
                        4 -> EmulatorInstruction(
                            emulator = "Citra (Nintendo 3DS)",
                            steps = listOf(
                                "Open Citra and navigate to Emulation > Configure > Controls > Motion.",
                                "Select 'Cemuhook UDP' as the motion provider.",
                                "Enter Server IP: 127.0.0.1 and Port: 26760.",
                                "Test gyro rotation in 3DS tilt minigames (Ocarina of Time 3D, WarioWare Gold)."
                            )
                        )
                        5 -> EmulatorInstruction(
                            emulator = "PCSX2 (PlayStation 2)",
                            steps = listOf(
                                "Open PCSX2 and navigate to Settings > Controllers > Controller Settings.",
                                "Enable the DSU / Cemuhook motion input provider.",
                                "Set Server: 127.0.0.1 and Port: 26760.",
                                "Compatible Sixaxis PS2 motion titles will now receive high-frequency gyro data."
                            )
                        )
                    }
                }
            }
        }
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
private fun EmulatorInstruction(
    emulator: String,
    steps: List<String>
) {
    Column {
        Text(text = "Setup for $emulator", color = NeonPalette.Cyan, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        steps.forEachIndexed { index, step ->
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)) {
                Text(text = "${index + 1}. ", color = NeonPalette.Purple, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text(text = step, color = Color(0xFFD0D7E5), fontSize = 12.sp, lineHeight = 16.sp)
            }
        }
    }
}
