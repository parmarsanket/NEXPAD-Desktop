package com.sanket.tools.nexpaddesktop.driver.jna

import com.sun.jna.Structure

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
    @JvmField var wButtons: Short = 0
    @JvmField var bLeftTrigger: Byte = 0
    @JvmField var bRightTrigger: Byte = 0
    @JvmField var sThumbLX: Short = 0
    @JvmField var sThumbLY: Short = 0
    @JvmField var sThumbRX: Short = 0
    @JvmField var sThumbRY: Short = 0

    companion object {
        const val DPAD_UP: Short = 0x0001
        const val DPAD_DOWN: Short = 0x0002
        const val DPAD_LEFT: Short = 0x0004
        const val DPAD_RIGHT: Short = 0x0008
        const val START: Short = 0x0010
        const val BACK: Short = 0x0020
        const val LEFT_THUMB: Short = 0x0040
        const val RIGHT_THUMB: Short = 0x0080
        const val LEFT_SHOULDER: Short = 0x0100
        const val RIGHT_SHOULDER: Short = 0x0200
        const val GUIDE: Short = 0x0400
        const val A: Short = 0x1000
        const val B: Short = 0x2000
        const val X: Short = 0x4000
        const val Y: Short = (-0x8000).toShort() // 0x8000 overflows signed short
    }
}
