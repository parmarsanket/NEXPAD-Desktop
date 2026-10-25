package com.sanket.tools.nexpaddesktop.ui

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.sanket.tools.nexpaddesktop.connection.wifi.UdpServer
import com.sanket.tools.nexpaddesktop.ui.components.DesktopVerticalScrollbar
import com.sanket.tools.nexpaddesktop.ui.theme.NeonPalette
import com.sanket.tools.nexpaddesktop.utils.AppLogger
import com.sanket.tools.nexpaddesktop.utils.LogCategory
import com.sanket.tools.nexpaddesktop.utils.LogEntry
import com.sanket.tools.nexpaddesktop.utils.LogLevel
import com.sanket.tools.nexpaddesktop.viewmodel.DesktopViewModel
import kotlin.math.PI
import kotlin.math.sqrt

import com.sanket.tools.nexpaddesktop.ui.debug.*
/**
 * Ultra-Transparent Engine Debug Screen for NEXPAD Desktop.
 *
 * Provides complete 360-degree visibility into the background engine loops:
 * - 100% Responsive and vertically scrollable (resilient to small laptop windows)
 * - Clean Log Size Presets (Compact, Standard, Expanded, Max) with intuitive expand/shrink controls
 * - Side-by-Side and Stacked Layouts with smooth collapsible Hardware Inspector
 * - Live System Telemetry HUD Bar (session socket, RX/TX packet counters, packet loss %, ViGEm bus, DSU port)
 * - Real-Time Hardware Stream Inspector (dual thumbstick crosshairs, trigger gauges, button matrix, 6-axis IMU, rumble bars)
 * - Rich Monospace Terminal with keyword search highlighting, auto-scroll toggle, pause/freeze stream
 * - Multi-dimensional filtering by Subsystem Category (Network, Input, Driver, Motion, Haptics, System) and Severity Level
 * - File Export (`.log` file generation with complete session headers and host diagnostics)
 * - Interactive event inspection popover for deep stack analysis
 */
