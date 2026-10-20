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

/**
 * Filter categories for subsystem routing.
 */
private enum class DebugFilter(val label: String, val icon: String) {
    ALL("All", "📋"),
    NETWORK("Network", "📡"),
    INPUT_DRIVER("Input & Driver", "🎮"),
    MOTION_DSU("Motion & DSU", "🎯"),
    HAPTICS("Haptics", "💥"),
    SYSTEM("System QoS", "⚙️"),
    WARNINGS_ERRORS("Issues", "⚠️")
}

/**
 * Log severity level filters.
 */
private enum class LogLevelFilter(val label: String, val level: LogLevel?) {
    ALL("ALL", null),
    INFO("INFO", LogLevel.INFO),
    WARN("WARN", LogLevel.WARN),
    ERROR("ERR", LogLevel.ERROR),
    VERBOSE("VRB", LogLevel.VERBOSE)
}

/**
 * Layout arrangements for telemetry and terminal presentation.
 */
private enum class DebugLayoutMode(val label: String, val icon: String) {
    STACKED("Stacked", "⬇"),
    SIDE_BY_SIDE("Side Layout", "⇄"),
    TERMINAL_ONLY("Logs Only", "📋")
}

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
            val rxPackets = UdpServer.liveTotalRxPackets
            val txFeedbacks = UdpServer.liveTotalTxFeedbacks
            val lossPct = UdpServer.livePacketLossPct
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
                    primaryText = "RX: ${String.format("%,d", rxPackets)} pkts",
                    secondaryText = "TX: ${String.format("%,d", txFeedbacks)} feedbacks ($pollingHz Sync)",
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

/**
 * Top title row for the Debug Screen.
 */
@Composable
private fun HeaderTitleRow(isStreamPaused: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(NeonPalette.Cyan.copy(alpha = 0.12f))
                .border(1.dp, NeonPalette.Cyan.copy(alpha = 0.45f), RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Terminal,
                contentDescription = "Debug",
                tint = NeonPalette.Cyan,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "ENGINE TELEMETRY & DIAGNOSTICS",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.6.sp
                )
                if (isStreamPaused) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFF3B2910))
                            .border(1.dp, Color(0xFFF59E0B), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = "STREAM PAUSED",
                            color = Color(0xFFFCD34D),
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
            Text(
                text = "100% loop transparency • ViGEmBus • DSU UDP 26760 • IMU Sensors",
                color = NeonPalette.CardIdleText,
                fontSize = 10.5.sp
            )
        }
    }
}

/**
 * Responsive Action Toolbar supporting horizontal scrolling when width is constrained.
 */
