/*
 * cobalt_yuv_shim.c
 *
 * Implementation of the portable extern-C libyuv facade declared in
 * cobalt_yuv_shim.h. Each wrapper forwards directly to the corresponding libyuv
 * function, passing plane pointers and strides through unchanged and casting the
 * fixed-width int32_t filter/rotation selectors back to libyuv's enum types
 * C-side. No libyuv enum or struct ever crosses the FFM boundary, so the
 * jextract-generated Java binding is host-ABI independent.
 *
 * This translation unit is compiled as C (not C++) so the libyuv headers expose
 * their plain extern-C prototypes (I420ToARGB, ...) rather than the
 * namespace-wrapped C++ declarations; the resulting calls resolve to the same
 * unmangled symbols the static libyuv.a (built as C++ with extern "C" linkage)
 * provides.
 *
 * Compiled into the combined cobalt-native shared library by
 * .github/scripts/build-natives.sh (build_libyuv), linked against the static
 * libyuv.a; the export union in build_combined forces the cobalt_yuv_* symbols
 * (drawn from generate.sh's --include-function list) into the library's export
 * table.
 */

#include "cobalt_yuv_shim.h"

#include "libyuv.h"

int32_t cobalt_yuv_i420_to_argb(const uint8_t *srcY, int32_t srcStrideY,
                                const uint8_t *srcU, int32_t srcStrideU,
                                const uint8_t *srcV, int32_t srcStrideV,
                                uint8_t *dstArgb, int32_t dstStrideArgb,
                                int32_t width, int32_t height) {
    return (int32_t) I420ToARGB(srcY, (int) srcStrideY,
                                srcU, (int) srcStrideU,
                                srcV, (int) srcStrideV,
                                dstArgb, (int) dstStrideArgb,
                                (int) width, (int) height);
}

int32_t cobalt_yuv_argb_to_i420(const uint8_t *srcArgb, int32_t srcStrideArgb,
                                uint8_t *dstY, int32_t dstStrideY,
                                uint8_t *dstU, int32_t dstStrideU,
                                uint8_t *dstV, int32_t dstStrideV,
                                int32_t width, int32_t height) {
    return (int32_t) ARGBToI420(srcArgb, (int) srcStrideArgb,
                                dstY, (int) dstStrideY,
                                dstU, (int) dstStrideU,
                                dstV, (int) dstStrideV,
                                (int) width, (int) height);
}

int32_t cobalt_yuv_nv12_to_i420(const uint8_t *srcY, int32_t srcStrideY,
                                const uint8_t *srcUv, int32_t srcStrideUv,
                                uint8_t *dstY, int32_t dstStrideY,
                                uint8_t *dstU, int32_t dstStrideU,
                                uint8_t *dstV, int32_t dstStrideV,
                                int32_t width, int32_t height) {
    return (int32_t) NV12ToI420(srcY, (int) srcStrideY,
                                srcUv, (int) srcStrideUv,
                                dstY, (int) dstStrideY,
                                dstU, (int) dstStrideU,
                                dstV, (int) dstStrideV,
                                (int) width, (int) height);
}

int32_t cobalt_yuv_i420_to_nv12(const uint8_t *srcY, int32_t srcStrideY,
                                const uint8_t *srcU, int32_t srcStrideU,
                                const uint8_t *srcV, int32_t srcStrideV,
                                uint8_t *dstY, int32_t dstStrideY,
                                uint8_t *dstUv, int32_t dstStrideUv,
                                int32_t width, int32_t height) {
    return (int32_t) I420ToNV12(srcY, (int) srcStrideY,
                                srcU, (int) srcStrideU,
                                srcV, (int) srcStrideV,
                                dstY, (int) dstStrideY,
                                dstUv, (int) dstStrideUv,
                                (int) width, (int) height);
}

int32_t cobalt_yuv_i420_scale(const uint8_t *srcY, int32_t srcStrideY,
                              const uint8_t *srcU, int32_t srcStrideU,
                              const uint8_t *srcV, int32_t srcStrideV,
                              int32_t srcWidth, int32_t srcHeight,
                              uint8_t *dstY, int32_t dstStrideY,
                              uint8_t *dstU, int32_t dstStrideU,
                              uint8_t *dstV, int32_t dstStrideV,
                              int32_t dstWidth, int32_t dstHeight,
                              int32_t filter) {
    return (int32_t) I420Scale(srcY, (int) srcStrideY,
                               srcU, (int) srcStrideU,
                               srcV, (int) srcStrideV,
                               (int) srcWidth, (int) srcHeight,
                               dstY, (int) dstStrideY,
                               dstU, (int) dstStrideU,
                               dstV, (int) dstStrideV,
                               (int) dstWidth, (int) dstHeight,
                               (enum FilterMode) filter);
}

int32_t cobalt_yuv_i420_rotate(const uint8_t *srcY, int32_t srcStrideY,
                               const uint8_t *srcU, int32_t srcStrideU,
                               const uint8_t *srcV, int32_t srcStrideV,
                               uint8_t *dstY, int32_t dstStrideY,
                               uint8_t *dstU, int32_t dstStrideU,
                               uint8_t *dstV, int32_t dstStrideV,
                               int32_t width, int32_t height,
                               int32_t mode) {
    return (int32_t) I420Rotate(srcY, (int) srcStrideY,
                                srcU, (int) srcStrideU,
                                srcV, (int) srcStrideV,
                                dstY, (int) dstStrideY,
                                dstU, (int) dstStrideU,
                                dstV, (int) dstStrideV,
                                (int) width, (int) height,
                                (enum RotationMode) mode);
}
