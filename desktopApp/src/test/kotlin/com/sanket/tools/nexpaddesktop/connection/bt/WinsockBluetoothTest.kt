package com.sanket.tools.nexpaddesktop.connection.bt

import kotlin.test.Test
import kotlin.test.assertEquals

class WinsockBluetoothTest {

    private class TestSockaddrBth : WinsockBluetooth.SOCKADDR_BTH() {
        fun getOffset(name: String): Int = fieldOffset(name)
    }

    @Test
    fun testSockaddrBthLayoutMatchesWin32PragmaPack1() {
        val sockaddr = TestSockaddrBth()
        
        // Exact 30-byte size requirement matching Windows ws2bth.h with pack(1)
        assertEquals(30, sockaddr.size(), "SOCKADDR_BTH must be exactly 30 bytes packed")
        
        // Exact field offsets
        assertEquals(0, sockaddr.getOffset("addressFamily"), "addressFamily must be at offset 0")
        assertEquals(2, sockaddr.getOffset("btAddr"), "btAddr must be at offset 2")
        assertEquals(10, sockaddr.getOffset("serviceClassId"), "serviceClassId must be at offset 10")
        assertEquals(26, sockaddr.getOffset("port"), "port must be at offset 26")
    }
}
