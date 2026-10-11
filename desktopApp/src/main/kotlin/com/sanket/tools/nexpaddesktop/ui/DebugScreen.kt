package com.sanket.tools.nexpaddesktop.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sanket.tools.nexpaddesktop.connection.wifi.UdpServer
import com.sanket.tools.nexpaddesktop.ui.theme.NeonPalette
import com.sanket.tools.nexpaddesktop.utils.AppLogger
import com.sanket.tools.nexpaddesktop.utils.LogCategory
import com.sanket.tools.nexpaddesktop.utils.LogEntry
import com.sanket.tools.nexpaddesktop.viewmodel.DesktopViewModel

/**
 * Filter categories for the telemetry terminal.
 */
private enum class DebugFilter(val label: String, val icon: String) {
    ALL("All", "📋"),
    NETWORK("Network", "📡"),
    INPUT_DRIVER("Input & Driver", "🎮"),
    MOTION_DSU("Motion & DSU", "🎯"),
    HAPTICS("Haptics", "💥"),
    WARNINGS_ERRORS("Issues", "⚠️")
}

/**
 * Dedicated Debug Console Screen for NEXPAD Desktop.
 *
 * Provides 100% transparency into the engine loop:
 * - Live System Telemetry HUD Bar (session socket, RX/TX packet counters, packet loss %, ViGEm bus, DSU port)
 * - Anti-spam deduplication and event milestone tracking
 * - Subsystem filtering (Network, Input, Motion, Haptics, Errors)
 * - Real-time keyword search filter and auto-scroll toggle
 * - Color-coded category badges with precise timestamps and line numbers
 */
