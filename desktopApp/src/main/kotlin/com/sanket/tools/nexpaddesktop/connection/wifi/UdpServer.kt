package com.sanket.tools.nexpaddesktop.connection.wifi

import com.sanket.tools.nexpad.model.GamepadInput
import io.ktor.network.selector.SelectorManager
import io.ktor.network.sockets.InetSocketAddress
import io.ktor.network.sockets.aSocket
import io.ktor.utils.io.core.readBytes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import com.sanket.tools.nexpad.model.GamepadFeedback
import com.sanket.tools.nexpad.protocol.NexpadProtocol
import java.nio.ByteBuffer
import io.ktor.network.sockets.BoundDatagramSocket
import io.ktor.network.sockets.Datagram
import io.ktor.network.sockets.SocketAddress
import io.ktor.utils.io.core.ByteReadPacket
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString

import io.ktor.network.sockets.toJavaAddress
import com.sun.jna.Native
import com.sun.jna.platform.win32.Kernel32
import com.sun.jna.platform.win32.WinBase
import com.sun.jna.win32.StdCallLibrary
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.io.readByteArray
import kotlin.time.Duration.Companion.milliseconds

private fun formatAddress(addr: SocketAddress?): String {
    if (addr == null) return "null"
    return try {
        val javaSock = (addr as? InetSocketAddress)?.toJavaAddress() as? java.net.InetSocketAddress
        if (javaSock != null) {
            "${javaSock.hostString}:${javaSock.port}"
        } else {
            addr.javaClass.simpleName
        }
    } catch (_: Exception) {
        "unknown"
    }
}

interface WinMM : StdCallLibrary {
    fun timeBeginPeriod(uPeriod: Int): Int
    fun timeEndPeriod(uPeriod: Int): Int
    companion object {
        val INSTANCE: WinMM? by lazy {
            try { Native.load("winmm", WinMM::class.java) } catch (e: Exception) { null }
        }
    }
}

