package com.sanket.tools.nexpaddesktop.connection.wifi

import java.net.NetworkInterface

object NetworkUtils {
    fun getAvailableConnectionTypes(): List<Int> {
        val available = mutableSetOf<Int>()
        try {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            while (interfaces.hasMoreElements()) {
                val iface = interfaces.nextElement()
                if (!iface.isUp || iface.isLoopback) continue
                
                val name = iface.displayName?.lowercase() ?: iface.name.lowercase()
                
                // Check for Wi-Fi
                if (name.contains("wi-fi") || name.contains("wlan") || name.contains("wireless")) {
                    available.add(1)
                }
                // Check for USB Tethering (RNDIS / NDIS / USB / tethering)
                if (name.contains("usb") || name.contains("ndis") || name.contains("rndis") || name.contains("tethering")) {
                    available.add(2)
                }
                // Check for Bluetooth PAN
                if (name.contains("bluetooth") || name.contains("bthpan")) {
                    available.add(3)
                }
                
                // Fallback: check IP addresses if name isn't clear
                iface.inetAddresses.asSequence().forEach { addr ->
                    val ip = addr.hostAddress
                    if (ip.startsWith("192.168.42.") || ip.startsWith("192.168.137.")) {
                        available.add(2) // Standard USB tethering subnets
                    }
                }
            }
        } catch (e: Exception) {
            // Ignore
        }
        
        // If nothing matched, assume Wi-Fi is available if any non-loopback interface is up
        if (available.isEmpty()) {
            available.add(1)
        }
        
        return available.toList()
    }

    /**
     * Checks if a client IP address belongs to the subnet of an active USB Tethering (NDIS/RNDIS) interface on PC.
     */
    fun isUsbTetheringAddress(clientAddress: java.net.InetAddress): Boolean {
        try {
            val interfaces = NetworkInterface.getNetworkInterfaces() ?: return false
            while (interfaces.hasMoreElements()) {
                val iface = interfaces.nextElement()
                if (!iface.isUp || iface.isLoopback) continue
                val name = iface.displayName?.lowercase() ?: iface.name.lowercase()
                val isUsbIface = name.contains("usb") || name.contains("ndis") || name.contains("rndis") || name.contains("tethering")
                if (isUsbIface) {
                    for (ifAddr in iface.interfaceAddresses) {
                        val addr = ifAddr.address
                        val prefixLength = ifAddr.networkPrefixLength.toInt()
                        if (addr is java.net.Inet4Address && clientAddress is java.net.Inet4Address) {
                            if (isInSameSubnet(addr.address, clientAddress.address, prefixLength)) {
                                return true
                            }
                        }
                    }
                }
            }
        } catch (_: Exception) {}
        return false
    }

    private fun isInSameSubnet(addr1: ByteArray, addr2: ByteArray, prefixLength: Int): Boolean {
        if (addr1.size != 4 || addr2.size != 4) return false
        if (prefixLength <= 0) return true
        if (prefixLength > 32) return false

        var remainingBits = prefixLength
        for (i in 0 until 4) {
            if (remainingBits >= 8) {
                if (addr1[i] != addr2[i]) return false
                remainingBits -= 8
            } else if (remainingBits > 0) {
                val mask = (0xFF shl (8 - remainingBits)) and 0xFF
                if ((addr1[i].toInt() and mask) != (addr2[i].toInt() and mask)) return false
                remainingBits = 0
            } else {
                break
            }
        }
        return true
    }
}
