package com.sanket.tools.nexpaddesktop.driver

import com.sanket.tools.nexpad.model.GamepadInput
import com.sanket.tools.nexpad.protocol.NexpadProtocol
import com.sanket.tools.nexpaddesktop.model.GyroSettings
import com.sanket.tools.nexpaddesktop.model.HorizontalAxis
import com.sanket.tools.nexpaddesktop.model.InputMode
import com.sanket.tools.nexpaddesktop.model.XboxBlendMode
import com.sanket.tools.nexpaddesktop.ui.ControllerType
import org.junit.Assert.*
import org.junit.Test
import kotlin.math.abs

class GyroComparisonAndBenchmarkTest {

    private val degToRad = (Math.PI / 180.0).toFloat()

    @Test
    fun benchmarkDs4VsXboxSmallInputResponse() {
        val defaultSettings = GyroSettings.DEFAULT.copy(precisionAiming = GyroSettings.DEFAULT.precisionAiming.copy(antiDeadzone = 0.14f)) // Benchmark with Anti-Deadzone = 0.14 and deadzone = 0.8°/s

        println("==========================================================================================================")
        println("  BENCHMARK 1: DS4 vs REFINED XBOX GYRO AT SPEEDS FROM 0.0°/s (REST) TO 100°/s (FLICK)")
        println("==========================================================================================================")
        println(String.format("%-10s | %-12s | %-18s | %-15s | %-18s | %-20s", 
            "Speed(°/s)", "Input(rad/s)", "DS4 Output(counts)", "Xbox Raw dps", "Xbox Stick Out", "In-Game Status"))
        println("----------------------------------------------------------------------------------------------------------")

        // 1. Warm up DS4 mapper calibration with small sensor noise so it completes calibration
        val warmupMapper = DualShock4InputMapper()
        for (i in 0..105) {
            warmupMapper.map(GamepadInput(gyroX = 0.0001f, gyroY = 0.0001f, gyroZ = 0.0001f))
        }

        val speedsDps = listOf(0.0f, 0.5f, 1.0f, 2.0f, 5.0f, 10.0f, 20.0f, 30.0f, 50.0f, 100.0f)

        for (speed in speedsDps) {
            val radPerSec = speed * degToRad
            val input = GamepadInput(
                gyroX = 0f, gyroY = 0f, gyroZ = radPerSec,
                sensorFlags = NexpadProtocol.SENSOR_MODE_IMU_6AXIS
            )

            // DS4 mapper output
            val ds4Report = warmupMapper.map(input)
            val ds4YawCounts = ds4Report.wGyroY.toInt()

            // Xbox via full InputPipeline
            val p = GyroProcessor()
            var lastOutput = input
            for (i in 0..10) {
                lastOutput = InputPipeline.processInput(
                    input = input,
                    activeController = ControllerType.XBOX_360,
                    gyroProcessor = p,
                    gyroSettings = defaultSettings,
                    lsSensitivityX = 1f, lsSensitivityY = 1f,
                    rsSensitivityX = 1f, rsSensitivityY = 1f,
                    onProcessedAngles = { _, _ -> }
                )
            }

            val xboxStick = lastOutput.rightStickX
            val status = when {
                speed == 0.0f && abs(xboxStick) < 0.001f -> "✅ PERFECT REST (0.0000 - No Drift)"
                abs(xboxStick) >= 0.14f -> "✅ ACTIVE (Penetrates In-Game Deadzone!)"
                abs(xboxStick) > 0f -> "⚡ Micro-ramp"
                else -> "⏹️ Still"
            }

            println(String.format("%-10.1f | %-12.4f | %-18d | %-15.2f | %-18.4f | %s",
                speed, radPerSec, ds4YawCounts, speed, xboxStick, status
            ))

            // Assertions:
            if (speed == 0.0f) {
                // Must be zero drift at rest
                assertEquals(0f, xboxStick, 0.0001f)
            } else if (speed >= 1.0f) {
                // Must penetrate in-game deadzone barrier (>= 0.14)
                assertTrue("Speed $speed°/s should break game deadzone (stick=$xboxStick)", abs(xboxStick) >= 0.14f)
            }
        }
        println("==========================================================================================================")
    }

