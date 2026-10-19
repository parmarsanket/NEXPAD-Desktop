package com.sanket.tools.nexpaddesktop.ui

import com.sanket.tools.nexpaddesktop.ui.theme.NeonPalette
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.VerticalAlignBottom
import androidx.compose.material.icons.filled.Usb
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.rounded.DesktopWindows
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sanket.tools.nexpaddesktop.ui.components.DesktopVerticalScrollbar
import com.sanket.tools.nexpaddesktop.ui.components.GradientDivider
import com.sanket.tools.nexpaddesktop.ui.components.drawCyberGrid
import com.sanket.tools.nexpaddesktop.ui.components.glassCard
import com.sanket.tools.nexpaddesktop.ui.components.selectedGlow
import com.sanket.tools.nexpaddesktop.ui.components.statusPillGlow
import com.sanket.tools.nexpaddesktop.viewmodel.DesktopViewModel

@Composable
fun HomeScreen(
    viewModel: DesktopViewModel
) {
    HomeScreen(
        isDriverConnected = viewModel.isDriverConnected,
        connectedDeviceName = viewModel.connectedDeviceName,
        activeTransport = viewModel.activeTransport,
        isAoaDriverNeeded = viewModel.isAoaDriverNeeded,
        driverInstallState = viewModel.driverInstallState,
        onInstallAoaDriver = viewModel.onInstallAoaDriver,
        isDeveloperOptionsEnabled = viewModel.isDeveloperOptionsEnabled,
        onDeveloperOptionsChange = { viewModel.updateDeveloperOptionsEnabled(it) },
        onMinimizeToTray = { viewModel.minimizeToTray() }
    )
}

