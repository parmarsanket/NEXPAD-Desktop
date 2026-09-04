package com.sanket.tools.nexpaddesktop.connection.usb.aoa

import com.sanket.tools.nexpad.model.GamepadFeedback
import com.sanket.tools.nexpad.model.GamepadInput
import com.sanket.tools.nexpad.protocol.NexpadProtocol
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import org.usb4java.DeviceHandle
import org.usb4java.LibUsb
import java.nio.ByteBuffer
import java.nio.IntBuffer
import java.util.concurrent.atomic.AtomicInteger

object AoaTransport {

    private const val BULK_READ_BUFFER_SIZE = 4096
    private const val FEEDBACK_BUFFER_SIZE = 10
    private const val READ_TIMEOUT_MS = 1000L
    private const val WRITE_TIMEOUT_MS = 100L
    private const val HEARTBEAT_FALLBACK_MS = 33L // ~30Hz fallback if no inputs arrive
    private const val STATS_WINDOW_PACKETS = 60

    /**
     * Borrows the DeviceHandle (opened by AoaManager). 
     * Claims the correct interface, reads/writes using the frame decoder, and releases the interface.
     * Does NOT close the DeviceHandle (AoaManager owns it).
     */
    suspend fun startBulkStreaming(
        handle: DeviceHandle,
        feedbackChannel: ReceiveChannel<GamepadFeedback>,
        onInputReceived: (GamepadInput) -> Unit
    ) = coroutineScope {
        val endpoints = AoaInterfaceDiscovery.discover(handle)
        if (endpoints == null) {
            println("[AOA/Transport] ERROR: Could not dynamically discover AOA endpoints!")
            return@coroutineScope
        }

        val interfaceNum = endpoints.interfaceNumber

        val claimResult = LibUsb.claimInterface(handle, interfaceNum)
        if (claimResult != LibUsb.SUCCESS) {
            println("[AOA/Transport] ERROR: Failed to claim interface $interfaceNum: ${LibUsb.errorName(claimResult)}")
            return@coroutineScope
        }
        println("[AOA/Transport] Claimed interface $interfaceNum. IN:${String.format("0x%02X", endpoints.bulkIn)} OUT:${String.format("0x%02X", endpoints.bulkOut)}")

        // Shared thread-safe metrics and echo state
        val latestEchoSequence = AtomicInteger(0)
        val latestLossPctByte = AtomicInteger(0)

        // Conflated channel to trigger immediate feedback echo (RTT ping) without blocking IN loop
        val triggerOutChannel = Channel<Unit>(Channel.CONFLATED)

        // Launch concurrent OUT loop to stream rumble and latency echoes back to phone
        val outJob = launch(Dispatchers.IO) {
            runOutLoop(
                handle = handle,
                bulkOutEndpoint = endpoints.bulkOut,
                feedbackChannel = feedbackChannel,
                triggerOutChannel = triggerOutChannel,
                latestEchoSequence = latestEchoSequence,
                latestLossPctByte = latestLossPctByte
            )
        }

        try {
            runInLoop(
                handle = handle,
                bulkInEndpoint = endpoints.bulkIn,
                triggerOutChannel = triggerOutChannel,
                latestEchoSequence = latestEchoSequence,
                latestLossPctByte = latestLossPctByte,
                onInputReceived = onInputReceived
            )
        } finally {
            // Cancel OUT loop immediately so coroutineScope doesn't hang indefinitely on disconnect
            outJob.cancel()
            LibUsb.releaseInterface(handle, interfaceNum)
            println("[AOA/Transport] Released interface $interfaceNum. Stream closed.")
            // NOTE: We do NOT LibUsb.close(handle) here! AoaManager is the owner!
        }
    }

