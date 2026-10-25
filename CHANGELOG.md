# 📝 Changelog

All notable changes to the **NEXPAD Desktop** project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

---

## [1.0.0] - 2026-10-25

### 🚀 Added
- **Multi-Transport Server Engine**:
  - Non-blocking Java NIO UDP server listening on port `9999` with strict 200 Hz polling rate ($5\,\text{ms}$ ticks).
  - Real-time honest microsecond Round-Trip Time (RTT), jitter calculation, and packet delivery telemetry.
  - Heartbeat watchdog timer automatically zeroing controller axes after $3000\,\text{ms}$ of silence.
- **Universal Android Open Accessory (AOA 2.0)**:
  - High-throughput USB Bulk IN/OUT streaming for sub-millisecond input latency.
  - Protocol-driven dynamic Windows Plug-and-Play discovery matching `MS_COMP_MTP` and class `WPD` across all Android OEMs without static vendor ID whitelists.
  - Deterministic protocol error branching: `LIBUSB_ERROR_NOT_SUPPORTED (-12)` routes to root composite WinUSB driver installation, while `LIBUSB_ERROR_ACCESS (-3)` scopes to MTP interface driver installation.
- **ADB Reverse Bridge**:
  - Automated USB fallback via bundled platform-tools (`adb reverse tcp:9999 tcp:9999` and `adb reverse tcp:9997 tcp:9997`).
  - Virtual emulator isolation filtering out Android Studio emulators (`emulator-*`) so background virtual devices never suppress physical USB accessories.
- **Windows Kernel Virtual Controller Emulation**:
  - Direct JNA native bindings interfacing with `ViGEmClient.dll` to spawn a virtual `Xbox 360 Controller for Windows` in Windows Device Manager.
  - Rapid input dispatch converting 44-byte binary packets into native C `XUSB_REPORT` structures in $< 0.1\,\text{ms}$.
  - Bidirectional haptic force feedback: extracts game motor rumble callbacks and streams 8-byte UDP feedback packets to the mobile phone.
- **CemuHook DSU Motion Server**:
  - UDP server on port `26760` broadcasting normalized 6-axis gyroscope and accelerometer data to console emulators (Cemu, Yuzu, Ryujinx, Dolphin).
- **Winsock RFCOMM Bluetooth Server**:
  - Native Windows RFCOMM serial listener with burst coalescing and rate-limited telemetry.
- **Single Active Transport Coordinator**:
  - Mutual-exclusion arbitration ensuring only one transport (`USB_AOA`, `USB_ADB`, `WIFI`, or `BLUETOOTH`) streams active inputs at any moment.
- **NXPRC Plugin Studio & Designer**:
  - Integrated visual authoring tab for `.nxprc` vector controller packages.
  - 3-zone Layer Studio modal featuring visual stage, layer hierarchy, and CSS-like code editor with live preview.
  - One-click package deployment to mobile devices via local FTP server (port `9996`) or ADB.
- **All-in-One NSIS Setup Installer**:
  - Production installer (`NEXPAD_Desktop_Setup.exe`) bundling JRE runtime, platform-tools, `libwdi.dll`, and `ViGEmBusSetup.exe`.
  - Automated silent ViGEmBus installation with zero external download requirements.
  - Automated Windows Firewall rule registration for inbound UDP port `9999` and discovery port `9998`.
- **System Tray Integration**:
  - Windows taskbar system tray integration with minimize-to-tray and background gaming execution mode.

### ⚡ Changed
- Replaced legacy JSON string protocol with high-speed 44-byte raw binary packet parser (`NexpadProtocol`) with zero-allocation memory reuse.
- Standardized all network polling and timer tick rates strictly to **200 Hz** ($5\,\text{ms}$ intervals).
- Modernized project documentation with clickable sub-system navigation and multi-platform distribution matrices.

### 🛡️ Fixed
- Fixed native JNA pointer memory corruption and thread starvation in high-concurrency UDP receiver loop.
- Calibrated DualShock 4 motion stillness drift filter and aligned 6-axis gyro/accelerometer coordinate mapping.
- Prevented CRT assertion failure in `poll_windows` on application exit in the libusb Windows backend.
- Eliminated Android UI thread ANRs during rapid joystick touch tracking.
