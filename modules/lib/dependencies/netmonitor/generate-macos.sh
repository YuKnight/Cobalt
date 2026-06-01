#!/usr/bin/env bash
#
# Regenerates the macOS FFM bindings for the network-connectivity monitor via
# jextract. Output lands in
# modules/lib/src/main/java/com/github/auties00/cobalt/net/bindings/macos/.
#
# These bind to the SystemConfiguration and CoreFoundation frameworks, so
# nothing is downloaded or vendored: the monitor resolves the symbols through
# SymbolLookup.libraryLookup of the framework binaries.
#
# MUST be run on macOS: jextract reads the local SDK framework headers and the
# emitted layouts are platform specific. If arm64 and x86_64 layouts differ,
# generate once per architecture into bindings/macos/arm64 and
# bindings/macos/x86_64 and dispatch by os.arch.
#
# Prerequisites: jextract 22+ on PATH (or JEXTRACT_HOME set) and the Xcode
# command line tools (for the macOS SDK frameworks).
#
# MacosNetworkConnectivityMonitor consumes these bindings and currently ships a
# hand-authored stand-in under net/bindings/macos; running this on a Mac
# replaces it with the canonical jextract output. The monitor registers a
# reachability callback on a CoreFoundation run loop (via an FFM upcall stub),
# so it needs the SCNetworkReachability callback/run-loop functions below plus
# the kCFRunLoopDefaultMode constant.
#
# Re-run this whenever netmonitor_macos.h changes.
#

set -euo pipefail

DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT="$(cd "$DIR/../../../.." && pwd)"
if [ -n "${JEXTRACT_HOME:-}" ]; then
  JEXTRACT="$JEXTRACT_HOME/bin/jextract"
else
  JEXTRACT="$(command -v jextract || true)"
fi
[ -n "$JEXTRACT" ] && [ -f "$JEXTRACT" ] || { echo "jextract not found; set JEXTRACT_HOME or add to PATH" >&2; exit 1; }

SDK="$(xcrun --show-sdk-path)"
OUT="$ROOT/modules/lib/src/main/java"
PKG="com.github.auties00.cobalt.net.bindings.macos"

"$JEXTRACT" \
  -t "$PKG" \
  --header-class-name SCReachability \
  --output "$OUT" \
  -F "$SDK/System/Library/Frameworks" \
  -I "$SDK/usr/include" \
  --include-function SCNetworkReachabilityCreateWithAddress \
  --include-function SCNetworkReachabilityGetFlags \
  --include-function SCNetworkReachabilitySetCallback \
  --include-function SCNetworkReachabilityScheduleWithRunLoop \
  --include-function SCNetworkReachabilityUnscheduleFromRunLoop \
  --include-function CFRunLoopGetCurrent \
  --include-function CFRunLoopRun \
  --include-function CFRunLoopStop \
  --include-function CFRelease \
  --include-var kCFRunLoopDefaultMode \
  "$DIR/netmonitor_macos.h"

echo "wrote $OUT/${PKG//.//}/SCReachability.java"
