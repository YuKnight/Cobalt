#!/usr/bin/env bash
#
# Regenerates the Java FFM binding for the ExecuTorch ML bandwidth-estimation
# shim (calls2 net.bwe stack, class com.github.auties00.cobalt.calls2.net.bwe.ExecuTorch).
#
# The bound surface is the PORTABLE extern-C shim cobalt_executorch_shim.h, NOT
# the raw ExecuTorch C++ API. The shim re-exposes the four ml_shim_* entry points
# (create / forward / free / status) through fixed-width scalars, float32 IO, and
# an opaque void* model handle, so the SAME committed binding is correct on every
# host ABI. See re/calls2-spec/ML-BWE-RE.md section 1 for the RE provenance of each
# entry point.
#
# Header class name: ExecuTorch. The four ml_shim_* symbols keep the names the
# reversed wa-voip shim uses (bwe_ml.cc fn4327/fn4328/fn4331/fn4330/fn4335). The
# shim cobalt_executorch_shim.cpp is compiled into cobalt-native with ExecuTorch +
# XNNPACK linked statically, and its ml_* symbols are forced into the export union
# by build-natives.sh build_combined (the per-function include flags below ARE
# that union's executorch contribution).
#
# Prerequisites: JEXTRACT_HOME pointing at a jextract 22+ install, or jextract on
# PATH. Download from https://jdk.java.net/jextract/ if absent.
#
# NOTE: This binding is currently committed by hand in the same shape jextract
# emits (see the other calls2 bindings) because the native ExecuTorch shim/library
# and the .pte model artifacts are not yet present in the tree; the engine gates
# the native model LOAD on symbol availability and falls back to NoopMlBweEngine
# until a model is present. Re-run this script once the shim is buildable to
# regenerate the binding from the header verbatim.
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
PKG="com.github.auties00.cobalt.calls2.net.bwe"

PKG_DIR="$OUT/${PKG//.//}"
rm -f "$PKG_DIR/ExecuTorch.java" "$PKG_DIR/ExecuTorch\$shared.java"

"$JEXTRACT" \
  -t "$PKG" \
  -I "$DIR/headers" \
  --header-class-name ExecuTorch \
  --output "$OUT" \
  --include-function ml_shim_create_executorch \
  --include-function ml_shim_forward \
  --include-function ml_shim_free \
  --include-function ml_get_shim_create_status \
  --include-constant COBALT_ET_OK \
  --include-constant COBALT_ET_ERR_OPEN \
  --include-constant COBALT_ET_ERR_LOAD \
  --include-constant COBALT_ET_ERR_BACKEND \
  --include-constant COBALT_ET_ERR_RUNTIME \
  "$DIR/headers/cobalt_executorch_shim.h"

echo "wrote $OUT/${PKG//.//}/ExecuTorch.java"
