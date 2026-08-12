package com.sanket.tools.nexpaddesktop.network

import com.sanket.tools.nexpaddesktop.model.GamepadInput
import io.ktor.network.selector.SelectorManager
import io.ktor.network.sockets.InetSocketAddress
import io.ktor.network.sockets.aSocket
import io.ktor.utils.io.core.readBytes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

import com.sanket.tools.nexpaddesktop.model.GamepadFeedback
import com.sanket.tools.nexpaddesktop.protocol.NexpadProtocol
import java.nio.ByteBuffer
import io.ktor.network.sockets.BoundDatagramSocket
import io.ktor.network.sockets.Datagram
import io.ktor.network.sockets.SocketAddress
import io.ktor.utils.io.core.ByteReadPacket
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString

import com.sun.jna.Native
import com.sun.jna.platform.win32.Kernel32
import com.sun.jna.platform.win32.WinBase
import com.sun.jna.win32.StdCallLibrary

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
    private val onInputReceived: (GamepadInput) -> Unit
) {
    @Volatile private var clientAddress: SocketAddress? = null
    private var selectorManager: SelectorManager? = null
    private var lastSequenceNumber = Int.MIN_VALUE
    private var serverSocket: BoundDatagramSocket? = null
    private var packetCount = 0
    @Volatile private var lastFeedback = GamepadFeedback(0, 0) // Cache last rumble state for Ping Echoes

    suspend fun start() = withContext(Dispatchers.IO) {
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
                val datagram = socket.receive()
                
                // Reset sequence tracker on a new client connection (IP+Port match)
                if (clientAddress != datagram.address) {
                    println("📡 [UDP DEBUG] New client session detected! Resetting sequence tracker.")
                    clientAddress = datagram.address
                    lastSequenceNumber = Int.MIN_VALUE
                }
                
                val data = datagram.packet.readBytes()
                
                packetCount++
                if (packetCount == 1 || packetCount % 60 == 0) {
                    val debugFirstByte = if (data.isNotEmpty()) data[0] else -1
                    println("📡 [UDP DEBUG] Received packet #$packetCount. Size: ${data.size} bytes | First byte: $debugFirstByte | From: $clientAddress")
                }
                
                val firstByte: Byte = if (data.isNotEmpty()) data[0] else (-1).toByte()
                
                // Formal Handshake Protocol
                if (firstByte == NexpadProtocol.PACKET_TYPE_CONNECT) {
                    println("🤝 [UDP DEBUG] Received CONNECT handshake from $clientAddress")
                    val buffer = ByteBuffer.allocate(1)
                    buffer.put(NexpadProtocol.PACKET_TYPE_CONNECTED)
                    val responsePacket = Datagram(ByteReadPacket(buffer.array()), datagram.address)
                    socket.send(responsePacket)
                    continue
                } else if (firstByte == NexpadProtocol.PACKET_TYPE_DISCONNECT) {
                    println("👋 [UDP DEBUG] Received DISCONNECT from $clientAddress")
                    clientAddress = null
                    continue
                }
                
                // Check if it's a binary packet. Fallback to JSON otherwise.
                val input = if (data.size == NexpadProtocol.INPUT_PACKET_SIZE && data.isNotEmpty() && firstByte == NexpadProtocol.PACKET_TYPE_INPUT) {
                    NexpadProtocol.decodeInput(data)
                } else {
                    if (packetCount % 60 == 0) {
                        println("⚠️ [UDP DEBUG] Packet size ${data.size} did not match Binary Protocol. Trying JSON...")
                    }
                    val jsonString = String(data)
                    Json.decodeFromString<GamepadInput>(jsonString)
                }
                
                if (input != null) {
                    // Sequence Validation (Wraparound-Safe Modular Math)
                    val delta = input.sequenceNumber - lastSequenceNumber
                    if (delta > 0 || lastSequenceNumber == Int.MIN_VALUE) {
                        lastSequenceNumber = input.sequenceNumber
                        onInputReceived(input)
                        
                        // RTT Jitter Measurement: Echo every 5th packet back to Android
                        if (packetCount % 5 == 0) {
                            sendFeedback(lastFeedback, echoSequenceNumber = input.sequenceNumber)
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
                // Ignore silent drops for high-speed UDP
            }
        }
    }

    suspend fun sendFeedback(feedback: GamepadFeedback, echoSequenceNumber: Int = 0) = withContext(Dispatchers.IO) {
        val target = clientAddress ?: return@withContext
        val socket = serverSocket ?: return@withContext
        lastFeedback = feedback
        try {
            val bytes = NexpadProtocol.encodeFeedback(feedback, echoSequenceNumber)
            val packet = Datagram(ByteReadPacket(bytes), target)
            socket.send(packet)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

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
        println("UDP Server stopped.")
    }
}
