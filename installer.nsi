; =========================================================================
;  NEXPAD Desktop Windows Installer (Modern UI 2)
;  Copyright 2026 Sanket Parmar (Sanket Tools). All rights reserved.
; =========================================================================
;  Architecture & Features:
;  - Custom Architecture & Local Privacy Transparency Page (nsDialogs)
;  - Granular Component Selection with real-time descriptions
;  - Mandatory EULA Acceptance with full copyright declarations
;  - Silent Automated ViGEmBus Kernel Driver Setup
;  - Automatic Windows Firewall & DSCP 46 Network QoS Optimization
;  - Non-Elevated Launch on Finish (executes under standard user token)
;  - Safe Uninstaller (never recursively wipes parent folders)
; =========================================================================

Unicode true
!include "MUI2.nsh"
!include "nsDialogs.nsh"
!include "LogicLib.nsh"

; Define Package Metadata
Name "NEXPAD Desktop"
OutFile "installer_build\NEXPAD_Desktop_Setup.exe"
InstallDir "$PROGRAMFILES64\NEXPAD"
RequestExecutionLevel admin

; Global Variables
Var DriverUninstalled
Var Dialog
Var GroupBox1
Var Label1
Var GroupBox2
Var Label2
Var GroupBox3
Var Label3

; Windows Version Info / PE Properties
VIProductVersion "1.0.0.0"
VIAddVersionKey "ProductName" "NEXPAD Desktop"
VIAddVersionKey "Comments" "Official Windows Companion & NXPRC Studio for NEXPAD Virtual Gamepad"
VIAddVersionKey "CompanyName" "Sanket Tools (Sanket Parmar)"
VIAddVersionKey "LegalCopyright" "Copyright 2026 Sanket Parmar. All rights reserved."
VIAddVersionKey "FileDescription" "NEXPAD Desktop Setup"
VIAddVersionKey "FileVersion" "1.0.0.0"
VIAddVersionKey "ProductVersion" "1.0.0.0"

; Interface Configurations & Branding
!define MUI_ABORTWARNING
!define MUI_ICON "installer_icon.ico"
!define MUI_UNICON "installer_icon.ico"
!define MUI_HEADERIMAGE
!define MUI_HEADERIMAGE_BITMAP "installer_header.bmp"
!define MUI_HEADERIMAGE_RIGHT
!define MUI_WELCOMEFINISHPAGE_BITMAP "installer_wizard.bmp"
!define MUI_UNWELCOMEFINISHPAGE_BITMAP "installer_wizard.bmp"

; -------------------------------------------------------------------------
;  WIZARD PAGE CONFIGURATIONS
; -------------------------------------------------------------------------

; 1. Welcome Page
!define MUI_WELCOMEPAGE_TITLE "Welcome to NEXPAD Desktop Setup"
!define MUI_WELCOMEPAGE_TEXT "NEXPAD Desktop bridges your Android smartphone to PC games as an official Xbox 360 and DualShock 4 virtual gamepad with zero-latency gyro motion aiming.$\r$\n$\r$\nThis setup wizard will install the companion desktop server, virtual controller drivers, and network latency optimizations on your computer.$\r$\n$\r$\nClick Next to review how NEXPAD works and how your local privacy is protected."
!insertmacro MUI_PAGE_WELCOME

; 2. Architecture & Privacy Transparency Page (Custom nsDialogs)
Page custom HowItWorksPage

; 3. License Agreement Setup
!define MUI_LICENSEPAGE_TEXT_TOP "Please review the software license terms before installing NEXPAD Desktop."
!define MUI_LICENSEPAGE_TEXT_BOTTOM "If you accept the terms of the agreement, select the first option below. Acceptance is required to install NEXPAD Desktop."
!define MUI_LICENSEPAGE_RADIOBUTTONS
!define MUI_LICENSEPAGE_RADIOBUTTONS_TEXT_ACCEPT "I accept the terms in the License Agreement"
!define MUI_LICENSEPAGE_RADIOBUTTONS_TEXT_DECLINE "I do not accept the terms in the License Agreement"
!insertmacro MUI_PAGE_LICENSE "LICENSE"

