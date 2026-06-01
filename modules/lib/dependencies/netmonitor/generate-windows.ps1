# Regenerates the Windows FFM bindings for the network-connectivity monitor via
# jextract. Output lands in
# modules/lib/src/main/java/com/github/auties00/cobalt/net/bindings/windows/,
# where WindowsNetworkConnectivityMonitor consumes it.
#
# These bind to the OS-provided Iphlpapi.dll, so nothing is downloaded or
# vendored: WindowsNetworkConnectivityMonitor calls System.loadLibrary("Iphlpapi")
# so the generated SymbolLookup.loaderLookup() resolves the exports.
#
# MUST be run on Windows via PowerShell: jextract reads the local Windows SDK
# headers, the emitted layouts are platform specific, and jextract.bat must be
# invoked directly (the bash launcher mishandles the spaces in the SDK path).
#
# Prerequisites:
#   - jextract 22+ on PATH (jextract.bat), or $env:JEXTRACT_HOME set.
#   - A Windows 10/11 SDK installed under
#     "C:\Program Files (x86)\Windows Kits\10\Include" (override the root with
#     $env:WINDOWS_SDK_INCLUDE).
#
# Re-run this whenever netmonitor_windows.h changes.

$ErrorActionPreference = 'Stop'

$dir = Split-Path -Parent $MyInvocation.MyCommand.Path
$root = (Resolve-Path (Join-Path $dir '../../../..')).Path
$out = Join-Path $root 'modules/lib/src/main/java'
$pkg = 'com.github.auties00.cobalt.net.bindings.windows'

if ($env:JEXTRACT_HOME) {
    $jextract = Join-Path $env:JEXTRACT_HOME 'bin/jextract.bat'
} else {
    $cmd = Get-Command jextract.bat -ErrorAction SilentlyContinue
    $jextract = if ($cmd) { $cmd.Source } else { $null }
}
if (-not $jextract -or -not (Test-Path $jextract)) {
    throw 'jextract.bat not found; set $env:JEXTRACT_HOME or add jextract to PATH'
}

$sdkRoot = if ($env:WINDOWS_SDK_INCLUDE) { $env:WINDOWS_SDK_INCLUDE } else { 'C:/Program Files (x86)/Windows Kits/10/Include' }
$sdkVersion = Get-ChildItem $sdkRoot -Directory -ErrorAction SilentlyContinue |
    Where-Object { $_.Name -match '^10\.' } | Sort-Object Name | Select-Object -Last 1
if (-not $sdkVersion) {
    throw "no Windows SDK Include directory found under $sdkRoot"
}
$sdk = $sdkVersion.FullName

& $jextract `
    -t $pkg `
    --header-class-name Iphlpapi `
    --output $out `
    -I "$sdk\um" -I "$sdk\shared" -I "$sdk\ucrt" `
    --include-function NotifyAddrChange `
    --include-function GetNetworkConnectivityHint `
    (Join-Path $dir 'netmonitor_windows.h')
if ($LASTEXITCODE -ne 0) {
    throw "jextract failed with exit code $LASTEXITCODE"
}
Write-Host "wrote $out/com/github/auties00/cobalt/net/bindings/windows/Iphlpapi.java"