@Composable
private fun HeaderActionToolbar(
    layoutMode: DebugLayoutMode,
    onLayoutModeChange: (DebugLayoutMode) -> Unit,
    isStreamPaused: Boolean,
    onTogglePause: () -> Unit,
    autoScrollEnabled: Boolean,
    onToggleAutoScroll: () -> Unit,
    isCopied: Boolean,
    onCopyLogs: () -> Unit,
    onClearLogs: () -> Unit,
    filteredCount: Int,
    totalCount: Int
) {
    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Layout Mode Switcher (Stacked vs Side-by-Side vs Logs Only)
        DebugLayoutMode.entries.forEach { mode ->
            val isSelected = layoutMode == mode
            OutlinedButton(
                onClick = { onLayoutModeChange(mode) },
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = if (isSelected) NeonPalette.Cyan.copy(alpha = 0.16f) else Color.Transparent,
                    contentColor = if (isSelected) NeonPalette.Cyan else Color.White.copy(alpha = 0.65f)
                ),
                border = BorderStroke(1.dp, if (isSelected) NeonPalette.Cyan.copy(alpha = 0.6f) else Color(0xFF26334D)),
                shape = RoundedCornerShape(7.dp),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                modifier = Modifier.height(28.dp)
            ) {
                Text(
                    text = "${mode.icon} ${mode.label}",
                    fontSize = 10.5.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
            }
        }

        Spacer(modifier = Modifier.width(4.dp))
        Box(modifier = Modifier.width(1.dp).height(16.dp).background(Color(0xFF26334D)))
        Spacer(modifier = Modifier.width(4.dp))

        // Pause / Resume Stream Toggle
        OutlinedButton(
            onClick = onTogglePause,
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = if (isStreamPaused) Color(0xFF382312) else Color.Transparent,
                contentColor = if (isStreamPaused) Color(0xFFFBBF24) else Color.White.copy(alpha = 0.7f)
            ),
            border = BorderStroke(1.dp, if (isStreamPaused) Color(0xFFF59E0B) else Color(0xFF26334D)),
            shape = RoundedCornerShape(7.dp),
            contentPadding = PaddingValues(horizontal = 9.dp, vertical = 4.dp),
            modifier = Modifier.height(28.dp)
        ) {
            Text(
                text = if (isStreamPaused) "▶ Resume" else "⏸ Pause",
                fontSize = 10.5.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        // Auto-Scroll Toggle
        OutlinedButton(
            onClick = onToggleAutoScroll,
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = if (autoScrollEnabled) NeonPalette.Cyan.copy(alpha = 0.12f) else Color.Transparent,
                contentColor = if (autoScrollEnabled) NeonPalette.Cyan else Color.White.copy(alpha = 0.5f)
            ),
            border = BorderStroke(1.dp, if (autoScrollEnabled) NeonPalette.Cyan.copy(alpha = 0.5f) else Color(0xFF26334D)),
            shape = RoundedCornerShape(7.dp),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
            modifier = Modifier.height(28.dp)
        ) {
            Text(
                text = if (autoScrollEnabled) "⬇ Auto-Scroll" else "⏸ Manual",
                fontSize = 10.5.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        // Copy All Logs
        Button(
            onClick = onCopyLogs,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isCopied) NeonPalette.Green.copy(alpha = 0.25f) else Color(0xFF1E283C),
                contentColor = if (isCopied) NeonPalette.Green else NeonPalette.Cyan
            ),
            shape = RoundedCornerShape(7.dp),
            contentPadding = PaddingValues(horizontal = 9.dp, vertical = 4.dp),
            modifier = Modifier.height(28.dp)
        ) {
            Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Copy", modifier = Modifier.size(13.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = if (isCopied) "Copied!" else "Copy", fontSize = 10.5.sp, fontWeight = FontWeight.SemiBold)
        }

        // Clear Logs
        OutlinedButton(
            onClick = onClearLogs,
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFF5555)),
            border = BorderStroke(1.dp, Color(0xFFFF5555).copy(alpha = 0.45f)),
            shape = RoundedCornerShape(7.dp),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
            modifier = Modifier.height(28.dp)
        ) {
            Icon(imageVector = Icons.Default.DeleteSweep, contentDescription = "Clear", modifier = Modifier.size(13.dp))
            Spacer(modifier = Modifier.width(3.dp))
            Text(text = "Clear", fontSize = 10.5.sp, fontWeight = FontWeight.SemiBold)
        }

        // Log Counter Pill
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0xFF0F172A))
                .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(6.dp))
                .padding(horizontal = 7.dp, vertical = 4.dp)
        ) {
            Text(
                text = "$filteredCount/$totalCount",
                color = NeonPalette.Cyan,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * Terminal Console Panel with category pills, severity filter, search input, and monospace list.
 */
@Composable
private fun TerminalConsolePanel(
    filteredLogs: List<LogEntry>,
    allLogs: List<LogEntry>,
    listState: androidx.compose.foundation.lazy.LazyListState,
    selectedFilter: DebugFilter,
    onSelectFilter: (DebugFilter) -> Unit,
    selectedLevelFilter: LogLevelFilter,
    onSelectLevelFilter: (LogLevelFilter) -> Unit,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onRowClick: (LogEntry) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF060911))
            .border(1.dp, Color(0xFF162034), RoundedCornerShape(10.dp))
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Filter bar & Search box
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Category & Level Filter Row
            Row(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Category Pills
                DebugFilter.entries.forEach { filter ->
                    val isSelected = selectedFilter == filter
                    Box(
                        modifier = Modifier
                            .height(24.dp)
                            .clip(RoundedCornerShape(5.dp))
                            .background(if (isSelected) NeonPalette.Cyan.copy(alpha = 0.20f) else Color(0xFF0D1322))
                            .border(1.dp, if (isSelected) NeonPalette.Cyan else Color(0xFF1F2A40), RoundedCornerShape(5.dp))
                            .clickable { onSelectFilter(filter) }
                            .padding(horizontal = 7.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${filter.icon} ${filter.label}",
                            color = if (isSelected) NeonPalette.Cyan else Color.White.copy(alpha = 0.7f),
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 10.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.width(4.dp))
                Box(modifier = Modifier.width(1.dp).height(14.dp).background(Color(0xFF26334D)))
                Spacer(modifier = Modifier.width(4.dp))

                // Severity Level Pills (ALL, INFO, WARN, ERR, VRB)
                LogLevelFilter.entries.forEach { lvl ->
                    val isSelected = selectedLevelFilter == lvl
                    val lvlColor = when (lvl) {
                        LogLevelFilter.ALL -> Color(0xFF94A3B8)
                        LogLevelFilter.INFO -> Color(0xFF38BDF8)
                        LogLevelFilter.WARN -> Color(0xFFFBBF24)
                        LogLevelFilter.ERROR -> Color(0xFFF87171)
                        LogLevelFilter.VERBOSE -> Color(0xFFA78BFA)
                    }
                    Box(
                        modifier = Modifier
                            .height(22.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (isSelected) lvlColor.copy(alpha = 0.22f) else Color(0xFF0B101E))
                            .border(1.dp, if (isSelected) lvlColor else Color(0xFF1E273A), RoundedCornerShape(4.dp))
                            .clickable { onSelectLevelFilter(lvl) }
                            .padding(horizontal = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = lvl.label,
                            color = if (isSelected) lvlColor else Color.White.copy(alpha = 0.5f),
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 9.5.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Quick Search Box
            Row(
                modifier = Modifier
                    .widthIn(min = 160.dp, max = 230.dp)
                    .height(26.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(Color(0xFF0A0F1A))
                    .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(5.dp))
                    .padding(horizontal = 7.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(12.dp))
                Spacer(modifier = Modifier.width(4.dp))
                androidx.compose.foundation.text.BasicTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    singleLine = true,
                    textStyle = TextStyle(color = Color.White, fontSize = 10.5.sp, fontFamily = FontFamily.Monospace),
                    modifier = Modifier.weight(1f),
                    decorationBox = { innerTextField ->
                        if (searchQuery.isEmpty()) {
                            Text("Search...", color = Color.Gray.copy(alpha = 0.6f), fontSize = 10.5.sp)
                        }
                        innerTextField()
                    }
                )
                if (searchQuery.isNotEmpty()) {
                    Text(
                        text = "${filteredLogs.size}",
                        color = NeonPalette.Cyan,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(end = 3.dp)
                    )
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Clear search",
                        tint = Color.Gray,
                        modifier = Modifier
                            .size(12.dp)
                            .clickable { onSearchQueryChange("") }
                    )
                }
            }
        }

        // Terminal List View
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0xFF04060C))
                .border(0.8.dp, Color(0xFF121B2C), RoundedCornerShape(6.dp))
                .padding(8.dp)
        ) {
            if (filteredLogs.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = if (allLogs.isEmpty()) "Waiting for engine events... UDP handshakes and input frames will appear here." else "No log entries match the current filter / query.",
                        color = NeonPalette.CardIdleText,
                        fontSize = 11.5.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize().padding(end = 13.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    items(items = filteredLogs, key = { it.id }) { log ->
                        LogTerminalRow(
                            log = log,
                            searchQuery = searchQuery,
                            onClick = { onRowClick(log) }
                        )
                    }
                }
                DesktopVerticalScrollbar(
                    adapter = rememberScrollbarAdapter(listState),
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .fillMaxHeight()
                        .padding(vertical = 2.dp, horizontal = 2.dp),
                    thickness = 8.dp,
                    trackWidth = 11.dp,
                    showTrackGutter = true
                )
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
                    fontSize = 9.sp,
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
 * Real-time Hardware Stream Inspector bench.
 * Renders live stick crosshairs, trigger pressures, pressed button matrix, 6-axis IMU, and haptic rumble gauges.
 */
@Composable
private fun HardwareStreamInspector(viewModel: DesktopViewModel) {
    val input = viewModel.latestInput
    val feedback = viewModel.latestFeedback

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF090E1B))
            .border(1.dp, Color(0xFF1C2740), RoundedCornerShape(10.dp))
            .padding(11.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🎮", fontSize = 12.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "LIVE HARDWARE & SENSOR STREAM INSPECTOR",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.6.sp
                    )
                }
                Text(
                    text = "Sequence #${input.sequenceNumber} • Stream: 200Hz • Display Budget: ~16.6ms (60 FPS)",
                    color = Color.White.copy(alpha = 0.45f),
                    fontSize = 9.5.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1. Dual Thumbstick Visualizers
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StickVisualizer(title = "LEFT STICK", x = input.leftStickX, y = input.leftStickY)
                    StickVisualizer(title = "RIGHT STICK", x = input.rightStickX, y = input.rightStickY)
                }

                // 2. Analog Triggers
                Column(
                    modifier = Modifier.width(90.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    TriggerBar(label = "LT TRIGGER", value = input.triggerL2)
                    TriggerBar(label = "RT TRIGGER", value = input.triggerR2)
                }

                // 3. Digital Buttons Matrix
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("BUTTON MATRIX", color = Color.White.copy(alpha = 0.6f), fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                        ButtonIndicatorChip(label = "A", isPressed = input.btnA)
                        ButtonIndicatorChip(label = "B", isPressed = input.btnB)
                        ButtonIndicatorChip(label = "X", isPressed = input.btnX)
                        ButtonIndicatorChip(label = "Y", isPressed = input.btnY)
                        ButtonIndicatorChip(label = "LB", isPressed = input.btnL1)
                        ButtonIndicatorChip(label = "RB", isPressed = input.btnR1)
                        ButtonIndicatorChip(label = "L3", isPressed = input.btnL3)
                        ButtonIndicatorChip(label = "R3", isPressed = input.btnR3)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                        ButtonIndicatorChip(label = "▲", isPressed = input.dpadUp)
                        ButtonIndicatorChip(label = "▼", isPressed = input.dpadDown)
                        ButtonIndicatorChip(label = "◀", isPressed = input.dpadLeft)
                        ButtonIndicatorChip(label = "▶", isPressed = input.dpadRight)
                        ButtonIndicatorChip(label = "Start", isPressed = input.btnStart)
                        ButtonIndicatorChip(label = "Select", isPressed = input.btnSelect)
                        ButtonIndicatorChip(label = "Guide", isPressed = input.btnGuide)
                    }
                }

                // 4. 6-Axis Motion / IMU Telemetry (Audit: X = Pitch, Y = Roll, Z = Yaw)
                val radToDeg = (180.0 / PI).toFloat()
                val gyroPitchDeg = input.gyroX * radToDeg
                val gyroRollDeg = input.gyroY * radToDeg
                val gyroYawDeg = input.gyroZ * radToDeg
                val totalG = sqrt(input.accelX * input.accelX + input.accelY * input.accelY + input.accelZ * input.accelZ) / 9.80665f

                Column(
                    modifier = Modifier.widthIn(min = 220.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text("6-AXIS IMU SENSORS", color = Color.White.copy(alpha = 0.6f), fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
                    Text(
                        text = "Gyro: P:${String.format("%+.1f", gyroPitchDeg)}° Y:${String.format("%+.1f", gyroYawDeg)}° R:${String.format("%+.1f", gyroRollDeg)}°/s",
                        color = NeonPalette.Cyan,
                        fontSize = 9.5.sp,
                        fontFamily = FontFamily.Monospace,
                        maxLines = 1,
                        softWrap = false
                    )
                    Text(
                        text = "Accel: ${String.format("%.2f", totalG)}G (X:${String.format("%+.1f", input.accelX)} Y:${String.format("%+.1f", input.accelY)} Z:${String.format("%+.1f", input.accelZ)})",
                        color = Color(0xFFA5F3FC),
                        fontSize = 9.5.sp,
                        fontFamily = FontFamily.Monospace,
                        maxLines = 1,
                        softWrap = false
                    )
                    Text(
                        text = "Fused Aim: Yaw ${String.format("%+.1f°", viewModel.processedYaw)} | Pitch ${String.format("%+.1f°", viewModel.processedPitch)}",
                        color = NeonPalette.Green,
                        fontSize = 9.5.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        softWrap = false
                    )
                }

                // 5. Haptic Rumble Feedback
                Column(
                    modifier = Modifier.width(110.dp),
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Text("HAPTIC RUMBLE", color = Color.White.copy(alpha = 0.6f), fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
                    RumbleMotorBar(label = "HEAVY MOTOR", speed = feedback.leftMotorSpeed, barColor = Color(0xFFF472B6))
                    RumbleMotorBar(label = "LIGHT MOTOR", speed = feedback.rightMotorSpeed, barColor = Color(0xFF38BDF8))
                }
            }
        }
    }
}

