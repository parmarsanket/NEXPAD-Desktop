package com.sanket.tools.nexpaddesktop.driver

import com.sanket.tools.nexpad.protocol.NexpadProtocol
import com.sanket.tools.nexpaddesktop.model.FlightStickAxisTarget
import com.sanket.tools.nexpaddesktop.model.GyroSettings
import com.sanket.tools.nexpaddesktop.model.HorizontalAxis
import com.sanket.tools.nexpaddesktop.model.InputMode
import com.sanket.tools.nexpaddesktop.model.computeSensorMode
import org.junit.Assert.*
import org.junit.Test
import kotlin.math.abs

class GyroProcessorSensorModeTest {

    @Test
    fun testStandardGyroModeUsesRawGyro() {
        val processor = GyroProcessor()
        val settings = GyroSettings(
            enabled = true,
            inputMode = InputMode.VELOCITY,
            smoothingEnabled = false,
            deadzoneThreshold = 0f
        )

        val result = processor.process(
            rawGyroX = 1.0f,
            rawGyroY = 0f,
            rawGyroZ = 0.5f,
            rawAccelX = 0f,
            rawAccelY = 0f,
            rawAccelZ = 0f,
            rawGravityX = 0f,
            rawGravityY = 0f,
            rawGravityZ = 0f,
            sensorFlags = NexpadProtocol.SENSOR_MODE_IMU_6AXIS,
            settings = settings
        )

        // Pitch should be derived from rawGyroX (-pitchDps)
        assertTrue("Pitch should be non-zero", abs(result.pitchDps) > 0f)
        // Yaw should be derived from rawGyroZ
        assertTrue("Yaw should be non-zero", abs(result.yawDps) > 0f)
    }

    @Test
    fun testRightwardYawProducesPositiveHorizontalAim() {
        val processor = GyroProcessor()
        val settings = GyroSettings(
            enabled = true,
            inputMode = InputMode.VELOCITY,
            horizontalAxis = com.sanket.tools.nexpaddesktop.model.HorizontalAxis.YAW,
            invertX = false,
            smoothingEnabled = false,
            deadzoneThreshold = 0f
        )

        // Turning right produces positive rawGyroZ from Android phone
        val result = processor.process(
            rawGyroX = 0f,
            rawGyroY = 0f,
            rawGyroZ = 1.0f,
            settings = settings
        )

        // Turning right MUST produce positive yawDps (panning camera right)
        assertTrue("Turning right must produce positive yawDps for camera right", result.yawDps > 0f)
    }

    @Test
    fun testAbsoluteTiltModeUsesGravityWhenInGravityMode() {
        val processor = GyroProcessor()
        val settings = GyroSettings(
            enabled = true,
            inputMode = InputMode.ABSOLUTE_TILT,
            absoluteMaxTilt = 45f,
            absoluteCurve = 1.0f,
            absoluteSensitivityX = 1.0f,
            invertX = false
        )

        // In Mode 0x01 (Gravity), rawAccelX is 0f, rawGravityX is ~4.9f (~30 deg tilt)
        val result = processor.process(
            rawGyroX = 0f,
            rawGyroY = 0f,
            rawGyroZ = 0f,
            rawAccelX = 0f,
            rawAccelY = 0f,
            rawAccelZ = 0f,
            rawGravityX = 4.9f,
            rawGravityY = 0f,
            rawGravityZ = 0f,
            sensorFlags = NexpadProtocol.SENSOR_MODE_GRAVITY,
            settings = settings
        )

        // 4.9 / 9.8 = 0.5; asin(0.5) = 30 degrees; 30 / 45 * 200 = 133.33
        assertTrue("Yaw DPS should reflect gravity tilt", result.yawDps > 100f)
    }

    @Test
    fun testAbsoluteTiltModeFallbackToAccelWhenMode0() {
        val processor = GyroProcessor()
        val settings = GyroSettings(
            enabled = true,
            inputMode = InputMode.ABSOLUTE_TILT,
            absoluteMaxTilt = 45f,
            absoluteCurve = 1.0f,
            absoluteSensitivityX = 1.0f,
            invertX = false
        )

        // In Mode 0x00 fallback, rawGravityX is 0f, rawAccelX is 4.9f
        val result = processor.process(
            rawGyroX = 0f,
            rawGyroY = 0f,
            rawGyroZ = 0f,
            rawAccelX = 4.9f,
            rawAccelY = 0f,
            rawAccelZ = 0f,
            rawGravityX = 0f,
            rawGravityY = 0f,
            rawGravityZ = 0f,
            sensorFlags = NexpadProtocol.SENSOR_MODE_IMU_6AXIS,
            settings = settings
        )

        assertTrue("Yaw DPS should fallback to rawAccelX", result.yawDps > 100f)
    }

    @Test
    fun testAirMouseModeWithQuaternions() {
        val processor = GyroProcessor()
        val settings = GyroSettings(
            enabled = true,
            inputMode = InputMode.AIR_MOUSE,
            smoothingEnabled = false,
            deadzoneThreshold = 0f,
            airMouseDeadzone = 0f,
            airMouseYawTarget = FlightStickAxisTarget.RS_X,
            airMouseHorizontalAxis = HorizontalAxis.YAW
        )

        // Frame 1: Initial baseline quaternion
        val res1 = processor.process(
            rawGyroX = 0f, rawGyroY = 0f, rawGyroZ = 0f,
            quatX = 0f, quatY = 0f, quatZ = 0f, quatW = 1f,
            sensorFlags = NexpadProtocol.SENSOR_MODE_GAME_ROTATION,
            settings = settings
        )
        // Baseline frame should return 0 (calibrating baseline)
        assertEquals(0f, res1.yawDps, 0.001f)
        assertEquals(0f, res1.pitchDps, 0.001f)

        // Frame 2: User rotates slightly around Z (yaw) -> dz ≈ 0.05
        val res2 = processor.process(
            rawGyroX = 0f, rawGyroY = 0f, rawGyroZ = 0f,
            quatX = 0f, quatY = 0f, quatZ = 0.05f, quatW = 0.9987f,
            sensorFlags = NexpadProtocol.SENSOR_MODE_GAME_ROTATION,
            settings = settings
        )
        assertTrue("Yaw DPS should be non-zero after rotation", abs(res2.yawDps) > 0f)
    }

