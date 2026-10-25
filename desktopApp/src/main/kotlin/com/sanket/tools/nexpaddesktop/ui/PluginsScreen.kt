@file:OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)

package com.sanket.tools.nexpaddesktop.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.rememberTextFieldVerticalScrollState
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.stateDescription
import com.sanket.tools.nexpaddesktop.ui.components.DesktopVerticalScrollbar
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sanket.tools.nexpad.category.CategoryType
import com.sanket.tools.nexpad.category.ControlKey
import com.sanket.tools.nexpaddesktop.model.ControllerTouchClassifier
import com.sanket.tools.nexpad.nxprc.NxprcDocument
import com.sanket.tools.nexpad.model.NexpadKeys
import com.sanket.tools.nexpaddesktop.connection.ActiveTransport
import com.sanket.tools.nexpaddesktop.plugins.DesktopPluginManager
import com.sanket.tools.nexpaddesktop.plugins.NxprcComponentDetector
import com.sanket.tools.nexpaddesktop.plugins.NxprcExporter
import com.sanket.tools.nexpaddesktop.plugins.NxprcHtmlCssConverter
import com.sanket.tools.nexpaddesktop.plugins.NxprcPresetStyle
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
import com.sanket.tools.nexpaddesktop.ui.plugins.*
import com.sanket.tools.nexpaddesktop.viewmodel.DesktopViewModel
import com.sanket.tools.nexpaddesktop.plugins.AiDesignOptions
import com.sanket.tools.nexpaddesktop.plugins.ModelCapability

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

    LaunchedEffect(activeTransport) {
        UniversalPushManager.currentTransport = activeTransport
    }

    val categories = remember {
        CategoryManager.getAllCategories().map { it.id to it.title }
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
    var selectedPresetStyle by remember { mutableStateOf(NxprcPresetStyle.XBOX_ELITE) }
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
    var lastCleanHtmlSource by remember { mutableStateOf(NxprcHtmlCssConverter.PRESET_NEO_TACTILE_A) }
    var pendingSwitchAction by remember { mutableStateOf<(() -> Unit)?>(null) }
    var showDiscardConfirmDialog by remember { mutableStateOf(false) }
    var undoHtmlBackup by remember { mutableStateOf<String?>(null) }

    val isCodeDirty = htmlSource.trim() != lastCleanHtmlSource.trim()

    val safeExecuteChange: (() -> Unit) -> Unit = { action ->
        if (isCodeDirty) {
            pendingSwitchAction = action
            showDiscardConfirmDialog = true
        } else {
            action()
        }
    }

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

    val handleSelectPresetStyle: (NxprcPresetStyle) -> Unit = { style ->
        selectedPresetStyle = style
        scope.launch {
            val base = withContext(Dispatchers.Default) {
                NxprcHtmlCssConverter.getReferenceTemplate(defaultControl, category, style)
            }
            val detected = withContext(Dispatchers.Default) {
                NxprcComponentDetector.detect(
                    html = base,
                    fallbackCategory = category,
                    fallbackControl = defaultControl,
                    fallbackId = componentId,
                    fallbackName = componentName,
                    fallbackWidthDp = targetWidthDp,
                    fallbackHeightDp = targetHeightDp
                )
            }
            val starterSeed = detected.seed ?: NxprcSeedEngine.canonicalSeedFor(defaultControl)
            currentSeed = starterSeed
            htmlCodeSeed = starterSeed

            val profile = NxprcSeedEngine.resolve(defaultControl, category, starterSeed, detected.componentName.ifBlank { componentName })
            val resolvedId = if (detected.componentId.isNotBlank()) detected.componentId else profile.componentId
            val resolvedName = if (detected.componentName.isNotBlank()) detected.componentName else profile.skinName
            componentId = resolvedId
            componentName = resolvedName

            undoHtmlBackup = htmlSource

            htmlSource = base

            lastCleanHtmlSource = base
            promptCopiedBanner = "✓ Applied ${style.displayName} style for $defaultControl ($category)!"
            try {
                val newDoc = withContext(Dispatchers.Default) {
                    NxprcHtmlCssConverter.convert(
                        source = base,
                        id = resolvedId,
                        name = resolvedName,
                        category = category,
                        defaultControl = defaultControl
                    )
                }
                compiledDoc = newDoc
                compileError = null
                resetLayersToAllEnabled(newDoc.canvas.layers.size)
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
            ),
            canvas = baseDoc.canvas.copy(
                layers = baseDoc.canvas.layers.filterIndexed { idx, _ -> idx in activeLayerIndices }
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

        scope.launch {
            val baseSource = btn.starterHtmlPreset ?: withContext(Dispatchers.Default) {
                NxprcHtmlCssConverter.getReferenceTemplate(btn.key, btn.categoryType.id, selectedPresetStyle)
            }
            val detected = withContext(Dispatchers.Default) {
                NxprcComponentDetector.detect(
                    html = baseSource,
                    fallbackCategory = btnCat,
                    fallbackControl = btn.key,
                    fallbackId = btn.defaultId,
                    fallbackName = btn.defaultName,
                    fallbackWidthDp = btn.defaultWidthDp,
                    fallbackHeightDp = btn.defaultHeightDp
                )
            }

            val buttonSeed = detected.seed ?: NxprcSeedEngine.canonicalSeedFor(btn.key)
            currentSeed = buttonSeed
            htmlCodeSeed = buttonSeed

            val profile = NxprcSeedEngine.resolve(btn.key, btnCat, buttonSeed, detected.componentName.ifBlank { btn.defaultName })
            val resolvedId = if (detected.componentId.isNotBlank()) detected.componentId else profile.componentId
            val resolvedName = if (detected.componentName.isNotBlank()) detected.componentName else profile.skinName
            componentId = resolvedId
            componentName = resolvedName

            undoHtmlBackup = htmlSource

            htmlSource = baseSource

            lastCleanHtmlSource = baseSource
            try {
                val newDoc = withContext(Dispatchers.Default) {
                    NxprcHtmlCssConverter.convert(
                        source = baseSource,
                        id = resolvedId,
                        name = resolvedName,
                        category = btnCat,
                        defaultControl = btn.key
                    )
                }
                compiledDoc = newDoc
                compileError = null
                resetLayersToAllEnabled(newDoc.canvas.layers.size)
            } catch (e: Exception) {
                compileError = e.message ?: "Compilation error"
            }
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
        scope.launch {
            val base = withContext(Dispatchers.Default) {
                NxprcHtmlCssConverter.getReferenceTemplate(defaultControl, category, selectedPresetStyle)
            }
            val detected = withContext(Dispatchers.Default) {
                NxprcComponentDetector.detect(
                    html = base,
                    fallbackCategory = category,
                    fallbackControl = defaultControl,
                    fallbackId = componentId,
                    fallbackName = componentName,
                    fallbackWidthDp = targetWidthDp,
                    fallbackHeightDp = targetHeightDp
                )
            }
            val starterSeed = detected.seed ?: NxprcSeedEngine.canonicalSeedFor(defaultControl)
            currentSeed = starterSeed
            htmlCodeSeed = starterSeed

            val profile = NxprcSeedEngine.resolve(defaultControl, category, starterSeed, detected.componentName.ifBlank { componentName })
            val resolvedId = if (detected.componentId.isNotBlank()) detected.componentId else profile.componentId
            val resolvedName = if (detected.componentName.isNotBlank()) detected.componentName else profile.skinName
            componentId = resolvedId
            componentName = resolvedName

            undoHtmlBackup = htmlSource

            htmlSource = base

            lastCleanHtmlSource = base
            promptCopiedBanner = "✓ Loaded starter template for $defaultControl ($category) [Seed #$starterSeed]!"
            try {
                val newDoc = withContext(Dispatchers.Default) {
                    NxprcHtmlCssConverter.convert(
                        source = base,
                        id = resolvedId,
                        name = resolvedName,
                        category = category,
                        defaultControl = defaultControl
                    )
                }
                compiledDoc = newDoc
                compileError = null
                resetLayersToAllEnabled(newDoc.canvas.layers.size)
            } catch (e: Exception) {
                compileError = e.message ?: "Compilation error"
            }
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
                    onLoadStarter = { safeExecuteChange { handleLoadStarter() } },
                    selectedCategory = selectedCategory,
                    onSelectCategory = { cat -> safeExecuteChange { handleSelectCategory(cat) } },
                    categories = categories,
                    buttonsByCategory = buttonsByCategory,
                    selectedButtonKey = selectedButtonKey,
                    onSelectButton = { btn -> safeExecuteChange { handleSelectButton(btn) } },
                    selectedPresetStyle = selectedPresetStyle,
                    onSelectPresetStyle = { st -> safeExecuteChange { handleSelectPresetStyle(st) } },
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
                            val exportDoc = withContext(Dispatchers.Default) { getLiveExportDoc() }
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
                            val exportDoc = withContext(Dispatchers.Default) { getLiveExportDoc() }
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
                // Material 3 Expressive Segmented Tab Switcher (Pill Container)
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val activeTabBrush = remember {
                            Brush.horizontalGradient(
                                listOf(
                                    NeonPalette.Cyan.copy(alpha = 0.22f),
                                    Color(0xFF7C3AED).copy(alpha = 0.18f)
                                )
                            )
                        }

                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp),
                            shape = RoundedCornerShape(12.dp),
                            color = if (singlePaneTab == 0) MaterialTheme.colorScheme.surfaceContainerHigh else Color.Transparent,
                            border = if (singlePaneTab == 0) BorderStroke(1.dp, NeonPalette.Cyan.copy(alpha = 0.6f)) else null,
                            onClick = { singlePaneTab = 0 }
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .then(if (singlePaneTab == 0) Modifier.background(activeTabBrush) else Modifier),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (availableWidth < 460.dp) "💻 Editor" else "💻 Code Editor & Mapping",
                                    color = if (singlePaneTab == 0) NeonPalette.Cyan else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = if (singlePaneTab == 0) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 12.sp,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }

                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp),
                            shape = RoundedCornerShape(12.dp),
                            color = if (singlePaneTab == 1) MaterialTheme.colorScheme.surfaceContainerHigh else Color.Transparent,
                            border = if (singlePaneTab == 1) BorderStroke(1.dp, NeonPalette.Cyan.copy(alpha = 0.6f)) else null,
                            onClick = { singlePaneTab = 1 }
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .then(if (singlePaneTab == 1) Modifier.background(activeTabBrush) else Modifier),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (availableWidth < 460.dp) "🎮 Sandbox" else "🎮 Live Sandbox & Export",
                                    color = if (singlePaneTab == 1) NeonPalette.Cyan else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = if (singlePaneTab == 1) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 12.sp,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }
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
                            onLoadStarter = { safeExecuteChange { handleLoadStarter() } },
                            selectedCategory = selectedCategory,
                            onSelectCategory = { cat -> safeExecuteChange { handleSelectCategory(cat) } },
                            categories = categories,
                            buttonsByCategory = buttonsByCategory,
                            selectedButtonKey = selectedButtonKey,
                            onSelectButton = { btn -> safeExecuteChange { handleSelectButton(btn) } },
                            selectedPresetStyle = selectedPresetStyle,
                            onSelectPresetStyle = { st -> safeExecuteChange { handleSelectPresetStyle(st) } },
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
                                    val exportDoc = withContext(Dispatchers.Default) { getLiveExportDoc() }
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
                                    val exportDoc = withContext(Dispatchers.Default) { getLiveExportDoc() }
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
                Surface(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .fillMaxHeight(0.85f),
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f)),
                    shadowElevation = 24.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(22.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
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
                                    fontSize = 17.sp,
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
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                                    shape = RoundedCornerShape(12.dp),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                                ) {
                                    Text("Copy to Clipboard", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
                                }

                                OutlinedButton(
                                    onClick = { showAiPromptModal = false },
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                                ) {
                                    Text("Close", color = Color.White, fontSize = 12.sp)
                                }
                            }
                        }

                        // Tier Tab Switcher (Material 3 Expressive Pills)
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerLow,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(4.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                PromptTier.entries.forEach { tier ->
                                    val isSelected = modalTier == tier
                                    Surface(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(34.dp),
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (isSelected) MaterialTheme.colorScheme.secondary else Color.Transparent,
                                        border = if (isSelected) null else BorderStroke(1.dp, Color.Transparent),
                                        onClick = { modalTier = tier }
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = "${tier.icon} ${tier.displayName}",
                                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                fontSize = 12.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Scrollable Prompt Text Display (Expressive Code Well)
                        val promptScrollState = rememberScrollState()
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .clip(RoundedCornerShape(16.dp))
                                .background(MaterialTheme.colorScheme.surfaceContainerLowest)
                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
                                .padding(14.dp)
                        ) {
                            Text(
                                text = generatedPrompt,
                                color = Color(0xFFE2E8F0),
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.5.sp,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(end = 14.dp)
                                    .verticalScroll(promptScrollState)
                            )
                            DesktopVerticalScrollbar(
                                adapter = rememberScrollbarAdapter(promptScrollState),
                                modifier = Modifier
                                    .align(Alignment.CenterEnd)
                                    .fillMaxHeight(),
                                thickness = 8.dp,
                                trackWidth = 11.dp,
                                showTrackGutter = true
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
                    scope.launch {
                        try {
                            val immediateDoc = withContext(Dispatchers.Default) {
                                NxprcHtmlCssConverter.convert(
                                    source = newHtml,
                                    id = componentId,
                                    name = componentName,
                                    category = category,
                                    defaultControl = defaultControl
                                )
                            }
                            compiledDoc = immediateDoc
                            compileError = null
                        } catch (e: Exception) {
                            compileError = e.message ?: "Compilation error"
                        }
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

        if (showDiscardConfirmDialog) {
            AlertDialog(
                onDismissRequest = {
                    showDiscardConfirmDialog = false
                    pendingSwitchAction = null
                },
                title = {
                    Text("Discard Unsaved Changes?", fontWeight = FontWeight.Bold)
                },
                text = {
                    Text("You have unsaved edits in the HTML/CSS editor. Switching controls or templates will replace your current code.\n\nAre you sure you want to discard your changes?")
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showDiscardConfirmDialog = false
                            val act = pendingSwitchAction
                            pendingSwitchAction = null
                            act?.invoke()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Discard & Replace", color = Color.White)
                    }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = {
                            showDiscardConfirmDialog = false
                            pendingSwitchAction = null
                        }
                    ) {
                        Text("Keep Editing")
                    }
                }
            )
        }
    }
}
