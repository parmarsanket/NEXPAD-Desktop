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
        val qx = sin(rollRad * 0.5f)
        val wx = cos(rollRad * 0.5f)

        val qy = -sin(pitchRad * 0.5f)
        val wy = cos(pitchRad * 0.5f)

        val qz = -sin(yawRad * 0.5f)
        val wz = cos(yawRad * 0.5f)

        // yx = qy * qx
        val yx_w = wy * wx
        val yx_x = wy * qx
        val yx_y = qy * wx
        val yx_z = -qy * qx

        // q = qz * yx
        val w = wz * yx_w - qz * yx_z
        val x = wz * yx_x - qz * yx_y
        val y = wz * yx_y + qz * yx_x
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
        val settings = GyroSettings.FLIGHT_SIM_PRESET.copy(flightStick = GyroSettings.FLIGHT_SIM_PRESET.flightStick.copy(deadzone = 2.0f))

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
            flightStick = GyroSettings.FLIGHT_SIM_PRESET.flightStick.copy(
                maxPitchAngle = 35.0f,
                maxRollAngle = 35.0f,
                maxYawAngle = 35.0f,
                deadzone = 0.0f,
                curveExponent = 1.0f,
                pitchInvert = true
            )
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
            flightStick = GyroSettings.FLIGHT_SIM_PRESET.flightStick.copy(
                rollTarget = FlightStickAxisTarget.LS_X,
                deadzone = 0.0f,
                curveExponent = 1.0f
            )
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
            flightStick = GyroSettings.FLIGHT_SIM_PRESET.flightStick.copy(
                yawTarget = FlightStickAxisTarget.RS_Y,
                yawInvert = true,
                pitchTarget = FlightStickAxisTarget.RS_X,
                pitchInvert = false,
                rollTarget = FlightStickAxisTarget.LS_Y,
                rollInvert = false,
                deadzone = 0.0f,
                curveExponent = 1.0f,
                maxYawAngle = 35.0f,
                maxPitchAngle = 35.0f,
                maxRollAngle = 35.0f,
            )
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
            flightStick = GyroSettings.FLIGHT_SIM_PRESET.flightStick.copy(
                maxRollAngle = 1.0f,
                curveExponent = 1.0f,
                rollTarget = FlightStickAxisTarget.LS_X,
                rollInvert = false,
            )
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
            flightStick = GyroSettings.FLIGHT_SIM_PRESET.flightStick.copy(
                maxRollAngle = 179.0f,
                deadzone = 0.0f,
                curveExponent = 1.0f,
                rollTarget = FlightStickAxisTarget.LS_X,
                rollInvert = false,
            )
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

    @Test
    fun testFlatSurfaceSteadyRestingZeroParity() {
        val processor = GyroProcessor()
        val settings = GyroSettings.FLIGHT_SIM_PRESET.copy(
            flightStick = GyroSettings.FLIGHT_SIM_PRESET.flightStick.copy(
                maxPitchAngle = 35.0f,
                maxRollAngle = 35.0f,
                deadzone = 0.0f
            )
        )

        // Phone lying flat on desk/surface: clean zero orientation
        val flatTableQuat = createQuat(0f, 0f, 0f)
        val resInitial = processor.process(
            rawGyroX = 0f, rawGyroY = 0f, rawGyroZ = 0f,
            quatX = flatTableQuat[0], quatY = flatTableQuat[1], quatZ = flatTableQuat[2], quatW = flatTableQuat[3],
            settings = settings
        )
        assertEquals(0f, resInitial.flightLsX, 0.001f, "Resting flat on table must produce zero Left Stick X")
        assertEquals(0f, resInitial.flightLsY, 0.001f, "Resting flat on table must produce zero Left Stick Y")

        val resSteady = processor.process(
            rawGyroX = 0f, rawGyroY = 0f, rawGyroZ = 0f,
            quatX = flatTableQuat[0], quatY = flatTableQuat[1], quatZ = flatTableQuat[2], quatW = flatTableQuat[3],
            settings = settings
        )
        assertEquals(0f, resSteady.flightLsX, 0.001f, "Steady flat table must never drift or deflect to right side")
        assertEquals(0f, resSteady.flightLsY, 0.001f, "Steady flat table must never drift or deflect up")
    }

    @Test
    fun testSteadyPhoneRollDirectionalParity() {
        val processor = GyroProcessor()
        val settings = GyroSettings.FLIGHT_SIM_PRESET.copy(
            flightStick = GyroSettings.FLIGHT_SIM_PRESET.flightStick.copy(
                maxRollAngle = 35.0f,
                maxPitchAngle = 35.0f,
                deadzone = 0.0f,
                curveExponent = 1.0f,
                rollInvert = false,
                pitchInvert = false
            )
        )

        // Baseline on flat table
        val neutralQ = createQuat(0f, 0f, 0f)
        processor.process(
            rawGyroX = 0f, rawGyroY = 0f, rawGyroZ = 0f,
            quatX = neutralQ[0], quatY = neutralQ[1], quatZ = neutralQ[2], quatW = neutralQ[3],
            settings = settings
        )

        // 1. Tilt Right by 35 deg (bank right wing down) -> must deflect LS to the RIGHT (positive)
        val rad35 = Math.toRadians(35.0).toFloat()
        val rollRightQ = createQuat(pitchRad = 0f, rollRad = rad35, yawRad = 0f)
        val resRight = processor.process(
            rawGyroX = 0f, rawGyroY = 0f, rawGyroZ = 0f,
            quatX = rollRightQ[0], quatY = rollRightQ[1], quatZ = rollRightQ[2], quatW = rollRightQ[3],
            settings = settings
        )
        assertEquals(1.0f, resRight.flightLsX, 0.01f, "Tilting phone right must deflect Left Stick to the RIGHT side (+1.0)")
        assertEquals(0.0f, resRight.flightLsY, 0.01f, "Roll must have zero pitch bleed")

        // 2. Tilt Left by 35 deg (bank left wing down) -> must deflect LS to the LEFT (negative)
        val rollLeftQ = createQuat(pitchRad = 0f, rollRad = -rad35, yawRad = 0f)
        val resLeft = processor.process(
            rawGyroX = 0f, rawGyroY = 0f, rawGyroZ = 0f,
            quatX = rollLeftQ[0], quatY = rollLeftQ[1], quatZ = rollLeftQ[2], quatW = rollLeftQ[3],
            settings = settings
        )
        assertEquals(-1.0f, resLeft.flightLsX, 0.01f, "Tilting phone left must deflect Left Stick to the LEFT side (-1.0)")
        assertEquals(0.0f, resLeft.flightLsY, 0.01f, "Roll must have zero pitch bleed")
    }

    @Test
    fun testSteadyPhonePitchDirectionalParity() {
        val processor = GyroProcessor()
        val settings = GyroSettings.FLIGHT_SIM_PRESET.copy(
            flightStick = GyroSettings.FLIGHT_SIM_PRESET.flightStick.copy(
                maxPitchAngle = 35.0f,
                maxRollAngle = 35.0f,
                deadzone = 0.0f,
                curveExponent = 1.0f,
                pitchInvert = false,
                rollInvert = false
            )
        )

        // Baseline on flat table
        val neutralQ = createQuat(0f, 0f, 0f)
        processor.process(
            rawGyroX = 0f, rawGyroY = 0f, rawGyroZ = 0f,
            quatX = neutralQ[0], quatY = neutralQ[1], quatZ = neutralQ[2], quatW = neutralQ[3],
            settings = settings
        )

        val rad35 = Math.toRadians(35.0).toFloat()

        // 1. Tilt Up by 35 deg (lift far edge) -> must deflect LS UP (positive)
        val pitchUpQ = createQuat(pitchRad = rad35, rollRad = 0f, yawRad = 0f)
        val resUp = processor.process(
            rawGyroX = 0f, rawGyroY = 0f, rawGyroZ = 0f,
            quatX = pitchUpQ[0], quatY = pitchUpQ[1], quatZ = pitchUpQ[2], quatW = pitchUpQ[3],
            settings = settings
        )
        assertEquals(1.0f, resUp.flightLsY, 0.01f, "Tilting phone up must deflect Left Stick UP (+1.0)")
        assertEquals(0.0f, resUp.flightLsX, 0.01f, "Pitch must have zero roll bleed")

        // 2. Tilt Down by 35 deg (push far edge down) -> must deflect LS DOWN (negative)
        val pitchDownQ = createQuat(pitchRad = -rad35, rollRad = 0f, yawRad = 0f)
        val resDown = processor.process(
            rawGyroX = 0f, rawGyroY = 0f, rawGyroZ = 0f,
            quatX = pitchDownQ[0], quatY = pitchDownQ[1], quatZ = pitchDownQ[2], quatW = pitchDownQ[3],
            settings = settings
        )
        assertEquals(-1.0f, resDown.flightLsY, 0.01f, "Tilting phone down must deflect Left Stick DOWN (-1.0)")
        assertEquals(0.0f, resDown.flightLsX, 0.01f, "Pitch must have zero roll bleed")
    }

    @Test
    fun testSteadyPhoneYawIsolation() {
        val processor = GyroProcessor()
        val settings = GyroSettings.FLIGHT_SIM_PRESET.copy(
            flightStick = GyroSettings.FLIGHT_SIM_PRESET.flightStick.copy(
                maxPitchAngle = 35.0f,
                maxRollAngle = 35.0f,
                maxYawAngle = 35.0f,
                deadzone = 0.0f
            )
        )

        // Baseline
        val neutralQ = createQuat(0f, 0f, 0f)
        processor.process(
            rawGyroX = 0f, rawGyroY = 0f, rawGyroZ = 0f,
            quatX = neutralQ[0], quatY = neutralQ[1], quatZ = neutralQ[2], quatW = neutralQ[3],
            settings = settings
        )

        // Swivel phone flat on table (Yaw 35 deg)
        val rad35 = Math.toRadians(35.0).toFloat()
        val yawQ = createQuat(pitchRad = 0f, rollRad = 0f, yawRad = rad35)
        val resYaw = processor.process(
            rawGyroX = 0f, rawGyroY = 0f, rawGyroZ = 0f,
            quatX = yawQ[0], quatY = yawQ[1], quatZ = yawQ[2], quatW = yawQ[3],
            settings = settings
        )
        assertEquals(0.0f, resYaw.flightLsX, 0.01f, "Flat table swivel must not bleed into roll stick X")
        assertEquals(0.0f, resYaw.flightLsY, 0.01f, "Flat table swivel must not bleed into pitch stick Y")
    }

    @Test
    fun testSteadyPhoneYawDirectionalParity() {
        val processor = GyroProcessor()
        val settings = GyroSettings.FLIGHT_SIM_PRESET.copy(
            flightStick = GyroSettings.FLIGHT_SIM_PRESET.flightStick.copy(
                rollTarget = FlightStickAxisTarget.LS_X,
                pitchTarget = FlightStickAxisTarget.LS_Y,
                yawTarget = FlightStickAxisTarget.RS_X,
                maxPitchAngle = 35.0f,
                maxRollAngle = 35.0f,
                maxYawAngle = 35.0f,
                deadzone = 0.0f,
                curveExponent = 1.0f,
                yawInvert = false
            )
        )

        // Baseline
        val neutralQ = createQuat(0f, 0f, 0f)
        processor.process(
            rawGyroX = 0f, rawGyroY = 0f, rawGyroZ = 0f,
            quatX = neutralQ[0], quatY = neutralQ[1], quatZ = neutralQ[2], quatW = neutralQ[3],
            settings = settings
        )

        val rad35 = Math.toRadians(35.0).toFloat()

        // 1. Swivel Right by 35 deg -> RS_X must deflect to the RIGHT (+1.0)
        val yawRightQ = createQuat(pitchRad = 0f, rollRad = 0f, yawRad = rad35)
        val resRight = processor.process(
            rawGyroX = 0f, rawGyroY = 0f, rawGyroZ = 0f,
            quatX = yawRightQ[0], quatY = yawRightQ[1], quatZ = yawRightQ[2], quatW = yawRightQ[3],
            settings = settings
        )
        assertEquals(1.0f, resRight.flightRsX, 0.01f, "Swiveling phone right on table must deflect Right Stick to the RIGHT side (+1.0)")
        assertEquals(0.0f, resRight.flightLsX, 0.01f, "Yaw must have zero roll bleed")
        assertEquals(0.0f, resRight.flightLsY, 0.01f, "Yaw must have zero pitch bleed")

        // 2. Swivel Left by 35 deg -> RS_X must deflect to the LEFT (-1.0)
        val yawLeftQ = createQuat(pitchRad = 0f, rollRad = 0f, yawRad = -rad35)
        val resLeft = processor.process(
            rawGyroX = 0f, rawGyroY = 0f, rawGyroZ = 0f,
            quatX = yawLeftQ[0], quatY = yawLeftQ[1], quatZ = yawLeftQ[2], quatW = yawLeftQ[3],
            settings = settings
        )
        assertEquals(-1.0f, resLeft.flightRsX, 0.01f, "Swiveling phone left on table must deflect Right Stick to the LEFT side (-1.0)")
        assertEquals(0.0f, resLeft.flightLsX, 0.01f, "Yaw must have zero roll bleed")
        assertEquals(0.0f, resLeft.flightLsY, 0.01f, "Yaw must have zero pitch bleed")
    }

    @Test
    fun testRightStickMappingParity() {
        val processor = GyroProcessor()
        val settings = GyroSettings.FLIGHT_SIM_PRESET.copy(
            flightStick = GyroSettings.FLIGHT_SIM_PRESET.flightStick.copy(
                rollTarget = FlightStickAxisTarget.RS_X,
                pitchTarget = FlightStickAxisTarget.RS_Y,
                maxPitchAngle = 35.0f,
                maxRollAngle = 35.0f,
                deadzone = 0.0f,
                rollInvert = false,
                pitchInvert = false
            )
        )

        // Baseline
        val neutralQ = createQuat(0f, 0f, 0f)
        processor.process(
            rawGyroX = 0f, rawGyroY = 0f, rawGyroZ = 0f,
            quatX = neutralQ[0], quatY = neutralQ[1], quatZ = neutralQ[2], quatW = neutralQ[3],
            settings = settings
        )

        val rad35 = Math.toRadians(35.0).toFloat()

        // 1. Tilt Right -> Right Stick moves RIGHT (+1.0)
        val rollRightQ = createQuat(pitchRad = 0f, rollRad = rad35, yawRad = 0f)
        val resRight = processor.process(
            rawGyroX = 0f, rawGyroY = 0f, rawGyroZ = 0f,
            quatX = rollRightQ[0], quatY = rollRightQ[1], quatZ = rollRightQ[2], quatW = rollRightQ[3],
            settings = settings
        )
        assertEquals(1.0f, resRight.flightRsX, 0.01f, "Right Stick X must deflect to the right side on tilt right")
        assertEquals(0.0f, resRight.flightLsX, 0.01f, "Left stick must remain unaffected")

        // 2. Tilt Up -> Right Stick moves UP (+1.0)
        val pitchUpQ = createQuat(pitchRad = rad35, rollRad = 0f, yawRad = 0f)
        val resUp = processor.process(
            rawGyroX = 0f, rawGyroY = 0f, rawGyroZ = 0f,
            quatX = pitchUpQ[0], quatY = pitchUpQ[1], quatZ = pitchUpQ[2], quatW = pitchUpQ[3],
            settings = settings
        )
        assertEquals(1.0f, resUp.flightRsY, 0.01f, "Right Stick Y must deflect UP on tilt up")
        assertEquals(0.0f, resUp.flightLsY, 0.01f, "Left stick must remain unaffected")
    }

    @Test
    fun testInputModeSwitchResetsReferencePosture() {
        val processor = GyroProcessor()
        val velocitySettings = GyroSettings(enabled = true, inputMode = InputMode.VELOCITY)
        val flightSettings = GyroSettings(enabled = true, inputMode = InputMode.AIR_MOUSE)

        // Process a frame in VELOCITY mode with arbitrary tilt
        processor.process(
            rawGyroX = 0f, rawGyroY = 0f, rawGyroZ = 0f,
            quatX = 0.5f, quatY = 0.5f, quatZ = 0.5f, quatW = 0.5f,
            settings = velocitySettings
        )

        // Switching to AIR_MOUSE mode must automatically capture the current orientation as zero
        val resInitialFlight = processor.process(
            rawGyroX = 0f, rawGyroY = 0f, rawGyroZ = 0f,
            quatX = 0.2f, quatY = 0.2f, quatZ = 0.2f, quatW = 0.9f,
            settings = flightSettings
        )
        assertEquals(0.0f, resInitialFlight.flightLsX, 0.001f, "Mode switch must capture new baseline and return 0 stick")
        assertEquals(0.0f, resInitialFlight.flightLsY, 0.001f, "Mode switch must capture new baseline and return 0 stick")
    }

    @Test
    fun testSteadyPhoneOnTableFlatYawZeroBaseline() {
        val processor = GyroProcessor()
        val settings = GyroSettings(
            enabled = true,
            inputMode = InputMode.AIR_MOUSE,
            flightStick = FlightStickSettings(
                pitchTarget = FlightStickAxisTarget.NONE,
                rollTarget = FlightStickAxisTarget.NONE,
                yawTarget = FlightStickAxisTarget.RS_X,
                maxYawAngle = 35.0f,
                deadzone = 0.0f,
                curveExponent = 1.0f,
                yawInvert = false
            )
        )

        // Real landscape phone resting flat on table facing north: (0, 0, -0.7071, 0.7071)
        val k = 0.70710678f
        val flatTableQuat = floatArrayOf(0f, 0f, -k, k)

        // Frame 1: Calibrate baseline on flat surface
        val res1 = processor.process(
            rawGyroX = 0f, rawGyroY = 0f, rawGyroZ = 0f,
            quatX = flatTableQuat[0], quatY = flatTableQuat[1], quatZ = flatTableQuat[2], quatW = flatTableQuat[3],
            settings = settings
        )
        assertEquals(0.0f, res1.flightRsX, 0.001f, "First frame captures baseline and outputs 0")

        // Frame 2: Still resting steady on flat surface -> MUST BE EXACTLY 0.00000
        val resSteady = processor.process(
            rawGyroX = 0f, rawGyroY = 0f, rawGyroZ = 0f,
            quatX = flatTableQuat[0], quatY = flatTableQuat[1], quatZ = flatTableQuat[2], quatW = flatTableQuat[3],
            settings = settings
        )
        assertEquals(0.0f, resSteady.flightRsX, 0.0001f, "Steady phone on surface must have strictly 0.0 deflection on RS_X")
        assertEquals(0.0f, resSteady.flightLsX, 0.0001f, "Zero roll bleed")
        assertEquals(0.0f, resSteady.flightLsY, 0.0001f, "Zero pitch bleed")

        // Frame 3: Swivel phone right (clockwise) by 20 deg on table -> RS_X moves RIGHT (+1.0 direction)
        val tCw = Math.toRadians(-20.0).toFloat()
        val qRawZCw = kotlin.math.sin(tCw * 0.5f)
        val qRawWCw = kotlin.math.cos(tCw * 0.5f)
        val swivelRightQ = floatArrayOf(0f, 0f, k * (qRawZCw - qRawWCw), k * (qRawWCw + qRawZCw))
        val resRight = processor.process(
            rawGyroX = 0f, rawGyroY = 0f, rawGyroZ = 0f,
            quatX = swivelRightQ[0], quatY = swivelRightQ[1], quatZ = swivelRightQ[2], quatW = swivelRightQ[3],
            settings = settings
        )
        assertTrue(resRight.flightRsX > 0.4f, "Turning clockwise (right) on table must deflect Right Stick to the RIGHT side (> 0.4)")
        assertEquals(0.0f, resRight.flightLsX, 0.001f, "Zero roll bleed")
        assertEquals(0.0f, resRight.flightLsY, 0.001f, "Zero pitch bleed")

        // Frame 4: Swivel phone left (counter-clockwise) by 20 deg on table -> RS_X moves LEFT (-1.0 direction)
        val tCcw = Math.toRadians(20.0).toFloat()
        val qRawZCcw = kotlin.math.sin(tCcw * 0.5f)
        val qRawWCcw = kotlin.math.cos(tCcw * 0.5f)
        val swivelLeftQ = floatArrayOf(0f, 0f, k * (qRawZCcw - qRawWCcw), k * (qRawWCcw + qRawZCcw))
        val resLeft = processor.process(
            rawGyroX = 0f, rawGyroY = 0f, rawGyroZ = 0f,
            quatX = swivelLeftQ[0], quatY = swivelLeftQ[1], quatZ = swivelLeftQ[2], quatW = swivelLeftQ[3],
            settings = settings
        )
        assertTrue(resLeft.flightRsX < -0.4f, "Turning counter-clockwise (left) on table must deflect Right Stick to the LEFT side (< -0.4)")
        assertEquals(0.0f, resLeft.flightLsX, 0.001f, "Zero roll bleed")
        assertEquals(0.0f, resLeft.flightLsY, 0.001f, "Zero pitch bleed")
    }

    @Test
    fun testFlightAxisTargetChangeResetsZeroAttitude() {
        val processor = GyroProcessor()
        val settingsOff = GyroSettings(
            enabled = true,
            inputMode = InputMode.AIR_MOUSE,
            flightStick = FlightStickSettings(
                pitchTarget = FlightStickAxisTarget.NONE,
                rollTarget = FlightStickAxisTarget.NONE,
                yawTarget = FlightStickAxisTarget.NONE
            )
        )
        val k = 0.70710678f
        val quat1 = floatArrayOf(0f, 0f, -k, k)
        processor.process(0f, 0f, 0f, quatX = quat1[0], quatY = quat1[1], quatZ = quat1[2], quatW = quat1[3], settings = settingsOff)

        // User now enables Yaw target (maps to RS_X) at a new heading:
        val settingsOn = settingsOff.copy(
            flightStick = settingsOff.flightStick.copy(yawTarget = FlightStickAxisTarget.RS_X)
        )
        val quat2 = floatArrayOf(0f, 0f, 0f, 1f)
        val resInitial = processor.process(0f, 0f, 0f, quatX = quat2[0], quatY = quat2[1], quatZ = quat2[2], quatW = quat2[3], settings = settingsOn)
        assertEquals(0.0f, resInitial.flightRsX, 0.001f, "Enabling yaw target must re-anchor zero baseline and output 0")
    }
}
