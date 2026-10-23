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

            htmlSource = base
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

            htmlSource = baseSource

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

            htmlSource = base
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
                    onLoadStarter = handleLoadStarter,
                    selectedCategory = selectedCategory,
                    onSelectCategory = handleSelectCategory,
                    categories = categories,
                    buttonsByCategory = buttonsByCategory,
                    selectedButtonKey = selectedButtonKey,
                    onSelectButton = handleSelectButton,
                    selectedPresetStyle = selectedPresetStyle,
                    onSelectPresetStyle = handleSelectPresetStyle,
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
                            onLoadStarter = handleLoadStarter,
                            selectedCategory = selectedCategory,
                            onSelectCategory = handleSelectCategory,
                            categories = categories,
                            buttonsByCategory = buttonsByCategory,
                            selectedButtonKey = selectedButtonKey,
                            onSelectButton = handleSelectButton,
                            selectedPresetStyle = selectedPresetStyle,
                            onSelectPresetStyle = handleSelectPresetStyle,
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
    }
}

/**
 * Material 3 Expressive SplitButton layout specification and defaults.
 */
object SplitButtonDefaults {
    val TrailingIconSize = 16.dp
    val Spacing = 3.dp
    val LeadingShape = RoundedCornerShape(topStart = 10.dp, bottomStart = 10.dp, topEnd = 3.dp, bottomEnd = 3.dp)
    val TrailingShape = RoundedCornerShape(topStart = 3.dp, bottomStart = 3.dp, topEnd = 10.dp, bottomEnd = 10.dp)

    @Composable
    fun LeadingButton(
        onClick: () -> Unit,
        modifier: Modifier = Modifier,
        enabled: Boolean = true,
        colors: ButtonColors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
        contentPadding: PaddingValues = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
        content: @Composable RowScope.() -> Unit
    ) {
        Button(
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
            shape = LeadingShape,
            colors = colors,
            contentPadding = contentPadding,
            content = content
        )
    }

    @Composable
    fun TrailingButton(
        checked: Boolean,
        onCheckedChange: (Boolean) -> Unit,
        modifier: Modifier = Modifier,
        enabled: Boolean = true,
        colors: ButtonColors = ButtonDefaults.buttonColors(
            containerColor = if (checked) MaterialTheme.colorScheme.secondary.copy(alpha = 0.85f) else MaterialTheme.colorScheme.secondary
        ),
        contentPadding: PaddingValues = PaddingValues(horizontal = 6.dp, vertical = 4.dp),
        content: @Composable RowScope.() -> Unit
    ) {
        Button(
            onClick = { onCheckedChange(!checked) },
            modifier = modifier,
            enabled = enabled,
            shape = TrailingShape,
            colors = colors,
            contentPadding = contentPadding,
            content = content
        )
    }
}

@Composable
fun SplitButtonLayout(
    leadingButton: @Composable () -> Unit,
    trailingButton: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    spacing: androidx.compose.ui.unit.Dp = SplitButtonDefaults.Spacing
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(spacing),
        verticalAlignment = Alignment.CenterVertically
    ) {
        leadingButton()
        trailingButton()
    }
}

/**
 * Material 3 Expressive Connected Button Group specification and shape defaults.
 * Follows SingleSelectConnectedButtonGroupSample specification.
 */
object ButtonGroupDefaults {
    val ConnectedSpaceBetween: androidx.compose.ui.unit.Dp = 3.dp

    fun connectedLeadingButtonShapes(
        outerRadius: androidx.compose.ui.unit.Dp = 10.dp,
        innerRadius: androidx.compose.ui.unit.Dp = 3.dp
    ): Shape =
        RoundedCornerShape(
            topStart = outerRadius,
            bottomStart = outerRadius,
            topEnd = innerRadius,
            bottomEnd = innerRadius
        )

