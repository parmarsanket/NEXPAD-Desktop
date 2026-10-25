package com.sanket.tools.nexpaddesktop.ui.debug

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sanket.tools.nexpaddesktop.ui.theme.NeonPalette

@Composable
fun HeaderTitleRow(isStreamPaused: Boolean) {
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
fun HeaderActionToolbar(
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
