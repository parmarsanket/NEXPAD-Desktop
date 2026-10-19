package com.sanket.tools.nexpaddesktop.ui.components.home

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Usb
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sanket.tools.nexpaddesktop.connection.ActiveTransport
import com.sanket.tools.nexpaddesktop.connection.DriverInstallState
import com.sanket.tools.nexpaddesktop.ui.components.selectedGlow
import com.sanket.tools.nexpaddesktop.ui.theme.NeonPalette

/**
 * Connection section listing all available communication channels:
 * Wi-Fi / USB Tethering, USB (AOA Protocol with Driver Installer), USB (ADB Bridge), and Bluetooth.
 */
@Composable
fun ConnectionSection(
    activeTransport: ActiveTransport,
    isDeviceConnected: Boolean,
    isAoaDriverNeeded: Boolean,
    driverInstallState: DriverInstallState,
    onInstallAoaDriver: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Connection",
            color = NeonPalette.CardIdleText,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(10.dp))

        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Wi-Fi / USB Tethering
            val isWifiActive = activeTransport == ActiveTransport.WIFI
            val isTetheringActive = activeTransport == ActiveTransport.USB_TETHERING
            val isNetworkActive = isWifiActive || isTetheringActive
            val networkLabel = when {
                isTetheringActive -> "USB Tethering"
                isWifiActive -> "Wi-Fi"
                else -> "Wi-Fi / USB Tethering"
            }
            val networkIcon = if (isTetheringActive) Icons.Default.Usb else Icons.Default.Wifi

            ConnectionToggleItem(
                label = networkLabel,
                icon = networkIcon,
                isDeviceConnected = isDeviceConnected,
                isActive = isNetworkActive
            )

            // 2. USB (AOA Protocol)
            val isAoaActive = activeTransport == ActiveTransport.USB_AOA
            val showAoaDriverInstall = !isAoaActive && (isAoaDriverNeeded || driverInstallState != DriverInstallState.IDLE)

            ConnectionToggleItem(
                label = "USB (AOA Protocol)",
                icon = Icons.Default.Usb,
                isDeviceConnected = isDeviceConnected,
                isActive = isAoaActive,
                hasCustomStatus = showAoaDriverInstall,
                extraContent = {
                    if (showAoaDriverInstall) {
                        when (driverInstallState) {
                            DriverInstallState.IDLE -> {
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
                            DriverInstallState.INSTALLING -> {
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
                            DriverInstallState.FINISHED -> {
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
            ConnectionToggleItem(
                label = "USB (ADB Bridge)",
                icon = Icons.Default.Usb,
                isDeviceConnected = isDeviceConnected,
                isActive = activeTransport == ActiveTransport.USB_ADB
            )

            // 4. Bluetooth
            ConnectionToggleItem(
                label = "Bluetooth",
                icon = Icons.Default.Bluetooth,
                isDeviceConnected = isDeviceConnected,
                isActive = activeTransport == ActiveTransport.BLUETOOTH
            )
        }
    }
}

/**
 * Unified individual connection toggle item.
 */
@Composable
fun ConnectionToggleItem(
    label: String,
    icon: ImageVector,
    isDeviceConnected: Boolean,
    isActive: Boolean,
    hasCustomStatus: Boolean = false,
    extraContent: @Composable () -> Unit = {}
) {
    val textColor by animateColorAsState(
        targetValue = if (isActive) Color.White else NeonPalette.CardIdleText,
        animationSpec = tween(200),
        label = "text"
    )

    var baseModifier = Modifier
        .fillMaxWidth(2 / 3f)
        .height(52.dp)

    if (isActive) {
        baseModifier = baseModifier.selectedGlow()
    } else {
        baseModifier = baseModifier.drawBehind {
            val cr = 12.dp.toPx()
            val borderAlpha = 0.05f
            drawRoundRect(
                color = Color.White.copy(alpha = borderAlpha),
                cornerRadius = CornerRadius(cr),
                style = Stroke(width = 1.dp.toPx())
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
                // Main Content (Icon + Label)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.padding(start = 18.dp)
                ) {
                    val iconBg = if (isActive) Color.White.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.05f)
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(iconBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(icon, contentDescription = null, tint = textColor, modifier = Modifier.size(16.dp))
                    }
                    Text(label, color = textColor, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }

                // Extra Content / Status Pill
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(end = 18.dp)
                ) {
                    extraContent()

                    if (!hasCustomStatus) {
                        Spacer(modifier = Modifier.width(8.dp))

                        if (isActive) {
                            Text("Active", color = NeonPalette.Cyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        } else if (isDeviceConnected) {
                            Text("Standby", color = NeonPalette.CardIdleText.copy(alpha = 0.45f), fontSize = 12.sp, fontWeight = FontWeight.Normal)
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
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
