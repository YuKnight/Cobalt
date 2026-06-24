package com.github.auties00.cobalt.calls2.media.video;

import com.github.auties00.cobalt.calls2.media.video.yuv.bindings.CobaltYuv;
import com.github.auties00.cobalt.calls2.stream.VideoFrame;
import com.github.auties00.cobalt.calls2.stream.VideoPixelFormat;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.util.Objects;

/**
 * Converts raw video pictures between the planar {@link VideoPixelFormat#I420 I420} layout the call
 * codecs operate on, the semi-planar {@link VideoPixelFormat#NV12 NV12} layout several cameras deliver,
 * and packed 32-bit ARGB pixels used by render and capture surfaces, and scales or rotates an
 * {@link VideoPixelFormat#I420 I420} picture.
 *
 * <p>This is a native libyuv-backed converter: every color-space transform, chroma repack, scale, and
 * rotate is performed by libyuv through the portable {@link CobaltYuv} shim binding rather than in
 * Java. Each call stages the source planes (and the destination scratch) in a per-call confined
 * {@link Arena}, drives the matching libyuv entry point, and copies the result back onto the heap; no
 * native memory escapes a call. The color transforms use libyuv's ITU-R BT.601 studio-swing
 * (limited-range) matrix, with Y in {@code [16, 235]} and chroma centered at {@code 128}. The repack
 * and color methods preserve picture geometry and orientation; only {@link #scale(VideoFrame, int, int)}
 * changes the dimensions and only {@link #rotate(VideoFrame, int)} changes the orientation. Both
 * dimensions must be even so the half-resolution chroma planes have integral sizes, matching
 * {@link VideoFrame}.
 *
 * <p>The class is stateless and its methods are pure, so a single instance is safe to share across
 * threads; each call confines its native scratch to a thread-local {@link Arena} closed before the call
 * returns. It is offered as an instance rather than a set of statics so the libyuv binding can be
 * swapped behind the same surface without changing call sites.
 *
 * @implNote This implementation delegates to libyuv via {@link CobaltYuv}, reproducing the color-convert
 * path the wa-voip WASM module {@code ff-tScznZ8P} drives in {@code converter_libyuv.cc} (string @0x9484
 * {@code libyuv_conv_convert}); the shipping engine performs the same conversions through libyuv
 * ({@code I420ToARGB}, {@code ARGBToI420}, {@code NV12ToI420}, {@code I420ToNV12}, {@code I420Scale},
 * {@code I420Rotate}). libyuv's {@code ARGB} pixel is four bytes ordered B, G, R, A in memory, which on a
 * little-endian host is a {@code uint32} numerically equal to Java's {@code 0xAARRGGBB} packed int. All
 * Cobalt target hosts are little-endian (x86_64, aarch64), so the native four-byte ARGB buffer is mapped
 * to the Java {@code int[]} one pixel at a time with {@link ValueLayout#JAVA_INT} in native byte order:
 * the int read or written is already {@code 0xAARRGGBB} and no manual channel shuffle is performed.
 * Reading the buffer with a big-endian or byte-reversed view would swap the red and blue channels.
 */
public final class YuvConverter {
    /**
     * Constructs a stateless converter.
     */
    public YuvConverter() {

    }

    /**
     * Converts a planar {@link VideoPixelFormat#I420 I420} or semi-planar
     * {@link VideoPixelFormat#NV12 NV12} frame to packed 32-bit ARGB pixels.
     *
     * <p>The result holds {@code width * height} pixels, one {@code int} each in {@code 0xAARRGGBB}
     * order with the alpha byte set to {@code 0xFF} (fully opaque). An {@link VideoPixelFormat#NV12 NV12}
     * input is first repacked to {@link VideoPixelFormat#I420 I420} (the shim has no direct NV12-to-ARGB
     * entry point) and then color converted by libyuv's BT.601 limited-range matrix; out-of-gamut
     * results are clamped to {@code [0, 255]} per channel inside libyuv.
     *
     * @param frame the source picture in {@link VideoPixelFormat#I420 I420} or
     *              {@link VideoPixelFormat#NV12 NV12}
     * @return the packed ARGB pixels, {@code width * height} ints in {@code 0xAARRGGBB} order
     * @throws NullPointerException  if {@code frame} is {@code null}
     * @throws IllegalStateException if the libyuv conversion fails
     */
    public int[] toArgb(VideoFrame frame) {
        Objects.requireNonNull(frame, "frame cannot be null");
        var i420 = toI420(frame);
        var width = i420.width();
        var height = i420.height();
        var argb = new int[width * height];
        try (var arena = Arena.ofConfined()) {
            var src = stageI420(arena, i420);
            var chromaWidth = width / 2;
            var lumaSize = width * height;
            var chromaSize = chromaWidth * (height / 2);
            var dstArgb = arena.allocate((long) width * height * 4);
            var status = CobaltYuv.cobalt_yuv_i420_to_argb(
                    src.asSlice(0, lumaSize), width,
                    src.asSlice(lumaSize, chromaSize), chromaWidth,
                    src.asSlice(lumaSize + chromaSize, chromaSize), chromaWidth,
                    dstArgb, width * 4,
                    width, height);
            checkStatus(status, "cobalt_yuv_i420_to_argb");
            MemorySegment.copy(dstArgb, ValueLayout.JAVA_INT, 0, argb, 0, argb.length);
        }
        return argb;
    }