/**
 * 2D Circular radar visualizer for thumbstick coordinates.
 */
@Composable
private fun StickVisualizer(
    title: String,
    x: Float,
    y: Float,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Text(
            text = title,
            color = Color.White.copy(alpha = 0.65f),
            fontSize = 9.5.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(3.dp))
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(CircleShape)
                .background(Color(0xFF060912))
                .border(1.dp, Color(0xFF1E2D4A), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val radius = size.width / 2f - 3f

                // Crosshairs
                drawLine(
                    color = Color(0xFF1A263D),
                    start = Offset(center.x, 3f),
                    end = Offset(center.x, size.height - 3f),
                    strokeWidth = 1f
                )
                drawLine(
                    color = Color(0xFF1A263D),
                    start = Offset(3f, center.y),
                    end = Offset(size.width - 3f, center.y),
                    strokeWidth = 1f
                )

                // Inner deadzone guide circle
                drawCircle(
                    color = Color(0xFF1A263D),
                    radius = radius * 0.45f,
                    style = Stroke(width = 1f)
                )

                // Moving puck
                val clampedX = x.coerceIn(-1f, 1f)
                val clampedY = y.coerceIn(-1f, 1f)
                val puckOffset = Offset(
                    center.x + clampedX * (radius - 5f),
                    center.y - clampedY * (radius - 5f)
                )
                drawCircle(
                    color = NeonPalette.Cyan.copy(alpha = 0.4f),
                    radius = 7f,
                    center = puckOffset
                )
                drawCircle(
                    color = NeonPalette.Cyan,
                    radius = 4f,
                    center = puckOffset
                )
            }
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = "X:${String.format("%+.2f", x)} Y:${String.format("%+.2f", y)}",
            color = NeonPalette.Cyan,
            fontSize = 9.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.SemiBold
        )
    }
}

