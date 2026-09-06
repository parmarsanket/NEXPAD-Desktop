package com.sanket.tools.nexpaddesktop.connection.bt

import com.sun.jna.Library
import com.sun.jna.Native
import com.sun.jna.Pointer
import com.sun.jna.Structure
import com.sun.jna.platform.win32.Guid.GUID
import com.sun.jna.ptr.IntByReference
import com.sun.jna.win32.W32APIOptions

/**
 * Native Winsock2 (ws2_32.dll) Bluetooth bindings via JNA.
 * Provides user-mode Bluetooth Classic RFCOMM server sockets on Windows
 * without requiring any 3rd-party drivers or C++ DLLs.
 */
object WinsockBluetooth {

    const val AF_BTH = 32
    const val SOCK_STREAM = 1
    const val BTHPROTO_RFCOMM = 3
    const val BT_PORT_ANY = -1 // (ULONG)-1 (0xFFFFFFFF)
    const val NS_BTH = 16
    const val RNRSERVICE_REGISTER = 0
    const val RNRSERVICE_DEREGISTER = 1
    const val INVALID_SOCKET = -1L
    const val SOCKET_ERROR = -1

    // Common Winsock error codes
    const val WSAENETDOWN = 10050 // Bluetooth radio is off/disabled
    const val WSAEINTR = 10004
    const val WSAEWOULDBLOCK = 10035
    const val WSAECONNRESET = 10054
    const val WSAESHUTDOWN = 10058

    // SO_RCVTIMEO / SO_SNDTIMEO / SO_RCVBUF / SO_SNDBUF
    const val SOL_SOCKET = 0xFFFF
    const val SO_SNDBUF = 0x1001
    const val SO_RCVBUF = 0x1002
    const val SO_SNDTIMEO = 0x1005
    const val SO_RCVTIMEO = 0x1006

    @Structure.FieldOrder("wVersion", "wHighVersion", "szDescription", "szSystemStatus", "iMaxSockets", "iMaxUdpDg", "lpVendorInfo")
    open class WSADATA : Structure() {
        @JvmField var wVersion: Short = 0
        @JvmField var wHighVersion: Short = 0
        @JvmField var szDescription: ByteArray = ByteArray(257)
        @JvmField var szSystemStatus: ByteArray = ByteArray(129)
        @JvmField var iMaxSockets: Short = 0
        @JvmField var iMaxUdpDg: Short = 0
        @JvmField var lpVendorInfo: Pointer? = null
    }

    /**
     * Win32 SOCKADDR_BTH (Pack = 1, exactly 30 bytes in Windows SDK):
     * USHORT   addressFamily (2 bytes)
     * BTH_ADDR btAddr (8 bytes)
     * GUID     serviceClassId (16 bytes)
     * ULONG    port (4 bytes)
     * Total = 30 bytes.
     */
    @Structure.FieldOrder("addressFamily", "btAddr", "serviceClassId", "port")
    open class SOCKADDR_BTH : Structure(ALIGN_NONE) {
        @JvmField var addressFamily: Short = AF_BTH.toShort()
        @JvmField var btAddr: Long = 0L
        @JvmField var serviceClassId: GUID = GUID()
        @JvmField var port: Int = BT_PORT_ANY

        companion object {
            val SIZE: Int = SOCKADDR_BTH().size()
        }
    }

    @Structure.FieldOrder("lpSockaddr", "iSockaddrLength")
    open class SOCKET_ADDRESS : Structure() {
        @JvmField var lpSockaddr: Pointer? = null
        @JvmField var iSockaddrLength: Int = 0
    }

    @Structure.FieldOrder("LocalAddr", "RemoteAddr", "iSocketType", "iProtocol")
    open class CSADDR_INFO : Structure() {
        @JvmField var LocalAddr: SOCKET_ADDRESS = SOCKET_ADDRESS()
        @JvmField var RemoteAddr: SOCKET_ADDRESS = SOCKET_ADDRESS()
        @JvmField var iSocketType: Int = SOCK_STREAM
        @JvmField var iProtocol: Int = BTHPROTO_RFCOMM
    }

