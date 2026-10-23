package com.sanket.tools.nexpaddesktop.ui.plugins

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.rememberTextFieldVerticalScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sanket.tools.nexpad.category.SubCategoryDefinition
import com.sanket.tools.nexpaddesktop.plugins.NxprcPresetStyle
import com.sanket.tools.nexpaddesktop.ui.components.DesktopVerticalScrollbar
import com.sanket.tools.nexpaddesktop.ui.components.glassCard
import com.sanket.tools.nexpaddesktop.ui.theme.NeonPalette

/**
 * Left Component Editor Pane with adaptive header, category carousels, and monospace code editor.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ComponentEditorPane(
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

                    // Dedicated Sub-row for Action Buttons
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

        // 2. Copy Feedback Banner
        var lastBannerText by remember { mutableStateOf("") }
        if (promptCopiedBanner != null) {
            lastBannerText = promptCopiedBanner
        }
        val bannerAlpha by animateFloatAsState(
            targetValue = if (promptCopiedBanner != null) 1f else 0f,
            label = "bannerAlpha"
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(if (isShort) 26.dp else 28.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            if (bannerAlpha > 0.001f && lastBannerText.isNotEmpty()) {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer { alpha = bannerAlpha },
                    color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.16f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.45f)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = lastBannerText,
                            color = Color(0xFFC4B5FD),
                            fontSize = if (isShort) 11.sp else 11.5.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        // 3. Category Selector
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

        // 4. Sub-Category Button Key Selector
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

        // 5. Editor Header
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