    @Test
    fun benchmarkStepResponseLatency() {
        println("==========================================================================================================")
        println("  BENCHMARK 2: STEP RESPONSE & LATENCY (SUDDEN ROTATION FROM 0°/s TO 10°/s)")
        println("==========================================================================================================")

        val p = GyroProcessor()
        val settings = GyroSettings.DEFAULT
        val targetRad = 10f * degToRad

        println("Target Speed: 10.0 °/s. Measuring frame-by-frame convergence (60Hz / ~16.6ms per frame):")
        for (frame in 1..6) {
            val processed = p.process(
                rawGyroX = 0f, rawGyroY = 0f, rawGyroZ = targetRad,
                settings = settings
            )
            val convergencePercent = (processed.yawDps / 10.0f) * 100.0f
            println("  Frame $frame (~${frame * 16}ms): Output = ${String.format("%.2f", processed.yawDps)} °/s (${String.format("%.1f", convergencePercent)}% convergence)")
            if (frame == 2) {
                // By frame 2 (< 33ms), output should exceed 75% with our responsive alpha
                assertTrue("Latency check: frame 2 convergence should be >= 75%", convergencePercent >= 75.0f)
            }
        }
        println("==========================================================================================================")
    }

    @Test
    fun benchmarkJoystickSupremacyVsIdle() {
        println("==========================================================================================================")
        println("  BENCHMARK 3: PHYSICAL JOYSTICK SUPREMACY (MOBILE TOUCH STICK VS GYRO)")
        println("==========================================================================================================")

        val gyroProcessor = GyroProcessor()
        val settings = GyroSettings.DEFAULT.copy(precisionAiming = GyroSettings.DEFAULT.precisionAiming.copy(antiDeadzone = 0.14f)) // Default is JOYSTICK_SUPREME with Anti-Deadzone
        val gyroRad = 15f * degToRad // Phone has 15°/s gyro motion

        // Case 1: User pushes physical Right Stick to full right (+1.0) on phone screen
        val inputFullDeflection = GamepadInput(
            rightStickX = 1.0f, rightStickY = 0.0f,
            gyroZ = gyroRad
        )
        val resultFull = InputPipeline.processInput(
            input = inputFullDeflection,
            activeController = ControllerType.XBOX_360,
            gyroProcessor = gyroProcessor,
            gyroSettings = settings,
            lsSensitivityX = 1f, lsSensitivityY = 1f,
            rsSensitivityX = 1f, rsSensitivityY = 1f,
            onProcessedAngles = { _, _ -> }
        )
        println("CASE 1 (Physical Stick = +1.0, Gyro Motion Active):")
        println("  Physical Thumb Stick Input : 1.000")
        println("  Resulting RightStickX       : ${resultFull.rightStickX}")
        assertEquals("Physical stick must have complete supremacy!", 1.0f, resultFull.rightStickX, 0.001f)

        // Case 2: User pushes physical Right Stick partially (+0.5)
        val inputHalfDeflection = GamepadInput(
            rightStickX = 0.5f, rightStickY = 0.0f,
            gyroZ = gyroRad
        )
        val resultHalf = InputPipeline.processInput(
            input = inputHalfDeflection,
            activeController = ControllerType.XBOX_360,
            gyroProcessor = gyroProcessor,
            gyroSettings = settings,
            lsSensitivityX = 1f, lsSensitivityY = 1f,
            rsSensitivityX = 1f, rsSensitivityY = 1f,
            onProcessedAngles = { _, _ -> }
        )
        println("\nCASE 2 (Physical Stick = +0.5, Gyro Motion Active):")
        println("  Physical Thumb Stick Input : 0.500")
        println("  Resulting RightStickX       : ${resultHalf.rightStickX}")
        assertEquals("Physical stick partial deflection must be preserved!", 0.5f, resultHalf.rightStickX, 0.001f)

        // Case 3: User releases physical stick (Idle: rightStickX = 0.0f)
        val inputIdle = GamepadInput(
            rightStickX = 0.0f, rightStickY = 0.0f,
            gyroZ = gyroRad
        )
        val resultIdle = InputPipeline.processInput(
            input = inputIdle,
            activeController = ControllerType.XBOX_360,
            gyroProcessor = gyroProcessor,
            gyroSettings = settings,
            lsSensitivityX = 1f, lsSensitivityY = 1f,
            rsSensitivityX = 1f, rsSensitivityY = 1f,
            onProcessedAngles = { _, _ -> }
        )
        println("\nCASE 3 (Physical Stick IDLE = 0.0, Gyro Motion Active):")
        println("  Physical Thumb Stick Input : 0.000 (Idle/Released)")
        println("  Resulting RightStickX       : ${resultIdle.rightStickX} (Gyro seamlessly drives camera!)")
        assertTrue("When stick is idle, gyro should drive stick!", resultIdle.rightStickX >= 0.14f)

        println("==========================================================================================================")
    }

