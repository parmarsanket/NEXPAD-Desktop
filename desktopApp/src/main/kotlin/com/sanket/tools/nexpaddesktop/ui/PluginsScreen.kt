package com.sanket.tools.nexpaddesktop.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sanket.tools.nexpad.nxprc.NxprcDocument
import com.sanket.tools.nexpaddesktop.connection.ActiveTransport
import com.sanket.tools.nexpaddesktop.plugins.DesktopPluginManager
import com.sanket.tools.nexpaddesktop.plugins.NxprcExporter
import com.sanket.tools.nexpaddesktop.plugins.NxprcHtmlCssConverter
import com.sanket.tools.nexpaddesktop.plugins.UniversalPushManager
import com.sanket.tools.nexpaddesktop.ui.components.glassCard
import com.sanket.tools.nexpaddesktop.ui.designer.FullAuditPreviewScreen
import com.sanket.tools.nexpaddesktop.ui.designer.LayerStudioFullScreen
import com.sanket.tools.nexpaddesktop.ui.designer.NxprcCanvasPreview
import com.sanket.tools.nexpaddesktop.ui.layout.AdaptiveLayoutSpec
import com.sanket.tools.nexpaddesktop.ui.layout.adaptiveLayoutSpec
import com.sanket.tools.nexpaddesktop.ui.theme.NeonPalette
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection

import com.sanket.tools.nexpad.category.CategoryManager
import com.sanket.tools.nexpad.category.SubCategoryDefinition

val SubCategoryDefinition.accentColor: Color get() = Color(accentColorArgb)

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