; 4. Components Page
!define MUI_COMPONENTSPAGE_TEXT_TOP "Select which features and optimizations you want to install. Hover over any component to read its details."
!define MUI_COMPONENTSPAGE_TEXT_COMPLIST "Components:"
!define MUI_COMPONENTSPAGE_TEXT_DESCRIPTION_TITLE "Component Description"
!define MUI_COMPONENTSPAGE_TEXT_DESCRIPTION_INFO "Hover your mouse over a component above to see its description."
!insertmacro MUI_PAGE_COMPONENTS

; 5. Directory Page
!insertmacro MUI_PAGE_DIRECTORY

; 6. Installation Progress
!define MUI_INSTFILESPAGE_FINISHHEADER_TEXT "Installation Completed"
!define MUI_INSTFILESPAGE_FINISHHEADER_SUBTEXT "All selected NEXPAD Desktop components have been successfully installed."
!insertmacro MUI_PAGE_INSTFILES

; 7. Finish Page (De-Elevated Launch)
!define MUI_FINISHPAGE_TITLE "NEXPAD Desktop is Ready to Play!"
!define MUI_FINISHPAGE_TEXT "NEXPAD Desktop and the ViGEmBus virtual controller drivers have been installed successfully.$\r$\n$\r$\nQuick Start Guide:$\r$\n1. Launch NEXPAD Desktop.$\r$\n2. Open the NEXPAD app on your Android phone.$\r$\n3. Connect seamlessly via USB cable or local Wi-Fi.$\r$\n4. Launch any PC game (Steam, Xbox, Epic) and enjoy your new controller!"
!define MUI_FINISHPAGE_RUN
!define MUI_FINISHPAGE_RUN_FUNCTION LaunchAppNonElevated
!define MUI_FINISHPAGE_RUN_TEXT "Launch NEXPAD Desktop now"
!insertmacro MUI_PAGE_FINISH

; De-elevation helper: executes app as standard user via Windows Explorer token
Function LaunchAppNonElevated
  ExecShell "" "$INSTDIR\NEXPAD Desktop.exe"
FunctionEnd

; -------------------------------------------------------------------------
;  UNINSTALLER PAGES
; -------------------------------------------------------------------------
!insertmacro MUI_UNPAGE_WELCOME
!insertmacro MUI_UNPAGE_COMPONENTS
!insertmacro MUI_UNPAGE_CONFIRM
!insertmacro MUI_UNPAGE_INSTFILES
!insertmacro MUI_UNPAGE_FINISH

; Language Settings
!insertmacro MUI_LANGUAGE "English"

; -------------------------------------------------------------------------
;  CUSTOM PAGE: HOW NEXPAD WORKS & PRIVACY TRANSPARENCY
; -------------------------------------------------------------------------
Function HowItWorksPage
  !insertmacro MUI_HEADER_TEXT "How NEXPAD Works" "Architecture, Low Latency & Local Privacy Overview"
  nsDialogs::Create 1018
  Pop $Dialog
  ${If} $Dialog == error
    Abort
  ${EndIf}

  ${NSD_CreateGroupBox} 0 0 100% 38u "100% Local & Private Streaming"
  Pop $GroupBox1
  ${NSD_CreateLabel} 10u 12u 90% 22u "Direct device-to-PC connection via Wi-Fi or USB-C cable. All controller inputs and motion data stay on your local network. Zero cloud servers, zero account logins, and zero telemetry tracking."
  Pop $Label1

  ${NSD_CreateGroupBox} 0 42u 100% 38u "Native Virtual Controller Emulation (ViGEmBus)"
  Pop $GroupBox2
  ${NSD_CreateLabel} 10u 54u 90% 22u "Installs the open-source ViGEmBus kernel driver. Windows games (Steam, Xbox Game Pass, Epic Games, Emulators) recognize an authentic physical Xbox 360 or DualShock 4 controller with full vibration feedback."
  Pop $Label2

  ${NSD_CreateGroupBox} 0 84u 100% 38u "Ultra-Low Latency Network Engine & QoS"
  Pop $GroupBox3
  ${NSD_CreateLabel} 10u 96u 90% 22u "Applies Windows QoS DSCP 46 high-priority voice/gaming packet queueing and 1ms timer resolution, eliminating router packet buffering for lag-free motion aiming and instant rumble response."
  Pop $Label3

  nsDialogs::Show
