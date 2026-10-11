package com.sanket.tools.nexpaddesktop.ui.visualizers

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sanket.tools.nexpaddesktop.model.FlightStickAxisTarget
import com.sanket.tools.nexpaddesktop.model.GestureJoystickMode
import com.sanket.tools.nexpaddesktop.model.GyroSettings
import com.sanket.tools.nexpaddesktop.model.HorizontalAxis
import com.sanket.tools.nexpaddesktop.model.InputMode
import com.sanket.tools.nexpaddesktop.model.XboxTargetStick
import com.sanket.tools.nexpaddesktop.ui.theme.NeonPalette
import kotlin.math.abs
import kotlin.math.asin
import kotlin.math.sqrt

/**
 * High-Tech Master 3D Motion Telemetry & Controller Visualizer Header.
 *
 * Provides:
 * 1. Mode-specific live telemetry (Velocity, Steering, Flight Attitude, Combat Kinetic, or Raw MEMS).
 * 2. True 3D Mobile Gaming Controller Model with height, width, and depth (length/thickness).
 * 3. Dynamic spatial rotation (Pitch, Roll, Yaw) with ergonomic grips, bumpers, sticks, D-pad, and screen HUD.
 */
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
    processedPitch: Float = 0f
) {
    val RAD_TO_DEG = 57.2957795f

    // 1. Resolve effective raw / uncalibrated gyroscope rates
    val effGyroX = if (rawUncalGyroX != 0f || rawUncalGyroY != 0f || rawUncalGyroZ != 0f) rawUncalGyroX else rawGyroX
    val effGyroY = if (rawUncalGyroX != 0f || rawUncalGyroY != 0f || rawUncalGyroZ != 0f) rawUncalGyroY else rawGyroY
    val effGyroZ = if (rawUncalGyroX != 0f || rawUncalGyroY != 0f || rawUncalGyroZ != 0f) rawUncalGyroZ else rawGyroZ

    val gyroPitchRate = effGyroX * RAD_TO_DEG
    val gyroRollRate = effGyroY * RAD_TO_DEG
    val gyroYawRate = effGyroZ * RAD_TO_DEG

    // 2. Resolve effective gravity vector: prioritize rawGravity, fall back to rawAccel
    val effGravityX = if (rawGravityX != 0f || rawGravityY != 0f || rawGravityZ != 0f) rawGravityX else rawAccelX
    val effGravityY = if (rawGravityX != 0f || rawGravityY != 0f || rawGravityZ != 0f) rawGravityY else rawAccelY
    val effGravityZ = if (rawGravityX != 0f || rawGravityY != 0f || rawGravityZ != 0f) rawGravityZ else rawAccelZ

    val gravMag = sqrt(effGravityX * effGravityX + effGravityY * effGravityY + effGravityZ * effGravityZ)
    val hasGrav = gravMag > 1.5f
    val gravityPitchDeg = if (hasGrav) {
        asin((effGravityY / gravMag).coerceIn(-1f, 1f)) * RAD_TO_DEG
    } else 0f
    val gravityRollDeg = if (hasGrav) {
        asin((effGravityX / gravMag).coerceIn(-1f, 1f)) * RAD_TO_DEG
    } else 0f

    // 3. Resolve effective linear acceleration / kinetic strike force
    val effLinX = if (linearAccelX != 0f || linearAccelY != 0f || linearAccelZ != 0f) linearAccelX else (rawAccelX - effGravityX)
    val effLinY = if (linearAccelX != 0f || linearAccelY != 0f || linearAccelZ != 0f) linearAccelY else (rawAccelY - effGravityY)
    val effLinZ = if (linearAccelX != 0f || linearAccelY != 0f || linearAccelZ != 0f) linearAccelZ else (rawAccelZ - effGravityZ)

    val linearMag = sqrt(effLinX * effLinX + effLinY * effLinY + effLinZ * effLinZ)
    val totalAccelMag = sqrt(rawAccelX * rawAccelX + rawAccelY * rawAccelY + rawAccelZ * rawAccelZ)
    val effectiveCombatForce = if (linearMag > 1.0f) linearMag else totalAccelMag

    val isStrikeDetected = (effectiveCombatForce >= settings.gestureFlickThreshold)
    val isShakingDetected = (effectiveCombatForce >= settings.gestureShakeThreshold)

    // ─────────────────────────────────────────────────────────────────────────
    // 1. Physical 3D Spatial Angles (Pitch, Roll, Yaw)
    // ─────────────────────────────────────────────────────────────────────────
    var displayPitch = 0f
    var displayRoll = 0f
    var displayYaw = 0f

    when (settings.inputMode) {
        InputMode.ABSOLUTE_TILT -> {
            // Steering Angle: computed from gravity vector X / accel X, with full fallback to processedYaw
            val effTiltX = if (rawGravityX != 0f) rawGravityX else if (rawAccelX != 0f) rawAccelX else (processedYaw / 200f * 9.8f)
            val steeringAngle = if (hasGrav && gravMag > 3.0f) {
                (asin((effTiltX / gravMag).coerceIn(-1f, 1f)) * RAD_TO_DEG / settings.absoluteMaxTilt * settings.absoluteMaxTilt)
                    .coerceIn(-settings.absoluteMaxTilt, settings.absoluteMaxTilt)
            } else if (processedYaw != 0f) {
                (processedYaw / 200.0f * settings.absoluteMaxTilt).coerceIn(-settings.absoluteMaxTilt, settings.absoluteMaxTilt)
            } else 0f

            // In Compose graphicsLayer:
            // rotationZ spins like a real steering wheel in front of the player (displayYaw)
            // rotationY gives subtle 3D driver cockpit perspective bank into the turn (displayRoll)
            // rotationX tilts forward/back with wrist angle (displayPitch)
            displayYaw = steeringAngle.coerceIn(-85f, 85f)
            displayRoll = (steeringAngle * 0.35f).coerceIn(-30f, 30f)
            displayPitch = (gravityPitchDeg * 0.55f).coerceIn(-35f, 35f)
        }
        InputMode.AIR_MOUSE -> {
            val flightPitch = (processedPitch / 200.0f) * settings.airMouseMaxPitch
            val flightRoll = (processedYaw / 200.0f) * settings.airMouseMaxRollYaw
            displayPitch = (-flightPitch).coerceIn(-65f, 65f)
            displayRoll = flightRoll.coerceIn(-65f, 65f)
            displayYaw = (rawGyroZ * RAD_TO_DEG * 0.1f).coerceIn(-30f, 30f)
        }
        InputMode.GESTURE -> {
            // Combat posture tracking with visceral strike recoil impulse and defensive guard articulation
            var pPitch = gravityPitchDeg
            var pRoll = gravityRollDeg
            var pYaw = gyroYawRate * 0.25f

            // 1. Kinetic Strike / Flick Recoil Impulse
            if (isStrikeDetected) {
                val strikeIntensity = ((effectiveCombatForce - settings.gestureFlickThreshold) / 5f).coerceIn(0f, 2f)
                val flickDirY = if (effLinY != 0f) kotlin.math.sign(effLinY) else 1f
                pPitch += flickDirY * (14f + strikeIntensity * 8f)
            }

            // 2. Defensive Guard Posture Articulation
            if (gravityPitchDeg >= settings.gestureTiltPitchThreshold) {
                // Shield / Guard Up stance: exaggerated defensive block posture
                pPitch = (pPitch * 1.25f).coerceIn(-65f, 65f)
            } else if (gravityPitchDeg <= -settings.gestureTiltPitchThreshold) {
                // Low Guard / Crouch stance
                pPitch = (pPitch * 1.25f).coerceIn(-65f, 65f)
            }

            if (abs(gravityRollDeg) >= settings.gestureTiltRollThreshold) {
                // Side Dodge / Parry stance
                pRoll = (pRoll * 1.2f).coerceIn(-65f, 65f)
            }

            displayPitch = pPitch.coerceIn(-65f, 65f)
            displayRoll = pRoll.coerceIn(-65f, 65f)
            displayYaw = pYaw.coerceIn(-60f, 60f)
        }
        InputMode.RAW_UNCALIBRATED -> {
            // Raw Silicon Gyro Mode: combines the static hand gravity posture
            // with high-speed unfiltered raw silicon angular velocities so it holds angle AND responds instantly
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

    val finalPitch = if (settings.invertY) -displayPitch else displayPitch
    val finalRoll = if (settings.invertRoll) -displayRoll else displayRoll
    val finalYaw = if (settings.invertX) -displayYaw else displayYaw

    // ─────────────────────────────────────────────────────────────────────────
    // 2. Active Mode Accents & Titles
    // ─────────────────────────────────────────────────────────────────────────
    val accentColor = when (settings.inputMode) {
        InputMode.VELOCITY -> NeonPalette.Cyan
        InputMode.ABSOLUTE_TILT -> Color(0xFFFFB300) // Amber / Gold
        InputMode.AIR_MOUSE -> Color(0xFF00E676) // Emerald Green
        InputMode.GESTURE -> Color(0xFFFF1744) // Crimson Red
        InputMode.RAW_UNCALIBRATED -> Color(0xFFD500F9) // Neon Purple
    }

    val headerTitle = when (settings.inputMode) {
        InputMode.ABSOLUTE_TILT -> "🏎️ VIRTUAL STEERING WHEEL"
        InputMode.AIR_MOUSE -> "✈️ 3D FLIGHT STICK"
        InputMode.GESTURE -> "⚔️ COMBAT GESTURES"
        InputMode.RAW_UNCALIBRATED -> "⚡ RAW SILICON GYRO"
        else -> "🎯 GYRO PRECISION AIMING"
    }

    val headerSubtitle = when (settings.inputMode) {
        InputMode.ABSOLUTE_TILT -> "GRAVITY-VECTOR STEERING EMULATION"
        InputMode.AIR_MOUSE -> "6-DOF AVIATION ATTITUDE YOKE"
        InputMode.GESTURE -> "KINETIC ACCELERATION & COMBAT ACTIONS"
        InputMode.RAW_UNCALIBRATED -> "ESPORTS DIRECT UNFILTERED MEMS STREAM"
        else -> "1:1 LINEAR VELOCITY CAMERA AIMING"
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 3. Container Card Surface with Adaptive Responsive Constraints
    // ─────────────────────────────────────────────────────────────────────────
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF0F1424),
        border = BorderStroke(
            width = 1.5.dp,
            brush = Brush.horizontalGradient(
                listOf(
                    accentColor.copy(alpha = 0.8f),
                    NeonPalette.Purple.copy(alpha = 0.45f),
                    Color(0xFF1E2638)
                )
            )
        )
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFF111728),
                            Color(0xFF0A0E18)
                        )
                    )
                )
        ) {
            val isNarrow = maxWidth < 560.dp
            val isCompact = maxWidth < 740.dp
            val hPad = if (isNarrow) 12.dp else if (isCompact) 14.dp else 20.dp
            val vPad = if (isCompact) 14.dp else 18.dp

            if (isNarrow) {
                // Stack vertically on narrow screens (< 560dp) to prevent horizontal collision
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = hPad, vertical = vPad),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    TelemetryContent(
                        settings = settings,
                        accentColor = accentColor,
                        headerTitle = headerTitle,
                        headerSubtitle = headerSubtitle,
                        processedYaw = processedYaw,
                        processedPitch = processedPitch,
                        displayYaw = displayYaw,
                        finalPitch = finalPitch,
                        finalRoll = finalRoll,
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
                        pitchDeg = finalPitch,
                        rollDeg = finalRoll,
                        yawDeg = finalYaw,
                        accentColor = accentColor,
                        inputMode = settings.inputMode,
                        isStrikeActive = isStrikeDetected,
                        isShakingActive = isShakingDetected,
                        combatForceG = effectiveCombatForce,
                        isCompact = true
                    )
                }
            } else {
                // Side-by-side layout for standard / compact desktop width
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = hPad, vertical = vPad),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // ── Left Column: Live Mode-Specific Telemetry ──
                    Box(modifier = Modifier.weight(1f)) {
                        TelemetryContent(
                            settings = settings,
                            accentColor = accentColor,
                            headerTitle = headerTitle,
                            headerSubtitle = headerSubtitle,
                            processedYaw = processedYaw,
                            processedPitch = processedPitch,
                            displayYaw = displayYaw,
                            finalPitch = finalPitch,
                            finalRoll = finalRoll,
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

                    Spacer(modifier = Modifier.width(if (isCompact) 12.dp else 20.dp))

                    // ── Right Column: 3D Mobile Gaming Controller Model ──
                    Gamepad3DModelViewport(
                        pitchDeg = finalPitch,
                        rollDeg = finalRoll,
                        yawDeg = finalYaw,
                        accentColor = accentColor,
                        inputMode = settings.inputMode,
                        isStrikeActive = isStrikeDetected,
                        isShakingActive = isShakingDetected,
                        combatForceG = effectiveCombatForce,
                        isCompact = isCompact
                    )
                }
            }
        }
    }
}

