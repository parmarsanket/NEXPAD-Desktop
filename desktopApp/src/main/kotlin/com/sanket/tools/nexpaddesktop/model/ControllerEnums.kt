package com.sanket.tools.nexpaddesktop.model

import com.sanket.tools.nexpad.category.CategoryManager
import com.sanket.tools.nexpad.category.CategoryType
import com.sanket.tools.nexpad.category.ComponentType
import com.sanket.tools.nexpad.category.ControlKey
import com.sanket.tools.nexpad.model.GamepadInput
import com.sanket.tools.nexpad.model.NexpadKeys
import kotlinx.serialization.Serializable

/**
 * Navigation screen destinations in NEXPAD Desktop.
 */
enum class Screen(val title: String) {
    HOME("Home"),
    CONTROLLER("Controller"),
    PLUGINS("Plugin Studio"),
    NODE("Node Graph"),
    CONVERTER("Code Converter"),
    OUTPUT("Output & Telemetry"),
    KBM("Keyboard & Mouse"),
    DEBUG("Diagnostics & Logs")
}

/**
 * Controller emulation targets supported by NEXPAD Desktop driver layer.
 * Maps to Microsoft XInput (Xbox 360) and Sony DirectInput/HID (DualShock 4) via ViGEmBus.
 */
@Serializable
enum class ControllerType(
    val id: String,
    val displayName: String,
    val emulationTarget: String
) {
    XBOX_360("xbox", "Standard Gamepad (XInput)", "Microsoft XInput Gamepad (ViGEm)"),
    DUALSHOCK_4("ds4", "Motion Gamepad (DS4)", "Sony DualShock 4 HID Gamepad (ViGEm)");

    constructor(displayName: String) : this(
        id = if (displayName.contains("DS4", ignoreCase = true)) "ds4" else "xbox",
        displayName = displayName,
        emulationTarget = if (displayName.contains("DS4", ignoreCase = true)) "Sony DualShock 4 HID Gamepad (ViGEm)" else "Microsoft XInput Gamepad (ViGEm)"
    )

    val isXbox: Boolean get() = this == XBOX_360
    val isDualShock4: Boolean get() = this == DUALSHOCK_4

    companion object {
        val DEFAULT = XBOX_360

        fun fromId(id: String?): ControllerType {
            if (id.isNullOrBlank()) return DEFAULT
            val clean = id.trim().lowercase()
            return entries.firstOrNull { it.id == clean || it.name.lowercase() == clean } ?: DEFAULT
        }
    }
}

/**
 * Canonical identifiers for native built-in components, skins, and special actuators.
 * Fully synchronized with Android mobile definitions.
 */
object BuiltinComponentIds {
    const val BUILTIN_PREFIX = "builtin."
    const val DEFAULT_FAMILY = "builtin.default"
    const val INBUILD_LTP = "builtin.inbuild_ltp"
    const val INBUILD_RTP = "builtin.inbuild_rtp"
    const val INBUILD_PREFIX = "builtin.inbuild_"
    const val DEFAULT_LTP = "builtin.default_ltp"
    const val DEFAULT_RTP = "builtin.default_rtp"
    const val DEFAULT_LT = "builtin.default_lt"
    const val DEFAULT_RT = "builtin.default_rt"
    const val DEFAULT_LB = "builtin.default_lb"
    const val DEFAULT_RB = "builtin.default_rb"
    const val DEFAULT_PREFIX = "builtin.default_"
    const val GYRO = "GYRO"
    const val GYRO_BUTTON = "builtin.gyro_btn"
    const val SLIDER_LT = "builtin.slider_lt"
    const val SLIDER_RT = "builtin.slider_rt"
    const val SLIDER_PREFIX = "builtin.slider_"

    fun isInbuildTouchpad(customComponentId: String?): Boolean =
        customComponentId != null && customComponentId.startsWith(INBUILD_PREFIX)

    fun isSliderTrigger(customComponentId: String?): Boolean =
        customComponentId != null && (
            customComponentId == SLIDER_LT ||
            customComponentId == SLIDER_RT ||
            customComponentId.startsWith(SLIDER_PREFIX) ||
            customComponentId.contains("slider", ignoreCase = true)
        )
}

/**
 * Centralized classification and hit-testing helpers for controller touch surfaces,
 * sticks, touchpads, and buttons across Desktop Studio and Runtime engines.
 *
 * Eliminates ad-hoc string comparisons and per-frame collection allocations.
 */
object ControllerTouchClassifier {
    /** Controls that are analog sticks, touchpads, or stick clicks (excluded from normal button tap/sweep). */
    val EXCLUDED_NON_BUTTON_KEYS: Set<String> = setOf(
        ControlKey.LS.key,
        ControlKey.RS.key,
        ControlKey.LSB.key,
        ControlKey.RSB.key,
        ControlKey.LTP.key,
        ControlKey.RTP.key,
        "L3",
        "R3",
        "LSB",
        "RSB",
        NexpadKeys.L3,
        NexpadKeys.R3
    )

