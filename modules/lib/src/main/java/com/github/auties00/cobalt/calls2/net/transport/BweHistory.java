package com.github.auties00.cobalt.calls2.net.transport;

/**
 * Holds a per-call ring of recent bandwidth-estimate samples used to seed a new call's estimate and to
 * decide whether the selective-forwarding-unit fast-probe mode should run.
 *
 * <p>Across a call the transport records the bandwidth estimate it observes; this ring keeps the most
 * recent {@value #CAPACITY} samples (in kilobits per second), overwriting the oldest once full. Two
 * consumers read it: a newly created transport seeds the peer initial bandwidth from
 * {@link #seedEstimateKbps()} so a reconnect does not restart from the conservative floor, and the
 * relay/SFU bring-up calls {@link #shouldActivateSfuFastProbeMode()} to decide whether to probe the
 * link aggressively, which it does when the recent history is empty or weak enough that the slow ramp
 * would be too cautious.
 *
 * <p>The ring is not thread-safe; the single call transport thread records and reads it.
 *
 * @implNote This implementation is a simplified bounded-ring model of {@code call_bwe_history.cc} from
 *           the wa-voip WASM module {@code ff-tScznZ8P}: a history of past bandwidth samples whose mean
 *           seeds a fresh estimate and whose recent level gates SFU fast-probe mode. Its
 *           {@link #FAST_PROBE_THRESHOLD_KBPS} and {@link #INSTANT_RAMP_UP_RATIO} are the recovered
 *           compiled voip-param defaults: {@code history_based_bwe_instant_ramp_up_threshold} stores
 *           {@code i32.const 500000} (500000 bps, 500 kbps) and
 *           {@code history_based_bwe_instant_ramp_up_ratio} stores {@code i32.const 0x3F800000}
 *           ({@code 1.0f}) in the param parser {@code fn12048}; the sibling
 *           {@code history_based_sfu_uplink_init_bwe_ratio} also stores {@code 0x3F800000} ({@code 1.0f}).
 *           The ramp-up-threshold and uplink-init-ratio keys are absent from the 759-key voip-settings
 *           union, so their compiled defaults govern, and the present
 *           {@code vid_rc.history_based_bwe_instant_ramp_up_ratio} equals {@code 1.0}.
 *           <p>The model still diverges from the engine in two ways that are not recoverable to byte
 *           accuracy from the static artifacts, documented rather than fabricated:
 *           <ul>
 *           <li>The native working history is not a fixed-capacity kilobits-per-second ring. The
 *           {@code should_activate_sfu_fast_pr_mode} probe ({@code fn11969}, snapshot {@code fn11753})
 *           walks a config-embedded array of up to 32 records of {@code 11128} bytes each (base config
 *           {@code +0x3190}, live count config {@code +0x16fc}, the gating float at record {@code +0x414}),
 *           comparing it against two per-instance float thresholds (config {@code +0x178c} versus instance
 *           {@code +0x5f688}, and the per-record value versus instance {@code +0x5f68c}). The writers of
 *           those two instance thresholds are not locatable in the captured instruction set (block-copied
 *           or import-initialised at call setup), so the float gate is approximated here by the recovered
 *           instant-ramp threshold and ratio over a kilobits-per-second ring.
 *           <li>Cross-call history is persisted by {@code wa_calling_history.cc} as a CRC'd JSON store
 *           ({@code wa_voip_history.json}) of multi-attribute records keyed by hashed self-plus-peer IP
 *           (attribute {@code history_based_bwe_video_tx_bitrate} and siblings, integer-valued, read via
 *           {@code wa_storage_get_records_by_attribute}, capped by {@code max_num_of_call_record} with
 *           timestamp FIFO eviction), not by this transient in-memory ring. Cobalt does not persist this
 *           ring across calls; see the {@code TODO} on {@link #CAPACITY}.
 *           </ul>
 */
public final class BweHistory {
    /**
     * The number of recent samples the ring retains.
     */
    // TODO: persist the history across calls and key it by endpoint, matching the engine. The native
    //  history is not an in-memory ring: wa_calling_history.cc persists past calls in a CRC'd JSON store
    //  (wa_voip_history.json) of multi-attribute records keyed by hashed self+peer IP, capped by the
    //  voip-param max_num_of_call_record with timestamp FIFO eviction, and the in-RAM fast-probe working
    //  set is a config-embedded array of up to 32 records (config +0x3190, count config +0x16fc). This
    //  fixed depth of 16 is a Cobalt model choice, not a WASM literal; the faithful design needs the
    //  wa_storage JSON persistence layer (a separate cluster) plus the config-array bound at config
    //  +0x16fc, which is set at config-build time and has no compiled-in default in the param parser.
    public static final int CAPACITY = 16;

    /**
     * The bandwidth level, in kilobits per second, at or below which fast-probe mode is worthwhile.
     *
     * <p>When the most recent sample is missing or no greater than this level the slow additive ramp
     * would take too long to reach a usable rate, so the SFU is asked to fast-probe instead.
     *
     * @implNote This implementation uses {@code 500}, the recovered compiled default of the voip param
     * {@code history_based_bwe_instant_ramp_up_threshold} ({@code i32.const 500000} bits per second,
     * stored at the param-struct field {@code +0xFE0} by the parser {@code fn12048}), expressed in
     * kilobits per second. The param is absent from the 759-key voip-settings union, so the compiled
     * default governs. In the engine this threshold gates the history-based instant-ramp-up in
     * {@code update_sender_bwe}: the ramp fires when the history-based estimate scaled by
     * {@link #INSTANT_RAMP_UP_RATIO} exceeds it; this ring uses it as the fast-probe floor.
     */
    public static final int FAST_PROBE_THRESHOLD_KBPS = 500;

