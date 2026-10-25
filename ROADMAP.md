# 🗺️ NEXPAD Desktop Roadmap

Strategic vision, architectural milestones, and upcoming technical tracks for the **NEXPAD Desktop** server and authoring platform.

```
                    ┌───────────────┐
                    │   README.md   │
                    │ "What is it?" │
                    └───────┬───────┘
                            │
          ┌─────────────────┼──────────────────┐
          ▼                 ▼                  ▼
     HISTORY.md         ROADMAP.md       ARCHITECTURE.md
     "Where we         "Where we're       "How it
      came from"          going"           works"
```

---

## 🎯 Current Milestone: `v1.0.0` (Completed)

* **Universal AOA 2.0 with Dynamic PnP Detection**: Zero-configuration USB accessory streaming using dynamic Windows PnP (`MS_COMP_MTP`, `WPD`) interface matching across all Android OEMs without hardcoded vendor lists.
* **Bundled Zero-Download NSIS Installer**: Automated silent ViGEmBus installation and automated Windows Firewall configuration in a single installer package (`NEXPAD_Desktop_Setup.exe`).
* **Strict 200 Hz Non-Blocking Input Loop**: Non-blocking NIO `DatagramChannel` engine operating at $5\,\text{ms}$ intervals with honest microsecond RTT and jitter telemetry.
* **Bidirectional Haptic Feedback**: Game motor vibration callbacks captured from ViGEmBus and dispatched as 8-byte UDP feedback packets to the phone in real time.
* **ADB Bridge with Emulator Isolation**: Seamless USB networking with automated `adb reverse` routing and active isolation against background Android Studio virtual emulators.
* **NXPRC Plugin Studio & Designer**: Visual authoring environment for custom HUD vector packages with Skia rendering, CSS-like styling, and one-click FTP push.

---

## 🚀 Near-Term Horizons (`v1.1` — `v1.3`)

### 1. Microsoft Store Packaging (MSIX)
* Package NEXPAD Desktop as a native MSIX package via Desktop Bridge (Centennial).
* Automated silent updates delivered through the Microsoft Store infrastructure.
* Zero elevation required for installation with Windows Store compliance.

### 2. DualShock 4 & DualSense Hardware Emulation
* Extend ViGEmBus client to spawn virtual Sony DualShock 4 (`DS4_REPORT`) targets alongside Xbox 360 targets.
* Transmit PC RGB lightbar color commands to phone screen glow and HUD buttons.
* Feed virtual touchpad touch coordinates directly into PlayStation PC ports (e.g., *Marvel's Spider-Man*, *The Last of Us*, *God of War*).

### 3. Standalone Windows Background Driver Service
* Decouple `DriverBrokerService` into an autonomous Windows Service managed by `prunsrv` / NSSM.
* Allows zero-elevation execution for standard users: the background service maintains kernel driver handles while the UI runs with standard privileges.
* Silent driver installation and automated health watchdog.

### 4. Linux Companion Server (`uinput`)
* Extend the Kotlin Multiplatform desktop server to Linux distributions.
* Interface directly with Linux kernel virtual gamepad subsystem via `/dev/uinput`.
* Distribute portable `.AppImage`, `.deb`, and Snap packages.

---

## 🔮 Long-Term Horizons (`v1.4`+)

### 5. Internet Remote Play Bridge (NAT Traversal / WebRTC)
* Connect phone to gaming PC remotely over 4G/5G/Fiber using WebRTC DataChannels with STUN/TURN hole punching.
* End-to-end encrypted packet transmission with DTLS.

### 6. Low-Latency Audio Streaming Server
* Capture Windows default audio playback device via WASAPI Loopback.
* Encode real-time stereo audio with Opus codec at $< 10\,\text{ms}$ latency and broadcast to connected phones over UDP/Wi-Fi.
