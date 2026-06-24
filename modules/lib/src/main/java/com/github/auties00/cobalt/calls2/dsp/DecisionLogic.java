package com.github.auties00.cobalt.calls2.dsp;

import java.util.Objects;

/**
 * Chooses the {@link NetEqOperation} for each playback pull from the buffer level against the target, the
 * availability of the next packet, and the previous operation.
 *
 * <p>On every get period {@link #decide(Input)} maps the current state onto exactly one operation. When the
 * next packet continues the sequence, the choice depends on how the buffered span sits against two limits
 * derived from the {@link DelayManager} target: a low limit at
 * {@link NetEqConfig#audioJitbufBufferLowerLimitScalePercent()} of the target, and a high limit one
 * {@link NetEqConfig#audioJitbufBufferLimitsWindowSizeMs()} packet window above the target plus the flat
 * {@link NetEqConfig#highThresholdOffsetMs()} offset. A span at or above the high limit time-compresses the
 * audio ({@link NetEqOperation#ACCELERATE}, or {@link NetEqOperation#FAST_ACCELERATE} when grossly
 * over-full); a span below the low limit time-stretches it ({@link NetEqOperation#PREEMPTIVE_EXPAND}); a
 * span between the limits decodes normally ({@link NetEqOperation#NORMAL}); and the first decode after a
 * concealment run cross-fades the new packet in ({@link NetEqOperation#MERGE}). Time-stretch is itself gated:
 * accelerate is taken only when {@link NetEqConfig#allowTimeStretchAcceleration()} is set, and both
 * accelerate and preemptive expand are suppressed unless the buffered span exceeds
 * {@link NetEqConfig#allowTimeStretchThresholdMs()} when {@link NetEqConfig#allowTimeStretchForHighLatency()}
 * is set. When the next packet is missing the choice is concealment: {@link NetEqOperation#CODEC_PLC} if the
 * codec exposes loss concealment and the configuration prefers it, otherwise {@link NetEqOperation#EXPAND};
 * if the buffer is entirely empty and a comfort-noise gap is in effect the choice is
 * {@link NetEqOperation#RFC3389_CNG}.
 *
 * <p>The logic holds no state of its own beyond a warm-up counter: it forces {@link NetEqOperation#NORMAL}
 * for the first {@link NetEqConfig#numInitialPackets()} decodes so the buffer fills before adaptive
 * time-stretching begins. Instances are not thread-safe; the pull path drives one decision logic from a
 * single thread.
 *
 * @implNote This implementation ports the {@code concerto::DecisionLogic} of the wa-voip WASM module
 * {@code ff-tScznZ8P} ({@code decision_logic.cc}, inlined into {@code concerto::NetEqImpl::GetAudioInternal}
 * fn7521, configured by the {@code NeteqDecisionLogicConfig} constructor fn7394): the buffer-level versus
 * target comparison selecting Normal/Accelerate/PreemptiveExpand/Expand each get period, the codec PLC
 * preference ({@code neteq_enable_codec_plc=true} captured), and the warm-up ({@code num_initial_packets}).
 * The low and high decision limits are the recovered native ones: the low limit is
 * {@code audio_jitbuf_buffer_lower_limit_scale_percent} (75) of the target, and the high limit is the larger
 * of the target and the low limit plus {@code audio_jitbuf_buffer_limits_window_size_ms} (20), then offset by
 * {@code high_threshold_offset_ms} (20), with {@code use_max_delay_in_high_threshold} false (fn7394 default
 * members). The time-stretch gates are the server-pushed ones: accelerate requires
 * {@code neteq_allow_time_stretch_acceleration} (captured false, so accelerate is suppressed in production),
 * and both accelerate and preemptive expand are suppressed below
 * {@code neteq_allow_time_stretch_threshold_ms} (40) when {@code neteq_allow_time_stretch_for_high_latency}
 * (captured true) is set. The {@link NetEqOperation#FAST_ACCELERATE} sub-gate uses the upstream WebRTC NetEq
 * rule (a span at least twice the high limit) because the exact fast-accelerate constant is inlined and
 * obfuscated in fn7521; with the captured configuration both accelerate variants are gated off, so this
 * sub-gate is inert in production. The native "Updated Playout Delay ms" logging path (fn7440), the
 * stable-playout deceleration offset ({@code deceleration_target_level_offset_ms}, applied on top of the
 * accelerate path which is suppressed here), and the group-decision-logic variant for bot/AI calls
 * ({@code enable_group_decision_logic_for_bot_calls}) are not modeled; the steady-state per-pull decision is
 * reproduced.
 */