    /** Critical system and utility buttons protected from rolling multi-touch sweeps. */
    val NON_SWEEPABLE_KEYS: Set<String> = setOf(
        ControlKey.START.key,
        ControlKey.BACK.key,
        ControlKey.GUIDE.key,
        ControlKey.SHARE.key,
        NexpadKeys.SELECT,
        NexpadKeys.MENU,
        NexpadKeys.HOME,
        NexpadKeys.CAPTURE,
        BuiltinComponentIds.GYRO
    )

    /** Returns true if the key or component corresponds to a left or right touchpad. */
    fun isTouchpad(key: String?, customComponentId: String? = null): Boolean {
        if (!key.isNullOrBlank()) {
            val ctrl = ControlKey.fromIdentifier(key)
            if (ctrl == ControlKey.LTP || ctrl == ControlKey.RTP || ctrl?.componentType == ComponentType.TOUCHPAD) {
                return true
            }
            val upper = key.uppercase()
            if (upper == ControlKey.LTP.key || upper == ControlKey.RTP.key || upper.contains("TOUCHPAD")) {
                return true
            }
        }
        if (!customComponentId.isNullOrBlank()) {
            if (BuiltinComponentIds.isInbuildTouchpad(customComponentId) ||
                customComponentId.contains("touchpad", ignoreCase = true) ||
                customComponentId.contains("ltp", ignoreCase = true) ||
                customComponentId.contains("rtp", ignoreCase = true)
            ) {
                return true
            }
        }
        return false
    }

    /** Returns true if the key or component corresponds to a left or right analog stick or trackpad. */
    fun isJoystick(key: String?, customComponentId: String? = null): Boolean {
        if (!key.isNullOrBlank()) {
            val ctrl = ControlKey.fromIdentifier(key)
            if (ctrl == ControlKey.LS || ctrl == ControlKey.RS || ctrl?.componentType == ComponentType.JOYSTICK) {
                return true
            }
            val upper = key.uppercase()
            if (upper == ControlKey.LS.key || upper == ControlKey.RS.key || upper.contains("JOYSTICK") || upper.contains("STICK")) {
                return true
            }
        }
        if (!customComponentId.isNullOrBlank()) {
            if (customComponentId.contains("joystick", ignoreCase = true) ||
                customComponentId.contains("stick", ignoreCase = true)
            ) {
                return true
            }
        }
        return false
    }

    /** Returns true if the key represents a non-button element (stick or touchpad). */
    fun isNonButton(key: String): Boolean {
        val ctrl = ControlKey.fromIdentifier(key)
        if (ctrl == ControlKey.LS || ctrl == ControlKey.RS || ctrl == ControlKey.LSB || ctrl == ControlKey.RSB ||
            ctrl == ControlKey.LTP || ctrl == ControlKey.RTP ||
            ctrl?.componentType == ComponentType.JOYSTICK || ctrl?.componentType == ComponentType.TOUCHPAD) {
            return true
        }
        val upper = key.uppercase()
        return EXCLUDED_NON_BUTTON_KEYS.contains(upper) ||
                upper == "L3" || upper == "R3" || upper == "LSB" || upper == "RSB" ||
                upper.contains("TOUCHPAD") ||
                upper.contains("STICK")
    }

    /** Returns true if the key or customComponentId represents the Left Touchpad (LTP). */
    fun isLeftTouchpad(key: String?, customComponentId: String? = null): Boolean {
        if (key != null) {
            val ctrl = ControlKey.fromIdentifier(key)
            if (ctrl == ControlKey.LTP || key.equals(ControlKey.LTP.key, ignoreCase = true)) return true
        }
        if (customComponentId != null) {
            if (customComponentId == BuiltinComponentIds.INBUILD_LTP ||
                customComponentId == BuiltinComponentIds.DEFAULT_LTP ||
                customComponentId.contains("ltp", ignoreCase = true)
            ) return true
        }
        return false
    }

    /** Returns true if the key or customComponentId represents the Right Touchpad (RTP). */
    fun isRightTouchpad(key: String?, customComponentId: String? = null): Boolean {
        if (key != null) {
            val ctrl = ControlKey.fromIdentifier(key)
            if (ctrl == ControlKey.RTP || key.equals(ControlKey.RTP.key, ignoreCase = true)) return true
        }
        if (customComponentId != null) {
            if (customComponentId == BuiltinComponentIds.INBUILD_RTP ||
                customComponentId == BuiltinComponentIds.DEFAULT_RTP ||
                customComponentId.contains("rtp", ignoreCase = true)
            ) return true
        }
        return false
    }

    /** Returns true if the key or component represents the Right Stick (RS) or Right Touchpad (RTP). */
    fun isRightStickOrTouchpad(key: String?, customComponentId: String? = null): Boolean {
        if (key != null) {
            val ctrl = ControlKey.fromIdentifier(key)
            if (ctrl == ControlKey.RS || ctrl == ControlKey.RTP ||
                key.equals(ControlKey.RS.key, ignoreCase = true) ||
                key.equals(ControlKey.RTP.key, ignoreCase = true) ||
                key.equals("RS", ignoreCase = true) ||
                key.equals("R3", ignoreCase = true)
            ) return true
        }
        if (customComponentId != null && (
            customComponentId.contains("rtp", ignoreCase = true) ||
            customComponentId.contains("right_stick", ignoreCase = true)
        )) return true
        return false
    }

