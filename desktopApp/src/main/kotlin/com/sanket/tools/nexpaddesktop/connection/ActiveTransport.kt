package com.sanket.tools.nexpaddesktop.connection

enum class ActiveTransport(val displayName: String, val typeCode: Int) {
    NONE("None", 0),
    WIFI("Wi-Fi", 1),
    USB_AOA("USB (Direct)", 2),
    USB_ADB("USB (ADB)", 2),
    BLUETOOTH("Bluetooth", 3)
}
