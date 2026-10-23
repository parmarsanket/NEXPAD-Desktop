package com.sanket.tools.nexpaddesktop.connection

import com.sanket.tools.nexpad.model.GamepadFeedback
import com.sanket.tools.nexpad.model.GamepadInput
import com.sanket.tools.nexpaddesktop.plugins.UniversalPushManager
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class ConnectionCoordinatorTest {

    private var activeTransport = ActiveTransport.NONE
    private lateinit var coordinator: ConnectionCoordinator

    @Before
    fun setUp() {
        activeTransport = ActiveTransport.NONE
        coordinator = ConnectionCoordinator(
            streamingPort = 9999,
            getActiveTransport = { activeTransport }
        )
    }

    @After
    fun tearDown() {
        coordinator.stop()
    }

    @Test
    fun `initial state has expected ports and idle transport`() {
        assertEquals(ActiveTransport.NONE, activeTransport)
        assertNull(coordinator.udpServer)
    }

    @Test
    fun `UniversalPushManager synchronizes on syncUniversalPush`() {
        activeTransport = ActiveTransport.USB_AOA
        coordinator.syncUniversalPush()

        assertEquals(ActiveTransport.USB_AOA, UniversalPushManager.currentTransport)
        assertEquals(coordinator.aoaManager, UniversalPushManager.aoaManager)
    }

    @Test
    fun `sendFeedback executes safely when no active transport`() = runBlocking {
        val feedback = GamepadFeedback(leftMotorSpeed = 255, rightMotorSpeed = 128)
        // Should not throw or crash
        coordinator.sendFeedback(feedback)
    }

    @Test
    fun `stop resets UniversalPushManager servers`() {
        activeTransport = ActiveTransport.BLUETOOTH
        coordinator.syncUniversalPush()
        assertEquals(ActiveTransport.BLUETOOTH, UniversalPushManager.currentTransport)

        coordinator.stop()
        assertEquals(ActiveTransport.NONE, UniversalPushManager.currentTransport)
        assertNull(UniversalPushManager.udpServer)
        assertNull(UniversalPushManager.aoaManager)
        assertNull(UniversalPushManager.btServer)
    }

    @Test
    fun `onInputReceived callback dispatches gamepad input`() {
        var receivedInput: GamepadInput? = null
        coordinator.onInputReceived = { input ->
            receivedInput = input
        }

        val testInput = GamepadInput(btnA = true, leftStickX = 0.75f)
        coordinator.onInputReceived?.invoke(testInput)

        assertNotNull(receivedInput)
        val input = receivedInput!!
        assertTrue(input.btnA)
        assertEquals(0.75f, input.leftStickX, 0.001f)
    }
}
