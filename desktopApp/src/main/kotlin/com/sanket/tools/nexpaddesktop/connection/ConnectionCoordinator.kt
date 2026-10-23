package com.sanket.tools.nexpaddesktop.connection

import com.sanket.tools.nexpad.model.GamepadFeedback
import com.sanket.tools.nexpad.model.GamepadInput
import com.sanket.tools.nexpaddesktop.connection.adb.AdbBridgeManager
import com.sanket.tools.nexpaddesktop.connection.bt.BluetoothRfcommServer
import com.sanket.tools.nexpaddesktop.connection.usb.aoa.AoaManager
import com.sanket.tools.nexpaddesktop.connection.wifi.DiscoveryServer
import com.sanket.tools.nexpaddesktop.connection.wifi.UdpServer
import com.sanket.tools.nexpaddesktop.plugins.UniversalPushManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * High-level connection coordinator and arbitrator across all 4 transport layers:
 * 1. Wi-Fi / USB Tethering (UDP via [UdpServer])
 * 2. USB Direct (Android Open Accessory 2.0 via [AoaManager])
 * 3. USB ADB Bridge (Unix Domain Socket via [AdbBridgeManager])
 * 4. Bluetooth RFCOMM (Winsock2 via [BluetoothRfcommServer])
 *
 * Responsibilities:
 * - Single Active Transport arbitration and mutual exclusion
 * - Unified feedback (rumble & sensor mode) dispatching
 * - Lifecycle synchronization (start, stop, port rebinding)
 * - Auto-syncing [UniversalPushManager] state
 */