    fun connectedMiddleButtonShapes(
        innerRadius: androidx.compose.ui.unit.Dp = 3.dp
    ): Shape =
        RoundedCornerShape(innerRadius)

    fun connectedTrailingButtonShapes(
        outerRadius: androidx.compose.ui.unit.Dp = 10.dp,
        innerRadius: androidx.compose.ui.unit.Dp = 3.dp
    ): Shape =
        RoundedCornerShape(
            topStart = innerRadius,
            bottomStart = innerRadius,
            topEnd = outerRadius,
            bottomEnd = outerRadius
        )

    fun connectedSingleButtonShapes(
        outerRadius: androidx.compose.ui.unit.Dp = 10.dp
    ): Shape =
        RoundedCornerShape(outerRadius)
}

/**
 * Material 3 Expressive ToggleButton item for connected button groups.
 */
@Composable
fun ToggleButton(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    shapes: Shape,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    accentColor: Color = NeonPalette.Cyan,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerLow,
    unselectedBorderColor: Color = MaterialTheme.colorScheme.outlineVariant,
    contentPadding: PaddingValues = PaddingValues(horizontal = 11.dp, vertical = 5.dp),
    content: @Composable RowScope.() -> Unit
) {
    OutlinedButton(
        onClick = { onCheckedChange(!checked) },
        shape = shapes,
        border = BorderStroke(
            width = if (checked) 1.5.dp else 1.dp,
            color = if (checked) accentColor else unselectedBorderColor
        ),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = if (checked) accentColor.copy(alpha = 0.18f) else containerColor,
            contentColor = if (checked) accentColor else MaterialTheme.colorScheme.onSurfaceVariant
        ),
        contentPadding = contentPadding,
        modifier = modifier.height(30.dp),
        enabled = enabled,
        content = content
    )
}

/**
 * SplitButton action for AI prompt copying with tier dropdown selector.
 */
