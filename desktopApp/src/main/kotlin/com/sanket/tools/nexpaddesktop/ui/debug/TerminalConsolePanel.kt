package com.sanket.tools.nexpaddesktop.ui.debug

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sanket.tools.nexpaddesktop.ui.components.DesktopVerticalScrollbar
import com.sanket.tools.nexpaddesktop.ui.theme.NeonPalette
import com.sanket.tools.nexpaddesktop.utils.LogCategory
import com.sanket.tools.nexpaddesktop.utils.LogEntry
import com.sanket.tools.nexpaddesktop.utils.LogLevel

@Composable
fun TerminalConsolePanel(
    filteredLogs: List<LogEntry>,
    allLogs: List<LogEntry>,
    listState: LazyListState,
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
                BasicTextField(
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
 * Color-coded terminal row with highlighted search keywords and clickable detail inspection.
 */
@Composable
fun LogTerminalRow(
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
fun buildHighlightedText(text: String, query: String, defaultColor: Color): AnnotatedString {
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
