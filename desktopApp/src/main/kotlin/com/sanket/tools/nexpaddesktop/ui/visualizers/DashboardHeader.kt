package com.sanket.tools.nexpaddesktop.ui.visualizers

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.sanket.tools.nexpaddesktop.model.FlightStickAxisTarget
import com.sanket.tools.nexpaddesktop.model.GyroSettings
import com.sanket.tools.nexpaddesktop.model.InputMode
import com.sanket.tools.nexpaddesktop.model.XboxTargetStick
import com.sanket.tools.nexpaddesktop.ui.theme.ModeAccentPair
import com.sanket.tools.nexpaddesktop.ui.theme.NexpadTheme
import kotlin.math.abs
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.sign
import kotlin.math.sin
import kotlin.math.sqrt

private const val RAD_TO_DEG = 57.29578f

@Composable
fun DashboardHeader(
    settings: GyroSettings,
    rawGyroX: Float = 0f,
    rawGyroY: Float = 0f,
    rawGyroZ: Float = 0f,
    rawAccelX: Float = 0f,
    rawAccelY: Float = 0f,
    rawAccelZ: Float = 0f,
    rawGravityX: Float = 0f,
    rawGravityY: Float = 0f,
    rawGravityZ: Float = 0f,
    linearAccelX: Float = 0f,
    linearAccelY: Float = 0f,
    linearAccelZ: Float = 0f,
    rawUncalGyroX: Float = 0f,
    rawUncalGyroY: Float = 0f,
    rawUncalGyroZ: Float = 0f,
    processedYaw: Float = 0f,
    processedPitch: Float = 0f,
    flightPitchDeg: Float = 0f,
    flightRollDeg: Float = 0f,
    flightYawDeg: Float = 0f
) {
    // 1. Resolve effective raw / uncalibrated gyroscope rates
    val hasUncal = rawUncalGyroX != 0f || rawUncalGyroY != 0f || rawUncalGyroZ != 0f
    val effGyroX = if (hasUncal) rawUncalGyroX else rawGyroX
    val effGyroY = if (hasUncal) rawUncalGyroY else rawGyroY
    val effGyroZ = if (hasUncal) rawUncalGyroZ else rawGyroZ

    val gyroPitchRate = effGyroX * RAD_TO_DEG
    val gyroRollRate = effGyroY * RAD_TO_DEG
    val gyroYawRate = effGyroZ * RAD_TO_DEG

    // 2. Resolve effective gravity vector
    val hasRawGrav = rawGravityX != 0f || rawGravityY != 0f || rawGravityZ != 0f
    val effGravityX = if (hasRawGrav) rawGravityX else rawAccelX
    val effGravityY = if (hasRawGrav) rawGravityY else rawAccelY
    val effGravityZ = if (hasRawGrav) rawGravityZ else rawAccelZ

    val gravMag = sqrt(effGravityX * effGravityX + effGravityY * effGravityY + effGravityZ * effGravityZ)
    val hasGrav = gravMag > 1.5f
    val gravityPitchDeg = if (hasGrav) asin((effGravityY / gravMag).coerceIn(-1f, 1f)) * RAD_TO_DEG else 0f
    val gravityRollDeg = if (hasGrav) asin((effGravityX / gravMag).coerceIn(-1f, 1f)) * RAD_TO_DEG else 0f

    // 3. Resolve effective linear acceleration / kinetic strike force
    val hasLinAccel = linearAccelX != 0f || linearAccelY != 0f || linearAccelZ != 0f
    val effLinX = if (hasLinAccel) linearAccelX else (rawAccelX - effGravityX)
    val effLinY = if (hasLinAccel) linearAccelY else (rawAccelY - effGravityY)
    val effLinZ = if (hasLinAccel) linearAccelZ else (rawAccelZ - effGravityZ)

    val linearMag = sqrt(effLinX * effLinX + effLinY * effLinY + effLinZ * effLinZ)
    val totalAccelMag = sqrt(rawAccelX * rawAccelX + rawAccelY * rawAccelY + rawAccelZ * rawAccelZ)
    val effectiveCombatForce = if (linearMag > 1.0f) linearMag else totalAccelMag

    val isStrikeDetected = effectiveCombatForce >= settings.combatGestures.flickThreshold
    val isShakingDetected = effectiveCombatForce >= settings.combatGestures.shakeThreshold

    // 4. Compute Target 3D Spatial Angles (Pitch, Roll, Yaw)
    var displayPitch = 0f
    var displayRoll = 0f
    var displayYaw = 0f

    when (settings.inputMode) {
        InputMode.ABSOLUTE_TILT -> {
            val effTiltX = when {
                rawGravityX != 0f -> rawGravityX
                rawAccelX != 0f -> rawAccelX
                else -> (processedYaw / 200f) * 9.8f
            }
            val maxLock = settings.steeringWheel.maxTiltAngle.coerceAtLeast(1f)
            val steeringAngle = if (hasGrav && gravMag > 3.0f) {
                (asin((effTiltX / gravMag).coerceIn(-1f, 1f)) * RAD_TO_DEG).coerceIn(-maxLock, maxLock)
            } else if (processedYaw != 0f) {
                ((processedYaw / 200.0f) * maxLock).coerceIn(-maxLock, maxLock)
            } else 0f

            displayYaw = steeringAngle.coerceIn(-85f, 85f)
            displayRoll = (steeringAngle * 0.35f).coerceIn(-30f, 30f)
            displayPitch = (gravityPitchDeg * 0.55f).coerceIn(-35f, 35f)
        }

        InputMode.AIR_MOUSE -> {
            if (flightPitchDeg != 0f || flightRollDeg != 0f || flightYawDeg != 0f) {
                displayPitch = (-flightPitchDeg).coerceIn(-65f, 65f)
                displayRoll = flightRollDeg.coerceIn(-65f, 65f)
                displayYaw = flightYawDeg.coerceIn(-65f, 65f)
            } else {
                val flightPitch = (processedPitch / 200.0f) * settings.flightStick.maxPitchAngle
                val flightRoll = (processedYaw / 200.0f) * settings.flightStick.maxRollAngle
                displayPitch = (-flightPitch).coerceIn(-65f, 65f)
                displayRoll = flightRoll.coerceIn(-65f, 65f)
                displayYaw = (rawGyroZ * RAD_TO_DEG * 0.1f).coerceIn(-30f, 30f)
            }
        }

        InputMode.GESTURE -> {
            var pPitch = gravityPitchDeg
            var pRoll = gravityRollDeg
            val pYaw = gyroYawRate * 0.25f

            if (isStrikeDetected) {
                val strikeIntensity = ((effectiveCombatForce - settings.combatGestures.flickThreshold) / 5f).coerceIn(0f, 2f)
                val flickDirY = if (effLinY != 0f) sign(effLinY) else 1f
                pPitch += flickDirY * (14f + strikeIntensity * 8f)
            }

            if (abs(gravityPitchDeg) >= settings.combatGestures.tiltPitchThreshold) {
                pPitch = (pPitch * 1.25f).coerceIn(-65f, 65f)
            }
            if (abs(gravityRollDeg) >= settings.combatGestures.tiltRollThreshold) {
                pRoll = (pRoll * 1.2f).coerceIn(-65f, 65f)
            }

            displayPitch = pPitch.coerceIn(-65f, 65f)
            displayRoll = pRoll.coerceIn(-65f, 65f)
            displayYaw = pYaw.coerceIn(-60f, 60f)
        }

        InputMode.RAW_UNCALIBRATED -> {
            displayPitch = (gravityPitchDeg * 0.75f + gyroPitchRate * 0.20f).coerceIn(-55f, 55f)
            displayRoll = (gravityRollDeg * 0.75f + gyroRollRate * 0.20f).coerceIn(-55f, 55f)
            displayYaw = (gyroYawRate * 0.30f).coerceIn(-60f, 60f)
        }

        InputMode.VELOCITY -> {
            displayPitch = (gravityPitchDeg * 0.7f + gyroPitchRate * 0.15f).coerceIn(-50f, 50f)
            displayRoll = (gravityRollDeg * 0.7f + gyroRollRate * 0.15f).coerceIn(-50f, 50f)
            displayYaw = (gyroYawRate * 0.25f).coerceIn(-50f, 50f)
        }
    }

    val targetPitch = if (settings.invertY) -displayPitch else displayPitch
    val targetRoll = if (settings.invertRoll) -displayRoll else displayRoll
    val targetYaw = if (settings.invertX) -displayYaw else displayYaw

    // 5. Resolve Semantic Theme Tokens & Animated Accents
    val modePalette = NexpadTheme.colors.forMode(settings.inputMode)
    val animatedAccent by animateColorAsState(
        targetValue = modePalette.accent,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "modeAccent"
    )

    // 6. Tonal Container Surface (No cluttered gradients, subtle top accent highlight)
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainer,
        border = BorderStroke(1.dp, animatedAccent.copy(alpha = 0.28f))
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val isNarrow = maxWidth < 580.dp
            val isCompact = maxWidth < 760.dp
            val padH = if (isNarrow) 16.dp else if (isCompact) 18.dp else 24.dp
            val padV = if (isCompact) 16.dp else 20.dp

            if (isNarrow) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = padH, vertical = padV),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    TelemetryPanel(
                        settings = settings,
                        modePalette = modePalette,
                        processedYaw = processedYaw,
                        processedPitch = processedPitch,
                        displayYaw = displayYaw,
                        finalPitch = targetPitch,
                        finalRoll = targetRoll,
                        gravityPitchDeg = gravityPitchDeg,
                        gravityRollDeg = gravityRollDeg,
                        effectiveCombatForce = effectiveCombatForce,
                        isStrikeDetected = isStrikeDetected,
                        isShakingDetected = isShakingDetected,
                        gyroPitchRate = gyroPitchRate,
                        gyroRollRate = gyroRollRate,
                        gyroYawRate = gyroYawRate,
                        isCompact = true
                    )

                    Gamepad3DModelViewport(
                        pitchDeg = targetPitch,
                        rollDeg = targetRoll,
                        yawDeg = targetYaw,
                        accentColor = animatedAccent,
                        inputMode = settings.inputMode,
                        isStrikeActive = isStrikeDetected,
                        isShakingActive = isShakingDetected,
                        isCompact = true
                    )
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = padH, vertical = padV),
                    horizontalArrangement = Arrangement.spacedBy(if (isCompact) 16.dp else 24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        TelemetryPanel(
                            settings = settings,
                            modePalette = modePalette,
                            processedYaw = processedYaw,
                            processedPitch = processedPitch,
                            displayYaw = displayYaw,
                            finalPitch = targetPitch,
                            finalRoll = targetRoll,
                            gravityPitchDeg = gravityPitchDeg,
                            gravityRollDeg = gravityRollDeg,
                            effectiveCombatForce = effectiveCombatForce,
                            isStrikeDetected = isStrikeDetected,
                            isShakingDetected = isShakingDetected,
                            gyroPitchRate = gyroPitchRate,
                            gyroRollRate = gyroRollRate,
                            gyroYawRate = gyroYawRate,
                            isCompact = isCompact
                        )
                    }

                    Gamepad3DModelViewport(
                        pitchDeg = targetPitch,
                        rollDeg = targetRoll,
                        yawDeg = targetYaw,
                        accentColor = animatedAccent,
                        inputMode = settings.inputMode,
                        isStrikeActive = isStrikeDetected,
                        isShakingActive = isShakingDetected,
                        isCompact = isCompact
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// MODE-AWARE TELEMETRY PANEL WITH EXPRESSIVE CROSSFADE
// ─────────────────────────────────────────────────────────────────────────────

private data class ModeHeaderMeta(
    val icon: ImageVector,
    val title: String,
    val subtitle: String
)

private fun InputMode.toHeaderMeta(): ModeHeaderMeta = when (this) {
    InputMode.VELOCITY -> ModeHeaderMeta(
        icon = Icons.Default.GpsFixed,
        title = "Gyro Precision Aiming",
        subtitle = "1:1 Linear Velocity Camera Stream"
    )
    InputMode.ABSOLUTE_TILT -> ModeHeaderMeta(
        icon = Icons.Default.DirectionsCar,
        title = "Virtual Steering Wheel",
        subtitle = "Gravity-Vector Steering Emulation"
    )
    InputMode.AIR_MOUSE -> ModeHeaderMeta(
        icon = Icons.Default.Flight,
        title = "3D Flight Stick",
        subtitle = "6-DoF Aviation Attitude Yoke"
    )
    InputMode.GESTURE -> ModeHeaderMeta(
        icon = Icons.Default.Bolt,
        title = "Combat Gestures",
        subtitle = "Kinetic Acceleration & Posture Triggers"
    )
    InputMode.RAW_UNCALIBRATED -> ModeHeaderMeta(
        icon = Icons.Default.Memory,
        title = "Raw Silicon Gyro",
        subtitle = "Esports Direct Unfiltered MEMS Stream"
    )
}

@Composable
private fun TelemetryPanel(
    settings: GyroSettings,
    modePalette: ModeAccentPair,
    processedYaw: Float,
    processedPitch: Float,
    displayYaw: Float,
    finalPitch: Float,
    finalRoll: Float,
    gravityPitchDeg: Float,
    gravityRollDeg: Float,
    effectiveCombatForce: Float,
    isStrikeDetected: Boolean,
    isShakingDetected: Boolean,
    gyroPitchRate: Float,
    gyroRollRate: Float,
    gyroYawRate: Float,
    isCompact: Boolean
) {
    val meta = settings.inputMode.toHeaderMeta()
    val statusColor = NexpadTheme.colors.statusConnected

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Expressive Top Header Row with Tonal Icon Container
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f, fill = false),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = modePalette.container,
                    modifier = Modifier.size(38.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = meta.icon,
                            contentDescription = null,
                            tint = modePalette.accent,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f, fill = false)) {
                    Text(
                        text = meta.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = meta.subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Live Active Pill
            Surface(
                shape = CircleShape,
                color = statusColor.copy(alpha = 0.14f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(statusColor)
                    )
                    Text(
                        text = "LIVE",
                        style = MaterialTheme.typography.labelSmall,
                        color = statusColor
                    )
                }
            }
        }

        // 2. Smooth Crossfade Between Input Modes
        AnimatedContent(
            targetState = settings.inputMode,
            transitionSpec = { fadeIn(spring()) togetherWith fadeOut(spring()) },
            label = "modeTelemetryTransition"
        ) { mode ->
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                when (mode) {
                    InputMode.VELOCITY -> {
                        val aimSpeed = sqrt(processedYaw * processedYaw + processedPitch * processedPitch)
                        val speedBadge = when {
                            aimSpeed > 180f -> "FAST FLICK"
                            aimSpeed > 60f -> "RAPID TRACKING"
                            aimSpeed > 8f -> "PRECISION AIM"
                            aimSpeed > settings.deadzoneThreshold -> "MICRO AIM"
                            else -> "RESTING"
                        }

                        val targetStickName = when (settings.xboxTargetStick) {
                            XboxTargetStick.RIGHT_STICK -> "Right Stick (RS)"
                            XboxTargetStick.LEFT_STICK -> "Left Stick (LS)"
                            XboxTargetStick.NONE -> "Unmapped"
                        }

                        HeroMetricSection(
                            heroValue = "${String.format("%.1f", aimSpeed)} °/s",
                            heroLabel = "Combined Camera Velocity",
                            badgeText = speedBadge,
                            modePalette = modePalette,
                            chips = listOf(
                                "Target: $targetStickName",
                                if (settings.accelerationEnabled) "Curve: Accelerated" else "Curve: 1:1 Linear",
                                "Deadzone: ${String.format("%.1f", settings.deadzoneThreshold)}°/s"
                            )
                        )

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            val yawDir = when {
                                processedYaw > 2.0f -> "▶"
                                processedYaw < -2.0f -> "◀"
                                else -> "•"
                            }
                            val pitchDir = when {
                                processedPitch > 2.0f -> "▲"
                                processedPitch < -2.0f -> "▼"
                                else -> "•"
                            }

                            TelemetryBar(
                                label = "Horizontal Yaw $yawDir",
                                value = abs(processedYaw) / settings.maxDpsYaw.coerceAtLeast(1f),
                                displayValue = "${String.format("%+.0f", processedYaw)}°/s",
                                color = NexpadTheme.colors.rollAxis,
                                labelWidth = if (isCompact) 130.dp else 150.dp
                            )

                            TelemetryBar(
                                label = "Vertical Pitch $pitchDir",
                                value = abs(processedPitch) / settings.maxDpsPitch.coerceAtLeast(1f),
                                displayValue = "${String.format("%+.0f", processedPitch)}°/s",
                                color = modePalette.accent,
                                labelWidth = if (isCompact) 130.dp else 150.dp
                            )
                        }
                    }

                    InputMode.ABSOLUTE_TILT -> {
                        val steeringAngle = displayYaw.toInt()
                        val steerStatus = when {
                            abs(steeringAngle) <= 2 -> "CENTERED"
                            steeringAngle > 0 -> "STEERING RIGHT ▶"
                            else -> "◀ STEERING LEFT"
                        }
                        val maxLock = settings.steeringWheel.maxTiltAngle.coerceAtLeast(1f)
                        val steerPct = (abs(displayYaw) / maxLock * 100f).coerceIn(0f, 100f)

                        val targetStickShort = when (settings.xboxTargetStick) {
                            XboxTargetStick.RIGHT_STICK -> "Right Stick (RS)"
                            XboxTargetStick.LEFT_STICK -> "Left Stick (LS)"
                            XboxTargetStick.NONE -> "Disabled"
                        }

                        HeroMetricSection(
                            heroValue = "${if (steeringAngle > 0) "+" else ""}$steeringAngle°",
                            heroLabel = "Wheel Rotation Angle",
                            badgeText = steerStatus,
                            modePalette = modePalette,
                            chips = listOf(
                                "Max Lock: ${maxLock.toInt()}°",
                                "Curve: ${String.format("%.1f", settings.steeringWheel.responseCurve)}x",
                                "Output: $targetStickShort"
                            )
                        )

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            TelemetryBar(
                                label = "Steering Lock Deflection",
                                value = steerPct / 100f,
                                displayValue = "${steerPct.toInt()}%",
                                color = modePalette.accent,
                                labelWidth = if (isCompact) 130.dp else 150.dp
                            )
                            TelemetryBar(
                                label = "Forward / Back Tilt",
                                value = abs(finalPitch) / 35f,
                                displayValue = "${String.format("%+.0f", finalPitch)}°",
                                color = NexpadTheme.colors.rollAxis,
                                labelWidth = if (isCompact) 130.dp else 150.dp
                            )
                        }
                    }

                    InputMode.AIR_MOUSE -> {
                        val pitchDegVal = (-finalPitch).toInt()
                        val rollDegVal = finalRoll.toInt()
                        val yawDegVal = displayYaw.toInt()
                        val hasYaw = settings.flightStick.yawTarget != FlightStickAxisTarget.NONE
                        val flightStatus = when {
                            abs(pitchDegVal) <= 3 && abs(rollDegVal) <= 3 && (!hasYaw || abs(yawDegVal) <= 3) -> "LEVEL FLIGHT"
                            pitchDegVal > 5 -> "CLIMBING ▲"
                            pitchDegVal < -5 -> "DIVING ▼"
                            rollDegVal > 5 -> "BANKING RIGHT ▶"
                            rollDegVal < -5 -> "◀ BANKING LEFT"
                            hasYaw && yawDegVal > 5 -> "RUDDER RIGHT ▶"
                            hasYaw && yawDegVal < -5 -> "◀ RUDDER LEFT"
                            else -> "LEVEL FLIGHT"
                        }

                        val chipsList = mutableListOf(
                            "Pitch: ${settings.flightStick.pitchTarget.displayName}",
                            "Roll: ${settings.flightStick.rollTarget.displayName}"
                        )
                        if (hasYaw) {
                            chipsList.add("Yaw: ${settings.flightStick.yawTarget.displayName}")
                        }
                        chipsList.add("Deadzone: ${String.format("%.1f", settings.flightStick.deadzone)}°")

                        val heroVal = if (hasYaw) {
                            "${if (pitchDegVal > 0) "+" else ""}$pitchDegVal° / ${if (rollDegVal > 0) "+" else ""}$rollDegVal° / ${if (yawDegVal > 0) "+" else ""}$yawDegVal°"
                        } else {
                            "${if (pitchDegVal > 0) "+" else ""}$pitchDegVal° / ${if (rollDegVal > 0) "+" else ""}$rollDegVal°"
                        }

                        HeroMetricSection(
                            heroValue = heroVal,
                            heroLabel = if (hasYaw) "Pitch / Roll / Rudder" else "Pitch (Elevator) / Roll (Aileron)",
                            badgeText = flightStatus,
                            modePalette = modePalette,
                            chips = chipsList
                        )

                        val pitchPct = (abs(finalPitch) / settings.flightStick.maxPitchAngle.coerceAtLeast(1f)).coerceIn(0f, 1f)
                        val rollPct = (abs(finalRoll) / settings.flightStick.maxRollAngle.coerceAtLeast(1f)).coerceIn(0f, 1f)

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            TelemetryBar(
                                label = "Elevator Deflection",
                                value = pitchPct,
                                displayValue = "${(pitchPct * 100).toInt()}%",
                                color = modePalette.accent,
                                labelWidth = if (isCompact) 125.dp else 140.dp
                            )
                            TelemetryBar(
                                label = "Aileron Bank",
                                value = rollPct,
                                displayValue = "${(rollPct * 100).toInt()}%",
                                color = NexpadTheme.colors.rollAxis,
                                labelWidth = if (isCompact) 125.dp else 140.dp
                            )
                            if (hasYaw) {
                                val yawPct = (abs(displayYaw) / settings.flightStick.maxYawAngle.coerceAtLeast(1f)).coerceIn(0f, 1f)
                                TelemetryBar(
                                    label = "Rudder Yaw",
                                    value = yawPct,
                                    displayValue = "${(yawPct * 100).toInt()}%",
                                    color = NexpadTheme.colors.yawAxis,
                                    labelWidth = if (isCompact) 125.dp else 140.dp
                                )
                            }
                        }
                    }

                    InputMode.GESTURE -> {
                        val strikeStatus = when {
                            isStrikeDetected -> "STRIKE TRIGGERED ⚡"
                            isShakingDetected -> "SHAKE ACTIVE ⚡"
                            abs(gravityPitchDeg) >= settings.combatGestures.tiltPitchThreshold ||
                                abs(gravityRollDeg) >= settings.combatGestures.tiltRollThreshold -> "POSTURE ACTIVE"
                            else -> "READY"
                        }

                        HeroMetricSection(
                            heroValue = "${String.format("%.1f", effectiveCombatForce)} m/s²",
                            heroLabel = "Live Kinetic Impulse Force",
                            badgeText = strikeStatus,
                            modePalette = modePalette,
                            chips = listOf(
                                "Flick Trigger: ${settings.combatGestures.flickThreshold} m/s²",
                                "Tilt Trigger: ${settings.combatGestures.tiltPitchThreshold.toInt()}°",
                                "Sticks: 100% Untouched"
                            )
                        )

                        val flickRatio = effectiveCombatForce / settings.combatGestures.flickThreshold.coerceAtLeast(1f)
                        val postureRatio = maxOf(
                            abs(gravityPitchDeg) / settings.combatGestures.tiltPitchThreshold.coerceAtLeast(1f),
                            abs(gravityRollDeg) / settings.combatGestures.tiltRollThreshold.coerceAtLeast(1f)
                        )

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            TelemetryBar(
                                label = "Kinetic Flick Threshold",
                                value = flickRatio,
                                displayValue = "${(flickRatio.coerceAtMost(1f) * 100).toInt()}%",
                                color = modePalette.accent,
                                labelWidth = if (isCompact) 130.dp else 150.dp
                            )
                            TelemetryBar(
                                label = "Guard Posture Angle",
                                value = postureRatio,
                                displayValue = "${maxOf(abs(gravityPitchDeg), abs(gravityRollDeg)).toInt()}°",
                                color = NexpadTheme.colors.pitchAxis,
                                labelWidth = if (isCompact) 130.dp else 150.dp
                            )
                        }
                    }

                    InputMode.RAW_UNCALIBRATED -> {
                        val rawMag = sqrt(gyroPitchRate * gyroPitchRate + gyroRollRate * gyroRollRate + gyroYawRate * gyroYawRate)

                        HeroMetricSection(
                            heroValue = "${String.format("%.1f", rawMag)} °/s",
                            heroLabel = "Unfiltered MEMS Angular Velocity",
                            badgeText = "ZERO FILTERING",
                            modePalette = modePalette,
                            chips = listOf(
                                "Pitch: ${String.format("%+.1f", gyroPitchRate)}°/s",
                                "Roll: ${String.format("%+.1f", gyroRollRate)}°/s",
                                "Yaw: ${String.format("%+.1f", gyroYawRate)}°/s"
                            )
                        )

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            TelemetryBar(
                                label = "Yaw Rate (Max ${settings.maxDpsYaw.toInt()}°/s)",
                                value = abs(gyroYawRate) / settings.maxDpsYaw.coerceAtLeast(1f),
                                displayValue = "${String.format("%+.0f", gyroYawRate)}°/s",
                                color = modePalette.accent,
                                labelWidth = if (isCompact) 135.dp else 155.dp
                            )
                            TelemetryBar(
                                label = "Pitch Rate (Max ${settings.maxDpsPitch.toInt()}°/s)",
                                value = abs(gyroPitchRate) / settings.maxDpsPitch.coerceAtLeast(1f),
                                displayValue = "${String.format("%+.0f", gyroPitchRate)}°/s",
                                color = NexpadTheme.colors.rollAxis,
                                labelWidth = if (isCompact) 135.dp else 155.dp
                            )
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 3D MOBILE GAMING CONTROLLER VIEWPORT WITH SPRING PHYSICS
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun Gamepad3DModelViewport(
    pitchDeg: Float,
    rollDeg: Float,
    yawDeg: Float,
    accentColor: Color,
    inputMode: InputMode,
    isStrikeActive: Boolean = false,
    isShakingActive: Boolean = false,
    isCompact: Boolean = false
) {
    val vpWidth = if (isCompact) 220.dp else 276.dp
    val vpHeight = if (isCompact) 168.dp else 204.dp
    val modelScale = if (isCompact) 0.82f else 1.0f

    // Smooth Spring Physics for 3D Orientation & Combat Recoil
    val smoothPitch by animateFloatAsState(
        targetValue = pitchDeg,
        animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMedium),
        label = "pitchSpring"
    )
    val smoothRoll by animateFloatAsState(
        targetValue = rollDeg,
        animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMedium),
        label = "rollSpring"
    )
    val smoothYaw by animateFloatAsState(
        targetValue = yawDeg,
        animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMedium),
        label = "yawSpring"
    )
    val strikeKickbackY by animateFloatAsState(
        targetValue = if (isStrikeActive) -16f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessHigh),
        label = "strikeKickback"
    )

    val wellColor = MaterialTheme.colorScheme.surfaceContainerLowest
    val gridColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f)
    val chassisTopColor = MaterialTheme.colorScheme.surfaceContainerHigh
    val chassisBottomColor = MaterialTheme.colorScheme.surfaceContainerLow

    // Recessed Viewport Well using surfaceContainerLowest (no heavy border required)
    Surface(
        modifier = Modifier.size(vpWidth, vpHeight),
        shape = MaterialTheme.shapes.medium,
        color = wellColor,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Box(contentAlignment = Alignment.Center) {
            // 1. Perspective Floor Grid
            Canvas(modifier = Modifier.fillMaxSize()) {
                val cx = size.width / 2f
                val cy = size.height / 2f
                for (i in -3..3) {
                    val offset = i * 28.dp.toPx()
                    drawLine(
                        color = gridColor,
                        start = Offset(cx + offset * 0.7f, cy + 48.dp.toPx()),
                        end = Offset(cx + offset * 1.5f, size.height),
                        strokeWidth = 1.dp.toPx()
                    )
                }
                drawLine(gridColor, Offset(0f, cy + 58.dp.toPx()), Offset(size.width, cy + 58.dp.toPx()), 1.dp.toPx())
                drawLine(gridColor, Offset(0f, cy + 78.dp.toPx()), Offset(size.width, cy + 78.dp.toPx()), 1.dp.toPx())
            }

            // 2. Floating 3D Controller Canvas
            Box(
                modifier = Modifier
                    .size(230.dp, 120.dp)
                    .graphicsLayer {
                        val shakeJitterX = if (isShakingActive) sin(System.currentTimeMillis() * 0.045f) * 7f else 0f
                        val shakeJitterY = if (isShakingActive) cos(System.currentTimeMillis() * 0.045f) * 5f else 0f

                        scaleX = modelScale
                        scaleY = modelScale
                        rotationX = smoothPitch
                        rotationY = smoothYaw
                        rotationZ = smoothRoll
                        translationX = shakeJitterX
                        translationY = strikeKickbackY + shakeJitterY
                        cameraDistance = 14f * density
                    },
                contentAlignment = Alignment.Center
            ) {
                val strikeColor = NexpadTheme.colors.combatGesture.accent
                val warningColor = NexpadTheme.colors.statusWarning
                val activeRimColor = when {
                    isStrikeActive -> strikeColor
                    isShakingActive -> warningColor
                    else -> accentColor
                }

                val chassisBrush = remember(chassisTopColor, chassisBottomColor) {
                    Brush.verticalGradient(listOf(chassisTopColor, chassisBottomColor))
                }
                val rimBrush = remember(activeRimColor, chassisTopColor) {
                    Brush.linearGradient(
                        listOf(
                            activeRimColor.copy(alpha = 0.9f),
                            chassisTopColor,
                            activeRimColor.copy(alpha = 0.45f)
                        )
                    )
                }
                val wavePath = remember { Path() }

                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    val cx = w / 2f
                    val cy = h / 2f

                    val depthOffsetX = (smoothYaw / 45f) * 6.dp.toPx()
                    val depthOffsetY = (-smoothPitch / 45f) * 6.dp.toPx()

                    // Extruded Underbody Shadow
                    drawRoundRect(
                        color = wellColor,
                        topLeft = Offset(-depthOffsetX * 0.8f, -depthOffsetY * 0.8f),
                        size = Size(w, h),
                        cornerRadius = CornerRadius(16.dp.toPx(), 16.dp.toPx())
                    )

                    // 3D Edge Chamfer Wall
                    drawRoundRect(
                        color = chassisTopColor,
                        topLeft = Offset(-depthOffsetX * 0.4f, -depthOffsetY * 0.4f),
                        size = Size(w, h),
                        cornerRadius = CornerRadius(16.dp.toPx(), 16.dp.toPx()),
                        style = Stroke(width = 3.dp.toPx())
                    )

                    // Top Shoulder Bumpers (LB / RB)
                    val bumperWidth = 52.dp.toPx()
                    val bumperHeight = 8.dp.toPx()
                    val bumperCorner = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                    listOf(24.dp.toPx(), w - 24.dp.toPx() - bumperWidth).forEach { leftX ->
                        drawRoundRect(
                            color = chassisTopColor,
                            topLeft = Offset(leftX, -6.dp.toPx()),
                            size = Size(bumperWidth, bumperHeight),
                            cornerRadius = bumperCorner
                        )
                        drawRoundRect(
                            color = accentColor.copy(alpha = 0.65f),
                            topLeft = Offset(leftX, -6.dp.toPx()),
                            size = Size(bumperWidth, bumperHeight),
                            cornerRadius = bumperCorner,
                            style = Stroke(width = 1.dp.toPx())
                        )
                    }

                    // Main Chassis Faceplate
                    drawRoundRect(
                        brush = chassisBrush,
                        topLeft = Offset.Zero,
                        size = Size(w, h),
                        cornerRadius = CornerRadius(16.dp.toPx(), 16.dp.toPx())
                    )
                    drawRoundRect(
                        brush = rimBrush,
                        topLeft = Offset.Zero,
                        size = Size(w, h),
                        cornerRadius = CornerRadius(16.dp.toPx(), 16.dp.toPx()),
                        style = Stroke(width = if (isStrikeActive) 2.4f.dp.toPx() else 1.6f.dp.toPx())
                    )

                    // Center Tactical Screen Display
                    val screenW = 98.dp.toPx()
                    val screenH = 82.dp.toPx()
                    val screenLeft = cx - screenW / 2f
                    val screenTop = cy - screenH / 2f

                    drawRoundRect(
                        color = wellColor,
                        topLeft = Offset(screenLeft, screenTop),
                        size = Size(screenW, screenH),
                        cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
                    )

                    // Dynamic Screen Reticle
                    val reticleOffsetX = (smoothRoll / 45f) * 16.dp.toPx()
                    val reticleOffsetY = (smoothPitch / 45f) * 16.dp.toPx()
                    val reticleCx = (cx + reticleOffsetX).coerceIn(screenLeft + 12.dp.toPx(), screenLeft + screenW - 12.dp.toPx())
                    val reticleCy = (cy + reticleOffsetY).coerceIn(screenTop + 12.dp.toPx(), screenTop + screenH - 12.dp.toPx())

                    // Crosshair Grid
                    drawLine(gridColor, Offset(screenLeft, cy), Offset(screenLeft + screenW, cy), 1.dp.toPx())
                    drawLine(gridColor, Offset(cx, screenTop), Offset(cx, screenTop + screenH), 1.dp.toPx())

                    when (inputMode) {
                        InputMode.ABSOLUTE_TILT -> {
                            val arcRadius = 24.dp.toPx()
                            drawArc(
                                color = accentColor.copy(alpha = 0.35f),
                                startAngle = 180f,
                                sweepAngle = 180f,
                                useCenter = false,
                                topLeft = Offset(cx - arcRadius, cy - arcRadius + 5.dp.toPx()),
                                size = Size(arcRadius * 2, arcRadius * 2),
                                style = Stroke(2.dp.toPx())
                            )
                            val steerAngleRad = (smoothYaw.coerceIn(-80f, 80f) - 90f) * (Math.PI / 180.0).toFloat()
                            val tickLen = 20.dp.toPx()
                            drawLine(
                                color = accentColor,
                                start = Offset(cx, cy + 5.dp.toPx()),
                                end = Offset(
                                    cx + cos(steerAngleRad) * tickLen,
                                    cy + 5.dp.toPx() + sin(steerAngleRad) * tickLen
                                ),
                                strokeWidth = 2.dp.toPx(),
                                cap = StrokeCap.Round
                            )
                        }

                        InputMode.RAW_UNCALIBRATED -> {
                            wavePath.reset()
                            wavePath.moveTo(screenLeft + 6.dp.toPx(), cy)
                            wavePath.lineTo(screenLeft + screenW * 0.30f, cy)
                            wavePath.lineTo(screenLeft + screenW * 0.40f, cy - 10.dp.toPx())
                            wavePath.lineTo(screenLeft + screenW * 0.50f, cy + 12.dp.toPx())
                            wavePath.lineTo(screenLeft + screenW * 0.60f, cy - 8.dp.toPx())
                            wavePath.lineTo(screenLeft + screenW * 0.70f, cy)
                            wavePath.lineTo(screenLeft + screenW - 6.dp.toPx(), cy)
                            drawPath(wavePath, color = accentColor.copy(alpha = 0.85f), style = Stroke(1.5f.dp.toPx()))
                        }

                        else -> {
                            val ringRadius = if (isStrikeActive) 20.dp.toPx() else 8.dp.toPx()
                            drawCircle(
                                color = activeRimColor.copy(alpha = 0.55f),
                                radius = ringRadius,
                                center = Offset(reticleCx, reticleCy),
                                style = Stroke(1.5f.dp.toPx())
                            )
                            drawCircle(
                                color = Color.White,
                                radius = 2.5f.dp.toPx(),
                                center = Offset(reticleCx, reticleCy)
                            )
                        }
                    }

                    // Left Stick (LS) & D-Pad (with restored subtraction operators!)
                    val lsCenter = Offset(32.dp.toPx(), 38.dp.toPx())
                    drawCircle(color = wellColor, radius = 14.dp.toPx(), center = lsCenter)
                    drawCircle(color = chassisTopColor, radius = 11.dp.toPx(), center = lsCenter)
                    drawCircle(color = accentColor.copy(alpha = 0.85f), radius = 2.dp.toPx(), center = lsCenter)

                    val dpadCenter = Offset(32.dp.toPx(), 84.dp.toPx())
                    val dpadArmW = 7.dp.toPx()
                    val dpadArmL = 22.dp.toPx()
                    drawRoundRect(
                        color = chassisTopColor,
                        topLeft = Offset(dpadCenter.x - dpadArmL / 2f, dpadCenter.y - dpadArmW / 2f),
                        size = Size(dpadArmL, dpadArmW),
                        cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
                    )
                    drawRoundRect(
                        color = chassisTopColor,
                        topLeft = Offset(dpadCenter.x - dpadArmW / 2f, dpadCenter.y - dpadArmL / 2f),
                        size = Size(dpadArmW, dpadArmL),
                        cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
                    )

                    // Right Action Diamond (ABXY) & Right Stick (RS)
                    val abxyCenter = Offset(w - 32.dp.toPx(), 38.dp.toPx())
                    val btnRadius = 3.8f.dp.toPx()
                    val dist = 8.5f.dp.toPx()
                    drawCircle(color = Color(0xFFFFD54F), radius = btnRadius, center = Offset(abxyCenter.x, abxyCenter.y - dist))
                    drawCircle(color = Color(0xFFFF5252), radius = btnRadius, center = Offset(abxyCenter.x + dist, abxyCenter.y))
                    drawCircle(color = Color(0xFF69F0AE), radius = btnRadius, center = Offset(abxyCenter.x, abxyCenter.y + dist))
                    drawCircle(color = Color(0xFF40C4FF), radius = btnRadius, center = Offset(abxyCenter.x - dist, abxyCenter.y))

                    val rsCenter = Offset(w - 32.dp.toPx(), 84.dp.toPx())
                    drawCircle(color = wellColor, radius = 14.dp.toPx(), center = rsCenter)
                    drawCircle(color = chassisTopColor, radius = 11.dp.toPx(), center = rsCenter)
                    drawCircle(color = accentColor.copy(alpha = 0.85f), radius = 2.dp.toPx(), center = rsCenter)
                }
            }

            // 3. Readable 11.sp Spatial Axis Pills (Replaces tiny 8.sp corner text)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(10.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    AxisReadoutTag("P", smoothPitch.toInt(), NexpadTheme.colors.pitchAxis)
                    AxisReadoutTag("R", smoothRoll.toInt(), NexpadTheme.colors.rollAxis)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    AxisReadoutTag("YAW", smoothYaw.toInt(), NexpadTheme.colors.yawAxis)
                }
            }
        }
    }
}

