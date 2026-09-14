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
import com.sanket.tools.nexpaddesktop.ui.theme.NeonPalette
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection

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
    activeLayerIndices: Set<Int>,
    onActiveLayersChange: (Set<Int>) -> Unit,
    soloLayerIndex: Int?,
    onSoloLayerChange: (Int?) -> Unit,
    selectedLayerIndex: Int,
    onSelectedLayerChange: (Int) -> Unit,
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
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF0A0E17))
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "No compiled layers found in this document.\nSwitch to 'HTML / CSS Source' to load a starter template.",
                color = Color.White.copy(alpha = 0.6f),
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
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF0A0F1A))
                .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(8.dp))
                .padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "LAYERS STACK",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (hasInactiveLayers) Color(0xFF332005) else Color(0xFF06352A))
                        .border(1.dp, if (hasInactiveLayers) Color(0xFFD97706) else Color(0xFF10B981), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "${activeLayerIndices.size}/$totalLayers Active",
                        color = if (hasInactiveLayers) Color(0xFFFBBF24) else Color(0xFF34D399),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                if (soloLayerIndex != null) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFF451A03))
                            .border(1.dp, Color(0xFFF59E0B), RoundedCornerShape(4.dp))
                            .clickable { onSoloLayerChange(null) }
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "SOLO: #$soloLayerIndex ✕",
                            color = Color(0xFFFBBF24),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Batch Buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Select All
                Button(
                    onClick = {
                        onActiveLayersChange((0 until totalLayers).toSet())
                        onFeedback("✓ Activated all $totalLayers layers")
                    },
                    shape = RoundedCornerShape(4.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 3.dp),
                    modifier = Modifier.height(26.dp)
                ) {
                    Text("✓ All", fontSize = 10.sp, color = Color.White)
                }

                // Invert Selection
                Button(
                    onClick = {
                        val inverted = (0 until totalLayers).toSet() - activeLayerIndices
                        onActiveLayersChange(inverted)
                        onFeedback("✓ Inverted layer selection (${inverted.size} active)")
                    },
                    shape = RoundedCornerShape(4.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 3.dp),
                    modifier = Modifier.height(26.dp)
                ) {
                    Text("🔄 Invert", fontSize = 10.sp, color = Color.White)
                }

                // Expand to Dedicated Full Screen Layer Studio
                Button(
                    onClick = onOpenFullScreen,
                    shape = RoundedCornerShape(4.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NeonPalette.Cyan
                    ),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 3.dp),
                    modifier = Modifier.height(26.dp)
                ) {
                    Text(
                        "⛶ Full Screen",
                        fontSize = 10.sp,
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
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF070A11))
                .border(1.dp, Color(0xFF172033), RoundedCornerShape(8.dp))
                .padding(6.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(4.dp)
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
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                when {
                                    isSelected -> Color(0xFF122238)
                                    isSolo -> Color(0xFF261D10)
                                    !isActive -> Color(0xFF0B0E14)
                                    else -> Color(0xFF0E1422)
                                }
                            )
                            .border(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = when {
                                    isSelected -> NeonPalette.Cyan
                                    isSolo -> Color(0xFFF59E0B)
                                    !isActive -> Color(0xFF1A1F2C)
                                    else -> Color(0xFF1D283E)
                                },
                                shape = RoundedCornerShape(6.dp)
                            )
                            .clickable { onSelectedLayerChange(idx) }
                            .padding(horizontal = 8.dp, vertical = 5.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Eye Toggle: Active / Mute
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (isActive) Color(0xFF0A2E28) else Color(0xFF261214))
                                .border(1.dp, if (isActive) Color(0xFF10B981) else Color(0xFF7F1D1D), RoundedCornerShape(4.dp))
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
                                .size(24.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (isSolo) Color(0xFFD97706) else Color(0xFF181F2F))
                                .border(1.dp, if (isSolo) Color(0xFFFBBF24) else Color(0xFF2B374E), RoundedCornerShape(4.dp))
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
                                .size(34.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFF04060B))
                                .border(1.dp, Color(0xFF1B2336), RoundedCornerShape(4.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            NxprcCanvasPreview(
                                document = document,
                                activeLayersOnly = listOf(layer),
                                sizeDp = 30
                            )
                        }

                        // Layer Index
                        Text(
                            text = "#$idx",
                            color = if (isActive) Color.White.copy(alpha = 0.6f) else Color.White.copy(alpha = 0.25f),
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )

                        // Category Badge Pill
                        val badgeCol = Color(details.badgeColor)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(badgeCol.copy(alpha = if (isActive) 0.18f else 0.08f))
                                .border(1.dp, badgeCol.copy(alpha = if (isActive) 0.8f else 0.3f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 5.dp, vertical = 2.dp)
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
                                color = if (isActive) Color.White else Color.White.copy(alpha = 0.45f),
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 11.5.sp,
                                maxLines = 1
                            )
                            Text(
                                text = details.summary,
                                color = if (isActive) Color(0xFF94A3B8) else Color(0xFF475569),
                                fontSize = 10.sp,
                                maxLines = 1
                            )
                        }

                        // Trailing Indicator (Status)
                        when {
                            isSolo -> {
                                Text("SOLO", color = Color(0xFFF59E0B), fontWeight = FontWeight.Bold, fontSize = 9.sp)
                            }
                            !isActive -> {
                                Text("MUTED", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold, fontSize = 9.sp)
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
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF070A11))
                .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(8.dp))
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Inspector Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val badgeCol = Color(selectedDetails.badgeColor)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(badgeCol.copy(alpha = 0.2f))
                            .border(1.dp, badgeCol, RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "LAYER #$safeSelectedIndex: ${selectedDetails.categoryBadge}",
                            color = badgeCol,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    }

                    Text(
                        text = selectedDetails.title,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.5.sp
                    )
                }

                // Copy Layer Code Button
                Button(
                    onClick = {
                        Toolkit.getDefaultToolkit().systemClipboard.setContents(
                            StringSelection(selectedDetails.codeSnippet),
                            null
                        )
                        onFeedback("✓ Layer #$safeSelectedIndex decomposed code copied to clipboard!")
                    },
                    shape = RoundedCornerShape(5.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 3.dp),
                    modifier = Modifier.height(26.dp)
                ) {
                    Text("📋 Copy Code", fontSize = 10.sp, color = NeonPalette.Cyan, fontWeight = FontWeight.Bold)
                }
            }

            // Monospace Decomposed Code Snippet Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFF030509))
                    .border(1.dp, Color(0xFF161F2E), RoundedCornerShape(6.dp))
                    .padding(8.dp)
            ) {
                Text(
                    text = selectedDetails.codeSnippet,
                    color = Color(0xFFCBD5E1),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    lineHeight = 15.sp,
                    modifier = Modifier.verticalScroll(rememberScrollState())
                )
            }

            // Surgical AI Prompt Copilot
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
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
                                color = Color.White.copy(alpha = 0.4f)
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp),
                        textStyle = LocalTextStyle.current.copy(fontSize = 11.sp, color = Color.White),
                        singleLine = true
                    )

                    Button(
                        onClick = {
                            val surgicalPrompt = NxprcLayerCodeGenerator.generateLayerAiPrompt(
                                layerIndex = safeSelectedIndex,
                                layer = selectedLayer,
                                doc = document,
                                userInstruction = userAiPrompt
                            )
                            Toolkit.getDefaultToolkit().systemClipboard.setContents(
                                StringSelection(surgicalPrompt),
                                null
                            )
                            onFeedback("✓ Surgical AI prompt for Layer #$safeSelectedIndex copied! Paste into Claude/GPT/Gemini.")
                        },
                        shape = RoundedCornerShape(6.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED)),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(38.dp)
                    ) {
                        Text(
                            "🤖 Copy Layer AI Prompt",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
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
