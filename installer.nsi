; ==========================================
;  NEXPAD Desktop Windows Installer (NSIS)
; ==========================================
;  To compile this:
;  1. Build the app using Gradle: .\gradlew.bat :desktopApp:packageApp
;  2. Place the official ViGEmBus installer inside "redist" folder as "ViGEmBusSetup.exe"
;  3. Run the compiler: makensis installer.nsi

Unicode true
!include "MUI2.nsh"

; Define Basic Package Info
Name "NEXPAD Desktop"
OutFile "installer_build\NEXPAD_Setup.exe"
InstallDir "$PROGRAMFILES64\NEXPAD"
RequestExecutionLevel admin

; Variables
Var DriverUninstalled

; Interface Configurations
!define MUI_ABORTWARNING
!define MUI_WELCOMEPAGE_TITLE "Welcome to NEXPAD"
!define MUI_WELCOMEPAGE_TEXT "This will install NEXPAD Desktop and configure the required virtual controller drivers on your computer.$\r$\n$\r$\nClick Next to continue."

; Pages
!insertmacro MUI_PAGE_WELCOME
!insertmacro MUI_PAGE_DIRECTORY
!insertmacro MUI_PAGE_INSTFILES
!define MUI_FINISHPAGE_RUN "$INSTDIR\com.sanket.tools.nexpaddesktop.exe"
!define MUI_FINISHPAGE_RUN_TEXT "Launch NEXPAD Desktop"
!insertmacro MUI_PAGE_FINISH

; Uninstaller Pages
!insertmacro MUI_UNPAGE_WELCOME
!insertmacro MUI_UNPAGE_COMPONENTS
!insertmacro MUI_UNPAGE_CONFIRM
!insertmacro MUI_UNPAGE_INSTFILES
!insertmacro MUI_UNPAGE_FINISH

; Language Settings
!insertmacro MUI_LANGUAGE "English"

Section "Install"
  ; Set output path to installation directory
  SetOutPath "$INSTDIR"
  
  ; Bundle all pre-compiled files from the packageApp task
  File /r "desktopApp\build\compose\binaries\main\app\com.sanket.tools.nexpaddesktop\*"
        
  ; Bundle the driver installer inside the installation directory so the uninstaller can use it later
  ; NOTE: SetOutPath is already "$INSTDIR" above, so /oname just needs the filename
  File /oname=ViGEmBusSetup.exe "redist\ViGEmBusSetup.exe"

  ; Write Uninstaller
  WriteUninstaller "$INSTDIR\uninstall.exe"

  ; Create shortcuts
  CreateDirectory "$SMPROGRAMS\NEXPAD"
  CreateShortcut "$SMPROGRAMS\NEXPAD\NEXPAD.lnk" "$INSTDIR\com.sanket.tools.nexpaddesktop.exe"
  CreateShortcut "$SMPROGRAMS\NEXPAD\Uninstall.lnk" "$INSTDIR\uninstall.exe"
  CreateShortcut "$DESKTOP\NEXPAD.lnk" "$INSTDIR\com.sanket.tools.nexpaddesktop.exe"

  ; Write registry keys for Windows Add/Remove Programs
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\NEXPAD" "DisplayName" "NEXPAD Desktop"
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\NEXPAD" "UninstallString" '"$INSTDIR\uninstall.exe"'
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\NEXPAD" "DisplayIcon" '"$INSTDIR\com.sanket.tools.nexpaddesktop.exe"'
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\NEXPAD" "DisplayVersion" "1.0.0"
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\NEXPAD" "Publisher" "Sanket Tools"

  ; Add Windows Firewall exception so the phone can send UDP packets to this app
  ; We add both the exe AND open port 9999 explicitly so Windows doesn't block either
  ExecWait 'netsh advfirewall firewall delete rule name="NEXPAD Desktop" >nul 2>&1'
  ExecWait 'netsh advfirewall firewall add rule name="NEXPAD Desktop" dir=in action=allow protocol=UDP localport=9999 enable=yes profile=any'
  ExecWait 'netsh advfirewall firewall add rule name="NEXPAD Desktop EXE" dir=in action=allow protocol=any program="$INSTDIR\com.sanket.tools.nexpaddesktop.exe" enable=yes profile=any'

  ; Apply Windows QoS Policy to force DSCP 46 (EF/Voice) tagging on UDP feedback packets
  ; This ensures low jitter over WMM routers for the PC-to-Android rumble stream
  DetailPrint "Applying Windows QoS Policy (DSCP 46)..."
  nsExec::ExecToLog 'powershell.exe -ExecutionPolicy Bypass -Command "Remove-NetQosPolicy -Name $\"NEXPAD_Gamepad_QoS$\" -Confirm:$$false" 2>nul'
  nsExec::ExecToLog 'powershell.exe -ExecutionPolicy Bypass -Command "New-NetQosPolicy -Name $\"NEXPAD_Gamepad_QoS$\" -AppPathNameMatchCondition $\"com.sanket.tools.nexpaddesktop.exe$\" -DSCPAction 46 -PriorityValue8021p 6 -NetworkProfile All"'

  ; Always run the ViGEmBus silent installer (it will safely install or repair automatically)
  DetailPrint "Ensuring ViGEmBus Driver is installed..."
  ExecWait '"$INSTDIR\ViGEmBusSetup.exe" /quiet /norestart'
