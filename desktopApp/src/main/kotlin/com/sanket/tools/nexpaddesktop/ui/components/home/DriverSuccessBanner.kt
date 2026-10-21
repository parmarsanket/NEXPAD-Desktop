package com.sanket.tools.nexpaddesktop.ui.components.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sanket.tools.nexpaddesktop.ui.theme.NeonPalette

/**
 * Modern user-friendly toast / success banner shown when the WinUSB driver installation succeeds.
 * Informs the user to restart or open the NEXPAD Android app to begin playing immediately.
 */
@Composable
fun DriverSuccessBanner(
    message: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val successColor = remember { Color(0xFF00E676) }
    val glowBrush = remember {
        Brush.radialGradient(
            colors = listOf(successColor.copy(alpha = 0.2f), Color.Transparent)
        )
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .drawBehind {
                drawRoundRect(
                    brush = glowBrush,
                    topLeft = Offset(-12f, -12f),
                    size = Size(size.width + 24f, size.height + 24f),
                    cornerRadius = CornerRadius(14.dp.toPx() + 12f)
                )
            }
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF0D1F18))
            .border(1.dp, successColor.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = successColor,
                modifier = Modifier.size(26.dp)
            )
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "🎉 WinUSB Driver Installed Successfully!",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (message.isNotBlank()) message else "Please open or restart the NEXPAD app on your Android phone to connect.",
                    color = Color(0xFFB0FFD0),
                    fontSize = 11.sp,
                    lineHeight = 16.sp
                )
            }
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Dismiss",
                tint = Color.White.copy(alpha = 0.7f),
                modifier = Modifier
                    .size(18.dp)
                    .clickable { onDismiss() }
            )
        }
    }
}
