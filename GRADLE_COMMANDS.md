# NEXPADDesktop Gradle Commands

Always run these from the desktop project folder:

```powershell
cd "C:\Users\parma\OneDrive\Desktop\NEXPAD Project\NEXPADDesktop"
```

Use `.\gradlew.bat`, not `.\gralaw.bad`.

## Basic

```powershell
.\gradlew.bat projects
.\gradlew.bat tasks --all
.\gradlew.bat clean
```

## Run App

```powershell
.\gradlew.bat :desktopApp:run
```

Run with command-line arguments:

```powershell
.\gradlew.bat :desktopApp:run --args="your arguments here"
```

## Debug App

Starts the app paused and waits for a debugger on port `5005`.

```powershell
.\gradlew.bat :desktopApp:run --debug-jvm
```

Hot reload with debugger:

```powershell
.\gradlew.bat :desktopApp:hotRun --debug-jvm
```

## Hot Reload

Run with hot reload enabled:

```powershell
.\gradlew.bat :desktopApp:hotRun
```

Run with automatic reload when files change:

```powershell
.\gradlew.bat :desktopApp:hotRun --auto
```

## Build

```powershell
.\gradlew.bat :desktopApp:build
.\gradlew.bat :desktopApp:assemble
```

## Package Installers

Build MSI installer:

```powershell
.\gradlew.bat :desktopApp:packageMsi
```

Build EXE installer:

```powershell
.\gradlew.bat :desktopApp:packageExe
```

Build all package types for current Windows OS:

```powershell
.\gradlew.bat :desktopApp:packageDistributionForCurrentOS
```

Build release MSI:

```powershell
.\gradlew.bat :desktopApp:packageReleaseMsi
```

Build release EXE:

```powershell
.\gradlew.bat :desktopApp:packageReleaseExe
```

Build release packages for current Windows OS:

```powershell
.\gradlew.bat :desktopApp:packageReleaseDistributionForCurrentOS
```

## Output Locations

MSI output:

```text
desktopApp\build\compose\binaries\main\msi\
```

EXE output:

```text
desktopApp\build\compose\binaries\main\exe\
```

Release MSI output:

```text
desktopApp\build\compose\binaries\main-release\msi\
```

Release EXE output:

```text
desktopApp\build\compose\binaries\main-release\exe\
```