    /**
     * Converts packed 32-bit ARGB pixels to a planar {@link VideoPixelFormat#I420 I420} frame.
     *
     * <p>The alpha channel is ignored. libyuv computes luma per pixel and the {@code 4:2:0} chroma from
     * each block, applying the BT.601 limited-range matrix; the resulting frame occupies the canonical
     * I420 byte count for the given dimensions.
     *
     * @param argb      the packed ARGB pixels, at least {@code width * height} ints in
     *                  {@code 0xAARRGGBB} order
     * @param width     the picture width in pixels; even and at least {@code 2}
     * @param height    the picture height in pixels; even and at least {@code 2}
     * @param ptsMicros the presentation timestamp to stamp on the produced frame
     * @return the converted {@link VideoPixelFormat#I420 I420} frame
     * @throws NullPointerException     if {@code argb} is {@code null}
     * @throws IllegalArgumentException if {@code width} or {@code height} is odd or below {@code 2}, or
     *                                  if {@code argb} is shorter than {@code width * height}
     * @throws IllegalStateException    if the libyuv conversion fails
     */
    public VideoFrame argbToI420(int[] argb, int width, int height, long ptsMicros) {
        Objects.requireNonNull(argb, "argb cannot be null");
        if (width < 2 || width % 2 != 0) {
            throw new IllegalArgumentException("width must be even and >= 2, got " + width);
        }
        if (height < 2 || height % 2 != 0) {
            throw new IllegalArgumentException("height must be even and >= 2, got " + height);
        }
        if (argb.length < width * height) {
            throw new IllegalArgumentException(
                    "argb must hold at least " + (width * height) + " pixels, got " + argb.length);
        }
        var chromaWidth = width / 2;
        var lumaSize = width * height;
        var chromaSize = chromaWidth * (height / 2);
        var out = new byte[lumaSize + 2 * chromaSize];
        try (var arena = Arena.ofConfined()) {
            var srcArgb = arena.allocate((long) width * height * 4);
            MemorySegment.copy(argb, 0, srcArgb, ValueLayout.JAVA_INT, 0, width * height);
            var dst = arena.allocate(out.length);
            var status = CobaltYuv.cobalt_yuv_argb_to_i420(
                    srcArgb, width * 4,
                    dst.asSlice(0, lumaSize), width,
                    dst.asSlice(lumaSize, chromaSize), chromaWidth,
                    dst.asSlice(lumaSize + chromaSize, chromaSize), chromaWidth,
                    width, height);
            checkStatus(status, "cobalt_yuv_argb_to_i420");
            MemorySegment.copy(dst, ValueLayout.JAVA_BYTE, 0, out, 0, out.length);
        }
        return new VideoFrame(out, VideoPixelFormat.I420, width, height, ptsMicros);
    }

