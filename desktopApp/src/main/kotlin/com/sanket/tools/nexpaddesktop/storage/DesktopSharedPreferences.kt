package com.sanket.tools.nexpaddesktop.storage

import com.sanket.tools.nexpaddesktop.model.GyroSettings
import com.sanket.tools.nexpaddesktop.ui.ControllerType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

/**
 * Android-like SharedPreferences implementation for NEXPAD Desktop.
 *
 * Persists all application settings, controller configurations, analog stick sensitivities,
 * and gyroscope/IMU motion calibration across application restarts.
 *
 * Stored in an atomic, safe JSON file under `%APPDATA%\NEXPAD\preferences.json` (Windows)
 * or `~/.nexpad/preferences.json` (macOS / Linux / fallback).
 */
class DesktopSharedPreferences private constructor(
    private val storageFile: File
) {

    private val lock = ReentrantReadWriteLock()
    private val memoryStore = ConcurrentHashMap<String, JsonElement>()
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
        isLenient = true
    }

    init {
        loadFromDisk()
    }

    /**
     * Reloads all values from disk into memory.
     */
    fun reload() {
        lock.write {
            loadFromDisk()
        }
    }

    private fun loadFromDisk() {
        if (!storageFile.exists() || storageFile.length() == 0L) {
            return
        }

        try {
            val content = storageFile.readText(Charsets.UTF_8)
            if (content.isBlank()) return

            val parsedObj = json.parseToJsonElement(content).jsonObject
            memoryStore.clear()
            for ((k, v) in parsedObj) {
                memoryStore[k] = v
            }
            println("⚙️ [Preferences] Loaded ${memoryStore.size} persistent preference entries from ${storageFile.absolutePath}")
        } catch (e: Exception) {
            println("⚠️ [Preferences] Failed to parse preferences from ${storageFile.absolutePath}: ${e.message}. Preserving defaults.")
        }
    }

    private fun persistToDiskInternal(): Boolean {
        return try {
            val parent = storageFile.parentFile
            if (parent != null && !parent.exists()) {
                parent.mkdirs()
            }

            val jsonObject = JsonObject(memoryStore)
            val jsonString = json.encodeToString(JsonObject.serializer(), jsonObject)

            // Atomic file write using temporary file to prevent corruption on crash
            val tempFile = File(parent ?: File("."), "${storageFile.name}.tmp")
            tempFile.writeText(jsonString, Charsets.UTF_8)

            try {
                Files.move(
                    tempFile.toPath(),
                    storageFile.toPath(),
                    StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.ATOMIC_MOVE
                )
            } catch (_: Exception) {
                // If atomic move is not supported on the target filesystem, use standard replace
                Files.move(
                    tempFile.toPath(),
                    storageFile.toPath(),
                    StandardCopyOption.REPLACE_EXISTING
                )
            }
            true
        } catch (e: Exception) {
            println("❌ [Preferences] Failed to persist preferences to disk: ${e.message}")
            false
        }
    }

    // ══════════════════════════════════════════════════════════
    //  CLASSIC ANDROID SHAREDPREFERENCES GETTERS
    // ══════════════════════════════════════════════════════════

    fun getString(key: String, defValue: String = ""): String = lock.read {
        val elem = memoryStore[key] ?: return defValue
        return if (elem is JsonPrimitive && elem.isString) elem.content else elem.toString().trim('"')
    }

    fun getInt(key: String, defValue: Int = 0): Int = lock.read {
        val elem = memoryStore[key] ?: return defValue
        return if (elem is JsonPrimitive) elem.intOrNull ?: defValue else defValue
    }

    fun getLong(key: String, defValue: Long = 0L): Long = lock.read {
        val elem = memoryStore[key] ?: return defValue
        return if (elem is JsonPrimitive) elem.longOrNull ?: defValue else defValue
    }

    fun getFloat(key: String, defValue: Float = 0f): Float = lock.read {
        val elem = memoryStore[key] ?: return defValue
        return if (elem is JsonPrimitive) elem.floatOrNull ?: defValue else defValue
    }

    fun getBoolean(key: String, defValue: Boolean = false): Boolean = lock.read {
        val elem = memoryStore[key] ?: return defValue
        return if (elem is JsonPrimitive) elem.booleanOrNull ?: defValue else defValue
    }

    fun getStringSet(key: String, defValue: Set<String> = emptySet()): Set<String> = lock.read {
        val elem = memoryStore[key] ?: return defValue
        if (elem is JsonArray) {
            return elem.mapNotNull { if (it is JsonPrimitive) it.content else null }.toSet()
        }
        return defValue
    }

    fun contains(key: String): Boolean = lock.read {
        return memoryStore.containsKey(key)
    }

    fun getAll(): Map<String, Any?> = lock.read {
        return memoryStore.toMap()
    }

    // ══════════════════════════════════════════════════════════
    //  DIRECT CONVENIENCE SETTERS
    // ══════════════════════════════════════════════════════════

    fun putString(key: String, value: String): DesktopSharedPreferences {
        edit().putString(key, value).commit()
        return this
    }

    fun putInt(key: String, value: Int): DesktopSharedPreferences {
        edit().putInt(key, value).commit()
        return this
    }

    fun putLong(key: String, value: Long): DesktopSharedPreferences {
        edit().putLong(key, value).commit()
        return this
    }

    fun putFloat(key: String, value: Float): DesktopSharedPreferences {
        edit().putFloat(key, value).commit()
        return this
    }

    fun putBoolean(key: String, value: Boolean): DesktopSharedPreferences {
        edit().putBoolean(key, value).commit()
        return this
    }

    fun putStringSet(key: String, values: Set<String>): DesktopSharedPreferences {
        edit().putStringSet(key, values).commit()
        return this
    }

    fun remove(key: String): DesktopSharedPreferences {
        edit().remove(key).commit()
        return this
    }

    fun clear(): DesktopSharedPreferences {
        edit().clear().commit()
        return this
    }

    // ══════════════════════════════════════════════════════════
    //  EDITOR PATTERN (MIRRORS ANDROID SHAREDPREFERENCES.EDITOR)
    // ══════════════════════════════════════════════════════════

    fun edit(): Editor = Editor()

    inner class Editor {
        private val modified = HashMap<String, JsonElement?>()
        private var clearAll = false

        fun putString(key: String, value: String): Editor {
            modified[key] = JsonPrimitive(value)
            return this
        }

        fun putInt(key: String, value: Int): Editor {
            modified[key] = JsonPrimitive(value)
            return this
        }

        fun putLong(key: String, value: Long): Editor {
            modified[key] = JsonPrimitive(value)
            return this
        }

        fun putFloat(key: String, value: Float): Editor {
            modified[key] = JsonPrimitive(value)
            return this
        }

        fun putBoolean(key: String, value: Boolean): Editor {
            modified[key] = JsonPrimitive(value)
            return this
        }

        fun putStringSet(key: String, values: Set<String>): Editor {
            modified[key] = JsonArray(values.map { JsonPrimitive(it) })
            return this
        }

        fun remove(key: String): Editor {
            modified[key] = null
            return this
        }

        fun clear(): Editor {
            clearAll = true
            return this
        }

        fun apply() {
            commitToMemory()
            scope.launch {
                lock.write {
                    persistToDiskInternal()
                }
            }
        }

        fun commit(): Boolean {
            commitToMemory()
            return lock.write {
                persistToDiskInternal()
            }
        }

        private fun commitToMemory() {
            lock.write {
                if (clearAll) {
                    memoryStore.clear()
                }
                for ((k, v) in modified) {
                    if (v == null) {
                        memoryStore.remove(k)
                    } else {
                        memoryStore[k] = v
                    }
                }
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    //  HIGH-LEVEL TYPED PERSISTENCE FOR NEXPAD
    // ══════════════════════════════════════════════════════════

    companion object {
        const val KEY_GYRO_SETTINGS = "nexpad_gyro_settings_v1"
        const val KEY_ACTIVE_CONTROLLER = "nexpad_active_controller"
        const val KEY_LS_SENS_X = "nexpad_ls_sensitivity_x"
        const val KEY_LS_SENS_Y = "nexpad_ls_sensitivity_y"
        const val KEY_RS_SENS_X = "nexpad_rs_sensitivity_x"
        const val KEY_RS_SENS_Y = "nexpad_rs_sensitivity_y"
        const val KEY_DSU_ENABLED = "nexpad_dsu_server_enabled"

        @Volatile
        private var instance: DesktopSharedPreferences? = null

        fun getInstance(): DesktopSharedPreferences {
            return instance ?: synchronized(this) {
                instance ?: run {
                    val appData = System.getenv("APPDATA")
                    val baseDir = if (!appData.isNullOrBlank()) {
                        File(appData, "NEXPAD")
                    } else {
                        File(System.getProperty("user.home") ?: ".", ".nexpad")
                    }
                    val prefsFile = File(baseDir, "preferences.json")
                    DesktopSharedPreferences(prefsFile).also { instance = it }
                }
            }
        }

        /**
         * Custom instance factory for testing with isolated temporary files.
         */
        fun getCustomInstance(file: File): DesktopSharedPreferences {
            return DesktopSharedPreferences(file)
        }
    }

    /**
     * Saves the entire GyroSettings model to disk.
     */
    fun saveGyroSettings(settings: GyroSettings) {
        try {
            val jsonStr = json.encodeToString(settings)
            putString(KEY_GYRO_SETTINGS, jsonStr)
        } catch (e: Exception) {
            println("❌ [Preferences] Failed to encode GyroSettings: ${e.message}")
        }
    }

    /**
     * Loads the saved GyroSettings model, or returns default settings if none saved.
     */
    fun loadGyroSettings(): GyroSettings {
        val jsonStr = getString(KEY_GYRO_SETTINGS, "")
        if (jsonStr.isNotBlank()) {
            try {
                return json.decodeFromString<GyroSettings>(jsonStr)
            } catch (e: Exception) {
                println("⚠️ [Preferences] Failed to decode saved GyroSettings: ${e.message}. Using factory defaults.")
            }
        }
        return GyroSettings()
    }

    /**
     * Saves the active virtual controller selection (Xbox 360 vs DualShock 4).
     */
    fun saveControllerType(controller: ControllerType) {
        putString(KEY_ACTIVE_CONTROLLER, controller.name)
    }

    /**
     * Loads the active virtual controller selection (defaults to Xbox 360).
     */
    fun loadControllerType(): ControllerType {
        val name = getString(KEY_ACTIVE_CONTROLLER, ControllerType.XBOX_360.name)
        return try {
            ControllerType.valueOf(name)
        } catch (_: Exception) {
            ControllerType.XBOX_360
        }
    }

    /**
     * Saves analog stick base sensitivities.
     */
    fun saveStickSensitivities(lsX: Float, lsY: Float, rsX: Float, rsY: Float) {
        edit()
            .putFloat(KEY_LS_SENS_X, lsX)
            .putFloat(KEY_LS_SENS_Y, lsY)
            .putFloat(KEY_RS_SENS_X, rsX)
            .putFloat(KEY_RS_SENS_Y, rsY)
            .commit()
    }

    /**
     * Data holder for stick sensitivities.
     */
    data class StickSensitivities(
        val lsX: Float,
        val lsY: Float,
        val rsX: Float,
        val rsY: Float
    )

    /**
     * Loads analog stick base sensitivities (defaults to 1.00×).
     */
    fun loadStickSensitivities(): StickSensitivities {
        return StickSensitivities(
            lsX = getFloat(KEY_LS_SENS_X, 1.0f),
            lsY = getFloat(KEY_LS_SENS_Y, 1.0f),
            rsX = getFloat(KEY_RS_SENS_X, 1.0f),
            rsY = getFloat(KEY_RS_SENS_Y, 1.0f)
        )
    }

    /**
     * Saves whether the DSU UDP motion server is enabled.
     */
    fun saveDsuEnabled(enabled: Boolean) {
        putBoolean(KEY_DSU_ENABLED, enabled)
    }

    /**
     * Loads whether the DSU UDP motion server is enabled (defaults to true).
     */
    fun loadDsuEnabled(): Boolean {
        return getBoolean(KEY_DSU_ENABLED, true)
    }
}
