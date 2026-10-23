package com.sanket.tools.nexpaddesktop.ui.debug

import com.sanket.tools.nexpaddesktop.utils.LogLevel

/**
 * Filter categories for subsystem routing.
 */
enum class DebugFilter(val label: String, val icon: String) {
    ALL("All", "📋"),
    NETWORK("Network", "📡"),
    INPUT_DRIVER("Input & Driver", "🎮"),
    MOTION_DSU("Motion & DSU", "🎯"),
    HAPTICS("Haptics", "💥"),
    SYSTEM("System QoS", "⚙️"),
    WARNINGS_ERRORS("Issues", "⚠️")
}

/**
 * Log severity level filters.
 */
enum class LogLevelFilter(val label: String, val level: LogLevel?) {
    ALL("ALL", null),
    INFO("INFO", LogLevel.INFO),
    WARN("WARN", LogLevel.WARN),
    ERROR("ERR", LogLevel.ERROR),
    VERBOSE("VRB", LogLevel.VERBOSE)
}

/**
 * Layout arrangements for telemetry and terminal presentation.
 */
enum class DebugLayoutMode(val label: String, val icon: String) {
    STACKED("Stacked", "⬇"),
    SIDE_BY_SIDE("Side Layout", "⇄"),
    TERMINAL_ONLY("Logs Only", "📋")
}
