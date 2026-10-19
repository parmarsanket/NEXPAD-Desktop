package com.sanket.tools.nexpaddesktop.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sanket.tools.nexpaddesktop.model.GestureButton

enum class GestureDirection(
    val title: String,
    val motionHint: String,
    val icon: ImageVector,
    val iconRotation: Float = 0f
) {
    UP("Upward", "Pitch Up / Flick Up", Icons.Default.ArrowUpward),
    LEFT("Left", "Roll Left / Flick Left", Icons.AutoMirrored.Filled.ArrowForward, 180f),
    RIGHT("Right", "Roll Right / Flick Right", Icons.AutoMirrored.Filled.ArrowForward),
    DOWN("Downward", "Pitch Down / Flick Down", Icons.Default.ArrowDownward)
}

/**
 * Spatial 4-Way Cross Grid for binding Flick or Tilt directions to [GestureButton] actions.
 */
@Composable
fun SpatialGestureMappingGrid(
    title: String,
    centerBadgeText: String,
    upAction: GestureButton,
    downAction: GestureButton,
    leftAction: GestureButton,
    rightAction: GestureButton,
    accentColor: Color,
    onUpdateDirection: (GestureDirection, GestureButton) -> Unit
) {
    var activeDirection by remember { mutableStateOf<GestureDirection?>(null) }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header + Helper Instruction
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (activeDirection == null) "Click any direction to rebind"
                    else "Selecting action for ${activeDirection!!.title}...",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (activeDirection != null) accentColor else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // ── 2D Spatial Cross Layout (Compass / D-Pad Topology) ──
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // TOP SLOT: UP
                Box(modifier = Modifier.fillMaxWidth(0.46f)) {
                    DirectionalSlotCard(
                        direction = GestureDirection.UP,
                        boundButton = upAction,
                        isSelected = activeDirection == GestureDirection.UP,
                        accentColor = accentColor,
                        onClick = {
                            activeDirection = if (activeDirection == GestureDirection.UP) null else GestureDirection.UP
                        }
                    )
                }

                // MIDDLE ROW: LEFT + CENTER SENSOR CORE + RIGHT
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        DirectionalSlotCard(
                            direction = GestureDirection.LEFT,
                            boundButton = leftAction,
                            isSelected = activeDirection == GestureDirection.LEFT,
                            accentColor = accentColor,
                            onClick = {
                                activeDirection = if (activeDirection == GestureDirection.LEFT) null else GestureDirection.LEFT
                            }
                        )
                    }

                    // Center Hub Emblem
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        border = BorderStroke(
                            width = 1.dp,
                            color = if (activeDirection != null) accentColor.copy(alpha = 0.6f)
                            else MaterialTheme.colorScheme.outlineVariant
                        ),
                        modifier = Modifier.size(64.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bolt,
                                contentDescription = null,
                                tint = accentColor,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = centerBadgeText,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Box(modifier = Modifier.weight(1f)) {
                        DirectionalSlotCard(
                            direction = GestureDirection.RIGHT,
                            boundButton = rightAction,
                            isSelected = activeDirection == GestureDirection.RIGHT,
                            accentColor = accentColor,
                            onClick = {
                                activeDirection = if (activeDirection == GestureDirection.RIGHT) null else GestureDirection.RIGHT
                            }
                        )
                    }
                }

                // BOTTOM SLOT: DOWN
                Box(modifier = Modifier.fillMaxWidth(0.46f)) {
                    DirectionalSlotCard(
                        direction = GestureDirection.DOWN,
                        boundButton = downAction,
                        isSelected = activeDirection == GestureDirection.DOWN,
                        accentColor = accentColor,
                        onClick = {
                            activeDirection = if (activeDirection == GestureDirection.DOWN) null else GestureDirection.DOWN
                        }
                    )
                }
            }

            // ── Inline Categorized Button Picker Tray (Expands when a slot is clicked) ──
            AnimatedVisibility(
                visible = activeDirection != null,
                enter = fadeIn() + expandVertically(spring(stiffness = Spring.StiffnessMediumLow)),
                exit = fadeOut() + shrinkVertically(spring(stiffness = Spring.StiffnessMedium))
            ) {
                val targetDir = activeDirection ?: GestureDirection.UP
                val currentSelection = when (targetDir) {
                    GestureDirection.UP -> upAction
                    GestureDirection.DOWN -> downAction
                    GestureDirection.LEFT -> leftAction
                    GestureDirection.RIGHT -> rightAction
                }

                GestureButtonPickerTray(
                    direction = targetDir,
                    currentButton = currentSelection,
                    accentColor = accentColor,
                    onSelectButton = { chosenButton ->
                        onUpdateDirection(targetDir, chosenButton)
                        activeDirection = null // Auto-collapse after quick selection
                    },
                    onDismiss = { activeDirection = null }
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// INDIVIDUAL DIRECTIONAL SLOT CARD
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun DirectionalSlotCard(
    direction: GestureDirection,
    boundButton: GestureButton,
    isSelected: Boolean,
    accentColor: Color,
    onClick: () -> Unit
) {
    val cornerRadius by animateDpAsState(
        targetValue = if (isSelected) 20.dp else 12.dp,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "slotCorner"
    )
    val containerColor by animateColorAsState(
        targetValue = when {
            isSelected -> accentColor.copy(alpha = 0.16f)
            boundButton == GestureButton.NONE -> MaterialTheme.colorScheme.surfaceContainerLow
            else -> MaterialTheme.colorScheme.surfaceContainerHigh
        },
        label = "slotBg"
    )

    val badgeColor = boundButton.toHardwareColor()

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(cornerRadius),
        color = containerColor,
        border = when {
            isSelected -> BorderStroke(1.5.dp, accentColor)
            boundButton != GestureButton.NONE -> BorderStroke(1.dp, badgeColor.copy(alpha = 0.35f))
            else -> null
        },
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Direction Arrow Icon Pill
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(
                            if (isSelected) accentColor.copy(alpha = 0.25f)
                            else MaterialTheme.colorScheme.surfaceContainerLowest
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = direction.icon,
                        contentDescription = direction.title,
                        tint = if (isSelected) accentColor else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .size(16.dp)
                            .rotate(direction.iconRotation)
                    )
                }

                Column {
                    Text(
                        text = direction.title,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = boundButton.displayName,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (boundButton == GestureButton.NONE) MaterialTheme.colorScheme.onSurfaceVariant
                        else badgeColor,
                        maxLines = 1
                    )
                }
            }

            // Hardware Short-Code Keycap Badge ("X", "RT", "180°")
            HardwareKeycapBadge(button = boundButton)
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// CATEGORIZED ACTION PICKER TRAY (ALSO USED FOR SINGLE SHAKE ACTION)
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun GestureButtonPickerTray(
    direction: GestureDirection,
    currentButton: GestureButton,
    accentColor: Color,
    onSelectButton: (GestureButton) -> Unit,
    onDismiss: () -> Unit
) {
    val categories = remember {
        listOf(
            "Tactical & Triggers" to listOf(
                GestureButton.SNAP_TURN_180,
                GestureButton.TRIGGER_RT,
                GestureButton.TRIGGER_LT,
                GestureButton.NONE
            ),
            "ABXY Action Buttons" to listOf(
                GestureButton.BTN_A,
                GestureButton.BTN_B,
                GestureButton.BTN_X,
                GestureButton.BTN_Y
            ),
            "Bumpers & Stick Clicks" to listOf(
                GestureButton.BTN_LB,
                GestureButton.BTN_RB,
                GestureButton.BTN_LS,
                GestureButton.BTN_RS
            ),
            "Directional Pad (D-Pad)" to listOf(
                GestureButton.DPAD_UP,
                GestureButton.DPAD_DOWN,
                GestureButton.DPAD_LEFT,
                GestureButton.DPAD_RIGHT
            )
        )
    }

    Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainer,
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.45f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = direction.icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier
                            .size(18.dp)
                            .rotate(direction.iconRotation)
                    )
                    Text(
                        text = "Bind ${direction.title} Motion (${direction.motionHint})",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close picker",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            categories.forEach { (categoryTitle, buttons) ->
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = categoryTitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        buttons.forEach { btn ->
                            val isSelected = btn == currentButton
                            val btnColor = btn.toHardwareColor()

                            FilterChip(
                                selected = isSelected,
                                onClick = { onSelectButton(btn) },
                                leadingIcon = {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(btnColor)
                                    )
                                },
                                label = {
                                    Text(
                                        text = btn.displayName,
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = btnColor.copy(alpha = 0.2f),
                                    selectedLabelColor = MaterialTheme.colorScheme.onSurface,
                                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = isSelected,
                                    borderColor = Color.Transparent,
                                    selectedBorderColor = btnColor
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// HARDWARE KEYCAP BADGE & COLOR MAPPING
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun HardwareKeycapBadge(button: GestureButton) {
    val badgeColor = button.toHardwareColor()
    val isDisabled = button == GestureButton.NONE

    Surface(
        shape = MaterialTheme.shapes.extraSmall,
        color = if (isDisabled) MaterialTheme.colorScheme.surfaceContainerLowest
        else badgeColor.copy(alpha = 0.18f),
        border = BorderStroke(
            width = 1.dp,
            color = if (isDisabled) MaterialTheme.colorScheme.outlineVariant
            else badgeColor.copy(alpha = 0.6f)
        )
    ) {
        Text(
            text = button.shortName,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.ExtraBold,
            color = if (isDisabled) MaterialTheme.colorScheme.onSurfaceVariant else badgeColor,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

/**
 * Maps each [GestureButton] to authentic Xbox hardware colors & semantic highlights.
 */
@Composable
fun GestureButton.toHardwareColor(): Color = when (this) {
    GestureButton.BTN_A -> Color(0xFF00E676)         // Xbox 'A' Emerald Green
    GestureButton.BTN_B -> Color(0xFFFF5252)         // Xbox 'B' Crimson Red
    GestureButton.BTN_X -> Color(0xFF40C4FF)         // Xbox 'X' Sky Blue
    GestureButton.BTN_Y -> Color(0xFFFFD54F)         // Xbox 'Y' Amber Gold
    GestureButton.SNAP_TURN_180 -> Color(0xFFD500F9) // Tactical 180° Turn Neon Purple
    GestureButton.TRIGGER_LT,
    GestureButton.TRIGGER_RT -> Color(0xFFFF9100)    // Analog Triggers Orange
    GestureButton.BTN_LB,
    GestureButton.BTN_RB,
    GestureButton.BTN_LS,
    GestureButton.BTN_RS -> Color(0xFF00E5FF)        // Shoulder & Stick Clicks Cyan
    GestureButton.DPAD_UP,
    GestureButton.DPAD_DOWN,
    GestureButton.DPAD_LEFT,
    GestureButton.DPAD_RIGHT -> Color(0xFFB0BEC5)    // D-Pad Silver/Slate
    GestureButton.NONE -> MaterialTheme.colorScheme.onSurfaceVariant
}
