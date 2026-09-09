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
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.ui.graphics.Brush
import com.sanket.tools.nexpaddesktop.plugins.NxprcExporter
import com.sanket.tools.nexpaddesktop.plugins.NxprcHtmlCssConverter
import com.sanket.tools.nexpaddesktop.ui.components.glassCard
import com.sanket.tools.nexpaddesktop.ui.designer.NxprcCanvasPreview
import com.sanket.tools.nexpaddesktop.ui.theme.NeonPalette
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection

data class ControllerButtonSpec(
    val key: String,
    val label: String,
    val category: String,
    val defaultName: String,
    val defaultId: String,
    val widthDp: Int,
    val heightDp: Int,
    val accentColor: Color
)

@Composable
fun PluginsScreen() {
    val scope = rememberCoroutineScope()

    val categories = remember {
        listOf(
            "ABXY" to "🎮 ABXY",
            "DPAD" to "🧭 D-Pad",
            "TRIGGERS" to "🎯 Triggers",
            "BUMPERS" to "🛡️ Bumpers",
            "STICKS" to "🕹️ Sticks",
            "SYSTEM" to "⚙️ System"
        )
    }

    val buttonsByCategory = remember {
        mapOf(
            "ABXY" to listOf(
                ControllerButtonSpec("A", "A Button", "BUTTON", "Action A Button", "rc.action_a", 96, 96, Color(0xFF4ADE80)),
                ControllerButtonSpec("B", "B Button", "BUTTON", "Action B Button", "rc.action_b", 96, 96, Color(0xFFFF3366)),
                ControllerButtonSpec("X", "X Button", "BUTTON", "Action X Button", "rc.action_x", 96, 96, Color(0xFF00B0FF)),
                ControllerButtonSpec("Y", "Y Button", "BUTTON", "Action Y Button", "rc.action_y", 96, 96, Color(0xFFFFCC00))
            ),
            "DPAD" to listOf(
                ControllerButtonSpec("UP", "D-Pad Up", "DPAD", "D-Pad Up Button", "rc.dpad_up", 80, 80, Color(0xFF00F0FF)),
                ControllerButtonSpec("DOWN", "D-Pad Down", "DPAD", "D-Pad Down Button", "rc.dpad_down", 80, 80, Color(0xFF00F0FF)),
                ControllerButtonSpec("LEFT", "D-Pad Left", "DPAD", "D-Pad Left Button", "rc.dpad_left", 80, 80, Color(0xFF00F0FF)),
                ControllerButtonSpec("RIGHT", "D-Pad Right", "DPAD", "D-Pad Right Button", "rc.dpad_right", 80, 80, Color(0xFF00F0FF)),
                ControllerButtonSpec("DPAD", "Cross Pad (4-Way)", "DPAD", "Tactile Cross Pad", "rc.dpad_cross", 140, 140, Color(0xFF00E5FF))
            ),
            "TRIGGERS" to listOf(
                ControllerButtonSpec("LT", "LT (Left Trigger)", "TRIGGER", "Trigger LT", "rc.trigger_lt", 72, 110, Color(0xFFFF3366)),
                ControllerButtonSpec("RT", "RT (Right Trigger)", "TRIGGER", "Trigger RT", "rc.trigger_rt", 72, 110, Color(0xFFFF3366))
            ),
            "BUMPERS" to listOf(
                ControllerButtonSpec("LB", "LB (Left Bumper)", "BUMPER", "Bumper LB", "rc.bumper_lb", 120, 52, Color(0xFF00F0FF)),
                ControllerButtonSpec("RB", "RB (Right Bumper)", "BUMPER", "Bumper RB", "rc.bumper_rb", 120, 52, Color(0xFF00F0FF))
            ),
            "STICKS" to listOf(
                ControllerButtonSpec("LS", "LS (Left Stick / L3)", "JOYSTICK", "Stick LS", "rc.stick_ls", 100, 100, Color(0xFF4ADE80)),
                ControllerButtonSpec("RS", "RS (Right Stick / R3)", "JOYSTICK", "Stick RS", "rc.stick_rs", 100, 100, Color(0xFF4ADE80))
            ),
            "SYSTEM" to listOf(
                ControllerButtonSpec("MENU", "Menu (Pause / Start)", "SYSTEM", "Menu Button", "rc.system_menu", 64, 44, Color(0xFFE0E0E0)),
                ControllerButtonSpec("VIEW", "View (Back / Select)", "SYSTEM", "View Button", "rc.system_view", 64, 44, Color(0xFFE0E0E0)),
                ControllerButtonSpec("HOME", "Home (Xbox / Guide)", "SYSTEM", "Home Button", "rc.system_home", 70, 70, Color(0xFFFFFFFF))
            )
        )
    }

    var selectedCategory by remember { mutableStateOf("ABXY") }
    var selectedButtonKey by remember { mutableStateOf("A") }

    var htmlSource by remember { mutableStateOf(NxprcHtmlCssConverter.PRESET_ULTRA_NEXPAD_A) }
    var componentId by remember { mutableStateOf("rc.action_a") }
    var componentName by remember { mutableStateOf("Action A Button") }
    var category by remember { mutableStateOf("BUTTON") }
    var defaultControl by remember { mutableStateOf("A") }
    var targetWidthDp by remember { mutableStateOf(96) }
    var targetHeightDp by remember { mutableStateOf(96) }

    var exportStatus by remember { mutableStateOf<String?>(null) }
    var isExporting by remember { mutableStateOf(false) }

    var showAiPromptModal by remember { mutableStateOf(false) }
    var promptCopiedBanner by remember { mutableStateOf<String?>(null) }

    // Debounced async compilation — prevents UI jank on every keystroke
    var compiledDoc by remember {
        mutableStateOf(
            NxprcHtmlCssConverter.convert(
                source = NxprcHtmlCssConverter.PRESET_ULTRA_NEXPAD_A,
                id = "rc.action_a",
                name = "Action A Button",
                category = "BUTTON",
                defaultControl = "A"
            )
        )
    }
    var compileError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(htmlSource, componentId, componentName, category, defaultControl) {
        delay(350) // Debounce keystrokes
        withContext(Dispatchers.Default) {
            try {
                val doc = NxprcHtmlCssConverter.convert(
                    source = htmlSource,
                    id = componentId,
                    name = componentName,
                    category = category,
                    defaultControl = defaultControl
                )
                compiledDoc = doc
                compileError = null
            } catch (e: Exception) {
                compileError = e.message ?: "Compilation error"
            }
        }
    }

    // Auto-dismiss copy feedback banner after 4 seconds
    LaunchedEffect(promptCopiedBanner) {
        if (promptCopiedBanner != null) {
            delay(4000)
            promptCopiedBanner = null
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ==========================================
            // Left Column: Controller Button Studio & Editor
            // ==========================================
            Column(
                modifier = Modifier
                    .weight(1.18f)
                    .fillMaxHeight()
                    .glassCard()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 1. Header & AI Instructor Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "🎮 Controller Component Studio",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Text(
                            "Target: $defaultControl ($category • ${targetWidthDp}x${targetHeightDp}dp)",
                            color = NeonPalette.Cyan.copy(alpha = 0.85f),
                            fontSize = 11.sp
                        )
                    }

                    // Specialized AI Prompt Action Buttons
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = {
                                val prompt = NxprcHtmlCssConverter.generateAiPrompt(
                                    control = defaultControl,
                                    category = category,
                                    widthDp = targetWidthDp,
                                    heightDp = targetHeightDp
                                )
                                Toolkit.getDefaultToolkit().systemClipboard.setContents(
                                    StringSelection(prompt),
                                    null
                                )
                                promptCopiedBanner = "✓ Specialized AI Prompt for $defaultControl ($category) copied!"
                            },
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED))
                        ) {
                            Text("🤖 Copy AI Prompt", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }

                        OutlinedButton(
                            onClick = { showAiPromptModal = true },
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("👁️ View Prompt", fontSize = 11.sp, color = NeonPalette.Cyan)
                        }

                        OutlinedButton(
                            onClick = {
                                htmlSource = NxprcHtmlCssConverter.getReferenceTemplate(defaultControl)
                                promptCopiedBanner = "✓ Loaded starter template for $defaultControl ($category)!"
                            },
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, NeonPalette.Cyan.copy(alpha = 0.5f))
                        ) {
                            Text("⚡ Load Starter Code", fontSize = 11.sp, color = NeonPalette.Cyan)
                        }
                    }
                }

                // 2. Copy Feedback Banner
                if (promptCopiedBanner != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1B4B)),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                promptCopiedBanner!!,
                                color = Color(0xFFC4B5FD),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // 3. Category Selector Carousel
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    categories.forEach { (catKey, catLabel) ->
                        val isSelected = selectedCategory == catKey
                        Button(
                            onClick = {
                                selectedCategory = catKey
                                val firstButton = buttonsByCategory[catKey]?.firstOrNull()
                                if (firstButton != null) {
                                    selectedButtonKey = firstButton.key
                                    defaultControl = firstButton.key
                                    category = firstButton.category
                                    componentId = firstButton.defaultId
                                    componentName = firstButton.defaultName
                                    targetWidthDp = firstButton.widthDp
                                    targetHeightDp = firstButton.heightDp
                                }
                            },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 3.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) NeonPalette.Cyan else Color(0xFF131826)
                            )
                        ) {
                            Text(
                                catLabel,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.Black else Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }
                }

                // 4. Button Selector Row for Active Category
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val buttons = buttonsByCategory[selectedCategory] ?: emptyList()
                    buttons.forEach { btn ->
                        val isSelected = selectedButtonKey == btn.key
                        OutlinedButton(
                            onClick = {
                                selectedButtonKey = btn.key
                                defaultControl = btn.key
                                category = btn.category
                                componentId = btn.defaultId
                                componentName = btn.defaultName
                                targetWidthDp = btn.widthDp
                                targetHeightDp = btn.heightDp
                            },
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 3.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) btn.accentColor else Color(0xFF232B3E)
                            ),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (isSelected) btn.accentColor.copy(alpha = 0.15f) else Color.Transparent
                            )
                        ) {
                            Text(
                                btn.label,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) btn.accentColor else Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }
                }

                // 5. HTML / CSS Source Editor
                OutlinedTextField(
                    value = htmlSource,
                    onValueChange = { htmlSource = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace, fontSize = 12.sp),
                    placeholder = { Text("Paste your AI-generated HTML / CSS code here...") }
                )
            }

            // ==========================================
            // Right Column: Live Sandbox & Export
            // ==========================================
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
                    Text("Live Sandbox & Export", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text("Tap to test tactile physics", color = NeonPalette.Cyan.copy(alpha = 0.7f), fontSize = 10.sp)
                }

                // Dynamic Aspect-Ratio Preview Box
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    val maxBoxW = maxWidth * 0.95f
                    val maxBoxH = maxHeight * 0.96f

                    val aspect = targetWidthDp.toFloat() / targetHeightDp.toFloat()
                    val (boxW, boxH) = if (aspect >= 1.0f) {
                        val w = minOf(maxBoxW, 260.dp)
                        val h = (w / aspect).coerceAtMost(maxBoxH)
                        Pair(w, h)
                    } else {
                        val h = minOf(maxBoxH, 260.dp)
                        val w = (h * aspect).coerceAtMost(maxBoxW)
                        Pair(w, h)
                    }

                    Box(
                        modifier = Modifier
                            .size(width = boxW, height = boxH)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                Brush.radialGradient(
                                    listOf(Color(0xFF0C1322), Color(0xFF030712))
                                )
                            )
                            .border(1.5.dp, NeonPalette.Cyan.copy(alpha = 0.35f), RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        val previewDp = (minOf(boxW.value, boxH.value) * 0.85f).toInt().coerceAtLeast(60)
                        NxprcCanvasPreview(document = compiledDoc, sizeDp = previewDp)
                    }
                }

                // Compact Layers & Physics Info Bar
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

                // Compile Warning (if any)
                if (compileError != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF3B121A)),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "Warning: $compileError (showing last valid preview)",
                            color = Color(0xFFFF8080),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontSize = 11.sp
                        )
                    }
                }

                // Export Status
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

                // Full-Width Export Button
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

        // ==========================================
        // Modal Dialog: AI Prompt Inspector
        // ==========================================
        if (showAiPromptModal) {
            val generatedPrompt = remember(defaultControl, category, targetWidthDp, targetHeightDp) {
                NxprcHtmlCssConverter.generateAiPrompt(
                    control = defaultControl,
                    category = category,
                    widthDp = targetWidthDp,
                    heightDp = targetHeightDp
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.75f)),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .fillMaxHeight(0.85f)
                        .border(1.5.dp, Color(0xFF7C3AED), RoundedCornerShape(14.dp)),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0D111A))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    "🤖 AI Prompt Instructor ($defaultControl • $category)",
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    "Compatible with: ChatGPT (GPT-4o, o1, o3-mini), Claude (3.5/3.7), Gemini (2.0/1.5), DeepSeek (V3/R1), Grok 2",
                                    color = Color(0xFFC4B5FD),
                                    fontSize = 12.sp
                                )
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = {
                                        Toolkit.getDefaultToolkit().systemClipboard.setContents(
                                            StringSelection(generatedPrompt),
                                            null
                                        )
                                        promptCopiedBanner = "✓ AI Prompt for $defaultControl copied to clipboard!"
                                        showAiPromptModal = false
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED)),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Copy to Clipboard", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }

                                OutlinedButton(
                                    onClick = { showAiPromptModal = false },
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Close", color = Color.White, fontSize = 12.sp)
                                }
                            }
                        }

                        // Scrollable Prompt Text Display
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF06090F))
                                .border(1.dp, Color(0xFF1E2638), RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = generatedPrompt,
                                color = Color(0xFFE2E8F0),
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.5.sp,
                                modifier = Modifier.verticalScroll(rememberScrollState())
                            )
                        }
                    }
                }
            }
        }
    }
}