    /**
     * Repacks a frame into the planar {@link VideoPixelFormat#I420 I420} layout, converting from
     * {@link VideoPixelFormat#NV12 NV12} if needed.
     *
     * <p>This is a loss-free chroma repack, not a color transform: an {@link VideoPixelFormat#I420 I420}
     * input is returned unchanged, and an {@link VideoPixelFormat#NV12 NV12} input has its interleaved
     * chroma plane split by libyuv into the separate U and V planes. The native encoders the call path
     * drives consume planar I420, so a camera that captures in {@link VideoPixelFormat#NV12 NV12} is
     * funneled through this method first.
     *
     * @param frame the source frame in {@link VideoPixelFormat#I420 I420} or
     *              {@link VideoPixelFormat#NV12 NV12}
     * @return an {@link VideoPixelFormat#I420 I420} frame with the same pixels, dimensions, and
     *         timestamp
     * @throws NullPointerException  if {@code frame} is {@code null}
     * @throws IllegalStateException if the libyuv conversion fails
     */
    public VideoFrame toI420(VideoFrame frame) {
        Objects.requireNonNull(frame, "frame cannot be null");
        if (frame.format() == VideoPixelFormat.I420) {
            return frame;
        }
        var width = frame.width();
        var height = frame.height();
        var lumaSize = width * height;
        var chromaWidth = width / 2;
        var chromaSize = chromaWidth * (height / 2);
        var out = new byte[lumaSize + 2 * chromaSize];
        try (var arena = Arena.ofConfined()) {
            var src = arena.allocate(frame.pixels().length);
            MemorySegment.copy(frame.pixels(), 0, src, ValueLayout.JAVA_BYTE, 0, frame.pixels().length);
            var dst = arena.allocate(out.length);
            var status = CobaltYuv.cobalt_yuv_nv12_to_i420(
                    src.asSlice(0, lumaSize), width,
                    src.asSlice(lumaSize, 2L * chromaSize), width,
                    dst.asSlice(0, lumaSize), width,
                    dst.asSlice(lumaSize, chromaSize), chromaWidth,
                    dst.asSlice(lumaSize + chromaSize, chromaSize), chromaWidth,
                    width, height);
            checkStatus(status, "cobalt_yuv_nv12_to_i420");
            MemorySegment.copy(dst, ValueLayout.JAVA_BYTE, 0, out, 0, out.length);
        }
        return new VideoFrame(out, VideoPixelFormat.I420, width, height, frame.ptsMicros());
    }

    /**
     * Repacks a frame into the semi-planar {@link VideoPixelFormat#NV12 NV12} layout, converting from
     * {@link VideoPixelFormat#I420 I420} if needed.
     *
     * <p>This is the inverse chroma repack of {@link #toI420(VideoFrame)}: an
     * {@link VideoPixelFormat#NV12 NV12} input is returned unchanged, and an
     * {@link VideoPixelFormat#I420 I420} input has its separate U and V planes interleaved by libyuv into
     * a single chroma plane. It is offered for render or capture surfaces that require interleaved
     * chroma.
     *
     * @param frame the source frame in {@link VideoPixelFormat#I420 I420} or
     *              {@link VideoPixelFormat#NV12 NV12}
     * @return an {@link VideoPixelFormat#NV12 NV12} frame with the same pixels, dimensions, and
     *         timestamp
     * @throws NullPointerException  if {@code frame} is {@code null}
     * @throws IllegalStateException if the libyuv conversion fails
     */
    public VideoFrame toNv12(VideoFrame frame) {
        Objects.requireNonNull(frame, "frame cannot be null");
        if (frame.format() == VideoPixelFormat.NV12) {
            return frame;
        }
        var width = frame.width();
        var height = frame.height();
        var lumaSize = width * height;
        var chromaWidth = width / 2;
        var chromaSize = chromaWidth * (height / 2);
        var out = new byte[lumaSize + 2 * chromaSize];
        try (var arena = Arena.ofConfined()) {
            var src = arena.allocate(frame.pixels().length);
            MemorySegment.copy(frame.pixels(), 0, src, ValueLayout.JAVA_BYTE, 0, frame.pixels().length);
            var dst = arena.allocate(out.length);
            var status = CobaltYuv.cobalt_yuv_i420_to_nv12(
                    src.asSlice(0, lumaSize), width,
                    src.asSlice(lumaSize, chromaSize), chromaWidth,
                    src.asSlice(lumaSize + chromaSize, chromaSize), chromaWidth,
                    dst.asSlice(0, lumaSize), width,
                    dst.asSlice(lumaSize, 2L * chromaSize), width,
                    width, height);
            checkStatus(status, "cobalt_yuv_i420_to_nv12");
            MemorySegment.copy(dst, ValueLayout.JAVA_BYTE, 0, out, 0, out.length);
        }
        return new VideoFrame(out, VideoPixelFormat.NV12, width, height, frame.ptsMicros());
    }