@Composable
private fun AxisReadoutTag(axisLabel: String, degrees: Int, color: Color) {
    Surface(
        shape = MaterialTheme.shapes.extraSmall,
        color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.85f)
    ) {
        Text(
            text = "$axisLabel: ${if (degrees > 0) "+" else ""}$degrees°",
            style = MaterialTheme.typography.labelSmall, // Strict 11.sp floor with tabular numerals
            color = color,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// TONAL M3 EXPRESSIVE METRIC & TELEMETRY COMPONENTS
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun HeroMetricSection(
    heroValue: String,
    heroLabel: String,
    badgeText: String,
    modePalette: ModeAccentPair,
    chips: List<String>
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = heroValue,
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.onSurface
            )

            Surface(
                shape = CircleShape,
                color = modePalette.container
            ) {
                Text(
                    text = badgeText,
                    style = MaterialTheme.typography.labelSmall,
                    color = modePalette.onContainer,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }

        Text(
            text = heroLabel,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(2.dp))

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            chips.forEach { MetricChip(it) }
        }
    }
}

@Composable
private fun MetricChip(label: String) {
    Surface(
        shape = MaterialTheme.shapes.extraSmall,
        color = MaterialTheme.colorScheme.surfaceContainerHigh
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            maxLines = 1,
            softWrap = false
        )
    }
}

@Composable
private fun TelemetryBar(
    label: String,
    value: Float,
    displayValue: String,
    color: Color,
    labelWidth: Dp = 140.dp
) {
    val animatedFill by animateFloatAsState(
        targetValue = value.coerceIn(0f, 1f),
        animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow),
        label = "telemetryBarFill"
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(labelWidth),
            maxLines = 1
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .height(6.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceContainerLowest)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(animatedFill)
                    .clip(CircleShape)
                    .background(color)
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = displayValue,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.width(52.dp),
            maxLines = 1
        )
    }
}