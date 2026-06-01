#!/usr/bin/env bash
#
# Regenerates the Linux FFM bindings for the network-connectivity monitor via
# jextract. Output lands in
# modules/lib/src/main/java/com/github/auties00/cobalt/net/bindings/linux/.
#
# These bind to libc, so nothing is downloaded or vendored: the monitor resolves
# the symbols through Linker.nativeLinker().defaultLookup().
#
# MUST be run on Linux: jextract reads the local system headers and the emitted
# struct layouts (sockaddr_nl, ifaddrs, nlmsghdr) are platform specific.
#
# Prerequisites: jextract 22+ on PATH (or JEXTRACT_HOME set), and the libc
# development headers installed (e.g. the glibc-headers / libc6-dev package).
#
# LinuxNetworkConnectivityMonitor consumes these bindings and currently ships a
# hand-authored stand-in under net/bindings/linux; running this on a Linux host
# replaces it with the canonical jextract output.
#
# Re-run this whenever netmonitor_linux.h changes.
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

OUT="$ROOT/modules/lib/src/main/java"
PKG="com.github.auties00.cobalt.net.bindings.linux"

"$JEXTRACT" \
  -t "$PKG" \
  --header-class-name Netlink \
  --output "$OUT" \
  --include-function socket \
  --include-function bind \
  --include-function recv \
  --include-function close \
  --include-function getifaddrs \
  --include-function freeifaddrs \
  --include-struct sockaddr_nl \
  --include-struct nlmsghdr \
  --include-struct ifaddrs \
  --include-struct sockaddr \
  "$DIR/netmonitor_linux.h"

echo "wrote $OUT/${PKG//.//}/Netlink.java"
