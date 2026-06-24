/*
 * cobalt_yuv_shim.h
 *
 * Portable extern-C facade over libyuv (Chromium libyuv) for the Cobalt calls2
 * video color-conversion path. It re-exposes only the libyuv functions the
 * YuvConverter uses, through PORTABLE SCALAR TYPES ONLY (the fixed-width
 * <stdint.h> integers and const/non-const uint8_t* plane pointers), so the
 * jextract-generated Java binding is identical on every host ABI.
 *
 * Why this shim exists: jextract bakes the host C ABI into the bindings it
 * emits. libyuv's own scalars are already portable (it uses int32_t-equivalent
 * `int` for strides and dimensions), but its public signatures take C enums by
 * value (enum FilterMode on I420Scale, enum RotationMode on I420Rotate) and its
 * format identifiers live in enum FourCC; the width of a C enum is host- and
 * compiler-dependent. Binding the shim keeps every libyuv enum C-side and passes
 * a fixed-width int32_t selector instead, and exposes the FourCC identifiers as
 * stable integer constants, so the generated binding carries no enum-width
 * sensitivity. Wrapping the conversion functions uniformly also gives the calls2
 * video stack a single cobalt_yuv_* surface to bind rather than libyuv's broad
 * public header set.
 *
 * How libyuv types are hidden: libyuv exposes no struct in the bound surface
 * (these six functions are pure plane-pointer transforms), so only the enums
 * need hiding. enum FilterMode and enum RotationMode are accepted as int32_t and
 * mapped back to the libyuv enum C-side; enum FourCC values are re-published as
 * COBALT_YUV_FOURCC_* integer constants.
 *
 * Symbol naming: every exported symbol is prefixed cobalt_yuv_ so it coexists in
 * the combined cobalt-native library with the statically-linked real libyuv
 * symbols (I420ToARGB, ARGBToI420, ...), which these wrappers call internally.
 *
 * Portability rule for this header: it uses ONLY uint8_t/int32_t. It never names
 * a libyuv type, and never uses bare `long`, `unsigned long` or `long double`.
 */

#ifndef COBALT_YUV_SHIM_H
#define COBALT_YUV_SHIM_H

#include <stdint.h>