@Composable
fun PluginsScreen(
    activeTransport: ActiveTransport = UniversalPushManager.currentTransport
) {
    val scope = rememberCoroutineScope()

    val categories = remember {
        CategoryManager.getAllCategories().map { it.id to "${it.emoji} ${it.title}" }
    }

    val buttonsByCategory = remember {
        CategoryManager.getAllCategories().associate { cat ->
            cat.id to cat.controls
        }
    }

    var selectedCategory by remember { mutableStateOf("ABXY") }
    var selectedButtonKey by remember { mutableStateOf("A") }

    var htmlSource by remember { mutableStateOf(NxprcHtmlCssConverter.PRESET_NEO_TACTILE_A) }
    var componentId by remember { mutableStateOf("rc.action_a") }
    var componentName by remember { mutableStateOf("Action A Button") }
    var category by remember { mutableStateOf("BUTTON") }
    var defaultControl by remember { mutableStateOf("A") }
    var targetWidthDp by remember { mutableStateOf(96) }
    var targetHeightDp by remember { mutableStateOf(96) }

    var exportStatus by remember { mutableStateOf<String?>(null) }
    var isExporting by remember { mutableStateOf(false) }

    var showAiPromptModal by remember { mutableStateOf(false) }
    var showFullAuditPreview by remember { mutableStateOf(false) }
    var showFullScreenLayerStudio by remember { mutableStateOf(false) }
    var promptCopiedBanner by remember { mutableStateOf<String?>(null) }

    // Single-pane tab selection when window is narrow
    var singlePaneTab by remember { mutableStateOf(0) } // 0: Editor, 1: Live Sandbox

    // Debounced async compilation — prevents UI jank on every keystroke
    var compiledDoc by remember {
        mutableStateOf(
            NxprcHtmlCssConverter.convert(
                source = NxprcHtmlCssConverter.PRESET_NEO_TACTILE_A,
                id = "rc.action_a",
                name = "Action A Button",
                category = "BUTTON",
                defaultControl = "A"
            )
        )
    }
    var compileError by remember { mutableStateOf<String?>(null) }

    // Layer Studio & Layer Manager States
    var activeLayerIndices by remember(compiledDoc) { mutableStateOf((0 until compiledDoc.canvas.layers.size).toSet()) }
    var soloLayerIndex by remember(compiledDoc) { mutableStateOf<Int?>(null) }
    var selectedLayerIndex by remember(compiledDoc) { mutableStateOf(0) }

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

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val availableWidth = maxWidth
        val availableHeight = maxHeight
        val layout = adaptiveLayoutSpec(availableWidth, availableHeight)

        if (layout.useTwoPaneLayout) {
            // =========================================================================
            // TWO-PANE DESKTOP WORKSTATION LAYOUT (Wide / Standard Window)
            // =========================================================================
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(layout.horizontalPadding),
                horizontalArrangement = Arrangement.spacedBy(layout.paneSpacing)
            ) {
                // Left Column: Controller Button Studio & Editor
                ComponentEditorPane(
                    modifier = Modifier
                        .weight(1.15f)
                        .fillMaxHeight(),
                    isNarrow = (availableWidth * 0.53f) < 540.dp,
                    isShort = layout.isShortScreen,
                    defaultControl = defaultControl,
                    category = category,
                    targetWidthDp = targetWidthDp,
                    targetHeightDp = targetHeightDp,
                    promptCopiedBanner = promptCopiedBanner,
                    onCopyAiPrompt = {
                        val prompt = NxprcHtmlCssConverter.generateAiPrompt(
                            control = defaultControl,
                            category = category,
                            widthDp = targetWidthDp,
                            heightDp = targetHeightDp
                        )
                        val ok = safeCopyToClipboard(prompt)
                        promptCopiedBanner = if (ok) "✓ AI Prompt for $defaultControl ($category) copied!" else "⚠️ Clipboard busy — please try again"
                    },
                    onOpenPromptModal = { showAiPromptModal = true },
                    onLoadStarter = {
                        htmlSource = NxprcHtmlCssConverter.getReferenceTemplate(defaultControl, category)
                        promptCopiedBanner = "✓ Loaded starter template for $defaultControl ($category)!"
                    },
                    selectedCategory = selectedCategory,
                    onSelectCategory = { catKey ->
                        selectedCategory = catKey
                        val firstButton = buttonsByCategory[catKey]?.firstOrNull()
                        if (firstButton != null) {
                            selectedButtonKey = firstButton.key
                            defaultControl = firstButton.key
                            category = firstButton.componentType.name
                            componentId = firstButton.defaultId
                            componentName = firstButton.defaultName
                            targetWidthDp = firstButton.defaultWidthDp
                            targetHeightDp = firstButton.defaultHeightDp
                            htmlSource = firstButton.starterHtmlPreset ?: NxprcHtmlCssConverter.getReferenceTemplate(firstButton.key, firstButton.componentType.name)
                        }
                    },
                    categories = categories,
                    buttonsByCategory = buttonsByCategory,
                    selectedButtonKey = selectedButtonKey,
                    onSelectButton = { btn ->
                        selectedButtonKey = btn.key
                        defaultControl = btn.key
                        category = btn.componentType.name
                        componentId = btn.defaultId
                        componentName = btn.defaultName
                        targetWidthDp = btn.defaultWidthDp
                        targetHeightDp = btn.defaultHeightDp
                        htmlSource = btn.starterHtmlPreset ?: NxprcHtmlCssConverter.getReferenceTemplate(btn.key, btn.componentType.name)
                    },
                    htmlSource = htmlSource,
                    onHtmlSourceChange = { htmlSource = it }
                )

                // Right Column: Live Sandbox & Export
                LiveSandboxPane(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    isNarrow = (availableWidth * 0.47f) < 460.dp,
                    isShort = layout.isShortScreen,
                    compiledDoc = compiledDoc,
                    activeLayerIndices = activeLayerIndices,
                    soloLayerIndex = soloLayerIndex,
                    targetWidthDp = targetWidthDp,
                    targetHeightDp = targetHeightDp,
                    componentId = componentId,
                    componentName = componentName,
                    defaultControl = defaultControl,
                    category = category,
                    compileError = compileError,
                    exportStatus = exportStatus,
                    isExporting = isExporting,
                    onOpenLayerStudio = { showFullScreenLayerStudio = true },
                    onOpenFullAudit = { showFullAuditPreview = true },
                    onExport = {
                        scope.launch {
                            isExporting = true
                            val exportDoc = if (activeLayerIndices.size == compiledDoc.canvas.layers.size) {
                                compiledDoc
                            } else {
                                compiledDoc.copy(
                                    canvas = compiledDoc.canvas.copy(
                                        layers = compiledDoc.canvas.layers.filterIndexed { idx, _ -> idx in activeLayerIndices }
                                    )
                                )
                            }
                            val res = NxprcExporter.exportToFile(exportDoc)
                            res.fold(
                                onSuccess = { file ->
                                    exportStatus = "Saved: ${file.name} (${file.length()} bytes • ${exportDoc.canvas.layers.size} layers)"
                                },
                                onFailure = { ex ->
                                    exportStatus = ex.message ?: "Export failed"
                                }
                            )
                            isExporting = false
                        }
                    },
                    activeTransport = activeTransport,
                    onPush = {
                        scope.launch {
                            isExporting = true
                            val exportDoc = if (activeLayerIndices.size == compiledDoc.canvas.layers.size) {
                                compiledDoc
                            } else {
                                compiledDoc.copy(
                                    canvas = compiledDoc.canvas.copy(
                                        layers = compiledDoc.canvas.layers.filterIndexed { idx, _ -> idx in activeLayerIndices }
                                    )
                                )
                            }
                            exportStatus = "Pushing ${exportDoc.canvas.layers.size} layers to phone via ${activeTransport.displayName}..."
                            val res = UniversalPushManager.pushComponent(exportDoc)
                            res.fold(
                                onSuccess = { msg -> exportStatus = msg },
                                onFailure = { ex -> exportStatus = "Push Error: ${ex.message}" }
                            )
                            isExporting = false
                        }
                    }
                )
            }
        } else {
            // =========================================================================
            // SINGLE-PANE ADAPTIVE MODE (Narrow / Compact Window < 880dp)
            // =========================================================================
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(layout.horizontalPadding),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Sleek Segmented Switcher Strip
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF090E18))
                        .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(8.dp))
                        .padding(3.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(32.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (singlePaneTab == 0) NeonPalette.Cyan else Color.Transparent)
                            .clickable { singlePaneTab = 0 },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "💻 Code Editor & Mapping",
                            color = if (singlePaneTab == 0) Color.Black else Color.White.copy(alpha = 0.7f),
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.5.sp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(32.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (singlePaneTab == 1) NeonPalette.Cyan else Color.Transparent)
                            .clickable { singlePaneTab = 1 },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "🎮 Live Sandbox & Export",
                            color = if (singlePaneTab == 1) Color.Black else Color.White.copy(alpha = 0.7f),
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.5.sp
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    if (singlePaneTab == 0) {
                        ComponentEditorPane(
                            modifier = Modifier.fillMaxSize(),
                            isNarrow = availableWidth < 540.dp,
                            isShort = layout.isShortScreen,
                            defaultControl = defaultControl,
                            category = category,
                            targetWidthDp = targetWidthDp,
                            targetHeightDp = targetHeightDp,
                            promptCopiedBanner = promptCopiedBanner,
                            onCopyAiPrompt = {
                                val prompt = NxprcHtmlCssConverter.generateAiPrompt(
                                    control = defaultControl,
                                    category = category,
                                    widthDp = targetWidthDp,
                                    heightDp = targetHeightDp
                                )
                                val ok = safeCopyToClipboard(prompt)
                                promptCopiedBanner = if (ok) "✓ AI Prompt for $defaultControl ($category) copied!" else "⚠️ Clipboard busy — please try again"
                            },
                            onOpenPromptModal = { showAiPromptModal = true },
                            onLoadStarter = {
                                htmlSource = NxprcHtmlCssConverter.getReferenceTemplate(defaultControl, category)
                                promptCopiedBanner = "✓ Loaded starter template for $defaultControl ($category)!"
                            },
                            selectedCategory = selectedCategory,
                            onSelectCategory = { catKey ->
                                selectedCategory = catKey
                                val firstButton = buttonsByCategory[catKey]?.firstOrNull()
                                if (firstButton != null) {
                                    selectedButtonKey = firstButton.key
                                    defaultControl = firstButton.key
                                    category = firstButton.componentType.name
                                    componentId = firstButton.defaultId
                                    componentName = firstButton.defaultName
                                    targetWidthDp = firstButton.defaultWidthDp
                                    targetHeightDp = firstButton.defaultHeightDp
                                    htmlSource = firstButton.starterHtmlPreset ?: NxprcHtmlCssConverter.getReferenceTemplate(firstButton.key, firstButton.componentType.name)
                                }
                            },
                            categories = categories,
                            buttonsByCategory = buttonsByCategory,
                            selectedButtonKey = selectedButtonKey,
                            onSelectButton = { btn ->
                                selectedButtonKey = btn.key
                                defaultControl = btn.key
                                category = btn.componentType.name
                                componentId = btn.defaultId
                                componentName = btn.defaultName
                                targetWidthDp = btn.defaultWidthDp
                                targetHeightDp = btn.defaultHeightDp
                                htmlSource = btn.starterHtmlPreset ?: NxprcHtmlCssConverter.getReferenceTemplate(btn.key, btn.componentType.name)
                            },
                            htmlSource = htmlSource,
                            onHtmlSourceChange = { htmlSource = it }
                        )
                    } else {
                        LiveSandboxPane(
                            modifier = Modifier.fillMaxSize(),
                            isNarrow = availableWidth < 460.dp,
                            isShort = layout.isShortScreen,
                            compiledDoc = compiledDoc,
                            activeLayerIndices = activeLayerIndices,
                            soloLayerIndex = soloLayerIndex,
                            targetWidthDp = targetWidthDp,
                            targetHeightDp = targetHeightDp,
                            componentId = componentId,
                            componentName = componentName,
                            defaultControl = defaultControl,
                            category = category,
                            compileError = compileError,
                            exportStatus = exportStatus,
                            isExporting = isExporting,
                            onOpenLayerStudio = { showFullScreenLayerStudio = true },
                            onOpenFullAudit = { showFullAuditPreview = true },
                            onExport = {
                                scope.launch {
                                    isExporting = true
                                    val exportDoc = if (activeLayerIndices.size == compiledDoc.canvas.layers.size) {
                                        compiledDoc
                                    } else {
                                        compiledDoc.copy(
                                            canvas = compiledDoc.canvas.copy(
                                                layers = compiledDoc.canvas.layers.filterIndexed { idx, _ -> idx in activeLayerIndices }
                                            )
                                        )
                                    }
                                    val res = NxprcExporter.exportToFile(exportDoc)
                                    res.fold(
                                        onSuccess = { file ->
                                            exportStatus = "Saved: ${file.name} (${file.length()} bytes • ${exportDoc.canvas.layers.size} layers)"
                                        },
                                        onFailure = { ex ->
                                            exportStatus = ex.message ?: "Export failed"
                                        }
                                    )
                                    isExporting = false
                                }
                            },
                            activeTransport = activeTransport,
                            onPush = {
                                scope.launch {
                                    isExporting = true
                                    val exportDoc = if (activeLayerIndices.size == compiledDoc.canvas.layers.size) {
                                        compiledDoc
                                    } else {
                                        compiledDoc.copy(
                                            canvas = compiledDoc.canvas.copy(
                                                layers = compiledDoc.canvas.layers.filterIndexed { idx, _ -> idx in activeLayerIndices }
                                            )
                                        )
                                    }
                                    exportStatus = "Pushing ${exportDoc.canvas.layers.size} layers to phone via ${activeTransport.displayName}..."
                                    val res = UniversalPushManager.pushComponent(exportDoc)
                                    res.fold(
                                        onSuccess = { msg -> exportStatus = msg },
                                        onFailure = { ex -> exportStatus = "Push Error: ${ex.message}" }
                                    )
                                    isExporting = false
                                }
                            }
                        )
                    }
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
                                        val ok = safeCopyToClipboard(generatedPrompt)
                                        promptCopiedBanner = if (ok) "✓ AI Prompt for $defaultControl copied to clipboard!" else "⚠️ Clipboard busy — please try again"
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

        // Full Layer-by-Layer Audit & Parity Preview Screen
        if (showFullAuditPreview) {
            FullAuditPreviewScreen(
                document = compiledDoc,
                htmlSource = htmlSource,
                onOpenLayerStudio = {
                    showFullAuditPreview = false
                    showFullScreenLayerStudio = true
                },
                onClose = { showFullAuditPreview = false }
            )
        }

        // Dedicated Full-Screen Layer Studio & Layer Manager Workspace
        if (showFullScreenLayerStudio) {
            LayerStudioFullScreen(
                document = compiledDoc,
                htmlSource = htmlSource,
                activeLayerIndices = activeLayerIndices,
                onActiveLayersChange = { activeLayerIndices = it },
                soloLayerIndex = soloLayerIndex,
                onSoloLayerChange = { soloLayerIndex = it },
                selectedLayerIndex = selectedLayerIndex,
                onSelectedLayerChange = { selectedLayerIndex = it },
                onExportDoc = { doc ->
                    scope.launch {
                        isExporting = true
                        val res = NxprcExporter.exportToFile(doc)
                        res.fold(
                            onSuccess = { file ->
                                exportStatus = "Saved: ${file.name} (${file.length()} bytes • ${doc.canvas.layers.size} layers)"
                            },
                            onFailure = { ex ->
                                exportStatus = ex.message ?: "Export failed"
                            }
                        )
                        isExporting = false
                    }
                },
                onPushAdbDoc = { doc ->
                    scope.launch {
                        isExporting = true
                        exportStatus = "Pushing ${doc.canvas.layers.size} layers to phone via ${activeTransport.displayName}..."
                        val res = UniversalPushManager.pushComponent(doc)
                        res.fold(
                            onSuccess = { msg ->
                                exportStatus = msg
                            },
                            onFailure = { ex ->
                                exportStatus = "Push Error: ${ex.message}"
                            }
                        )
                        isExporting = false
                    }
                },
                isPushEnabled = activeTransport != ActiveTransport.NONE,
                pushLabel = if (activeTransport != ActiveTransport.NONE) "Push via ${activeTransport.displayName} (${compiledDoc.canvas.layers.size} L)" else "No Phone Connected",
                onFeedback = { promptCopiedBanner = it },
                onClose = { showFullScreenLayerStudio = false }
            )
        }
    }
}

