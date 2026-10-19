package com.sanket.tools.nexpaddesktop.ui.components.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
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

/**
 * Driver warning banner displayed when ViGEmBus virtual controller driver is missing.
 */
@Composable
fun DriverWarningBanner(
    modifier: Modifier = Modifier
) {
    val errorColor = remember { Color(0xFFFF4D4D) }
    val glowBrush = remember {
        Brush.radialGradient(
            colors = listOf(errorColor.copy(alpha = 0.15f), Color.Transparent)
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
            .background(errorColor.copy(alpha = 0.08f))
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = errorColor,
                modifier = Modifier.size(24.dp)
            )
            Column {
                Text(
                    text = "ViGEmBus Driver Missing",
                    color = errorColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Text(
                    text = "Virtual controllers require the ViGEmBus driver. Please install it from the redist folder.",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 12.sp
                )
            }
        }
    }
}
