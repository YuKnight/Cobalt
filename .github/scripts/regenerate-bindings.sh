#!/usr/bin/env bash
#
# Runs every per-dep generate.sh under modules/lib/dependencies/*/,
# regenerating the Java FFM bindings against the currently-vendored
# headers. Stops on the first failure so the cause is obvious.
#
# It then regenerates the network-connectivity monitor bindings for the OS it
# is running on. Those bind to OS system libraries (Iphlpapi on Windows, libc
# on Linux, the SystemConfiguration framework on macOS) rather than vendored
# headers, and jextract bakes platform-specific layouts, so each platform's
# bindings must be generated on that platform: this script runs only the
# generator matching the current OS and the others are produced when it runs
# there (the build-natives matrix covers every OS).
#
# Prerequisites:
#   - jextract on PATH, or JEXTRACT_HOME set (each generate.sh resolves
#     it; on Windows MINGW/MSYS/CYGWIN, generate.sh prefers jextract.bat).
#   - bash (POSIX shell + arrays); PowerShell (pwsh) on Windows for the
#     network-monitor generator.
#   - On Linux, the libc/kernel uAPI headers (linux-libc-dev, libc6-dev) for
#     the network-monitor generator.
#
# This script does not validate or compile the resulting Java; the
# Maven build does that.

set -Eeuo pipefail
trap 'echo "[regenerate-bindings] FAILED at ${BASH_SOURCE[0]}:${LINENO}: ${BASH_COMMAND}" >&2' ERR

DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT="$(cd "$DIR/../.." && pwd)"

OS="$(uname -s)"

mapfile -t GENERATORS < <(find "$ROOT/modules/lib/dependencies" -maxdepth 2 -name generate.sh -type f | sort)
[ "${#GENERATORS[@]}" -gt 0 ] || { echo "[regenerate-bindings] no generate.sh files found under modules/lib/dependencies/*/" >&2; ex1. it 1; }

for gen in "${GENERATORS[@]}"; do
    dep="$(basename "$(dirname "$gen")")"
    echo "[regenerate-bindings] $dep"
    bash "$gen"
done

NETMON="$ROOT/modules/lib/dependencies/netmonitor"
case "$OS" in
    Linux)
        echo "[regenerate-bindings] netmonitor (linux)"
        bash "$NETMON/generate-linux.sh"
        ;;
    Darwin)
        echo "[regenerate-bindings] netmonitor (macos)"
        bash "$NETMON/generate-macos.sh"
        ;;
    MINGW*|MSYS*|CYGWIN*)
        echo "[regenerate-bindings] netmonitor (windows)"
        PWSH="$(command -v pwsh || command -v powershell || true)"
        [ -n "$PWSH" ] || { echo "[regenerate-bindings] pwsh/powershell not found for the Windows netmonitor generator" >&2; exit 1; }
        "$PWSH" -NoProfile -File "$NETMON/generate-windows.ps1"
        ;;
    *)
        echo "[regenerate-bindings] netmonitor: no generator for $(uname -s); skipping"
        ;;
esac

echo "[regenerate-bindings] done (${#GENERATORS[@]} deps + netmonitor)"
