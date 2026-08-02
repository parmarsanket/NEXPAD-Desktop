package com.sanket.tools.nexpaddesktop.ui.visualizers

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import com.sanket.tools.nexpaddesktop.model.GyroSettings
import com.sanket.tools.nexpaddesktop.model.InputMode

@Composable
fun InputModeCards(
    currentSettings: GyroSettings,
    onSettingsChange: (GyroSettings) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        InputModeCard(
            modifier = Modifier.weight(1f),
            mode = InputMode.VELOCITY,
            currentMode = currentSettings.inputMode,
            title = "Gyro Aiming",
            imagePath = "images/fps_gun.png",
            description = "Translates physical motion into camera speed. Best for FPS games.",
            onClick = { onSettingsChange(currentSettings.copy(inputMode = InputMode.VELOCITY)) }
        )

        InputModeCard(
            modifier = Modifier.weight(1f),
            mode = InputMode.ABSOLUTE_TILT,
            currentMode = currentSettings.inputMode,
            title = "Steering Wheel",
            imagePath = "images/steering_wheel.png",
            description = "Maps absolute angle directly to the stick. Best for racing games.",
            onClick = { onSettingsChange(currentSettings.copy(inputMode = InputMode.ABSOLUTE_TILT)) }
        )
    }
}

@Composable
private fun InputModeCard(
    modifier: Modifier = Modifier,
    mode: InputMode,
    currentMode: InputMode,
    title: String,
    imagePath: String,
    description: String,
    onClick: () -> Unit
) {
    val isSelected = mode == currentMode
    
    val containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
    val contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        color = containerColor,
        contentColor = contentColor,
        border = BorderStroke(2.dp, borderColor)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(imagePath),
                contentDescription = title,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .size(80.dp)
                    .padding(bottom = 12.dp)
            )
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Text(
                text = description,
                fontSize = 12.sp,
                color = if (isSelected) contentColor else Color.Gray,
                lineHeight = 16.sp,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
