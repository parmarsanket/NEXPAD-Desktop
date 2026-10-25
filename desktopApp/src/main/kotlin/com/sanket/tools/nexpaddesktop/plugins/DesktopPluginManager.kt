package com.sanket.tools.nexpaddesktop.plugins

import com.sanket.tools.nexpad.category.ControlKey
import com.sanket.tools.nexpad.nxprc.NxprcCategory
import com.sanket.tools.nexpad.nxprc.NxprcDocument
import com.sanket.tools.nexpaddesktop.connection.adb.AdbPathResolver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.TimeUnit

data class DesktopPluginItem(
    val id: String,
    val name: String,
    val category: String,
    val defaultControl: String,
    val author: String,
    val description: String,
    val jsonContent: String
)

object DesktopPluginManager {

    val PRESETS = listOf(
        DesktopPluginItem(
            id = "custom.scifi_hex_a",
            name = "Sci-Fi Hex Attack",
            category = NxprcCategory.BUTTON.id,
            defaultControl = ControlKey.A.key,
            author = "NEXPAD Core",
            description = "Cyan holographic hexagonal attack button with spring bounce feedback.",
            jsonContent = """
{
  "manifest": {
    "id": "custom.scifi_hex_a",
    "name": "Sci-Fi Hex Attack",
    "author": "NEXPAD Core",
    "version": "1.0.0",
    "category": "${NxprcCategory.BUTTON.id}",
    "defaultControl": "${ControlKey.A.key}",
    "description": "Cyan holographic hexagonal attack button with spring bounce feedback."
  },
  "geometry": {
    "type": "Polygon",
    "sides": 6,
    "cornerRadius": 10.0
  },
  "visual": {
    "fillColor": "#0A192F",
    "opacity": 0.90,
    "borderColor": "#00F0FF",
    "borderWidth": 2.5,
    "glowColor": "#00F0FF",
    "glowRadius": 12.0
  },
  "pressed": {
    "scale": 0.86,
    "rotation": 3.0,
    "fillColor": "#00F0FF",
    "borderColor": "#FFFFFF",
    "glowRadius": 20.0,
    "springDamping": 0.55,
    "springStiffness": 750.0
  },
  "label": {
    "text": "A",
    "color": "#00F0FF",
    "pressedColor": "#0A192F",
    "fontSize": 24.0
  },
  "size": {
    "widthDp": 76,
    "heightDp": 76
  }
}
            """.trimIndent()
        ),
        DesktopPluginItem(
            id = "custom.cyber_octa_b",
            name = "Cyber Octa Burst",
            category = NxprcCategory.BUTTON.id,
            defaultControl = ControlKey.B.key,
            author = "NEXPAD Core",
            description = "Neon crimson octagonal defense/burst button.",
            jsonContent = """
{
  "manifest": {
    "id": "custom.cyber_octa_b",
    "name": "Cyber Octa Burst",
    "author": "NEXPAD Core",
    "version": "1.0.0",
    "category": "${NxprcCategory.BUTTON.id}",
    "defaultControl": "${ControlKey.B.key}",
    "description": "Neon crimson octagonal defense/burst button."
  },
  "geometry": {
    "type": "Polygon",
    "sides": 8,
    "cornerRadius": 8.0
  },
  "visual": {
    "fillColor": "#1F0A12",
    "opacity": 0.90,
    "borderColor": "#FF0055",
    "borderWidth": 2.5,
    "glowColor": "#FF0055",
    "glowRadius": 12.0
  },
  "pressed": {
    "scale": 0.86,
    "rotation": -3.0,
    "fillColor": "#FF0055",
    "borderColor": "#FFFFFF",
    "glowRadius": 20.0,
    "springDamping": 0.55,
    "springStiffness": 750.0
  },
  "label": {
    "text": "B",
    "color": "#FF0055",
    "pressedColor": "#FFFFFF",
    "fontSize": 24.0
  },
  "size": {
    "widthDp": 76,
    "heightDp": 76
  }
}
            """.trimIndent()
        ),
        DesktopPluginItem(
            id = "custom.neon_matrix_ls",
            name = "Neon Matrix Analog Stick",
            category = NxprcCategory.JOYSTICK.id,
            defaultControl = ControlKey.LS.key,
            author = "NEXPAD Core",
            description = "Floating dual-ring cyber analog stick with dynamic deadzone indicators.",
            jsonContent = """
{
  "manifest": {
    "id": "custom.neon_matrix_ls",
    "name": "Neon Matrix Analog Stick",
    "author": "NEXPAD Core",
    "version": "1.0.0",
    "category": "${NxprcCategory.JOYSTICK.id}",
    "defaultControl": "${ControlKey.LS.key}",
    "description": "Floating dual-ring cyber analog stick with dynamic deadzone indicators."
  },
  "geometry": {
    "type": "Circle"
  },
  "visual": {
    "fillColor": "#0B132B",
    "opacity": 0.85,
    "borderColor": "#00F0FF",
    "borderWidth": 2.0
  },
  "pressed": {
    "fillColor": "#00F0FF"
  },
  "interaction": {
    "type": "Joystick",
    "deadzone": 0.05
  },
  "size": {
    "widthDp": 100,
    "heightDp": 100
  }
}
            """.trimIndent()
        ),
        DesktopPluginItem(
            id = "custom.mecha_trigger_rt",
            name = "Mecha Linear Trigger",
            category = NxprcCategory.TRIGGER.id,
            defaultControl = ControlKey.RT.key,
            author = "NEXPAD Core",
            description = "Cyber mecha high-precision linear trigger.",
            jsonContent = """
{
  "manifest": {
    "id": "custom.mecha_trigger_rt",
    "name": "Mecha Linear Trigger",
    "author": "NEXPAD Core",
    "version": "1.0.0",
    "category": "${NxprcCategory.TRIGGER.id}",
    "defaultControl": "${ControlKey.RT.key}",
    "description": "Cyber mecha high-precision linear trigger."
  },
  "geometry": {
    "type": "RoundedRect",
    "cornerRadius": 16.0
  },
  "visual": {
    "fillColor": "#121A0F",
    "opacity": 0.88,
    "borderColor": "#00FF66",
    "borderWidth": 2.5
  },
  "pressed": {
    "scale": 0.90,
    "fillColor": "#00FF66",
    "borderColor": "#FFFFFF"
  },
  "label": {
    "text": "RT",
    "color": "#00FF66",
    "pressedColor": "#000000",
    "fontSize": 20.0
  },
  "size": {
    "widthDp": 80,
    "heightDp": 120
  }
}
            """.trimIndent()
        )
    )

