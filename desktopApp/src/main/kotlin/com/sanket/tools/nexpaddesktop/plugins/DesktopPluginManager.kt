package com.sanket.tools.nexpaddesktop.plugins

import com.sanket.tools.nexpad.protocol.NexpadProtocol
import com.sanket.tools.nexpaddesktop.connection.adb.AdbPathResolver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.net.InetSocketAddress
import java.net.Socket

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
            category = "BUTTON",
            defaultControl = "A",
            author = "NEXPAD Core",
            description = "Cyan holographic hexagonal attack button with spring bounce feedback.",
            jsonContent = """
{
  "manifest": {
    "id": "custom.scifi_hex_a",
    "name": "Sci-Fi Hex Attack",
    "author": "NEXPAD Core",
    "version": "1.0.0",
    "category": "BUTTON",
    "defaultControl": "A",
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
            category = "BUTTON",
            defaultControl = "B",
            author = "NEXPAD Core",
            description = "Neon crimson octagonal defense/burst button.",
            jsonContent = """
{
  "manifest": {
    "id": "custom.cyber_octa_b",
    "name": "Cyber Octa Burst",
    "author": "NEXPAD Core",
    "version": "1.0.0",
    "category": "BUTTON",
    "defaultControl": "B",
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
            category = "JOYSTICK",
            defaultControl = "LS",
            author = "NEXPAD Core",
            description = "Floating dual-ring cyber analog stick with dynamic deadzone indicators.",
            jsonContent = """
{
  "manifest": {
    "id": "custom.neon_matrix_ls",
    "name": "Neon Matrix Analog Stick",
    "author": "NEXPAD Core",
    "version": "1.0.0",
    "category": "JOYSTICK",
    "defaultControl": "LS",
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
            category = "TRIGGER",
            defaultControl = "RT",
            author = "NEXPAD Core",
            description = "Cyber mecha high-precision linear trigger.",
            jsonContent = """
{
  "manifest": {
    "id": "custom.mecha_trigger_rt",
    "name": "Mecha Linear Trigger",
    "author": "NEXPAD Core",
    "version": "1.0.0",
    "category": "TRIGGER",
    "defaultControl": "RT",
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
    suspend fun transferViaAdb(item: DesktopPluginItem): Result<String> = withContext(Dispatchers.IO) {
        val adbPath = AdbPathResolver.resolveAdbPath()
            ?: return@withContext Result.failure(IllegalStateException("ADB executable not found. Ensure USB debugging is enabled."))

        try {
            // Check connected ADB devices
            val devicesProcess = ProcessBuilder(adbPath, "devices").redirectErrorStream(true).start()
            val output = devicesProcess.inputStream.bufferedReader().readText()
            devicesProcess.waitFor()

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
                .redirectErrorStream(true).start()
            val pushOut = pushProcess.inputStream.bufferedReader().readText()
            pushProcess.waitFor()

            // 2. Copy into app's private filesDir using run-as
            val copyCmd = "run-as com.sanket.tools.nexpad cp $tmpRemotePath /data/data/com.sanket.tools.nexpad/files/nxp_components/$remoteFileName"
            ProcessBuilder(adbPath, "-s", serial, "shell", copyCmd).redirectErrorStream(true).start().waitFor()

            // 3. Fallback: Also try pushing to external app directory
            val extPath = "/sdcard/Android/data/com.sanket.tools.nexpad/files/nxp_components/$remoteFileName"
            ProcessBuilder(adbPath, "-s", serial, "shell", "mkdir -p /sdcard/Android/data/com.sanket.tools.nexpad/files/nxp_components").start().waitFor()
            ProcessBuilder(adbPath, "-s", serial, "shell", "cp $tmpRemotePath $extPath").start().waitFor()

            // 4. Send broadcast to tell Android app to reload immediately
            ProcessBuilder(adbPath, "-s", serial, "shell", "am broadcast -a com.sanket.tools.nexpad.RELOAD_COMPONENTS").start().waitFor()

            // Cleanup temp file
            tempFile.delete()

            Result.success("Transferred ${item.name} via ADB to phone successfully! (Hot-reloaded)")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Transfers a component using NexpadProtocol FTP packets over a TCP socket (Wi-Fi or forwarded ADB port).
     */
    suspend fun transferViaFtp(
        item: DesktopPluginItem,
        host: String = "127.0.0.1",
        port: Int = NexpadProtocol.FTP_PORT
    ): Result<String> = withContext(Dispatchers.IO) {
        val socket = Socket()
        try {
            socket.connect(InetSocketAddress(host, port), 3000)
            val out = socket.getOutputStream()
            val input = socket.getInputStream()

            val fileName = "${item.id}.json"
            val fileBytes = item.jsonContent.encodeToByteArray()

            // 1. Send FTP Start Packet
            val startPacket = NexpadProtocol.encodeFtpStart(fileName, fileBytes.size)
            out.write(startPacket)
            out.flush()

            // Read ACK
            val ackBuf = ByteArray(32)
            val ackLen = input.read(ackBuf)
            if (ackLen < 0) return@withContext Result.failure(IllegalStateException("Connection closed before ACK"))

            // 2. Send Chunks
            var offset = 0
            var chunkIndex = 0
            while (offset < fileBytes.size) {
                val chunkSize = minOf(NexpadProtocol.FTP_DEFAULT_CHUNK_SIZE, fileBytes.size - offset)
                val chunkPacket = NexpadProtocol.encodeFtpChunk(chunkIndex, fileBytes, offset, chunkSize)
                out.write(chunkPacket)
                out.flush()

                // Read chunk ACK
                input.read(ackBuf)

                offset += chunkSize
                chunkIndex++
            }

            // 3. Send Complete Packet
            val completePacket = NexpadProtocol.encodeFtpComplete(fileName)
            out.write(completePacket)
            out.flush()

            socket.close()
            Result.success("Transferred ${item.name} (${fileBytes.size} bytes) via FTP to $host:$port!")
        } catch (e: Exception) {
            socket.close()
            Result.failure(e)
        }
    }
}