    /** Returns true if the key is non-sweepable (system/menu key). */
    fun isNonSweepable(key: String): Boolean =
        NON_SWEEPABLE_KEYS.contains(key.uppercase()) ||
        ControlKey.fromIdentifier(key)?.categoryType == CategoryType.SYSTEM

    /** Returns true if the customComponentId represents an analog slider trigger (e.g. SLIDER_LT / SLIDER_RT). */
    fun isSliderTrigger(customComponentId: String?): Boolean =
        BuiltinComponentIds.isSliderTrigger(customComponentId)
}

/**
 * Fast zero-allocation input evaluator for gamepad buttons, triggers, and sticks.
 * Used heavily on the high-frequency 120Hz/250Hz input pipeline.
 */
object GamepadControlClassifier {

    /**
     * Resolves a raw string identifier (e.g. "LT", "L2", "LEFT_TRIGGER", "CROSS")
     * into a canonical [ControlKey].
     */
    fun resolveControl(raw: String?): ControlKey? {
        if (raw.isNullOrBlank()) return null
        return CategoryManager.resolveControl(raw) ?: when (raw.trim().uppercase()) {
            "L2", "LEFT_TRIGGER" -> ControlKey.LT
            "R2", "RIGHT_TRIGGER" -> ControlKey.RT
            "L1", "LEFT_BUMPER" -> ControlKey.LB
            "R1", "RIGHT_BUMPER" -> ControlKey.RB
            "CROSS" -> ControlKey.A
            "CIRCLE" -> ControlKey.B
            "SQUARE" -> ControlKey.X
            "TRIANGLE" -> ControlKey.Y
            "LS", "L3" -> ControlKey.LSB
            "RS", "R3" -> ControlKey.RSB
            else -> ControlKey.fromIdentifier(raw)
        }
    }

    /**
     * Fast check whether a button or trigger designated by [key] is currently held down in [input].
     * Direct string branch to prevent unnecessary object allocation on the hot path.
     */
    fun isInputActive(key: String, input: GamepadInput): Boolean {
        // Fast paths for common upper-case canonical keys
        return when (key) {
            "LT", "L2", "LEFT_TRIGGER" -> input.triggerL2 > 0.1f
            "RT", "R2", "RIGHT_TRIGGER" -> input.triggerR2 > 0.1f
            "LB", "L1", "LEFT_BUMPER" -> input.btnL1
            "RB", "R1", "RIGHT_BUMPER" -> input.btnR1
            "A", "CROSS" -> input.btnA
            "B", "CIRCLE" -> input.btnB
            "X", "SQUARE" -> input.btnX
            "Y", "TRIANGLE" -> input.btnY
            "LS", "LSB", "L3" -> input.btnL3
            "RS", "RSB", "R3" -> input.btnR3
            "START" -> input.btnStart
            "BACK", "SELECT" -> input.btnSelect
            "GUIDE", "HOME" -> input.btnGuide
            "SHARE" -> input.btnShare
            "UP" -> input.dpadUp
            "DOWN" -> input.dpadDown
            "LEFT" -> input.dpadLeft
            "RIGHT" -> input.dpadRight
            else -> {
                // Fallback for non-canonical case or spacing
                val clean = key.trim().uppercase()
                when (clean) {
                    "LT", "L2", "LEFT_TRIGGER" -> input.triggerL2 > 0.1f
                    "RT", "R2", "RIGHT_TRIGGER" -> input.triggerR2 > 0.1f
                    "LB", "L1", "LEFT_BUMPER" -> input.btnL1
                    "RB", "R1", "RIGHT_BUMPER" -> input.btnR1
                    "A", "CROSS" -> input.btnA
                    "B", "CIRCLE" -> input.btnB
                    "X", "SQUARE" -> input.btnX
                    "Y", "TRIANGLE" -> input.btnY
                    "LS", "LSB", "L3" -> input.btnL3
                    "RS", "RSB", "R3" -> input.btnR3
                    "START" -> input.btnStart
                    "BACK", "SELECT" -> input.btnSelect
                    "GUIDE", "HOME" -> input.btnGuide
                    "SHARE" -> input.btnShare
                    "UP" -> input.dpadUp
                    "DOWN" -> input.dpadDown
                    "LEFT" -> input.dpadLeft
                    "RIGHT" -> input.dpadRight
                    else -> false
                }
            }
        }
    }

    /**
     * Checks if any of the buttons in [activationButtons] is active in [input].
     * Returns true immediately if [activationButtons] is empty (always active).
     */
    fun isActivationButtonPressed(activationButtons: Set<String>, input: GamepadInput): Boolean {
        if (activationButtons.isEmpty()) return true
        for (btn in activationButtons) {
            if (isInputActive(btn, input)) return true
        }
        return false
    }
}
