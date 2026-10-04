package com.sanket.tools.nexpaddesktop.connection.adb

import com.sanket.tools.nexpad.model.GamepadFeedback
import com.sanket.tools.nexpad.model.GamepadInput
import com.sanket.tools.nexpad.protocol.NexpadProtocol
import com.sanket.tools.nexpaddesktop.connection.usb.aoa.AoaFrameDecoder
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import java.io.IOException
import java.net.InetAddress
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import kotlinx.coroutines.asCoroutineDispatcher

/**
 * Manages ultra-low latency ADB Bridge connectivity using Unix Domain Sockets (localabstract)
 * via `adb reverse localabstract:nexpad_controller tcp:9999`.
 *
 * Key Optimizations:
 * 1. Zero driver swap / UAC prompt required (uses standard Android USB debugging).
 * 2. Reuses AoaFrameDecoder for streaming byte extraction with zero memory allocations.
 * 3. TCP_NODELAY = true with generous buffers to eliminate Nagle buffering and sliding window stalls.
 * 4. Dedicated high-priority dispatcher and instant inline RTT echo with zero thread hops.
 */
class AdbBridgeManager(
    private val tcpPort: Int = 9999
) {
    private val adbDispatcher = Executors.newFixedThreadPool(2) { runnable ->
        Thread(runnable, "AdbStreamThread").apply { isDaemon = true }
    }.asCoroutineDispatcher()
    companion object {
        private const val ABSTRACT_SOCKET_NAME = "nexpad_controller"
        private const val READ_BUFFER_SIZE = 4096
        private const val HEARTBEAT_FALLBACK_MS = 33L
        private const val STATS_WINDOW_PACKETS = 60
    }

    var onAdbConnected: ((deviceName: String) -> Unit)? = null
    var onAdbDisconnected: (() -> Unit)? = null
    var onInputReceived: ((GamepadInput) -> Unit)? = null

    private val isRunning = AtomicBoolean(false)
    private val isConnected = AtomicBoolean(false)

    private var serverSocket: ServerSocket? = null
    private var clientSocket: Socket? = null
    private var activeDeviceSerial: String? = null

    private var scannerJob: Job? = null
    private var connectionScope: CoroutineScope? = null

    private val feedbackChannel = Channel<GamepadFeedback>(Channel.CONFLATED)

    // Single Active Transport Guard: pause scanner when another transport is active
    var isScanningPaused: () -> Boolean = { false }

    val isAdbDevicePresent = AtomicBoolean(false)
    val isInitialScanCompleted = AtomicBoolean(false)

    fun hasActiveAdb(): Boolean = isConnected.get() || isAdbDevicePresent.get()

    fun startScanner(parentScope: CoroutineScope) {
        if (isRunning.getAndSet(true)) return

        scannerJob = parentScope.launch(Dispatchers.IO) {
            println("[ADB/Bridge] Scanner started. Resolving ADB executable...")
            val adbPath = AdbPathResolver.resolveAdbPath()
            if (adbPath == null) {
                println("[ADB/Bridge] ADB executable not available. Scanner standing by.")
                isInitialScanCompleted.set(true)
            } else {
                findReadyDevice(adbPath)
                isInitialScanCompleted.set(true)
            }

            while (isActive && isRunning.get()) {
                if (isScanningPaused()) {
                    delay(1500)
                    continue
                }
                if (!isConnected.get()) {
                    val resolvedPath = adbPath ?: AdbPathResolver.resolveAdbPath()
                    if (resolvedPath != null) {
                        val readyDevice = findReadyDevice(resolvedPath)
                        if (readyDevice != null) {
                            setupAndListen(resolvedPath, readyDevice)
                        }
                    }
                }
                delay(1500)
            }
        }
    }

    private data class AdbDeviceInfo(val serial: String, val displayName: String)

    private fun findReadyDevice(adbPath: String): AdbDeviceInfo? {
        return try {
            val process = ProcessBuilder(adbPath, "devices", "-l")
                .redirectErrorStream(true)
                .start()

            val lines = process.inputStream.bufferedReader().readLines()
            process.waitFor()

            var foundAnyDevice = false
            var readyDevice: AdbDeviceInfo? = null

            for (line in lines) {
                val trimmed = line.trim()
                if (trimmed.isEmpty() || trimmed.startsWith("List of devices")) continue

                val parts = trimmed.split("\\s+".toRegex())
                if (parts.size >= 2) {
                    foundAnyDevice = true
                    if (parts[1] == "device" && readyDevice == null) {
                        val serial = parts[0]
                        var modelName = "Android Device"
                        for (part in parts) {
                            if (part.startsWith("model:")) {
                                modelName = part.substringAfter("model:").replace('_', ' ')
                            }
                        }
                        readyDevice = AdbDeviceInfo(serial, modelName)
                    }
                }
            }

            isAdbDevicePresent.set(foundAnyDevice)
            readyDevice
        } catch (_: Exception) {
            isAdbDevicePresent.set(false)
            null
        }
    }

    private suspend fun setupAndListen(adbPath: String, device: AdbDeviceInfo) {
        println("[ADB/Bridge] Found ready device: ${device.displayName} [${device.serial}]")

        // 1. Setup reverse forwarding: adb -s <serial> reverse localabstract:nexpad_controller tcp:9999
        val reverseOk = executeAdbReverse(adbPath, device.serial)
        if (!reverseOk) {
            println("[ADB/Bridge] Failed to set up adb reverse for ${device.serial}")
            return
        }

        activeDeviceSerial = device.serial
        broadcastAdbDeviceReady(adbPath, device.serial)

        // 2. Open ServerSocket if not already listening
        try {
            if (serverSocket == null || serverSocket?.isClosed == true) {
                serverSocket = ServerSocket(tcpPort, 1, InetAddress.getByName("127.0.0.1")).apply {
                    soTimeout = 2000 // 2s timeout for accept so we don't block indefinitely
                    setPerformancePreferences(0, 2, 1) // Prioritize lowest latency
                }
                println("[ADB/Bridge] Listening on 127.0.0.1:$tcpPort for ADB client connection.")
            }
        } catch (e: Exception) {
            println("[ADB/Bridge] Failed to bind ServerSocket on port $tcpPort: ${e.message}")
            return
        }

        // 3. Accept connection with timeout
        val socket = try {
            broadcastAdbDeviceReady(adbPath, device.serial)
            serverSocket?.accept()
        } catch (_: java.net.SocketTimeoutException) {
            null
        } catch (e: Exception) {
            println("[ADB/Bridge] ServerSocket accept error: ${e.message}")
            null
        }

        if (socket != null) {
            handleClientSession(adbPath, device, socket)
        }
    }

    private fun getHostDisplayName(): String {
        return System.getenv("COMPUTERNAME")
            ?: try { InetAddress.getLocalHost().hostName } catch (_: Exception) { null }
            ?: "Windows PC"
    }

    private fun broadcastAdbDeviceReady(adbPath: String, serial: String) {
        try {
            val pcName = getHostDisplayName()
            ProcessBuilder(
                adbPath, "-s", serial,
                "shell", "am", "broadcast",
                "-a", "com.sanket.tools.nexpad.ADB_DEVICE_READY",
                "--es", "pc_name", pcName
            ).start()
        } catch (_: Exception) {}
    }

    private fun broadcastAdbDeviceDisconnected(adbPath: String?, serial: String?) {
        if (adbPath == null || serial == null) return
        try {
            ProcessBuilder(
                adbPath, "-s", serial,
                "shell", "am", "broadcast",
                "-a", "com.sanket.tools.nexpad.ADB_DEVICE_DISCONNECTED"
            ).start()
        } catch (_: Exception) {}
    }

    private fun executeAdbReverse(adbPath: String, serial: String): Boolean {
        return try {
            val process = ProcessBuilder(
                adbPath, "-s", serial,
                "reverse", "localabstract:$ABSTRACT_SOCKET_NAME", "tcp:$tcpPort"
            ).redirectErrorStream(true).start()

            val output = process.inputStream.bufferedReader().readText()
            val exitCode = process.waitFor()
            if (exitCode == 0) {
                println("[ADB/Bridge] adb reverse configured: localabstract:$ABSTRACT_SOCKET_NAME -> tcp:$tcpPort")
                true
            } else {
                println("[ADB/Bridge] adb reverse failed (exit $exitCode): $output")
                false
            }
        } catch (e: Exception) {
            println("[ADB/Bridge] Error executing adb reverse: ${e.message}")
            false
        }
    }

    private fun removeAdbReverse(adbPath: String?, serial: String?) {
        if (adbPath == null || serial == null) return
        try {
            ProcessBuilder(
                adbPath, "-s", serial,
                "reverse", "--remove", "localabstract:$ABSTRACT_SOCKET_NAME"
            ).start().waitFor()
            println("[ADB/Bridge] Removed adb reverse rule for $serial")
        } catch (_: Exception) {}
    }

    private suspend fun handleClientSession(adbPath: String, device: AdbDeviceInfo, socket: Socket) {
        clientSocket = socket
        isConnected.set(true)

        // Ultra-low latency socket configuration (generous buffers prevent TCP window exhaustion)
        try {
            socket.tcpNoDelay = true
            socket.setPerformancePreferences(0, 2, 1) // Prioritize lowest latency
            socket.sendBufferSize = 65536
            socket.receiveBufferSize = 65536
        } catch (_: Exception) {}

        println("[ADB/Bridge] Connected to ${device.displayName} via ADB tunnel!")
        onAdbConnected?.invoke(device.displayName)

        val latestLossPctByte = AtomicInteger(0)
        val outLock = Any()
        var currentFeedback = GamepadFeedback(0, 0)

        connectionScope = CoroutineScope(SupervisorJob() + adbDispatcher)

        val out = socket.getOutputStream()

        // Send initial connected frame with PC hostname so phone learns laptop name
        try {
            val pcName = getHostDisplayName()
            val pcBytes = pcName.toByteArray(Charsets.UTF_8)
            val safeLen = pcBytes.size.coerceAtMost(255)
            val hsBytes = ByteArray(2 + safeLen)
            hsBytes[0] = NexpadProtocol.PACKET_TYPE_CONNECTED
            hsBytes[1] = safeLen.toByte()
            System.arraycopy(pcBytes, 0, hsBytes, 2, safeLen)
            synchronized(outLock) {
                out.write(hsBytes)
                out.flush()
            }
        } catch (_: Exception) {}

        // Outbound Rumble / Heartbeat keepalive loop (dedicated coroutine on adbDispatcher)
        val outJob = connectionScope?.launch(adbDispatcher) {
            val heartbeatBytes = ByteArray(NexpadProtocol.FEEDBACK_PACKET_SIZE)
            try {
                while (isActive && isConnected.get()) {
                    val nextFeedback = withTimeoutOrNull(HEARTBEAT_FALLBACK_MS) {
                        feedbackChannel.receive()
                    }
                    if (nextFeedback != null) {
                        currentFeedback = nextFeedback
                    }

                    // Heartbeat or rumble update: send with echoSequenceNumber = 0 so phone ignores it for RTT
                    synchronized(outLock) {
                        NexpadProtocol.encodeFeedback(
                            feedback = currentFeedback,
                            echoSequenceNumber = 0,
                            packetLossByte = latestLossPctByte.get().toByte(),
                            out = heartbeatBytes,
                            offset = 0
                        )
                        out.write(heartbeatBytes)
                        out.flush()
                    }
                }
            } catch (_: Exception) {}
        }

        // Inbound Input Stream Loop
        try {
            val input = socket.getInputStream()
            val rawBuffer = ByteArray(READ_BUFFER_SIZE)
            val decoder = AoaFrameDecoder(NexpadProtocol.INPUT_PACKET_SIZE)
            val echoBytes = ByteArray(NexpadProtocol.FEEDBACK_PACKET_SIZE)
            var hasLoggedFirstPacket = false

            var lastSequenceNumber = Int.MIN_VALUE
            var lostInWindow = 0
            var receivedInWindow = 0
            var packetCount = 0

            while (isConnected.get()) {
                val bytesRead = input.read(rawBuffer)
                if (bytesRead < 0) break

                decoder.append(rawBuffer, 0, bytesRead) { packetBuffer, packetOffset ->
                    val gamepadInput = NexpadProtocol.decodeInput(packetBuffer, packetOffset) ?: return@append

                    if (!hasLoggedFirstPacket) {
                        println("[ADB/Bridge] First valid packet received over ADB! Streaming is active.")
                        hasLoggedFirstPacket = true
                    }

                    packetCount = (packetCount % STATS_WINDOW_PACKETS) + 1
                    val delta = gamepadInput.sequenceNumber - lastSequenceNumber
                    if (delta > 0 || lastSequenceNumber == Int.MIN_VALUE) {
                        if (lastSequenceNumber != Int.MIN_VALUE && delta > 1) {
                            lostInWindow += (delta - 1).coerceAtMost(255)
                        }
                        receivedInWindow++
                        lastSequenceNumber = gamepadInput.sequenceNumber

                        if (packetCount % STATS_WINDOW_PACKETS == 0) {
                            val total = lostInWindow + receivedInWindow
                            val currentLossPct = if (total > 0) (255 * lostInWindow / total).coerceIn(0, 255) else 0
                            latestLossPctByte.set(currentLossPct)
                            lostInWindow = 0
                            receivedInWindow = 0
                        }

                        // IMMEDIATE INLINE ECHO: Send echo back to phone instantly with zero thread hop or channel delay!
                        synchronized(outLock) {
                            NexpadProtocol.encodeFeedback(
                                feedback = currentFeedback,
                                echoSequenceNumber = gamepadInput.sequenceNumber,
                                packetLossByte = latestLossPctByte.get().toByte(),
                                out = echoBytes,
                                offset = 0
                            )
                            out.write(echoBytes)
                            out.flush()
                        }
                    }

                    onInputReceived?.invoke(gamepadInput)
                }
            }
        } catch (e: Exception) {
            println("[ADB/Bridge] Socket read terminated: ${e.message}")
        } finally {
            isConnected.set(false)
            outJob?.cancel()
            connectionScope?.cancel()
            try { socket.close() } catch (_: Exception) {}
            clientSocket = null

            println("[ADB/Bridge] Client disconnected.")
            broadcastAdbDeviceDisconnected(adbPath, activeDeviceSerial)
            removeAdbReverse(adbPath, activeDeviceSerial)
            activeDeviceSerial = null
            onAdbDisconnected?.invoke()
        }
    }

    suspend fun sendFeedback(feedback: GamepadFeedback) {
        if (isConnected.get()) {
            feedbackChannel.send(feedback)
        }
    }

    fun stop() {
        isRunning.set(false)
        isConnected.set(false)
        scannerJob?.cancel()
        connectionScope?.cancel()

        try { clientSocket?.close() } catch (_: Exception) {}
        try { serverSocket?.close() } catch (_: Exception) {}
        clientSocket = null
        serverSocket = null

        val adbPath = AdbPathResolver.resolveAdbPath()
        broadcastAdbDeviceDisconnected(adbPath, activeDeviceSerial)
        removeAdbReverse(adbPath, activeDeviceSerial)
        activeDeviceSerial = null
    }
}
