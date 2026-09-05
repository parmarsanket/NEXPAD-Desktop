package com.sanket.tools.nexpaddesktop.connection.bt

import com.sanket.tools.nexpad.model.GamepadFeedback
import com.sanket.tools.nexpad.model.GamepadInput
import com.sanket.tools.nexpad.protocol.NexpadProtocol
import com.sanket.tools.nexpaddesktop.connection.usb.aoa.AoaFrameDecoder
import com.sun.jna.Memory
import com.sun.jna.Pointer
import com.sun.jna.platform.win32.Guid.GUID
import com.sun.jna.ptr.IntByReference
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.selects.select
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

/**
 * Ultra-low latency Bluetooth Classic RFCOMM Server for Windows Desktop.
 *
 * Uses native Winsock2 (ws2_32.dll) via JNA to bind an AF_BTH streaming socket,
 * registers the NEXPAD SDP Service Record, and handles full-duplex 200 Hz
 * gamepad streaming directly with zero external drivers or elevation.
 */
class BluetoothRfcommServer {

    companion object {
        const val NEXPAD_BT_UUID_STRING = "457a7be2-36c1-4b2e-a342-e1d90479d28a"
        private const val READ_BUFFER_SIZE = 1024
        private const val STATS_WINDOW_PACKETS = 60
    }

    var onBtConnected: ((deviceName: String) -> Unit)? = null
    var onBtDisconnected: (() -> Unit)? = null
    var onInputReceived: ((GamepadInput) -> Unit)? = null

    private val isRunning = AtomicBoolean(false)
    private val isConnected = AtomicBoolean(false)
    private var serverSocket: Long = WinsockBluetooth.INVALID_SOCKET
    private var clientSocket: Long = WinsockBluetooth.INVALID_SOCKET
    private var assignedChannel: Int = 0

    private val feedbackChannel = Channel<GamepadFeedback>(Channel.CONFLATED)
    private val latestEchoSequence = AtomicInteger(0)
    private val latestLossPctByte = AtomicInteger(0)

    private var serverJob: Job? = null

    fun isConnected(): Boolean = isConnected.get()

    fun start(scope: CoroutineScope) {
        if (!System.getProperty("os.name").lowercase().contains("win")) {
            println("[Bluetooth] RFCOMM Server is only supported on Windows.")
            return
        }

        if (isRunning.getAndSet(true)) return

        serverJob = scope.launch(Dispatchers.IO) {
            runServerLoop()
        }
    }

    fun sendFeedback(feedback: GamepadFeedback) {
        if (isConnected.get()) {
            feedbackChannel.trySend(feedback)
        }
    }

    fun stop() {
        if (!isRunning.getAndSet(false)) return

        isConnected.set(false)
        serverJob?.cancel()

        closeSocket(clientSocket)
        clientSocket = WinsockBluetooth.INVALID_SOCKET

        closeSocket(serverSocket)
        serverSocket = WinsockBluetooth.INVALID_SOCKET

        try {
            WinsockBluetooth.INSTANCE.WSACleanup()
        } catch (_: Exception) {}

        println("[Bluetooth] RFCOMM Server stopped.")
    }

