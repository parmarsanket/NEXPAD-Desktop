package com.sanket.tools.nexpaddesktop.driver

import com.sanket.tools.nexpaddesktop.model.GamepadInput

/**
 * Scalable contract interface for virtual gamepad drivers.
 * 
 * This interface bridges the networking layer to the native C drivers (e.g., ViGEmBus, vJoy).
 * Implementations are responsible for translating generic [GamepadInput] packets into 
 * hardware-specific binary reports (XUSB or DS4).
 * 
 * **Warning regarding Memory Ownership:**
 * Implementations *must* manage their own native pointers. Calling [connect] will allocate 
 * C-heap memory. The caller is strictly responsible for invoking [disconnect] when shutting down 
 * to free this memory; failing to do so will cause a memory leak and leave ghost devices attached to the OS.
 */
interface IGamepadDriver {
    
    /**
     * Allocates native memory and attaches the virtual controller to the Windows OS.
     * This will trigger the Windows "device connected" plug-and-play event.
     */
    fun connect()

    /**
     * Detaches the virtual controller from Windows and safely frees all native memory pointers.
     * Must be called during application shutdown or driver failure to prevent memory leaks.
     */
    fun disconnect()

    /**
     * Translates a generic network input state into a packed binary hardware report 
     * and submits it to the native bus.
     * 
     * @param input The generic state received over the network from the Android client.
     */
    fun updateInput(input: GamepadInput)

    /**
     * Forces the driver into a crashed state. Primarily used for unit testing 
     * network resilience and state recovery.
     */
    fun simulateCrash()

    /**
     * Queries the active state of the driver.
     * 
     * @return `true` if the device is currently attached to the OS and pointers are valid.
     */
    fun isDriverConnected(): Boolean
}
