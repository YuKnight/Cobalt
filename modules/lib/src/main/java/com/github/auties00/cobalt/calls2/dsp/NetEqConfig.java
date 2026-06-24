package com.github.auties00.cobalt.calls2.dsp;

/**
 * Holds the tuning knobs that govern the {@link LiveNetEq} adaptive jitter buffer, the WhatsApp
 * {@code neteq_field_trial_params} blended with the {@code mvp->jb} wiring fields.
 *
 * <p>The configuration is immutable and is installed once when a call's audio receive path opens its
 * jitter buffer. It collects four groups of knobs. The playout-delay bounds and offsets
 * ({@link #minDelayMs()}, {@link #maxDelayMs()}, {@link #delayOffsetMs()}, {@link #targetDelayMs()},
 * {@link #initMinE2eDelayMs()}) clamp and bias the target buffer level the {@link DelayManager}
 * estimates. The histogram and estimator parameters ({@link #dmHistorySizeMs()},
 * {@link #dlHistorySizeMs()}, {@link #maxHistoryMs()}, {@link #underrunQuantile()},
 * {@link #underrunForgetFactor()}, {@link #reorderForgetFactor()}, {@link #reorderStartForgetWeight()})
 * drive the inter-arrival-time histogram and its forgetting. The peak-detector and buffer-management
 * flags ({@link #enablePeakDetector()}, {@link #smartBufferFlushEnabled()},
 * {@link #bufferFlushMaxLengthMs()}, {@link #maxPacketsInBuffer()}, {@link #use20msGetPeriod()},
 * {@link #numInitialPackets()}) shape the decision logic. The accelerate and preemptive-expand decision
 * limits ({@link #audioJitbufBufferLowerLimitScalePercent()},
 * {@link #audioJitbufBufferLimitsWindowSizeMs()}, {@link #highThresholdOffsetMs()},
 * {@link #useMaxDelayInHighThreshold()}, {@link #allowTimeStretchAcceleration()},
 * {@link #allowTimeStretchForHighLatency()}, {@link #allowTimeStretchThresholdMs()}) set the low and high
 * buffer-level thresholds the {@link DecisionLogic} compares the buffered span against and gate whether the
 * buffer is time-compressed or time-stretched at all. The NACK and concealment behaviour
 * ({@link #enableCodecPlc()}, {@link #preexpandWithFilteredLevelPerc()}, {@link #skipNackWithFec()},
 * {@link #ladEnabledForNack()}, {@link #ladEnabledForFec()}, {@link #ladNackExtraInsertTimeMs()},
 * {@link #nackRttLimitMs()}, {@link #maxNackListSize()}, {@link #audioNackMaxSeqReq()},
 * {@link #enableSpeakerStatus()}) wires the {@link NackTracker} and the loss-recovery path.
 *
 * <p>Obtain the production configuration from {@link #defaults()}, which carries the live
 * {@code voip_settings} value for every key the server pushes and the compiled-in upstream default for the
 * keys absent from the live capture.
 *
 * @param minDelayMs                     the lower bound on the target playout delay in milliseconds
 * @param maxDelayMs                     the upper bound on the target playout delay in milliseconds
 * @param delayOffsetMs                  a signed bias added to the estimated target playout delay
 * @param targetDelayMs                  an explicit target playout delay override, or {@code 0} for none
 * @param initMinE2eDelayMs              the initial minimum end-to-end delay seeded before estimation
 * @param dmHistorySizeMs               the {@link DelayManager} inter-arrival histogram window
 * @param dlHistorySizeMs               the {@link DecisionLogic} buffer-level history window
 * @param maxHistoryMs                   the overall cap on histogram history retained
 * @param underrunQuantile               the histogram quantile the target level is read at
 * @param underrunForgetFactor           the exponential-forgetting factor of the underrun estimator
 * @param reorderForgetFactor            the forgetting factor applied to reordered-arrival deviations
 * @param reorderStartForgetWeight       the initial weight the reorder-optimization forgetting ramps from
 * @param enablePeakDetector             whether the inter-arrival peak detector raises the delay floor
 * @param smartBufferFlushEnabled        whether gross over-buffering is drained by a smart flush
 * @param bufferFlushMaxLengthMs         the buffer length above which a flush is forced
 * @param maxPacketsInBuffer             the {@link PacketBuffer} capacity in packets
 * @param use20msGetPeriod               whether the get period is 20 ms rather than 10 ms
 * @param numInitialPackets              the warm-up packet count before normal decisions begin
 * @param audioJitbufBufferLowerLimitScalePercent the percentage of the target level the low decision limit
 *                                       sits at, below which the buffer is preemptively expanded
 * @param audioJitbufBufferLimitsWindowSizeMs the window in milliseconds added above the low limit to form
 *                                       the high decision limit, above which the buffer is accelerated
 * @param highThresholdOffsetMs          a flat millisecond offset added to the high decision limit
 * @param useMaxDelayInHighThreshold     whether the maximum delay bound also floors the high decision limit
 * @param allowTimeStretchAcceleration   whether accelerate (time-compression) is permitted at all
 * @param allowTimeStretchForHighLatency whether time-stretch is gated on the buffer exceeding the
 *                                       high-latency threshold
 * @param allowTimeStretchThresholdMs    the buffered span in milliseconds above which time-stretch is
 *                                       permitted when gated by {@link #allowTimeStretchForHighLatency()}
 * @param enableCodecPlc                 whether codec-internal loss concealment is preferred over expand
 * @param preexpandWithFilteredLevelPerc the smoothed-level percentage used before an expansion
 * @param skipNackWithFec                whether a packet a FEC copy will recover is excluded from NACK
 * @param ladEnabledForNack              whether lost-audio-detection extends NACK timing
 * @param ladEnabledForFec               whether lost-audio-detection extends FEC timing
 * @param ladNackExtraInsertTimeMs       the extra insert time lost-audio-detection grants a NACK
 * @param nackRttLimitMs                 the round-trip-time ceiling above which NACK is suppressed
 * @param maxNackListSize                the hard cap on the {@link NackTracker} list length
 * @param audioNackMaxSeqReq             the maximum sequence-number span a single NACK request covers
 * @param enableSpeakerStatus            whether speaker-status change events are emitted from the buffer
 * @implNote This implementation carries the {@code mvp->jb.neteq_field_trial_params} struct documented in
 * {@code rev-rtc-dsp} for the wa-voip WASM module {@code ff-tScznZ8P}. The defaults in {@link #defaults()}
 * are taken from the live server-pushed {@code voip_settings}, decoded from the
 * {@code <voip_settings uncompressed="1">} blobs in stanzas-primary.jsonl (68 blobs unioned to 759 leaf keys
 * in re/calls2-spec/captures/voip-settings-merged.json, rev 1041451627), {@code options.*} keys:
 * {@code neteq_init_min_e2e_delay_ms=280}, {@code neteq_delay_offset_ms=-50},
 * {@code neteq_dm_history_size_ms=500}, {@code neteq_dl_history_size_ms=500},
 * {@code neteq_underrun_quantile=0.98}, {@code neteq_underrun_forget_factor=0.99},
 * {@code neteq_enable_peak_detector=false}, {@code neteq_smart_buffer_flush_enabled=true},
 * {@code buffer_flush_max_length_ms=1000}, {@code neteq_enable_codec_plc=true}, {@code neteq_max_delay=500},
 * {@code neteq_use_20ms_get_period=true}, {@code neteq_preexpand_with_filtered_level_perc=50},
 * {@code neteq_skip_nack_with_fec=false}, {@code neteq_lad_enabled_for_nack/fec=true},
 * {@code neteq_lad_nack_extra_insert_time_ms=20}, {@code neteq_nack_rtt_limit_ms=500},
 * {@code neteq_enable_speaker_status=true}, {@code audio_nack_max_seq_req=10}. The accelerate and
 * preemptive-expand decision limits are taken from the {@code NeteqDecisionLogicConfig} constructor (fn7394),
 * which registers the {@code mvp->jb.neteq_field_trial_params.audio_jitbuf_*} field-trial keys with these
 * compiled-in default members: {@code audio_jitbuf_buffer_lower_limit_scale_percent=75} (the low limit is
 * 75% of the target), {@code audio_jitbuf_buffer_limits_window_size_ms=20} (the high limit is the target
 * plus this one-packet window), {@code high_threshold_offset_ms=20} (a flat offset added to the high limit),
 * and {@code use_max_delay_in_high_threshold=false} (data-segment-0002.bin {@code use_max_delay_in_high_threshold:false}).
 * None of these keys appear in the 759-key live voip_settings union, so the server does not push them and
 * the compiled-in members above are operative. The time-stretch gates, by contrast, are server-pushed: the
 * live voip_settings carries {@code options.neteq_allow_time_stretch_acceleration=false} (accelerate is
 * suppressed in the production configuration), {@code options.neteq_allow_time_stretch_for_high_latency=true},
 * and {@code options.neteq_allow_time_stretch_threshold_ms=40} (time-stretch is permitted only above a 40 ms
 * buffered span). The companion smart-flush
 * tuning {@code neteq_smart_buffer_flush_multiplier=1000} and {@code neteq_smart_buffer_flush_target_ms=500}
 * is present in the union but has no dedicated field on this record. The {@code kNackListSizeLimitLocal=500}
 * cap is asserted by {@code concerto::NackTracker::SetMaxNackListSize} (fn7318). Three further fields are
 * pinned directly against the compiled-in WASM rather than the server blob: {@code maxPacketsInBuffer=200}
 * is the interned default-config literal {@code "max_packets_in_buffer:200"}
 * (data-segment-0002.bin {@code +0xc8690}, address {@code 0xcab5f}); and the reorder-optimization decay
 * ({@code reorderForgetFactor=0.9993}, {@code reorderStartForgetWeight=2.0}) matches the default-member
 * initializers stored by the {@code NeteqDelayManagerConfig} constructor fn7362 (the doubles
 * {@code 0x3feffa43fe5c91d1} and {@code 0x4000000000000000} stored at struct offsets {@code 0x08}/{@code 0x20}
 * and {@code 0x10}/{@code 0x28}); the reorder path is itself inert because the same constructor pins
 * {@code "use_reorder_optimizer:false"} (data-segment-0002.bin, address {@code 0x75d11}). Four fields are
 * absent from the 759-key live voip_settings union, so the server does not push them and they keep their
 * compiled-in upstream default: {@link #minDelayMs()} ({@code 0}), {@link #targetDelayMs()} ({@code 0}),
 * {@link #maxHistoryMs()} ({@code 2000}), and {@link #numInitialPackets()} ({@code 5}). They tune playout
 * quality, not interoperability.
 */