public final class DecisionLogic {
    /**
     * The multiple of the high decision limit at or above which the aggressive fast-accelerate is chosen.
     *
     * <p>A buffered span at least this multiple of the high limit is grossly over-full and warrants
     * compressing more than one period per frame; the upstream WebRTC NetEq fast-accelerate rule, applied
     * only when accelerate itself is permitted.
     */
    private static final int FAST_ACCELERATE_HIGH_LIMIT_MULTIPLE = 2;

    /**
     * The denominator the lower-limit scale percentage is taken over.
     */
    private static final int PERCENT = 100;

    /**
     * The configuration carrying the warm-up count, the decision limits, and the codec-PLC preference.
     */
    private final NetEqConfig config;

    /**
     * The number of normal decodes performed so far, capped at the warm-up count.
     *
     * <p>While below {@link NetEqConfig#numInitialPackets()} the logic forces {@link NetEqOperation#NORMAL}
     * so the buffer fills before adaptive time-stretching begins.
     */
    private int decodesPerformed;

    /**
     * Constructs decision logic governed by the given configuration.
     *
     * @param config the configuration carrying the warm-up count and codec-PLC preference; never
     *               {@code null}
     * @throws NullPointerException if {@code config} is {@code null}
     */
    public DecisionLogic(NetEqConfig config) {
        this.config = Objects.requireNonNull(config, "config cannot be null");
        this.decodesPerformed = 0;
    }

    /**
     * The state the decision depends on for one get period.
     *
     * <p>{@link #bufferSpanMillis()} is the buffered playout duration; {@link #targetLevelMillis()} is the
     * {@link DelayManager} target; {@link #nextPacketAvailable()} reports whether any packet is buffered;
     * {@link #nextPacketContiguous()} reports whether the next buffered packet continues the sequence
     * without a gap; {@link #comfortNoiseActive()} reports whether a discontinuous-transmission
     * comfort-noise gap is in effect; {@link #codecHasPlc()} reports whether the active codec exposes
     * loss concealment; and {@link #lastOperation()} is the operation chosen on the previous pull, which
     * distinguishes a fresh decode from the merge that follows a concealment run.
     *
     * @param bufferSpanMillis     the buffered playout duration in milliseconds
     * @param targetLevelMillis    the target playout level in milliseconds
     * @param nextPacketAvailable  whether any packet is buffered
     * @param nextPacketContiguous whether the next buffered packet continues the sequence without a gap
     * @param comfortNoiseActive   whether a comfort-noise gap is in effect
     * @param codecHasPlc          whether the codec exposes its own packet-loss concealment
     * @param lastOperation        the operation chosen on the previous pull; never {@code null}
     */
    public record Input(
            int bufferSpanMillis,
            int targetLevelMillis,
            boolean nextPacketAvailable,
            boolean nextPacketContiguous,
            boolean comfortNoiseActive,
            boolean codecHasPlc,
            NetEqOperation lastOperation
    ) {
        /**
         * Validates the input, rejecting a {@code null} previous operation.
         *
         * @throws NullPointerException if {@code lastOperation} is {@code null}
         */
        public Input {
            Objects.requireNonNull(lastOperation, "lastOperation cannot be null");
        }
    }

