# 🖥️ NEXPAD Desktop (Virtual Controller Server & Studio)

**NEXPAD Desktop** is a high-performance Windows companion server and authoring studio built with **Compose Multiplatform (Desktop)** and **JNA (Java Native Access)**. It bridges your Android smartphone to Windows, injecting high-speed gamepad inputs directly into the Windows Kernel via `ViGEmBus` to emulate a genuine physical Xbox 360 controller.

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

## 🧭 Documentation Pillars

- 📖 **[`HISTORY.md`](./HISTORY.md)** — **Where We Came From**: Chronological timeline of all 32 development branches from Master Genesis to the Universal Timeline Track Studio.
- 🗺️ **[`ROADMAP.md`](./ROADMAP.md)** — **Where We're Going**: Inno Setup / MSIX zero-click packaging, DualSense haptic emulation, and standalone background driver services.
- 🏛️ **[`ARCHITECTURE.md`](./ARCHITECTURE.md)** — **How It Works**: ViGEmBus C++ driver JNA kernel bindings, 1000Hz UdpServer, AOA WinUSB endpoints, and Compose Multiplatform architecture.

---

## 🚀 Key Features

1. **Kernel-Level Xbox 360 Emulation (`ViGEmBus`)**:
   - Uses low-level JNA bindings to feed raw 16-bit binary reports (`XUSBReport`) directly to the Windows Virtual Gamepad Emulation Bus.
   - 100% native compatibility across Steam, Epic Games, Xbox Game Pass, and standalone PC titles (Forza Horizon, GTA V, Cyberpunk 2077).
2. **Multi-Transport Server Engine**:
   - **Zero-Driver USB (AOA)**: Communicates directly with Android Open Accessory endpoints over USB with sub-millisecond latency.
   - **1000Hz UDP Socket Server**: Asynchronous NIO server receiving 44-byte binary packets and dispatching 8-byte rumble feedback packets.
   - **ADB Reverse TCP Bridge**: High-reliability fallback bridge over USB debugging.
   - **Bluetooth RFCOMM**: Wireless connection over Windows Bluetooth serial ports.
3. **CemuHook DSU Motion Server**:
   - Integrated motion server streaming 6-axis gyroscope and accelerometer data over UDP port 26760 for emulators (Cemu, Yuzu, Ryujinx, RPCS3, Dolphin).
4. **NXPRC Plugin Studio & Designer**:
   - Built-in visual designer for authoring `.nxprc` vector controller components with instant one-click push to Android via ADB or FTP.
5. **Modern Glassmorphic Cyberpunk UI**:
   - Real-time network telemetry, RTT latency monitoring, connection state switcher, and driver health indicators.

---

## 🛠️ Build & Run

### Prerequisites
- **JDK 17+**
- **ViGEmBus Driver** installed on Windows ([Download ViGEmBus](https://github.com/nefarius/ViGEmBus/releases))

### Running Desktop App
```bash
# Run Compose Multiplatform Desktop Application
./gradlew :desktopApp:run

# Compile Kotlin JVM sources
./gradlew :desktopApp:compileKotlin
```