/**
 * Analog trigger progress bar.
 */
@Composable
private fun TriggerBar(
    label: String,
    value: Float,
    modifier: Modifier = Modifier
) {
    val clamped = value.coerceIn(0f, 1f)
    val percent = (clamped * 100).toInt()
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, color = Color.White.copy(alpha = 0.6f), fontSize = 9.sp, fontWeight = FontWeight.Bold)
            Text("$percent%", color = if (percent > 0) NeonPalette.Green else Color.Gray, fontSize = 9.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.SemiBold)
        }
        Spacer(modifier = Modifier.height(2.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(5.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(Color(0xFF0F172A))
                .border(0.5.dp, Color(0xFF1E293B), RoundedCornerShape(3.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(clamped)
                    .clip(RoundedCornerShape(3.dp))
                    .background(if (percent > 50) NeonPalette.Green else NeonPalette.Cyan)
            )
        }
    }
}

/**
 * Digital button active chip.
 */
@Composable
private fun ButtonIndicatorChip(
    label: String,
    isPressed: Boolean
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(if (isPressed) NeonPalette.Green.copy(alpha = 0.28f) else Color(0xFF080D19))
            .border(0.8.dp, if (isPressed) NeonPalette.Green else Color(0xFF1C2740), RoundedCornerShape(4.dp))
            .padding(horizontal = 5.dp, vertical = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (isPressed) NeonPalette.Green else Color.White.copy(alpha = 0.45f),
            fontWeight = if (isPressed) FontWeight.Bold else FontWeight.Normal,
            fontSize = 8.5.sp,
            fontFamily = FontFamily.Monospace
        )
    }
}