/**
 * Left Component Editor Pane with adaptive header, category carousels, and monospace code editor.
 */
@Composable
private fun ComponentEditorPane(
    modifier: Modifier = Modifier,
    isNarrow: Boolean,
    isShort: Boolean,
    defaultControl: String,
    category: String,
    targetWidthDp: Int,
    targetHeightDp: Int,
    promptCopiedBanner: String?,
    onCopyAiPrompt: () -> Unit,
    onOpenPromptModal: () -> Unit,
    onLoadStarter: () -> Unit,
    selectedCategory: String,
    onSelectCategory: (String) -> Unit,
    categories: List<Pair<String, String>>,
    buttonsByCategory: Map<String, List<SubCategoryDefinition>>,
    selectedButtonKey: String,
    onSelectButton: (SubCategoryDefinition) -> Unit,
    htmlSource: String,
    onHtmlSourceChange: (String) -> Unit
) {
    Column(
        modifier = modifier
            .glassCard()
            .padding(if (isShort) 8.dp else 12.dp),
        verticalArrangement = Arrangement.spacedBy(if (isShort) 6.dp else 8.dp)
    ) {
        // 1. Header & AI Instructor Actions (Responsive to column width)
        if (isNarrow) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Column {
                    Text(
                        "🎮 Controller Component Studio",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        "Target: $defaultControl ($category • ${targetWidthDp}x${targetHeightDp}dp)",
                        color = NeonPalette.Cyan.copy(alpha = 0.85f),
                        fontSize = 10.5.sp
                    )
                }

                // Dedicated Sub-row for Action Buttons — NEVER squished into vertical letters!
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = onCopyAiPrompt,
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 3.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED)),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Text("🤖 Copy AI Prompt", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }

                    OutlinedButton(
                        onClick = onOpenPromptModal,
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 3.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Text("👁️ View", fontSize = 10.5.sp, color = NeonPalette.Cyan)
                    }

                    OutlinedButton(
                        onClick = onLoadStarter,
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 3.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, NeonPalette.Cyan.copy(alpha = 0.5f)),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Text("⚡ Starter", fontSize = 10.5.sp, color = NeonPalette.Cyan)
                    }
                }
            }
        } else {
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
                    Text(
                        "Category is mapping metadata; your HTML/CSS owns the shape.",
                        color = Color.White.copy(alpha = 0.58f),
                        fontSize = 10.sp
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = onCopyAiPrompt,
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED))
                    ) {
                        Text("🤖 Copy AI Prompt", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }

                    OutlinedButton(
                        onClick = onOpenPromptModal,
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("👁️ View Prompt", fontSize = 11.sp, color = NeonPalette.Cyan)
                    }

                    OutlinedButton(
                        onClick = onLoadStarter,
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, NeonPalette.Cyan.copy(alpha = 0.5f))
                    ) {
                        Text("⚡ Load Starter Code", fontSize = 11.sp, color = NeonPalette.Cyan)
                    }
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
                        promptCopiedBanner,
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
                    onClick = { onSelectCategory(catKey) },
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSelected) NeonPalette.Cyan else Color(0xFF131A2A)
                    ),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(28.dp)
                ) {
                    Text(
                        text = catLabel,
                        color = if (isSelected) Color.Black else Color.White.copy(alpha = 0.8f),
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 11.sp
                    )
                }
            }
        }

        // 4. Button Key Selector Carousel
        val currentButtons = buttonsByCategory[selectedCategory] ?: emptyList()
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            currentButtons.forEach { btn ->
                val isSelected = selectedButtonKey == btn.key
                OutlinedButton(
                    onClick = { onSelectButton(btn) },
                    shape = RoundedCornerShape(6.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        width = if (isSelected) 1.5.dp else 1.dp,
                        color = if (isSelected) btn.accentColor else Color(0xFF222F46)
                    ),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (isSelected) btn.accentColor.copy(alpha = 0.15f) else Color(0xFF0E1320)
                    ),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(28.dp)
                ) {
                    Text(
                        text = if (btn.emoji.isNotBlank()) "${btn.emoji} ${btn.label}" else btn.label,
                        color = if (isSelected) btn.accentColor else Color.White.copy(alpha = 0.7f),
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 11.sp
                    )
                }
            }
        }

        // 5. Editor Header (Clean & Uncluttered)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("📑 HTML / CSS Source", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
            }
            Text("Native GPU Skia Compiler", color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp)
        }

        // 6. Monospace Code Editor
        OutlinedTextField(
            value = htmlSource,
            onValueChange = onHtmlSourceChange,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .defaultMinSize(minHeight = 160.dp),
            textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace, fontSize = 12.sp),
            placeholder = { Text("Paste your AI-generated HTML / CSS code here...") }
        )
    }
}