    private suspend fun runServerLoop() {
        println("[Bluetooth] Initializing Winsock2 Bluetooth subsystem...")

        val wsaData = WinsockBluetooth.WSADATA()
        val initRes = WinsockBluetooth.INSTANCE.WSAStartup(0x0202.toShort(), wsaData)
        if (initRes != 0) {
            println("⚠️ [Bluetooth] WSAStartup failed with error code: $initRes")
            isRunning.set(false)
            return
        }

        while (isRunning.get() && currentCoroutineContext().isActive) {
            // 1. Create Bluetooth Socket
            val sock = WinsockBluetooth.INSTANCE.socket(
                WinsockBluetooth.AF_BTH,
                WinsockBluetooth.SOCK_STREAM,
                WinsockBluetooth.BTHPROTO_RFCOMM
            )

            if (sock == WinsockBluetooth.INVALID_SOCKET) {
                val err = WinsockBluetooth.INSTANCE.WSAGetLastError()
                println("⚠️ [Bluetooth] Failed to create AF_BTH socket: Win32 Error $err")
                delay(3000)
                continue
            }

            serverSocket = sock

            // 2. Bind to any available RFCOMM port
            val serverAddr = WinsockBluetooth.SOCKADDR_BTH().apply {
                addressFamily = WinsockBluetooth.AF_BTH.toShort()
                btAddr = 0L // Local radio
                serviceClassId = GUID()
                port = WinsockBluetooth.BT_PORT_ANY
                write()
            }

            val bindRes = WinsockBluetooth.INSTANCE.bind(sock, serverAddr, WinsockBluetooth.SOCKADDR_BTH.SIZE)
            if (bindRes == WinsockBluetooth.SOCKET_ERROR) {
                val err = WinsockBluetooth.INSTANCE.WSAGetLastError()
                closeSocket(sock)
                serverSocket = WinsockBluetooth.INVALID_SOCKET

                if (err == WinsockBluetooth.WSAENETDOWN) {
                    println("⚠️ [Bluetooth] Bluetooth radio is OFF or unavailable in Windows. Retrying in 4s...")
                } else {
                    println("⚠️ [Bluetooth] Failed to bind Bluetooth socket: Win32 Error $err. Retrying in 4s...")
                }
                delay(4000)
                continue
            }

            // 3. Read assigned channel via getsockname
            val boundAddr = WinsockBluetooth.SOCKADDR_BTH()
            val addrLen = IntByReference(WinsockBluetooth.SOCKADDR_BTH.SIZE)
            if (WinsockBluetooth.INSTANCE.getsockname(sock, boundAddr, addrLen) == 0) {
                boundAddr.read()
                assignedChannel = boundAddr.port
                println("⚡ [Bluetooth] Bound to RFCOMM Channel $assignedChannel")
            }

            // 4. Register SDP Service Record via WSASetService
            registerSdpService(boundAddr)

            // 5. Start listening
            val listenRes = WinsockBluetooth.INSTANCE.listen(sock, 1)
            if (listenRes == WinsockBluetooth.SOCKET_ERROR) {
                val err = WinsockBluetooth.INSTANCE.WSAGetLastError()
                println("⚠️ [Bluetooth] listen() failed: Win32 Error $err")
                closeSocket(sock)
                serverSocket = WinsockBluetooth.INVALID_SOCKET
                delay(3000)
                continue
            }

            println("⚡ [Bluetooth] RFCOMM Server listening for mobile connections on UUID: $NEXPAD_BT_UUID_STRING")

            // 6. Accept Loop
            while (isRunning.get() && currentCoroutineContext().isActive) {
                val clientAddr = WinsockBluetooth.SOCKADDR_BTH()
                val clientAddrLen = IntByReference(WinsockBluetooth.SOCKADDR_BTH.SIZE)

                val acceptedSock = WinsockBluetooth.INSTANCE.accept(sock, clientAddr, clientAddrLen)
                if (acceptedSock == WinsockBluetooth.INVALID_SOCKET) {
                    val err = WinsockBluetooth.INSTANCE.WSAGetLastError()
                    if (!isRunning.get()) break
                    println("⚠️ [Bluetooth] accept() returned error: $err")
                    delay(500)
                    continue
                }

                clientAddr.read()
                val clientMac = formatBtAddress(clientAddr.btAddr)
                val deviceDisplayName = "Phone ($clientMac)"
                println("⚡ [Bluetooth] Client connected: $deviceDisplayName")

                clientSocket = acceptedSock
                isConnected.set(true)
                onBtConnected?.invoke(deviceDisplayName)

                // Handle bidirectional streaming for this client
                handleClientSession(acceptedSock)

                // Clean up client disconnect
                closeSocket(acceptedSock)
                clientSocket = WinsockBluetooth.INVALID_SOCKET
                isConnected.set(false)
                onBtDisconnected?.invoke()
                println("[Bluetooth] Client disconnected. Waiting for next connection...")
            }
        }
    }

