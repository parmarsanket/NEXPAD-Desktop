# 💻 NEXPAD-Desktop (Virtual Controller Server)

Welcome to the **NEXPAD-Desktop** App! This is the PC server application that receives inputs over Wi-Fi from your NEXPAD Android app, and magically tricks Windows into thinking a physical Xbox 360 controller is plugged in!

This README is a **beginner-friendly guide** to help you understand how this desktop application works and where the important logic is located.

---

## 🏗️ How the Code is Structured

This is a Kotlin project built using **Compose Multiplatform** for the Desktop UI, and **JNA (Java Native Access)** to talk directly to the Windows Kernel.

All the main code lives in this folder:
`desktopApp/src/main/kotlin/com/sanket/tools/nexpaddesktop/`

### 1. 🛞 The Driver (`/driver`)
This is the most critical part of the app. It connects to the Windows OS.
*   **`VirtualGamepadDriver.kt`**: This class acts as the bridge to Windows. It asks Windows to create a fake Xbox 360 controller in the Device Manager.
*   **`ViGEmInputMapper.kt`**: This file contains a massive mapping function. It takes the simple `GamepadInput` from Android (like `btnA = true`) and translates it into raw binary Hex codes (`XUSBReport.A`) that the Windows Kernel understands.
*   **`/jna` folder**: Contains advanced, low-level Java Native Access bindings that allow Kotlin code to talk directly to the `ViGEmBus` C++ driver installed on your PC.

### 2. 🌐 The Network (`/network`)
This folder listens for the Android app.
*   **`UdpServer.kt`**: This is a server that listens on port `9999`. It waits for fast UDP packets coming from your phone, unpacks the JSON into a `GamepadInput` object, and passes it to the driver.
*   **`DsuServer.kt`**: A specialized server that runs on a different port. It specifically handles Gyroscope and Accelerometer data using the open-source DSU (DualShock4 UDP) protocol, allowing emulators like Yuzu and Cemu to read motion controls.

### 3. 🖥️ The UI (`/ui`)
*   **`MainScreen.kt`**: A simple Compose Desktop user interface that shows your PC's IP address (so you know what to type into your phone) and shows whether the controller is currently Connected or Disconnected.

### 4. 📦 The Data (`/model`)
*   **`GamepadInput.kt`**: The exact same data class used in the Android app. It ensures both the phone and the PC speak the exact same language when discussing buttons and joysticks.

---

## 🚀 How the App Actually Works (Step-by-Step)

1. **Listening for Packets**: The `UdpServer.kt` is constantly listening to your Wi-Fi network.
2. **Receiving Data**: It receives a JSON string from the Android app that says: `{"btnA": true, "leftStickY": 1.0}`.
3. **Translating**: It converts that JSON text back into a Kotlin `GamepadInput` object and hands it to the `VirtualGamepadDriver`.
4. **Mapping**: The driver gives the object to `ViGEmInputMapper.kt`, which translates `btnA` into the official Xbox 360 binary code (`0x1000`).
5. **Pressing the Button**: The driver injects that binary code directly into the Windows Kernel using JNA.
6. **Game Responds**: Your PC game (like Forza Horizon or GTA V) sees that the "A" button on an Xbox 360 controller was pressed, and your character jumps!

*All of this happens instantly, dozens of times a second!*