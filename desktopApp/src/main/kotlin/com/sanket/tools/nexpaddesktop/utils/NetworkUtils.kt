package com.sanket.tools.nexpaddesktop.utils

import java.net.Inet4Address
import java.net.NetworkInterface

object NetworkUtils {
    fun getLocalIpAddress(): String {
        try {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            while (interfaces.hasMoreElements()) {
                val networkInterface = interfaces.nextElement()
                // Ignore loopback, virtual adapters, and inactive interfaces
                if (networkInterface.isLoopback || !networkInterface.isUp || networkInterface.isVirtual) continue
                val addresses = networkInterface.inetAddresses
                while (addresses.hasMoreElements()) {
                    val addr = addresses.nextElement()
                    if (addr is Inet4Address) {
                        return addr.hostAddress
                    }
                }
            }
        } catch (e: Exception) {
            return "Unknown"
        }
        return "Unknown"
    }
}