    @Test
    fun benchmarkRawSiliconGyroVsCalibratedGyro() {
        val calibratedSettings = GyroSettings.DEFAULT.copy(inputMode = InputMode.VELOCITY, precisionAiming = GyroSettings.DEFAULT.precisionAiming.copy(antiDeadzone = 0.14f))
        val rawSiliconSettings = GyroSettings.DEFAULT.copy(inputMode = InputMode.RAW_UNCALIBRATED, rawSilicon = GyroSettings.DEFAULT.rawSilicon.copy(antiDeadzone = 0.14f))

        println("==========================================================================================================")
        println("  BENCHMARK 4: RAW SILICON GYRO (UNCALIBRATED + BIAS) VS CALIBRATED GYRO (0.0°/s TO 100°/s)")
        println("==========================================================================================================")
        println(String.format("%-10s | %-14s | %-18s | %-16s | %-16s | %-12s | %-15s", 
            "Speed(°/s)", "Bias(rad/s)", "Raw Silic(+Bias)", "Calibrated Stick", "Raw Silic Stick", "Delta", "Status"))
        println("----------------------------------------------------------------------------------------------------------")

        val speedsDps = listOf(0.0f, 0.5f, 1.0f, 2.0f, 5.0f, 10.0f, 20.0f, 30.0f, 50.0f, 100.0f)
        val siliconDriftBias = 0.035f // Typical ~2.0 °/s MEMS silicon resting hardware drift

        for (speed in speedsDps) {
            val radPerSec = speed * degToRad

            // 1. Calibrated Gyro Input
            val calInput = GamepadInput(
                gyroX = 0f, gyroY = 0f, gyroZ = radPerSec,
                sensorFlags = NexpadProtocol.SENSOR_MODE_IMU_6AXIS
            )
            val calProcessor = GyroProcessor()
            var calResult = calInput
            for (i in 0..10) {
                calResult = InputPipeline.processInput(
                    input = calInput,
                    activeController = ControllerType.XBOX_360,
                    gyroProcessor = calProcessor,
                    gyroSettings = calibratedSettings,
                    lsSensitivityX = 1f, lsSensitivityY = 1f,
                    rsSensitivityX = 1f, rsSensitivityY = 1f,
                    onProcessedAngles = { _, _ -> }
                )
            }

            // 2. Raw Silicon Gyro Input (Raw Silicon has angular rate + drift bias)
            val rawSiliconInput = GamepadInput(
                rawGyroX = 0f, rawGyroY = 0f, rawGyroZ = (radPerSec + siliconDriftBias),
                gyroBiasX = 0f, gyroBiasY = 0f, gyroBiasZ = siliconDriftBias,
                sensorFlags = NexpadProtocol.SENSOR_MODE_RAW_UNCALIBRATED
            )
            val rawProcessor = GyroProcessor()
            var rawResult = rawSiliconInput
            for (i in 0..10) {
                rawResult = InputPipeline.processInput(
                    input = rawSiliconInput,
                    activeController = ControllerType.XBOX_360,
                    gyroProcessor = rawProcessor,
                    gyroSettings = rawSiliconSettings,
                    lsSensitivityX = 1f, lsSensitivityY = 1f,
                    rsSensitivityX = 1f, rsSensitivityY = 1f,
                    onProcessedAngles = { _, _ -> }
                )
            }

            val calStick = calResult.rightStickX
            val rawStick = rawResult.rightStickX
            val delta = abs(calStick - rawStick)

            val status = when {
                speed == 0.0f && abs(rawStick) < 0.0001f -> "✅ PERFECT REST (0.0000 - Bias Canceled)"
                rawStick >= 0.14f && delta < 0.005f -> "✅ IDENTICAL (Anti-Deadzone Active)"
                delta < 0.005f -> "✅ MATCHED"
                else -> "⚠️ DIVERGED"
            }

            println(String.format("%-10.1f | %-14.4f | %-18.4f | %-16.4f | %-16.4f | %-12.4f | %s",
                speed, siliconDriftBias, radPerSec + siliconDriftBias, calStick, rawStick, delta, status
            ))

            // Assertions:
            if (speed == 0.0f) {
                assertEquals("Raw Silicon Gyro must cancel drift bias at rest!", 0f, rawStick, 0.0001f)
            } else if (speed >= 1.0f) {
                assertTrue("Raw Silicon Gyro must break in-game deadzone (stick=$rawStick)", rawStick >= 0.14f)
                assertEquals("Raw Silicon output must match Calibrated output!", calStick, rawStick, 0.005f)
            }
        }
        println("==========================================================================================================")
    }