#ifdef __cplusplus
extern "C" {
#endif

/*
 * Scaling filter selectors for cobalt_yuv_i420_scale. The values match libyuv's
 * FilterMode enumerators so the meaning is unambiguous, but the enum itself
 * never crosses the boundary:
 *
 *   0 = none     (point sample; fastest, lowest quality)
 *   1 = linear   (horizontal filtering only)
 *   2 = bilinear (good quality, faster than box when downscaling)
 *   3 = box      (highest quality)
 */
#define COBALT_YUV_FILTER_NONE     0
#define COBALT_YUV_FILTER_LINEAR   1
#define COBALT_YUV_FILTER_BILINEAR 2
#define COBALT_YUV_FILTER_BOX      3

/*
 * Rotation selectors for cobalt_yuv_i420_rotate. The values match libyuv's
 * RotationMode enumerators (degrees clockwise) so the meaning is unambiguous,
 * but the enum itself never crosses the boundary:
 *
 *     0 = no rotation
 *    90 = 90 degrees clockwise
 *   180 = 180 degrees
 *   270 = 270 degrees clockwise
 */
#define COBALT_YUV_ROTATE_0   0
#define COBALT_YUV_ROTATE_90  90
#define COBALT_YUV_ROTATE_180 180
#define COBALT_YUV_ROTATE_270 270

/*
 * FourCC format identifiers re-published from libyuv's enum FourCC as stable
 * integer constants. Each is the little-endian packing of its four ASCII bytes,
 * identical to libyuv's FOURCC_* values, so the Java side has portable names for
 * the formats these wrappers convert between without binding the enum.
 */
#define COBALT_YUV_FOURCC_I420 0x30323449
#define COBALT_YUV_FOURCC_NV12 0x3231564E
#define COBALT_YUV_FOURCC_ARGB 0x42475241

/*
 * Status codes returned by the conversion entry points. Zero is success; every
 * wrapped libyuv function returns 0 on success and -1 on a parameter error
 * (e.g. a NULL plane or non-positive dimension), and the shim forwards that
 * convention unchanged.
 */
#define COBALT_YUV_OK    0
#define COBALT_YUV_ERROR (-1)

/**
 * Converts an I420 (planar YUV 4:2:0) frame to packed ARGB.
 *
 * Forwards to libyuv I420ToARGB. The three source planes are read with their
 * respective strides and the result is written to a single packed ARGB plane
 * (four bytes per pixel) with stride dstStrideArgb. All planes must be sized for
 * width by height pixels with chroma planes subsampled 2x2.
 *
 * @param srcY          the source luma plane.
 * @param srcStrideY    the luma row stride in bytes.
 * @param srcU          the source U (Cb) chroma plane.
 * @param srcStrideU    the U row stride in bytes.
 * @param srcV          the source V (Cr) chroma plane.
 * @param srcStrideV    the V row stride in bytes.
 * @param dstArgb       the destination packed ARGB plane.
 * @param dstStrideArgb the ARGB row stride in bytes.
 * @param width         the frame width in pixels.
 * @param height        the frame height in pixels.
 * @return COBALT_YUV_OK on success, COBALT_YUV_ERROR on a parameter error.
 */
int32_t cobalt_yuv_i420_to_argb(const uint8_t *srcY, int32_t srcStrideY,
                                const uint8_t *srcU, int32_t srcStrideU,
                                const uint8_t *srcV, int32_t srcStrideV,
                                uint8_t *dstArgb, int32_t dstStrideArgb,
                                int32_t width, int32_t height);

/**
 * Converts a packed ARGB frame to I420 (planar YUV 4:2:0).
 *
 * Forwards to libyuv ARGBToI420. The single packed ARGB source plane (four bytes
 * per pixel) is read with stride srcStrideArgb and the result is written to
 * three planes, the chroma planes subsampled 2x2.
 *
 * @param srcArgb       the source packed ARGB plane.
 * @param srcStrideArgb the ARGB row stride in bytes.
 * @param dstY          the destination luma plane.
 * @param dstStrideY    the luma row stride in bytes.
 * @param dstU          the destination U (Cb) chroma plane.
 * @param dstStrideU    the U row stride in bytes.
 * @param dstV          the destination V (Cr) chroma plane.
 * @param dstStrideV    the V row stride in bytes.
 * @param width         the frame width in pixels.
 * @param height        the frame height in pixels.
 * @return COBALT_YUV_OK on success, COBALT_YUV_ERROR on a parameter error.
 */
int32_t cobalt_yuv_argb_to_i420(const uint8_t *srcArgb, int32_t srcStrideArgb,
                                uint8_t *dstY, int32_t dstStrideY,
                                uint8_t *dstU, int32_t dstStrideU,
                                uint8_t *dstV, int32_t dstStrideV,
                                int32_t width, int32_t height);

/**
 * Converts an NV12 (planar luma plus interleaved UV) frame to I420.
 *
 * Forwards to libyuv NV12ToI420. The luma plane and the interleaved UV plane are
 * read with their strides and de-interleaved into three I420 planes, the chroma
 * planes subsampled 2x2.
 *
 * @param srcY        the source luma plane.
 * @param srcStrideY  the luma row stride in bytes.
 * @param srcUv       the source interleaved UV plane.
 * @param srcStrideUv the interleaved UV row stride in bytes.
 * @param dstY        the destination luma plane.
 * @param dstStrideY  the luma row stride in bytes.
 * @param dstU        the destination U (Cb) chroma plane.
 * @param dstStrideU  the U row stride in bytes.
 * @param dstV        the destination V (Cr) chroma plane.
 * @param dstStrideV  the V row stride in bytes.
 * @param width       the frame width in pixels.
 * @param height      the frame height in pixels.
 * @return COBALT_YUV_OK on success, COBALT_YUV_ERROR on a parameter error.
 */
int32_t cobalt_yuv_nv12_to_i420(const uint8_t *srcY, int32_t srcStrideY,
                                const uint8_t *srcUv, int32_t srcStrideUv,
                                uint8_t *dstY, int32_t dstStrideY,
                                uint8_t *dstU, int32_t dstStrideU,
                                uint8_t *dstV, int32_t dstStrideV,
                                int32_t width, int32_t height);

/**
 * Converts an I420 frame to NV12 (planar luma plus interleaved UV).
 *
 * Forwards to libyuv I420ToNV12. The three I420 planes are read with their
 * strides and the chroma planes are interleaved into a single UV plane.
 *
 * @param srcY        the source luma plane.
 * @param srcStrideY  the luma row stride in bytes.
 * @param srcU        the source U (Cb) chroma plane.
 * @param srcStrideU  the U row stride in bytes.
 * @param srcV        the source V (Cr) chroma plane.
 * @param srcStrideV  the V row stride in bytes.
 * @param dstY        the destination luma plane.
 * @param dstStrideY  the luma row stride in bytes.
 * @param dstUv       the destination interleaved UV plane.
 * @param dstStrideUv the interleaved UV row stride in bytes.
 * @param width       the frame width in pixels.
 * @param height      the frame height in pixels.
 * @return COBALT_YUV_OK on success, COBALT_YUV_ERROR on a parameter error.
 */
int32_t cobalt_yuv_i420_to_nv12(const uint8_t *srcY, int32_t srcStrideY,
                                const uint8_t *srcU, int32_t srcStrideU,
                                const uint8_t *srcV, int32_t srcStrideV,
                                uint8_t *dstY, int32_t dstStrideY,
                                uint8_t *dstUv, int32_t dstStrideUv,
                                int32_t width, int32_t height);

/**
 * Scales an I420 frame to a new resolution.
 *
 * Forwards to libyuv I420Scale. The source planes of srcWidth by srcHeight are
 * resampled into the destination planes of dstWidth by dstHeight using the
 * filter selected by filter.
 *
 * @param srcY       the source luma plane.
 * @param srcStrideY the source luma row stride in bytes.
 * @param srcU       the source U (Cb) chroma plane.
 * @param srcStrideU the source U row stride in bytes.
 * @param srcV       the source V (Cr) chroma plane.
 * @param srcStrideV the source V row stride in bytes.
 * @param srcWidth   the source frame width in pixels.
 * @param srcHeight  the source frame height in pixels.
 * @param dstY       the destination luma plane.
 * @param dstStrideY the destination luma row stride in bytes.
 * @param dstU       the destination U (Cb) chroma plane.
 * @param dstStrideU the destination U row stride in bytes.
 * @param dstV       the destination V (Cr) chroma plane.
 * @param dstStrideV the destination V row stride in bytes.
 * @param dstWidth   the destination frame width in pixels.
 * @param dstHeight  the destination frame height in pixels.
 * @param filter     one of COBALT_YUV_FILTER_* selecting the resampling filter.
 * @return COBALT_YUV_OK on success, COBALT_YUV_ERROR on a parameter error.
 */
int32_t cobalt_yuv_i420_scale(const uint8_t *srcY, int32_t srcStrideY,
                              const uint8_t *srcU, int32_t srcStrideU,
                              const uint8_t *srcV, int32_t srcStrideV,
                              int32_t srcWidth, int32_t srcHeight,
                              uint8_t *dstY, int32_t dstStrideY,
                              uint8_t *dstU, int32_t dstStrideU,
                              uint8_t *dstV, int32_t dstStrideV,
                              int32_t dstWidth, int32_t dstHeight,
                              int32_t filter);

/**
 * Rotates an I420 frame by a multiple of 90 degrees.
 *
 * Forwards to libyuv I420Rotate. The source planes of width by height are
 * rotated into the destination planes by the angle selected by mode; for a 90 or
 * 270 degree rotation the destination width and height are swapped relative to
 * the source, which the caller must account for when sizing the destination
 * planes.
 *
 * @param srcY       the source luma plane.
 * @param srcStrideY the source luma row stride in bytes.
 * @param srcU       the source U (Cb) chroma plane.
 * @param srcStrideU the source U row stride in bytes.
 * @param srcV       the source V (Cr) chroma plane.
 * @param srcStrideV the source V row stride in bytes.
 * @param dstY       the destination luma plane.
 * @param dstStrideY the destination luma row stride in bytes.
 * @param dstU       the destination U (Cb) chroma plane.
 * @param dstStrideU the destination U row stride in bytes.
 * @param dstV       the destination V (Cr) chroma plane.
 * @param dstStrideV the destination V row stride in bytes.
 * @param width      the source frame width in pixels.
 * @param height     the source frame height in pixels.
 * @param mode       one of COBALT_YUV_ROTATE_* selecting the rotation angle.
 * @return COBALT_YUV_OK on success, COBALT_YUV_ERROR on a parameter error.
 */
int32_t cobalt_yuv_i420_rotate(const uint8_t *srcY, int32_t srcStrideY,
                               const uint8_t *srcU, int32_t srcStrideU,
                               const uint8_t *srcV, int32_t srcStrideV,
                               uint8_t *dstY, int32_t dstStrideY,
                               uint8_t *dstU, int32_t dstStrideU,
                               uint8_t *dstV, int32_t dstStrideV,
                               int32_t width, int32_t height,
                               int32_t mode);

#ifdef __cplusplus
}
#endif

#endif /* COBALT_YUV_SHIM_H */
