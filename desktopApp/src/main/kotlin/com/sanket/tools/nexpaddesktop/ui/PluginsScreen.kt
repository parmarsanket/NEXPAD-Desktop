package com.sanket.tools.nexpaddesktop.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
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
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.ui.graphics.Brush
import com.sanket.tools.nexpaddesktop.plugins.NxprcExporter
import com.sanket.tools.nexpaddesktop.plugins.NxprcHtmlCssConverter
import com.sanket.tools.nexpaddesktop.ui.components.glassCard
import com.sanket.tools.nexpaddesktop.ui.designer.NxprcCanvasPreview
import com.sanket.tools.nexpaddesktop.ui.theme.NeonPalette
import kotlinx.coroutines.launch

@Composable
fun PluginsScreen() {
    val scope = rememberCoroutineScope()

    var htmlSource by remember { mutableStateOf(NxprcHtmlCssConverter.PRESET_ULTRA_NEXPAD_A) }
    var componentId by remember { mutableStateOf("rc.ultra_a") }
    var componentName by remember { mutableStateOf("Ultra A Button") }
    var category by remember { mutableStateOf("BUTTON") }
    var defaultControl by remember { mutableStateOf("A") }
    var exportStatus by remember { mutableStateOf<String?>(null) }
    var isExporting by remember { mutableStateOf(false) }

    // Live compiled document with safe fallback
    val compiledDoc = remember(htmlSource, componentId, componentName, category, defaultControl) {
        try {
            NxprcHtmlCssConverter.convert(
                source = htmlSource,
                id = componentId,
                name = componentName,
                category = category,
                defaultControl = defaultControl
            )
        } catch (_: Exception) {
            NxprcHtmlCssConverter.convert(
                source = NxprcHtmlCssConverter.PRESET_ULTRA_NEXPAD_A,
                id = componentId,
                name = componentName,
                category = category,
                defaultControl = defaultControl
            )
        }
    }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Left Column: HTML / CSS Code Editor
        Column(
            modifier = Modifier
                .weight(1.15f)
                .fillMaxHeight()
                .glassCard()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("HTML / CSS Code", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)

            // Compact Presets Row
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Presets:", fontSize = 11.sp, color = NeonPalette.CardIdleText)
                OutlinedButton(
                    onClick = {
                        htmlSource = NxprcHtmlCssConverter.PRESET_ULTRA_NEXPAD_A
                        componentId = "rc.ultra_a"
                        componentName = "Ultra A Button"
                        defaultControl = "A"
                    },
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text("Ultra (A)", fontSize = 11.sp, color = NeonPalette.Cyan)
                }

                OutlinedButton(
                    onClick = {
                        htmlSource = NxprcHtmlCssConverter.PRESET_CYBER_REACTOR
                        componentId = "rc.cyber_reactor_a"
                        componentName = "Cyber Reactor A"
                        defaultControl = "A"
                    },
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text("Reactor (A)", fontSize = 11.sp, color = Color(0xFF69D980))
                }

                OutlinedButton(
                    onClick = {
                        htmlSource = NxprcHtmlCssConverter.PRESET_CRIMSON_OCTA
                        componentId = "rc.crimson_octa_b"
                        componentName = "Crimson Octa B"
                        defaultControl = "B"
                    },
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text("Octa (B)", fontSize = 11.sp, color = Color(0xFFFF0055))
                }

                OutlinedButton(
                    onClick = {
                        htmlSource = NxprcHtmlCssConverter.PRESET_SPEED_TURBO
                        componentId = "rc.speed_turbo_x"
                        componentName = "Speed Turbo X"
                        defaultControl = "X"
                    },
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text("Turbo (X)", fontSize = 11.sp, color = Color(0xFFFFCC00))
                }
            }

            OutlinedTextField(
                value = htmlSource,
                onValueChange = { htmlSource = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace, fontSize = 12.sp),
                placeholder = { Text("Paste HTML / CSS / SVG button code here...") }
            )
        }

        // Right Column: Merged Sandbox Preview & Export
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .glassCard()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Sandbox Preview & Export", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text("Tap to test tactile physics", color = NeonPalette.Cyan.copy(alpha = 0.7f), fontSize = 10.sp)
            }

            // Dynamic Square Preview Box
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                val previewSize = minOf(maxWidth * 0.95f, maxHeight * 0.98f, 220.dp).coerceAtLeast(100.dp)
                Box(
                    modifier = Modifier
                        .size(previewSize)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.radialGradient(
                                listOf(Color(0xFF0C1322), Color(0xFF030712))
                            )
                        )
                        .border(1.5.dp, NeonPalette.Cyan.copy(alpha = 0.35f), RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    val buttonDp = (previewSize.value * 0.78f).toInt().coerceAtLeast(70)
                    NxprcCanvasPreview(document = compiledDoc, sizeDp = buttonDp)
                }
            }

            // Compact Layers Info Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFF070E1A))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Layers: ${compiledDoc.canvas.layers.size}", color = NeonPalette.Cyan, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                Text("Idle: ${compiledDoc.animations.idleType}", color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp)
                Text("Touch: ${compiledDoc.animations.pressFeedback}", color = Color.White.copy(alpha = 0.6f), fontSize = 10.sp)
            }

            // Metadata Inputs (ID, Name, Key, Category)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = componentId,
                    onValueChange = { componentId = it },
                    label = { Text("ID") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    textStyle = LocalTextStyle.current.copy(fontSize = 12.sp)
                )
                OutlinedTextField(
                    value = componentName,
                    onValueChange = { componentName = it },
                    label = { Text("Name") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    textStyle = LocalTextStyle.current.copy(fontSize = 12.sp)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = defaultControl,
                    onValueChange = { defaultControl = it },
                    label = { Text("Key") },
                    modifier = Modifier.weight(0.42f),
                    singleLine = true,
                    textStyle = LocalTextStyle.current.copy(fontSize = 12.sp)
                )
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Category") },
                    modifier = Modifier.weight(0.58f),
                    singleLine = true,
                    textStyle = LocalTextStyle.current.copy(fontSize = 12.sp)
                )
            }

            if (exportStatus != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (exportStatus!!.contains("Error") || exportStatus!!.contains("canceled")) Color(0xFF3B121A) else Color(0xFF0F2C24)
                    ),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = exportStatus!!,
                        color = if (exportStatus!!.contains("Error") || exportStatus!!.contains("canceled")) Color(0xFFFF6B6B) else Color(0xFF00FF99),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 11.sp
                    )
                }
            }

            // Prominent Full-Width Export Button
            Button(
                onClick = {
                    scope.launch {
                        isExporting = true
                        val res = NxprcExporter.exportToFile(compiledDoc)
                        res.fold(
                            onSuccess = { file ->
                                exportStatus = "Saved: ${file.name} (${file.length()} bytes)"
                            },
                            onFailure = { ex ->
                                exportStatus = ex.message ?: "Export failed"
                            }
                        )
                        isExporting = false
                    }
                },
                enabled = !isExporting,
                modifier = Modifier.fillMaxWidth().height(44.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NeonPalette.Cyan)
            ) {
                Icon(Icons.Default.Download, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Export .nxprc File", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
    }
}
