package com.sanket.tools.nexpaddesktop.ui.designer

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sanket.tools.nexpad.nxprc.CanvasLayer
import com.sanket.tools.nexpad.nxprc.NxprcDocument
import com.sanket.tools.nexpaddesktop.plugins.LayerDetails
import com.sanket.tools.nexpaddesktop.plugins.NxprcLayerCodeGenerator
import com.sanket.tools.nexpaddesktop.plugins.NxprcSurgicalReplacer
import com.sanket.tools.nexpaddesktop.ui.theme.NeonPalette
import java.awt.Toolkit
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.StringSelection

private fun safeCopyToClipboard(text: String): Boolean {
    val selection = StringSelection(text)
    for (attempt in 1..3) {
        try {
            Toolkit.getDefaultToolkit().systemClipboard.setContents(selection, null)
            return true
        } catch (_: Throwable) {
            try { Thread.sleep(30) } catch (_: InterruptedException) {}
        }
    }
    return false
}

/**
 * Interactive Layer Studio & Manager panel.
 *
 * Provides granular control over each discrete button layer:
 * - Active / Inactive mute toggles ([👁️])
 * - Solo isolation mode ([🎯])
 * - Decomposed CSS/SVG code inspection with 1-click clipboard copy
 * - Surgical single-layer AI prompt generator with strict boundaries
 * - Batch operations (Select All, Invert, Purge Deactivated)
 */
