# 📖 NEXPAD Desktop Development History & Branch Timeline

This document details the chronological evolution of the **NEXPAD Desktop** server and authoring suite across its development branches.

---

## 📅 Chronological Branch Milestone Index

| # | Date & Time | Branch Name | Key Breakthroughs & Deliverables |
|:---:|:---:|---|---|
| **01** | `2026-05-31 02:56` | [`master`](https://github.com/parmarsanket/NEXPAD-Desktop/tree/master) | **Project Genesis**: Initial Kotlin/JVM desktop project structure with Gradle setup. |
| **02** | `2026-05-31 03:32` | [`init`](https://github.com/parmarsanket/NEXPAD-Desktop/tree/init) | **Desktop UI Foundation**: Basic Compose Multiplatform desktop window and layout scaffolding. |
| **03** | `2026-05-31 06:48` | [`feature/vigem-driver`](https://github.com/parmarsanket/NEXPAD-Desktop/tree/feature/vigem-driver) | **ViGEmBus JNA Bindings**: Integrated low-level C++ ViGEmClient library via JNA to emulate Xbox 360 controller. |
| **04** | `2026-06-02 19:21` | [`feature/modularization-and-scalability`](https://github.com/parmarsanket/NEXPAD-Desktop/tree/feature/modularization-and-scalability) | **Architecture Modularization**: Decoupled driver abstraction, networking coroutines, and UI state. |
| **05** | `2026-06-03 04:28` | [`debugging`](https://github.com/parmarsanket/NEXPAD-Desktop/tree/debugging) | **Driver Health Diagnostics**: Fixed JNA pointer allocations and driver bus connection leaks. |
| **06** | `2026-06-03 11:59` | [`feature/controller-navigation`](https://github.com/parmarsanket/NEXPAD-Desktop/tree/feature/controller-navigation) | **Gamepad Navigation & State**: Handled button release timeouts and analog stick center deadzones. |
| **07** | `2026-06-07 00:10` | [`feature/ds4-gyro-axis-fix`](https://github.com/parmarsanket/NEXPAD-Desktop/tree/feature/ds4-gyro-axis-fix) | **DualShock 4 & Motion Axis Inversion**: Corrected roll, pitch, and yaw axis sign errors in DSU motion server. |
| **08** | `2026-06-07 04:05` | [`feature/testing-and-bug-fixing`](https://github.com/parmarsanket/NEXPAD-Desktop/tree/feature/testing-and-bug-fixing) | **Socket Pipeline Hardening**: Added error recovery on socket bind failures and port conflicts. |
| **09** | `2026-06-07 04:46` | [`feature/controller-config`](https://github.com/parmarsanket/NEXPAD-Desktop/tree/feature/controller-config) | **Controller Configuration System**: Supported custom deadzones, stick sensitivities, and trigger thresholds. |
| **10** | `2026-06-07 08:18` | [`feature/controller-config-v2`](https://github.com/parmarsanket/NEXPAD-Desktop/tree/feature/controller-config-v2) | **Profile Persistence**: Saved controller configurations to local JSON preferences. |
| **11** | `2026-08-03 08:01` | [`feature/controller-setting-ui`](https://github.com/parmarsanket/NEXPAD-Desktop/tree/feature/controller-setting-ui) | **Settings UI Screen**: Interactive Compose Desktop UI for adjusting joystick curves and deadzones. |
| **12** | `2026-08-03 12:34` | [`core/high-speed-binary-protocol`](https://github.com/parmarsanket/NEXPAD-Desktop/tree/core/high-speed-binary-protocol) | **1000Hz Binary Protocol**: Switched from JSON strings to high-speed 44-byte raw binary packet parser. |
| **13** | `2026-08-11 10:44` | [`feature/installer-and-driver-resilience`](https://github.com/parmarsanket/NEXPAD-Desktop/tree/feature/installer-and-driver-resilience) | **Installer & ViGEm Auto-Detection**: Added automated ViGEm driver presence checks and NSIS installer scripts. |
| **14** | `2026-08-11 13:40` | [`feature/honest-telemetry`](https://github.com/parmarsanket/NEXPAD-Desktop/tree/feature/honest-telemetry) | **Honest Telemetry Engine**: Embedded microsecond RTT ping calculation and packet rate counters. |
| **15** | `2026-08-11 17:51` | [`fix/deep-audit-networking-fixes`](https://github.com/parmarsanket/NEXPAD-Desktop/tree/fix/deep-audit-networking-fixes) | **Deep Concurrency Audit**: Fixed memory corruption in JNA native buffers and thread starvation in UDP loop. |
| **16** | `2026-08-12 19:53` | [`main`](https://github.com/parmarsanket/NEXPAD-Desktop/tree/main) | **Desktop Baseline Sync**: Merged stabilized networking, telemetry, and settings UI into main. |
| **17** | `2026-08-15 22:37` | [`feature/kmp-protocol-migration`](https://github.com/parmarsanket/NEXPAD-Desktop/tree/feature/kmp-protocol-migration) | **KMP Shared Protocol Migration**: Migrated desktop packet parser to use shared `protocol` library. |
| **18** | `2026-09-02 13:16` | [`feature/udp-qos-and-heartbeat-optimization`](https://github.com/parmarsanket/NEXPAD-Desktop/tree/feature/udp-qos-and-heartbeat-optimization) | **UDP QoS & Heartbeats**: Implemented packet sequence validation, dropping stale out-of-order packets. |
| **19** | `2026-09-03 17:39` | [`feature/aoa-winusb-poc`](https://github.com/parmarsanket/NEXPAD-Desktop/tree/feature/aoa-winusb-poc) | **AOA WinUSB Architecture**: Developed initial Named Pipe IPC server and libwdi driver swapping prototype. |
| **20** | `2026-09-04 01:23` | [`feature/aoa-winusb-improvements`](https://github.com/parmarsanket/NEXPAD-Desktop/tree/feature/aoa-winusb-improvements) | **AOA Bulk Streamer**: Implemented usb4java bulk transfer endpoints for Android accessory mode. |
| **21** | `2026-09-04 18:20` | [`feature/feedback-packet`](https://github.com/parmarsanket/NEXPAD-Desktop/tree/feature/feedback-packet) | **Haptic Feedback Callback**: Extracted game motor vibration values from ViGEmBus and emitted 8-byte UDP packets. |
| **22** | `2026-09-04 19:23` | [`feature/aoa-bugfixes`](https://github.com/parmarsanket/NEXPAD-Desktop/tree/feature/aoa-bugfixes) | **USB Connection Watchdog**: Handled sudden USB cable disconnects and device re-enumeration events. |
| **23** | `2026-09-04 21:55` | [`feature/aoa-bugfixes-v2`](https://github.com/parmarsanket/NEXPAD-Desktop/tree/feature/aoa-bugfixes-v2) | **WinUSB Descriptor Parser**: Enhanced interface descriptor matching across diverse Android OEM vendor IDs. |
| **24** | `2026-09-05 05:42` | [`feature/adb-bridge`](https://github.com/parmarsanket/NEXPAD-Desktop/tree/feature/adb-bridge) | **Integrated ADB Bridge**: Built automated `adb reverse` command launcher for seamless USB fallback. |
| **25** | `2026-09-06 03:58` | [`feature/bt-connection`](https://github.com/parmarsanket/NEXPAD-Desktop/tree/feature/bt-connection) | **Bluetooth Server Socket**: Added Windows Bluetooth RFCOMM serial connection listener. |
| **26** | `2026-09-06 08:01` | [`feature/homescreen-logic-improvements`](https://github.com/parmarsanket/NEXPAD-Desktop/tree/feature/homescreen-logic-improvements) | **Command Center Redesign**: Glassmorphic UI dashboard with live IP resolver, connection switchers, and metrics. |
| **27** | `2026-09-09 13:19` | [`feature/hud-metrics-and-plugins`](https://github.com/parmarsanket/NEXPAD-Desktop/tree/feature/hud-metrics-and-plugins) | **Desktop Plugin Manager & FTP**: Built FTP server and file pusher to upload custom HUD skins to phone. |
| **28** | `2026-09-10 01:25` | [`feature/nxprc-html-css-engine`](https://github.com/parmarsanket/NEXPAD-Desktop/tree/feature/nxprc-html-css-engine) | **NXPRC Plugin Studio Genesis**: Added visual authoring tab for `.nxprc` vector controller buttons. |
| **29** | `2026-09-11 02:40` | [`fix/nxprc-engine-bugs`](https://github.com/parmarsanket/NEXPAD-Desktop/tree/fix/nxprc-engine-bugs) | **Vector Geometry Tuning**: Fixed border radiuses, sweep gradients, and multi-color glow shaders. |
| **30** | `2026-09-11 10:57` | [`test/nxprc-engine-testing`](https://github.com/parmarsanket/NEXPAD-Desktop/tree/test/nxprc-engine-testing) | **Chrome Headless Test Pipeline**: Validated vector rendering with pixel-differential comparison against Chromium. |
| **31** | `2026-09-11 12:16` | [`test/nxprc-engine-testing-v2`](https://github.com/parmarsanket/NEXPAD-Desktop/tree/test/nxprc-engine-testing-v2) | **6 Gamepad Categories Studio**: Added authoring templates for ABXY, D-Pad, Thumbsticks, Bumpers, Triggers, and System buttons. |
| **32** | `2026-09-12 09:06` | [`test/nxprc-engine-testing-v3`](https://github.com/parmarsanket/NEXPAD-Desktop/tree/test/nxprc-engine-testing-v3) | **Universal Keyframe Studio & 3 Pillars**: Implemented dynamic timeline track keyframes, joystick physics, and 3-pillar docs. |

---

## 🔍 How to Browse Historical Code
To view any development branch directly on GitHub:
👉 **[github.com/parmarsanket/NEXPAD-Desktop/branches](https://github.com/parmarsanket/NEXPAD-Desktop/branches)**
*(Note: Older branches appear under the "Stale" tab or via branch search)*
