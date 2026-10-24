; ==========================================
;  NEXPAD Desktop Windows Installer (NSIS)
; ==========================================

Unicode true
!include "MUI2.nsh"

; Define Basic Package Info
Name "NEXPAD Desktop"
OutFile "installer_build\NEXPAD_Desktop_Setup.exe"
InstallDir "$PROGRAMFILES64\NEXPAD"
RequestExecutionLevel admin

; Variables
Var DriverUninstalled

; Windows File Properties / Version Info
VIProductVersion "1.0.0.0"
VIAddVersionKey "ProductName" "NEXPAD Desktop"
VIAddVersionKey "Comments" "Official Windows Companion for NEXPAD Virtual Gamepad"
VIAddVersionKey "CompanyName" "Sanket Tools"
VIAddVersionKey "LegalCopyright" "Copyright 2026 Sanket Tools"
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

; Welcome Page Setup
!define MUI_WELCOMEPAGE_TITLE "Welcome to NEXPAD Desktop Setup"
!define MUI_WELCOMEPAGE_TEXT "This wizard will guide you through installing NEXPAD Desktop and configuring the required ViGEmBus virtual controller drivers on your system.$\r$\n$\r$\nPlease review the End User License Agreement on the next screen before continuing.$\r$\n$\r$\nClick Next to continue."

; License Agreement Setup with Mandatory Radio Button Acceptance
!define MUI_LICENSEPAGE_TEXT_TOP "Please review the license terms before installing NEXPAD Desktop."
!define MUI_LICENSEPAGE_TEXT_BOTTOM "If you accept the terms of the agreement, select the first option below. You must accept the agreement to install NEXPAD Desktop."
!define MUI_LICENSEPAGE_RADIOBUTTONS
!define MUI_LICENSEPAGE_RADIOBUTTONS_TEXT_ACCEPT "I accept the terms in the License Agreement"
!define MUI_LICENSEPAGE_RADIOBUTTONS_TEXT_DECLINE "I do not accept the terms in the License Agreement"

; Pages
!insertmacro MUI_PAGE_WELCOME
!insertmacro MUI_PAGE_LICENSE "LICENSE"
!insertmacro MUI_PAGE_DIRECTORY
!insertmacro MUI_PAGE_INSTFILES
!define MUI_FINISHPAGE_RUN "$INSTDIR\NEXPAD Desktop.exe"
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
  
  ; Close running instance and background processes if user is updating/re-installing
  DetailPrint "Ensuring NEXPAD and background tools are closed before updating..."
  IfFileExists "$INSTDIR\tools\adb\adb.exe" 0 +2
    nsExec::ExecToLog '"$INSTDIR\tools\adb\adb.exe" kill-server'
  nsExec::ExecToLog 'taskkill /F /T /IM "NEXPAD Desktop.exe"'
  nsExec::ExecToLog 'taskkill /F /T /IM "NEXPAD Desktop.exe"'
  nsExec::ExecToLog 'taskkill /F /T /IM adb.exe'
  nsExec::ExecToLog 'taskkill /F /T /IM nexpad-service.exe'
  Sleep 1500

  SetOverwrite on

  ; Bundle all pre-compiled files from the packageApp task
  File /r "desktopApp\build\compose\binaries\main-release\app\NEXPAD Desktop\*"
        
  ; Bundle standalone ADB tools and dependencies for non-developer end users
  SetOutPath "$INSTDIR\tools\adb"
  File /r "desktopApp\tools\adb\*"
  SetOutPath "$INSTDIR"

  ; Bundle Open Source License and Notice
  File "LICENSE"
  File "NOTICE"

  ; Bundle the driver installers, service wrapper, and libwdi library
  File /oname=ViGEmBusSetup.exe "redist\ViGEmBusSetup.exe"
  File /oname=libwdi.dll "redist\libwdi.dll"

  ; Write Uninstaller
  WriteUninstaller "$INSTDIR\uninstall.exe"

  ; Create shortcuts
  CreateDirectory "$SMPROGRAMS\NEXPAD"
  CreateShortcut "$SMPROGRAMS\NEXPAD\NEXPAD.lnk" "$INSTDIR\NEXPAD Desktop.exe"
  CreateShortcut "$SMPROGRAMS\NEXPAD\Uninstall.lnk" "$INSTDIR\uninstall.exe"
  CreateShortcut "$DESKTOP\NEXPAD.lnk" "$INSTDIR\NEXPAD Desktop.exe"

  ; Write registry keys for Windows Add/Remove Programs
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\NEXPAD" "DisplayName" "NEXPAD Desktop"
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\NEXPAD" "UninstallString" '"$INSTDIR\uninstall.exe"'
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\NEXPAD" "DisplayIcon" '"$INSTDIR\NEXPAD Desktop.exe"'
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\NEXPAD" "DisplayVersion" "1.0.0"
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\NEXPAD" "Publisher" "Sanket Tools"

  ; Add Windows Firewall exceptions so the phone can communicate with this app
  DetailPrint "Configuring Windows Firewall rules..."
  ExecWait 'netsh advfirewall firewall delete rule name="NEXPAD Desktop" >nul 2>&1'
  ExecWait 'netsh advfirewall firewall delete rule name="NEXPAD Gamepad UDP" >nul 2>&1'
  ExecWait 'netsh advfirewall firewall add rule name="NEXPAD Gamepad UDP" dir=in action=allow protocol=UDP localport=9999 enable=yes profile=any'
  ExecWait 'netsh advfirewall firewall delete rule name="NEXPAD Discovery" >nul 2>&1'
  ExecWait 'netsh advfirewall firewall add rule name="NEXPAD Discovery" dir=in action=allow protocol=UDP localport=9998 enable=yes profile=any'
  ExecWait 'netsh advfirewall firewall delete rule name="NEXPAD DSU" >nul 2>&1'
  ExecWait 'netsh advfirewall firewall add rule name="NEXPAD DSU" dir=in action=allow protocol=UDP localport=26760 enable=yes profile=any'
  ExecWait 'netsh advfirewall firewall delete rule name="NEXPAD Desktop EXE" >nul 2>&1'
  ExecWait 'netsh advfirewall firewall add rule name="NEXPAD Desktop EXE" dir=in action=allow protocol=any program="$INSTDIR\NEXPAD Desktop.exe" enable=yes profile=any'

  ; Apply Windows QoS Policy to force DSCP 46 (EF/Voice) tagging on UDP feedback packets
  DetailPrint "Applying Windows QoS Policy (DSCP 46)..."
  nsExec::ExecToLog 'powershell.exe -ExecutionPolicy Bypass -Command "Remove-NetQosPolicy -Name $\"NEXPAD_Gamepad_QoS$\" -Confirm:$$false" 2>nul'
  nsExec::ExecToLog 'powershell.exe -ExecutionPolicy Bypass -Command "New-NetQosPolicy -Name $\"NEXPAD_Gamepad_QoS$\" -AppPathNameMatchCondition $\"NEXPAD Desktop.exe$\" -DSCPAction 46 -PriorityValue8021p 6 -NetworkProfile All"'

  ; Always run the ViGEmBus silent installer (it will safely install or repair automatically)
  DetailPrint "Ensuring ViGEmBus Driver is installed..."
  ExecWait '"$INSTDIR\ViGEmBusSetup.exe" /quiet /norestart'

  ; Stop and delete the old Background Service if the user is upgrading from the buggy version
  DetailPrint "Cleaning up legacy background service..."
  nsExec::ExecToLog 'cmd.exe /c "net stop nexpad-service & sc delete nexpad-service"'
  Delete "$INSTDIR\nexpad-service.exe"
  Delete "$INSTDIR\nexpad-service.xml"