class ConnectionCoordinator(
    private var streamingPort: Int = 9999,
    private val getActiveTransport: () -> ActiveTransport = { ActiveTransport.NONE },
    val aoaManager: AoaManager = AoaManager(),
    val adbBridgeManager: AdbBridgeManager = AdbBridgeManager(tcpPort = streamingPort),
    val btServer: BluetoothRfcommServer = BluetoothRfcommServer(),
    val discoveryServer: DiscoveryServer = DiscoveryServer(streamingPort = { streamingPort })
) {
    var udpServer: UdpServer? = null
        private set

    var onTransportConnected: ((ActiveTransport, String) -> Unit)? = null
    var onTransportDisconnected: ((ActiveTransport) -> Unit)? = null
    var onInputReceived: ((GamepadInput) -> Unit)? = null
    var onDriverNeedChanged: ((needed: Boolean, vid: Short?, pid: Short?, mi: Int?) -> Unit)? = null

    private var currentScope: CoroutineScope? = null

    /**
     * Starts all transports and establishes mutual exclusion guards.
     */
    fun start(scope: CoroutineScope) {
        currentScope = scope

        // 1. Initialize UDP Server
        setupUdpServer(streamingPort)

        // 2. Configure AOA Manager
        aoaManager.onAoaConnected = { name ->
            onTransportConnected?.invoke(ActiveTransport.USB_AOA, name)
            btServer.pause()
            syncUniversalPush()
        }
        aoaManager.onAoaDisconnected = {
            onTransportDisconnected?.invoke(ActiveTransport.USB_AOA)
            syncUniversalPush()
        }
        aoaManager.onInputReceived = { input -> onInputReceived?.invoke(input) }

        // Mutual exclusion: If USB Debugging is ON and ADB is detected, AOA is suppressed
        aoaManager.isAdbActive = { adbBridgeManager.hasActiveAdb() }
        aoaManager.isAdbInitialScanCompleted = { adbBridgeManager.isInitialScanCompleted.get() }
        aoaManager.isScanningPaused = {
            val current = getActiveTransport()
            current != ActiveTransport.NONE && current != ActiveTransport.USB_AOA
        }
        aoaManager.onDriverNeedChanged = { needed, vid, pid, mi ->
            onDriverNeedChanged?.invoke(needed, vid, pid, mi)
        }

        // 3. Configure ADB Bridge Manager
        adbBridgeManager.onAdbConnected = { name ->
            onTransportConnected?.invoke(ActiveTransport.USB_ADB, name)
            btServer.pause()
            syncUniversalPush()
        }
        adbBridgeManager.onAdbDisconnected = {
            onTransportDisconnected?.invoke(ActiveTransport.USB_ADB)
            syncUniversalPush()
        }
        adbBridgeManager.onInputReceived = { input -> onInputReceived?.invoke(input) }
        adbBridgeManager.isScanningPaused = {
            val current = getActiveTransport()
            current != ActiveTransport.NONE && current != ActiveTransport.USB_ADB
        }
        adbBridgeManager.startScanner(scope)

        // 4. Configure Bluetooth RFCOMM Server
        btServer.isExternalTransportActive = {
            val current = getActiveTransport()
            current != ActiveTransport.NONE && current != ActiveTransport.BLUETOOTH
        }
        btServer.onBtConnected = { name ->
            onTransportConnected?.invoke(ActiveTransport.BLUETOOTH, name)
            syncUniversalPush()
        }
        btServer.onBtDisconnected = {
            onTransportDisconnected?.invoke(ActiveTransport.BLUETOOTH)
            syncUniversalPush()
        }
        btServer.onInputReceived = { input -> onInputReceived?.invoke(input) }
        btServer.start(scope)

        // 5. Configure UDP Discovery Server (LAN broadcast)
        discoveryServer.isPaused = { false }

        // Launch background listeners
        scope.launch(Dispatchers.IO) { aoaManager.scanAndConnect() }
        scope.launch { udpServer?.start() }
        scope.launch { discoveryServer.start() }

        syncUniversalPush()
    }

    /**
     * Instantiates and configures a UdpServer instance.
     */
    private fun setupUdpServer(port: Int) {
        val server = UdpServer(
            port = port,
            onClientConnected = { name, connType ->
                val transport = if (connType == 2) ActiveTransport.USB_TETHERING else ActiveTransport.WIFI
                onTransportConnected?.invoke(transport, name)
                btServer.pause()
                syncUniversalPush()
            },
            onClientDisconnected = {
                onTransportDisconnected?.invoke(ActiveTransport.WIFI)
                onTransportDisconnected?.invoke(ActiveTransport.USB_TETHERING)
                syncUniversalPush()
            },
            onInputReceived = { input -> onInputReceived?.invoke(input) }
        ).apply {
            isExternalTransportActive = {
                val current = getActiveTransport()
                current != ActiveTransport.NONE && current != ActiveTransport.WIFI && current != ActiveTransport.USB_TETHERING
            }
        }
        udpServer = server
    }

    /**
     * Updates the streaming port across UDP and ADB transports without service disruption.
     */
    fun updateStreamingPort(newPort: Int, scope: CoroutineScope) {
        streamingPort = newPort
        adbBridgeManager.updatePort(newPort)
        scope.launch {
            try {
                udpServer?.stop()
                setupUdpServer(newPort)
                syncUniversalPush()
                udpServer?.start()
                println("📡 [ConnectionCoordinator] UDP server rebound to port $newPort")
            } catch (e: Exception) {
                println("⚠️ [ConnectionCoordinator] Failed to restart UDP on port $newPort: ${e.message}")
            }
        }
    }

    /**
     * Dispatches feedback (rumble & requested sensor mode) directly to the active transport.
     */
    suspend fun sendFeedback(feedback: GamepadFeedback) {
        val active = getActiveTransport()
        try {
            when (active) {
                ActiveTransport.WIFI, ActiveTransport.USB_TETHERING -> udpServer?.sendFeedback(feedback)
                ActiveTransport.USB_AOA -> aoaManager.sendFeedback(feedback)
                ActiveTransport.USB_ADB -> adbBridgeManager.sendFeedback(feedback)
                ActiveTransport.BLUETOOTH -> btServer.sendFeedback(feedback)
                ActiveTransport.NONE -> {}
            }
        } catch (e: Throwable) {
            println("⚠️ [ConnectionCoordinator] Failed to send feedback on $active: ${e.message}")
        }
    }

    /**
     * Synchronizes live transport references with UniversalPushManager.
     */
    fun syncUniversalPush() {
        UniversalPushManager.syncServers(
            transport = getActiveTransport(),
            udp = udpServer,
            aoa = aoaManager,
            bt = btServer
        )
    }

    /**
     * Gracefully stops and cleans up all transport resources.
     */
    fun stop() {
        UniversalPushManager.syncServers(
            transport = ActiveTransport.NONE,
            udp = null,
            aoa = null,
            bt = null
        )
        aoaManager.cleanup()
        btServer.stop()
        adbBridgeManager.stop()
        udpServer?.stop()
        udpServer = null
        discoveryServer.stop()
    }
}
