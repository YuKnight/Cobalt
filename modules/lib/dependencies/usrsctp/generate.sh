#!/usr/bin/env bash
#
# Regenerates Java FFM bindings for usrsctp (calls2 transport stack, package
# calls2.net.transport.sctp.bindings).
#
# The bound surface is the PORTABLE extern-C shim cobalt_sctp_shim.h, NOT the raw
# usrsctp.h. The shim re-exposes only the DataChannel association surface through
# fixed-width scalars and opaque void* handles, and builds every usrsctp and
# socket struct (sockaddr_conn / sctp_sndinfo / sctp_sendv_spa / sctp_initmsg /
# sctp_event) C-side, absorbing the by-value union sctp_sockstore + struct
# sctp_rcvinfo of usrsctp's receive callback behind a C trampoline. jextract
# therefore emits a binding with no ABI-sensitive layout (no struct classes, no
# platform-varying sockaddr_conn, no by-value union/struct callback arguments),
# so the SAME committed binding is correct on LP64 (Linux/macOS) and LLP64
# (Windows) alike. The sysstubs header the old raw binding needed to coax
# jextract into a portable sockaddr layout is no longer used: the shim hides
# every socket type C-side.
#
# Header class name: CobaltSctp (not UsrSctp). The Java side now sees the Cobalt
# shim API, so the name reflects the shim and gives a clean break from the
# raw-struct binding. The shim is compiled into cobalt-native and its
# cobalt_sctp_* symbols are forced into the export union by build-natives.sh
# build_combined (the per-function include flags below ARE that union's usrsctp
# contribution).
#
# usrsctp.h stays vendored under headers/ only as a fallback compile reference;
# build_usrsctp compiles cobalt_sctp_shim.c against the usrsctp install tree.
# Re-run whenever cobalt_sctp_shim.h changes.
#
# Prerequisites: JEXTRACT_HOME pointing at a jextract 22+ install, or jextract on
# PATH. Download from https://jdk.java.net/jextract/ if absent.
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
PKG="com.github.auties00.cobalt.calls2.net.transport.sctp.bindings"

# Remove the prior shim binding and every leftover raw-struct/callback class from
# before the portable-shim migration, so a regen leaves only the freshly emitted
# set.
PKG_DIR="$OUT/${PKG//.//}"
rm -f "$PKG_DIR/CobaltSctp.java" "$PKG_DIR/CobaltSctp\$shared.java" \
      "$PKG_DIR/UsrSctp.java" "$PKG_DIR/UsrSctp\$shared.java" \
      "$PKG_DIR/usrsctp_init\$x0.java" "$PKG_DIR/usrsctp_init\$x1.java" \
      "$PKG_DIR/usrsctp_init_nothreads\$x0.java" "$PKG_DIR/usrsctp_init_nothreads\$x1.java" \
      "$PKG_DIR/usrsctp_set_upcall\$upcall.java" \
      "$PKG_DIR/usrsctp_socket\$receive_cb.java" "$PKG_DIR/usrsctp_socket\$send_cb.java" \
      "$PKG_DIR/sctp_sockstore.java" "$PKG_DIR/sctp_rcvinfo.java" "$PKG_DIR/sctp_sndinfo.java" \
      "$PKG_DIR/sctp_prinfo.java" "$PKG_DIR/sctp_authinfo.java" "$PKG_DIR/sctp_sendv_spa.java" \
      "$PKG_DIR/sctp_event.java" "$PKG_DIR/sctp_event_subscribe.java" "$PKG_DIR/sctp_initmsg.java" \
      "$PKG_DIR/sctp_assoc_value.java" "$PKG_DIR/sctp_paddrparams.java" "$PKG_DIR/sctp_rtoinfo.java" \
      "$PKG_DIR/sockaddr.java" "$PKG_DIR/sockaddr_storage.java" "$PKG_DIR/sockaddr_conn.java" \
      "$PKG_DIR/sockaddr_in.java" "$PKG_DIR/sockaddr_in6.java" \
      "$PKG_DIR/in_addr.java" "$PKG_DIR/in6_addr.java"

"$JEXTRACT" \
  -t "$PKG" \
  -I "$DIR" \
  --header-class-name CobaltSctp \
  --output "$OUT" \
  --include-function cobalt_sctp_global_init \
  --include-function cobalt_sctp_register_address \
  --include-function cobalt_sctp_deregister_address \
  --include-function cobalt_sctp_handle_timers \
  --include-function cobalt_sctp_socket_create \
  --include-function cobalt_sctp_bind \
  --include-function cobalt_sctp_connect \
  --include-function cobalt_sctp_conninput \
  --include-function cobalt_sctp_send \
  --include-function cobalt_sctp_send_pr \
  --include-function cobalt_sctp_set_nodelay \
  --include-function cobalt_sctp_set_disable_fragments \
  --include-function cobalt_sctp_set_initmsg \
  --include-function cobalt_sctp_subscribe_assoc_change \
  --include-function cobalt_sctp_set_non_blocking \
  --include-function cobalt_sctp_close \
  --include-constant COBALT_SCTP_OK \
  --include-constant COBALT_SCTP_IN_PROGRESS \
  --include-constant COBALT_SCTP_BAD_PARAM \
  --include-constant COBALT_SCTP_NOMEM \
  --include-constant COBALT_SCTP_FAIL \
  --include-constant COBALT_SCTP_PR_NONE \
  --include-constant COBALT_SCTP_PR_TTL \
  --include-constant COBALT_SCTP_PR_RTX \
  "$DIR/cobalt_sctp_shim.h"

echo "wrote $OUT/${PKG//.//}/CobaltSctp.java"
