package com.sanket.tools.nexpaddesktop.ui.designer

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
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
 * Dedicated, full-screen creative workspace for the NEXPAD Button Controller Layer Studio.
 *
 * Professional 3-zone visual layout:
 * - Zone 1 (Left): Layer Stack Organizer with live Skia thumbnails, category filters, and active/solo toggles
 * - Zone 2 (Center): Visual Workspace with zoomable composite sandbox and dedicated isolated layer comparison stage
 * - Zone 3 (Right): Contextual Layer Inspector, decomposed CSS/SVG code viewer, and surgical single-layer AI copilot
 */
@Composable
fun LayerStudioFullScreen(
    document: NxprcDocument,
    htmlSource: String,
    activeLayerIndices: Set<Int>,
    onActiveLayersChange: (Set<Int>) -> Unit,
    soloLayerIndex: Int?,
    onSoloLayerChange: (Int?) -> Unit,
    selectedLayerIndex: Int,
    onSelectedLayerChange: (Int) -> Unit,
    onExportDoc: (NxprcDocument) -> Unit,
    onPushAdbDoc: (NxprcDocument) -> Unit,
    onFeedback: (String) -> Unit,
    onClose: () -> Unit
) {
    val layers = document.canvas.layers
    val totalLayers = layers.size

    val safeSelectedIndex = selectedLayerIndex.coerceIn(0, maxOf(0, totalLayers - 1))
    val selectedLayer = layers.getOrNull(safeSelectedIndex)
    val selectedDetails = remember(safeSelectedIndex, selectedLayer, document) {
        if (selectedLayer != null) {
            NxprcLayerCodeGenerator.getLayerDetails(safeSelectedIndex, selectedLayer, document)
        } else null
    }

    // Filter & Search State
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf("ALL") }
    var reverseStackOrder by remember { mutableStateOf(false) }

    // Canvas Workspace State
    var zoomScale by remember { mutableStateOf(1.0f) }
    var isAutoFit by remember { mutableStateOf(true) }
    var showGrid by remember { mutableStateOf(true) }

    // Surgical AI Copilot State
    var userAiPrompt by remember(safeSelectedIndex) { mutableStateOf("") }

    // Pre-computed filtered export doc (non-destructive exclusion)
    val exportDoc = remember(document, activeLayerIndices) {
        if (activeLayerIndices.size == document.canvas.layers.size) {
            document
        } else {
            document.copy(
                canvas = document.canvas.copy(
                    layers = document.canvas.layers.filterIndexed { idx, _ -> idx in activeLayerIndices }
                )
            )
        }
    }

    // Composite preview layers: solo overrides active set
    val previewLayers = remember(document, activeLayerIndices, soloLayerIndex) {
        val soloIdx = soloLayerIndex
        when {
            soloIdx != null -> {
                val solo = document.canvas.layers.getOrNull(soloIdx)
                if (solo != null) listOf(solo) else null
            }
            activeLayerIndices.size < document.canvas.layers.size -> {
                document.canvas.layers.filterIndexed { idx, _ -> idx in activeLayerIndices }
            }
            else -> null
        }
    }

    // Full-Screen Backdrop
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF060910))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // =========================================================================
            // 1. TOP APP BAR: Context, Stats & Global Actions
            // =========================================================================
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .background(NeonPalette.Cyan)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0A0F1D))
                    .border(BorderStroke(1.dp, Color(0xFF1B2438)))
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left: Button Identity & Subtitle
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF142036))
                            .border(1.dp, NeonPalette.Cyan.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = document.manifest.defaultControl,
                            color = NeonPalette.Cyan,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }

                    Column {
                        Text(
                            text = "${document.manifest.name.uppercase()} — LAYER STUDIO",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Category: ${document.manifest.category} • Canvas: ${document.manifest.widthDp}x${document.manifest.heightDp}dp • Skia GPU Vector Engine",
                            color = Color.White.copy(alpha = 0.55f),
                            fontSize = 10.5.sp
                        )
                    }
                }

                // Center: Quick Health Stats Pills
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Active Count Pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF06352A))
                            .border(1.dp, Color(0xFF10B981), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "● ${activeLayerIndices.size}/$totalLayers Active",
                            color = Color(0xFF34D399),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // Muted Count Pill
                    val mutedCount = totalLayers - activeLayerIndices.size
                    if (mutedCount > 0) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF3B151A))
                                .border(1.dp, Color(0xFFEF4444), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "🚫 $mutedCount Muted",
                                color = Color(0xFFFCA5A5),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    // Solo Status Pill
                    if (soloLayerIndex != null) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF451A03))
                                .border(1.dp, Color(0xFFF59E0B), RoundedCornerShape(6.dp))
                                .clickable { onSoloLayerChange(null) }
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "🎯 Solo: Layer #$soloLayerIndex ✕",
                                color = Color(0xFFFBBF24),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Right: Batch Actions & Close
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Activate All
                    OutlinedButton(
                        onClick = {
                            onActiveLayersChange((0 until totalLayers).toSet())
                            onFeedback("✓ Activated all $totalLayers layers")
                        },
                        shape = RoundedCornerShape(6.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFF2E3D5C)),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(30.dp)
                    ) {
                        Text("✓ Activate All", fontSize = 11.sp)
                    }

                    // Invert Selection
                    OutlinedButton(
                        onClick = {
                            val inverted = (0 until totalLayers).toSet() - activeLayerIndices
                            onActiveLayersChange(inverted)
                            onFeedback("✓ Inverted layer selection (${inverted.size} active)")
                        },
                        shape = RoundedCornerShape(6.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFF2E3D5C)),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(30.dp)
                    ) {
                        Text("🔄 Invert", fontSize = 11.sp)
                    }

                    // Close & Return
                    Button(
                        onClick = onClose,
                        shape = RoundedCornerShape(6.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.height(30.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = null, tint = Color.White, modifier = Modifier.size(13.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Return to Studio", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // =========================================================================
            // 2. MAIN 3-ZONE WORKSPACE
            // =========================================================================
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                // =====================================================================
                // ZONE 1 (LEFT): LAYER STACK ORGANIZER (Dedicated Sidebar)
                // =====================================================================
                Column(
                    modifier = Modifier
                        .width(310.dp)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF090D18))
                        .border(1.dp, Color(0xFF1B2438), RoundedCornerShape(10.dp))
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Header & Order Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "📚 LAYER ORGANIZER",
                            color = Color(0xFF94A3B8),
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                        Text(
                            text = if (reverseStackOrder) "Order: Foreground First" else "Order: Stacking 0..N",
                            color = NeonPalette.Cyan.copy(alpha = 0.8f),
                            fontSize = 10.sp,
                            modifier = Modifier.clickable { reverseStackOrder = !reverseStackOrder }
                        )
                    }

                    // Category Filter Carousel
                    val categories = listOf("ALL", "SOCKET", "KEYCAP", "GLOSS", "GLOW", "ICON", "TEXT")
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        categories.forEach { cat ->
                            val isSelected = selectedCategoryFilter == cat
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (isSelected) NeonPalette.Cyan else Color(0xFF121828))
                                    .clickable { selectedCategoryFilter = cat }
                                    .padding(horizontal = 7.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = cat,
                                    color = if (isSelected) Color.Black else Color.White.copy(alpha = 0.7f),
                                    fontSize = 9.5.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }

                    // Scrollable Layer Stack
                    val rawIndices = (0 until totalLayers).toList()
                    val orderedIndices = if (reverseStackOrder) rawIndices.reversed() else rawIndices
                    val filteredIndices = orderedIndices.filter { idx ->
                        val layer = layers[idx]
                        val details = NxprcLayerCodeGenerator.getLayerDetails(idx, layer, document)
                        val matchesCat = selectedCategoryFilter == "ALL" || details.categoryBadge.equals(selectedCategoryFilter, ignoreCase = true)
                        val matchesSearch = searchQuery.isBlank() || details.title.contains(searchQuery, ignoreCase = true) || details.categoryBadge.contains(searchQuery, ignoreCase = true)
                        matchesCat && matchesSearch
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            filteredIndices.forEach { idx ->
                                val layer = layers[idx]
                                val isActive = idx in activeLayerIndices
                                val isSolo = soloLayerIndex == idx
                                val isSelected = safeSelectedIndex == idx
                                val details = remember(idx, layer, document) {
                                    NxprcLayerCodeGenerator.getLayerDetails(idx, layer, document)
                                }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            when {
                                                isSelected -> Color(0xFF13223A)
                                                isSolo -> Color(0xFF281D10)
                                                !isActive -> Color(0xFF0C0F16)
                                                else -> Color(0xFF0F1524)
                                            }
                                        )
                                        .border(
                                            width = if (isSelected) 1.5.dp else 1.dp,
                                            color = when {
                                                isSelected -> NeonPalette.Cyan
                                                isSolo -> Color(0xFFF59E0B)
                                                !isActive -> Color(0xFF1B1E28)
                                                else -> Color(0xFF1E283E)
                                            },
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .clickable { onSelectedLayerChange(idx) }
                                        .padding(horizontal = 8.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // 1. Mute Toggle Button
                                    Box(
                                        modifier = Modifier
                                            .size(26.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(if (isActive) Color(0xFF0B2F25) else Color(0xFF2A1215))
                                            .border(1.dp, if (isActive) Color(0xFF10B981) else Color(0xFF7F1D1D), RoundedCornerShape(4.dp))
                                            .clickable {
                                                val newSet = if (isActive) activeLayerIndices - idx else activeLayerIndices + idx
                                                onActiveLayersChange(newSet)
                                                onFeedback(if (isActive) "Layer #$idx muted (cleanly omitted)" else "Layer #$idx activated")
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(text = if (isActive) "👁️" else "🚫", fontSize = 11.sp)
                                    }

                                    // 2. Visual Isolated Skia Thumbnail
                                    Box(
                                        modifier = Modifier
                                            .size(46.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color(0xFF05070C))
                                            .border(1.dp, Color(0xFF1E273A), RoundedCornerShape(6.dp))
                                            .alpha(if (isActive) 1f else 0.4f),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        NxprcCanvasPreview(
                                            document = document,
                                            activeLayersOnly = listOf(layer),
                                            sizeDp = 42
                                        )
                                    }

                                    // 3. Layer Info & Badge
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                                        ) {
                                            Text(
                                                text = "#$idx",
                                                color = Color.White.copy(alpha = 0.5f),
                                                fontFamily = FontFamily.Monospace,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 10.5.sp
                                            )

                                            val badgeCol = Color(details.badgeColor)
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(3.dp))
                                                    .background(badgeCol.copy(alpha = if (isActive) 0.2f else 0.08f))
                                                    .border(1.dp, badgeCol.copy(alpha = if (isActive) 0.8f else 0.3f), RoundedCornerShape(3.dp))
                                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                                            ) {
                                                Text(
                                                    text = details.categoryBadge,
                                                    color = if (isActive) badgeCol else badgeCol.copy(alpha = 0.4f),
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 8.5.sp
                                                )
                                            }

                                            if (!isActive) {
                                                Text("(MUTED)", color = Color(0xFFEF4444), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }

                                        Text(
                                            text = details.title,
                                            color = if (isActive) Color.White else Color.White.copy(alpha = 0.4f),
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            fontSize = 11.sp,
                                            maxLines = 1
                                        )
                                    }

                                    // 4. Solo Button
                                    Box(
                                        modifier = Modifier
                                            .size(26.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(if (isSolo) Color(0xFFD97706) else Color(0xFF182030))
                                            .border(1.dp, if (isSolo) Color(0xFFFBBF24) else Color(0xFF2A364E), RoundedCornerShape(4.dp))
                                            .clickable {
                                                val newSolo = if (isSolo) null else idx
                                                onSoloLayerChange(newSolo)
                                                onFeedback(if (newSolo != null) "Soloing Layer #$idx in workspace" else "Exited solo mode")
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("🎯", fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }

                // =====================================================================
                // ZONE 2 (CENTER ~40% width): VISUAL WORKSPACE (THE FOCUS)
                // =====================================================================
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Top Canvas Toolbar: Zoom & View Controls
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF090D18))
                            .border(1.dp, Color(0xFF1B2438), RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text("CANVAS WORKSPACE", color = NeonPalette.Cyan, fontWeight = FontWeight.Bold, fontSize = 11.5.sp)
                            Text("• Interactive Spring Physics", color = Color.White.copy(alpha = 0.5f), fontSize = 10.5.sp)
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    isAutoFit = false
                                    zoomScale = (zoomScale - 0.25f).coerceAtLeast(0.5f)
                                },
                                shape = RoundedCornerShape(4.dp),
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                modifier = Modifier.height(24.dp)
                            ) {
                                Text("-", fontSize = 11.sp, color = Color.White)
                            }

                            Text(
                                text = if (isAutoFit) "AUTO" else "${(zoomScale * 100).toInt()}%",
                                color = Color.White,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.5.sp
                            )

                            OutlinedButton(
                                onClick = {
                                    isAutoFit = false
                                    zoomScale = (zoomScale + 0.25f).coerceAtMost(2.0f)
                                },
                                shape = RoundedCornerShape(4.dp),
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                modifier = Modifier.height(24.dp)
                            ) {
                                Text("+", fontSize = 11.sp, color = Color.White)
                            }

                            OutlinedButton(
                                onClick = {
                                    isAutoFit = true
                                    zoomScale = 1.0f
                                },
                                shape = RoundedCornerShape(4.dp),
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                modifier = Modifier.height(24.dp)
                            ) {
                                Text(if (isAutoFit) "Auto-Fit ✓" else "Auto-Fit", fontSize = 10.sp, color = if (isAutoFit) NeonPalette.Cyan else Color.White)
                            }

                            OutlinedButton(
                                onClick = { showGrid = !showGrid },
                                shape = RoundedCornerShape(4.dp),
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                modifier = Modifier.height(24.dp)
                            ) {
                                Text(if (showGrid) "Grid On" else "Grid Off", fontSize = 10.sp, color = Color.White)
                            }
                        }
                    }

                    // Main Stage: Large Composite Button (Spring physics sandbox)
                    BoxWithConstraints(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1.2f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (showGrid) {
                                    Brush.radialGradient(listOf(Color(0xFF0C1322), Color(0xFF04060C)))
                                } else {
                                    Brush.verticalGradient(listOf(Color(0xFF030509), Color(0xFF030509)))
                                }
                            )
                            .border(1.5.dp, Color(0xFF1B263E), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        val availableDimension = minOf(maxWidth.value, maxHeight.value)
                        val autoFitSize = (availableDimension * 0.72f).toInt().coerceIn(180, 440)
                        val scaledSize = if (isAutoFit) autoFitSize else (220 * zoomScale).toInt()

                        NxprcCanvasPreview(
                            document = document,
                            activeLayersOnly = previewLayers,
                            sizeDp = scaledSize
                        )
                    }

                    // Lower Split Stage: Dedicated Isolated Layer Comparison
                    if (selectedDetails != null && selectedLayer != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(140.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF080C16))
                                .border(1.dp, Color(selectedDetails.badgeColor).copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                                .padding(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Isolated Skia Canvas
                                Box(
                                    modifier = Modifier
                                        .size(116.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFF030509))
                                        .border(1.dp, Color(0xFF182236), RoundedCornerShape(8.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    NxprcCanvasPreview(
                                        document = document,
                                        activeLayersOnly = listOf(selectedLayer),
                                        sizeDp = 104
                                    )
                                }

                                // Isolated Layer Description & Quick Toggles
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = "ISOLATED LAYER PREVIEW (#$safeSelectedIndex)",
                                            color = Color(selectedDetails.badgeColor),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.5.sp
                                        )
                                        Text(
                                            text = "[${selectedDetails.categoryBadge}]",
                                            color = Color.White.copy(alpha = 0.6f),
                                            fontSize = 10.sp
                                        )
                                    }

                                    Text(
                                        text = selectedDetails.title,
                                        color = Color.White,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 12.sp
                                    )
                                    Text(
                                        text = selectedDetails.summary,
                                        color = Color(0xFF94A3B8),
                                        fontSize = 10.5.sp
                                    )

                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.padding(top = 4.dp)
                                    ) {
                                        val isActive = safeSelectedIndex in activeLayerIndices
                                        Button(
                                            onClick = {
                                                val newSet = if (isActive) activeLayerIndices - safeSelectedIndex else activeLayerIndices + safeSelectedIndex
                                                onActiveLayersChange(newSet)
                                                onFeedback(if (isActive) "Muted Layer #$safeSelectedIndex" else "Activated Layer #$safeSelectedIndex")
                                            },
                                            shape = RoundedCornerShape(4.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (isActive) Color(0xFF0B2F25) else Color(0xFF2A1215)
                                            ),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                            modifier = Modifier.height(26.dp)
                                        ) {
                                            Text(if (isActive) "👁️ Active" else "🚫 Muted", fontSize = 10.sp)
                                        }

                                        val isSolo = soloLayerIndex == safeSelectedIndex
                                        Button(
                                            onClick = {
                                                val newSolo = if (isSolo) null else safeSelectedIndex
                                                onSoloLayerChange(newSolo)
                                                onFeedback(if (newSolo != null) "Soloing Layer #$safeSelectedIndex" else "Exited solo mode")
                                            },
                                            shape = RoundedCornerShape(4.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (isSolo) Color(0xFFD97706) else Color(0xFF182030)
                                            ),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                            modifier = Modifier.height(26.dp)
                                        ) {
                                            Text(if (isSolo) "🎯 Solo Active" else "🎯 Solo Layer", fontSize = 10.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // =====================================================================
                // ZONE 3 (RIGHT): LAYER INSPECTOR & SURGICAL AI COPILOT (Dedicated Sidebar)
                // =====================================================================
                Column(
                    modifier = Modifier
                        .width(350.dp)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF090D18))
                        .border(1.dp, Color(0xFF1B2438), RoundedCornerShape(10.dp))
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (selectedDetails != null && selectedLayer != null) {
                        // Inspector Title & Copy Code
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "🔍 LAYER INSPECTOR",
                                    color = Color(0xFF94A3B8),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                                Text(
                                    text = "Layer #$safeSelectedIndex: ${selectedDetails.categoryBadge}",
                                    color = Color(selectedDetails.badgeColor),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }

                            Button(
                                onClick = {
                                    Toolkit.getDefaultToolkit().systemClipboard.setContents(
                                        StringSelection(selectedDetails.codeSnippet),
                                        null
                                    )
                                    onFeedback("✓ Layer #$safeSelectedIndex code copied!")
                                },
                                shape = RoundedCornerShape(4.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B263A)),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier.height(26.dp)
                            ) {
                                Text("📋 Copy", fontSize = 10.sp, color = NeonPalette.Cyan, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Decomposed Monospace Code Box
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF030509))
                                .border(1.dp, Color(0xFF151E2E), RoundedCornerShape(6.dp))
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

                        // Surgical AI Copilot Section
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF0D101C))
                                .border(1.dp, Color(0xFF7C3AED).copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                .padding(8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text("🤖 SURGICAL AI COPILOT", color = Color(0xFFC4B5FD), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                Text("🔒 Scope: Layer #$safeSelectedIndex Only", color = Color.White.copy(alpha = 0.5f), fontSize = 9.5.sp)
                            }

                            OutlinedTextField(
                                value = userAiPrompt,
                                onValueChange = { userAiPrompt = it },
                                placeholder = {
                                    Text(
                                        "e.g. 'Make gloss 20% subtler', 'Change icon to neon skull'...",
                                        fontSize = 11.sp,
                                        color = Color.White.copy(alpha = 0.4f)
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp),
                                textStyle = LocalTextStyle.current.copy(fontSize = 11.sp, color = Color.White)
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
                                    onFeedback("✓ Surgical prompt for Layer #$safeSelectedIndex copied! Paste into Claude/GPT/Gemini.")
                                },
                                shape = RoundedCornerShape(6.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED)),
                                modifier = Modifier.fillMaxWidth().height(32.dp),
                                contentPadding = PaddingValues(vertical = 4.dp)
                            ) {
                                Text("🤖 Copy Single-Layer AI Prompt", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }

                            Text(
                                text = "Strict contract: AI modifies ONLY Layer #$safeSelectedIndex, never touching or breaking other layers.",
                                color = Color(0xFFA78BFA),
                                fontSize = 9.sp
                            )
                        }
                    } else {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Select a layer to inspect", color = Color.White.copy(alpha = 0.4f), fontSize = 12.sp)
                        }
                    }
                }
            }

            // =========================================================================
            // 3. BOTTOM CONTEXT & ACTION BAR
            // =========================================================================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF090D18))
                    .border(BorderStroke(1.dp, Color(0xFF1B2438)))
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "ℹ️ Non-Destructive Muting Active:",
                        color = Color(0xFF34D399),
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                    Text(
                        text = "Deactivated layers are cleanly excluded from live render and device binaries, but code is never deleted.",
                        color = Color.White.copy(alpha = 0.65f),
                        fontSize = 10.5.sp
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Quick Export .nxprc
                    Button(
                        onClick = { onExportDoc(exportDoc) },
                        shape = RoundedCornerShape(6.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NeonPalette.Cyan),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.height(30.dp)
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, tint = Color.Black, modifier = Modifier.size(13.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = "Export .nxprc (${exportDoc.canvas.layers.size} L)",
                            fontSize = 11.sp,
                            color = Color.Black,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Quick Push via ADB
                    Button(
                        onClick = { onPushAdbDoc(exportDoc) },
                        shape = RoundedCornerShape(6.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00FF99)),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.height(30.dp)
                    ) {
                        Icon(Icons.Default.Send, contentDescription = null, tint = Color.Black, modifier = Modifier.size(13.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = "Push to Phone (${exportDoc.canvas.layers.size} L)",
                            fontSize = 11.sp,
                            color = Color.Black,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