class UdpServer(
    private val port: Int = 9999,
    private val onClientConnected: ((deviceName: String, connectionType: Int) -> Unit)? = null,
    private val onClientDisconnected: (() -> Unit)? = null,
    private val onInputReceived: (GamepadInput) -> Unit
) {
    @Volatile private var clientAddress: SocketAddress? = null
    private var selectorManager: SelectorManager? = null
    private var lastSequenceNumber = Int.MIN_VALUE
    private var serverSocket: BoundDatagramSocket? = null
    private var packetCount = 0
    private var hasLoggedFirstPacket = false
    private var totalPackets = 0L
    @Volatile private var lastFeedback = GamepadFeedback(0, 0)
    @Volatile private var cachedLossPctByte: Byte = 0 // Cache last rumble state for Ping Echoes
    private var lostInWindow = 0
    private var receivedInWindow = 0

    // Single Active Transport Guard: drop incoming UDP packets if another transport is active
    var isExternalTransportActive: () -> Boolean = { false }

    @OptIn(kotlinx.coroutines.DelicateCoroutinesApi::class, kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    private val dedicatedDispatcher = kotlinx.coroutines.newSingleThreadContext("UdpServerThread")

    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun start() = kotlinx.coroutines.withContext(dedicatedDispatcher) {
        // Optimize Windows Thread Priority and Timer Resolution for minimal jitter
        if (System.getProperty("os.name").lowercase().contains("win")) {
            try {
                WinMM.INSTANCE?.timeBeginPeriod(1)
                val currentThread = Kernel32.INSTANCE.GetCurrentThread()
                // WinBase.THREAD_PRIORITY_HIGHEST = 2
                Kernel32.INSTANCE.SetThreadPriority(currentThread, 2)
                println("⚡ [QoS] Windows Timer Resolution set to 1ms. Thread Priority = HIGHEST.")
            } catch (e: Exception) {
                println("⚠️ [QoS] Failed to set Windows specific thread optimizations: ${e.message}")
            }
        }
        
        selectorManager = SelectorManager(Dispatchers.IO)
        serverSocket = aSocket(selectorManager!!).udp().bind(InetSocketAddress("0.0.0.0", port))
        
        println("UDP Server listening on port $port")
        
        while (isActive) {
            try {
                val socket = serverSocket ?: break
                
                // Wait for a packet, timeout after 2 seconds to detect sudden disconnects
                val datagram = kotlinx.coroutines.withTimeoutOrNull(2000L.milliseconds) {
                    socket.receive()
                }

                if (datagram == null) {
                    if (clientAddress != null) {
                        println("⚠️ [UDP DEBUG] Connection timed out (no packets for 2 seconds). Disconnecting...")
                        clientAddress = null
                        onClientDisconnected?.invoke()
                    }
                    continue
                }

                if (isExternalTransportActive()) {
                    // Another transport (USB/Bluetooth) has exclusive control; drop incoming UDP
                    continue
                }
                
                val data = datagram.packet.readByteArray()
                val firstByte: Byte = if (data.isNotEmpty()) data[0] else (-1).toByte()
                
                // Security & Robustness: Only adopt a new address if it sends a formal CONNECT handshake.
                if (clientAddress != datagram.address) {
                    if (firstByte != NexpadProtocol.PACKET_TYPE_CONNECT) {
                        continue // Ignore unsolicited packets from anyone who hasn't handshaked
                    }
                    println("📡 [UDP DEBUG] New client session detected! Resetting sequence tracker.")
                    clientAddress = datagram.address
                    lastSequenceNumber = Int.MIN_VALUE
                }
                
                totalPackets++
                packetCount = (packetCount % 60) + 1  // Wraps 1→60, never grows unbounded
                if (!hasLoggedFirstPacket) {
                    hasLoggedFirstPacket = true
                    val debugFirstByte = if (data.isNotEmpty()) data[0] else -1
                    println("📡 [UDP DEBUG] Received first packet. Size: ${data.size} bytes | First byte: $debugFirstByte | From: ${formatAddress(clientAddress)}")
                } else if (packetCount == 60) {
                    println("📡 [UDP DEBUG] Packets received: $totalPackets | From: ${formatAddress(clientAddress)}")
                }
                
                
                // Formal Handshake Protocol
                if (firstByte == NexpadProtocol.PACKET_TYPE_CONNECT) {
                    println("🤝 [UDP DEBUG] Received CONNECT handshake from ${formatAddress(clientAddress)}")
                    
                    var deviceName = "Unknown Device"
                    var connType = 1
                    
                    if (data.size >= 3) {
                        connType = data[1].toInt()
                        val nameLen = data[2].toInt() and 0xFF
                        if (data.size >= 3 + nameLen) {
                            deviceName = String(data, 3, nameLen, Charsets.UTF_8)
                        }
                    }

                    // Auto-detect USB Tethering without reverse DNS lookup (zero blocking)
                    if (connType != 2) {
                        try {
                            val javaSock = (datagram.address as? io.ktor.network.sockets.InetSocketAddress)?.toJavaAddress() as? java.net.InetSocketAddress
                            val inet = javaSock?.address
                            if (inet != null && NetworkUtils.isUsbTetheringAddress(inet)) {
                                println("⚡ [UDP Server] Auto-detected USB Tethering from client address: ${javaSock.hostString}")
                                connType = 2
                            }
                        } catch (_: Exception) {}
                    }
                    
                    onClientConnected?.invoke(deviceName, connType)

                    val pcName = System.getenv("COMPUTERNAME") ?: "Windows PC"
                    val pcBytes = pcName.toByteArray(Charsets.UTF_8)
                    val safeLen = pcBytes.size.coerceAtMost(255)
                    val buffer = ByteBuffer.allocate(2 + safeLen)
                    buffer.put(NexpadProtocol.PACKET_TYPE_CONNECTED)
                    buffer.put(safeLen.toByte())
                    buffer.put(pcBytes, 0, safeLen)
                    val responsePacket = Datagram(ByteReadPacket(buffer.array()), datagram.address)
                    socket.send(responsePacket)
                    continue
                } else if (firstByte == NexpadProtocol.PACKET_TYPE_DISCONNECT) {
                    println("👋 [UDP DEBUG] Received DISCONNECT from ${formatAddress(clientAddress)}")
                    clientAddress = null
                    onClientDisconnected?.invoke()
                    continue
                }
                
                // Check if it's a binary packet. No JSON fallback.
                val input = if (data.size == NexpadProtocol.INPUT_PACKET_SIZE && data.isNotEmpty() && firstByte == NexpadProtocol.PACKET_TYPE_INPUT) {
                    NexpadProtocol.decodeInput(data)
                } else {
                    null
                }
                
                if (input != null) {
                    // Sequence Validation (Wraparound-Safe Modular Math)
                    val delta = input.sequenceNumber - lastSequenceNumber
                    if (delta > 0 || lastSequenceNumber == Int.MIN_VALUE) {
                        
                        if (lastSequenceNumber != Int.MIN_VALUE && delta > 1) {
                            lostInWindow += (delta - 1).coerceAtMost(255)
                        }
                        receivedInWindow++
                        
                        lastSequenceNumber = input.sequenceNumber
                        onInputReceived(input)
                        
                        // Calculate packet loss over a 1-second window (approx 60 packets)
                        if (packetCount % 60 == 0) {
                            val total = lostInWindow + receivedInWindow
                            val currentLossPct = if (total > 0) (255 * lostInWindow / total).coerceIn(0, 255) else 0
                            cachedLossPctByte = currentLossPct.toByte()
                            lostInWindow = 0
                            receivedInWindow = 0
                        }
                        
                        // RTT Jitter Measurement & Feedback Heartbeat (Every 2 packets / 33ms)
                        if (packetCount % 2 == 0) {
                            sendFeedback(lastFeedback, echoSequenceNumber = input.sequenceNumber, packetLossByte = cachedLossPctByte)
                        }
                    } else {
                        if (packetCount % 10 == 0) {
                            println("⚠️ [UDP DEBUG] Dropped STALE/REORDERED packet! Seq: ${input.sequenceNumber}, Expected > $lastSequenceNumber")
                        }
                    }
                }
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e // Preserve structured concurrency
            } catch (e: Exception) {
                if (packetCount % 10 == 0) {
                    println("⚠️ [UDP ERROR] Exception in receive loop: ${e.message}")
                    e.printStackTrace()
                }
            }
        }
    }

    suspend fun sendFeedback(feedback: GamepadFeedback, echoSequenceNumber: Int = 0, packetLossByte: Byte = 0) {
        val target = clientAddress ?: return
        val socket = serverSocket ?: return
        lastFeedback = feedback
        try {
            val bytes = NexpadProtocol.encodeFeedback(feedback, echoSequenceNumber, packetLossByte)
            val packet = Datagram(ByteReadPacket(bytes), target)
            socket.send(packet)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun stop() {
        if (System.getProperty("os.name").lowercase().contains("win")) {
            try {
                WinMM.INSTANCE?.timeEndPeriod(1)
            } catch (e: Exception) {
                // Ignore
            }
        }
        serverSocket?.close()
        selectorManager?.close()
        dedicatedDispatcher.close()
        println("UDP Server stopped.")
    }
}