@Composable
fun DebugScreen(viewModel: DesktopViewModel = remember { DesktopViewModel() }) {
    val listState = rememberLazyListState()
    val trigger = AppLogger.updateTrigger.value
    
    // Read structured copy of log entries whenever trigger fires
    val allLogs = remember(trigger) { AppLogger.getLogEntriesCopy() }
    val clipboardManager = LocalClipboardManager.current
    var isCopied by remember { mutableStateOf(false) }
    var selectedFilter by remember { mutableStateOf(DebugFilter.ALL) }
    var searchQuery by remember { mutableStateOf("") }
    var autoScrollEnabled by remember { mutableStateOf(true) }

    // Filter logs according to active filter and search query
    val filteredLogs = remember(allLogs, selectedFilter, searchQuery) {
        allLogs.filter { entry ->
            val matchesFilter = when (selectedFilter) {
                DebugFilter.ALL -> true
                DebugFilter.NETWORK -> entry.category == LogCategory.NETWORK || entry.category == LogCategory.HANDSHAKE
                DebugFilter.INPUT_DRIVER -> entry.category == LogCategory.INPUT || entry.category == LogCategory.DRIVER
                DebugFilter.MOTION_DSU -> entry.category == LogCategory.MOTION
                DebugFilter.HAPTICS -> entry.category == LogCategory.HAPTICS
                DebugFilter.WARNINGS_ERRORS -> entry.category == LogCategory.WARNING || entry.category == LogCategory.ERROR
            }
            val matchesSearch = if (searchQuery.isBlank()) true else {
                entry.message.contains(searchQuery, ignoreCase = true) ||
                entry.timestamp.contains(searchQuery, ignoreCase = true) ||
                entry.category.badge.contains(searchQuery, ignoreCase = true)
            }
            matchesFilter && matchesSearch
        }
    }

    LaunchedEffect(filteredLogs.size, autoScrollEnabled) {
        if (autoScrollEnabled && filteredLogs.isNotEmpty()) {
            listState.animateScrollToItem(filteredLogs.size - 1)
        }
    }

    LaunchedEffect(isCopied) {
        if (isCopied) {
            kotlinx.coroutines.delay(2000)
            isCopied = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NeonPalette.PanelBgBottom)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // ── 1. Top Header & Primary Actions ──
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(NeonPalette.Cyan.copy(alpha = 0.12f))
                        .border(1.dp, NeonPalette.Cyan.copy(alpha = 0.45f), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.BugReport,
                        contentDescription = "Debug",
                        tint = NeonPalette.Cyan,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "DEBUG & ENGINE TELEMETRY",
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    )
                    Text(
                        text = "100% loop transparency • Handshakes • ViGEm driver • DSU UDP 26760",
                        color = NeonPalette.CardIdleText,
                        fontSize = 11.5.sp
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Log count pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF131A2B))
                        .border(1.dp, NeonPalette.CardIdleBorder, RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "${filteredLogs.size}/${allLogs.size} Logs",
                        color = NeonPalette.Green,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                // Auto-Scroll Toggle
                OutlinedButton(
                    onClick = { autoScrollEnabled = !autoScrollEnabled },
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (autoScrollEnabled) NeonPalette.Cyan.copy(alpha = 0.12f) else Color.Transparent,
                        contentColor = if (autoScrollEnabled) NeonPalette.Cyan else Color.White.copy(alpha = 0.6f)
                    ),
                    border = BorderStroke(1.dp, if (autoScrollEnabled) NeonPalette.Cyan.copy(alpha = 0.6f) else Color(0xFF26334D)),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = if (autoScrollEnabled) "⬇ Auto-Scroll ON" else "⏸ Auto-Scroll OFF",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Copy All Logs
                Button(
                    onClick = {
                        val allLogsText = filteredLogs.joinToString("\n") { it.displayString() }
                        clipboardManager.setText(buildAnnotatedString { append(allLogsText) })
                        isCopied = true
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isCopied) NeonPalette.Green.copy(alpha = 0.25f) else Color(0xFF1E283C),
                        contentColor = if (isCopied) NeonPalette.Green else NeonPalette.Cyan
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Copy", modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(text = if (isCopied) "Copied!" else "Copy", fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold)
                }

                // Clear Logs
                OutlinedButton(
                    onClick = { AppLogger.clearLogs() },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFF5555)),
                    border = BorderStroke(1.dp, Color(0xFFFF5555).copy(alpha = 0.45f)),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(imageVector = Icons.Default.DeleteSweep, contentDescription = "Clear", modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Clear", fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        // ── 2. Live Telemetry HUD Bar (Under-the-Hood Transparency) ──
        val isSessionActive = UdpServer.isLiveSessionActive || (viewModel.activeTransport != com.sanket.tools.nexpaddesktop.connection.ActiveTransport.NONE)
        val deviceName = UdpServer.liveDeviceName.ifBlank { viewModel.connectedDeviceName ?: "No Controller" }
        val transport = UdpServer.liveTransportType.ifBlank { viewModel.activeTransport.displayName }
        val socketStr = UdpServer.liveClientAddress.ifBlank { "Port 9999 Standby" }
        val rxPackets = UdpServer.liveTotalRxPackets
        val txFeedbacks = UdpServer.liveTotalTxFeedbacks
        val lossPct = UdpServer.livePacketLossPct

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // HUD 1: Connection Session & Remote Socket
            HudGlassCard(
                title = "NETWORK SESSION",
                primaryText = if (isSessionActive) deviceName else "Awaiting Client",
                secondaryText = if (isSessionActive) "$transport • $socketStr" else "Wi-Fi UDP: 0.0.0.0:9999",
                statusColor = if (isSessionActive) NeonPalette.Green else Color(0xFF64748B),
                icon = "📡"
            )

            // HUD 2: Traffic Counters (Zero-Spam Live Metrics)
            HudGlassCard(
                title = "TRAFFIC TELEMETRY",
                primaryText = "RX: ${String.format("%,d", rxPackets)} pkts",
                secondaryText = "TX: ${String.format("%,d", txFeedbacks)} feedback frames",
                statusColor = if (rxPackets > 0) NeonPalette.Cyan else Color(0xFF64748B),
                icon = "📥"
            )

            // HUD 3: Link Stability & Loss Rate
            HudGlassCard(
                title = "LINK QUALITY",
                primaryText = if (lossPct == 0) "0.0% Loss (Stable)" else "$lossPct% Packet Loss",
                secondaryText = if (isSessionActive) "60 Hz Polling • RTT Echo" else "Idle Watchdog",
                statusColor = if (lossPct == 0) Color(0xFF10B981) else Color(0xFFF59E0B),
                icon = "📶"
            )

            // HUD 4: Virtual Gamepad Driver (ViGEmBus)
            HudGlassCard(
                title = "VIRTUAL GAMEPAD",
                primaryText = viewModel.activeController.displayName,
                secondaryText = if (viewModel.isDriverConnected) "ViGEmBus: Mounted & Active" else "ViGEmBus: Standby",
                statusColor = if (viewModel.isDriverConnected) Color(0xFF34D399) else Color(0xFF64748B),
                icon = "🎮"
            )

            // HUD 5: Motion DSU Server (UDP 26760)
            HudGlassCard(
                title = "DSU MOTION SERVER",
                primaryText = if (viewModel.isDsuServerEnabled) "UDP Port 26760 (Active)" else "Server Disabled",
                secondaryText = "${viewModel.dsuClientCount} Emulator Client(s)",
                statusColor = if (viewModel.isDsuServerEnabled) Color(0xFF38BDF8) else Color(0xFF64748B),
                icon = "🎯"
            )
        }

        // ── 3. Subsystem Filter Tabs & Quick Search ──
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Filter Pills
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                DebugFilter.entries.forEach { filter ->
                    val isSelected = selectedFilter == filter
                    Box(
                        modifier = Modifier
                            .height(28.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isSelected) NeonPalette.Cyan.copy(alpha = 0.20f) else Color(0xFF0F1523))
                            .border(1.dp, if (isSelected) NeonPalette.Cyan else Color(0xFF222E46), RoundedCornerShape(6.dp))
                            .clickable { selectedFilter = filter }
                            .padding(horizontal = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${filter.icon} ${filter.label}",
                            color = if (isSelected) NeonPalette.Cyan else Color.White.copy(alpha = 0.7f),
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            // Quick Search Box
            Row(
                modifier = Modifier
                    .widthIn(min = 160.dp, max = 240.dp)
                    .height(30.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFF0A0F1A))
                    .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(6.dp))
                androidx.compose.foundation.text.BasicTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    singleLine = true,
                    textStyle = TextStyle(color = Color.White, fontSize = 11.5.sp, fontFamily = FontFamily.Monospace),
                    modifier = Modifier.weight(1f),
                    decorationBox = { innerTextField ->
                        if (searchQuery.isEmpty()) {
                            Text("Filter logs...", color = Color.Gray.copy(alpha = 0.6f), fontSize = 11.5.sp)
                        }
                        innerTextField()
                    }
                )
                if (searchQuery.isNotEmpty()) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Clear search",
                        tint = Color.Gray,
                        modifier = Modifier
                            .size(14.dp)
                            .clickable { searchQuery = "" }
                    )
                }
            }
        }

        // ── 4. Monospace Terminal Box ──
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFF060910))
                .border(1.dp, Color(0xFF162034), RoundedCornerShape(10.dp))
                .padding(12.dp)
        ) {
            if (filteredLogs.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = if (allLogs.isEmpty()) "Waiting for engine events... UDP handshakes and input frames will appear here." else "No log entries match the current filter.",
                            color = NeonPalette.CardIdleText,
                            fontSize = 12.5.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        if (allLogs.isNotEmpty() && filteredLogs.isEmpty()) {
                            OutlinedButton(
                                onClick = {
                                    selectedFilter = DebugFilter.ALL
                                    searchQuery = ""
                                },
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.height(26.dp)
                            ) {
                                Text("Reset Filters", fontSize = 10.5.sp, color = NeonPalette.Cyan)
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    items(items = filteredLogs, key = { it.id }) { log ->
                        LogTerminalRow(log = log)
                    }
                }
            }
        }
    }
}

