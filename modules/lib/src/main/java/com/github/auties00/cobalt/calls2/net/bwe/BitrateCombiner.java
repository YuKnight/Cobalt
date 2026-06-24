package com.github.auties00.cobalt.calls2.net.bwe;

/**
 * Fuses the sender-side estimate, the remote receiver estimate, the packet-pair link capacity, and
 * optional machine-learning estimates into one combined target bitrate.
 *
 * <p>{@link #combine(long, long, long, long)} selects a base fusion of the sender and remote estimates
 * by the configured {@link CombineMode}, then runs the second-stage remote-picture-in-picture combine
 * when {@link CombineMode#REMOTE_PIP} is active. The {@link RemotePipPhase} starts in
 * {@link RemotePipPhase#ENTER} and converts to {@link RemotePipPhase#THROTTLE} once
 * {@link #PHASE_TIME_THRESHOLD_MS} has elapsed or the remote estimate reaches
 * {@link #REMOTE_PIP_ENTER_BPS}. The combiner tracks a receive-drop condition (the sender estimate
 * falling below the remote estimate, or the two converging within {@link #RECV_DROP_DELTA_FRACTION})
 * and exposes it so the {@link BweHoldController} can freeze the target, and it blends the result
 * toward the sender estimate with an exponential moving average when the inflection latch is set under
 * {@link CombineMode#EMA_BLEND}.
 *
 * <p>Instances are not thread-safe; the owning sender estimator drives one combiner from the single
 * transport thread.
 *
 * @implNote This implementation ports {@code tfrc_combine_bitrate_estimates} (fn4364) from the wa-voip
 * engine ({@code bwe/tfrc_sender_bwe_bitrate_update.cc}). The constants are the recovered literals:
 * {@value #REMOTE_PIP_ENTER_BPS} bps ({@code 0x30d41}) remote-pip enter, {@value #PHASE_TIME_THRESHOLD_MS}
 * ms phase threshold, {@value #EMA_WINDOW_MS} ms ({@code 0x752f}) exponential-moving-average / staleness
 * window, and the five-percent receive-drop delta. The active-probing increase and machine-learning
 * inputs are folded in by the caller via {@link #applyProbingIncrease(long, long)}; the random forced
 * probing path is gated on a caller-supplied draw (re/calls2-spec/SPEC.md sec 15.3).
 */
public final class BitrateCombiner {
    /**
     * Remote-estimate threshold, in bits per second, at or above which the remote-pip phase converts
     * to throttle and the gated-average mode engages.
     *
     * <p>The recovered literal {@code 0x30d41}.
     */
    static final long REMOTE_PIP_ENTER_BPS = 200_001;

    /**
     * Elapsed time, in milliseconds, after which the remote-pip phase converts from enter to throttle.
     */
    static final long PHASE_TIME_THRESHOLD_MS = 7_500;

    /**
     * Window, in milliseconds, for the exponential-moving-average blend and the staleness gate.
     *
     * <p>The recovered literal {@code 0x752f}.
     */
    static final long EMA_WINDOW_MS = 30_000;

    /**
     * Fraction of the current estimate within which the sender and remote estimates are treated as
     * converged, arming the receive-drop condition.
     */
    static final double RECV_DROP_DELTA_FRACTION = 0.05;

    /**
     * Smoothing factor for the exponential-moving-average blend toward the sender estimate.
     *
     * <p>The blend is {@code result = combined * (1 - w) + w * sender}; this constant is {@code w}.
     *
     * @implNote This implementation uses the compiled voip-param default {@code 0.5}, recovered from the
     * param-defaults applier {@code fn11831} ({@code voip_param_internal.cc}). That applier assigns its
     * config base {@code p} exactly once ({@code local.get 6; i32.const 12668; i32.add; local.tee 3} at
     * instruction index 11) and then writes every default through that single base, so all of its
     * {@code local.get 3} stores belong to one struct. Among them, instruction index 149048 stores
     * {@code i32.const 1056964608} (the IEEE-754 bit pattern of {@code 0.5f}) with {@code i32.store
     * {off=0xb4}} at index 149049, placing {@code p+0xb4 = 0.5f}. The reader is
     * {@code tfrc_combine_bitrate_estimates} ({@code fn4364}, {@code bwe/tfrc_sender_bwe_bitrate_update.cc}):
     * it takes its config as {@code cfg = *(int*)(param1 + 4)} and, on the {@code combine_policy == 6}
     * path, computes {@code combined * (1 - *(float*)(cfg + 0xb4)) + *(float*)(cfg + 0xb4) * sender}. That
     * {@code cfg} is the same {@code p}: {@code fn4364} also reads {@code *(int*)(cfg + 0xf4c)}, and
     * {@code fn11831} defaults {@code p+0xf4c = 1} through the same single {@code local.get 3} base
     * (index 452539). The selector at {@code p+0xac} defaults to {@code 5} (index 148238) and the adjacent
     * {@code p+0xb0} (the {@code dl_bwe_combine_policy}) defaults to {@code 5} (index 148641); the live
     * {@code voip_settings} capture overrides these to {@code bwe.sbwe_combine_policy=3} and
     * {@code vid_rc.dl_bwe_combine_policy=4} (re/calls2-spec/captures/voip-settings-merged.json) but
     * carries no {@code +0xb4} key, so the {@code 0.5} compiled default is the operative blend weight.
     */
    static final double EMA_BLEND_COEFFICIENT = 0.5;

