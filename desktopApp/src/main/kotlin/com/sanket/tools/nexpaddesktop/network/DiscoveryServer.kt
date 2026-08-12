package com.sanket.tools.nexpaddesktop.network

import com.sanket.tools.nexpaddesktop.protocol.NexpadProtocol
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
import java.net.InetAddress
import java.nio.ByteBuffer

class DiscoveryServer(private val port: Int = 9998) {
    private var serverSocket: BoundDatagramSocket? = null
    private var selectorManager: SelectorManager? = null

    suspend fun start() = withContext(Dispatchers.IO) {
        selectorManager = SelectorManager(Dispatchers.IO)
        // Bind to all interfaces on discovery port
        serverSocket = aSocket(selectorManager!!).udp().bind(InetSocketAddress("0.0.0.0", port))
        
        println("📡 Discovery Server listening on broadcast port $port")
        
        val hostName = InetAddress.getLocalHost().hostName
        val hostNameBytes = hostName.toByteArray(Charsets.UTF_8)
        
        while (isActive) {
            try {
                val socket = serverSocket ?: break
                val datagram = socket.receive()
                val data = datagram.packet.readBytes()
                
                if (data.isNotEmpty() && data[0] == NexpadProtocol.PACKET_TYPE_DISCOVER) {
                    println("🔍 Received DISCOVER packet from ${datagram.address}")
                    
                    // Reply Format: [PACKET_TYPE_SERVER_INFO(1)] [NameLength(1)] [NameBytes(N)]
                    val buffer = ByteBuffer.allocate(2 + hostNameBytes.size)
                    buffer.put(NexpadProtocol.PACKET_TYPE_SERVER_INFO)
                    buffer.put(hostNameBytes.size.toByte())
                    buffer.put(hostNameBytes)
                    
                    val responsePacket = Datagram(ByteReadPacket(buffer.array()), datagram.address)
                    socket.send(responsePacket)
                }
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e // Preserve structured concurrency
            } catch (e: Exception) {
                // Ignore silent drops
            }
        }
    }

    fun stop() {
        serverSocket?.close()
        selectorManager?.close()
        println("📡 Discovery Server stopped.")
    }
}
