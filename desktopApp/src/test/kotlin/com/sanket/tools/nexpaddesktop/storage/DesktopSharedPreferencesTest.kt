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
            gameDeadzoneCompensation = 0.18f,
            activationButtons = setOf("LT", "RT"),
            lowSpeedAmplifierEnabled = true,
            lowSpeedAmplifierAmount = 3.2f,
            lowSpeedAmplifierThreshold = 18.0f,
            // Mode-specific fields
            inputMode = InputMode.AIR_MOUSE,
            absoluteMaxTilt = 60.0f,
            absoluteSensitivityX = 1.45f,
            absoluteCurve = 1.8f,
            airMousePitchTarget = FlightStickAxisTarget.RS_Y,
            airMousePitchInvert = false,
            airMouseMaxPitch = 50.0f,
            airMouseRollTarget = FlightStickAxisTarget.LS_X,
            airMouseRollInvert = true,
            airMouseMaxRoll = 42.0f,
            airMouseYawTarget = FlightStickAxisTarget.NONE,
            airMouseDeadzone = 2.2f,
            airMouseCurve = 1.5f,
            // Combat Gestures fields
            gestureFlicksEnabled = true,
            gestureFlickUpAction = GestureButton.TRIGGER_LT,
            gestureFlickDownAction = GestureButton.BTN_RS,
            gestureFlickLeftAction = GestureButton.DPAD_LEFT,
            gestureFlickRightAction = GestureButton.DPAD_RIGHT,
            gestureFlickThreshold = 12.5f,
            gestureFlickCooldownMs = 280L,
            gestureTiltEnabled = true,
            gestureTiltPitchThreshold = 35.0f,
            gestureTiltRollThreshold = 25.0f,
            gestureTiltUpAction = GestureButton.BTN_A,
            gestureTiltDownAction = GestureButton.BTN_B,
            gestureTiltLeftAction = GestureButton.BTN_LB,
            gestureTiltRightAction = GestureButton.BTN_RB,
            gestureTiltHoldMode = true,
            gestureShakeEnabled = true,
            gestureShakeThreshold = 14.5f,
            gestureShakeAction = GestureButton.TRIGGER_RT,
            gestureShakeCooldownMs = 500L,
            gestureJoystickMode = GestureJoystickMode.FLIGHT_STICK,
            xboxTargetStick = XboxTargetStick.LEFT_STICK,
            xboxBlendMode = XboxBlendMode.ADDITIVE
        )

        // Save customized settings
        prefs.saveGyroSettings(customizedSettings)

        // Emulate App Restart: create brand new instance reading from disk
        val restartedPrefs = DesktopSharedPreferences.getCustomInstance(tempFile)
        val loadedSettings = restartedPrefs.loadGyroSettings()

        // Verify full fidelity restoration
        assertEquals(customizedSettings.enabled, loadedSettings.enabled)
        assertEquals(customizedSettings.invertX, loadedSettings.invertX)
        assertEquals(customizedSettings.invertRoll, loadedSettings.invertRoll)
        assertEquals(customizedSettings.invertY, loadedSettings.invertY)
        assertEquals(customizedSettings.horizontalAxis, loadedSettings.horizontalAxis)
        assertEquals(customizedSettings.deadzoneThreshold, loadedSettings.deadzoneThreshold, 0.001f)
        assertEquals(customizedSettings.smoothingAmount, loadedSettings.smoothingAmount, 0.001f)
        assertEquals(customizedSettings.adaptiveSmoothing, loadedSettings.adaptiveSmoothing)
        assertEquals(customizedSettings.accelerationEnabled, loadedSettings.accelerationEnabled)
        assertEquals(customizedSettings.maxSensitivity, loadedSettings.maxSensitivity, 0.001f)
        assertEquals(customizedSettings.stickResponseCurve, loadedSettings.stickResponseCurve)
        assertEquals(customizedSettings.gameDeadzoneCompensation, loadedSettings.gameDeadzoneCompensation, 0.001f)
        assertEquals(customizedSettings.activationButtons, loadedSettings.activationButtons)
        assertEquals(customizedSettings.lowSpeedAmplifierEnabled, loadedSettings.lowSpeedAmplifierEnabled)
        assertEquals(customizedSettings.lowSpeedAmplifierAmount, loadedSettings.lowSpeedAmplifierAmount, 0.001f)

        // Verify Flight Stick fields
        assertEquals(customizedSettings.inputMode, loadedSettings.inputMode)
        assertEquals(customizedSettings.airMousePitchTarget, loadedSettings.airMousePitchTarget)
        assertEquals(customizedSettings.airMousePitchInvert, loadedSettings.airMousePitchInvert)
        assertEquals(customizedSettings.airMouseMaxPitch, loadedSettings.airMouseMaxPitch, 0.001f)
        assertEquals(customizedSettings.airMouseRollTarget, loadedSettings.airMouseRollTarget)
        assertEquals(customizedSettings.airMouseRollInvert, loadedSettings.airMouseRollInvert)
        assertEquals(customizedSettings.airMouseMaxRoll, loadedSettings.airMouseMaxRoll, 0.001f)
        assertEquals(customizedSettings.airMouseYawTarget, loadedSettings.airMouseYawTarget)
        assertEquals(customizedSettings.airMouseDeadzone, loadedSettings.airMouseDeadzone, 0.001f)
        assertEquals(customizedSettings.airMouseCurve, loadedSettings.airMouseCurve, 0.001f)

        // Verify Combat Gesture fields
        assertEquals(customizedSettings.gestureFlicksEnabled, loadedSettings.gestureFlicksEnabled)
        assertEquals(customizedSettings.gestureFlickUpAction, loadedSettings.gestureFlickUpAction)
        assertEquals(customizedSettings.gestureFlickDownAction, loadedSettings.gestureFlickDownAction)
        assertEquals(customizedSettings.gestureFlickLeftAction, loadedSettings.gestureFlickLeftAction)
        assertEquals(customizedSettings.gestureFlickRightAction, loadedSettings.gestureFlickRightAction)
        assertEquals(customizedSettings.gestureFlickThreshold, loadedSettings.gestureFlickThreshold, 0.001f)
        assertEquals(customizedSettings.gestureFlickCooldownMs, loadedSettings.gestureFlickCooldownMs)
        assertEquals(customizedSettings.gestureTiltEnabled, loadedSettings.gestureTiltEnabled)
        assertEquals(customizedSettings.gestureTiltPitchThreshold, loadedSettings.gestureTiltPitchThreshold, 0.001f)
        assertEquals(customizedSettings.gestureTiltRollThreshold, loadedSettings.gestureTiltRollThreshold, 0.001f)
        assertEquals(customizedSettings.gestureTiltHoldMode, loadedSettings.gestureTiltHoldMode)
        assertEquals(customizedSettings.gestureShakeEnabled, loadedSettings.gestureShakeEnabled)
        assertEquals(customizedSettings.gestureShakeThreshold, loadedSettings.gestureShakeThreshold, 0.001f)
        assertEquals(customizedSettings.gestureShakeAction, loadedSettings.gestureShakeAction)
        assertEquals(customizedSettings.gestureJoystickMode, loadedSettings.gestureJoystickMode)
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