    /**
     * The configured fusion strategy.
     */
    private final CombineMode mode;

    /**
     * Current remote-picture-in-picture phase.
     */
    private RemotePipPhase remotePipPhase = RemotePipPhase.ENTER;

    /**
     * Timestamp, in milliseconds, at which the current remote-pip phase began, or {@code -1} when no
     * combine has run.
     */
    private long phaseStartMs = -1;

    /**
     * Whether the sender-versus-remote inflection latch is set.
     *
     * <p>Set once the sender estimate has risen to meet or exceed the remote estimate; gates the
     * floor in {@link CombineMode#MIN_FLOOR} and the blend in {@link CombineMode#EMA_BLEND}.
     */
    private boolean inflectionLatched = false;

    /**
     * Whether the most recent combine observed the receive-drop condition.
     */
    private boolean recvDropDetected = false;

    /**
     * Last combined output, in bits per second, used as the prior for the exponential-moving-average
     * blend.
     */
    private long lastCombinedBps = 0;

    /**
     * Constructs a combiner with the given fusion strategy.
     *
     * @param mode the fusion strategy; never {@code null}
     * @throws NullPointerException if {@code mode} is {@code null}
     */
    public BitrateCombiner(CombineMode mode) {
        if (mode == null) {
            throw new NullPointerException("mode");
        }
        this.mode = mode;
    }

    /**
     * Fuses the sender and remote estimates into one combined target.
     *
     * <p>Computes the base fusion for the configured {@link CombineMode}, runs the remote-pip second
     * stage when that mode is active (advancing the {@link RemotePipPhase}), updates the inflection
     * latch and the receive-drop condition, applies the exponential-moving-average blend when the
     * blend mode is latched, and stores the result as the prior for the next call.
     *
     * @param senderBweBps    the sender-side estimate, in bits per second
     * @param remoteBweBps    the remote receiver estimate, in bits per second; {@code 0} when none has
     *                        arrived
     * @param linkCapacityBps the packet-pair link-capacity estimate, in bits per second; {@code 0} when
     *                        none is available
     * @param nowMs           the monotonic timestamp, in milliseconds, at which this combine runs
     * @return the combined target, in bits per second
     */
    public long combine(long senderBweBps, long remoteBweBps, long linkCapacityBps, long nowMs) {
        if (phaseStartMs < 0) {
            phaseStartMs = nowMs;
        }
        updateInflectionLatch(senderBweBps, remoteBweBps);
        updateRecvDrop(senderBweBps, remoteBweBps);

        var base = baseCombine(senderBweBps, remoteBweBps);
        if (mode == CombineMode.REMOTE_PIP) {
            base = remotePipCombine(base, remoteBweBps, linkCapacityBps, nowMs);
        }
        if (mode == CombineMode.EMA_BLEND && inflectionLatched && lastCombinedBps > 0) {
            base = (long) ((1.0 - EMA_BLEND_COEFFICIENT) * base + EMA_BLEND_COEFFICIENT * senderBweBps);
        }
        lastCombinedBps = base;
        return base;
    }

