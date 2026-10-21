package com.sanket.tools.nexpaddesktop.connection.usb.winusb

import java.io.File

object WinUsbDriverManager {
    
    private fun installForDevice(vidHex: String, pidHex: String, miStr: String): Int {
        try {
            val cleanVid = vidHex.trim().uppercase()
            val cleanPid = pidHex.trim().uppercase()
            val isComposite = miStr.isNotBlank() && !miStr.equals("none", ignoreCase = true)
            val formattedMi = if (isComposite) {
                miStr.trim().toIntOrNull()?.let { String.format("%02X", it) } ?: miStr.trim()
            } else ""

            val deviceId = if (isComposite) {
                "USB\\VID_$cleanVid&PID_$cleanPid&MI_$formattedMi"
            } else {
                "USB\\VID_$cleanVid&PID_$cleanPid"
            }
            
            val safeId = deviceId.replace("\\", "_").replace("&", "_")
            val tmpDir = File(System.getProperty("java.io.tmpdir"))
            val infFolder = File(tmpDir, "nexpad_usb_${safeId}_${System.nanoTime()}")
            val infPath = infFolder.absolutePath
            infFolder.mkdirs()
            
            val targetDevice = LibWdiBinding.wdi_device_info().apply {
                this.vid = cleanVid.toInt(16).toShort()
                this.pid = cleanPid.toInt(16).toShort()
                this.device_id = deviceId
                this.hardware_id = deviceId
                this.desc = "NEXPAD USB Device"
                this.is_composite = if (isComposite) 1 else 0
                this.mi = if (isComposite) (miStr.trim().toIntOrNull()?.toByte() ?: 0) else 0
            }
            println("[WinUSB/Driver] Forged Target Hardware ID: ${targetDevice.hardware_id}")
            
            try {
                val optionsPrepare = LibWdiBinding.wdi_options_prepare().apply {
                    this.driver_type = LibWdiBinding.WDI_WINUSB
                    this.vendor_name = "NEXPAD"
                }
                
                println("[WinUSB/Driver] Preparing driver at $infPath...")
                val prepRes = LibWdiBinding.INSTANCE.wdi_prepare_driver(
                    targetDevice, infPath, "nexpad.inf", optionsPrepare
                )
                
                if (prepRes != 0) {
                    val errDesc = try { LibWdiBinding.INSTANCE.wdi_strerror(prepRes) } catch (_: Throwable) { "$prepRes" }
                    println("[WinUSB/Driver] ERROR: wdi_prepare_driver failed: $prepRes ($errDesc)")
                    return 1
                }
                
                val optionsInstall = LibWdiBinding.wdi_options_install()
                println("[WinUSB/Driver] Installing WinUSB driver...")
                val installRes = LibWdiBinding.INSTANCE.wdi_install_driver(
                    targetDevice, infPath, "nexpad.inf", optionsInstall
                )
                
                if (installRes != 0) {
                    val errDesc = try { LibWdiBinding.INSTANCE.wdi_strerror(installRes) } catch (_: Throwable) { "$installRes" }
                    println("[WinUSB/Driver] ERROR: wdi_install_driver failed: $installRes ($errDesc)")
                    return 1
                }
                
                println("[WinUSB/Driver] SUCCESS: WinUSB driver installed for $deviceId.")
                return 0
            } catch (e: Exception) {
                println("[WinUSB/Driver] ERROR during driver operations: ${e.message}")
                return 1
            } finally {
                try {
                    infFolder.deleteRecursively()
                } catch (_: Exception) {}
            }
        } catch (e: Exception) {
            println("[WinUSB/Driver] ERROR: ${e.message}")
            return 1
        }
    }

    /**
     * Installs the WinUSB driver dynamically for any target phone.
     * Works with standard and custom vendor IDs.
     * - Scopes composite devices to the specific MTP interface (MI_00)
     * - Automatically pre-installs WinUSB for standard Google AOA Accessory (18D1:2D00)
     */
    fun installWinUsb(vidHex: String, pidHex: String, miStr: String): Int {
        val cleanVid = vidHex.trim().uppercase()
        val cleanPid = pidHex.trim().uppercase()
        if (cleanVid.isBlank() || cleanPid.isBlank()) {
            println("[WinUSB/Driver] ERROR: Cannot install WinUSB: VID or PID is empty.")
            return 1
        }

        try {
            cleanVid.toInt(16).toShort()
        } catch (e: Exception) {
            println("[WinUSB/Driver] ERROR: Malformed VID '$cleanVid': ${e.message}")
            return 1
        }

        println("[WinUSB/Driver] Elevating to install WinUSB for VID:$cleanVid PID:$cleanPid MI:$miStr...")
        val res = installForDevice(cleanVid, cleanPid, miStr)
        if (res != 0) return res

        // Pre-install WinUSB for Google AOA Accessory (18D1:2D00)
        // so Phase 2 AOA accessory re-enumeration succeeds without requiring a second elevation prompt!
        if (!cleanVid.equals("18D1", ignoreCase = true)) {
            val isAoaDriverPresent = isAoaDriverStorePresent()
            if (!isAoaDriverPresent) {
                println("[WinUSB/Driver] Pre-installing WinUSB for Google AOA Accessory (VID:18D1 PID:2D00)...")
                try { installForDevice("18D1", "2D00", "none") } catch (_: Exception) {}
            } else {
                println("[WinUSB/Driver] WinUSB for Google AOA Accessory (18D1:2D00) is already present in Windows driver store.")
            }
        }
        return 0
    }