    /**
     * Returns the operation to render for the given state.
     *
     * <p>Concealment is chosen when no contiguous packet is available: comfort noise for an empty buffer in
     * a discontinuous-transmission gap, codec concealment or built-in expansion otherwise. With a
     * contiguous packet available, the warm-up forces a normal decode, a concealment run is closed with a
     * merge, and the buffered span against the low and high decision limits selects normal decode, accelerate
     * (or fast-accelerate), or preemptive expand, subject to the time-stretch gates.
     *
     * @param input the state for this get period; never {@code null}
     * @return the chosen operation; never {@code null} and never {@link NetEqOperation#UNDEFINED}
     * @throws NullPointerException if {@code input} is {@code null}
     */
    public NetEqOperation decide(Input input) {
        Objects.requireNonNull(input, "input cannot be null");
        if (!input.nextPacketAvailable() || !input.nextPacketContiguous()) {
            if (!input.nextPacketAvailable() && input.comfortNoiseActive()) {
                return NetEqOperation.RFC3389_CNG;
            }
            if (input.codecHasPlc() && config.enableCodecPlc()) {
                return NetEqOperation.CODEC_PLC;
            }
            return NetEqOperation.EXPAND;
        }
        if (decodesPerformed < config.numInitialPackets()) {
            decodesPerformed++;
            return NetEqOperation.NORMAL;
        }
        if (isConcealment(input.lastOperation())) {
            return NetEqOperation.MERGE;
        }
        var target = Math.max(input.targetLevelMillis(), 1);
        var span = input.bufferSpanMillis();
        var lowLimit = target * config.audioJitbufBufferLowerLimitScalePercent() / PERCENT;
        var highLimit = Math.max(target, lowLimit + config.audioJitbufBufferLimitsWindowSizeMs())
                + config.highThresholdOffsetMs();
        if (config.useMaxDelayInHighThreshold()) {
            highLimit = Math.max(highLimit, config.maxDelayMs());
        }
        if (!timeStretchAllowed(span)) {
            return NetEqOperation.NORMAL;
        }
        if (span >= highLimit) {
            if (!config.allowTimeStretchAcceleration()) {
                return NetEqOperation.NORMAL;
            }
            return span >= highLimit * FAST_ACCELERATE_HIGH_LIMIT_MULTIPLE
                    ? NetEqOperation.FAST_ACCELERATE
                    : NetEqOperation.ACCELERATE;
        }
        if (span < lowLimit) {
            return NetEqOperation.PREEMPTIVE_EXPAND;
        }
        return NetEqOperation.NORMAL;
    }

    /**
     * Returns whether time-stretch is permitted for the given buffered span under the high-latency gate.
     *
     * <p>When {@link NetEqConfig#allowTimeStretchForHighLatency()} is set, time-stretch is permitted only
     * once the buffered span exceeds {@link NetEqConfig#allowTimeStretchThresholdMs()}, so a near-empty
     * buffer is never compressed or stretched. When the gate is clear, time-stretch is always permitted.
     *
     * @param bufferSpanMillis the buffered playout duration in milliseconds
     * @return {@code true} if accelerate and preemptive expand may be chosen for this span
     */
    private boolean timeStretchAllowed(int bufferSpanMillis) {
        if (!config.allowTimeStretchForHighLatency()) {
            return true;
        }
        return bufferSpanMillis > config.allowTimeStretchThresholdMs();
    }

    /**
     * Resets the warm-up counter so the next decodes refill the buffer before adaptive time-stretching.
     *
     * <p>Used when the stream is reconfigured or the buffer is flushed.
     */
    public void reset() {
        decodesPerformed = 0;
    }

    /**
     * Returns whether the given operation is a concealment operation that a merge must follow.
     *
     * @param operation the operation to classify
     * @return {@code true} if the operation concealed a gap
     */
    private static boolean isConcealment(NetEqOperation operation) {
        return operation == NetEqOperation.EXPAND
                || operation == NetEqOperation.CODEC_PLC
                || operation == NetEqOperation.RFC3389_CNG
                || operation == NetEqOperation.CODEC_INTERNAL_CNG;
    }
}