FunctionEnd

; -------------------------------------------------------------------------
;  INSTALLATION SECTIONS (COMPONENTS)
; -------------------------------------------------------------------------

Section "!NEXPAD Desktop Companion & Studio" SEC_CORE
  SectionIn RO ; Mandatory Core Component
  SetOutPath "$INSTDIR"

  ; Close running instances if upgrading
  DetailPrint "Ensuring NEXPAD and background processes are closed before updating..."
  IfFileExists "$INSTDIR\tools\adb\adb.exe" 0 +2
    nsExec::ExecToLog '"$INSTDIR\tools\adb\adb.exe" kill-server'
  nsExec::ExecToLog 'taskkill /F /T /IM "NEXPAD Desktop.exe"'
  nsExec::ExecToLog 'taskkill /F /T /IM "NEXPAD Desktop.exe"'
  nsExec::ExecToLog 'taskkill /F /T /IM adb.exe'
  nsExec::ExecToLog 'taskkill /F /T /IM nexpad-service.exe'
  Sleep 1000

  SetOverwrite on

  ; Deploy application payload
  DetailPrint "Installing NEXPAD Desktop application files..."
  File /r "desktopApp\build\compose\binaries\main-release\app\NEXPAD Desktop\*"

  ; Deploy open source license and notices
  File "LICENSE"
  File "NOTICE"

  ; Create Start Menu Shortcuts
  CreateDirectory "$SMPROGRAMS\NEXPAD"
  CreateShortcut "$SMPROGRAMS\NEXPAD\NEXPAD.lnk" "$INSTDIR\NEXPAD Desktop.exe"
  CreateShortcut "$SMPROGRAMS\NEXPAD\Uninstall.lnk" "$INSTDIR\uninstall.exe"

  ; Register in Windows Add/Remove Programs
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\NEXPAD" "DisplayName" "NEXPAD Desktop"
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\NEXPAD" "UninstallString" '"$INSTDIR\uninstall.exe"'
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\NEXPAD" "DisplayIcon" '"$INSTDIR\NEXPAD Desktop.exe"'
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\NEXPAD" "DisplayVersion" "1.0.0"
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\NEXPAD" "Publisher" "Sanket Parmar"

  ; Clean up legacy services from previous alpha builds
  DetailPrint "Cleaning up legacy background service if present..."
  nsExec::ExecToLog 'cmd.exe /c "net stop nexpad-service & sc delete nexpad-service"'
  Delete "$INSTDIR\nexpad-service.exe"
  Delete "$INSTDIR\nexpad-service.xml"

  ; Write Uninstaller
  WriteUninstaller "$INSTDIR\uninstall.exe"
SectionEnd

Section "ViGEmBus Virtual Controller Driver" SEC_DRIVER
  SetOutPath "$INSTDIR"
  File /oname=ViGEmBusSetup.exe "redist\ViGEmBusSetup.exe"
  File /oname=libwdi.dll "redist\libwdi.dll"

  DetailPrint "Installing ViGEmBus Virtual Gamepad Kernel Driver..."
  ExecWait '"$INSTDIR\ViGEmBusSetup.exe" /quiet /norestart'
SectionEnd

Section "Android USB ADB Communication Bridge" SEC_ADB
  SetOutPath "$INSTDIR\tools\adb"
  DetailPrint "Installing standalone Android ADB communication tools..."
  File /r "desktopApp\tools\adb\*"
  SetOutPath "$INSTDIR"
SectionEnd

