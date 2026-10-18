package com.sanket.tools.nexpaddesktop.driver

import com.sanket.tools.nexpaddesktop.model.*
import kotlin.math.*

/**
 * Stateful processor for gyroscope and accelerometer data.
 *
 * This class acts as the mathematical **State Layer**. It intercepts raw rad/s 
 * gyro data and applies a full deadzone, tightening, and exponential moving average 
 * (EMA) smoothing pipeline before passing the filtered data on to the driver.
 *
 * ## Coordinate Systems
 * - **Android Phone (Landscape):**
 *   - Gyro X (Pitch): Tilting phone forward
 *   - Gyro Y (Roll): Tilting phone sideways
 *   - Gyro Z (Yaw): Turning phone flat on a table
 */
class GyroProcessor {

    // =========================================================================
    // STATE VARIABLES
    // =========================================================================
    
    /** EMA History for X-axis */
    private var smoothedX = 0f
    
    /** EMA History for Y-axis */
    private var smoothedY = 0f

    private var toggleActive = false
    private var toggleButtonWasPressed = false

    // Air Mouse (3D Flight Yoke / Attitude Stick) zero reference
    private var refQuatX = 0f
    private var refQuatY = 0f
    private var refQuatZ = 0f
    private var refQuatW = 1f
    private var hasInitialQuat = false

    // Motion Gesture kinetic state
    private var lastFlickTriggerTime = 0L
    private var flickPulseEndTime = 0L
    private var activeFlickAction: GestureButton? = null
    private var snapTurnDirection = 0f
    private var snapTurnEndTime = 0L

    // Gravity Tilt Posture state (Up, Down, Left, Right)
    private var isTiltUpActive = false
    private var isTiltDownActive = false
    private var isTiltLeftActive = false
    private var isTiltRightActive = false
    private var tiltUpPulseEndTime = 0L
    private var tiltDownPulseEndTime = 0L
    private var tiltLeftPulseEndTime = 0L
    private var tiltRightPulseEndTime = 0L

    // Shake Detection state
    private val shakePeakTimestamps = mutableListOf<Long>()
    private var lastShakeTriggerTime = 0L
    private var shakePulseEndTime = 0L
    private var activeShakeAction: GestureButton? = null

    // Fallback gravity filter for isolating linear acceleration from accelerometer
    private var filtGravityX = 0f
    private var filtGravityY = 0f
    private var filtGravityZ = 9.8f

    /**
     * DTO representing the fully processed gyroscope data.
     * All output values are in degrees per second (°/s) or stick deflection scaled to 200.0f.
     */
    data class ProcessedGyro(
        val yawDps: Float,    
        val pitchDps: Float,  
        val rollDps: Float = 0f,   
        val rawYawDps: Float = 0f, 
        val rawPitchDps: Float = 0f,
        val gestureMagnitude: Float = 0f,
        val gestureDetected: Boolean = false,
        val gesturePulseRT: Boolean = false,
        val gesturePulseB: Boolean = false,
        val gesturePulseX: Boolean = false,
        val gesturePulseA: Boolean = false,
        val snapTurnStickX: Float = 0f,
        val flightLsX: Float = 0f,
        val flightLsY: Float = 0f,
        val flightRsX: Float = 0f,
        val flightRsY: Float = 0f,
        val activeGestureButtons: Set<GestureButton> = emptySet(),
        val tiltAngleDeg: Float = 0f,
        val isShaking: Boolean = false,
    )

