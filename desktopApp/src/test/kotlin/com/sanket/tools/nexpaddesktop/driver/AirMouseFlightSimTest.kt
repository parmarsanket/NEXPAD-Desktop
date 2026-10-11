package com.sanket.tools.nexpaddesktop.driver

import com.sanket.tools.nexpad.model.GamepadInput
import com.sanket.tools.nexpaddesktop.model.*
import com.sanket.tools.nexpaddesktop.ui.ControllerType
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Validates the specialized 3D Flight Stick & Aviation Yoke pipeline (InputMode.AIR_MOUSE).
 *
 * Checks that Android's 3D Game Rotation Vector quaternion is accurately translated
 * into elevator (pitch) and aileron (roll banking) controls for flight simulators,
 * with center tremor deadzones and aviation pitch inversion.
 */
class AirMouseFlightSimTest {

    private fun createQuat(pitchRad: Float, rollRad: Float, yawRad: Float): FloatArray {
        val qx = sin(pitchRad * 0.5f)
        val wx = cos(pitchRad * 0.5f)
        
        val qy = sin(rollRad * 0.5f)
        val wy = cos(rollRad * 0.5f)
        
        val qz = sin(yawRad * 0.5f)
        val wz = cos(yawRad * 0.5f)
        
        // Tait-Bryan q = q_z * q_y * q_x
        val yx_w = wy * wx
        val yx_x = wy * qx
        val yx_y = qy * wx
        val yx_z = -qy * qx
        
        val w = wz * yx_w - qz * yx_z
        val x = wz * yx_x + qz * yx_y
        val y = wz * yx_y - qz * yx_x
        val z = wz * yx_z + qz * yx_w
        return floatArrayOf(x, y, z, w)
    }

    @Test
    fun testInitialReferenceQuaternionCapturesZeroAttitude() {
        val processor = GyroProcessor()
        val settings = GyroSettings.FLIGHT_SIM_PRESET

        // Frame 1: Initial natural forward hold (neutral forward posture)
        val neutralQ = createQuat(0f, 0f, 0f)
        val result1 = processor.process(
            rawGyroX = 0f, rawGyroY = 0f, rawGyroZ = 0f,
            quatX = neutralQ[0], quatY = neutralQ[1], quatZ = neutralQ[2], quatW = neutralQ[3],
            settings = settings
        )

        // First frame must calibrate zero reference and return neutral stick (0, 0)
        assertEquals(0f, result1.yawDps, 0.001f)
        assertEquals(0f, result1.pitchDps, 0.001f)

        // Frame 2: Still holding at the same reference attitude -> must remain 0 deflection
        val result2 = processor.process(
            rawGyroX = 0f, rawGyroY = 0f, rawGyroZ = 0f,
            quatX = neutralQ[0], quatY = neutralQ[1], quatZ = neutralQ[2], quatW = neutralQ[3],
            settings = settings
        )
        assertEquals(0f, result2.yawDps, 0.001f)
        assertEquals(0f, result2.pitchDps, 0.001f)
    }

    @Test
    fun testCenterRestingDeadzoneFiltersMicroTremors() {
        val processor = GyroProcessor()
        val settings = GyroSettings.FLIGHT_SIM_PRESET.copy(airMouseDeadzone = 2.0f)

        // Initialize at 0
        val neutralQ = createQuat(0f, 0f, 0f)
        processor.process(
            rawGyroX = 0f, rawGyroY = 0f, rawGyroZ = 0f,
            quatX = neutralQ[0], quatY = neutralQ[1], quatZ = neutralQ[2], quatW = neutralQ[3],
            settings = settings
        )

        // Micro-tremor: 1.0 degree tilt (less than 2.0 degree deadzone)
        val tremorRad = Math.toRadians(1.0).toFloat()
        val tremorQ = createQuat(tremorRad, tremorRad, 0f)
        val resultTremor = processor.process(
            rawGyroX = 0f, rawGyroY = 0f, rawGyroZ = 0f,
            quatX = tremorQ[0], quatY = tremorQ[1], quatZ = tremorQ[2], quatW = tremorQ[3],
            settings = settings
        )

        assertEquals(0f, resultTremor.yawDps, 0.001f, "Resting tremor below deadzone must output 0")
        assertEquals(0f, resultTremor.pitchDps, 0.001f, "Resting tremor below deadzone must output 0")
    }