@Composable
fun DebugScreen(viewModel: DesktopViewModel = remember { DesktopViewModel() }) {
    DisposableEffect(Unit) {
        AppLogger.isScreenActive = true
        UdpServer.refreshLiveTelemetry()
        onDispose {
            AppLogger.isScreenActive = false
        }
    }

    val listState = rememberLazyListState()
    val outerScrollState = rememberScrollState()
    val trigger = AppLogger.updateTrigger.value
    
    // Live reactive logs and paused snapshot buffer
    val liveLogs = remember(trigger) { AppLogger.getLogEntriesCopy() }
    var isStreamPaused by remember { mutableStateOf(false) }
    var pausedSnapshot by remember { mutableStateOf<List<LogEntry>>(emptyList()) }
    
    val allLogs = if (isStreamPaused) pausedSnapshot else liveLogs
    val clipboardManager = LocalClipboardManager.current
    var isCopied by remember { mutableStateOf(false) }
    var selectedFilter by remember { mutableStateOf(DebugFilter.ALL) }
    var selectedLevelFilter by remember { mutableStateOf(LogLevelFilter.ALL) }
    var layoutMode by remember { mutableStateOf(DebugLayoutMode.STACKED) }
    var searchQuery by remember { mutableStateOf("") }
    var autoScrollEnabled by remember { mutableStateOf(true) }
    var notificationMessage by remember { mutableStateOf<String?>(null) }
    var selectedLogEntry by remember { mutableStateOf<LogEntry?>(null) }
    var isInspectorCollapsed by remember { mutableStateOf(false) }

    // Fixed comfortable log section height
    val terminalHeight = 440.dp

    // Filter logs according to active category, level, and search query
    val filteredLogs = remember(allLogs, selectedFilter, selectedLevelFilter, searchQuery) {
        allLogs.filter { entry ->
            val matchesCategory = when (selectedFilter) {
                DebugFilter.ALL -> true
                DebugFilter.NETWORK -> entry.category == LogCategory.NETWORK || entry.category == LogCategory.HANDSHAKE
                DebugFilter.INPUT_DRIVER -> entry.category == LogCategory.INPUT || entry.category == LogCategory.DRIVER
                DebugFilter.MOTION_DSU -> entry.category == LogCategory.MOTION
                DebugFilter.HAPTICS -> entry.category == LogCategory.HAPTICS
                DebugFilter.SYSTEM -> entry.category == LogCategory.SYSTEM
                DebugFilter.WARNINGS_ERRORS -> entry.category == LogCategory.WARNING || entry.category == LogCategory.ERROR
            }
            val matchesLevel = when (selectedLevelFilter) {
                LogLevelFilter.ALL -> true
                else -> entry.level == selectedLevelFilter.level
            }
            val matchesSearch = if (searchQuery.isBlank()) true else {
                entry.message.contains(searchQuery, ignoreCase = true) ||
                entry.timestamp.contains(searchQuery, ignoreCase = true) ||
                entry.category.badge.contains(searchQuery, ignoreCase = true) ||
                entry.level.badge.contains(searchQuery, ignoreCase = true)
            }
            matchesCategory && matchesLevel && matchesSearch
        }
    }

    LaunchedEffect(filteredLogs.size, autoScrollEnabled, isStreamPaused) {
        if (autoScrollEnabled && !isStreamPaused && filteredLogs.isNotEmpty()) {
            listState.scrollToItem(filteredLogs.size - 1)
        }
    }

    LaunchedEffect(notificationMessage) {
        if (notificationMessage != null) {
            kotlinx.coroutines.delay(3500)
            notificationMessage = null
        }
    }

    LaunchedEffect(isCopied) {
        if (isCopied) {
            kotlinx.coroutines.delay(2000)
            isCopied = false
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(NeonPalette.PanelBgBottom)
    ) {
        val screenWidth = maxWidth
        val isNarrow = screenWidth < 980.dp
        val isUltraNarrow = screenWidth < 680.dp

        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(outerScrollState)
                    .padding(horizontal = if (isUltraNarrow) 10.dp else 16.dp, vertical = 14.dp)
                    .padding(end = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // ── 1. Top Header & Primary Action Controls (Fully Responsive) ──
                if (isNarrow) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        HeaderTitleRow(isStreamPaused = isStreamPaused)
                        HeaderActionToolbar(
                            layoutMode = layoutMode,
                            onLayoutModeChange = { layoutMode = it },
                            isStreamPaused = isStreamPaused,
                            onTogglePause = {
                                if (isStreamPaused) {
                                    isStreamPaused = false
                                } else {
                                    pausedSnapshot = AppLogger.getLogEntriesCopy()
                                    isStreamPaused = true
                                }
                            },
                            autoScrollEnabled = autoScrollEnabled,
                            onToggleAutoScroll = { autoScrollEnabled = !autoScrollEnabled },
                            isCopied = isCopied,
                            onCopyLogs = {
                                val allLogsText = filteredLogs.joinToString("\n") { it.displayString() }
                                clipboardManager.setText(buildAnnotatedString { append(allLogsText) })
                                isCopied = true
                                notificationMessage = "Copied ${filteredLogs.size} logs to clipboard!"
                            },
                            onClearLogs = { AppLogger.clearLogs() },
                            filteredCount = filteredLogs.size,
                            totalCount = allLogs.size
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HeaderTitleRow(isStreamPaused = isStreamPaused)
                        HeaderActionToolbar(
                            layoutMode = layoutMode,
                            onLayoutModeChange = { layoutMode = it },
                            isStreamPaused = isStreamPaused,
                            onTogglePause = {
                                if (isStreamPaused) {
                                    isStreamPaused = false
                                } else {
                                    pausedSnapshot = AppLogger.getLogEntriesCopy()
                                    isStreamPaused = true
                                }
                            },
                            autoScrollEnabled = autoScrollEnabled,
                            onToggleAutoScroll = { autoScrollEnabled = !autoScrollEnabled },
                            isCopied = isCopied,
                            onCopyLogs = {
                                val allLogsText = filteredLogs.joinToString("\n") { it.displayString() }
                                clipboardManager.setText(buildAnnotatedString { append(allLogsText) })
                                isCopied = true
                                notificationMessage = "Copied ${filteredLogs.size} logs to clipboard!"
                            },
                            onClearLogs = { AppLogger.clearLogs() },
                            filteredCount = filteredLogs.size,
                            totalCount = allLogs.size
                        )
                    }
                }

            // ── 2. Floating Notification / Export Toast Banner ──
            AnimatedVisibility(
                visible = notificationMessage != null,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF0C243B))
                        .border(1.dp, NeonPalette.Cyan, RoundedCornerShape(8.dp))
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("✨", fontSize = 12.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = notificationMessage ?: "",
                                color = Color.White,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.Gray,
                            modifier = Modifier
                                .size(14.dp)
                                .clickable { notificationMessage = null }
                        )
                    }
                }
            }

            // ── 3. Live Telemetry HUD Bar (Under-the-Hood Transparency) ──
            val isSessionActive = UdpServer.isLiveSessionActive || (viewModel.activeTransport != com.sanket.tools.nexpaddesktop.connection.ActiveTransport.NONE)
            val deviceName = UdpServer.liveDeviceName.ifBlank { viewModel.connectedDeviceName ?: "No Controller" }
            val transport = UdpServer.liveTransportType.ifBlank { viewModel.activeTransport.displayName }
            val socketStr = UdpServer.liveClientAddress.ifBlank { "Port ${viewModel.streamingPort} Standby" }
            val rxPackets = UdpServer.liveTotalRxPackets.coerceAtLeast(0L)
            val txFeedbacks = UdpServer.liveTotalTxFeedbacks.coerceAtLeast(0L)
            val lossPct = UdpServer.livePacketLossPct.coerceIn(0, 100)
            val pollingHz = if (transport.contains("Bluetooth", ignoreCase = true)) "125 Hz" else "200 Hz"

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HudGlassCard(
                    title = "NETWORK SESSION",
                    primaryText = if (isSessionActive) deviceName else "Awaiting Client",
                    secondaryText = if (isSessionActive) "$transport • $socketStr" else "Wi-Fi UDP: 0.0.0.0:${viewModel.streamingPort}",
                    statusColor = if (isSessionActive) NeonPalette.Green else Color(0xFF64748B),
                    icon = "📡"
                )

                HudGlassCard(
                    title = "TRAFFIC TELEMETRY",
                    primaryText = "RX: ${String.format(java.util.Locale.US, "%,d", rxPackets)} pkts",
                    secondaryText = "TX: ${String.format(java.util.Locale.US, "%,d", txFeedbacks)} feedbacks ($pollingHz Sync)",
                    statusColor = if (rxPackets > 0) NeonPalette.Cyan else Color(0xFF64748B),
                    icon = "📥"
                )

                HudGlassCard(
                    title = "LINK QUALITY",
                    primaryText = if (lossPct == 0) "0.0% Loss (Pristine)" else "$lossPct% Packet Loss",
                    secondaryText = if (isSessionActive) "$pollingHz Polling • RTT Echo" else "Watchdog Standby",
                    statusColor = if (lossPct == 0) Color(0xFF10B981) else Color(0xFFF59E0B),
                    icon = "📶"
                )

                HudGlassCard(
                    title = "VIRTUAL GAMEPAD",
                    primaryText = viewModel.activeController.displayName,
                    secondaryText = if (viewModel.isDriverConnected) "ViGEmBus: Mounted & Active" else "ViGEmBus: Standby",
                    statusColor = if (viewModel.isDriverConnected) Color(0xFF34D399) else Color(0xFF64748B),
                    icon = "🎮"
                )

                HudGlassCard(
                    title = "DSU MOTION SERVER",
                    primaryText = if (viewModel.isDsuServerEnabled) "UDP Port 26760 (Active)" else "Server Disabled",
                    secondaryText = "${viewModel.dsuClientCount} Emulator Client(s)",
                    statusColor = if (viewModel.isDsuServerEnabled) Color(0xFF38BDF8) else Color(0xFF64748B),
                    icon = "🎯"
                )

                val runtime = Runtime.getRuntime()
                val usedMemMb = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024)
                val maxMemMb = runtime.maxMemory() / (1024 * 1024)
                HudGlassCard(
                    title = "SYSTEM MEMORY",
                    primaryText = "${usedMemMb} MB Used (${maxMemMb} MB Max)",
                    secondaryText = "Ring Buffer: ${allLogs.size} logs (Bounded)",
                    statusColor = if (usedMemMb < maxMemMb * 0.8) NeonPalette.Green else Color(0xFFF59E0B),
                    icon = "🧠"
                )
            }

            // ── 4. Main Body: Stacked vs Side-by-Side vs Logs-Only ──
            when (layoutMode) {
                DebugLayoutMode.SIDE_BY_SIDE -> {
                    // Side-by-Side: Hardware Inspector on left, Terminal on right
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(terminalHeight),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Left Column: Hardware Stream Inspector
                        Box(
                            modifier = Modifier
                                .weight(0.46f)
                                .fillMaxHeight()
                        ) {
                            HardwareStreamInspector(viewModel = viewModel)
                        }

                        // Right Column: Terminal Console
                        Box(
                            modifier = Modifier
                                .weight(0.54f)
                                .fillMaxHeight()
                        ) {
                            TerminalConsolePanel(
                                filteredLogs = filteredLogs,
                                allLogs = allLogs,
                                listState = listState,
                                selectedFilter = selectedFilter,
                                onSelectFilter = { selectedFilter = it },
                                selectedLevelFilter = selectedLevelFilter,
                                onSelectLevelFilter = { selectedLevelFilter = it },
                                searchQuery = searchQuery,
                                onSearchQueryChange = { searchQuery = it },
                                onRowClick = { selectedLogEntry = it }
                            )
                        }
                    }
                }

                DebugLayoutMode.STACKED -> {
                    // 1. Hardware Stream Inspector (with Collapse Toggle)
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable { isInspectorCollapsed = !isInspectorCollapsed }
                                    .padding(vertical = 2.dp, horizontal = 4.dp)
                            ) {
                                Icon(
                                    imageVector = if (isInspectorCollapsed) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowUp,
                                    contentDescription = "Toggle Inspector",
                                    tint = NeonPalette.Cyan,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isInspectorCollapsed) "Show Hardware Inspector" else "Live Hardware & Sensor Stream Inspector",
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }

                        AnimatedVisibility(
                            visible = !isInspectorCollapsed,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            HardwareStreamInspector(viewModel = viewModel)
                        }
                    }

                    // 2. Terminal Console Panel
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(terminalHeight)
                    ) {
                        TerminalConsolePanel(
                            filteredLogs = filteredLogs,
                            allLogs = allLogs,
                            listState = listState,
                            selectedFilter = selectedFilter,
                            onSelectFilter = { selectedFilter = it },
                            selectedLevelFilter = selectedLevelFilter,
                            onSelectLevelFilter = { selectedLevelFilter = it },
                            searchQuery = searchQuery,
                            onSearchQueryChange = { searchQuery = it },
                            onRowClick = { selectedLogEntry = it }
                        )
                    }
                }

                DebugLayoutMode.TERMINAL_ONLY -> {
                    // Terminal Only: Full Height Maximized
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(600.dp)
                    ) {
                        TerminalConsolePanel(
                            filteredLogs = filteredLogs,
                            allLogs = allLogs,
                            listState = listState,
                            selectedFilter = selectedFilter,
                            onSelectFilter = { selectedFilter = it },
                            selectedLevelFilter = selectedLevelFilter,
                            onSelectLevelFilter = { selectedLevelFilter = it },
                            searchQuery = searchQuery,
                            onSearchQueryChange = { searchQuery = it },
                            onRowClick = { selectedLogEntry = it }
                        )
                    }
                }
            }
        }

        DesktopVerticalScrollbar(
            adapter = rememberScrollbarAdapter(outerScrollState),
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

    // ── 5. Detailed Log Entry Inspection Modal Dialog ──
    if (selectedLogEntry != null) {
        val entry = selectedLogEntry!!
        Dialog(onDismissRequest = { selectedLogEntry = null }) {
            Box(
                modifier = Modifier
                    .widthIn(min = 400.dp, max = 650.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF0B111E))
                    .border(1.dp, NeonPalette.Cyan.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    .padding(18.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "EVENT INSPECTOR #${entry.id}",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFF1E293B))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = entry.category.badge,
                                    color = NeonPalette.Cyan,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                        IconButton(onClick = { selectedLogEntry = null }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
                        }
                    }

                    // Metadata Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Column {
                            Text("TIMESTAMP", color = Color.Gray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            Text(entry.timestamp, color = Color.White, fontSize = 11.5.sp, fontFamily = FontFamily.Monospace)
                        }
                        Column {
                            Text("LEVEL", color = Color.Gray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            Text(entry.level.displayName, color = Color.White, fontSize = 11.5.sp, fontFamily = FontFamily.Monospace)
                        }
                        Column {
                            Text("REPETITIONS", color = Color.Gray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            Text("${entry.count} time(s)", color = Color.White, fontSize = 11.5.sp, fontFamily = FontFamily.Monospace)
                        }
                    }

                    // Raw Message Container
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF050810))
                            .border(1.dp, Color(0xFF1B283F), RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = entry.message,
                            color = Color(0xFFE2E8F0),
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            lineHeight = 18.sp
                        )
                    }

                    // Modal Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = {
                                clipboardManager.setText(buildAnnotatedString { append(entry.displayString()) })
                                notificationMessage = "Copied event #${entry.id} to clipboard!"
                                selectedLogEntry = null
                            },
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(5.dp))
                            Text("Copy Full Event", fontSize = 11.5.sp, color = NeonPalette.Cyan)
                        }
                    }
                }
            }
        }
    }
}