    /**
     * Processes one frame of raw gyroscope data through the mathematical pipeline.
     */
    fun process(
        rawGyroX: Float,
        rawGyroY: Float,
        rawGyroZ: Float,
        rawAccelX: Float = 0f,
        rawAccelY: Float = 0f,
        rawAccelZ: Float = 0f,
        rawGravityX: Float = 0f,
        rawGravityY: Float = 0f,
        rawGravityZ: Float = 0f,
        quatX: Float = 0f,
        quatY: Float = 0f,
        quatZ: Float = 0f,
        quatW: Float = 1f,
        linearAccelX: Float = 0f,
        linearAccelY: Float = 0f,
        linearAccelZ: Float = 0f,
        rawUncalGyroX: Float = 0f,
        rawUncalGyroY: Float = 0f,
        rawUncalGyroZ: Float = 0f,
        gyroBiasX: Float = 0f,
        gyroBiasY: Float = 0f,
        gyroBiasZ: Float = 0f,
        sensorFlags: Byte = 0,
        settings: GyroSettings,
        isActivationButtonPressed: Boolean = true,
    ): ProcessedGyro {

        if (!settings.enabled || !isGyroActive(settings, isActivationButtonPressed)) {
            smoothedX = 0f
            smoothedY = 0f
            hasInitialQuat = false
            return ProcessedGyro(0f, 0f, 0f, 0f, 0f)
        }

        // ---------------------------------------------------------------------
        // 1. Absolute Tilt Mode (Steering Wheel emulation via Gravity Vector)
        // ---------------------------------------------------------------------
        if (settings.inputMode == InputMode.ABSOLUTE_TILT) {
            val cfg = settings.steeringWheel
            val maxAccel = 9.8f
            val targetStickRange = 200.0f

            // In Gravity mode (0x01), phone streams filtered gravity in rawGravityX..Z (zero-noise tilt).
            // Fall back to raw accelerometer if gravity data is not present.
            val isGravity = (sensorFlags.toInt() and 0x01) != 0 || (rawGravityX != 0f || rawGravityY != 0f)
            val tiltX = if (isGravity) rawGravityX else rawAccelX
            val tiltY = if (isGravity) rawGravityY else rawAccelY

            val horizontalAngleDeg = asin((tiltX / maxAccel).coerceIn(-1.0f, 1.0f)) * RAD_TO_DEG
            val verticalAngleDeg = asin((tiltY / maxAccel).coerceIn(-1.0f, 1.0f)) * RAD_TO_DEG

            val defH = mapAngleToStick(horizontalAngleDeg, cfg.maxTiltAngle, cfg.deadzoneThreshold, cfg.responseCurve)
            val defV = mapAngleToStick(verticalAngleDeg, cfg.maxTiltAngle, cfg.deadzoneThreshold, cfg.responseCurve)

            var mappedH = defH * targetStickRange
            var mappedV = defV * targetStickRange

            var hVal = if (cfg.invertSteering) -mappedH else mappedH
            var vVal = if (cfg.invertPitch) -mappedV else mappedV

            hVal *= cfg.sensitivityX
            vVal *= cfg.sensitivityY

            return ProcessedGyro(
                yawDps = hVal,
                pitchDps = vVal,
                rollDps = 0f,
                rawYawDps = mappedH,
                rawPitchDps = mappedV,
                tiltAngleDeg = horizontalAngleDeg
            )
        }

        // ---------------------------------------------------------------------
        // 2. Air Mouse Mode (3D Flight Yoke / Attitude Stick via 3D Quaternion)
        // ---------------------------------------------------------------------
        if (settings.inputMode == InputMode.AIR_MOUSE) {
            val cfg = settings.flightStick
            val magSq = quatX * quatX + quatY * quatY + quatZ * quatZ + quatW * quatW
            if (magSq < 0.1f) {
                return ProcessedGyro(0f, 0f, 0f, 0f, 0f)
            }
            if (!hasInitialQuat) {
                refQuatX = quatX
                refQuatY = quatY
                refQuatZ = quatZ
                refQuatW = quatW
                hasInitialQuat = true
                return ProcessedGyro(0f, 0f, 0f, 0f, 0f)
            }

            // Relative attitude Q_rel = Q_curr * Q_ref^-1
            val rx = -refQuatX
            val ry = -refQuatY
            val rz = -refQuatZ
            val rw = refQuatW

            val qx = quatW * rx + quatX * rw + quatY * rz - quatZ * ry
            val qy = quatW * ry - quatX * rz + quatY * rw + quatZ * rx
            val qz = quatW * rz + quatX * ry - quatY * rx + quatZ * rw
            val qw = quatW * rw - quatX * rx - quatY * ry - quatZ * rz

            // Euler Angles from Q_rel (Phone Landscape Coordinates)
            val sinP = 2.0f * (qw * qx - qy * qz)
            val pitchRad = if (abs(sinP) >= 1.0f) {
                sign(sinP) * (Math.PI.toFloat() / 2.0f)
            } else {
                asin(sinP)
            }
            val pitchDeg = pitchRad * RAD_TO_DEG

            val rollRad = atan2(2.0f * (qw * qy + qz * qx), 1.0f - 2.0f * (qy * qy + qx * qx))
            val rollDeg = rollRad * RAD_TO_DEG

            val yawRad = atan2(2.0f * (qw * qz + qx * qy), 1.0f - 2.0f * (qx * qx + qz * qz))
            val yawDeg = yawRad * RAD_TO_DEG

            // 1. Pitch Deflection (tilt forward/backward)
            var defPitch = mapAngleToStick(pitchDeg, cfg.maxPitchAngle, cfg.deadzone, cfg.curveExponent)
            if (cfg.pitchInvert) {
                defPitch = -defPitch
            }

            // 2. Roll Deflection (tilt sideways / wing banking)
            var defRoll = mapAngleToStick(rollDeg, cfg.maxRollAngle, cfg.deadzone, cfg.curveExponent)
            if (cfg.rollInvert) {
                defRoll = -defRoll
            }

            // 3. Yaw Deflection (flat wrist turn / rudder)
            var defYaw = mapAngleToStick(yawDeg, cfg.maxYawAngle, cfg.deadzone, cfg.curveExponent)
            if (cfg.yawInvert) {
                defYaw = -defYaw
            }

            // Route deflections to the individually configured joystick axes
            var fLsX = 0f
            var fLsY = 0f
            var fRsX = 0f
            var fRsY = 0f

            fun routeDeflection(target: FlightStickAxisTarget, value: Float) {
                when (target) {
                    FlightStickAxisTarget.NONE -> {}
                    FlightStickAxisTarget.LS_X -> fLsX += value
                    FlightStickAxisTarget.LS_Y -> fLsY += value
                    FlightStickAxisTarget.RS_X -> fRsX += value
                    FlightStickAxisTarget.RS_Y -> fRsY += value
                }
            }

            routeDeflection(cfg.pitchTarget, defPitch)
            routeDeflection(cfg.rollTarget, defRoll)
            routeDeflection(cfg.yawTarget, defYaw)

            fLsX = fLsX.coerceIn(-1f, 1f)
            fLsY = fLsY.coerceIn(-1f, 1f)
            fRsX = fRsX.coerceIn(-1f, 1f)
            fRsY = fRsY.coerceIn(-1f, 1f)

            // Scaled stick representation for visualizers and live dashboard
            val activeStickX = if (abs(fLsX) > abs(fRsX)) fLsX else fRsX
            val activeStickY = if (abs(fLsY) > abs(fRsY)) fLsY else fRsY
            val targetStickRange = 200.0f
            val hVal = activeStickX * targetStickRange
            val vVal = activeStickY * targetStickRange

            return ProcessedGyro(
                yawDps = hVal,
                pitchDps = vVal,
                rollDps = rollDeg,
                rawYawDps = yawDeg,
                rawPitchDps = pitchDeg,
                gestureMagnitude = 0f,
                gestureDetected = false,
                flightLsX = fLsX,
                flightLsY = fLsY,
                flightRsX = fRsX,
                flightRsY = fRsY,
                tiltAngleDeg = rollDeg,
            )
        }

        // ---------------------------------------------------------------------
        // 3. Combat Gestures & Kinetic Actions Mode
        // ---------------------------------------------------------------------
        if (settings.inputMode == InputMode.GESTURE) {
            val cfg = settings.combatGestures
            val now = System.currentTimeMillis()

            var effLinX = linearAccelX
            var effLinY = linearAccelY
            var effLinZ = linearAccelZ

            if (effLinX == 0f && effLinY == 0f && effLinZ == 0f && (rawAccelX != 0f || rawAccelY != 0f || rawAccelZ != 0f)) {
                filtGravityX = filtGravityX * 0.90f + rawAccelX * 0.10f
                filtGravityY = filtGravityY * 0.90f + rawAccelY * 0.10f
                filtGravityZ = filtGravityZ * 0.90f + rawAccelZ * 0.10f
                effLinX = rawAccelX - filtGravityX
                effLinY = rawAccelY - filtGravityY
                effLinZ = rawAccelZ - filtGravityZ
            }

            val gestureMag = sqrt(effLinX * effLinX + effLinY * effLinY + effLinZ * effLinZ)
            var gestureDet = false
            var snapStickX = 0f
            val activeButtons = mutableSetOf<GestureButton>()
            var isCurrentlyShaking = false
            var currentPitchDeg = 0f
            var currentRollDeg = 0f

            // ── Card 1: Directional Kinetic Flicks ──
            if (cfg.flicksEnabled && gestureMag >= cfg.flickThreshold && (now - lastFlickTriggerTime) >= cfg.flickCooldownMs) {
                lastFlickTriggerTime = now
                gestureDet = true

                val absX = abs(effLinX)
                val absY = abs(effLinY)

                val triggeredAction: GestureButton = if (absY >= absX) {
                    if (effLinY > 0f) cfg.flickUpAction else cfg.flickDownAction
                } else {
                    if (effLinX > 0f) cfg.flickRightAction else cfg.flickLeftAction
                }

                activeFlickAction = triggeredAction

                if (triggeredAction == GestureButton.SNAP_TURN_180) {
                    val flickDir = if (effLinX < -1.5f || rawGyroZ < -1.5f) -1.0f else 1.0f
                    snapTurnDirection = flickDir
                    snapTurnEndTime = now + 120L
                } else if (triggeredAction != GestureButton.NONE) {
                    flickPulseEndTime = now + 120L
                }
            }

            // Inject active flick pulse
            if (now < snapTurnEndTime) {
                snapStickX = snapTurnDirection
                gestureDet = true
            }
            if (now < flickPulseEndTime && activeFlickAction != null && activeFlickAction != GestureButton.NONE && activeFlickAction != GestureButton.SNAP_TURN_180) {
                activeButtons.add(activeFlickAction!!)
                gestureDet = true
            }

            // ── Card 2: Gravity Tilt Posture (Angle Trigger - Up, Down, Left, Right) ──
            val hasQuat = (quatX != 0f || quatY != 0f || quatZ != 0f || quatW != 1f)
            if (hasQuat) {
                val sinP = 2.0f * (quatW * quatX - quatY * quatZ)
                val pitchRad = if (abs(sinP) >= 1.0f) sign(sinP) * (Math.PI.toFloat() / 2.0f) else asin(sinP)
                currentPitchDeg = pitchRad * RAD_TO_DEG
                val rollRad = atan2(2.0f * (quatW * quatY + quatZ * quatX), 1.0f - 2.0f * (quatY * quatY + quatX * quatX))
                currentRollDeg = rollRad * RAD_TO_DEG
            } else {
                val gx = if (rawGravityX != 0f) rawGravityX else rawAccelX
                val gy = if (rawGravityY != 0f) rawGravityY else rawAccelY
                if (gx != 0f || gy != 0f) {
                    currentRollDeg = asin((gx / 9.8f).coerceIn(-1f, 1f)) * RAD_TO_DEG
                    currentPitchDeg = asin((gy / 9.8f).coerceIn(-1f, 1f)) * RAD_TO_DEG
                }
            }

            if (cfg.tiltEnabled) {
                val pitchThresh = cfg.tiltPitchThreshold
                val rollThresh = cfg.tiltRollThreshold
                val hysteresis = 8.0f

                fun evaluateTilt(
                    isAboveThreshold: Boolean,
                    isBelowHysteresis: Boolean,
                    isActive: Boolean,
                    pulseEndTime: Long,
                    action: GestureButton,
                    setActive: (Boolean) -> Unit,
                    setPulseEndTime: (Long) -> Unit
                ) {
                    if (action == GestureButton.NONE) return
                    if (cfg.tiltHoldMode) {
                        if (isAboveThreshold) {
                            activeButtons.add(action)
                            gestureDet = true
                        }
                    } else {
                        var currentPulseEnd = pulseEndTime
                        if (isAboveThreshold) {
                            if (!isActive) {
                                setActive(true)
                                currentPulseEnd = now + 120L
                                setPulseEndTime(currentPulseEnd)
                            }
                        } else if (isBelowHysteresis) {
                            setActive(false)
                        }
                        if (now < currentPulseEnd) {
                            activeButtons.add(action)
                            gestureDet = true
                        }
                    }
                }

                // 1. Tilt UP
                evaluateTilt(
                    isAboveThreshold = currentPitchDeg >= pitchThresh,
                    isBelowHysteresis = currentPitchDeg < (pitchThresh - hysteresis),
                    isActive = isTiltUpActive,
                    pulseEndTime = tiltUpPulseEndTime,
                    action = cfg.tiltUpAction,
                    setActive = { isTiltUpActive = it },
                    setPulseEndTime = { tiltUpPulseEndTime = it }
                )

                // 2. Tilt DOWN
                evaluateTilt(
                    isAboveThreshold = currentPitchDeg <= -pitchThresh,
                    isBelowHysteresis = currentPitchDeg > -(pitchThresh - hysteresis),
                    isActive = isTiltDownActive,
                    pulseEndTime = tiltDownPulseEndTime,
                    action = cfg.tiltDownAction,
                    setActive = { isTiltDownActive = it },
                    setPulseEndTime = { tiltDownPulseEndTime = it }
                )

                // 3. Tilt LEFT
                evaluateTilt(
                    isAboveThreshold = currentRollDeg <= -rollThresh,
                    isBelowHysteresis = currentRollDeg > -(rollThresh - hysteresis),
                    isActive = isTiltLeftActive,
                    pulseEndTime = tiltLeftPulseEndTime,
                    action = cfg.tiltLeftAction,
                    setActive = { isTiltLeftActive = it },
                    setPulseEndTime = { tiltLeftPulseEndTime = it }
                )

                // 4. Tilt RIGHT
                evaluateTilt(
                    isAboveThreshold = currentRollDeg >= rollThresh,
                    isBelowHysteresis = currentRollDeg < (rollThresh - hysteresis),
                    isActive = isTiltRightActive,
                    pulseEndTime = tiltRightPulseEndTime,
                    action = cfg.tiltRightAction,
                    setActive = { isTiltRightActive = it },
                    setPulseEndTime = { tiltRightPulseEndTime = it }
                )
            }

            // ── Card 3: Shake Motion Detection ──
            if (cfg.shakeEnabled && cfg.shakeAction != GestureButton.NONE) {
                if (gestureMag >= cfg.shakeThreshold) {
                    shakePeakTimestamps.removeAll { (now - it) > 400L }
                    if (shakePeakTimestamps.isEmpty() || (now - shakePeakTimestamps.last()) >= 50L) {
                        shakePeakTimestamps.add(now)
                    }
                    if (shakePeakTimestamps.size >= 3 && (now - lastShakeTriggerTime) >= cfg.shakeCooldownMs) {
                        lastShakeTriggerTime = now
                        shakePulseEndTime = now + 120L
                        activeShakeAction = cfg.shakeAction
                        shakePeakTimestamps.clear()
                        isCurrentlyShaking = true
                        gestureDet = true
                    }
                }

                if (now < shakePulseEndTime && activeShakeAction != null && activeShakeAction != GestureButton.NONE) {
                    activeButtons.add(activeShakeAction!!)
                    gestureDet = true
                    isCurrentlyShaking = true
                }
            }

            val pulseRT = activeButtons.contains(GestureButton.TRIGGER_RT)
            val pulseB = activeButtons.contains(GestureButton.BTN_B)
            val pulseX = activeButtons.contains(GestureButton.BTN_X)
            val pulseA = activeButtons.contains(GestureButton.BTN_A)

            if (now - lastFlickTriggerTime < 250L || now - lastShakeTriggerTime < 250L) {
                gestureDet = true
            }

            return ProcessedGyro(
                yawDps = 0f,
                pitchDps = 0f,
                rollDps = 0f,
                rawYawDps = 0f,
                rawPitchDps = 0f,
                gestureMagnitude = gestureMag,
                gestureDetected = gestureDet,
                gesturePulseRT = pulseRT,
                gesturePulseB = pulseB,
                gesturePulseX = pulseX,
                gesturePulseA = pulseA,
                snapTurnStickX = snapStickX,
                flightLsX = 0f,
                flightLsY = 0f,
                flightRsX = 0f,
                flightRsY = 0f,
                activeGestureButtons = activeButtons,
                tiltAngleDeg = currentPitchDeg,
                isShaking = isCurrentlyShaking,
            )
        }

        // ---------------------------------------------------------------------
        // 4. Gyro Aiming (Precision Aiming & Raw Silicon Modes)
        // ---------------------------------------------------------------------
        val isRawSilicon = settings.inputMode == InputMode.RAW_UNCALIBRATED
        val pitchDps: Float
        val yawDps: Float
        val rollDps: Float

        val horizontalAxis: HorizontalAxis
        val invertX: Boolean
        val invertY: Boolean
        val invertRoll: Boolean
        val deadzoneThreshold: Float
        val tighteningEnabled: Boolean
        val tighteningThreshold: Float
        val lowSpeedAmpEnabled: Boolean
        val lowSpeedAmpAmount: Float
        val lowSpeedAmpThreshold: Float
        val lowSpeedAmpHarshness: Float
        val maxDpsYaw: Float
        val maxDpsPitch: Float
        val maxDpsRoll: Float

        // Velocity-only features
        val smoothingEnabled: Boolean
        val smoothingAmount: Float
        val adaptiveSmoothing: Boolean
        val smoothingThreshold: Float
        val accelerationEnabled: Boolean
        val accelerationType: AccelerationType
        val minSensitivity: Float
        val maxSensitivity: Float
        val minThreshold: Float
        val maxThreshold: Float

        if (isRawSilicon) {
            val rawCfg = settings.rawSilicon
            val cleanX = rawUncalGyroX - gyroBiasX
            val cleanY = rawUncalGyroY - gyroBiasY
            val cleanZ = rawUncalGyroZ - gyroBiasZ

            pitchDps = cleanX * RAD_TO_DEG
            yawDps   = cleanZ * RAD_TO_DEG
            rollDps  = cleanY * RAD_TO_DEG

            horizontalAxis = rawCfg.horizontalAxis
            invertX = rawCfg.invertX
            invertY = rawCfg.invertY
            invertRoll = rawCfg.invertRoll
            deadzoneThreshold = rawCfg.deadzoneThreshold
            tighteningEnabled = rawCfg.tighteningEnabled
            tighteningThreshold = rawCfg.tighteningThreshold
            lowSpeedAmpEnabled = rawCfg.lowSpeedAmplifierEnabled
            lowSpeedAmpAmount = rawCfg.lowSpeedAmplifierAmount
            lowSpeedAmpThreshold = rawCfg.lowSpeedAmplifierThreshold
            lowSpeedAmpHarshness = rawCfg.lowSpeedAmplifierHarshness
            maxDpsYaw = rawCfg.maxDpsYaw
            maxDpsPitch = rawCfg.maxDpsPitch
            maxDpsRoll = rawCfg.maxDpsRoll

            smoothingEnabled = false
            smoothingAmount = 0f
            adaptiveSmoothing = false
            smoothingThreshold = 0f
            accelerationEnabled = false
            accelerationType = AccelerationType.LINEAR
            minSensitivity = 1f
            maxSensitivity = 1f
            minThreshold = 0f
            maxThreshold = 1f
        } else {
            val aimCfg = settings.precisionAiming
            pitchDps = rawGyroX * RAD_TO_DEG
            yawDps   = rawGyroZ * RAD_TO_DEG
            rollDps  = rawGyroY * RAD_TO_DEG

            horizontalAxis = aimCfg.horizontalAxis
            invertX = aimCfg.invertX
            invertY = aimCfg.invertY
            invertRoll = aimCfg.invertRoll
            deadzoneThreshold = aimCfg.deadzoneThreshold
            tighteningEnabled = aimCfg.tighteningEnabled
            tighteningThreshold = aimCfg.tighteningThreshold
            lowSpeedAmpEnabled = aimCfg.lowSpeedAmplifierEnabled
            lowSpeedAmpAmount = aimCfg.lowSpeedAmplifierAmount
            lowSpeedAmpThreshold = aimCfg.lowSpeedAmplifierThreshold
            lowSpeedAmpHarshness = aimCfg.lowSpeedAmplifierHarshness
            maxDpsYaw = aimCfg.maxDpsYaw
            maxDpsPitch = aimCfg.maxDpsPitch
            maxDpsRoll = aimCfg.maxDpsRoll

            smoothingEnabled = aimCfg.smoothingEnabled
            smoothingAmount = aimCfg.smoothingAmount
            adaptiveSmoothing = aimCfg.adaptiveSmoothing
            smoothingThreshold = aimCfg.smoothingThreshold
            accelerationEnabled = aimCfg.accelerationEnabled
            accelerationType = aimCfg.accelerationType
            minSensitivity = aimCfg.minSensitivity
            maxSensitivity = aimCfg.maxSensitivity
            minThreshold = aimCfg.minThreshold
            maxThreshold = aimCfg.maxThreshold
        }

        val rawHorizontalDps = when (horizontalAxis) {
            HorizontalAxis.YAW  -> yawDps
            HorizontalAxis.ROLL -> rollDps
            HorizontalAxis.MIX  -> {
                val sum = yawDps + rollDps
                if (yawDps * rollDps < 0f) {
                    sum
                } else {
                    val sign = if (sum < 0f) -1f else 1f
                    sign * kotlin.math.sqrt(yawDps * yawDps + rollDps * rollDps)
                }
            }
        }
        val verticalDps = -pitchDps

        val rawH = rawHorizontalDps
        val rawV = verticalDps

        val hYaw = if (invertX) -yawDps else yawDps
        val hRoll = if (invertRoll || (horizontalAxis == HorizontalAxis.ROLL && invertX)) -rollDps else rollDps

        val horizontalDps = when (horizontalAxis) {
            HorizontalAxis.YAW  -> hYaw
            HorizontalAxis.ROLL -> hRoll
            HorizontalAxis.MIX  -> {
                val sum = hYaw + hRoll
                if (hYaw * hRoll < 0f) {
                    sum
                } else {
                    val sign = if (sum < 0f) -1f else 1f
                    sign * kotlin.math.sqrt(hYaw * hYaw + hRoll * hRoll)
                }
            }
        }

        var hVal = horizontalDps
        var vVal = if (invertY) -verticalDps else verticalDps

        // Applying mathematical filters...
        hVal = applyDeadzone(hVal, deadzoneThreshold)
        vVal = applyDeadzone(vVal, deadzoneThreshold)

        if (tighteningEnabled) {
            hVal = applyTightening(hVal, tighteningThreshold)
            vVal = applyTightening(vVal, tighteningThreshold)
        }

        if (smoothingEnabled) {
            val speedH = abs(hVal)
            val speedV = abs(vVal)
            val maxSpeed = max(speedH, speedV)

            val alpha = if (adaptiveSmoothing) {
                computeAdaptiveAlpha(maxSpeed, smoothingThreshold, smoothingAmount)
            } else {
                smoothingAmount
            }

            smoothedX = alpha * hVal + (1f - alpha) * smoothedX
            smoothedY = alpha * vVal + (1f - alpha) * smoothedY
            hVal = smoothedX
            vVal = smoothedY
        }

        if (accelerationEnabled) {
            hVal *= computeAccelerationSensitivity(abs(hVal), minThreshold, maxThreshold, minSensitivity, maxSensitivity, accelerationType)
            vVal *= computeAccelerationSensitivity(abs(vVal), minThreshold, maxThreshold, minSensitivity, maxSensitivity, accelerationType)
        }

        hVal = applyLowSpeedAmplifier(hVal, lowSpeedAmpEnabled, lowSpeedAmpAmount, lowSpeedAmpThreshold, lowSpeedAmpHarshness)
        vVal = applyLowSpeedAmplifier(vVal, lowSpeedAmpEnabled, lowSpeedAmpAmount, lowSpeedAmpThreshold, lowSpeedAmpHarshness)

        val effectiveSensX = when (horizontalAxis) {
            HorizontalAxis.YAW -> 200.0f / maxDpsYaw
            HorizontalAxis.ROLL -> 200.0f / maxDpsRoll
            HorizontalAxis.MIX -> 200.0f / maxDpsYaw
        }
        
        hVal *= effectiveSensX
        vVal *= (200.0f / maxDpsPitch)

        return ProcessedGyro(
            yawDps = hVal,
            pitchDps = vVal,
            rollDps = rollDps,
            rawYawDps = rawH,
            rawPitchDps = rawV,
        )
    }

