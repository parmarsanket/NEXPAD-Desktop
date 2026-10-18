package com.sanket.tools.nexpaddesktop.storage

import com.sanket.tools.nexpaddesktop.model.*
import com.sanket.tools.nexpaddesktop.ui.ControllerType
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.File
import kotlin.test.*

class DesktopSharedPreferencesTest {

    private lateinit var tempFile: File
    private lateinit var prefs: DesktopSharedPreferences

    @Before
    fun setUp() {
        tempFile = File.createTempFile("nexpad_test_prefs", ".json")
        prefs = DesktopSharedPreferences.getCustomInstance(tempFile)
    }

    @After
    fun tearDown() {
        if (tempFile.exists()) {
            tempFile.delete()
        }
        val tmpAux = File(tempFile.parentFile, "${tempFile.name}.tmp")
        if (tmpAux.exists()) {
            tmpAux.delete()
        }
    }

    @Test
    fun testBasicPrimitiveTypesPersistence() {
        prefs.edit()
            .putString("username", "NexusGamer")
            .putInt("poll_rate", 1000)
            .putLong("timestamp", 1728639200000L)
            .putFloat("deadzone", 0.12f)
            .putBoolean("haptics_enabled", true)
            .putStringSet("active_binds", setOf("BTN_X", "BTN_Y", "TRIGGER_RT"))
            .commit()

        // Verify in-memory readout
        assertEquals("NexusGamer", prefs.getString("username"))
        assertEquals(1000, prefs.getInt("poll_rate"))
        assertEquals(1728639200000L, prefs.getLong("timestamp"))
        assertEquals(0.12f, prefs.getFloat("deadzone"), 0.0001f)
        assertTrue(prefs.getBoolean("haptics_enabled"))
        assertEquals(setOf("BTN_X", "BTN_Y", "TRIGGER_RT"), prefs.getStringSet("active_binds"))

        // Emulate App Restart: create brand new instance reading from the exact same file
        val restartedPrefs = DesktopSharedPreferences.getCustomInstance(tempFile)
        assertEquals("NexusGamer", restartedPrefs.getString("username"))
        assertEquals(1000, restartedPrefs.getInt("poll_rate"))
        assertEquals(1728639200000L, restartedPrefs.getLong("timestamp"))
        assertEquals(0.12f, restartedPrefs.getFloat("deadzone"), 0.0001f)
        assertTrue(restartedPrefs.getBoolean("haptics_enabled"))
        assertEquals(setOf("BTN_X", "BTN_Y", "TRIGGER_RT"), restartedPrefs.getStringSet("active_binds"))
    }