    /**
     * The ratio the history-based estimate is scaled by when it seeds a fresh call's bandwidth.
     *
     * @implNote This implementation uses {@code 1.0}, the recovered compiled default of the voip param
     * {@code history_based_bwe_instant_ramp_up_ratio} ({@code i32.const 0x3F800000}, the {@code 1.0f}
     * bit pattern, stored at the param-struct field {@code +0xFDC} by the parser {@code fn12048}). The
     * present {@code vid_rc.history_based_bwe_instant_ramp_up_ratio} in the 759-key voip-settings union
     * equals {@code 1.0}, matching the compiled default; the sibling
     * {@code history_based_sfu_uplink_init_bwe_ratio} compiles to the same {@code 1.0f} and is absent
     * from the union. A ratio of {@code 1.0} leaves the history-based estimate unscaled when it seeds a
     * new call.
     */
    public static final double INSTANT_RAMP_UP_RATIO = 1.0;

    /**
     * Holds the recent samples in a circular buffer, in kilobits per second.
     *
     * <p>Indices in {@code [0, count)} after wrap are addressed modulo {@value #CAPACITY} from
     * {@code head}; before wrap the live samples are {@code [0, count)} directly.
     */
    private final int[] samples;

    /**
     * Holds the index at which the next sample is written, modulo {@value #CAPACITY}.
     */
    private int head;

    /**
     * Holds the number of live samples, saturating at {@value #CAPACITY}.
     */
    private int count;

    /**
     * Constructs an empty history with no recorded samples.
     */
    public BweHistory() {
        this.samples = new int[CAPACITY];
        this.head = 0;
        this.count = 0;
    }

    /**
     * Records one bandwidth-estimate sample, overwriting the oldest when the ring is full.
     *
     * <p>A non-positive sample is ignored, treated as a missing measurement rather than a data point.
     *
     * @param estimateKbps the observed bandwidth estimate in kilobits per second; ignored when not
     *                     strictly positive
     */
    public void record(int estimateKbps) {
        if (estimateKbps <= 0) {
            return;
        }
        samples[head] = estimateKbps;
        head = (head + 1) % CAPACITY;
        if (count < CAPACITY) {
            count++;
        }
    }

    /**
     * Returns the bandwidth estimate, in kilobits per second, to seed a fresh transport with.
     *
     * <p>This is the arithmetic mean of the retained samples scaled by {@link #INSTANT_RAMP_UP_RATIO},
     * which smooths a single outlier while still reflecting the call's recent capacity. It is {@code 0}
     * when no sample has been recorded, in which case the transport falls back to its own conservative
     * initial estimate.
     *
     * @implNote This implementation scales the history mean by {@link #INSTANT_RAMP_UP_RATIO}, the
     * recovered {@code history_based_bwe_instant_ramp_up_ratio} default ({@code 1.0f}), matching the
     * engine's history-based instant-ramp-up which sets the new sender bandwidth to the history-based
     * estimate times that ratio. At the compiled default of {@code 1.0} the mean is seeded unscaled.
     *
     * @return the ratio-scaled mean of the retained samples, or {@code 0} when the history is empty
     */
    public int seedEstimateKbps() {
        if (count == 0) {
            return 0;
        }
        var total = 0L;
        for (var index = 0; index < count; index++) {
            total += samples[index];
        }
        return (int) (total / count * INSTANT_RAMP_UP_RATIO);
    }

    /**
     * Returns the most recently recorded sample, in kilobits per second.
     *
     * @return the last recorded sample, or {@code 0} when the history is empty
     */
    public int mostRecentKbps() {
        if (count == 0) {
            return 0;
        }
        var lastIndex = (head - 1 + CAPACITY) % CAPACITY;
        return samples[lastIndex];
    }

    /**
     * Returns whether the selective-forwarding-unit fast-probe mode should run for this call.
     *
     * <p>Fast probing is activated when the history is empty (a first call, with no capacity to seed
     * from) or when the most recent sample is at or below {@value #FAST_PROBE_THRESHOLD_KBPS}, where the
     * slow additive ramp would be too cautious to reach a usable rate promptly.
     *
     * @return {@code true} when the SFU should fast-probe rather than slow-ramp the estimate
     */
    // TODO: the native gate should_activate_sfu_fast_pr_mode (fn11969, snapshot fn11753) does NOT test a
    //  single kbps ring against one threshold; it walks a config-embedded array of up to 32 records (each
    //  11128 bytes, the gating float at record +0x414) and compares it against two per-instance float
    //  thresholds (config +0x178c versus instance +0x5f688, and the per-record value versus instance
    //  +0x5f68c). The writers of those two instance floats are not in the captured instruction set
    //  (block-copied or import-initialised at call setup), so the record-walk comparison and the two
    //  thresholds cannot be reproduced faithfully without a live memory dump of a connected call reading
    //  instance +0x5f688 / +0x5f68c and a record's +0x414. This empty-OR-weak-history gate stands in for
    //  that walk; it is the closest recoverable behaviour and is not byte-faithful.
    public boolean shouldActivateSfuFastProbeMode() {
        if (count == 0) {
            return true;
        }
        return mostRecentKbps() <= FAST_PROBE_THRESHOLD_KBPS;
    }

    /**
     * Returns the number of live samples in the history.
     *
     * @return the sample count, in {@code [0, }{@value #CAPACITY}{@code ]}
     */
    public int size() {
        return count;
    }

    /**
     * Removes all recorded samples, returning the history to its empty state.
     */
    public void clear() {
        head = 0;
        count = 0;
    }
}