    /**
     * Computes the base fusion of the sender and remote estimates for the configured mode.
     *
     * <p>{@link CombineMode#MIN}, {@link CombineMode#MAX}, and {@link CombineMode#AVG} are the plain
     * fusions. {@link CombineMode#MIN_FLOOR} takes the minimum but never drops below the sender
     * estimate once the inflection has latched. {@link CombineMode#AVG_GATED} averages only when the
     * remote estimate has reached {@link #REMOTE_PIP_ENTER_BPS} and no inflection has latched.
     * {@link CombineMode#EMA_BLEND} uses the sender estimate as the base before the caller's blend.
     * {@link CombineMode#REMOTE_PIP} passes the sender estimate through to its dedicated second stage.
     * {@link CombineMode#EARLY_CONGESTION} also passes the sender estimate through, because its faithful
     * receiver-trusted second stage is not yet implemented (see the TODO below); the live configuration
     * selects {@link CombineMode#MIN_FLOOR}, so this arm is latent.
     *
     * @param senderBweBps the sender-side estimate, in bits per second
     * @param remoteBweBps the remote receiver estimate, in bits per second
     * @return the base combined value, in bits per second
     */
    // TODO: implement the CombineMode.EARLY_CONGESTION (mode 7) receiver-trusted second stage of
    //  tfrc_combine_bitrate_estimates (fn4364, bwe/tfrc_sender_bwe_bitrate_update.cc). Mode 7 manages the
    //  inflection latch (+0x240) from the enable flags +0xf4c/+0xf54 (logging "early congestion detect:
    //  receiver trusted, recv=%d send=%u"), selects toward the remote estimate when early congestion is
    //  latched, and floors at the packet-pair link capacity (+0xd0) otherwise; the rest of the block is
    //  the active/random/mid-call probing path gated on +0xb8/+0xbc/+0xc0/+0xc4/+0xc8 with probe counters
    //  +0x244/+0x24c/+0x250 and a forced-probe RNG. That probing state and those config offsets are not
    //  modeled on BitrateCombiner, so a faithful port needs the probing controller (wa_one_side_bwe) and
    //  the extra config first; until then EARLY_CONGESTION passes the sender estimate through (the live
    //  config selects MIN_FLOOR, so this is latent). SPEC 15.3.
    private long baseCombine(long senderBweBps, long remoteBweBps) {
        return switch (mode) {
            case MIN -> Math.min(senderBweBps, remoteBweBps == 0 ? senderBweBps : remoteBweBps);
            case MAX -> Math.max(senderBweBps, remoteBweBps);
            case AVG -> remoteBweBps == 0 ? senderBweBps : (senderBweBps + remoteBweBps) / 2;
            case MIN_FLOOR -> minFloorCombine(senderBweBps, remoteBweBps);
            case AVG_GATED -> remoteBweBps >= REMOTE_PIP_ENTER_BPS && !inflectionLatched
                    ? (senderBweBps + remoteBweBps) / 2
                    : senderBweBps;
            case EMA_BLEND, EARLY_CONGESTION, REMOTE_PIP -> senderBweBps;
        };
    }

    /**
     * Computes the minimum-with-floor fusion.
     *
     * <p>Returns the minimum of the sender and remote estimates, except that once the inflection has
     * latched the result is floored at the sender estimate so a transient dip in the remote estimate
     * cannot pull the combined value back down.
     *
     * @param senderBweBps the sender-side estimate, in bits per second
     * @param remoteBweBps the remote receiver estimate, in bits per second
     * @return the floored minimum, in bits per second
     */
    private long minFloorCombine(long senderBweBps, long remoteBweBps) {
        if (remoteBweBps == 0) {
            return senderBweBps;
        }
        var min = Math.min(senderBweBps, remoteBweBps);
        return inflectionLatched ? Math.max(min, senderBweBps) : min;
    }

    /**
     * Runs the remote-picture-in-picture second-stage combine and advances its phase.
     *
     * <p>While in {@link RemotePipPhase#ENTER} the combined value rises toward the maximum of the base,
     * the remote estimate, and the link capacity, letting the estimate ramp; the phase converts to
     * {@link RemotePipPhase#THROTTLE} once {@link #PHASE_TIME_THRESHOLD_MS} has elapsed or the remote
     * estimate reaches {@link #REMOTE_PIP_ENTER_BPS}. In {@link RemotePipPhase#THROTTLE} the combined
     * value averages the base with the link capacity for a steadier target.
     *
     * @param base            the base combined value, in bits per second
     * @param remoteBweBps    the remote receiver estimate, in bits per second
     * @param linkCapacityBps the packet-pair link-capacity estimate, in bits per second
     * @param nowMs           the monotonic timestamp, in milliseconds
     * @return the second-stage combined value, in bits per second
     */
    private long remotePipCombine(long base, long remoteBweBps, long linkCapacityBps, long nowMs) {
        if (remotePipPhase == RemotePipPhase.ENTER) {
            var elapsed = nowMs - phaseStartMs;
            if (elapsed >= PHASE_TIME_THRESHOLD_MS || remoteBweBps >= REMOTE_PIP_ENTER_BPS) {
                remotePipPhase = RemotePipPhase.THROTTLE;
                phaseStartMs = nowMs;
            }
        }
        if (remotePipPhase == RemotePipPhase.ENTER) {
            var ceiling = Math.max(base, Math.max(remoteBweBps, linkCapacityBps));
            return ceiling > 0 ? ceiling : base;
        }
        return linkCapacityBps > 0 ? (base + linkCapacityBps) / 2 : base;
    }

