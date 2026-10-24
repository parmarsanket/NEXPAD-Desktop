package com.sanket.tools.nexpaddesktop.connection.wifi

import com.sanket.tools.nexpad.protocol.NexpadProtocol
import io.ktor.network.selector.SelectorManager
import io.ktor.network.sockets.BoundDatagramSocket
import io.ktor.network.sockets.Datagram
import io.ktor.network.sockets.InetSocketAddress
import io.ktor.network.sockets.aSocket
import io.ktor.utils.io.core.ByteReadPacket
import io.ktor.utils.io.core.readBytes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import kotlinx.io.readByteArray
import java.net.BindException
import java.net.InetAddress
import java.nio.ByteBuffer

class DiscoveryServer(
    private val port: Int = 9998,
    var streamingPort: () -> Int = { 9999 }
) {
    private var serverSocket: BoundDatagramSocket? = null
    private var selectorManager: SelectorManager? = null
    @Volatile private var isRunning = false

    // Single Active Transport Guard: do not advertise if a client is already connected
    var isPaused: () -> Boolean = { false }

    suspend fun start() = withContext(Dispatchers.IO) {
        try {
            selectorManager = SelectorManager(Dispatchers.IO)
            // Bind to all interfaces on discovery port
            serverSocket = aSocket(selectorManager!!).udp().bind(InetSocketAddress("0.0.0.0", port))
        } catch (e: BindException) {
            println("⚠️ Discovery Server port $port already in use (BindException): ${e.message}")
            selectorManager?.close()
            selectorManager = null
            serverSocket = null
            return@withContext
        } catch (e: Exception) {
            println("⚠️ Failed to bind Discovery Server on port $port: ${e.message}")
            selectorManager?.close()
            selectorManager = null
            serverSocket = null
            return@withContext
        }
        
        isRunning = true
        println("📡 Discovery Server listening on broadcast port $port")
        
        val hostName = try {
            InetAddress.getLocalHost().hostName
        } catch (_: Throwable) {
            "NEXPAD-PC"
        }
        val hostNameBytes = hostName.toByteArray(Charsets.UTF_8)
        
        while (isActive && isRunning) {
            try {
                val socket = serverSocket ?: break
                if (!isRunning) break
                val datagram = socket.receive()
                val data = datagram.packet.readByteArray()
                
                if (isPaused()) {
                    continue
                }
                
                if (data.isNotEmpty() && data[0] == NexpadProtocol.PACKET_TYPE_DISCOVER) {
                    println("🔍 Received DISCOVER packet from ${datagram.address}")
                    
                    val activeStreamingPort = streamingPort().coerceIn(1024, 65535)
                    // Reply Format: [PACKET_TYPE_SERVER_INFO(1)] [NameLength(1)] [NameBytes(N)] [ActiveStreamingPort(2)]
                    val buffer = ByteBuffer.allocate(2 + hostNameBytes.size + 2)
                    buffer.put(NexpadProtocol.PACKET_TYPE_SERVER_INFO)
                    buffer.put(hostNameBytes.size.toByte())
                    buffer.put(hostNameBytes)
                    buffer.putShort(activeStreamingPort.toShort())
                    
                    val responsePacket = Datagram(ByteReadPacket(buffer.array()), datagram.address)
                    socket.send(responsePacket)
                    // Burst redundancy to counteract Wi-Fi UDP packet loss
                    kotlinx.coroutines.delay(10)
                    socket.send(Datagram(ByteReadPacket(buffer.array()), datagram.address))
                }
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e // Preserve structured concurrency
            } catch (e: Exception) {
                if (!isRunning) break
                // Ignore silent drops
            }
        }
    }

    fun stop() {
        isRunning = false
        try { serverSocket?.close() } catch (_: Exception) {}
        serverSocket = null
        try { selectorManager?.close() } catch (_: Exception) {}
        selectorManager = null
        println("📡 Discovery Server stopped.")
    }
}