/**
 * Responsive Live Telemetry Column supporting all 5 motion modes.
 */
@Composable
private fun TelemetryContent(
    settings: GyroSettings,
    accentColor: Color,
    headerTitle: String,
    headerSubtitle: String,
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
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(if (isCompact) 10.dp else 12.dp)
    ) {
        // Header Title & Mode Subtitle with active pill badge
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = headerTitle,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = if (isCompact) 15.sp else 16.5.sp,
                    color = accentColor,
                    letterSpacing = 0.5.sp,
                    maxLines = 1,
                    softWrap = false
                )
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF00E676).copy(alpha = 0.15f),
                    border = BorderStroke(0.8.dp, Color(0xFF00E676).copy(alpha = 0.7f))
                ) {
                    Text(
                        text = "ACTIVE",
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF00E676),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.5.dp),
                        letterSpacing = 0.6.sp
                    )
                }
            }
            Text(
                text = headerSubtitle,
                fontSize = if (isCompact) 9.5.sp else 10.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = NeonPalette.CardIdleText,
                letterSpacing = 0.7.sp,
                maxLines = 1,
                softWrap = false
            )
        }

        // Mode-Specific Hero Metric & Status Badge
        when (settings.inputMode) {
            InputMode.VELOCITY -> {
                val aimSpeed = sqrt(processedYaw * processedYaw + processedPitch * processedPitch)
                val speedBadge = when {
                    aimSpeed > 180f -> "FAST FLICK ⚡"
                    aimSpeed > 60f -> "RAPID TRACKING"
                    aimSpeed > 8f -> "PRECISION AIM"
                    aimSpeed > settings.deadzoneThreshold -> "SLOW AIM"
                    else -> "DEAD CENTER"
                }

                // Hero Live Camera Aim Speed Header
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "${String.format("%.1f", aimSpeed)} °/s",
                            fontSize = if (isCompact) 21.sp else 24.sp,
                            fontWeight = FontWeight.Light,
                            color = Color.White
                        )

                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = accentColor.copy(alpha = 0.14f),
                            border = BorderStroke(1.dp, accentColor.copy(alpha = 0.6f))
                        ) {
                            Text(
                                text = speedBadge,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = accentColor,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.5.dp),
                                letterSpacing = 0.6.sp
                            )
                        }
                    }

                    Text(
                        text = "LIVE CAMERA AIM SPEED",
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = NeonPalette.CardIdleText,
                        letterSpacing = 0.8.sp
                    )
                }

                // Live Camera Aim Speed Values — Arranged in a Column so each value is clearly readable
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    val yawSpeedAbs = abs(processedYaw)
                    val pitchSpeedAbs = abs(processedPitch)

                    val yawDir = when {
                        processedYaw > 2.0f -> "TURN RIGHT ▶"
                        processedYaw < -2.0f -> "◀ TURN LEFT"
                        else -> "CENTER"
                    }
                    val pitchDir = when {
                        processedPitch > 2.0f -> "AIM UP ▲"
                        processedPitch < -2.0f -> "▼ AIM DOWN"
                        else -> "CENTER"
                    }

                    val yawRatio = (yawSpeedAbs / settings.maxDpsYaw).coerceIn(0f, 1f)
                    val pitchRatio = (pitchSpeedAbs / settings.maxDpsPitch).coerceIn(0f, 1f)
                    val overallRatio = (aimSpeed / settings.maxDpsYaw).coerceIn(0f, 1f)

                    val targetStickName = when (settings.xboxTargetStick) {
                        XboxTargetStick.RIGHT_STICK -> "Right Stick (RS)"
                        XboxTargetStick.LEFT_STICK -> "Left Stick (LS)"
                        XboxTargetStick.NONE -> "Off (Unmapped)"
                    }

                    LiveAimSpeedRow(
                        axisName = "Horizontal Aim (Yaw)",
                        directionText = yawDir,
                        speedText = "${String.format("%+.1f", processedYaw)} °/s",
                        maxLimitText = "Max: ${settings.maxDpsYaw.toInt()}°/s",
                        barRatio = yawRatio,
                        accentColor = Color(0xFF00E5FF)
                    )

                    LiveAimSpeedRow(
                        axisName = "Vertical Aim (Pitch)",
                        directionText = pitchDir,
                        speedText = "${String.format("%+.1f", processedPitch)} °/s",
                        maxLimitText = "Max: ${settings.maxDpsPitch.toInt()}°/s",
                        barRatio = pitchRatio,
                        accentColor = Color(0xFF69F0AE)
                    )

                    LiveAimSpeedRow(
                        axisName = "Target Stick",
                        directionText = if (settings.accelerationEnabled) "ACCEL CURVE" else "1:1 DIRECT",
                        speedText = targetStickName,
                        maxLimitText = "Deadzone: ${String.format("%.1f", settings.deadzoneThreshold)}°/s",
                        barRatio = overallRatio,
                        accentColor = Color(0xFFFFB300)
                    )
                }
            }

            InputMode.ABSOLUTE_TILT -> {
                val steeringAngle = (displayYaw).toInt()
                val steerStatus = when {
                    abs(steeringAngle) <= 2 -> "DEAD CENTER"
                    steeringAngle > 0 -> "TURNING RIGHT ▶"
                    else -> "TURNING LEFT ◀"
                }
                val steerPct = (abs(displayYaw) / settings.absoluteMaxTilt * 100f).coerceIn(0f, 100f)

                val targetStickShort = when (settings.xboxTargetStick) {
                    XboxTargetStick.RIGHT_STICK -> "RS"
                    XboxTargetStick.LEFT_STICK -> "LS"
                    XboxTargetStick.NONE -> "Off"
                }

                HeroMetricSection(
                    heroValue = "${if (steeringAngle > 0) "+" else ""}$steeringAngle °",
                    heroLabel = "VIRTUAL STEERING ANGLE",
                    badgeText = steerStatus,
                    badgeColor = accentColor,
                    chip1 = "Deflection: ${steerPct.toInt()}%",
                    chip2 = "Max Lock: ${settings.absoluteMaxTilt.toInt()}°",
                    chip3 = "Target: $targetStickShort",
                    isCompact = isCompact
                )

                Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    TelemetryBar(
                        label = "Steering Travel",
                        value = (steerPct / 100f).coerceIn(0f, 1f),
                        displayValue = "${steerPct.toInt()}%",
                        color = Color(0xFFFFB300),
                        labelWidth = if (isCompact) 105.dp else 125.dp
                    )
                    TelemetryBar(
                        label = "Centering Authority",
                        value = (settings.absoluteSensitivityX / 2.5f).coerceIn(0f, 1f),
                        displayValue = "${String.format("%.2f", settings.absoluteSensitivityX)}×",
                        color = Color(0xFF00E5FF),
                        labelWidth = if (isCompact) 105.dp else 125.dp
                    )
                    TelemetryBar(
                        label = "Response Curve",
                        value = (settings.absoluteCurve / 3.0f).coerceIn(0f, 1f),
                        displayValue = "${String.format("%.1f", settings.absoluteCurve)} exp",
                        color = Color(0xFF69F0AE),
                        labelWidth = if (isCompact) 105.dp else 125.dp
                    )
                }
            }

            InputMode.AIR_MOUSE -> {
                val pitchDegVal = (-finalPitch).toInt()
                val rollDegVal = finalRoll.toInt()
                val flightStatus = when {
                    abs(pitchDegVal) <= 3 && abs(rollDegVal) <= 3 -> "LEVEL FLIGHT"
                    pitchDegVal > 5 -> "CLIMBING ▲"
                    pitchDegVal < -5 -> "DIVING ▼"
                    rollDegVal > 5 -> "BANKING RIGHT ▶"
                    else -> "BANKING LEFT ◀"
                }

                val pitchTarget = when (settings.airMousePitchTarget) {
                    FlightStickAxisTarget.LS_Y -> "LS-Y"
                    FlightStickAxisTarget.RS_Y -> "RS-Y"
                    FlightStickAxisTarget.LS_X -> "LS-X"
                    FlightStickAxisTarget.RS_X -> "RS-X"
                    FlightStickAxisTarget.NONE -> "Off"
                }
                val rollTarget = when (settings.airMouseRollTarget) {
                    FlightStickAxisTarget.LS_X -> "LS-X"
                    FlightStickAxisTarget.RS_X -> "RS-X"
                    FlightStickAxisTarget.LS_Y -> "LS-Y"
                    FlightStickAxisTarget.RS_Y -> "RS-Y"
                    FlightStickAxisTarget.NONE -> "Off"
                }

                HeroMetricSection(
                    heroValue = "${if (pitchDegVal > 0) "+" else ""}$pitchDegVal° / ${if (rollDegVal > 0) "+" else ""}$rollDegVal°",
                    heroLabel = "PITCH (ELEVATOR) / ROLL (BANK)",
                    badgeText = flightStatus,
                    badgeColor = accentColor,
                    chip1 = "Pitch: $pitchTarget",
                    chip2 = "Roll: $rollTarget",
                    chip3 = "Yaw: ${if (settings.airMouseYawTarget == FlightStickAxisTarget.NONE) "Off" else "Active"}",
                    isCompact = isCompact
                )

                val pitchPct = (abs(finalPitch) / settings.airMouseMaxPitch).coerceIn(0f, 1f)
                val rollPct = (abs(finalRoll) / settings.airMouseMaxRoll).coerceIn(0f, 1f)

                Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    TelemetryBar(
                        label = "Elevator Travel",
                        value = pitchPct,
                        displayValue = "${(pitchPct * 100).toInt()}%",
                        color = Color(0xFF00E676),
                        labelWidth = if (isCompact) 105.dp else 125.dp
                    )
                    TelemetryBar(
                        label = "Aileron Bank",
                        value = rollPct,
                        displayValue = "${(rollPct * 100).toInt()}%",
                        color = Color(0xFF00E5FF),
                        labelWidth = if (isCompact) 105.dp else 125.dp
                    )
                    TelemetryBar(
                        label = "Tremor Deadzone",
                        value = (settings.airMouseDeadzone / 5f).coerceIn(0f, 1f),
                        displayValue = "${String.format("%.1f", settings.airMouseDeadzone)}°",
                        color = Color(0xFFFFB300),
                        labelWidth = if (isCompact) 105.dp else 125.dp
                    )
                }
            }

            InputMode.GESTURE -> {
                val strikeStatus = when {
                    isStrikeDetected -> "STRIKE DETECTED ⚡"
                    isShakingDetected -> "SHAKING / CHARGING ⚡"
                    abs(gravityPitchDeg) >= settings.gestureTiltPitchThreshold || abs(gravityRollDeg) >= settings.gestureTiltRollThreshold -> "POSTURE TRIGGERED"
                    else -> "MONITORING KINETIC FORCE"
                }

                val gestureJoy = settings.effectiveGestureJoystickMode.shortName

                HeroMetricSection(
                    heroValue = "${String.format("%.1f", effectiveCombatForce)} m/s²",
                    heroLabel = "LIVE KINETIC MOTION FORCE",
                    badgeText = strikeStatus,
                    badgeColor = if (isStrikeDetected) Color(0xFFFF1744) else accentColor,
                    chip1 = "Pitch: ${String.format("%+.0f", gravityPitchDeg)}°",
                    chip2 = "Roll: ${String.format("%+.0f", gravityRollDeg)}°",
                    chip3 = "Joystick: $gestureJoy",
                    isCompact = isCompact
                )

                val flickProgress = (effectiveCombatForce / settings.gestureFlickThreshold).coerceIn(0f, 1f)
                val pitchPostureProgress = (abs(gravityPitchDeg) / settings.gestureTiltPitchThreshold).coerceIn(0f, 1f)

                Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    TelemetryBar(
                        label = "Flick Trigger Force",
                        value = flickProgress,
                        displayValue = "${settings.gestureFlickThreshold.toInt()} m/s²",
                        color = Color(0xFFFF1744),
                        labelWidth = if (isCompact) 105.dp else 125.dp
                    )
                    TelemetryBar(
                        label = "Tilt Posture Force",
                        value = pitchPostureProgress,
                        displayValue = "${settings.gestureTiltPitchThreshold.toInt()}°",
                        color = Color(0xFFFF9100),
                        labelWidth = if (isCompact) 105.dp else 125.dp
                    )
                    TelemetryBar(
                        label = "Shake Trigger Window",
                        value = (settings.gestureShakeThreshold / 20f).coerceIn(0f, 1f),
                        displayValue = "${settings.gestureShakeThreshold.toInt()} m/s²",
                        color = Color(0xFF00E5FF),
                        labelWidth = if (isCompact) 105.dp else 125.dp
                    )
                }
            }

            InputMode.RAW_UNCALIBRATED -> {
                val rawMag = sqrt(gyroPitchRate * gyroPitchRate + gyroRollRate * gyroRollRate + gyroYawRate * gyroYawRate)

                HeroMetricSection(
                    heroValue = "${String.format("%.1f", rawMag)} °/s",
                    heroLabel = "RAW SILICON ANGULAR RATE",
                    badgeText = "ESPORTS 1:1 DIRECT",
                    badgeColor = accentColor,
                    chip1 = "P: ${String.format("%+.1f", gyroPitchRate)}°/s",
                    chip2 = "R: ${String.format("%+.1f", gyroRollRate)}°/s",
                    chip3 = "Y: ${String.format("%+.1f", gyroYawRate)}°/s",
                    isCompact = isCompact
                )

                Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    TelemetryBar(
                        label = "Sampling Latency",
                        value = 0.05f,
                        displayValue = "0.0 ms",
                        color = Color(0xFF00E5FF),
                        labelWidth = if (isCompact) 105.dp else 125.dp
                    )
                    TelemetryBar(
                        label = "Filter Bypass",
                        value = 1.0f,
                        displayValue = "100% Direct",
                        color = Color(0xFFD500F9),
                        labelWidth = if (isCompact) 105.dp else 125.dp
                    )
                    TelemetryBar(
                        label = "Hardware Sensitivity",
                        value = (settings.maxDpsYaw / 600f).coerceIn(0f, 1f),
                        displayValue = "${settings.maxDpsYaw.toInt()} °/s",
                        color = Color(0xFF69F0AE),
                        labelWidth = if (isCompact) 105.dp else 125.dp
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 3D MOBILE GAMING CONTROLLER MODEL (Full Height, Width, Depth / Thickness)
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
    combatForceG: Float = 0f,
    isCompact: Boolean = false
) {
    val vpWidth = if (isCompact) 210.dp else 270.dp
    val vpHeight = if (isCompact) 155.dp else 195.dp
    val modelScale = if (isCompact) 0.80f else 1.0f

    Box(
        modifier = Modifier
            .width(vpWidth)
            .height(vpHeight)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF090D18))
            .border(BorderStroke(1.dp, Color(0xFF1B2438)), RoundedCornerShape(16.dp)),
        contentAlignment = Alignment.Center
    ) {
        // 1. Holographic Spatial Coordinate Axes Overlay
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cx = size.width / 2f
            val cy = size.height / 2f

            // Perspective Grid Floor lines
            val gridColor = Color(0xFF141C2E).copy(alpha = 0.5f)
            val stroke1 = Stroke(1.dp.toPx())
            for (i in -3..3) {
                val offset = i * 28.dp.toPx()
                drawLine(
                    color = gridColor,
                    start = Offset(cx + offset * 0.7f, cy + 50.dp.toPx()),
                    end = Offset(cx + offset * 1.5f, size.height),
                    strokeWidth = 1.dp.toPx()
                )
            }
            drawLine(gridColor, Offset(0f, cy + 60.dp.toPx()), Offset(size.width, cy + 60.dp.toPx()), 1.dp.toPx())
            drawLine(gridColor, Offset(0f, cy + 80.dp.toPx()), Offset(size.width, cy + 80.dp.toPx()), 1.dp.toPx())
        }

        // 2. The Floating 3D Mobile Gaming Controller with Combat Impulse & Shake Flutter
        Box(
            modifier = Modifier
                .size(230.dp, 120.dp)
                .graphicsLayer {
                    val shakeJitterX = if (isShakingActive) {
                        (kotlin.math.sin(System.currentTimeMillis() * 0.045f) * 8f)
                    } else 0f
                    val shakeJitterY = if (isShakingActive) {
                        (kotlin.math.cos(System.currentTimeMillis() * 0.045f) * 6f)
                    } else 0f
                    val strikeKickbackY = if (isStrikeActive) -16f else 0f

                    this.scaleX = modelScale
                    this.scaleY = modelScale
                    this.rotationX = pitchDeg
                    this.rotationY = rollDeg
                    this.rotationZ = yawDeg
                    this.translationX = shakeJitterX
                    this.translationY = strikeKickbackY + shakeJitterY
                    this.cameraDistance = 14f * density
                },
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val cx = w / 2f
                val cy = h / 2f

                // ─────────────────────────────────────────────────────────────
                // 3D CHASSIS EXTRUSION & THICKNESS (Height, Width, Depth/Length)
                // ─────────────────────────────────────────────────────────────
                // Calculate physical depth offsets based on 3D rotation angles
                val depthOffsetX = (rollDeg / 45f) * 6.dp.toPx()
                val depthOffsetY = (-pitchDeg / 45f) * 6.dp.toPx()

                // Layer A: 3D Extruded Chassis Shadow / Underbody Plate
                drawRoundRect(
                    color = Color(0xFF080B12),
                    topLeft = Offset(-depthOffsetX * 0.8f, -depthOffsetY * 0.8f),
                    size = Size(w, h),
                    cornerRadius = CornerRadius(16.dp.toPx(), 16.dp.toPx())
                )

                // Layer B: 3D Metallic Edge Chamfer Wall (creates visible edge thickness)
                drawRoundRect(
                    color = Color(0xFF1A2234),
                    topLeft = Offset(-depthOffsetX * 0.4f, -depthOffsetY * 0.4f),
                    size = Size(w, h),
                    cornerRadius = CornerRadius(16.dp.toPx(), 16.dp.toPx()),
                    style = Stroke(width = 3.dp.toPx())
                )

                // ─────────────────────────────────────────────────────────────
                // TOP SHOULDER RAILS (LB/RB Bumpers & LT/RT Triggers)
                // ─────────────────────────────────────────────────────────────
                val bumperWidth = 52.dp.toPx()
                val bumperHeight = 8.dp.toPx()
                val bumperCorner = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                // Left Bumper (LB)
                drawRoundRect(
                    color = Color(0xFF222C42),
                    topLeft = Offset(24.dp.toPx(), -6.dp.toPx()),
                    size = Size(bumperWidth, bumperHeight),
                    cornerRadius = bumperCorner
                )
                drawRoundRect(
                    color = accentColor.copy(alpha = 0.6f),
                    topLeft = Offset(24.dp.toPx(), -6.dp.toPx()),
                    size = Size(bumperWidth, bumperHeight),
                    cornerRadius = bumperCorner,
                    style = Stroke(width = 1.dp.toPx())
                )
                // Right Bumper (RB)
                drawRoundRect(
                    color = Color(0xFF222C42),
                    topLeft = Offset(w - 24.dp.toPx() - bumperWidth, -6.dp.toPx()),
                    size = Size(bumperWidth, bumperHeight),
                    cornerRadius = bumperCorner
                )
                drawRoundRect(
                    color = accentColor.copy(alpha = 0.6f),
                    topLeft = Offset(w - 24.dp.toPx() - bumperWidth, -6.dp.toPx()),
                    size = Size(bumperWidth, bumperHeight),
                    cornerRadius = bumperCorner,
                    style = Stroke(width = 1.dp.toPx())
                )

                // ─────────────────────────────────────────────────────────────
                // FRONT GAMEPAD CHASSIS FACEPLATE
                // ─────────────────────────────────────────────────────────────
                val activeBorderColor = if (isStrikeActive) {
                    Color(0xFFFF1744) // Kinetic Strike crimson
                } else if (isShakingActive) {
                    Color(0xFFFF9100) // Energetic kinetic amber
                } else {
                    accentColor
                }

                // Main chassis body with sculpted ergonomic grip curves
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        listOf(Color(0xFF161D2E), Color(0xFF0F1420))
                    ),
                    topLeft = Offset.Zero,
                    size = Size(w, h),
                    cornerRadius = CornerRadius(16.dp.toPx(), 16.dp.toPx())
                )
                // Outer chamfer border with dynamic active combat/steering glow
                drawRoundRect(
                    brush = Brush.linearGradient(
                        listOf(
                            activeBorderColor.copy(alpha = 0.9f),
                            Color(0xFF2E3A52),
                            activeBorderColor.copy(alpha = 0.4f)
                        )
                    ),
                    topLeft = Offset.Zero,
                    size = Size(w, h),
                    cornerRadius = CornerRadius(16.dp.toPx(), 16.dp.toPx()),
                    style = Stroke(width = if (isStrikeActive) 2.4f.dp.toPx() else 1.8f.dp.toPx())
                )

                // ─────────────────────────────────────────────────────────────
                // CENTER TACTICAL SCREEN / HUD DISPLAY
                // ─────────────────────────────────────────────────────────────
                val screenW = 98.dp.toPx()
                val screenH = 82.dp.toPx()
                val screenLeft = cx - screenW / 2f
                val screenTop = cy - screenH / 2f

                // Screen Bezel Well
                drawRoundRect(
                    color = Color(0xFF070B14),
                    topLeft = Offset(screenLeft, screenTop),
                    size = Size(screenW, screenH),
                    cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
                )
                drawRoundRect(
                    color = Color(0xFF1E283E),
                    topLeft = Offset(screenLeft, screenTop),
                    size = Size(screenW, screenH),
                    cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx()),
                    style = Stroke(width = 1.dp.toPx())
                )

                // Screen Optical Specular Glass Sheen
                drawPath(
                    path = Path().apply {
                        moveTo(screenLeft, screenTop)
                        lineTo(screenLeft + screenW * 0.65f, screenTop)
                        lineTo(screenLeft, screenTop + screenH * 0.65f)
                        close()
                    },
                    color = Color.White.copy(alpha = 0.06f)
                )

                // Screen Gyroscope Dynamic Reticle (Moves inside screen with motion)
                val reticleOffsetX = (rollDeg / 45f) * 16.dp.toPx()
                val reticleOffsetY = (pitchDeg / 45f) * 16.dp.toPx()
                val reticleCx = (cx + reticleOffsetX).coerceIn(screenLeft + 12.dp.toPx(), screenLeft + screenW - 12.dp.toPx())
                val reticleCy = (cy + reticleOffsetY).coerceIn(screenTop + 12.dp.toPx(), screenTop + screenH - 12.dp.toPx())

                // Screen Grid Lines
                drawLine(
                    color = Color(0xFF121B2C),
                    start = Offset(screenLeft, cy),
                    end = Offset(screenLeft + screenW, cy),
                    strokeWidth = 1.dp.toPx()
                )
                drawLine(
                    color = Color(0xFF121B2C),
                    start = Offset(cx, screenTop),
                    end = Offset(cx, screenTop + screenH),
                    strokeWidth = 1.dp.toPx()
                )

                // Mode-Specific Tactical Screen Elements
                when (inputMode) {
                    InputMode.GESTURE -> {
                        if (isStrikeActive) {
                            // Kinetic Strike Impact Shockwave Ripple
                            drawCircle(
                                color = Color(0xFFFF1744).copy(alpha = 0.55f),
                                radius = 24.dp.toPx(),
                                center = Offset(reticleCx, reticleCy),
                                style = Stroke(2.dp.toPx())
                            )
                            drawCircle(
                                color = Color(0xFFFF1744).copy(alpha = 0.25f),
                                radius = 16.dp.toPx(),
                                center = Offset(reticleCx, reticleCy)
                            )
                            drawCircle(
                                color = Color.White,
                                radius = 4.dp.toPx(),
                                center = Offset(reticleCx, reticleCy)
                            )
                        } else if (isShakingActive) {
                            // Kinetic Charge / Shake Ripple
                            drawCircle(
                                color = Color(0xFFFF9100).copy(alpha = 0.5f),
                                radius = 18.dp.toPx(),
                                center = Offset(cx, cy),
                                style = Stroke(1.5f.dp.toPx())
                            )
                            drawCircle(
                                color = Color.White,
                                radius = 2.5f.dp.toPx(),
                                center = Offset(reticleCx, reticleCy)
                            )
                        } else {
                            // Monitoring Crosshair
                            drawCircle(
                                color = Color(0xFFFF1744).copy(alpha = 0.45f),
                                radius = 8.dp.toPx(),
                                center = Offset(reticleCx, reticleCy),
                                style = Stroke(1.dp.toPx())
                            )
                            drawCircle(
                                color = Color.White,
                                radius = 2.dp.toPx(),
                                center = Offset(reticleCx, reticleCy)
                            )
                        }
                    }

                    InputMode.ABSOLUTE_TILT -> {
                        // Racing Steering Gauge Arc
                        val arcRadius = 24.dp.toPx()
                        drawArc(
                            color = Color(0xFFFFB300).copy(alpha = 0.35f),
                            startAngle = 180f,
                            sweepAngle = 180f,
                            useCenter = false,
                            topLeft = Offset(cx - arcRadius, cy - arcRadius + 5.dp.toPx()),
                            size = Size(arcRadius * 2, arcRadius * 2),
                            style = Stroke(2.dp.toPx())
                        )
                        // Dynamic Steering Needle pointing to active steer angle
                        val steerAngleRad = (yawDeg.coerceIn(-80f, 80f) - 90f) * (Math.PI / 180.0).toFloat()
                        val tickLen = 20.dp.toPx()
                        drawLine(
                            color = Color(0xFFFFB300),
                            start = Offset(cx, cy + 5.dp.toPx()),
                            end = Offset(
                                cx + kotlin.math.cos(steerAngleRad) * tickLen,
                                cy + 5.dp.toPx() + kotlin.math.sin(steerAngleRad) * tickLen
                            ),
                            strokeWidth = 2.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                        drawCircle(
                            color = Color.White,
                            radius = 2.dp.toPx(),
                            center = Offset(cx, cy + 5.dp.toPx())
                        )
                    }

                    InputMode.RAW_UNCALIBRATED -> {
                        // High-Speed Unfiltered Silicon MEMS Waveform
                        val wavePath = Path().apply {
                            moveTo(screenLeft + 6.dp.toPx(), cy)
                            lineTo(screenLeft + screenW * 0.30f, cy)
                            lineTo(screenLeft + screenW * 0.40f, cy - 10.dp.toPx())
                            lineTo(screenLeft + screenW * 0.50f, cy + 12.dp.toPx())
                            lineTo(screenLeft + screenW * 0.60f, cy - 8.dp.toPx())
                            lineTo(screenLeft + screenW * 0.70f, cy)
                            lineTo(screenLeft + screenW - 6.dp.toPx(), cy)
                        }
                        drawPath(
                            path = wavePath,
                            color = Color(0xFFD500F9).copy(alpha = 0.85f),
                            style = Stroke(1.5f.dp.toPx())
                        )
                        drawCircle(
                            color = Color.White,
                            radius = 2.5f.dp.toPx(),
                            center = Offset(reticleCx, reticleCy)
                        )
                    }

                    else -> {
                        // Standard Motion Crosshair
                        drawCircle(
                            color = accentColor.copy(alpha = 0.4f),
                            radius = 8.dp.toPx(),
                            center = Offset(reticleCx, reticleCy),
                            style = Stroke(1.dp.toPx())
                        )
                        drawCircle(
                            color = Color.White,
                            radius = 2.dp.toPx(),
                            center = Offset(reticleCx, reticleCy)
                        )
                    }
                }

                // ─────────────────────────────────────────────────────────────
                // LEFT CONTROL CLUSTER: Analog Stick (LS) & D-Pad Cross
                // ─────────────────────────────────────────────────────────────
                val lsCenter = Offset(32.dp.toPx(), 38.dp.toPx())
                // LS Socket Base
                drawCircle(color = Color(0xFF090D16), radius = 14.dp.toPx(), center = lsCenter)
                drawCircle(color = Color(0xFF253046), radius = 14.dp.toPx(), center = lsCenter, style = Stroke(1.2f.dp.toPx()))
                // LS Thumb Cap with Concave Dish & Knurl
                drawCircle(color = Color(0xFF1B2334), radius = 11.dp.toPx(), center = lsCenter)
                drawCircle(color = Color(0xFF101522), radius = 6.5f.dp.toPx(), center = lsCenter)
                drawCircle(color = accentColor.copy(alpha = 0.8f), radius = 2.dp.toPx(), center = lsCenter)

                // Directional D-Pad Cross
                val dpadCenter = Offset(32.dp.toPx(), 84.dp.toPx())
                val dpadArmW = 7.dp.toPx()
                val dpadArmL = 22.dp.toPx()
                // Horizontal crossbar
                drawRoundRect(
                    color = Color(0xFF1E2638),
                    topLeft = Offset(dpadCenter.x - dpadArmL / 2f, dpadCenter.y - dpadArmW / 2f),
                    size = Size(dpadArmL, dpadArmW),
                    cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
                )
                // Vertical crossbar
                drawRoundRect(
                    color = Color(0xFF1E2638),
                    topLeft = Offset(dpadCenter.x - dpadArmW / 2f, dpadCenter.y - dpadArmL / 2f),
                    size = Size(dpadArmW, dpadArmL),
                    cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
                )
                // Center core
                drawCircle(color = Color(0xFF141A26), radius = 3.dp.toPx(), center = dpadCenter)

                // ─────────────────────────────────────────────────────────────
                // RIGHT CONTROL CLUSTER: ABXY Action Diamond & Analog Stick (RS)
                // ─────────────────────────────────────────────────────────────
                val abxyCenter = Offset(w - 32.dp.toPx(), 38.dp.toPx())
                val btnRadius = 3.8f.dp.toPx()
                val dist = 8.5f.dp.toPx()
                // Y (Top - Gold)
                drawCircle(color = Color(0xFFFFD54F), radius = btnRadius, center = Offset(abxyCenter.x, abxyCenter.y - dist))
                // B (Right - Red)
                drawCircle(color = Color(0xFFFF5252), radius = btnRadius, center = Offset(abxyCenter.x + dist, abxyCenter.y))
                // A (Bottom - Green)
                drawCircle(color = Color(0xFF69F0AE), radius = btnRadius, center = Offset(abxyCenter.x, abxyCenter.y + dist))
                // X (Left - Blue)
                drawCircle(color = Color(0xFF40C4FF), radius = btnRadius, center = Offset(abxyCenter.x - dist, abxyCenter.y))

                // RS Thumbstick
                val rsCenter = Offset(w - 32.dp.toPx(), 84.dp.toPx())
                drawCircle(color = Color(0xFF090D16), radius = 14.dp.toPx(), center = rsCenter)
                drawCircle(color = Color(0xFF253046), radius = 14.dp.toPx(), center = rsCenter, style = Stroke(1.2f.dp.toPx()))
                drawCircle(color = Color(0xFF1B2334), radius = 11.dp.toPx(), center = rsCenter)
                drawCircle(color = Color(0xFF101522), radius = 6.5f.dp.toPx(), center = rsCenter)
                drawCircle(color = accentColor.copy(alpha = 0.8f), radius = 2.dp.toPx(), center = rsCenter)

                // ─────────────────────────────────────────────────────────────
                // SYSTEM BUTTONS & NEXUS GUIDE ORB
                // ─────────────────────────────────────────────────────────────
                val nexusCenter = Offset(cx, screenTop - 3.dp.toPx())
                drawCircle(color = accentColor, radius = 3.dp.toPx(), center = nexusCenter)
                drawCircle(color = Color.White, radius = 1.2f.dp.toPx(), center = nexusCenter)
            }
        }

        // 3. 3D Spatial Vector HUD Overlay Labels (Pitch X, Roll Y, Yaw Z)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(if (isCompact) 6.dp else 8.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "PITCH (X): ${if (pitchDeg.toInt() > 0) "+" else ""}${pitchDeg.toInt()}°",
                    fontSize = if (isCompact) 8.sp else 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFF9100)
                )
                Text(
                    text = "ROLL (Y): ${if (rollDeg.toInt() > 0) "+" else ""}${rollDeg.toInt()}°",
                    fontSize = if (isCompact) 8.sp else 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF00E5FF)
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF0C101C).copy(alpha = 0.85f),
                    border = BorderStroke(1.dp, Color(0xFF1E283D))
                ) {
                    Text(
                        text = "YAW (Z): ${if (yawDeg.toInt() > 0) "+" else ""}${yawDeg.toInt()}°",
                        fontSize = if (isCompact) 8.sp else 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFD500F9),
                        modifier = Modifier.padding(horizontal = if (isCompact) 6.dp else 8.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// REUSABLE HERO METRIC & TELEMETRY COMPONENTS
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun HeroMetricSection(
    heroValue: String,
    heroLabel: String,
    badgeText: String,
    badgeColor: Color,
    chip1: String,
    chip2: String,
    chip3: String,
    isCompact: Boolean = false
) {
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = heroValue,
                fontSize = if (isCompact) 21.sp else 24.sp,
                fontWeight = FontWeight.Light,
                color = Color.White
            )

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = badgeColor.copy(alpha = 0.14f),
                border = BorderStroke(1.dp, badgeColor.copy(alpha = 0.6f))
            ) {
                Text(
                    text = badgeText,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = badgeColor,
                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.5.dp),
                    letterSpacing = 0.5.sp
                )
            }
        }

        Text(
            text = heroLabel,
            fontSize = 9.5.sp,
            fontWeight = FontWeight.SemiBold,
            color = NeonPalette.CardIdleText,
            letterSpacing = 0.6.sp
        )

        Spacer(modifier = Modifier.height(2.dp))

        // Informational Chips (Wrapping FlowRow to prevent any clipping)
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            MetricChip(chip1)
            MetricChip(chip2)
            MetricChip(chip3)
        }
    }
}