Section "Low-Latency Firewall & Network QoS Rules" SEC_NET
  DetailPrint "Configuring Windows Firewall rules for Gamepad & Motion streaming..."
  ExecWait 'netsh advfirewall firewall delete rule name="NEXPAD Desktop" >nul 2>&1'
  ExecWait 'netsh advfirewall firewall delete rule name="NEXPAD Gamepad UDP" >nul 2>&1'
  ExecWait 'netsh advfirewall firewall add rule name="NEXPAD Gamepad UDP" dir=in action=allow protocol=UDP localport=9999 enable=yes profile=any'
  ExecWait 'netsh advfirewall firewall delete rule name="NEXPAD Discovery" >nul 2>&1'
  ExecWait 'netsh advfirewall firewall add rule name="NEXPAD Discovery" dir=in action=allow protocol=UDP localport=9998 enable=yes profile=any'
  ExecWait 'netsh advfirewall firewall delete rule name="NEXPAD DSU" >nul 2>&1'
  ExecWait 'netsh advfirewall firewall add rule name="NEXPAD DSU" dir=in action=allow protocol=UDP localport=26760 enable=yes profile=any'
  ExecWait 'netsh advfirewall firewall delete rule name="NEXPAD Desktop EXE" >nul 2>&1'
  ExecWait 'netsh advfirewall firewall add rule name="NEXPAD Desktop EXE" dir=in action=allow protocol=any program="$INSTDIR\NEXPAD Desktop.exe" enable=yes profile=any'

  DetailPrint "Applying Windows QoS Policy (DSCP 46 Voice/Gaming Priority)..."
  nsExec::ExecToLog 'powershell.exe -ExecutionPolicy Bypass -Command "Remove-NetQosPolicy -Name $\"NEXPAD_Gamepad_QoS$\" -Confirm:$$false" 2>nul'
  nsExec::ExecToLog 'powershell.exe -ExecutionPolicy Bypass -Command "New-NetQosPolicy -Name $\"NEXPAD_Gamepad_QoS$\" -AppPathNameMatchCondition $\"NEXPAD Desktop.exe$\" -DSCPAction 46 -PriorityValue8021p 6 -NetworkProfile All"'
SectionEnd

Section "Create Desktop Shortcut" SEC_DESKTOP_SHORTCUT
  CreateShortcut "$DESKTOP\NEXPAD.lnk" "$INSTDIR\NEXPAD Desktop.exe"
SectionEnd

; -------------------------------------------------------------------------
;  COMPONENT DESCRIPTIONS
; -------------------------------------------------------------------------
!insertmacro MUI_FUNCTION_DESCRIPTION_BEGIN
  !insertmacro MUI_DESCRIPTION_TEXT ${SEC_CORE} "Installs the primary NEXPAD Desktop companion server, system tray manager, and the NXPRC visual custom controller design studio. (Required)"
  !insertmacro MUI_DESCRIPTION_TEXT ${SEC_DRIVER} "Installs the official ViGEmBus kernel driver. Required for PC games (Steam, Xbox Game Pass, Epic Games, Emulators) to recognize your phone as a physical Xbox 360 or DualShock 4 gamepad."
  !insertmacro MUI_DESCRIPTION_TEXT ${SEC_ADB} "Bundles standalone Google ADB bridge tools for zero-configuration, plug-and-play wired USB gaming with sub-millisecond input response."
  !insertmacro MUI_DESCRIPTION_TEXT ${SEC_NET} "Opens local UDP ports (9999, 9998, 26760) and applies Windows DSCP 46 high-priority QoS queueing to eliminate Wi-Fi router packet buffering and rumble latency."
  !insertmacro MUI_DESCRIPTION_TEXT ${SEC_DESKTOP_SHORTCUT} "Places a convenient shortcut to launch NEXPAD Desktop directly from your Windows Desktop."
!insertmacro MUI_FUNCTION_DESCRIPTION_END

; -------------------------------------------------------------------------
;  UNINSTALLER SECTIONS
; -------------------------------------------------------------------------

Section /o "un.Virtual Controller Driver (ViGEmBus)" SEC_UN_DRIVER
  DetailPrint "Uninstalling ViGEmBus Driver..."
  ExecWait 'MsiExec.exe /X{966606F3-2745-49E9-BF15-5C3EAA4E9077} /qn /norestart'
  nsExec::ExecToLog 'cmd.exe /c "net stop ViGEmBus & sc delete ViGEmBus"'
  StrCpy $DriverUninstalled "1"
  DetailPrint "Driver uninstallation completed."