    @Test
    fun testWingBankingAndElevatorPitchDeflection() {
        val processor = GyroProcessor()
        val settings = GyroSettings.FLIGHT_SIM_PRESET.copy(
            airMouseMaxPitch = 35.0f,
            airMouseMaxRollYaw = 35.0f,
            airMouseDeadzone = 0.0f,
            airMouseCurve = 1.0f, // Pure linear for exact check
            airMouseInvertPitch = true
        )

        // Init reference
        val neutralQ = createQuat(0f, 0f, 0f)
        processor.process(
            rawGyroX = 0f, rawGyroY = 0f, rawGyroZ = 0f,
            quatX = neutralQ[0], quatY = neutralQ[1], quatZ = neutralQ[2], quatW = neutralQ[3],
            settings = settings
        )

        // Tilt 35° pitch (pull back / nose up) and 17.5° roll (half wing bank)
        // Note: in phone coordinates, pitch is rotation along X, roll is rotation along Y
        val pitchRad = Math.toRadians(35.0).toFloat()
        val rollRad = Math.toRadians(17.5).toFloat()
        val bankQ = createQuat(pitchRad, rollRad, 0f)

        val result = processor.process(
            rawGyroX = 0f, rawGyroY = 0f, rawGyroZ = 0f,
            quatX = bankQ[0], quatY = bankQ[1], quatZ = bankQ[2], quatW = bankQ[3],
            settings = settings
        )

        // yawDps is scaled by 200.0f in GyroProcessor
        // Half bank (17.5° / 35°) = 0.5 -> 0.5 * 200 = 100.0f
        assertEquals(100.0f, result.yawDps, 15.0f)

        // Full pitch (35° / 35°) = 1.0 -> 1.0 * 200 = 200.0f (or -200 depending on invert)
        assertTrue(abs(result.pitchDps) > 170.0f, "Pitch deflection at 35° must near full deflection (200 dps)")
    }

    @Test
    fun testInputPipelineRoutesFlightControlsToLeftStickByDefault() {
        val processor = GyroProcessor()
        val settings = GyroSettings.FLIGHT_SIM_PRESET.copy(
            airMouseTargetStick = XboxTargetStick.LEFT_STICK,
            airMouseDeadzone = 0.0f,
            airMouseCurve = 1.0f
        )

        // Init
        val neutralQ = createQuat(0f, 0f, 0f)
        processor.process(
            rawGyroX = 0f, rawGyroY = 0f, rawGyroZ = 0f,
            quatX = neutralQ[0], quatY = neutralQ[1], quatZ = neutralQ[2], quatW = neutralQ[3],
            settings = settings
        )

        // 35° banking
        val bankRad = Math.toRadians(35.0).toFloat()
        val bankQ = createQuat(0f, bankRad, 0f)

        val gamepadInput = GamepadInput(
            quatX = bankQ[0], quatY = bankQ[1], quatZ = bankQ[2], quatW = bankQ[3]
        )

        val processedInput = InputPipeline.processInput(
            input = gamepadInput,
            activeController = ControllerType.XBOX_360,
            gyroProcessor = processor,
            gyroSettings = settings,
            lsSensitivityX = 1.0f,
            lsSensitivityY = 1.0f,
            rsSensitivityX = 1.0f,
            rsSensitivityY = 1.0f,
            onProcessedAngles = { _, _ -> }
        )

        // Left stick receives flight controls
        assertTrue(abs(processedInput.leftStickX) > 0.8f, "Left stick X must receive wing bank deflection")
        assertEquals(0f, processedInput.rightStickX, 0.01f, "Right stick must remain unaffected")
    }

    @Test
    fun testIndividualAxisCustomStickAndDirectionMapping() {
        val processor = GyroProcessor()
        // Custom setup:
        // Yaw -> Right Stick Up/Down (RS_Y), inverted
        // Pitch -> Right Stick Left/Right (RS_X), normal
        // Roll -> Left Stick Up/Down (LS_Y), normal
        val settings = GyroSettings.FLIGHT_SIM_PRESET.copy(
            airMouseYawTarget = FlightStickAxisTarget.RS_Y,
            airMouseYawInvert = true,
            airMousePitchTarget = FlightStickAxisTarget.RS_X,
            airMousePitchInvert = false,
            airMouseRollTarget = FlightStickAxisTarget.LS_Y,
            airMouseRollInvert = false,
            airMouseDeadzone = 0.0f,
            airMouseCurve = 1.0f,
            airMouseMaxYaw = 35.0f,
            airMouseMaxPitch = 35.0f,
            airMouseMaxRoll = 35.0f,
        )

        // Init
        val neutralQ = createQuat(0f, 0f, 0f)
        processor.process(
            rawGyroX = 0f, rawGyroY = 0f, rawGyroZ = 0f,
            quatX = neutralQ[0], quatY = neutralQ[1], quatZ = neutralQ[2], quatW = neutralQ[3],
            settings = settings
        )

        // Rotate 35° Yaw only (flat table turn)
        val yawRad = Math.toRadians(35.0).toFloat()
        val yawQ = createQuat(0f, 0f, yawRad)

        val gamepadInput = GamepadInput(
            quatX = yawQ[0], quatY = yawQ[1], quatZ = yawQ[2], quatW = yawQ[3]
        )

        val processedInput = InputPipeline.processInput(
            input = gamepadInput,
            activeController = ControllerType.XBOX_360,
            gyroProcessor = processor,
            gyroSettings = settings,
            lsSensitivityX = 1.0f, lsSensitivityY = 1.0f,
            rsSensitivityX = 1.0f, rsSensitivityY = 1.0f,
            onProcessedAngles = { _, _ -> }
        )

        // Yaw was mapped to RS_Y inverted -> rightStickY must be near -1.0
        assertTrue(processedInput.rightStickY < -0.8f, "Yaw mapped to RS_Y inverted must deflect rightStickY negatively")
        assertEquals(0f, processedInput.rightStickX, 0.01f, "RS_X must be 0")
        assertEquals(0f, processedInput.leftStickX, 0.01f, "LS_X must be 0")
        assertEquals(0f, processedInput.leftStickY, 0.01f, "LS_Y must be 0")
    }

