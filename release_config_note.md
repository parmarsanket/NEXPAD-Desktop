# NEXPAD Desktop Release Configuration Note

Here is exactly how it will work when you are ready to publish NEXPAD Desktop:

### 1. The DLL File (vigemclient.dll)
Your users will never have to download or touch this file. When we build the final release version of the Desktop App (for example, creating a .msi or .exe installer file), we will configure the build system to bundle vigemclient.dll directly inside your app's installation folder automatically. It will be completely invisible to the user.

### 2. The ViGEmBus Kernel Driver
Unfortunately, Windows security strictly prevents any app from pretending to be a real hardware controller (like an Xbox controller) without a kernel-level driver. Because of this, the ViGEmBus driver MUST be installed on the user's PC. This is a Microsoft Windows limitation, and every major app (like DS4Windows, Parsec, Moonlight, x360ce) has to deal with it.

However, we can make this painless for the user in two ways:

* **Option A (Bundled Installer):** When we create the final .msi or .exe installer for NEXPAD Desktop, we can bundle the ViGEmBusSetup.exe inside it. When the user installs your app, your installer will automatically run the ViGEmBus setup in the background.
* **Option B (In-App Prompt - Recommended):** We can program the NEXPAD Desktop App to check if ViGEmBus is installed when it opens. If it is not installed, we can show a beautiful screen that says: *"NEXPAD requires a Virtual Controller Driver to work with games."* with a big **"Install Driver"** button. When they click it, the app will automatically download and run the official ViGEmBus installer for them.
