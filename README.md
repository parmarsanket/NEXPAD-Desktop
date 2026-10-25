<div align="center">

  # 🖥️ NEXPAD Desktop
  ### Turn Any Android Smartphone into an Ultra-Low-Latency PC Gaming Controller

  <p align="center">
    <b>Kernel-Level Emulation • 200Hz Polling Rate • CemuHook DSU Motion • Bidirectional Rumble • NXPRC Studio</b>
  </p>

  <p align="center">
    <a href="https://github.com/parmarsanket/NEXPAD-Desktop/releases/tag/v1.0.0">
      <img src="https://img.shields.io/badge/Download-GitHub%20Release%20v1.0.0-2da44e?style=for-the-badge&logo=github&logoColor=white" alt="Download GitHub Release" height="36"/>
    </a>
    &nbsp;&nbsp;
    <a href="https://github.com/parmarsanket/NEXPAD-Desktop/releases/tag/v1.0.0">
      <img src="https://img.shields.io/badge/Microsoft%20Store-Coming%20Soon-0078D4?style=for-the-badge&logo=microsoft-store&logoColor=white" alt="Microsoft Store" height="36"/>
    </a>
  </p>

  <p align="center">
    <img src="https://img.shields.io/badge/Platform-Windows%2010%20%2F%2011%20(x64)-0078D6.svg?style=flat-square&logo=windows" alt="Platform" />
    <img src="https://img.shields.io/badge/UI-Compose%20Multiplatform%20(Skia)-4285F4.svg?style=flat-square&logo=jetpackcompose" alt="Compose Multiplatform" />
    <img src="https://img.shields.io/badge/Driver-ViGEmBus%20Kernel%20(Bundled)-7B1FA2.svg?style=flat-square" alt="ViGEmBus Driver" />
    <img src="https://img.shields.io/badge/Polling%20Rate-200Hz%20Low%20Latency-3FD25A.svg?style=flat-square" alt="200Hz Low Latency" />
    <img src="https://img.shields.io/badge/DSU%20Motion-CemuHook%20(Port%2026760)-FF9800.svg?style=flat-square" alt="DSU Motion Server" />
    <img src="https://img.shields.io/badge/Privacy-100%25%20Local%20%7C%200%20Telemetry-00C853.svg?style=flat-square" alt="Zero Telemetry" />
    <img src="https://img.shields.io/badge/Standard-standard--readme-brightgreen.svg?style=flat-square" alt="Standard Readme Compliant" />
    <img src="https://img.shields.io/badge/License-Apache%202.0-blue.svg?style=flat-square" alt="License" />
  </p>

</div>

<br/>

---

## 📑 Table of Contents