@Composable
private fun MetricChip(label: String) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = Color(0xFF161E30).copy(alpha = 0.8f),
        border = BorderStroke(1.dp, Color(0xFF26334D))
    ) {
        Text(
            text = label,
            fontSize = 9.5.sp,
            color = Color(0xFFB0BEC5),
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.5.dp),
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
    labelWidth: androidx.compose.ui.unit.Dp = 115.dp
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 10.5.sp,
            color = Color(0xFFA0AEC0),
            modifier = Modifier.width(labelWidth),
            maxLines = 1,
            softWrap = false
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .height(5.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(Color(0xFF182236))
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val fillWidth = size.width * value.coerceIn(0f, 1f)
                if (fillWidth > 0f) {
                    drawRoundRect(
                        brush = Brush.horizontalGradient(
                            listOf(color.copy(alpha = 0.5f), color)
                        ),
                        size = Size(fillWidth, size.height),
                        cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
                    )
                }
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = displayValue,
            fontSize = 10.5.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White,
            modifier = Modifier.width(50.dp),
            maxLines = 1,
            softWrap = false
        )
    }
}

@Composable
private fun LiveAimSpeedRow(
    axisName: String,
    directionText: String,
    speedText: String,
    maxLimitText: String,
    barRatio: Float,
    accentColor: Color
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFF131A29).copy(alpha = 0.85f),
        border = BorderStroke(1.dp, Color(0xFF222D42))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            // Line 1: Axis label + direction badge on left, live speed/target stick value on right
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f, fill = false),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = axisName,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFCFD8DC),
                        maxLines = 1,
                        softWrap = false
                    )
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = accentColor.copy(alpha = 0.14f),
                        border = BorderStroke(0.8.dp, accentColor.copy(alpha = 0.45f))
                    ) {
                        Text(
                            text = directionText,
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = accentColor,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp),
                            letterSpacing = 0.5.sp,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = speedText,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    softWrap = false
                )
            }

            // Line 2: Real-time dynamic speed deflection gradient bar + secondary max/deadzone limit text
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color(0xFF1B2436))
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val fillWidth = size.width * barRatio.coerceIn(0f, 1f)
                        if (fillWidth > 0f) {
                            drawRoundRect(
                                brush = Brush.horizontalGradient(
                                    listOf(accentColor.copy(alpha = 0.4f), accentColor)
                                ),
                                size = Size(fillWidth, size.height),
                                cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
                            )
                        }
                    }
                }

                Text(
                    text = maxLimitText,
                    fontSize = 9.sp,
                    color = Color(0xFF78909C),
                    maxLines = 1,
                    softWrap = false
                )
            }
        }
    }
}