    private suspend fun runOutLoop(
        handle: DeviceHandle,
        bulkOutEndpoint: Byte,
        feedbackChannel: ReceiveChannel<GamepadFeedback>,
        triggerOutChannel: Channel<Unit>,
        latestEchoSequence: AtomicInteger,
        latestLossPctByte: AtomicInteger
    ) = coroutineScope {
        val outBuffer = ByteBuffer.allocateDirect(FEEDBACK_BUFFER_SIZE)
        val outTransferred = IntBuffer.allocate(1)
        val feedbackBytes = ByteArray(NexpadProtocol.FEEDBACK_PACKET_SIZE)
        var lastFeedback = GamepadFeedback(0, 0)

        try {
            while (isActive) {
                // Wait for either an immediate trigger from the IN loop,
                // or the fallback timeout to send heartbeats/rumble if inputs cease.
                withTimeoutOrNull(HEARTBEAT_FALLBACK_MS) {
                    triggerOutChannel.receive()
                }

                // Drain any pending rumble updates from the app
                var nextFeedback = feedbackChannel.tryReceive().getOrNull()
                while (nextFeedback != null) {
                    lastFeedback = nextFeedback
                    nextFeedback = feedbackChannel.tryReceive().getOrNull()
                }

                NexpadProtocol.encodeFeedback(
                    feedback = lastFeedback,
                    echoSequenceNumber = latestEchoSequence.get(),
                    packetLossByte = latestLossPctByte.get().toByte(),
                    out = feedbackBytes,
                    offset = 0
                )

                outBuffer.clear()
                outBuffer.put(feedbackBytes, 0, NexpadProtocol.FEEDBACK_PACKET_SIZE)
                outBuffer.flip()
                outTransferred.clear()

                val result = LibUsb.bulkTransfer(handle, bulkOutEndpoint, outBuffer, outTransferred, WRITE_TIMEOUT_MS)
                if (result < 0 && result != LibUsb.ERROR_TIMEOUT) {
                    // Suppress timeout spam, break on actual fatal pipe/disconnect errors
                    break
                }
            }
        } catch (e: Exception) {
            // Cancellation or channel close
        }
    }

    private fun CoroutineScope.runInLoop(
        handle: DeviceHandle,
        bulkInEndpoint: Byte,
        triggerOutChannel: Channel<Unit>,
        latestEchoSequence: AtomicInteger,
        latestLossPctByte: AtomicInteger,
        onInputReceived: (GamepadInput) -> Unit
    ) {
        val buffer = ByteBuffer.allocateDirect(BULK_READ_BUFFER_SIZE)
        val transferred = IntBuffer.allocate(1)
        val rawReadBytes = ByteArray(BULK_READ_BUFFER_SIZE)

        val packetSize = NexpadProtocol.INPUT_PACKET_SIZE
        val decoder = AoaFrameDecoder(packetSize)
        var hasLoggedFirstPacket = false

        var lastSequenceNumber = Int.MIN_VALUE
        var lostInWindow = 0
        var receivedInWindow = 0
        var packetCount = 0

        while (isActive) {
            buffer.clear()
            transferred.clear()
            val result = LibUsb.bulkTransfer(handle, bulkInEndpoint, buffer, transferred, READ_TIMEOUT_MS)

            if (result == LibUsb.SUCCESS && transferred.get(0) > 0) {
                val bytesRead = transferred.get(0)
                buffer.get(rawReadBytes, 0, bytesRead)

                decoder.append(rawReadBytes, 0, bytesRead) { packetBuffer, packetOffset ->
                    val input = NexpadProtocol.decodeInput(packetBuffer, packetOffset) ?: return@append

                    if (!hasLoggedFirstPacket) {
                        println("[AOA/Transport] First valid packet received! Streaming is active.")
                        hasLoggedFirstPacket = true
                    }

                    packetCount = (packetCount % STATS_WINDOW_PACKETS) + 1
                    val delta = input.sequenceNumber - lastSequenceNumber
                    if (delta > 0 || lastSequenceNumber == Int.MIN_VALUE) {
                        if (lastSequenceNumber != Int.MIN_VALUE && delta > 1) {
                            lostInWindow += (delta - 1).coerceAtMost(255)
                        }
                        receivedInWindow++
                        lastSequenceNumber = input.sequenceNumber
                        latestEchoSequence.set(input.sequenceNumber)

                        if (packetCount % STATS_WINDOW_PACKETS == 0) {
                            val total = lostInWindow + receivedInWindow
                            val currentLossPct = if (total > 0) (255 * lostInWindow / total).coerceIn(0, 255) else 0
                            latestLossPctByte.set(currentLossPct)
                            lostInWindow = 0
                            receivedInWindow = 0
                        }

                        // Trigger immediate feedback echo for EVERY packet (Conflated channel avoids queueing)
                        triggerOutChannel.trySend(Unit)
                    }

                    onInputReceived(input)
                }
            } else if (result == LibUsb.ERROR_TIMEOUT) {
                // Normal timeout, keep polling
            } else if (result == LibUsb.ERROR_PIPE || result == LibUsb.ERROR_NO_DEVICE) {
                println("[AOA/Transport] Device disconnected (${LibUsb.errorName(result)}).")
                break
            } else if (result < 0) {
                println("[AOA/Transport] Bulk read error: ${LibUsb.errorName(result)}")
                break
            }
        }
    }
}