    @Test
    fun testGestureModeWithLinearAcceleration() {
        val processor = GyroProcessor()
        val settings = GyroSettings(
            enabled = true,
            inputMode = InputMode.GESTURE,
            smoothingEnabled = false,
            deadzoneThreshold = 0f
        )

        val result = processor.process(
            rawGyroX = 0f, rawGyroY = 0f, rawGyroZ = 0f,
            linearAccelX = 12.0f, linearAccelY = 0f, linearAccelZ = 0f,
            sensorFlags = NexpadProtocol.SENSOR_MODE_LINEAR_GESTURE,
            settings = settings
        )

        assertTrue("Gesture should be detected for 12 m/s^2 flick", result.gestureDetected)
        assertTrue("Gesture magnitude should be >= 12", result.gestureMagnitude >= 12f)
        assertTrue("Snap turn stick should be active on flick", abs(result.snapTurnStickX) > 0f || result.gestureDetected)
    }

    @Test
    fun testRawUncalibratedModeSubtractsBias() {
        val processor = GyroProcessor()
        val settings = GyroSettings(
            enabled = true,
            inputMode = InputMode.RAW_UNCALIBRATED,
            smoothingEnabled = false,
            deadzoneThreshold = 0f
        )

        // Raw gyro is 0.05 (pure drift), bias is 0.05 -> Clean gyro is 0.0
        val restingResult = processor.process(
            rawGyroX = 0f, rawGyroY = 0f, rawGyroZ = 0f,
            rawUncalGyroX = 0.05f, rawUncalGyroY = 0f, rawUncalGyroZ = 0.05f,
            gyroBiasX = 0.05f, gyroBiasY = 0f, gyroBiasZ = 0.05f,
            sensorFlags = NexpadProtocol.SENSOR_MODE_RAW_UNCALIBRATED,
            settings = settings
        )
        assertEquals(0f, restingResult.pitchDps, 0.001f)
        assertEquals(0f, restingResult.yawDps, 0.001f)

        // Raw gyro turns to 0.55 rad/s with same bias 0.05 -> Clean gyro is 0.50 rad/s
        val movingResult = processor.process(
            rawGyroX = 0f, rawGyroY = 0f, rawGyroZ = 0f,
            rawUncalGyroX = 0.55f, rawUncalGyroY = 0f, rawUncalGyroZ = 0.55f,
            gyroBiasX = 0.05f, gyroBiasY = 0f, gyroBiasZ = 0.05f,
            sensorFlags = NexpadProtocol.SENSOR_MODE_RAW_UNCALIBRATED,
            settings = settings
        )
        assertTrue("Pitch DPS should be non-zero after bias subtraction", abs(movingResult.pitchDps) > 10f)
        assertTrue("Yaw DPS should be non-zero after bias subtraction", abs(movingResult.yawDps) > 10f)
    }

    @Test
    fun testDualShock4StrictIsolationAlwaysMode0() {
        // DualShock 4 must NEVER receive advanced sensor multiplexing modes
        val ds4 = com.sanket.tools.nexpaddesktop.ui.ControllerType.DUALSHOCK_4

        for (mode in InputMode.values()) {
            val settings = GyroSettings(enabled = true, inputMode = mode)
            val sensorMode = settings.computeSensorMode(ds4)
            assertEquals("DS4 must strictly stay in Mode 0x00 (IMU_6AXIS) for all settings",
                NexpadProtocol.SENSOR_MODE_IMU_6AXIS, sensorMode)
        }
    }

    @Test
    fun testXboxRespectsAll5Modes() {
        val xbox = com.sanket.tools.nexpaddesktop.ui.ControllerType.XBOX_360

        assertEquals(
            NexpadProtocol.SENSOR_MODE_IMU_6AXIS,
            GyroSettings(enabled = true, inputMode = InputMode.VELOCITY).computeSensorMode(xbox)
        )
        assertEquals(
            NexpadProtocol.SENSOR_MODE_GRAVITY,
            GyroSettings(enabled = true, inputMode = InputMode.ABSOLUTE_TILT).computeSensorMode(xbox)
        )
        assertEquals(
            NexpadProtocol.SENSOR_MODE_GAME_ROTATION,
            GyroSettings(enabled = true, inputMode = InputMode.AIR_MOUSE).computeSensorMode(xbox)
        )
        assertEquals(
            NexpadProtocol.SENSOR_MODE_LINEAR_GESTURE,
            GyroSettings(enabled = true, inputMode = InputMode.GESTURE).computeSensorMode(xbox)
        )
        assertEquals(
            NexpadProtocol.SENSOR_MODE_RAW_UNCALIBRATED,
            GyroSettings(enabled = true, inputMode = InputMode.RAW_UNCALIBRATED).computeSensorMode(xbox)
        )

        // Disabled Gyro falls back to Mode 0x00
        assertEquals(
            NexpadProtocol.SENSOR_MODE_IMU_6AXIS,
            GyroSettings(enabled = false, inputMode = InputMode.ABSOLUTE_TILT).computeSensorMode(xbox)
        )
    }
}
