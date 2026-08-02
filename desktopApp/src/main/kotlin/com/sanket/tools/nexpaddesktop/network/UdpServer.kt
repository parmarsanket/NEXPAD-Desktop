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
import io.ktor.network.sockets.BoundDatagramSocket
import io.ktor.network.sockets.Datagram
import io.ktor.network.sockets.SocketAddress
import io.ktor.utils.io.core.ByteReadPacket
import kotlinx.serialization.encodeToString

class UdpServer(
    private val port: Int = 9999,
    private val onInputReceived: (GamepadInput) -> Unit
) {
    private var clientAddress: SocketAddress? = null
    private var serverSocket: BoundDatagramSocket? = null
    suspend fun start() = withContext(Dispatchers.IO) {
        val selectorManager = SelectorManager(Dispatchers.IO)
        serverSocket = aSocket(selectorManager).udp().bind(InetSocketAddress("0.0.0.0", port))
        
        println("UDP Server listening on port $port")
        
        while (isActive) {
            try {
                val datagram = serverSocket!!.receive()
                clientAddress = datagram.address
                val jsonString = String(datagram.packet.readBytes())
                val input = Json.decodeFromString<GamepadInput>(jsonString)
                onInputReceived(input)
            } catch (e: Exception) {
                // Ignore silent drops for high-speed UDP
            }
        }
    }

    suspend fun sendFeedback(feedback: GamepadFeedback) = withContext(Dispatchers.IO) {
        val target = clientAddress ?: return@withContext
        val socket = serverSocket ?: return@withContext
        try {
            val jsonString = Json.encodeToString(feedback)
            val packet = Datagram(ByteReadPacket(jsonString.toByteArray()), target)
            socket.send(packet)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