/**
 * Compact HUD Glass Card for real-time engine telemetry.
 */
@Composable
private fun HudGlassCard(
    title: String,
    primaryText: String,
    secondaryText: String,
    statusColor: Color,
    icon: String
) {
    Box(
        modifier = Modifier
            .widthIn(min = 175.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF0A0F1D))
            .border(1.dp, Color(0xFF1B273F), RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 7.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$icon $title",
                    color = Color.White.copy(alpha = 0.55f),
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(statusColor)
                )
            }
            Text(
                text = primaryText,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 11.5.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = secondaryText,
                color = statusColor,
                fontWeight = FontWeight.Medium,
                fontSize = 10.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Single color-coded row in the terminal with line numbers, timestamp, category badge, and repetition pill.
 */
@Composable
private fun LogTerminalRow(log: LogEntry) {
    val (badgeBg, badgeText, messageColor) = when (log.category) {
        LogCategory.ERROR -> Triple(Color(0xFF451A1A), Color(0xFFFF6B6B), Color(0xFFFF8B8B))
        LogCategory.WARNING -> Triple(Color(0xFF422810), Color(0xFFFCD34D), Color(0xFFFDE68A))
        LogCategory.HANDSHAKE -> Triple(Color(0xFF0F3B2C), Color(0xFF34D399), Color(0xFF6EE7B7))
        LogCategory.NETWORK -> Triple(Color(0xFF0B2E40), Color(0xFF38BDF8), Color(0xFF7DD3FC))
        LogCategory.INPUT -> Triple(Color(0xFF32144E), Color(0xFFC084FC), Color(0xFFE9D5FF))
        LogCategory.MOTION -> Triple(Color(0xFF0D3349), Color(0xFF22D3EE), Color(0xFFA5F3FC))
        LogCategory.DRIVER -> Triple(Color(0xFF063A29), Color(0xFF10B981), Color(0xFFA7F3D0))
        LogCategory.HAPTICS -> Triple(Color(0xFF42122E), Color(0xFFF472B6), Color(0xFFFBCFE8))
        LogCategory.SYSTEM, LogCategory.ALL -> Triple(Color(0xFF1E293B), Color(0xFF94A3B8), Color(0xFFE2E8F0))
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 1.5.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Line number
        Text(
            text = String.format("%04d", log.id % 10000),
            color = Color(0xFF334155),
            fontFamily = FontFamily.Monospace,
            fontSize = 10.5.sp,
            modifier = Modifier.width(36.dp)
        )

        // Timestamp
        if (log.timestamp.isNotBlank()) {
            Text(
                text = log.timestamp,
                color = Color(0xFF64748B),
                fontFamily = FontFamily.Monospace,
                fontSize = 10.5.sp,
                modifier = Modifier.width(58.dp)
            )
        }

        // Subsystem Category Badge Pill
        Box(
            modifier = Modifier
                .padding(end = 8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(badgeBg)
                .border(0.8.dp, badgeText.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                .padding(horizontal = 5.dp, vertical = 1.dp)
        ) {
            Text(
                text = log.category.badge,
                color = badgeText,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 9.sp
            )
        }

        // Log message
        Text(
            text = log.message,
            color = messageColor,
            fontFamily = FontFamily.Monospace,
            fontSize = 11.5.sp,
            lineHeight = 16.sp,
            modifier = Modifier.weight(1f)
        )

        // Repetition count pill (e.g. x14)
        if (log.count > 1) {
            Spacer(modifier = Modifier.width(6.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFF3B2910))
                    .border(0.8.dp, Color(0xFFF59E0B), RoundedCornerShape(4.dp))
                    .padding(horizontal = 5.dp, vertical = 1.dp)
            ) {
                Text(
                    text = "x${log.count}",
                    color = Color(0xFFFCD34D),
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 9.5.sp
                )
            }
        }
    }
}
