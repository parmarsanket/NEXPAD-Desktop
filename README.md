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
- [Key Features](#-key-features)
- [Transport Comparison Matrix](#-transport-comparison-matrix)
- [Download & Distribution](#-download--distribution-channels)
- [Quick Start Guide](#-quick-start-guide-zero-configuration)
- [Kernel Emulation & Controller Mapping](#-kernel-emulation--controller-mapping)
- [CemuHook DSU Motion Setup](#-cemuhook-dsu-motion-setup)
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

## 📊 Transport Comparison Matrix

Choose the connection method that best fits your playstyle:

| Connection Transport | Polling Rate | Hardware Latency | Setup Effort | Windows Driver | Recommended For |
|---|:---:|:---:|:---:|:---:|---|
| 🔌 **USB Direct (AOA 2.0)** | **200 Hz** | **&lt; 1 ms** | Plug & Play | Automatic WinUSB | **Competitive Gaming, FPS, Fighting Games** |
| 🛠️ **USB ADB Bridge** | **200 Hz** | **~1 ms** | USB Debugging ON | Zero Driver Required | **Quick Testing, Zero UAC Prompts** |
| 📶 **Wi-Fi (UDP Stream)** | **200 Hz** | **~2 – 3 ms** | One-Click Connect | None | **Couch Gaming, Wireless Handheld Mode** |
| 🔵 **Bluetooth RFCOMM** | **125 Hz** | **~8 ms** | Standard BT Pair | None | **Cable-Free Wireless Casual Play** |

---

## 📥 Download & Distribution Channels

| Channel | Format | What's Included | Status / Link |
|---|---|---|---|
| 🚀 **GitHub Releases (v1.0.0)** | `NEXPAD_Desktop_Setup.exe` | Complete All-in-One Installer (Bundles App + ViGEmBus Driver) | [**Download Setup (v1.0.0)**](https://github.com/parmarsanket/NEXPAD-Desktop/releases/tag/v1.0.0) |
| 🏬 **Microsoft Store** | Sandboxed MSIX | Windows 10/11 Automated Updates | *Coming Soon (In Review)* |
| 🧰 **Standalone MSI** | `NEXPAD-Desktop.msi` | Automated Enterprise / Network Deployment | [**Download MSI**](https://github.com/parmarsanket/NEXPAD-Desktop/releases/tag/v1.0.0) |
| 💼 **Portable Package** | `.zip` Archive | Self-Contained App + Bundled `redist/` Drivers | [**Download ZIP**](https://github.com/parmarsanket/NEXPAD-Desktop/releases/tag/v1.0.0) |

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

## 🎮 Kernel Emulation & Controller Mapping

NEXPAD Desktop communicates directly with `vigemclient.dll` to inject native controller state structures into Windows:

### Xbox 360 Emulation (`XUSBReport`)
* **Digital Buttons (10)**: A, B, X, Y, Left Bumper (LB), Right Bumper (RB), Back, Start, Left Stick Click (LS), Right Stick Click (RS).
* **Directional Pad**: 4-way discrete D-Pad states (Up, Down, Left, Right).
* **Analog Triggers**: Left Trigger (LT) & Right Trigger (RT) with 8-bit resolution ($0\dots 255$).
* **Analog Thumbsticks**: Left and Right 2D thumbsticks with 16-bit signed integer resolution ($-32768\dots 32767$).
* **XInput Force Feedback**: Intercepts high-frequency and low-frequency rumble commands from games and forwards them to mobile haptic actuators.

### Sony DualShock 4 Emulation (`DualShock4InputMapper`)
* **Full DS4 Compatibility**: Emulates genuine DualShock 4 hardware, recognized by PlayStation-compatible games, emulators, and DS4Windows.
* **Touch Pad & Lightbar**: Supports multi-touch touchpad coordinate mapping and RGB lightbar status syncing.

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
