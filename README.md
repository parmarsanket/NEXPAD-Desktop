# 🖥️ NEXPAD Desktop — Virtual Controller Server & Studio

[![Platform](https://img.shields.io/badge/Platform-Windows%2010%20%2F%2011%20(x64)-0078D6.svg)](https://microsoft.com/windows)
[![UI Framework](https://img.shields.io/badge/UI-Compose%20Multiplatform%20(Desktop)-4285F4.svg)](https://www.jetbrains.com/lp/compose-multiplatform/)
[![Windows Driver](https://img.shields.io/badge/Driver-ViGEmBus%20Kernel%20Emulation-purple.svg)](https://github.com/nefarius/ViGEmBus)
[![Polling Rate](https://img.shields.io/badge/Input%20Rate-1000Hz%20(Sub--millisecond)-3FD25A.svg)](#-multi-transport-server-engine)
[![License: Apache 2.0](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](./LICENSE)
[![Zero Telemetry](https://img.shields.io/badge/Privacy-100%25%20Local%20%7C%200%20Telemetry-brightgreen.svg)](#-privacy-driver-safety--security-transparency)
[![Code Quality](https://img.shields.io/badge/Code%20Standard-Zero%20%40Suppress%20Guarantee-purple.svg)](#-developer--contributor-guide)

**NEXPAD Desktop** is an ultra-high performance Windows companion server and controller authoring studio built with **Compose Multiplatform (Desktop)** and **JNA (Java Native Access)**.

It seamlessly bridges your Android smartphone to Windows, injecting high-speed gamepad inputs directly into the Windows Kernel via **ViGEmBus** to emulate a genuine physical **Xbox 360 controller**. It provides out-of-the-box compatibility with all PC games, competitive low-latency USB and Wi-Fi streaming, bidirectional force-feedback rumble, and integrated **CemuHook DSU motion controls** for console emulators.

---

## 🧭 Subsystem Navigation

```
                    ┌────────────────────────────┐
                    │   NEXPADDesktop/README.md  │
                    │       (Windows Server)     │
                    └─────────────┬──────────────┘
                                  │
          ┌───────────────────────┼───────────────────────┐
          ▼                       ▼                       ▼
     HISTORY.md              ROADMAP.md            ARCHITECTURE.md
   "Where We Came From"    "Where We're Going"      "How It Works"
```

- 📖 **[`HISTORY.md`](./HISTORY.md)** — Chronological record of all 32 development branches from Master Genesis to the Universal Timeline Track Studio.
- 🗺️ **[`ROADMAP.md`](./ROADMAP.md)** — Forward-looking development milestones, MSIX packaging, and DualSense haptic synthesizer.
- 🏛️ **[`ARCHITECTURE.md`](./ARCHITECTURE.md)** — Deep technical specification of the JNA ViGEmClient C++ kernel driver binding, WinUSB AOA bulk endpoints, and NIO UdpServer.
- 📜 **[`GRADLE_COMMANDS.md`](./GRADLE_COMMANDS.md)** — Quick reference cheat-sheet for running, testing, building, and packaging the desktop application.
- ⚖️ **[`DISCLAIMER.md`](./DISCLAIMER.md)** — Nominative Fair Use, driver safety, and legal non-affiliation disclosure.
- 📄 **[`NOTICE`](./NOTICE)** — Third-party open-source attributions (ViGEmClient, JNA, libwdi, JetBrains Compose).

---

## 🚀 Key Capabilities & Architecture

### 1. 🎮 Kernel-Level Xbox 360 Emulation (`ViGEmBus`)
- Uses low-level **Java Native Access (JNA)** bindings to communicate directly with `vigemclient.dll` and the **Virtual Gamepad Emulation Bus (ViGEmBus)** kernel driver.
- Dispatches raw 16-bit binary reports (`XUSBReport`) directly into the Windows driver subsystem:
  - 10 digital buttons (A, B, X, Y, LB, RB, Back, Start, LS Click, RS Click).
  - 4-way D-Pad states (Up, Down, Left, Right).
  - Dual analog triggers ($0\dots 255$ 8-bit unsigned integer resolution).
  - Dual 2D analog thumbsticks ($-32768\dots 32767$ 16-bit signed integer resolution).
- **100% Native Game Compatibility**: Recognized immediately by Windows as a genuine physical Xbox 360 gamepad across:
  - **Steam**, **Xbox Game Pass (PC)**, **Epic Games Store**, **EA App**, **Ubisoft Connect**, and **GOG Galaxy**.
  - All PC AAA titles (*Forza Horizon*, *Rocket League*, *Grand Theft Auto V*, *Cyberpunk 2077*, *Elden Ring*, *Call of Duty*).

### 2. ⚡ Multi-Transport Server Pipeline
- **Zero-Driver USB (AOA)**: Communicates directly with Android Open Accessory endpoints over WinUSB bulk streams with sub-millisecond ($<1\,\text{ms}$) hardware response times.
- **1000Hz Asynchronous NIO UDP Server**: Non-blocking UDP socket pipeline receiving 44-byte binary packets from the phone and streaming 8-byte bidirectional motor speed vibration packets (`GamepadFeedback`).
- **ADB Reverse TCP Tunnel**: Fallback port forwarding bridge over standard Android Debug Bridge (`127.0.0.1:9999`).
- **Bluetooth RFCOMM Serial Bridge**: Wireless connection over Windows Bluetooth serial ports.

### 3. 🎯 CemuHook DSU Motion Server
- Integrated UDP motion server running on standard port **`26760`**.
- Receives 6-axis gyroscope and accelerometer orientation vectors from NEXPAD Mobile and broadcasts standard DSU protocol packets.
- Provides native motion steering and tilt aiming in all modern console emulators:
  - **Cemu** (Wii U)
  - **Yuzu** & **Ryujinx** (Nintendo Switch)
  - **RPCS3** (PlayStation 3)
  - **Dolphin** (Wii & GameCube)

### 4. 📳 Bidirectional Force-Feedback Game Rumble
- Intercepts rumble vibration commands dispatched by PC games through the XInput API.
- Captures high-frequency (small motor) and low-frequency (large motor) vibration strengths.
- Encodes motor values into real-time 8-byte feedback packets transmitted back to the smartphone to drive tactile haptic actuators.

### 5. 🎨 Modern Cyberpunk Glassmorphic UI
- Built with **Compose Multiplatform (Desktop)** powered by the Skia graphics engine.
- Displays live connection telemetry, round-trip time (RTT) ping latency, incoming packet counters, driver health state, and active rumble motor indicators.
- One-click transport switcher and real-time port configuration.

---

## 📥 Multi-Platform Distribution & Downloads

NEXPAD Desktop is distributed through multiple official channels:

### Option A: Microsoft Store *(Coming Soon)*
1. Open the **Microsoft Store** on Windows 10 or 11.
2. Search for **NEXPAD Desktop**.
3. Click **Get / Install** for an automatic, sandboxed MSIX installation with background updates.

### Option B: All-in-One Windows Setup (`NEXPAD_Setup.exe`)
1. Download the latest **`NEXPAD_Setup.exe`** installer from [GitHub Releases](https://github.com/).
2. Run the installer. The setup wizard will:
   - Install the NEXPAD Desktop application to your Program Files.
   - Automatically detect and prompt to install the official **ViGEmBus** driver if not already present.
   - Create Start Menu and Desktop shortcuts.

### Option C: Standalone MSI / Portable ZIP
1. Download `NEXPAD-Desktop.msi` or the portable `.zip` archive from [GitHub Releases](https://github.com/).
2. Extract or install without requiring administrative elevation (portable mode).

---

## 🕹️ User Quick Start Guide (4 Simple Steps)

### Step 1: Install ViGEmBus Driver
NEXPAD relies on the open-source **ViGEmBus** driver to emulate physical controllers in Windows:
- If you ran `NEXPAD_Setup.exe`, this was installed automatically.
- Otherwise, download and run the official installer from [ViGEmBus Releases](https://github.com/nefarius/ViGEmBus/releases).

### Step 2: Launch NEXPAD Desktop
Run NEXPAD Desktop from your Start Menu.
- The status indicator should display **"Driver Status: READY (ViGEmBus Connected)"**.
- The server will immediately begin listening for incoming connections on UDP port `9999` and monitor USB endpoints.

### Step 3: Connect NEXPAD from Your Phone
- **USB (AOA)**: Plug your phone into your PC via USB-C and select "Connect USB (AOA)" in the mobile app.
- **Wi-Fi**: Check the local IP address displayed on NEXPAD Desktop (e.g., `192.168.1.150`). Enter this IP in the mobile app and tap "Connect".

### Step 4: Play Your PC Games!
Windows will chime and register an **Xbox 360 Controller for Windows**. Launch Steam, Game Pass, or any game, and your phone acts as your gamepad!

---

## 🛠️ Developer & Contributor Guide

### Technical Stack & Dependencies
- **Language**: Kotlin 2.x JVM
- **UI Framework**: Compose Multiplatform Desktop (Skia rendering)
- **Native Interop**: JNA (`net.java.dev.jna:jna:5.14.0`)
- **Native Driver**: `vigemclient.dll` (x64 Windows dynamic library)
- **USB Subsystem**: `libwdi.dll` & WinUSB bulk transfer endpoints
- **Networking**: Java NIO asynchronous non-blocking sockets

### Source Organization
```
NEXPADDesktop/desktopApp/src/main/
├── kotlin/com/sanket/tools/nexpaddesktop/
│   ├── connection/       # AOA USB stream handler, UdpServer (1000Hz), CemuHook DSU server
│   ├── driver/           # JNA bindings: ViGEmClient, ViGEmBus, XUSBReport
│   ├── model/            # Desktop telemetry models, client connection states
│   ├── plugins/          # NXPRC vector plugin parser and desktop asset compiler
│   ├── ui/               # Compose Multiplatform UI, telemetry monitors, glassmorphic HUD
│   ├── utils/            # Network address discovery, Win32 registry utilities
│   ├── viewmodel/        # DesktopMainViewModel, DriverViewModel, ServerViewModel
│   └── main.kt           # Desktop application entry point & window frame
└── resources/
    ├── fonts/            # Plus Jakarta Sans & Space Grotesk typography
    ├── images/           # UI vector icons and branding assets
    └── win32-x86-64/     # vigemclient.dll, libwdi.dll native binaries
```

### Strict Code Quality Guarantee
- **Zero Suppression Rule**: Strictly **0 `@Suppress`** and **0 `@SuppressLint`** across all Kotlin JVM sources.
- **Thread Safety**: All incoming 1000Hz network packets are processed in lock-free ring buffers before dispatching to JNA kernel memory.

### Building from Source

```powershell
# Clone the repository
git clone https://github.com/parmarsanket/nexpad.git
cd nexpad/NEXPADDesktop

# Compile Kotlin JVM sources
.\gradlew.bat :desktopApp:compileKotlin

# Run the Desktop Application in development mode
.\gradlew.bat :desktopApp:run

# Run automated tests
.\gradlew.bat :desktopApp:test

# Package Standalone Windows MSI installer
.\gradlew.bat :desktopApp:packageMsi

# Compile NSIS All-in-One Setup (requires NSIS installed)
makensis installer.nsi
```

---

## 🔒 Privacy, Driver Safety & Security Transparency

NEXPAD Desktop is built with complete transparency:
- **Zero Telemetry**: Collects zero analytics, crash reports, or personal identifiers.
- **100% Local Processing**: All server sockets listen strictly on your local subnet. No network traffic ever leaves your home network.
- **Why ViGEmBus Needs Driver Privileges**: The ViGEmBus driver operates as a legitimate Windows Kernel-Mode Driver Framework (KMDF) device driver. It is required to tell the Windows Operating System that a real physical gamepad hardware device is connected. NEXPAD communicates with this driver entirely through standard user-mode APIs (`vigemclient.dll`).

For full disclosures, read [`DISCLAIMER.md`](./DISCLAIMER.md).

---

## ⚖️ Legal Disclaimer & Nominative Fair Use

- **Non-Affiliation**: NEXPAD Desktop is an independent open-source project and is not affiliated with, sponsored by, or endorsed by Microsoft Corporation, Sony Interactive Entertainment, Nintendo Co., Ltd., Valve Corporation, or Nefarius Software Solutions e.U.
- **Trademarks**: All trademarks including **Windows**, **Xbox 360**, **Steam**, **PlayStation**, and **Nintendo** are property of their respective owners. Their mention in this repository is solely for **Nominative Fair Use** to describe technical interoperability.
- Read [`DISCLAIMER.md`](./DISCLAIMER.md) for full terms.

---

## 📄 License & Attribution

This project is licensed under the **Apache License, Version 2.0**.
- See the full [LICENSE](./LICENSE) file for legal terms.
- Attributions for third-party libraries (ViGEmClient, JNA, libwdi) are detailed in the [NOTICE](./NOTICE) file.

Copyright © 2026 **Sanket Parmar**. All rights reserved.