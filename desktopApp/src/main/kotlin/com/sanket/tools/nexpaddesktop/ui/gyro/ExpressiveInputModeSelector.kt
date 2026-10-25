package com.sanket.tools.nexpaddesktop.ui.gyro

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.sanket.tools.nexpaddesktop.model.InputMode
import com.sanket.tools.nexpaddesktop.ui.theme.NexpadTheme
import com.sanket.tools.nexpaddesktop.ui.visualizers.*

@Composable
fun ExpressiveInputModeSelector(
    activeMode: InputMode,
    onSelectMode: (InputMode) -> Unit
) {
    val modes = remember {
        listOf(
            InputMode.VELOCITY to "Precision Aim",
            InputMode.ABSOLUTE_TILT to "Steering Wheel",
            InputMode.AIR_MOUSE to "3D Flight Stick",
            InputMode.GESTURE to "Combat",
            InputMode.RAW_UNCALIBRATED to "Raw Silicon"
        )
    }

    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val isNarrow = maxWidth < 640.dp
        if (isNarrow) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    modes.take(3).forEach { (mode, shortTitle) ->
                        ModeSelectorCard(
                            mode = mode,
                            shortTitle = shortTitle,
                            isSelected = activeMode == mode,
                            onClick = { onSelectMode(mode) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    modes.drop(3).forEach { (mode, shortTitle) ->
                        ModeSelectorCard(
                            mode = mode,
                            shortTitle = shortTitle,
                            isSelected = activeMode == mode,
                            onClick = { onSelectMode(mode) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                modes.forEach { (mode, shortTitle) ->
                    ModeSelectorCard(
                        mode = mode,
                        shortTitle = shortTitle,
                        isSelected = activeMode == mode,
                        onClick = { onSelectMode(mode) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun ModeSelectorCard(
    mode: InputMode,
    shortTitle: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = NexpadTheme.colors.forMode(mode)

    val cornerRadius by animateDpAsState(
        targetValue = if (isSelected) 20.dp else 12.dp,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "modeCorner"
    )
    val bgColor by animateColorAsState(
        targetValue = if (isSelected) palette.container else MaterialTheme.colorScheme.surfaceContainerLow,
        label = "modeBg"
    )

    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(cornerRadius),
        color = bgColor,
        border = if (isSelected) BorderStroke(1.5.dp, palette.accent) else null
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier.size(52.dp),
                contentAlignment = Alignment.Center
            ) {
                when (mode) {
                    InputMode.VELOCITY -> ModeVectorIconGyroAiming(modifier = Modifier.fillMaxSize(), isSelected = isSelected)
                    InputMode.ABSOLUTE_TILT -> ModeVectorIconSteeringWheel(modifier = Modifier.fillMaxSize(), isSelected = isSelected)
                    InputMode.AIR_MOUSE -> ModeVectorIconFlightStick(modifier = Modifier.fillMaxSize(), isSelected = isSelected)
                    InputMode.GESTURE -> ModeVectorIconCombatGestures(modifier = Modifier.fillMaxSize(), isSelected = isSelected)
                    InputMode.RAW_UNCALIBRATED -> ModeVectorIconRawSilicon(modifier = Modifier.fillMaxSize(), isSelected = isSelected)
                }
            }
            Text(
                text = shortTitle,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) MaterialTheme.colorScheme.onSurface
                else MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
