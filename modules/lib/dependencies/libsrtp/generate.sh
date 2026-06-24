#!/usr/bin/env bash
#
# Regenerates Java FFM bindings for libsrtp (package
# calls2.net.transport.srtp.bindings).
#
# The bound surface is the PORTABLE extern-C shim cobalt_srtp_shim.h, NOT the
# raw srtp.h. The shim re-exposes only the libsrtp functions the calls2 relay
# uses, through fixed-width scalars and opaque void* handles, and builds every
# libsrtp struct (srtp_policy_t / srtp_crypto_policy_t / srtp_ssrc_t) C-side.
# jextract therefore emits a binding with no ABI-sensitive layout (no OfLong vs
# OfInt for `unsigned long` fields, no struct classes), so the SAME committed
# binding is correct on LP64 (Linux/macOS) and LLP64 (Windows) alike. The shim
# is compiled into cobalt-native and its cobalt_srtp_* symbols are forced into
# the export union by build-natives.sh build_combined (the per-function include
# flags below ARE that union's libsrtp contribution).
#
# Header class name: CobaltSrtp (not LibSrtp). The Java side now sees the Cobalt
# shim API, not the raw libsrtp API, so the name reflects the shim and gives a
# clean break from the deleted raw-struct binding.
#
# srtp.h stays vendored under headers/ only because build_libsrtp consumes it as
# the compile target for cobalt_srtp_shim.cpp; it is no longer bound here.
# Re-run whenever cobalt_srtp_shim.h changes.
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
PKG="com.github.auties00.cobalt.calls2.net.transport.srtp.bindings"

# Remove the prior shim binding and any leftover raw-struct binding from before
# the portable-shim migration, so a regen leaves only the freshly emitted set.
PKG_DIR="$OUT/${PKG//.//}"
rm -f "$PKG_DIR/CobaltSrtp.java" "$PKG_DIR/CobaltSrtp\$shared.java" \
      "$PKG_DIR/LibSrtp.java" "$PKG_DIR/LibSrtp\$shared.java" \
      "$PKG_DIR/srtp_policy_t.java" "$PKG_DIR/srtp_crypto_policy_t.java" \
      "$PKG_DIR/srtp_master_key_t.java" "$PKG_DIR/srtp_ssrc_t.java"

"$JEXTRACT" \
  -t "$PKG" \
  -I "$DIR" \
  --header-class-name CobaltSrtp \
  --output "$OUT" \
  --include-function cobalt_srtp_init \
  --include-function cobalt_srtp_shutdown \
  --include-function cobalt_srtp_create \
  --include-function cobalt_srtp_protect \
  --include-function cobalt_srtp_unprotect \
  --include-function cobalt_srtp_protect_rtcp \
  --include-function cobalt_srtp_unprotect_rtcp \
  --include-function cobalt_srtp_dealloc \
  --include-constant COBALT_SRTP_SUITE_AES_CM_128_HMAC_SHA1_80 \
  --include-constant COBALT_SRTP_SUITE_AES_CM_128_HMAC_SHA1_32 \
  --include-constant COBALT_SRTP_DIR_INBOUND \
  --include-constant COBALT_SRTP_DIR_OUTBOUND \
  --include-constant COBALT_SRTP_MASTER_LEN \
  --include-constant COBALT_SRTP_MAX_TRAILER_LEN \
  --include-constant COBALT_SRTP_OK \
  --include-constant COBALT_SRTP_FAIL \
  --include-constant COBALT_SRTP_BAD_PARAM \
  --include-constant COBALT_SRTP_ALLOC_FAIL \
  --include-constant COBALT_SRTP_INIT_FAIL \
  --include-constant COBALT_SRTP_AUTH_FAIL \
  --include-constant COBALT_SRTP_CIPHER_FAIL \
  --include-constant COBALT_SRTP_REPLAY_FAIL \
  --include-constant COBALT_SRTP_REPLAY_OLD \
  "$DIR/cobalt_srtp_shim.h"

echo "wrote $OUT/${PKG//.//}/CobaltSrtp.java"