    /**
     * Updates the sender-versus-remote inflection latch.
     *
     * <p>The latch sets once the sender estimate rises to meet or exceed a non-zero remote estimate,
     * marking that the sender path has caught up to the receiver, and it stays set for the life of the
     * combiner.
     *
     * @param senderBweBps the sender-side estimate, in bits per second
     * @param remoteBweBps the remote receiver estimate, in bits per second
     */
    private void updateInflectionLatch(long senderBweBps, long remoteBweBps) {
        if (!inflectionLatched && remoteBweBps > 0 && senderBweBps >= remoteBweBps) {
            inflectionLatched = true;
        }
    }

    /**
     * Updates the receive-drop condition for this combine.
     *
     * <p>The condition is set when the sender estimate has fallen below a non-zero remote estimate, or
     * when the two are within {@link #RECV_DROP_DELTA_FRACTION} of the current sender estimate, meaning
     * the sender path is at or below what the receiver reports and the combined target should be frozen
     * rather than ramped.
     *
     * @param senderBweBps the sender-side estimate, in bits per second
     * @param remoteBweBps the remote receiver estimate, in bits per second
     */
    private void updateRecvDrop(long senderBweBps, long remoteBweBps) {
        if (remoteBweBps == 0 || senderBweBps == 0) {
            recvDropDetected = false;
            return;
        }
        var delta = Math.abs(senderBweBps - remoteBweBps);
        recvDropDetected = senderBweBps < remoteBweBps
                || delta < senderBweBps * RECV_DROP_DELTA_FRACTION;
    }

    /**
     * Applies an active-probing additive increase to a combined target.
     *
     * <p>The increase is {@code additivePercent} percent of the supplied video bitrate added to the
     * target, the form the recovered combiner uses for forced and random probing. The caller decides
     * whether probing is active (including the random forced-probe draw) and supplies the percent and
     * the video bitrate.
     *
     * @param targetBps        the combined target before the increase, in bits per second
     * @param additiveBitsBps  the additive increase, in bits per second, already scaled to the probe
     *                         percent and video bitrate by the caller
     * @return the target with the probing increase applied, in bits per second
     */
    public long applyProbingIncrease(long targetBps, long additiveBitsBps) {
        if (additiveBitsBps <= 0) {
            return targetBps;
        }
        return targetBps + additiveBitsBps;
    }

    /**
     * Returns whether the most recent combine observed the receive-drop condition.
     *
     * @return {@code true} when the sender estimate is at or below the remote estimate within the
     *         receive-drop band
     */
    public boolean recvDropDetected() {
        return recvDropDetected;
    }

    /**
     * Returns whether the sender-versus-remote inflection latch is set.
     *
     * @return {@code true} once the sender estimate has met or exceeded the remote estimate
     */
    public boolean inflectionLatched() {
        return inflectionLatched;
    }

    /**
     * Returns the current remote-picture-in-picture phase.
     *
     * @return the phase, {@link RemotePipPhase#ENTER} until it converts to
     *         {@link RemotePipPhase#THROTTLE}
     */
    public RemotePipPhase remotePipPhase() {
        return remotePipPhase;
    }

    /**
     * Returns the last combined output.
     *
     * @return the last combined target, in bits per second, or {@code 0} before the first combine
     */
    public long lastCombinedBps() {
        return lastCombinedBps;
    }

    /**
     * Resets the combiner to its initial phase and clears the latches and prior.
     */
    public void reset() {
        remotePipPhase = RemotePipPhase.ENTER;
        phaseStartMs = -1;
        inflectionLatched = false;
        recvDropDetected = false;
        lastCombinedBps = 0;
    }
}