@Composable
private fun PromptSplitButton(
    selectedPromptTier: PromptTier,
    onSelectPromptTier: (PromptTier) -> Unit,
    onCopyAiPrompt: () -> Unit,
    height: androidx.compose.ui.unit.Dp = 28.dp
) {
    var checked by remember { mutableStateOf(false) }

    Box {
        SplitButtonLayout(
            leadingButton = {
                SplitButtonDefaults.LeadingButton(
                    onClick = onCopyAiPrompt,
                    modifier = Modifier.height(height)
                ) {
                    Text(
                        text = "Copy AI Prompt",
                        fontSize = if (height < 30.dp) 10.5.sp else 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            },
            trailingButton = {
                val description = "Toggle AI Prompt Tier"

                // Provides an accessible tooltip for the icon-only button.
                TooltipBox(
                    positionProvider =
                        TooltipDefaults.rememberTooltipPositionProvider(
                            TooltipAnchorPosition.Above
                        ),
                    tooltip = {
                        PlainTooltip(
                            modifier = Modifier.semantics {
                                liveRegion = LiveRegionMode.Assertive
                                paneTitle = description
                            }
                        ) {
                            Text(description)
                        }
                    },
                    state = rememberTooltipState()
                ) {
                    SplitButtonDefaults.TrailingButton(
                        checked = checked,
                        onCheckedChange = { checked = it },
                        modifier = Modifier
                            .size(height)
                            .semantics {
                                stateDescription =
                                    if (checked) "Expanded" else "Collapsed"
                                contentDescription = description
                            }
                    ) {
                        val rotation by animateFloatAsState(
                            targetValue = if (checked) 180f else 0f,
                            label = "Trailing Icon Rotation"
                        )

                        Icon(
                            imageVector = Icons.Filled.KeyboardArrowDown,
                            modifier = Modifier
                                .size(SplitButtonDefaults.TrailingIconSize)
                                .graphicsLayer {
                                    rotationZ = rotation
                                },
                            tint = Color.White,
                            contentDescription = null
                        )
                    }
                }
            }
        )

        DropdownMenu(
            expanded = checked,
            onDismissRequest = {
                checked = false
            }
        ) {
            PromptTier.entries.forEach { tier ->
                DropdownMenuItem(
                    text = {
                        Column {
                            Text(
                                text = buildString {
                                    append("${tier.icon} ${tier.displayName}")
                                    if (tier == selectedPromptTier) {
                                        append(" ✓")
                                    }
                                },
                                fontWeight = if (tier == selectedPromptTier) {
                                    FontWeight.Bold
                                } else {
                                    FontWeight.Normal
                                },
                                fontSize = 13.sp
                            )

                            Text(
                                text = tier.description,
                                fontSize = 10.5.sp,
                                color = Color.Gray
                            )
                        }
                    },
                    onClick = {
                        onSelectPromptTier(tier)
                        checked = false
                    }
                )
            }
        }
    }
}

/**
 * Split Button for Loading Starter Code with Hardware Preset Style dropdown selector.
 * Allows quick loading of the current starter, or expanding the dropdown to switch between
 * Xbox Elite, Candy Cab Arcade, Cyberpunk, and Satin styles.
 */
@Composable
private fun StarterSplitButton(
    selectedPresetStyle: NxprcPresetStyle,
    onSelectPresetStyle: (NxprcPresetStyle) -> Unit,
    onLoadStarter: () -> Unit,
    height: androidx.compose.ui.unit.Dp = 28.dp
) {
    var checked by remember { mutableStateOf(false) }

    Box {
        SplitButtonLayout(
            leadingButton = {
                SplitButtonDefaults.LeadingButton(
                    onClick = onLoadStarter,
                    modifier = Modifier.height(height)
                ) {
                    Text(
                        text = "Load Starter Code",
                        fontSize = if (height < 30.dp) 10.5.sp else 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            },
            trailingButton = {
                val description = "Choose Preset Hardware Style"

                TooltipBox(
                    positionProvider = TooltipDefaults.rememberTooltipPositionProvider(
                        TooltipAnchorPosition.Above
                    ),
                    tooltip = {
                        PlainTooltip(
                            modifier = Modifier.semantics {
                                liveRegion = LiveRegionMode.Assertive
                                paneTitle = description
                            }
                        ) {
                            Text(description)
                        }
                    },
                    state = rememberTooltipState()
                ) {
                    SplitButtonDefaults.TrailingButton(
                        checked = checked,
                        onCheckedChange = { checked = it },
                        modifier = Modifier
                            .size(height)
                            .semantics {
                                stateDescription = if (checked) "Expanded" else "Collapsed"
                                contentDescription = description
                            }
                    ) {
                        val rotation by animateFloatAsState(
                            targetValue = if (checked) 180f else 0f,
                            label = "Trailing Icon Rotation"
                        )

                        Icon(
                            imageVector = Icons.Filled.KeyboardArrowDown,
                            modifier = Modifier
                                .size(SplitButtonDefaults.TrailingIconSize)
                                .graphicsLayer {
                                    rotationZ = rotation
                                },
                            tint = Color.White,
                            contentDescription = null
                        )
                    }
                }
            }
        )

        DropdownMenu(
            expanded = checked,
            onDismissRequest = {
                checked = false
            }
        ) {
            NxprcPresetStyle.entries.forEach { style ->
                val isSelected = style == selectedPresetStyle
                DropdownMenuItem(
                    text = {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = buildString {
                                    append(style.displayName)
                                    if (isSelected) {
                                        append(" ✓")
                                    }
                                },
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) NeonPalette.Cyan else Color.White
                            )
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = if (isSelected) NeonPalette.Cyan.copy(alpha = 0.35f) else Color.White.copy(alpha = 0.12f)
                            ) {
                                Text(
                                    text = style.badge,
                                    color = if (isSelected) Color.White else Color.White.copy(alpha = 0.65f),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                )
                            }
                        }
                    },
                    onClick = {
                        checked = false
                        onSelectPresetStyle(style)
                    }
                )
            }
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
    selectedPresetStyle: NxprcPresetStyle = NxprcPresetStyle.XBOX_ELITE,
    onSelectPresetStyle: (NxprcPresetStyle) -> Unit = {},
    htmlSource: String,
    onHtmlSourceChange: (String) -> Unit
) {
    Column(
        modifier = modifier
            .glassCard()
            .padding(if (isShort) 8.dp else 12.dp),
        verticalArrangement = Arrangement.spacedBy(if (isShort) 6.dp else 8.dp)
    ) {
        // 1. Header & AI Instructor Actions (Responsive BoxWithConstraints)
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val isHeaderStacked = maxWidth < 820.dp
            if (isHeaderStacked) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Column {
                        Text(
                            "🎮 Controller Component Studio",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            maxLines = 1,
                            softWrap = false
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Target: $defaultControl",
                                color = NeonPalette.Cyan,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 10.5.sp,
                                maxLines = 1,
                                softWrap = false
                            )
                            Text(
                                "• $category • ${targetWidthDp}x${targetHeightDp}dp",
                                color = Color.White.copy(alpha = 0.65f),
                                fontSize = 10.5.sp,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }

                    // Dedicated Sub-row for Action Buttons — NEVER squished into vertical letters!
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        PromptSplitButton(
                            selectedPromptTier = selectedPromptTier,
                            onSelectPromptTier = onSelectPromptTier,
                            onCopyAiPrompt = onCopyAiPrompt,
                            height = 28.dp
                        )

                        OutlinedButton(
                            onClick = onOpenPromptModal,
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(1.dp, NeonPalette.Cyan.copy(alpha = 0.5f)),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = NeonPalette.Cyan.copy(alpha = 0.08f)
                            ),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 3.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Text("View Prompt", fontSize = 10.5.sp, color = NeonPalette.Cyan, maxLines = 1, softWrap = false)
                        }

                        StarterSplitButton(
                            selectedPresetStyle = selectedPresetStyle,
                            onSelectPresetStyle = onSelectPresetStyle,
                            onLoadStarter = onLoadStarter,
                            height = 28.dp
                        )
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f, fill = false)) {
                        Text(
                            "🎮 Controller Component Studio",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            maxLines = 1,
                            softWrap = false
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Target: $defaultControl ($category • ${targetWidthDp}x${targetHeightDp}dp)",
                                color = NeonPalette.Cyan.copy(alpha = 0.85f),
                                fontSize = 11.sp,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                        Text(
                            "Category is mapping metadata; your HTML/CSS owns the shape.",
                            color = Color.White.copy(alpha = 0.58f),
                            fontSize = 10.sp,
                            maxLines = 1,
                            softWrap = false
                        )
                    }

                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        PromptSplitButton(
                            selectedPromptTier = selectedPromptTier,
                            onSelectPromptTier = onSelectPromptTier,
                            onCopyAiPrompt = onCopyAiPrompt,
                            height = 30.dp
                        )

                        OutlinedButton(
                            onClick = onOpenPromptModal,
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, NeonPalette.Cyan.copy(alpha = 0.5f)),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = NeonPalette.Cyan.copy(alpha = 0.08f)
                            ),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Text("View Prompt", fontSize = 11.sp, color = NeonPalette.Cyan, maxLines = 1, softWrap = false)
                        }

                        StarterSplitButton(
                            selectedPresetStyle = selectedPresetStyle,
                            onSelectPresetStyle = onSelectPresetStyle,
                            onLoadStarter = onLoadStarter,
                            height = 30.dp
                        )
                    }
                }
            }
        }

        // 2. Copy Feedback Banner (Material 3 Expressive Tonal Pill)
        if (promptCopiedBanner != null) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.16f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.45f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        promptCopiedBanner,
                        color = Color(0xFFC4B5FD),
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // 3. Category Selector (Material 3 SingleSelectConnectedButtonGroup)
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
            verticalAlignment = Alignment.CenterVertically
        ) {
            categories.forEachIndexed { index, (catKey, catLabel) ->
                val isSelected = selectedCategory == catKey
                val shapes = when {
                    categories.size <= 1 -> ButtonGroupDefaults.connectedSingleButtonShapes()
                    index == 0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                    index == categories.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                    else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                }
                ToggleButton(
                    checked = isSelected,
                    onCheckedChange = { onSelectCategory(catKey) },
                    shapes = shapes,
                    accentColor = NeonPalette.Cyan,
                    containerColor = Color(0xFF090E18),
                    unselectedBorderColor = Color(0xFF1E293B),
                    modifier = Modifier.semantics { role = Role.RadioButton }
                ) {
                    Text(
                        text = catLabel,
                        color = if (isSelected) NeonPalette.Cyan else Color.White.copy(alpha = 0.7f),
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 11.sp,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }

        // 4. Sub-Category Button Key Selector (Material 3 SingleSelectConnectedButtonGroup)
        val currentButtons = buttonsByCategory[selectedCategory] ?: emptyList()
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
            verticalAlignment = Alignment.CenterVertically
        ) {
            currentButtons.forEachIndexed { index, btn ->
                val isSelected = selectedButtonKey == btn.key
                val shapes = when {
                    currentButtons.size <= 1 -> ButtonGroupDefaults.connectedSingleButtonShapes()
                    index == 0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                    index == currentButtons.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                    else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                }
                ToggleButton(
                    checked = isSelected,
                    onCheckedChange = { onSelectButton(btn) },
                    shapes = shapes,
                    accentColor = btn.accentColor,
                    containerColor = Color(0xFF0E1320),
                    unselectedBorderColor = Color(0xFF222F46),
                    modifier = Modifier.semantics { role = Role.RadioButton }
                ) {
                    Text(
                        text = btn.label,
                        color = if (isSelected) btn.accentColor else Color.White.copy(alpha = 0.7f),
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 11.sp,
                        maxLines = 1,
                        softWrap = false
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

        // 6. Monospace Code Editor (Material 3 Expressive Code Well with High-Visibility Desktop Scrollbar)
        val codeScrollState = rememberTextFieldVerticalScrollState()

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .defaultMinSize(minHeight = 160.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerLowest)
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(14.dp))
        ) {
            BasicTextField(
                value = htmlSource,
                onValueChange = onHtmlSourceChange,
                scrollState = codeScrollState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 12.dp, top = 12.dp, bottom = 12.dp, end = 22.dp),
                textStyle = TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    color = Color(0xFFF1F5F9),
                    lineHeight = 18.sp
                ),
                cursorBrush = SolidColor(NeonPalette.Cyan),
                decorationBox = { innerTextField ->
                    if (htmlSource.isEmpty()) {
                        Text(
                            "Paste your AI-generated HTML / CSS code here...",
                            color = Color.White.copy(alpha = 0.35f),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp
                        )
                    }
                    innerTextField()
                }
            )

            DesktopVerticalScrollbar(
                adapter = rememberScrollbarAdapter(codeScrollState),
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .fillMaxHeight()
                    .padding(vertical = 4.dp, horizontal = 2.dp),
                thickness = 8.dp,
                trackWidth = 11.dp,
                showTrackGutter = true
            )
        }
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
            border = androidx.compose.foundation.BorderStroke(
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
                    // Seed Input Area: Clear label + obvious editable text field with '#' prefix and pencil '✏️'
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
                        // 🎲 Reroll Button
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

                        // ↺ Reset Button: Reverts sandbox back to HTML code's seed
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

        // Compact Glass Metadata Strip (Responsive against line wrapping)
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

            // Push via Active Transport Button (Enabled only when connected)
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
                    Icons.Default.Send,
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

