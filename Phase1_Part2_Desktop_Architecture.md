# Phase 1 (Part 2): Desktop Companion App Architecture

The core Android app is successfully transmitting UDP packets. The next step is building the KMP Desktop Companion app to receive these packets and translate them into a virtual gamepad using ViGEmBus.

## Proposed Changes

### 1. Shared Models
*   Copy `GamepadInput.kt` into the `shared` KMP module.
*   Add `kotlinx-serialization-json` to the KMP build script so both Android and Desktop can parse the exact same payload.

### 2. Networking (UDP Receiver)
*   **Library:** Ktor Network (UDP).
*   **Implementation:** Create a `UdpServer` class in the `desktopApp` module that listens on port `9999`. 
*   It will run a coroutine loop, deserialize incoming byte packets back into the `GamepadInput` data class at 60Hz.

### 3. Driver Integration (ViGEmBus via JNA)
*   Since Kotlin/Java cannot talk directly to a C++ Windows Kernel Driver, we will use **JNA (Java Native Access)**.
*   We will define an interface mapping to `vigemclient.dll`.
*   We will create a `VirtualGamepadDriver` class that takes the parsed `GamepadInput` and maps it to the `XUSB_REPORT` C-struct.

## Verification Plan
1. Start the KMP Desktop App on your PC.
2. Launch the Android App on your phone.
3. Press a button on the phone and verify the Desktop App console prints the received JSON.
4. Verify Windows `joy.cpl` registers a new Xbox 360 controller.