    @Test
    fun benchmarkYawAndRollSimultaneousBlending() {
        val processor = GyroProcessor()
        val mixSettings = GyroSettings.DEFAULT.copy(
            precisionAiming = GyroSettings.DEFAULT.precisionAiming.copy(
                horizontalAxis = HorizontalAxis.MIX,
                deadzoneThreshold = 0f,
                tighteningEnabled = false,
                smoothingEnabled = false,
                accelerationEnabled = false,
                maxDpsYaw = 200f
            )
        )

        println("==========================================================================================================")
        println("  BENCHMARK 5: SIMULTANEOUS YAW & ROLL BLENDING (USER SCENARIO: YAW=5°/s, ROLL=4°/s)")
        println("==========================================================================================================")
        println("Scenario: Player turns right with wrists while tilting phone naturally at 38.6° pitch angle.")
        println("Physical inputs: Yaw = +5.0°/s, Roll = +4.0°/s")
        println("----------------------------------------------------------------------------------------------------------")

        val yawDps = 5.0f
        val rollDps = 4.0f
        val naiveSum = yawDps + rollDps // 9.0°/s (The flaw user noticed)
        val naiveMax = maxOf(yawDps, rollDps) // 5.0°/s (Discontinuous loss)
        val trueVectorNorm = kotlin.math.sqrt(yawDps * yawDps + rollDps * rollDps) // 6.403°/s (True physical rotation)

        // Process through GyroProcessor:
        val yawRad = yawDps * degToRad
        val rollRad = rollDps * degToRad
        val result = processor.process(
            rawGyroX = 0f,
            rawGyroY = rollRad,
            rawGyroZ = yawRad,
            settings = mixSettings
        )

        val processedDps = result.rawYawDps

        println(String.format("1. Old Naive Sum (Yaw + Roll) : %6.2f °/s (⚠️  FLAW: Artificially doubles sensitivity by +40.5%%!)", naiveSum))
        println(String.format("2. Naive Max (max(Yaw, Roll)) : %6.2f °/s (⚠️  FLAW: Under-reports speed by -21.9%% & creates axis-flip stutter)", naiveMax))
        println(String.format("3. True 2D Vector Norm (√Y²+R²): %6.2f °/s (⭐ TRUE KINEMATICS: Preserves exact physical angular turning speed)", trueVectorNorm))
        println("----------------------------------------------------------------------------------------------------------")
        println(String.format("NEXPAD Engine Output           : %6.2f °/s (Status: %s)", 
            processedDps, 
            if (abs(processedDps - trueVectorNorm) < 0.05f) "✅ VERIFIED EXACT PHYSICAL ROTATION" else "❌ MISMATCH"
        ))
        println("==========================================================================================================")

        // Assert that the engine does NOT produce 9.0°/s (the bug)
        assertFalse("Engine must NEVER produce naive sum 9.0°/s!", abs(processedDps - naiveSum) < 0.1f)
        // Assert that the engine does NOT produce 5.0°/s (naive max)
        assertFalse("Engine must NEVER drop physical energy to naive max 5.0°/s!", abs(processedDps - naiveMax) < 0.1f)
        // Assert that the engine produces the true physical vector magnitude (~6.403°/s)
        assertEquals("Engine must produce exact 2D vector magnitude sqrt(5^2 + 4^2) = 6.403°/s", trueVectorNorm, processedDps, 0.05f)
    }

