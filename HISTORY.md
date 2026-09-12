# 📖 NEXPAD Desktop Development History & Milestone Timeline

This document details the chronological evolution of the **NEXPAD Desktop** server and authoring suite, mapping every development branch to its permanent Git Tag.

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
          │
          ▼
      Git Tags
   (v0.1 ──► v0.32)
```

---

## 📅 Chronological Milestone Index

| Tag | Date & Time | Original Branch | Key Breakthroughs & Deliverables |
|:---:|:---:|---|---|
| **[`v0.1`](https://github.com/parmarsanket/NEXPADDesktop/releases/tag/v0.1)** | `2026-05-31 02:56` | `master` | **Project Genesis**: Initial Kotlin/JVM desktop project structure with Gradle setup. |
| **[`v0.2`](https://github.com/parmarsanket/NEXPADDesktop/releases/tag/v0.2)** | `2026-05-31 03:32` | `init` | **Desktop UI Foundation**: Basic Compose Multiplatform desktop window and layout scaffolding. |
| **[`v0.3`](https://github.com/parmarsanket/NEXPADDesktop/releases/tag/v0.3)** | `2026-05-31 06:48` | `feature/vigem-driver` | **ViGEmBus JNA Bindings**: Integrated low-level C++ ViGEmClient library via JNA to emulate Xbox 360 controller. |
| **[`v0.4`](https://github.com/parmarsanket/NEXPADDesktop/releases/tag/v0.4)** | `2026-06-02 19:21` | `feature/modularization-and-scalability` | **Architecture Modularization**: Decoupled driver abstraction, networking coroutines, and UI state. |
| **[`v0.5`](https://github.com/parmarsanket/NEXPADDesktop/releases/tag/v0.5)** | `2026-06-03 04:28` | `debugging` | **Driver Health Diagnostics**: Fixed JNA pointer allocations and driver bus connection leaks. |
| **[`v0.6`](https://github.com/parmarsanket/NEXPADDesktop/releases/tag/v0.6)** | `2026-06-03 11:59` | `feature/controller-navigation` | **Gamepad Navigation & State**: Handled button release timeouts and analog stick center deadzones. |
| **[`v0.7`](https://github.com/parmarsanket/NEXPADDesktop/releases/tag/v0.7)** | `2026-06-07 00:10` | `feature/ds4-gyro-axis-fix` | **DualShock 4 & Motion Axis Inversion**: Corrected roll, pitch, and yaw axis sign errors in DSU motion server. |
| **[`v0.8`](https://github.com/parmarsanket/NEXPADDesktop/releases/tag/v0.8)** | `2026-06-07 04:05` | `feature/testing-and-bug-fixing` | **Socket Pipeline Hardening**: Added error recovery on socket bind failures and port conflicts. |
| **[`v0.9`](https://github.com/parmarsanket/NEXPADDesktop/releases/tag/v0.9)** | `2026-06-07 04:46` | `feature/controller-config` | **Controller Configuration System**: Supported custom deadzones, stick sensitivities, and trigger thresholds. |
| **[`v0.10`](https://github.com/parmarsanket/NEXPADDesktop/releases/tag/v0.10)** | `2026-06-07 08:18` | `feature/controller-config-v2` | **Profile Persistence**: Saved controller configurations to local JSON preferences. |
| **[`v0.11`](https://github.com/parmarsanket/NEXPADDesktop/releases/tag/v0.11)** | `2026-08-03 08:01` | `feature/controller-setting-ui` | **Settings UI Screen**: Interactive Compose Desktop UI for adjusting joystick curves and deadzones. |
| **[`v0.12`](https://github.com/parmarsanket/NEXPADDesktop/releases/tag/v0.12)** | `2026-08-03 12:34` | `core/high-speed-binary-protocol` | **1000Hz Binary Protocol**: Switched from JSON strings to high-speed 44-byte raw binary packet parser. |
| **[`v0.13`](https://github.com/parmarsanket/NEXPADDesktop/releases/tag/v0.13)** | `2026-08-11 10:44` | `feature/installer-and-driver-resilience` | **Installer & ViGEm Auto-Detection**: Added automated ViGEm driver presence checks and NSIS installer scripts. |
| **[`v0.14`](https://github.com/parmarsanket/NEXPADDesktop/releases/tag/v0.14)** | `2026-08-11 13:40` | `feature/honest-telemetry` | **Honest Telemetry Engine**: Embedded microsecond RTT ping calculation and packet rate counters. |
| **[`v0.15`](https://github.com/parmarsanket/NEXPADDesktop/releases/tag/v0.15)** | `2026-08-11 17:51` | `fix/deep-audit-networking-fixes` | **Deep Concurrency Audit**: Fixed memory corruption in JNA native buffers and thread starvation in UDP loop. |
| **[`v0.16`](https://github.com/parmarsanket/NEXPADDesktop/releases/tag/v0.16)** | `2026-08-12 19:53` | `main` | **Desktop Baseline Sync**: Merged stabilized networking, telemetry, and settings UI into main. |
| **[`v0.17`](https://github.com/parmarsanket/NEXPADDesktop/releases/tag/v0.17)** | `2026-08-15 22:37` | `feature/kmp-protocol-migration` | **KMP Shared Protocol Migration**: Migrated desktop packet parser to use shared `protocol` library. |
| **[`v0.18`](https://github.com/parmarsanket/NEXPADDesktop/releases/tag/v0.18)** | `2026-09-02 13:16` | `feature/udp-qos-and-heartbeat-optimization` | **UDP QoS & Heartbeats**: Implemented packet sequence validation, dropping stale out-of-order packets. |
| **[`v0.19`](https://github.com/parmarsanket/NEXPADDesktop/releases/tag/v0.19)** | `2026-09-03 17:39` | `feature/aoa-winusb-poc` | **AOA WinUSB Architecture**: Developed initial Named Pipe IPC server and libwdi driver swapping prototype. |
| **[`v0.20`](https://github.com/parmarsanket/NEXPADDesktop/releases/tag/v0.20)** | `2026-09-04 01:23` | `feature/aoa-winusb-improvements` | **AOA Bulk Streamer**: Implemented usb4java bulk transfer endpoints for Android accessory mode. |
| **[`v0.21`](https://github.com/parmarsanket/NEXPADDesktop/releases/tag/v0.21)** | `2026-09-04 18:20` | `feature/feedback-packet` | **Haptic Feedback Callback**: Extracted game motor vibration values from ViGEmBus and emitted 8-byte UDP packets. |
| **[`v0.22`](https://github.com/parmarsanket/NEXPADDesktop/releases/tag/v0.22)** | `2026-09-04 19:23` | `feature/aoa-bugfixes` | **USB Connection Watchdog**: Handled sudden USB cable disconnects and device re-enumeration events. |
| **[`v0.23`](https://github.com/parmarsanket/NEXPADDesktop/releases/tag/v0.23)** | `2026-09-04 21:55` | `feature/aoa-bugfixes-v2` | **WinUSB Descriptor Parser**: Enhanced interface descriptor matching across diverse Android OEM vendor IDs. |
| **[`v0.24`](https://github.com/parmarsanket/NEXPADDesktop/releases/tag/v0.24)** | `2026-09-05 05:42` | `feature/adb-bridge` | **Integrated ADB Bridge**: Built automated `adb reverse` command launcher for seamless USB fallback. |
| **[`v0.25`](https://github.com/parmarsanket/NEXPADDesktop/releases/tag/v0.25)** | `2026-09-06 03:58` | `feature/bt-connection` | **Bluetooth Server Socket**: Added Windows Bluetooth RFCOMM serial connection listener. |
| **[`v0.26`](https://github.com/parmarsanket/NEXPADDesktop/releases/tag/v0.26)** | `2026-09-06 08:01` | `feature/homescreen-logic-improvements` | **Command Center Redesign**: Glassmorphic UI dashboard with live IP resolver, connection switchers, and metrics. |
| **[`v0.27`](https://github.com/parmarsanket/NEXPADDesktop/releases/tag/v0.27)** | `2026-09-09 13:19` | `feature/hud-metrics-and-plugins` | **Desktop Plugin Manager & FTP**: Built FTP server and file pusher to upload custom HUD skins to phone. |
| **[`v0.28`](https://github.com/parmarsanket/NEXPADDesktop/releases/tag/v0.28)** | `2026-09-10 01:25` | `feature/nxprc-html-css-engine` | **NXPRC Plugin Studio Genesis**: Added visual authoring tab for `.nxprc` vector controller buttons. |
| **[`v0.29`](https://github.com/parmarsanket/NEXPADDesktop/releases/tag/v0.29)** | `2026-09-11 02:40` | `fix/nxprc-engine-bugs` | **Vector Geometry Tuning**: Fixed border radiuses, sweep gradients, and multi-color glow shaders. |
| **[`v0.30`](https://github.com/parmarsanket/NEXPADDesktop/releases/tag/v0.30)** | `2026-09-11 10:57` | `test/nxprc-engine-testing` | **Chrome Headless Test Pipeline**: Validated vector rendering with pixel-differential comparison against Chromium. |
| **[`v0.31`](https://github.com/parmarsanket/NEXPADDesktop/releases/tag/v0.31)** | `2026-09-11 12:16` | `test/nxprc-engine-testing-v2` | **6 Gamepad Categories Studio**: Added authoring templates for ABXY, D-Pad, Thumbsticks, Bumpers, Triggers, and System buttons. |
| **[`v0.32`](https://github.com/parmarsanket/NEXPADDesktop/releases/tag/v0.32)** | `2026-09-12 09:06` | `test/nxprc-engine-testing-v3` | **Universal Keyframe Studio & 3 Pillars**: Implemented dynamic timeline track keyframes, joystick physics, and 3-pillar docs. |