    fun reset() {
        smoothedX = 0f
        smoothedY = 0f
        toggleActive = false
        toggleButtonWasPressed = false
        refQuatX = 0f
        refQuatY = 0f
        refQuatZ = 0f
        refQuatW = 1f
        hasInitialQuat = false
        lastFlickTriggerTime = 0L
        flickPulseEndTime = 0L
        activeFlickAction = null
        snapTurnDirection = 0f
        snapTurnEndTime = 0L
        isTiltUpActive = false
        isTiltDownActive = false
        isTiltLeftActive = false
        isTiltRightActive = false
        tiltUpPulseEndTime = 0L
        tiltDownPulseEndTime = 0L
        tiltLeftPulseEndTime = 0L
        tiltRightPulseEndTime = 0L
        shakePeakTimestamps.clear()
        lastShakeTriggerTime = 0L
        shakePulseEndTime = 0L
        activeShakeAction = null
    }

    /**
     * Zeros the current attitude orientation for Air Mouse (3D Flight Yoke).
     * The next quaternion frame will be captured as the new neutral forward attitude.
     */
    fun recalibrateZeroAttitude() {
        hasInitialQuat = false
    }

    private fun mapAngleToStick(
        angleDeg: Float,
        maxAngleDeg: Float,
        deadzoneDeg: Float,
        curveExp: Float
    ): Float {
        val absAngle = abs(angleDeg)
        val safeDeadzone = min(deadzoneDeg, max(0f, maxAngleDeg - 0.2f))
        if (absAngle <= safeDeadzone) return 0f
        val span = maxAngleDeg - safeDeadzone
        if (span <= 0.001f) return sign(angleDeg) * 1.0f
        val effectiveAngle = (absAngle - safeDeadzone) / span
        val clamped = effectiveAngle.coerceIn(0f, 1f)
        val curved = clamped.pow(curveExp)
        return sign(angleDeg) * curved
    }