    @Test
    fun testGyroSettingsFullPersistenceAcrossAppRestart() {
        // Build customized GyroSettings with distinct non-default values across all modes
        val customizedSettings = GyroSettings(
            enabled = true,
            inputMode = InputMode.AIR_MOUSE,
            precisionAiming = PrecisionAimingSettings(
                invertX = true,
                invertRoll = true,
                invertY = false,
                horizontalAxis = HorizontalAxis.YAW,
                deadzoneThreshold = 1.8f,
                smoothingEnabled = true,
                smoothingAmount = 0.92f,
                adaptiveSmoothing = true,
                smoothingThreshold = 45.0f,
                accelerationEnabled = true,
                maxSensitivity = 2.8f,
                maxThreshold = 220.0f,
                stickResponseCurve = StickResponseCurve.LINEARIZED,
                antiDeadzone = 0.18f,
                activationButtons = setOf("LT", "RT"),
                lowSpeedAmplifierEnabled = true,
                lowSpeedAmplifierAmount = 3.2f,
                lowSpeedAmplifierThreshold = 18.0f
            ),
            steeringWheel = SteeringWheelSettings(
                maxTiltAngle = 60.0f,
                sensitivityX = 1.45f,
                responseCurve = 1.8f,
                targetStick = XboxTargetStick.LEFT_STICK,
                blendMode = XboxBlendMode.ADDITIVE
            ),
            flightStick = FlightStickSettings(
                pitchTarget = FlightStickAxisTarget.RS_Y,
                pitchInvert = false,
                maxPitchAngle = 50.0f,
                rollTarget = FlightStickAxisTarget.LS_X,
                rollInvert = true,
                maxRollAngle = 42.0f,
                yawTarget = FlightStickAxisTarget.NONE,
                deadzone = 2.2f,
                curveExponent = 1.5f
            ),
            combatGestures = CombatGesturesSettings(
                flicksEnabled = true,
                flickUpAction = GestureButton.TRIGGER_LT,
                flickDownAction = GestureButton.BTN_RS,
                flickLeftAction = GestureButton.DPAD_LEFT,
                flickRightAction = GestureButton.DPAD_RIGHT,
                flickThreshold = 12.5f,
                flickCooldownMs = 280L,
                tiltEnabled = true,
                tiltPitchThreshold = 35.0f,
                tiltRollThreshold = 25.0f,
                tiltUpAction = GestureButton.BTN_A,
                tiltDownAction = GestureButton.BTN_B,
                tiltLeftAction = GestureButton.BTN_LB,
                tiltRightAction = GestureButton.BTN_RB,
                tiltHoldMode = true,
                shakeEnabled = true,
                shakeThreshold = 14.5f,
                shakeAction = GestureButton.TRIGGER_RT,
                shakeCooldownMs = 500L
            ),
            rawSilicon = RawSiliconSettings(
                deadzoneThreshold = 0.5f
            )
        )

        // Save customized settings
        prefs.saveGyroSettings(customizedSettings)

        // Emulate App Restart: create brand new instance reading from disk
        val restartedPrefs = DesktopSharedPreferences.getCustomInstance(tempFile)
        val loadedSettings = restartedPrefs.loadGyroSettings()

        // Verify full fidelity restoration across all 5 isolated domain models
        assertEquals(customizedSettings.enabled, loadedSettings.enabled)
        assertEquals(customizedSettings.inputMode, loadedSettings.inputMode)
        assertEquals(customizedSettings.precisionAiming, loadedSettings.precisionAiming)
        assertEquals(customizedSettings.steeringWheel, loadedSettings.steeringWheel)
        assertEquals(customizedSettings.flightStick, loadedSettings.flightStick)
        assertEquals(customizedSettings.combatGestures, loadedSettings.combatGestures)
        assertEquals(customizedSettings.rawSilicon, loadedSettings.rawSilicon)

        // Verify backward-compatible delegation
        assertEquals(customizedSettings.invertX, loadedSettings.invertX)
        assertEquals(customizedSettings.invertRoll, loadedSettings.invertRoll)
        assertEquals(customizedSettings.invertY, loadedSettings.invertY)
        assertEquals(customizedSettings.horizontalAxis, loadedSettings.horizontalAxis)
        assertEquals(customizedSettings.deadzoneThreshold, loadedSettings.deadzoneThreshold, 0.001f)
        assertEquals(customizedSettings.airMousePitchTarget, loadedSettings.airMousePitchTarget)
        assertEquals(customizedSettings.gestureFlicksEnabled, loadedSettings.gestureFlicksEnabled)
        assertEquals(customizedSettings.gestureFlickCooldownMs, loadedSettings.gestureFlickCooldownMs)
        assertEquals(customizedSettings.gestureTiltEnabled, loadedSettings.gestureTiltEnabled)
        assertEquals(customizedSettings.gestureTiltPitchThreshold, loadedSettings.gestureTiltPitchThreshold, 0.001f)
        assertEquals(customizedSettings.gestureTiltRollThreshold, loadedSettings.gestureTiltRollThreshold, 0.001f)
        assertEquals(customizedSettings.gestureTiltHoldMode, loadedSettings.gestureTiltHoldMode)
        assertEquals(customizedSettings.gestureShakeEnabled, loadedSettings.gestureShakeEnabled)
        assertEquals(customizedSettings.gestureShakeThreshold, loadedSettings.gestureShakeThreshold, 0.001f)
        assertEquals(customizedSettings.gestureShakeAction, loadedSettings.gestureShakeAction)
        assertEquals(customizedSettings.xboxTargetStick, loadedSettings.xboxTargetStick)
        assertEquals(customizedSettings.xboxBlendMode, loadedSettings.xboxBlendMode)
    }

    @Test
    fun testControllerSelectionAndStickSensitivitiesPersistence() {
        prefs.saveControllerType(ControllerType.DUALSHOCK_4)
        prefs.saveStickSensitivities(lsX = 1.25f, lsY = 1.30f, rsX = 0.85f, rsY = 0.90f)

        // Emulate App Restart
        val restartedPrefs = DesktopSharedPreferences.getCustomInstance(tempFile)
        assertEquals(ControllerType.DUALSHOCK_4, restartedPrefs.loadControllerType())

        val sens = restartedPrefs.loadStickSensitivities()
        assertEquals(1.25f, sens.lsX, 0.001f)
        assertEquals(1.30f, sens.lsY, 0.001f)
        assertEquals(0.85f, sens.rsX, 0.001f)
        assertEquals(0.90f, sens.rsY, 0.001f)
    }

    @Test
    fun testCorruptFileFallbackToDefaultsWithoutCrash() {
        // Write corrupt non-JSON content
        tempFile.writeText("{ corrupted_json_syntax: true, unclosed }", Charsets.UTF_8)

        val safePrefs = DesktopSharedPreferences.getCustomInstance(tempFile)
        // Should not crash and should return safe default values
        assertEquals("default_val", safePrefs.getString("unknown_key", "default_val"))
        assertEquals(ControllerType.XBOX_360, safePrefs.loadControllerType())
        val defaultGyro = safePrefs.loadGyroSettings()
        assertEquals(GyroSettings(), defaultGyro)
    }

    @Test
    fun testEditorRemovalAndClear() {
        prefs.edit()
            .putString("key1", "val1")
            .putString("key2", "val2")
            .commit()

        assertTrue(prefs.contains("key1"))
        assertTrue(prefs.contains("key2"))

        prefs.edit().remove("key1").commit()
        assertFalse(prefs.contains("key1"))
        assertTrue(prefs.contains("key2"))

        prefs.edit().clear().commit()
        assertFalse(prefs.contains("key2"))
        assertTrue(prefs.getAll().isEmpty())
    }
}
