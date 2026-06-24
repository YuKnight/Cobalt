package com.github.auties00.cobalt.calls2.dsp;

import java.util.Arrays;
import java.util.Objects;

/**
 * Estimates the target playout buffer level from an exponentially-forgetting histogram of packet
 * inter-arrival-time deviations, the adaptive heart of the {@link LiveNetEq} jitter buffer.
 *
 * <p>The manager keeps a histogram over inter-arrival-time deviations measured in whole packet durations:
 * each {@link #update(long, long)} computes how many packet durations elapsed between this packet's arrival
 * and the previous one, subtracts the one packet duration a perfectly paced stream would show, and adds
 * the resulting non-negative deviation bucket into the histogram with an exponential forgetting that
 * decays older observations. The target level is read as the inverse cumulative distribution of that
 * histogram at the underrun quantile: the smallest deviation under which the configured fraction of
 * packets arrived in time, so a buffer of that depth absorbs almost all the jitter. The estimate is
 * biased by the configured offset and clamped into the configured delay bounds, and on a bursty link the
 * optional peak detector raises a floor under the estimate so a recent spike is not forgotten before the
 * next one arrives.
 *
 * <p>The target level is reported in milliseconds through {@link #targetLevelMillis()} and is recomputed on
 * each update. {@link #reset()} clears the histogram and the peak detector when the stream is
 * reconfigured. Instances are not thread-safe; the receive path drives one manager from a single thread.
 *
 * @implNote This implementation ports {@code concerto::DelayManager} of the wa-voip WASM module
 * {@code ff-tScznZ8P} ({@code rev-rtc-dsp} algorithm "NetEq adaptive jitter buffer"): the
 * exponentially-forgetting inter-arrival histogram with the underrun quantile
 * ({@code options.neteq_underrun_quantile=0.98}, {@code options.neteq_underrun_forget_factor=0.99} from the
 * live voip_settings, voip-settings-merged.json decoded from {@code <voip_settings uncompressed="1">} in
 * stanzas-primary.jsonl), the signed delay offset ({@code options.neteq_delay_offset_ms=-50}, same source),
 * the clamp into {@code [neteq_min_delay, neteq_max_delay=500]} (where {@code neteq_min_delay} is absent
 * from the live union and defaults to {@code 0}), and the {@code DelayPeakDetector}
 * ({@code options.neteq_enable_peak_detector=false}, same source, so the peak floor is inert in the
 * production configuration but kept for completeness). The reorder-optimization decay
 * ({@code reorderForgetFactor=0.9993}, {@code reorderStartForgetWeight=2.0}) is itself inert: the
 * {@code NeteqDelayManagerConfig} constructor (fn7362) pins {@code "use_reorder_optimizer:false"}
 * (data-segment-0002.bin, address {@code 0x75d11}); the two decay constants nonetheless match that
 * constructor's default-member initializers (see {@link NetEqConfig}).
 */
public final class DelayManager {
    /**
     * The number of inter-arrival-deviation buckets the histogram spans.
     *
     * <p>Each bucket is one packet duration wide, so the histogram covers deviations up to this many packet
     * durations; a deviation beyond it is clamped into the last bucket.
     */
    private static final int HISTOGRAM_BUCKETS = 100;

    /**
     * The configuration carrying the quantile, forgetting factor, offset, and delay bounds.
     */
    private final NetEqConfig config;

    /**
     * The inter-arrival-deviation histogram, one probability mass per packet-duration bucket.
     *
     * <p>The masses sum to approximately one after the first update; each update scales the whole array by
     * the forgetting factor and adds the complement into the observed bucket.
     */
    private final double[] histogram;

    /**
     * The peak-detector floor in packet durations, or {@code 0} when no peak is held.
     *
     * <p>Raised to a recent deviation peak when the peak detector is enabled and the peak recurs often
     * enough; decays back toward zero as peaks age out.
     */
    private int peakFloorPackets;

    /**
     * The arrival time of the previous packet in milliseconds, or {@code -1} before the first update.
     *
     * <p>The inter-arrival time is the difference between the current and this previous arrival.
     */
    private long previousArrivalMillis;

    /**
     * The most recently computed target level in milliseconds.
     *
     * <p>Recomputed on each update from the histogram quantile, the offset, the peak floor, and the
     * bounds; reported through {@link #targetLevelMillis()}.
     */
    private int targetLevelMillis;

    /**
     * Constructs a delay manager seeded to the configured initial minimum end-to-end delay.
     *
     * @param config the configuration carrying the estimator parameters and bounds; never {@code null}
     * @throws NullPointerException if {@code config} is {@code null}
     */
    public DelayManager(NetEqConfig config) {
        this.config = Objects.requireNonNull(config, "config cannot be null");
        this.histogram = new double[HISTOGRAM_BUCKETS];
        this.peakFloorPackets = 0;
        this.previousArrivalMillis = -1;
        this.targetLevelMillis = clampTarget(Math.max(config.initMinE2eDelayMs(), config.minDelayMs()));
    }

