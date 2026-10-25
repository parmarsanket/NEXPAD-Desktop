# 🤖 AGENTS.md — Machine-Readable Developer Guide for AI Agents

> This document defines architecture boundaries, system constraints, deterministic CLI commands, and code quality invariants for AI coding assistants (Google Antigravity, Claude Code, GitHub Copilot, Cursor, Devin, etc.) working on the **NEXPAD Desktop** codebase.

---

## 1. Project Context & Purpose

* **Repository**: `NEXPAD-Desktop`
* **Target Runtime**: Windows 10 / 11 (x64)
* **Language & Compiler**: Kotlin 2.x JVM, JDK 21 (Temurin / Corretto)
* **UI Framework**: Jetpack Compose Multiplatform Desktop (Skia graphics engine)
* **Native Drivers**:
  * `vigemclient.dll` (x64) via JNA (`net.java.dev.jna:jna:5.14.0`)
  * `libwdi.dll` (x64) for WinUSB driver elevation
  * `libusb4java` (1.3.0) for Android Open Accessory (AOA 2.0) USB bulk transfers

---

## 2. Strict Invariants & Constraints (Negative Rules)

AI agents working on this codebase **MUST ALWAYS** obey the following rules:

1. **Zero Suppression Guarantee**:
   * **STRICTLY ZERO** `@Suppress` and `@SuppressLint` annotations across all Kotlin source files.
   * Never suppress warnings or type errors. Fix the underlying types, null-safety, or imports properly.
2. **Fixed 200 Hz Polling Rate**:
   * The network UDP streaming and timer scheduling pipeline is strictly tuned for **200 Hz** ($5\,\text{ms}$ interval).
   * Do not alter the 200 Hz rate or introduce arbitrary sleep intervals in high-speed input pipelines.
3. **Zero Hardcoded Vendor ID (VID) Lists**:
   * AOA detection must remain 100% dynamic and protocol-driven.
   * Devices are identified through Microsoft PnP compatible IDs (`MS_COMP_MTP`, `WPD`) and standard USB descriptor interfaces (Class 6 / Subclass 1 / Protocol 1).
   * Proprietary OEM composite drivers are branched universally via `LIBUSB_ERROR_NOT_SUPPORTED (-12)` $\to$ root composite WinUSB.
   * Never hardcode brand names or static VID/PID tables.
4. **Deterministic Native Handle Lifecycle**:
   * Any `LibUsb.getDeviceList()` must be freed with `LibUsb.freeDeviceList(list, true)`.
   * Any referenced candidate must have `.unref()` called in a `finally` block to prevent Windows handle leaks.
5. **No External Driver Download Prompts**:
   * The NSIS installer (`installer.nsi`) bundles `redist/ViGEmBusSetup.exe`.
   * Never add instructions or UI prompting users to download ViGEmBus from external websites.
6. **Zero Telemetry Guarantee**:
   * The application is 100% offline and local subnet only.
   * Never introduce analytics SDKs, telemetry beacons, or external network requests.

---

## 3. Subsystem Map & Key Directories

```
NEXPADDesktop/desktopApp/src/main/kotlin/com/sanket/tools/nexpaddesktop/
├── app/                  # Application bootstrap, QoS priority, system tray, log redirection
├── connection/           # Transport arbitration & arbitration coordinator
│   ├── adb/              # ADB reverse Unix Domain Socket bridge (emulator-isolated)
│   ├── bt/               # Winsock2 Bluetooth RFCOMM 125Hz server
│   ├── usb/aoa/          # AOA 2.0 bulk streaming & dynamic PnP detector
│   ├── usb/winusb/       # JNA bindings for libwdi & dynamic WinUSB driver elevation
│   └── wifi/             # 200Hz non-blocking NIO UdpServer & LAN DiscoveryServer
├── driver/               # Kernel emulation layer
│   ├── dsu/              # CemuHook DSU motion server (UDP port 26760)
│   ├── jna/              # JNA bindings to vigemclient.dll & XUSBReport
│   └── vigem/            # ViGEmBus Xbox 360 & DualShock 4 mappers
├── plugins/              # NXPRC vector layout compiler & UniversalPushManager
├── ui/                   # Jetpack Compose Multiplatform UI, HUD visualizers, Glassmorphism
└── utils/                # NetworkUtils, AppLogger, WindowsElevation
```

---

## 4. Deterministic Shell Commands

Agents must execute these exact Gradle commands for building, compiling, and testing:

```powershell
# Compile Kotlin JVM sources for desktopApp
.\gradlew.bat :desktopApp:compileKotlin

# Compile shared library
.\gradlew.bat :shared:compileKotlinJvm

# Run Desktop Application in development
.\gradlew.bat :desktopApp:run

# Run Unit & Integration Tests
.\gradlew.bat :desktopApp:test

# Package Release Executable
.\gradlew.bat :desktopApp:packageReleaseExe

# Package Windows MSI Installer
.\gradlew.bat :desktopApp:packageMsi

# Compile All-in-One NSIS Setup (requires makensis.exe on PATH)
makensis installer.nsi
```

---

## 5. Architectural Standards

* **Single Active Transport Arbitration**:
  Managed centrally in [`ConnectionCoordinator.kt`](./desktopApp/src/main/kotlin/com/sanket/tools/nexpaddesktop/connection/ConnectionCoordinator.kt). Only one transport (`USB_AOA`, `USB_ADB`, `WIFI`, or `BLUETOOTH`) streams active gamepad reports at any given time.
* **CemuHook Motion Protocol**:
  Runs on standard port **`26760`** using standard DSU header structures (`0x44 0x53 0x55 0x53` = `DSUS`). Gyro quaternions and accelerometer coordinates are normalized in [`GyroProcessor.kt`](./desktopApp/src/main/kotlin/com/sanket/tools/nexpaddesktop/driver/GyroProcessor.kt).
* **Bi-directional Haptics**:
  XInput rumble values ($0\dots 65535$) received via `ViGEmClientLibrary` notification callbacks are downsampled to 8-bit motor strengths and dispatched to the active transport via `sendFeedback()`.