/**
 * Haptic rumble motor gauge bar.
 */
@Composable
private fun RumbleMotorBar(
    label: String,
    speed: Int,
    barColor: Color,
    modifier: Modifier = Modifier
) {
    val fraction = speed.coerceIn(0, 255) / 255f
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, color = Color.White.copy(alpha = 0.6f), fontSize = 8.5.sp, fontWeight = FontWeight.Bold)
            Text("$speed", color = if (speed > 0) barColor else Color.Gray, fontSize = 8.5.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.SemiBold)
        }
        Spacer(modifier = Modifier.height(2.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.5.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(Color(0xFF0F172A))
                .border(0.5.dp, Color(0xFF1E293B), RoundedCornerShape(3.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(fraction)
                    .clip(RoundedCornerShape(3.dp))
                    .background(barColor)
            )
        }
    }
}

/**
 * Color-coded terminal row with highlighted search keywords and clickable detail inspection.
 */
@Composable
private fun LogTerminalRow(
    log: LogEntry,
    searchQuery: String,
    onClick: () -> Unit
) {
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

    val levelColor = when (log.level) {
        LogLevel.ERROR -> Color(0xFFFF5555)
        LogLevel.WARN -> Color(0xFFFBBF24)
        LogLevel.INFO -> Color(0xFF38BDF8)
        LogLevel.VERBOSE -> Color(0xFFA78BFA)
        LogLevel.ALL -> Color(0xFF94A3B8)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 1.5.dp, horizontal = 2.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Line number
        Text(
            text = String.format("%04d", log.id % 10000),
            color = Color(0xFF334155),
            fontFamily = FontFamily.Monospace,
            fontSize = 10.sp,
            modifier = Modifier.width(34.dp)
        )

        // Timestamp
        if (log.timestamp.isNotBlank()) {
            Text(
                text = log.timestamp,
                color = Color(0xFF64748B),
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                modifier = Modifier.width(56.dp)
            )
        }

        // Log Level indicator
        Text(
            text = "[${log.level.badge}]",
            color = levelColor,
            fontFamily = FontFamily.Monospace,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(36.dp)
        )

        // Subsystem Category Badge Pill
        Box(
            modifier = Modifier
                .padding(end = 6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(badgeBg)
                .border(0.6.dp, badgeText.copy(alpha = 0.5f), RoundedCornerShape(3.dp))
                .padding(horizontal = 4.dp, vertical = 0.5.dp)
        ) {
            Text(
                text = log.category.badge,
                color = badgeText,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 8.5.sp
            )
        }

        // Highlighted Log Message (memoized per entry/query)
        val annotatedMessage = remember(log.id, log.count, searchQuery) {
            buildHighlightedText(
                text = log.message,
                query = searchQuery,
                defaultColor = messageColor
            )
        }

        Text(
            text = annotatedMessage,
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            lineHeight = 15.sp,
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
                    .padding(horizontal = 4.dp, vertical = 0.5.dp)
            ) {
                Text(
                    text = "x${log.count}",
                    color = Color(0xFFFCD34D),
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 9.sp
                )
            }
        }
    }
}

/**
 * Builds an [AnnotatedString] that highlights occurrences of [query] in [text] with a golden background.
 */
private fun buildHighlightedText(text: String, query: String, defaultColor: Color): AnnotatedString {
    if (query.isBlank()) {
        return buildAnnotatedString {
            pushStyle(SpanStyle(color = defaultColor))
            append(text)
            pop()
        }
    }
    return buildAnnotatedString {
        var startIndex = 0
        val lowerText = text.lowercase()
        val lowerQuery = query.lowercase()
        while (startIndex < text.length) {
            val matchIndex = lowerText.indexOf(lowerQuery, startIndex)
            if (matchIndex == -1) {
                pushStyle(SpanStyle(color = defaultColor))
                append(text.substring(startIndex))
                pop()
                break
            }
            if (matchIndex > startIndex) {
                pushStyle(SpanStyle(color = defaultColor))
                append(text.substring(startIndex, matchIndex))
                pop()
            }
            pushStyle(
                SpanStyle(
                    background = Color(0xFFF59E0B),
                    color = Color.Black,
                    fontWeight = FontWeight.Bold
                )
            )
            append(text.substring(matchIndex, matchIndex + query.length))
            pop()
            startIndex = matchIndex + query.length
        }
    }
}
