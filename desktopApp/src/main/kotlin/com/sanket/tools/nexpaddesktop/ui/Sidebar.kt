package com.sanket.tools.nexpaddesktop.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sanket.tools.nexpaddesktop.ui.theme.NeonPalette

@Composable
fun Sidebar(
    currentScreen: Screen,
    onNavigate: (Screen) -> Unit
) {
    Box(
        modifier = Modifier
            .width(100.dp)
            .fillMaxHeight()
            .background(NeonPalette.PanelBgTop)
            .drawBehind {
                // Gradient edge line on right side
                val brush = Brush.verticalGradient(
                    colors = listOf(NeonPalette.Cyan, NeonPalette.Purple, NeonPalette.Cyan)
                )
                drawLine(
                    brush = brush,
                    start = Offset(size.width, 0f),
                    end = Offset(size.width, size.height),
                    strokeWidth = 1.dp.toPx()
                )
            }
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Logo Area — gradient-filled circle (#4)
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .drawBehind {
                        // Outer glow
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    NeonPalette.Cyan.copy(alpha = 0.15f),
                                    Color.Transparent
                                )
                            ),
                            radius = 28.dp.toPx()
                        )
                        // Gradient ring
                        drawCircle(
                            brush = Brush.linearGradient(
                                colors = NeonPalette.GradientBorder
                            ),
                            style = Stroke(width = 1.5.dp.toPx())
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(NeonPalette.Cyan, NeonPalette.Purple)
                            )
                        )
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            SidebarItem(Icons.Default.Home, "Home", currentScreen == Screen.HOME) { onNavigate(Screen.HOME) }
            SidebarItem(Icons.Default.SportsEsports, "Controller", currentScreen == Screen.CONTROLLER) { onNavigate(Screen.CONTROLLER) }
            SidebarItem(Icons.Default.DeviceHub, "Node", currentScreen == Screen.NODE) { onNavigate(Screen.NODE) }
            SidebarItem(Icons.Default.SettingsEthernet, "Converter", currentScreen == Screen.CONVERTER) { onNavigate(Screen.CONVERTER) }
            SidebarItem(Icons.Default.Output, "Output", currentScreen == Screen.OUTPUT) { onNavigate(Screen.OUTPUT) }
            SidebarItem(Icons.Default.Keyboard, "KBM", currentScreen == Screen.KBM) { onNavigate(Screen.KBM) }
        }
    }
}

@Composable
private fun SidebarItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    val contentColorTarget = when {
        isSelected -> NeonPalette.Cyan
        isHovered -> Color.White
        else -> NeonPalette.CardIdleText
    }
    val contentColor by animateColorAsState(contentColorTarget, tween(200), label = "content")

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(90.dp)
            .hoverable(interactionSource)
            .clickable(interactionSource, indication = null, onClick = onClick)
            .drawBehind {
                val cr = 8.dp.toPx()
                val inset = 6.dp.toPx()

                if (isSelected) {
                    // Soft outer glow behind the pill (#3)
                    drawRoundRect(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                NeonPalette.Cyan.copy(alpha = 0.10f),
                                NeonPalette.Purple.copy(alpha = 0.08f)
                            )
                        ),
                        topLeft = Offset(inset - 4.dp.toPx(), 4.dp.toPx()),
                        size = Size(size.width - inset * 2 + 8.dp.toPx(), size.height - 8.dp.toPx()),
                        cornerRadius = CornerRadius(cr + 4.dp.toPx())
                    )

                    // Gradient-tinted glass fill (#3)
                    drawRoundRect(
                        brush = Brush.linearGradient(
                            colors = NeonPalette.SelectedFillGradient
                        ),
                        topLeft = Offset(inset, 6.dp.toPx()),
                        size = Size(size.width - inset * 2, size.height - 12.dp.toPx()),
                        cornerRadius = CornerRadius(cr)
                    )

                    // Gradient border on the pill (#3)
                    drawRoundRect(
                        brush = Brush.linearGradient(
                            colors = NeonPalette.GradientBorder,
                            start = Offset.Zero,
                            end = Offset(size.width, size.height)
                        ),
                        topLeft = Offset(inset, 6.dp.toPx()),
                        size = Size(size.width - inset * 2, size.height - 12.dp.toPx()),
                        cornerRadius = CornerRadius(cr),
                        style = Stroke(width = 1.5.dp.toPx())
                    )
                } else if (isHovered) {
                    drawRoundRect(
                        color = Color.White.copy(alpha = 0.04f),
                        topLeft = Offset(inset, 6.dp.toPx()),
                        size = Size(size.width - inset * 2, size.height - 12.dp.toPx()),
                        cornerRadius = CornerRadius(cr)
                    )
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = label, tint = contentColor, modifier = Modifier.size(30.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                label, color = contentColor, fontSize = 10.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                textAlign = TextAlign.Center
            )
        }
    }
}
