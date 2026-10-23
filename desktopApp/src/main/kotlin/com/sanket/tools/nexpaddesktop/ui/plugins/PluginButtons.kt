package com.sanket.tools.nexpaddesktop.ui.plugins

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sanket.tools.nexpad.category.SubCategoryDefinition
import com.sanket.tools.nexpaddesktop.plugins.ModelCapability
import com.sanket.tools.nexpaddesktop.plugins.NxprcPresetStyle
import com.sanket.tools.nexpaddesktop.ui.theme.NeonPalette
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection

val SubCategoryDefinition.accentColor: Color get() = Color(accentColorArgb)

enum class PromptTier(
    val icon: String,
    val displayName: String,
    val description: String,
    val modelCapability: ModelCapability
) {
    COMPACT("⚡", "Compact", "Simple • Clean • Small/Local LLMs", ModelCapability.COMPACT),
    STANDARD("🎮", "Standard", "Auto • Balanced • GPT-4o, Claude Sonnet", ModelCapability.STANDARD),
    FRONTIER("🚀", "Frontier", "Unlimited • Dense • Claude 3.7, o1/o3", ModelCapability.FRONTIER)
}

fun safeCopyToClipboard(text: String): Boolean {
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

@Composable
fun PromptSplitButton(
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

@Composable
fun StarterSplitButton(
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