    /**
     * Resamples an {@link VideoPixelFormat#I420 I420} picture to new dimensions with libyuv's bilinear
     * filter.
     *
     * <p>The source is first repacked to {@link VideoPixelFormat#I420 I420} via
     * {@link #toI420(VideoFrame)} if it is in {@link VideoPixelFormat#NV12 NV12}, then libyuv resamples
     * each plane to the destination geometry. The returned frame keeps the source timestamp. The
     * destination dimensions follow the same parity rule as every frame: both must be even and at least
     * {@code 2}, which {@link VideoFrame} enforces on construction.
     *
     * @param src       the source picture in {@link VideoPixelFormat#I420 I420} or
     *                  {@link VideoPixelFormat#NV12 NV12}
     * @param dstWidth  the target width in pixels; even and at least {@code 2}
     * @param dstHeight the target height in pixels; even and at least {@code 2}
     * @return the resampled {@link VideoPixelFormat#I420 I420} frame at {@code dstWidth x dstHeight}
     * @throws NullPointerException     if {@code src} is {@code null}
     * @throws IllegalArgumentException if {@code dstWidth} or {@code dstHeight} is odd or below
     *                                  {@code 2}
     * @throws IllegalStateException    if the libyuv conversion fails
     */
    public VideoFrame scale(VideoFrame src, int dstWidth, int dstHeight) {
        Objects.requireNonNull(src, "src cannot be null");
        if (dstWidth < 2 || dstWidth % 2 != 0) {
            throw new IllegalArgumentException("dstWidth must be even and >= 2, got " + dstWidth);
        }
        if (dstHeight < 2 || dstHeight % 2 != 0) {
            throw new IllegalArgumentException("dstHeight must be even and >= 2, got " + dstHeight);
        }
        var source = toI420(src);
        var srcWidth = source.width();
        var srcHeight = source.height();
        var dstChromaWidth = dstWidth / 2;
        var dstLumaSize = dstWidth * dstHeight;
        var dstChromaSize = dstChromaWidth * (dstHeight / 2);
        var out = new byte[dstLumaSize + 2 * dstChromaSize];
        try (var arena = Arena.ofConfined()) {
            var srcSeg = stageI420(arena, source);
            var srcChromaWidth = srcWidth / 2;
            var srcLumaSize = srcWidth * srcHeight;
            var srcChromaSize = srcChromaWidth * (srcHeight / 2);
            var dst = arena.allocate(out.length);
            var status = CobaltYuv.cobalt_yuv_i420_scale(
                    srcSeg.asSlice(0, srcLumaSize), srcWidth,
                    srcSeg.asSlice(srcLumaSize, srcChromaSize), srcChromaWidth,
                    srcSeg.asSlice(srcLumaSize + srcChromaSize, srcChromaSize), srcChromaWidth,
                    srcWidth, srcHeight,
                    dst.asSlice(0, dstLumaSize), dstWidth,
                    dst.asSlice(dstLumaSize, dstChromaSize), dstChromaWidth,
                    dst.asSlice(dstLumaSize + dstChromaSize, dstChromaSize), dstChromaWidth,
                    dstWidth, dstHeight,
                    CobaltYuv.COBALT_YUV_FILTER_BILINEAR());
            checkStatus(status, "cobalt_yuv_i420_scale");
            MemorySegment.copy(dst, ValueLayout.JAVA_BYTE, 0, out, 0, out.length);
        }
        return new VideoFrame(out, VideoPixelFormat.I420, dstWidth, dstHeight, source.ptsMicros());
    }