    @Test
    fun testExtremeSmallMaxLockAngleOneDegree() {
        val processor = GyroProcessor()
        val settings = GyroSettings.FLIGHT_SIM_PRESET.copy(
            airMouseMaxRoll = 1.0f,
            airMouseCurve = 1.0f,
            airMouseRollTarget = FlightStickAxisTarget.LS_X,
            airMouseRollInvert = false,
        )

        // Init
        val neutralQ = createQuat(0f, 0f, 0f)
        processor.process(
            rawGyroX = 0f, rawGyroY = 0f, rawGyroZ = 0f,
            quatX = neutralQ[0], quatY = neutralQ[1], quatZ = neutralQ[2], quatW = neutralQ[3],
            settings = settings
        )

        // Tilt 1.0° roll
        val rollRad = Math.toRadians(1.0).toFloat()
        val rollQ = createQuat(0f, rollRad, 0f)

        val gamepadInput = GamepadInput(
            quatX = rollQ[0], quatY = rollQ[1], quatZ = rollQ[2], quatW = rollQ[3]
        )

        val processedInput = InputPipeline.processInput(
            input = gamepadInput,
            activeController = ControllerType.XBOX_360,
            gyroProcessor = processor,
            gyroSettings = settings,
            lsSensitivityX = 1.0f, lsSensitivityY = 1.0f,
            rsSensitivityX = 1.0f, rsSensitivityY = 1.0f,
            onProcessedAngles = { _, _ -> }
        )

        // At 1.0° max angle, 1.0° tilt must reach 100% full stick deflection
        assertTrue(processedInput.leftStickX >= 0.95f, "1.0 degree tilt must yield full deflection when maxAngle is 1.0°")
    }

    @Test
    fun testExtremeLargeMaxLockAngle179Degrees() {
        val processor = GyroProcessor()
        val settings = GyroSettings.FLIGHT_SIM_PRESET.copy(
            airMouseMaxRoll = 179.0f,
            airMouseDeadzone = 0.0f,
            airMouseCurve = 1.0f,
            airMouseRollTarget = FlightStickAxisTarget.LS_X,
            airMouseRollInvert = false,
        )

        // Init
        val neutralQ = createQuat(0f, 0f, 0f)
        processor.process(
            rawGyroX = 0f, rawGyroY = 0f, rawGyroZ = 0f,
            quatX = neutralQ[0], quatY = neutralQ[1], quatZ = neutralQ[2], quatW = neutralQ[3],
            settings = settings
        )

        // Half-way turn: 89.5° roll
        val rollRad = Math.toRadians(89.5).toFloat()
        val rollQ = createQuat(0f, rollRad, 0f)

        val gamepadInput = GamepadInput(
            quatX = rollQ[0], quatY = rollQ[1], quatZ = rollQ[2], quatW = rollQ[3]
        )

        val processedInput = InputPipeline.processInput(
            input = gamepadInput,
            activeController = ControllerType.XBOX_360,
            gyroProcessor = processor,
            gyroSettings = settings,
            lsSensitivityX = 1.0f, lsSensitivityY = 1.0f,
            rsSensitivityX = 1.0f, rsSensitivityY = 1.0f,
            onProcessedAngles = { _, _ -> }
        )

        // At 179° max angle, 89.5° must yield approximately 0.50 stick deflection
        assertEquals(0.50f, processedInput.leftStickX, 0.05f, "Halfway 89.5° tilt must yield ~0.50 deflection with 179° max angle")
    }
}