SectionEnd

Section /o "un.Virtual Controller Driver (ViGEmBus)" SEC_UN_DRIVER
  DetailPrint "Uninstalling ViGEmBus Driver..."
  ExecWait 'MsiExec.exe /X{966606F3-2745-49E9-BF15-5C3EAA4E9077} /qn /norestart'
  nsExec::ExecToLog 'cmd.exe /c "net stop ViGEmBus & sc delete ViGEmBus"'
  StrCpy $DriverUninstalled "1"
  DetailPrint "Driver uninstallation completed."
SectionEnd

Section "un.NEXPAD Desktop" SEC_UN_APP
  SectionIn RO

  DetailPrint "Closing any running instances of NEXPAD Desktop and background tools..."
  IfFileExists "$INSTDIR\tools\adb\adb.exe" 0 +2
    nsExec::ExecToLog '"$INSTDIR\tools\adb\adb.exe" kill-server'
  nsExec::ExecToLog 'taskkill /F /T /IM "NEXPAD Desktop.exe"'
  nsExec::ExecToLog 'taskkill /F /T /IM "NEXPAD Desktop.exe"'
  nsExec::ExecToLog 'taskkill /F /T /IM adb.exe'
  nsExec::ExecToLog 'taskkill /F /T /IM nexpad-service.exe'
  Sleep 1500

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

; Component Descriptions for the Uninstaller
!insertmacro MUI_UNFUNCTION_DESCRIPTION_BEGIN
  !insertmacro MUI_DESCRIPTION_TEXT ${SEC_UN_APP} "Uninstalls the NEXPAD Desktop application."
  !insertmacro MUI_DESCRIPTION_TEXT ${SEC_UN_DRIVER} "WARNING: Uninstalls the ViGEmBus Virtual Controller driver. Only check this if no other apps (like DS4Windows) on your PC are using it."
!insertmacro MUI_UNFUNCTION_DESCRIPTION_END

Function un.onUninstSuccess
  IfSilent end
  StrCmp $DriverUninstalled "1" 0 end
    MessageBox MB_YESNO|MB_ICONEXCLAMATION "The ViGEmBus Virtual Controller Driver was uninstalled.$\r$\n$\r$\nWindows strictly requires a system restart to fully remove kernel drivers from memory before they can be reinstalled in the future.$\r$\n$\r$\nWould you like to restart your computer now to flush the driver from RAM?" IDYES reboot IDNO end
    reboot:
      Reboot
  end:
FunctionEnd
