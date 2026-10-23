package com.sanket.tools.nexpaddesktop.data.repository

import com.sanket.tools.nexpaddesktop.storage.DesktopSharedPreferences

/**
 * Domain repository contract for centralized network port management and reserved port validation.
 */
interface PortRepository {
    fun getStreamingPort(): Int
    fun saveStreamingPort(port: Int)
    fun isReservedPort(port: Int): Boolean
    fun isValidPort(port: Int): Boolean
}

class PortRepositoryImpl(
    private val prefs: DesktopSharedPreferences = DesktopSharedPreferences.getInstance()
) : PortRepository {

    override fun getStreamingPort(): Int = prefs.getStreamingPort()

    override fun saveStreamingPort(port: Int) = prefs.saveStreamingPort(port)

    override fun isReservedPort(port: Int): Boolean = port == 9995 || port == 9998

    override fun isValidPort(port: Int): Boolean = port in 1024..65535 && !isReservedPort(port)
}