    /**
     * Updates the estimator with a packet's arrival and recomputes the target level.
     *
     * <p>The first call only seeds the previous-arrival time. A later call computes the inter-arrival
     * deviation in packet durations relative to one packet of spacing, folds it into the histogram with the
     * configured forgetting, optionally updates the peak floor, then recomputes the target level as the
     * underrun-quantile inverse cumulative distribution biased by the offset, lifted to the peak floor, and
     * clamped into the delay bounds.
     *
     * @param arrivalMillis the packet's local arrival time in milliseconds
     * @param packetMillis  the nominal duration of one packet in milliseconds, the histogram unit
     */
    public void update(long arrivalMillis, long packetMillis) {
        if (packetMillis <= 0) {
            return;
        }
        if (previousArrivalMillis < 0) {
            previousArrivalMillis = arrivalMillis;
            return;
        }
        var interArrival = arrivalMillis - previousArrivalMillis;
        previousArrivalMillis = arrivalMillis;
        var deviationPackets = (int) Math.round((double) interArrival / packetMillis) - 1;
        if (deviationPackets < 0) {
            deviationPackets = 0;
        }
        if (deviationPackets >= HISTOGRAM_BUCKETS) {
            deviationPackets = HISTOGRAM_BUCKETS - 1;
        }
        addToHistogram(deviationPackets);
        if (config.enablePeakDetector()) {
            updatePeakFloor(deviationPackets);
        }
        recomputeTarget(packetMillis);
    }

    /**
     * Returns the current target playout buffer level in milliseconds.
     *
     * @return the target level the decision logic compares the buffer span against
     */
    public int targetLevelMillis() {
        return targetLevelMillis;
    }

    /**
     * Returns the current peak-detector floor in packet durations.
     *
     * @return the peak floor, {@code 0} when no peak is held or the detector is disabled
     */
    public int peakFloorPackets() {
        return peakFloorPackets;
    }

    /**
     * Clears the histogram and peak detector and reseeds the target to its initial level.
     *
     * <p>Used when the stream is reconfigured so jitter measured before the discontinuity does not bias the
     * post-discontinuity target.
     */
    public void reset() {
        Arrays.fill(histogram, 0.0);
        peakFloorPackets = 0;
        previousArrivalMillis = -1;
        targetLevelMillis = clampTarget(Math.max(config.initMinE2eDelayMs(), config.minDelayMs()));
    }

    /**
     * Folds one observed deviation bucket into the histogram with exponential forgetting.
     *
     * <p>Scales every bucket by the forgetting factor, then adds the complement of the factor into the
     * observed bucket, so the masses continue to sum to approximately one while older observations decay.
     *
     * @param bucket the observed inter-arrival-deviation bucket, in {@code 0..}{@code HISTOGRAM_BUCKETS-1}
     */
    private void addToHistogram(int bucket) {
        var forget = config.underrunForgetFactor();
        for (var i = 0; i < histogram.length; i++) {
            histogram[i] *= forget;
        }
        histogram[bucket] += 1.0 - forget;
    }

    /**
     * Recomputes the target level from the histogram quantile, the offset, the peak floor, and the bounds.
     *
     * <p>Walks the histogram accumulating probability mass until the underrun quantile of the total mass is
     * reached; the bucket index at that point is the inverse cumulative distribution in packet durations,
     * normalized against the total so the quantile is meaningful before the masses have converged. That
     * index is converted to milliseconds, lifted to the peak floor, biased by the signed offset, and
     * clamped into the configured delay bounds.
     *
     * @param packetMillis the duration of one packet in milliseconds, to convert buckets to milliseconds
     */
    private void recomputeTarget(long packetMillis) {
        var totalMass = 0.0;
        for (var mass : histogram) {
            totalMass += mass;
        }
        if (totalMass <= 0.0) {
            targetLevelMillis = clampTarget(config.delayOffsetMs());
            return;
        }
        var target = config.underrunQuantile() * totalMass;
        var cumulative = 0.0;
        var quantileBucket = 0;
        for (var i = 0; i < histogram.length; i++) {
            cumulative += histogram[i];
            quantileBucket = i;
            if (cumulative >= target) {
                break;
            }
        }
        var levelPackets = Math.max(quantileBucket, peakFloorPackets);
        var levelMillis = (int) (levelPackets * packetMillis) + config.delayOffsetMs();
        targetLevelMillis = clampTarget(levelMillis);
    }

    /**
     * Raises or decays the peak floor from a newly observed deviation.
     *
     * <p>When the observed deviation exceeds the current floor it becomes the new floor; otherwise the
     * floor decays by one packet duration so a stale peak does not pin the target high indefinitely. This
     * is the simplified peak path; the live configuration disables the detector, so it is inert in
     * production.
     *
     * @param deviationPackets the observed inter-arrival-deviation bucket
     * @implNote This implementation is a one-pole decay floor rather than the full upstream
     * {@code DelayPeakDetector}, which is never exercised because the live {@code voip_settings} pins
     * {@code options.neteq_enable_peak_detector=false} (voip-settings-merged.json, decoded from
     * {@code <voip_settings uncompressed="1">} in stanzas-primary.jsonl). The only detector trigger knob the
     * server pushes is {@code options.neteq_min_peaks_to_trigger=3} (same source), and it applies solely when
     * the detector is enabled; the remaining trigger knobs ({@code peak_detection_threshold},
     * {@code max_peak_period_ms}, {@code effective_peak_period_fraction_perc}) and the peak-ring size
     * ({@code max_num_peaks}) are absent from the 759-key live voip_settings union. The full detector is
     * therefore unmodeled by design: it is dead in the production configuration.
     */
    private void updatePeakFloor(int deviationPackets) {
        if (deviationPackets > peakFloorPackets) {
            peakFloorPackets = deviationPackets;
        } else if (peakFloorPackets > 0) {
            peakFloorPackets--;
        }
    }

    /**
     * Clamps a candidate target level into the configured delay bounds.
     *
     * @param candidateMillis the candidate target level in milliseconds
     * @return the candidate confined to {@code [minDelayMs, maxDelayMs]}
     */
    private int clampTarget(int candidateMillis) {
        return Math.clamp(candidateMillis, config.minDelayMs(), config.maxDelayMs());
    }
}