- [Overview](#-overview)
- [Quick Install](#-quick-install)
- [Key Features](#-key-features)
- [System Requirements](#-system-requirements)
- [Transport Comparison Matrix](#-transport-comparison-matrix)
- [Quick Start Guide](#-quick-start-guide-zero-configuration)
- [Network Ports & Windows Firewall](#-network-ports--windows-firewall)
- [Kernel Emulation & Controller Mapping](#-kernel-emulation--controller-mapping)
- [CemuHook DSU Motion Setup](#-cemuhook-dsu-motion-setup)
- [Troubleshooting & Common Fixes](#-troubleshooting--common-fixes)
- [System Architecture & Data Flow](#-system-architecture--data-flow)
- [Subsystem Navigation](#-subsystem-navigation)
- [Developer & Build Guide](#-developer--build-guide)
- [Frequently Asked Questions (FAQ)](#-frequently-asked-questions-faq)
- [Privacy & Security](#-privacy--security-transparency)
- [Legal Disclosures](#-legal-disclosures)
- [License & Attribution](#-license--attribution)

---

## ⚡ Overview

**NEXPAD Desktop** is an ultra-high performance Windows companion server and controller authoring studio built with **Kotlin 2.x JVM**, **Compose Multiplatform (Desktop)**, and **JNA (Java Native Access)**.

It seamlessly bridges your Android smartphone to Windows, injecting controller inputs directly into the Windows Kernel via **ViGEmBus** to emulate genuine physical **Xbox 360** and **Sony DualShock 4** gamepads. The Windows installer automatically configures all drivers out-of-the-box, giving you immediate plug-and-play compatibility across Steam, Xbox Game Pass, Epic Games, and emulators (Cemu, Yuzu, Dolphin, RPCS3).

---

## 🚀 Quick Install

Select your platform to download or install the pre-compiled production release:

### 🖥️ Desktop (Windows)

| Platform | Download | Details |
|---|---|---|
| **Windows (Setup)** | [**Download `NEXPAD_Desktop_Setup.exe`**](https://github.com/parmarsanket/NEXPAD-Desktop/releases/tag/v1.0.0) | **Recommended:** Complete All-in-One installer. Automatically installs NEXPAD Desktop and configures the bundled ViGEmBus kernel driver. |
| **Microsoft Store** | [**Get it from Microsoft Store**](https://github.com/parmarsanket/NEXPAD-Desktop/releases/tag/v1.0.0) | Official native package for Windows 10 & 11 via the Microsoft Store *(Coming Soon / In Review)*. |
| **Standalone MSI** | [**Download `NEXPAD-Desktop.msi`**](https://github.com/parmarsanket/NEXPAD-Desktop/releases/tag/v1.0.0) | Standard Windows Installer package for silent or enterprise network deployments. |
| **Portable Package** | [**Download `NEXPAD-Desktop.zip`**](https://github.com/parmarsanket/NEXPAD-Desktop/releases/tag/v1.0.0) | Standalone portable archive with bundled `redist/` drivers. Extract and run without admin elevation. |

### 📱 Mobile (Android Companion)

| Platform | Download | Details |
|---|---|---|
| **Google Play** | [**Get it on Google Play**](https://play.google.com/store/apps/details?id=com.sanket.tools.nexpad) | Official release for Android smartphones and tablets (supports Android 6.0+ / API 23+). |
| **Direct APK** | [**Download `NEXPAD-Mobile.apk`**](https://github.com/parmarsanket/NEXPAD-Desktop/releases/tag/v1.0.0) | Standalone direct `.apk` binary release for offline installation and sideloading. |

### 📦 Standalone Packages & Assets

For direct downloads, offline installers, or manual installation, fetch the assets directly from GitHub Releases:

[![Download Standalone Binaries](https://img.shields.io/badge/GitHub%20Releases-Download%20v1.0.0%20Assets-2ea44f?style=for-the-badge&logo=github&logoColor=white)](https://github.com/parmarsanket/NEXPAD-Desktop/releases/tag/v1.0.0)

**Standalone assets available on GitHub Releases:**
* 🪟 **Windows Setup**: `NEXPAD_Desktop_Setup.exe` (Complete All-in-One Installer with bundled ViGEmBus driver)
* 🧰 **Windows MSI**: `NEXPAD-Desktop.msi` (Enterprise Windows Installer)
* 💼 **Windows Portable**: `NEXPAD-Desktop-portable.zip` (Portable ZIP archive with bundled `redist/` drivers)
* 🤖 **Android Companion**: `NEXPAD-Mobile.apk` (Production signed APK)

---

## 🌟 Key Features

<table>
  <tr>
    <td width="50%">
      <b>⚡ 200Hz Ultra-Low Latency Pipeline</b><br/>
      Sub-millisecond (<b>&lt;1ms</b>) hardware response over direct USB (AOA 2.0) and high-speed <b>200Hz</b> asynchronous non-blocking NIO UDP streaming over Wi-Fi. Every millisecond counts.
    </td>
    <td width="50%">
      <b>🎮 Dual Kernel Gamepad Emulation</b><br/>
      Full native <b>Xbox 360</b> (XInput) and <b>Sony DualShock 4</b> emulation via the bundled ViGEmBus kernel driver. Zero third-party mapping software required.
    </td>
  </tr>
  <tr>
    <td width="50%">
      <b>🎯 CemuHook DSU Motion Server</b><br/>
      Integrated UDP motion server broadcasting on standard port <b>26760</b>. Enjoy 6-axis gyroscope steering, flight stick controls, and precision FPS gyro aiming on Cemu, Yuzu, Ryujinx, RPCS3, and Dolphin.
    </td>
    <td width="50%">
      <b>📳 Bidirectional Force Feedback</b><br/>
      Intercepts game rumble commands from XInput/ViGEm and streams real-time feedback packets back to your phone to trigger immersive haptic vibrations.
    </td>
  </tr>
  <tr>
    <td width="50%">
      <b>🌐 4-Way Multi-Transport Engine</b><br/>
      Connect your way: <b>USB Direct (AOA 2.0)</b>, <b>USB ADB Bridge</b> (zero driver installation), <b>200Hz Wi-Fi UDP</b>, or <b>Bluetooth RFCOMM</b> (125Hz).
    </td>
    <td width="50%">
      <b>📱 Universal Android Compatibility</b><br/>
      Dynamic protocol-level detection works out-of-the-box on <b>Samsung, Xiaomi, Vivo, OPPO, OnePlus, Google Pixel, Motorola, Nothing, ASUS, Sony</b>, and all other OEMs without root.
    </td>
  </tr>
  <tr>
    <td width="50%">
      <b>🎨 Live NXPRC Component Studio</b><br/>
      Design and customize vector controller layouts in real-time on desktop with instant bidirectional synchronization to your connected phone.
    </td>
    <td width="50%">
      <b>🔒 100% Offline & Zero Telemetry</b><br/>
      All processing happens strictly on your local PC and local subnet. Zero analytics, zero data collection, zero internet required.
    </td>
  </tr>
</table>

---

## 🛡️ System Requirements

| Component | Minimum Requirements | Recommended for Pro Gaming |
|---|---|---|
| **Operating System** | Windows 10 (64-bit, Version 1809 or higher) | Windows 11 (64-bit, Version 23H2 or higher) |
| **Android Smartphone** | Android 6.0 (Marshmallow) with Gyroscope & Accelerometer | Android 10+ with 120Hz display & USB 3.1 Type-C |
| **Processor & RAM** | Dual-core 2.0 GHz CPU, 2 GB RAM | Quad-core 3.0+ GHz CPU, 4 GB RAM |
| **Wireless Connection** | 2.4 GHz Wi-Fi Router or Bluetooth 4.2 | 5 GHz Wi-Fi 6 Router or USB 3.1 Tethering |
| **Virtual Drivers** | Bundled ViGEmBus (Installed automatically by setup) | Microsoft Visual C++ 2015–2022 x64 (Bundled) |

---

## 📊 Transport Comparison Matrix

Choose the connection method that best fits your playstyle:

| Connection Transport | Polling Rate | Hardware Latency | Setup Effort | Windows Driver | Recommended For |
|---|:---:|:---:|:---:|:---:|---|
| 🔌 **USB Direct (AOA 2.0)** | **200 Hz** | **&lt; 1 ms** | Plug & Play | Automatic WinUSB | **Competitive Gaming, FPS, Fighting Games** |
| 🛠️ **USB ADB Bridge** | **200 Hz** | **~1 ms** | USB Debugging ON | Zero Driver Required | **Quick Testing, Zero UAC Prompts** |
| 📶 **Wi-Fi (UDP Stream)** | **200 Hz** | **~2 – 3 ms** | One-Click Connect | None | **Couch Gaming, Wireless Handheld Mode** |
| 🔵 **Bluetooth RFCOMM** | **125 Hz** | **~8 ms** | Standard BT Pair | None | **Cable-Free Wireless Casual Play** |

---

## 🕹️ Quick Start Guide (Zero Configuration)

Everything you need is bundled inside the installer. No external driver hunting or complex setup needed.

### 1️⃣ Run the Installer
* Download and run **`NEXPAD_Desktop_Setup.exe`**.
* The setup wizard automatically installs NEXPAD Desktop and configures the bundled **ViGEmBus** virtual controller driver in one smooth step.

> [!NOTE]
> All required virtual controller drivers are bundled directly inside `NEXPAD_Desktop_Setup.exe` and configured automatically during installation. Zero manual driver downloads are required.

### 2️⃣ Launch NEXPAD Desktop
* Open NEXPAD Desktop from your Start Menu.
* The status indicator will display **"ViGEmBus: READY"**, and the server will immediately begin listening for incoming connections.

### 3️⃣ Connect Phone & Play!
* Open the **NEXPAD** app on your Android phone and select your preferred transport:
  * **USB (AOA)**: Connect via USB-C cable and tap **Connect USB**.
  * **Wi-Fi**: Ensure your phone is on the same local Wi-Fi, enter the IP shown on Desktop, and tap **Connect Wi-Fi**.
* Windows instantly registers an **Xbox 360 Controller for Windows**. Launch your game and play!

> [!TIP]
> You can verify your virtual controller at any time by pressing <kbd>Win</kbd> + <kbd>R</kbd>, typing `joy.cpl`, and pressing <kbd>Enter</kbd> to open Windows Game Controllers.

---

## 🌐 Network Ports & Windows Firewall

NEXPAD Desktop communicates locally over these dedicated network ports:

| Protocol | Port Number | Subsystem / Function | Scope & Direction |
|---|:---:|---|:---:|
| **UDP** | `9999` | 200Hz Gamepad Input Stream & LAN Discovery Beacons | Inbound (Local Subnet) |
| **UDP** | `26760` | CemuHook DSU Motion Protocol (Cemu, Yuzu, Dolphin, RPCS3) | Local Loopback (`127.0.0.1`) |
| **TCP** | `9999` | ADB Reverse Bridge Tunnel (`localabstract:nexpad_controller`) | Local Loopback (`127.0.0.1`) |

> [!NOTE]
> When Windows prompts with a "Windows Defender Firewall" alert on first launch, check both **Private networks** and **Public networks** to allow seamless Wi-Fi auto-discovery and UDP streaming.

---

## 🎮 Kernel Emulation & Controller Mapping

NEXPAD Desktop communicates directly with `vigemclient.dll` to inject native controller state structures into Windows:

| Smartphone Touch / Sensor Input | Virtual Xbox 360 Output | Virtual DualShock 4 Output | Behavior & Resolution |
|---|---|---|---|
| **Face Buttons** | A, B, X, Y | ✕, ○, □, △ | Zero-debounce digital click |
| **Shoulder Bumpers** | LB, RB | L1, R1 | Instant microsecond trigger |
| **Analog Triggers** | LT, RT | L2, R2 | 8-bit analog pressure ($0\dots 255$) |
| **Dual Thumbsticks** | Left & Right Stick (LS / RS) | Left & Right Stick (L3 / R3) | 16-bit signed axis ($-32768\dots 32767$) |
| **Directional Pad** | D-Pad (Up, Down, Left, Right) | D-Pad (Up, Down, Left, Right) | 4-way discrete digital states |
| **System Buttons** | Back / View, Start / Menu | Share, Options, PS Home | Standard game pause / menu navigation |
| **6-Axis Gyro / Accelerometer** | Right Stick Aim / Steering | DS4 Motion / CemuHook DSU | 200Hz smoothed orientation vectors |
| **Smartphone Vibrator** | Left (Heavy) & Right (Light) Motors | Dual Rumble Motors | Real-time XInput feedback packet echo |

---

## 🎯 CemuHook DSU Motion Setup

To enable motion controls (gyro steering / aiming) in console emulators:

1. In NEXPAD Desktop, navigate to **Gyro Settings** and choose your mode (**FPS Aim Assist**, **Steering Wheel**, or **Flight Stick**).
2. Open your emulator of choice:
   * **Cemu**: Go to `Options` → `Gamepad motion source` → `DSU1` → Select `By Slot` or enter IP `127.0.0.1` and Port `26760`.
   * **Yuzu / Ryujinx**: Go to `Settings` → `Input` → `Motion` → Check `Enable Motion` and configure DSU Server `127.0.0.1:26760`.
   * **Dolphin**: Go to `Controllers` → `Alternate Input Sources` → Enable DSU Client (`127.0.0.1:26760`).
   * **RPCS3**: Go to `Pads` → Set Handler to `DSU` and Port to `26760`.

---

## 🔧 Troubleshooting & Common Fixes

<details>
<summary><b>1. Phone cannot discover PC over Wi-Fi</b></summary>
<ul>
  <li><b>Check Windows Network Profile</b>: Open Windows Settings → Network & Internet → Properties → Set network profile to <b>Private</b> (Windows blocks UDP broadcasts on Public networks).</li>
  <li><b>Verify Windows Firewall</b>: Ensure <code>NEXPAD Desktop.exe</code> is permitted through Windows Defender Firewall for UDP port <code>9999</code>.</li>
  <li><b>Same Wi-Fi Band</b>: Verify both phone and PC are connected to the same Wi-Fi router (or use your phone's Wi-Fi Hotspot / USB Tethering).</li>
</ul>
</details>

<details>
<summary><b>2. USB Direct (AOA) mode does not start</b></summary>
<ul>
  <li><b>Check USB Connection Mode</b>: When you plug your phone in, swipe down from the notification bar and select <b>File Transfer / MTP</b> (not "Charging Only").</li>
  <li><b>Accept UAC Elevation Prompt</b>: On the first USB connection, Windows will ask for permission to bind the high-speed WinUSB driver. Click <b>Yes</b>.</li>
  <li><b>Try USB ADB Bridge</b>: If your phone OEM driver locks MTP, turn on <b>USB Debugging</b> in Android Developer Options and select <i>Connect USB (ADB)</i> for instant driverless play.</li>
</ul>
</details>

<details>
<summary><b>3. Game or Steam is not recognizing the controller</b></summary>
<ul>
  <li><b>Check Windows Game Controllers</b>: Press <kbd>Win</kbd> + <kbd>R</kbd>, typing <code>joy.cpl</code>, and press <kbd>Enter</kbd>. If you see <i>"Controller (XBOX 360 For Windows)"</i>, the driver is working properly.</li>
  <li><b>Enable Steam Controller Support</b>: Open Steam → Settings → Controller → Toggle ON <b>Xbox Controller Support</b>.</li>
  <li><b>Restart Game</b>: Some older games only detect controllers if NEXPAD Desktop is connected <i>before</i> the game launches.</li>
</ul>
</details>

<details>
<summary><b>4. Gyro motion is not responding in Cemu or Yuzu</b></summary>
<ul>
  <li><b>Check DSU Server Port</b>: Verify that your emulator's DSU/CemuHook motion input is configured to IP <code>127.0.0.1</code> and Port <code>26760</code>.</li>
  <li><b>Confirm Motion is Active</b>: In NEXPAD Desktop, open <b>Gyro Settings</b> and ensure the motion mode toggle is set to <i>Precision Aiming</i>, <i>Steering Wheel</i>, or <i>Flight Stick</i>.</li>
</ul>
</details>

---

## 🏗️ System Architecture & Data Flow

```
┌────────────────────────────────────────────────────────┐
│               Android Phone (NEXPAD Mobile)            │
│  Touch Controls • 6-Axis Gyroscope/Accelerometer • IMU │
└──────────────────────────┬─────────────────────────────┘
                           │
    4-Way Multi-Transport: │ AOA 2.0 (USB) | UDP (Wi-Fi) | ADB | Bluetooth
                           ▼
┌────────────────────────────────────────────────────────┐
│                    NEXPAD Desktop                      │
│        (Kotlin 2.x JVM + Compose Multiplatform)        │
│                                                        │
│  • ConnectionCoordinator (Single-Active Arbitration)   │
│  • AoaFrameDecoder / UdpServer (200Hz Low Latency)     │
│  • GyroProcessor (Precision Crosshairs, Deadzones)     │
│  • DsuServer (CemuHook Motion Protocol on Port 26760)  │
│  • UniversalPushManager (.nxprc Component Sync)        │
└──────────────────────────┬─────────────────────────────┘
                           │ JNA (Java Native Access)
                           ▼
┌────────────────────────────────────────────────────────┐
│           ViGEmBus Kernel Driver Subsystem             │
│       Xbox 360 (XUSBReport) | Sony DualShock 4         │
└──────────────────────────┬─────────────────────────────┘
                           │ Windows Kernel Driver Framework (KMDF)
                           ▼
┌────────────────────────────────────────────────────────┐
│                      Windows OS                        │
│    DirectInput / XInput Native Physical Gamepad        │
│                                                        │
│   Steam • Xbox Game Pass • Epic Games • PC Emulators   │
└────────────────────────────────────────────────────────┘
```

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

- 📖 **[`HISTORY.md`](./HISTORY.md)** — Chronological engineering log of all development milestones.
- 🗺️ **[`ROADMAP.md`](./ROADMAP.md)** — Forward-looking development milestones and store packaging.
- 🏛️ **[`ARCHITECTURE.md`](./ARCHITECTURE.md)** — Deep technical specification of the JNA ViGEmClient binding, WinUSB AOA bulk endpoints, and NIO UdpServer.
- 🤖 **[`AGENTS.md`](./AGENTS.md)** — Machine-readable technical reference, boundaries, and deterministic commands for AI coding assistants.
- 📜 **[`GRADLE_COMMANDS.md`](./GRADLE_COMMANDS.md)** — Quick reference cheat-sheet for running, testing, building, and packaging.
- ⚖️ **[`DISCLAIMER.md`](./DISCLAIMER.md)** — Nominative Fair Use, driver safety, and legal non-affiliation disclosure.
- 📄 **[`NOTICE`](./NOTICE)** — Third-party open-source attributions (ViGEmClient, JNA, libwdi, JetBrains Compose).

---

## 🛠️ Developer & Build Guide

### Prerequisites
* **JDK**: Eclipse Temurin or Amazon Corretto **JDK 21**
* **OS**: Windows 10 or 11 (x64)
* **Gradle**: 9.x (Wrapper included)

### Build Commands

```powershell
# Clone the repository
git clone https://github.com/parmarsanket/NEXPAD-Desktop.git
cd NEXPAD-Desktop

# Compile Kotlin JVM sources
.\gradlew.bat :desktopApp:compileKotlin

# Launch Desktop application in development mode
.\gradlew.bat :desktopApp:run

# Run test suite
.\gradlew.bat :desktopApp:test

# Package Standalone Release (.exe)
.\gradlew.bat :desktopApp:packageReleaseExe

# Package Windows MSI Installer
.\gradlew.bat :desktopApp:packageMsi

# Compile All-in-One NSIS Setup Wizard (requires makensis)
makensis installer.nsi
```

---

## ❓ Frequently Asked Questions (FAQ)

<details>
<summary><b>Does NEXPAD require root access on Android?</b></summary>
<p>
<b>No.</b> NEXPAD works entirely within standard Android APIs. Neither USB (AOA 2.0), Wi-Fi UDP, nor Bluetooth require root permissions.
</p>
</details>

<details>
<summary><b>Do I need to download ViGEmBus separately?</b></summary>
<p>
<b>No.</b> The official <code>NEXPAD_Desktop_Setup.exe</code> installer already bundles the official ViGEmBus installer and configures it automatically during installation.
</p>
</details>

<details>
<summary><b>Does it work without an internet connection?</b></summary>
<p>
<b>Yes, 100%.</b> NEXPAD Desktop functions completely offline. Direct USB cable connection requires zero network. Over Wi-Fi, it communicates strictly within your local home subnet.
</p>
</details>

<details>
<summary><b>Why is 200Hz polling rate important?</b></summary>
<p>
A 200Hz polling rate delivers a fresh controller input packet every <b>5 milliseconds</b>. This synchronizes cleanly with high refresh-rate monitors (144Hz, 165Hz, 240Hz), ensuring zero stutter or input delay during competitive gaming.
</p>
</details>

<details>
<summary><b>Why does the USB driver prompt for Administrator elevation?</b></summary>
<p>
Standard Windows ships with a read-only MTP media driver for smartphones. To send high-speed controller data directly to the phone's hardware USB endpoints, NEXPAD uses the standard Microsoft WinUSB driver. The elevation prompt runs once via Windows UAC to configure this high-speed pathway.
</p>
</details>

---

## 🔒 Privacy & Security Transparency

* **Zero Telemetry**: We collect zero analytics, crash logs, IP addresses, or personal data.
* **Local Subnet Only**: Servers listen strictly on your local adapter. No traffic ever leaves your home network.
* **Driver Safety**: NEXPAD interfaces with the verified, open-source ViGEmBus kernel driver purely through user-mode APIs (`vigemclient.dll`).

Read [`DISCLAIMER.md`](./DISCLAIMER.md) for full legal disclosures.

---

## ⚖️ Legal Disclosures

* **Non-Affiliation**: NEXPAD Desktop is an independent open-source project. It is not affiliated with, sponsored by, or endorsed by Microsoft Corporation, Sony Interactive Entertainment, Nintendo Co., Ltd., Valve Corporation, or Nefarius Software Solutions e.U.
* **Trademarks**: All trademarks including **Windows**, **Xbox 360**, **Steam**, **PlayStation**, **DualShock**, and **Nintendo** belong to their respective owners and are mentioned strictly under **Nominative Fair Use** to describe interoperability.

---

## 📄 License & Attribution

This project is licensed under the **Apache License, Version 2.0**.
* Read the full [LICENSE](./LICENSE) file.
* Third-party attributions (ViGEmClient, JNA, libwdi, JetBrains Compose) are documented in [NOTICE](./NOTICE).

<br/>

<div align="center">
  <sub>Built with ❤️ by <b>Sanket Parmar</b> for the PC gaming community.</sub>
</div>