    fun getAvailablePlugins(): List<DesktopPluginItem> {
        val list = PRESETS.toMutableList()
        val customDir = File("components")
        if (customDir.exists()) {
            customDir.listFiles { f -> f.extension.lowercase() in listOf("json", "nxpcomponent") }?.forEach { f ->
                try {
                    val content = f.readText()
                    list.add(
                        DesktopPluginItem(
                            id = f.nameWithoutExtension,
                            name = f.nameWithoutExtension.replace('_', ' ').replaceFirstChar { it.uppercase() },
                            category = "CUSTOM",
                            defaultControl = "CUSTOM",
                            author = "Local User",
                            description = "Imported from Desktop components folder",
                            jsonContent = content
                        )
                    )
                } catch (_: Exception) {}
            }
        }
        return list
    }

    /**
     * Transfers a component to the mobile device using active USB Debugging (ADB).
     */
    private fun runAdbCommand(vararg args: String, timeoutSec: Long = 15): Boolean {
        return try {
            val process = ProcessBuilder(*args)
                .redirectOutput(ProcessBuilder.Redirect.DISCARD)
                .redirectError(ProcessBuilder.Redirect.DISCARD)
                .start()
            try { process.outputStream.close() } catch (_: Throwable) {}
            val finished = process.waitFor(timeoutSec, TimeUnit.SECONDS)
            if (!finished) {
                process.destroyForcibly()
                false
            } else {
                val ok = process.exitValue() == 0
                process.destroy()
                ok
            }
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Transfers a component to the mobile device using active USB Debugging (ADB).
     */
    suspend fun transferViaAdb(item: DesktopPluginItem): Result<String> = withContext(Dispatchers.IO) {
        val adbPath = AdbPathResolver.resolveAdbPath()
            ?: return@withContext Result.failure(IllegalStateException("ADB executable not found. Ensure USB debugging is enabled."))

        try {
            // Check connected ADB devices
            val devicesProcess = ProcessBuilder(adbPath, "devices").redirectErrorStream(true).start()
            try { devicesProcess.outputStream.close() } catch (_: Throwable) {}
            var output = ""
            val devicesFinished = try {
                output = devicesProcess.inputStream.bufferedReader().use { it.readText() }
                devicesProcess.waitFor(15, TimeUnit.SECONDS)
            } finally {
                if (!devicesProcess.isAlive) devicesProcess.destroy() else devicesProcess.destroyForcibly()
            }
            if (!devicesFinished) {
                return@withContext Result.failure(IllegalStateException("ADB devices check timed out."))
            }
            if (devicesProcess.exitValue() != 0) {
                return@withContext Result.failure(IllegalStateException("ADB devices check failed with exit code ${devicesProcess.exitValue()}."))
            }

            val lines = output.lines().filter { it.contains("device") && !it.startsWith("List") }
            if (lines.isEmpty()) {
                return@withContext Result.failure(IllegalStateException("No phone connected via USB Debugging. Check cable & authorization."))
            }

            val serial = lines.first().split("\\s+".toRegex()).firstOrNull() ?: ""

            // Create temp file on PC
            val tempFile = File.createTempFile("nxp_", ".json")
            tempFile.writeText(item.jsonContent)

            val remoteFileName = "${item.id.replace(Regex("[^a-zA-Z0-9_.-]"), "_")}.json"
            val tmpRemotePath = "/data/local/tmp/$remoteFileName"

            // 1. Push to /data/local/tmp/
            val pushProcess = ProcessBuilder(adbPath, "-s", serial, "push", tempFile.absolutePath, tmpRemotePath)
                .redirectOutput(ProcessBuilder.Redirect.DISCARD)
                .redirectError(ProcessBuilder.Redirect.DISCARD)
                .start()
            try { pushProcess.outputStream.close() } catch (_: Throwable) {}
            val pushFinished = try {
                pushProcess.waitFor(15, TimeUnit.SECONDS)
            } finally {
                if (!pushProcess.isAlive) pushProcess.destroy() else pushProcess.destroyForcibly()
            }
            if (!pushFinished) {
                tempFile.delete()
                return@withContext Result.failure(IllegalStateException("ADB push timed out."))
            }
            if (pushProcess.exitValue() != 0) {
                tempFile.delete()
                return@withContext Result.failure(IllegalStateException("ADB push failed with exit code ${pushProcess.exitValue()}."))
            }

            // 2. Copy into app's private filesDir using run-as
            val copyCmd = "run-as com.sanket.tools.nexpad cp $tmpRemotePath /data/data/com.sanket.tools.nexpad/files/nxp_components/$remoteFileName"
            val copyOk = runAdbCommand(adbPath, "-s", serial, "shell", copyCmd)

            // 3. Fallback: Also try pushing to external app directory
            val extPath = "/sdcard/Android/data/com.sanket.tools.nexpad/files/nxp_components/$remoteFileName"
            runAdbCommand(adbPath, "-s", serial, "shell", "mkdir -p /sdcard/Android/data/com.sanket.tools.nexpad/files/nxp_components")
            val extOk = runAdbCommand(adbPath, "-s", serial, "shell", "cp $tmpRemotePath $extPath")

            if (!copyOk && !extOk) {
                tempFile.delete()
                return@withContext Result.failure(IllegalStateException("Could not copy component to phone storage. Check phone authorization."))
            }

            // 4. Send broadcast to tell Android app to reload immediately
            runAdbCommand(adbPath, "-s", serial, "shell", "am broadcast -a com.sanket.tools.nexpad.RELOAD_COMPONENTS")

            // Cleanup temp file
            tempFile.delete()

            Result.success("Transferred ${item.name} via ADB to phone successfully! (Hot-reloaded)")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Transfers an NxprcDocument (.nxprc binary) to the mobile device using active USB Debugging (ADB).
     * Pushes to private filesDir (via run-as) with fallback to external app dir, and triggers instant reload.
     */
    suspend fun transferNxprcViaAdb(doc: NxprcDocument): Result<String> = withContext(Dispatchers.IO) {
        val adbPath = AdbPathResolver.resolveAdbPath()
            ?: return@withContext Result.failure(IllegalStateException("ADB executable not found. Ensure USB debugging is enabled."))

        try {
            // Check connected ADB devices
            val devicesProcess = ProcessBuilder(adbPath, "devices").redirectErrorStream(true).start()
            try { devicesProcess.outputStream.close() } catch (_: Throwable) {}
            var output = ""
            val devicesFinished = try {
                output = devicesProcess.inputStream.bufferedReader().use { it.readText() }
                devicesProcess.waitFor(15, TimeUnit.SECONDS)
            } finally {
                if (!devicesProcess.isAlive) devicesProcess.destroy() else devicesProcess.destroyForcibly()
            }
            if (!devicesFinished) {
                return@withContext Result.failure(IllegalStateException("ADB devices check timed out."))
            }
            if (devicesProcess.exitValue() != 0) {
                return@withContext Result.failure(IllegalStateException("ADB devices check failed with exit code ${devicesProcess.exitValue()}."))
            }

            val lines = output.lines().filter { it.contains("device") && !it.startsWith("List") }
            if (lines.isEmpty()) {
                return@withContext Result.failure(IllegalStateException("No phone connected via USB Debugging. Check cable & authorization."))
            }

            val serial = lines.first().split("\\s+".toRegex()).firstOrNull() ?: ""

            // Encode to binary bytes
            val binaryBytes = NxprcDocument.encodeToBytes(doc)
            val tempFile = File.createTempFile("nxprc_", ".nxprc")
            tempFile.writeBytes(binaryBytes)

            val remoteFileName = "${doc.manifest.id.replace(Regex("[^a-zA-Z0-9_.-]"), "_")}.nxprc"
            val tmpRemotePath = "/data/local/tmp/$remoteFileName"

            try {
                // 1. Push to /data/local/tmp/
                val pushProcess = ProcessBuilder(adbPath, "-s", serial, "push", tempFile.absolutePath, tmpRemotePath)
                    .redirectOutput(ProcessBuilder.Redirect.DISCARD)
                    .redirectError(ProcessBuilder.Redirect.DISCARD)
                    .start()
                try { pushProcess.outputStream.close() } catch (_: Throwable) {}
                val pushFinished = try {
                    pushProcess.waitFor(15, TimeUnit.SECONDS)
                } finally {
                    if (!pushProcess.isAlive) pushProcess.destroy() else pushProcess.destroyForcibly()
                }
                if (!pushFinished) {
                    return@withContext Result.failure(IllegalStateException("ADB push timed out."))
                }
                if (pushProcess.exitValue() != 0) {
                    return@withContext Result.failure(IllegalStateException("ADB push failed with exit code ${pushProcess.exitValue()}."))
                }

                // 2. Copy into app's private nxp_remote dir using run-as
                val copyCmd = "run-as com.sanket.tools.nexpad sh -c 'mkdir -p /data/data/com.sanket.tools.nexpad/files/nxp_remote && cp $tmpRemotePath /data/data/com.sanket.tools.nexpad/files/nxp_remote/$remoteFileName'"
                val copyOk = runAdbCommand(adbPath, "-s", serial, "shell", copyCmd)

                // 3. Fallback: Also try pushing to external app directory
                val extDir = "/sdcard/Android/data/com.sanket.tools.nexpad/files/nxp_remote"
                val extOk = runAdbCommand(adbPath, "-s", serial, "shell", "mkdir -p $extDir && cp $tmpRemotePath $extDir/$remoteFileName")

                if (!copyOk && !extOk) {
                    return@withContext Result.failure(IllegalStateException("Could not copy NXPRC to phone storage. Check phone authorization."))
                }

                // 4. Send targeted broadcasts to tell Android app to reload immediately
                runAdbCommand(adbPath, "-s", serial, "shell", "am broadcast -a com.sanket.tools.nexpad.RELOAD_REMOTE_COMPONENTS -p com.sanket.tools.nexpad")
                runAdbCommand(adbPath, "-s", serial, "shell", "am broadcast -a com.sanket.tools.nexpad.RELOAD_COMPONENTS -p com.sanket.tools.nexpad")

                Result.success("Pushed ${doc.manifest.name} (.nxprc) to phone via ADB! (Hot-reloaded)")
            } finally {
                // Ensure temp files on PC and remote tmp directory are always cleaned up
                try { runAdbCommand(adbPath, "-s", serial, "shell", "rm -f $tmpRemotePath") } catch (_: Throwable) {}
                try { tempFile.delete() } catch (_: Throwable) {}
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
