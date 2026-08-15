package com.sanket.tools.nexpaddesktop.ui.visualizers

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sanket.tools.nexpaddesktop.model.GyroSettings

@Composable
fun PresetCards(
    currentSettings: GyroSettings,
    onSettingsChange: (GyroSettings) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        PresetCard(
            modifier = Modifier.weight(1f),
            title = "FPS",
            icon = "🎯",
            description = "Fast Aim & Precision",
            onClick = { onSettingsChange(GyroSettings.FPS_PRESET) }
        )
        
        PresetCard(
            modifier = Modifier.weight(1f),
            title = "Racing",
            icon = "🏎️",
            description = "Smooth Steering",
            onClick = { onSettingsChange(GyroSettings.RACING_PRESET) }
        )
        
        PresetCard(
            modifier = Modifier.weight(1f),
            title = "Precision",
            icon = "🔬",
            description = "Slow & Accurate",
            onClick = { onSettingsChange(GyroSettings.PRECISION_PRESET) }
        )

        PresetCard(
            modifier = Modifier.weight(1f),
            title = "Reset",
            icon = "↺",
            description = "Default Settings",
            onClick = { onSettingsChange(GyroSettings.DEFAULT) }
        )
    }
}

@Composable
private fun PresetCard(
    modifier: Modifier = Modifier,
    title: String,
    icon: String,
    description: String,
    onClick: () -> Unit
) {
    ElevatedCard(
        modifier = modifier.clickable { onClick() },
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(icon, fontSize = 24.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(description, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
