package com.sanket.tools.nexpaddesktop.network

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
                // Check for USB Tethering (RNDIS / NDIS / USB / rndis / tethering)
                if (name.contains("usb") || name.contains("rndis") || name.contains("ndiswan") || name.contains("tethering")) {
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
}
