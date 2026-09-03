package com.sanket.tools.nexpaddesktop.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sanket.tools.nexpaddesktop.ui.theme.NeonPalette
import com.sanket.tools.nexpaddesktop.utils.AppLogger

@Composable
fun OutputScreen() {
    val listState = rememberLazyListState()
    val trigger = AppLogger.updateTrigger.value
    
    // Read the copy of logs whenever the trigger changes
    val logs = remember(trigger) { AppLogger.getLogsCopy() }
    
    val clipboardManager = LocalClipboardManager.current

    LaunchedEffect(logs.size) {
        if (logs.isNotEmpty()) {
            listState.animateScrollToItem(logs.size - 1)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D0D14))
            .padding(16.dp)
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize().padding(end = 40.dp)
        ) {
            items(logs) { log ->
                Text(
                    text = log,
                    color = NeonPalette.Green,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }
        }

        IconButton(
            onClick = {
                val allLogs = logs.joinToString("\n")
                clipboardManager.setText(buildAnnotatedString { append(allLogs) })
            },
            modifier = Modifier.align(Alignment.TopEnd).background(Color(0xFF111111).copy(alpha = 0.8f))
        ) {
            Icon(
                imageVector = Icons.Default.List,
                contentDescription = "Copy Logs",
                tint = NeonPalette.Cyan
            )
        }
    }
}
