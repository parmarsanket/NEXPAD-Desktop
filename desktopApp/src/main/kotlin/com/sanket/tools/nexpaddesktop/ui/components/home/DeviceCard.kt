package com.sanket.tools.nexpaddesktop.ui.components.home

import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Usb
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.rounded.DesktopWindows
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sanket.tools.nexpaddesktop.connection.ActiveTransport
import com.sanket.tools.nexpaddesktop.ui.components.glassCard
import com.sanket.tools.nexpaddesktop.ui.components.selectedGlow
import com.sanket.tools.nexpaddesktop.ui.theme.NeonPalette

/**
 * Section displaying the current device state:
 * - When no device is connected: renders a high-tech RadarAnimation searching for mobile pads.
 * - When connected: renders the device details, connection status, and active hardware transport badge.
 */
@Composable
fun DeviceSection(
    modifier: Modifier = Modifier,
    connectedDeviceName: String?,
    activeTransport: ActiveTransport = ActiveTransport.NONE
) {
    val sectionTitle = if (connectedDeviceName == null) "Device" else "Connected Device"

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = sectionTitle,
            color = NeonPalette.CardIdleText,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(10.dp))

        if (connectedDeviceName == null) {
            RadarAnimation()
        } else {
            ConnectedDeviceCard(
                deviceName = connectedDeviceName,
                activeTransport = activeTransport
            )
        }
    }
}

@Composable
fun ConnectedDeviceCard(
    deviceName: String,
    activeTransport: ActiveTransport,
    modifier: Modifier = Modifier
) {
    val cleanDeviceName = remember(deviceName) {
        deviceName
            .replace(
                Regex("\\s*\\((?:Bluetooth|USB Tethering|USB Direct|ADB|USB / ADB|Wi-Fi)\\)\\s*$", RegexOption.IGNORE_CASE),
                ""
            )
            .trim()
            .ifEmpty { deviceName }
    }

    val (typeIcon, typeText) = remember(activeTransport) {
        when (activeTransport) {
            ActiveTransport.WIFI -> Icons.Default.Wifi to "Wi-Fi"
            ActiveTransport.USB_TETHERING -> Icons.Default.Usb to "USB Tethering"
            ActiveTransport.USB_AOA -> Icons.Default.Usb to "USB (AOA)"
            ActiveTransport.USB_ADB -> Icons.Default.Usb to "USB (ADB)"
            ActiveTransport.BLUETOOTH -> Icons.Default.Bluetooth to "Bluetooth"
            ActiveTransport.NONE -> Icons.Default.Wifi to "Wi-Fi"
        }
    }

    val iconGlowBrush = remember {
        Brush.radialGradient(
            colors = listOf(NeonPalette.ConnectedDot.copy(alpha = 0.2f), Color.Transparent)
        )
    }

    Box(
        modifier = modifier
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
                // Device Icon
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .drawBehind {
                            drawCircle(
                                brush = iconGlowBrush,
                                radius = 22.dp.toPx() + 12f
                            )
                        }
                        .clip(CircleShape)
                        .background(NeonPalette.ConnectedDot.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.DesktopWindows,
                        contentDescription = null,
                        tint = NeonPalette.ConnectedDot,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Column {
                    Text(
                        text = cleanDeviceName,
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(NeonPalette.ConnectedDot)
                        )
                        Text(
                            text = "Connected",
                            color = NeonPalette.CardIdleText,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // Connection Type Badge
            Row(
                modifier = Modifier
                    .selectedGlow(cornerRadius = 10.dp, glowAlpha = 0.15f)
                    .padding(horizontal = 10.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = typeIcon,
                    contentDescription = null,
                    tint = NeonPalette.Cyan,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = typeText,
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun RadarAnimation(
    modifier: Modifier = Modifier
) {
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
        modifier = modifier
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
                    val ringStroke = Stroke(width = 1.dp.toPx())
                    val pulseStroke = Stroke(width = 2.dp.toPx())
                    val ringColor = NeonPalette.Cyan.copy(alpha = 0.1f)

                    // Draw static rings
                    drawCircle(color = ringColor, radius = maxRadius * 0.33f, style = ringStroke)
                    drawCircle(color = ringColor, radius = maxRadius * 0.66f, style = ringStroke)
                    drawCircle(color = ringColor, radius = maxRadius, style = ringStroke)

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
                        style = pulseStroke
                    )
                }
        ) {
            Icon(
                imageVector = Icons.Rounded.DesktopWindows,
                contentDescription = null,
                tint = NeonPalette.Cyan,
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(24.dp)
            )
        }

        Text(
            text = "Waiting for device...",
            color = NeonPalette.Cyan.copy(alpha = 0.8f),
            fontSize = 12.sp,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}
