param(
    [Parameter(Mandatory=$true)]
    [string]$FilePath
)

$thumbprint = "87F387BADB67DCC20BF36CB9F00B72FB9FD4A6F5"
$cert = Get-Item "Cert:\CurrentUser\My\$thumbprint" -ErrorAction SilentlyContinue

if (-not $cert) {
    Write-Warning "Code signing certificate with thumbprint $thumbprint not found in Cert:\CurrentUser\My."
    exit 0
}

if (-not (Test-Path $FilePath)) {
    Write-Warning "Target file does not exist: $FilePath"
    exit 0
}

# Ensure file is writable (jpackage generates read-only binaries)
Set-ItemProperty -Path $FilePath -Name IsReadOnly -Value $false -ErrorAction SilentlyContinue

Write-Host "[SIGNING] Signing $FilePath with certificate: $($cert.Subject)"
try {
    $sig = Set-AuthenticodeSignature -FilePath $FilePath -Certificate $cert -TimestampServer "http://timestamp.digicert.com" -ErrorAction Stop
    Write-Host "[SIGNING] Successfully signed with RFC 3161 timestamp: $($sig.Status)"
} catch {
    Write-Warning "[SIGNING] Timestamp server unavailable or timed out. Signing locally without timestamp..."
    $sig = Set-AuthenticodeSignature -FilePath $FilePath -Certificate $cert
    Write-Host "[SIGNING] Successfully signed locally: $($sig.Status)"
}