public record NetEqConfig(
        int minDelayMs,
        int maxDelayMs,
        int delayOffsetMs,
        int targetDelayMs,
        int initMinE2eDelayMs,
        int dmHistorySizeMs,
        int dlHistorySizeMs,
        int maxHistoryMs,
        double underrunQuantile,
        double underrunForgetFactor,
        double reorderForgetFactor,
        double reorderStartForgetWeight,
        boolean enablePeakDetector,
        boolean smartBufferFlushEnabled,
        int bufferFlushMaxLengthMs,
        int maxPacketsInBuffer,
        boolean use20msGetPeriod,
        int numInitialPackets,
        int audioJitbufBufferLowerLimitScalePercent,
        int audioJitbufBufferLimitsWindowSizeMs,
        int highThresholdOffsetMs,
        boolean useMaxDelayInHighThreshold,
        boolean allowTimeStretchAcceleration,
        boolean allowTimeStretchForHighLatency,
        int allowTimeStretchThresholdMs,
        boolean enableCodecPlc,
        int preexpandWithFilteredLevelPerc,
        boolean skipNackWithFec,
        boolean ladEnabledForNack,
        boolean ladEnabledForFec,
        int ladNackExtraInsertTimeMs,
        int nackRttLimitMs,
        int maxNackListSize,
        int audioNackMaxSeqReq,
        boolean enableSpeakerStatus
) {
    /**
     * The hard cap on the NACK list the native engine asserts on, {@code kNackListSizeLimitLocal}.
     *
     * <p>{@code concerto::NackTracker::SetMaxNackListSize} (fn7318) asserts the requested size is greater
     * than zero and no larger than this value.
     */
    public static final int NACK_LIST_SIZE_LIMIT = 500;

    /**
     * The duration of one packet in milliseconds at the 20 ms get period, the unit the histogram counts
     * inter-arrival deviations in.
     *
     * <p>The captured configuration sets {@code neteq_use_20ms_get_period=true}, so the buffer level and
     * the target level are denominated in 20 ms packet units.
     */
    public static final int PACKET_MILLIS = 20;

    /**
     * Validates the bounds and quantile, rejecting an inverted delay range or an out-of-range quantile.
     *
     * @throws IllegalArgumentException if {@code minDelayMs} is negative, if {@code maxDelayMs} is below
     *                                  {@code minDelayMs}, if {@code underrunQuantile} is outside the open
     *                                  interval {@code (0, 1)}, if {@code maxPacketsInBuffer} is not
     *                                  positive, if {@code audioJitbufBufferLowerLimitScalePercent} is not
     *                                  positive, or if {@code maxNackListSize} is outside
     *                                  {@code 1..}{@link #NACK_LIST_SIZE_LIMIT}
     */
    public NetEqConfig {
        if (minDelayMs < 0) {
            throw new IllegalArgumentException("minDelayMs must be non-negative, got " + minDelayMs);
        }
        if (maxDelayMs < minDelayMs) {
            throw new IllegalArgumentException("maxDelayMs " + maxDelayMs + " is below minDelayMs " + minDelayMs);
        }
        if (underrunQuantile <= 0.0 || underrunQuantile >= 1.0) {
            throw new IllegalArgumentException("underrunQuantile must be in (0, 1), got " + underrunQuantile);
        }
        if (maxPacketsInBuffer <= 0) {
            throw new IllegalArgumentException("maxPacketsInBuffer must be positive, got " + maxPacketsInBuffer);
        }
        if (audioJitbufBufferLowerLimitScalePercent <= 0) {
            throw new IllegalArgumentException("audioJitbufBufferLowerLimitScalePercent must be positive, got "
                    + audioJitbufBufferLowerLimitScalePercent);
        }
        if (maxNackListSize < 1 || maxNackListSize > NACK_LIST_SIZE_LIMIT) {
            throw new IllegalArgumentException("maxNackListSize must be in 1.." + NACK_LIST_SIZE_LIMIT
                    + ", got " + maxNackListSize);
        }
    }

    /**
     * Returns the production configuration seeded from the captured {@code voip_settings} values.
     *
     * <p>The fields present in the live {@code voip_settings} union take the server value, the WASM-pinned
     * fields take the compiled-in default (see the class {@code @implNote}), and the fields absent from the
     * union take their compiled-in upstream default because the server does not push them. The result
     * enables the codec packet-loss-concealment path, disables the peak detector, runs the 20 ms get period,
     * and caps the NACK list at the native {@link #NACK_LIST_SIZE_LIMIT}.
     *
     * @return the default jitter-buffer configuration
     */
    public static NetEqConfig defaults() {
        return new NetEqConfig(
                // neteq_min_delay: absent from the 759-key live voip_settings union, so the server does not
                //  push it and the upstream "no forced floor" default of 0 is the operative value.
                0,
                500,   // neteq_max_delay (voip-settings-merged.json options.neteq_max_delay=500)
                -50,   // neteq_delay_offset_ms (voip-settings-merged.json options.neteq_delay_offset_ms=-50)
                // target_delay_ms: absent from the 759-key live voip_settings union, so the server does not
                //  push it and the upstream default 0 ("estimate, do not override") is the operative value.
                0,
                // init_min_e2e_delay_ms: voip-settings-merged.json options.neteq_init_min_e2e_delay_ms=280
                //  (decoded from <voip_settings uncompressed=1> in stanzas-primary.jsonl).
                280,
                500,   // neteq_dm_history_size_ms (voip-settings-merged.json options.neteq_dm_history_size_ms=500)
                500,   // neteq_dl_history_size_ms (voip-settings-merged.json options.neteq_dl_history_size_ms=500)
                // max_history_ms: absent from the 759-key live voip_settings union, so the server does not
                //  push it and the upstream NetEq histogram cap default of 2000 is the operative value.
                2000,
                0.98,  // neteq_underrun_quantile (voip-settings-merged.json options.neteq_underrun_quantile=0.98)
                0.99,  // neteq_underrun_forget_factor (voip-settings-merged.json options.neteq_underrun_forget_factor=0.99)
                0.9993, // reorder_forget_factor: NeteqDelayManagerConfig ctor default-member 0.9993
                        // (fn7362 i64.const 0x3feffa43fe5c91d1 stored at struct off 0x08/0x20); inert
                        // because the same ctor pins "use_reorder_optimizer:false" (data-segment-0002.bin
                        // address 0x75d11)
                2.0,    // reorder_start_forget_weight: NeteqDelayManagerConfig ctor default-member 2.0
                        // (fn7362 i64.const 0x4000000000000000 stored at struct off 0x10/0x28); inert (see
                        // use_reorder_optimizer:false above)
                false, // neteq_enable_peak_detector (voip-settings-merged.json options.neteq_enable_peak_detector=false)
                // smart_buffer_flush_enabled: voip-settings-merged.json options.neteq_smart_buffer_flush_enabled=true
                //  (decoded from <voip_settings uncompressed=1> in stanzas-primary.jsonl). The companion
                //  options.neteq_smart_buffer_flush_multiplier=1000 and
                //  options.neteq_smart_buffer_flush_target_ms=500 have no dedicated fields on this record.
                true,
                // buffer_flush_max_length_ms: voip-settings-merged.json options.buffer_flush_max_length_ms=1000
                //  (decoded from <voip_settings uncompressed=1> in stanzas-primary.jsonl).
                1000,
                200,   // max_packets_in_buffer: interned default-config literal "max_packets_in_buffer:200"
                       // (data-segment-0002.bin +0xc8690, address 0xcab5f); also the upstream NetEq default
                true,  // neteq_use_20ms_get_period (voip-settings-merged.json options.neteq_use_20ms_get_period=true)
                // num_initial_packets: absent from the 759-key live voip_settings union, so the server does
                //  not push it and the upstream NetEq default of 5 warm-up packets is the operative value.
                5,
                75,    // audio_jitbuf_buffer_lower_limit_scale_percent: NeteqDecisionLogicConfig ctor default
                       // member (fn7394 param1[0xc]=0x4b); absent from the live voip_settings union
                20,    // audio_jitbuf_buffer_limits_window_size_ms: NeteqDecisionLogicConfig ctor default
                       // member (fn7394 param1[0x15]=0x14); absent from the live voip_settings union
                20,    // high_threshold_offset_ms: NeteqDecisionLogicConfig ctor default member
                       // (fn7394 param1[0x2e]=0x14); absent from the live voip_settings union
                false, // use_max_delay_in_high_threshold: data-segment-0002.bin
                       // "use_max_delay_in_high_threshold:false"; absent from the live voip_settings union
                false, // neteq_allow_time_stretch_acceleration (voip-settings-merged.json
                       // options.neteq_allow_time_stretch_acceleration=false)
                true,  // neteq_allow_time_stretch_for_high_latency (voip-settings-merged.json
                       // options.neteq_allow_time_stretch_for_high_latency=true)
                40,    // neteq_allow_time_stretch_threshold_ms (voip-settings-merged.json
                       // options.neteq_allow_time_stretch_threshold_ms=40)
                true,  // neteq_enable_codec_plc (voip-settings-merged.json options.neteq_enable_codec_plc=true)
                50,    // neteq_preexpand_with_filtered_level_perc (voip-settings-merged.json options.neteq_preexpand_with_filtered_level_perc=50)
                false, // neteq_skip_nack_with_fec (voip-settings-merged.json options.neteq_skip_nack_with_fec=false)
                true,  // neteq_lad_enabled_for_nack (voip-settings-merged.json options.neteq_lad_enabled_for_nack=true)
                true,  // neteq_lad_enabled_for_fec (voip-settings-merged.json options.neteq_lad_enabled_for_fec=true)
                20,    // neteq_lad_nack_extra_insert_time_ms (voip-settings-merged.json options.neteq_lad_nack_extra_insert_time_ms=20)
                500,   // neteq_nack_rtt_limit_ms (voip-settings-merged.json options.neteq_nack_rtt_limit_ms=500)
                NACK_LIST_SIZE_LIMIT, // kNackListSizeLimitLocal (fn7318 assert bound)
                10,    // audio_nack_max_seq_req (voip-settings-merged.json options.audio_nack_max_seq_req=10)
                true   // neteq_enable_speaker_status (voip-settings-merged.json options.neteq_enable_speaker_status=true)
        );
    }

    /**
     * Returns the get period in milliseconds, either 20 ms or 10 ms.
     *
     * <p>Derived from {@link #use20msGetPeriod()}; one playback pull renders one frame of this duration.
     *
     * @return the get period, {@code 20} when {@link #use20msGetPeriod()} is set, else {@code 10}
     */
    public int getPeriodMs() {
        return use20msGetPeriod ? 20 : 10;
    }
}