    private fun registerSdpService(boundAddr: WinsockBluetooth.SOCKADDR_BTH) {
        try {
            val serviceGuid = GUID.fromString(NEXPAD_BT_UUID_STRING)

            // Memory for CSADDR_INFO and SOCKADDR_BTH
            val saMem = Memory(WinsockBluetooth.SOCKADDR_BTH.SIZE.toLong())
            boundAddr.write()
            Pointer.nativeValue(saMem)
            val boundBytes = boundAddr.pointer.getByteArray(0, WinsockBluetooth.SOCKADDR_BTH.SIZE)
            saMem.write(0, boundBytes, 0, WinsockBluetooth.SOCKADDR_BTH.SIZE)

            val csAddr = WinsockBluetooth.CSADDR_INFO().apply {
                LocalAddr.lpSockaddr = saMem
                LocalAddr.iSockaddrLength = WinsockBluetooth.SOCKADDR_BTH.SIZE
                RemoteAddr.lpSockaddr = null
                RemoteAddr.iSockaddrLength = 0
                iSocketType = WinsockBluetooth.SOCK_STREAM
                iProtocol = WinsockBluetooth.BTHPROTO_RFCOMM
                write()
            }

            val querySet = WinsockBluetooth.WSAQUERYSETW().apply {
                dwSize = size()
                lpszServiceInstanceName = com.sun.jna.WString("NEXPAD Gamepad Server")
                lpszComment = com.sun.jna.WString("NEXPAD Bluetooth RFCOMM Gamepad Service")
                lpServiceClassId = serviceGuid.pointer
                dwNumberOfCsAddrs = 1
                lpcsaBuffer = csAddr.pointer
                write()
            }

            val res = WinsockBluetooth.INSTANCE.WSASetServiceW(
                querySet,
                WinsockBluetooth.RNRSERVICE_REGISTER,
                0
            )

            if (res == 0) {
                println("⚡ [Bluetooth] SDP Service successfully registered for UUID $NEXPAD_BT_UUID_STRING")
            } else {
                val err = WinsockBluetooth.INSTANCE.WSAGetLastError()
                println("⚠️ [Bluetooth] WSASetService warning: Win32 Error $err (Direct RFCOMM connection will still work)")
            }
        } catch (e: Exception) {
            println("⚠️ [Bluetooth] SDP registration exception: ${e.message}")
        }
    }

