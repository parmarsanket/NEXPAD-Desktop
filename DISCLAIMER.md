# ⚖️ Legal Disclaimer, Trademark Notice & Fair Use Statement

**Project:** NEXPAD Desktop (Virtual Controller Server & Studio)  
**Author / Copyright Holder:** Sanket Parmar  
**License:** Apache License, Version 2.0  
**Last Updated:** October 2, 2026

---

## 1. Unofficial & Independent Open-Source Project
**NEXPAD Desktop** is an independent, non-commercial open-source software project.  
It is **NOT affiliated, associated, authorized, endorsed by, or in any way officially connected** with:
- **Microsoft Corporation** or any of its subsidiaries, brands, or affiliates (including **Xbox**, **Xbox 360**, **Xbox One**, **Xbox Series X|S**, or **Windows**).
- **Sony Interactive Entertainment Inc.** or any of its subsidiaries or affiliates (including **PlayStation**, **PS4**, **PS5**, **DualShock**, or **DualSense**).
- **Nintendo Co., Ltd.** or any of its subsidiaries or affiliates (including **Nintendo Switch**, **Wii U**, or **GameCube**).
- **Valve Corporation** (including **Steam**).
- The developers of third-party emulators (including **Cemu**, **Yuzu**, **Ryujinx**, **RPCS3**, or **Dolphin**).

---

## 2. Trademark Notices & Nominative Fair Use
All registered trademarks, company names, product names, and brand names mentioned within this repository or user interface belong strictly to their respective owners:
- *"Xbox"*, *"Xbox 360"*, *"Xbox One"*, *"Xbox Series X|S"*, and *"Windows"* are registered trademarks of Microsoft Corporation.
- *"PlayStation"*, *"DualShock"*, and *"DualSense"* are registered trademarks of Sony Interactive Entertainment Inc.
- *"Nintendo"*, *"Switch"*, and *"Wii U"* are registered trademarks of Nintendo Co., Ltd.
- *"Steam"* is a registered trademark of Valve Corporation.

The mention of these trademarks in NEXPAD Desktop is strictly for **Nominative Fair Use** (describing hardware emulation compatibility, controller profile mappings, and technical interoperability). Their reference does **NOT** imply any endorsement, sponsorship, affiliation, or commercial partnership by the respective trademark owners.

---

## 3. Third-Party Virtual Gamepad Drivers (`ViGEmBus`)
NEXPAD Desktop interfaces with the open-source **ViGEmBus** driver (Virtual Gamepad Emulation Framework by Nefarius Software Solutions e.U.) via JNA (Java Native Access) to inject virtual controller reports into the Windows Kernel:
- **Independent Project**: ViGEmBus is an independent open-source software project developed by Benjamin Höglinger-Stelzer (Nefarius) and its community contributors.
- **Separate Licensing**: ViGEmBus and ViGEmClient are licensed under their respective open-source licenses (MIT/BSD/GPL). NEXPAD Desktop does not claim ownership or authorship of the ViGEmBus driver framework.
- **Kernel-Level Disclaimer**: While ViGEmBus is a widely used and signed Windows driver, the installation and operation of any kernel-mode device driver carries inherent risks. Users install and run device drivers at their own risk.

---

## 4. Clean-Room Implementation & Absence of Proprietary Binaries
- **Zero Proprietary Code**: NEXPAD Desktop was developed independently through clean-room software engineering. It contains **no reverse-engineered proprietary Microsoft, Sony, or Nintendo code, no extracted console firmware, and no confidential SDKs**.
- **Public Communication Protocols**: All network protocols (1000Hz UDP binary stream, CemuHook DSU motion protocol over port 26760, and AOA USB endpoints) utilize public networking standards and open protocol specifications.

---

## 5. Anti-Cheat & Third-Party Game Terms of Service
NEXPAD Desktop acts strictly as a virtual gamepad input bridge:
- It generates standard 1:1 human gamepad reports without macros, rapid-fire scripts, memory tampering, or aimbots.
- Users are solely responsible for ensuring that using virtual gamepad drivers complies with the Terms of Service, EULAs, and rules of the specific games or online multiplayer platforms they play.
- The author of NEXPAD Desktop bears no responsibility for third-party anti-cheat detections, game account suspensions, or service bans.

---

## 6. Disclaimer of Warranty & Limitation of Liability
As stated in Section 7 and Section 8 of the Apache License 2.0:

> **THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, AND NONINFRINGEMENT.**  
> **IN NO EVENT SHALL THE AUTHOR OR COPYRIGHT HOLDER BE LIABLE FOR ANY CLAIM, DAMAGES, SYSTEM CRASHES, DRIVER CONFLICTS, BLUE SCREENS (BSOD), HARDWARE MALFUNCTION, DATA LOSS, OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT, OR OTHERWISE, ARISING FROM, OUT OF, OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.**

---

## 7. Contact for Copyright & Legal Inquiries
For questions, licensing queries, or trademark/copyright concerns, please reach out to:
- **Developer:** Sanket Parmar
- **Email:** parmarsanket265@gmail.com
