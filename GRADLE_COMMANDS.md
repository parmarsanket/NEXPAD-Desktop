# 📜 NEXPAD Desktop Gradle & Build Commands

This document provides a reference cheat-sheet for compiling, running, testing, packaging, and producing installers for **NEXPAD Desktop**.

Always run these commands from the `NEXPADDesktop` project root directory:

```powershell
cd "C:\Users\parma\OneDrive\Desktop\NEXPAD Project\NEXPADDesktop"
```

> [!NOTE]
> On Windows PowerShell, execute using `.\gradlew.bat`. When combining commands, use `;` as a statement separator (do not use `&&`).

---

## ⚡ 1. Fast Compilation & Validation

Compile Kotlin JVM source files without packaging overhead:

```powershell
# Fast compilation of desktopApp Kotlin sources
.\gradlew.bat :desktopApp:compileKotlin

# Fast compilation of shared protocol library
.\gradlew.bat :shared:compileKotlinJvm

# Clean build artifacts
.\gradlew.bat clean
```

---

## 🚀 2. Run & Debug Application

```powershell
# Run Desktop Application in development mode
.\gradlew.bat :desktopApp:run

# Run with custom command-line arguments
.\gradlew.bat :desktopApp:run --args="your arguments here"

# Launch in debug mode (pauses and waits for remote debugger on port 5005)
.\gradlew.bat :desktopApp:run --debug-jvm

# Run with Compose Hot Reload
.\gradlew.bat :desktopApp:hotRun

# Run with automatic reload on file changes
.\gradlew.bat :desktopApp:hotRun --auto
```

---

## 🧪 3. Testing & Code Quality

```powershell
# Run desktopApp unit and integration tests
.\gradlew.bat :desktopApp:test

# Run all test suites across all modules
.\gradlew.bat test
```

---

## 📦 4. Application Packaging (Compose Multiplatform)

### Distributable Binaries
```powershell
# Create unpacked application directory
.\gradlew.bat :desktopApp:createDistributable

# Create release unpacked application directory
.\gradlew.bat :desktopApp:packageReleaseApp
```

### MSI & Native Installers
```powershell
# Build Windows MSI installer (debug)
.\gradlew.bat :desktopApp:packageMsi

# Build Windows MSI installer (release)
.\gradlew.bat :desktopApp:packageReleaseMsi

# Build standard EXE package
.\gradlew.bat :desktopApp:packageReleaseExe

# Build all package types for current Windows OS
.\gradlew.bat :desktopApp:packageReleaseDistributionForCurrentOS
```

---

## 🛠️ 5. All-in-One NSIS Setup Generation

The production installer (`NEXPAD_Desktop_Setup.exe`) bundles the application runtime, ADB platform tools, firewall rules, and the silent ViGEmBus kernel driver installer:

```powershell
# Step 1: Generate release distributable binaries
.\gradlew.bat :desktopApp:packageReleaseApp

# Step 2: Compile NSIS installer (requires makensis on PATH)
makensis installer.nsi
```

The resulting setup executable is generated at:
```text
installer_build\NEXPAD_Desktop_Setup.exe
```

---

## 📂 6. Build Output Locations

| Artifact | Output Path |
|---|---|
| **Unpacked App (Release)** | `desktopApp\build\compose\binaries\main-release\app\NEXPAD Desktop\` |
| **Release MSI** | `desktopApp\build\compose\binaries\main-release\msi\` |
| **Release EXE** | `desktopApp\build\compose\binaries\main-release\exe\` |
| **All-in-One NSIS Setup** | `installer_build\NEXPAD_Desktop_Setup.exe` |
