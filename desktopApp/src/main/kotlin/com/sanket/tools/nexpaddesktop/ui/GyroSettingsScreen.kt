package com.sanket.tools.nexpaddesktop.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sanket.tools.nexpaddesktop.model.GyroSettings
import com.sanket.tools.nexpaddesktop.model.InputMode
import com.sanket.tools.nexpaddesktop.ui.gyro.*
import com.sanket.tools.nexpaddesktop.ui.theme.NexpadTheme
import com.sanket.tools.nexpaddesktop.ui.visualizers.DashboardHeader

/**
 * Material 3 Expressive motion settings screen for Xbox 360 virtual controller.
 * Decomposed into modular domain panels:
 * - [MotionMasterControlBar]: Power toggle, zero recalibration, genre presets
 * - [DashboardHeader]: Live 3D controller visualizer & telemetry HUD
 * - [ExpressiveInputModeSelector]: 5-mode expressive switch cards
 * - [PrecisionAimingPanel]: 1:1 camera velocity aiming & ADS trigger
 * - [SteeringWheelPanel]: Tilt steering wheel emulation
 * - [FlightStickPanel]: 6-DoF aviation yoke mapping
 * - [CombatGesturesPanel]: Directional flicks & posture stance binds
 * - [RawSiliconPanel]: Direct uncalibrated MEMS esports stream
 */
@Composable
fun XboxMotionSettingsSection(
    settings: GyroSettings,
    onSettingsChange: (GyroSettings) -> Unit,
    rawGyroX: Float,
    rawGyroY: Float,
    rawGyroZ: Float,
    rawAccelX: Float,
    rawAccelY: Float,
    rawAccelZ: Float,
    rawGravityX: Float = 0f,
    rawGravityY: Float = 0f,
    rawGravityZ: Float = 0f,
    linearAccelX: Float = 0f,
    linearAccelY: Float = 0f,
    linearAccelZ: Float = 0f,
    rawUncalGyroX: Float = 0f,
    rawUncalGyroY: Float = 0f,
    rawUncalGyroZ: Float = 0f,
    processedYaw: Float,
    processedPitch: Float,
    flightPitchDeg: Float = 0f,
    flightRollDeg: Float = 0f,
    flightYawDeg: Float = 0f,
    onRecalibrate: () -> Unit
) {
    val activePalette = NexpadTheme.colors.forMode(settings.inputMode)

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // 1. Master Motion Power Bar + Interactive Zero Calibration + Genre Presets
        MotionMasterControlBar(
            settings = settings,
            activePalette = activePalette,
            onSettingsChange = onSettingsChange,
            onRecalibrate = onRecalibrate
        )

        // Motion Processing Dashboard & Controls (Hidden when motion processing is paused)
        AnimatedVisibility(
            visible = settings.enabled,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                // 2. Live 3D Controller & Telemetry Header
                DashboardHeader(
                    settings = settings,
                    rawGyroX = rawGyroX,
                    rawGyroY = rawGyroY,
                    rawGyroZ = rawGyroZ,
                    rawAccelX = rawAccelX,
                    rawAccelY = rawAccelY,
                    rawAccelZ = rawAccelZ,
                    rawGravityX = rawGravityX,
                    rawGravityY = rawGravityY,
                    rawGravityZ = rawGravityZ,
                    linearAccelX = linearAccelX,
                    linearAccelY = linearAccelY,
                    linearAccelZ = linearAccelZ,
                    rawUncalGyroX = rawUncalGyroX,
                    rawUncalGyroY = rawUncalGyroY,
                    rawUncalGyroZ = rawUncalGyroZ,
                    processedYaw = processedYaw,
                    processedPitch = processedPitch,
                    flightPitchDeg = flightPitchDeg,
                    flightRollDeg = flightRollDeg,
                    flightYawDeg = flightYawDeg
                )

                // 3. Expressive 5-Mode Selector Strip
                ExpressiveInputModeSelector(
                    activeMode = settings.inputMode,
                    onSelectMode = { onSettingsChange(settings.copy(inputMode = it)) }
                )

                // 4. Mode-Specific Configuration Panels (Crossfades smoothly on mode switch)
                AnimatedContent(
                    targetState = settings.inputMode,
                    transitionSpec = {
                        fadeIn(spring(stiffness = Spring.StiffnessMediumLow)) togetherWith
                            fadeOut(spring(stiffness = Spring.StiffnessMedium))
                    },
                    label = "xboxModeSettingsPanel"
                ) { mode ->
                    val modePalette = NexpadTheme.colors.forMode(mode)
                    when (mode) {
                        InputMode.VELOCITY -> PrecisionAimingPanel(
                            config = settings.precisionAiming,
                            palette = modePalette,
                            onUpdate = { onSettingsChange(settings.copy(precisionAiming = it)) }
                        )

                        InputMode.ABSOLUTE_TILT -> SteeringWheelPanel(
                            config = settings.steeringWheel,
                            palette = modePalette,
                            onUpdate = { onSettingsChange(settings.copy(steeringWheel = it)) }
                        )

                        InputMode.AIR_MOUSE -> FlightStickPanel(
                            config = settings.flightStick,
                            palette = modePalette,
                            onUpdate = { onSettingsChange(settings.copy(flightStick = it)) }
                        )

                        InputMode.GESTURE -> CombatGesturesPanel(
                            config = settings.combatGestures,
                            palette = modePalette,
                            onUpdate = { onSettingsChange(settings.copy(combatGestures = it)) }
                        )

                        InputMode.RAW_UNCALIBRATED -> RawSiliconPanel(
                            config = settings.rawSilicon,
                            palette = modePalette,
                            onUpdate = { onSettingsChange(settings.copy(rawSilicon = it)) }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Backward compatibility wrapper for [XboxMotionSettingsSection].
 */
@Composable
fun GyroSettingsSection(
    settings: GyroSettings,
    onSettingsChange: (GyroSettings) -> Unit,
    activeController: ControllerType,
    rawGyroX: Float,
    rawGyroY: Float,
    rawGyroZ: Float,
    rawAccelX: Float,
    rawAccelY: Float,
    rawAccelZ: Float,
    processedYaw: Float,
    processedPitch: Float,
    onRecalibrate: () -> Unit
) {
    if (activeController == ControllerType.XBOX_360) {
        XboxMotionSettingsSection(
            settings = settings,
            onSettingsChange = onSettingsChange,
            rawGyroX = rawGyroX,
            rawGyroY = rawGyroY,
            rawGyroZ = rawGyroZ,
            rawAccelX = rawAccelX,
            rawAccelY = rawAccelY,
            rawAccelZ = rawAccelZ,
            processedYaw = processedYaw,
            processedPitch = processedPitch,
            onRecalibrate = onRecalibrate
        )
    }
}