    @Test
    fun benchmarkFullSyntheticInputDs4VsXboxComparison() {
        println("============================================================================================================================")
        println("  BENCHMARK 6: FULL 0% TO 100% SYNTHETIC SPEED VARIATION COMPARISON (DS4 NATIVE VS XBOX JOYSTICK EMULATION)")
        println("============================================================================================================================")
        println("Evaluating transfer functions from resting micro-noise (0.1°/s) up to 100% saturation (200°/s & 300°/s):")
        println(String.format("%-8s | %-12s | %-14s | %-12s | %-14s | %-16s | %-18s",
            "Speed", "DS4 Counts", "DS4 Lin °/s", "Xbox Stick", "Game (Linear)", "Game (Quad x^2)", "Xbox Linearized"))
        println("----------------------------------------------------------------------------------------------------------------------------")

        val ds4Mapper = DualShock4InputMapper()
        // Warm up DS4 calibration
        for (i in 0..105) {
            ds4Mapper.map(GamepadInput(gyroX = 0.0001f, gyroY = 0.0001f, gyroZ = 0.0001f))
        }

        val baseSettings = GyroSettings.DEFAULT.copy(
            precisionAiming = GyroSettings.DEFAULT.precisionAiming.copy(
                maxDpsYaw = 200f,
                accelerationEnabled = false,
                deadzoneThreshold = 0.8f,
                antiDeadzone = 0.14f
            )
        )
        val linearizedSettings = baseSettings.copy(
            precisionAiming = baseSettings.precisionAiming.copy(
                stickResponseCurve = com.sanket.tools.nexpaddesktop.model.StickResponseCurve.LINEARIZED
            )
        )

        // Speed sweep: 0.0, 0.2, 0.5, 0.8 (threshold), 1.0, 2.0, 5.0, 10.0, 25.0, 50.0, 100.0, 150.0, 200.0, 250.0, 300.0 °/s
        val sweepSpeeds = listOf(
            0.0f, 0.2f, 0.5f, 0.8f, 1.0f, 2.0f, 5.0f, 10.0f, 25.0f, 50.0f, 75.0f, 100.0f, 150.0f, 200.0f, 300.0f
        )

        for (speed in sweepSpeeds) {
            val radZ = speed * degToRad
            val input = GamepadInput(
                gyroX = 0f, gyroY = 0f, gyroZ = radZ,
                sensorFlags = NexpadProtocol.SENSOR_MODE_IMU_6AXIS
            )

            // 1. DS4 Native Output
            val ds4Report = ds4Mapper.map(input)
            val ds4RawCounts = ds4Report.wGyroY.toInt()
            // DS4 counts to physical dps: counts / 16.384 or counts / (939 * radToDeg)
            val ds4LinearDps = abs(ds4RawCounts / (DualShock4InputMapper.GYRO_SCALAR * degToRad))

            // 2. Xbox Standard Linear Stick
            val pLinear = GyroProcessor()
            var xLinearOut = input
            for (i in 0..10) {
                xLinearOut = InputPipeline.processInput(
                    input = input,
                    activeController = ControllerType.XBOX_360,
                    gyroProcessor = pLinear,
                    gyroSettings = baseSettings,
                    lsSensitivityX = 1f, lsSensitivityY = 1f,
                    rsSensitivityX = 1f, rsSensitivityY = 1f,
                    onProcessedAngles = { _, _ -> }
                )
            }
            val stickDeflection = xLinearOut.rightStickX

            // 3. Simulated Game Camera Turn Rate:
            // Case A: Game has Linear Stick Curve (camera speed proportional to net stick)
            val gameDeadzone = 0.14f
            val netStick = (stickDeflection - gameDeadzone).coerceAtLeast(0f) / (1f - gameDeadzone)
            val gameLinearSpeed = netStick * 200f

            // Case B: Game has Quadratic Stick Curve (x^2)
            val gameQuadSpeed = (netStick * netStick) * 200f

            // 4. Xbox Linearized Response Curve (counteracts game quad curve)
            val pComp = GyroProcessor()
            var xCompOut = input
            for (i in 0..10) {
                xCompOut = InputPipeline.processInput(
                    input = input,
                    activeController = ControllerType.XBOX_360,
                    gyroProcessor = pComp,
                    gyroSettings = linearizedSettings,
                    lsSensitivityX = 1f, lsSensitivityY = 1f,
                    rsSensitivityX = 1f, rsSensitivityY = 1f,
                    onProcessedAngles = { _, _ -> }
                )
            }
            val compStick = xCompOut.rightStickX
            val compNetStick = (compStick - gameDeadzone).coerceAtLeast(0f) / (1f - gameDeadzone)
            // If the game squares this linearized stick:
            val gameLinearizedFinal = (compNetStick * compNetStick) * 200f

            println(String.format("%-8.1f | %-12d | %-14.2f | %-12.4f | %-14.2f | %-16.2f | %-18.2f",
                speed, ds4RawCounts, ds4LinearDps, stickDeflection, gameLinearSpeed, gameQuadSpeed, gameLinearizedFinal
            ))
        }
        println("============================================================================================================================")
    }
}

