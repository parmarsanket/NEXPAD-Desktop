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

## 🎯 Current Milestone: `v0.32` (Completed)
- **Universal Keyframe Studio**: Timeline track authoring for dynamic `.nxprc` vector controller packages.
- **Full Physical Gamepad Feedback**: Bidirectional 8-byte game motor rumble callbacks translated to phone vibration.
- **Zero-Driver USB Streaming**: Android Open Accessory (AOA) mode with high-throughput bulk endpoints.
- **Chromium Parity Auditing**: Certified pixel-accurate vector rendering pipeline across 6 gamepad button classes.

---

## 🚀 Near-Term Horizons (`v0.33` — `v0.36`)

### 1. DualShock 4 & DualSense Hardware Emulation
- Extend ViGEmBus client to spawn virtual Sony DualShock 4 (`DS4_REPORT`) targets alongside Xbox 360 targets.
- Transmit PC RGB lightbar color commands to phone screen glow and HUD buttons.
- Feed virtual touchpad touch coordinates directly into PlayStation PC ports (e.g. Marvel's Spider-Man, The Last of Us, God of War).

### 2. Standalone Windows Background Driver Service
- Decouple `DriverBrokerService` into an autonomous Windows Service managed by `prunsrv` / NSSM.
- Allows zero-elevation execution for standard users: the background service maintains kernel driver handles while the UI runs with standard privileges.
- Silent driver installation and automated health watchdog.

### 3. All-in-One MSIX & Inno Setup Installer
- Modern MSIX package for seamless one-click installation and Windows Startup registration.
- Auto-bundle the signed ViGEmBus runtime installer (`ViGEmBus_Setup_1.22.0.exe`) with silent pre-requisite detection.

---

## 🔮 Long-Term Horizons (`v0.37`+)

### 4. Internet Remote Play Bridge (NAT Traversal / WebRTC)
- Connect phone to gaming PC remotely over 4G/5G/Fiber using WebRTC DataChannels with STUN/TURN hole punching.
- End-to-end encrypted packet transmission with DTLS.

### 5. Low-Latency Audio Streaming Server
- Capture Windows default audio playback device via WASAPI Loopback.
- Encode real-time stereo audio with Opus codec at $< 10\,\text{ms}$ latency and broadcast to connected phones over UDP/Wi-Fi.