SectionEnd

Section /o "un.Virtual Controller Driver (ViGEmBus)" SEC_UN_DRIVER
  ; /o makes this unchecked by default. The user must manually check the box to uninstall the driver.
  DetailPrint "Uninstalling ViGEmBus Driver..."
  ExecWait 'MsiExec.exe /X{966606F3-2745-49E9-BF15-5C3EAA4E9077} /qn /norestart'
  
  ; Forcefully stop and wipe the orphaned service silently (prevents a black CMD box from flashing)
  nsExec::ExecToLog 'cmd.exe /c "net stop ViGEmBus & sc delete ViGEmBus"'
  
  ; Flag for reboot prompt
  StrCpy $DriverUninstalled "1"
  
  DetailPrint "Driver uninstallation completed."
SectionEnd

Section "un.NEXPAD Desktop" SEC_UN_APP
  SectionIn RO ; Read Only: The main app must always be uninstalled

  ; Remove shortcuts
  Delete "$SMPROGRAMS\NEXPAD\NEXPAD.lnk"
  Delete "$SMPROGRAMS\NEXPAD\Uninstall.lnk"
  RMDir "$SMPROGRAMS\NEXPAD"
  Delete "$DESKTOP\NEXPAD.lnk"

  ; Clean registry keys
  DeleteRegKey HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\NEXPAD"

  ; Clean up Firewall Rules and QoS Policy
  nsExec::ExecToLog 'netsh advfirewall firewall delete rule name="NEXPAD Desktop"'
  nsExec::ExecToLog 'netsh advfirewall firewall delete rule name="NEXPAD Desktop EXE"'
  nsExec::ExecToLog 'powershell.exe -ExecutionPolicy Bypass -Command "Remove-NetQosPolicy -Name $\"NEXPAD_Gamepad_QoS$\" -Confirm:$$false"'

  ; Recursively remove all files in installation directory (including ViGEmBusSetup.exe)
  RMDir /r "$INSTDIR"
SectionEnd

; Component Descriptions for the Uninstaller
!insertmacro MUI_UNFUNCTION_DESCRIPTION_BEGIN
  !insertmacro MUI_DESCRIPTION_TEXT ${SEC_UN_APP} "Uninstalls the NEXPAD Desktop application."
  !insertmacro MUI_DESCRIPTION_TEXT ${SEC_UN_DRIVER} "WARNING: Uninstalls the ViGEmBus Virtual Controller driver. Only check this if no other apps (like DS4Windows) on your PC are using it."
!insertmacro MUI_UNFUNCTION_DESCRIPTION_END

Function un.onUninstSuccess
  StrCmp $DriverUninstalled "1" 0 end
    MessageBox MB_YESNO|MB_ICONEXCLAMATION "The ViGEmBus Virtual Controller Driver was uninstalled.$\r$\n$\r$\nWindows strictly requires a system restart to fully remove kernel drivers from memory before they can be reinstalled in the future.$\r$\n$\r$\nWould you like to restart your computer now to flush the driver from RAM?" IDYES reboot IDNO end
    reboot:
      Reboot
  end:
FunctionEnd
