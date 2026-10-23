package com.sanket.tools.nexpaddesktop.ui.components.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lan
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sanket.tools.nexpaddesktop.ui.components.glassCard
import com.sanket.tools.nexpaddesktop.ui.theme.NeonPalette

/**
 * System Preferences card for configuring the centralized Network Streaming Port
 * on the desktop companion application.
 */
@Composable
fun PortConfigurationCard(
    currentPort: Int,
    onPortChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var portText by remember(currentPort) { mutableStateOf(currentPort.toString()) }
    val parsedPort = portText.toIntOrNull()
    val isReservedPort = parsedPort == 9995 || parsedPort == 9998
    val isValid = parsedPort != null && parsedPort in 1024..65535 && !isReservedPort
    val isChanged = isValid && parsedPort != currentPort
    val isDefault = currentPort == 9999

    Box(
        modifier = modifier
            .fillMaxWidth()
            .glassCard(cornerRadius = 14.dp)
            .padding(horizontal = 20.dp, vertical = 18.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    val accentColor = NeonPalette.Cyan

                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .drawBehind {
                                drawCircle(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            accentColor.copy(alpha = 0.25f),
                                            Color.Transparent
                                        )
                                    ),
                                    radius = 20.dp.toPx() + 10f
                                )
                            }
                            .clip(CircleShape)
                            .background(accentColor.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lan,
                            contentDescription = "Network Port",
                            tint = accentColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "Network Streaming Port",
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "UDP gamepad receiver & ADB reverse bridge listening port (Default: 9999 • Reserved: 9995, 9998)",
                            color = NeonPalette.CardIdleText,
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Input & Action Controls
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Port Text Field Container
                    Box(
                        modifier = Modifier
                            .width(96.dp)
                            .height(38.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF111726))
                            .border(
                                1.dp,
                                if ((!isValid && portText.isNotBlank()) || isReservedPort) Color(0xFFEF4444) else Color(0xFF222E4A),
                                RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 10.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        BasicTextField(
                            value = portText,
                            onValueChange = { input ->
                                if (input.length <= 5 && input.all { it.isDigit() }) {
                                    portText = input
                                }
                            },
                            singleLine = true,
                            textStyle = TextStyle(
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            ),
                            cursorBrush = SolidColor(NeonPalette.Cyan),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    if (isChanged) {
                                        onPortChange(parsedPort)
                                    }
                                }
                            )
                        )
                    }

                    // Save Button
                    Button(
                        onClick = {
                            if (isChanged) {
                                onPortChange(parsedPort)
                            }
                        },
                        enabled = isChanged,
                        modifier = Modifier.height(38.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NeonPalette.Cyan,
                            contentColor = Color.Black,
                            disabledContainerColor = Color(0xFF1E293B),
                            disabledContentColor = Color(0xFF64748B)
                        )
                    ) {
                        Text(
                            text = "APPLY",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Reset Button (if altered or non-default)
                    if (!isDefault || isChanged || portText != "9999") {
                        OutlinedButton(
                            onClick = {
                                portText = "9999"
                                onPortChange(9999)
                            },
                            modifier = Modifier.height(38.dp),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, NeonPalette.Purple.copy(alpha = 0.5f)),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = NeonPalette.Purple
                            )
                        ) {
                            Text(
                                text = "RESET",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            // Reserved Port Warning Banner
            if (isReservedPort) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFEF4444).copy(alpha = 0.12f))
                        .border(1.dp, Color(0xFFEF4444).copy(alpha = 0.45f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Reserved Port Warning",
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Ports 9995 and 9998 are already used by NEXPAD (${if (parsedPort == 9998) "Auto-Discovery" else "Layout Sync"}) — please do not use them!",
                        color = Color(0xFFFCA5A5),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Status Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(start = 54.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                isReservedPort || (!isValid && portText.isNotBlank()) -> Color(0xFFEF4444)
                                else -> NeonPalette.Green
                            }
                        )
                )
                Text(
                    text = when {
                        isReservedPort -> "Port $parsedPort is reserved by NEXPAD (Ports 9995 & 9998 are already in use)"
                        !isValid && portText.isNotBlank() -> "Invalid port! Enter a number between 1024 and 65535"
                        portText.isBlank() -> "Please enter a valid port number"
                        else -> "Server listening on 0.0.0.0:$currentPort • Port valid (1024 - 65535)"
                    },
                    color = if (isValid) NeonPalette.CardIdleText else Color(0xFFEF4444),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