    private fun isAoaDriverStorePresent(): Boolean {
        return try {
            val proc = ProcessBuilder("pnputil.exe", "/enum-drivers")
                .redirectErrorStream(true)
                .start()
            try { proc.outputStream.close() } catch (_: Throwable) {}
            val text = try {
                proc.inputStream.bufferedReader().use { it.readText() }
            } finally {
                if (!proc.waitFor(3, java.util.concurrent.TimeUnit.SECONDS)) {
                    proc.destroyForcibly()
                } else {
                    proc.destroy()
                }
            }
            text.contains("18D1&PID_2D00", ignoreCase = true)
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Reverts third-party WinUSB driver packages installed by NEXPAD for a device,
     * restoring the default Microsoft Windows driver (e.g. MTP USB Device / WPD).
     */
    fun revertWinUsb(vidHex: String, pidHex: String): Int {
        val cleanVid = vidHex.trim().uppercase()
        val cleanPid = pidHex.trim().uppercase()
        if (cleanVid.isBlank() || cleanPid.isBlank()) {
            println("[WinUSB/Driver] ERROR: Cannot revert driver: VID or PID is empty.")
            return 1
        }

        println("[WinUSB/Driver] Rollback: Reverting WinUSB driver for VID:$cleanVid PID:$cleanPid...")
        var uninstalledCount = 0

        try {
            val proc = ProcessBuilder("pnputil.exe", "/enum-drivers")
                .redirectErrorStream(true)
                .start()
            try { proc.outputStream.close() } catch (_: Throwable) {}
            val text = try {
                proc.inputStream.bufferedReader().use { it.readText() }
            } finally {
                if (!proc.waitFor(5, java.util.concurrent.TimeUnit.SECONDS)) {
                    proc.destroyForcibly()
                } else {
                    proc.destroy()
                }
            }

            val blocks = text.split("Published Name:")
            for (block in blocks) {
                val lower = block.lowercase()
                val isNexpadDriver = lower.contains("nexpad")
                if (isNexpadDriver) {
                    val publishedName = block.lines().firstOrNull()?.trim()
                    if (!publishedName.isNullOrBlank() && publishedName.endsWith(".inf", ignoreCase = true)) {
                        println("[WinUSB/Driver] Found NEXPAD driver package: $publishedName. Deleting and uninstalling...")
                        val delProc = ProcessBuilder("pnputil.exe", "/delete-driver", publishedName, "/uninstall", "/force")
                            .redirectErrorStream(true)
                            .start()
                        try { delProc.outputStream.close() } catch (_: Throwable) {}
                        val delOutput = try {
                            delProc.inputStream.bufferedReader().use { it.readText() }
                        } finally {
                            if (!delProc.waitFor(5, java.util.concurrent.TimeUnit.SECONDS)) {
                                delProc.destroyForcibly()
                            } else {
                                delProc.destroy()
                            }
                        }
                        println("[WinUSB/Driver] Delete result for $publishedName: ${delOutput.trim()}")
                        uninstalledCount++
                    }
                }
            }

            println("[WinUSB/Driver] Triggering PnP hardware re-enumeration...")
            try {
                val scanP = ProcessBuilder("pnputil.exe", "/scan-devices")
                    .redirectOutput(ProcessBuilder.Redirect.DISCARD)
                    .redirectError(ProcessBuilder.Redirect.DISCARD)
                    .start()
                try { scanP.outputStream.close() } catch (_: Throwable) {}
                if (!scanP.waitFor(3, java.util.concurrent.TimeUnit.SECONDS)) {
                    scanP.destroyForcibly()
                } else {
                    scanP.destroy()
                }
            } catch (_: Exception) {}

            println("[WinUSB/Driver] Rollback completed. (Uninstalled $uninstalledCount driver packages). Default Windows driver will bind.")
            return 0
        } catch (e: Exception) {
            println("[WinUSB/Driver] ERROR during driver rollback: ${e.message}")
            return 1
        }
    }
}