    // =========================================================================
    // PRIVATE MATHEMATICAL UTILITIES
    // =========================================================================

    private fun applyDeadzone(value: Float, threshold: Float): Float {
        if (threshold <= 0f) return value
        val absVal = abs(value)
        if (absVal < threshold) return 0f
        return sign(value) * (absVal - threshold)
    }

    private fun applyTightening(value: Float, threshold: Float): Float {
        if (threshold <= 0f) return value
        val absVal = abs(value)
        if (absVal >= threshold) return value
        val t = absVal / threshold 
        return sign(value) * (t * t * threshold)
    }

    private fun applyLowSpeedAmplifier(
        value: Float,
        enabled: Boolean,
        amount: Float,
        threshold: Float,
        harshness: Float
    ): Float {
        if (!enabled) return value
        val speed = abs(value)
        if (speed >= threshold || threshold <= 0f) {
            return value
        }
        val slownessRatio = 1f - (speed / threshold)
        val curve = slownessRatio.pow(harshness)
        val multiplier = 1.0f + (amount - 1.0f) * curve
        return value * multiplier
    }

    private fun computeAdaptiveAlpha(speed: Float, threshold: Float, baseAlpha: Float): Float {
        if (threshold <= 0f) return baseAlpha
        val alphaMin = max(baseAlpha * 0.85f, 0.72f) 
        val alphaMax = 1.0f              
        return when {
            speed >= threshold -> alphaMax
            speed <= threshold / 2f -> alphaMin
            else -> {
                val t = (speed - threshold / 2f) / (threshold / 2f)
                alphaMin + t * (alphaMax - alphaMin)
            }
        }
    }

    private fun computeAccelerationSensitivity(
        speed: Float,
        minT: Float,
        maxT: Float,
        minS: Float,
        maxS: Float,
        accelType: AccelerationType
    ): Float {
        if (maxT <= minT) return minS
        return when {
            speed <= minT -> minS
            speed >= maxT -> maxS
            else -> {
                val t = (speed - minT) / (maxT - minT) 
                val curved = when (accelType) {
                    AccelerationType.LINEAR -> t
                    AccelerationType.POWER -> t.pow(2.0f)
                    AccelerationType.SMOOTH_STEP -> t * t * (3f - 2f * t)
                }
                minS + curved * (maxS - minS)
            }
        }
    }

    private fun isGyroActive(settings: GyroSettings, isButtonPressed: Boolean): Boolean {
        return when (settings.activationMode) {
            ActivationMode.ALWAYS_ON -> true
            ActivationMode.BUTTON_HOLD -> isButtonPressed
            ActivationMode.TOGGLE -> {
                if (isButtonPressed && !toggleButtonWasPressed) {
                    toggleActive = !toggleActive
                }
                toggleButtonWasPressed = isButtonPressed
                toggleActive
            }
        }
    }

    companion object {
        const val RAD_TO_DEG = 57.2957795f
    }
}
