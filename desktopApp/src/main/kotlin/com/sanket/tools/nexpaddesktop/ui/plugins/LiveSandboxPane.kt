package com.sanket.tools.nexpaddesktop.ui.plugins

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.unit.coerceAtMost
import androidx.compose.ui.unit.coerceIn
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sanket.tools.nexpad.nxprc.NxprcDocument
import com.sanket.tools.nexpaddesktop.connection.ActiveTransport
import com.sanket.tools.nexpaddesktop.model.ControllerTouchClassifier
import com.sanket.tools.nexpaddesktop.plugins.NxprcSeedEngine
import com.sanket.tools.nexpaddesktop.ui.designer.NxprcCanvasPreview
import com.sanket.tools.nexpaddesktop.ui.components.glassCard
import com.sanket.tools.nexpaddesktop.ui.theme.NeonPalette

/**
 * Right Live Sandbox Pane with auto-fit vector preview, live animation stats, and device push actions.
 */
@Composable
fun LiveSandboxPane(
    modifier: Modifier = Modifier,
    isNarrow: Boolean,
    isShort: Boolean,
    compiledDoc: NxprcDocument,
    activeLayerIndices: Set<Int>,
    soloLayerIndex: Int?,
    targetWidthDp: Int,
    targetHeightDp: Int,
    componentId: String,
    componentName: String,
    defaultControl: String,
    category: String,
    currentSeed: Long,
    htmlCodeSeed: Long?,
    onSeedChange: (Long) -> Unit,
    onRerollSeed: () -> Unit,
    onResetSeed: () -> Unit,
    compileError: String?,
    exportStatus: String?,
    isExporting: Boolean,
    onOpenLayerStudio: () -> Unit,
    onOpenFullAudit: () -> Unit,
    onExport: () -> Unit,
    activeTransport: ActiveTransport = ActiveTransport.NONE,
    onPush: () -> Unit
) {
    var stickDeflection by remember { mutableStateOf(Pair(0f, 0f)) }

    // Effective metadata from compiled document (authoritative source of truth)
    val effCategory = compiledDoc.manifest.category.ifBlank { category }
    val effControl = compiledDoc.manifest.defaultControl.ifBlank { defaultControl }
    val effId = compiledDoc.manifest.id.ifBlank { componentId }
    val effName = compiledDoc.manifest.name.ifBlank { componentName }
    val effWidthDp = if (compiledDoc.manifest.widthDp > 0) compiledDoc.manifest.widthDp else targetWidthDp
    val effHeightDp = if (compiledDoc.manifest.heightDp > 0) compiledDoc.manifest.heightDp else targetHeightDp

    Column(
        modifier = modifier
            .glassCard()
            .padding(if (isShort) 8.dp else 12.dp),
        verticalArrangement = Arrangement.spacedBy(if (isShort) 6.dp else 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Header: Title & Studio/Audit Navigation (Responsive)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f, fill = false)) {
                Text("Live Sandbox & Export", color = Color.White, fontWeight = FontWeight.Bold, fontSize = if (isNarrow) 13.5.sp else 15.sp, maxLines = 1, softWrap = false)
                if (!isShort) {
                    Text("Tap to test tactile physics", color = NeonPalette.Cyan.copy(alpha = 0.7f), fontSize = 10.sp, maxLines = 1, softWrap = false)
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                Button(
                    onClick = onOpenLayerStudio,
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = if (isNarrow) 8.dp else 12.dp, vertical = 4.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                    modifier = Modifier.height(30.dp)
                ) {
                    Text(if (isNarrow) "🎛️ Layers" else "🎛️ Layer Studio", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp, maxLines = 1, softWrap = false)
                }

                OutlinedButton(
                    onClick = onOpenFullAudit,
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = if (isNarrow) 8.dp else 12.dp, vertical = 4.dp),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = NeonPalette.Cyan.copy(alpha = 0.08f)),
                    border = BorderStroke(1.dp, NeonPalette.Cyan),
                    modifier = Modifier.height(30.dp)
                ) {
                    Text(if (isNarrow) "🔍 Audit" else "🔍 Full Audit", color = NeonPalette.Cyan, fontWeight = FontWeight.Bold, fontSize = 11.sp, maxLines = 1, softWrap = false)
                }
            }
        }

        // Dynamic Adaptive Aspect-Ratio Preview Box
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            val maxBoxW = maxWidth * 0.95f
            val maxBoxH = maxHeight * 0.96f

            val aspect = effWidthDp.toFloat() / effHeightDp.toFloat()
            val idealDimension = (maxBoxH * 0.88f).coerceIn(160.dp, 460.dp)
            val (boxW, boxH) = if (aspect >= 1.0f) {
                val w = minOf(maxBoxW, idealDimension * aspect, 460.dp)
                val h = (w / aspect).coerceAtMost(maxBoxH)
                Pair(w, h)
            } else {
                val h = minOf(maxBoxH, idealDimension, 460.dp)
                val w = (h * aspect).coerceAtMost(maxBoxW)
                Pair(w, h)
            }

            Box(
                modifier = Modifier
                    .size(width = boxW, height = boxH)
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.radialGradient(
                            listOf(MaterialTheme.colorScheme.surfaceContainer, MaterialTheme.colorScheme.surfaceContainerLowest)
                        )
                    )
                    .border(1.5.dp, NeonPalette.Cyan.copy(alpha = 0.45f), RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) {
                val previewDp = (minOf(boxW.value, boxH.value) * 0.95f).toInt().coerceAtLeast(60)
                val previewLayers = remember(compiledDoc, activeLayerIndices, soloLayerIndex) {
                    val soloIdx = soloLayerIndex
                    when {
                        soloIdx != null -> {
                            val solo = compiledDoc.canvas.layers.getOrNull(soloIdx)
                            if (solo != null) listOf(solo) else null
                        }
                        activeLayerIndices.size < compiledDoc.canvas.layers.size -> {
                            compiledDoc.canvas.layers.filterIndexed { idx, _ -> idx in activeLayerIndices }
                        }
                        else -> null
                    }
                }

                NxprcCanvasPreview(
                    document = compiledDoc,
                    activeLayersOnly = previewLayers,
                    sizeDp = previewDp,
                    onStickDeflection = { x, y -> stickDeflection = Pair(x, y) }
                )
            }
        }

        // Live Animation & Physics Stats Strip
        val isStick = ControllerTouchClassifier.isJoystick(effControl, compiledDoc.manifest.id) || ControllerTouchClassifier.isTouchpad(effControl, compiledDoc.manifest.id) || effCategory.equals("JOYSTICK", ignoreCase = true) || effCategory.equals("TOUCHPAD", ignoreCase = true)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (soloLayerIndex != null) {
                Text("SOLO: Layer #$soloLayerIndex", color = Color(0xFFF59E0B), fontWeight = FontWeight.Bold, fontSize = 10.5.sp)
            } else {
                Text("Layers: ${activeLayerIndices.size}/${compiledDoc.canvas.layers.size} active", color = NeonPalette.Cyan, fontWeight = FontWeight.Bold, fontSize = 10.5.sp)
            }
            if (isStick) {
                val dx = stickDeflection.first
                val dy = stickDeflection.second
                val xStr = if (dx >= 0f) "+${"%.2f".format(dx)}" else "%.2f".format(dx)
                val yStr = if (dy >= 0f) "+${"%.2f".format(dy)}" else "%.2f".format(dy)
                val isRightStick = ControllerTouchClassifier.isRightStickOrTouchpad(effControl, compiledDoc.manifest.id)
                val stickName = if (isRightStick) "RS Stick" else "LS Stick"
                Text(
                    "$stickName: X:$xStr Y:$yStr",
                    color = if (dx != 0f || dy != 0f) Color(0xFF10B981) else Color.White.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.5.sp
                )
            } else {
                Text("Idle: ${compiledDoc.animations.idleType}", color = Color.White.copy(alpha = 0.8f), fontSize = 10.5.sp)
            }
            Text("Touch: ${if (isStick) "360° Analog" else compiledDoc.animations.pressFeedback}", color = Color.White.copy(alpha = 0.6f), fontSize = 10.sp)
        }

        // Procedural Seed & Theme Control Bar in Live Sandbox
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(
                1.dp,
                if (htmlCodeSeed != null && currentSeed != htmlCodeSeed) Color(0xFFF59E0B).copy(alpha = 0.6f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = if (isShort) 8.dp else 10.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Seed Input Area
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "🎲 Seed:",
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )

                        var seedInputText by remember(currentSeed) { mutableStateOf(currentSeed.toString()) }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.surfaceContainerLowest)
                                .border(1.dp, NeonPalette.Cyan.copy(alpha = 0.7f), RoundedCornerShape(10.dp))
                                .padding(horizontal = 9.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "#",
                                color = NeonPalette.Cyan,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(Modifier.width(3.dp))
                            BasicTextField(
                                value = seedInputText,
                                onValueChange = { newVal ->
                                    val digits = newVal.filter { it.isDigit() }.take(10)
                                    seedInputText = digits
                                    val parsed = digits.toLongOrNull()
                                    if (parsed != null && parsed != currentSeed) {
                                        onSeedChange(parsed)
                                    }
                                },
                                textStyle = TextStyle(
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                ),
                                singleLine = true,
                                cursorBrush = SolidColor(NeonPalette.Cyan),
                                modifier = Modifier
                                    .width(IntrinsicSize.Min)
                                    .widthIn(min = 52.dp, max = 88.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = "✏️",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }

                    // Action Controls: Reroll, Reset, Apply
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = onRerollSeed,
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, Color(0xFFF59E0B)),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = Color(0xFFF59E0B).copy(alpha = 0.15f)
                            ),
                            contentPadding = PaddingValues(horizontal = if (isNarrow) 8.dp else 12.dp, vertical = 2.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Text(
                                text = "🎲 Reroll",
                                color = Color(0xFFFCD34D),
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                maxLines = 1,
                                softWrap = false
                            )
                        }

                        val isModifiedFromCode = htmlCodeSeed != null && currentSeed != htmlCodeSeed
                        OutlinedButton(
                            onClick = onResetSeed,
                            enabled = isModifiedFromCode,
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(
                                1.dp,
                                if (isModifiedFromCode) MaterialTheme.colorScheme.outlineVariant else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                            ),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (isModifiedFromCode) MaterialTheme.colorScheme.surfaceContainerHigh else Color.Transparent,
                                disabledContainerColor = Color.Transparent
                            ),
                            contentPadding = PaddingValues(horizontal = if (isNarrow) 8.dp else 12.dp, vertical = 2.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Text(
                                text = "↺ Reset",
                                color = if (isModifiedFromCode) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }

                // Row 2: Status & Resolved Palette
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val isModifiedFromCode = htmlCodeSeed != null && currentSeed != htmlCodeSeed
                    val profile = remember(currentSeed, effControl, effCategory) {
                        NxprcSeedEngine.resolve(effControl, effCategory, currentSeed)
                    }
                    Text(
                        text = if (isModifiedFromCode) {
                            "⚡ Active Sandbox Seed: #${currentSeed} (Original Code: #${htmlCodeSeed})"
                        } else {
                            "✓ Matches HTML Code Seed (#${currentSeed})"
                        },
                        color = if (isModifiedFromCode) Color(0xFFF59E0B) else Color(0xFF10B981),
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Text(
                        text = "Theme: ${profile.palette.name} (${profile.skinName})",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 10.5.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        // Compact Glass Metadata Strip
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerLowest)
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                .padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f, fill = false)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(NeonPalette.Cyan.copy(alpha = 0.12f))
                        .border(1.dp, NeonPalette.Cyan.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(text = effId, color = NeonPalette.Cyan, fontSize = 10.5.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                }
                Text(
                    text = effName,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Key: $effControl", color = Color(0xFF4ADE80), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Text(text = "•", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f), fontSize = 10.sp)
                Text(text = effCategory, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.5.sp)
            }
        }

        // Compile Warning (if any)
        if (compileError != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
            ) {
                Text(
                    text = "Warning: $compileError (showing last valid preview)",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    fontSize = 11.sp
                )
            }
        }

        // Export Status
        if (exportStatus != null) {
            val isError = exportStatus.contains("Error") || exportStatus.contains("canceled")
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (isError) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f) else Color(0xFF0F2C24)
                ),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, if (isError) MaterialTheme.colorScheme.error.copy(alpha = 0.5f) else Color(0xFF10B981).copy(alpha = 0.5f))
            ) {
                Text(
                    text = exportStatus,
                    color = if (isError) MaterialTheme.colorScheme.error else Color(0xFF34D399),
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    fontSize = 11.sp
                )
            }
        }

        // Action Buttons Row: Export & Push via Active Connection
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val exportText = when {
                activeLayerIndices.size < compiledDoc.canvas.layers.size -> "Export (${activeLayerIndices.size} L)"
                isNarrow -> "Export"
                else -> "Export File"
            }
            val isConnected = activeTransport != ActiveTransport.NONE
            val pushText = when {
                !isConnected -> "No Phone Connected"
                activeLayerIndices.size < compiledDoc.canvas.layers.size -> when (activeTransport) {
                    ActiveTransport.WIFI -> "Push Wi-Fi (${activeLayerIndices.size} L)"
                    ActiveTransport.USB_TETHERING -> "Push Tether (${activeLayerIndices.size} L)"
                    ActiveTransport.USB_AOA -> "Push Direct (${activeLayerIndices.size} L)"
                    ActiveTransport.USB_ADB -> "Push ADB (${activeLayerIndices.size} L)"
                    ActiveTransport.BLUETOOTH -> "Push BT (${activeLayerIndices.size} L)"
                    ActiveTransport.NONE -> "No Phone"
                }
                isNarrow -> when (activeTransport) {
                    ActiveTransport.WIFI -> "Push Wi-Fi"
                    ActiveTransport.USB_TETHERING -> "Push Tether"
                    ActiveTransport.USB_AOA -> "Push Direct"
                    ActiveTransport.USB_ADB -> "Push ADB"
                    ActiveTransport.BLUETOOTH -> "Push BT"
                    ActiveTransport.NONE -> "No Phone"
                }
                else -> when (activeTransport) {
                    ActiveTransport.WIFI -> "Push via Wi-Fi"
                    ActiveTransport.USB_TETHERING -> "Push via Tethering"
                    ActiveTransport.USB_AOA -> "Push via USB Direct"
                    ActiveTransport.USB_ADB -> "Push via ADB"
                    ActiveTransport.BLUETOOTH -> "Push via Bluetooth"
                    ActiveTransport.NONE -> "No Phone Connected"
                }
            }

            // Export Button
            Button(
                onClick = onExport,
                enabled = !isExporting,
                modifier = Modifier
                    .weight(1f)
                    .height(if (isShort) 38.dp else 42.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = NeonPalette.Cyan,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                ),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
            ) {
                Icon(Icons.Default.Download, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text(
                    text = exportText,
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Push via Active Transport Button
            Button(
                onClick = onPush,
                enabled = !isExporting && isConnected,
                modifier = Modifier
                    .weight(1f)
                    .height(if (isShort) 38.dp else 42.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF10B981),
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                ),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.Send,
                    contentDescription = null,
                    tint = if (isConnected) Color.Black else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = pushText,
                    color = if (isConnected) Color.Black else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