/**
 * Right Live Sandbox Pane with auto-fit vector preview, live animation stats, and device push actions.
 */
@Composable
private fun LiveSandboxPane(
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
                Text("Live Sandbox & Export", color = Color.White, fontWeight = FontWeight.Bold, fontSize = if (isNarrow) 13.5.sp else 15.sp, maxLines = 1)
                if (!isShort) {
                    Text("Tap to test tactile physics", color = NeonPalette.Cyan.copy(alpha = 0.7f), fontSize = 10.sp)
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(5.dp), verticalAlignment = Alignment.CenterVertically) {
                Button(
                    onClick = onOpenLayerStudio,
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = if (isNarrow) 6.dp else 10.dp, vertical = 3.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED)),
                    modifier = Modifier.height(28.dp)
                ) {
                    Text(if (isNarrow) "🎛️ Layers" else "🎛️ Layer Studio", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 10.5.sp)
                }

                Button(
                    onClick = onOpenFullAudit,
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = if (isNarrow) 6.dp else 10.dp, vertical = 3.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F, 0x24, 0x36)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NeonPalette.Cyan),
                    modifier = Modifier.height(28.dp)
                ) {
                    Text(if (isNarrow) "🔍 Audit" else "🔍 Full Audit", color = NeonPalette.Cyan, fontWeight = FontWeight.Bold, fontSize = 10.5.sp)
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

            val aspect = targetWidthDp.toFloat() / targetHeightDp.toFloat()
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
        val isStick = category.equals("JOYSTICK", ignoreCase = true) || defaultControl.uppercase() in listOf("LS", "RS")
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
                Text(
                    "Stick: X:$xStr Y:$yStr",
                    color = if (dx != 0f || dy != 0f) Color(0xFF10B981) else Color.White.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.5.sp
                )
            } else {
                Text("Idle: ${compiledDoc.animations.idleType}", color = Color.White.copy(alpha = 0.8f), fontSize = 10.5.sp)
            }
            Text("Touch: ${if (isStick) "360° Analog" else compiledDoc.animations.pressFeedback}", color = Color.White.copy(alpha = 0.6f), fontSize = 10.sp)
        }

        // Compact Glass Metadata Strip (Responsive against line wrapping)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF090E18))
                .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(8.dp))
                .padding(horizontal = 8.dp, vertical = 5.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f, fill = false)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFF131D30))
                        .border(1.dp, NeonPalette.Cyan.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 5.dp, vertical = 2.dp)
                ) {
                    Text(text = componentId, color = NeonPalette.Cyan, fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                }
                Text(
                    text = componentName,
                    color = Color.White,
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Key: $defaultControl", color = Color(0xFF4ADE80), fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                Text(text = "•", color = Color.White.copy(alpha = 0.3f), fontSize = 10.sp)
                Text(text = category, color = Color.White.copy(alpha = 0.7f), fontSize = 10.sp)
            }
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
                    containerColor = if (exportStatus.contains("Error") || exportStatus.contains("canceled")) Color(0xFF3B121A) else Color(0xFF0F2C24)
                ),
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(
                    text = exportStatus,
                    color = if (exportStatus.contains("Error") || exportStatus.contains("canceled")) Color(0xFFFF6B6B) else Color(0xFF00FF99),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
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
                    .height(if (isShort) 36.dp else 40.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NeonPalette.Cyan),
                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Icon(Icons.Default.Download, contentDescription = null, tint = Color.Black, modifier = Modifier.size(15.dp))
                Spacer(Modifier.width(4.dp))
                Text(
                    text = exportText,
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    maxLines = 1
                )
            }

            // Push via Active Transport Button (Enabled only when connected)
            Button(
                onClick = onPush,
                enabled = !isExporting && isConnected,
                modifier = Modifier
                    .weight(1f)
                    .height(if (isShort) 36.dp else 40.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF00FF99),
                    disabledContainerColor = Color(0xFF1E293B)
                ),
                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Icon(
                    Icons.Default.Send,
                    contentDescription = null,
                    tint = if (isConnected) Color.Black else Color.White.copy(alpha = 0.35f),
                    modifier = Modifier.size(15.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = pushText,
                    color = if (isConnected) Color.Black else Color.White.copy(alpha = 0.35f),
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    maxLines = 1
                )
            }
        }
    }
}