    @Structure.FieldOrder(
        "dwSize",
        "lpszServiceInstanceName",
        "lpServiceClassId",
        "lpVersion",
        "lpszComment",
        "dwNameSpace",
        "lpNSProviderId",
        "lpszContext",
        "dwNumberOfProtocols",
        "lpafpProtocols",
        "lpszQueryString",
        "dwNumberOfCsAddrs",
        "lpcsaBuffer",
        "dwOutputFlags",
        "lpBlob"
    )
    open class WSAQUERYSETW : Structure() {
        @JvmField var dwSize: Int = 0
        @JvmField var lpszServiceInstanceName: com.sun.jna.WString? = null
        @JvmField var lpServiceClassId: Pointer? = null
        @JvmField var lpVersion: Pointer? = null
        @JvmField var lpszComment: com.sun.jna.WString? = null
        @JvmField var dwNameSpace: Int = NS_BTH
        @JvmField var lpNSProviderId: Pointer? = null
        @JvmField var lpszContext: com.sun.jna.WString? = null
        @JvmField var dwNumberOfProtocols: Int = 0
        @JvmField var lpafpProtocols: Pointer? = null
        @JvmField var lpszQueryString: com.sun.jna.WString? = null
        @JvmField var dwNumberOfCsAddrs: Int = 0
        @JvmField var lpcsaBuffer: Pointer? = null
        @JvmField var dwOutputFlags: Int = 0
        @JvmField var lpBlob: Pointer? = null
    }

    interface Ws2_32 : Library {
        fun WSAStartup(wVersionRequested: Short, lpWSAData: WSADATA): Int
        fun WSACleanup(): Int
        fun socket(af: Int, type: Int, protocol: Int): Long
        fun closesocket(s: Long): Int
        fun bind(s: Long, name: SOCKADDR_BTH, namelen: Int): Int
        fun getsockname(s: Long, name: SOCKADDR_BTH, namelen: IntByReference): Int
        fun listen(s: Long, backlog: Int): Int
        fun accept(s: Long, addr: SOCKADDR_BTH, addrlen: IntByReference): Long
        fun recv(s: Long, buf: ByteArray, len: Int, flags: Int): Int
        fun send(s: Long, buf: ByteArray, len: Int, flags: Int): Int
        fun WSAGetLastError(): Int
        fun WSASetServiceW(lpqsRegInfo: WSAQUERYSETW, essOperation: Int, dwControlFlags: Int): Int
        fun setsockopt(s: Long, level: Int, optname: Int, optval: Pointer, optlen: Int): Int
    }

    @Structure.FieldOrder(
        "dwSize", "Address", "ulClassofDevice", "fConnected", "fRemembered", "fAuthenticated",
        "stLastSeen", "stLastUsed", "szName"
    )
    open class BLUETOOTH_DEVICE_INFO : Structure() {
        @JvmField var dwSize: Int = 0
        @JvmField var Address: Long = 0L
        @JvmField var ulClassofDevice: Int = 0
        @JvmField var fConnected: Int = 0
        @JvmField var fRemembered: Int = 0
        @JvmField var fAuthenticated: Int = 0
        @JvmField var stLastSeen: com.sun.jna.platform.win32.WinBase.SYSTEMTIME = com.sun.jna.platform.win32.WinBase.SYSTEMTIME()
        @JvmField var stLastUsed: com.sun.jna.platform.win32.WinBase.SYSTEMTIME = com.sun.jna.platform.win32.WinBase.SYSTEMTIME()
        @JvmField var szName: CharArray = CharArray(248)

        init {
            dwSize = size()
        }
    }

    interface BthProps : Library {
        fun BluetoothGetDeviceInfo(hRadio: Pointer?, pbtdi: BLUETOOTH_DEVICE_INFO): Int
    }

    val BTH_PROPS: BthProps? by lazy {
        try {
            Native.load("bthprops.cpl", BthProps::class.java, W32APIOptions.UNICODE_OPTIONS)
        } catch (_: Throwable) {
            try {
                Native.load("bluetoothapis", BthProps::class.java, W32APIOptions.UNICODE_OPTIONS)
            } catch (_: Throwable) {
                null
            }
        }
    }

    val INSTANCE: Ws2_32 by lazy {
        Native.load("ws2_32", Ws2_32::class.java, W32APIOptions.UNICODE_OPTIONS)
    }
}
