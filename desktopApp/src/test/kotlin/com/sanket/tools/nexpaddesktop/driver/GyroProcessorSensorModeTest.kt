package com.sanket.tools.nexpaddesktop.driver

import com.sanket.tools.nexpad.protocol.NexpadProtocol
import com.sanket.tools.nexpaddesktop.model.GyroSettings
import com.sanket.tools.nexpaddesktop.model.InputMode
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
        // Yaw should be derived from rawGyroZ (-yawDps)
        assertTrue("Yaw should be non-zero", abs(result.yawDps) > 0f)
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
}