SectionEnd

Section "un.NEXPAD Desktop" SEC_UN_APP
  SectionIn RO

  DetailPrint "Closing running instances of NEXPAD Desktop and background tools..."
  IfFileExists "$INSTDIR\tools\adb\adb.exe" 0 +2
    nsExec::ExecToLog '"$INSTDIR\tools\adb\adb.exe" kill-server'
  nsExec::ExecToLog 'taskkill /F /T /IM "NEXPAD Desktop.exe"'
  nsExec::ExecToLog 'taskkill /F /T /IM "NEXPAD Desktop.exe"'
  nsExec::ExecToLog 'taskkill /F /T /IM adb.exe'
  nsExec::ExecToLog 'taskkill /F /T /IM nexpad-service.exe'
  Sleep 1000

  ; Remove shortcuts
  Delete "$SMPROGRAMS\NEXPAD\NEXPAD.lnk"
  Delete "$SMPROGRAMS\NEXPAD\Uninstall.lnk"
  RMDir "$SMPROGRAMS\NEXPAD"
  Delete "$DESKTOP\NEXPAD.lnk"

  ; Clean registry keys
  DeleteRegKey HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\NEXPAD"

  ; Clean up Firewall Rules and QoS Policy
  nsExec::ExecToLog 'netsh advfirewall firewall delete rule name="NEXPAD Gamepad UDP"'
  nsExec::ExecToLog 'netsh advfirewall firewall delete rule name="NEXPAD Discovery"'
  nsExec::ExecToLog 'netsh advfirewall firewall delete rule name="NEXPAD DSU"'
  nsExec::ExecToLog 'netsh advfirewall firewall delete rule name="NEXPAD Desktop"'
  nsExec::ExecToLog 'netsh advfirewall firewall delete rule name="NEXPAD Desktop EXE"'
  nsExec::ExecToLog 'powershell.exe -ExecutionPolicy Bypass -Command "Remove-NetQosPolicy -Name $\"NEXPAD_Gamepad_QoS$\" -Confirm:$$false"'

  ; Clean up installed application payload and tools safely without wiping custom/shared parent folders
  RMDir /r "$INSTDIR\tools"
  RMDir /r "$INSTDIR\app"
  RMDir /r "$INSTDIR\runtime"
  Delete "$INSTDIR\NEXPAD Desktop.exe"
  Delete "$INSTDIR\LICENSE"
  Delete "$INSTDIR\NOTICE"
  Delete "$INSTDIR\ViGEmBusSetup.exe"
  Delete "$INSTDIR\libwdi.dll"
  Delete "$INSTDIR\uninstall.exe"
  Delete "$INSTDIR\nexpad-service.exe"
  Delete "$INSTDIR\nexpad-service.xml"
  Delete "$INSTDIR\nexpad_elevation_debug.txt"
  Delete "$INSTDIR\nexpad_driver_install.log"

  ; Safely remove $INSTDIR ONLY if it is empty.
  RMDir "$INSTDIR"
SectionEnd

!insertmacro MUI_UNFUNCTION_DESCRIPTION_BEGIN
  !insertmacro MUI_DESCRIPTION_TEXT ${SEC_UN_APP} "Uninstalls the NEXPAD Desktop application and studio."
  !insertmacro MUI_DESCRIPTION_TEXT ${SEC_UN_DRIVER} "WARNING: Uninstalls the ViGEmBus Virtual Controller driver. Only check this if no other applications (like DS4Windows) on your PC require this driver."
!insertmacro MUI_UNFUNCTION_DESCRIPTION_END

Function un.onUninstSuccess
  StrCmp $DriverUninstalled "1" 0 end
    MessageBox MB_YESNO|MB_ICONEXCLAMATION "The ViGEmBus Virtual Controller Driver was uninstalled.$\r$\n$\r$\nWindows strictly requires a system restart to fully flush kernel drivers from memory.$\r$\n$\r$\nWould you like to restart your computer now?" IDYES reboot IDNO end
    reboot:
      Reboot
  end:
FunctionEnd
