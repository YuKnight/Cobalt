package com.github.auties00.cobalt.calls2.net.bwe;

/**
 * Estimates the bottleneck link capacity from packet-pair dispersion and tracks a flip counter that
 * decides whether the link can sustain high-definition video.
 *
 * <p>A packet pair is two packets sent back to back; the time between their arrivals (the dispersion)
 * divided into their combined size gives an instantaneous link-capacity sample. {@link #onPacketPair(int,
 * long, long)} smooths these samples with an exponential moving average into the running link-capacity
 * estimate, ignoring samples whose dispersion is non-positive or whose arrival is older than the
 * configured window. The estimate crossing above {@link #hdHighThresholdBps} increments a flip counter
 * toward high-definition capability, and crossing below {@link #hdLowThresholdBps} decrements it; once
 * the counter reaches {@link #FLIP_COUNT_FOR_HD} the link is reported high-definition capable, giving
 * the capability hysteresis so a single sample does not toggle it.
 *
 * <p>Instances are not thread-safe; the owning sender estimator drives one estimator from the single
 * transport thread.
 *
 * @implNote This implementation ports the packet-pair link-capacity estimator ({@code bwe_pp_create} /
 * {@code bwe_pp_latest_link_capacity} in {@code bwe/bwe_webrtc_pp_local.cc}) and the HD-targeting
 * flip-count logic of {@code tfrc_update_link_capacity} (fn4375 in {@code bwe/tfrc_sender_bwe.cc}) from
 * the wa-voip engine; the captured {@code voip_settings} blob sets {@code delay_based_bwe_use_pp=1},
 * enabling this path. The high/low HD thresholds and the flip count are read from the voip parameters;
 * the captured blob does not carry those literals, so the thresholds are supplied by the caller and
 * the flip count uses the recovered hysteresis count (re/calls2-spec/SPEC.md sec 15.3).
 */
public final class PacketPairEstimator {
    /**
     * Smoothing factor for the exponential moving average over packet-pair capacity samples.
     */
    static final double LINK_CAPACITY_ALPHA = 0.1;

    /**
     * Flip-counter magnitude at which the link is reported high-definition capable.
     *
     * <p>The counter rises toward this on samples above the high threshold and falls toward its
     * negative on samples below the low threshold, giving the capability hysteresis.
     */
    static final int FLIP_COUNT_FOR_HD = 3;

    /**
     * Maximum age, in milliseconds, of a packet-pair sample relative to the window head before it is
     * ignored as stale.
     */
    static final long SAMPLE_WINDOW_MS = 5_000;

    /**
     * Link-capacity estimate, in bits per second, above which the flip counter rises toward
     * high-definition capability.
     */
    private final long hdHighThresholdBps;

    /**
     * Link-capacity estimate, in bits per second, below which the flip counter falls away from
     * high-definition capability.
     */
    private final long hdLowThresholdBps;

    /**
     * Running smoothed link-capacity estimate, in bits per second, or {@code 0} when not yet seeded.
     */
    private long linkCapacityBps = 0;

    /**
     * Arrival timestamp, in milliseconds, of the most recently accepted sample, or {@code -1} when
     * none.
     *
     * <p>Used to reject samples older than {@link #SAMPLE_WINDOW_MS} relative to the latest.
     */
    private long lastSampleMs = -1;

    /**
     * High-definition flip counter, clamped to {@code [-FLIP_COUNT_FOR_HD, FLIP_COUNT_FOR_HD]}.
     *
     * <p>Reaching {@link #FLIP_COUNT_FOR_HD} reports high-definition capability; reaching its negative
     * reports incapability.
     */
    private int flipCount = 0;

    /**
     * Constructs a packet-pair estimator with the high-definition capability thresholds.
     *
     * @param hdHighThresholdBps the estimate above which the flip counter rises, in bits per second
     * @param hdLowThresholdBps  the estimate below which the flip counter falls, in bits per second
     */
    public PacketPairEstimator(long hdHighThresholdBps, long hdLowThresholdBps) {
        this.hdHighThresholdBps = hdHighThresholdBps;
        this.hdLowThresholdBps = hdLowThresholdBps;
    }

    /**
     * Reports one packet pair and updates the link-capacity estimate and flip counter.
     *
     * <p>Ignores a pair whose dispersion is non-positive or whose arrival is older than
     * {@link #SAMPLE_WINDOW_MS} relative to the latest accepted sample. Otherwise computes the
     * instantaneous capacity as the combined size in bits divided by the dispersion, blends it into the
     * running estimate with {@link #LINK_CAPACITY_ALPHA}, then steps the flip counter up when the
     * estimate is above the high threshold and down when it is below the low threshold.
     *
     * @param combinedBytes the combined size of the two packets, in bytes
     * @param dispersionMs  the arrival-time gap between the two packets, in milliseconds; ignored when
     *                      non-positive
     * @param arrivalMs     the arrival timestamp of the second packet, in milliseconds
     */
    public void onPacketPair(int combinedBytes, long dispersionMs, long arrivalMs) {
        if (dispersionMs <= 0) {
            return;
        }
        if (lastSampleMs >= 0 && arrivalMs - lastSampleMs > SAMPLE_WINDOW_MS) {
            linkCapacityBps = 0;
            flipCount = 0;
        }
        lastSampleMs = arrivalMs;
        var instantBps = (long) combinedBytes * 8 * 1000 / dispersionMs;
        if (linkCapacityBps == 0) {
            linkCapacityBps = instantBps;
        } else {
            linkCapacityBps += (long) (LINK_CAPACITY_ALPHA * (instantBps - linkCapacityBps));
        }
        if (linkCapacityBps > hdHighThresholdBps) {
            flipCount = Math.min(FLIP_COUNT_FOR_HD, flipCount + 1);
        } else if (linkCapacityBps < hdLowThresholdBps) {
            flipCount = Math.max(-FLIP_COUNT_FOR_HD, flipCount - 1);
        }
    }

    /**
     * Returns the running link-capacity estimate.
     *
     * @return the link-capacity estimate, in bits per second, or {@code 0} when not yet seeded
     */
    public long linkCapacityBps() {
        return linkCapacityBps;
    }

    /**
     * Returns whether the link is currently reported high-definition capable.
     *
     * @return {@code true} once the flip counter has reached {@link #FLIP_COUNT_FOR_HD}
     */
    public boolean isHdCapable() {
        return flipCount >= FLIP_COUNT_FOR_HD;
    }

    /**
     * Returns the current high-definition flip counter.
     *
     * @return the flip counter, in {@code [-FLIP_COUNT_FOR_HD, FLIP_COUNT_FOR_HD]}
     */
    public int flipCount() {
        return flipCount;
    }

    /**
     * Resets the estimator, clearing the link-capacity estimate, the flip counter, and the sample
     * timestamp.
     */
    public void reset() {
        linkCapacityBps = 0;
        lastSampleMs = -1;
        flipCount = 0;
    }
}
