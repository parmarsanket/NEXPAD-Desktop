package com.sanket.tools.nexpaddesktop.driver.jna

import com.sun.jna.Structure

/**
 * JNA memory mapping for the ViGEm `XUSB_REPORT` native C structure.
 * 
 * This class defines the exact, byte-for-byte memory layout of an Xbox 360 controller's
 * hardware report. This structure is strictly 12 bytes long. It is serialized and passed
 * directly into the Windows kernel via `vigemclient.dll`.
 * 
 * ### C/C++ Equivalent (ViGEm/vigemclient.h)
 * ```c
 * typedef struct _XUSB_REPORT {
 *     USHORT wButtons;
 *     UCHAR bLeftTrigger;
 *     UCHAR bRightTrigger;
 *     SHORT sThumbLX;
 *     SHORT sThumbLY;
 *     SHORT sThumbRX;
 *     SHORT sThumbRY;
 * } XUSB_REPORT, *PXUSB_REPORT;
 * ```
 * 
 * ### Memory Layout (12 Bytes Total)
 * | Offset | Size | Type | Field Name | Description |
 * | :--- | :--- | :--- | :--- | :--- |
 * | 0 | 2 | `SHORT` | `wButtons` | 16-bit bitmask of pressed digital buttons. |
 * | 2 | 1 | `BYTE` | `bLeftTrigger` | Left analog trigger (0-255). |
 * | 3 | 1 | `BYTE` | `bRightTrigger` | Right analog trigger (0-255). |
 * | 4 | 2 | `SHORT` | `sThumbLX` | Left analog stick X-axis (-32768 to 32767). |
 * | 6 | 2 | `SHORT` | `sThumbLY` | Left analog stick Y-axis (-32768 to 32767). |
 * | 8 | 2 | `SHORT` | `sThumbRX` | Right analog stick X-axis (-32768 to 32767). |
 * | 10 | 2 | `SHORT` | `sThumbRY` | Right analog stick Y-axis (-32768 to 32767). |
 * 
 * **CRITICAL WARNING:**
 * 100% binary compatibility is required. Do not change the `@Structure.FieldOrder`, 
 * data types, or field names. Changing this layout will cause a fatal memory violation (Segfault) 
 * in the native driver due to misaligned memory reads.
 */
@Structure.FieldOrder(
    "wButtons",
    "bLeftTrigger",
    "bRightTrigger",
    "sThumbLX",
    "sThumbLY",
    "sThumbRX",
    "sThumbRY"
)
class XUSBReport : Structure() {
    
    /** 
     * Offset 0: A 16-bit bitmask defining the pressed state of all digital buttons. 
     * Corresponds to `USHORT wButtons` in C. 
     */
    @JvmField var wButtons: Short = 0
    
    /** 
     * Offset 2: Left analog trigger. 0 = released, 255 = fully pressed. 
     * Corresponds to `UCHAR bLeftTrigger` in C. 
     */
    @JvmField var bLeftTrigger: Byte = 0
    
    /** 
     * Offset 3: Right analog trigger. 0 = released, 255 = fully pressed. 
     * Corresponds to `UCHAR bRightTrigger` in C. 
     */
    @JvmField var bRightTrigger: Byte = 0
    
    /** 
     * Offset 4: Left analog thumbstick X-axis. -32768 (Left) to 32767 (Right). 
     * Corresponds to `SHORT sThumbLX` in C. 
     */
    @JvmField var sThumbLX: Short = 0
    
    /** 
     * Offset 6: Left analog thumbstick Y-axis. -32768 (Down) to 32767 (Up). 
     * Corresponds to `SHORT sThumbLY` in C. 
     */
    @JvmField var sThumbLY: Short = 0
    
    /** 
     * Offset 8: Right analog thumbstick X-axis. -32768 (Left) to 32767 (Right). 
     * Corresponds to `SHORT sThumbRX` in C. 
     */
    @JvmField var sThumbRX: Short = 0
    
    /** 
     * Offset 10: Right analog thumbstick Y-axis. -32768 (Down) to 32767 (Up). 
     * Corresponds to `SHORT sThumbRY` in C. 
     */
    @JvmField var sThumbRY: Short = 0

    companion object {
        // ---------------------------------------------------------------------
        // D-PAD Constants
        // ---------------------------------------------------------------------
        const val DPAD_UP: Short = 0x0001
        const val DPAD_DOWN: Short = 0x0002
        const val DPAD_LEFT: Short = 0x0004
        const val DPAD_RIGHT: Short = 0x0008

        // ---------------------------------------------------------------------
        // System / Menu Buttons
        // ---------------------------------------------------------------------
        const val START: Short = 0x0010
        const val BACK: Short = 0x0020
        const val GUIDE: Short = 0x0400
        
        // ---------------------------------------------------------------------
        // Thumbstick Clicks (L3 / R3)
        // ---------------------------------------------------------------------
        const val LEFT_THUMB: Short = 0x0040
        const val RIGHT_THUMB: Short = 0x0080

        // ---------------------------------------------------------------------
        // Shoulder Buttons (Bumpers / L1 & R1)
        // ---------------------------------------------------------------------
        const val LEFT_SHOULDER: Short = 0x0100
        const val RIGHT_SHOULDER: Short = 0x0200

        // ---------------------------------------------------------------------
        // Face Buttons
        // ---------------------------------------------------------------------
        const val A: Short = 0x1000
        const val B: Short = 0x2000
        const val X: Short = 0x4000
        
        /** 
         * The Y button uses the highest bit in a 16-bit signed short. 
         * `0x8000` evaluates to 32768, which overflows Java's signed 16-bit `Short`. 
         * Casting `-0x8000` (or `Short.MIN_VALUE`) produces the correct exact binary layout.
         */
        const val Y: Short = (-0x8000).toShort() 
    }
}
