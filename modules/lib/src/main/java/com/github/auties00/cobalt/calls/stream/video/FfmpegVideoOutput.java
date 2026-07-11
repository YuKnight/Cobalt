package com.github.auties00.cobalt.calls.stream.video;

import com.github.auties00.cobalt.util.ffmpeg.AVCodecParameters;
import com.github.auties00.cobalt.util.ffmpeg.AVFormatContext;
import com.github.auties00.cobalt.util.ffmpeg.AVFrame;
import com.github.auties00.cobalt.util.ffmpeg.AVPacket;
import com.github.auties00.cobalt.util.ffmpeg.AVRational;
import com.github.auties00.cobalt.util.ffmpeg.AVStream;
import com.github.auties00.cobalt.util.ffmpeg.Ffmpeg;
import com.github.auties00.cobalt.util.ffmpeg.FFmpegError;
import com.github.auties00.cobalt.util.ffmpeg.FFmpegLoader;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.net.URI;
import java.nio.file.Path;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import com.github.auties00.cobalt.calls.stream.VideoFrame;
import com.github.auties00.cobalt.calls.stream.VideoOutput;
import com.github.auties00.cobalt.calls.stream.VideoPixelFormat;
import com.github.auties00.cobalt.calls.stream.ffmpeg.FfmpegHttpAvio;
import com.github.auties00.cobalt.calls.stream.ffmpeg.FfmpegIoWatchdog;

/**
 * Provides the FFmpeg backed base of the demuxed media video sources of a call, decoding the video track
 * of an input into the call's {@link VideoPixelFormat#I420 I420} frames at the input's detected geometry.
 *
 * <p>This is the shared engine of its nested {@link File} (a local media file) and {@link Uri} (a media
 * stream addressed by URI) sources. A subclass supplies only how the input is opened, by overriding
 * {@link #open(Arena, FfmpegIoWatchdog)}; the advertised geometry is the input's own native pixel
 * geometry, capped to {@code 1280} on the longer side and rounded to even, detected by that probe rather
 * than supplied by the subclass. Everything downstream of the opened demuxer is identical and lives here:
 * picking the first video stream, recording its time base for timestamp rescaling, opening its decoder,
 * decoding each packet, and converting each decoded picture to I420 at the detected {@link #width()} by
 * {@link #height()} geometry with libswscale. Each frame's presentation timestamp is rescaled from the
 * stream's native time base to microseconds. The end of the stream is signalled by {@link #take()}
 * returning {@code null}, and {@link #shutdown()} releases the native demuxer, decoder, and scaler.
 *
 * <p>Unlike the audio base, this source decodes inline on the engine's drain thread rather than ahead of
 * it: the call engine's capture loop paces the outbound video to wall clock using each frame's
 * presentation timestamp, so the source itself does not need to read ahead or sleep. Every blocking demux
 * read is bounded by the optional read timeout passed to the constructor: a local file source passes
 * {@code null}, and a network source passes its timeout, in which case a read the watchdog aborts surfaces
 * as an {@link IllegalStateException} from {@link #take()} rather than a clean end of input.
 *
 * @implNote This implementation pulls one picture directly from the decoder on each {@link #take()},
 * feeding a demux packet only when the decoder needs more input, so a packet carrying several pictures is
 * drained one picture at a time with no Java side frame queue. Each converted picture carries the
 * {@link VideoFrame#ptsMicros()} microsecond presentation timestamp rescaled from the stream time base, and
 * reuses one I420 pixel buffer lent under the {@link VideoOutput#take()} borrow contract.
 */
