#!/usr/bin/env bash
#
# Regenerates Java FFM bindings for libyuv (calls2 video stack, package
# calls2.media.video.yuv.bindings).
#
# The bound surface is the PORTABLE extern-C shim cobalt_yuv_shim.h, NOT the raw
# libyuv umbrella header. The shim re-exposes only the libyuv conversion
# functions the YuvConverter uses, through fixed-width scalars and const/non-const
# uint8_t* plane pointers, and keeps libyuv's by-value C enums (FilterMode on
# I420Scale, RotationMode on I420Rotate) and its FourCC identifiers C-side. The
# width of a C enum is host- and compiler-dependent, so binding the raw functions
# would bake an enum-width assumption into the generated layout; the shim passes a
# fixed-width int32_t selector instead. jextract therefore emits a binding with no
# ABI-sensitive layout (no enum-width sensitivity, no struct classes), so the SAME
# committed binding is correct on LP64 (Linux/macOS) and LLP64 (Windows) alike.
#
# Header class name: CobaltYuv (not LibYuv). The Java side now sees the Cobalt
# shim API, not the raw libyuv API, so the name reflects the shim and gives a
# clean break from the deleted raw binding. The shim is compiled into
# cobalt-native and its cobalt_yuv_* symbols are forced into the export union by
# build-natives.sh build_combined (the per-function include flags below ARE that
# union's libyuv contribution).
#
# The libyuv public header tree stays vendored under headers/ only because
# build_libyuv consumes it as the compile target for cobalt_yuv_shim.c (which
# includes <libyuv.h>); it is no longer bound here.
# Re-run whenever cobalt_yuv_shim.h changes.
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
PKG="com.github.auties00.cobalt.calls2.media.video.yuv.bindings"

# Remove the prior shim binding and any leftover raw binding from before the
# portable-shim migration, so a regen leaves only the freshly emitted set.
PKG_DIR="$OUT/${PKG//.//}"
rm -f "$PKG_DIR/CobaltYuv.java" "$PKG_DIR/CobaltYuv\$shared.java" \
      "$PKG_DIR/LibYuv.java" "$PKG_DIR/LibYuv\$shared.java"

"$JEXTRACT" \
  -t "$PKG" \
  -I "$DIR" \
  --header-class-name CobaltYuv \
  --output "$OUT" \
  --include-function cobalt_yuv_i420_to_argb \
  --include-function cobalt_yuv_argb_to_i420 \
  --include-function cobalt_yuv_nv12_to_i420 \
  --include-function cobalt_yuv_i420_to_nv12 \
  --include-function cobalt_yuv_i420_scale \
  --include-function cobalt_yuv_i420_rotate \
  --include-constant COBALT_YUV_FILTER_NONE \
  --include-constant COBALT_YUV_FILTER_LINEAR \
  --include-constant COBALT_YUV_FILTER_BILINEAR \
  --include-constant COBALT_YUV_FILTER_BOX \
  --include-constant COBALT_YUV_ROTATE_0 \
  --include-constant COBALT_YUV_ROTATE_90 \
  --include-constant COBALT_YUV_ROTATE_180 \
  --include-constant COBALT_YUV_ROTATE_270 \
  --include-constant COBALT_YUV_FOURCC_I420 \
  --include-constant COBALT_YUV_FOURCC_NV12 \
  --include-constant COBALT_YUV_FOURCC_ARGB \
  --include-constant COBALT_YUV_OK \
  --include-constant COBALT_YUV_ERROR \
  "$DIR/cobalt_yuv_shim.h"

echo "wrote $OUT/${PKG//.//}/CobaltYuv.java"