    /**
     * Rotates an {@link VideoPixelFormat#I420 I420} picture clockwise by a quarter-turn multiple.
     *
     * <p>The source is first repacked to {@link VideoPixelFormat#I420 I420} via
     * {@link #toI420(VideoFrame)} if it is in {@link VideoPixelFormat#NV12 NV12}, then libyuv rotates
     * each plane. A {@code 90}- or {@code 270}-degree rotation transposes the picture, so the returned
     * frame swaps width and height; {@code 0} and {@code 180} keep the source geometry. The returned
     * frame keeps the source timestamp.
     *
     * @param src     the source picture in {@link VideoPixelFormat#I420 I420} or
     *                {@link VideoPixelFormat#NV12 NV12}
     * @param degrees the clockwise rotation in degrees; exactly {@code 0}, {@code 90}, {@code 180}, or
     *                {@code 270}
     * @return the rotated {@link VideoPixelFormat#I420 I420} frame, with width and height swapped for
     *         {@code 90} and {@code 270}
     * @throws NullPointerException     if {@code src} is {@code null}
     * @throws IllegalArgumentException if {@code degrees} is not {@code 0}, {@code 90}, {@code 180}, or
     *                                  {@code 270}
     * @throws IllegalStateException    if the libyuv conversion fails
     */
    public VideoFrame rotate(VideoFrame src, int degrees) {
        Objects.requireNonNull(src, "src cannot be null");
        var mode = rotateMode(degrees);
        var source = toI420(src);
        var srcWidth = source.width();
        var srcHeight = source.height();
        var swap = degrees == 90 || degrees == 270;
        var dstWidth = swap ? srcHeight : srcWidth;
        var dstHeight = swap ? srcWidth : srcHeight;
        var dstChromaWidth = dstWidth / 2;
        var dstLumaSize = dstWidth * dstHeight;
        var dstChromaSize = dstChromaWidth * (dstHeight / 2);
        var out = new byte[dstLumaSize + 2 * dstChromaSize];
        try (var arena = Arena.ofConfined()) {
            var srcSeg = stageI420(arena, source);
            var srcChromaWidth = srcWidth / 2;
            var srcLumaSize = srcWidth * srcHeight;
            var srcChromaSize = srcChromaWidth * (srcHeight / 2);
            var dst = arena.allocate(out.length);
            var status = CobaltYuv.cobalt_yuv_i420_rotate(
                    srcSeg.asSlice(0, srcLumaSize), srcWidth,
                    srcSeg.asSlice(srcLumaSize, srcChromaSize), srcChromaWidth,
                    srcSeg.asSlice(srcLumaSize + srcChromaSize, srcChromaSize), srcChromaWidth,
                    dst.asSlice(0, dstLumaSize), dstWidth,
                    dst.asSlice(dstLumaSize, dstChromaSize), dstChromaWidth,
                    dst.asSlice(dstLumaSize + dstChromaSize, dstChromaSize), dstChromaWidth,
                    srcWidth, srcHeight,
                    mode);
            checkStatus(status, "cobalt_yuv_i420_rotate");
            MemorySegment.copy(dst, ValueLayout.JAVA_BYTE, 0, out, 0, out.length);
        }
        return new VideoFrame(out, VideoPixelFormat.I420, dstWidth, dstHeight, source.ptsMicros());
    }

    /**
     * Stages a planar {@link VideoPixelFormat#I420 I420} frame into a native segment in the given arena.
     *
     * <p>The whole I420 byte buffer is copied contiguously, so the Y plane sits at offset {@code 0}, the
     * U plane at {@code width*height}, and the V plane at {@code width*height + (width/2)*(height/2)},
     * each ready to be sliced for a libyuv call.
     *
     * @param arena the arena owning the returned segment
     * @param frame the planar I420 source frame
     * @return a native segment holding the frame's pixel bytes
     */
    private static MemorySegment stageI420(Arena arena, VideoFrame frame) {
        var pixels = frame.pixels();
        var segment = arena.allocate(pixels.length);
        MemorySegment.copy(pixels, 0, segment, ValueLayout.JAVA_BYTE, 0, pixels.length);
        return segment;
    }

    /**
     * Resolves the {@code COBALT_YUV_ROTATE_*} mode for a clockwise rotation in degrees.
     *
     * @param degrees the clockwise rotation in degrees
     * @return the matching libyuv rotation mode
     * @throws IllegalArgumentException if {@code degrees} is not {@code 0}, {@code 90}, {@code 180}, or
     *                                  {@code 270}
     */
    private static int rotateMode(int degrees) {
        return switch (degrees) {
            case 0 -> CobaltYuv.COBALT_YUV_ROTATE_0();
            case 90 -> CobaltYuv.COBALT_YUV_ROTATE_90();
            case 180 -> CobaltYuv.COBALT_YUV_ROTATE_180();
            case 270 -> CobaltYuv.COBALT_YUV_ROTATE_270();
            default -> throw new IllegalArgumentException("degrees must be 0, 90, 180, or 270, got " + degrees);
        };
    }

    /**
     * Throws when a libyuv shim call returned a non-OK status.
     *
     * @param status   the status code returned by the shim
     * @param function the shim function name, for the thrown message
     * @throws IllegalStateException if {@code status} is not {@link CobaltYuv#COBALT_YUV_OK()}
     */
    private static void checkStatus(int status, String function) {
        if (status != CobaltYuv.COBALT_YUV_OK()) {
            throw new IllegalStateException(function + " failed with status " + status);
        }
    }
}