public abstract sealed class FfmpegVideoOutput implements VideoOutput
        permits FfmpegVideoOutput.File, FfmpegVideoOutput.Uri {
    /**
     * Holds the advertised frame rate a decoded media source reports, since the engine paces to wall clock
     * from each frame's presentation timestamp rather than from a fixed rate.
     */
    private static final int DEFAULT_FPS = 30;

    /**
     * Holds the advertised initial encoder bitrate in bits per second.
     *
     * @implNote This implementation mirrors the encoder seed
     * {@link com.github.auties00.cobalt.calls.media.video.codec.VideoCodecParams#DEFAULT_INIT_TARGET_BITRATE},
     * the WhatsApp {@code vid_rc.max_init_bwe} value; it is advertised only.
     */
    private static final int DEFAULT_BITRATE_BPS = 350_000;

    /**
     * Holds the libswscale destination stride alignment in bytes.
     *
     * @implNote This implementation pads each destination plane's stride up to this boundary so libswscale's
     * SIMD write paths, which round each row up to their vector width, never spill past a plane or the
     * destination buffer for a width that is not itself a multiple of the vector width; {@code 32} covers the
     * AVX2 routines the bundled build may select.
     */
    private static final int SWS_DST_ALIGN = 32;

    /**
     * Holds the maximum time any single blocking demux read may take before the watchdog aborts it, or
     * {@code null} for an input that does not block and needs no read timeout.
     */
    private final Duration readTimeout;

    /**
     * Holds the timeout watchdog the opener installs and the read loop arms, shared with the opener so a
     * stalled open or read aborts instead of blocking the engine's video drain thread forever.
     */
    private final FfmpegIoWatchdog watchdog;

    /**
     * Holds the arena owning every native allocation this source makes, closed when the source shuts
     * down.
     */
    private final Arena arena;

    /**
     * Holds the libavformat demuxer context pointer, which owns the input handle.
     */
    private final MemorySegment formatCtx;

    /**
     * Holds the libavcodec decoder context pointer.
     */
    private final MemorySegment codecCtx;

    /**
     * Holds the libswscale converter pointer that scales the decoded pixel format to
     * {@code AV_PIX_FMT_YUV420P}, built lazily and rebuilt when the frame geometry changes.
     */
    private MemorySegment swsCtx;

    /**
     * Holds the reusable demuxer packet pointer driven by the read loop.
     */
    private final MemorySegment packet;

    /**
     * Holds the reusable decoder output frame pointer that the decoder writes into.
     */
    private final MemorySegment frame;

    /**
     * Holds the index of the video stream chosen from the container.
     */
    private final int streamIndex;

    /**
     * Holds the numerator of the stream's time base, used to rescale presentation timestamps to
     * microseconds.
     */
    private final int timeBaseNum;

    /**
     * Holds the denominator of the stream's time base, used to rescale presentation timestamps to
     * microseconds.
     */
    private final int timeBaseDen;

    /**
     * Holds whether the decoder has been fully drained (reported the end of its input) or the source has
     * errored, so {@link #take()} returns {@code null} without touching the decoder again.
     */
    private boolean drained;

    /**
     * Holds whether the demuxer has reached the end of its input and the flush packet has been sent to the
     * decoder, so {@link #feedDecoder()} stops reading and the decoder emits its buffered frames before
     * ending.
     */
    private boolean flushed;

    /**
     * Holds the decoded source width the current {@link #swsCtx} converter was built for.
     */
    private int swsW;

    /**
     * Holds the decoded source height the current {@link #swsCtx} converter was built for.
     */
    private int swsH;

    /**
     * Holds the decoded source pixel format the current {@link #swsCtx} converter was built for.
     *
     * <p>When a later frame uses a different format, the converter is torn down and rebuilt.
     */
    private int swsFmt;

    /**
     * Holds the reusable libswscale destination plane buffer, or {@code null} until the first conversion.
     *
     * <p>Its geometry is the advertised {@link #width()} by {@link #height()} with each plane stride padded
     * to {@link #SWS_DST_ALIGN}, fixed for the source's life, so it is allocated once from {@link #arena} on
     * the first {@link #convertCurrentFrame()} and reused by every later conversion rather than opening a
     * confined arena and allocating a full destination buffer for each frame.
     */
    private MemorySegment scaleBuf;

    /**
     * Holds the reusable {@code uint8_t *dst[8]} destination plane pointer array handed to
     * {@code sws_scale}, pointing into {@link #scaleBuf}; built once alongside it.
     */
    private MemorySegment scaleDstData;

    /**
     * Holds the reusable {@code int dstStride[8]} destination stride array handed to {@code sws_scale};
     * built once alongside {@link #scaleBuf}.
     */
    private MemorySegment scaleDstStride;

    /**
     * Holds the padded Y plane stride, in bytes, of {@link #scaleBuf}.
     */
    private int scaleYStride;

    /**
     * Holds the padded chroma plane stride, in bytes, of {@link #scaleBuf}.
     */
    private int scaleCStride;

    /**
     * Holds the byte offset of the U plane within {@link #scaleBuf}, equal to its padded Y plane size.
     */
    private long scaleYPlaneBytes;

    /**
     * Holds the byte size of each padded chroma plane within {@link #scaleBuf}.
     */
    private long scaleCPlaneBytes;

    /**
     * Holds the reusable tightly packed I420 pixel buffer every converted frame is written into and lent
     * out over.
     *
     * <p>The advertised geometry is fixed for the source's life, so every frame occupies exactly
     * {@link #width()} by {@link #height()} I420 bytes; this one buffer is filled afresh by each
     * {@link #take()} and lent to the returned {@link VideoFrame} under the {@link VideoOutput#take()}
     * borrow contract, valid only until the next {@link #take()}, rather than allocating for each frame.
     * Safe because the decoder is drained one frame per {@link #take()}, so only one converted frame is ever
     * outstanding.
     */
    private final byte[] pixelBuffer;

    /**
     * Holds the advertised frame width in pixels, the input's detected native width capped to {@code 1280}
     * on the longer side and rounded to even.
     */
    private final int width;

    /**
     * Holds the advertised frame height in pixels, the input's detected native height capped and rounded to
     * even.
     */
    private final int height;

    /**
     * Guards {@link #shutdown()} so the teardown runs at most once, and read by the decode loop to end once
     * the source is closed.
     */
    private final AtomicBoolean closed = new AtomicBoolean();

    /**
     * Opens and probes the input demuxer, returning its context.
     *
     * <p>Called once from the constructor, before any field derived from the input is set.
     *
     * @implSpec An implementation opens the demuxer with {@code avformat_open_input}, probes it with
     * {@code avformat_find_stream_info}, and returns the opened {@code AVFormatContext} pointer reinterpreted
     * to the size of {@link AVFormatContext#layout()}. A network implementation installs the watchdog on the
     * context before opening and arms it around each blocking call; a file implementation ignores the
     * watchdog.
     * @param arena    the source's lifetime arena that owns the native allocations the open makes
     * @param watchdog the timeout watchdog to install and arm around blocking calls, or ignore for an input
     *                 that does not block
     * @return the opened and probed {@code AVFormatContext} pointer, reinterpreted to its layout size
     * @throws IllegalStateException if the input cannot be opened or probed
     */
    protected abstract MemorySegment open(Arena arena, FfmpegIoWatchdog watchdog);

    /**
     * Opens the input, detects its video geometry, and prepares the demuxer, decoder, and scaler chain.
     *
     * <p>Ensures the FFmpeg libraries are loaded, allocates the lifetime arena and the timeout watchdog,
     * opens and probes the input through {@link #open(Arena, FfmpegIoWatchdog)}, picks its first video
     * stream, records its time base, opens a decoder for its codec, and advertises the stream's native
     * geometry capped to {@code 1280} on the longer side and rounded to even. If any step fails the native
     * allocations are freed and the arena is closed before the exception propagates, so a failed
     * construction leaks no native resource.
     *
     * @param readTimeout the maximum time a single blocking demux read may take, or {@code null} for an
     *                    input that does not block and needs no read timeout
     * @throws IllegalStateException if the input cannot be opened, has no video stream, or its decoder
     *                               cannot be initialized
     */
    public FfmpegVideoOutput(Duration readTimeout) {
        FFmpegLoader.ensureLoaded();
        this.readTimeout = readTimeout;
        this.arena = Arena.ofShared();
        this.watchdog = new FfmpegIoWatchdog(arena);
        var localCodecCtx = MemorySegment.NULL;
        var localPacket = MemorySegment.NULL;
        var localFrame = MemorySegment.NULL;
        try {
            var openedCtx = open(arena, watchdog);
            var index = pickVideoStream(openedCtx);
            if (index < 0) {
                throw new IllegalStateException("no video stream in input");
            }
            var stream = streamPointer(openedCtx, index);
            var tb = AVStream.time_base(stream);
            var tbNum = AVRational.num(tb);
            var tbDen = AVRational.den(tb);
            var params = AVStream.codecpar(stream);
            var nativeWidth = AVCodecParameters.width(params);
            var nativeHeight = AVCodecParameters.height(params);
            var codecId = AVCodecParameters.codec_id(params);
            var codec = FFmpegError.requireNonNull("avcodec_find_decoder(" + codecId + ")",
                    Ffmpeg.avcodec_find_decoder(codecId));
            localCodecCtx = FFmpegError.requireNonNull("avcodec_alloc_context3",
                    Ffmpeg.avcodec_alloc_context3(codec));
            FFmpegError.check("avcodec_parameters_to_context",
                    Ffmpeg.avcodec_parameters_to_context(localCodecCtx, params));
            FFmpegError.check("avcodec_open2",
                    Ffmpeg.avcodec_open2(localCodecCtx, codec, MemorySegment.NULL));
            localPacket = FFmpegError.requireNonNull("av_packet_alloc", Ffmpeg.av_packet_alloc());
            localFrame = FFmpegError.requireNonNull("av_frame_alloc", Ffmpeg.av_frame_alloc());
            var capped = capGeometry(nativeWidth, nativeHeight);
            this.formatCtx = openedCtx;
            this.codecCtx = localCodecCtx;
            this.packet = localPacket;
            this.frame = localFrame;
            this.streamIndex = index;
            this.timeBaseNum = tbNum;
            this.timeBaseDen = tbDen;
            this.width = capped[0];
            this.height = capped[1];
            this.pixelBuffer = new byte[width * height + 2 * ((width / 2) * (height / 2))];
        } catch (RuntimeException e) {
            freePointer(localFrame, Ffmpeg::av_frame_free);
            freePointer(localPacket, Ffmpeg::av_packet_free);
            freePointer(localCodecCtx, Ffmpeg::avcodec_free_context);
            arena.close();
            throw e;
        }
    }

    /**
     * {@inheritDoc}
     *
     * <p>Pulls the next decoded picture straight from the decoder and returns it converted to
     * {@link VideoPixelFormat#I420 I420}. When the decoder needs more input it reads and feeds one video
     * packet (or the flush at the end of the input) and retries. Returns {@code null} once the decoder is
     * fully drained and no further frames remain, or once {@link #shutdown()} has ended the source. A demux,
     * decode, or convert failure, including a read the watchdog aborts, ends the source and propagates as an
     * {@link IllegalStateException}.
     *
     * @return {@inheritDoc}
     * @throws IllegalStateException if a configured read timeout aborts the demux read, or the demux,
     *                               decode, or convert reports a hard failure
     * @implNote This implementation does not pace itself: it returns frames as fast as the input decodes.
     * The call engine's capture loop paces the outbound video to wall clock using each frame's presentation
     * timestamp, so the input is transmitted at its natural rate without this source having to sleep. It
     * pulls exactly one frame per call directly from the decoder, feeding a packet only when the decoder
     * reports it needs more input, so a packet carrying several pictures is drained one picture at a time
     * with no Java side frame queue; each converted picture reuses {@link #pixelBuffer} lent under the
     * borrow contract.
     */
    @Override
    public VideoFrame take() {
        if (closed.get() || drained) {
            return null;
        }
        try {
            while (true) {
                var got = Ffmpeg.avcodec_receive_frame(codecCtx, frame);
                if (got >= 0) {
                    try {
                        return convertCurrentFrame();
                    } finally {
                        Ffmpeg.av_frame_unref(frame);
                    }
                }
                if (FFmpegError.isEof(got)) {
                    drained = true;
                    return null;
                }
                if (!FFmpegError.isAgain(got)) {
                    throw new IllegalStateException("avcodec_receive_frame failed: "
                            + FFmpegError.describe(got));
                }
                feedDecoder();
            }
        } catch (RuntimeException e) {
            drained = true;
            throw e;
        }
    }

    /**
     * {@inheritDoc}
     *
     * <p>Marks the source ended, cancels the timeout watchdog so a parked demux read returns at once, then
     * frees the libswscale converter, the decoder output frame, the demuxer packet, the decoder context,
     * and the demuxer context, and closes the owning arena. Guards each pointer against {@code null} and a
     * zero address, so the call is idempotent.
     */
    @Override
    public void shutdown() {
        if (!closed.compareAndSet(false, true)) {
            return;
        }
        watchdog.cancel();
        try (arena) {
            if (swsCtx != null && swsCtx.address() != 0L) {
                Ffmpeg.sws_freeContext(swsCtx);
            }
            freePointer(frame, Ffmpeg::av_frame_free);
            freePointer(packet, Ffmpeg::av_packet_free);
            freePointer(codecCtx, Ffmpeg::avcodec_free_context);
            freePointer(formatCtx, Ffmpeg::avformat_close_input);
            watchdog.closeIo();
        }
    }

    /**
     * {@inheritDoc}
     *
     * <p>A demuxed media source produces its frames inside {@link #take()} from the decoder and ignores
     * application writes, so this method does nothing.
     *
     * @param frame the frame that would be written; ignored
     */
    @Override
    public void write(VideoFrame frame) {
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     */
    @Override
    public int width() {
        return width;
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     */
    @Override
    public int height() {
        return height;
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     * @implNote This implementation advertises the fixed default frame rate; the engine paces the outbound
     * video to wall clock from each frame's presentation timestamp rather than from this rate.
     */
    @Override
    public int fps() {
        return DEFAULT_FPS;
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     * @implNote This implementation advertises the WhatsApp initial bitrate; the engine seeds the encoder
     * from its own rate controller rather than from this advertised field.
     */
    @Override
    public int bitrateBps() {
        return DEFAULT_BITRATE_BPS;
    }

    /**
     * Frees a native pointer held behind a single indirection, guarding against a {@code null} or zero
     * address.
     *
     * <p>Wraps the pointer in a temporary {@code T**} the way each libav* free expects and invokes the free.
     *
     * @param ptr  the native pointer to free, or a {@code null} or zero segment to skip
     * @param free the libav* free that takes the address of the pointer
     */
    private static void freePointer(MemorySegment ptr, Consumer<MemorySegment> free) {
        if (ptr == null || ptr.address() == 0L) {
            return;
        }
        try (var local = Arena.ofConfined()) {
            var pp = local.allocate(ValueLayout.ADDRESS);
            pp.set(ValueLayout.ADDRESS, 0L, ptr);
            free.accept(pp);
        }
    }

    /**
     * Feeds the decoder the next video packet, or the flush at the end of the input when the input is
     * exhausted.
     *
     * <p>Called by {@link #take()} when the decoder reports it needs more input. Reads packets, arming the
     * watchdog around each blocking read when a read timeout is configured, and skips packets of other
     * streams until one belongs to the chosen video stream, which it sends to the decoder. At the end of the
     * input it sends the null flush packet once and records that the demuxer is
     * {@linkplain #flushed exhausted}, so the decoder emits its remaining buffered frames; a read the
     * watchdog aborts or a transport error partway through is surfaced rather than treated as a clean end.
     * Does nothing once the input has already been flushed.
     *
     * @throws IllegalStateException if a configured read timeout aborts the demux read, a transport error
     *                               truncates it, or {@code avcodec_send_packet} reports a hard failure
     */
    private void feedDecoder() {
        if (flushed) {
            return;
        }
        while (true) {
            if (readTimeout != null) {
                watchdog.arm(readTimeout);
            }
            var read = Ffmpeg.av_read_frame(formatCtx, packet);
            if (readTimeout != null) {
                watchdog.disarm();
            }
            if (read < 0) {
                if (readTimeout != null && watchdog.fired()) {
                    throw new IllegalStateException("input read timed out after " + readTimeout);
                }
                var failure = watchdog.readFailure();
                if (failure != null) {
                    throw new IllegalStateException("input read failed: " + failure);
                }
                Ffmpeg.avcodec_send_packet(codecCtx, MemorySegment.NULL);
                flushed = true;
                return;
            }
            try {
                if (AVPacket.stream_index(packet) != streamIndex) {
                    continue;
                }
                var sent = Ffmpeg.avcodec_send_packet(codecCtx, packet);
                if (sent < 0 && !FFmpegError.isAgain(sent)) {
                    throw new IllegalStateException("avcodec_send_packet failed: "
                            + FFmpegError.describe(sent));
                }
                return;
            } finally {
                Ffmpeg.av_packet_unref(packet);
            }
        }
    }

    /**
     * Converts the current {@link #frame} to {@link VideoPixelFormat#I420 I420} at the advertised geometry
     * and wraps it in a {@link VideoFrame} with its presentation timestamp rescaled to microseconds.
     *
     * <p>Rejects decoded frames whose dimensions are below {@code 2} or odd, since I420's half resolution
     * chroma planes require even dimensions. Rebuilds the converter when the decoded geometry changes,
     * scales the three planes through the reusable {@link #scaleBuf} scratch (see
     * {@link #ensureScaleScratch(int, int)}) into a tightly packed I420 buffer sized for the advertised
     * {@link #width()} by {@link #height()} geometry the engine encodes at, and rescales the frame's best
     * effort timestamp from the stream time base to a microsecond value clamped to at least zero.
     *
     * @return the converted frame at the advertised geometry
     * @throws IllegalStateException if the decoded frame has unsupported dimensions or the scale fails
     * @implNote This implementation scales the decoded picture to the advertised geometry here, in the
     * source, because the source already owns an {@code sws} context; WhatsApp likewise scales the decoded
     * media file picture to the negotiated encode resolution before encoding, so the advertised and encoded
     * resolutions stay identical and the engine's encoder, built at the advertised geometry, never rejects a
     * native resolution frame. The libswscale destination is a scratch of the source's lifetime reused
     * across frames, and its plane strides are padded to {@link #SWS_DST_ALIGN} so the SIMD write paths
     * cannot spill past a plane; the scaled planes are copied into the reusable {@link #pixelBuffer},
     * stripping the padding.
     */
    private VideoFrame convertCurrentFrame() {
        var w = AVFrame.width(frame);
        var h = AVFrame.height(frame);
        var srcFmt = AVFrame.format(frame);
        if (w < 2 || h < 2 || (w & 1) != 0 || (h & 1) != 0) {
            throw new IllegalStateException(
                    "decoded frame has unsupported dimensions " + w + "x" + h);
        }
        var dstW = width();
        var dstH = height();
        rebuildSwsIfNeeded(w, h, srcFmt);
        ensureScaleScratch(dstW, dstH);

        var ySize = dstW * dstH;
        var uvSize = (dstW / 2) * (dstH / 2);
        var pixels = pixelBuffer;

        var produced = Ffmpeg.sws_scale(swsCtx,
                AVFrame.data(frame), AVFrame.linesize(frame),
                0, h, scaleDstData, scaleDstStride);
        if (produced < 0) {
            throw new IllegalStateException("sws_scale failed: " + produced);
        }
        copyPlaneRows(scaleBuf, 0L, scaleYStride, dstW, dstH, pixels, 0);
        copyPlaneRows(scaleBuf, scaleYPlaneBytes, scaleCStride, dstW / 2, dstH / 2, pixels, ySize);
        copyPlaneRows(scaleBuf, scaleYPlaneBytes + scaleCPlaneBytes, scaleCStride,
                dstW / 2, dstH / 2, pixels, ySize + uvSize);

        var ptsRaw = AVFrame.best_effort_timestamp(frame);
        var ptsMicros = (timeBaseDen == 0)
                ? 0L
                : Math.max(0L, ptsRaw * 1_000_000L * timeBaseNum / timeBaseDen);
        return new VideoFrame(pixels, VideoPixelFormat.I420, dstW, dstH, ptsMicros);
    }

    /**
     * Allocates the reusable libswscale destination scratch once, on the first conversion.
     *
     * <p>The advertised destination geometry is fixed for the source's life, so {@link #scaleBuf} and its
     * companion {@link #scaleDstData} plane pointer and {@link #scaleDstStride} stride arrays are built once
     * from {@link #arena} and reused by every later conversion, rather than opening a confined arena and
     * allocating a full destination buffer for each frame. Each destination plane's stride is padded to
     * {@link #SWS_DST_ALIGN} so libswscale's SIMD write paths cannot spill past a plane. A later call does
     * nothing once the scratch exists.
     *
     * @param dstW the advertised destination width in pixels
     * @param dstH the advertised destination height in pixels
     */
    private void ensureScaleScratch(int dstW, int dstH) {
        if (scaleBuf != null) {
            return;
        }
        var yStride = alignUp(dstW, SWS_DST_ALIGN);
        var cStride = alignUp(dstW / 2, SWS_DST_ALIGN);
        var yPlane = (long) yStride * dstH;
        var cPlane = (long) cStride * (dstH / 2);
        var buf = arena.allocate(yPlane + 2L * cPlane + SWS_DST_ALIGN);
        var dstData = arena.allocate(8L * ValueLayout.ADDRESS.byteSize());
        var dstStride = arena.allocate(8L * Integer.BYTES);
        dstData.setAtIndex(ValueLayout.ADDRESS, 0L, buf);
        dstData.setAtIndex(ValueLayout.ADDRESS, 1L, buf.asSlice(yPlane));
        dstData.setAtIndex(ValueLayout.ADDRESS, 2L, buf.asSlice(yPlane + cPlane));
        dstStride.setAtIndex(ValueLayout.JAVA_INT, 0L, yStride);
        dstStride.setAtIndex(ValueLayout.JAVA_INT, 1L, cStride);
        dstStride.setAtIndex(ValueLayout.JAVA_INT, 2L, cStride);
        this.scaleBuf = buf;
        this.scaleDstData = dstData;
        this.scaleDstStride = dstStride;
        this.scaleYStride = yStride;
        this.scaleCStride = cStride;
        this.scaleYPlaneBytes = yPlane;
        this.scaleCPlaneBytes = cPlane;
    }

    /**
     * Copies one plane from a strided native buffer into a tightly packed region of a byte array.
     *
     * <p>A plane whose row stride already equals its width is contiguous and copied in a single bulk
     * transfer; otherwise each row is copied separately to strip the stride padding, since
     * {@link VideoFrame} carries planes with no padding between rows.
     *
     * @param src       the native buffer holding the plane
     * @param srcOffset the byte offset of the plane's first row within {@code src}
     * @param srcStride the plane's row stride in bytes
     * @param planeW    the plane's width in bytes per row
     * @param planeH    the plane's height in rows
     * @param dst       the destination byte array
     * @param dstOffset the byte offset of the plane within {@code dst}
     */
    private static void copyPlaneRows(MemorySegment src, long srcOffset, int srcStride,
                                      int planeW, int planeH, byte[] dst, int dstOffset) {
        if (srcStride == planeW) {
            MemorySegment.copy(src, ValueLayout.JAVA_BYTE, srcOffset, dst, dstOffset, planeW * planeH);
            return;
        }
        for (var row = 0; row < planeH; row++) {
            MemorySegment.copy(src, ValueLayout.JAVA_BYTE, srcOffset + (long) row * srcStride,
                    dst, dstOffset + row * planeW, planeW);
        }
    }

    /**
     * Rounds a value up to the next multiple of the given power of two alignment.
     *
     * @param value the value to round
     * @param align the alignment, a power of two
     * @return the smallest multiple of {@code align} not below {@code value}
     */
    private static int alignUp(int value, int align) {
        return (value + align - 1) & ~(align - 1);
    }

    /**
     * Rebuilds the libswscale converter when the decoded frame dimensions or pixel format change between
     * frames.
     *
     * <p>Returns immediately when the current converter already matches the requested source geometry;
     * otherwise frees the old converter and builds one scaling from the decoded {@code (w, h, fmt)} triple
     * to {@code AV_PIX_FMT_YUV420P} at the advertised {@link #width()} by {@link #height()} geometry.
     *
     * @param w   the decoded source width
     * @param h   the decoded source height
     * @param fmt the decoded source pixel format
     * @throws IllegalStateException if the converter cannot be built
     */
    private void rebuildSwsIfNeeded(int w, int h, int fmt) {
        if (swsCtx != null && swsCtx.address() != 0L
                && w == swsW && h == swsH && fmt == swsFmt) {
            return;
        }
        if (swsCtx != null && swsCtx.address() != 0L) {
            Ffmpeg.sws_freeContext(swsCtx);
        }
        this.swsCtx = FFmpegError.requireNonNull(
                "sws_getContext",
                Ffmpeg.sws_getContext(w, h, fmt, width(), height(), Ffmpeg.AV_PIX_FMT_YUV420P(),
                        Ffmpeg.SWS_BILINEAR(),
                        MemorySegment.NULL, MemorySegment.NULL, MemorySegment.NULL));
        this.swsW = w;
        this.swsH = h;
        this.swsFmt = fmt;
    }

    /**
     * Returns the index of the first video stream in the container, or {@code -1} when none exists.
     *
     * @param formatCtx the demuxer context
     * @return the video stream index, or {@code -1} if the container has no video stream
     */
    private static int pickVideoStream(MemorySegment formatCtx) {
        var n = AVFormatContext.nb_streams(formatCtx);
        var streamsArr = AVFormatContext.streams(formatCtx)
                .reinterpret((long) n * ValueLayout.ADDRESS.byteSize());
        for (var i = 0; i < n; i++) {
            var stream = streamsArr.getAtIndex(ValueLayout.ADDRESS, i)
                    .reinterpret(AVStream.layout().byteSize());
            var params = AVStream.codecpar(stream);
            if (AVCodecParameters.codec_type(params) == Ffmpeg.AVMEDIA_TYPE_VIDEO()) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Returns the {@code AVStream} pointer at the given index in the container.
     *
     * @param formatCtx the demuxer context
     * @param index     the stream index
     * @return the stream pointer at that index
     */
    private static MemorySegment streamPointer(MemorySegment formatCtx, int index) {
        var n = AVFormatContext.nb_streams(formatCtx);
        var streamsArr = AVFormatContext.streams(formatCtx)
                .reinterpret((long) n * ValueLayout.ADDRESS.byteSize());
        return streamsArr.getAtIndex(ValueLayout.ADDRESS, index)
                .reinterpret(AVStream.layout().byteSize());
    }

    /**
     * Caps a native pixel geometry to the engine's maximum encoded resolution, preserving aspect ratio.
     *
     * <p>Returns the geometry rounded down to even when neither dimension exceeds {@code 1280}; otherwise
     * scales the longer dimension down to {@code 1280} and the shorter dimension proportionally, then rounds
     * both down to the nearest even value of at least {@code 2}. H264 requires even dimensions, and capping
     * the longer side bounds the encode cost of a high resolution source while keeping its aspect ratio, so
     * a 16:9 input is advertised as 16:9 rather than squished to a fixed 4:3 default.
     *
     * @param width  the native pixel width
     * @param height the native pixel height
     * @return a two element array of the capped, even {@code [width, height]}
     */
    private static int[] capGeometry(int width, int height) {
        if (Math.max(width, height) <= 1280) {
            return new int[]{evenDown(width), evenDown(height)};
        }
        int cappedWidth;
        int cappedHeight;
        if (width >= height) {
            cappedWidth = 1280;
            cappedHeight = (int) Math.round((double) height * 1280 / width);
        } else {
            cappedHeight = 1280;
            cappedWidth = (int) Math.round((double) width * 1280 / height);
        }
        return new int[]{evenDown(cappedWidth), evenDown(cappedHeight)};
    }

    /**
     * Rounds a dimension down to the nearest even value of at least {@code 2}.
     *
     * @param value the dimension to round
     * @return the largest even value not exceeding {@code value}, or {@code 2} when that would be below
     *         {@code 2}
     */
    private static int evenDown(int value) {
        return Math.max(2, value & ~1);
    }

    /**
     * Transmits the video track of a local media file.
     *
     * <p>Opens the file through libavformat with no read timeout, since a local read does not stall.
     */
    public static final class File extends FfmpegVideoOutput {
        /**
         * Holds the media file to open, assigned before the base constructor runs so {@link #open} can read
         * it.
         */
        private final Path path;

        /**
         * Opens the given media file, detects its native video geometry, and prepares the shared video
         * decode pipeline.
         *
         * @param path the media file to open
         * @throws NullPointerException  if {@code path} is {@code null}
         * @throws IllegalStateException if the file cannot be opened or has no video stream
         */
        public File(Path path) {
            this.path = path;
            super(null);
        }

        /**
         * {@inheritDoc}
         *
         * <p>Opens the file through libavformat's file protocol; a local read does not stall, so the
         * watchdog is ignored.
         *
         * @param arena    {@inheritDoc}
         * @param watchdog {@inheritDoc}
         * @return {@inheritDoc}
         */
        @Override
        protected MemorySegment open(Arena arena, FfmpegIoWatchdog watchdog) {
            return FfmpegHttpAvio.openFile(arena, path);
        }
    }

    /**
     * Transmits the video track of a media stream addressed by a URI, bounding every blocking demux call by
     * a timeout.
     *
     * <p>Opens {@code file}, {@code http}, and {@code https} URIs through {@link FfmpegHttpAvio}, which
     * serves HTTP over a JDK connection so the native library carries no network or TLS code.
     */
    public static final class Uri extends FfmpegVideoOutput {
        /**
         * Holds the media stream to open, assigned before the base constructor runs so {@link #open} can
         * read it.
         */
        private final URI uri;

        /**
         * Holds the timeout for each operation, assigned before the base constructor runs so {@link #open}
         * can read it.
         */
        private final Duration ioTimeout;

        /**
         * Opens the given URI, detects its native video geometry, and prepares the shared video decode
         * pipeline, bounding every blocking demux call with the given timeout.
         *
         * @param uri       the media stream to open
         * @param ioTimeout the maximum time any single connect, probe, or read may block; must be positive
         * @throws NullPointerException     if {@code uri} or {@code ioTimeout} is {@code null}
         * @throws IllegalArgumentException if {@code ioTimeout} is not positive, or the scheme is not
         *                                  permitted
         * @throws IllegalStateException    if the stream cannot be opened or has no video stream
         */
        public Uri(URI uri, Duration ioTimeout) {
            this.uri = uri;
            this.ioTimeout = ioTimeout;
            super(ioTimeout);
        }

        /**
         * {@inheritDoc}
         *
         * <p>Opens the URI through {@link FfmpegHttpAvio}, installing and arming the watchdog around the
         * blocking connect and probe.
         *
         * @param arena    {@inheritDoc}
         * @param watchdog {@inheritDoc}
         * @return {@inheritDoc}
         */
        @Override
        protected MemorySegment open(Arena arena, FfmpegIoWatchdog watchdog) {
            return FfmpegHttpAvio.openUri(arena, watchdog, uri, ioTimeout);
        }
    }
}