    private suspend fun handleClientSession(socket: Long) = coroutineScope {
        // 1. Configure low-latency Winsock buffers to prevent OS-level bufferbloat
        try {
            val rcvBufMem = Memory(4)
            rcvBufMem.setInt(0, 2048) // 2KB receive buffer (approx 46 frames max)
            WinsockBluetooth.INSTANCE.setsockopt(socket, WinsockBluetooth.SOL_SOCKET, WinsockBluetooth.SO_RCVBUF, rcvBufMem, 4)

            val sndBufMem = Memory(4)
            sndBufMem.setInt(0, 1024) // 1KB send buffer
            WinsockBluetooth.INSTANCE.setsockopt(socket, WinsockBluetooth.SOL_SOCKET, WinsockBluetooth.SO_SNDBUF, sndBufMem, 4)
        } catch (e: Exception) {
            println("⚠️ [Bluetooth] setsockopt buffer tuning warning: ${e.message}")
        }

        val decoder = AoaFrameDecoder(NexpadProtocol.INPUT_PACKET_SIZE)
        val rxRawBuffer = ByteArray(READ_BUFFER_SIZE)

        var lastSequenceNumber = Int.MIN_VALUE
        var lostInWindow = 0
        var receivedInWindow = 0
        var packetCount = 0

        // TX Feedback Job (PC -> Phone Rumble + 50 Hz Telemetry Keepalive)
        // Rate-limiting feedback prevents half-duplex Bluetooth baseband slot contention
        val txJob = launch(Dispatchers.IO) {
            val feedbackBytes = ByteArray(NexpadProtocol.FEEDBACK_PACKET_SIZE)
            var currentFeedback = GamepadFeedback(0, 0)
            var lastTelemetrySent = 0L

            while (isActive && isConnected.get()) {
                // Event-driven rumble feedback or 20ms periodic telemetry keepalive
                val fb = withTimeoutOrNull(20L) {
                    feedbackChannel.receive()
                }
                if (fb != null) {
                    currentFeedback = fb
                }

                // Drain any additional pending feedback updates
                var nextFeedback = feedbackChannel.tryReceive().getOrNull()
                while (nextFeedback != null) {
                    currentFeedback = nextFeedback
                    nextFeedback = feedbackChannel.tryReceive().getOrNull()
                }

                val now = System.currentTimeMillis()
                val isRumbleEvent = (fb != null)
                val isTelemetryTick = (now - lastTelemetrySent >= 20L)

                if (isRumbleEvent || isTelemetryTick) {
                    lastTelemetrySent = now
                    NexpadProtocol.encodeFeedback(
                        feedback = currentFeedback,
                        echoSequenceNumber = latestEchoSequence.get(),
                        packetLossByte = latestLossPctByte.get().toByte(),
                        out = feedbackBytes,
                        offset = 0
                    )
                    val sent = WinsockBluetooth.INSTANCE.send(socket, feedbackBytes, feedbackBytes.size, 0)
                    if (sent <= 0) {
                        break
                    }
                }
            }
        }

        // RX Inbound Input Loop (Phone -> PC Inputs)
        try {
            var hasLoggedFirstPacket = false

            while (isActive && isConnected.get()) {
                val bytesRead = WinsockBluetooth.INSTANCE.recv(socket, rxRawBuffer, rxRawBuffer.size, 0)
                if (bytesRead <= 0) {
                    val err = WinsockBluetooth.INSTANCE.WSAGetLastError()
                    if (bytesRead < 0 && err != WinsockBluetooth.WSAEWOULDBLOCK) {
                        println("[Bluetooth] Client read error: $err")
                    }
                    break
                }

                var latestInputInBurst: GamepadInput? = null

                decoder.append(rxRawBuffer, 0, bytesRead) { packetBuffer, packetOffset ->
                    val gamepadInput = NexpadProtocol.decodeInput(packetBuffer, packetOffset) ?: return@append

                    if (!hasLoggedFirstPacket) {
                        println("⚡ [Bluetooth] Streaming active! Low-latency RFCOMM pipeline online.")
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
                        latestEchoSequence.set(gamepadInput.sequenceNumber)

                        if (packetCount % STATS_WINDOW_PACKETS == 0) {
                            val total = lostInWindow + receivedInWindow
                            val currentLossPct = if (total > 0) (255 * lostInWindow / total).coerceIn(0, 255) else 0
                            latestLossPctByte.set(currentLossPct)
                            lostInWindow = 0
                            receivedInWindow = 0
                        }
                    }

                    // Always capture latest packet in this read batch
                    latestInputInBurst = gamepadInput
                }

                // Discard stale intermediate frames from burst; dispatch only freshest state
                latestInputInBurst?.let { onInputReceived?.invoke(it) }
            }
        } catch (e: Exception) {
            println("[Bluetooth] Session exception: ${e.message}")
        } finally {
            txJob.cancel()
        }
    }

    private fun closeSocket(sock: Long) {
        if (sock != WinsockBluetooth.INVALID_SOCKET && sock != 0L) {
            try {
                WinsockBluetooth.INSTANCE.closesocket(sock)
            } catch (_: Exception) {}
        }
    }

    private fun formatBtAddress(bthAddr: Long): String {
        val b0 = ((bthAddr ushr 40) and 0xFF).toInt()
        val b1 = ((bthAddr ushr 32) and 0xFF).toInt()
        val b2 = ((bthAddr ushr 24) and 0xFF).toInt()
        val b3 = ((bthAddr ushr 16) and 0xFF).toInt()
        val b4 = ((bthAddr ushr 8) and 0xFF).toInt()
        val b5 = (bthAddr and 0xFF).toInt()
        return String.format("%02X:%02X:%02X:%02X:%02X:%02X", b0, b1, b2, b3, b4, b5)
    }
}