@Composable
fun HomeScreen(
    isDriverConnected: Boolean,
    connectedDeviceName: String? = null,
    activeTransport: com.sanket.tools.nexpaddesktop.connection.ActiveTransport = com.sanket.tools.nexpaddesktop.connection.ActiveTransport.NONE,
    isAoaDriverNeeded: Boolean = false,
    driverInstallState: com.sanket.tools.nexpaddesktop.connection.DriverInstallState = com.sanket.tools.nexpaddesktop.connection.DriverInstallState.IDLE,
    onInstallAoaDriver: () -> Unit = {},
    isDeveloperOptionsEnabled: Boolean = false,
    onDeveloperOptionsChange: (Boolean) -> Unit = {},
    onMinimizeToTray: () -> Unit = {}
) {
    // Server pulse
    val infinite = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infinite.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1200, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "pulse"
    )

    // Poll available hardware connections (Wi-Fi, USB, Bluetooth)
    var availableConnections by remember { mutableStateOf(listOf(1)) }
    LaunchedEffect(Unit) {
        while (true) {
            availableConnections = com.sanket.tools.nexpaddesktop.connection.wifi.NetworkUtils.getAvailableConnectionTypes()
            kotlinx.coroutines.delay(3000)
        }
    }

    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(start = 32.dp, top = 32.dp, bottom = 32.dp, end = 24.dp)
        ) {
            // ── Header ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "NEXPAD DESKTOP",
                    style = TextStyle(
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp,
                        brush = Brush.linearGradient(
                            colors = listOf(Color.White, NeonPalette.Cyan, NeonPalette.Purple)
                        )
                    )
                )

                // Server Active Pill (#6 - glowing pill instead of flat outline)
                Row(
                    modifier = Modifier
                        .statusPillGlow(color = NeonPalette.Green, glowAlpha = 0.2f * pulseAlpha)
                        .clip(RoundedCornerShape(20.dp))
                        .background(NeonPalette.Green.copy(alpha = 0.1f))
                        .padding(horizontal = 14.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(NeonPalette.Green.copy(alpha = pulseAlpha))
                    )
                    Text(
                        "Server Active",
                        color = NeonPalette.Green,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── Driver Warning Banner ──
            if (!isDriverConnected) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .drawBehind {
                            drawRoundRect(
                                brush = Brush.radialGradient(
                                    colors = listOf(Color(0xFFFF4D4D).copy(alpha = 0.15f), Color.Transparent)
                                ),
                                topLeft = Offset(-12f, -12f),
                                size = Size(size.width + 24f, size.height + 24f),
                                cornerRadius = CornerRadius(14.dp.toPx() + 12f)
                            )
                        }
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFFFF4D4D).copy(alpha = 0.08f))
                        .padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFFF4D4D), modifier = Modifier.size(24.dp))
                        Column {
                            Text("ViGEmBus Driver Missing", color = Color(0xFFFF4D4D), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(
                                "Virtual controllers require the ViGEmBus driver. Please install it from the redist folder.",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 12.sp
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // ── Background Gaming Mode Card (Above Device Section) ──
            BackgroundGamingCard(
                onMinimizeToTray = onMinimizeToTray
            )

            Spacer(modifier = Modifier.height(24.dp))

            // ── Gradient Divider ──
            GradientDivider()

            Spacer(modifier = Modifier.height(24.dp))

            // ── Device Section ──
            Text(if (connectedDeviceName == null) "Device" else "Connected Device", color = NeonPalette.CardIdleText, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(10.dp))

            if (connectedDeviceName == null) {
                // Radar Animation when no device is connected
                RadarAnimation()
            } else {
                val cleanDeviceName = connectedDeviceName
                    .replace(Regex("\\s*\\((?:Bluetooth|USB Tethering|USB Direct|ADB|USB / ADB|Wi-Fi)\\)\\s*$", RegexOption.IGNORE_CASE), "")
                    .trim()
                    .ifEmpty { connectedDeviceName }

                // Device Connected Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .glassCard()
                        .padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            // Device Icon (#6)
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .drawBehind {
                                        drawCircle(
                                            brush = Brush.radialGradient(
                                                colors = listOf(NeonPalette.ConnectedDot.copy(alpha = 0.2f), Color.Transparent)
                                            ),
                                            radius = 22.dp.toPx() + 12f
                                        )
                                    }
                                    .clip(CircleShape)
                                    .background(NeonPalette.ConnectedDot.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Rounded.DesktopWindows, // Using monitor icon to match existing, or phone if preferred
                                    contentDescription = null,
                                    tint = NeonPalette.ConnectedDot,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
    
                            Column {
                                Text(cleanDeviceName, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(NeonPalette.ConnectedDot))
                                    Text("Connected", color = NeonPalette.CardIdleText, fontSize = 12.sp)
                                }
                            }
                        }
    
                        // Connection Type Badge
                        val (typeIcon, typeText) = when (activeTransport) {
                            com.sanket.tools.nexpaddesktop.connection.ActiveTransport.WIFI -> Icons.Default.Wifi to "Wi-Fi"
                            com.sanket.tools.nexpaddesktop.connection.ActiveTransport.USB_TETHERING -> Icons.Default.Usb to "USB Tethering"
                            com.sanket.tools.nexpaddesktop.connection.ActiveTransport.USB_AOA -> Icons.Default.Usb to "USB (AOA)"
                            com.sanket.tools.nexpaddesktop.connection.ActiveTransport.USB_ADB -> Icons.Default.Usb to "USB (ADB)"
                            com.sanket.tools.nexpaddesktop.connection.ActiveTransport.BLUETOOTH -> Icons.Default.Bluetooth to "Bluetooth"
                            com.sanket.tools.nexpaddesktop.connection.ActiveTransport.NONE -> Icons.Default.Wifi to "Wi-Fi"
                        }
                        
                        Row(
                            modifier = Modifier
                                .selectedGlow(cornerRadius = 10.dp, glowAlpha = 0.15f)
                                .padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(typeIcon, contentDescription = null, tint = NeonPalette.Cyan, modifier = Modifier.size(14.dp))
                            Text(typeText, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // ── Gradient Divider ──
            GradientDivider()

            Spacer(modifier = Modifier.height(24.dp))

            // ── Connection Section ──
            Text("Connection", color = NeonPalette.CardIdleText, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(10.dp))

            val isDeviceConnected = connectedDeviceName != null

            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 1. Wi-Fi / USB Tethering
                val isWifiActive = activeTransport == com.sanket.tools.nexpaddesktop.connection.ActiveTransport.WIFI
                val isTetheringActive = activeTransport == com.sanket.tools.nexpaddesktop.connection.ActiveTransport.USB_TETHERING
                val isNetworkActive = isWifiActive || isTetheringActive
                val networkLabel = when {
                    isTetheringActive -> "USB Tethering"
                    isWifiActive -> "Wi-Fi"
                    else -> "Wi-Fi / USB Tethering"
                }
                val networkIcon = if (isTetheringActive) Icons.Default.Usb else Icons.Default.Wifi

                ConnectionToggle(
                    label = networkLabel,
                    icon = networkIcon,
                    isDeviceConnected = isDeviceConnected,
                    isActive = isNetworkActive
                )

                // 2. USB (AOA Protocol)
                val isAoaActive = activeTransport == com.sanket.tools.nexpaddesktop.connection.ActiveTransport.USB_AOA
                val showAoaDriverInstall = !isAoaActive && (isAoaDriverNeeded || driverInstallState != com.sanket.tools.nexpaddesktop.connection.DriverInstallState.IDLE)
                ConnectionToggle(
                    label = "USB (AOA Protocol)",
                    icon = Icons.Default.Usb,
                    isDeviceConnected = isDeviceConnected,
                    isActive = isAoaActive,
                    hasCustomStatus = showAoaDriverInstall,
                    extraContent = {
                        if (showAoaDriverInstall) {
                            when (driverInstallState) {
                                com.sanket.tools.nexpaddesktop.connection.DriverInstallState.IDLE -> {
                                    Button(
                                        onClick = onInstallAoaDriver,
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = NeonPalette.Purple,
                                            contentColor = Color.White
                                        ),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                        modifier = Modifier.height(28.dp)
                                    ) {
                                        Text("Install Driver", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                                com.sanket.tools.nexpaddesktop.connection.DriverInstallState.INSTALLING -> {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(14.dp),
                                            strokeWidth = 2.dp,
                                            color = NeonPalette.Cyan
                                        )
                                        Text("Installing...", color = NeonPalette.Cyan, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                                    }
                                }
                                com.sanket.tools.nexpaddesktop.connection.DriverInstallState.FINISHED -> {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = NeonPalette.Green,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Text("Finished", color = NeonPalette.Green, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                )

                // 3. USB (ADB Bridge)
                ConnectionToggle(
                    label = "USB (ADB Bridge)",
                    icon = Icons.Default.Usb,
                    isDeviceConnected = isDeviceConnected,
                    isActive = activeTransport == com.sanket.tools.nexpaddesktop.connection.ActiveTransport.USB_ADB
                )

                // 4. Bluetooth
                ConnectionToggle(
                    label = "Bluetooth",
                    icon = Icons.Default.Bluetooth,
                    isDeviceConnected = isDeviceConnected,
                    isActive = activeTransport == com.sanket.tools.nexpaddesktop.connection.ActiveTransport.BLUETOOTH
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // ── Gradient Divider ──
            GradientDivider()

            Spacer(modifier = Modifier.height(24.dp))

            // ── System Preferences ──
            Text("System Preferences", color = NeonPalette.CardIdleText, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(10.dp))

            // ── System Preferences / Developer Options ──
            Text("System Preferences", color = NeonPalette.CardIdleText, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(10.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .glassCard(cornerRadius = 14.dp)
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .drawBehind {
                                    drawCircle(
                                        brush = Brush.radialGradient(
                                            colors = listOf(
                                                (if (isDeveloperOptionsEnabled) NeonPalette.Cyan else Color.Gray).copy(alpha = 0.25f),
                                                Color.Transparent
                                            )
                                        ),
                                        radius = 20.dp.toPx() + 10f
                                    )
                                }
                                .clip(CircleShape)
                                .background(
                                    (if (isDeveloperOptionsEnabled) NeonPalette.Cyan else Color.Gray).copy(alpha = 0.12f)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Code,
                                contentDescription = "Developer Options",
                                tint = if (isDeveloperOptionsEnabled) NeonPalette.Cyan else Color.Gray,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Column {
                            Text(
                                text = "Developer Options",
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Show low-level debugging tools, IMU inspector & driver logs in sidebar",
                                color = NeonPalette.CardIdleText,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF161E30))
                            .border(1.dp, Color(0xFF222E4A), RoundedCornerShape(10.dp))
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = if (isDeveloperOptionsEnabled) "ON" else "OFF",
                            color = if (isDeveloperOptionsEnabled) NeonPalette.Cyan else Color.Gray,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Switch(
                            checked = isDeveloperOptionsEnabled,
                            onCheckedChange = onDeveloperOptionsChange,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = NeonPalette.Cyan,
                                checkedTrackColor = NeonPalette.Cyan.copy(alpha = 0.35f),
                                uncheckedThumbColor = Color.Gray,
                                uncheckedTrackColor = Color(0xFF111726)
                            )
                        )
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

@Composable
private fun ConnectionToggle(
    label: String,
    icon: ImageVector,
    isDeviceConnected: Boolean,
    isActive: Boolean,
    hasCustomStatus: Boolean = false,
    extraContent: @Composable () -> Unit = {}
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    val textColor by animateColorAsState(
        targetValue = if (isActive || isHovered) Color.White else NeonPalette.CardIdleText,
        animationSpec = tween(200),
        label = "text"
    )

    // Base box with conditional modifiers based on state (#2 & #8 consistency)
    var baseModifier = Modifier
        .fillMaxWidth(2/3f)
        .height(52.dp) // Fixed height for smooth transitions
        .hoverable(interactionSource)
        .clickable(interactionSource, indication = null) { /* toggle */ }
    
    if (isActive) {
        baseModifier = baseModifier.selectedGlow()
    } else {
        baseModifier = baseModifier.drawBehind {
            val cr = 12.dp.toPx()
            
            // Background
            val bgAlpha = if (isHovered) 0.03f else 0.0f
            drawRoundRect(
                color = Color.White.copy(alpha = bgAlpha),
                cornerRadius = CornerRadius(cr)
            )
            
            // Faint border
            val borderAlpha = if (isHovered) 0.1f else 0.05f
            drawRoundRect(
                color = Color.White.copy(alpha = borderAlpha),
                cornerRadius = CornerRadius(cr),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.dp.toPx())
            )
        }
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = baseModifier,
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // ── Main Content ──
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.padding(start = 18.dp)
                ) {
                    // Circular icon holder
                    val iconBg = if (isActive || isHovered) Color.White.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.05f)
                    Box(
                        modifier = Modifier.size(34.dp).clip(CircleShape).background(iconBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(icon, contentDescription = null, tint = textColor, modifier = Modifier.size(16.dp))
                    }
    
                    Text(label, color = textColor, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }
                
                // ── Extra Content / Status ──
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(end = 18.dp)
                ) {
                    extraContent()
                    
                    if (!hasCustomStatus) {
                        // Spacer between extra content and status dot
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        if (isActive) {
                            Text("Active", color = NeonPalette.Cyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        } else if (isDeviceConnected) {
                            Text("Standby", color = NeonPalette.CardIdleText.copy(alpha = 0.45f), fontSize = 12.sp, fontWeight = FontWeight.Normal)
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(NeonPalette.Green))
                                Text("Ready", color = NeonPalette.CardIdleText, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RadarAnimation() {
    val infinite = rememberInfiniteTransition(label = "radar")
    val pulse by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .glassCard()
            .padding(18.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    val maxRadius = size.height / 2f
                    // Draw static rings
                    drawCircle(
                        color = NeonPalette.Cyan.copy(alpha = 0.1f),
                        radius = maxRadius * 0.33f,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.dp.toPx())
                    )
                    drawCircle(
                        color = NeonPalette.Cyan.copy(alpha = 0.1f),
                        radius = maxRadius * 0.66f,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.dp.toPx())
                    )
                    drawCircle(
                        color = NeonPalette.Cyan.copy(alpha = 0.1f),
                        radius = maxRadius,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.dp.toPx())
                    )

                    // Draw expanding radar pulse
                    val currentRadius = maxRadius * pulse
                    val pulseAlpha = 1f - pulse
                    drawCircle(
                        color = NeonPalette.Cyan.copy(alpha = pulseAlpha * 0.4f),
                        radius = currentRadius
                    )
                    drawCircle(
                        color = NeonPalette.Cyan.copy(alpha = pulseAlpha * 0.8f),
                        radius = currentRadius,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
                    )
                }
        ) {
            // Server icon in the center
            Icon(
                Icons.Rounded.DesktopWindows,
                contentDescription = null,
                tint = NeonPalette.Cyan,
                modifier = Modifier.align(Alignment.Center).size(24.dp)
            )
        }
        
        Text(
            "Waiting for device...", 
            color = NeonPalette.Cyan.copy(alpha = 0.8f), 
            fontSize = 12.sp,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun BackgroundGamingCard(
    onMinimizeToTray: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    val glowAlpha by animateFloatAsState(
        targetValue = if (isHovered) 0.35f else 0.18f,
        animationSpec = tween(250),
        label = "card_glow"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .drawBehind {
                drawRoundRect(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            NeonPalette.Cyan.copy(alpha = glowAlpha),
                            NeonPalette.Purple.copy(alpha = glowAlpha * 0.5f),
                            Color.Transparent
                        ),
                        center = Offset(size.width * 0.85f, size.height * 0.5f),
                        radius = size.width * 0.45f
                    ),
                    cornerRadius = CornerRadius(16.dp.toPx())
                )
            }
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFF0F1626),
                        Color(0xFF131D33),
                        Color(0xFF18182E)
                    )
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        NeonPalette.Cyan.copy(alpha = if (isHovered) 0.5f else 0.25f),
                        NeonPalette.Purple.copy(alpha = if (isHovered) 0.5f else 0.25f)
                    )
                ),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(horizontal = 22.dp, vertical = 18.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left Content: Badge + Title + 2-Line Explanation
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f)
            ) {
                // Glowing Icon Pod
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    NeonPalette.Cyan.copy(alpha = 0.2f),
                                    NeonPalette.Purple.copy(alpha = 0.2f)
                                )
                            )
                        )
                        .border(
                            1.dp,
                            Brush.linearGradient(
                                colors = listOf(
                                    NeonPalette.Cyan.copy(alpha = 0.6f),
                                    NeonPalette.Purple.copy(alpha = 0.4f)
                                )
                            ),
                            RoundedCornerShape(14.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.SportsEsports,
                        contentDescription = "Gaming Mode",
                        tint = NeonPalette.Cyan,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Performance Badge
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(NeonPalette.Cyan.copy(alpha = 0.12f))
                            .border(1.dp, NeonPalette.Cyan.copy(alpha = 0.35f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(5.dp)
                                .clip(CircleShape)
                                .background(NeonPalette.Cyan)
                        )
                        Text(
                            text = "ZERO OVERHEAD GAMING",
                            color = NeonPalette.Cyan,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.6.sp
                        )
                    }

                    // Title
                    Text(
                        text = "Run NEXPAD in Background",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        letterSpacing = 0.3.sp
                    )

                    // 2-Line Explanation
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "Minimize NEXPAD to the Windows system tray to free up GPU & CPU resources for your game.",
                            color = NeonPalette.CardIdleText,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Normal,
                            lineHeight = 16.sp
                        )
                        Text(
                            text = "Virtual gamepad emulation, gyro motion aiming, and connection servers remain 100% active.",
                            color = NeonPalette.CardIdleText.copy(alpha = 0.85f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Normal,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(24.dp))

            // Right Action: "Minimize for Game" Button with Subtitle
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Button(
                    onClick = onMinimizeToTray,
                    interactionSource = interactionSource,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor = Color.White
                    ),
                    contentPadding = PaddingValues(horizontal = 18.dp, vertical = 11.dp),
                    modifier = Modifier
                        .height(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFF00C6FF),
                                    Color(0xFF0072FF)
                                )
                            )
                        )
                        .border(
                            1.dp,
                            Color.White.copy(alpha = 0.3f),
                            RoundedCornerShape(12.dp)
                        )
                ) {
                    Icon(
                        Icons.Default.SportsEsports,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(17.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Minimize for Game",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.4.sp
                    )
                }

                Text(
                    text = "Restores from system tray anytime",
                    color = Color.White.copy(alpha = 0.45f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}