@Composable
fun LayerStudioPanel(
    document: NxprcDocument,
    htmlSource: String = "",
    activeLayerIndices: Set<Int>,
    onActiveLayersChange: (Set<Int>) -> Unit,
    soloLayerIndex: Int?,
    onSoloLayerChange: (Int?) -> Unit,
    selectedLayerIndex: Int,
    onSelectedLayerChange: (Int) -> Unit,
    onHtmlChange: ((String) -> Unit)? = null,
    onSaveHtml: ((String) -> Unit)? = null,
    onOpenFullScreen: () -> Unit = {},
    onFeedback: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val layers = document.canvas.layers
    val totalLayers = layers.size

    if (totalLayers == 0) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(14.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerLow)
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "No compiled layers found in this document.\nSwitch to 'HTML / CSS Source' to load a starter template.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
        return
    }

    val safeSelectedIndex = selectedLayerIndex.coerceIn(0, totalLayers - 1)
    val selectedLayer = layers[safeSelectedIndex]
    val selectedDetails = remember(safeSelectedIndex, selectedLayer, document) {
        NxprcLayerCodeGenerator.getLayerDetails(safeSelectedIndex, selectedLayer, document)
    }

    var userAiPrompt by remember(safeSelectedIndex) { mutableStateOf("") }
    var replacementCode by remember(safeSelectedIndex) { mutableStateOf("") }
    val hasInactiveLayers = activeLayerIndices.size < totalLayers

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // =====================================================================
        // 1. TOP TOOLBAR: Batch Operations & Status
        // =====================================================================
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerLow)
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                .padding(horizontal = 12.dp, vertical = 7.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "LAYERS STACK",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.5.sp
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (hasInactiveLayers) Color(0xFFF59E0B).copy(alpha = 0.15f) else Color(0xFF10B981).copy(alpha = 0.15f))
                        .border(1.dp, if (hasInactiveLayers) Color(0xFFF59E0B).copy(alpha = 0.5f) else Color(0xFF10B981).copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 7.dp, vertical = 2.5.dp)
                ) {
                    Text(
                        text = "${activeLayerIndices.size}/$totalLayers Active",
                        color = if (hasInactiveLayers) Color(0xFFFBBF24) else Color(0xFF34D399),
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                if (soloLayerIndex != null) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFF59E0B).copy(alpha = 0.15f))
                            .border(1.dp, Color(0xFFF59E0B).copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .clickable { onSoloLayerChange(null) }
                            .padding(horizontal = 7.dp, vertical = 2.5.dp)
                    ) {
                        Text(
                            text = "SOLO: #$soloLayerIndex ✕",
                            color = Color(0xFFFBBF24),
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Batch Buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Select All
                Button(
                    onClick = {
                        onActiveLayersChange((0 until totalLayers).toSet())
                        onFeedback("✓ Activated all $totalLayers layers")
                    },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                    contentPadding = PaddingValues(horizontal = 9.dp, vertical = 2.dp),
                    modifier = Modifier.height(28.dp)
                ) {
                    Text("✓ All", fontSize = 10.5.sp, color = MaterialTheme.colorScheme.onSurface)
                }

                // Invert Selection
                Button(
                    onClick = {
                        val inverted = (0 until totalLayers).toSet() - activeLayerIndices
                        onActiveLayersChange(inverted)
                        onFeedback("✓ Inverted layer selection (${inverted.size} active)")
                    },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                    contentPadding = PaddingValues(horizontal = 9.dp, vertical = 2.dp),
                    modifier = Modifier.height(28.dp)
                ) {
                    Text("🔄 Invert", fontSize = 10.5.sp, color = MaterialTheme.colorScheme.onSurface)
                }

                // Expand to Dedicated Full Screen Layer Studio
                Button(
                    onClick = onOpenFullScreen,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NeonPalette.Cyan
                    ),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                    modifier = Modifier.height(28.dp)
                ) {
                    Text(
                        "⛶ Full Screen",
                        fontSize = 10.5.sp,
                        color = Color.Black,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // =====================================================================
        // 2. LAYER STACK LIST (Top-Down Scrollable Hierarchy)
        // =====================================================================
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.05f)
                .clip(RoundedCornerShape(14.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerLow)
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                layers.forEachIndexed { idx, layer ->
                    val isActive = idx in activeLayerIndices
                    val isSolo = soloLayerIndex == idx
                    val isSelected = safeSelectedIndex == idx
                    val details = remember(idx, layer, document) {
                        NxprcLayerCodeGenerator.getLayerDetails(idx, layer, document)
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                when {
                                    isSelected -> MaterialTheme.colorScheme.surfaceContainerHighest
                                    isSolo -> Color(0xFFF59E0B).copy(alpha = 0.18f)
                                    !isActive -> MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = 0.4f)
                                    else -> MaterialTheme.colorScheme.surfaceContainer
                                }
                            )
                            .border(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = when {
                                    isSelected -> NeonPalette.Cyan
                                    isSolo -> Color(0xFFF59E0B)
                                    !isActive -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
                                    else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                                },
                                shape = RoundedCornerShape(10.dp)
                            )
                            .clickable { onSelectedLayerChange(idx) }
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Eye Toggle: Active / Mute
                        Box(
                            modifier = Modifier
                                .size(26.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isActive) Color(0xFF10B981).copy(alpha = 0.16f) else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f))
                                .border(1.dp, if (isActive) Color(0xFF10B981).copy(alpha = 0.5f) else MaterialTheme.colorScheme.error.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                .clickable {
                                    val newSet = if (isActive) activeLayerIndices - idx else activeLayerIndices + idx
                                    onActiveLayersChange(newSet)
                                    onFeedback(if (isActive) "Layer #$idx muted (excluded from render)" else "Layer #$idx enabled")
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (isActive) "👁️" else "🚫",
                                fontSize = 11.sp
                            )
                        }

                        // Solo Toggle: Target isolation
                        Box(
                            modifier = Modifier
                                .size(26.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSolo) Color(0xFFF59E0B).copy(alpha = 0.3f) else MaterialTheme.colorScheme.surfaceContainerHigh)
                                .border(1.dp, if (isSolo) Color(0xFFF59E0B) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                .clickable {
                                    val newSolo = if (isSolo) null else idx
                                    onSoloLayerChange(newSolo)
                                    onFeedback(if (newSolo != null) "Soloing Layer #$idx in sandbox" else "Exited solo mode")
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "🎯",
                                fontSize = 11.sp
                            )
                        }

                        // Visual Isolated Skia Thumbnail
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceContainerLowest)
                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f), RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            NxprcCanvasPreview(
                                document = document,
                                activeLayersOnly = listOf(layer),
                                sizeDp = 32
                            )
                        }

                        // Layer Index
                        Text(
                            text = "#$idx",
                            color = if (isActive) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )

                        // Category Badge Pill
                        val badgeCol = Color(details.badgeColor)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(badgeCol.copy(alpha = if (isActive) 0.18f else 0.08f))
                                .border(1.dp, badgeCol.copy(alpha = if (isActive) 0.8f else 0.3f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = details.categoryBadge,
                                color = if (isActive) badgeCol else badgeCol.copy(alpha = 0.4f),
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp
                            )
                        }

                        // Layer Title & Summary
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = details.title,
                                color = if (isActive) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f),
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 11.5.sp,
                                maxLines = 1
                            )
                            Text(
                                text = details.summary,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 10.sp,
                                maxLines = 1
                            )
                        }

                        // Trailing Indicator (Status)
                        when {
                            isSolo -> {
                                Text("SOLO", color = Color(0xFFF59E0B), fontWeight = FontWeight.Bold, fontSize = 9.5.sp)
                            }
                            !isActive -> {
                                Text("MUTED", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold, fontSize = 9.5.sp)
                            }
                        }
                    }
                }
            }
        }

        // =====================================================================
        // 3. SELECTED LAYER INSPECTOR & SURGICAL AI COPILOT
        // =====================================================================
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.95f)
                .clip(RoundedCornerShape(14.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerLow)
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Inspector Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val badgeCol = Color(selectedDetails.badgeColor)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(badgeCol.copy(alpha = 0.18f))
                            .border(1.dp, badgeCol, RoundedCornerShape(8.dp))
                            .padding(horizontal = 7.dp, vertical = 2.5.dp)
                    ) {
                        Text(
                            text = "LAYER #$safeSelectedIndex: ${selectedDetails.categoryBadge}",
                            color = badgeCol,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.5.sp
                        )
                    }

                    Text(
                        text = selectedDetails.title,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp
                    )
                }

                // Copy Layer Code Button
                Button(
                    onClick = {
                        val copied = safeCopyToClipboard(selectedDetails.codeSnippet)
                        if (copied) {
                            onFeedback("✓ Layer #$safeSelectedIndex decomposed code copied to clipboard!")
                        } else {
                            onFeedback("⚠️ Failed to access clipboard. Please retry.")
                        }
                    },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                    modifier = Modifier.height(28.dp)
                ) {
                    Text("📋 Copy Code", fontSize = 10.5.sp, color = NeonPalette.Cyan, fontWeight = FontWeight.Bold)
                }
            }

            // Monospace Decomposed Code Snippet Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerLowest)
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
                    .padding(10.dp)
            ) {
                Text(
                    text = selectedDetails.codeSnippet,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    lineHeight = 15.sp,
                    modifier = Modifier.verticalScroll(rememberScrollState())
                )
            }

            // Surgical AI Prompt Copilot
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = userAiPrompt,
                        onValueChange = { userAiPrompt = it },
                        placeholder = {
                            Text(
                                "Prompt AI to modify or replace Layer #$safeSelectedIndex...",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp),
                        textStyle = LocalTextStyle.current.copy(fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface),
                        singleLine = true
                    )

                    Button(
                        onClick = {
                            val surgicalPrompt = NxprcLayerCodeGenerator.generateLayerAiPrompt(
                                layerIndex = safeSelectedIndex,
                                layer = selectedLayer,
                                doc = document,
                                userInstruction = userAiPrompt,
                                htmlSource = htmlSource
                            )
                            val copied = safeCopyToClipboard(surgicalPrompt)
                            if (copied) {
                                onFeedback("✓ Surgical AI prompt for Layer #$safeSelectedIndex copied!")
                            } else {
                                onFeedback("⚠️ Failed to access clipboard. Please retry.")
                            }
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5CF6)),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(38.dp)
                    ) {
                        Text(
                            "🤖 Copy Prompt",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Button(
                        onClick = {
                            replacementCode = selectedDetails.codeSnippet
                            onFeedback("Loaded Layer #$safeSelectedIndex code for editing.")
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(38.dp)
                    ) {
                        Text(
                            "✏️ Edit",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonPalette.Cyan
                        )
                    }

                    Button(
                        onClick = {
                            try {
                                val clipboard = Toolkit.getDefaultToolkit().systemClipboard
                                val contents = clipboard.getContents(null)
                                if (contents != null && contents.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                                    val text = contents.getTransferData(DataFlavor.stringFlavor) as? String
                                    if (!text.isNullOrBlank()) {
                                        replacementCode = text
                                        onFeedback("Pasted from clipboard.")
                                    }
                                }
                            } catch (e: Exception) {
                                onFeedback("Could not paste: ${e.message}")
                            }
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(38.dp)
                    ) {
                        Text(
                            "📋 Paste",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFBBF24)
                        )
                    }
                }

                if (replacementCode.isNotBlank()) {
                    OutlinedTextField(
                        value = replacementCode,
                        onValueChange = { replacementCode = it },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(72.dp),
                        textStyle = LocalTextStyle.current.copy(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )

                    Button(
                        onClick = {
                            val result = NxprcSurgicalReplacer.applySurgicalChange(
                                originalHtml = htmlSource,
                                layerIndex = safeSelectedIndex,
                                layer = selectedLayer,
                                doc = document,
                                replacementInput = replacementCode
                            )
                            if (result.success) {
                                onHtmlChange?.invoke(result.updatedHtml)
                                onFeedback("✓ ${result.message}")
                            } else {
                                onFeedback("⚠️ ${result.message}")
                            }
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                        modifier = Modifier.fillMaxWidth().height(36.dp),
                        contentPadding = PaddingValues(vertical = 4.dp)
                    ) {
                        Text(
                            "⚡ Apply Code & Update Live Preview",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }
                }

                Text(
                    text = "Strict single-layer prompt: the AI modifies ONLY Layer #$safeSelectedIndex without breaking the rest of the button.",
                    color = Color(0xFFA78BFA),
                    fontSize = 9.5.sp
                )
            }
        }
    }
}
