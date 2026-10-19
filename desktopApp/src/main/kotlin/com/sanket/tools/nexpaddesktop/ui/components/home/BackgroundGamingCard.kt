package com.sanket.tools.nexpaddesktop.ui.components.home

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sanket.tools.nexpaddesktop.ui.theme.NeonPalette

/**
 * Hero Card placed above the Device section that allows the user
 * to minimize NEXPAD Desktop to the Windows system tray to eliminate UI overhead
 * while keeping gamepad emulation, gyro aiming, and server pipelines 100% active.
 */
@Composable
fun BackgroundGamingCard(
    modifier: Modifier = Modifier,
    onMinimizeToTray: () -> Unit
) {
    val cardInteractionSource = remember { MutableInteractionSource() }
    val buttonInteractionSource = remember { MutableInteractionSource() }
    val isHovered by cardInteractionSource.collectIsHoveredAsState()
    val isButtonHovered by buttonInteractionSource.collectIsHoveredAsState()

    val glowAlpha by animateFloatAsState(
        targetValue = if (isHovered) 0.35f else 0.18f,
        animationSpec = tween(250),
        label = "card_glow"
    )

    val cardBgBrush = remember {
        Brush.horizontalGradient(
            colors = listOf(
                Color(0xFF0F1626),
                Color(0xFF131D33),
                Color(0xFF18182E)
            )
        )
    }

    val iconBgBrush = remember {
        Brush.linearGradient(
            colors = listOf(
                NeonPalette.Cyan.copy(alpha = 0.2f),
                NeonPalette.Purple.copy(alpha = 0.2f)
            )
        )
    }

    val iconBorderBrush = remember {
        Brush.linearGradient(
            colors = listOf(
                NeonPalette.Cyan.copy(alpha = 0.6f),
                NeonPalette.Purple.copy(alpha = 0.4f)
            )
        )
    }

    val buttonOutlineBrush = remember {
        Brush.horizontalGradient(
            colors = listOf(
                NeonPalette.Cyan,
                NeonPalette.Purple
            )
        )
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .hoverable(cardInteractionSource)
            .drawBehind {
                drawRoundRect(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            NeonPalette.Cyan.copy(alpha = glowAlpha),
                            NeonPalette.Purple.copy(alpha = glowAlpha * 0.5f),
                            Color.Transparent
                        ),
                        center = Offset(size.width * 0.85f, size.height * 0.5f),
                        radius = size.width * 0.45f
                    ),
                    cornerRadius = CornerRadius(16.dp.toPx())
                )
            }
            .clip(RoundedCornerShape(16.dp))
            .background(cardBgBrush)
            .border(
                width = 1.dp,
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        NeonPalette.Cyan.copy(alpha = if (isHovered) 0.5f else 0.25f),
                        NeonPalette.Purple.copy(alpha = if (isHovered) 0.5f else 0.25f)
                    )
                ),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(horizontal = 22.dp, vertical = 18.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left Content: Badge + Title + 2-Line Explanation
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f)
            ) {
                // Glowing Icon Pod
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(iconBgBrush)
                        .border(
                            1.dp,
                            iconBorderBrush,
                            RoundedCornerShape(14.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.SportsEsports,
                        contentDescription = "Gaming Mode",
                        tint = NeonPalette.Cyan,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Performance Badge
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(NeonPalette.Cyan.copy(alpha = 0.12f))
                            .border(1.dp, NeonPalette.Cyan.copy(alpha = 0.35f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(5.dp)
                                .clip(CircleShape)
                                .background(NeonPalette.Cyan)
                        )
                        Text(
                            text = "ZERO OVERHEAD GAMING",
                            color = NeonPalette.Cyan,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.6.sp
                        )
                    }

                    // Title
                    Text(
                        text = "Run NEXPAD in Background",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        letterSpacing = 0.3.sp
                    )

                    // 2-Line Explanation
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "Minimize NEXPAD to the Windows system tray to free up GPU & CPU resources for your game.",
                            color = NeonPalette.CardIdleText,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Normal,
                            lineHeight = 16.sp
                        )
                        Text(
                            text = "Virtual gamepad emulation, gyro motion aiming, and connection servers remain 100% active.",
                            color = NeonPalette.CardIdleText.copy(alpha = 0.85f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Normal,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(24.dp))

            // Right Action: "Minimize for Game" Outline Button with Subtitle
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                OutlinedButton(
                    onClick = onMinimizeToTray,
                    interactionSource = buttonInteractionSource,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(
                        width = 1.5.dp,
                        brush = Brush.horizontalGradient(
                            colors = if (isButtonHovered) {
                                listOf(NeonPalette.Cyan, NeonPalette.Purple)
                            } else {
                                listOf(NeonPalette.Cyan.copy(alpha = 0.65f), NeonPalette.Purple.copy(alpha = 0.5f))
                            }
                        )
                    ),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (isButtonHovered) NeonPalette.Cyan.copy(alpha = 0.12f) else Color.Transparent,
                        contentColor = NeonPalette.Cyan
                    ),
                    contentPadding = PaddingValues(horizontal = 18.dp, vertical = 11.dp),
                    modifier = Modifier.height(42.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.SportsEsports,
                        contentDescription = null,
                        tint = if (isButtonHovered) Color.White else NeonPalette.Cyan,
                        modifier = Modifier.size(17.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Minimize for Game",
                        color = if (isButtonHovered) Color.White else NeonPalette.Cyan,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.4.sp
                    )
                }

                Text(
                    text = "Restores from system tray anytime",
                    color = Color.White.copy(alpha = 0.45f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
