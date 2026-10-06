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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sanket.tools.nexpad.category.CategoryType
import com.sanket.tools.nexpad.category.ControlKey
import com.sanket.tools.nexpad.nxprc.NxprcDocument
import com.sanket.tools.nexpad.model.NexpadKeys
import com.sanket.tools.nexpaddesktop.connection.ActiveTransport
import com.sanket.tools.nexpaddesktop.plugins.DesktopPluginManager
import com.sanket.tools.nexpaddesktop.plugins.NxprcComponentDetector
import com.sanket.tools.nexpaddesktop.plugins.NxprcExporter
import com.sanket.tools.nexpaddesktop.plugins.NxprcHtmlCssConverter
import com.sanket.tools.nexpaddesktop.plugins.NxprcSeedEngine
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
import com.sanket.tools.nexpaddesktop.viewmodel.DesktopViewModel
import com.sanket.tools.nexpaddesktop.plugins.AiDesignOptions
import com.sanket.tools.nexpaddesktop.plugins.ModelCapability

val SubCategoryDefinition.accentColor: Color get() = Color(accentColorArgb)

/**
 * Prompt tier for AI model targeting. Controls the level of detail in generated prompts.
 */
private enum class PromptTier(
    val icon: String,
    val displayName: String,
    val description: String,
    val modelCapability: ModelCapability
) {
    COMPACT("⚡", "Compact", "Simple • Clean • Small/Local LLMs", ModelCapability.COMPACT),
    STANDARD("🎮", "Standard", "Auto • Balanced • GPT-4o, Claude Sonnet", ModelCapability.STANDARD),
    FRONTIER("🚀", "Frontier", "Unlimited • Dense • Claude 3.7, o1/o3", ModelCapability.FRONTIER)
}

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
    viewModel: DesktopViewModel
) {
    PluginsScreen(activeTransport = viewModel.activeTransport)
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

    val defaultCtrl = ControlKey.A
    val initialDetected = remember {
        NxprcComponentDetector.detect(
            html = NxprcHtmlCssConverter.PRESET_NEO_TACTILE_A,
            fallbackCategory = defaultCtrl.componentType.name,
            fallbackControl = defaultCtrl.key,
            fallbackId = defaultCtrl.defaultId,
            fallbackName = defaultCtrl.defaultName,
            fallbackWidthDp = defaultCtrl.defaultWidthDp,
            fallbackHeightDp = defaultCtrl.defaultHeightDp
        )
    }
    var currentSeed by remember { mutableStateOf(initialDetected.seed ?: NxprcSeedEngine.canonicalSeedFor(defaultCtrl.key)) }
    var htmlCodeSeed by remember { mutableStateOf(initialDetected.seed ?: currentSeed) }
    val initialSeedProfile = remember(currentSeed) { NxprcSeedEngine.resolve(defaultCtrl.key, defaultCtrl.componentType.name, currentSeed) }
    var selectedCategory by remember { mutableStateOf(defaultCtrl.categoryType.id) }
    var selectedButtonKey by remember { mutableStateOf(defaultCtrl.key) }
    var htmlSource by remember { mutableStateOf(NxprcHtmlCssConverter.PRESET_NEO_TACTILE_A) }
    var componentId by remember { mutableStateOf(if (initialDetected.componentId.isNotBlank()) initialDetected.componentId else initialSeedProfile.componentId) }
    var componentName by remember { mutableStateOf(if (initialDetected.componentName.isNotBlank()) initialDetected.componentName else initialSeedProfile.skinName) }
    var category by remember { mutableStateOf(defaultCtrl.componentType.name) }
    var defaultControl by remember { mutableStateOf(defaultCtrl.key) }
    var targetWidthDp by remember { mutableStateOf(defaultCtrl.defaultWidthDp) }
    var targetHeightDp by remember { mutableStateOf(defaultCtrl.defaultHeightDp) }

    var exportStatus by remember { mutableStateOf<String?>(null) }
    var isExporting by remember { mutableStateOf(false) }

    var showAiPromptModal by remember { mutableStateOf(false) }
    var selectedPromptTier by remember { mutableStateOf(PromptTier.STANDARD) }
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
                id = defaultCtrl.defaultId,
                name = defaultCtrl.defaultName,
                category = defaultCtrl.componentType.name,
                defaultControl = defaultCtrl.key
            )
        )
    }
    var compileError by remember { mutableStateOf<String?>(null) }

    // Layer Studio & Layer Manager States - Preserved across surgical modifications
    var activeLayerIndices by remember { mutableStateOf((0 until compiledDoc.canvas.layers.size).toSet()) }
    var soloLayerIndex by remember { mutableStateOf<Int?>(null) }
    var selectedLayerIndex by remember { mutableStateOf(0) }

    // Helper: Reset layer state so that ALL layers are 100% active and enabled
    val resetLayersToAllEnabled: (Int) -> Unit = { totalLayers ->
        activeLayerIndices = (0 until totalLayers).toSet()
        soloLayerIndex = null
        selectedLayerIndex = 0
    }

    // Keep indices in sync when document changes — always ensure all layers enabled in main studio
    LaunchedEffect(compiledDoc) {
        val total = compiledDoc.canvas.layers.size
        if (total > 0) {
            selectedLayerIndex = selectedLayerIndex.coerceIn(0, total - 1)
            if (!showFullScreenLayerStudio) {
                // In main studio workspace, all layers are always 100% active and enabled
                resetLayersToAllEnabled(total)
            } else {
                // Inside full-screen layer studio, keep existing selection valid and include any new layers
                val validIndices = activeLayerIndices.filter { it < total }.toSet()
                activeLayerIndices = if (validIndices.isEmpty()) (0 until total).toSet() else validIndices
                if (soloLayerIndex != null && soloLayerIndex!! >= total) {
                    soloLayerIndex = null
                }
            }
        }
    }

    LaunchedEffect(htmlSource, currentSeed) {
        delay(250) // Debounce keystrokes / rapid seed typing
        withContext(Dispatchers.Default) {
            val detected = NxprcComponentDetector.detect(
                html = htmlSource,
                fallbackCategory = category,
                fallbackControl = defaultControl,
                fallbackId = componentId,
                fallbackName = componentName,
                fallbackWidthDp = targetWidthDp,
                fallbackHeightDp = targetHeightDp
            )

            // When HTML code in editor has an explicit seed that changed, sync code seed & sandbox seed
            val detectedSeed = detected.seed
            if (detectedSeed != null && detectedSeed != htmlCodeSeed) {
                htmlCodeSeed = detectedSeed
                currentSeed = detectedSeed
            }

            // If sandbox preview seed differs from HTML code's seed, compile preview with currentSeed applied dynamically
            // (Preserves original htmlSource in code editor intact!)
            val effectiveSource = if (currentSeed != htmlCodeSeed) {
                NxprcSeedEngine.applySeedToHtml(
                    currentHtml = htmlSource,
                    control = detected.defaultControl.ifBlank { defaultControl },
                    category = detected.category.ifBlank { category },
                    seed = currentSeed
                )
            } else {
                htmlSource
            }

            try {
                val previewDetected = if (effectiveSource !== htmlSource) {
                    NxprcComponentDetector.detect(
                        html = effectiveSource,
                        fallbackCategory = category,
                        fallbackControl = defaultControl,
                        fallbackId = componentId,
                        fallbackName = componentName,
                        fallbackWidthDp = targetWidthDp,
                        fallbackHeightDp = targetHeightDp
                    )
                } else detected

                val doc = NxprcHtmlCssConverter.convert(
                    source = effectiveSource,
                    id = previewDetected.componentId,
                    name = previewDetected.componentName,
                    category = previewDetected.category,
                    defaultControl = previewDetected.defaultControl
                )
                compiledDoc = doc
                compileError = null
                if (previewDetected.isExplicitlyDefined && previewDetected.componentName.isNotBlank() && previewDetected.componentName != componentName) {
                    componentName = previewDetected.componentName
                    componentId = previewDetected.componentId
                }
            } catch (e: Exception) {
                compileError = e.message ?: "Compilation error"
            }
        }
    }

    val handleSetSeed: (Long) -> Unit = { newSeed ->
        currentSeed = newSeed
        val profile = NxprcSeedEngine.resolve(defaultControl, category, newSeed, componentName)
        componentName = profile.skinName
        componentId = profile.componentId
        promptCopiedBanner = "🎲 Seed #${newSeed} previewing in Live Sandbox (${profile.palette.name})"
    }

    val handleRerollSeed: () -> Unit = {
        var newSeed = NxprcSeedEngine.randomSeed()
        val currentProfile = NxprcSeedEngine.resolve(defaultControl, category, currentSeed)
        var newProfile = NxprcSeedEngine.resolve(defaultControl, category, newSeed)
        var attempts = 0
        while (newProfile.palette.hexCode == currentProfile.palette.hexCode && attempts < 50) {
            newSeed = NxprcSeedEngine.randomSeed()
            newProfile = NxprcSeedEngine.resolve(defaultControl, category, newSeed)
            attempts++
        }
        handleSetSeed(newSeed)
    }

    val handleResetToCodeSeed: () -> Unit = {
        val detected = NxprcComponentDetector.detect(
            html = htmlSource,
            fallbackCategory = category,
            fallbackControl = defaultControl,
            fallbackId = componentId,
            fallbackName = componentName
        )
        val codeSeed = detected.seed ?: htmlCodeSeed
        currentSeed = codeSeed
        htmlCodeSeed = codeSeed
        val profile = NxprcSeedEngine.resolve(defaultControl, category, codeSeed, detected.componentName.ifBlank { componentName })
        componentName = profile.skinName
        componentId = profile.componentId
        promptCopiedBanner = "↺ Reset to HTML code seed #${codeSeed} (${profile.palette.name})"
    }

    val getLiveExportDoc: () -> NxprcDocument = {
        val currentEffectiveSource = if (currentSeed != htmlCodeSeed) {
            NxprcSeedEngine.applySeedToHtml(
                currentHtml = htmlSource,
                control = defaultControl,
                category = category,
                seed = currentSeed
            )
        } else {
            htmlSource
        }
        val detected = NxprcComponentDetector.detect(
            html = currentEffectiveSource,
            fallbackCategory = category,
            fallbackControl = defaultControl,
            fallbackId = componentId,
            fallbackName = componentName,
            fallbackWidthDp = targetWidthDp,
            fallbackHeightDp = targetHeightDp
        )
        val baseDoc = try {
            NxprcHtmlCssConverter.convert(
                source = currentEffectiveSource,
                id = detected.componentId,
                name = detected.componentName,
                category = detected.category,
                defaultControl = detected.defaultControl
            )
        } catch (e: Exception) {
            compiledDoc
        }
        baseDoc.copy(
            manifest = baseDoc.manifest.copy(
                id = componentId.ifBlank { baseDoc.manifest.id },
                name = componentName.ifBlank { baseDoc.manifest.name }
            )
        )
    }

    val handleSelectButton: (SubCategoryDefinition) -> Unit = { btn ->
        selectedButtonKey = btn.key
        defaultControl = btn.key
        val btnCat = if (btn.categoryType == CategoryType.MACROS) "MACRO" else btn.componentType.name
        category = btnCat
        targetWidthDp = btn.defaultWidthDp
        targetHeightDp = btn.defaultHeightDp

        val baseSource = btn.starterHtmlPreset ?: NxprcHtmlCssConverter.getReferenceTemplate(btn.key, btn.categoryType.id)
        val detected = NxprcComponentDetector.detect(
            html = baseSource,
            fallbackCategory = btnCat,
            fallbackControl = btn.key,
            fallbackId = btn.defaultId,
            fallbackName = btn.defaultName,
            fallbackWidthDp = btn.defaultWidthDp,
            fallbackHeightDp = btn.defaultHeightDp
        )

        val buttonSeed = detected.seed ?: NxprcSeedEngine.canonicalSeedFor(btn.key)
        currentSeed = buttonSeed
        htmlCodeSeed = buttonSeed

        val profile = NxprcSeedEngine.resolve(btn.key, btnCat, buttonSeed, detected.componentName.ifBlank { btn.defaultName })
        componentId = if (detected.componentId.isNotBlank()) detected.componentId else profile.componentId
        componentName = if (detected.componentName.isNotBlank()) detected.componentName else profile.skinName

        htmlSource = baseSource

        try {
            val newDoc = NxprcHtmlCssConverter.convert(
                source = baseSource,
                id = componentId,
                name = componentName,
                category = btnCat,
                defaultControl = btn.key
            )
            compiledDoc = newDoc
            compileError = null
            resetLayersToAllEnabled(newDoc.canvas.layers.size)
        } catch (e: Exception) {
            compileError = e.message ?: "Compilation error"
        }
    }

    val handleSelectCategory: (String) -> Unit = { catKey ->
        selectedCategory = catKey
        val firstButton = buttonsByCategory[catKey]?.firstOrNull()
        if (firstButton != null) {
            handleSelectButton(firstButton)
        }
    }

    val handleLoadStarter: () -> Unit = {
        val base = NxprcHtmlCssConverter.getReferenceTemplate(defaultControl, category)
        val detected = NxprcComponentDetector.detect(
            html = base,
            fallbackCategory = category,
            fallbackControl = defaultControl,
            fallbackId = componentId,
            fallbackName = componentName,
            fallbackWidthDp = targetWidthDp,
            fallbackHeightDp = targetHeightDp
        )
        val starterSeed = detected.seed ?: NxprcSeedEngine.canonicalSeedFor(defaultControl)
        currentSeed = starterSeed
        htmlCodeSeed = starterSeed

        val profile = NxprcSeedEngine.resolve(defaultControl, category, starterSeed, detected.componentName.ifBlank { componentName })
        componentId = if (detected.componentId.isNotBlank()) detected.componentId else profile.componentId
        componentName = if (detected.componentName.isNotBlank()) detected.componentName else profile.skinName

        htmlSource = base
        promptCopiedBanner = "✓ Loaded starter template for $defaultControl ($category) [Seed #$starterSeed]!"
        try {
            val newDoc = NxprcHtmlCssConverter.convert(
                source = base,
                id = componentId,
                name = componentName,
                category = category,
                defaultControl = defaultControl
            )
            compiledDoc = newDoc
            compileError = null
            resetLayersToAllEnabled(newDoc.canvas.layers.size)
        } catch (e: Exception) {
            compileError = e.message ?: "Compilation error"
        }
    }

    val handleOpenLayerStudio: () -> Unit = {
        resetLayersToAllEnabled(compiledDoc.canvas.layers.size)
        showFullScreenLayerStudio = true
    }

    val handleCloseLayerStudio: () -> Unit = {
        resetLayersToAllEnabled(compiledDoc.canvas.layers.size)
        showFullScreenLayerStudio = false
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
                    selectedPromptTier = selectedPromptTier,
                    onSelectPromptTier = { selectedPromptTier = it },
                    onCopyAiPrompt = {
                        val prompt = NxprcHtmlCssConverter.generateAiPrompt(
                            control = defaultControl,
                            category = category,
                            widthDp = targetWidthDp,
                            heightDp = targetHeightDp,
                            options = AiDesignOptions(
                                modelCapability = selectedPromptTier.modelCapability,
                                seed = currentSeed,
                                componentName = componentName
                            )
                        )
                        val ok = safeCopyToClipboard(prompt)
                        promptCopiedBanner = if (ok) "✓ ${selectedPromptTier.icon} ${selectedPromptTier.displayName} AI Prompt for $defaultControl ($category) [Seed #$currentSeed] copied!" else "⚠️ Clipboard busy — please try again"
                    },
                    onOpenPromptModal = { showAiPromptModal = true },
                    onLoadStarter = handleLoadStarter,
                    selectedCategory = selectedCategory,
                    onSelectCategory = handleSelectCategory,
                    categories = categories,
                    buttonsByCategory = buttonsByCategory,
                    selectedButtonKey = selectedButtonKey,
                    onSelectButton = handleSelectButton,
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
                    currentSeed = currentSeed,
                    htmlCodeSeed = htmlCodeSeed,
                    onSeedChange = handleSetSeed,
                    onRerollSeed = handleRerollSeed,
                    onResetSeed = handleResetToCodeSeed,
                    compileError = compileError,
                    exportStatus = exportStatus,
                    isExporting = isExporting,
                    onOpenLayerStudio = handleOpenLayerStudio,
                    onOpenFullAudit = { showFullAuditPreview = true },
                    onExport = {
                        scope.launch {
                            isExporting = true
                            val exportDoc = getLiveExportDoc()
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
                            val exportDoc = getLiveExportDoc()
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
                            selectedPromptTier = selectedPromptTier,
                            onSelectPromptTier = { selectedPromptTier = it },
                            onCopyAiPrompt = {
                                val prompt = NxprcHtmlCssConverter.generateAiPrompt(
                                    control = defaultControl,
                                    category = category,
                                    widthDp = targetWidthDp,
                                    heightDp = targetHeightDp,
                                    options = AiDesignOptions(
                                        modelCapability = selectedPromptTier.modelCapability,
                                        seed = currentSeed,
                                        componentName = componentName
                                    )
                                )
                                val ok = safeCopyToClipboard(prompt)
                                promptCopiedBanner = if (ok) "✓ ${selectedPromptTier.icon} ${selectedPromptTier.displayName} AI Prompt for $defaultControl ($category) [Seed #$currentSeed] copied!" else "⚠️ Clipboard busy — please try again"
                            },
                            onOpenPromptModal = { showAiPromptModal = true },
                            onLoadStarter = handleLoadStarter,
                            selectedCategory = selectedCategory,
                            onSelectCategory = handleSelectCategory,
                            categories = categories,
                            buttonsByCategory = buttonsByCategory,
                            selectedButtonKey = selectedButtonKey,
                            onSelectButton = handleSelectButton,
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
                            currentSeed = currentSeed,
                            htmlCodeSeed = htmlCodeSeed,
                            onSeedChange = handleSetSeed,
                            onRerollSeed = handleRerollSeed,
                            onResetSeed = handleResetToCodeSeed,
                            compileError = compileError,
                            exportStatus = exportStatus,
                            isExporting = isExporting,
                            onOpenLayerStudio = handleOpenLayerStudio,
                            onOpenFullAudit = { showFullAuditPreview = true },
                            onExport = {
                                scope.launch {
                                    isExporting = true
                                    val exportDoc = getLiveExportDoc()
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
                                    val exportDoc = getLiveExportDoc()
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
            var modalTier by remember { mutableStateOf(selectedPromptTier) }
            val generatedPrompt = remember(defaultControl, category, targetWidthDp, targetHeightDp, modalTier, currentSeed, componentName) {
                NxprcHtmlCssConverter.generateAiPrompt(
                    control = defaultControl,
                    category = category,
                    widthDp = targetWidthDp,
                    heightDp = targetHeightDp,
                    options = AiDesignOptions(
                        modelCapability = modalTier.modelCapability,
                        seed = currentSeed,
                        componentName = componentName
                    )
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
                                    "${modalTier.icon} ${modalTier.displayName} tier (${if (modalTier == PromptTier.FRONTIER) "Unlimited" else modalTier.modelCapability.defaultComplexity.name.lowercase().replaceFirstChar { it.uppercase() }} • ${modalTier.modelCapability.defaultVisualDensity.name.lowercase().replaceFirstChar { it.uppercase() }}) • Compatible with: ${when(modalTier) {
                                        PromptTier.COMPACT -> "Gemma, Llama 3.2, DeepSeek R1-Distill, Haiku"
                                        PromptTier.STANDARD -> "GPT-4o, Claude 3.5 Sonnet, Gemini 1.5 Pro"
                                        PromptTier.FRONTIER -> "Claude 3.7 Opus, o1/o3, GPT-4.5, Gemini 2.0 Pro"
                                    }}",
                                    color = Color(0xFFC4B5FD),
                                    fontSize = 12.sp
                                )
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = {
                                        val ok = safeCopyToClipboard(generatedPrompt)
                                        promptCopiedBanner = if (ok) "✓ ${modalTier.icon} ${modalTier.displayName} AI Prompt for $defaultControl copied to clipboard!" else "⚠️ Clipboard busy — please try again"
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

                        // Tier Tab Switcher
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF090E18))
                                .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(8.dp))
                                .padding(3.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            PromptTier.entries.forEach { tier ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(30.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (modalTier == tier) Color(0xFF7C3AED) else Color.Transparent)
                                        .clickable { modalTier = tier },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${tier.icon} ${tier.displayName}",
                                        color = if (modalTier == tier) Color.White else Color.White.copy(alpha = 0.6f),
                                        fontWeight = if (modalTier == tier) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 11.5.sp
                                    )
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
                    handleOpenLayerStudio()
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
                onSaveHtml = { newHtml ->
                    htmlSource = newHtml
                    try {
                        val immediateDoc = NxprcHtmlCssConverter.convert(
                            source = newHtml,
                            id = componentId,
                            name = componentName,
                            category = category,
                            defaultControl = defaultControl
                        )
                        compiledDoc = immediateDoc
                        compileError = null
                    } catch (e: Exception) {
                        compileError = e.message ?: "Compilation error"
                    }
                },
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
                pushLabel = if (activeTransport != ActiveTransport.NONE) "Push via ${activeTransport.displayName}" else "No Phone Connected",
                onFeedback = { promptCopiedBanner = it },
                onClose = handleCloseLayerStudio
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
    selectedPromptTier: PromptTier,
    onSelectPromptTier: (PromptTier) -> Unit,
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
                    // SplitButton: Copy AI Prompt with tier dropdown
                    Box {
                        var showTierMenu by remember { mutableStateOf(false) }
                        Row(
                            modifier = Modifier
                                .height(28.dp)
                                .clip(RoundedCornerShape(6.dp))
                        ) {
                            // Main copy action
                            Button(
                                onClick = onCopyAiPrompt,
                                shape = RoundedCornerShape(topStart = 6.dp, bottomStart = 6.dp, topEnd = 0.dp, bottomEnd = 0.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 3.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED)),
                                modifier = Modifier.height(28.dp)
                            ) {
                                Text("${selectedPromptTier.icon} Copy AI Prompt", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                            // Dropdown chevron
                            Button(
                                onClick = { showTierMenu = true },
                                shape = RoundedCornerShape(topStart = 0.dp, bottomStart = 0.dp, topEnd = 6.dp, bottomEnd = 6.dp),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 3.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6D28D9)),
                                modifier = Modifier.height(28.dp)
                            ) {
                                Text("▼", fontSize = 9.sp, color = Color.White)
                            }
                        }
                        DropdownMenu(
                            expanded = showTierMenu,
                            onDismissRequest = { showTierMenu = false }
                        ) {
                            PromptTier.entries.forEach { tier ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(
                                                "${tier.icon} ${tier.displayName}${if (tier == selectedPromptTier) " ✓" else ""}",
                                                fontWeight = if (tier == selectedPromptTier) FontWeight.Bold else FontWeight.Normal,
                                                fontSize = 13.sp
                                            )
                                            Text(
                                                tier.description,
                                                fontSize = 10.5.sp,
                                                color = Color.Gray
                                            )
                                        }
                                    },
                                    onClick = {
                                        onSelectPromptTier(tier)
                                        showTierMenu = false
                                    }
                                )
                            }
                        }
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
                    // SplitButton: Copy AI Prompt with tier dropdown
                    Box {
                        var showTierMenu by remember { mutableStateOf(false) }
                        Row(
                            modifier = Modifier.clip(RoundedCornerShape(6.dp))
                        ) {
                            // Main copy action
                            Button(
                                onClick = onCopyAiPrompt,
                                shape = RoundedCornerShape(topStart = 6.dp, bottomStart = 6.dp, topEnd = 0.dp, bottomEnd = 0.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED))
                            ) {
                                Text("${selectedPromptTier.icon} Copy AI Prompt", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                            // Dropdown chevron
                            Button(
                                onClick = { showTierMenu = true },
                                shape = RoundedCornerShape(topStart = 0.dp, bottomStart = 0.dp, topEnd = 6.dp, bottomEnd = 6.dp),
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6D28D9))
                            ) {
                                Text("▼", fontSize = 9.5.sp, color = Color.White)
                            }
                        }
                        DropdownMenu(
                            expanded = showTierMenu,
                            onDismissRequest = { showTierMenu = false }
                        ) {
                            PromptTier.entries.forEach { tier ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(
                                                "${tier.icon} ${tier.displayName}${if (tier == selectedPromptTier) " ✓" else ""}",
                                                fontWeight = if (tier == selectedPromptTier) FontWeight.Bold else FontWeight.Normal,
                                                fontSize = 13.sp
                                            )
                                            Text(
                                                tier.description,
                                                fontSize = 10.5.sp,
                                                color = Color.Gray
                                            )
                                        }
                                    },
                                    onClick = {
                                        onSelectPromptTier(tier)
                                        showTierMenu = false
                                    }
                                )
                            }
                        }
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

        // 7. Monospace Code Editor
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
        val isStick = effCategory.equals("JOYSTICK", ignoreCase = true) ||
                effCategory.equals("TOUCHPAD", ignoreCase = true) ||
                effControl.uppercase() in listOf(
                    com.sanket.tools.nexpad.category.ControlKey.LS.key,
                    com.sanket.tools.nexpad.category.ControlKey.RS.key,
                    com.sanket.tools.nexpad.category.ControlKey.LTP.key,
                    com.sanket.tools.nexpad.category.ControlKey.RTP.key
                )
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
                val isRightStick = effControl.uppercase() in listOf(
                    com.sanket.tools.nexpad.category.ControlKey.RS.key,
                    com.sanket.tools.nexpad.category.ControlKey.RTP.key
                ) || compiledDoc.manifest.id.contains("rtp", ignoreCase = true)
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
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0D1527)),
            shape = RoundedCornerShape(8.dp),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (htmlCodeSeed != null && currentSeed != htmlCodeSeed) Color(0xFFF59E0B).copy(alpha = 0.6f) else Color(0xFF1E293B)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = if (isShort) 5.dp else 7.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Seed Input Area: Clear label + obvious editable text field with '#' prefix and pencil '✏️'
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "🎲 Seed:",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.5.sp
                        )

                        var seedInputText by remember(currentSeed) { mutableStateOf(currentSeed.toString()) }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF060B14))
                                .border(1.2.dp, NeonPalette.Cyan.copy(alpha = 0.75f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 7.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "#",
                                color = NeonPalette.Cyan,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(Modifier.width(2.dp))
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
                                    color = Color.White,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                ),
                                singleLine = true,
                                cursorBrush = SolidColor(NeonPalette.Cyan),
                                modifier = Modifier
                                    .width(IntrinsicSize.Min)
                                    .widthIn(min = 50.dp, max = 85.dp)
                            )
                            Spacer(Modifier.width(3.dp))
                            Text(
                                text = "✏️",
                                fontSize = 9.sp,
                                color = Color.White.copy(alpha = 0.5f)
                            )
                        }
                    }

                    // Action Controls: Reroll, Reset, Apply
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 🎲 Reroll Button
                        OutlinedButton(
                            onClick = onRerollSeed,
                            shape = RoundedCornerShape(6.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF59E0B)),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = Color(0xFFF59E0B).copy(alpha = 0.15f)
                            ),
                            contentPadding = PaddingValues(horizontal = if (isNarrow) 6.dp else 9.dp, vertical = 2.dp),
                            modifier = Modifier.height(26.dp)
                        ) {
                            Text(
                                text = "🎲 Reroll",
                                color = Color(0xFFFCD34D),
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.5.sp
                            )
                        }

                        // ↺ Reset Button: Reverts sandbox back to HTML code's seed
                        val isModifiedFromCode = htmlCodeSeed != null && currentSeed != htmlCodeSeed
                        OutlinedButton(
                            onClick = onResetSeed,
                            enabled = isModifiedFromCode,
                            shape = RoundedCornerShape(6.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isModifiedFromCode) Color(0xFF64748B) else Color(0xFF334155).copy(alpha = 0.4f)
                            ),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (isModifiedFromCode) Color(0xFF1E293B) else Color.Transparent,
                                disabledContainerColor = Color.Transparent
                            ),
                            contentPadding = PaddingValues(horizontal = if (isNarrow) 6.dp else 10.dp, vertical = 2.dp),
                            modifier = Modifier.height(26.dp)
                        ) {
                            Text(
                                text = "↺ Reset",
                                color = if (isModifiedFromCode) Color(0xFFE2E8F0) else Color(0xFF64748B),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 10.5.sp
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
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Text(
                        text = "Theme: ${profile.palette.name} (${profile.skinName})",
                        color = Color.White.copy(alpha = 0.65f),
                        fontSize = 10.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
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
                    Text(text = effId, color = NeonPalette.Cyan, fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                }
                Text(
                    text = effName,
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
                Text(text = "Key: $effControl", color = Color(0xFF4ADE80), fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                Text(text = "•", color = Color.White.copy(alpha = 0.3f), fontSize = 10.sp)
                Text(text = effCategory, color = Color.White.copy(alpha = 0.7f), fontSize = 10.sp)
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
