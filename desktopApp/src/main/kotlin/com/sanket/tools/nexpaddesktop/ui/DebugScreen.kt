package com.sanket.tools.nexpaddesktop.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteSweep
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sanket.tools.nexpaddesktop.ui.theme.NeonPalette
import com.sanket.tools.nexpaddesktop.utils.AppLogger
import kotlinx.coroutines.launch

/**
 * Dedicated Debug Console Screen for NEXPAD Desktop.
 *
 * Streams real-time system logs, network packets, ViGEm driver events,
 * and IMU telemetry from [AppLogger] with full copy and purge capabilities.
 */
@Composable
fun DebugScreen() {
    val listState = rememberLazyListState()
    val trigger = AppLogger.updateTrigger.value
    val coroutineScope = rememberCoroutineScope()
    
    // Read the copy of logs whenever the trigger changes
    val logs = remember(trigger) { AppLogger.getLogsCopy() }
    val clipboardManager = LocalClipboardManager.current
    var isCopied by remember { mutableStateOf(false) }

    LaunchedEffect(logs.size) {
        if (logs.isNotEmpty()) {
            listState.animateScrollToItem(logs.size - 1)
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
            .padding(24.dp)
    ) {
        // ── Top Header & Actions ──
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(NeonPalette.Cyan.copy(alpha = 0.12f))
                        .border(1.dp, NeonPalette.Cyan.copy(alpha = 0.4f), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.BugReport,
                        contentDescription = "Debug",
                        tint = NeonPalette.Cyan,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = "DEBUG & SYSTEM CONSOLE",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Real-time engine logs, transport handshakes, and input telemetry",
                        color = NeonPalette.CardIdleText,
                        fontSize = 12.sp
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Log count pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF161B2C))
                        .border(1.dp, NeonPalette.CardIdleBorder, RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "${logs.size} Entries",
                        color = NeonPalette.Green,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                // Copy Logs Button
                Button(
                    onClick = {
                        val allLogs = logs.joinToString("\n")
                        clipboardManager.setText(buildAnnotatedString { append(allLogs) })
                        isCopied = true
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isCopied) NeonPalette.Green.copy(alpha = 0.2f) else Color(0xFF1E2638),
                        contentColor = if (isCopied) NeonPalette.Green else NeonPalette.Cyan
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy Logs",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isCopied) "Copied!" else "Copy All",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                // Clear Logs Button
                OutlinedButton(
                    onClick = { AppLogger.clearLogs() },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFF5555)),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFFF5555).copy(alpha = 0.5f))
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteSweep,
                        contentDescription = "Clear Logs",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Clear", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // ── Monospace Terminal Box ──
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF080C14))
                .border(1.dp, Color(0xFF1A2338), RoundedCornerShape(12.dp))
                .padding(16.dp)
        ) {
            if (logs.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Waiting for application logs... Connected device telemetry will appear here.",
                        color = NeonPalette.CardIdleText,
                        fontSize = 13.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize()
                ) {
                    itemsIndexed(logs) { index, log ->
                        val logColor = when {
                            log.contains("ERROR", ignoreCase = true) || log.contains("❌") || log.contains("Exception") -> Color(0xFFFF5555)
                            log.contains("WARN", ignoreCase = true) || log.contains("⚠️") -> Color(0xFFFFB300)
                            log.contains("DSU", ignoreCase = true) || log.contains("🎮") -> NeonPalette.Cyan
                            log.contains("✅") || log.contains("Connected", ignoreCase = true) -> NeonPalette.Green
                            log.contains("QoS", ignoreCase = true) || log.contains("⚡") -> Color(0xFFE040FB)
                            else -> Color(0xFF90E0EF)
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp)
                        ) {
                            Text(
                                text = String.format("%04d ", index + 1),
                                color = Color(0xFF4A5568),
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                modifier = Modifier.width(42.dp)
                            )
                            Text(
                                text = log,
                                color = logColor,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
