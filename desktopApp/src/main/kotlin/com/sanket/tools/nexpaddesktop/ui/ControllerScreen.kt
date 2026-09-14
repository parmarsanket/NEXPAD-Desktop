package com.sanket.tools.nexpaddesktop.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sanket.tools.nexpad.model.GamepadInput
import com.sanket.tools.nexpaddesktop.model.GyroSettings
import com.sanket.tools.nexpaddesktop.ui.components.drawCyberGrid
import com.sanket.tools.nexpaddesktop.ui.components.glassCard
import com.sanket.tools.nexpaddesktop.ui.components.glassCardHovered
import com.sanket.tools.nexpaddesktop.ui.components.selectedGlow
import com.sanket.tools.nexpaddesktop.ui.theme.NeonPalette

@Composable
fun ControllerScreen(
    latestInput: GamepadInput,
    activeController: ControllerType,
    lsSensitivityX: Float,
    lsSensitivityY: Float,
    rsSensitivityX: Float,
    rsSensitivityY: Float,
    onLsSensitivityXChange: (Float) -> Unit,
    onLsSensitivityYChange: (Float) -> Unit,
    onRsSensitivityXChange: (Float) -> Unit,
    onRsSensitivityYChange: (Float) -> Unit,
    onSaveController: (ControllerType) -> Unit,

    gyroSettings: GyroSettings,
    onGyroSettingsChange: (GyroSettings) -> Unit,
    processedYaw: Float,
    processedPitch: Float,
    onRecalibrate: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var selectedPreset by remember { mutableStateOf("FPS") }
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp)
                .verticalScroll(scrollState)
        ) {
            // ── Header ──
            Text(
                text = "NEXPAD DESKTOP",
                style = TextStyle(
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp,
                    brush = Brush.linearGradient(
                        colors = listOf(Color.White, NeonPalette.Cyan, NeonPalette.Purple)
                    )
                )
            )

            Spacer(modifier = Modifier.height(32.dp))

            // ── Controller Type ──
            Text("Controller Type", color = NeonPalette.CardIdleText, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(10.dp))

            Box {
                val dropdownInteraction = remember { MutableInteractionSource() }
                val dropdownHovered by dropdownInteraction.collectIsHoveredAsState()

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .glassCardHovered(isHovered = dropdownHovered) // (#5)
                        .hoverable(dropdownInteraction)
                        .clickable(dropdownInteraction, indication = null) { expanded = true }
                        .padding(horizontal = 18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(Icons.Default.SportsEsports, contentDescription = null, tint = NeonPalette.Cyan, modifier = Modifier.size(22.dp))
                    Text(activeController.displayName, color = Color.White, fontSize = 15.sp)
                    Spacer(modifier = Modifier.weight(1f))
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = NeonPalette.CardIdleText)
                }

                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    DropdownMenuItem(
                        text = { Text(ControllerType.XBOX_360.displayName) },
                        onClick = { onSaveController(ControllerType.XBOX_360); expanded = false }
                    )
                    DropdownMenuItem(
                        text = { Text(ControllerType.DUALSHOCK_4.displayName) },
                        onClick = { onSaveController(ControllerType.DUALSHOCK_4); expanded = false }
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // ── GYRO Status ──
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .glassCard() // (#5)
                    .padding(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        val dotColor = if (gyroSettings.enabled) NeonPalette.ConnectedDot else NeonPalette.CardIdleText
                        val infinite = rememberInfiniteTransition(label = "gyroPulse")
                        val gyroAlpha by infinite.animateFloat(
                            initialValue = 0.5f, targetValue = 1f,
                            animationSpec = infiniteRepeatable(tween(800, easing = FastOutSlowInEasing), RepeatMode.Reverse),
                            label = "gyroAlpha"
                        )
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(dotColor.copy(alpha = if (gyroSettings.enabled) gyroAlpha else 0.5f))
                        )
                        Text(
                            if (gyroSettings.enabled) "GYRO Active" else "GYRO Disabled",
                            color = if (gyroSettings.enabled) NeonPalette.ConnectedDot else NeonPalette.CardIdleText,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                    
                    // Note: Replacing Switch with standard Switch for now.
                    // Compose Desktop Switch doesn't easily accept gradient tracks without complete custom drawing.
                    // We can implement a fully custom toggle later if needed, but for now we keep layout exactly as-is.
                    Switch(
                        checked = gyroSettings.enabled,
                        onCheckedChange = { onGyroSettingsChange(gyroSettings.copy(enabled = it)) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = NeonPalette.Cyan,
                            checkedTrackColor = NeonPalette.Cyan.copy(alpha = 0.5f)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // ── Input Mode Presets ──
            Text("Input Mode Presets", color = NeonPalette.CardIdleText, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                PresetChip("FPS", Icons.Default.Speed, selectedPreset == "FPS", Modifier.weight(1f)) { selectedPreset = "FPS" }
                PresetChip("Racing", Icons.Default.DirectionsCar, selectedPreset == "Racing", Modifier.weight(1f)) { selectedPreset = "Racing" }
                PresetChip("Premium", Icons.Default.Star, selectedPreset == "Premium", Modifier.weight(1f)) { selectedPreset = "Premium" }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // ── Joystick Sensitivity ──
            if (activeController == ControllerType.XBOX_360) {
                Text("Stick Sensitivity", color = NeonPalette.CardIdleText, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(10.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .glassCard() // (#5)
                        .padding(18.dp)
                ) {
                    Column {
                        Text("Right Stick (Aiming)", color = NeonPalette.Cyan, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        SensSlider("X Axis", rsSensitivityX, onRsSensitivityXChange)
                        SensSlider("Y Axis", rsSensitivityY, onRsSensitivityYChange)

                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Left Stick (Movement)", color = NeonPalette.Cyan, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        SensSlider("X Axis", lsSensitivityX, onLsSensitivityXChange)
                        SensSlider("Y Axis", lsSensitivityY, onLsSensitivityYChange)
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .glassCard() // (#5)
                        .padding(18.dp)
                ) {
                    Text(
                        "DualShock 4 uses raw sensor pass-through. Sensitivity is controlled by the game itself.",
                        color = NeonPalette.CardIdleText,
                        fontSize = 13.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // ── 6-Axis Gyro Settings (Merged) ──
            Text("6-Axis Gyro Settings", color = NeonPalette.CardIdleText, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(10.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .glassCard() // (#5)
                    .padding(18.dp)
            ) {
                GyroSettingsSection(
                    settings = gyroSettings,
                    onSettingsChange = onGyroSettingsChange,
                    activeController = activeController,
                    rawGyroX = latestInput.gyroX,
                    rawGyroY = latestInput.gyroY,
                    rawGyroZ = latestInput.gyroZ,
                    rawAccelX = latestInput.accelX,
                    rawAccelY = latestInput.accelY,
                    rawAccelZ = latestInput.accelZ,
                    processedYaw = processedYaw,
                    processedPitch = processedPitch,
                    onRecalibrate = onRecalibrate
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// ── Preset Chip ──
@Composable
private fun PresetChip(label: String, icon: ImageVector, isActive: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    val contentColor by animateColorAsState(
        if (isActive) Color.White else if (isHovered) Color.White else NeonPalette.CardIdleText,
        tween(200), label = "chipContent"
    )

    // Base box with conditional modifiers based on state (#2 & #7 & #8 consistency)
    var baseModifier = modifier
        .height(44.dp)
        .hoverable(interactionSource)
        .clickable(interactionSource, indication = null, onClick = onClick)
    
    if (isActive) {
        baseModifier = baseModifier.selectedGlow(cornerRadius = 10.dp)
    } else {
        baseModifier = baseModifier.drawBehind {
            val cr = 10.dp.toPx()
            
            // Background
            val bgAlpha = if (isHovered) 0.03f else 0.0f
            drawRoundRect(
                color = Color.White.copy(alpha = bgAlpha),
                cornerRadius = CornerRadius(cr)
            )
            
            // Faint border
            val borderAlpha = if (isHovered) 0.1f else 0.05f
            drawRoundRect(
                color = Color.White.copy(alpha = borderAlpha),
                cornerRadius = CornerRadius(cr),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.dp.toPx())
            )
        }
    }

    Box(
        modifier = baseModifier,
        contentAlignment = Alignment.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = contentColor, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(label, color = contentColor, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }
    }
}

// ── Sensitivity Slider ──
@Composable
private fun SensSlider(label: String, value: Float, onChange: (Float) -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            "$label (${String.format("%.2f", value)})",
            modifier = Modifier.width(130.dp),
            color = Color.White.copy(alpha = 0.8f),
            fontSize = 13.sp
        )
        Slider(
            value = value,
            onValueChange = onChange,
            valueRange = 0.1f..3.0f,
            modifier = Modifier.weight(1f),
            colors = SliderDefaults.colors(
                thumbColor = NeonPalette.Cyan,
                activeTrackColor = NeonPalette.Cyan,
                inactiveTrackColor = NeonPalette.CardIdleBorder
            )
        )
    }
}
