package com.sanket.tools.nexpaddesktop.plugins

import com.sanket.tools.nexpad.nxprc.NxprcDocument
import com.sanket.tools.nexpad.protocol.NexpadProtocol
import com.sanket.tools.nexpaddesktop.connection.ActiveTransport
import com.sanket.tools.nexpaddesktop.connection.bt.BluetoothRfcommServer
import com.sanket.tools.nexpaddesktop.connection.usb.aoa.AoaManager
import com.sanket.tools.nexpaddesktop.connection.wifi.UdpServer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.InetSocketAddress
import java.net.Socket

/**
 * Universal Multi-Transport Push Manager for NXPRC Plugins.
 *
 * Dispatches NXPRC component file transfers over whichever connection channel is actively connected:
 * - Wi-Fi / USB Tethering: Dedicated out-of-band TCP connection on port 9995 (zero UDP disruption)
 * - USB (Direct AOA): Multiplexed burst on bulk OUT endpoint (interception magic 0xAF on Android)
 * - Bluetooth: Multiplexed burst on RFCOMM feedback socket
 * - USB (ADB): Direct private filesDir push with broadcast reload
 */
object UniversalPushManager {

    @Volatile
    var currentTransport: ActiveTransport = ActiveTransport.NONE

    var udpServer: UdpServer? = null
    var aoaManager: AoaManager? = null
    var btServer: BluetoothRfcommServer? = null

    /**
     * True if a phone is actively connected via any transport.
     */
    fun canPush(): Boolean {
        return currentTransport != ActiveTransport.NONE
    }

    /**
     * User-facing label for the Push button reflecting the active transport.
     */
    fun getPushActionLabel(layerCount: Int? = null): String {
        val suffix = if (layerCount != null) " ($layerCount L)" else ""
        return when (currentTransport) {
            ActiveTransport.WIFI -> "Push via Wi-Fi$suffix"
            ActiveTransport.USB_TETHERING -> "Push via Tethering$suffix"
            ActiveTransport.USB_AOA -> "Push via USB Direct$suffix"
            ActiveTransport.USB_ADB -> "Push via ADB$suffix"
            ActiveTransport.BLUETOOTH -> "Push via Bluetooth$suffix"
            ActiveTransport.NONE -> "No Phone Connected"
        }
    }

    /**
     * Push the given NXPRC document to the phone via the current active transport.
     */
    suspend fun pushComponent(doc: NxprcDocument): Result<String> = withContext(Dispatchers.IO) {
        val transport = currentTransport
        if (transport == ActiveTransport.NONE) {
            return@withContext Result.failure(IllegalStateException("No phone connected. Connect via Wi-Fi, USB, or Bluetooth first."))
        }

        val rawBytes = try {
            NxprcDocument.encodeToBytes(doc)
        } catch (e: Exception) {
            return@withContext Result.failure(IllegalStateException("Failed to encode document: ${e.message}", e))
        }

        val componentId = doc.manifest.id
        val name = doc.manifest.name

        when (transport) {
            ActiveTransport.WIFI, ActiveTransport.USB_TETHERING -> {
                pushViaTcp(componentId, name, rawBytes)
            }
            ActiveTransport.USB_AOA -> {
                pushViaAoa(componentId, name, rawBytes)
            }
            ActiveTransport.BLUETOOTH -> {
                pushViaBluetooth(componentId, name, rawBytes)
            }
            ActiveTransport.USB_ADB -> {
                DesktopPluginManager.transferNxprcViaAdb(doc)
            }
            ActiveTransport.NONE -> {
                Result.failure(IllegalStateException("Disconnected"))
            }
        }
    }

    private fun pushViaTcp(componentId: String, name: String, bytes: ByteArray): Result<String> {
        val host = udpServer?.getConnectedClientHost()
            ?: return Result.failure(IllegalStateException("Cannot resolve phone IP from active UDP session."))

        return try {
            Socket().use { socket ->
                socket.soTimeout = 8000
                socket.connect(InetSocketAddress(host, NexpadProtocol.SYNC_TCP_PORT), 4000)
                val out = DataOutputStream(socket.getOutputStream())
                val `in` = DataInputStream(socket.getInputStream())

                val checksum = NexpadProtocol.computeCrc32(bytes)
                val header = NexpadProtocol.encodeFileSyncHeader(componentId, bytes.size, checksum)

                out.write(header)
                out.write(bytes)
                out.flush()

                val ack = `in`.readByte()
                if (ack == NexpadProtocol.FILE_SYNC_ACK) {
                    Result.success("Pushed '$name' ($componentId) via Wi-Fi/Tethering! (Hot-reloaded)")
                } else {
                    Result.failure(IllegalStateException("Phone rejected file (status=0x${(ack.toInt() and 0xFF).toString(16)}). Checksum mismatch or write error."))
                }
            }
        } catch (e: Exception) {
            Result.failure(IllegalStateException("TCP Push failed to $host:${NexpadProtocol.SYNC_TCP_PORT}: ${e.message}", e))
        }
    }

    private fun pushViaAoa(componentId: String, name: String, bytes: ByteArray): Result<String> {
        val aoa = aoaManager ?: return Result.failure(IllegalStateException("USB AOA Manager not initialized"))
        val queued = aoa.pushNxprc(componentId, bytes)
        return if (queued) {
            Result.success("Pushed '$name' ($componentId) via USB Direct! (Hot-reloaded)")
        } else {
            Result.failure(IllegalStateException("Failed to queue file packet on USB AOA pipe."))
        }
    }

    private fun pushViaBluetooth(componentId: String, name: String, bytes: ByteArray): Result<String> {
        val bt = btServer ?: return Result.failure(IllegalStateException("Bluetooth Server not initialized"))
        val queued = bt.pushNxprc(componentId, bytes)
        return if (queued) {
            Result.success("Pushed '$name' ($componentId) via Bluetooth! (Hot-reloaded)")
        } else {
            Result.failure(IllegalStateException("Failed to queue file packet on Bluetooth stream."))
        }
    }
}
