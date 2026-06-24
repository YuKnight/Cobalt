package com.github.auties00.cobalt.calls2.dsp;

import com.github.auties00.cobalt.calls2.media.video.EncodedVideoFrame;
import com.github.auties00.cobalt.calls2.util.RateCalculator;

import java.util.Objects;
import java.util.TreeMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * The production {@link VideoJitterBuffer}: orders received frames by capture timestamp, schedules each
 * for release at the render time the {@link VideoTimingController} computes, and tracks the receiver loss
 * ratio.
 *
 * <p>Frames are held in a capture-timestamp-ordered map; {@link #insert(EncodedVideoFrame, long, long,
 * int)} computes the inter-frame delay against the previously inserted frame and feeds it with the frame
 * size to the {@link VideoJitterEstimator}, then records the sequence gap for the loss estimate.
 * {@link #poll(long)} releases the earliest frame whose render time has arrived, dropping a frame whose
 * render time has fallen more than the maximum video delay into the past and resetting the estimator and
 * timing controller when render timing collapses. After each release the buffer reconciles the released
 * frame's render time with the audio playout instant through the {@link AvSyncFeedbackSink}.
 *
 * <p>Insertion runs on the transport receive thread and polling on the render thread; a single
 * {@link ReentrantLock} serialises the two so the ordered map, the estimator, and the loss counters stay
 * consistent. The lock is held only for the bounded map and arithmetic work of one insert or one poll,
 * never across a blocking call, so neither thread stalls the other.
 *
 * @implNote This implementation ports {@code WaWebrtcVideoJitterBufferController}
 * ({@code video/video_stream_buffer_controller.cc}, {@code OnFrameToDecode} fn10480 of the wa-voip WASM
 * module {@code ff-tScznZ8P}, {@code rev-rtc-dsp}): it computes render time from {@code VCMTiming} plus
 * the jitter estimate, A/V-syncs to audio, drops frames past {@code kMaxVideoDelay_ms}, and resets the
 * jitter estimator and timing module on bad render timing
 * ({@code "Resetting jitter estimator and timing module"}). The capture-timestamp-ordered
 * {@link TreeMap} stands in for the native {@code FrameBuffer}; the loss ratio reuses two
 * {@link RateCalculator} sliding windows (lost and received frame counts) whose ratio yields the
 * receiver packet-loss ratio fed back to the sender, matching the {@code wa_seq_loss_calc} loss-over-
 * window computation. The maximum-video-delay drop bound {@link #MAX_VIDEO_DELAY_MS} is the upstream
 * WebRTC {@code kMaxVideoDelay_ms} ({@code 10000}, confirmed against the {@code 10000000}-microsecond
 * comparison in {@code OnFrameToDecode} fn10480). The loss-window duration {@link #LOSS_WINDOW_BUCKET_MS}
 * is pinned to {@code 50} ms so the ten buckets span the native {@code 500} ms loss window: the native
 * {@code wa_seq_loss_calc_create} (fn6359, asserting at
 * {@code xplat/wa-voip/wacall/network/src/utils/wa_seq_loss_calc.cc}, called from the video path
 * {@code media/video_state.cc do_video_upgrade}/{@code do_video_downgrade}) reads voip_param config
 * offset {@code 0xc14} and passes it as the window argument to {@code wa_ratio_calc_create} (fn6711,
 * {@code wa_ratio_calc.cc}), which divides it by ten and accumulates lost/received over ten buckets;
 * config {@code 0xc14} ({@code p->strm_param.max_npsi_timer_ms}) defaults to {@code 500} (fn11831 stores
 * {@code i32.const 500} to base {@code +0xc14} at instruction {@code 209628}). The {@code get_ratio}
 * (fn6714) returns {@code lost / received}, the same ratio shape as {@link #packetLossRatio()}. Because
 * {@code max_npsi_timer_ms} is a registered voip_param the {@code 500} ms window is server-overridable at
 * runtime; {@code 500} is the compiled-in default. The earlier-recovered
 * {@code wavtc_plr_scan_interval_ms} default of {@code 33} is the scan-task period, not the window.
 */
public final class LiveVideoJitterBuffer implements VideoJitterBuffer {
    /**
     * The maximum age, in milliseconds, by which a frame's render time may have passed before the frame
     * is dropped rather than rendered.
     *
     * <p>The {@code kMaxVideoDelay_ms} ceiling: a frame later than this is discarded so the stream does
     * not stall trying to render stale frames.
     */
    public static final long MAX_VIDEO_DELAY_MS = 10_000;

    /**
     * The per-bucket duration, in milliseconds, of the loss-ratio sliding windows.
     *
     * <p>The {@link RateCalculator} divides each window into ten buckets, so this duration times ten is
     * the loss-scan window over which the ratio is measured: ten buckets of {@code 50} ms give the
     * {@code 500} ms native loss window.
     */
    private static final long LOSS_WINDOW_BUCKET_MS = 50;

    /**
     * The minimum number of filled loss-window buckets before the loss ratio is reported.
     *
     * <p>Until this many buckets have accumulated the ratio is considered too sparse and is reported as
     * zero.
     */
    private static final int LOSS_WINDOW_MIN_BUCKETS = 2;

    /**
     * The jitter estimator fed each frame's size and inter-frame delay.
     */
    private final VideoJitterEstimator jitterEstimator;

    /**
     * The timing controller computing each frame's render time and driving A/V sync.
     */
    private final VideoTimingController timingController;

    /**
     * The sink the released frame's render time is reconciled against the audio playout instant through.
     */
    private final AvSyncFeedbackSink avSyncSink;

    /**
     * Serialises insertion on the transport thread against polling on the render thread.
     */
    private final ReentrantLock lock;

    /**
     * Holds the buffered frames ordered by capture RTP timestamp, earliest first.
     */
    private final TreeMap<Long, Buffered> frames;

    /**
     * The sliding window counting frames received, the denominator of the loss ratio.
     *
     * <p>Reallocated on {@link #reset()} because {@link RateCalculator} exposes no clear-to-empty
     * operation; a fresh window starts the loss measurement over.
     */
    private RateCalculator receivedFrames;

    /**
     * The sliding window counting frames detected lost from sequence gaps, the numerator of the loss
     * ratio.
     *
     * <p>Reallocated on {@link #reset()} alongside {@link #receivedFrames} so the two windows stay
     * aligned over the same scan interval.
     */
    private RateCalculator lostFrames;

    /**
     * The capture RTP timestamp of the previously inserted frame, for the inter-frame delay, or
     * {@code -1} before the first insert.
     */
    private long lastInsertCaptureTimestamp;

    /**
     * The local arrival time of the previously inserted frame, for the inter-frame delay, in
     * milliseconds.
     */
    private long lastInsertArrivalMs;

    /**
     * The RTP sequence number of the highest frame seen, for gap detection, or {@code -1} before the
     * first insert.
     */
    private int highestSequenceNumber;

    /**
     * The capture RTP timestamp of the most recently released frame, below which a later insert is
     * treated as already released and discarded, or {@code -1} before the first release.
     */
    private long lastReleasedCaptureTimestamp;

    /**
     * The audio playout instant, in local-clock milliseconds, the next A/V-sync reconciliation measures
     * against, or {@code -1} when no audio reference has been supplied.
     */
    private long audioPlayoutMs;

    /**
     * The current local-clock time supplied by the render thread, used for the A/V-sync measurement, in
     * milliseconds.
     */
    private long currentNowMs;

    /**
     * Constructs a video jitter buffer with the given estimator, timing controller, and A/V-sync sink.
     *
     * @param jitterEstimator  the jitter estimator fed each frame; never {@code null}
     * @param timingController  the timing controller computing render times; never {@code null}
     * @param avSyncSink        the sink the audio buffer reconciles against; never {@code null}, pass
     *                          {@link AvSyncFeedbackSink#noop()} to disable lip-sync correction
     */
    public LiveVideoJitterBuffer(VideoJitterEstimator jitterEstimator,
                                 VideoTimingController timingController,
                                 AvSyncFeedbackSink avSyncSink) {
        this.jitterEstimator = Objects.requireNonNull(jitterEstimator, "jitterEstimator cannot be null");
        this.timingController = Objects.requireNonNull(timingController, "timingController cannot be null");
        this.avSyncSink = Objects.requireNonNull(avSyncSink, "avSyncSink cannot be null");
        this.lock = new ReentrantLock();
        this.frames = new TreeMap<>();
        this.receivedFrames = new RateCalculator(LOSS_WINDOW_BUCKET_MS, LOSS_WINDOW_MIN_BUCKETS);
        this.lostFrames = new RateCalculator(LOSS_WINDOW_BUCKET_MS, LOSS_WINDOW_MIN_BUCKETS);
        this.lastInsertCaptureTimestamp = -1;
        this.lastInsertArrivalMs = 0;
        this.highestSequenceNumber = -1;
        this.lastReleasedCaptureTimestamp = -1;
        this.audioPlayoutMs = -1;
        this.currentNowMs = 0;
    }

    /**
     * {@inheritDoc}
     *
     * @implNote Computes the inter-frame delay as the arrival gap minus the timestamp gap converted to
     * milliseconds at {@link VideoTimingController#RTP_CLOCK_HZ}, feeds it with the frame size to the
     * jitter estimator, records the sequence gap against the highest sequence number into the loss
     * windows, and orders the frame into the map.
     */
    @Override
    public boolean insert(EncodedVideoFrame frame, long arrivalMs, long captureRtpTimestamp, int sequenceNumber) {
        Objects.requireNonNull(frame, "frame cannot be null");
        lock.lock();
        try {
            if (lastReleasedCaptureTimestamp >= 0 && captureRtpTimestamp <= lastReleasedCaptureTimestamp) {
                return false;
            }
            if (frames.containsKey(captureRtpTimestamp)) {
                return false;
            }

            if (lastInsertCaptureTimestamp >= 0) {
                var timestampGapMs = (captureRtpTimestamp - lastInsertCaptureTimestamp) * 1000.0
                        / VideoTimingController.RTP_CLOCK_HZ;
                var arrivalGapMs = (double) (arrivalMs - lastInsertArrivalMs);
                var frameDelayMs = arrivalGapMs - timestampGapMs;
                jitterEstimator.updateEstimate(frameDelayMs, frame.payload().length, arrivalMs);
            } else {
                jitterEstimator.updateEstimate(0.0, frame.payload().length, arrivalMs);
            }
            lastInsertCaptureTimestamp = captureRtpTimestamp;
            lastInsertArrivalMs = arrivalMs;

            recordSequence(sequenceNumber, arrivalMs);
            frames.put(captureRtpTimestamp, new Buffered(frame, arrivalMs, captureRtpTimestamp, sequenceNumber));
            return true;
        } finally {
            lock.unlock();
        }
    }

    /**
     * {@inheritDoc}
     *
     * @implNote Releases the earliest frame whose render time has arrived; drops a frame past
     * {@link #MAX_VIDEO_DELAY_MS} and, when its render time is implausibly far in the future, resets the
     * estimator and timing module ({@code "Resetting jitter estimator and timing module"}). After a
     * release reconciles the render time against {@link #audioPlayoutMs} through the A/V-sync sink.
     */
    @Override
    public ReleasedFrame poll(long nowMs) {
        lock.lock();
        try {
            currentNowMs = nowMs;
            while (!frames.isEmpty()) {
                var entry = frames.firstEntry();
                var buffered = entry.getValue();
                var renderTimeMs = timingController.renderTimeMs(
                        buffered.captureRtpTimestamp(), buffered.arrivalMs(), frames.size());

                if (renderTimeMs - nowMs > MAX_VIDEO_DELAY_MS) {
                    resetLocked();
                    return null;
                }
                if (nowMs - renderTimeMs > MAX_VIDEO_DELAY_MS) {
                    frames.pollFirstEntry();
                    lastReleasedCaptureTimestamp = buffered.captureRtpTimestamp();
                    continue;
                }
                if (nowMs < renderTimeMs) {
                    return null;
                }

                frames.pollFirstEntry();
                lastReleasedCaptureTimestamp = buffered.captureRtpTimestamp();
                if (audioPlayoutMs >= 0) {
                    timingController.updateAvSync(renderTimeMs, audioPlayoutMs, avSyncSink, nowMs);
                }
                return new ReleasedFrame(buffered.frame(), renderTimeMs);
            }
            return null;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Records the local-clock playout instant of the audio sample currently playing for the next
     * A/V-sync reconciliation.
     *
     * <p>Supplied by the audio playback path so the buffer can measure the video-minus-audio offset when
     * it releases a frame; until called, A/V-sync corrections are not emitted.
     *
     * @param playoutMs the audio playout instant in local-clock milliseconds
     */
    public void setAudioPlayoutMs(long playoutMs) {
        lock.lock();
        try {
            this.audioPlayoutMs = playoutMs;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Feeds a round-trip-time sample to the timing controller's retransmission margin.
     *
     * @param rttMs the round-trip-time measurement in milliseconds
     */
    public void onRttSample(double rttMs) {
        lock.lock();
        try {
            timingController.onRttSample(rttMs, currentNowMs);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Feeds an observed decode duration to the timing controller's decode-time estimate.
     *
     * @param decodeDurationMs the wall-clock duration the decoder took for a frame, in milliseconds
     */
    public void onFrameDecoded(long decodeDurationMs) {
        lock.lock();
        try {
            timingController.onFrameDecoded(decodeDurationMs, currentNowMs);
        } finally {
            lock.unlock();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int bufferedFrameCount() {
        lock.lock();
        try {
            return frames.size();
        } finally {
            lock.unlock();
        }
    }

    /**
     * {@inheritDoc}
     *
     * @implNote The ratio of the lost-frame sliding window to the sum of the lost and received windows,
     * both measured over the same scan duration so the per-second scaling cancels; zero when no frames
     * have been received.
     */
    @Override
    public double packetLossRatio() {
        lock.lock();
        try {
            var lost = lostFrames.rate(currentNowMs);
            var received = receivedFrames.rate(currentNowMs);
            var total = lost + received;
            if (total <= 0) {
                return 0.0;
            }
            return (double) lost / total;
        } finally {
            lock.unlock();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void reset() {
        lock.lock();
        try {
            resetLocked();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Records a received frame's sequence number into the loss windows, counting any gap as lost.
     *
     * <p>When the sequence number advances the highest seen by more than one, the intervening frames are
     * counted as lost; a sequence number at or below the highest seen is a reordered or duplicate frame
     * and adds only to the received count. The highest sequence number is advanced to the new value when
     * it is newer.
     *
     * @param sequenceNumber the frame's RTP sequence number
     * @param nowMs          the current time in milliseconds, advancing the loss windows
     */
    private void recordSequence(int sequenceNumber, long nowMs) {
        receivedFrames.update(nowMs, 1);
        if (highestSequenceNumber < 0) {
            highestSequenceNumber = sequenceNumber;
            return;
        }
        var gap = sequenceDistance(sequenceNumber, highestSequenceNumber);
        if (gap > 1) {
            lostFrames.update(nowMs, gap - 1);
        }
        if (gap > 0) {
            highestSequenceNumber = sequenceNumber;
        }
    }

    /**
     * Returns the forward distance from a reference sequence number to a newer one, accounting for
     * 16-bit wrap.
     *
     * <p>A positive result is the number of sequence steps the new number is ahead of the reference; a
     * non-positive result means the new number is not newer (reordered or duplicate).
     *
     * @param sequenceNumber the candidate newer sequence number
     * @param reference      the highest sequence number seen so far
     * @return the wrap-aware forward distance, non-positive when not newer
     */
    private static int sequenceDistance(int sequenceNumber, int reference) {
        return (short) (sequenceNumber - reference);
    }

    /**
     * Clears all buffered frames and resets the estimator, timing controller, and loss windows.
     *
     * <p>The lock-held body of {@link #reset()} and the bad-render-timing path of {@link #poll(long)}.
     */
    private void resetLocked() {
        frames.clear();
        jitterEstimator.reset();
        timingController.reset();
        receivedFrames = new RateCalculator(LOSS_WINDOW_BUCKET_MS, LOSS_WINDOW_MIN_BUCKETS);
        lostFrames = new RateCalculator(LOSS_WINDOW_BUCKET_MS, LOSS_WINDOW_MIN_BUCKETS);
        lastInsertCaptureTimestamp = -1;
        lastInsertArrivalMs = 0;
        highestSequenceNumber = -1;
        lastReleasedCaptureTimestamp = -1;
    }

    /**
     * One buffered frame together with the timing recorded when it was inserted.
     *
     * @param frame               the compressed frame; never {@code null}
     * @param arrivalMs           the local arrival time in milliseconds
     * @param captureRtpTimestamp the 90 kHz capture RTP timestamp, the map key
     * @param sequenceNumber      the RTP sequence number
     */
    private record Buffered(EncodedVideoFrame frame, long arrivalMs, long captureRtpTimestamp, int sequenceNumber) {
    }
}
