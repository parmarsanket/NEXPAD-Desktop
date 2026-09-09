package com.sanket.tools.nexpaddesktop.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sanket.tools.nexpaddesktop.plugins.DesktopPluginItem
import com.sanket.tools.nexpaddesktop.plugins.DesktopPluginManager
import com.sanket.tools.nexpaddesktop.ui.components.glassCard
import com.sanket.tools.nexpaddesktop.ui.theme.NeonPalette
import kotlinx.coroutines.launch

@Composable
fun PluginsScreen() {
    val plugins = remember { DesktopPluginManager.getAvailablePlugins() }
    var selectedPlugin by remember { mutableStateOf(plugins.firstOrNull()) }
    var transferStatus by remember { mutableStateOf<String?>(null) }
    var isTransferring by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Controller Plugins & Component Studio",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Black,
                        color = NeonPalette.Cyan
                    )
                )
                Text(
                    text = "Manage, create, and hot-transfer .nxpcomponent designs directly to your Android device",
                    style = MaterialTheme.typography.bodyMedium.copy(color = NeonPalette.CardIdleText)
                )
            }

            // Status Pill
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(NeonPalette.PanelBgTop)
                    .border(1.dp, NeonPalette.Cyan.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Bolt, contentDescription = null, tint = NeonPalette.Cyan, modifier = Modifier.size(18.dp))
                    Text("Zero-Latency Pipeline Active", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Main 2-Column Layout
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Left Column: Component List
            Column(
                modifier = Modifier
                    .width(360.dp)
                    .fillMaxHeight()
                    .glassCard()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Available Components (${plugins.size})",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(plugins, key = { it.id }) { item ->
                        val isSelected = selectedPlugin?.id == item.id
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) NeonPalette.Cyan.copy(alpha = 0.15f) else Color(0xFF0C1322))
                                .border(
                                    1.dp,
                                    if (isSelected) NeonPalette.Cyan else Color.White.copy(alpha = 0.08f),
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable {
                                    selectedPlugin = item
                                    transferStatus = null
                                }
                                .padding(12.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = item.name,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) NeonPalette.Cyan else Color.White,
                                        fontSize = 14.sp
                                    )
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(NeonPalette.Purple.copy(alpha = 0.2f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(item.defaultControl, color = NeonPalette.Purple, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                                Text(
                                    text = "${item.category} • by ${item.author}",
                                    fontSize = 11.sp,
                                    color = NeonPalette.CardIdleText
                                )
                            }
                        }
                    }
                }
            }

            // Right Column: Inspector & Transfer Action
            val current = selectedPlugin
            if (current != null) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .glassCard()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Component Meta Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = current.name,
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                            )
                            Text(
                                text = current.description,
                                style = MaterialTheme.typography.bodySmall.copy(color = NeonPalette.CardIdleText)
                            )
                        }

                        // Transfer Buttons
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Button(
                                onClick = {
                                    scope.launch {
                                        isTransferring = true
                                        transferStatus = "Transferring via USB Debugging (ADB)..."
                                        val res = DesktopPluginManager.transferViaAdb(current)
                                        res.fold(
                                            onSuccess = { msg -> transferStatus = msg },
                                            onFailure = { ex -> transferStatus = "ADB Error: ${ex.message}" }
                                        )
                                        isTransferring = false
                                    }
                                },
                                enabled = !isTransferring,
                                colors = ButtonDefaults.buttonColors(containerColor = NeonPalette.Cyan),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Usb, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Push via ADB (USB)", color = Color.Black, fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = {
                                    scope.launch {
                                        isTransferring = true
                                        transferStatus = "Sending via FTP Protocol (Port 9996)..."
                                        val res = DesktopPluginManager.transferViaFtp(current)
                                        res.fold(
                                            onSuccess = { msg -> transferStatus = msg },
                                            onFailure = { ex -> transferStatus = "FTP Error: ${ex.message}" }
                                        )
                                        isTransferring = false
                                    }
                                },
                                enabled = !isTransferring,
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonPalette.Purple)
                            ) {
                                Icon(Icons.Default.Wifi, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Send via FTP")
                            }
                        }
                    }

                    // Transfer Status Alert
                    if (transferStatus != null) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = if (transferStatus!!.contains("Error")) Color(0xFF3B121A) else Color(0xFF0F2C24)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = transferStatus!!,
                                color = if (transferStatus!!.contains("Error")) Color(0xFFFF6B6B) else Color(0xFF00FF99),
                                modifier = Modifier.padding(12.dp),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // JSON Specification View
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Declarative Component Specification (.nxpcomponent JSON)",
                            color = NeonPalette.CardIdleText,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF040810))
                                .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                                .padding(14.dp)
                        ) {
                            Text(
                                text = current.jsonContent,
                                fontFamily = FontFamily.Monospace,
                                color = NeonPalette.Cyan.copy(alpha = 0.9f),
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
